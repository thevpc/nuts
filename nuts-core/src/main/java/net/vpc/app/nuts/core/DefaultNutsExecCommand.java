package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.util.CharacterizedFile;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.terminals.NutsDefaultFormattedPrintStream;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.executors.CustomNutsExecutorComponent;
import net.vpc.app.nuts.core.executors.JavaExecutorOptions;

import net.vpc.app.nuts.core.terminals.DefaultNutsSessionTerminal;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

public class DefaultNutsExecCommand implements NutsExecCommand {

    public static final Logger log = Logger.getLogger(DefaultNutsExecCommand.class.getName());
    private static final NutsDescriptor TEMP_DESC = new DefaultNutsDescriptorBuilder()
            .setId(CoreNutsUtils.parseNutsId("temp:exe#1.0"))
            .setPackaging("exe")
            .setExecutable(true)
            .setExecutor(new NutsExecutorDescriptor(CoreNutsUtils.parseNutsId("exec")))
            .build();

    private List<String> command;
    private List<String> executorOptions;
    private Properties env;
    private DefaultNutsWorkspace ws;
    private NutsSession session;
    private NutsExecutionException result;
    private boolean executed;
    private boolean ask = true;
    private String directory;
    private PrintStream out;
    private PrintStream err;
    private InputStream in;
    private NutsExecutionType executionType = NutsExecutionType.SPAWN;
    private boolean redirectErrorStream;
    private boolean failFast;
    private NutsCommandStringFormatter commandStringFormatter;

