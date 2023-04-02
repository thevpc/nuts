package net.thevpc.nuts.toolbox.nsh.cmds.posix.grep;

import net.thevpc.nuts.toolbox.nsh.cmds.util.WindowFilter;

public class TrueObjectWindowFilter<T> implements WindowFilter<T> {
    @Override
    public boolean accept(T line) {
        return true;
    }

    @Override
    public WindowFilter<T> copy() {
        return this;
    }
}
