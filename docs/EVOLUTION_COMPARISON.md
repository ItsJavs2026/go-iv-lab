# Evolution-stage PvP comparison (v0.5 experimental)

## What this feature provides
After manually calculating a Pokémon IV rank, tap **EVOLUTIONS** in the native rank popup or **COMPARE EVOLUTIONS · MY IVs** in GO IV Lab. The app checks first/middle/final stages and alternative branches that are actually linked by pinned PvPoke evolution edges, holding the **same IV spread, league and level cap** constant.

It displays:
- **IV rank and % of the stage's own rank-1 spread:** how good these IVs are *for that exact species*. A high % is not proof of good battle matchups.
- **PvPoke Open League overall score and league rank:** simulation-based species-level PvP strength from Oct 26, 2026, when present. This is distinct from IV rank and does **not** simulate the user's exact moves or IVs.
- **Recommended Open League stage:** the highest rated **eligible, known** linked species. If no ratings are present, no winner is invented. Special-cup bans or eligibility are not modeled.

## Data provenance
All bundled data is pinned to source commit `d98f52174e01f8f67414bc6973599788d098c10e` from PvPoke.
- `data/pokemon-stats.json`: 1,689 base-stat species and form entries; unchanged from v0.4.
- `data/evolution-families.json`: 1,341 entries with graph edges derived from gamemaster Git blob `f5a1c50c9162fac0f0c34bb7a662f995c6506b74`, filtered to exact available forms.
- `data/open-league-pvpoke-rankings.json`: open Little/Great/Ultra/Master `overall` simulation scores and source blob SHAs at the same source commit, with missing entries left unknown rather than zero.

See `third_party/PVPoke-LICENSE.txt`; preserve MIT attribution. This is a fixed snapshot and will age with live move and balance updates.

## Guarantees and limits
- Matches **form IDs** exactly, including Shadow or regional evolutions where edges exist; does not simply group all forms with the same family name.
- Validates source pins and rejects duplicate records. Does not invent evolutionary relationships for disconnected species.
- Performs extra rank calculations **only after explicit COMPARE/EVOLUTIONS request**, on a background worker with progress messages. Current floating IV popup is unchanged and remains responsive.
- If rankings/metadata fail to load, the ordinary manual IV calculator still works.
- **No PvPoke meta score** for a species means unranked in this snapshot, **not** a score of 0.
- The rating may not reflect the Pokémon's actual equipped moves, special-cup meta, availability, resource cost, or the user's specific IV breakpoint matchups.
- The evolution comparison is a calculation, **not an automatic evolution or game interaction**. It does not read or change Pokémon GO data.
- Purification/other transformations can change IVs; using the same input IVs is not always valid for those transformations.

## First important validations
- Bulbasaur 0/15/15 GL: stages Bulbasaur/Ivysaur/Venusaur; Ivysaur has stronger relative IV fit than Bulbasaur while Venusaur has higher PvPoke Open GL simulation score in this pinned snapshot.
- Eevee GL: compare all available recognized branches; do not assign all evolutions the same stage form.
- Shadow Bulbasaur: do not mix a regular evolution with a Shadow evolution.
- A disconnected species such as Mewtwo must not claim a first/middle/final comparison.
- Recalculation or species change invalidates a pending comparison result and must not attach an old result to a new Pokémon.

## Before phone installation
- v0.5 JVM evolution and rating tests pass.
- Android lint, compiled APK asset checks, and routine popup/overlay emulator tests pass.
- Prefer additional Android milestone tests before sending as the next major Samsung APK.
- v0.4 Overlay Lab remains the first physical Pokémon GO overlay test candidate.
