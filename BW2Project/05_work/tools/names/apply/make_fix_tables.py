#!/usr/bin/env python3
"""make_fix_tables.py (test only): writes the small scratch tables the T08E1F fix runs use.
Usage: make_fix_tables.py <named-functions.tsv> <output folder>
Every row sits at an address that is a function in the 1.42 program, so only the rule under test decides its fate."""
import sys
hdr = open(sys.argv[1], encoding="utf-8").readline().rstrip("\n").split("\t")
def row(addr, name, status):
    r = [""] * len(hdr)
    r[hdr.index("pc_address")], r[hdr.index("single_name")], r[hdr.index("status")] = addr, name, status
    r[hdr.index("mac_mangled")], r[hdr.index("mac_demangled")] = "__Ztest", "test"
    return r
def write(name, rows, raw=()):
    out = ["\t".join(hdr)] + ["\t".join(r) for r in rows] + list(raw)
    open(sys.argv[2] + "/" + name, "w", encoding="utf-8").write("\n".join(out) + "\n")
V, S = "claimed, f test", "suggested, f test, read: 90 of 100 fit"
write("f1-suggested-two.tsv", [row("0x4010b0", "Army::CastArmy", S), row("0x4010c0", "Army::IsArmy", S)])
write("f1-verified-up.tsv", [row("0x4010b0", "Army::CastArmyVerified", V), row("0x4010c0", "", "folded, 2 names")])
write("f5-replaced-skipped.tsv", [row("0x401000", "Foo Bar::Baz Qux", V), row("0x401040", "Café::naïve", V)])
write("f6-short.tsv", [row("0x4010b0", "A::B", V), row("0x4010c0", "A::C", V)], raw=["0x4010d0\tA::D"])
write("f6-nostatus.tsv", [row("0x4010b0", "A::B", V), row("0x4010c0", "A::C", ""), row("0x4010d0", "A::D", V)])
write("f7-badnames.tsv", [row(a, n, V) for a, n in [
    ("0x401000", "::"), ("0x401040", "FUN_00401120"), ("0x401090", "thunk_FUN_00401130"), ("0x4010f0", "LAB_00401140"),
    ("0x401100", "DAT_00401150"), ("0x401110", "SUB_00401160"), ("0x401120", "NS::FUN_00401170"),
    ("0x401170", "fun_00401120"), ("0x401200", "Fine::Name")]])
