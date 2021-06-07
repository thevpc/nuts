package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsQuestion;
import net.thevpc.nuts.runtime.standalone.util.console.CProgressBar;
import net.thevpc.nuts.spi.NutsTerminalBase;

import java.io.*;
import java.util.Scanner;

@NutsPrototype
public class DefaultNutsSessionTerminal extends AbstractNutsTerminal implements NutsSessionTerminal,NutsSessionAware {

    protected NutsWorkspace ws;
    protected NutsSession session;
    protected NutsPrintStream out;
    protected NutsPrintStream err;
    protected InputStream in;
    protected BufferedReader inReader;
    protected NutsTerminalBase parent;
    protected CProgressBar progressBar;

    @Override
    public void setSession(NutsSession session) {
        if(session!=null && this.session!=null){
            //ignore
        }else {
            this.session = session;
            this.ws = session==null?null:session.getWorkspace();
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

//    @Override
//    public NutsTerminal setOutMode(NutsTerminalMode mode) {
//        if (mode == null) {
//            mode = NutsTerminalMode.INHERITED;
//        }
//        this.outMode = mode;
//        return this;
//    }

//    @Override
//    public NutsTerminal setErrMode(NutsTerminalMode mode) {
//        if (mode == null) {
//            mode = NutsTerminalMode.INHERITED;
//        }
//        this.errMode = mode;
//        return this;
//    }

//    @Override
//    public NutsTerminalMode getOutMode() {
//        return this.outMode;
//    }
//
//    @Override
//    public NutsTerminalMode getErrMode() {
//        return this.errMode;
//    }

//    @Override
//    public NutsSessionTerminal setMode(NutsTerminalMode mode) {
//        if (mode == null) {
//            mode = NutsTerminalMode.INHERITED;
//        }
//        this.outMode = mode;
//        return this;
//    }

    @Override
    public NutsPrintStream out() {
        return getOut();
    }

    @Override
    public NutsPrintStream err() {
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
    public NutsPrintStream getOut() {
        if(out==null){
            NutsTerminalBase p = getParent();
            if(p!=null){
                return p.getOut();
            }
        }
        return this.out;
    }

    @Override
    public NutsPrintStream getErr() {
        if(err==null){
            NutsTerminalBase p = getParent();
            if(p!=null){
                return p.getErr();
            }
        }
        return this.err;
    }

    @Override
    public void setIn(InputStream in) {
        this.in = in;
        this.inReader = null;
    }

    @Override
    public void setOut(NutsPrintStream out) {
        this.out=out;
    }

    @Override
    public void setErr(NutsPrintStream err) {
        this.err=err;
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
    public String readLine(NutsPrintStream out, String prompt, Object... params) {
        if (out == null) {
            out = out();
        }
        if (out == null) {
            out = ws.io().stdout();
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
    public NutsTerminal printProgress(float progress, String prompt, Object... params) {
        if(getParent() instanceof NutsTerminal) {
            ((NutsTerminal)getParent()).printProgress(progress, prompt, params);
        }else{
            getProgressBar().printProgress(
                    Float.isNaN(progress)?-1:
                    (int)(progress*100),
                    session.getWorkspace().text().toText(NutsMessage.cstyle(prompt,params)).toString(),
                    err()
            );
        }
        return this;
    }

    @Override
    public NutsTerminal printProgress(String prompt, Object... params) {
        if(getParent() instanceof NutsTerminal) {
            ((NutsTerminal)getParent()).printProgress(prompt, params);
        }else{
            getProgressBar().printProgress(-1,
                    session.getWorkspace().text().toText(NutsMessage.cstyle(prompt,params)).toString(),
                    err()
                    );
        }
        return this;
    }

    private CProgressBar getProgressBar() {
        if(progressBar==null){
            progressBar=CoreTerminalUtils.createProgressBar(session);
        }
        return progressBar;
    }

    @Override
    public char[] readPassword(NutsPrintStream out, String prompt, Object... params) {
        if (out == null) {
            out = out();
        }
        if (out == null) {
            out = ws.io().stdout();
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
            in = ws.io().stdin();
        }
        if ((
                in==ws.io().stdin()
        ) && ((cons = System.console()) != null)) {
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
        this.out=other.out;
        this.err=other.err;
        this.in = other.in;
        this.inReader = other.inReader;
    }

    @Override
    public <T> NutsQuestion<T> ask() {
        return
                new DefaultNutsQuestion<T>(ws, this, out())
                        .setSession(session)
                ;
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
}
