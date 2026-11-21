# Alignment rules for BotG against v1.42 (T09B2, and the carry by body of T09B3)

The rules the scripts in 05_work/tools/names/t09 apply when they pair a BotG function with a v1.42 function through its body bytes. Sections 1 to 6 are the method of T09B2, written from its card (BW2Project/08_notes/tasks/T09B2-botg-via-v142.md, "The rules", with its item 7 as amended) and from DECISIONS.md "2026-10-30 (Fable, T09B2 sign-off, T08 closed, string anchors, T12B7F, mirror)", which ordered this file. Section 7 is the rule of T09B3, from its card (BW2Project/08_notes/tasks/T09B3-carry-by-body.md, with its 2026-11-03 amendments). This file sits beside docs/vtable-rules.md, whose rule numbers (rule 11, the fold) it uses. Where this file and a card or a ruling differ, the card and the ruling are the rule and this file is corrected by the next session that finds the difference.

## 1. The masked body hash (body_hash.py)

- A census function is a row of the census functions.tsv (census_digest.py) with the status FUNCTION. A MISSING row (a body over an int3 fill run) is not a function and is not hashed.
- Its body is every address range Ghidra gives the function (ExportRanges.java over the restored archive), read in address order from the exe through its PE section table. The census size must equal the sum of the range sizes, or the body is "undecodable".
- Each range is decoded by capstone (32 bit x86) from its first byte. When the instructions do not end exactly at the range's last byte, or a byte lies outside the file's raw section data, the body is "undecodable".
- In every instruction, an immediate operand or a memory displacement whose value, read as an unsigned 32 bit number, lies inside the image (ImageBase to ImageBase plus SizeOfImage of that exe) is zeroed in the bytes.
- A relative call, jump or conditional jump (capstone's BRANCH_RELATIVE group) is the exception: its rel32 or rel8 is zeroed when its target lies outside every range of the body, and kept as it is when the target lies inside the body.
- The masked bytes of the ranges, joined in address order, are hashed with sha256. The key is "size:sha256", so two bodies of different byte length never match.
- An undecodable body never anchors and never matches.
- The hasher prints, per build, the bodies hashed, the undecodable ones, and the keys that occur more than once within the build with the bodies under them (a shared body, rule 11's shape, where the PC linker folded identical functions). Two runs over the same inputs must be identical by cmp.
- What the mask means for a reader: relocations are hidden, so a body compiled from the same source at another address matches. So does a body that calls a different function at the same place, because the call target is masked, and a five byte jump thunk matches every other five byte jump thunk. Short bodies are therefore weak evidence: in T09B2, 2,630 of the 17,349 anchors had bodies of 6 bytes or fewer, 1,196 of them five byte jump thunks (05_work/analysis/t09b2-verification/out/v-anchor-quality.txt).

## 2. The anchors of a class

- A class with one slotted primary vftable on both builds is aligned, its v1.42 vtable against its BotG vtable. A slot whose address is no census function, or whose body is undecodable, has no key and never anchors.
- An anchor is a pair (v1.42 slot i, BotG slot j) whose keys are equal, where that key occurs exactly once among the v1.42 vtable's slots and exactly once among the BotG vtable's slots. The _purecall stubs and the shared return bodies repeat, so they never anchor.

## 3. The alignment of a class

- The chain is the longest subsequence of anchors increasing in i and in j (dynamic programming over the two slot lists keyed by hash).
- When several chains have that length, the chain taken is the smallest in order of (i, j) pairs read from the first, and the class is counted as a tie (none occurred in T09B2).
- A class with fewer than two anchors is left, with the reason "under two anchors" and both counts. A class with two or more anchors but no chain of length two is left with the reason "anchors cross".

## 4. The gap rule and the list ends

- An anchor pairs its own two slots.
- Between two consecutive anchors (i1, j1) and (i2, j2): when j2 minus j1 equals i2 minus i1, every slot between pairs one to one by position. When it is larger, the difference is the count of BotG slots inserted in that gap; they are located exactly only when i2 minus i1 is 1 (no unmatched v1.42 slot between), otherwise they are "unlocated", and nothing in the gap is named. When it is smaller, BotG removed a method there; the gap is counted as removed and nothing in it is named.
- The list ends (the convention accepted by the 2026-10-30 ruling): a list end counts as an anchor at the position before the first slot, (-1, -1), and at the position after the last slot, (nv, nb). Before the first anchor the slots pair one to one from slot 0 when the first anchor has i equal to j. After the last anchor the remaining slots pair one to one when the remaining counts are equal. Otherwise the end gap holds inserted or removed slots as between two anchors, and an insert in an end gap is located only when no unmatched v1.42 slot lies in that gap.
- Under this convention T09B2 located 130 inserted slots against 127 under the card's literal words, and the MenuManager bridge gave 3 of 5 classes against 2 of 5.

## 5. The carry, with rule 11 folding

- A BotG slot paired one to one with a v1.42 slot takes the names of the v1.42 row at that slot's address: a single name row gives its single name, a folded row gives every name of its fold, and a v1.42 slot with no row (a rule 12 stub, a pure entry, a class the T06 rules skipped) gives nothing.
- Status "claimed, T09B2 (via v1.42)". Reason "aligned to v1.42 slot i between anchors i1/j1 and i2/j2", or "at anchor i/j" for an anchor's own slot, or "from slot 0 to anchor" and "from anchor to end" in the end gaps.
- Rule 11 folds across classes: a BotG address reached through several slots keeps every name. Where a newly aligned slot reaches a BotG address that already holds a T09B1 row, the row keeps its names and gains the new ones; when every new name is already on the row, only reached_by grows. The 2026-10-30 ruling found this the carry rule's correct result (32 T09B1 rows gained names) and amended the card's item 7.
- Inserted slots take nothing. They are counted per class and in all, with their positions where located; the 2,015 unlocated ones are BotG's own methods, counted and not placed.

## 6. The canaries and the known limits

- The identity canary, before any name: over the classes whose slot counts are equal on both builds, every class the method aligns must map every slot to the same slot; one class mapped otherwise and nothing is named. The classes it cannot align are counted and printed, because that figure says how thin the anchors are.
- The plant canary, before any name: one wrong name planted at one slot in a scratch copy of the v1.42 table must reach exactly the one BotG slot that aligns to it and no other row.
- Recorded limits: the weak anchors of section 1 (short bodies, and 1,649 anchors whose keys occur more than 10 times in the build), with which the alignment was consistent; the unlocated inserts of section 5.

## 7. T09B3: the carry by body, unique on both builds

The card's rule, written before the code of T09B3; the docstrings of carry_body.py, blind_body.py, plant_b3.py, count_b3.py and combine_botg.py repeat it.

- The source of every name is the combined 1.42 file, 05_work/analysis/names/v142/named-functions.tsv at commit 80e80f0 or later (10,930 rows, combine_v142.py), read only. The keys are body_hash.py's, unchanged, over every census function of both builds.
- Unique on both sides: a 1.42 key that occurs exactly once among the 1.42 census functions, and the same key occurring exactly once among the BotG census functions. A key occurring more than once on either side names nothing through this rule; such rows are listed in body-log.tsv with their counts on both builds, for a later card.
- Short bodies: a body of 16 bytes or fewer is counted apart as short and its row says so in the reason. The blind test and the holdout report short rows separately. No threshold of the executor's own excludes them.
- The carry: for every row of the combined 1.42 file whose PC address has a unique key with a unique BotG twin, the BotG function at the twin's address takes the row's name, its single name or its folded name set as a folded row. Status "claimed, T09B3 (body)". Reason "body hash equal and unique on both builds: 1.42 0xADDR, N bytes, the first 12 hex of the hash; source status S", with "short body" added for a short one. reached_by 1.
- A BotG function already named in the T09 table is never changed. The same name or name set is counted as an agreement; a different name is the conflict "against the BotG named set" and names nothing.
- Two 1.42 rows whose keys land on one BotG function, or one 1.42 row landing on two BotG functions, cannot occur under uniqueness; the script asserts both. Conflicts are never resolved by majority.
- The blind test, before any name: over the BotG functions the T09 table names, in folds, each fold's names hidden, predict them from 1.42 by body (the prediction reads only the combined 1.42 file and the keys) and grade each prediction against the hidden name; 95 percent or more of the predicted rows must agree, the predicted count printed beside the agreement and called too few to prove much under 100, the short rows reported apart with their own agreement. Under 95 nothing is named.
- The plant: in a scratch copy of the combined 1.42 file one unique row's name is swapped with another unique row's name; exactly the two BotG rows those keys reach change, equal to the prediction; RULE HOLDS or nothing is named.
- The combined BotG file: combine_botg.py in the form of combine_v142.py unions the T09 table and the T09B3 table into 05_work/analysis/names/botg/named-functions.tsv, every row's source table and status kept, refusing when any address carries two different names.

Readings the T09B3 executor took where the card leaves room (its report, section on decisions, says why):
- Agreement, in the blind test and in the carry alike: every predicted name is a name of the BotG row (a predicted single name equal to the row's single name or among its folded names; a predicted folded set held whole by the row's names). Anything else is a disagreement in the blind test and a conflict in the carry. The two sets being equal is printed apart from one held inside a larger fold.
- Folds: 10, over the named BotG functions in address order, fold i every tenth from i. The method reads no BotG name, so the folded prediction must equal a single pass; the script prints both and stops if they differ.
- A folded 1.42 row is carried with the one status above, its N names in mac_mangled and mac_demangled and its single_name as in the source row (empty, or for a fold of one method in N classes that method's name, as the T06 tables write it), and "source status folded, N names" in the reason. (Corrected by the T09B3 executor after its first carry run: the line first said "an empty single_name", which the T06 form of a one method fold contradicts.)
- A 1.42 row whose address is no census function with a key, or whose key is undecodable, carries nothing and is counted.
