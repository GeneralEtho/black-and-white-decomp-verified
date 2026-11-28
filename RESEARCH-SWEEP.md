# Research sweep (2026-10-22)

Four parallel sweeps: community sites, code hosts, mod/download sites, and a read-through of every downloaded repo (that one lives in REPO-DIGEST.md).
Sections get filled as each sweep finishes.

## 2. Code hosts (GitHub code search, GitLab/Codeberg/SourceForge, decomp directories, talks)

### Coverage notes
- GitHub code search: run from the owner's PC (authenticated) for: "Everything.stuff", "BattleOfTheGods.exe", "white.exe" lionhead, GScript, LHVM, HandMachine, bw2text.bin, LHFilePack, BW2 hook dll, CreatureLearningMatrix. Hit the rate limit on the last query. New hits: only Mod Organizer 2's BW2 plugin and a Wine probe script (vgrichina/berrry-wine).
- GitLab, Codeberg, Bitbucket: APIs blocked from the cloud side; web searches found nothing BW/Lionhead.
- SourceForge: nothing BW2.
- decomp.dev SHUT DOWN 2026-10-16 (AI-driven bad actors). decomp.me still up. No BW2 entry anywhere.
- No public Ghidra/IDA databases or signature files for BW2 or The Movies. No credible leaked Lionhead source.

