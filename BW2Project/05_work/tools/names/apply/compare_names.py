#!/usr/bin/env python3
"""compare_names.py (test only): checks what ApplyNames.java did, row by row.
Usage: compare_names.py <named-functions.tsv> <readback-before.tsv> <readback-after.tsv> <apply-run.txt>
The first three are tab separated; the readbacks come from ReadBackNames.java; apply-run.txt is the
printed output of the apply run (its SKIP and REPLACED lines are checked against). Last line: COMPARE: HOLDS or FAILS."""
import sys

def table(path):
    rows = [l.split("\t") for l in open(path, encoding="utf-8").read().split("\n") if l]
    return rows[0], rows[1:]

def readback(path):
    d = {}
    for f in table(path)[1]:
        d[f[0]] = (f[1], f[2], f[3].replace("\\n", "\n").replace("\\t", "\t").replace("\\\\", "\\"))
    return d

def split(s):
    out, depth, cur, i = [], 0, "", 0
    while i < len(s):
        c = s[i]
        if c == "<": depth += 1
        elif c == ">": depth = max(0, depth - 1)
        if depth == 0 and s.startswith("::", i):
            out.append(cur); cur = ""; i += 2; continue
        cur += c; i += 1
    out.append(cur)
    out = [p for p in out if p]
    return out or ["_"]

def clean(p):
    return "".join("_" if ord(c) <= 0x20 or ord(c) >= 0x7F else c for c in p)

def main(argv):
    hdr, rows = table(argv[1]); H = {n: i for i, n in enumerate(hdr)}
    before, after = readback(argv[2]), readback(argv[3])
    skips, repl = {}, 0
    for l in open(argv[4], encoding="utf-8"):
        f = l.rstrip("\n").split("\t")
        if f[0] == "SKIP": skips[f[1]] = f[2]
        if f[0] == "REPLACED": repl += 1
    fails, n = [], dict(single=0, folded=0, skipped=0)
    expect_repl, listed = 0, set()
    for f in rows:
        a, single, status = f[H["pc_address"]], f[H["single_name"]], f[H["status"]]
        listed.add(a)
        b0, a0 = before.get(a), after.get(a)
        if a in skips:
            n["skipped"] += 1
            if a0 != b0: fails.append(f"{a}: skipped ({skips[a]}) but changed")
            continue
        if a0 is None: fails.append(f"{a}: no function after, and not in the skip list"); continue
        word = status.split(",")[0].split(" ")[0]
        prefix = word + "_" if word in ("suggested", "noise") else ""
        plate = a0[2]
        tier = word.upper() if prefix else "VERIFIED"
        if tier + " (BW2 names table" not in plate: fails.append(f"{a}: the tier is missing in the plate comment"); continue
        if status not in plate: fails.append(f"{a}: status missing in the plate comment"); continue
        if b0[2] and b0[2] not in plate: fails.append(f"{a}: the earlier comment was lost"); continue
        if not single or status.startswith("folded"):
            n["folded"] += 1
            if a0[0] != b0[0] and not (a0[1] == b0[1] == "DEFAULT" and b0[0].startswith("thunk_")):
                fails.append(f"{a}: folded row was labelled {a0[0]}")
            continue
        n["single"] += 1
        parts = split(single); new = [clean(p) for p in parts]
        if new != parts: expect_repl += 1
        new[-1] = prefix + new[-1]
        want = "::".join(new)
        if a0[0] != want: fails.append(f"{a}: name {a0[0]} but the table says {want}")
        if "name: " + single not in plate: fails.append(f"{a}: exact name missing in the plate comment")
    follow = 0
    for a, v in after.items():
        if a in listed: continue
        b0 = before.get(a)
        if v == b0: continue
        if b0 and v[2] == b0[2] and v[1] == b0[1] == "DEFAULT" and b0[0].startswith("thunk_"): follow += 1; continue
        fails.append(f"{a}: a function outside the table changed: {b0} -> {v}")
    if len(before) != len(after): fails.append(f"function count {len(before)} before, {len(after)} after")
    if expect_repl != repl: fails.append(f"rows with replaced characters: compare counts {expect_repl}, the apply run listed {repl}")
    print(f"rows in the table: {len(rows)}; single name rows checked: {n['single']}; folded rows checked: {n['folded']}; skipped as listed: {n['skipped']}")
    print(f"rows with characters replaced: {expect_repl} (apply run listed {repl}); outside functions whose thunk name follows its target: {follow}")
    for x in fails[:10]: print("DIFF " + x)
    print("COMPARE: HOLDS" if not fails else f"COMPARE: FAILS ({len(fails)} problems, first {min(10, len(fails))} above)")
    return 0 if not fails else 1

if __name__ == "__main__":
    sys.exit(main(sys.argv))
