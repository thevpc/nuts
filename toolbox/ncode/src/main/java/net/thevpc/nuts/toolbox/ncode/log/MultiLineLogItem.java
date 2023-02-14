package net.thevpc.nuts.toolbox.ncode.log;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class MultiLineLogItem implements LogItem {
    private List<String> lines=new ArrayList<>();

    public MultiLineLogItem(List<String> lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        return String.join("\n",lines);
    }
}
