# Black & White 2: verified reverse-engineering notes, name map and tools

Work in progress. A verification-first effort to understand Lionhead's **Black & White 2** (PC, 2005, v1.2 build 1448) and its expansion **Battle of the Gods**, aimed at fixing long-standing bugs (hand/mouse input, UI scaling, draw distance) and making the game properly moddable. Every claim is backed by a machine check against the original game.

**What makes this different from a pile of notes:** nothing here is taken on anyone's word, human or AI. Originals are fingerprinted; checks are protected and have canaries; the model that writes a result never grades it; every published name will carry its evidence and a measured confidence.

Source: commit 7d8d56fd61520d723743b3450b3ad12151efa6d0 of the private repository, built 2026-11-21.

## Status (2026-11)
Phase 0 (archive, fingerprints, research) is complete. Phase 1 is under way. It builds a named, cross-referenced map of `white.exe` from the Mac port's C++ symbols, a hook framework for running our code next to Lionhead's and comparing the two, and the check machinery. This mirror is a snapshot of the results that have passed their checks; the working record stays in the private repository for now (see "Not on the mirror yet" below).

Already established in the private repository's facts file (FINDINGS.md, on this mirror):
- The Mac port's binary keeps **195,745 symbols** with full C++ signatures and corresponds to the **base game** (not the expansion).
- The script-native table in `white.exe` is `{fn, in, out, unknown, name[128]}` (144 bytes/entry), identical on the Mac side: **1,106 entries, 1,102 with a pointer (native_table.py)** before any clever matching.
- `white.exe` keeps RTTI: 2,781 type descriptors; 98.3% of its non-template classes exist in the Mac symbols, 1,871 of them with a vtable on both sides.
- Compiler: Visual C++ 7.1, build 13.10.3077. Community fan patches ship decrypted executables; the on-disk differences from the official build are documented.

