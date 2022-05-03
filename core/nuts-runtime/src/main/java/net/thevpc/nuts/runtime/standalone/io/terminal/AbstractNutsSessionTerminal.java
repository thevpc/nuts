package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.io.NutsSessionTerminal;

public abstract class AbstractNutsSessionTerminal implements NutsSessionTerminal {
    @Override
    public String readLine(NutsPrintStream out, String prompt, Object... params) {
        return readLine(out,NutsMessage.ofCstyle(prompt,params));
    }

    @Override
    public char[] readPassword(NutsPrintStream out, String prompt, Object... params) {
        return readPassword(out,NutsMessage.ofCstyle(prompt,params));
    }

    @Override
    public NutsSessionTerminal printProgress(float progress, String prompt, Object... params) {
        printProgress(progress,NutsMessage.ofCstyle(prompt,params));
        return this;
    }

    @Override
    public NutsSessionTerminal printProgress(String prompt, Object... params) {
        printProgress(Float.NaN,prompt,params);
        return this;
    }

    @Override
    public NutsSessionTerminal printProgress(NutsMessage message) {
        printProgress(Float.NaN,message);
        return this;
    }

    public abstract NutsSession getSession();
}
