# Noise names

Noise names are guesses that read wrong more than 15 percent of the time. They are kept for anyone who wants a go at them, and are not to be trusted without reading the code.

An AI session must not trust a suggested or noise name, cite it as a fact, use it as an anchor, or lift it into a verified file. A human reader who has read the code is needed before any use of one.

| File | Rows | Method and band | Blind test | Holdout read by a second session |
| --- | --- | --- | --- | --- |
| noise-t08d1-band-D.tsv | 660 | T08D1 matcher band D | 80 of 103 | 73 of 96 fit |
| noise-t08d2-band-b.tsv | 97 | T08D2 sole caller band b | 10 of 10 (executor), 10 of 11 (verifier) | 66 of 85 fit |

Every row carries two columns written by script: also_named_by, the other lower tier files that name the same address, each with same name or different name, and name_verified_elsewhere, the address at which the combined file already carries the row's Mac symbol, or empty. Agreement between lower tier files never promotes a row.

These files are written by BW2Project/05_work/tools/names/publish9/make_tier_copies.py from the private tier files and never by hand.