Names: The combined v1.42 name file gives 10,988 of the 55,682 functions found in white.exe v1.42 a name, 19.73 percent, after the 4 rows listed under Errata were withdrawn (combine_v142.py over 7 tables, joined by script and never by hand). 9,426 come from the vtable method (the Mac port's C++ symbols paired slot by slot with the Windows vtables of the same class, six batches), 1,348 from a string used by exactly one function on each side (the table's count; 4 of those rows are withdrawn), 64 from a string used by exactly two functions on each side with one of them already named, 15 from call lists of equal length whose first and last callee were already named, 80 from a set of two or more already named callees that is the same on both sides and occurs once on each side, 38 from the body matcher's top band and 24 from the sole caller rule's strict band; 3 functions two methods reached carry the same name and count once. Every row's status says which method and batch named it. The combined files are the verified tier: a name is in it because the method or band that produced it read 95 of 100 or better on a reader's holdout, and a single name can still be wrong, in which case it is withdrawn and listed under Errata and in CORRECTIONS.md. A second session reproduced each table from the same inputs with its own code, and random holdouts read against decompiled bodies found 3 wrong of 77 readable (vtable batch 1), 4 of 73 (batch 2, all four in one band of Object's vtable, withdrawn and renamed under a tested rule), 0 of 42 and 0 of 35 (batch 4), 24 consistent of 24 (batch 5), 0 of 86 (batch 6), 0 of 99 (unique strings, from which 4 rows were later found wrong by screens of every verified address and withdrawn, listed under Errata), 0 of 62 (string pair), 0 of 13 (anchored calls) and 0 of 80 (callee set, every row). The transfer of Mac names to v1.42 is at its plateau: eight readings of the two binaries have run, the string method was the last to add more than 1 percent of the functions, and every signal since gave under 100 names or failed its check, so this count stands until a method of a new kind is designed. The vtable table, BW2Project/05_work/analysis/t06/named-functions.tsv, names functions from the Mac port's C++ symbols by pairing vtable slots class by class (the rules are in BW2Project/05_work/tools/names/t06/docs/vtable-rules.md). The string table, BW2Project/05_work/analysis/t08/c1/named-functions.tsv, pairs a function with its Mac twin through a string that occurs once in each binary and is referenced from exactly one function on each side. The two T08C2 tables, BW2Project/05_work/analysis/t08/c2/pair/named-functions.tsv and BW2Project/05_work/analysis/t08/c2/calls/named-functions.tsv, hold the string pair and anchored calls names. The callee set table, BW2Project/05_work/analysis/t08/c3/sets/named-functions.tsv, pairs a function with its Mac twin through the set of its already named direct callees, two or more, equal on both sides and occurring once on each side. The body matcher's top band table, BW2Project/05_work/analysis/t08/d1/verified/named-functions.tsv, holds 38 rows (blind test 1 of 1, holdout 38 of 38, every row), and the sole caller rule's strict band table, BW2Project/05_work/analysis/t08/d2/verified/named-functions.tsv, holds 24 rows (blind tests 17 of 17 (executor), 13 of 13 (verifier), holdout 23 of 23 readable of 24, every row). The combined Battle of the Gods name file, BW2Project/05_work/analysis/names/botg/named-functions.tsv, gives 10,311 of the 54,314 functions found in the fan patched BattleOfTheGods.exe (build 1530) a name, 18.98 percent, after the 2 rows listed under Errata were withdrawn (combine_botg.py over three tables, joined by script and never by hand): 9,159 by the vtable method (BW2Project/05_work/analysis/t09/named-functions.tsv: the Mac port's C++ symbols paired slot by slot with the BotG vtables, then the 349 vtables BotG lengthened aligned to v1.42's through byte identical bodies) and 1,105 carried from the v1.42 name file where a function's body, with its relocations masked, is byte identical and unique on both builds (BW2Project/05_work/analysis/t09/b3/named-functions.tsv; before it named anything the carry predicted 2,384 of the 9,159 names already held and got 2,383 right), and 49 more carried the same way in November 2026, out of the 62 rows the v1.42 file gained from the body matcher's top band and the sole caller rule's strict band (BW2Project/05_work/analysis/t09/b5/named-functions.tsv; before it named anything the carry's blind test was run again and predicted 2,384 of the 9,159 names already held, 2,383 of them right). The 1,105 is the first carry table's count, and 2 of its rows are withdrawn. The combined files are the verified tier: a name is in it because the method or band that produced it read 95 of 100 or better on a reader's holdout, and a single name can still be wrong, in which case it is withdrawn and listed under Errata and in CORRECTIONS.md. A second session reproduced each table from the same inputs with its own code, and random holdouts read against decompiled bodies found 1 wrong of 70 readable (classes with a Mac twin), 0 of 23 (BotG only classes named through a base class), 0 of 88 (the alignment) and 0 of 100 (the carry, every row read against the bodies of both builds). Battle of the Gods is at its plateau by these methods: the v1.42 names are carried wherever bodies match, and what is left is either BotG only code or code that is not byte identical between the builds, so this count stands until a method of a new kind is designed. The status column says which batch and rule produced each name (claimed, T06B1, T06B2, T06B4 (band), (rule 12), (rule 17), T06B5 (rule 17), T06B5 (rule 18); claimed, T08C1 (string), T08C2 (string pair), T08C2 (anchored calls), T08C3 (callee set), T08D1 (band A), T08D2 (band a); claimed, T09B1, (rule 17), (BotG only), (PC only), T09B2 (via v1.42), T09B3 (body), T09B5 (body)); rows marked folded carry several names because one Windows body serves several methods; a class whose slot counts differ between the builds is listed in class-counts.tsv and names nothing. Nothing here comes from any other project's name lists.

