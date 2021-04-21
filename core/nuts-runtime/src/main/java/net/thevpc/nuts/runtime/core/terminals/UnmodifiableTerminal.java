package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsTerminalBase;

import java.io.InputStream;
import java.io.PrintStream;

public class UnmodifiableTerminal extends AbstractNutsTerminal implements NutsSessionTerminal {

    private final NutsSessionTerminal base;

    public UnmodifiableTerminal(NutsSessionTerminal base) {
        this.base = base;
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
        return getBase().ask();
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
    public NutsTerminalBase printProgress(float progress, String prompt, Object... params) {
        getBase().printProgress(progress, prompt, params);
        return this;
    }

    @Override
    public NutsTerminalBase printProgress(String prompt, Object... params) {
        getBase().printProgress(prompt, params);
        return this;
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
