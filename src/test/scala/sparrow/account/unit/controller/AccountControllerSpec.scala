package sparrow.account.unit.controller

import org.scalatest.{FlatSpec, BeforeAndAfter, Matchers}
import com.twitter.util.{Return, Throw, Future => TwitterFuture}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import sparrow.account.controller.AccountController
import sparrow.account.interceptors.AccountError

class AccountControllerSpec extends FlatSpec with Matchers with BeforeAndAfter with AccountError {
  implicit val ec = ExecutionContext.global
  private[this] var controller = AccountController()
  private[this] val accountName = "jack.sparrow"
  private[this] val accountName2 = "black.beard"

  def fromTwitter[A](twitterFuture: TwitterFuture[A]): Future[A] = {
    val promise = Promise[A]()
    twitterFuture respond {
      case Return(a) => promise success a
      case Throw(e) => promise failure e
    }
    promise.future
  }

  it should "creating a new account" in {
    controller.createAccount(accountName, 0.0)
    val r = Await.result(fromTwitter(controller.balanceAccount(accountName)), 2 seconds).get
    r.uuid should be (accountName)
    r.amount should be (0.0)
  }

  it should "creating a new account that already exists" in {
    controller.createAccount(accountName, 1.0)
    val r = Await.result(fromTwitter(controller.createAccount(accountName, 1.0)), 2 seconds)
    r match {
      case Left(_) => true
      //case Left(_: AccountCreateException) => true
      case _ => fail()
    }
  }

  it should "trying to create an account with negative balance" in {
    controller.createAccount(accountName, -100.93)
    val r = Await.result(fromTwitter(controller.balanceAccount(accountName)), 2 seconds).get
    r.uuid should be (accountName)
    r.amount should be (0.0)
  }

  it should "deposit fill money into account and check the balance" in {
    controller.createAccount(accountName, 0.0)
    controller.fillAccount(accountName, 10.99)
    val r = Await.result(fromTwitter(controller.balanceAccount(accountName)), 2 seconds).get
    r.uuid should be (accountName)
    r.amount should be (10.99)
  }

  it should "try to withdraw money without enough balance" in {
    controller.createAccount(accountName, 1000.0)
    val r = Await.result(fromTwitter(controller.balanceAccount(accountName)), 2 seconds).get
    r.amount should be (1000.0)

    controller.fillAccount(accountName, -1001.0)

    val r1 = Await.result(fromTwitter(controller.balanceAccount(accountName)), 2 seconds).get
    r1.amount should be (1000.0)

    controller.fillAccount(accountName, -1000.99)

    val r2 = Await.result(fromTwitter(controller.balanceAccount(accountName)), 2 seconds).get
    r2.amount should be (1000.0)
  }

  it should "it should not be possible to create a negative account" in {
    Await.result(fromTwitter(controller.fillAccount(accountName, -20.0)), 2 seconds) match {
      case Right(_) => assert(true)
      //case Left(_: AccountFillException) => fail()
      case _ => fail()
    }

    val r = Await.result(fromTwitter(controller.balanceAccount(accountName)), 2 seconds).get
    r.amount should be(0.0)
  }

  it should "deposit fill money and withdraw money of account" in {
    controller.createAccount(accountName, 0.0)
    controller.fillAccount(accountName, 500.95)
    val deposit1 = Await.result(fromTwitter(controller.balanceAccount(accountName)), 2 seconds).get
    deposit1.uuid should be (accountName)
    deposit1.amount should be (500.95)

    controller.fillAccount(accountName, -100.00)
    val withdraw1 = Await.result(fromTwitter(controller.balanceAccount(accountName)), 2 seconds).get
    withdraw1.uuid should be (accountName)
    withdraw1.amount should be (400.95)

    controller.fillAccount(accountName, -200)
    val withdraw2 = Await.result(fromTwitter(controller.balanceAccount(accountName)), 2 seconds).get
    withdraw2.uuid should be (accountName)
    withdraw2.amount should be (200.95)

    controller.fillAccount(accountName, -0.95)
    val withdraw3 = Await.result(fromTwitter(controller.balanceAccount(accountName)), 2 seconds).get
    withdraw3.uuid should be (accountName)
    withdraw3.amount should be (200.0)

    controller.fillAccount(accountName, -500)
    val withdraw4 = Await.result(fromTwitter(controller.balanceAccount(accountName)), 2 seconds).get
    withdraw4.uuid should be (accountName)
    withdraw4.amount should be (200.0)
  }

  it should "concurrence stress testing first round" in {
    controller.createAccount(accountName, 0.0)
    controller.createAccount(accountName2, 0.0)

    val trx1 = (0 to 1000).map(x => Future{controller.fillAccount(accountName, 1000.99)})
    val trx2 = (0 to 1000).map(x => Future{controller.fillAccount(accountName2, 1.99)})
    val trxSigma = trx1 ++ trx2

    Await.result(Future.sequence(trxSigma), 10 seconds)

    val acc1 = Await.result(fromTwitter(controller.balanceAccount(accountName)), 2 seconds).get
    val acc2 = Await.result(fromTwitter(controller.balanceAccount(accountName2)), 2 seconds).get
    
    acc1.amount + acc2.amount should be > 1003982.0
  }

  it should "concurrence stress testing second round" in {
    controller.createAccount(accountName, 10.0)
    controller.createAccount(accountName2, 10.0)

    val trx1 = (0 to 2000).map(x => Future{controller.fillAccount(accountName, 1000.00)})
    val trx2 = (0 to 2000).map(x => Future{controller.fillAccount(accountName2, 1000.00)})
    val trxSigma = trx1 ++ trx2

    Await.result(Future.sequence(trxSigma), 10 seconds)

    val acc1 = Await.result(fromTwitter(controller.balanceAccount(accountName)), 2 seconds).get
    val acc2 = Await.result(fromTwitter(controller.balanceAccount(accountName2)), 2 seconds).get

    acc1.amount + acc2.amount should be (4002020.0)
  }

  before {
    controller = AccountController()
  }
}
