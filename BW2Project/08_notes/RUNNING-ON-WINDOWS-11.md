# Getting BW2 running on Windows 11 (community tips)

Collected from the 2023 community install guide (personal details removed) and bwgame.net.

1. Mount discs with WinCDEmu, NOT Windows' built-in mounting. Reported reason: the built-in mount makes the installer skip Land 4, and the game crashes after Land 3.
2. Install, then patches in order: v1.1 -> v1.2 -> Fan Patch v1.42. Run each as administrator.
3. Don't use any replacement white.exe. The game has anti-piracy measures that deliberately break things (villagers stop aging) when it detects a modified exe. Fan patch v1.42 is reported to be all you need.
4. "Register with EA" prompt: choose Register Later.
5. Overlapping audio: cap FPS at 60 (NVIDIA Control Panel > Manage 3D settings > Program Settings > white.exe > Max Frame Rate = 60).
6. Mouse won't move properly: lower mouse polling rate to 125 Hz in your mouse software.
7. Crash at the end of a level: fan patch v1.42 adds a skip option on Esc during the land-to-land transition.
More: https://forum.bwgame.net/threads/running-black-white-2-on-windows-10.7917/ (page 6)
