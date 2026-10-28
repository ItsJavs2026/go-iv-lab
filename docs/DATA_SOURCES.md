# Data sourcing and acceptance checklist

The app must NOT ship made-up or unreviewed Pokémon base stats or CP multipliers.

## Required provenance per release
- Original source URL and dataset version/commit
- Retrieval date, declared license and redistribution permission
- SHA-256 of the exact imported source artifact
- Species and form identifiers, with explicit handling of alternate forms
- Level-to-CPM values and their supported level range

## Independent verification
- Compare CP and HP at several levels and IV extremes against published game-calculator results
- Compare full PvP rank for selected species and forms across Little, Great, and Ultra League
- Test capped species, evolved forms, Best Buddy level 51, and tie policies
- Record actual reference outputs and links before marking a result verified

## Current status
No production data has been imported. Synthetic unit fixtures are for software behavior only, not Pokémon GO accuracy certification.
