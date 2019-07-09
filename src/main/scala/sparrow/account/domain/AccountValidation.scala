package sparrow.account.domain

import wvlet.log.LogSupport

trait AccountValidation extends LogSupport {

  lazy val zeroOrGreater = (amount: Double) => amount match {
    case amount if amount < 0 => 0
    case _ => amount
  }

  lazy val amountIsNegative = (amount: Double) => amount match {
    case amount if amount < 0 => true
    case _ => false
  }

  lazy val displayOperationType = (uuid: String, amount: Double) => amount match {
    case amount if amount > 0 => info(s"Deposit for: ${uuid} in the amount of: ${amount}")
    case amount if amount < 0 => info(s"Withdraw for: ${uuid} in the amount of: ${amount}")
    case _ => info(s"Zero for: ${uuid} in the amount of: ${amount}")
  }

}
