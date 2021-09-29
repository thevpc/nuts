package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsQuestion;
import net.thevpc.nuts.runtime.standalone.io.progress.CProgressBar;

import java.io.*;
import java.util.Scanner;

@NutsPrototype
public class DefaultNutsSessionTerminal2 extends AbstractNutsSessionTerminal {

    protected NutsWorkspace ws;
    protected NutsSession session;
    protected NutsPrintStream out;
    protected NutsPrintStream err;
    protected InputStream in;
    protected BufferedReader inReader;
    protected NutsSessionTerminal parent;
    protected CProgressBar progressBar;

    public DefaultNutsSessionTerminal2(NutsSession session, DefaultNutsSessionTerminal2 other) {
        this.session = session;
        this.parent = other.parent;
        this.ws = session.getWorkspace();
        this.in = other.in;
        this.inReader = other.inReader;
        this.out = other.out;
        this.err = other.err;
    }

    public DefaultNutsSessionTerminal2(NutsSession session, NutsSessionTerminal parent) {
        this.session = session;
        this.parent = parent;
        this.ws = session.getWorkspace();
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
    public String readLine(NutsPrintStream out, NutsMessage message) {
        return readLine(out,message,null);
    }

    @Override
    public String readLine(NutsPrintStream out, NutsMessage message, NutsSession session) {
        if(session==null){
            this.session=session;
        }
        if (out == null) {
            out = out();
        }
        if (out == null) {
            out = ws.io().stdout();
        }
        if (this.in == null && parent != null) {
            if (this.out == null) {
                return parent.readLine(out, message, session);
            } else {
                return parent.readLine(out, message, session);
            }
        }
        out.printf("%s", message);
        out.flush();
        try {
            return getReader().readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public char[] readPassword(NutsPrintStream out, NutsMessage message) {
        return readPassword(out,message,null);
    }

    @Override
    public char[] readPassword(NutsPrintStream out, NutsMessage message,NutsSession session) {
        if(session==null){
            this.session=session;
        }
        if (out == null) {
            out = out();
        }
        if (out == null) {
            out = ws.io().stdout();
        }

        if (this.in == null && parent != null) {
            if (this.out == null) {
                return parent.readPassword(out, message, session);
            } else {
                return parent.readPassword(out, message, session);
            }
        }

        InputStream in = getIn();
        Console cons = null;
        char[] passwd = null;
        if (in == null) {
            in = ws.io().stdin();
        }
        if ((
                in == ws.io().stdin()
        ) && ((cons = System.console()) != null)) {
            String txt=session.text().toText(message).toString();
            if ((passwd = cons.readPassword("%s",txt)) != null) {
                return passwd;
            } else {
                return null;
            }
        } else {
            out.printf("%s", message);
            out.flush();
            Scanner s = new Scanner(in);
            return s.nextLine().toCharArray();
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
    public NutsPrintStream getOut() {
        if (out == null) {
            NutsSessionTerminal p = getParent();
            if (p != null) {
                return p.getOut();
            }
        }
        return this.out;
    }

    @Override
    public NutsPrintStream getErr() {
        if (err == null) {
            NutsSessionTerminal p = getParent();
            if (p != null) {
                return p.getErr();
            }
        }
        return this.err;
    }

    public NutsSessionTerminal getParent() {
        return parent;
    }

    @Override
    public void setErr(NutsPrintStream err) {
        if(err!=null){
            err=err.convertSession(session);
        }
        this.err = err;
    }

    @Override
    public NutsSessionTerminal copy() {
        final DefaultNutsSessionTerminal2 r = new DefaultNutsSessionTerminal2(session,parent);
        r.copyFrom(this);
        return r;
    }

    @Override
    public void setOut(NutsPrintStream out) {
        if(out!=null){
            out=out.convertSession(session);
        }
        this.out = out;
    }

    @Override
    public void setIn(InputStream in) {
        this.in = in;
        this.inReader = null;
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
    public <T> NutsQuestion<T> ask() {
        return
                new DefaultNutsQuestion<T>(ws, this, out())
                        .setSession(session)
                ;
    }

    @Override
    public InputStream in() {
        return getIn();
    }

    @Override
    public NutsPrintStream out() {
        return getOut();
    }

    @Override
    public NutsPrintStream err() {
        return getErr();
    }

    @Override
    public NutsSessionTerminal printProgress(float progress, NutsMessage message) {
        if (CoreNutsUtils.acceptProgress(session)) {
            if (getParent() instanceof NutsSystemTerminal) {
                ((NutsSystemTerminal) getParent()).printProgress(progress, message,session);
            } else {
                getProgressBar().printProgress(
                        Float.isNaN(progress) ? -1 :
                                (int) (progress * 100),
                        session.text().toText(message).toString(),
                        err()
                );
            }
        }
        return this;
    }

    private CProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = CoreTerminalUtils.createProgressBar(session);
        }
        return progressBar;
    }

    protected void copyFrom(DefaultNutsSessionTerminal2 other) {
        this.ws = other.ws;
        this.parent = other.parent;
        this.out = other.out;
        this.err = other.err;
        this.in = other.in;
        this.inReader = other.inReader;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }
}
