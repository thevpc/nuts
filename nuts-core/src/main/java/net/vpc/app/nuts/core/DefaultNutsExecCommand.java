package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.commands.ComponentExecutable;
import net.vpc.app.nuts.core.commands.CheckUpdatesInternalExecutable;
import net.vpc.app.nuts.core.commands.AliasExecutable;
import net.vpc.app.nuts.core.commands.ExecInternalExecutable;
import net.vpc.app.nuts.core.commands.InstallInternalExecutable;
import net.vpc.app.nuts.core.commands.WelcomeInternalExecutable;
import net.vpc.app.nuts.core.commands.PathComponentExecutable;
import net.vpc.app.nuts.core.commands.NutsExecutableImpl;
import net.vpc.app.nuts.core.commands.LicenseInternalExecutable;
import net.vpc.app.nuts.core.commands.WhichInternalExecutable;
import net.vpc.app.nuts.core.commands.FetchInternalExecutable;
import net.vpc.app.nuts.core.commands.HelpInternalExecutable;
import net.vpc.app.nuts.core.commands.DeployInternalExecutable;
import net.vpc.app.nuts.core.commands.UninstallInternalExecutable;
import net.vpc.app.nuts.core.commands.SystemExecutable;
import net.vpc.app.nuts.core.commands.PushInternalExecutable;
import net.vpc.app.nuts.core.commands.UpdateInternalExecutable;
import net.vpc.app.nuts.core.commands.InfoInternalExecutable;
import net.vpc.app.nuts.core.commands.UndeployInternalExecutable;
import net.vpc.app.nuts.core.commands.VersionInternalExecutable;
import net.vpc.app.nuts.core.commands.FindInternalExecutable;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.terminals.NutsDefaultFormattedPrintStream;
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

