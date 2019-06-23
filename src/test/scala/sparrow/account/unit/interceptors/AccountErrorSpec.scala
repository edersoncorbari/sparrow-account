package sparrow.account.unit.interceptors

import org.scalatest.{FlatSpec, Matchers}
import sparrow.account.interceptors.AccountError

class AccountErrorSpec extends FlatSpec with Matchers {
  object AccountError extends AccountError

  it should "validation test in exception messages" in {
    val th1 = intercept[AccountError.AccountCreateException] {
      throw AccountError.AccountCreateException("Unable to create account")
    }
    th1.getMessage shouldEqual "Unable to create account"

    val th2 = intercept[AccountError.AccountFillException] {
      throw AccountError.AccountFillException("Unable to deposit account")
    }
    th2.getMessage shouldEqual "Unable to deposit account"

    val th3 = intercept[AccountError.AccountMoneyException] {
      throw AccountError.AccountMoneyException("The amount deposited is invalid")
    }
    th3.getMessage shouldEqual "The amount deposited is invalid"
  }
}
