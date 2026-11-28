# Credits

This project stands on other people's work. Where a specific finding, file or technique came from someone else, it is credited inline in the reports; this page is the roll-call.

## Foundations
- **Lionhead Studios**: the game. Nothing here replaces it; you need your own copy.
- **openblack** (https://github.com/openblack), bw1-decomp (the matching decompilation of Black & White 1 whose workflow this project copies), openblack (engine reimplementation), bw2-modding (file formats, LHVM opcodes, 812 original script sources, 96 enum headers, native tables), bw2-tools, bw2-unstuff, bwmtool, BWMViewer, decomp-toolkit (coff_prototype), bw1-patches, bw1-devmenus.
- **HandsomeMatt (Matt Stevens)**: the original BW2 modding documentation and tools; co-author of fan patch 1.41/1.42; bw1-mp, bw1-setup.
- **Egerion (Ege Demirbas)**: fan patch 1.42 (the decrypted v1.2 executable this project analyses), God's Tool, bwrealm.
- **Daniels118**: CHL compiler/decompiler, BWCI_debugger (script-VM hooking pattern), BW_mods_loader, BWLandEditor.
- **bwrsandman (Sandy Carter)**: bw1-ai-sandbox: the parallel-AI-worker harness whose rules (one unit per worker, cycle caps, deferring is success, learnings file) this playbook adopts.
- **sp00nznet**: pcrecomp and the bw/bw2 recon notes: RTTI/vtable type recovery, the "force analysis before concluding nothing writes this" lesson, emulated initialisers.
- **Buck (BW1-VR)**, **itsloopyo (headtracking)**, **sedthh (batch_and_vibe)**, **Barrell Titor**, **Matthew Deaves (mac-tools)**: hooking, injection, exe-hash guard and traceback-table techniques documented in REPO-DIGEST.md.
- **FoxToolworks (BW2StuffExplorer)**, **FalseIlyu (bwm-blender)**, **Boxhead78 (Redux)** (https://github.com/Boxhead78/Black-White-2-Redux), **kytulendu (TigerUnlocker)**, **adampasek (RTX experiments)**: format work and reference material.
- **ThirteenAG**: Ultimate ASI Loader, the community-standard injection path. **Tsuda Kageyu**: MinHook.
- **NSA Ghidra**, **Google BinDiff**, **Unicorn**, **Capstone**, **pefile**: the analysis tools.

## The Black & White community
- The community Discord, bwgame.net, bwrealm and the PBnW Modding Wiki archive: twenty years of format notes, balance tables and bug lists (RESEARCH-SWEEP.md).
- 2026 executable work that serves as independent cross-checks here: **Part-time God** (mouse-fraction fix, windowed mode, UI texture, population and migration patches), **TallyMark** (villager scheduler, memory-leak fix), **crazy hacker man** (SafeDisc unwrapping, mod loader, LuaJIT, netplay, glTF loader, posted addresses), **shivazgod** (multiplayer determinism notes), **HomoRexual** (byte-for-byte recompile), **zixpixel** (netplay testing, Mac dev-file lead), **Bill** (the "one shared foundation" principle), **Blu / Dr.Maxy / Herolich1 / KnowingOwinga** (PLUS, balance research, dialogue extraction).
- Name transfer cross-checks (2026-10-28): **crazy hacker man**'s decompilation knowledge pack of his commit ad164419 (MIT, his choice offered and the owner's pick on Discord, 2026-10-28; a LICENSE file in his next export is requested) is the independent PC side check of our vtable slot numbering (2,079 band slots compared, 0 disagreements, `BW2Project/05_work/analysis/community/chm_vfunc_crosscheck.out.txt`) and a second source beside the Mac symbols on the T06 tables; every method name in it is his and stays his, in a column of its own, never replacing a Mac name. **Part-time God**'s pathing package (license not stated, kept local) supplies the decompiled bodies the T06 holdouts read where his index has the address, and the class list of the pilot.
- Richard Evans and the original Lionhead programmers (David Bryson, Daniel Deptford and Tid Cooney, as the games' own credits name them) for the engine being interesting enough to spend this effort on.

## Tooling
- Built with Claude (Anthropic), Fable for planning and auditing, Opus for engineering, Sonnet and Haiku for batch work, under the verification rules in PLAYBOOK.md.
- The community's public Discord channels were read for research.

If you are missing from this list and should be on it, open an issue.