/**
 *
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultNutsExecCommand extends NutsWorkspaceCommandBase<NutsExecCommand> implements NutsExecCommand {

    public static final Logger LOG = Logger.getLogger(DefaultNutsExecCommand.class.getName());
    public static final NutsDescriptor TEMP_DESC = new DefaultNutsDescriptorBuilder()
            .setId(CoreNutsUtils.parseNutsId("temp:exe#1.0"))
            .setPackaging("exe")
            .setExecutable(true)
            .setExecutor(new NutsExecutorDescriptor(CoreNutsUtils.parseNutsId("exec")))
            .build();

    private List<String> command;
    private List<String> executorOptions;
    private Properties env;
    private NutsExecutionException result;
    private boolean executed;
    private String directory;
    private PrintStream out;
    private PrintStream err;
    private InputStream in;
    private NutsExecutionType executionType = NutsExecutionType.SPAWN;
    private boolean redirectErrorStream;
    private boolean failFast;
    private NutsCommandStringFormatter commandStringFormatter;

    public DefaultNutsExecCommand(DefaultNutsWorkspace ws) {
        super(ws);
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
    public String[] getCommand() {
        return command == null ? new String[0] : command.toArray(new String[0]);
    }

    @Override
    public String[] getExecutorOptions() {
        return executorOptions == null ? new String[0] : executorOptions.toArray(new String[0]);
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
    public NutsExecCommand run() {
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
        DefaultNutsSessionTerminal terminal = new DefaultNutsSessionTerminal();
        terminal.install(ws);
        terminal.setParent(getValidSession().getTerminal());
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
                terminal.setErr(getValidSession().getTerminal().out());
            }
        }
        terminal.out().flush();
        terminal.err().flush();
        String[] ts = command.toArray(new String[0]);
        NutsExecutableImpl exec = null;
        switch (executionType) {
            case SYSCALL: {
                exec = new SystemExecutable(ts, executorOptions == null ? new String[0] : executorOptions.toArray(new String[0]),
                        ws,
                        getValidSession().copy().setTerminal(terminal),
                        this
                );
                break;
            }
            case SPAWN: {
                exec = execEmbeddedOrExternal(ts, executorOptions == null ? new String[0] : executorOptions.toArray(new String[0]), getValidSession().copy().setTerminal(terminal), false);
                break;
            }
            case EMBEDDED: {
                exec = execEmbeddedOrExternal(ts, executorOptions == null ? new String[0] : executorOptions.toArray(new String[0]), getValidSession().copy().setTerminal(terminal), true);
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException("Invalid executionType " + executionType);
            }
        }
        return exec;
    }

    @Override
    public boolean isRedirectErrorStream() {
        return redirectErrorStream;
    }

    @Override
    public NutsExecCommand redirectErrorStream() {
        return setRedirectErrorStream(true);
    }

    @Override
    public NutsExecCommand setRedirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    @Override
    public NutsExecCommand copyFrom(NutsExecCommand other) {
        super.copyFromWorkspaceCommandBase((NutsWorkspaceCommandBase) other);
        addCommand(other.getCommand());
        addEnv(other.getEnv());
        addExecutorOptions(other.getExecutorOptions());
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
        NutsCommandLine cmd = ws.parser().parseCommandLine(args);
        NutsArgument a;
        if (command == null) {
            command = new ArrayList<>();
        }
        while ((a = cmd.next()) != null) {
            if (a.isOption()) {
                if (command.isEmpty()) {
                    switch (a.strKey()) {
                        case "--external":
                        case "--spawn":
                        case "-x": {
                            setExecutionType(NutsExecutionType.SPAWN);
                            break;
                        }
                        case "--embedded":
                        case "-b": {
                            setExecutionType(NutsExecutionType.EMBEDDED);
                            break;
                        }
                        case "--native":
                        case "--syscall":
                        case "-n": {
                            setExecutionType(NutsExecutionType.SYSCALL);
                            break;
                        }
                        default: {
                            if (!super.parseOption(a, cmd)) {
                                executorOptions.add(a.getString());
                            }
                        }
                    }
                } else {
                    throw new NutsIllegalArgumentException("Unexpected option here");
                }
            } else {
                command.add(a.getString());
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
                run();
            } catch (Exception ex) {
                // ignore;
            }
        }
        return result == null ? 0 : result.getExitCode();
    }

    @Override
    public NutsExecutionException getResultException() {
        if (!executed) {
            run();
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
        NutsCommandStringFormatter f = getCommandStringFormatter();
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
                return new UpdateInternalExecutable(args, ws, session);
            }
            case "check-updates": {
                return new CheckUpdatesInternalExecutable(args, ws, session);
            }
            case "install": {
                return new InstallInternalExecutable(args, ws, session);
            }
            case "uninstall": {
                return new UninstallInternalExecutable(args, ws, session);
            }
            case "deploy": {
                return new DeployInternalExecutable(args, ws, session);
            }
            case "undeploy": {
                return new UndeployInternalExecutable(args, ws, session);
            }
            case "push": {
                return new PushInternalExecutable(args, ws, session);
            }
            case "fetch": {
                return new FetchInternalExecutable(args, ws, session);
            }
            case "find": {
                return new FindInternalExecutable(args, ws, session);
            }
            case "version": {
                return new VersionInternalExecutable(args, ws, session, this);
            }
            case "license": {
                return new LicenseInternalExecutable(args, ws, session);
            }
            case "help": {
                return new HelpInternalExecutable(args, ws, session);
            }
            case "welcome": {
                return new WelcomeInternalExecutable(args, ws, session);
            }
            case "info": {
                return new InfoInternalExecutable(args, ws, session);
            }
            case "which": {
                return new WhichInternalExecutable(args, ws, session, this);
            }
            case "exec": {
                return new ExecInternalExecutable(args, ws, session, this);
            }
        }
        if (cmdName.contains("/") || cmdName.contains("\\")) {
            return new PathComponentExecutable(cmdName, args, executorOptions, embedded, ws, getValidSession(), this);
        } else if (cmdName.contains(":")) {
            return ws_exec(cmdName, args, executorOptions, env, directory, failFast, session, embedded);
        } else {
            NutsWorkspaceCommandAlias command = null;
            command = ws.config().findCommandAlias(cmdName);
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
        NutsFindCommand ff = ws.find().id(nid).session(session).setAcceptOptional(false).includeDependencies().latestVersions().setLenient(true)
                .defaultVersions()
                .installed();
        def = ff.getResultDefinitions().first();
        if (def == null) {
            //retest whhout checking it the version is default or not
            // this help recovering from "invalid default version" issue
            def = ws.find().id(nid).session(session).setAcceptOptional(false).includeDependencies().latestVersions().setLenient(true).installed().getResultDefinitions().first();
        }
        if (def == null) {
            def = ws.find().id(nid).session(session).setAcceptOptional(false).includeDependencies().setLenient(false).wired().latestVersions().getResultDefinitions().required();
        }
        return new ComponentExecutable(def, commandName, appArgs, executorOptions, env, dir, failFast, ws, session, embedded, this);
    }

    public void ws_exec(NutsDefinition nutToRun, String commandName, String[] appArgs, String[] executorOptions, Properties env, String dir, boolean failFast, NutsSession session, boolean embedded) {
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
}
