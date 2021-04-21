package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsQuestion;
import net.thevpc.nuts.runtime.standalone.util.SearchTraceHelper;
import net.thevpc.nuts.runtime.standalone.util.console.CProgressBar;
import net.thevpc.nuts.spi.NutsTerminalBase;

import java.io.*;
import java.util.Scanner;

@NutsPrototype
public class DefaultNutsSessionTerminal extends AbstractNutsTerminal implements NutsSessionTerminal,NutsSessionAware {

    protected NutsWorkspace ws;
    protected NutsSession session;
    protected final OutInfo out = new OutInfo(true);
    protected final OutInfo err = new OutInfo(false);
    protected InputStream in;
    protected BufferedReader inReader;
    protected NutsTerminalBase parent;
    protected NutsTerminalMode outMode = NutsTerminalMode.FORMATTED;
    protected NutsTerminalMode errMode = NutsTerminalMode.FORMATTED;
    protected CProgressBar progressBar;

    @Override
    public void setSession(NutsSession session) {
        if(session!=null && this.session!=null){
            //ignore
        }else {
            this.session = session;
            this.ws = session==null?null:session.getWorkspace();
            if(this.ws!=null) {
                this.out.sessionTerm = this;
                this.err.sessionTerm = this;
            }
        }
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
    public NutsTerminal setOutMode(NutsTerminalMode mode) {
        if (mode == null) {
            mode = NutsTerminalMode.INHERITED;
        }
        this.outMode = mode;
        return this;
    }

    @Override
    public NutsTerminal setErrMode(NutsTerminalMode mode) {
        if (mode == null) {
            mode = NutsTerminalMode.INHERITED;
        }
        this.errMode = mode;
        return this;
    }

    @Override
    public NutsTerminalMode getOutMode() {
        return this.outMode;
    }

    @Override
    public NutsTerminalMode getErrMode() {
        return this.errMode;
    }

    @Override
    public NutsSessionTerminal setMode(NutsTerminalMode mode) {
        if (mode == null) {
            mode = NutsTerminalMode.INHERITED;
        }
        this.outMode = mode;
        return this;
    }

    @Override
    public PrintStream out() {
        return getOut();
    }

    @Override
    public PrintStream err() {
        return getErr();
    }

    @Override
    public InputStream in() {
        return getIn();
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
        return this.out.curr(outMode);
    }

    @Override
    public PrintStream getErr() {
        return this.err.curr(errMode);
    }

    @Override
    public void setIn(InputStream in) {
        this.in = in;
        this.inReader = null;
    }

    @Override
    public void setOut(PrintStream out) {
        this.out.setBase(out);
    }

    @Override
    public void setErr(PrintStream err) {
        this.err.setBase(err);
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
            out = out();
        }
        if (out == null) {
            out = CoreIOUtils.out(ws);
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
        return readLine(out(), prompt, params);
    }

    @Override
    public char[] readPassword(String prompt, Object... params) {
        return readPassword(out(), prompt, params);
    }

    @Override
    public NutsTerminalBase printProgress(float progress, String prompt, Object... params) {
        if(getParent()!=null) {
            getParent().printProgress(progress, prompt, params);
        }else{
            getProgressBar().printProgress(
                    Float.isNaN(progress)?-1:
                    (int)(progress*100),
                    session.getWorkspace().formats().text().toText(NutsMessage.cstyle(prompt,params)).toString(),
                    err()
            );
        }
        return this;
    }

    @Override
    public NutsTerminalBase printProgress(String prompt, Object... params) {
        if(getParent()!=null) {
            getParent().printProgress(prompt, params);
        }else{
            getProgressBar().printProgress(-1,
                    session.getWorkspace().formats().text().toText(NutsMessage.cstyle(prompt,params)).toString(),
                    err()
                    );
        }
        return this;
    }

    private CProgressBar getProgressBar() {
        if(progressBar==null){
            progressBar=SearchTraceHelper.createProgressBar(session);
        }
        return progressBar;
    }

    @Override
    public char[] readPassword(PrintStream out, String prompt, Object... params) {
        if (out == null) {
            out = out();
        }
        if (out == null) {
            out = CoreIOUtils.out(ws);
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
            in = CoreIOUtils.in(ws);
        }
        if ((in == System.in || in==CoreIOUtils.in(ws)) && ((cons = System.console()) != null)) {
            if ((passwd = cons.readPassword(prompt, params)) != null) {
                return passwd;
            } else {
                return null;
            }
        } else {
            out.printf(prompt, params);
            out.flush();
            Scanner s = new Scanner(in);
            return s.nextLine().toCharArray();
        }
    }

    protected void copyFrom(DefaultNutsSessionTerminal other) {
        this.ws = other.ws;
        this.parent = other.parent;
        this.outMode = other.outMode;
        this.errMode = other.errMode;
        this.out.setBase(other.out.base);;
        this.err.setBase(other.err.base);;
        this.in = other.in;
        this.inReader = other.inReader;
    }

    @Override
    public <T> NutsQuestion<T> ask() {
        return new DefaultNutsQuestion<T>(ws, this, out());
    }

    @Override
    public NutsSessionTerminal copy() {
        final DefaultNutsSessionTerminal r = new DefaultNutsSessionTerminal();
        r.copyFrom(this);
        return r;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsTerminalSpec> criteria) {
        return DEFAULT_SUPPORT;
    }

    protected static class OutInfo {

        PrintStream base;
        PrintStream baseOld;
        PrintStream formatted;
        PrintStream filtered;
        boolean typeOut;
        DefaultNutsSessionTerminal sessionTerm;

        public OutInfo(boolean out) {
            this.typeOut = out;
        }

//        NutsTerminalMode evalMode(PrintStream b) {
//            NutsTerminalMode currMode = NutsTerminalMode.INHERITED;
//            if (b == null) {
//                currMode = NutsTerminalMode.INHERITED;
//            } else if (b instanceof NutsFormattedPrintStream) {
//                currMode = NutsTerminalMode.FORMATTED;
//            } else if (b instanceof NutsNonFormattedPrintStream) {
//                currMode = NutsTerminalMode.FILTERED;
//            } else {
//                currMode = NutsTerminalMode.INHERITED;
//            }
//            return currMode;
//        }
        void setBase(PrintStream b) {
            if (this.base != b) {
                this.base = b;
                this.formatted = null;
                this.filtered = null;
            }
        }

        PrintStream base() {
            PrintStream _out = base;
            if (_out != null) {
                return _out;
            }
            if (sessionTerm.parent != null) {
                return typeOut ? sessionTerm.parent.getOut() : sessionTerm.parent.getErr();
            }
            _out = typeOut ? CoreIOUtils.out(sessionTerm.ws) : CoreIOUtils.err(sessionTerm.ws);
            if (_out != null) {
                return _out;
            }
            return typeOut ? System.out : System.err;
        }

        PrintStream formatted() {
            PrintStream b = base();
            if (b == null) {
                return null;
            }
            if (formatted == null || baseOld != b) {
                baseOld = b;
                formatted = sessionTerm.ws.io().setSession(sessionTerm.session).createPrintStream(b, NutsTerminalMode.FORMATTED);
            }
            return formatted;
        }

        PrintStream filtered() {
            PrintStream b = base();
            if (b == null) {
                return null;
            }
            if (filtered == null || baseOld != b) {
                baseOld = b;
                filtered = sessionTerm.ws.io().setSession(sessionTerm.session).createPrintStream(b, NutsTerminalMode.FILTERED);
            }
            return filtered;
        }

        PrintStream curr(NutsTerminalMode mode) {
            switch (mode) {
                case FORMATTED:
                    return formatted();
                case FILTERED:
                    return filtered();
            }
            return base();
        }
    }

    @Override
    public NutsSessionTerminal sendOutCommand(NutsTerminalCommand command) {
        session.getWorkspace().term().sendTerminalCommand(out(), command);
        return this;
    }
    @Override
    public NutsSessionTerminal sendErrCommand(NutsTerminalCommand command) {
        session.getWorkspace().term().sendTerminalCommand(out(), command);
        return this;
    }
}
