# Verification policy — GO IV Lab

## Independently inspected sources (2026-10-28)
- PvPoke MIT license: https://github.com/pvpoke/pvpoke/blob/d98f52174e01f8f67414bc6973599788d098c10e/LICENSE
- Pinned CP multiplier table: PvPoke src/js/pokemon/Pokemon.js at the same commit, Git blob 27040b4e134b70703b1e6947355d27a57a8a0ff8.
- Independent reference: https://db.pokemongohub.net/tools/pvp-iv-checker?id=1
- Pinned game master: PvPoke src/data/gamemaster.json, Git blob f5a1c50c9162fac0f0c34bb7a662f995c6506b74.

## Implementation status
- Nickname format core: Kotlin/JUnit tests passed on GitHub Actions, 2026-10-28.
- CP formula and level 1–51 CPM values: Kotlin/JUnit tests passed on GitHub Actions, 2026-10-28.
- Rank enumeration and exact-score tie handling: Kotlin/JUnit tests passed with Bulbasaur and Ivysaur independent golden vectors, 2026-10-28.
- Species data import: pinned/reproducible Python pipeline; GitHub Actions imported and validated full source hashes, regenerated source data byte-for-byte, and uploaded a provenance artifact, 2026-10-28.
- Offline species/form base-stat dataset: 1,689 validated entries from the 2026-10-26 PvPoke game-master snapshot, committed to data/pokemon-stats.json; local presence verified and CI regeneration check passed.
- Pokémon species database shipped in app: NO.
- Complete independent multi-species parity: NOT VERIFIED.
- Screenshot recognition / overlay / Android APK: NOT IMPLEMENTED.

## CI checkpoint (2026-10-28)
The following all passed for Git commit `c5a07314e76a66d5be1db3d02ca2ae794ad5cf8e`:
- Kotlin core tests — GitHub Actions run #40765284640
- Pinned Pokémon data verification — run #40765284663
- Repository validation — run #40765284656

The first two Kotlin CI attempts failed due a legitimate floating-point
sort/display mismatch exposed by the tests; this was fixed before the
successful checkpoint. A passing test suite is not blanket certification of
all forms or all competitive leagues.

## Release requirements
- Pass Kotlin unit tests and Python importer tests in GitHub Actions.
- Verify that source download at pinned commit matches its Git blob SHA.
- Record SHA-256 provenance and preserve MIT license notice.
- Validate ranks for multiple species/forms, capped and uncapped leagues, level 40/50/51.
- Verify evolution chains, Buddy rules, form separation, and tie behavior.
- Distinguish code compiled, CI passed, and APK personally tested on device.
