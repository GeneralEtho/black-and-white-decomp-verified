# Suggested names

Suggested names come from a method that read right between 85 and 94 times in 100, or that read higher on its holdout and under 95 on its blind test. They are kept apart from the verified file and are not used by any session as a name.

An AI session must not trust a suggested or noise name, cite it as a fact, use it as an anchor, or lift it into a verified file. A human reader who has read the code is needed before any use of one.

| File | Rows | Method and band | Blind test | Holdout read by a second session |
| --- | --- | --- | --- | --- |
| suggested-t08b1-depth1.tsv | 1348 | T08B1 call position at depth 1 | none (it predates the blind test) | 77 of 85 fit |
| suggested-t08b2-witness.tsv | 220 | T08B2 witness rule | none | 73 of 79 fit |
| suggested-t08c3-store.tsv | 54 | T08C3 vtable store | none | 49 of 54 fit |
| suggested-t08d1-band-B.tsv | 137 | T08D1 matcher band B | 12 of 13 | 96 of 98 fit |
| suggested-t08d1-band-C.tsv | 564 | T08D1 matcher band C | 80 of 95 | 88 of 97 fit |
| suggested-t16b1-band-c.tsv | 90 | T16B1 two signs band c | 1,984 of 1,991 | 64 of 68 fit |

Every row carries two columns written by script: also_named_by, the other lower tier files that name the same address, each with same name or different name, and name_verified_elsewhere, the address at which the combined file already carries the row's Mac symbol, or empty. Agreement between lower tier files never promotes a row.

The two signs file, suggested-t16b1-band-c.tsv, has one more column before those two, holdout_mark: the mark the row got when a second session read it (consistent, inconsistent or cannot tell); the rows read wrong stay in the file. When the band was read it held 95 rows, and 24 of them were functions that are empty or return a constant on both builds; a reading cannot contradict such a row, so they were not counted in the figure of the table above. Since the reading 5 of the band's rows have been named by a verified method and have left the file, which now holds 90 rows.

These files are written by BW2Project/05_work/tools/names/publish9/make_tier_copies.py from the private tier files and never by hand.
