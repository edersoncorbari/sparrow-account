package sparrow.account.domain

trait AccountValidation {

  lazy val zeroOrGreater = (amount: Double) => amount match {
    case amount if amount < 0 => 0
    case _ => amount
  }

  lazy val amountIsNegative = (amount: Double) => amount match {
    case amount if amount < 0 => true
    case _ => false
  }

  lazy val displayOperationType = (uuid: String, amount: Double) => amount match {
    case amount if amount > 0 => println(s"Deposit for: ${uuid} in the amount of: ${amount}")
    case amount if amount < 0 => println(s"Withdraw for: ${uuid} in the amount of: ${amount}")
    case _ => println(s"Zero for: ${uuid} in the amount of: ${amount}")
  }

}
