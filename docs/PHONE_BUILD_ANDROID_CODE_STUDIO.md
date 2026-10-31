# Build GO IV Lab v0.5 directly on Samsung using Android Code Studio

The v0.5 experiment includes the v0.4 **manual IV floating overlay** and separate **EVOLUTIONS** comparison: same IVs across verified stages, with pinned Open League PvPoke battle ratings. No screenshots, opponent recognition or game manipulation.

Use your Orivra-proven Android Code Studio setup: Java 17, Gradle 8.9 (existing `AndroidIDEProjects/Orivra414Live` wrapper is supported), GitHub SSH, and `$HOME/.androidide/aapt2`.

## First v0.5 phone build

```bash
cd ~
git clone --branch experiment/evolution-family-v0.5 --single-branch git@github.com:ItsJavs2026/go-iv-lab.git GO-IV-Lab-Evolutions
bash ~/GO-IV-Lab-Evolutions/scripts/build_goivlab_android_code_studio.sh
```

After successful compilation use **Samsung My Files**:
`Internal storage/AndroidIDEProjects/GOIVLabV05_<commit>/app/build/outputs/apk/debug/app-debug.apk`.

It installs as `GO IV Lab Overlay Lab` (`com.goivlab.app.overlaylab`), separate from the stable app. v0.4 and v0.5 are **not side-by-side with each other**: they share the experimental package ID, deliberately so a locally signed v0.4 can be upgraded to locally signed v0.5 using the same phone debug keystore. GitHub/cloud-built experimental APKs may use a different signing key.

## Subsequent commits

```bash
cd ~/GO-IV-Lab-Evolutions
git pull --ff-only
bash scripts/build_goivlab_android_code_studio.sh
```

The script exports only tracked source into a new commit-specific `AndroidIDEProjects/GOIVLabV05_<commit>` folder and refuses to overwrite older workspaces. It validates the experimental package and version before reporting success.

## What to verify
- Existing rank popup still calculates correct IVs and copies nickname.
- FLOAT and OPEN POKÉMON GO work on the Galaxy S26 Ultra; minimize, drag and close do not trap gameplay touch input.
- EVOLUTIONS compares real first/middle/final stages and branching alternatives. It separates relative IV quality from PvPoke Open League simulated battle rating (not the special-cup meta).
- Failures: use COPY DIAGNOSTICS and report the app version, error class and steps. Do not share account data.

Full evolution notes: `docs/EVOLUTION_COMPARISON.md`. Automatic opponent capture is not yet implemented; the separately planned `docs/OPPONENT_TRACKING_DESIGN.md` requires screen-sharing consent.

No root, GitHub Actions APK download, or Android phone-to-PC connection is required.
