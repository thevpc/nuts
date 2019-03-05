package net.vpc.app.nuts.core.terminals;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsQuestionExecutor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public abstract class AbstractSystemTerminalAdapter implements NutsSystemTerminal {
    @Override
    public void install(NutsWorkspace workspace) {
        getParent().install(workspace);
    }

    public abstract NutsSystemTerminalBase getParent() ;

    @Override
    public void uninstall() {
        getParent().uninstall();
    }

    @Override
    public void setMode(NutsTerminalMode mode) {
        getParent().setOutMode(mode);
        getParent().setErrorMode(mode);
    }

    @Override
    public void setOutMode(NutsTerminalMode mode) {
        getParent().setOutMode(mode);

    }

    @Override
    public NutsTerminalMode getOutMode() {
        return getParent().getOutMode();
    }

    @Override
    public void setErrorMode(NutsTerminalMode mode) {
        getParent().setErrorMode(mode);
    }

    @Override
    public NutsTerminalMode getErrorMode() {
        return getParent().getErrorMode();
    }

    @Override
    public String readLine(String promptFormat, Object... params) {
        return getParent().readLine(getOut(),promptFormat,params);
    }

    @Override
    public String readPassword(String prompt,Object ... params) {
        return getParent().readLine(getOut(),prompt,params);
    }

    @Override
    public InputStream getIn() {
        return getParent().getIn();
    }

    @Override
    public PrintStream getOut() {
        return getParent().getOut();
    }

    @Override
    public PrintStream getErr() {
        return getParent().getErr();
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
        if (out instanceof NutsOutputStreamTransparentAdapter) {
            return isStandardOutputStream(((NutsOutputStreamTransparentAdapter) out).baseOutputStream());
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
        if (out instanceof NutsOutputStreamTransparentAdapter) {
            return isStandardErrorStream(((NutsOutputStreamTransparentAdapter) out).baseOutputStream());
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
        if (in instanceof NutsInputStreamTransparentAdapter) {
            return isStandardInputStream(((NutsInputStreamTransparentAdapter) in).baseInputStream());
        }
        return false;
    }

    @Override
    public String readLine(PrintStream out, String prompt, Object... params) {
        return getParent().readLine(out,prompt,params);
    }

    @Override
    public String readPassword(PrintStream out, String prompt, Object... params) {
        return getParent().readPassword(out,prompt,params);
    }
}
