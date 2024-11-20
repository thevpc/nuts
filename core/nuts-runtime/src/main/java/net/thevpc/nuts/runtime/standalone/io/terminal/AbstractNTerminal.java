package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.io.NTerminal;

public abstract class AbstractNTerminal implements NTerminal {
    @Override
    public char[] readPassword(NMsg prompt) {
        return readPassword(out(), prompt);
    }

    public String readLine(NMsg message) {
        return readLine(out(), message);
    }

    @Override
    public NTerminal printProgress(NMsg message) {
        printProgress(Float.NaN, message);
        return this;
    }

}
