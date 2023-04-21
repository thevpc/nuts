package net.thevpc.nuts.toolbox.nsh.cmds.posix.grep;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NText;

public class GrepResultItem {
    NPath path;
    long number;
    NText line;
    Boolean match;

    public GrepResultItem(NPath path, long number, NText line, Boolean match) {
        this.path = path;
        this.number = number;
        this.line = line;
        this.match = match;
    }
}
