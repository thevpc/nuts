package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.terminals.DefaultNutsTerminal;
import net.vpc.app.nuts.extensions.terminals.NutsDefaultFormattedPrintStream;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.common.io.RuntimeIOException;

import java.io.*;
import java.util.*;

public class DefaultNutsCommandExecBuilder implements NutsCommandExecBuilder {
    private List<String> command;
    private Properties env;
    private NutsWorkspace ws;
    private NutsSession session;
    private int result;
    private String directory;
    private NutsPrintStream out;
    private NutsPrintStream err;
    private InputStream in;
    private boolean nativeCommand;

    public DefaultNutsCommandExecBuilder(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsCommandExecBuilder setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public List<String> getCommand() {
        return command;
    }

    @Override
    public NutsCommandExecBuilder addCommand(String... command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        this.command.addAll(Arrays.asList(command));
        return this;
    }

    @Override
    public NutsCommandExecBuilder addCommand(List<String> command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        this.command.addAll(command);
        return this;
    }

    @Override
    public NutsCommandExecBuilder setCommand(String... command) {
        setCommand(Arrays.asList(command));
        return this;
    }

    @Override
    public NutsCommandExecBuilder setCommand(List<String> command) {
        this.command = command == null ? null : new ArrayList<>(command);
        return this;
    }

    @Override
    public Properties getEnv() {
        return env;
    }

    @Override
    public NutsCommandExecBuilder addEnv(Map<String, String> env) {
        if (env != null) {
            if (this.env == null) {
                this.env = new Properties();
                env.putAll(env);
            } else {
                this.env.putAll(env);
            }
        }
        return this;
    }


    @Override
    public NutsCommandExecBuilder setEnv(String k, String val) {
        if (env == null) {
            env = new Properties();
        }
        env.put(k, val);
        return this;
    }

    @Override
    public NutsCommandExecBuilder setEnv(Properties env) {
        this.env = env == null ? null : new Properties();
        if (env != null) {
            this.env.putAll(env);
        }
        return this;
    }

    @Override
    public NutsCommandExecBuilder setEnv(Map<String, String> env) {
        this.env = env == null ? null : new Properties();
        if (env != null) {
            this.env.putAll(env);
        }
        return this;
    }

    @Override
    public String getDirectory() {
        return directory;
    }

    @Override
    public NutsCommandExecBuilder setDirectory(String directory) {
        this.directory = directory;
        return this;
    }

    @Override
    public InputStream getIn() {
        return in;
    }

    @Override
    public NutsCommandExecBuilder setIn(InputStream in) {
        this.in = in;
        return this;
    }

    @Override
    public NutsPrintStream getOut() {
        return out;
    }

    @Override
    public NutsCommandExecBuilder setOutAndErrStringBuffer() {
        setOut(new SPrintStream2());
        setErr(getOut());
        return this;
    }

    @Override
    public NutsCommandExecBuilder setOutStringBuffer() {
        setOut(new SPrintStream2());
        return this;
    }

    @Override
    public NutsCommandExecBuilder setErrStringBuffer() {
        setOut(new SPrintStream2());
        return this;
    }

    @Override
    public String getOutString() {
        NutsPrintStream o = getOut();
        if (o instanceof SPrintStream2) {
            return ((SPrintStream2) o).out.getStringBuffer();
        }
        throw new IllegalArgumentException("No Buffer was configured. Should call setOutString");
    }

    @Override
    public String getErrString() {
        NutsPrintStream o = getErr();
        if (o instanceof SPrintStream2) {
            return ((SPrintStream2) o).out.getStringBuffer();
        }
        throw new IllegalArgumentException("No Buffer was configured. Should call setOutString");
    }

    @Override
    public NutsCommandExecBuilder setOut(NutsPrintStream out) {
        this.out = out;
        return this;
    }

    @Override
    public NutsCommandExecBuilder setOut(PrintStream out) {
        return setOut(
                out == null ? null : ws.getExtensionManager().createPrintStream(out, true)
        );
    }

    @Override
    public NutsCommandExecBuilder setErr(PrintStream err) {
        return setErr(
                err == null ? null : ws.getExtensionManager().createPrintStream(err, true)
        );
    }

    @Override
    public NutsPrintStream getErr() {
        return err;
    }

    @Override
    public NutsCommandExecBuilder setErr(NutsPrintStream err) {
        this.err = err;
        return this;
    }

    @Override
    public NutsCommandExecBuilder exec() {
        if (this.session == null) {
            this.session = ws.createSession();
        }
        DefaultNutsTerminal terminal = new DefaultNutsTerminal();
        terminal.setIn(in != null ? in : session.getTerminal().getIn());
        terminal.setOut(out != null ? out : session.getTerminal().getOut());
        terminal.setErr(err != null ? err : session.getTerminal().getErr());
        String[] ts = command.toArray(new String[command.size()]);
        if (nativeCommand) {
            Map<String, String> e2 = null;
            if (env != null) {
                e2 = new HashMap<>();
                e2.putAll((Map) env);
            }
            try {
                result = CoreIOUtils.execAndWait(ts,
                        e2,
                        directory == null ? null : new File(directory),
                        terminal, true);
            } catch (InterruptedException e) {
                throw new RuntimeIOException(e.toString());
            } catch (IOException e) {
                throw new RuntimeIOException(e);
            }

        } else {
            result = ws.exec(ts, env, directory, session);
        }
        return this;
    }

    @Override
    public boolean isNativeCommand() {
        return nativeCommand;
    }

    @Override
    public NutsCommandExecBuilder setNativeCommand(boolean nativeCommand) {
        this.nativeCommand = nativeCommand;
        return this;
    }

    @Override
    public int getResult() {
        return result;
    }

    private static class SPrintStream2 extends NutsDefaultFormattedPrintStream {
        private SPrintStream out;

        public SPrintStream2() {
            this(new SPrintStream());
        }

        public SPrintStream2(SPrintStream s) {
            super(s);
            this.out=s;
        }
    }

    private static class SPrintStream extends PrintStream {
        private ByteArrayOutputStream out;

        public SPrintStream() {
            this(new ByteArrayOutputStream());
        }

        public SPrintStream(ByteArrayOutputStream out1) {
            super(out1);
            this.out = out1;
        }

        public String getStringBuffer() {
            flush();
            return new String(out.toByteArray());
        }
    }
}
