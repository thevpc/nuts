package net.thevpc.nuts.runtime.standalone.text.art.table;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class TableRow {
    List<DefaultCell> cells = new ArrayList<>();
    int index;
    int charHeight;

    public void dump(PrintStream out, String prefix) {
        out.println(prefix + "index  = " + index);
        out.println(prefix + "  charHeight  = " + charHeight);
    }
}
