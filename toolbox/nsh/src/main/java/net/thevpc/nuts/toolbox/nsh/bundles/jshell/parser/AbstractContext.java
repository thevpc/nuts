package net.thevpc.nuts.toolbox.nsh.bundles.jshell.parser;

public abstract class AbstractContext implements Context {
    protected final JShellParser reader;

    public AbstractContext(JShellParser reader) {
        this.reader = reader;
    }

}
