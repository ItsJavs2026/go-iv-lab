# Verification policy — GO IV Lab

## Independently inspected sources (2026-10-28)
- PvPoke MIT license: https://github.com/pvpoke/pvpoke/blob/d98f52174e01f8f67414bc6973599788d098c10e/LICENSE
- Pinned CP multiplier table: PvPoke src/js/pokemon/Pokemon.js at the same commit, Git blob 27040b4e134b70703b1e6947355d27a57a8a0ff8.
- Independent reference: https://db.pokemongohub.net/tools/pvp-iv-checker?id=1
- Pinned game master: PvPoke src/data/gamemaster.json, Git blob f5a1c50c9162fac0f0c34bb7a662f995c6506b74.

## Implementation status
- Nickname format core: committed; CI success not yet confirmed.
- CP formula and level 1–51 CPM values: committed with unit tests.
- Rank enumeration and exact-score tie handling: committed with Bulbasaur reference regression.
- Species data import: pinned/reproducible Python pipeline committed. Download + artifact availability await CI.
- Offline species/form base-stat dataset: 1,689 validated entries from the 2026-10-26 PvPoke game-master snapshot, committed to data/pokemon-stats.json; local presence verified, CI regeneration check pending.
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
