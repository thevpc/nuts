package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsQuestion;
import net.thevpc.nuts.runtime.standalone.io.progress.CProgressBar;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

import java.io.*;
import java.util.Scanner;

//@NutsPrototype
public class DefaultNutsSessionTerminalFromSystem extends AbstractNutsSessionTerminal {

    protected NutsWorkspace ws;
    protected NutsSession session;
    protected NutsPrintStream out;
    protected NutsPrintStream err;
    protected NutsPrintStreamCache outCache=new NutsPrintStreamCache();
    protected NutsPrintStreamCache errCache=new NutsPrintStreamCache();
    protected InputStream in;
    protected BufferedReader inReader;
    protected NutsSystemTerminalBase parent;
    protected CProgressBar progressBar;

    public DefaultNutsSessionTerminalFromSystem(NutsSession session, DefaultNutsSessionTerminalFromSystem other) {
        this.session = session;
        this.parent = other.parent;
        this.ws = session.getWorkspace();
        this.in = other.in;
        this.inReader = other.inReader;
        setOut(other.out);
        setErr(other.err);
    }

    public DefaultNutsSessionTerminalFromSystem(NutsSession session, NutsSystemTerminalBase parent) {
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
        return readLine(out, message, session);
    }

    @Override
    public char[] readPassword(NutsPrintStream out, NutsMessage message) {
        return readPassword(out, message, session);
    }

    @Override
    public String readLine(NutsPrintStream out, NutsMessage message, NutsSession session) {
        if (session == null) {
            session = this.session;
        }
        if (out == null) {
            out = out();
        }
        if (out == null) {
            out = NutsPrintStreams.of(session).stdout();
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
            throw new NutsIOException(session,e);
        }
    }

    @Override
    public char[] readPassword(NutsPrintStream out, NutsMessage message, NutsSession session) {
        if (session == null) {
            session = this.session;
        }
        if (out == null) {
            out = out();
        }
        if (out == null) {
            out = NutsPrintStreams.of(session).stdout();
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
            in = NutsInputStreams.of(session).stdin();
        }
        if ((
                in == NutsInputStreams.of(session).stdin()
        ) && ((cons = System.console()) != null)) {
            String txt = NutsTexts.of(session).toText(message).toString();
            if ((passwd = cons.readPassword("%s", txt)) != null) {
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
    public void setIn(InputStream in) {
        this.in = in;
        this.inReader = null;
    }

    @Override
    public NutsPrintStream getOut() {
        if (out == null) {
            NutsSystemTerminalBase p = getParent();
            if (p != null) {
                NutsPrintStream o = p.getOut();
                if(o!=null){
                    return outCache.get(o,getSession());
                }
            }
        }
        return this.out;
    }

    @Override
    public void setOut(NutsPrintStream out) {
        if (out != null) {
            out = out.setSession(session);
        }
        this.out = out;
    }

    @Override
    public NutsPrintStream getErr() {
        if (err == null) {
            NutsSystemTerminalBase p = getParent();
            if (p != null) {
                NutsPrintStream o = p.getErr();
                if(o!=null){
                    return errCache.get(o,getSession());
                }
            }
        }
        return this.err;
    }

    @Override
    public void setErr(NutsPrintStream err) {
        if (err != null) {
            err = err.setSession(session);
        }
        this.err = err;
    }

    @Override
    public NutsSessionTerminal copy() {
        final DefaultNutsSessionTerminalFromSystem r = new DefaultNutsSessionTerminalFromSystem(session, parent);
        r.copyFrom(this);
        return r;
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
                ((NutsSystemTerminal) getParent()).printProgress(progress, message, session);
            } else {
                getProgressBar().printProgress(
                        Float.isNaN(progress) ? -1 :
                                (int) (progress * 100),
                        NutsTexts.of(session).toText(message).toString(),
                        err()
                );
            }
        }
        return this;
    }

    public NutsSystemTerminalBase getParent() {
        return parent;
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

    private CProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = CoreTerminalUtils.createProgressBar(session);
        }
        return progressBar;
    }

    protected void copyFrom(DefaultNutsSessionTerminalFromSystem other) {
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