### Top new finds (downloaded unless noted)
1. openblack/decomp-toolkit, branch coff_prototype: PE/COFF splitting + relinking for MSVC binaries (the matching-decomp toolchain behind bw1-decomp). Needs MSVC 7.1 support for BW2. -> 07_shared-reference/decomp-toolkit-coff_prototype
2. openblack/llvm-project, branch bw1-decomp: lld-link fork reproducing VC6 link.exe output byte-for-byte. BW2 would need the VS.NET 2003 equivalent. (Huge; link only: https://github.com/openblack/llvm-project)
3. FoxToolworks/BW2StuffExplorer: C#/.NET 8 Everything.stuff explorer, v0.7.0 (2026-09). docs/FORMAT.md + docs/research: BWM material records (448 bytes, 6 texture slots, 820 retail models), creature files (CSK/CHA/CCS), .555 image format, all 13 stuff extensions; offset 264 in a stuff entry = Unix timestamp. -> BW2Project/07_reference/community/BW2StuffExplorer
4. openblack/bw2-modding is much bigger than its README suggests: 812 ORIGINAL BW2 script sources, 96 ORIGINAL enum headers (CreatureDesiresEnum.h, GameBalanceBuilding.h, GSubStates.h, CreatureLearningMatrixEnums.h...), opcodes.md, native_functions.md (updated by Daniels118 2023), Lionhead's Script Compiler.exe + ScriptLibraryR.dll (shares LHVM code with white.exe: great RE starting point), shader decryptor (Caesar +3) + decrypted shaders. Already downloaded.
5. HandsomeMatt originals of bw2-tools/modding/unstuff (earlier history); bw2-scripts, bw2-bwmexport, bw1-modding now gone/private (scripts appear folded into bw2-modding).
6. bwgame.net "Decompiling Challenge Scripts" thread (2007-2014): BW2 CHL layout notes, unresolved opcodes 30/32/47 + arrays. https://www.bwgame.net/threads/decompiling-challenge-scripts.2172/page-4
7. bwgame.net "BW2 General Modding Discussion": BoTG buildings hard-coded in exe; enabling BoTG spells in BW2 by binary edit crashes; bw2text.bin is UTF-16; Matt's C# BWM reader (magic 721465829, versions 5-6, 48-byte vertex). https://www.bwgame.net/threads/bw2-general-modding-discussion-help.8050
8. Official BW2 modding kit: LionheadUnstuffer, Landscape Editor, Script Compiler, Script Language docs. First-party binaries from the same codebase: cross-reference functions/RTTI with white.exe. (We have BW2ModTools.exe.)
9. PBnW Modding Wiki archive: BW2 pages for BWE, TER, WAL, PAT, _walk.xml, CHL, BWM, .met/.fnt/.jty, balancing constants, BW2 Script API Specification. https://archive.bwgame.net/ModdingWiki/wiki.planetblackandwhite.gamespy.com/PBnWModdingWIKI/index.php/Main_Page.html
10. sp00nznet/pcrecomp: tools/drm (SafeDisc memory dump + emulated unpacking via Unicorn), tools/cpp (RTTI + vtable recovery), Ghidra/IDA headless scripts, win32hle. -> 07_shared-reference/pcrecomp

### Other notes
- sp00nznet/themovies: The Movies (Lionhead 2005) keeps RTTI: 1,312 classes in TM, 204 in MV (engine), LSWF (Lionhead Flash player). Engine classes may overlap BW2.
- sp00nznet/bw: ~430 of 464 BW1 LHVM natives implemented; GhidraDumpClass.java RTTI walker.
- openblack/openblack: apps/lhvmtool, components/ScriptLibrary/LHVM*.cpp, LHVMViewer (BW1 but LHVM is shared).
- bw1-decomp docs worth reading: msvc6_inliner.md, msvc6_scheduler.md, msvc6_comdat.md, villager_states.md.
- TCRF page: unused miracles (Stop Time, Verdant, Doomsday), cut lands (Norse 5, Aztec 10), unused multiplayer code, 4th creature tier. https://tcrf.net/Black_%26_White_2
- Proton issue #8023 confirms retail SafeDisc wrapping, image base 0x400000. https://github.com/ValveSoftware/Proton/issues/8023
- No BW2 postmortem/GDC talk found. Programmers per Wikipedia: David Bryson, Daniel Deptford, Tid Cooney. GDCPhysicsDemo.txt script hints at a GDC physics demo.
- Richard Evans (BW1 AI lead) writings: conceptual background for creature AI.
- Mod Organizer 2 supports BW2: games/game_blackandwhite2.py (saved to 07_reference/community).

## 3. Mod and download sites (archive sites, ModDB, Nexus, PCGamingWiki, archive.org, gamepressure)

### Coverage notes
- Nexus and PCGamingWiki community file pages were blocked from the cloud side; Wayback snapshots of bwgame.com/lionhead.com also blocked. ModDB readable but links stripped.

### New facts
- Script engine probably lives in PlugIns\ScriptLibraryR.dll (also shipped with Lionhead's Script Compiler). Script native function counts: BW1 464, CI 528, BW2 1106, BotG 1160 (full tables exist only for BW1/CI).
- bw2-modding lhvm: 48 opcodes; ~120 enum headers; original scripts incl. dev/test ones (Land2StressTest, ATIDemo, GDCPhysicsDemo, ArmyDemo, AdamTest, JontyTest, GodsPlaygroundChallenges, IsleMap).
- .stuff format: no header; directory at end; last 4 bytes = directory offset; 268-byte entries (256 name + offset + size + timestamp). ~3,928 files.
- Script compiler command line: "Script Compiler" -path . -enumfile .\Enums.txt -dbfile .\bw2text.lhts -scriptpath . -inputfile ./x/Challenges.txt out.chl
- system.ini <uma>/<memory> sections tune texture/LOD/mip limits. Graphics.config = GPU whitelist driving "ultra low" auto-detect.
- Settings: %USERPROFILE%\Documents\Black & White 2\Settings.ini (Adapter, ScreenW, ScreenH). Saves: ...\Profiles\. Skip intros: empty Data\Splash.
- No documented command-line switches for BW2 (BW1/CI have /console etc.): worth testing.
- Widescreen supported but HUD stretched; 4K UI doesn't scale (tiny). No widescreen fixer exists (no ThirteenAG/Flawless Widescreen support). FOV not adjustable. This confirms UI scaling as genuinely unsolved: a real contribution if we fix it.
- Community fixes: single-core CPU affinity (terrain glitches/crashes), 60 FPS cap (audio), 125 Hz mouse polling, Win98/ME compat mode (disappearing terrain), borderless window apps / D3DWindower.
- Cut content (TCRF): unused lands 5 (Norse) and 10 (Aztec), epics Stop Time / epic Verdant / Doomsday, miracles Decay/Wind/Rain, 9 creature miracles planned (4 shipped), 4th creature role tier, multiplayer code. BW2 Plus restores lands 5 and 10.
- BotG buildings are hard-coded in BotG's exe; enabling BotG spells in BW2 via balance edits crashes (script workaround exists).

### Worth downloading (bwgame.net downloads usually need a free login: owner to fetch)
| Item | Why | URL |
| --- | --- | --- |
| Black & White 2 Fixer | Unofficial fix on PCGamingWiki, possibly resolution/widescreen. Unverified | https://community.pcgamingwiki.com/files/file/3983-black-white-2-fixer/ |
| BW2 v1.1 trainers v0.9.92/0.9.93 (Zhoul) | v1.1 memory addresses: diff builds | https://www.bwgame.net/downloads/bw2-v1-1-trainer-v0-9-93.580/ |
| EMMT_BW2 Mod Tools v0.1 (ego533) | Python creature-mesh tools, shader decrypter, structure notes | https://www.bwgame.net/downloads/emmt_bw2-mod-tools-v0-1.1380/ |
| 00din's B&W2 Script Tool | Script IDE with enum lookup | https://www.bwgame.net/downloads/00dins-b-w2-script-tool-beta.975/ |
| BW2LHTS Editor | Text database (bw2text.lhts) viewer | https://www.bwgame.net/downloads/bw2lhts-editor.1107/ |
| God Engine 0.9b (source included) | AI-god script library | https://www.bwgame.net/downloads/god-engine-beta0-9b.1333/ |
| Army Engine (Egerion) | Platoon-control script library | https://www.bwgame.net/downloads/army-engine.1445/ |
| Secret Island | Loads an unused shipped landscape | https://www.bwgame.net/downloads/secret-island.572/ |
| Xfire developer chat (Sept 2005) | Lionhead devs answering questions | https://www.bwgame.net/downloads/xfire-developers-chat-sept-30-2005-formatted-version.529/ |
| BW2 Syntax Highlighter (Notepad++) | Script editing | https://www.bwgame.net/downloads/bw2-syntax-highlighter-notepad.1417/ |
| OGaM open-source development files | On ModDB BW2 downloads list | https://www.moddb.com/games/black-white-2/downloads |
| Helix Mod (3D Vision d3d9 wrapper) | Shows a working d3d9 wrapper/injection on BW2 | https://helixmod.blogspot.com/2018/02/black-white-2.html |
Already in hand: the Windows demo installer (another build to compare).

### Other
- Kays mapping tutorial: https://www.moddb.com/tutorials/black-white-2-mapping-tutorialby-kays
- Community Discord: https://discord.gg/EtmRWXCWf3
- Guides: Prima eGuide (archive.org), Gamepressure + Prima guides on archive sites.
- BW2 4K HD texture pack (~6 GB, Nexus): https://www.dsogaming.com/mods/black-white-2-gets-a-4k-hd-texture-pack/
- More faction/balance mods: https://www.bwgame.net/downloads/categories/bw2-mods.59/

## 1. Community sites (bwgame.net all BW2 modding + tech help pages, downloads tree, PBnW wiki archive, bwrealm)
forum.bwgame.net and bwfiles.com now redirect to www.bwgame.net. bwrealm.com is a JS site with nothing readable. Wayback blocked from cloud side.

### Formats and internals
- BWM: Matt's unfinished C# reader in thread 8050 (header magic/version 5-6, materials, meshes, bones, entities, strides, vertices, indices, cleave points). Original models came from a 3ds Max exporter.
- LUG audio banks (ego533, thread 6475 #2): sections LiOnHeAdLHAudioBankMetaData, LHFileSegmentBankInfo (520 B), LHAudioWaveData (raw RIFF PCM), LHAudioBankSampleTable (256-B paths, offsets, loops), CriteiaInfo, GlobalProps, RandomModulator. Sound enum IDs in thread 1563. Dragon UnPACKer extracts; no repacker.
- Creature files (ego533): .CSK base mesh + named morphs (evil/good/fat/thin) as bit-flagged vertex deltas; .CHA fur ribbons; .CBN attributes + LUG refs; .CCS bounding boxes + bone groups.
- "LiOnHeAd..." magic family = one shared Lionhead container library: .wal LiOnHeAdWallVersion, .pat LiOnHeAdPathVersion, info.cdat LiOnHeAdInfoAmmendments (compiled Scripts/info.txt; exe also makes editinfo.dat), LUG.
- Text DB (LHTS / bw2text.bin, UTF-16): header "LHTS 01 02 00 EN-UK"; entry = 2-byte ID, constant name, speaker, 00 FF, text. Nobody can ADD strings (custom quests show "Default"; edits crashed). A rebuild tool is an open gap.
- graphics.config (Data\): lines "<card substring> RES MINSPEC DETAIL_LEVEL", matched top-down against the DirectX device name. This is the "stuck on low" hardware detection; fan patch 1.42 still fails on some GPUs. Fix the parser in white.exe properly.
- Savegame header: 18 01 00 00, UTF-16 name (4-259), FILETIME ~264, play time 276.
- Visual effects: .ves/.vep/.vet. Balance column names for every GameBalance*.txt: thread 8232. Engine reads balance files line by line.
- 164 BWE/text commands extracted from the binary incl. debug verbs: SET_STARTUP, KEYCONFIG, CONFIG, OUTPUT_RAND/ALLOC/COLLIDE/CREATURES/TOWN/VILLAGERS, PAUSE_GAME, MULTIPLAYER_DEBUG, LOAD/SAVE_FOR_NET_DEBUG, CHEAT_SET_CREATURE/ALIGNMENT/UNLOCK_ITEM/TRIBUTE, EDIT_LEVEL, VERSION, LOAD_RAW_GAME_SCRIPT, FLY_BY_FILE. Scripts/map.txt uses the same dispatcher: the place to find debug menus/dev flags.
- Loose files override Everything.stuff (no repacking ever needed for mods). Shaders: Caesar +3.
- CHL: 2007 thread lists 100+ opcodes in a dotted category.subcategory.function scheme (may mirror native-table grouping). BotG shifted native IDs and added ~100 natives. Compiler quirks: string literals crash it unless bw2text.bin is beside it.

### Known engine bugs (exe work needed): candidate fixes
- Land IDs above 11 break "load my_creature" and villager counter.
- More than 3 player creatures glitch/crash. Script-created creature starts with a zeroed learning matrix.
- Mouse polling >125 Hz makes the hand stutter (input loop).
- HUD/advisors stretch on widescreen; UI tiny at 4K.
- Black squares around torches at night; AMD driver drops terrain tiles.
- Fan patch 1.42 bugs: crash on save after mermaid use, see-through terrain, flicker, black torches.

### Tools / downloads (most need bwgame.net login)
- Zhoul's trainers v1.2.1 (in hand), v1.2.0, v1.1 (0.9.93): VB6. Decompiling them likely recovers v1.2 memory addresses.
- Wally's Destuff It, DDS converter (in hand), 00din's Script Tool, Notepad++ syntax, Kays' generic .chl, God Engine (source), Army Engine, EMMT_BW2 (+ v0.4 fix in thread post #3), BW2LHTS Editor, ego533's LUG analysis .DOC (thread 6475).
- Mod Organizer 2 has a BW2 plugin (thread 8405). Redux ships its own whitemod.exe; Tribal Pack Plus modified white.exe.
- BW2 Plus bundles a 4GB (LAA) patch: memory limits matter.
