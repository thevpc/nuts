package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.jshell.JShellOptions;

public class NshOptions extends JShellOptions {

    public boolean isNsh() {
        return !bash && !posix;
    }
}
