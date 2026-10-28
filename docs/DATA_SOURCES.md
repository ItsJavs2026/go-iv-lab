# Reproducible Pokémon GO data sourcing

PvPoke is an open-source battle simulator with MIT-licensed source code and game data:
https://github.com/pvpoke/pvpoke

The GO IV Lab importer pins commit `04ee0835e80f30c45376415725c0039cbdd12c5e` rather than following the moving master branch.

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
