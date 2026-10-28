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
