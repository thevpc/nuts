package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.runtime.standalone.io.progress.CProgressBar;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NQuestion;

import java.io.InputStream;

public class UnmodifiableSessionTerminal extends AbstractNSessionTerminal {

    private final NSessionTerminal base;
    protected CProgressBar progressBar;
    protected NSession session;

    public UnmodifiableSessionTerminal(NSessionTerminal base, NSession session) {
        this.base = base;
        this.session = session;
    }

    @Override
    public String readLine(NOutStream out, String promptFormat, Object... params) {
        return getBase().readLine(out, promptFormat, params);
    }

    @Override
    public String readLine(NOutStream out, NMsg message) {
        return getBase().readLine(out, message);
    }

    @Override
    public char[] readPassword(NOutStream out, NMsg message) {
        return getBase().readPassword(out, message);
    }

    @Override
    public String readLine(NOutStream out, NMsg message, NSession session) {
        return getBase().readLine(out, message, session);
    }

    @Override
    public char[] readPassword(NOutStream out, NMsg message, NSession session) {
        return getBase().readPassword(out, message, session);
    }

//    @Override
//    public NutsTerminal sendOutCommand(NutsTerminalCommand command) {
//        getBase().sendOutCommand(command);
//        return this;
//    }
//
//    @Override
//    public NutsTerminal sendErrCommand(NutsTerminalCommand command) {
//        getBase().sendErrCommand(command);
//        return this;
//    }

    @Override
    public InputStream getIn() {
        return getBase().getIn();
    }

    @Override
    public void setIn(InputStream in) {

    }

    @Override
    public NOutStream getOut() {
        return getBase().getOut();
    }

    @Override
    public void setOut(NOutStream out) {

    }

    @Override
    public NOutStream getErr() {
        return getBase().getErr();
    }

    @Override
    public void setErr(NOutStream out) {

    }

    @Override
    public NSessionTerminal copy() {
        return getBase().copy();
    }

    @Override
    public String readLine(String promptFormat, Object... params) {
        return getBase().readLine(promptFormat, params);
    }

    @Override
    public char[] readPassword(String prompt, Object... params) {
        return getBase().readPassword(prompt, params);
    }

    @Override
    public <T> NQuestion<T> ask() {
        return getBase()
                .<T>ask()
                .setSession(session);
    }

    @Override
    public InputStream in() {
        return getBase().in();
    }

    @Override
    public NOutStream out() {
        return getBase().out();
    }

    @Override
    public NOutStream err() {
        return getBase().err();
    }

//    @Override
//    public int getSupportLevel(NutsSupportLevelContext<NutsTerminalSpec> criteria) {
//        return getBase().getSupportLevel(criteria);
//    }

    @Override
    public NSessionTerminal printProgress(float progress, NMsg message) {
        if (session.isProgress()) {
            if (getBase() != null) {
                getBase().printProgress(progress, message);
            } else {
                getProgressBar().printProgress(
                        Float.isNaN(progress) ? -1
                                : (int) (progress * 100),
                        NTexts.of(session).ofText(message),
                        err()
                );
            }
        }
        return this;
    }

    private CProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = CProgressBar.of(session);
        }
        return progressBar;
    }

    public NSessionTerminal getBase() {
        return base;
    }

    @Override
    public NSession getSession() {
        return session;
    }
}
