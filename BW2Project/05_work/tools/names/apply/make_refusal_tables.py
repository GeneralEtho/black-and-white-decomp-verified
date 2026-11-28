#!/usr/bin/env python3
"""make_refusal_tables.py (test only): writes the three scratch tables the refusal runs use.
Usage: make_refusal_tables.py <named-functions.tsv> <output folder>
scratch-suggested.tsv: every status "suggested, test, read: 90 of 100 fit" (folded rows lose their single name)
scratch-withheld.tsv: every status "withheld, test"; scratch-mixed.tsv: only the first row is suggested."""
import sys
lines = open(sys.argv[1], encoding="utf-8").read().split("\n")
h = lines[0].split("\t"); si, st = h.index("single_name"), h.index("status")
def make(name, fn):
    out = [lines[0]]
    for i, l in enumerate(x for x in lines[1:] if x):
        f = l.split("\t"); fn(i, f); out.append("\t".join(f))
    open(sys.argv[2] + "/" + name, "w", encoding="utf-8").write("\n".join(out) + "\n")
def suggested(i, f):
    if f[st].startswith("folded"): f[si] = ""
    f[st] = "suggested, test, read: 90 of 100 fit"
make("scratch-suggested.tsv", suggested)
make("scratch-withheld.tsv", lambda i, f: f.__setitem__(st, "withheld, test"))
make("scratch-mixed.tsv", lambda i, f: f.__setitem__(st, "suggested, test, read: 90 of 100 fit") if i == 0 else None)
