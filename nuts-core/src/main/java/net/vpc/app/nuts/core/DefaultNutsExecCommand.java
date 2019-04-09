package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.util.CharacterizedFile;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.terminals.NutsDefaultFormattedPrintStream;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.executors.CustomNutsExecutorComponent;
import net.vpc.app.nuts.core.executors.JavaExecutorOptions;

import net.vpc.app.nuts.core.terminals.DefaultNutsSessionTerminal;

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
    private int result;
    private boolean executed;
    private String directory;
    private PrintStream out;
    private PrintStream err;
    private InputStream in;
    private NutsExecutionType executionType = NutsExecutionType.EXTERNAL;
    private boolean redirectErrorStream;
    private boolean failFast;

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public NutsExecCommand setFailFast() {
        return setFailFast(true);
    }

    @Override
    public NutsExecCommand setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    public DefaultNutsExecCommand(DefaultNutsWorkspace ws) {
        this.ws = ws;
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
    public List<String> getCommand() {
        return command;
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
    public NutsExecCommand addCommand(List<String> command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        this.command.addAll(command);
        return this;
    }

    @Override
    public NutsExecCommand addExecutorOptions(String... executorOptions) {
        if (this.executorOptions == null) {
            this.executorOptions = new ArrayList<>();
        }
        this.executorOptions.addAll(Arrays.asList(executorOptions));
        return this;
    }

    @Override
    public NutsExecCommand addExecutorOptions(List<String> executorOptions) {
        if (this.executorOptions == null) {
            this.executorOptions = new ArrayList<>();
        }
        this.executorOptions.addAll(executorOptions);
        return this;
    }

    @Override
    public NutsExecCommand setCommand(String... command) {
        setCommand(Arrays.asList(command));
        return this;
    }

    @Override
    public NutsExecCommand setCommand(List<String> command) {
        this.command = command == null ? null : new ArrayList<>(command);
        return this;
    }

    @Override
    public NutsExecCommand setExecutorOptions(String... options) {
        setExecutorOptions(options == null ? null : Arrays.asList(options));
        return this;
    }

    @Override
    public NutsExecCommand setExecutorOptions(List<String> options) {
        this.executorOptions = options == null ? null : new ArrayList<>(options);
        return this;
    }

    @Override
    public Properties getEnv() {
        return env;
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
    public NutsExecCommand setEnv(String k, String val) {
        if (env == null) {
            env = new Properties();
        }
        env.put(k, val);
        return this;
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
    public NutsExecCommand setDirectory(String directory) {
        this.directory = directory;
        return this;
    }

    @Override
    public InputStream getIn() {
        return in;
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
        executed = true;
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
        String[] ts = command.toArray(new String[0]);
        switch (executionType) {
            case NATIVE: {
                result = execNative(ts, executorOptions == null ? new String[0] : executorOptions.toArray(new String[0]), session.copy().setTerminal(terminal));
                break;
            }
            case EXTERNAL: {
                result = execExternal(ts, executorOptions == null ? new String[0] : executorOptions.toArray(new String[0]), session.copy().setTerminal(terminal));
                break;
            }
            case EMBEDDED: {
                result = execEmbedded(ts, executorOptions == null ? new String[0] : executorOptions.toArray(new String[0]), session.copy().setTerminal(terminal));
                break;
            }
        }
        return this;
    }

    public boolean isRedirectErrorStream() {
        return redirectErrorStream;
    }

    @Override
    public NutsExecCommand setRedirectErrorStream() {
        return setRedirectErrorStream(true);
    }

    public NutsExecCommand setRedirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    @Override
    public NutsExecutionType getExecutionType() {
        return executionType;
    }

    @Override
    public NutsExecCommand setExecutionType(NutsExecutionType executionType) {
        if (executionType == null) {
            executionType = NutsExecutionType.EXTERNAL;
        }
        this.executionType = executionType;
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

    public int execNative(String[] cmd, String[] executorOptions, NutsSession session) {
        Map<String, String> e2 = null;
        if (env != null) {
            e2 = new HashMap<>((Map) env);
        }
        result = CoreIOUtils.execAndWait(ws, cmd,
                e2,
                ws.io().path(directory),
                session.getTerminal(), true, isFailFast());
        checkFailFast(result);
        return result;
    }

    public int execExternal(String[] cmd, String[] executorOptions, NutsSession session) {
        return execEmbeddedOrExternal(cmd, executorOptions, session, false);
    }

    public int execEmbedded(String[] cmd, String[] executorOptions, NutsSession session) {
        return execEmbeddedOrExternal(cmd, executorOptions, session, true);
    }

    private int execEmbeddedOrExternal(String[] cmd, String[] executorOptions, NutsSession session, boolean embedded) {
        if (cmd == null || cmd.length == 0) {
            throw new NutsIllegalArgumentException("Missing command");
        }
        String[] args = new String[cmd.length - 1];
        System.arraycopy(cmd, 1, args, 0, args.length);
        String cmdName = cmd[0];
        ws.security().checkAllowed(NutsConstants.Rights.EXEC, cmdName);
        int result = 0;
        if (cmdName.contains("/") || cmdName.contains("\\")) {
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
                result = ws_exec(nutToRun, cmdName, args, executorOptions, env, directory, failFast, session, embedded);
            }
        } else if (cmdName.contains(":")) {
            NutsDefinition nutToRun = null;
            nutToRun = ws.fetch().id(cmdName).session(session).setAcceptOptional(false).includeDependencies().installed().getResultDefinition();
            if (!nutToRun.getInstallation().isInstalled()) {
                ws.security().checkAllowed(NutsConstants.Rights.AUTO_INSTALL, cmdName);
                ws.install().id(nutToRun.getId()).setArgs(args).setForce(true).setSession(session).install();
            }
            result = ws_exec(nutToRun, cmdName, args, executorOptions, env, directory, failFast, session, embedded);
        } else {
            NutsWorkspaceCommand command = null;
            if (embedded) {
                command = ws.config().findEmbeddedCommand(cmdName);
                if (command == null) {
                    command = ws.config().findCommand(cmdName);
                }
            } else {
                command = ws.config().findCommand(cmdName);
                if (command == null) {
                    command = ws.config().findEmbeddedCommand(cmdName);
                }
            }
            NutsCommandExecOptions o = new NutsCommandExecOptions().setExecutorOptions(executorOptions).setDirectory(directory).setFailFast(failFast)
                    .setExecutionType(embedded ? NutsExecutionType.EMBEDDED : NutsExecutionType.EXTERNAL).setEnv(env);
            if (command != null) {
                result = command.exec(args, o, session);
            } else {
                NutsDefinition def = null;
                def = ws.fetch().id(cmdName).session(session).getResultDefinition();
                log.log(Level.FINE, "Command {0} not found. Trying to resolve command as valid Nuts Id.", new Object[]{cmdName});
                if (!def.getInstallation().isInstalled()) {
                    ws.security().checkAllowed(NutsConstants.Rights.AUTO_INSTALL, cmdName);
                    ws.install().id(def.getId()).setArgs(args).setForce(true).setSession(session).install();
                }
                result = ws_exec(def, cmdName, args, executorOptions, env, directory, failFast, session, embedded);
            }
        }

        checkFailFast(result);
        return result;
    }

    private void checkFailFast(int result) {
        if (result != 0) {
            if (isFailFast()) {
                if (isRedirectErrorStream()) {
                    if (isGrabOutputString()) {
                        throw new UncheckedIOException(new IOException("Execution Failed with code " + result + " and message : " + getOutputString()));
                    }
                } else {
                    if (isGrabErrorString()) {
                        throw new UncheckedIOException(new IOException("Execution Failed with code " + result + " and message : " + getErrorString()));
                    }
                    if (isGrabOutputString()) {
                        throw new UncheckedIOException(new IOException("Execution Failed with code " + result + " and message : " + getOutputString()));
                    }
                }
                throw new UncheckedIOException(new IOException("Execution Failed with code " + result));
            }
        }
    }

    protected int ws_exec(NutsDefinition nutToRun, String commandName, String[] appArgs, String[] executorOptions, Properties env, String dir, boolean failFast, NutsSession session, boolean embedded) {
        ws.security().checkAllowed(NutsConstants.Rights.EXEC, "exec");
        session = CoreNutsUtils.validateSession(session, ws);
        if (nutToRun != null && nutToRun.getContent().getPath() != null) {
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
                        env, execProps, dir, session, ws, failFast, commandName);
                return execComponent.exec(executionContext);
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
                        mainMethod = cls.getMethod("launch", NutsWorkspace.class, String[].class);
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
                            nutsApp = cls.newInstance();
                            isNutsApp = true;
                        }
                    } catch (Exception rr) {
                        //ignore

                    }
                    if (isNutsApp) {
                        //NutsWorkspace
                        return ((Integer) mainMethod.invoke(nutsApp, new Object[]{this, options.getApp().toArray(new String[0])}));
                    } else {
                        //NutsWorkspace
                        mainMethod = cls.getMethod("main", String[].class);
                        mainMethod.invoke(null, new Object[]{options.getApp().toArray(new String[0])});
                    }

                } catch (MalformedURLException e) {
                    th = e;
                } catch (NoSuchMethodException e) {
                    th = e;
                } catch (SecurityException e) {
                    th = e;
                } catch (IllegalAccessException e) {
                    th = e;
                } catch (IllegalArgumentException e) {
                    th = e;
                } catch (InvocationTargetException e) {
                    th = e;
                } catch (ClassNotFoundException e) {
                    th = e;
                }
                if (th != null) {
                    throw new NutsExecutionException("Error Executing " + nutToRun.getId(), th, 204);
                }
            }
        }
        throw new NutsNotFoundException(nutToRun == null ? null : nutToRun.getId());
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

}
