package com.claimline.model

import java.time.LocalDate

/** One line of a submitted claim batch.
  *
  * Amounts ride as cents in a Long so money math never touches a
  * floating point type. Dates are parsed at read time by the job and
  * by the generator contract tests.
  */
final case class ClaimLine(
    claimNumber: String,
    lineNumber: Int,
    memberId: String,
    providerNpi: String,
    procedureCode: String,
    diagnosisCode: String,
    serviceDate: LocalDate,
    receivedDate: LocalDate,
    units: Int,
    billedCents: Long
)

/** One line of a remittance file, the payer answer to a claim line. */
final case class RemittanceLine(
    claimNumber: String,
    lineNumber: Int,
    paidCents: Long,
    adjustmentReason: Option[String]
)

/** Something a rule or the reconciliation pass found on a claim line. */
final case class Finding(
    claimNumber: String,
    lineNumber: Int,
    ruleName: String,
    severity: String,
    message: String
)

object Severity {
  val Error = "error"
  val Warning = "warning"
}
