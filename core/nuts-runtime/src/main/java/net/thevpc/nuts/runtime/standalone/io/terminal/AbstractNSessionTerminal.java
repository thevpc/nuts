package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NSessionTerminal;

public abstract class AbstractNSessionTerminal implements NSessionTerminal {
    @Override
    public char[] readPassword(NMsg prompt) {
        return readPassword(out(), prompt);
    }

    @Override
    public char[] readPassword(NPrintStream out, NMsg prompt) {
        return readPassword((out == null ? out() : out), prompt, getSession());
    }

    public String readLine(NPrintStream out, NMsg message) {
        return readLine((out == null ? out() : out), message, getSession());
    }

    public String readLine(NMsg message) {
        return readLine(out(), message, getSession());
    }


    @Override
    public NSessionTerminal printProgress(NMsg message) {
        printProgress(Float.NaN, message);
        return this;
    }

    public abstract NSession getSession();
}
