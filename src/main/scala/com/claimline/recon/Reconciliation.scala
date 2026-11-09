package com.claimline.recon

import com.claimline.model.{ClaimLine, Finding, RemittanceLine, Severity}

/** Duplicate detection and reconciliation against remittance lines. */
object Reconciliation {

  /** Tolerance for a paid amount to still count as matching. */
  val ToleranceCents: Long = 100L

  private def key(claimNumber: String, lineNumber: Int): (String, Int) =
    (claimNumber, lineNumber)

  /** Lines that repeat an earlier line in full.
    *
    * The first occurrence is kept as the original. Every later line
    * with the same claim number, line number, and content counts as
    * a duplicate and gets a finding.
    */
  def findDuplicates(lines: Seq[ClaimLine]): List[Finding] = {
    val seen = scala.collection.mutable.Set.empty[ClaimLine]
    val findings = List.newBuilder[Finding]
    lines.foreach { line =>
      if (seen.contains(line)) {
        findings += Finding(
          line.claimNumber,
          line.lineNumber,
          "duplicate-line",
          Severity.Error,
          "Claim line repeats an earlier line in the same batch word for word."
        )
      } else {
        seen += line
      }
    }
    findings.result()
  }

  /** Compare claim lines against remittance lines.
    *
    * A claim line with no remittance partner is reported missing. A
    * partner paid further from the billed amount than the tolerance
    * is reported as a mismatch with both amounts in the message.
    */
  def reconcile(
      claims: Seq[ClaimLine],
      remittance: Seq[RemittanceLine]
  ): List[Finding] = {
    val paid = remittance.map(r => key(r.claimNumber, r.lineNumber) -> r).toMap
    claims.flatMap { claim =>
      paid.get(key(claim.claimNumber, claim.lineNumber)) match {
        case None =>
          List(
            Finding(
              claim.claimNumber,
              claim.lineNumber,
              "remittance-missing",
              Severity.Warning,
              "No remittance line came back for this claim line."
            )
          )
        case Some(line) =>
          val gap = claim.billedCents - line.paidCents
          if (math.abs(gap) > ToleranceCents)
            List(
              Finding(
                claim.claimNumber,
                claim.lineNumber,
                "amount-mismatch",
                Severity.Warning,
                s"Paid amount differs from billed amount by $gap cents on this line."
              )
            )
          else Nil
      }
    }.toList
  }

  /** Group findings by rule name with counts, sorted by rule name. */
  def summarize(findings: Seq[Finding]): List[(String, Int)] =
    findings.groupBy(_.ruleName).map { case (name, group) => (name, group.size) }.toList.sortBy(_._1)
}
