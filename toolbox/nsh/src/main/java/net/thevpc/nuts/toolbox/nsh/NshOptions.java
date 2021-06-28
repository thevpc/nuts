package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellOptions;

public class NshOptions extends JShellOptions {

    public boolean isNsh() {
        return !bash && !posix;
    }
}
