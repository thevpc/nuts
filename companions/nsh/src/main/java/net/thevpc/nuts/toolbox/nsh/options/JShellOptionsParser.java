package net.thevpc.nuts.toolbox.nsh.options;

import net.thevpc.nuts.toolbox.nsh.jshell.JShellOptions;

public interface JShellOptionsParser {
    JShellOptions parse(String[] args);
}
