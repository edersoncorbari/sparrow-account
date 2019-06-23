package sparrow.account

import com.twitter.server.TwitterServer
import com.twitter.finagle.Http
import com.twitter.util.Await
import com.typesafe.config.ConfigFactory
import io.circe.generic.auto._
import io.finch.circe._

object ServerApp extends TwitterServer {
  import sparrow.account.model.ConfigProperty.ServerConfig
  private[this] lazy val loadConf = ConfigFactory.load().getConfig("sparrow.account.server")
  private[this] lazy val serverConf = ServerConfig(loadConf.getString("name"),
    loadConf.getString("host"),
    loadConf.getInt("port"),
    loadConf.getInt("maxConcurrentRequests"),
    loadConf.getInt("maxWaiters"))

  def runServer(): Unit = {
    val app = Http
      .server
      .withLabel(serverConf.name)
      .withAdmissionControl.concurrencyLimit(
      maxConcurrentRequests = serverConf.maxConcurrentRequests,
      maxWaiters = serverConf.maxWaiters
    ).serve(s"${serverConf.host}:${serverConf.port}",
      (Routes.balanceAccount :+: Routes.fillAccount).toService)

    onExit {
      app.close()
    }

    Await.ready(app)
  }

  def main(): Unit = {
    val greeting = () => {s"*** Stating ${serverConf.name} ****"}
    val info = () => {s"Host: ${serverConf.host} Port: ${serverConf.port}"}
    println(greeting() + "\n" + info())

    runServer()
  }
}