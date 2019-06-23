package sparrow.account.model

import com.twitter.util.Future

trait AccountOperation {
  def balanceAccount(uuid: String): Future[Option[Any]]
  def createAccount(uuid: String, amount: Double): Future[Either[Exception, Any]]
  def fillAccount(uuid: String, amount: Double): Future[Either[Exception, Any]]
}

final case class AccountTransaction(uuid: String, amount: Double)
final case class AccountFillRequest(uuid: String, amount: Double)