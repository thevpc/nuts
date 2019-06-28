package net.vpc.app.nuts.core.terminals;

import net.vpc.app.nuts.NutsQuestion;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.InputStream;
import java.io.PrintStream;

import net.vpc.app.nuts.NutsSessionTerminal;
import net.vpc.app.nuts.NutsSupportLevelContext;
import net.vpc.app.nuts.NutsTerminalBase;
import net.vpc.app.nuts.NutsTerminalMode;

public class UnmodifiableTerminal implements NutsSessionTerminal {

    private NutsSessionTerminal base;

    public UnmodifiableTerminal(NutsSessionTerminal base) {
        this.base = base;
    }

    @Override
    public void setParent(NutsTerminalBase parent) {
    }

    @Override
    public NutsTerminalBase getParent() {
        return base.getParent();
    }

    @Override
    public PrintStream out() {
        return base.out();
    }

    @Override
    public PrintStream err() {
        return base.err();
    }

    @Override
    public InputStream in() {
        return base.in();
    }

    @Override
    public void setIn(InputStream in) {

    }

    @Override
    public void setOut(PrintStream out) {

    }

    @Override
    public void setErr(PrintStream out) {

    }

    @Override
    public NutsSessionTerminal setTerminalMode(NutsTerminalMode mode) {
        //
        return this;
    }

    @Override
    public NutsTerminalMode getTerminalMode() {
        return base.getTerminalMode();
    }

    @Override
    public NutsSessionTerminal copy() {
        return base.copy();
    }

    @Override
    public void install(NutsWorkspace workspace) {
        //
    }

    @Override
    public String readLine(String promptFormat, Object... params) {
        return base.readLine(promptFormat, params);
    }

    @Override
    public char[] readPassword(String prompt, Object... params) {
        return base.readPassword(prompt, params);
    }

    @Override
    public String readLine(PrintStream out, String promptFormat, Object... params) {
        return base.readLine(out, promptFormat, params);
    }

    @Override
    public char[] readPassword(PrintStream out, String prompt, Object... params) {
        return base.readPassword(out, prompt, params);
    }

    @Override
    public InputStream getIn() {
        return base.getIn();
    }

    @Override
    public PrintStream getOut() {
        return base.getOut();
    }

    @Override
    public PrintStream getErr() {
        return base.getErr();
    }

    @Override
    public <T> NutsQuestion<T> ask() {
        return base.ask();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return base.getSupportLevel(criteria);
    }

    @Override
    public void uninstall() {

    }
}
