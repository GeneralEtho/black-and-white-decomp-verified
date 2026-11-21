// ReadBackNames.java (test only): writes every function's address, full name, name source and plate comment
// to a tab separated file, so compare_names.py can check what ApplyNames.java did. Argument: <output file>.
// Newlines and tabs inside a comment are written as \n and \t.
//@category BW2
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ReadBackNames extends GhidraScript {
    @Override
    public void run() throws Exception {
        String[] a = getScriptArgs();
        if (a.length < 1) { printerr("usage: ReadBackNames.java <output file>"); return; }
        int n = 0;
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(a[0]), StandardCharsets.UTF_8))) {
            w.print("address\tname\tsource\tplate\n");
            for (Function f : currentProgram.getFunctionManager().getFunctions(true)) {
                String c = currentProgram.getListing().getComment(CommentType.PLATE, f.getEntryPoint());
                c = c == null ? "" : c.replace("\\", "\\\\").replace("\t", "\\t").replace("\r", "").replace("\n", "\\n");
                w.print("0x" + Long.toHexString(f.getEntryPoint().getOffset()) + "\t" + f.getName(true) + "\t"
                        + f.getSymbol().getSource() + "\t" + c + "\n");
                n++;
            }
        }
        println("ReadBackNames: " + n + " functions written to " + a[0]);
    }
}