    public DefaultNutsExecCommand(DefaultNutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsCommandStringFormatter getCommandStringFormatter() {
        return commandStringFormatter;
    }

    @Override
    public NutsExecCommand setCommandStringFormatter(NutsCommandStringFormatter commandStringFormatter) {
        this.commandStringFormatter = commandStringFormatter;
        return this;
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public NutsExecCommand failFast() {
        return setFailFast(true);
    }

    @Override
    public NutsExecCommand setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public NutsExecCommand failFast(boolean failFast) {
        return setFailFast(failFast);
    }

    public boolean isAsk() {
        return ask;
    }

    public NutsExecCommand setAsk(boolean ask) {
        this.ask = ask;
        return this;
    }

    public NutsExecCommand ask(boolean ask) {
        return setAsk(true);
    }

    public NutsExecCommand ask() {
        return ask(true);
    }

    @Override
    public NutsExecCommand clearCommand() {
        this.command = null;
        return this;
    }

    @Override
    public NutsExecCommand executorOption(String executorOption) {
        return addExecutorOption(executorOption);
    }

    @Override
    public NutsExecCommand addExecutorOption(String executorOption) {
        if (executorOption != null) {
            if (this.executorOptions == null) {
                this.executorOptions = new ArrayList<>();
            }
            this.executorOptions.add(executorOption);
        }
        return this;
    }

    @Override
    public NutsExecCommand clearExecutorOptions() {
        this.executorOptions = null;
        return this;
    }

    @Override
    public NutsExecCommand clearEnv() {
        this.env = null;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsExecCommand setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsExecCommand session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public String[] getCommand() {
        return command.toArray(new String[0]);
    }

    @Override
    public String[] getExecutorOptions() {
        return executorOptions.toArray(new String[0]);
    }

    @Override
    public NutsExecCommand command(String... command) {
        return addCommand(command);
    }

    @Override
    public NutsExecCommand command(Collection<String> command) {
        return addCommand(command);
    }

    @Override
    public NutsExecCommand addCommand(String... command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        this.command.addAll(Arrays.asList(command));
        return this;
    }

    @Override
    public NutsExecCommand addCommand(Collection<String> command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        this.command.addAll(command);
        return this;
    }

    @Override
    public NutsExecCommand executorOptions(String... executorOptions) {
        return addExecutorOptions(executorOptions);
    }

    @Override
    public NutsExecCommand addExecutorOptions(String... executorOptions) {
        if (executorOptions != null) {
            for (String executorOption : executorOptions) {
                addExecutorOption(executorOption);
            }
        }
        return this;
    }

    @Override
    public NutsExecCommand executorOptions(Collection<String> executorOptions) {
        return addExecutorOptions(executorOptions);
    }

    @Override
    public NutsExecCommand addExecutorOptions(Collection<String> executorOptions) {
        if (executorOptions != null) {
            for (String executorOption : executorOptions) {
                addExecutorOption(executorOption);
            }
        }
        return this;
    }

    @Override
    public Properties getEnv() {
        return env;
    }

    @Override
    public NutsExecCommand addEnv(Properties env) {
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
    public NutsExecCommand addEnv(Map<String, String> env) {
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
    public NutsExecCommand env(Map<String, String> env) {
        return setEnv(env);
    }

    @Override
    public NutsExecCommand env(String k, String val) {
        return setEnv(k, val);
    }

    @Override
    public NutsExecCommand setEnv(String k, String val) {
        if (env == null) {
            env = new Properties();
        }
        env.put(k, val);
        return this;
    }

    @Override
    public NutsExecCommand env(Properties env) {
        return setEnv(env);
    }

    @Override
    public NutsExecCommand setEnv(Properties env) {
        this.env = env == null ? null : new Properties();
        if (env != null) {
            this.env.putAll(env);
        }
        return this;
    }

    @Override
    public NutsExecCommand setEnv(Map<String, String> env) {
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
    public NutsExecCommand directory(String directory) {
        return setDirectory(directory);
    }

    @Override
    public NutsExecCommand setDirectory(String directory) {
        this.directory = directory;
        return this;
    }

    @Override
    public InputStream getIn() {
        return in;
    }

    @Override
    public InputStream in() {
        return getIn();
    }

    @Override
    public NutsExecCommand in(InputStream in) {
        return setIn(in);
    }

    @Override
    public NutsExecCommand setIn(InputStream in) {
        this.in = in;
        return this;
    }

    @Override
    public PrintStream getOut() {
        return out;
    }

    @Override
    public NutsExecCommand grabOutputString() {
        setOut(new SPrintStream2());
        return this;
    }

    @Override
    public NutsExecCommand grabErrorString() {
        setErr(new SPrintStream2());
        return this;
    }

    @Override
    public String getOutputString() {
        PrintStream o = getOut();
        if (o instanceof SPrintStream2) {
            return ((SPrintStream2) o).out.getStringBuffer();
        }
        throw new NutsIllegalArgumentException("No Buffer was configured. Should call setOutString");
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
        throw new NutsIllegalArgumentException("No Buffer was configured. Should call setOutString");
    }

    @Override
    public NutsExecCommand setOut(PrintStream out) {
        this.out = (out == null ? null : ws.io().createPrintStream(out, NutsTerminalMode.FORMATTED));
        return this;
    }

    @Override
    public NutsExecCommand setErr(PrintStream err) {
        this.err = (err == null ? null : ws.io().createPrintStream(err, NutsTerminalMode.FORMATTED));
        return this;
    }

    @Override
    public PrintStream getErr() {
        return err;
    }

    @Override
    public NutsExecCommand exec() {
        NutsExecutableImpl exec = (NutsExecutableImpl) which();
        executed = true;
        try {
            exec.execute();
        } catch (NutsExecutionException ex) {
            result = ex;
        } catch (Exception ex) {
            result = new NutsExecutionException(ex, 244);
        }
        if (result != null) {
            throw result;
//            checkFailFast(result.getExitCode());
        }
        return this;
    }

    @Override
    public NutsExecutableInfo which() {
        if (this.session == null) {
            this.session = ws.createSession();
        }
        DefaultNutsSessionTerminal terminal = new DefaultNutsSessionTerminal();
        terminal.install(ws);
        terminal.setParent(session.getTerminal());
        if (this.in != null) {
            terminal.setIn(this.in);
        }
        if (this.out != null) {
            terminal.setOut(this.out);
        }
        if (isRedirectErrorStream()) {
            if (this.out != null) {
                terminal.setErr(this.out);
            } else {
                terminal.setErr(session.getTerminal().getOut());
            }
        }
        terminal.getOut().flush();
        terminal.getErr().flush();
        String[] ts = command.toArray(new String[0]);
        NutsExecutableImpl exec = null;
        switch (executionType) {
            case SYSCALL: {
                exec = new SystemExecutable(ts, executorOptions == null ? new String[0] : executorOptions.toArray(new String[0]), session.copy().setTerminal(terminal));
                break;
            }
            case SPAWN: {
                exec = execEmbeddedOrExternal(ts, executorOptions == null ? new String[0] : executorOptions.toArray(new String[0]), session.copy().setTerminal(terminal), false);
                break;
            }
            case EMBEDDED: {
                exec = execEmbeddedOrExternal(ts, executorOptions == null ? new String[0] : executorOptions.toArray(new String[0]), session.copy().setTerminal(terminal), true);
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException("Invalid executionType " + executionType);
            }
        }
        return exec;
    }

    public boolean isRedirectErrorStream() {
        return redirectErrorStream;
    }

    @Override
    public NutsExecCommand redirectErrorStream() {
        return setRedirectErrorStream(true);
    }

    public NutsExecCommand setRedirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    @Override
    public NutsExecCommand copyFrom(NutsExecCommand other) {
        addCommand(other.getCommand());
        addEnv(other.getEnv());
        addExecutorOptions(other.getExecutorOptions());
        setAsk(other.isAsk());
        setDirectory(other.getDirectory());
        setIn(other.getIn());
        setOut(other.getOut());
        setErr(other.getErr());
        setRedirectErrorStream(other.isRedirectErrorStream());
        setSession(other.getSession());
        setFailFast(other.isFailFast());
        setExecutionType(other.getExecutionType());
        return this;
    }

    @Override
    public NutsExecCommand copy() {
        return ws.exec().copyFrom(this);
    }

    @Override
    public NutsExecCommand parseOptions(String... args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                if (command.isEmpty()) {
                    executorOptions.add(arg);
                } else {
                    throw new NutsIllegalArgumentException("Unexpected option here");
                }
            } else {
                command.add(arg);
            }
        }
        return this;
    }

    @Override
    public NutsExecutionType getExecutionType() {
        return executionType;
    }

    @Override
    public NutsExecCommand setExecutionType(NutsExecutionType executionType) {
        if (executionType == null) {
            executionType = NutsExecutionType.SPAWN;
        }
        this.executionType = executionType;
        return this;
    }

    @Override
    public int getResult() {
        if (!executed) {
            try {
                exec();
            } catch (Exception ex) {
                // ignore;
            }
        }
        return result == null ? 0 : result.getExitCode();
    }

    @Override
    public NutsExecutionException getResultException() {
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

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsId nutsId) {
        for (NutsExecutorComponent nutsExecutorComponent : ws.extensions().createAll(NutsExecutorComponent.class)) {
            if (nutsExecutorComponent.getId().equalsSimpleName(nutsId)
                    || nutsExecutorComponent.getId().getName().equals(nutsId.toString())
                    || nutsExecutorComponent.getId().toString().equals("net.vpc.app.nuts.exec:exec-" + nutsId.toString())) {
                return nutsExecutorComponent;
            }
        }
        return new CustomNutsExecutorComponent(nutsId);
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsDefinition nutsDefinition) {
        NutsExecutorComponent executorComponent = ws.extensions().createSupported(NutsExecutorComponent.class, nutsDefinition);
        if (executorComponent != null) {
            return executorComponent;
        }
        throw new NutsExecutorNotFoundException(nutsDefinition.getId());
    }

    @Override
    public PrintStream out() {
        return getOut();
    }

    @Override
    public NutsExecCommand out(PrintStream out) {
        return setOut(out);
    }

    @Override
    public NutsExecCommand err(PrintStream err) {
        return setErr(err);
    }

    @Override
    public PrintStream err() {
        return getErr();
    }

    @Override
    public NutsExecCommand redirectErrorStream(boolean redirectErrorStream) {
        return setRedirectErrorStream(redirectErrorStream);
    }

    @Override
    public NutsExecCommand executionType(NutsExecutionType executionType) {
        return setExecutionType(executionType);
    }

    @Override
    public NutsExecCommand spawn() {
        return setExecutionType(NutsExecutionType.SPAWN);
    }

    @Override
    public NutsExecCommand embedded() {
        return setExecutionType(NutsExecutionType.EMBEDDED);
    }

    @Override
    public NutsExecCommand syscall() {
        return setExecutionType(NutsExecutionType.SYSCALL);
    }

    @Override
    public String getCommandString() {
        NutsCommandStringFormatter f=getCommandStringFormatter();
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

    public NutsExecutableImpl execExternal(String[] cmd, String[] executorOptions, NutsSession session) {
        return execEmbeddedOrExternal(cmd, executorOptions, session, false);
    }

    public NutsExecutableImpl execEmbedded(String[] cmd, String[] executorOptions, NutsSession session) {
        return execEmbeddedOrExternal(cmd, executorOptions, session, true);
    }

    private NutsExecutableImpl execEmbeddedOrExternal(String[] cmd, String[] executorOptions, NutsSession session, boolean embedded) {
        if (cmd == null || cmd.length == 0) {
            throw new NutsIllegalArgumentException("Missing command");
        }
        String[] args = new String[cmd.length - 1];
        System.arraycopy(cmd, 1, args, 0, args.length);
        String cmdName = cmd[0];
        //resolve internal commands!
        switch (cmdName) {
            case "update": {
                return new InternalExecutable(cmdName, args) {
                    @Override
                    public void execute() {
                        ws.update().parseOptions(args)
                                .update();
                    }
                };
            }
            case "check-updates": {
                return new InternalExecutable(cmdName, args) {
                    @Override
                    public void execute() {
                        ws.update().parseOptions(args)
                                .checkUpdates();
                    }
                };
            }
            case "install": {
                return new InternalExecutable(cmdName, args) {
                    @Override
                    public void execute() {
                        ws.install().parseOptions(args).install();
                    }
                };
            }
            case "uninstall": {
                return new InternalExecutable(cmdName, args) {
                    @Override
                    public void execute() {
                        ws.uninstall().parseOptions(args).uninstall();
                    }
                };
            }
            case "version": {
                return new InternalExecutable(cmdName, args) {
                    @Override
                    public void execute() {
                        PrintStream out = session.getTerminal().getFormattedOut();
                        ws.formatter().createWorkspaceVersionFormat()
                                .parseOptions(args)
                                .println(out);
                    }
                };
            }
            case "license": {
                return new InternalExecutable(cmdName, args) {
                    @Override
                    public void execute() {
                        session.getTerminal().getFormattedOut().println(ws.getLicenseText());
                    }
                };
            }
            case "help": {
                return new InternalExecutable(cmdName, args) {
                    @Override
                    public void execute() {
                        if (args.length == 0) {
                            session.getTerminal().fout().println(ws.getHelpText());
                        }
                        for (String arg : args) {
                            NutsExecutableInfo w=null;
                            try {
                                w = ws.exec().command(arg).which();
                                
                            } catch (Exception ex) {
                            }
                            if(w!=null){
                                
                                session.getTerminal().fout().println(arg+" :");
                                session.getTerminal().fout().println(w.getHelpText());
                            }else{
                                session.getTerminal().ferr().println(arg+" : Not found");
                            }
                        }
                    }
                };
            }
            case "info": {
                return new InternalExecutable(cmdName, args) {
                    @Override
                    public void execute() {
                        PrintStream out = session.getTerminal().getFormattedOut();
                        ws.formatter().createWorkspaceInfoFormat()
                                .parseOptions(args)
                                .println(out);
                    }
                };
            }
            case "exec": {
                return new InternalExecutable(cmdName, args) {
                    @Override
                    public void execute() {
                        DefaultNutsExecCommand.this.copy()
                                .clearCommand()
                                .parseOptions(args)
                                .failFast()
                                .exec();
                    }
                };
            }
        }
        if (cmdName.contains("/") || cmdName.contains("\\")) {
            return new PathComponentExecutable(cmdName, args, executorOptions, embedded);
        } else if (cmdName.contains(":")) {
            return ws_exec(cmdName, args, executorOptions, env, directory, failFast, session, embedded);
        } else {
            NutsWorkspaceCommand command = null;
            command = ws.config().findCommand(cmdName);
            if (command != null) {
                NutsCommandExecOptions o = new NutsCommandExecOptions().setExecutorOptions(executorOptions).setDirectory(directory).setFailFast(failFast)
                        .setExecutionType(embedded ? NutsExecutionType.EMBEDDED : NutsExecutionType.SPAWN).setEnv(env);
                return new AliasExecutable(command, o, session, args);
            } else {
                return ws_exec(cmdName, args, executorOptions, env, directory, failFast, session, embedded);
            }
        }
    }

//    private void checkFailFast(int result) {
//        if (result != 0) {
//            if (isFailFast()) {
//                if (isRedirectErrorStream()) {
//                    if (isGrabOutputString()) {
//                        throw new UncheckedIOException(new IOException("Execution Failed with code " + result + " and message : " + getOutputString()));
//                    }
//                } else {
//                    if (isGrabErrorString()) {
//                        throw new UncheckedIOException(new IOException("Execution Failed with code " + result + " and message : " + getErrorString()));
//                    }
//                    if (isGrabOutputString()) {
//                        throw new UncheckedIOException(new IOException("Execution Failed with code " + result + " and message : " + getOutputString()));
//                    }
//                }
//                throw new UncheckedIOException(new IOException("Execution Failed with code " + result));
//            }
//        }
//    }
    protected NutsExecutableImpl ws_exec(String commandName, String[] appArgs, String[] executorOptions, Properties env, String dir, boolean failFast, NutsSession session, boolean embedded) {
        NutsDefinition def = null;
        NutsId nid = ws.parser().parseId(commandName);
        def = ws.fetch().id(nid).session(session).setAcceptOptional(false).includeDependencies().setLenient(true).installed().getResultDefinition();
        if (def == null) {
            def = ws.fetch().id(nid).session(session).setAcceptOptional(false).includeDependencies().setLenient(false).wired().getResultDefinition();
        }
        return new ComponentExecutable(def, commandName, appArgs, executorOptions, env, dir, failFast, session, embedded);
    }

    protected void ws_exec(NutsDefinition nutToRun, String commandName, String[] appArgs, String[] executorOptions, Properties env, String dir, boolean failFast, NutsSession session, boolean embedded) {
        ws.security().checkAllowed(NutsConstants.Rights.EXEC, commandName);
        session = NutsWorkspaceUtils.validateSession(ws, session);
        if (nutToRun != null && nutToRun.getPath() != null) {
            NutsDescriptor descriptor = nutToRun.getDescriptor();
            if (!descriptor.isExecutable()) {
//                session.getTerminal().getErr().println(nutToRun.getId()+" is not executable... will perform extra checks.");
//                throw new NutsNotExecutableException(descriptor.getId());
            }
            if (!embedded) {
                NutsExecutorDescriptor executor = descriptor.getExecutor();
                NutsExecutorComponent execComponent = null;
                List<String> executorArgs = new ArrayList<>();
                Properties execProps = null;
                if (executor == null) {
                    execComponent = resolveNutsExecutorComponent(nutToRun);
                } else {
                    if (executor.getId() == null) {
                        execComponent = resolveNutsExecutorComponent(nutToRun);
                    } else {
                        execComponent = resolveNutsExecutorComponent(executor.getId());
                    }
                    executorArgs.addAll(Arrays.asList(executor.getOptions()));
                    execProps = executor.getProperties();
                }
                executorArgs.addAll(Arrays.asList(executorOptions));
                final NutsExecutionContext executionContext = new NutsExecutionContextImpl(nutToRun,
                        appArgs, executorArgs.toArray(new String[0]),
                        env, execProps, dir, session, ws, true, commandName);
                execComponent.exec(executionContext);
                return;
            } else {
                JavaExecutorOptions options = new JavaExecutorOptions(
                        nutToRun, appArgs, executorOptions, dir, ws, session
                );
                ClassLoader classLoader = null;
                Throwable th = null;
                try {
                    classLoader = new NutsURLClassLoader(
                            ws,
                            options.getClassPath().toArray(new String[0]),
                            ws.config().getBootClassLoader()
                    );
                    Class<?> cls = Class.forName(options.getMainClass(), true, classLoader);
                    boolean isNutsApp = false;
                    Method mainMethod = null;
                    Object nutsApp = null;
                    try {
                        mainMethod = cls.getMethod("run", NutsWorkspace.class, String[].class);
                        mainMethod.setAccessible(true);
                        Class p = cls.getSuperclass();
                        while (p != null) {
                            if (p.getName().equals("net.vpc.app.nuts.app.NutsApplication")) {
                                isNutsApp = true;
                                break;
                            }
                            p = p.getSuperclass();
                        }
                        if (isNutsApp) {
                            isNutsApp = false;
                            nutsApp = cls.getConstructor().newInstance();
                            isNutsApp = true;
                        }
                    } catch (Exception rr) {
                        //ignore

                    }
                    if (isNutsApp) {
                        //NutsWorkspace
                        mainMethod.invoke(nutsApp, new Object[]{ws, options.getApp().toArray(new String[0])});
                    } else {
                        //NutsWorkspace
                        System.setProperty("nuts.boot.args", ws.config().getOptions().getExportedBootArgumentsString());
                        mainMethod = cls.getMethod("main", String[].class);
                        List<String> nargs = new ArrayList<>();
                        nargs.addAll(options.getApp());
                        mainMethod.invoke(null, new Object[]{options.getApp().toArray(new String[0])});
                    }
                    return;
                } catch (MalformedURLException | NoSuchMethodException | SecurityException
                        | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | ClassNotFoundException e) {
                    th = e;
                }
                if (th != null) {
                    throw new NutsExecutionException("Error Executing " + nutToRun.getId(), th);
                }
            }
        }
        throw new NutsNotFoundException(nutToRun == null ? null : nutToRun.getId());
    }

    public static interface NutsExecutableImpl extends NutsExecutableInfo {

        void execute();
    }

    public abstract class AbstractExecutable implements NutsExecutableImpl {

        private NutsExecutableType type;
        private String name;

        public AbstractExecutable(String name, NutsExecutableType type) {
            this.type = type;
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public NutsExecutableType getType() {
            return type;
        }

        @Override
        public String getDescription() {
            return toString();
        }

        @Override
        public String getHelpText() {
            return "No help available. Try '" + getName() + " --help'";
        }

    }

    public class AliasExecutable extends AbstractExecutable {

        private NutsWorkspaceCommand command;
        private NutsCommandExecOptions o;
        private NutsSession session;
        private String[] args;

        public AliasExecutable(NutsWorkspaceCommand command, NutsCommandExecOptions o, NutsSession session, String[] args) {
            super(command.getName(), NutsExecutableType.ALIAS);
            this.command = command;
            this.o = o;
            this.session = session;
            this.args = args;
        }

        @Override
        public NutsId getId() {
            return command.getOwner();
        }

        public void execute() {
            command.exec(args, o, session);
        }

        @Override
        public String getHelpText() {
            String t = command.getHelpText();
            if (t != null) {
                return t;
            }
            return "No help available. Try '" + getName() + " --help'";
        }

        @Override
        public String toString() {
            return "CMD " + command.getName() + " @ " + command.getOwner();
        }

    }

    public class PathComponentExecutable extends AbstractExecutable {

        private String cmdName;
        private String[] args;
        private String[] executorOptions;
        private boolean embedded;

        public PathComponentExecutable(String cmdName, String[] args, String[] executorOptions, boolean embedded) {
            super(cmdName, NutsExecutableType.COMPONENT);
            this.cmdName = cmdName;
            this.args = args;
            this.executorOptions = executorOptions;
            this.embedded = embedded;
        }

        @Override
        public NutsId getId() {
            NutsFetchCommand p = ws.fetch();
            p.setTransitive(true);
            try (CharacterizedFile c = CoreIOUtils.characterize(ws, CoreIOUtils.createInputSource(cmdName), p, session)) {
                return c.descriptor.getId();
            }
        }

        @Override
        public void execute() {
            NutsFetchCommand p = ws.fetch();
            p.setTransitive(true);

            try (CharacterizedFile c = CoreIOUtils.characterize(ws, CoreIOUtils.createInputSource(cmdName), p, session)) {
                if (c.descriptor == null) {
                    //this is a native file?
                    c.descriptor = TEMP_DESC;
                }
                NutsDefinition nutToRun = new DefaultNutsDefinition(
                        ws, null,
                        c.descriptor.getId(),
                        c.descriptor,
                        new NutsContent(c.getContentPath(), false, c.temps.size() > 0),
                        null
                );
                ws_exec(nutToRun, cmdName, args, executorOptions, env, directory, failFast, session, embedded);
            }
        }

        @Override
        public String toString() {
            return "NUTS " + cmdName + " " + NutsMinimalCommandLine.escapeArguments(args);
        }

    }

    public class ComponentExecutable extends AbstractExecutable {

        private NutsDefinition def;
        private String commandName;
        private String[] appArgs;
        private String[] executorOptions;
        private Properties env;
        private String dir;
        private boolean failFast;
        private NutsSession session;
        private boolean embedded;

        public ComponentExecutable(NutsDefinition def, String commandName, String[] appArgs, String[] executorOptions, Properties env, String dir, boolean failFast, NutsSession session, boolean embedded) {
            super(commandName, NutsExecutableType.COMPONENT);
            this.def = def;
            this.commandName = commandName;
            this.appArgs = appArgs;
            this.executorOptions = executorOptions;
            this.env = env;
            this.dir = dir;
            this.failFast = failFast;
            this.session = session;
            this.embedded = embedded;
        }

        @Override
        public NutsId getId() {
            return def.getId();
        }

        @Override
        public void execute() {
            if (!def.getInstallation().isInstalled()) {
                ws.security().checkAllowed(NutsConstants.Rights.AUTO_INSTALL, commandName);
                if (session.getTerminal().ask(NutsQuestion.forBoolean("==%s== is not yet installed. Do you want to proceed", def.getId().getLongName()).defautValue(true))) {
                    ws.install().id(def.getId()).args(appArgs).setForce(true).setSession(session).install();
                } else {
                    throw new NutsUserCancelException();
                }
            }
            ws_exec(def, commandName, appArgs, executorOptions, env, directory, failFast, session, embedded);
        }

        @Override
        public String toString() {
            return "NUTS " + getId().toString() + " " + NutsMinimalCommandLine.escapeArguments(appArgs);
        }
    }

    public abstract class InternalExecutable extends AbstractExecutable {

        protected String[] args;

        public InternalExecutable(String name, String[] args) {
            super(name, NutsExecutableType.INTERNAL);
            this.args = args;
        }

        @Override
        public NutsId getId() {
            return null;
        }

        @Override
        public String getHelpText() {
            return getName() + " is an internal command. Help is accessible via 'nuts help'";
        }
    }

    public class SystemExecutable extends AbstractExecutable {

        private String[] cmd;
        private String[] executorOptions;
        private NutsSession session;

        public SystemExecutable(String[] cmd, String[] executorOptions, NutsSession session) {
            super(cmd[0], NutsExecutableType.SYSTEM);
            this.cmd = cmd;
            this.executorOptions = executorOptions;
            this.session = session;
        }

        @Override
        public NutsId getId() {
            return null;
        }

        @Override
        public void execute() {
            Map<String, String> e2 = null;
            if (env != null) {
                e2 = new HashMap<>((Map) env);
            }
            CoreIOUtils.execAndWait(ws, cmd,
                    e2,
                    ws.io().path(directory),
                    session.getTerminal(), true, true);
        }

        @Override
        public String getHelpText() {
            switch (NutsPlatformUtils.getPlatformOsFamily()) {
                case WINDOWS: {
                    return "No help available. Try " + getName() + " /help";
                }
                default: {
                    return "No help available. Try 'man " + getName() + "' or '" + getName() + " --help'";
                }
            }
        }

        @Override
        public String toString() {
            return "SYSEXEC " + NutsMinimalCommandLine.escapeArguments(cmd);
        }

    }

}
