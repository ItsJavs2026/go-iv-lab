# GO IV Lab — Pokémon GO overlay architecture research

Status: research and design only. **No overlay implementation, permission or screen capture is included in v0.3.** Keep v0.2 stable and PR #3 isolated.

## Goal
A small, movable GO IV Lab result card visible while Pokémon GO is foregrounded. The first prototype must work with manually selected species/IVs and precomputed offline ranks. It must not inspect or modify Pokémon GO memory, input, files, network traffic or accessibility tree.

## Recommended staged design

### Stage A — floating manual result card (first)
- Separate candidate branch from v0.3 after that baseline is verified.
- Add `android.permission.SYSTEM_ALERT_WINDOW` only to that candidate; request access by launching `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`, and check `Settings.canDrawOverlays()` when resuming.
- User explicitly taps **Start overlay** from GO IV Lab; add a **Stop overlay** control and a clear persistent indication while active.
- `WindowManager.addView` with `TYPE_APPLICATION_OVERLAY`; compact draggable card, collapse/expand, close, safe screen-edge clamping.
- Keep overlay non-focusable when collapsed and never intercept Pokémon GO touches outside its bounds. Avoid covering important game buttons.
- Display cached, immutable result data produced by the offline rank engine; no Pokémon GO automation.
- Handle rotation, process death, permission revocation, display changes and duplicate-start attempts; release views and services reliably.
- Test Android 17 / Samsung One UI separately. Android and the foreground app may suppress overlays on sensitive screens; do not bypass such controls.
- Validate foreground service necessity, type, notification and background-start restrictions for the app's target SDK before implementation. Prefer initiating while GO IV Lab is visible.

### Stage B — screenshot-assisted manual scan (separate candidate)
- Only after Stage A works: user explicitly starts a MediaProjection capture session and sees the Android system consent dialog.
- Android 14+ requires fresh consent per capture session and one `createVirtualDisplay()` call per MediaProjection instance; register onStop and release resources.
- Handle app-window versus full-display selection, orientation and scaling, secure/blank captures, notification/PII exposure, and cancellation.
- Run OCR on-device, preview recognized species/IVs for user confirmation, then calculate. Never upload screenshots by default.
- No AccessibilityService, no automatic taps or in-game actions.

### Stage C — quality
- Test app foreground/background, switching into/out of Pokémon GO, touch passthrough, orientation, game FPS, thermal load and battery drain.
- Confirm behavior on multiple Android versions and manufacturers before any public release.
- Review Niantic/Pokémon GO terms and Google Play policy separately; technical capability does not guarantee distribution or game-policy acceptance.

## Official references
- Android overlays: https://developer.android.com/reference/android/view/WindowManager.LayoutParams#TYPE_APPLICATION_OVERLAY
- Overlay permission: https://developer.android.com/reference/android/provider/Settings#canDrawOverlays(android.content.Context)
- Android MediaProjection: https://developer.android.com/media/grow/media-projection
- Android 15 background service restrictions: https://developer.android.com/about/versions/15/behavior-changes-15
- Google Play sensitive permissions: https://support.google.com/googleplay/android-developer/answer/16558241

## Acceptance criteria for first overlay prototype
1. Explicit user permission and start; clear stop; no unexpected permission requests.
2. A draggable, readable popup appears over a test app and, where Android permits, over Pokémon GO.
3. Overlay does not block gameplay outside its visible bounds; minimize, dismiss, and restore work.
4. Existing offline ranking and nickname clipboard continue to work.
5. No screenshots, game automation, internet access or accessibility permission.
6. No crashes, leaked windows, or stranded overlays after permission revocation or app shutdown.
7. Tested on a physical Samsung Galaxy S26 Ultra Android 17; CI compilation alone is insufficient.
