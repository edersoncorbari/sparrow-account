package sparrow.account.unit.domain

import org.scalatest.{FlatSpec, Matchers}
import sparrow.account.domain.AccountValidation

class AccountValidationSpec extends FlatSpec with Matchers with AccountValidation {
  val accountName = "jack.sparrow"

  it should "if the value is negative, the zero value prevails" in {
    zeroOrGreater(0) should be(0.0)
    zeroOrGreater(0.0) should be(0)

    zeroOrGreater(1) should be(1.0)
    zeroOrGreater(-1) should be(0.0)

    zeroOrGreater(-100.10) should be(0.0)
    zeroOrGreater(-0.0) should be(0.0)
  }

  it should "checks if a amount is negative" in {
    amountIsNegative(0) shouldEqual false
    amountIsNegative(1) shouldEqual false

    amountIsNegative(10.10) shouldEqual false
    amountIsNegative(20.20) shouldEqual false

    amountIsNegative(-1) shouldEqual true
    amountIsNegative(-2) shouldEqual true

    amountIsNegative(-10.10) shouldEqual true
    amountIsNegative(-20.20) shouldEqual true
  }

  it should "checks the type operation withdrawal or deposit" in {
    displayOperationType(accountName, 2000)
    displayOperationType(accountName, 4000)

    displayOperationType(accountName, -2000)
    displayOperationType(accountName, -4000)

    displayOperationType(accountName, 0)
    displayOperationType(accountName, 0.0)

    displayOperationType(accountName, -1)
    displayOperationType(accountName, -1.0)
  }
}
