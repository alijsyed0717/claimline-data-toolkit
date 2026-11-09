"""Generate synthetic claim and remittance files with planted defects.

The output gives the rule set honest work. Most lines are clean. A
small share carry one defect each, drawn from the same problems the
rules check for. A couple of exact duplicate lines and short paid
remittance lines exercise the reconciliation pass.

All data is invented. Member and claim identifiers are made up and
no real claim data is used anywhere.
"""

import argparse
import csv
import os
import random
from datetime import date, timedelta

PROCEDURES = ("99213", "99214", "97110", "36415", "87804")
DIAGNOSES = ("J06.9", "M54.5", "E11.9", "I10", "Z00.00")


def build_rows(count, seed):
    rng = random.Random(seed)
    claims = []
    remittance = []
    base_service = date(2026, 9, 14)
    for index in range(1, count + 1):
        service = base_service + timedelta(days=rng.randint(0, 27))
        received = service + timedelta(days=rng.randint(2, 12))
        row = {
            "claim_number": f"CLM-{1000 + index}",
            "line_number": 1,
            "member_id": f"MBR-{rng.randint(100, 899)}",
            "provider_npi": f"12{rng.randint(10000000, 99999999)}",
            "procedure_code": rng.choice(PROCEDURES),
            "diagnosis_code": rng.choice(DIAGNOSES),
            "service_date": service.isoformat(),
            "received_date": received.isoformat(),
            "units": 1,
            "billed_cents": rng.choice((8500, 14500, 22000, 36750)),
        }
        roll = rng.random()
        if roll < 0.03:
            row["member_id"] = ""
        elif roll < 0.05:
            row["procedure_code"] = "99A13"
        elif roll < 0.07:
            row["billed_cents"] = 0
        elif roll < 0.09:
            row["received_date"] = (service - timedelta(days=3)).isoformat()
        elif roll < 0.11:
            row["units"] = 240
        claims.append(row)
        if rng.random() < 0.06:
            continue
        paid = row["billed_cents"]
        if rng.random() < 0.08:
            paid = int(paid * 0.6)
        remittance.append(
            {
                "claim_number": row["claim_number"],
                "line_number": 1,
                "paid_cents": paid,
                "adjustment_reason": "" if paid == row["billed_cents"] else "contractual-adjustment",
            }
        )
    if count >= 10:
        claims.append(dict(claims[3]))
        claims.append(dict(claims[3]))
    return claims, remittance


def write_csv(path, rows, fields):
    with open(path, "w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--claims", type=int, default=1000)
    parser.add_argument("--seed", type=int, default=7)
    parser.add_argument("--out", default="sample-data/out")
    args = parser.parse_args()

    os.makedirs(args.out, exist_ok=True)
    claims, remittance = build_rows(args.claims, args.seed)
    write_csv(
        os.path.join(args.out, "claims.csv"),
        claims,
        (
            "claim_number", "line_number", "member_id", "provider_npi",
            "procedure_code", "diagnosis_code", "service_date",
            "received_date", "units", "billed_cents",
        ),
    )
    write_csv(
        os.path.join(args.out, "remittance.csv"),
        remittance,
        ("claim_number", "line_number", "paid_cents", "adjustment_reason"),
    )
    print(f"Wrote {len(claims)} claim lines and {len(remittance)} remittance lines.")


if __name__ == "__main__":
    main()
