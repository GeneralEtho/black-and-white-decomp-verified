# Automation toolkit: what exists and what to build

Goal: the owner can't read code or judge correctness, so every claim the AI makes must be checked by a machine against
something the AI can't change (the original game), and progress must show up as objective numbers.
Second goal: minimize hand work for both the owner and the AI.

## Built
| Tool | What it does | Status |
| --- | --- | --- |
| `BW2Project/05_work/tools/bwkit/bwkit.py verify-vault` | Re-hashes all 6 vault snapshots + the fingerprint lists themselves. PASS/FAIL. | Working (PASS, ~2 min). Tamper test caught a 1-word change |
| `bwkit fingerprint / diff` | Fingerprint any folder; compare two fingerprint lists | Working |
| `bwkit snapshot` | Resumable copy that survives command timeouts | Working |
| `bwkit inspect-exe` | Encrypted vs readable, build date, sections, RTTI count | Working on all 3 exes |
| `bwkit mac-symbols` | Counts symbols in the Mac binary | Working (195,744 defined) |
| registry scan / fix .bat + .reg | Diagnose and fix install-path problems | Used once, worked |

## To build (order fixed by PHASE1-PLAN.md; one task card each in BW2Project/08_notes/tasks)
1. **bootstrap-cloud.sh / stage-inputs.sh / bootstrap-device.sh** (T01): rebuild the toolchain in the cloud container in < 10 min; stage the exes + Mac binary from the PC with hash checks against the committed fingerprints. (The PC's Linux VM cannot host Ghidra: 3 GB RAM, Java 11, home wiped per session.)
2. **bwcheck** (T02): runs everything in 04_checks; refuses if vault fingerprints or the checks manifest differ from git HEAD; runs each check's canaries; writes PASS/FAIL reports and the dashboard JSON.
3. **bwstuff + check C-03** (T03): the Phase 0 round-trip check, proven against openblack's extractor and the vault hash.
4. **Ghidra headless census** (T05): deterministic analysis of both exes + Mac, RTTI/vtables, native tables, JSON export; .gzf archives cached on the PC.
5. **Fan-patch delta map** (T04): stub emulation (Unicorn) and a one-click memory dumper must agree on every byte the fan patches change at runtime.
6. **Name-transfer engine v1** (T06): natives (1,092 certain), vtable alignment (1,871 classes), strings/constants, TU segmentation -> name-map.json with tiers + coverage report.
7. **Dashboard** (T07): numbers only from scripts, each with its source file and hash.
8. **Name-transfer v2** (T08): call-graph propagation, BinDiff second opinion, Sonnet comparison batches (one signal only), verifier-seeded holdout precision check C-07 with a stop rule.
9. **BotG port** (T09): base -> BotG by byte-identical hashing + structure; BotG native table as holdout.
10. **Hook framework + one-click test runner** (T10): ASI loader + bw2hook.asi (MinHook), exe-hash guard, ini toggles, logging, scenario driver, RUN-TEST.bat.
11. **A/B harness + record/replay** (M6 cards): original vs reimplementation on live calls; traces replayed offline as a check.

## Not planned
- Local LLM on the 4090 for real work: owner doesn't trust its output (decision 2026-10-22). Claude tiers only; cheap tiers (Haiku/Sonnet) for bulk.
