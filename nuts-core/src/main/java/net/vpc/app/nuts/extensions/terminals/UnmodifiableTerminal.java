package net.vpc.app.nuts.extensions.terminals;

import net.vpc.app.nuts.NutsQuestion;
import net.vpc.app.nuts.NutsTerminal;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.InputStream;
import java.io.PrintStream;

public class UnmodifiableTerminal implements NutsTerminal {
    private NutsTerminal base;

    public UnmodifiableTerminal(NutsTerminal base) {
        this.base = base;
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
    public NutsTerminal copy() {
        return base.copy();
    }

    @Override
    public void install(NutsWorkspace workspace, InputStream in, PrintStream out, PrintStream err) {
        //
    }

    @Override
    public String readLine(String promptFormat, Object... params) {
        return base.readLine(promptFormat,params);
    }

    @Override
    public String readPassword(String prompt) {
        return base.readPassword(prompt);
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
}
