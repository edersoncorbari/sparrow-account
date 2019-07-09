package sparrow.account.controller

import com.twitter.util.Future
import scala.concurrent.stm._
import sparrow.account.domain.AccountValidation
import sparrow.account.model.{AccountOperation, AccountTransaction}
import sparrow.account.interceptors.AccountError

class AccountController extends AccountOperation with AccountValidation with AccountError {
  private[this] var accounts: Map[String, Ref[AccountTransaction]] = Map()

  override def balanceAccount(uuid: String): Future[Option[AccountTransaction]] =
    Future{accounts.get(uuid).map(_.single.get)}

  override def createAccount(uuid: String, amount: Double): Future[Either[AccountCreateException, AccountTransaction]] = Future {
      if (accounts.get(uuid).isEmpty) {
        val newAccount = Ref(AccountTransaction(uuid, amount = zeroOrGreater(amount)))
        accounts = accounts + (uuid -> newAccount)
        Right(newAccount.single())
      } else {
        Left(AccountCreateException("The account already exists."))
      }
  }

  override def fillAccount(uuid: String, amount: Double): Future[Either[AccountFillException, AccountTransaction]] = Future {
    if (accounts.get(uuid).isEmpty) createAccount(uuid, 0)

    accounts.get(uuid) match {
      case Some(transact) => {
        atomic {implicit tx =>
          transact() = AccountTransaction(transact().uuid, transact().amount + amount)

          displayOperationType(transact().uuid, transact().amount)

          if (amountIsNegative(transact().amount)) {
            transact() = AccountTransaction(transact().uuid, transact().amount - amount)
          }

          Right(transact())
        }
      }
      case _ => Left(AccountFillException("Fill account not found."))
    }
  }
}

object AccountController {
  def apply(): AccountController = new AccountController()
}
