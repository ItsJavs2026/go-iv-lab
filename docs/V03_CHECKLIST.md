# GO IV Lab v0.3 — Popup and presets candidate

Source branch: candidate/v0.3-popup-presets. Based on user-tested v0.2; main and v0.2 branch unchanged.

## Implemented in source
- Native in-app AlertDialog displaying species/form, IV spread rank, CP, level, stat product, and optional nickname copy.
- Auto-open popup after a completed calculation (enabled by default, saved toggle); manual popup button still available.
- Compact spacing to reduce scrolling; v0.3 label shown in UI.
- Results popup disabled whenever a stale calculation is invalidated.
- Locally saved nickname template presets (up to 12), selection, save, deletion.
- Same offline engine and user-tested clipboard pathway.
- Version 0.3.0-candidate; no new permissions.

## Not yet verified
- Latest commit CI Android compilation, lint and artifact (previous commit passed; rerun pending).
- On-device dialog layout, copy behavior and template persistence.
- Accessibility, small-screen overflow and keyboard interaction.
- Independent ranking parity for Alolan Diglett.
- Evolution comparison and screenshot recognition, intentionally deferred.

## Device regression plan
1. Bulbasaur 0/15/15 GL level 50 → rank 770, CP 1118; confirm popup auto-opens and closes. Disable auto-open, calculate again, then use manual popup button.
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

## Search correctness and IV quick actions
- Species lookup at calculation time uses current visible search text, not a stale previously selected ID.
- The popup shows the exact species/form ID alongside the display name.
- Quick IV buttons set 0/15/15 and 15/15/15; calculation must still be explicitly requested.
- Regression: select Alolan Diglett, edit search to another form without selecting a suggestion, then calculate; never show the old Diglett result.
- Regression: choose the same form twice, edit IVs rapidly, and verify only the latest calculation may populate the popup.
- Regression: verify all quick IV buttons update the visible sliders and invalidate any previous result.

## Public repository verified
- Repository visibility checked via GitHub repository metadata: public.
- Keep draft PR #3 unmerged until the exact release candidate passes CI and the first round of device feedback.
- Public tester docs: docs/PUBLIC_TESTING.md; issue forms under .github/ISSUE_TEMPLATE.
- Important: the successful Actions run 40771861862 is for an earlier commit; do not confuse it with the latest candidate.
