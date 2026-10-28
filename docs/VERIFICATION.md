# Verification policy — GO IV Lab

## Independently inspected sources (2026-10-28)
- PvPoke MIT license: https://github.com/pvpoke/pvpoke/blob/04ee0835e80f30c45376415725c0039cbdd12c5e/LICENSE
- Pinned CP multiplier table: PvPoke src/js/pokemon/Pokemon.js at the same commit, Git blob a6a0bfbae03a81b6a0772252908ff1865e9ebdfc.
- Independent reference: https://db.pokemongohub.net/tools/pvp-iv-checker?id=1
- Pinned game master: PvPoke src/data/gamemaster.json, Git blob d5cefa162670e8c9ef85b30dfb3595edaf4d3697.

## Implementation status
- Nickname format core: committed; CI success not yet confirmed.
- CP formula and level 1–51 CPM values: committed with unit tests.
- Rank enumeration and exact-score tie handling: committed with Bulbasaur reference regression.
- Species data import: pinned/reproducible Python pipeline committed. Download + artifact availability await CI.
- Pokémon species database shipped in app: NO.
- Complete independent multi-species parity: NOT VERIFIED.
- Screenshot recognition / overlay / Android APK: NOT IMPLEMENTED.

## Release requirements
- Pass Kotlin unit tests and Python importer tests in GitHub Actions.
- Verify that source download at pinned commit matches its Git blob SHA.
- Record SHA-256 provenance and preserve MIT license notice.
- Validate ranks for multiple species/forms, capped and uncapped leagues, level 40/50/51.
- Verify evolution chains, Buddy rules, form separation, and tie behavior.
- Distinguish code compiled, CI passed, and APK personally tested on device.
