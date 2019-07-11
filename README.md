# Sparrow Account 0.1 [![Build Status](https://travis-ci.org/edersoncorbari/sparrow-account.svg?branch=master)](https://travis-ci.org/edersoncorbari/sparrow-account)

![](https://raw.githubusercontent.com/edersoncorbari/sparrow-account/master/doc/img/jack-sparrow.png)
“Not all treasure is silver and gold, mate.”
## A small micro-service developed in Scala using Hexagonal Architecture

This article was published in:

 * [https://dzone.com/articles/a-small-micro-service-developed-in-scala-using-hex](https://dzone.com/articles/a-small-micro-service-developed-in-scala-using-hex)

This is a fictional project used for studies in my laboratory using **Scala** Language, with a focus on micro-services.

**Project Owner Requirements:**

Create a micro-service that checks and creates a hypothetical bank account.

It must comprise a **HTTP** Server with two endpoints:

  - One to insert a new monetary transaction, money in or out, for a given user;
  - One to return a user's current balance.

Requirements:

  - It must not be possible to withdraw money for a given user when they don't have enough balance;
  - You should take concurrency issues into consideration;
  - Store data in memory;
  - You should pay homage to Jack Sparrow.

Designer partners and good coding practices:

  - Immutability;
  - Separation of concerns;
  - Unit and integration tests;
  - API design;
  - Error handling;
  - Language idiomatic use;
  - Use functional programming paradigm.

## 1 Proposed Solution

The architecture of the proposed solution follows the **Hexagonal Architecture** concept. The design is based on two books:

  - [Functional Programming Patterns in Scala and Clojure](https://www.amazon.com/dp/B00HUEG8KK)
  - [Scala Design Patterns](https://www.amazon.com/dp/B075Z2CMRX)
 
Example Diagram of a Hexagonal Architecture:

![](https://raw.githubusercontent.com/edersoncorbari/sparrow-account/master/doc/img/ports-and-adapters.png)

Organization Application Package Diagram:

![](https://raw.githubusercontent.com/edersoncorbari/sparrow-account/master/doc/img/sparrow-account-pkg.png)

Organization of Project Directories:

| Directory | Comments |
| ------ | ------ | 
| src/main/scala/sparrow/account/**domain** | Contains class models and business logic |
| src/main/scala/sparrow/account/**model** | Contains template class and interface | 
| src/main/scala/sparrow/account/**interceptors** | Classes with templates with errors and exceptions |
| src/main/scala/sparrow/account/**controller** | Account control services |
|src/test/scala/sparrow/account/**unit** | The unit testing of the application |
|src/test/scala/sparrow/account/**integration** | Client http application for integrated testing |

The language used to develop the challenge was **Scala**. Using the following technologies:

#### 1.1 Http Rest Server

To build a request and response Http Rest Server, **Finagle-Finch** was used:

  - [https://twitter.github.io/finagle/](https://twitter.github.io/finagle/)
  - [https://finagle.github.io/finch/](https://finagle.github.io/finch/)

Piece of code where the server is used: [src/main/scala/sparrow/account/ServerApp.scala](https://github.com/edersoncorbari/sparrow-account/blob/master/src/main/scala/sparrow/account/ServerApp.scala)
```scala
def run(): Unit = {
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
```

The EndPoints available on the server:

| Method | EndPoint | Example Parameter|
| ------ | ------ | ------ |
| POST | /account | *{"uuid":"1", "amount":100.50}* |
| GET | /balance/<uuid> | *not required* |

Piece of code of the routes with the endpoints: [src/main/scala/sparrow/account/Routes.scala](https://github.com/edersoncorbari/sparrow-account/blob/master/src/main/scala/sparrow/account/Routes.scala)
```scala
final val fillAccount: Endpoint[Account] =
  post("account" :: jsonBody[AccountFillRequest]) {req: AccountFillRequest =>
    for {
      r <- accountService.fillAccount(req.uuid, req.amount)
    } yield r match {
      case Right(a) => Ok(a)
      case Left(m) => BadRequest(m)
    }
  }
```

There are two endpoints (**fillAccount**) that can create an account and deposit a value, as well as can withdraw using the negative value. And the endpoint (**balanceAccount**) to see the balance available to the user.

#### 1.2 Transactional Memory and Concurrency Control

The **ScalaSTM** was used to store the data in memory and control of the concurrency.

  - [https://nbronson.github.io/scala-stm/](https://nbronson.github.io/scala-stm/)
  
Piece of code where atomicity is used: [src/main/scala/sparrow/account/controller/AccountController.scala](https://github.com/edersoncorbari/sparrow-account/blob/master/src/main/scala/sparrow/account/controller/AccountController.scala)
```scala
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
```

#### 1.3 Other Tools Used

Other tools used in the project are in the order below:

  - [https://github.com/lightbend/config](https://github.com/lightbend/config) - Configuration library
  - [http://www.scalatest.org/](http://www.scalatest.org/) - Tests

## 2 Requirements

First, have the project folder unzipped on your machine and the terminal open.

### 2.1 Docker

A **Docker** image has been generated that can be used for testing, so it is necessary to have the docker installed on your machine. 

### 2.2 Create Docker

Simply run the script:

```sh
$ ./scripts/create-docker.sh
```

> Note: Please install docker in your Linux distribution.

With docker already installed on your machine, list docker images using the command:

```sh
$ docker images
```

Start docker with the image to do the tests:

```sh
$ docker run -p 8080:8080 sparrow-account:v0.1
```

Docker will run the unit tests and then start the server. Wait until the server loads the message:

> *** Stating Jack Sparrow HTTP Server ****
> Host: 0.0.0.0 Port: 8080

You can do a quick test on the project source code by running the curl scripts.

```sh
$ cd sparrow-account
$ ./scripts/curl-test.sh
```

> Note: See more command curl information in item 3.3, to run the integrated tests, see item 3.4. More information on removing and stopping the docker image see item 4.

### 2.2 Local SBT and Java

To build the project on the machine it is necessary to have the programs installed:

  - [JDK-1.8.0](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) - Java Development Kit
  - [SBT-1.2.8](https://www.scala-sbt.org/) - The interactive build tool

> Note: The programs above are with the version used to develop this solution.

## 3 Installation

The following procedures were using a Linux machine running on Ubuntu-64 18.04.2 LTS.

#### 3.1 Install JDK

For JDK installation:

```sh
$ tar -xvzf jdk-8u212-linux-x64.tar.gz
$ sudo mv jdk-8xxx /opt/jdk1.8.0
$ export JAVA_HOME=/opt/jdk1.8.0
$ export PATH=/opt/jdk1.8.0/bin:${PATH}
```

#### 3.1 Install SBT

For SBT installation:

```sh
$ wget https://piccolo.link/sbt-1.2.8.tgz
$ tar xfv sbt-1.2.8.tgz
$ sudo mv sbt /opt/sbt
$ export PATH=/opt/sbt/bin:${PATH}
```

> Note: You can also use OpenJDK, find out how to install OpenJDK in your distribution.

### 3.2 Building and Testing Sparrow-Account Application

To build the application just run:

```sh
$ git clone https://github.com/edersoncorbari/sparrow-account.git
$ sbt compile; sbt test; sbt run
```

You can also run unit tests like this:

```sh
$ sbt clean coverage test coverageReport
```

Checking the [ScalaStyle](http://www.scalastyle.org/) code:

```sh
$ sbt scalastyle
```

When you receive the message on the terminal after the *sbt run*:

> *** Stating Jack Sparrow HTTP Server ****
> Host: 0.0.0.0 Port: 8080

On another terminal run the command:

```sh
$ cd sparrow-account
$ ./scripts/curl-test.sh
```

This command uses the curl to make the API endpoint requests. The command create an account for the user *jack.sparrow* and make the deposit in the amount of *1000.0*. Then checks the balance, and then withdraw the value again and the account gets value zero amount.

### 3.3 More Test Commands

Creating an account manually via curl: 

```sh
$ curl -i -H "Content-Type: application/json" -X POST -d '{"uuid":"1", "amount":10.95}' http://127.0.0.1:8080/account
```

**The answer should be:**

> {"uuid":"1","amount":10.95}

Depositing more than one account:

```sh
$ curl -i -H "Content-Type: application/json" -X POST -d '{"uuid":"1", "amount":51.99}' http://127.0.0.1:8080/account
```

**The answer should be:**

> {"uuid":"1","amount":62.94}

Withdraw account amount:

```sh
$ curl -i -H "Content-Type: application/json" -X POST -d '{"uuid":"1", "amount":-30.50}' http://127.0.0.1:8080/account
```

**The answer should be:**

> {"uuid":"1","amount":32.44}

Withdrawing value where a with no sufficient value in the account:

```sh
$ curl -i -H "Content-Type: application/json" -X POST -d '{"uuid":"1", "amount":-300.0}' http://127.0.0.1:8080/account
```

**The answer should be:**

> {"uuid":"1","amount":32.44}

To check your account balance simply use:

```sh
$ curl -i -H "Content-Type: application/json" -X GET http://127.0.0.1:8080/balance/1
```

**The answer should be:**

> {"uuid":"1","amount":32.44}

If necessary, you can change the host and port configuration of the server. This can be checked at:  [src/main/resources/application.conf](https://github.com/edersoncorbari/sparrow-account/blob/master/src/main/resources/application.conf)

### 3.4 Integration Test

A small HTTP client server was created to do the integrated test. To run the server it is necessary to compile the code via SBT and it is necessary that the server is running via **local** compilation or in the **docker**.

In the project root directory run:

```sh
$ sbt "test:runMain sparrow.account.integration.HttpClientSuiteTest"
```
## 4 Docker commands

Stop the docker image:

```sh
$ docker container ls
$ docker stop <CONTAINER-ID>
```

Remove the docker image:

```sh
$ docker images
$ docker rmi -f <IMAGE-ID>
```

> Note: This application was developed using a *FreeBSD 11.2* machine and using a virtual machine with *Ubuntu 18.04.2 LTS* for testing.

**Enjoy**
