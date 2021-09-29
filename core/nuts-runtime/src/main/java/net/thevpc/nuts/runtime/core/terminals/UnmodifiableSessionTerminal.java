package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.io.progress.CProgressBar;

import java.io.InputStream;

public class UnmodifiableSessionTerminal extends AbstractNutsSessionTerminal {

    private final NutsSessionTerminal base;
    protected CProgressBar progressBar;
    protected NutsSession session;

    public UnmodifiableSessionTerminal(NutsSessionTerminal base, NutsSession session) {
        this.base = base;
        this.session = session;
    }

    @Override
    public String readLine(NutsPrintStream out, String promptFormat, Object... params) {
        return getBase().readLine(out, promptFormat, params);
    }

    @Override
    public String readLine(NutsPrintStream out, NutsMessage message) {
        return getBase().readLine(out, message);
    }

    @Override
    public char[] readPassword(NutsPrintStream out, NutsMessage message) {
        return getBase().readPassword(out, message);
    }

    @Override
    public String readLine(NutsPrintStream out, NutsMessage message, NutsSession session) {
        return getBase().readLine(out, message, session);
    }

    @Override
    public char[] readPassword(NutsPrintStream out, NutsMessage message, NutsSession session) {
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
    public NutsPrintStream getOut() {
        return getBase().getOut();
    }

    @Override
    public void setOut(NutsPrintStream out) {

    }

    @Override
    public NutsPrintStream getErr() {
        return getBase().getErr();
    }

    @Override
    public void setErr(NutsPrintStream out) {

    }

    @Override
    public NutsSessionTerminal copy() {
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
    public <T> NutsQuestion<T> ask() {
        return getBase()
                .<T>ask()
                .setSession(session);
    }

    @Override
    public InputStream in() {
        return getBase().in();
    }

    @Override
    public NutsPrintStream out() {
        return getBase().out();
    }

    @Override
    public NutsPrintStream err() {
        return getBase().err();
    }

//    @Override
//    public int getSupportLevel(NutsSupportLevelContext<NutsTerminalSpec> criteria) {
//        return getBase().getSupportLevel(criteria);
//    }

    @Override
    public NutsSessionTerminal printProgress(float progress, NutsMessage message) {
        if (CoreNutsUtils.acceptProgress(session)) {
            if (getBase() != null) {
                getBase().printProgress(progress, message);
            } else {
                getProgressBar().printProgress(
                        Float.isNaN(progress) ? -1
                                : (int) (progress * 100),
                        session.text().toText(message).toString(),
                        err()
                );
            }
        }
        return this;
    }

    private CProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = CoreTerminalUtils.createProgressBar(session);
        }
        return progressBar;
    }

    public NutsSessionTerminal getBase() {
        return base;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }
}
