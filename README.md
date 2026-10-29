# GO IV Lab

Experimental, offline-first Android **Pokémon GO PvP IV rank calculator**. This is a community project, not affiliated with Niantic, Pokémon GO, or PvPoke.

## What works today
- Offline species/form search and manual 0–15 IV input.
- Little, Great, Ultra, and Master League IV spread rankings; level 50/51 choices.
- Nickname formatting and clipboard copy.
- Candidate v0.3 adds an in-app result popup, saved nickname templates, and quick IV presets.

**Important:** IV spread ranking is not battle performance or cup eligibility. Evolution comparisons, automatic screenshot recognition, and floating overlays are **planned**, not currently implemented.

## Test the candidate
- [Public testing guide](docs/PUBLIC_TESTING.md)
- [Candidate checklist](docs/V03_CHECKLIST.md)
- [v0.3 APK artifact from successful Actions run](https://github.com/ItsJavs2026/go-iv-lab/actions/runs/40771861862)
- [Report a bug or suggest a feature](../../issues/new/choose)

The APK artifact is a ZIP downloaded from GitHub Actions. This is an experimental debug build; install at your own discretion. A debug signing mismatch may prevent installing it over a previous build.

## Development
Development proceeds through draft pull requests. The user-tested v0.2 branch is preserved, and v0.3 changes are isolated in `candidate/v0.3-popup-presets`.

The bundled species data is pinned to a PvPoke game-master snapshot dated October 26, 2026; preserve its provenance, licensing, and attribution when redistributing. See repository source and verification scripts for the exact pin.

Do not submit Pokémon GO credentials, private keys, location details, or personal information in issues.
