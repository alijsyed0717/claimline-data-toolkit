# Contributing

This project grows one rule at a time, and contributions follow that shape.

## Getting set up

You need a JDK 17 and sbt. Clone the repo and run `sbt test` first so you know the suite starts green. Generate a sample batch with the script in sample-data if you want data to look at.

## Adding a rule

A good rule catches a defect that costs someone time when it slips through. Write the rule as its own function in the rules package, give the finding a severity and a message a claims analyst can act on, and add tests covering a line that passes, a line that fails, and the edge case that sits between them.

Add the rule to the catalogue in docs with a short note on why it exists. Rules without a written reason tend to get argued about later, and the note settles most of those arguments.

## Changing reconciliation

The matching keys and the amount tolerance carry real consequences for the reports. Changes there need tests that show the before and after behavior on the planted defect data, and the pull request should spell out which report lines move.

## Style

Scala code follows the layout already in the source tree. Keep functions small and keep Spark out of the rules and recon packages. If a piece of logic needs a Spark session to test, it is in the wrong package.

## Reporting a problem

Open an issue with a trimmed sample line that shows the problem, the finding you got, and the finding you expected. Synthetic data only in issues, always.
