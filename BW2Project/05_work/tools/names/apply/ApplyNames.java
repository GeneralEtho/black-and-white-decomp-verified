// ApplyNames.java: puts the names of a published named-functions.tsv into YOUR OWN Ghidra program.
// MIT licence. Reads the table and your program; contains no game bytes.
// Arguments: <path of named-functions.tsv> <dry|apply> [allow-suggested|allow-noise]
// Headless: analyzeHeadless <projectdir> <project> -process <program> -noanalysis -scriptPath <this folder>
//           -postScript ApplyNames.java <tsv> dry
// From the Script Manager with no arguments the script asks for the file and the mode.
// dry prints the counts and changes nothing. apply labels the functions and adds a plate comment.
// The tier is read from the status cells: claimed or folded = verified (applied by default),
// suggested or noise = refused unless you pass the matching allow word (then every label gets the
// prefix suggested_ or noise_), anything else (withheld, banded, mixed) is refused always.
// A function is never created. A function that already has a user given name is left alone.
// A function whose plate comment already holds a line of this table is ours: a row of an equal or
// lower tier leaves it alone, a row of a higher tier replaces the lower tier's label and comment. A row of
// a higher tier that only comments takes the lower tier's label away too (the function goes back to its
// default name) unless the user has renamed the function since; so the tiers may be applied in either order.
//@category BW2
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.*;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

public class ApplyNames extends GhidraScript {
    private static final String MARKER = "BW2 names table";   // written into every comment and read back from it
    private static final String EQUAL_OR_HIGHER = "already named by the names table at an equal or higher tier";
    private static final String[] TIERS = {"noise", "suggested", "verified"};   // rank 1, 2, 3
    private static final Pattern DEFAULT_NAME = Pattern.compile("(FUN_|thunk_FUN_|LAB_|DAT_|SUB_)[0-9A-Fa-f]+");
    private static final Pattern TIER_LINE = Pattern.compile("^(VERIFIED|SUGGESTED|NOISE) \\(" + Pattern.quote(MARKER) + "[,)]");
    private final Map<String, Integer> skipped = new TreeMap<>();
    private final List<String> lines = new ArrayList<>();
    private final Set<String> seen = new HashSet<>();
    private int labelled, folded, alreadyNamed, rowsReplaced, charsReplaced, upgraded;
    private int cA, cS, cD, cM, cT, rank;
    private String prefix, head;
    private boolean apply;

    @Override
    public void run() throws Exception {
        String[] a = getScriptArgs();
        File tsv; String mode; String allow = "";
        if (a.length == 0 && !isRunningHeadless()) {
            tsv = askFile("named-functions.tsv", "Use");
            mode = askChoice("Mode", "dry changes nothing", Arrays.asList("dry", "apply"), "dry");
        } else if (a.length >= 2) {
            tsv = new File(a[0]); mode = a[1]; allow = a.length > 2 ? a[2] : "";
        } else { refuse("usage: ApplyNames.java <named-functions.tsv> <dry|apply> [allow-suggested|allow-noise]"); return; }
        if (!mode.equals("dry") && !mode.equals("apply")) { refuse("mode must be dry or apply, got: " + mode); return; }
        apply = mode.equals("apply");
        List<String[]> rows = new ArrayList<>();
        if (!readTable(tsv, rows)) return;
        // Tier: from the first word of every status cell.
        Set<String> tiers = new TreeSet<>();
        for (String[] f : rows) tiers.add(tierOf(f[cT]));
        if (tiers.size() != 1) { refuse("the status cells are not one tier: " + tiers); return; }
        String tier = tiers.iterator().next();
        if (tier.equals("other")) { refuse("status is withheld, banded or unknown: this table is never applied"); return; }
        if (!tier.equals("verified") && !allow.equals("allow-" + tier)) {
            refuse("this table is the " + tier + " tier (names a method read as less than 95 of 100 right); to apply it on purpose add the word allow-" + tier);
            return;
        }
        prefix = tier.equals("verified") ? "" : tier + "_";
        head = tier.toUpperCase();
        rank = rankOf(tier);
        for (String[] f : rows) row(f);
        report(rows.size(), tier, tsv);
    }

