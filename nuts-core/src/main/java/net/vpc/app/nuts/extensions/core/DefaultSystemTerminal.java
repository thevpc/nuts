package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.terminals.DefaultNutsQuestionExecutor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class DefaultSystemTerminal implements NutsSystemTerminal {
    private NutsSystemTerminalBase base;

    public DefaultSystemTerminal(NutsSystemTerminalBase base) {
        this.base = base;
    }

    @Override
    public void install(NutsWorkspace workspace) {
        base.install(workspace);
    }

    @Override
    public void setMode(NutsTerminalMode mode) {
        base.setOutMode(mode);
        base.setErrorMode(mode);
    }

    @Override
    public void setOutMode(NutsTerminalMode mode) {
        base.setOutMode(mode);

    }

    @Override
    public NutsTerminalMode getOutMode() {
        return base.getOutMode();
    }

    @Override
    public void setErrorMode(NutsTerminalMode mode) {
        base.setErrorMode(mode);
    }

    @Override
    public NutsTerminalMode getErrorMode() {
        return base.getErrorMode();
    }

    @Override
    public String readLine(String promptFormat, Object... params) {
        return base.readLine(promptFormat,params);
    }

    @Override
    public String readPassword(String prompt) {
        return base.readLine(prompt);
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
        return new DefaultNutsQuestionExecutor<T>(question,this,getOut())
                .execute();
    }


    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public boolean isStandardOutputStream(OutputStream out) {
        if (out == null) {
            return true;
        }
        if (out == System.out) {
            return true;
        }
        if (out instanceof OutputStreamTransparentAdapter) {
            return isStandardOutputStream(((OutputStreamTransparentAdapter) out).baseOutputStream());
        }
        return false;
    }

    @Override
    public boolean isStandardErrorStream(OutputStream out) {
        if (out == null) {
            return true;
        }
        if (out == System.err) {
            return true;
        }
        if (out instanceof OutputStreamTransparentAdapter) {
            return isStandardErrorStream(((OutputStreamTransparentAdapter) out).baseOutputStream());
        }
        return false;
    }

    @Override
    public boolean isStandardInputStream(InputStream in) {
        if (in == null) {
            return true;
        }
        if (in == System.in) {
            return true;
        }
        if (in instanceof InputStreamTransparentAdapter) {
            return isStandardInputStream(((InputStreamTransparentAdapter) in).baseInputStream());
        }
        return false;
    }
}
