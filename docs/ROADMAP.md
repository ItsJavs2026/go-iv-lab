# GO IV Lab roadmap

Independent, offline-first Android IV checker inspired by Poke Genie's user workflow. Do not copy proprietary assets or code.

## v0.1 foundation
- Kotlin Android app with manual species/form, CP and 0–15 appraisal IV input
- Deterministic CP/stat-product ranking with tested Pokémon base-stat and CP multiplier datasets
- League eligibility and evolution comparisons
- Configurable nickname templates and 12-character validation
- Screenshot import prototype with manual correction
- Local-only processing; no Pokémon GO credentials

## v0.2 overlay
- Explicitly consented MediaProjection capture and foreground service
- Movable bubble and compact/expanded results
- Reliable screenshot matching and confidence handling

## Quality gates
- Golden rank test vectors from independently verified sources
- Regression tests for IV boundaries, level caps, forms, and nickname Unicode
- Keep candidate APKs distinct from stable versions
- Do not publish ranking claims until the underlying data and tests are verified
