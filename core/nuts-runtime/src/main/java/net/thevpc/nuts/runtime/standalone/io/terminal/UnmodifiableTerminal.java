package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.runtime.standalone.io.progress.CProgressBar;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NAsk;

import java.io.InputStream;

public class UnmodifiableTerminal extends AbstractNTerminal {

    private final NTerminal base;
    protected CProgressBar progressBar;
    protected NWorkspace workspace;

    public UnmodifiableTerminal(NTerminal base, NWorkspace workspace) {
        this.base = base;
        this.workspace = workspace;
    }

    @Override
    public String readLine(NPrintStream out, NMsg message) {
        return getBase().readLine(out, message);
    }

    @Override
    public char[] readPassword(NPrintStream out, NMsg prompt) {
        return getBase().readPassword(out, prompt);
    }

    @Override
    public InputStream getIn() {
        return getBase().getIn();
    }

    @Override
    public void setIn(InputStream in) {

    }

    @Override
    public NPrintStream getOut() {
        return getBase().getOut();
    }

    @Override
    public void setOut(NPrintStream out) {

    }

    @Override
    public NPrintStream getErr() {
        return getBase().getErr();
    }

    @Override
    public void setErr(NPrintStream out) {

    }

    @Override
    public NTerminal copy() {
        return getBase().copy();
    }

    @Override
    public <T> NAsk<T> ask() {
        return getBase()
                .<T>ask()
                ;
    }

    @Override
    public InputStream in() {
        return getBase().in();
    }

    @Override
    public NPrintStream out() {
        return getBase().out();
    }

    @Override
    public NPrintStream err() {
        return getBase().err();
    }

//    @Override
//    public int getSupportLevel(NutsSupportLevelContext<NutsTerminalSpec> criteria) {
//        return getBase().getSupportLevel(criteria);
//    }

    @Override
    public NTerminal printProgress(float progress, NMsg message) {
        NSession session=workspace.currentSession();
        if (session.isProgress()) {
            if (getBase() != null) {
                getBase().printProgress(progress, message);
            } else {
                getProgressBar().printProgress(
                        Float.isNaN(progress) ? -1
                                : (int) (progress * 100),
                        NTexts.of().ofText(message),
                        err()
                );
            }
        }
        return this;
    }

    private CProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = CProgressBar.of();
        }
        return progressBar;
    }

    public NTerminal getBase() {
        return base;
    }

}
