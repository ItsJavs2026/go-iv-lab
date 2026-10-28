# Architecture decisions

- Android native Kotlin; min/target SDK to be selected and validated against current Android SDK/tooling.
- Modules planned: app, iv-engine, scan-core, nickname-core, data.
- IV engine must distinguish appraisal percentage from PvP stat-product rank.
- Pokémon data source must be versioned and license-reviewed; no scraping proprietary APK resources.
- Screen capture: Android MediaProjection with user consent, appropriate foreground service, and stop control.
- Overlay: SYSTEM_ALERT_WINDOW only after explicit permission; screenshot import works without it.
- Scanner uses normalized screen coordinates and bar segmentation, with uncertainty surfaced to the user.
- Nicknames: templates, saved profiles, symbol presets, length check, copy-to-clipboard; never automate Pokémon GO inputs.
- Privacy: no game login, no remote image upload by default, no accessibility automation.
