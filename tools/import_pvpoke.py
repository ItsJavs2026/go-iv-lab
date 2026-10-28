#!/usr/bin/env python3
"""Reproducible, offline-output Pokémon GO data import from a PINNED PvPoke commit.

Network access is used ONLY during the development import/build; not by the app.
The original third-party MIT license is preserved in the generated artifact.
"""
import hashlib
import json
import re
import sys
import urllib.request
from datetime import date
from pathlib import Path

COMMIT = "d98f52174e01f8f67414bc6973599788d098c10e"
BASE_URL = f"https://raw.githubusercontent.com/pvpoke/pvpoke/{COMMIT}/"
EXPECTED_GIT_BLOBS = {
    "src/data/gamemaster.json": "f5a1c50c9162fac0f0c34bb7a662f995c6506b74",
    "src/js/pokemon/Pokemon.js": "27040b4e134b70703b1e6947355d27a57a8a0ff8",
    "LICENSE": "19295f2ae4280e78e4ac77cd70cbcb6089dd3f6e",
}
EXCLUDED_TAGS = {"mega", "supermega", "primal"}


def git_blob_sha1(raw):
    return hashlib.sha1(b"blob " + str(len(raw)).encode() + b"\0" + raw).hexdigest()


def retrieve(path):
    req = urllib.request.Request(BASE_URL + path, headers={"User-Agent": "GO-IV-Lab-data-import/0.1"})
    with urllib.request.urlopen(req, timeout=45) as response:
        raw = response.read(12_000_001)
    if len(raw) > 12_000_000:
        raise ValueError(f"{path}: downloaded source too large")
    sha = git_blob_sha1(raw)
    if sha != EXPECTED_GIT_BLOBS[path]:
        raise ValueError(f"{path}: upstream Git blob changed ({sha})")
    return raw


def parse_pokemon(master_raw):
    data = json.loads(master_raw.decode("utf-8"))
    records = data["pokemon"]
    if not isinstance(records, list) or len(records) < 1500:
        raise ValueError("Unexpectedly short Pokémon list")
    imported = []
    seen = set()
    for p in records:
        pid = p["speciesId"]
        if not isinstance(pid, str) or not pid:
            raise ValueError("Missing speciesId")
        tags = set(p.get("tags") or [])
        if tags & EXCLUDED_TAGS or re.search(r"(?:^|_)(?:mega|primal)(?:_|$)", pid):
            continue
        if pid in seen:
            raise ValueError(f"Duplicate Pokémon ID: {pid}")
        seen.add(pid)
        name = p["speciesName"]
        stats = p["baseStats"]
        values = [stats["atk"], stats["def"], stats["hp"]]
        if not isinstance(name, str) or not name or any(type(v) is not int or v <= 0 for v in values):
            raise ValueError(f"Invalid Pokémon base stats: {pid}")
        imported.append({
            "speciesId": pid,
            "name": name,
            "base": {"attack": values[0], "defense": values[1], "stamina": values[2]},
        })
    if len(imported) < 1200:
        raise ValueError("Imported Pokémon count is unexpectedly low")
    return imported


def parse_cpms(pokemon_js_raw):
    source = pokemon_js_raw.decode("utf-8")
    match = re.search(r"var cpms = (\[[\d.,\s]+\]);", source)
    if not match:
        raise ValueError("CPM table absent")
    values = json.loads(match.group(1))[:101]
    if len(values) != 101 or any(
        not isinstance(v, (int, float)) or not (0 < v < 1)
        or (i > 0 and v <= values[i - 1]) for i, v in enumerate(values)
    ):
        raise ValueError("Invalid CP multiplier sequence")
    return values


def import_data(output):
    raw = {path: retrieve(path) for path in EXPECTED_GIT_BLOBS}
    if not raw["LICENSE"].startswith(b"MIT License\n") or b"Copyright (c) 2019 pvpoke" not in raw["LICENSE"]:
        raise ValueError("License changed")
    pokemon = parse_pokemon(raw["src/data/gamemaster.json"])
    multipliers = parse_cpms(raw["src/js/pokemon/Pokemon.js"])
    output.mkdir(parents=True, exist_ok=True)
    (output / "pokemon.json").write_text(json.dumps(pokemon, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (output / "cpms.json").write_text(json.dumps(multipliers, indent=2) + "\n", encoding="utf-8")
    (output / "PVPoke-LICENSE.txt").write_bytes(raw["LICENSE"])
    provenance = {
        "source": "https://github.com/pvpoke/pvpoke",
        "revision": COMMIT,
        "retrievedDate": date.today().isoformat(),
        "license": "MIT; original notice included",
        "files": {path: {"gitBlobSHA1": EXPECTED_GIT_BLOBS[path],
                         "sha256": hashlib.sha256(blob).hexdigest()}
                  for path, blob in raw.items()},
        "pokemonCount": len(pokemon),
        "cpmCount": len(multipliers),
        "rankingVerified": False,
    }
    (output / "provenance.json").write_text(json.dumps(provenance, indent=2) + "\n", encoding="utf-8")
    print(f"Imported {len(pokemon)} records and {len(multipliers)} CPMs from {COMMIT}")


if __name__ == "__main__":
    destination = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("build/imported-pvpoke")
    import_data(destination)
