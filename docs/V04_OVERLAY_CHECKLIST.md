# v0.4 manual overlay experiment — verification gates

**Branch:** `experiment/overlay-manual-v0.4` / draft PR #4. Keep v0.2 stable and the v0.3 candidate unmodified.

## Both primary UI paths

**In-app popup (v0.3):** Calculate a rank, open the in-app dialog, view exact form/league/rank/IVs/CP/level/stat product, copy a valid nickname, close. The new **FLOAT** dialog action passes its immutable, currently valid result to the overlay pathway.

**Floating result (v0.4):** User starts from the visible GO IV Lab app, explicitly grants Draw over other apps, taps FLOAT again, and sees the result above other apps. Includes draggable header, collapse/expand, close, optional in-overlay nickname copy, and a persistent overlay foreground-service notification with Stop action. The overlay never captures screens or interacts with Pokémon GO internals.

## Automated pre-install gates
- [ ] Exact commit passes app:testDebugUnitTest, iv-engine tests, nickname-core tests, app:lintDebug, app:assembleDebug.
- [ ] Packaged manifest contains SYSTEM_ALERT_WINDOW, FOREGROUND_SERVICE and FOREGROUND_SERVICE_SPECIAL_USE; no INTERNET, accessibility, media-projection, camera or audio permissions.
- [ ] Overlay service is exported=false, FGS specialUse type declared with explanation.
- [ ] APK includes pinned offline Pokémon stats; no external requests added.
- [ ] Workflow captures APK SHA-256 and signing certificate details.
- [ ] Candidate builds labeled v0.4; no stable v0.2/v0.3 artifact mislabeled.
- [ ] Existing core rank tests pass for known species/form cases.
- [ ] Standalone unit tests confirm exact form ID and requested IV information in overlay.

## Physical Samsung Galaxy S26 Ultra / Android 17 tests (not yet done)
1. Calculate Bulbasaur 0/15/15 Great League level 50; confirm v0.2 reference #770, CP 1118. Confirm in-app rank dialog and nickname copy unchanged.
2. Tap FLOAT from the **in-app popup**. If permission is missing, visit Android Settings after tapping FLOAT, grant Draw over other apps, and return; only the one pending user-initiated request may resume automatically. Verify denial does not start anything.
3. Verify foreground-service notification appears and its Stop action dismisses the card.
4. Open Pokémon GO: overlay should remain visible in supported situations, while taps outside card reach the game; no extra screenshot or accessibility permission appears.
5. Drag card along edges, minimize, expand, close and reopen. Ensure no off-screen card, stuck window or duplicate overlays.
5a. For valid nickname template, tap COPY NICKNAME inside the game overlay and paste it manually into a test text field. Invalid or >12-character nickname must not show a copy action. Collapsed card must hide the copy action.
6. Reopen GO IV Lab, change IV, Pokémon/form, or league; obsolete floating results must be dismissed.
7. Quickly repeat Start/Stop and calculate; check no crashes or old results returning.
8. Rotate screen or change display size if practical; check visible position and close access.
9. Revoke Draw over other apps with card active; app should stop gracefully rather than crash.
10. Observe battery consumption, Pokémon GO responsiveness, background/notification behavior and what happens if Android stops the service.
11. If Android blocks overlay on some screens, record the behavior; do not attempt to bypass protection.

## Constraints and limitations
- No screenshot scanning, OCR, auto detection or game automation in this experiment.
- No automatic re-start from background, boot, network, or Pokémon GO app launch.
- Android can suppress overlays on sensitive screens. Samsung-specific behavior must be physically verified.
- A foreground service is user-visible and may require notification handling; user can stop at any time.
- Clipboard action writes only a valid generated nickname; it does not read clipboard content.
- Debug signing certificate may differ from an earlier APK: **do not uninstall stable builds solely to force this experiment over them**.
- This branch is not approved for public release until physical testing and policy review.

## Known gaps
- First-use overlay permission resumes one pending user-initiated FLOAT request on returning to the app; cancellation must leave the overlay off.
- Floating card is manual result-only. The long-term screenshot/recognition workflow is a separate future candidate.
- No CI or emulator can prove that Pokémon GO actually shows the overlay on this specific Samsung firmware.
