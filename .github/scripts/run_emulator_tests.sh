#!/usr/bin/env bash
set -euo pipefail

tier="${1:-routine}"
case "$tier" in
  routine)
    echo "Running fast Android UI popup + overlay smoke checks"
    gradle :app:connectedDebugAndroidTest --no-daemon --stacktrace \
      -Pandroid.testInstrumentationRunnerArguments.class=com.goivlab.app.PopupAndOverlaySmokeTest
    ;;
  milestone)
    echo "Running fast Android UI checks before full ranking integration"
    gradle :app:connectedDebugAndroidTest --no-daemon --stacktrace \
      -Pandroid.testInstrumentationRunnerArguments.class=com.goivlab.app.PopupAndOverlaySmokeTest
    echo "Running slow, genuine offline ranking through the native Android UI"
    gradle :app:connectedDebugAndroidTest --no-daemon --stacktrace \
      -Pandroid.testInstrumentationRunnerArguments.class=com.goivlab.app.MilestoneRankEndToEndTest
    ;;
  *)
    echo "Unknown test tier: $tier. Expected routine or milestone." >&2
    exit 2
    ;;
esac
