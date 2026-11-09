package com.claimline.rules

import java.time.LocalDate

import org.scalatest.funsuite.AnyFunSuite

import com.claimline.model.ClaimLine

class ClaimRulesSpec extends AnyFunSuite {

  private def cleanLine: ClaimLine =
    ClaimLine(
      claimNumber = "CLM-1001",
      lineNumber = 1,
      memberId = "MBR-778",
      providerNpi = "1234567893",
      procedureCode = "99213",
      diagnosisCode = "J06.9",
      serviceDate = LocalDate.of(2026, 10, 2),
      receivedDate = LocalDate.of(2026, 10, 9),
      units = 1,
      billedCents = 14500L
    )

  test("a clean line passes every rule") {
    assert(ClaimRules.validate(cleanLine).isEmpty)
  }

  test("a missing member identifier is an error") {
    val findings = ClaimRules.memberIdPresent(cleanLine.copy(memberId = "  "))
    assert(findings.size == 1)
    assert(findings.head.ruleName == "member-id-present")
    assert(findings.head.severity == "error")
  }

  test("a short provider identifier fails the shape rule") {
    val findings = ClaimRules.providerNpiShape(cleanLine.copy(providerNpi = "12345"))
    assert(findings.size == 1)
    assert(findings.head.ruleName == "provider-npi-shape")
  }

  test("a malformed procedure code fails the shape rule") {
    val findings = ClaimRules.procedureCodeShape(cleanLine.copy(procedureCode = "99A13"))
    assert(findings.size == 1)
    assert(findings.head.ruleName == "procedure-code-shape")
  }

  test("a service date after the received date is an error") {
    val line = cleanLine.copy(
      serviceDate = LocalDate.of(2026, 10, 12),
      receivedDate = LocalDate.of(2026, 10, 9)
    )
    val findings = ClaimRules.serviceBeforeReceived(line)
    assert(findings.size == 1)
    assert(findings.head.severity == "error")
  }

  test("a claim filed over a year late draws the timely filing warning") {
    val line = cleanLine.copy(
      serviceDate = LocalDate.of(2025, 8, 1),
      receivedDate = LocalDate.of(2026, 10, 9)
    )
    val findings = ClaimRules.timelyFilingWindow(line)
    assert(findings.size == 1)
    assert(findings.head.severity == "warning")
  }

  test("a claim inside the filing window stays quiet") {
    assert(ClaimRules.timelyFilingWindow(cleanLine).isEmpty)
  }

  test("a zero billed amount is an error") {
    val findings = ClaimRules.billedAmountSane(cleanLine.copy(billedCents = 0L))
    assert(findings.size == 1)
    assert(findings.head.ruleName == "billed-amount-sane")
  }

  test("a very high unit count draws the units warning") {
    val findings = ClaimRules.unitsSane(cleanLine.copy(units = 250))
    assert(findings.size == 1)
    assert(findings.head.ruleName == "units-sane")
  }

  test("several defects on one line produce several findings") {
    val line = cleanLine.copy(memberId = "", billedCents = -5L, units = 0)
    val names = ClaimRules.validate(line).map(_.ruleName).toSet
    assert(names == Set("member-id-present", "billed-amount-sane", "units-sane"))
  }
}
