# GO IV Lab — public testing guide

**Status:** Experimental Android app. This is not an official Pokémon GO or Niantic product. The current candidate is v0.3, and a successful GitHub Actions build does not mean it has passed real-device tests.

## Download and verify
1. Open the [v0.3 Actions run](https://github.com/ItsJavs2026/go-iv-lab/actions/runs/40771861862) and download the **GO-IV-Lab-v0.3-popup-candidate** artifact ZIP.
2. Extract the APK from the ZIP. Do not install files sent by strangers or unofficial mirrors.
3. If Android reports that the app cannot be updated because of a signature mismatch, **do not uninstall your previous copy just to force an update**. Report the error first; uninstalling can erase saved settings.
4. This version is a manual, offline IV checker. It does **not** scan Pokémon GO screens, overlay other apps, access your Pokémon GO account, or automatically rename Pokémon.

## First 10-minute test
- Open the app and check the offline Pokémon/form database loads.
- Bulbasaur, 0/15/15, Great League, level 50: compare rank and CP against the documented v0.2 reference (rank #770, CP 1118).
- Alolan Diglett, 13/14/11, Great League, level 50: compare to the v0.2 reference (rank #125, CP 735). External parity remains unverified.
- Try both quick IV buttons, then manually adjust one IV and calculate.
- Verify the in-app popup opens, dismisses, and can be disabled with its toggle.
- Save, use, and delete a nickname template. Close and reopen the app and check persistence.
- Copy a valid nickname and paste into a text field. Try an invalid nickname longer than 12 code points and confirm copy is disabled.
- Change species while an old result is displayed; confirm the app does not show the old form's rank as a new result.
- Optional: turn off mobile data/Wi-Fi to confirm calculations remain offline.

## Reporting a bug
Use GitHub Issues with the **Bug report** template. Include app version, device model, Android version, exact species/form ID, IVs, league, level cap, steps, expected vs actual behavior, and whether the problem reproduces. For crashes, share a redacted log if available.

**Privacy:** Do not post Pokémon GO credentials, account identifiers, location data, or unredacted screenshots of private notifications. Do not share private signing keys or passwords. Do not enable extra Android permissions for this candidate.

## What is not ready
Screenshot recognition, floating overlays, evolution-chain comparisons, and independent parity for every species/form are future work. Please do not describe these as implemented.