    // Reads the file. A row with too few cells or no status refuses the whole file, naming the row and its address.
    private boolean readTable(File tsv, List<String[]> rows) throws IOException {
        Map<String, Integer> col = new HashMap<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(tsv), StandardCharsets.UTF_8))) {
            String h = r.readLine();
            if (h == null) { refuse("the table is empty"); return false; }
            if (h.startsWith("\uFEFF")) h = h.substring(1);   // a UTF-8 byte order mark before the header is skipped
            String[] hs = h.split("\t", -1);
            for (int i = 0; i < hs.length; i++) col.put(hs[i].trim(), i);
            for (String n : new String[]{"pc_address", "single_name", "mac_demangled", "mac_mangled", "status"})
                if (!col.containsKey(n)) { refuse("column missing in the header: " + n); return false; }
            cA = col.get("pc_address"); cS = col.get("single_name"); cD = col.get("mac_demangled");
            cM = col.get("mac_mangled"); cT = col.get("status");
            int need = Math.max(Math.max(cA, cS), Math.max(Math.max(cD, cM), cT)) + 1, line = 1, rowNo = 0;
            String l;
            while ((l = r.readLine()) != null) {
                line++;
                if (l.isEmpty()) continue;
                rowNo++;
                String[] f = l.split("\t", -1);
                if (f.length < need || f[cT].trim().isEmpty()) {
                    refuse("row " + rowNo + " (line " + line + " of the file, address " + (f.length > cA ? "'" + f[cA].trim() + "'" : "none") + ") "
                        + (f.length < need ? "has " + f.length + " cells, at least " + need + " are needed" : "has no status"));
                    return false;
                }
                rows.add(f);
            }
        }
        if (rows.isEmpty()) { refuse("the table has a header and no rows"); return false; }
        return true;
    }

    private void row(String[] f) {
        String addrText = f[cA].trim();
        Address ad = null;
        try { ad = currentProgram.getAddressFactory().getAddress(addrText); } catch (Exception e) { }
        if (ad == null) { skip("bad row", addrText); return; }
        if (!seen.add(ad.toString())) { skip("duplicate address", addrText); return; }   // the address as Ghidra parsed it, not its text
        if (!currentProgram.getMemory().contains(ad)) { skip("address outside the program", addrText); return; }
        Function fn = currentProgram.getFunctionManager().getFunctionAt(ad);
        if (fn == null) { skip("no function at the address", addrText); return; }
        String single = f[cS].trim(), status = f[cT].trim();
        boolean fold = single.isEmpty() || status.startsWith("folded");
        String mangled = f[cM].trim(), dem = f[cD].trim();
        Listing listing = currentProgram.getListing();
        String old = listing.getComment(CommentType.PLATE, ad);
        int have = tierRank(old);
        if (have >= rank) { skip(EQUAL_OR_HIGHER, addrText); return; }
        String lower = have > 0 ? lowerWant(old) : null;   // the label a lower tier of ours wrote, null if it only commented
        String note = head + " (" + MARKER + ", not a Ghidra analysis result)\nstatus: " + status;
        if (fold) {
            if (lower != null && !unlabel(fn, lower)) { skip("Ghidra refused the name", addrText); return; }
            note += "\nnames (one body, several Mac names, not labelled): " + (dem.isEmpty() ? mangled : dem) + "\nmac: " + mangled;
            folded++;
        } else {
            if (!label(fn, addrText, single, lower)) return;
            note += "\nname: " + single + "\nmac: " + mangled;
        }
        if (have > 0) { upgraded++; lines.add("UPGRADED\t" + addrText); }
        if (apply) {
            String base = have > 0 ? dropLower(old) : old;
            listing.setComment(ad, CommentType.PLATE, base == null || base.isEmpty() ? note : base + "\n\n" + note);
        }
    }

    // Labels one function. False when the row is skipped (the skip is counted here).
    private boolean label(Function fn, String addrText, String single, String lower) {
        int[] n = {0};
        String[] w = want(single, prefix, n);
        if (w == null) { skip("bad row", addrText); return false; }
        String want = String.join("::", w), now = fn.getName(true);
        SourceType src = fn.getSymbol().getSource();
        boolean ours = lower != null && (src != SourceType.USER_DEFINED || now.equals(lower));
        if (src == SourceType.USER_DEFINED && !now.equals(want) && !ours) { skip("already has a different user given name", addrText); return false; }
        if (now.equals(want)) alreadyNamed++;
        else if (refusal(w) || (apply && !setName(fn, w))) { skip("Ghidra refused the name", addrText); return false; }
        labelled++;
        if (n[0] > 0) { rowsReplaced++; charsReplaced += n[0]; lines.add("REPLACED\t" + addrText + "\t" + n[0]); }
        return true;
    }

    // A row that only comments, over a lower tier's label: the label goes back to Ghidra's default name in the global
    // namespace when it is still the one that tier wrote; a name the user gave since stays. False when Ghidra refuses.
    private boolean unlabel(Function fn, String lower) {
        if (!apply || !fn.getName(true).equals(lower)) return true;
        try { fn.getSymbol().setNamespace(currentProgram.getGlobalNamespace()); currentProgram.getSymbolTable().removeSymbolSpecial(fn.getSymbol()); return true; }
        catch (Exception e) { return false; }
    }

    // The name as it will be written, part by part; null for a bad name (nothing left after splitting at "::",
    // or one of Ghidra's own default forms). n[0] counts the characters replaced.
    private static String[] want(String single, String pre, int[] n) {
        String[] parts = split(single);
        if (parts.length == 0) return null;
        for (String p : parts) if (DEFAULT_NAME.matcher(p).matches()) return null;
        for (int i = 0; i < parts.length; i++) parts[i] = (i == parts.length - 1 ? pre : "") + clean(parts[i], n);
        return parts;
    }

    // Ghidra's own test of a name, used by dry and apply alike. True when Ghidra refuses it.
    private boolean refusal(String[] w) {
        for (String p : w) {
            try { SymbolUtilities.validateName(p); } catch (Exception e) { return true; }
        }
        return false;
    }

    // Sets the name and namespace in one step; a namespace made for a row that fails is deleted again.
    private boolean setName(Function fn, String[] w) {
        List<Namespace> made = new ArrayList<>();
        SymbolTable st = currentProgram.getSymbolTable();
        try {
            Namespace ns = currentProgram.getGlobalNamespace();
            for (int i = 0; i < w.length - 1; i++) {
                boolean isNew = st.getNamespace(w[i], ns) == null;
                ns = st.getOrCreateNameSpace(ns, w[i], SourceType.USER_DEFINED);
                if (isNew) made.add(ns);
            }
            fn.getSymbol().setNameAndNamespace(w[w.length - 1], ns, SourceType.USER_DEFINED);
            return true;
        } catch (Exception e) {
            for (int i = made.size() - 1; i >= 0; i--) made.get(i).getSymbol().delete();
            return false;
        }
    }

    private void skip(String why, String addr) { skipped.merge(why, 1, Integer::sum); lines.add("SKIP\t" + addr + "\t" + why); }

    private void refuse(String why) {
        println("REFUSED: " + why); println("Nothing was changed.");
        if (!isRunningHeadless()) popup("REFUSED: " + why);
    }

    private static String tierOf(String status) {
        String w = status.trim().split("[ ,]")[0];
        if (w.equals("claimed") || w.equals("folded")) return "verified";
        if (w.equals("suggested") || w.equals("noise")) return w;
        return "other";
    }

    private static int rankOf(String tier) { return Arrays.asList(TIERS).indexOf(tier) + 1; }

    // The highest tier of the table's lines in a plate comment; 0 when it holds none.
    private static int tierRank(String comment) {
        int best = 0;
        if (comment != null) for (String l : comment.split("\n")) {
            Matcher m = TIER_LINE.matcher(l);
            if (m.find()) best = Math.max(best, rankOf(m.group(1).toLowerCase()));
        }
        return best;
    }

    private static boolean isBlockLine(String l) {
        return l.startsWith("status: ") || l.startsWith("name: ") || l.startsWith("names (") || l.startsWith("mac: ");
    }

    // The comment without the table's blocks of a lower tier (a block is its tier line and the lines under it).
    private String dropLower(String comment) {
        List<String> out = new ArrayList<>();
        String[] ls = comment.split("\n", -1);
        for (int i = 0; i < ls.length; i++) {
            Matcher m = TIER_LINE.matcher(ls[i]);
            if (m.find() && rankOf(m.group(1).toLowerCase()) < rank) { while (i + 1 < ls.length && isBlockLine(ls[i + 1])) i++; continue; }
            out.add(ls[i]);
        }
        return String.join("\n", out).replaceAll("\n{3,}", "\n\n").trim();
    }

    // The label a lower tier's block of ours wrote, as it would have written it; null when no block labelled.
    private static String lowerWant(String comment) {
        String[] ls = comment.split("\n", -1);
        for (int i = 0; i < ls.length; i++) {
            Matcher m = TIER_LINE.matcher(ls[i]);
            if (!m.find()) continue;
            String t = m.group(1).toLowerCase();
            for (int j = i + 1; j < ls.length && isBlockLine(ls[j]); j++) {
                if (!ls[j].startsWith("name: ")) continue;
                String[] w = want(ls[j].substring(6), t.equals("verified") ? "" : t + "_", new int[1]);
                if (w != null) return String.join("::", w);
            }
        }
        return null;
    }

    // Split a name at "::" outside angle brackets; empty parts are dropped.
    private static String[] split(String s) {
        List<String> out = new ArrayList<>(); int depth = 0, st = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '<') depth++; else if (c == '>') depth = Math.max(0, depth - 1);
            else if (depth == 0 && c == ':' && i + 1 < s.length() && s.charAt(i + 1) == ':') { out.add(s.substring(st, i)); st = i + 2; i++; }
        }
        out.add(s.substring(st));
        out.removeIf(String::isEmpty);
        return out.toArray(new String[0]);
    }

    // A label may hold no space, control or non ASCII character: each one becomes an underscore.
    private static String clean(String s, int[] n) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c <= 0x20 || c >= 0x7f || SymbolUtilities.containsInvalidChars(String.valueOf(c))) { b.append('_'); n[0]++; }
            else b.append(c);
        }
        return b.toString();
    }

    private void report(int rows, String tier, File tsv) {
        int sk = 0; for (int v : skipped.values()) sk += v;
        println("ApplyNames " + (apply ? "APPLY" : "DRY RUN (nothing is changed)") + " on " + currentProgram.getName() + " with " + tsv.getName());
        println("tier: " + tier + (tier.equals("verified") ? "" : " (applied on purpose, labels carry the prefix " + tier + "_)"));
        println("rows in: " + rows);
        println("labelled: " + labelled + (apply ? "" : " (would be)") + ", of which already had the name: " + alreadyNamed);
        println("folded (comment only, no label): " + folded);
        println("skipped: " + sk);
        for (Map.Entry<String, Integer> e : skipped.entrySet()) println("  skipped, " + e.getKey() + ": " + e.getValue());
        println("replaced a lower tier's name: " + upgraded + " (counted in labelled or folded)");
        println("rows with a character replaced by an underscore: " + rowsReplaced + " (" + charsReplaced + " characters), labelled rows only");
        println("check: labelled + folded + skipped = " + (labelled + folded + sk) + (labelled + folded + sk == rows ? " equals rows in" : " DOES NOT EQUAL rows in"));
        for (String l : lines) println(l);
        println("RESULT: " + (apply ? "APPLIED" : "DRY") + " rows=" + rows + " labelled=" + labelled + " folded=" + folded + " skipped=" + sk);
    }
}
