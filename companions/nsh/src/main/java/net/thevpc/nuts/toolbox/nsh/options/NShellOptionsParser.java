package net.thevpc.nuts.toolbox.nsh.options;

import net.thevpc.nuts.toolbox.nsh.nshell.NShellOptions;

public interface NShellOptionsParser {
    NShellOptions parse(String[] args);
}
