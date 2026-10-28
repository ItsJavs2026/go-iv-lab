# GO IV Lab v0.2 candidate — Android manual IV app

Isolated branch: `candidate/v0.2-android-ui`, based on the previously tested `development/v0.1-foundation`. Neither main nor the foundation branch should be changed by this candidate.

## Candidate scope
- Native Kotlin Activity (no WebView, no root and no game automation)
- Offline search across 1,689 exact Pokémon / form identifiers
- Three appraisal IV controls (0 through 15)
- Little / Great / Ultra / Master CP-limit presets
- Level 50 (default) and explicit level 51 Best Buddy toggle
- Background rank calculation using existing Kotlin ranker, non-blocking UI
- Nickname template preview, 12-code-point warning and one-tap copy
- Local nickname template preference, no network or screen-capture permissions

## Explicit limitations
- Manual IV selection only: no screenshot scan, floating bubble or OCR
- Results are stat-product **IV spread rank for the selected species**, not PvPoke battle/meta rankings
- Level 51 requires an active Best Buddy boost in-game
- No special-cup legality checks; only the chosen CP cap is modeled
- Species absent from the pinned October 26 PvPoke snapshot cannot be guessed
- 12-character limits and special symbols should be checked inside Pokémon GO
- Master League uses no CP cap, so 15/15/15 is generally rank 1 for fixed level caps
- No checked-in secret signing keys; GitHub Actions debug APK uses CI's generated debug signing key
- This is a candidate, not verified on a Galaxy S26 Ultra until the user actually installs/tests it

## Acceptance
Run all Kotlin core unit tests, Android lint and assembleDebug on GitHub Actions. Check the APK contains the pinned offline `pokemon-stats.json` asset. Verify installation and manual reference cases on the target device before promoting to stable.