## What is here
| Path | Contents |
| --- | --- |
| `BW2Project/05_work/analysis/names/v142/named-functions.tsv` | The combined v1.42 name file, 10,988 functions: the vtable, string, string pair, anchored calls, callee set, body matcher and sole caller tables joined by script, minus the rows of withdrawals.tsv, with the columns source_table and agreement; start here |
| `BW2Project/05_work/analysis/t08/c1/named-functions.tsv` | The string method's v1.42 table, 1,348 functions, one of the seven tables the combined file is built from |
| `BW2Project/05_work/analysis/t08/c2/pair/named-functions.tsv` | The string pair table (T08C2), 64 functions, one of the seven tables the combined file is built from |
| `BW2Project/05_work/analysis/t08/c2/calls/named-functions.tsv` | The anchored calls table (T08C2), 15 functions, one of the seven tables the combined file is built from |
| `BW2Project/05_work/analysis/t08/c3/sets/named-functions.tsv` | The callee set table (T08C3), 80 functions, one of the seven tables the combined file is built from |
| `BW2Project/05_work/analysis/t08/d1/verified/named-functions.tsv`, `BW2Project/05_work/analysis/t08/d2/verified/named-functions.tsv` | The body matcher's top band table (38 functions) and the sole caller rule's strict band table (24 functions), two of the seven tables the combined 1.42 file is built from |
| `BW2Project/05_work/analysis/t06/` | The v1.42 name map: `named-functions.tsv` (with its status column), `class-counts.tsv`, `status-counts.tsv` |
| `BW2Project/05_work/analysis/t09/` | The Battle of the Gods name map: the same three tables |
| `BW2Project/05_work/analysis/names/botg/named-functions.tsv` | The combined Battle of the Gods name file, 10,311 functions: the vtable table and the two body carry tables joined by script, minus the rows of withdrawals.tsv, with the columns source_table and agreement; start here for BotG |
| `BW2Project/05_work/analysis/t09/b3/named-functions.tsv` | The carry by body table (T09B3), 1,105 functions, one of the three tables the combined BotG file is built from |
| `BW2Project/05_work/analysis/t09/b5/named-functions.tsv` | The second carry by body table (T09B5), 49 functions, one of the three tables the combined BotG file is built from |
| `BW2Project/05_work/analysis/names/suggested/` | The suggested names, five files (1,390, 231, 54, 167, 568 rows) and a README; every file adds the columns also_named_by and name_verified_elsewhere |
| `BW2Project/05_work/analysis/names/noise/` | The noise names, two files (664, 97 rows) and a README |
| `BW2Project/05_work/tools/names/apply/` | The applier: labels the functions of your own Ghidra program from the verified file |
| `CORRECTIONS.md`, `BW2Project/05_work/analysis/names/withdrawals.tsv`, `withdrawn-functions.tsv` beside each combined file | The names withdrawn after they were published, the list that keeps them out, and the withdrawn rows with their reasons |
| `BW2Project/05_work/tools/names/t06/docs/vtable-rules.md` | The rules the name maps were built by |
| `BW2Project/05_work/tools/names/t06/docs/alignment-rules.md` | The rules the BotG alignment and the carry by body were built by |
| `BW2Project/03_vault/fingerprints/`, `BW1Project/03_vault/fingerprints/` | SHA-256 lists of the retail install, official patches, fan patches and the expansion; use them to verify your own copy |
| `BW2Project/START HERE.md`, `BW1Project/START HERE.md` | Where each project starts |
| `BW2Project/08_notes/BASELINE-PLAYTEST.md`, `BW2Project/08_notes/RUNNING-ON-WINDOWS-11.md` | Playtest baseline and notes on running the game on Windows 11 |
| `AGENTS.md`, `CLAUDE.md`, `AUTOMATION.md` | How the AI sessions are instructed |
| `GITHUB-SURVEY.md`, `RESEARCH-SWEEP.md`, `REPO-DIGEST.md` | Survey of everything the community has already built, so nothing is rebuilt |
| `FINDINGS.md` | The established facts file; errata are appended lines, never edits |
| `BW2Project/07_reference/KNOWLEDGE-BASE.md` | Community findings; every entry carries a status and a source; one entry, BB-13, is left out of this public copy by script |
| `BW2Project/08_notes/LEARNINGS.md` | Proven, generalisable lessons |
| `CREDITS.md` | Full roll-call |

Each combined file is the union of its method tables minus the rows of BW2Project/05_work/analysis/names/withdrawals.tsv. The method tables are the record of what each method produced and still hold the withdrawn rows; the combined files are the names in force.

