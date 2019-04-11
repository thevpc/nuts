package net.vpc.app.nuts.core.terminals;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsQuestionExecutor;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class DefaultNutsSessionTerminal implements NutsSessionTerminal {

    protected PrintStream out_getFormatted_Force;
    protected PrintStream out_getFormatted_NoForce;
    protected PrintStream err_getFormatted_Force;
    protected PrintStream err_getFormatted_NoForce;
    protected NutsWorkspace ws;
    protected PrintStream out;
    protected PrintStream err;
    protected InputStream in;
    protected BufferedReader inReader;
    protected NutsTerminalBase parent;

    @Override
    public void install(NutsWorkspace workspace) {
        this.ws = workspace;
    }

    @Override
    public void setParent(NutsTerminalBase parent) {
        this.parent = parent;
    }

    @Override
    public NutsTerminalBase getParent() {
        return parent;
    }

    @Override
    public PrintStream getFormattedOut() {
        return getFormattedOut(false);
    }

    @Override
    public PrintStream getFormattedErr() {
        return getFormattedErr(false);
    }

    @Override
    public PrintStream getFormattedOut(boolean forceNoColors) {
        if (forceNoColors) {
            if (out_getFormatted_Force == null) {
                out_getFormatted_Force = ws.io().createPrintStream(getOut(), NutsTerminalMode.FILTERED);
            }
            return out_getFormatted_Force;
        } else {
            if (out_getFormatted_NoForce == null) {
                out_getFormatted_NoForce = ws.io().createPrintStream(getOut(), NutsTerminalMode.FORMATTED);
            }
            return out_getFormatted_NoForce;
        }
    }

    @Override
    public PrintStream getFormattedErr(boolean forceNoColors) {
        if (forceNoColors) {
            if (err_getFormatted_Force == null) {
                err_getFormatted_Force = ws.io().createPrintStream(getErr(), NutsTerminalMode.FILTERED);
            }
            return err_getFormatted_Force;
        } else {
            if (err_getFormatted_NoForce == null) {
                err_getFormatted_NoForce = ws.io().createPrintStream(getErr(), NutsTerminalMode.FORMATTED);
            }
            return err_getFormatted_NoForce;
        }
    }

    @Override
    public InputStream getIn() {
        if (this.in != null) {
            return this.in;
        }
        if (parent != null) {
            return parent.getIn();
        }
        return null;
    }

    @Override
    public PrintStream getOut() {
        if (this.out != null) {
            return this.out;
        }
        if (parent != null) {
            return parent.getOut();
        }
        return null;
    }

    @Override
    public PrintStream getErr() {
        if (this.err != null) {
            return this.err;
        }
        if (parent != null) {
            return parent.getErr();
        }
        return null;
    }

    @Override
    public void setIn(InputStream in) {
        this.in = in;
        this.inReader = null;
    }

    @Override
    public void setOut(PrintStream out) {
        this.out = out;
        out_getFormatted_Force = null;
        out_getFormatted_NoForce = null;
    }

    @Override
    public void setErr(PrintStream err) {
        this.err = err;
        err_getFormatted_Force = null;
        err_getFormatted_NoForce = null;
    }

    public BufferedReader getReader() {
        if (this.inReader != null) {
            return this.inReader;
        }
        final InputStream _in = getIn();
        if (_in != null) {
            this.inReader = new BufferedReader(new InputStreamReader(_in));
        }
        return this.inReader;
    }

    @Override
    public String readLine(PrintStream out, String prompt, Object... params) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = System.out;
        }
        if (this.in == null && parent != null) {
            if (this.out == null) {
                return parent.readLine(out, prompt, params);
            } else {
                return parent.readLine(out, prompt, params);
            }
        }
        out.printf(prompt, params);
        out.flush();
        try {
            return getReader().readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean isInOverridden() {
        return in != null;
    }

    public boolean isOutOverridden() {
        return out != null;
    }

    public boolean isErrOverridden() {
        return err != null;
    }

    @Override
    public String readLine(String prompt, Object... params) {
        return readLine(getOut(), prompt, params);
    }

    @Override
    public String readPassword(String prompt, Object... params) {
        return readPassword(getOut(), prompt, params);
    }

    @Override
    public String readPassword(PrintStream out, String prompt, Object... params) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = System.out;
        }

        if (this.in == null && parent != null) {
            if (this.out == null) {
                return parent.readPassword(out, prompt, params);
            } else {
                return parent.readPassword(out, prompt, params);
            }
        }

        InputStream in = getIn();
        Console cons = null;
        char[] passwd = null;
        if (in == null) {
            in = System.in;
        }
        if (in == System.in && ((cons = System.console()) != null)) {
            if ((passwd = cons.readPassword(prompt, params)) != null) {
                String pwd = new String(passwd);
                Arrays.fill(passwd, ' ');
                return pwd;
            } else {
                return null;
            }
        } else {
            out.printf(prompt, params);
            out.flush();
            Scanner s = new Scanner(in);
            return s.nextLine();
        }
    }

    protected void copyFrom(DefaultNutsSessionTerminal other) {
        this.ws = other.ws;
        this.parent = other.parent;
        this.out_getFormatted_Force = other.out_getFormatted_Force;
        this.out_getFormatted_NoForce = other.out_getFormatted_NoForce;
        this.err_getFormatted_Force = other.err_getFormatted_Force;
        this.err_getFormatted_NoForce = other.err_getFormatted_NoForce;
        this.in = other.in;
        this.inReader = other.inReader;
        this.out = other.out;
        this.err = other.err;
    }

    @Override
    public <T> T ask(NutsQuestion<T> question) {
        return new DefaultNutsQuestionExecutor<T>(ws, question, this, getFormattedOut())
                .execute();
    }

    @Override
    public NutsSessionTerminal copy() {
        final DefaultNutsSessionTerminal r = new DefaultNutsSessionTerminal();
        r.copyFrom(this);
        return r;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public void uninstall() {

    }
}
