# Roadmap

Work is grouped by area. Items move to the changelog when they ship.

## Now

- A rule for diagnosis and procedure pairing on common code families
- Findings output in parquet alongside the CSV the job already writes
- The generator gains a batch with a shifted column so the shape rules show their tripwire value

## Next

- A835 style summary output grouped by payer once a payer field joins the claim model
- Trend output that compares finding counts between two batches
- A rule pack split so teams can run a subset of the catalogue

## Later

- Streaming intake over a Kafka topic with the same rule set, once the batch path has real mileage
- A small review UI for findings, only if the CSV reports stop being enough

## Not planned

- Claim submission. This toolkit reads and reports. It never sends anything to a payer.
- Real claim data in the repo. Sample data stays synthetic, always.
