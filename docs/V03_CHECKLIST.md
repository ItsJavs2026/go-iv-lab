# GO IV Lab v0.3 — Popup and presets candidate

Source branch: candidate/v0.3-popup-presets. Based on user-tested v0.2; main and v0.2 branch unchanged.

## Implemented in source
- Native in-app AlertDialog displaying species/form, IV spread rank, CP, level, stat product, and optional nickname copy.
- Results popup disabled whenever a stale calculation is invalidated.
- Locally saved nickname template presets (up to 12), selection, save, deletion.
- Same offline engine and user-tested clipboard pathway.
- Version 0.3.0-candidate; no new permissions.

## Not yet verified
- CI Android compilation, lint and artifact.
- On-device dialog layout, copy behavior and template persistence.
- Accessibility, small-screen overflow and keyboard interaction.
- Independent ranking parity for Alolan Diglett.
- Evolution comparison and screenshot recognition, intentionally deferred.

## Device regression plan
1. Bulbasaur 0/15/15 GL level 50 → rank 770, CP 1118; open popup and close.
2. Alolan Diglett 13/14/11 GL level 50 → compare app result to v0.2 (#125, CP 735), external rank parity still pending.
3. Change IV while popup result exists: popup must disable until recalculated.
4. Save `{league}{rank}` and `{name}` presets; switch between them; restart app and confirm persistence.
5. Copy nickname from popup; paste manually in another app.
6. Try long nickname (>12 Unicode code points): copy must stay disabled.

## Expanded release gates — do not ask for another install prematurely
- Preserve the known-working v0.2 APK and its exact original debug signing identity. The v0.3 GitHub debug signing key may differ between runners; if Android reports a signature conflict, do **not** delete v0.2 without exporting any important app settings and deciding on a signing plan.
- Verify the **latest** v0.3 head SHA in GitHub Actions, not an older green commit.
- Ensure all Kotlin tests, pinned data checks, Android lint, and APK packaging pass.
- Validate no INTERNET, SYSTEM_ALERT_WINDOW, AccessibilityService, or MediaProjection permission has slipped into the merged manifest.
- Verify the offline catalog is packaged and a fresh installation can calculate without mobile data.
- Run a regression matrix for Little/Great/Ultra/Master, both level caps, regional forms, nickname length, template persistence, and popup invalidation.
- Check that the popup Copy action copies its displayed nickname, not a stale or newer value.
- Confirm popup can be dismissed and the UI stays responsive during rapid species/IV changes.
- Check long names, empty preset lists, repeated save/delete, rotation and keyboard resizing.
- Keep screenshot recognition and evolution comparisons separate until data and interaction design are verified.

## Device feedback log
- v0.2: Bulbasaur 0/15/15 GL L50 showed rank 770, CP 1118 (user screenshot).
- v0.2: Alolan Diglett 13/14/11 GL L50 showed rank 125, CP 735 (user screenshot; external rank parity still unconfirmed).
- v0.2: User confirmed nickname clipboard copy works.
- v0.3: No on-device tests yet. Do not infer them from CI success.
