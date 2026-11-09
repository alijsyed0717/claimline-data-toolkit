package com.claimline.recon

import java.time.LocalDate

import org.scalatest.funsuite.AnyFunSuite

import com.claimline.model.{ClaimLine, RemittanceLine}

class ReconciliationSpec extends AnyFunSuite {

  private def line(number: String, units: Int = 1, billed: Long = 14500L): ClaimLine =
    ClaimLine(
      claimNumber = number,
      lineNumber = 1,
      memberId = "MBR-778",
      providerNpi = "1234567893",
      procedureCode = "99213",
      diagnosisCode = "J06.9",
      serviceDate = LocalDate.of(2026, 10, 2),
      receivedDate = LocalDate.of(2026, 10, 9),
      units = units,
      billedCents = billed
    )

  test("an exact repeat of a line is flagged once") {
    val first = line("CLM-1")
    val findings = Reconciliation.findDuplicates(List(first, first, line("CLM-2")))
    assert(findings.size == 1)
    assert(findings.head.ruleName == "duplicate-line")
    assert(findings.head.claimNumber == "CLM-1")
  }

  test("same claim number with different content is not a duplicate") {
    val findings = Reconciliation.findDuplicates(List(line("CLM-1"), line("CLM-1", units = 2)))
    assert(findings.isEmpty)
  }

  test("a claim with no remittance partner is reported missing") {
    val findings = Reconciliation.reconcile(List(line("CLM-1")), List.empty)
    assert(findings.size == 1)
    assert(findings.head.ruleName == "remittance-missing")
  }

  test("a fully paid line inside tolerance stays quiet") {
    val paid = RemittanceLine("CLM-1", 1, 14450L, None)
    val findings = Reconciliation.reconcile(List(line("CLM-1")), List(paid))
    assert(findings.isEmpty)
  }

  test("a short paid line beyond tolerance is a mismatch") {
    val paid = RemittanceLine("CLM-1", 1, 9000L, Some("contractual-adjustment"))
    val findings = Reconciliation.reconcile(List(line("CLM-1")), List(paid))
    assert(findings.size == 1)
    assert(findings.head.ruleName == "amount-mismatch")
    assert(findings.head.message.contains("5500"))
  }

  test("the summary groups findings by rule") {
    val findings = Reconciliation.reconcile(
      List(line("CLM-1"), line("CLM-2")),
      List(RemittanceLine("CLM-2", 1, 2000L, None))
    )
    val summary = Reconciliation.summarize(findings).toMap
    assert(summary("remittance-missing") == 1)
    assert(summary("amount-mismatch") == 1)
  }
}
