package sparrow.account.unit.model

import org.scalatest.{FlatSpec, Matchers}
import com.typesafe.config.ConfigFactory
import sparrow.account.model.ConfigProperty.ServerConfig

class ConfigPropertySpec extends FlatSpec with Matchers {
  private[this] lazy val conf = ConfigFactory.load().getConfig("sparrow.account.server")

  it should "load test resource application" in {
    val conf = ConfigFactory.load()
    conf.getObject("sparrow.account.server") should have size 5
  }

  it should "test directives in conf file" in {
    val serverConf = ServerConfig(conf.getString("name"),
      conf.getString("host"),
      conf.getInt("port"),
      conf.getInt("maxConcurrentRequests"),
      conf.getInt("maxWaiters"))

    serverConf.name shouldEqual "Jack Sparrow HTTP Server"
    serverConf.host shouldEqual "0.0.0.0"
    serverConf.port shouldEqual 8080
    serverConf.maxConcurrentRequests shouldEqual 400
    serverConf.maxWaiters shouldEqual 100
  }

}
