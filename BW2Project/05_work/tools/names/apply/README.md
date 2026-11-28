# Put our names into your own copy of the game in Ghidra

Licence: MIT, the LICENSE file at the root of the repository covers this folder.

This folder holds a script that reads a table of addresses and names (`named-functions.tsv`) and labels the functions in your own Ghidra project with those names. You need your own copy of the game: the script works on the program you opened, it contains no game bytes and it runs nothing inside the game.

## The three steps (Ghidra 12)

1. Open your copy of the 1.42 exe in Ghidra and let it analyse as usual.
2. Open the Script Manager, add this folder with the "Manage Script Directories" button, choose `ApplyNames.java`, and run it. It asks for `named-functions.tsv` and for a mode. Choose **dry** first. It prints what it would do and changes nothing.
3. Read the counts, then run it again and choose **apply**. Save the program.

Headless, for those who use it:
`analyzeHeadless <projectdir> <project> -process <program> -noanalysis -scriptPath <this folder> -postScript ApplyNames.java <path to named-functions.tsv> dry`
(then the same with `apply`). A third word, `allow-suggested` or `allow-noise`, is explained below.

## What it does to each row

* A row with one name: the function at that address is renamed (a name with `::` becomes a namespace and a function name). A character Ghidra does not allow in a label, such as a space, becomes an underscore. The rows where that happened are counted and listed.
* A row with several names for one function body ("folded"): no label. Only a comment is added that lists all the names. See the counts below.
* Every touched function gets a plate comment. It says the tier, the status of the row, the exact name from the table and the Mac symbol. A comment you already had is kept, ours goes under it.
* The script never creates a function. If the address is not the start of a function in your program, or lies outside it, the row is skipped, counted and listed.
* A function that already has a name you gave it yourself is left alone, counted and listed.
* A function whose plate comment already holds a line of this table is ours, whatever Ghidra says about the source of its name. A row of an equal or lower tier leaves it alone and is counted as "skipped, already named by the names table at an equal or higher tier". A row of a higher tier replaces the lower tier's label and its comment lines and is counted in "replaced a lower tier's name". This holds for the rows that only comment too: if a lower tier had put a label there, the label is taken away and the function goes back to Ghidra's default name in the global namespace, unless you renamed the function yourself after that label was written, in which case your name stays and only the comment changes. So the tiers may be applied in either order, and a function ends with the verified name, or, where the verified file gives only a comment, with no label of ours and the verified comment. One limit: if Ghidra's own analysis had named the function before a lower tier labelled it, that earlier name is not brought back. On the 1.42 table no row that only comments sits at such a function (all 169 read as default names before any apply). Running the same file twice skips every row the second time.
* The same address written two ways (`0x4010b0`, `4010b0` and `0x004010b0`) is one address: the first row is kept and the others are skipped as "duplicate address". A byte order mark at the start of the file is ignored. A file with a header and no rows is refused.
* A name that is nothing after splitting at `::`, or that is one of Ghidra's own default forms (`FUN_`, `thunk_FUN_`, `LAB_`, `DAT_` or `SUB_` followed by hex digits), is a bad row: skipped, counted and listed.
* A name Ghidra refuses is skipped and counted, and leaves nothing behind (no namespace). Dry and apply count it the same way, except a clash with another symbol that shows only at apply, which the apply counts as skipped.
* A newer table of the same tier adds its new rows and leaves the rows already applied as they are, also where the newer table changed a name. If a name is ever withdrawn from the verified file, the mirror's README lists it under Errata. Take it out of your program by hand, or apply the table to a fresh program.
* An empty namespace may be left behind when a higher tier replaces a lower tier's label. It holds nothing and may be deleted by hand.
* A row with too few cells or with no status refuses the whole file. The message gives the row number, its line and its address, and nothing is changed.
* At the end it prints: rows in, labelled, folded, skipped (with the reasons), rows replaced from a lower tier, and the rows and characters replaced by an underscore (only rows that were labelled). Labelled plus folded plus skipped always equals rows in.

## What the table holds, in counts

These figures are for the 1.42 table as it stood on 2026-11-15: 10,992 rows, whose sha256 begins eba8e2ec5418. 10,823 are labelled and 169 get a comment only. Of those 169, 40 have a status of the folded kind and also a single name. That single name is a bare method name that one body shares with 2 to 567 Mac names from several classes, so it names none of them. The script writes the comment with every name and no label, and it does not count those 40 among the labelled rows. Your copy of the table may be a newer one, so run **dry** first: it prints the counts for the file you use. (The hook build carries the earlier table of 10,930 rows and counts 10,801 single names, the 10,761 labelled rows and those 40; that sentence is true for that table only.)

## What the status column means, and which tier is applied

Every row says how its name was found and how well that method did on a test that was set up before the rows were read.

* `claimed` (and `folded`): the verified tier. The method read 95 of 100 or better. **Only this tier is applied by default.**
* `suggested`: the method read 85 to under 95 of 100. Useful, but about one in ten may be wrong. The script refuses such a file unless you add `allow-suggested`. Then every label starts with `suggested_` and every comment starts with SUGGESTED, so nobody mistakes it for a verified name.
* `noise`: the method read under 85 of 100. Guesses, published so others can try them. Same rule with `allow-noise` and the prefix `noise_`.
* Anything else (`withheld`, `banded`, a file that mixes tiers) is always refused. The script says why and changes nothing.

## IDA

`apply_names_ida.py` follows the same rules for IDA through IDAPython. **It is UNTESTED.** It was written without IDA and has never been run in IDA. It has only been driven against a stub of IDA's module, which is a fake and not IDA. Try it on a copy of your database with `DRY = True` first, and do not rely on it.

What it does without IDA to prove it: it checks every row before it changes anything, lists the bad rows (a bad address, a short row, an empty or default form name) and skips them, and it keeps the first row of a duplicate address. What is known and untested:

* `::` becomes `__`, because the file makes no namespaces. The Ghidra script does.
* IDA keeps names unique. In the 1.42 table of 10,992 rows, 101 rows repeat a name an earlier row already holds and 162 rows share a name with another row, so IDA is expected to refuse the repeats or to need a suffix you add yourself.
* It does not read the tier of an earlier comment, so the order matters in IDA. Apply the verified file first, or to a fresh database. A lower tier applied over the verified one is stopped only because a function with a user given name is left alone. The verified file applied after a lower tier leaves the lower tier's label and writes no comment for that row.
* A row that only comments gets a comment that lists every name of the row, as the Ghidra script's does. A byte order mark is skipped, and the duplicate test is by the parsed address.

## If something looks wrong

Run **dry** again; it never changes anything. When you report a problem, include the printed counts and the first lines it listed.
