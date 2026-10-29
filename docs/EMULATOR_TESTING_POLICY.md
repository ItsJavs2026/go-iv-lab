# GO IV Lab — automated testing policy

## Goal
Do not ask the owner to install another APK to discover basic regressions. Use GitHub Actions tests on ordinary development changes, reserve Samsung Galaxy S26 Ultra Android 17 testing for meaningful milestones and issues that require physical Pokémon GO/One UI behavior.

## Two emulator tiers

| Tier | Trigger | Android emulator | Checks | Owner action |
| --- | --- | --- | --- | --- |
| **Routine** | PR opened/updated with Android source, engine, data, or build changes; pushes to `main` affecting code | Android 15 (API 35) | Native popup, FLOAT and nickname callbacks, foreground service/window attachment, visible result above emulator Home, close action | No APK installation |
| **Milestone** | Add the `milestone-test` PR label, or manually dispatch workflow with `tier=milestone` after the workflow is available on the default branch | Android 15 and 16 (API 35/36) | Routine tests **plus** real 4,096-IV ranking through the Android Activity, popup reopen, compiled SDK tests | Only after both emulator and build CI pass, decide whether to test on Samsung |

The routine emulator uses a synthetic, clearly identified Bulbasaur **result display fixture** rather than recalculating all 4,096 spreads. Separate JVM tests cover ranking math. The milestone emulator executes the real offline engine. A synthetic result in smoke tests is never evidence of independent IV rank parity.

GitHub-hosted x86_64 emulators are **not** Samsung hardware. They do not verify:
- Pokémon GO foreground overlay visibility, game interaction or anti-cheat compatibility.
- One UI or Samsung Android 17 background process/overlay behavior.
- Samsung screen cutouts, device thermal conditions, responsiveness, real battery drain, or touch passthrough during actual gameplay.

## Faster iteration and resource controls
- Gradle cache and Android Virtual Device snapshot cache reduce repeated setup.
- PR changes containing only documentation don't need Android emulation.
- CI concurrency cancels superseded runs on the same branch/PR.
- Routine is one emulator and short tests. The slower two-version matrix runs only for milestone requests.
- GitHub Actions usage is billable or quota-dependent; use the Actions tab to watch minutes and artifact retention.
- External fork PRs are deliberately excluded from automatic emulator runs on this draft setup, to avoid consuming unreviewed CI capacity.
- If a run fails, inspect its precise test and error log. Never label an APK device-tested because compilation succeeded.

## Readiness checklist for a Samsung install
1. Latest commit has green JVM tests, build/lint, manifest permissions, and packaged APK identity verification.
2. Routine Android emulator UI tests pass for the **same** commit.
3. Milestone emulator checks pass for a build intended for the owner's phone (both API levels when available).
4. Owner-approved experimental APK is separately installed as **GO IV Lab Overlay Lab** so the stable app stays intact.
5. Confirm floating window over actual Pokémon GO, minimized touches, foreground notification, stopping, permission revoke, game responsiveness and nickname clipboard.

## Important implementation notes
- The Android emulator workflow lives at `.github/workflows/android-overlay-emulator.yml`, test scripts at `.github/scripts/run_emulator_tests.sh`.
- UI smoke code is `app/src/androidTest/java/com/goivlab/app/PopupAndOverlaySmokeTest.kt`.
- Milestone Android test is `app/src/androidTest/java/com/goivlab/app/MilestoneRankEndToEndTest.kt`.
- The workflow currently lives in the experimental v0.4 branch. GitHub's manual workflow_dispatch UI generally requires the workflow file to exist on the repository's default branch. Until this testing infrastructure is merged or otherwise made available on `main`, the PR label trigger is the practical way to request milestone checks for this draft.
- Do not merge v0.4 product/permission changes into the stable app merely to enable workflow dispatch.
