#!/usr/bin/env bash
# GO IV Lab v0.5 phone-only debug APK build, compatible with Android Code Studio.
# Builds locally on the Samsung: no GitHub Actions APK download, no root.
set -euo pipefail

SOURCE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORKSPACE="/storage/emulated/0/AndroidIDEProjects"
AAPT2="$HOME/.androidide/aapt2"
EXPECTED_BRANCH="experiment/evolution-family-v0.5"
EXPECTED_APPLICATION_ID="com.goivlab.app.overlaylab"
EXPECTED_VERSION_CODE=5
EXPECTED_VERSION_NAME="0.5.0-evolution-experiment"

fail() { echo "ERROR: $*" >&2; exit 1; }

echo "=== GO IV Lab 0.5 | Samsung phone-only APK build ==="
[ -d "$SOURCE/.git" ] || fail "Use the Git-cloned repository (not a ZIP)."
[ "$(git -C "$SOURCE" branch --show-current)" = "$EXPECTED_BRANCH" ] ||
  fail "Expected $EXPECTED_BRANCH; run git status and switch to that branch first."
[ -f "$SOURCE/app/build.gradle.kts" ] || fail "Android project is missing."
grep -Fq "versionName = \"$EXPECTED_VERSION_NAME\"" "$SOURCE/app/build.gradle.kts" ||
  fail "Wrong app version in source; do not build a different branch by mistake."
[ -x "$AAPT2" ] ||
  fail "Android-compatible AAPT2 missing at $AAPT2. Reuse the Android Code Studio / Orivra setup."
command -v java >/dev/null || fail "Java 17 is required."
java -version 2>&1 | grep -Eq 'version "17|openjdk 17' ||
  fail "Java must be version 17; check java -version."

SHORT="$(git -C "$SOURCE" rev-parse --short=8 HEAD)"
PROJECT="$WORKSPACE/GOIVLabV05_$SHORT"
echo "Source commit: $SHORT"
echo "Output workspace: $PROJECT"
mkdir -p "$WORKSPACE"
if [ -e "$PROJECT" ]; then
  fail "$PROJECT already exists. No existing build or data will be overwritten."
fi

# Choose a known working Gradle 8.9 wrapper from the owner's Orivra workspace.
WRAPPER_SOURCE=""
for candidate in Orivra414Live Orivra416Space Orivra407Full Orivra407; do
  root="$WORKSPACE/$candidate"
  if [ -f "$root/gradlew" ] &&
     [ -f "$root/gradle/wrapper/gradle-wrapper.jar" ] &&
     grep -Fq 'gradle-8.9-bin.zip' "$root/gradle/wrapper/gradle-wrapper.properties"; then
    WRAPPER_SOURCE="$root"
    break
  fi
done
if [ -z "$WRAPPER_SOURCE" ]; then
  if ! command -v gradle >/dev/null; then
    fail "Gradle 8.9 or an existing Orivra Gradle 8.9 wrapper is required."
  fi
  if ! gradle --version | grep -Eq '^Gradle 8\.9$'; then
    fail "The installed Gradle is not version 8.9. Do not change the project's dependencies."
  fi
fi

# Export tracked sources only, keeping SSH credentials/.git outside shared storage.
mkdir "$PROJECT"
git -C "$SOURCE" archive HEAD | tar -xf - -C "$PROJECT"
if [ -n "$WRAPPER_SOURCE" ]; then
  cp -R "$WRAPPER_SOURCE/gradle" "$PROJECT/gradle"
  cp "$WRAPPER_SOURCE/gradlew" "$PROJECT/gradlew"
fi

cd "$PROJECT"
echo "Building with Android Code Studio's native AAPT2..."
if [ -n "$WRAPPER_SOURCE" ]; then
  bash gradlew -Pandroid.aapt2FromMavenOverride="$AAPT2" \
    :app:assembleDebug --no-daemon --max-workers=2
else
  gradle -Pandroid.aapt2FromMavenOverride="$AAPT2" \
    :app:assembleDebug --no-daemon --max-workers=2
fi

APK="app/build/outputs/apk/debug/app-debug.apk"
METADATA="app/build/outputs/apk/debug/output-metadata.json"
[ -s "$APK" ] || fail "Gradle did not generate the APK."
[ -s "$METADATA" ] || fail "APK package metadata missing."
grep -Eq "\"applicationId\"[[:space:]]*:[[:space:]]*\"$EXPECTED_APPLICATION_ID\"" "$METADATA" ||
  fail "Wrong Android package ID; do not install."
grep -Eq "\"versionCode\"[[:space:]]*:[[:space:]]*$EXPECTED_VERSION_CODE([,[:space:]]|$)" "$METADATA" ||
  fail "Wrong APK version code; do not install."
grep -Fq "$EXPECTED_VERSION_NAME" "$METADATA" ||
  fail "Wrong APK version name; do not install."

echo
echo "SUCCESS: locally built and verified GO IV Lab Overlay Lab APK."
echo "Git source: $(git -C "$SOURCE" rev-parse HEAD)"
echo "Package: $EXPECTED_APPLICATION_ID"
echo "Version: $EXPECTED_VERSION_NAME"
echo "APK: $PROJECT/$APK"
if command -v sha256sum >/dev/null; then sha256sum "$APK"; fi
echo
echo "Use Samsung My Files > Internal storage > AndroidIDEProjects > GOIVLabV05_$SHORT"
echo "Then open app/build/outputs/apk/debug/app-debug.apk to install."
echo "No existing GO IV Lab or Orivra installations were changed."
