# Changelog

All notable changes to this project are recorded here. The format follows the spirit of Keep a Changelog and the versions follow semantic versioning.

## Unreleased

- Rule catalogue moved into docs with a written reason per rule
- Generator gains a planted timely filing defect for the date rules to catch

## 0.1.0, 2026-11-08

- First release. Claim line validation rules for identifiers, procedure codes, dates, amounts, and units.
- Duplicate claim line detection ahead of reconciliation.
- Reconciliation pass matching claims to remittance lines with a missing and mismatch report.
- Spark job that runs the rule set over a CSV batch and writes findings to disk.
- ScalaTest suite covering every rule and the reconciliation keys.
- Synthetic data generator with planted defects.
