package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.console.CProgressBar;
import net.thevpc.nuts.spi.NutsTerminalBase;

import java.io.InputStream;
import java.io.PrintStream;

public class UnmodifiableTerminal extends AbstractNutsTerminal implements NutsSessionTerminal {

    private final NutsSessionTerminal base;
    protected CProgressBar progressBar;
    protected NutsSession session;

    public UnmodifiableTerminal(NutsSessionTerminal base, NutsSession session) {
        this.base = base;
        this.session = session;
    }

//    @Override
//    public NutsSessionTerminal setMode(NutsTerminalMode mode) {
//        //
//        return this;
//    }
//
//    @Override
//    public NutsTerminal setOutMode(NutsTerminalMode mode) {
//        return this;
//    }
//
//    @Override
//    public NutsTerminal setErrMode(NutsTerminalMode mode) {
//        return this;
//    }
//
//    @Override
//    public NutsTerminalMode getErrMode() {
//        return getBase().getErrMode();
//    }
//
//    @Override
//    public NutsTerminalMode getOutMode() {
//        return getBase().getOutMode();
//    }

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
    public NutsTerminal printProgress(float progress, String prompt, Object... params) {
        if (getBase() != null) {
            ((NutsTerminal) getParent()).printProgress(progress, prompt, params);
        } else {
            getProgressBar().printProgress(
                    Float.isNaN(progress) ? -1
                            : (int) (progress * 100),
                    session.getWorkspace().text().toText(NutsMessage.cstyle(prompt, params)).toString(),
                    err()
            );
        }
        return this;
    }

    @Override
    public NutsTerminal printProgress(String prompt, Object... params) {
        if (getBase() != null) {
            getBase().printProgress(prompt, params);
        } else {
            getProgressBar().printProgress(-1,
                    session.getWorkspace().text().toText(NutsMessage.cstyle(prompt, params)).toString(),
                    err()
            );
        }
        return this;
    }

    @Override
    public String readLine(NutsPrintStream out, String promptFormat, Object... params) {
        return getBase().readLine(out, promptFormat, params);
    }

    @Override
    public char[] readPassword(NutsPrintStream out, String prompt, Object... params) {
        return getBase().readPassword(out, prompt, params);
    }

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
    public NutsSessionTerminal copy() {
        return getBase().copy();
    }

    @Override
    public NutsPrintStream getErr() {
        return getBase().getErr();
    }

    @Override
    public NutsTerminalBase getParent() {
        return getBase().getParent();
    }

    @Override
    public void setParent(NutsTerminalBase parent) {
    }

    @Override
    public void setErr(NutsPrintStream out) {

    }

    private CProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = CoreTerminalUtils.createProgressBar(session);
        }
        return progressBar;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsTerminalSpec> criteria) {
        return getBase().getSupportLevel(criteria);
    }

    public NutsSessionTerminal getBase() {
        return base;
    }
}
