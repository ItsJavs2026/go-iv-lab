#!/usr/bin/env python3
"""Offline integrity checks for the checked-in Pokémon GO stat subset."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


def verify():
    records = json.loads((ROOT / "data/pokemon-stats.json").read_text(encoding="utf-8"))
    manifest = json.loads((ROOT / "data/manifest.json").read_text(encoding="utf-8"))
    if not isinstance(records, list) or len(records) != manifest["count"] or len(records) < 1500:
        raise ValueError("Pokémon count differs from source manifest")
    if manifest["schemaVersion"] != 1 or len(manifest["sourceCommit"]) != 40:
        raise ValueError("Bad data source metadata")
    ids = set()
    for item in records:
        pid = item["speciesId"]
        if not isinstance(pid, str) or not pid or pid in ids:
            raise ValueError(f"Duplicate or missing form ID: {pid}")
        ids.add(pid)
        for key in ("attack", "defense", "stamina"):
            stat = item["base"][key]
            if type(stat) is not int or stat <= 0:
                raise ValueError(f"Invalid {pid} {key}: {stat}")
        if not isinstance(item["name"], str) or not item["name"]:
            raise ValueError(f"Missing name for {pid}")

    by_id = {x["speciesId"]: x["base"] for x in records}
    anchors = {
        "bulbasaur": (118, 111, 128),
        "ivysaur": (151, 143, 155),
        "diglett_alolan": (108, 81, 67),
    }
    for pid, stats in anchors.items():
        entry = by_id[pid]
        actual = (entry["attack"], entry["defense"], entry["stamina"])
        if actual != stats:
            raise ValueError(f"Known base-stat regression for {pid}: {actual}")
    assert (ROOT / "third_party/PVPoke-LICENSE.txt").exists()
    print(f"Verified {len(records)} species/form records; pinned commit {manifest['sourceCommit']}")


if __name__ == "__main__":
    verify()
