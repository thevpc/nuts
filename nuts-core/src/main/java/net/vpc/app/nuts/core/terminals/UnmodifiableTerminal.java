package net.vpc.app.nuts.core.terminals;

import net.vpc.app.nuts.NutsQuestion;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.InputStream;
import java.io.PrintStream;

import net.vpc.app.nuts.NutsSessionTerminal;
import net.vpc.app.nuts.NutsTerminalBase;

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
    public PrintStream getFormattedOut() {
        return base.getFormattedOut();
    }

    @Override
    public PrintStream getFormattedErr() {
        return base.getFormattedErr();
    }

    @Override
    public PrintStream getFormattedOut(boolean forceNoColors) {
        return base.getFormattedOut(forceNoColors);
    }

    @Override
    public PrintStream getFormattedErr(boolean forceNoColors) {
        return base.getFormattedErr(forceNoColors);
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
    public String readPassword(String prompt, Object... params) {
        return base.readPassword(prompt, params);
    }

    @Override
    public String readLine(PrintStream out, String promptFormat, Object... params) {
        return base.readLine(out, promptFormat, params);
    }

    @Override
    public String readPassword(PrintStream out, String prompt, Object... params) {
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
    public <T> T ask(NutsQuestion<T> question) {
        return base.ask(question);
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return base.getSupportLevel(criteria);
    }

    @Override
    public void uninstall() {

    }
}
