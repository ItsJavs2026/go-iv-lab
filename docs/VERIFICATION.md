# Verification policy

## Status
- Nickname core: source committed; CI result not yet confirmed.
- IV arithmetic: source committed; CI result not yet confirmed.
- PvP IV rank: enumeration algorithm committed; NOT YET VERIFIED against game data or independent golden vectors.
- Pokémon base-stat and CP multiplier datasets: NOT INCLUDED pending provenance review.
- Screenshot recognition, overlay, Android APK: NOT IMPLEMENTED.

## Ranking requirements
- Enumerate all 4096 IV combinations for each species/form/evolution.
- Determine highest eligible power-up level under each league CP cap.
- Use game-accurate floor rules, integer HP, and a versioned CP multiplier table.
- Apply level caps and Best Buddy rules explicitly.
- Rank by stat product with documented tie handling; include eligibility and max-CP cases.
- Verify against independent published calculators using fixed golden vectors.
- Never present sample rank numbers as verified.

## Source controls
Record data origin, license, version/date and checksums before shipping datasets.
Do not import Poke Genie APK code, proprietary tables, or visual assets.
