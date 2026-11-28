> **Project status note (2026-10-22):** several 'gaps' listed at the end are already closed by THIS project:
> white.exe SafeDisc status (v1.0/v1.2 wrapped; fan-patch v1.42 and BotG fan-patch exes decrypted), RTTI census (1,366 base classes; BotG 2,840 entries),
> Mac BW2 symbols (195,744 defined, 122,650 game C++, 94% class overlap), ScriptLibraryR.dll unencrypted. See INVENTORY.md and BW2Project/06_reports.

# Community Repo Digest for the BW2 Decompilation Project

Generated 2026-10-22 from the downloaded repos in:
- `C:\Dev\Game\BW2Project\07_reference\community\` (abbreviated `BW2C/` below)
- `C:\Dev\Game\BW1Project\07_reference\community\` (abbreviated `BW1C/` below)

Everything below comes from the repos' own docs and source. Paths are relative to those two roots. "Unverified" means a repo says it but nobody has confirmed it against a BW2 binary.

---

## Part 1: One section per repo

### BW2 repos

#### BW2C/bw2-modding (HandsomeMatt). The main BW2 knowledge base
- **What it is:** a documentation repo for modding Black & White 2. It covers file formats, the LHVM script VM, original script sources, enum headers and shaders. License: none found in the repo root (check before you copy text from it).
- **Key facts:**
  - `file_formats/stuff.md`: the `Everything.stuff` archive. It has no header. The data comes first and the directory sits at the end. The last 4 bytes are a u32 offset to the directory. Each entry is 268 (0x10C) bytes: `char name[256]; u32 offset; u32 size; u32 unknown`. Entry count = `(filesize - dirOffset - 4) / 268`.
  - `file_formats/bwm.md`: the model format. Magic `LiOnHeAdMODEL` + 27 NUL bytes (40 bytes in all), u32 size, magic `0x2B00B1E5`, version 5 or 6, metadataSize, then the model header (bbox, centre, height, radius, volume, then counts for materials, meshes, bones, entities, unknown1, collision points, vertices, strides, type (2=model, 3=skin) and indices). Material defs are 7x char[64] (diffuse, light, growth, specular, animated, normal, type). Mesh descriptions, material refs (8 u32), bones (4x3 floats), entities (4x3 floats + char[256] name), strides (count + id/size pairs, 136 bytes), the vertex stream and u16 indices. v6 adds a "model cleaves" list. An ImHex pattern is included.
  - `file_formats/al.md`: the animation library format (`Art\binary_anim_libs`). Magic `0xFEC`, `0xFFFF0002`, char[64] name, armature of `char[32] name; u32 parent`. Animation metadata uses version 9, type 3 = hierarchical, frameCount, samplingRate, duration, isCyclic. Anim events use names like `LHAnimEvent04` / `::FOOTSTEPS.99`. The keyframes are only partly understood: frame-0 full quaternions, then compressed `s16[3]` quaternions. An ImHex pattern is included.
  - `file_formats/bwe.md`: "Mission Environment Script" files. They are plain-text commands, one per line, such as `CREATE_VILLAGER_POS("x,y","x,y","GREEK_HOUSEWIFE",23)`. The file lists about 170 command names extracted from the binary, in groups: cheats, engine, file access, creature, multiplayer, camera, land.
  - `file_formats/chl.md` and `lhvm/opcodes.md`: the compiled challenge (CHL) format and the LHVM opcodes. See Part 3.
  - `lhvm/native_functions.md` (3293 lines): native function tables with ID, name, "Pointer", stack in, stack out and an unknown column. Counts: BW1 has 464, Creature Isle 528, **BW2 1106**, BOTG 1160 (54 more than BW2). Stack-in `4294967295` means a variable count.
  - `lhvm/enums/` holds **96 original Lionhead C headers**. Examples: `ScriptEnums.h`, `info.h` (it includes `<LHLib/LHTypes.h>`), `CreatureEnum.h`, `GameBalanceGeneral.h`, `ObjectTypeEnum.h`, `VillagerInfoEnum.h`, `PlatoonInfoEnum.h`, `EpicSpellEnum.h`, `LHKeyBoard.h`. They appear to ship with the official BW2 modding tools. These are real BW2 enum names and values.
  - `lhvm/scripts/` holds 368 original uncompiled BW2 script `.txt` sources, including Land*, Globals and ChallengeHeaders.
  - `lhvm/tools/`: `Script Compiler.exe`, `ScriptLibraryR.dll` (the challenge DLL: it parses, compiles, loads and runs scripts) and `bw2text.lhts` (an LHTS string table).
  - `lhvm/docs/*.md`: the official "Lionhead Challenge Language Documentation" (statements, conditions, objects, constants, exceptions).
  - `shaders/README.md`: `data\encryptedshaders` are encrypted with a Caesar shift (each byte minus 3). `decrypt.cs` and `encrypt.cs` are included, plus 36 decrypted shaders (DirectX PS 2.0).
- **Reuse directly:** the enum headers, the native function table (names, IDs, stack in and out), the opcode table, the format specs, the ImHex patterns and the original script sources. The scripts can be used to check CHL line numbers.
- **Warnings:** the opcode table contains copy-paste errors. For example, DIVIDE is listed under type 8, and opcode 45 (SQRT) "pops two". The "Pointer" column is not explained. The BW2 values range from 13216392 to 13437998 (0xC9AB88 to 0xCD0C2E), and some are not 4-byte aligned. They may be string or descriptor addresses, not code. The game version is not stated. Treat them as unverified.

#### BW2C/bw2-tools (HandsomeMatt). MIT, C#, premake4/5
- **What it is:** two command-line tools. `src/app/unstuff/Program.cs` is a .stuff extractor. `src/app/chlextract/` (ChallengeFile.cs, ASMOut.cs) is a CHL disassembler. `CONTRIBUTING.md` contains only boilerplate.
- **Key facts:** `ChallengeFile.cs` checks the header `LHVM` (0x4D56484C) and **version 12** ("new in bw2 from bw1"). It then reads globals, 20-byte instructions, an "unknown section" (actually the autostart list, see chl.md), the functions table, a data section, **4096 NUL padding**, a u32 that must be 0, and saved globals (`u32 unk; float value; string name`). `ASMOut.cs` gives mnemonics for opcode types 0 to 46. Type 1/22/24 instructions carry jump targets.
- **Reuse:** the CHL reader logic, which is small and easy to port to Python.
- **Warnings:** ASMOut mnemonics differ from bw2-modding's names (for example type 1 = `jmpc` and type 29 = `except`). Global indexing in `pushv` has a bug: it uses `GlobalTable[func.VariableOffset - 1]` instead of the parameter.

#### BW2C/bw2-unstuff (HandsomeMatt). MIT, C# .NET (VS sln)
- A standalone `Everything.stuff` extractor: `BW2Unstuff/Program.cs`. It uses the same format as above. The 268-byte directory entry is mapped with `StructLayout` (256 + 4 + 4 + 4). Usage: `unstuff Everything.stuff (outdir)`.

#### BW2C/bwmtool (HandsomeMatt). MIT, C# 7, VS2017
- Converts BWM to OBJ. `BWMTool/BWM.cs` gives the header offsets (counts at 68 to 120 after the header) and notes "Stride count of more than 1 not supported". The vertex stride is 32 bytes (pos, normal, uv) and extra bytes are skipped. A comment quotes decompiler output: `*((_DWORD *)v75[66] + 30) = 2;`. This suggests the author traced the loader in IDA.

#### BW2C/BWMViewer (HandsomeMatt). MIT, C# WinForms, premake5
- A BWM viewer. `src/BWMLib/ModelReader.cs`, `Model.cs`, `SkinModel.cs` (the bones code is a stub). The README is one line.

#### BW2C/bwm-blender. GPL-3.0, Python Blender add-on (Blender 2.93+)
- A BWM import and **export** add-on. `operator_utilities/file_definition_bwm.py` is the most complete BWM parser and writer:
  - Enums: `FileType` MODEL=2, SKIN=3. `StrideType` POINT=0, NORMAL=1, UV_MAP=2, BONE_INDEX=6, BONE_WEIGHT=7. `StrideSize` FLOAT=0, TUPLE=1, POINT_3D=2, INT=3, BYTE=4.
  - Record sizes from `metadataSize()`: header 0x80, matDef 0x1C0, meshDesc 0xDC, matRef 0x20, bone 0x30, entity 0x130, unknown and collision 0xC, stride 0x88.
- **Reuse:** a round-trip BWM writer, which can be used to test a decompiled loader. GPL-3.0 limits copying the code itself.

#### BW2C/Black-White-2-Redux. No license file ("credits would be nice")
- Source of the Redux mod: 371 `.txt` LHVM script sources (Campaign Land1 to 22, Skirmish AI/GodEngine, Globals, Spells). `Globals/Localization/BotgDialogues.txt` points to a BOTG-era base.
- **Reuse:** real-world script usage examples and a second corpus to test a CHL compiler against.

#### BW2C/BW2TigerUnlocker (kytulendu). GPL-3.0, C, Code::Blocks
- Rewrites the Collector's Edition tiger unlocker. `main.c` reads `HKLM\Software\Microsoft\Windows\CurrentVersion\ProductId` (WOW6432 view), with the NT CurrentVersion key as a fallback. It writes **`Unlock.dat`**: each byte of the ProductId string XORed with the bytes of the int `1234567890`, key byte `(i & 3)`.
- **Reuse:** the `Unlock.dat` format. The game must contain matching check code, which gives a search target in white.exe.

#### BW2C/Black-and-White-2-Install (N00bScriptr). PowerShell
- `Install-BW2.ps1` mounts the ISO, runs `arun.exe`, then patches v1.1, v1.2 and the fan patch 1.42. It contains no technical content. It confirms the usual patch chain: 1.1, then 1.2, then 1.42.

#### BW2C/bw2 (sp00nznet). MIT (own work only)
- **What it is:** stage-0 notes for a static recompilation of BW2 with the pcrecomp toolchain. Nothing has been lifted yet. The files are `README.md`, `analysis/catalog.txt` and `analysis/installer-listing.txt`.
- **Key facts:**
  - The installer is **InstallShield 9** on 4 discs (data1.hdr, data1.cab, data2.cab per disc). There are 895 file entries.
  - **`white.exe` = 21,739,061 bytes** (file index 893, not on disc 1).
  - The launcher `Black & White 2_code.exe` is 323,584 bytes, built 2005-08-19 with linker 7.10 (**MSVC 7.1 / VS.NET 2003**). It is not wrapped.
  - `DrvMgt.dll` plus `00000001.TMP` and `00000002.TMP` on the disc are signs of **SafeDisc**. Nobody knows yet whether white.exe itself is wrapped.
  - Other installed files: `PlugIns/ScriptLibraryR.dll` (679,936 bytes), `dbghelp.dll` (894,464), `binkw32.dll`, `Data/Everything.stuff` (1,427,446,404 bytes), `bw2text.bin`, `Speech.lug`, many `Audio/**/*.lug`, and `Scripts\BW2/Land*.chl`.
  - Plan: run `disasm32.py`, then `tools/cpp/rtti.py` and `tools/cpp/vtable_scan.py` (in the pcrecomp repo, which is not downloaded). Then test BW1's 569 type names as hypotheses against BW2.
- **Warnings:** pcrecomp's own measurements show that RTTI and vtable scanning "added names, not functions". Do not assume SafeDisc covers or does not cover white.exe. On another title the disc markers covered only the launcher.

### BW1 repos

#### BW1C/bw1-decomp (openblack). CC0-1.0. The model to copy
- **What it is:** a matching decompilation of BW1, built on `encounter/dtk-template` and adapted for PE/COFF and PEF. Versions: BW1W100, BW1W110 (MSVC6 SP4), BW1W120 (MSVC6 SP5, the main target), BW1W130, BW1WCI, plus Mac PEF builds BW1M110, M115 and M119 (CodeWarrior). Tooling is Python + ninja. Tool versions in `configure.py`: dtk `v0.0.33` (openblack fork of decomp-toolkit), objdiff `v3.7.2`, wibo `1.2.0` (runs cl.exe on Linux), lld-link tag `bw1-decomp-024`, MSVC 6.4/6.5 compilers.
- **Docs read:** `README.md`, `AGENTS.md` (697 lines, the main rules), `CLAUDE.md` (`@AGENTS.md`), `docs/getting_started.md`, `symbols.md`, `splits.md`, `porting_versions.md`, `differential_testing.md`, `transformations.md`, `msvc6_inliner.md`, `msvc6_scheduler.md`, `msvc6_comdat.md`, `msvc6_passes.md`, `villager_states.md`, `.claude/agents/*.md`, `.claude/skills/*/SKILL.md`.
- **Key techniques:** see Part 2.
- **Config:** `config/<VER>/config.yml`, `symbols.txt` (44,369 lines for W120 and 52,429 for M119), `splits.txt` and `build.sha1`. Per-DLL configs live in `config/LHAudio`, `LHDialog`, `LHLog` and `LHMultiplayer`.
- **Tools** (`tools/`): `decomp-diff.py`, `decomp-verify.py`, `decomp-regress.py`, `decomp-relocs.py`, `codegen-check.py`, `c2-il.py`, `c2-inline-log.py`, `c2-sched-log.py`, `inline-budget.py`, `tiebreak-probe.py`, `variant-sweep.py`, `comdat-scan.py`, `minmax-scan.py`, `mac-disasm.py`, `port_version.py`, `pre_dtk_patch.py`, `post_link_patch.py`, `check_addr_comments.py`, `check_stub_addrs.py`, `review-lint.py`, `msvc_probe.py`, `difftest/` (a Unicorn emulator).
- **Warnings:**
  - `AGENTS.md` and the sandbox refer to `tools/cwdemangle.py`, `mac_symbol_version.py`, `idioms.py`, `decomp-similar.py`, `decomp-queue.py` and `docs/msvc6_idioms.md`. **None of these exist at this checkout** (commit 570e31c, 2026-10-22).
  - Decrypting the SafeDisc-wrapped exe is "undocumented tribal knowledge".
  - The decryptor "vandalizes" the header. `pre_dtk_patch.py` and `post_link_patch.py` strip and restore it.
- **Reuse for BW2:** the whole workflow (dtk + objdiff + ninja + agent docs). BW2 needs **MSVC 7.1** (VS.NET 2003) compilers instead of MSVC 6, so none of the c2.dll 8966 inliner and scheduler findings carry over directly. The method of patching c2.dll to log its decisions does carry over.

#### BW1C/bw1-ai-sandbox (Sandy Carter). MIT, Python
- **What it is:** runs many `claude -p --dangerously-skip-permissions` decomp workers in parallel. Each worker gets its own Docker container inside one libvirt/KVM VM. Files: `sandbox.py` (CLI), `portal.py` (web UI), `mcp_server.py` (orchestrator MCP), `vm_agent.py`, `worker_entry.py`, `Dockerfile`, `cloud-init.yaml.in`, `nwfilter.xml.in`, `sandbox.toml`, `preamble.md`, `.claude/skills/sandbox-orchestrator/SKILL.md`.
- **Details:** in Part 2 ("Splitting work for AI agents").
- **Warning (from the README):** "security features are best effort". The OAuth token is inside the VM. Agents can write to the Ghidra project over MCP.

#### BW1C/openblack. GPL-3.0, C++20, CMake + vcpkg
- **What it is:** a clean-room engine reimplementation of BW1, not a decompilation. Docs: `readme.md`, `.github/contributing*.md`, `components/ScriptLibrary/README.md`, `components/ScriptLibrary/docs/opcodes.md`, `apps/glwtool/readme.md`.
- **Format components** (`components/*`, each with a header comment describing the layout):
  - `pack`: `PackFile.cpp` describes the "LiOnHeAd" 8-byte magic, then blocks with a 32-char name and a u32 size. Block names include MESHES (magic `MKJC` + L3D list), INFO, Body (ANM list) and LHAudioBankSampleTable.
  - `l3d`: `L3D0` magic, 76-byte header.
  - `lnd`: 1052-byte header and 17x17 cell blocks of 2520 bytes.
  - `anm`: 84-byte header.
  - `morph`, `glw`.
  - `ScriptLibrary`: LHVM VM and compiler. `LHVMTypes.h` lists `LHVMVersion { BlackAndWhite=7, CreatureIsle=8, BlackAndWhite2=12 }`, but `LHVMFile.cpp` returns early for any version other than 7.
- **Apps:** anmtool, glwtool, l3dtool, lhvmtool, lndtool, morphtool, packtool.
- **Also:** `src/LHScriptX/` (lexer and command table for the plain-text map and feature scripts, the BW1 form of `.bwe`), `src/Parsers/InfoFile.cpp`, `src/ScriptHeaders/ScriptEnums.h`.
- **Reuse:** the Pack/LiOnHeAd container. BW2 files use the same Lionhead libraries (the BWM magic is `LiOnHeAdMODEL`). Also the LHVM VM skeleton and the LHScriptX lexer, which can be adapted for `.bwe`. **Warning:** these were written for BW1. BW2 versions of the formats differ.

#### BW1C/bw (sp00nznet). MIT (own code). Vendored: bw1-decomp headers (CC0), kjmp2 (zlib), SafeDiscLoader2 (GPL-3)
- **What it is:** a static recompilation and reimplementation of BW1 v1.0, about 72k lines of C++ (CMake, VS2022 Win32). Docs: `README.md`, `CHANGELOG.md`, `docs/*.md` (chl-format, info-dat, level-loader, physics, creature-chooser, villager-states, gestures, psys-files, town-economy, constructors, players, fire), `tools/decomp/README.md`, `work/ANALYSIS_REPORT.md`, `work/ghidra_decompile_plan.md`, `vendor/*/README.md`.
- **Type recovery** (`tools/decomp/README.md`): the v1.0 exe has **full RTTI but no symbol table** (17,627 functions, 5 named). The pipeline goes class name, then RTTI `??_7Class@@6B@` vftable label, then slot pointers, then decompile. Slot *i* pairs with the *i*-th virtual in the bw1-decomp header. `bw_decomp.py` uses IDA 9.1 idalib and is the primary tool. `GhidraDumpClass.java` is the cross-check. It needs the "Windows x86 PE RTTI Analyzer" enabled, otherwise it finds 0 vftables. The walk stops at the next `??_7` address. A "slot-filtered extraction" pulls the same slots across all classes (for example, Save/Load slots 58 to 61 across 200 classes), and turns the shared-shape bodies into a data table (`parse_saveslots.py` then `gen_saveload.py`).
- **The `mkfunc` lesson:** IDA leaves large inlined initialiser regions unanalysed. Xref searches then come back empty because the code was never parsed. Forcing code and functions over a range (`bw_decomp.py mkfunc 0x47F000 0x484000`) recovered a 1,513-line action-table initialiser.
- **Emulating initialisers:** `.bss` predicate tables are filled at runtime, so the project recovered them by emulating their initialiser code (CHANGELOG lines 210 and 238).
- **`info.dat`:** 44-byte header (`LiOnHeAdInfo`, payload size at 0x28), then 102 sections with no tags. The layout was recovered by summing the loader's per-class read sizes with IDA (`work/info_layout.py`). The check: the sum equals the size in the header, and the walk ends on the last byte of the file.
- **CHL v7** (`docs/chl-format.md`): globals, code (20-byte instructions), autostart, scripts (name, source file, type, var_offset, var_count, names, first instruction, parameter count, id), data. Variable ids form one space. An id greater than var_offset is a local; otherwise it is `global[id-1]` (1-based). They warn that opcode semantics are "unverified against the game's interpreter". Opcode 1 is a conditional jump, not a wait.
- **Analysis report:** 936 RTTI classes and the hierarchy `Base > GameThing > GameThingWithPos > Object > ...`.
- **Reuse for BW2:** the RTTI vftable walk scripts. The BW2 README says white.exe may keep RTTI, which is unknown until it is extracted. Also the idea of "extract shared-shape method bodies as data", and the emulation of initialisers.

#### BW1C/bw1-patches (openblack). Rust tool `bw1patch` + TOML patches. No root LICENSE file; `tool/Cargo.toml` declares MIT
- 15 documented exe patches, each in `docs/*.md` and `patches/*.toml`. Each op has `kind` (nop and others), `offset` and `len`, and **`expect` bytes that are checked before writing**. The README states this check.
- Facts that may also apply to BW2:
  - The VRAM signed-int overflow forced low-res textures on cards with 2 GB or more (`GetTextureAvaliable() < 13000000`).
  - A CPU-family check capped landscape LOD.
  - The windowed-mode registry value was ignored because of an unconditional `SetWindowStyle(&window,1)`.
  - Max resolution above 2048 fails inside `d3dim700.dll` `DIRECT3DDEVICEI::Init` (`0x88760082`). This is fixed with a proxy DLL.
  - Screenshots came out pink in windowed mode because the game read the front buffer instead of the back buffer.
- **Reuse:** the TOML patch format with expected-byte checks.

#### BW1C/bw1-devmenus. Python (uv, ruff, pytest). No license file
- `patch_devmenus.py` re-enables the developer key menus in `runblack.exe` 1.42 (SHA-256 `3f58bc02...aba`). It is a model for a code-cave patch:
  - It grows the `.text` VirtualSize to cover the zero padding.
  - It puts a stub at 0x8A8960, with state at 0xFBFE00 in the unused `.data` tail.
  - It hooks with `e9` jmp at 0x63F2C6.
  - It has a tiny built-in two-pass assembler (`class Asm`).
- It names the unreached `PCInput.cpp` handlers (0x640CC0 and others), the debug-text print at 0x63ED40 and the game pointer at 0xD0195C. The README says "The patched program has not been run yet."

#### BW1C/black-and-white-fixes (Barrell Titor). MIT, Python
- A 5-byte screenshot fix for BW1 and Creature Isle. Reversible, with SHA-256 checks before and after, verified backups and `status` and `restore` commands. It changes the address bytes in pointers (`0xE85080` to `0xE85084`) so capture reads the back buffer. A clean template for a safe patcher. "Developed with AI assistance."

#### BW1C/BW_mods_loader (Daniels118). GPL-3.0, C++ (VS)
- A **`ddraw.dll` proxy**. `ddraw/exports.def` lists 20 forwarded exports (DirectDrawCreate, DirectDrawCreateEx, ...). `dllmain.cpp` loads the real `%SystemDir%\ddraw.dll`, then `LoadLibraryA` on every `.dll` in `<exe dir>\mods\`. `/console` on the command line calls `AllocConsole` and redirects stdout. It is derived from BW Ultimate. Warning: "never run the game as administrator" because mods run with your rights.

#### BW1C/BWCI_debugger (Daniels118). GPL-3.0, C++ (VS)
- A CHL script debugger, loaded through BW_mods_loader. It has a gdb-like text interface and partial xdebug.
- It hooks with **Microsoft Detours** (`DetourTransactionBegin`, `DETOUR(...)`).
- It gets `GetModuleHandleA("ScriptLibraryR.dll")`, resolves exports with GetProcAddress, and calls `Version()`: 7 means vanilla, 8 means Creature Isle. It then sets **internal offsets relative to the DLL base** for each version (`SETOFFSET(lhvmCpuLoop, 0x8230)`, `opcodesImpl 0x21610`, `ppNativeFunctions 0x3BDE0`, ...).
- It detours exports (`Initialise`, `LoadBinary`, `RestoreState`, `StartScript`) and internal functions (`lhvmCpuLoop`, `opcode_24_CALL`, `doStartScript`, `stopTask0`).
- Game-side hooks at fixed addresses: `GGame::LoadScriptLibrary`, `GGame::Init`, `PauseGame`. It picks BW1 or CI by `*(DWORD*)0x00800000 == 0xD4B95308`.
- `ScriptLibraryR.h` holds recovered structs: `Instruction {opcode, mode, datatype, union value, linenumber}`, `NATIVE_FUNCTION {fn, stackIn, stackOut, unknown, char name[128]}`, Task, Script, Stack, ExceptStruct, and `Initialise(a1, NATIVE_FUNCTION*, errCb, enterCb, exitCb, a6, stopTaskCb)`.
- **Reuse for BW2:** BW2 also ships `PlugIns/ScriptLibraryR.dll`. The same export-plus-version approach should work. BW2 LHVM is version 12 (unverified for the DLL's `Version()` return value).

#### BW1C/blackandwhite and blackandwhite_ci (Daniels118). GPL-3.0, Java 11 (`chlasm`, `chlasm_ci`)
- CHL compiler, assembler and disassembler for BW1. The CI version also decompiles. `dev_doc/exceptions.txt` explains how exceptions compile: EXCEPT, then ENDEXCEPT, a jump, the handler, ITEREXCEPT. An "until" emits `PUSHB false; SYS SET_WIDESCREEN; SYS END_GAME_SPEED; SYS END_DIALOGUE; SYS END_CAMERA_CONTROL; ... BRKEXCEPT; JMP`.
- `dev_doc/instruction_examples.txt` pairs every documented statement with its bytecode, taken from the original scripts of the Mac version. `opcodes.xlsx` and `headers/*.h` are included.
- **Reuse:** the method for writing a BW2 (v12) CHL compiler and decompiler that round-trips the original BW2 script sources in bw2-modding.

#### BW1C/BWLandEditor (Daniels118). GPL-3.0, Java (libGDX, gradle)
- A GUI editor for BW1 `.lnd` landscapes (403 Java files under `core/src/it/ld/bw/lndgui`). It has no format docs beyond its code. It is useful only if BW2 `.lnd` is close to BW1's (unknown).

#### BW1C/bw1-mp (HandsomeMatt). MIT, C# .NET 10
- An emulator for the dead online servers: an HTTP login, db and storage server, a GameSpy PeerChat IRC server and a partial GameSpy master server. The writeups document:
  - Login reply: `bnwuserid:%d %d %d %s`.
  - **LHWebEncode**: each nibble + 0x41.
  - An obfuscation based on Fibonacci hashing (constant `0x61C88647`), implemented in `BWMP/Lionhead.cs` as `Obsufucate`.
  - The db result table format: `[rows]:%d[columns]:%d[totalcolumns]:%d`, cells wrapped in 0x02/0x03.
  - PeerChat `USRIP` and nick `BNW_<uid&0x1FFFFFFF|0x20000000>`.
  - `structs.txt` holds recovered structs.
- **For BW2:** if BW2 used GameSpy or Lionhead web services, the same encode and obfuscation may be present (unverified).

#### BW1C/bw1-setup and bw1-ci-setup (HandsomeMatt). MIT, C# WinForms
- Replacements for the BWSetup options apps. The registry values are in `BWSetup/Configuration.cs`. No other docs.

#### BW1C/BW1-VR (Buck). MIT, C++ (CMake, VS2022, 32-bit)
- **What it is:** a VR mod. It is a `ddraw.dll` proxy with **MinHook** hooks in `runblack.exe` 1.42, plus dxwrapper (DX7 to D3D9), a D3D11 bridge and OpenXR. Docs: `CLAUDE.md`, `CONTRIBUTING.md`, and 25 docs under `bwvr/docs/`, of which `address-map.md` is the key one.
- **How it finds addresses** (`bwvr/docs/address-map.md`, "Tools and method"). It uses three independent methods and requires them to agree:
  1. An **RTTI walk** from type descriptor to complete object locator to vtable, across 1052 classes.
  2. **Assert-string attribution:** 411 source paths from `.text`, across 4,768 call sites. Each one names the `.cpp` of the function that contains it.
  3. The bw1-decomp `symbols.txt`.
- Image base 0x400000, relocations stripped, no ASLR, so every address is constant. Fan-patch byte patches do not move code, so the W120 symbol addresses still hold.
- Tools: `bwvr/tools/disasm.py` (capstone with names, plus `--find`, `--callers`, `--xref`), `re.ps1` (headless Ghidra decompile), `check_names.py` (compares Windows names with the Mac symbol parameter classes), `bwvr/ghidra/name-corrections.txt` (a name override with proof).
- `src/common/exe_check.h` checks the SHA-1 of the exe on disk before any hook and names known unsupported builds.
- Test method: desktop runs with a scripted controller (`bwvr/tools/desk/*.ps1`, `verify_build.ps1` prints VERIFIED).
- **CLAUDE.md rules worth copying:**
  - Never touch saves, the registry or the user's mouse.
  - One git worktree per agent.
  - Every behaviour change gets an ini key with the old behaviour as a value.
  - Docs hold the current state. History stays in git.

#### BW1C/black-and-white-headtracking (itsloopyo). MIT, C++ (CMake, pixi)
- Injection: `bw-headtracking-launcher.exe` starts `runblack.exe` with `CREATE_SUSPENDED`, then `VirtualAllocEx` + `CreateRemoteThread(LoadLibraryW)` with `HeadTracking.dll`, then `ResumeThread` (`src/loader_main.cpp`). Hooks use **MinHook**.
- `src/engine_addresses.h` holds documented globals for 1.42, such as the camera struct at 0xEA1D28, the scaled matrix at 0xEA9E40, the projection scales at 0xE83A00/04 and the cursor at 0xE852C0.
- `scripts/ghidra/*` are Jython probes run against a local Ghidra project ("which functions write this global").
- OpenTrack UDP protocol: 6 little-endian doubles (x, y, z, yaw, pitch, roll) on port 4242.

#### BW1C/batch_and_vibe. MIT, Python 3.11 + native C++ (built with zig)
- An LLM creature mod. Docs: `README.md`, `SKILL.md`, `docs/usage.md`, `docs/contributing.md`.
- **Hook point:** it replaces **`Plug Ins\ScriptLibraryR.dll`** with a proxy (`native/exports.txt` lists the full export set: `Initialise`, `LookIn`, `LoadBinary`, `RestoreState`, `Version`, `LoopGlobalVariables`, `OpCode`, ...). It renames the real DLL to `_bwv_original_vm.dll` and forwards every call (`native/observer.cpp` `bootstrap()`).
- It wraps `Initialise` to capture the **native function table** pointer and the enter and leave callbacks. It ticks on `LookIn`. It talks to the Python side over a named pipe.
- Compatibility checks: exact SHA-256 of the exe and the VM DLL, with a fallback to a "canonical PE layout" hash that zeroes timestamps and checksums (`native/pe_signature.h`, `src/bwv/compat.py`). The comment reads: "Fixed native offsets must never be enabled merely because a patch label matches."
- **Reuse for BW2:** the ScriptLibraryR proxy is the least invasive way into BW2's script VM, because BW2 also ships `PlugIns/ScriptLibraryR.dll`.

#### BW1C/linux-blackandwhite. CC BY-SA 4.0, guides + Bottles YAML
- Has a **BW2 section** (`Black & White 2 (2005)/README.md`, `Black-&-White-2.yml`, `-Plus.yml`). It covers Wine/Proton with DXVK on, VKD3D off, a DLL override for `binkw32`, the `d3dx9` dependency and the patch order 1.1, 1.2, 1.42. For BOTG, `BattleOfTheGods.exe` with a BOTG fan patch 1.1.

#### BW1C/black-and-white-linux (Seth). MIT, bash
- A Wine and gamescope installer. Note: dGPU OpenGL is broken on hybrid laptops, so it forces the iGPU (`__GLX_VENDOR_LIBRARY_NAME=mesa`). It needs patch 1.20 (DirectInput COM class) and patch 1.42 (Bink/SafeDisc).

#### BW1C/black-and-white-mac-tools (Matthew Deaves). C89
- `oldmacpatch` fixes a BW1 crash on OS X 10.5: a null dereference with member offset 0x6798 in `BindableAction::GetPrimaryKey`.
- **Technique:** read the **PowerPC traceback tables** in the PEF code section to recover symbols (17,973 for BW1, 18,370 for CI, fully mangled). Find the patch site structurally by symbol name and instruction pattern, not by hardcoded offset. Put the code cave in the dead traceback table of the function before it.
- Written with Claude. `docs/black-and-white.md` is a good worked example of crash forensics.

#### BW1C/black_and_white_launcher. WTFPL, shell
- A Linux Wine launcher script with backup and restore of profiles and creatures. No RE content.

---

## Part 2: Techniques to copy

### 2.1 Matching-decomp organisation (bw1-decomp)
- **Layout:** `configure.py` (generator; per-TU `Object(Matching|NonMatching|Equivalent|MatchingFor(...), "path.cpp")`), `config/<VER>/{config.yml,symbols.txt,splits.txt,build.sha1}`, `src/`, `include/`, and `orig/<VER>/` (user-supplied, git-ignored).
- **symbols.txt:** `mangled = .section:0xADDR; // type:function size:0x.. scope:global|local|weak align:4`. dtk rewrites this file. Duplicate names become `fn_0x...`.
- **splits.txt:** per-`.cpp` section ranges (`.text start:.. end:..`).
- **Build pipeline** (`docs/transformations.md`): `orig/<ver>/runblack-decrypted.exe`, then `pre_dtk_patch.py` (removes decryptor and SafeDisc damage, gives a "pristine linker shape"), then `dtk` split into per-TU `.o`, then lld-link to `runblack-linked.exe` + **`.pdb`**, then `post_link_patch.py`, then `runblack.exe`, then a **SHA-1 check against build.sha1**. The `runblack-linked.exe`/`.pdb` is loaded into Ghidra and IDA to get names and the types of matched TUs. Ghidra needs `-Dghidra.pdb.developerMode=true`.
- **Statuses:**
  - `Matching` units are linked from source.
  - `NonMatching` units are not linked.
  - A unit is "matched" only when `configure.py` is flipped.
  - `MatchingFor(...)` lists the versions where a unit links byte-identical.
- **Version porting:** `tools/port_version.py` carries splits and symbols from one build to another by bytes. Results for W120 to WCI: 25,087 of 28,590 symbols, 2,121 of 2,549 ranges. Useful if we target several BW2 builds (1.0, 1.2, 1.42 fan patch, BOTG).

### 2.2 Verifying a function
- **Fast loop:** `ninja build/<VER>/src/<path>.o` builds only the object, then `python tools/decomp-diff.py --source src/X.cpp -d "Class::Method"`. That is 1 to 2 s per cycle. Never run bare `ninja` in the loop.
- **decomp-diff.py** wraps `objdiff-cli`:
  - Overview columns: STATUS / MATCH% / SIZE / SECTION / NAME, with filters `-s nonmatching -t function --section .text`, `--sections`, `--strict` (relocation identities and addends).
  - Diff mode: target on the LEFT, ours on the RIGHT. Row markers: ` ` same, `~` operand differs `{braces}`, `|` opcode differs, `>` only ours, `<` only target. `--no-collapse`, `-C`, `--range`.
- **Before and after:** run `ninja baseline` before editing. Then `decomp-regress.py --strict --snapshot`, `decomp-verify.py --since main --run --baseline ...` (format, compiler replay, hashes, report, regressions) and `ninja changes_all`.
- **Readability passes:** `codegen-check.py snapshot/compare` proves that renames do not change the code.
- **Report three match levels separately:** report match, strict object match, resolved-byte match. "A rounded 100.0% display is not sufficient proof."
- **Differential testing for non-matching code** (`tools/difftest`, `docs/differential_testing.md`):
  - Unicorn maps the original exe. Our COFF object is linked into scratch memory.
  - It runs 10k seeded random states and compares EAX/ST0, ESP, x87 depth, hooked call sequences and every byte written.
  - Both x87 precisions (0x27F and 0x07F) are tested.
  - Failure classes: uninitialised-read (not a failure), hang, fault, float-only, real.
- **Compiler internals** (MSVC6 specific): `c2-inline-log.py` and `c2-sched-log.py` use a **patched c2.dll that logs inliner and scheduler decisions**. Prior art: gta2_re. Classify every diff before experimenting: inline budget, x87 tie-break, scheduling, or real source difference. **For BW2 the compiler is MSVC 7.1, so the same logging approach would need porting to the 7.1 c2.dll.**

### 2.3 Mac symbols as ground truth
- The BW1 Mac PEF build (CodeWarrior) kept real mangled symbols (`config/BW1M119/symbols.txt`). AGENTS.md: "Ground truth for a function's name, class, argument list and const-ness is the demangled CodeWarrior symbol." Windows names were rebuilt from it.
- CW mangling has no return type, calling convention or static flag. Get those from the Windows code (`ret N`, ECX for `this`, al versus eax).
- Derived rules:
  - Mac `T&` means a reference in the header.
  - A missing first parameter in the Mac symbol means a hidden return buffer, so return by value.
  - `mac-disasm.py` disassembles PPC bodies to settle argument order and empty destructors.
- Each header declaration carries a comment like `// BW1W120 0054cbd0 BW1M119 010cca10`, checked by CI (`check_addr_comments.py`).
- **RTTI in the Windows binary is absolute ground truth for RTTI type names.**
- black-and-white-mac-tools shows that PPC **traceback tables** also carry names.
- **For BW2:** a Mac port of BW2 (Feral, 2006, PPC/Intel Mach-O) may exist with symbols. Nobody in these repos checked. **This is worth investigating.**

### 2.4 Splitting work for AI agents (bw1-ai-sandbox + bw1-decomp agent files)
- **Isolation:**
  - The host runs one KVM VM with no shared folders and no graphics.
  - A host-side libvirt **nwfilter** allows only TCP 443 to Anthropic's API range (`160.79.104.0/23`), the host Ghidra port (8080), DHCP and inbound ssh. DNS and IPv6 are dropped. `sandbox.py check` proves this by trying 1.1.1.1, 8.8.8.8 and GitHub.
  - Each worker runs in a Docker container: uid 2000, `cap-drop ALL`, `no-new-privileges`, read-only rootfs, private /tmp, 2 CPU / 4 GB.
- **Code flow:**
  - `sync` pushes the user's committed HEAD to a mirror inside the VM.
  - Each worker gets `/work`, a clone on `agent/<name>`. Its `origin` is read-only (fetch works, push fails).
  - The toolchain (`build/tools`, `build/compilers`) and `orig/` are root-owned and read-only (hardlinked or mounted at `/opt/orig`).
  - Results leave only when the human runs `fetch`, which creates `sandbox/<name>` branches. The human reviews and cherry-picks.
  - Nothing pushes to GitHub.
- **Worker prompt** (`preamble.md`):
  - It starts: "unattended worker... nobody will answer".
  - Work only in `/work`. Run commands in the foreground. No internet. Commit at milestones. End with a summary.
  - Check shared knowledge first (`idioms.py list/grep/show`, `decomp-similar.py`, `decomp-queue.py blockers`).
  - Record only *proven, generalisable* learnings in `~/learnings.md` (outside the repo) in a set format: `### slug / Rule: / Diff signature:`.
- **Orchestrator** (`.claude/skills/sandbox-orchestrator/SKILL.md`):
  - One objdiff unit per worker. No two workers share a `.cpp` or header. The orchestrator is the lock and keeps an assignment table.
  - It polls `workers`, uses `resume` for nudges, `pause` near usage limits, and auto-resumes after the limit resets.
  - On collect it checks: "edits stay inside its unit; **no fakematches or padding; no build/tool/config tampering**; no edits to shared headers that clash".
  - It curates learnings into `docs/msvc6_idioms.md` only with the user's approval. Concurrency is about VM CPUs divided by worker CPUs (4).
- **How cheating is prevented** (rules in AGENTS.md, villager-matcher.md, inline-budget-matcher.md and the villager-state-matching SKILL):
  - Fakematches are "negative progress". Agents must never add dummy locals or contort code. The fakematch decision belongs to the human.
  - **12 build-diff cycles per function cap**, then log `deferred`. "Deferring is a successful outcome."
  - Forbidden edits: `configure.py`, `symbols.txt`, `splits.txt`, other units' `.cpp`, struct layout. Ghidra MCP is read-only for workers.
  - No `extern` for globals. No `__FILE__` or `__LINE__`. No `_LH_3D_LIB_` tricks. No padding to restore percentages.
  - "Show it matching": a PR must show objdiff 100% plus the CI report. One TU per PR.
  - Proof against tampering is structural: hashes in `build.sha1`, a read-only toolchain, a read-only origin, and a human review of each branch.
  - Unit claims and ledger: `vsm.py claim/next/log/release`. Every attempt is logged as matched, improved or deferred, with pct and notes.
- BW1-VR adds: one git worktree and branch per agent, land by fast-forward, brief each agent with a file.

### 2.5 Hooking the running game
| Project | Entry point | Hook library | How addresses are found | Version safety |
|---|---|---|---|---|
| BW_mods_loader | `ddraw.dll` proxy (forwards to `%System%\ddraw.dll`), loads `mods\*.dll` | none (loader only) | n/a | none |
| BWCI_debugger | loaded as a mod by BW_mods_loader | **Microsoft Detours** | ScriptLibraryR exports by name + per-version offsets from the DLL base; game functions at fixed VAs | `ScriptLibraryR!Version()` (7/8) and a dword at 0x800000 |
| BW1-VR | `ddraw.dll` proxy (+ dxwrapper) | **MinHook** | RTTI walk + assert-string attribution + bw1-decomp symbols.txt, cross-checked; `name-corrections.txt` | SHA-1 of the exe on disk before hooking (`exe_check.h`) |
| headtracking | launcher: `CREATE_SUSPENDED` + `CreateRemoteThread(LoadLibraryW)` | **MinHook** | Ghidra Jython probes on writers and readers of globals | developed against 1.42 |
| batch_and_vibe | **`Plug Ins\ScriptLibraryR.dll` proxy**, original renamed `_bwv_original_vm.dll` | none (export forwarding + wrapping `Initialise`/`LookIn`) | captures the native function table passed to `Initialise` | SHA-256 of the exe and the VM, or a canonical-PE-layout hash |
| bw1-devmenus / bw1-patches / fixes | static exe patch | code cave + `e9` jmp | Ghidra/IDA + bw1-decomp names | SHA-256 plus expected bytes at each site |

**For BW2:** candidate proxy DLLs are the D3D9 family (BW2 is DX9, so `d3d9.dll` or `d3dx9`), `binkw32.dll`, `dbghelp.dll` (shipped next to the exe), `version.dll` (the SafeDiscLoader2 approach) and `PlugIns/ScriptLibraryR.dll`. None of these has been tested for BW2 in these repos.

### 2.6 Type recovery (sp00nznet)
- RTTI vftable walk: the type descriptor gives the `??_7Class@@6B@` vftable. Slot order equals virtual declaration order, so match slots to known headers. Stop at the next vftable. IDA idalib is the primary tool, with Ghidra as a cross-check once the RTTI analyzer is on.
- Find non-virtual methods through xrefs from the virtual bodies.
- Class families share a vtable shape. Slots with constant returns or self-registering editor strings give a schema mechanically (210 particle parameters were named this way).
- Force analysis (`mkfunc`) over unparsed ranges before you conclude that "nothing writes this". Watch for spurious functions created at padding.
- Emulate `.bss` table initialisers to recover runtime-filled tables.
- Validate a recovered layout by a closed sum: the reader byte sizes must equal the header size field and the walk must end on the last byte of the file.
- pcrecomp (via BW2C/bw2/README.md) has `tools/cpp/rtti.py` and `tools/cpp/vtable_scan.py`. The vtable scanner found 100% of findable vtables on Trespasser. These tools are not downloaded.

### 2.7 File-format knowledge that applies directly to BW2
- `.stuff` (bw2-unstuff, bw2-tools, stuff.md). Fully known.
- `.bwm` v5/v6 (bwm.md, bwm-blender, bwmtool, BWMViewer). Readable and writable. Bones and skins are partly known.
- `.al` animation libraries (al.md). Headers known, keyframe encoding partly known.
- `.chl` LHVM v12 (chl.md, bw2-tools). The structure is known. For opcodes see Part 3.
- `.bwe` text commands (bwe.md). The command list is known. Use openblack LHScriptX as a parser model.
- Encrypted shaders: Caesar minus 3 (shaders/).
- `Unlock.dat`: ProductId XOR the bytes of 1234567890 (BW2TigerUnlocker).
- The `LiOnHeAd` Pack container (openblack `pack`) and the `LiOnHeAdInfo` header (sp00nznet info.dat) are Lionhead library formats. BW2 is likely to use them too (BWM's `LiOnHeAdMODEL` fits the pattern). Unverified for each BW2 file.
- Not covered anywhere: `.lug` (audio), `.lhts` and `bw2text.bin` (text), `.lnd` for BW2, BW2 `info.dat`, BW2 save games.

---

## Part 3: Specific BW2 facts found

### Binaries and install (BW2C/bw2/README.md, analysis/)
- `white.exe` is 21,739,061 bytes. InstallShield 9, 4 discs, file index 893.
- `Black & White 2_code.exe` launcher: 323,584 bytes, SHA1 `f953b7d30a97cc1eb20d2bf808e49ed89a04ea6c`, PE32 linker 7.10, built 2005-08-19 17:48:32 UTC. `.text` is 134,068 bytes. Not packed.
- `DrvMgt.dll`: 41,472 bytes, SHA1 `7c5ab9bdf965b70e60b99086519327168f43f362`, linker 6.0, SafeDisc driver manager.
- `binkw32.dll`: 339,456 bytes, 88 exports.
- Other shipped files: `PlugIns/ScriptLibraryR.dll` (679,936 bytes), `dbghelp.dll` (894,464), `Data/Everything.stuff` (about 1.43 GB), `Scripts\BW2/*.chl`, `bw2text.bin`, `Speech.lug`, `spooky_names.lug`, `Audio/**.lug`, `Splash.tga`, `EAlogocompressed.bik`.
- Compiler: **MSVC 7.1 (VS.NET 2003)**, from the launcher's linker version. white.exe itself has not been checked.
- BOTG exe name: `BattleOfTheGods.exe` (linux-blackandwhite).
- Patch chain: v1.1, v1.2, unofficial v1.42. BOTG fan patch v1.1.
- Wine notes: needs `d3dx9`, works with DXVK, needs a `binkw32` native override.

### LHVM / CHL (bw2-modding chl.md + opcodes.md, bw2-tools ChallengeFile.cs, openblack LHVMTypes.h)
- Header `"LHVM"`, then u32 **version 12** (BW1 = 7, CI = 8).
- Sections in order:
  1. Globals: count, then NUL-terminated names.
  2. Instructions: count × 20 bytes `{u32 type; u32 subtype(mode); u32 datatype; 4-byte param (int/float); u32 line}`.
  3. Autostart: count × u32 script id.
  4. Scripts: count × `{name; sourcefile; u32 type (1, or 2 for StandardBanter/StandardReminder); u32 var_offset; u32 nLocals + names; u32 instrAddr; u32 nParams; u32 scriptId}`.
  5. Data: u32 length + bytes.
  6. **4096 NUL bytes.**
  7. u32 0.
  8. Saved globals: count × `{u32 unk; 4-byte value; name}`.
- Data types: 1 int, 2 float, 3 vector (v3d stack), 4 object, 6 bool, 7 variable reference.
- Opcode types (bw2-modding names, with bw2-tools names in brackets):
  - 0 END
  - 1 conditional jump [jmpc]. Subtype 1 jumps back, 2 jumps forward.
  - 2 PUSH
  - 3 POP
  - 4 ADD
  - 5 CALL native [engcall]
  - 6 MINUS, 7 UMINUS, 8 TIMES, 9 DIVIDE, 10 POWER
  - 11 BITWISEAND [and], 12 MODULUS, 13 NOT, 14 AND, 15 OR
  - 16 EQ, 17 NEQ, 18 GEQ, 19 LEQ, 20 GT, 21 LT
  - 22 JUMP
  - 23 SLEEP [wait]
  - 24 EXCEPT [regexhnd]
  - 25 cast (int, float, coord, thing, bool) [v3dpush]
  - 26 RUN script [call]
  - 27 ENDEXCEPT [rcl/sav stack]
  - 28 RETEXCEPT, 29 FAILEXCEPT [except], 30 BRKEXCEPT
  - 31 SWAP, 32 STACK_COPY, 33 LINE
  - 34 REF_AND_OFFSET_PUSH [target], 35 REF_AND_OFFSET_POP [store], 36 REF_PUSH [load], 37 REF_ADD_PUSH
  - 38 TAN, 39 SIN, 40 COS, 41 ARCTAN, 42 ARCSIN, 43 ARCCOS, 44 ARCTAN2, 45 SQRT, 46 ABS
  - 47 FORCE_JUMP
- BW1's openblack VM has 31 opcodes. BW2 adds the REF_* group and others. The semantics of 34 to 37 "need more research".
- Variable addressing is BW1-confirmed in sp00nznet `docs/chl-format.md`: an id ≤ var_offset is `global[id-1]`, otherwise `local[id-var_offset-1]`. This probably carries over to BW2 (unverified).

### Native functions (bw2-modding lhvm/native_functions.md)
- BW2 has 1106 natives and BOTG 1160. Columns: ID, name, Pointer, stackIn, stackOut, unknown.
- BW2 examples: 1 SET_CREATURE_ICON_VISIBLE, 2 SET_PAUSE_IN_WIDESCREEN, 3 SET_PRECIPITATION (7 in, 0 out), 4 GET_MICRO_CLIMATE, 6 IS_CINEMA_SKIPPED, 8 PLAY_MIDI_FILE, 11 GET_DESIRED_BUILDING.
- BOTG examples: 1 SET_GESTURES_ENABLED, 2 START_ABODE_SEARCH. BOTG renumbers, so IDs are **not** stable between BW2 and BOTG.
- BW2 "Pointer" values range from 0xC9AB88 to 0xCD0C2E. Their meaning and exe version are not stated.
- BW1 form of the native table entry (BWCI_debugger): `{fnptr, stackIn, stackOut, unknown, char name[128]}`, passed to `ScriptLibraryR!Initialise`. BW2 probably uses the same layout (unverified).

### ScriptLibraryR.dll exports (BW1 list from batch_and_vibe native/exports.txt; BW2 assumed similar, unverified)
- `Initialise, LookIn, LoadBinary, RestoreState, Version, UnInitialise, Reboot, NumTasks, TaskNumber, TaskName, LoopGlobalVariables, LoopTaskVariables, GetGlobalVariableValue, GetLocalVariableValue, FindScript, StartScript, AutoStart, CodeSize, OpCode, OpCodeName, Mode, LineNumber, GetScriptID, GetScriptType, GetTaskFilename, ...`
- C++ exports: `??0CSCRIPT_LIBRARY@@QAE@XZ`, `?fnSCRIPT_LIBRARY@@YAHXZ`, `?nSCRIPT_LIBRARY@@3HA`.

### Enums and constants
- 96 original headers in `bw2-modding/lhvm/enums/`. Examples: `SCRIPT_FIND_TYPE_ANY = 5000`. `SCRIPT_SPIRIT_TYPE {NONE, GOOD, EVIL, ALIGNMENT, ANTI_ALIGNMENT, RANDOM, LAST}`. `SCRIPT_OBJECT_PROPERTY_TYPE {NONE, HEALTH, ANGLE, XANGLE, ZANGLE, FLYING, DROWNING, MOVING, SCALE, IN_HAND, ...}`. `CREATURE_PEN_INFO {FEED, TRAIN, SLEEP, DISCIPLINE, PLEASURE, LAST}`. `const ULONG SUB_TYPE_ANY = LH_MAX_ULONG`.
- These headers include `<LHLib/LHTypes.h>`, so BW2 still uses Lionhead's LHLib, the same library family as BW1's `src/Lionhead/LHLib`.
- `ChallengeHeaders.txt` maps script constants to enums (for example `GREEK_HOUSEWIFE = VILLAGER_INFO_GREEK_HOUSEWIFE_FEMALE`).

### File formats (Part 2.7 gives the summary)
- `.stuff` directory entry is 268 bytes. BWM magic is `LiOnHeAdMODEL` + `0x2B00B1E5`, versions 5 and 6. AL magic is `0xFEC`, animation version 9. Shaders are Caesar minus 3. `Unlock.dat` is ProductId XOR the bytes of 1234567890.
- `.bwe` commands include `SET_TOWN_NAME`, `CREATE_VILLAGER_POS`, `CREATE_ORE_LODE`, `LOAD_LANDSCAPE`, `LOAD_GAME_SCRIPT`, `CREATE_CREATURE`, `CREATE_TOWN`, `CREATE_ABODE`, `CREATE_PLANNED_EPIC_WONDER`, `CHEAT_SET_CREATURE`, and about 160 more (see bwe.md).
- BW2 anim event names: `LHAnimEvent0N` bones, `::FOOTSTEPS.99` events.

### Addresses
- No white.exe function addresses are documented in any of these repos, apart from the unexplained "Pointer" column.

---

## Part 4: Gaps (what none of these cover for BW2)
1. **white.exe itself:** no symbols, splits, function list, RTTI or vtable census, import list or section map. sp00nznet had not even extracted it. Nobody knows if it keeps RTTI or assert strings.
2. **Copy protection:** whether white.exe is SafeDisc-wrapped (and which version), and how to get a clean, decrypted, "pristine linker shape" exe. The fan patch 1.42 exe may already be unwrapped (unverified). SafeDiscLoader2 (in BW1C/bw/vendor) claims SafeDisc 2.0 to 4.9 support at runtime but does not unwrap the file.
3. **Compiler toolchain:** no MSVC 7.1 (VS.NET 2003) build setup, CRT and STL libs, or service-pack identification (Rich header) for BW2. No DirectX 9 SDK version identified. None of the MSVC6 c2.dll inliner and scheduler research transfers directly.
4. **Mac symbols for BW2:** none of the repos look for a Mac BW2 build with symbols (the BW1 project's main name source). No PDB or symbol leak is mentioned.
5. **Version matrix:** no hashes for the BW2 1.0, 1.1, 1.2 or 1.42 white.exe, or for BOTG's exe.
6. **DLL inventory:** no list of BW2's own DLLs beyond `PlugIns/ScriptLibraryR.dll`, `binkw32.dll`, `DrvMgt.dll` and `dbghelp.dll`. It is unclear whether BW2 has LHAudio, LHMultiplayer or LHLog DLLs like BW1, or links them statically.
7. **LHVM v12 runtime:** no recovered BW2 interpreter. Opcodes 34 to 37 and several subtypes are unverified. No BW2 CHL compiler or decompiler exists (chlasm covers v7 and v8 only, and openblack refuses anything but v7). The "Pointer" column of the native table is unexplained.
8. **Formats not covered:** `.lug` audio banks, `.lhts` and `bw2text.bin` text, BW2 `.lnd` landscape, BW2 `info.dat` / balance data, `.bik` (standard Bink), save games and profiles, creature mind files, BW2 particle and effects files, the full `.al` keyframe encoding, BWM bone and skin details and the "unknown" header fields.
9. **Hooking BW2:** no BW2 mod loader, proxy DLL, hook addresses or engine globals (camera, render, hand). No D3D9 proxy has been tried.
10. **Networking:** nothing on BW2 or BOTG online services (GameSpy or other).
11. **Agent tooling at this checkout:** the bw1-decomp tools named in AGENTS.md (`cwdemangle.py`, `mac_symbol_version.py`, `idioms.py`, `decomp-similar.py`, `decomp-queue.py`, `docs/msvc6_idioms.md`) are missing. We would have to write our own.
12. **Licensing:** bw2-modding, Black-White-2-Redux, bw1-devmenus and Black-and-White-2-Install have no license file in the downloaded tree. bw1-patches has MIT only in tool/Cargo.toml. Check before you copy code or text from them.
