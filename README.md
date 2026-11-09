Claimline is a data quality and reconciliation toolkit for healthcare claims data, built on Apache Spark and Scala. It reads claim batches in CSV form, runs a set of plain validation rules over every claim line, finds duplicate and mismatched records, and compares the batch against remittance lines to show what was paid, what was short, and what never came back.

The rules are the heart of the project. Each rule is a small function over one claim line that returns findings with a severity and a plain explanation. The Spark job is a thin shell around the rule set, so the logic can be tested without a cluster and new rules can be added without touching the plumbing.

Sample data in this repo is synthetic. A generator script builds claim batches with a handful of planted defects so the rules have something honest to find. No real claim data belongs in here.

## What is inside

**src/main/scala/com/claimline/model** holds the claim, remittance, and finding case classes everything else works with.

**src/main/scala/com/claimline/rules** holds the validation rule set. Missing identifiers, malformed procedure codes, impossible dates, negative amounts, and unit counts that do not belong on a single line.

**src/main/scala/com/claimline/recon** holds the duplicate detection and the reconciliation pass that matches claims to remittance lines and reports amount gaps.

**src/main/scala/com/claimline/jobs** holds the Spark entry point that reads a batch, runs the rules, and writes findings and a reconciliation report.

**sample-data** holds the generator that builds synthetic claim and remittance files.

**docs** holds the rule catalogue with the reasoning behind each rule and the project roadmap.

## Run it

You need a JDK 17 and sbt. The tests run with no cluster and no Spark session for the pure logic.

1. Run the suite with `sbt test`.
2. Build sample data with `python3 sample-data/generate_claims.py --claims 1000`. The files land in `sample-data/out`.
3. Run the job locally with `sbt "run sample-data/out/claims.csv sample-data/out/remittance.csv build/report"`. Findings and the reconciliation summary land in the output folder as CSV.

## How a rule is written

A rule takes one claim line and returns zero or more findings. It never throws on bad data, because bad data is the normal case here. A line with a missing member identifier gets a finding, not a crashed job. Rules stay independent of each other so the batch report can group findings by rule and a team can argue about one rule without reopening the rest.

Reconciliation keys on claim number and line number. A claim line with no remittance partner is reported as missing. A partner whose paid amount differs from the billed amount beyond a small tolerance is reported as a mismatch with both amounts shown. Exact duplicate claim lines, same claim number and same content, are flagged before reconciliation so they do not double count.

## License

MIT. See LICENSE.