A holdout reader checked whether the code at an address does what the Mac function of that name does. That does not show that the address is where the function begins. Every verified address of both builds was therefore swept for compiler library code and for addresses that are not a function's first instruction, and every row the sweeps listed was read; the rows that failed are in CORRECTIONS.md. The sweeps ran on 2026-11-19 over the 21,256 verified rows of both builds and listed 54 of them; a second AI session read 33 itself, 16 rows carried to Battle of the Gods by equal code took the result of the v1.42 row they came from, and 5 had been withdrawn before the sweeps were read; of the rows read, 32 stand, and the number withdrawn after reading is 1.

## Name tiers
Verified names are in the two combined files and nowhere else. Suggested names come from a method that read right between 85 and 94 times in 100, or that read higher on its holdout and under 95 on its blind test. They are kept apart from the verified file and are not used by any session as a name.

Noise names are guesses that read wrong more than 15 percent of the time. They are kept for anyone who wants a go at them, and are not to be trusted without reading the code.

An AI session must not trust a suggested or noise name, cite it as a fact, use it as an anchor, or lift it into a verified file. A human reader who has read the code is needed before any use of one.

Every row carries two columns written by script: also_named_by, the other lower tier files that name the same address, each with same name or different name, and name_verified_elsewhere, the address at which the combined file already carries the row's Mac symbol, or empty. Agreement between lower tier files never promotes a row.

## The applier
The applier, BW2Project/05_work/tools/names/apply, labels the functions of your own Ghidra program from the verified file. It applies only the verified tier unless you ask for another, and its IDA file is UNTESTED.

## Errata
Names withdrawn from a verified file after they were published are listed here; the reason for each is in CORRECTIONS.md.

- Battle of the Gods, 0x9a2cd1, ParticleSystem::DefaultParticleSetFactory::DoCreate, 2026-11-19
- Battle of the Gods, 0xb1a81b, LoadFXRFromFile, 2026-11-19
- v1.42, 0x4835ad, CreatureAgendaImpl::ConstructSubActionsForDanceWithVillagers, 2026-11-19
- v1.42, 0x97c461, ParticleSystem::DefaultParticleSetFactory::DoCreate, 2026-11-19
- v1.42, 0xad351d, yylex, 2026-11-20
- v1.42, 0xaebf3b, LoadFXRFromFile, 2026-11-19

## Not on the mirror yet
The mirror carries only files that pass its scan as they stand; nothing is edited to get onto it. The reports, the task cards, the plan, the ledger, the checks and the tools other than the applier wait for a scrub rule that publishes them with named lines left out by script. Paths named in the files above that are not in this repository belong to that working record.

## What is not here at all
No game files, no disc images, no extracted assets, no executables, no bytes from the game. You need your own copy of Black & White 2 (and the official 1.1/1.2 patches plus the community fan patch 1.42) for anything here to be useful. The fingerprints let you check that your copy matches the builds the notes describe.

## Built with AI, checked by machines
The analysis and tooling are produced by Claude models (Anthropic) directed by a non-programmer. That is exactly why the project is organised around verification: fingerprints, protected checks, canaries, a random holdout for measuring name-transfer precision, and reports whose numbers come from scripts. Judge the evidence attached to each result, not its author.

## Credits
Full roll-call in `CREDITS.md`. In short:
- openblack (bw1-decomp, bw2-modding, bw2-tools, bw2-unstuff, decomp-toolkit): the foundation everything here stands on.
- HandsomeMatt and Egerion (fan patches), Daniels118 (CHL tools, debugger, mod loader), bwrsandman (bw1-ai-sandbox), sp00nznet (pcrecomp, type recovery).
- The Black & White community Discord, in particular the people doing 2026 exe work: Part-time God, TallyMark, crazy hacker man, shivazgod, HomoRexual, their published patches and addresses serve as independent cross-checks here and are credited where used.

## License
Code: MIT. Documentation and data produced by this project (notes, fingerprints, the name map): CC0. Third-party material referenced in `07_reference` keeps its own license.

## Contributing
Open an issue. The most useful contributions are corrections with evidence (a hash, an address with bytes, a reproducible script). Please don't send game files or anything derived from the game's bytes.
