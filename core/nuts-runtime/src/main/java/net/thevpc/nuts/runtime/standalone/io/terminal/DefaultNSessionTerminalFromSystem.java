package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.ask.DefaultNAsk;
import net.thevpc.nuts.runtime.standalone.io.progress.CProgressBar;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NAsk;

import java.io.*;
import java.util.Scanner;

//@NutsPrototype
public class DefaultNSessionTerminalFromSystem extends AbstractNSessionTerminal {

    protected NWorkspace ws;
    protected NSession session;
    protected NPrintStream out;
    protected NPrintStream err;
    protected NPrintStreamCache outCache=new NPrintStreamCache();
    protected NPrintStreamCache errCache=new NPrintStreamCache();
    protected InputStream in;
    protected BufferedReader inReader;
    protected NSystemTerminalBase parent;
    protected CProgressBar progressBar;

    public DefaultNSessionTerminalFromSystem(NSession session, DefaultNSessionTerminalFromSystem other) {
        this.session = session;
        this.parent = other.parent;
        this.ws = session.getWorkspace();
        this.in = other.in;
        this.inReader = other.inReader;
        setOut(other.out);
        setErr(other.err);
    }

    public DefaultNSessionTerminalFromSystem(NSession session, NSystemTerminalBase parent) {
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
    public String readLine(NPrintStream out, NMsg message, NSession session) {
        if (session == null) {
            session = this.session;
        }
        if (out == null) {
            out = out();
        }
        if (out == null) {
            out = NIO.of(session).stdout();
        }
        if (this.in == null && parent != null) {
            if (this.out == null) {
                return parent.readLine(out, message, session);
            } else {
                return parent.readLine(out, message, session);
            }
        }
        out.print(message);
        out.flush();
        try {
            return getReader().readLine();
        } catch (IOException e) {
            throw new NIOException(session,e);
        }
    }

    @Override
    public char[] readPassword(NPrintStream out, NMsg prompt, NSession session) {
        if (session == null) {
            session = this.session;
        }
        if (out == null) {
            out = out();
        }
        if (out == null) {
            out = NIO.of(session).stdout();
        }

        if (this.in == null && parent != null) {
            if (this.out == null) {
                return parent.readPassword(out, prompt, session);
            } else {
                return parent.readPassword(out, prompt, session);
            }
        }

        InputStream in = getIn();
        Console cons = null;
        char[] passwd = null;
        if (in == null) {
            in = NIO.of(session).stdin();
        }
        if ((
                in == NIO.of(session).stdin()
        ) && ((cons = System.console()) != null)) {
            String txt = NTexts.of(session).ofText(prompt).toString();
            if ((passwd = cons.readPassword("%s", txt)) != null) {
                return passwd;
            } else {
                return null;
            }
        } else {
            out.print(prompt);
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
    public NPrintStream getOut() {
        if (out == null) {
            NSystemTerminalBase p = getParent();
            if (p != null) {
                NPrintStream o = p.getOut();
                if(o!=null){
                    return outCache.get(o,getSession());
                }
            }
        }
        return this.out;
    }

    @Override
    public void setOut(NPrintStream out) {
        if (out != null) {
            out = out.setSession(session);
        }
        this.out = out;
    }

    @Override
    public NPrintStream getErr() {
        if (err == null) {
            NSystemTerminalBase p = getParent();
            if (p != null) {
                NPrintStream o = p.getErr();
                if(o!=null){
                    return errCache.get(o,getSession());
                }
            }
        }
        return this.err;
    }

    @Override
    public void setErr(NPrintStream err) {
        if (err != null) {
            err = err.setSession(session);
        }
        this.err = err;
    }

    @Override
    public NSessionTerminal copy() {
        final DefaultNSessionTerminalFromSystem r = new DefaultNSessionTerminalFromSystem(session, parent);
        r.setAll(this);
        return r;
    }

    @Override
    public <T> NAsk<T> ask() {
        return new DefaultNAsk<T>(ws, this, out())
                        .setSession(session)
                ;
    }

    @Override
    public InputStream in() {
        return getIn();
    }

    @Override
    public NPrintStream out() {
        return getOut();
    }

    @Override
    public NPrintStream err() {
        return getErr();
    }

    @Override
    public NSessionTerminal printProgress(float progress, NMsg message) {
        if (session.isProgress()) {
            if (getParent() instanceof NSystemTerminal) {
                ((NSystemTerminal) getParent()).printProgress(progress, message, session);
            } else {
                getProgressBar().printProgress(
                        Float.isNaN(progress) ? -1 :
                                (int) (progress * 100),
                        NTexts.of(session).ofText(message),
                        err()
                );
            }
        }
        return this;
    }

    public NSystemTerminalBase getParent() {
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
            progressBar = CProgressBar.of(session);
        }
        return progressBar;
    }

    protected void setAll(DefaultNSessionTerminalFromSystem other) {
        this.ws = other.ws;
        this.parent = other.parent;
        this.out = other.out;
        this.err = other.err;
        this.in = other.in;
        this.inReader = other.inReader;
    }

    @Override
    public NSession getSession() {
        return session;
    }
}
