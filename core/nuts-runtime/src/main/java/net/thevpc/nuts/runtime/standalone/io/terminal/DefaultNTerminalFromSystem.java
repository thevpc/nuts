package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.ask.DefaultNAsk;
import net.thevpc.nuts.runtime.standalone.time.CProgressBar;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NAsk;

import java.io.*;
import java.util.Scanner;

//@NutsPrototype
public class DefaultNTerminalFromSystem extends AbstractNTerminal {

    protected NPrintStream out;
    protected NPrintStream err;
    protected InputStream in;
    protected BufferedReader inReader;
    protected NSystemTerminalBase parent;
    protected CProgressBar progressBar;

    public DefaultNTerminalFromSystem(DefaultNTerminalFromSystem other) {
        this.parent = other.parent;
        this.in = other.in;
        this.inReader = other.inReader;
        setOut(other.out);
        setErr(other.err);
    }

    public DefaultNTerminalFromSystem(NSystemTerminalBase parent) {
        this.parent = parent;
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
    public String readLine(NPrintStream out, NMsg message) {
        if (out == null) {
            out = out();
        }
        if (out == null) {
            out = NIO.of().stdout();
        }
        if (this.in == null && parent != null) {
            if (this.out == null) {
                return parent.readLine(out, message);
            } else {
                return parent.readLine(out, message);
            }
        }
        out.print(message);
        out.flush();
        try {
            return getReader().readLine();
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    @Override
    public char[] readPassword(NPrintStream out, NMsg prompt) {
        if (out == null) {
            out = out();
        }
        if (out == null) {
            out = NIO.of().stdout();
        }

        if (this.in == null && parent != null) {
            if (this.out == null) {
                return parent.readPassword(out, prompt);
            } else {
                return parent.readPassword(out, prompt);
            }
        }

        InputStream in = getIn();
        Console cons = null;
        char[] passwd = null;
        if (in == null) {
            in = NIO.of().stdin();
        }
        if ((
                in == NIO.of().stdin()
        ) && ((cons = System.console()) != null)) {
            String txt = NText.of(prompt).toString();
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
                if (o != null) {
                    return o;
                }
            }
        }
        return this.out;
    }

    @Override
    public void setOut(NPrintStream out) {
        this.out = out;
    }

    @Override
    public NPrintStream getErr() {
        if (err == null) {
            NSystemTerminalBase p = getParent();
            if (p != null) {
                NPrintStream o = p.getErr();
                if (o != null) {
                    return o;
                }
            }
        }
        return this.err;
    }

    @Override
    public void setErr(NPrintStream err) {
        this.err = err;
    }

    @Override
    public NTerminal copy() {
        final DefaultNTerminalFromSystem r = new DefaultNTerminalFromSystem(parent);
        r.copyFrom(this);
        return r;
    }

    @Override
    public <T> NAsk<T> ask() {
        return new DefaultNAsk<T>(this, out());
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
    public NTerminal printProgress(float progress, NMsg message) {
        NSession session = NSession.of();
        if (session.isProgress()) {
            if (getParent() instanceof NSystemTerminal) {
                ((NSystemTerminal) getParent()).printProgress(progress, message);
            } else {
                getProgressBar().printProgress(
                        Float.isNaN(progress) ? -1 :
                                (int) (progress * 100),
                        NText.of(message),
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
            progressBar = CProgressBar.of();
        }
        return progressBar;
    }

    protected void copyFrom(DefaultNTerminalFromSystem other) {
        this.parent = other.parent;
        this.out = other.out;
        this.err = other.err;
        this.in = other.in;
        this.inReader = other.inReader;
    }

}
