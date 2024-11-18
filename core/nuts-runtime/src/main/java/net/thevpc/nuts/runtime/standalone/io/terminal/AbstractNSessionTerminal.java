package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NSessionTerminal;

public abstract class AbstractNSessionTerminal implements NSessionTerminal {
    @Override
    public char[] readPassword(NMsg prompt) {
        return readPassword(out(), prompt);
    }

    public String readLine(NMsg message) {
        return readLine(out(), message);
    }

    @Override
    public NSessionTerminal printProgress(NMsg message) {
        printProgress(Float.NaN, message);
        return this;
    }

}
