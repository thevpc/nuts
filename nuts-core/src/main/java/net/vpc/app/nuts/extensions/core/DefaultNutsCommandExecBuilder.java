package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.terminals.DefaultNutsTerminal;
import net.vpc.app.nuts.extensions.terminals.NutsDefaultFormattedPrintStream;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.io.RuntimeIOException;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultNutsCommandExecBuilder implements NutsCommandExecBuilder {
    public static final Logger log = Logger.getLogger(DefaultNutsCommandExecBuilder.class.getName());
    private static final NutsDescriptor TEMP_DESC = new DefaultNutsDescriptorBuilder()
            .setId(CoreNutsUtils.parseNutsId("temp:exe#1.0"))
            .setExt("exe")
            .setPackaging("exe")
            .setExecutable(true)
            .setExecutor(new NutsExecutorDescriptor(CoreNutsUtils.parseNutsId("exec")))
            .build();

    private List<String> command;
    private List<String> executorOptions;
    private Properties env;
    private DefaultNutsWorkspace ws;
    private NutsSession session;
    private int result;
    private boolean executed;
    private String directory;
    private PrintStream out;
    private PrintStream err;
    private InputStream in;
    private boolean nativeCommand;
    private boolean redirectErrorStream;
    private boolean failFast;

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public NutsCommandExecBuilder setFailFast() {
        return setFailFast(true);
    }

    public NutsCommandExecBuilder setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    public DefaultNutsCommandExecBuilder(DefaultNutsWorkspace ws) {
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
    public NutsCommandExecBuilder addExecutorOptions(String... executorOptions) {
        if (this.executorOptions == null) {
            this.executorOptions = new ArrayList<>();
        }
        this.executorOptions.addAll(Arrays.asList(executorOptions));
        return this;
    }

    @Override
    public NutsCommandExecBuilder addExecutorOptions(List<String> executorOptions) {
        if (this.executorOptions == null) {
            this.executorOptions = new ArrayList<>();
        }
        this.executorOptions.addAll(executorOptions);
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
    public NutsCommandExecBuilder setExecutorOptions(String... options) {
        setExecutorOptions(Arrays.asList(options));
        return this;
    }

    @Override
    public NutsCommandExecBuilder setExecutorOptions(List<String> options) {
        this.executorOptions = options == null ? null : new ArrayList<>(options);
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
                this.env.putAll(env);
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
    public PrintStream getOut() {
        return out;
    }

    @Override
    public NutsCommandExecBuilder grabOutputString() {
        setOut(new SPrintStream2());
        return this;
    }

    @Override
    public NutsCommandExecBuilder grabErrorString() {
        setErr(new SPrintStream2());
        return this;
    }


    @Override
    public String getOutputString() {
        PrintStream o = getOut();
        if (o instanceof SPrintStream2) {
            return ((SPrintStream2) o).out.getStringBuffer();
        }
        throw new IllegalArgumentException("No Buffer was configured. Should call setOutString");
    }

    @Override
    public String getErrorString() {
        if (isRedirectErrorStream()) {
            return getOutputString();
        }
        PrintStream o = getErr();
        if (o instanceof SPrintStream2) {
            return ((SPrintStream2) o).out.getStringBuffer();
        }
        throw new IllegalArgumentException("No Buffer was configured. Should call setOutString");
    }

    @Override
    public NutsCommandExecBuilder setOut(PrintStream out) {
        this.out = (
                out == null ? null : ws.createPrintStream(out, true)
        );
        return this;
    }

    @Override
    public NutsCommandExecBuilder setErr(PrintStream err) {
        this.err = (
                err == null ? null : ws.createPrintStream(err, true)
        );
        return this;
    }

    @Override
    public PrintStream getErr() {
        return err;
    }


    @Override
    public NutsCommandExecBuilder exec() {
        executed = true;
        if (this.session == null) {
            this.session = ws.createSession();
        }
        DefaultNutsTerminal terminal = new DefaultNutsTerminal();
        PrintStream out2 = this.out != null ? this.out : session.getTerminal().getOut();
        terminal.install(ws,
                in != null ? in : session.getTerminal().getIn(),
                out2,
                isRedirectErrorStream() ? out2 : (err != null ? err : session.getTerminal().getErr())
        );
        String[] ts = command.toArray(new String[0]);
        if (nativeCommand) {
            Map<String, String> e2 = null;
            if (env != null) {
                e2 = new HashMap<>((Map) env);
            }
            result = CoreIOUtils.execAndWait(ws, ts,
                    e2,
                    directory == null ? null : new File(directory),
                    terminal, true, isFailFast());

        } else {
            result = execNuts(ts, executorOptions == null ? new String[0] : executorOptions.toArray(new String[0]), env, directory, isFailFast(), session.copy().setTerminal(terminal));
        }
        return this;
    }

    public boolean isRedirectErrorStream() {
        return redirectErrorStream;
    }


    @Override
    public NutsCommandExecBuilder setRedirectErrorStream() {
        return setRedirectErrorStream(true);
    }

    public NutsCommandExecBuilder setRedirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
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
        if (!executed) {
            exec();
        }
        return result;
    }

    private static class SPrintStream2 extends NutsDefaultFormattedPrintStream {
        private SPrintStream out;

        public SPrintStream2() {
            this(new SPrintStream());
        }

        public SPrintStream2(SPrintStream s) {
            super(s);
            this.out = s;
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

    public String getCommandString() {
        return getCommandString(null);
    }

    public String getCommandString(NutsCommandStringFormatter f) {
        StringBuilder sb = new StringBuilder();
        if (env != null) {
            for (Map.Entry<Object, Object> e : env.entrySet()) {
                String k = (String) e.getKey();
                String v = (String) e.getValue();
                if (f != null) {
                    if (!f.acceptEnvName(k, v)) {
                        continue;
                    }
                    String k2 = f.replaceEnvName(k, v);
                    if (k2 != null) {
                        k = k2;
                    }
                    String v2 = f.replaceEnvValue(k, v);
                    if (v2 != null) {
                        v = v2;
                    }
                }
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(enforceDoubleQuote(k)).append("=").append(enforceDoubleQuote(v));
            }
        }
        for (int i = 0; i < command.size(); i++) {
            String s = command.get(i);
            if (f != null) {
                if (!f.acceptArgument(i, s)) {
                    continue;
                }
                String k2 = f.replaceArgument(i, s);
                if (k2 != null) {
                    s = k2;
                }
            }
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(enforceDoubleQuote(s));
        }
        if (isRedirectErrorStream()) {
            if (out != null) {
                if (f == null || f.acceptRedirectOutput()) {
                    sb.append(" > ").append("{stream}");
                }
                if (f == null || f.acceptRedirectError()) {
                    sb.append(" 2>&1");
                }
            }
            if (in != null) {
                if (f == null || f.acceptRedirectInput()) {
                    sb.append(" < ").append("{stream}");
                }
            }
        } else {
            if (out != null) {
                if (f == null || f.acceptRedirectOutput()) {
                    sb.append(" > ").append("{stream}");
                }
            }
            if (err != null) {
                if (f == null || f.acceptRedirectError()) {
                    sb.append(" 2> ").append("{stream}");
                }
            }
            if (in != null) {
                if (f == null || f.acceptRedirectInput()) {
                    sb.append(" < ").append("{stream}");
                }
            }
        }
        return sb.toString();
    }

    public boolean isGrabOutputString() {
        return out instanceof SPrintStream2;
    }

    public boolean isGrabErrorString() {
        return err instanceof SPrintStream2;
    }

    private static String enforceDoubleQuote(String s) {
        if (s.isEmpty() || s.contains(" ") || s.contains("\"")) {
            s = "\"" + s.replace("\"", "\\\"") + "\"";
        }
        return s;
    }

    public int execNuts(String[] cmd, String[] executorOptions, Properties env, String dir, boolean failSafe, NutsSession session) {
        if (cmd == null || cmd.length == 0) {
            throw new NutsIllegalArgumentException("Missing command");
        }
        String[] args = new String[cmd.length - 1];
        System.arraycopy(cmd, 1, args, 0, args.length);
        String cmdName = cmd[0];
        ws.getSecurityManager().checkAllowed(NutsConstants.RIGHT_EXEC, cmdName);
        int result = 0;
        if (cmdName.contains("/") || cmdName.contains("\\")) {
            try (CharacterizedFile c = CoreNutsUtils.characterize(ws, IOUtils.toInputStreamSource(cmdName, "path", cmdName, new File(ws.getConfigManager().getCwd())), session)) {
                if (c.descriptor == null) {
                    //this is a native file?
                    c.descriptor = TEMP_DESC;
                }
                NutsDefinition nutToRun = new NutsDefinition(
                        c.descriptor.getId(),
                        c.descriptor,
                        ((File) c.contentFile.getSource()).getPath(),
                        false,
                        c.temps.size() > 0,
                        null,null
                );
                result = ws.exec(nutToRun, cmdName, args, executorOptions, env, dir, failSafe, session);
            }
        } else if(cmdName.contains(":")){
            NutsDefinition nutToRun = null;
            nutToRun = ws.fetchWithDependencies(cmdName, null, false, session);
            if (!ws.isInstalled(nutToRun.getId(), true, session)) {
                ws.getSecurityManager().checkAllowed(NutsConstants.RIGHT_AUTO_INSTALL, cmdName);
                ws.install(nutToRun.getId().toString(), args, NutsConfirmAction.FORCE, session);
            }
            result = ws.exec(nutToRun, cmdName, args, executorOptions, env, dir, failSafe, session);
        } else {
            NutsWorkspaceCommand command = ws.getConfigManager().findCommand(cmdName);
            NutsDefinition nutToRun = null;
            if (command == null) {
                nutToRun = ws.fetch(cmdName, session);
                log.log(Level.FINE, "Command {0} not found. Trying to resolve command as valid Nuts Id.", new Object[]{cmdName});
                if (!ws.isInstalled(nutToRun.getId(), false, session)) {
                    ws.getSecurityManager().checkAllowed(NutsConstants.RIGHT_AUTO_INSTALL, cmdName);
                    ws.install(nutToRun.getId(), args, NutsConfirmAction.FORCE, session);
                }
            } else {
                if(command.getId()==null) {
                    throw new NutsExecutionException("Invalid Command Definition "+command,1);
                }
                nutToRun = ws.fetch(command.getId(), session);
                List<String> r=new ArrayList<>(Arrays.asList(command.getCommand()));
                r.addAll(Arrays.asList(args));
                args=r.toArray(new String[0]);
            }
            //load all needed dependencies!
            result = ws.exec(nutToRun, cmdName, args, executorOptions, env, dir, failSafe, session);
        }

        if (result != 0) {
            if (isFailFast()) {
                if (isRedirectErrorStream()) {
                    if (isGrabOutputString()) {
                        throw new RuntimeIOException("Execution Failed with code " + result + " and message : " + getOutputString());
                    }
                } else {
                    if (isGrabErrorString()) {
                        throw new RuntimeIOException("Execution Failed with code " + result + " and message : " + getErrorString());
                    }
                    if (isGrabOutputString()) {
                        throw new RuntimeIOException("Execution Failed with code " + result + " and message : " + getOutputString());
                    }
                }
                throw new RuntimeIOException("Execution Failed with code " + result);
            }
        }
        return result;
    }


}
