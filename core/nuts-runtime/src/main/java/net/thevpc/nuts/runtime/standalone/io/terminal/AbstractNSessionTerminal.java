package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NSessionTerminal;

public abstract class AbstractNSessionTerminal implements NSessionTerminal {
    @Override
    public String readLine(NOutStream out, String prompt, Object... params) {
        return readLine(out, NMsg.ofCstyle(prompt,params));
    }

    @Override
    public char[] readPassword(NOutStream out, String prompt, Object... params) {
        return readPassword(out, NMsg.ofCstyle(prompt,params));
    }

    @Override
    public NSessionTerminal printProgress(float progress, String prompt, Object... params) {
        printProgress(progress, NMsg.ofCstyle(prompt,params));
        return this;
    }

    @Override
    public NSessionTerminal printProgress(String prompt, Object... params) {
        printProgress(Float.NaN,prompt,params);
        return this;
    }

    @Override
    public NSessionTerminal printProgress(NMsg message) {
        printProgress(Float.NaN,message);
        return this;
    }

    public abstract NSession getSession();
}
