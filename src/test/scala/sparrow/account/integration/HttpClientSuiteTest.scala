package sparrow.account.integration

import org.scalatest.{FlatSpec, Matchers}
import util.Random.nextInt
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Method, Request, Version}
import com.twitter.util.{Await, Future}
import com.typesafe.config.ConfigFactory
import io.circe._
import io.circe.generic.semiauto.deriveDecoder
import sparrow.account.model.AccountTransaction
import sparrow.account.interceptors.AccountError

class HttpClient {
  private[this] lazy val conf = ConfigFactory.load().getConfig("sparrow.account.server")

  lazy val client = Http.client
    .withLabel("http-client-integration")
    .newService(s"${conf.getString("host")}:${conf.getInt("port")}")

  private def requestHandler(request: Request): Future[String] = client(request).map(_.contentString)

  def get(url: String): Future[String] = {
    val request = Request(Version.Http11, Method.Get, url)
    requestHandler(request)
  }

  def post(url: String, data: String): Future[String] = {
    val request = Request(Version.Http11, Method.Post, url)
    request.setContentTypeJson()
    request.setContentString(data.stripMargin)
    requestHandler(request)
  }
}

object HttpClient {
  def apply() = new HttpClient()
}

object HttpClientSuiteTest extends FlatSpec with Matchers with AccountError {
  private[this] val httpClient = HttpClient()
  lazy val randomAccount = f"${('A' to 'Z')(nextInt(26))}${nextInt(10000)}%06d"

  def accountTestValidation(uuid: String, amount: Double, jsonStr: String): Unit = {
    implicit val staffDecoder = deriveDecoder[AccountTransaction]
    val decodeResult = parser.decode[AccountTransaction](jsonStr)

    decodeResult match {
      case Right(x) => {
        x.uuid should equal (uuid)
        x.amount should be >= 0.0
      }
      case Left(e) => fail(e)
      case _ => fail()
    }
  }

  def createAccountAndFillTest(uuid: String, amount: Double): Any = {
    Await.ready(httpClient post ("/account", s"""{"uuid":"${uuid}", "amount":${amount}}""")) onSuccess {r =>
      println(s"fillAccount: ${r}")
      accountTestValidation(uuid, 0, r)
    } onFailure { e =>
      fail(e)
    }
  }

  def balanceAccountTest(uuid: String, it: Int = 0): Unit = {
    Await.ready(httpClient get s"/balance/${uuid}") onSuccess {r =>
      println(s"balanceAccount: ${r}")
      accountTestValidation(uuid, 0, r)
    } onFailure { e =>
      fail(e)
    }
  }

  def getNotFoundTest(url: String): Unit = {
    Await.ready(httpClient get url) onSuccess {r =>
      if (r.isEmpty) println("Get Result empty.") else println(r)
    } onFailure { e =>
      fail(e)
    }
  }

  def postNotFoundTest(uuid: String = "1", amount: Double = -10.0): Unit = {
    Await.ready(httpClient post("/account000", s"""{}""")) onSuccess { r =>
      if (r.isEmpty) println("Post Result empty.") else println(r)
    } onFailure { e =>
      fail(e)
    }
  }

  def main(args: Array[String]) {
    // Test with Endpoints that are invalid.
    getNotFoundTest("/balance-invalid|||||<>")
    postNotFoundTest("/account-invalid|||||<>")

    // Create an account, deposit money and then try to
    // withdraw money.
    val accountName = randomAccount
    // 1. Deposit money into account, default 0.0
    createAccountAndFillTest(accountName, 0)
    // 2. Check your account balance.
    balanceAccountTest(accountName)
    // 3. Deposit money into account (10.90).
    createAccountAndFillTest(accountName, 10.90)
    // 4. Withdraw money from account (-10.90).
    createAccountAndFillTest(accountName, -10.90)

    // Create an account with and deposit money, check the balance and
    // then make the withdrawal for account to keep the balance 0.
    val accountName1 = randomAccount
    // 1. Deposit money into account.
    for (i <- 1 to 1000) yield createAccountAndFillTest(accountName1, i * 2)
    // 2. Check your account balance.
    for (i <- 1 to 10) yield balanceAccountTest(accountName1, i)
    // 3. Withdraw money from account.
    for (i <- 1 to 1000) yield createAccountAndFillTest(accountName1, -i * 2)

    // Create an account with and deposit money, check the balance and
    // then make the withdrawal without having enough balance.
    val accountName2 = randomAccount
    // 1. Deposit money into account.
    for (i <- 1 to 1000) yield createAccountAndFillTest(accountName2, i)
    // 2. Check your account balance.
    for (i <- 1 to 10) yield balanceAccountTest(accountName2, i)
    // 3. Withdraw money from account. You do not have enough money,
    // then you get zero.
    for (i <- 1 to 1002) yield createAccountAndFillTest(accountName2, -i)
  }
}
