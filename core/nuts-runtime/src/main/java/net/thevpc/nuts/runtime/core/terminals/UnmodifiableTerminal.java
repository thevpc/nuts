package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsTerminalBase;

import java.io.InputStream;
import java.io.PrintStream;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.util.console.CProgressBar;

public class UnmodifiableTerminal extends AbstractNutsTerminal implements NutsSessionTerminal {

    private final NutsSessionTerminal base;
    protected CProgressBar progressBar;
    protected NutsSession session;

    public UnmodifiableTerminal(NutsSessionTerminal base,NutsSession session) {
        this.base = base;
        this.session = session;
    }

    @Override
    public NutsSessionTerminal setMode(NutsTerminalMode mode) {
        //
        return this;
    }

    @Override
    public NutsTerminal setOutMode(NutsTerminalMode mode) {
        return this;
    }

    @Override
    public NutsTerminal setErrMode(NutsTerminalMode mode) {
        return this;
    }

    @Override
    public NutsTerminalMode getErrMode() {
        return getBase().getErrMode();
    }

    @Override
    public NutsTerminalMode getOutMode() {
        return getBase().getOutMode();
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
    public PrintStream out() {
        return getBase().out();
    }

    @Override
    public PrintStream err() {
        return getBase().err();
    }

    @Override
    public String readLine(PrintStream out, String promptFormat, Object... params) {
        return getBase().readLine(out, promptFormat, params);
    }

    @Override
    public char[] readPassword(PrintStream out, String prompt, Object... params) {
        return getBase().readPassword(out, prompt, params);
    }

 @Override
    public NutsTerminal printProgress(float progress, String prompt, Object... params) {
        if (getBase() instanceof NutsTerminal) {
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
        if (getBase() instanceof NutsTerminal) {
            ((NutsTerminal) getBase()).printProgress(prompt, params);
        } else {
            getProgressBar().printProgress(-1,
                    session.getWorkspace().text().toText(NutsMessage.cstyle(prompt, params)).toString(),
                    err()
            );
        }
        return this;
    }

    private CProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = CoreTerminalUtils.createProgressBar(session);
        }
        return progressBar;
    }

    @Override
    public InputStream getIn() {
        return getBase().getIn();
    }

    @Override
    public void setIn(InputStream in) {

    }

    @Override
    public PrintStream getOut() {
        return getBase().getOut();
    }

    @Override
    public void setOut(PrintStream out) {

    }

    @Override
    public PrintStream getErr() {
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
    public void setErr(PrintStream out) {

    }

    @Override
    public NutsSessionTerminal copy() {
        return getBase().copy();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsTerminalSpec> criteria) {
        return getBase().getSupportLevel(criteria);
    }

    public NutsSessionTerminal getBase() {
        return base;
    }

    @Override
    public NutsTerminal sendOutCommand(NutsTerminalCommand command) {
         getBase().sendOutCommand(command);
        return this;
    }

    @Override
    public NutsTerminal sendErrCommand(NutsTerminalCommand command) {
        getBase().sendErrCommand(command);
        return this;
    }
}
