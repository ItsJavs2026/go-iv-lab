# Battle opponent recognition → GO Battle Lab history (design, not implemented)

## User goal
During Pokémon GO PvP, collect which **opponent Pokémon** appeared (up to three in standard trainer battles), persist their identities automatically, and review each matchup afterwards. Win/loss may be entered manually later. Prioritize no interaction while battling, low battery consumption, and reliable offline saves.

## Existing compatible project
The user's separate `ItsJavs2026/GO-Battle-Lab` repo already stores a battle journal in WebView localStorage `gobattlelab_v01`, with `state.battles` and fields `opponentLead`, `opponentSwap`, `opponentCloser`, `date`, `time`, `result`, `cup`, `teamId`, `teamName`, `lag`, `notes`. Its `quickRecord(result)` requires a user-confirmed win/loss, so **do not invent results** when importing recognized opponents. GO Battle Lab has import/export JSON backup in its Android app; it currently has no auto-capture.

## Stage A: native on-device journal
- Define `OpponentBattle` with stable local UUID, start/end timestamps, cup (optional), up to 3 ordered observations (species/form ID, display name, slot when confidently known, time first observed, detection confidence), `result: null | win | loss`, notes/lag, and recognition state `unreviewed | verified | corrected`.
- Persist atomically **as soon as each new opponent is confidently recognized**, not at the end of the match, so battles survive force stops and game crashes.
- Deduplicate within a battle using exact normalized species/form IDs, but never merge distinct matches. Keep uncertain/ambiguous form suggestions for review instead of silently choosing.
- Offer local history and editing, delete, and export. Only export data the user confirms, and include schema version and provenance.
- Preserve fast manual fallback while automated recognition is being tested.

## Stage B: opted-in recognition session
- After the user taps **Start opponent recognition**, request Android MediaProjection screen-sharing consent. A capture session is explicit and user-visible; don't turn it on automatically on game launch or after reboot.
- Android 14+ requires consent for **each** new capture session and permits a single `createVirtualDisplay` call per MediaProjection instance. Register `MediaProjection.Callback.onStop` and release ImageReader / Surface / VirtualDisplay immediately.
- On-device OCR reads Pokémon's opponent-side name or other stable visual identifiers. Validate text against pinned species IDs, including regional/Shadow names. Reject uncertain recognition.
- Only record **opponent-side** species: never mistake the player's own team, recommendation UI, or team preview for an observed opponent. Battle state is best-effort; if side cannot be determined, prompt for correction rather than auto-saving.
- Sample sparsely and only while user-approved capture is active; avoid retaining raw screenshots by default, and do not upload/stream screens.
- If detection confidence is insufficient, show a non-intrusive review badge after the match. Do not interrupt the battle.

## Stage C: GO Battle Lab bridge
- Generate a backward-compatible GO Battle Lab import file only after mapping native observations into `opponentLead/opponentSwap/opponentCloser`.
- **Never assign opponent switch vs closer based on appearance order unless confirmed.** Third observed Pokémon does not necessarily mean closer.
- Unknown win/loss remains missing in native journal. Existing GO Battle Lab v0.3 requires win/loss for some statistics; add an explicit unknown result state there before importing draft encounters, or export separate match observations until compatibility is verified.
- Do not access another app's private localStorage or modify its database directly.

## Non-goals for first IV overlay release
No screen recording, OCR, accessibility inspection, game automation, network traffic interception, opponent identification claims, or unattended capture in v0.4. The experimental floating IV window and first Samsung test must remain safe and independent.

## References
- Android MediaProjection: https://developer.android.com/media/grow/media-projection
- Android 14 consent: https://developer.android.com/about/versions/14/behavior-changes-14
- Existing GO Battle Lab data schema: https://github.com/ItsJavs2026/GO-Battle-Lab/blob/main/app/src/main/assets/index.html
