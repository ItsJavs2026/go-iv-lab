# Reproducible Pokémon GO data sourcing

PvPoke is an open-source battle simulator with MIT-licensed source code and game data:
https://github.com/pvpoke/pvpoke

The GO IV Lab importer pins commit `d98f52174e01f8f67414bc6973599788d098c10e` rather than following the moving master branch.

### Imported assets
- `src/data/gamemaster.json`: species/form IDs and GO base attack, defense, stamina
- `src/js/pokemon/Pokemon.js`: level multipliers
- `LICENSE`: retained unmodified in the generated artifact

The Python importer checks exact Git blob hashes, source shape, uniqueness, positive integer base stats, 101 strictly increasing CPMs, license notice, and records source SHA-256 hashes and retrieval date.

### How to regenerate
Run `python -m unittest discover -s tools -p test_import_pvpoke.py -v`, then `python tools/import_pvpoke.py build/imported-pvpoke` in an environment with network access.

The GitHub Actions workflow `data-validation.yml` runs both commands and uploads the generated data and provenance. This is a development artifact, not a production app bundle or proof that the entire Pokédex is accurate.

### Licensing and interpretation
PvPoke's MIT license must be preserved when using substantial portions of its files. Pokémon names, characters, artwork and trademarks remain the property of their respective rights holders; no third-party art or APK code is imported.

Game data can lag in-game releases. Verify form identifiers, evolution graphs, game availability, rounding and league eligibility before labeling a scanned result verified.

### Independent golden fixture
https://db.pokemongohub.net/tools/pvp-iv-checker?id=1

Bulbasaur, Great League 1500 CP, max level 50: 15/15/15 ranks 1 (1260 CP); 0/15/15 ranks 770 (1118 CP), including tie sorting.


### Source refresh — 2026-10-28
- Pinned PvPoke commit: `d98f52174e01f8f67414bc6973599788d098c10e`
- Source game master timestamp: `2026-10-26 22:08:07`
- Source records: 1,751; after excluding Mega/Primal variants: 1,689
- Committed static file: `data/pokemon-stats.json` (IDs, names, base stats)
- Metadata: `data/manifest.json`
- Alolan Diglett identifier `diglett_alolan` has base 108 Attack / 81 Defense / 67 Stamina
- Level 1–51 CPM data matched the earlier pinned PvPoke table exactly.
- CI must regenerate from the pinned upstream Git blobs and prove byte-for-byte equality before acceptance.

The inclusion of a species in the source does not imply it is currently obtainable or legal in a special cup.
