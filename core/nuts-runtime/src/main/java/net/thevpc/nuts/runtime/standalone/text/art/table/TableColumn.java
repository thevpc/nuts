package net.thevpc.nuts.runtime.standalone.text.art.table;

import java.io.PrintStream;

class TableColumn {
    int index = 0;
    int charWidth = 0;

    public void dump(PrintStream out, String prefix) {
        out.println(prefix + "index  = " + index);
        out.println(prefix + "    charWidth  = " + charWidth);
    }
}
