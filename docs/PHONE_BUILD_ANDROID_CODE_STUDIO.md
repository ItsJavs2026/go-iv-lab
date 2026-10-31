# Build GO IV Lab v0.4 directly on your Samsung via Android Code Studio

**No GitHub Actions APK download.** Source is cloned from GitHub via the same SSH setup already used for Orivra, and compiled natively on the phone. The resulting APK remains in Samsung Internal Storage.

Prerequisites from the known-good Orivra setup:
- Android Code Studio terminal on Samsung Galaxy S26 Ultra
- Java 17
- Gradle 8.9 via the existing `AndroidIDEProjects/Orivra414Live` Gradle wrapper, or installed `gradle` version 8.9
- Native AAPT2 executable at `$HOME/.androidide/aapt2`
- GitHub SSH key already authenticated, phone storage and Android SDK setup from Orivra

## First v0.4 overlay build

Open **Android Code Studio → Terminal**, then paste:

```bash
cd ~
git clone --branch experiment/overlay-manual-v0.4 --single-branch git@github.com:ItsJavs2026/go-iv-lab.git GO-IV-Lab-Overlay
bash ~/GO-IV-Lab-Overlay/scripts/build_goivlab_android_code_studio.sh
```

The script verifies source branch, Java, native AAPT2, Gradle wrapper/version, APK package and version metadata. It only exports files tracked by the commit. It refuses to overwrite any existing build folder. Source stays under `~/GO-IV-Lab-Overlay`.

On success:
`Internal storage/AndroidIDEProjects/GOIVLabV04_<commit>/app/build/outputs/apk/debug/app-debug.apk`.

Open that file in **Samsung My Files** to install `GO IV Lab Overlay Lab` with package `com.goivlab.app.overlaylab`. This is a separate Android app from the stable `com.goivlab.app` installation.

## Future v0.4 code updates

```bash
cd ~/GO-IV-Lab-Overlay
git pull --ff-only
bash scripts/build_goivlab_android_code_studio.sh
```

Each new commit goes into a distinct workspace folder, preserving older APKs and avoiding accidental overwrites. If you already cloned the repository, use the update commands, not `git clone` again.

## Current test procedure
1. Calculate Bulbasaur 0/15/15, Great League, level 50.
2. Tap **FLOAT**. Grant **Draw over other apps / Appear on top** to `GO IV Lab Overlay Lab` and return.
3. Use **OPEN POKÉMON GO**. Verify drag/minimize/expand/close and that gameplay outside the popup remains touchable.
4. If it fails, use **COPY DIAGNOSTICS** in Overlay Lab and share the text.

The emulator passed on API 35, but actual Pokémon GO + Samsung Android 17 remains a physical device test. No screenshot recognition or opponent tracking in v0.4.

## When something fails
- **Permission denied with Git clone:** Run `ssh -T git@github.com` and confirm the GitHub SSH key from Orivra is still available. Repository is public and HTTPS clone also works.
- **AAPT2 syntax error:** Do not use the Linux Maven AAPT2 binary; the script uses the AndroidIDE-native `$HOME/.androidide/aapt2`, exactly as in Orivra.
- **Missing Gradle wrapper:** Ensure the Orivra414Live project and its Gradle 8.9 wrapper are still present or that `gradle --version` reports Gradle 8.9.
- **APK install conflicts:** Do not uninstall the stable GO IV Lab. v0.4 and v0.5 local builds use the same *experimental* package; a previously installed cloud-built experiment with a different signing key may conflict. Preserve data and check which build is installed before replacing.

Do not share or commit SSH keys, signing keys, APK private user data, or personal battle logs.
