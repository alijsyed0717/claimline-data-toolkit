package com.claimline.rules

import java.time.temporal.ChronoUnit

import com.claimline.model.{ClaimLine, Finding, Severity}

/** The claim line validation rule set.
  *
  * Every rule is a function from one claim line to a list of findings.
  * Bad data produces findings, never exceptions, because bad data is
  * the expected input here. The rules know nothing about Spark.
  */
object ClaimRules {

  private val ProcedurePattern = "^[0-9]{5}$".r
  private val NpiPattern = "^[0-9]{10}$".r

  def memberIdPresent(line: ClaimLine): List[Finding] =
    if (line.memberId == null || line.memberId.trim.isEmpty)
      List(
        Finding(
          line.claimNumber,
          line.lineNumber,
          "member-id-present",
          Severity.Error,
          "Claim line carries no member identifier, so the payer cannot match it to a covered person."
        )
      )
    else Nil

  def providerNpiShape(line: ClaimLine): List[Finding] =
    if (line.providerNpi == null || !NpiPattern.pattern.matcher(line.providerNpi).matches())
      List(
        Finding(
          line.claimNumber,
          line.lineNumber,
          "provider-npi-shape",
          Severity.Error,
          "Provider identifier is not a ten digit NPI value."
        )
      )
    else Nil

  def procedureCodeShape(line: ClaimLine): List[Finding] =
    if (
      line.procedureCode == null || !ProcedurePattern.pattern
        .matcher(line.procedureCode)
        .matches()
    )
      List(
        Finding(
          line.claimNumber,
          line.lineNumber,
          "procedure-code-shape",
          Severity.Error,
          "Procedure code is not a five digit CPT style value."
        )
      )
    else Nil

  def serviceBeforeReceived(line: ClaimLine): List[Finding] =
    if (line.serviceDate.isAfter(line.receivedDate))
      List(
        Finding(
          line.claimNumber,
          line.lineNumber,
          "service-before-received",
          Severity.Error,
          "Service date falls after the date the claim was received."
        )
      )
    else Nil

  def timelyFilingWindow(line: ClaimLine): List[Finding] = {
    val days = ChronoUnit.DAYS.between(line.serviceDate, line.receivedDate)
    if (days > 365)
      List(
        Finding(
          line.claimNumber,
          line.lineNumber,
          "timely-filing-window",
          Severity.Warning,
          "Claim arrived more than a year after the service date and sits outside the common timely filing window."
        )
      )
    else Nil
  }

  def billedAmountSane(line: ClaimLine): List[Finding] =
    if (line.billedCents <= 0)
      List(
        Finding(
          line.claimNumber,
          line.lineNumber,
          "billed-amount-sane",
          Severity.Error,
          "Billed amount is zero or negative on a submitted claim line."
        )
      )
    else Nil

  def unitsSane(line: ClaimLine): List[Finding] =
    if (line.units <= 0 || line.units > 99)
      List(
        Finding(
          line.claimNumber,
          line.lineNumber,
          "units-sane",
          Severity.Warning,
          "Unit count sits outside the range a single claim line normally carries."
        )
      )
    else Nil

  /** Every rule in the set, in catalogue order. */
  val all: List[ClaimLine => List[Finding]] = List(
    memberIdPresent,
    providerNpiShape,
    procedureCodeShape,
    serviceBeforeReceived,
    timelyFilingWindow,
    billedAmountSane,
    unitsSane
  )

  /** Run the full rule set over one claim line. */
  def validate(line: ClaimLine): List[Finding] =
    all.flatMap(rule => rule(line))
}
