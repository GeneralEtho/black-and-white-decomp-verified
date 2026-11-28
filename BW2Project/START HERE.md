# Black & White 2 Project

Personal decompilation and modding project. You direct and judge; the AI builds and reports.
Full plan: `08_notes/PHASE1-PLAN.md` (Phase 0 is done; its plan was a Claude doc).

## Folders, in the order you'll use them

| Folder | What goes in it | Who touches it |
| --- | --- | --- |
| 01_incoming | Your downloads and disc images | You drop files in; the AI sorts them |
| 02_game | The playable game install | You install here; you play-test here |
| 03_vault | Untouched originals + fingerprints. The answer key | Nobody, once it's filled. Read-only |
| 04_checks | The PASS/FAIL tests | Only you approve changes |
| 05_work | Everything the AI builds | The AI |
| 06_reports | Plain-English results for you to read | The AI writes, you read |
| 07_reference | Community docs and tools (openblack etc.) | The AI fetches |
| 08_notes | Progress checklist and decisions log | Both |

## Your next steps
1. Download the files listed in `01_incoming/README.md`.
2. Install the game into `02_game` (see its README).
3. Tick things off in `08_notes/PROGRESS.md`, or just tell Claude what's done.

## Golden rules
- Never edit anything in `03_vault`.
- A FAIL is fixed by fixing the work, never by changing the check.
- If the AI asks to change something in `04_checks`, treat it as a red flag and get a second review.
