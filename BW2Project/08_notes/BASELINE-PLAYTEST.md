# Baseline playtest (2026-10-22)

Owner played both games on the patched working copy. Verdict: both run "good enough". Nothing looks broken by our setup; these are the game's own known issues.

## BW2 base (v1.2 + fan patch v1.42)
- Graphical glitches; better at lower resolution.
- Hand won't move well vertically with the mouse. Touchpad works perfectly.
  Likely cause: high mouse polling rate (known community issue; fix = set mouse to 125 Hz in its software).
  Lowering resolution also helps, which hints the hand code handles mouse input per frame or per pixel badly.

## Battle of the Gods (BotG + its fan patch)
- Runs better and looks better than base. Mouse issue gone.
- Some pop-in; some text lines cut off.
- Every villager shows a floating label: name, health and current activity. Possibly a leftover debug/info display (maybe an options toggle). To investigate: it's a great clue that villager-info debug code exists in the exe.

## Fix wishlist (why we're doing the decomp)
1. Hand/mouse input in base BW2 (BotG apparently fixed it: comparing the two exes may show the fix directly).
2. Graphical glitches at high resolutions.
3. Pop-in / draw distance.
4. Cut-off text lines (UI scaling at modern resolutions).
5. UI scaling: the interface is tiny at 1440p. Needs a scale option.
6. Optional toggle for the villager info labels (keep as a feature!).
