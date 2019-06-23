package sparrow.account.unit

import org.scalatest.{FlatSpec, Matchers}
import sparrow.account.Routes

class RoutesSpec extends FlatSpec with Matchers {

  it should "balance endpoint route test" in {
    Routes.balanceAccount.toString should startWith ("GET")
    Routes.balanceAccount.toString should include regex "/balance"
  }

  it should "fill account endpoint route test" in {
    Routes.fillAccount.toString should startWith ("POST")
    Routes.fillAccount.toString should include regex "/account"
  }
}
