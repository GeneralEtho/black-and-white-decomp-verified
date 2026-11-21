# UNTESTED. UNTESTED. UNTESTED. Written without IDA Pro or IDA Free; it has never been run in IDA.
# It follows the rules of ApplyNames.java (the Ghidra script, which is tested) and has been driven only
# against a stub of idaapi (a fake written for the test, not IDA). Nothing here is known to work in IDA.
# Run it first with DRY = True on a copy of your database and read what it prints.
#
# apply_names_ida.py: puts the names of a published named-functions.tsv into YOUR OWN IDA database.
# MIT licence. Reads the table and your database; contains no game bytes.
# Run from IDA with File > Script file. Set the three lines below first.
#
# What it does before it changes anything: every row is checked first. A row with a bad address, too few
# cells, no status, a name left empty by the split, or a name in a default form (sub_, FUN_, LAB_, DAT_
# followed by hex digits) is listed and skipped; a second row at the same address is skipped (the first
# is kept, as ApplyNames.java does). Only when that list is complete does it set the first name.
#
# Known, and untested:
#  * "::" becomes "__" because this file makes no namespaces (ApplyNames.java makes them).
#  * IDA keeps names unique. In the combined 1.42 table of 10,992 rows, 101 rows repeat a name that an
#    earlier row already holds and 162 rows share a name with another row, so those rows are expected to be
#    refused by IDA (counted and listed) or to need a suffix you add yourself.
#  * Unlike ApplyNames.java it does not read the tier of an earlier comment of ours, so the order matters
#    here. Apply the verified file first, or to a fresh database. A lower tier applied over the verified one is
#    stopped only because a function with a user given name is left alone. The verified file applied after a
#    lower tier leaves the lower tier's label in place and writes no comment for that row.
#  * A row that only comments gets a comment that lists every name of the row, as the Ghidra script's does.
import csv
import re

import idaapi

DRY = True              # True prints the counts and changes nothing; False applies
TSV = ""                # path of named-functions.tsv; empty = ask for the file
ALLOW = ""              # "" (verified tier only), "allow-suggested" or "allow-noise"

MARKER = "BW2 names table"
NEED = ("pc_address", "single_name", "mac_demangled", "mac_mangled", "status")
DEFAULT_NAME = re.compile(r"(sub_|FUN_|thunk_FUN_|LAB_|DAT_)[0-9A-Fa-f]+")


def tier_of(status):
    word = re.split(r"[ ,]", status.strip())[0]
    if word in ("claimed", "folded"):
        return "verified"
    return word if word in ("suggested", "noise") else "other"


def clean(part):
    return re.sub(r"[^A-Za-z0-9_?$@]", "_", part)   # "::" becomes "__", spaces and "<>" become "_"


def check_rows(rows):
    """Returns (plan, skips). Nothing is changed here. A bad row is listed with its reason and left out."""
    plan, skips, seen = [], [], set()
    for no, r in enumerate(rows, 1):
        where = "row %d" % no
        if any(r.get(k) is None for k in NEED) or not r["status"].strip():
            skips.append((where, "short row or no status"))
            continue
        try:
            ea = int(r["pc_address"].strip(), 16)
        except ValueError:
            skips.append((r["pc_address"].strip() or where, "bad row"))
            continue
        if ea in seen:
            skips.append((r["pc_address"].strip(), "duplicate address"))
            continue
        seen.add(ea)
        name = r["single_name"].strip()
        parts = [p for p in name.split("::") if p]
        if name and not r["status"].startswith("folded") and (not parts or any(DEFAULT_NAME.fullmatch(p) for p in parts)):
            skips.append((r["pc_address"].strip(), "bad row"))
            continue
        plan.append((ea, r))
    return plan, skips


def main():
    path = TSV or idaapi.ask_file(0, "*.tsv", "named-functions.tsv")
    with open(path, encoding="utf-8-sig", newline="") as fh:   # utf-8-sig skips a byte order mark
        rows = list(csv.DictReader(fh, delimiter="\t", quoting=csv.QUOTE_NONE))
    if not rows:
        print("REFUSED: the table is empty or has a header and no rows")
        return
    for need in NEED:
        if need not in rows[0]:
            print("REFUSED: column missing in the header: " + need)
            return
    tiers = {tier_of(r["status"]) for r in rows if r.get("status")}   # a row with no status is listed by check_rows
    if len(tiers) != 1 or "other" in tiers:
        print("REFUSED: the status cells are not one applicable tier: %s" % sorted(tiers))
        return
    tier = tiers.pop()
    if tier != "verified" and ALLOW != "allow-" + tier:
        print("REFUSED: this table is the %s tier; to apply it on purpose set ALLOW = 'allow-%s'" % (tier, tier))
        return
    prefix = "" if tier == "verified" else tier + "_"
    plan, skips = check_rows(rows)
    for where, why in skips:
        print("SKIP\t%s\t%s" % (where, why))
    n = dict(labelled=0, folded=0)
    for ea, r in plan:
        func = idaapi.get_func(ea)
        why = None
        if func is None or func.start_ea != ea:
            why = "no function at the address"
        elif r["single_name"].strip() and not r["status"].startswith("folded"):
            name = prefix + clean(r["single_name"].strip())
            current = idaapi.get_name(ea)
            if idaapi.has_user_name(idaapi.get_flags(ea)) and current != name:
                why = "already has a different user given name"
            elif not DRY and current != name and not idaapi.set_name(ea, name, idaapi.SN_CHECK):
                why = "IDA refused the name"
            else:
                n["labelled"] += 1
        else:
            n["folded"] += 1
        if why:
            skips.append((r["pc_address"].strip(), why))
            print("SKIP\t%s\t%s" % (r["pc_address"].strip(), why))
            continue
        head = "%s (%s)\nstatus: %s\n" % (tier.upper(), MARKER, r["status"])
        if r["single_name"].strip() and not r["status"].startswith("folded"):
            text = head + "name: %s\nmac: %s" % (r["single_name"].strip(), r["mac_mangled"])
        else:   # comment only: every name of the row, as ApplyNames.java writes it
            text = head + "names (one body, several Mac names, not labelled): %s\nmac: %s" % (
                r["mac_demangled"].strip() or r["mac_mangled"].strip(), r["mac_mangled"])
        old = idaapi.get_func_cmt(func, False) or ""
        if not DRY and text not in old:
            idaapi.set_func_cmt(func, (old + "\n\n" + text) if old else text, False)
    reasons = {}
    for _, why in skips:
        reasons[why] = reasons.get(why, 0) + 1
    print("apply_names_ida.py (UNTESTED) %s, tier %s" % ("DRY RUN" if DRY else "APPLY", tier))
    print("rows in: %d, labelled: %d, folded: %d, skipped: %d %s" % (len(rows), n["labelled"], n["folded"], len(skips), reasons))
    print("check: %d" % (n["labelled"] + n["folded"] + len(skips)))


main()
