# GitHub survey: everything Black & White (checked 2026-10-22)

Purpose: never redo groundwork someone already did. ~25 search queries plus full repo lists of openblack, HandsomeMatt, Daniels118, sp00nznet, bwrsandman. Pokemon Black/White and other name collisions filtered out.
Local copies: BW2Project/07_reference/community and BW1Project/07_reference/community (not uploaded to our GitHub).

## BW2: what already exists (and what we must NOT rebuild)
| Repo | What it gives us | Our plan |
| --- | --- | --- |
| openblack/bw2-modding | Docs: .al .bwe .bwm .chl .stuff formats, LHVM opcodes, shaders | Use as the spec for all format work |
| openblack/bw2-unstuff | .stuff extractor (C, MIT) | Basis for the Phase 0 round-trip check (we add the repacker) |
| openblack/bw2-tools | CHL disassembler toolkit | Use; don't write our own disassembler |
| openblack/bwmtool | .bwm model -> OBJ | Use for model work |
| openblack/BWMViewer | .bwm viewer | Reference |
| FalseIlyu/bwm-blender | Blender import/export for .bwm | Use for any model mods |
| Boxhead78/Black-White-2-Redux | Full script source of the Redux mod | Best real-world example of BW2 script modding |
| sp00nznet/bw2 | Recon only (no code lifted yet) | Our project is ahead of it now |
| kytulendu/BW2TigerUnlocker | Tiger unlocker that works on modern Windows | Reference |
| N00bScriptr/Black-and-White-2-Install | PowerShell install helper | Reference |
| Tatzq/Black-and-white-2-Aztec_Undead_God | A content mod (assets) | Moved to 09_community/content-mods |
**Gap nobody has filled:** a BW2 decompilation or runtime hook framework. That's our lane.

## BW1: very well covered
| Repo | What it gives us |
| --- | --- |
| openblack/bw1-decomp | Matching decompilation of BW1 v1.2 (107 stars, active daily) |
| openblack/openblack | Engine reimplementation (1,590 stars) |
| openblack/bw1-patches | Binary patches + Rust patch tool (targets unprotected exe) |
| openblack/bw1-devmenus | Re-enables developer menus |
| openblack/bw1-setup, bw1-ci-setup | Replacement graphics-options tools |
| HandsomeMatt/bw1-mp | Multiplayer server emulator |
| Daniels118/blackandwhite, blackandwhite_ci | CHL compiler / decompiler for BW1 and Creature Isle |
| Daniels118/BWCI_debugger | Script debugger attached to the LHVM |
| Daniels118/BW_mods_loader | DLL mod loader (ddraw.dll proxy) + console. Exactly the hook pattern we plan for BW2 |
| Daniels118/BWLandEditor | Modern 3D land editor |
| bwrsandman/bw1-ai-sandbox | The harness openblack uses to run parallel Claude decomp workers in sandboxed VMs. Directly relevant to our playbook |
| sedthh/batch_and_vibe | LLM-driven creature mod (chat with your creature) |
| BW1-VR/BW1-VR | VR mod |
| itsloopyo/black-and-white-headtracking | Head tracking mod |
| sp00nznet/bw | Static recompilation, 569 recovered types |
| barrelltitor/black-and-white-fixes, furudbat/linux-blackandwhite, 87611JoeJohn/black-and-white-linux, matthewdeaves/black-and-white-mac-tools, abugher/black_and_white_launcher | Fixes, Linux/Mac running guides, launcher |

## Same studio / same methods (links only, not downloaded)
- sp00nznet/pcrecomp: the static-recompilation toolbox behind sp00nznet/bw and bw2
- sp00nznet/themovies, best4bond/OpenTheMoviesGame: The Movies (Lionhead, 2005), the same engine generation as BW2
- BuffJesus/FableDecomp: "evidence-gated" Fable reconstruction (Lionhead), methodology close to our playbook
- DohmBoy64Bit/anvil-decomp, PCRECOMP-Next: pcrecomp forks with Ghidra/AI tooling
Removed as junk or duplicate: Tatzq/bw2-mod (copy of bw2-modding), AzuraK/Black-White-2 (empty), nortrix04-crypto overlay (two HTML text files).
