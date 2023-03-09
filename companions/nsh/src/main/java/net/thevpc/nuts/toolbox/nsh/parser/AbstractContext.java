package net.thevpc.nuts.toolbox.nsh.parser;

public abstract class AbstractContext implements Context {
    protected final NShellParser reader;

    public AbstractContext(NShellParser reader) {
        this.reader = reader;
    }

}
