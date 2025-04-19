package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.shell;

import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringBuilder;

public abstract class AbstractShellWriter implements NShellWriter {
    private NShellFamily family;
    private NStringBuilder out = new NStringBuilder();
    private boolean commentsMode;

    public AbstractShellWriter(NShellFamily family) {
        this.family = family;
    }

    public NStringBuilder out() {
        return out;
    }

    public boolean isCommentsMode() {
        return commentsMode;
    }

    @Override
    public NShellWriter printlnSetAppendVar(String varName, String varExpr) {
        return printlnSetVar(varName, "${" + varName + "}" + varExpr);
    }

    public NShellWriter setCommentsMode(boolean commentsMode) {
        this.commentsMode = commentsMode;
        return this;
    }

    protected void printlnCommandImpl(String any) {
        if (isCommentsMode()) {
            out().println(lineCommentImpl(any));
        } else {
            out().println(any);
        }
    }

    protected String replaceDollarVar(String a) {
        return NMsg.ofV(a, this::varValue).toString();
    }


    @Override
    public NShellWriter println() {
        out().println("");
        return this;
    }

    @Override
    public NShellWriter printlnComment(String comment) {
        new NStringBuilder(comment).lines().forEach(x -> {
            out.println(lineCommentImpl(x));
        });
        return this;
    }

    protected abstract String lineCommentImpl(String anyString);


    @Override
    public String build() {
        return out.build();
    }

    @Override
    public NShellWriter printlnCommand(String any) {
        printlnCommandImpl(replaceDollarVar(any));
        return this;
    }

    public abstract String varValue(String varName);

}
