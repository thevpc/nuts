package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.executor.system.ProcessBuilder2;
import net.thevpc.nuts.runtime.standalone.io.printstream.NutsByteArrayPrintStream;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NutsWorkspaceCommandBase;
import net.thevpc.nuts.util.NutsStringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public abstract class AbstractNutsExecCommand extends NutsWorkspaceCommandBase<NutsExecCommand> implements NutsExecCommand {

    protected NutsDefinition commandDefinition;
    protected List<String> command;
    protected List<String> executorOptions;
    protected List<String> workspaceOptions;
    protected Map<String, String> env;
    protected NutsExecutionException result;
    protected boolean executed;
    protected String directory;
    protected NutsPrintStream out;
    protected NutsPrintStream err;
    protected InputStream in;
    protected NutsExecutionType executionType = NutsExecutionType.SPAWN;
    protected NutsRunAs runAs = NutsRunAs.CURRENT_USER;
    protected boolean redirectErrorStream;
    protected boolean failFast;
    private boolean inheritSystemIO;
    private String redirectOutputFile;
    private String redirectInputFile;
    private long sleepMillis = 1000;

    public AbstractNutsExecCommand(NutsWorkspace ws) {
        super(ws, "exec");
    }

    @Override
    public NutsExecCommandFormat formatter() {
        return NutsExecCommandFormat.of(getSession()).setValue(this);
    }

    @Override
    public NutsString format() {
        return formatter().format();
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public NutsExecCommand setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public List<String> getCommand() {
        return CoreCollectionUtils.unmodifiableList(command);
    }

    @Override
    public NutsExecCommand setCommand(String... command) {
        this.command = null;
        return addCommand(command);
    }

    @Override
    public NutsExecCommand setCommand(Collection<String> command) {
        this.command = null;
        return addCommand(command);
    }

    @Override
    public NutsExecCommand setCommand(NutsDefinition definition) {
        this.commandDefinition = definition;
        if (this.commandDefinition != null) {
            this.commandDefinition.getContent().get(session);
            this.commandDefinition.getDependencies().get(session);
            this.commandDefinition.getEffectiveDescriptor().get(session);
//            this.commandDefinition.getInstallInformation().get(session);
        }
        return this;
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
    public NutsExecCommand clearCommand() {
        this.command = null;
        return this;
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
    public NutsExecCommand addExecutorOptions(String... executorOptions) {
        if (executorOptions != null) {
            for (String executorOption : executorOptions) {
                addExecutorOption(executorOption);
            }
        }
        return this;
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
    public NutsExecCommand clearExecutorOptions() {
        this.executorOptions = null;
        return this;
    }

    @Override
    public List<String> getWorkspaceOptions() {
        return CoreCollectionUtils.unmodifiableList(workspaceOptions);
    }

    @Override
    public NutsExecCommand clearWorkspaceOptions(String workspaceOptions) {
        this.workspaceOptions = null;
        return this;
    }

    @Override
    public NutsExecCommand addWorkspaceOptions(NutsWorkspaceOptions workspaceOptions) {
        if (workspaceOptions != null) {
            addWorkspaceOptions(workspaceOptions.toCommandLine().toString());
        }
        return this;
    }

    @Override
    public NutsExecCommand addWorkspaceOptions(String workspaceOptions) {
        if (workspaceOptions != null) {
            if (this.workspaceOptions == null) {
                this.workspaceOptions = new ArrayList<>();
            }
            this.workspaceOptions.add(workspaceOptions);
        }
        return this;
    }

    @Override
    public Map<String, String> getEnv() {
        return env;
    }

    @Override
    public NutsExecCommand setEnv(Map<String, String> env) {
        clearEnv();
        addEnv(env);
        return this;
    }

    @Override
    public NutsExecCommand addEnv(Map<String, String> env) {
        if (env != null) {
            for (Map.Entry<String, String> entry : env.entrySet()) {
                setEnv(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public NutsExecCommand setEnv(String key, String value) {
        if (value == null) {
            if (env != null) {
                env.remove(key);
            }
        } else {
            if (env == null) {
                env = new LinkedHashMap<>();
            }
            env.put(key, value);
        }
        return this;
    }

    @Override
    public NutsExecCommand clearEnv() {
        this.env = null;
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

    //    @Override
//    public InputStream in() {
//        return getIn();
//    }
//
//    @Override
//    public NutsExecCommand in(InputStream in) {
//        return setIn(in);
//    }
    @Override
    public NutsExecCommand setIn(InputStream in) {
        this.in = in;
        return this;
    }

    @Override
    public NutsPrintStream getOut() {
        return out;
    }

    //    @Override
//    public NutsExecCommand out(PrintStream out) {
//        return setOut(out);
//    }
    @Override
    public NutsExecCommand setOut(NutsPrintStream out) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        this.out = out;
        return this;
    }
//
//    @Override
//    public PrintStream out() {
//        return getOut();
//    }

    @Override
    public NutsExecCommand grabOutputString() {
        checkSession();
        // DO NOT CALL setOut :: setOut(new SPrintStream());
        this.out = new SPrintStream(getSession());
        return this;
    }

    @Override
    public NutsExecCommand grabErrorString() {
        checkSession();
        // DO NOT CALL setErr :: setErr(new SPrintStream());
        this.err = new SPrintStream(getSession());
        return this;
    }

    @Override
    public String getOutputString() {
        checkSession();
        if (!executed) {
            run();
        }
        NutsPrintStream o = getOut();
        if (o instanceof SPrintStream) {
            return o.toString();
        }
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.ofPlain("no buffer was configured; should call grabOutputString"));
    }

    @Override
    public String getErrorString() {
        checkSession();
        if (!executed) {
            run();
        }
        if (isRedirectErrorStream()) {
            return getOutputString();
        }
        NutsPrintStream o = getErr();
        if (o instanceof SPrintStream) {
            return o.toString();
        }
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.ofPlain("no buffer was configured; should call grabErrorString"));
    }

    @Override
    public NutsPrintStream getErr() {
        return err;
    }

    //    @Override
//    public NutsExecCommand err(PrintStream err) {
//        return setErr(err);
//    }
    @Override
    public NutsExecCommand setErr(NutsPrintStream err) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        this.err = err;
        return this;
    }

    @Override
    public NutsExecutionType getExecutionType() {
        return executionType;
    }

    @Override
    public NutsExecCommand setExecutionType(NutsExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }
//
//    @Override
//    public PrintStream err() {
//        return getErr();
//    }

    @Override
    public boolean isRedirectErrorStream() {
        return redirectErrorStream;
    }

    @Override
    public NutsExecCommand setRedirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    @Override
    public NutsRunAs getRunAs() {
        return runAs;
    }

    @Override
    public NutsExecCommand setRunAs(NutsRunAs runAs) {
        this.runAs = runAs == null ? NutsRunAs.currentUser() : runAs;
        return this;
    }

    @Override
    public NutsExecCommand setAll(NutsExecCommand other) {
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
        setRunAs(other.getRunAs());
        return this;
    }

    @Override
    public NutsExecCommand copy() {
        return session.exec().setAll(this);
    }

    @Override
    public int getResult() {
        if (!executed) {
//            try {
            run();
//            } catch (Exception ex) {
//                // ignore;
//            }
        }
        if (result != null && result.getExitCode() != 0 && failFast) {
            throw result;
//            checkFailFast(result.getExitCode());
        }
        return result == null ? 0 : result.getExitCode();
    }

    @Override
    public List<String> getExecutorOptions() {
        return CoreCollectionUtils.unmodifiableList(executorOptions);
    }

    @Override
    public NutsExecutionException getResultException() {
        if (!executed) {
            run();
        }
        return result;
    }

    public long getSleepMillis() {
        return sleepMillis;
    }

    public NutsExecCommand setSleepMillis(long sleepMillis) {
        this.sleepMillis = sleepMillis;
        return this;
    }

    @Override
    public boolean isInheritSystemIO() {
        return inheritSystemIO;
    }

    @Override
    public NutsExecCommand setInheritSystemIO(boolean inheritSystemIO) {
        this.inheritSystemIO = inheritSystemIO;
        return this;
    }

    public String getRedirectOutputFile() {
        return redirectOutputFile;
    }

    public NutsExecCommand setRedirectOutputFile(String redirectOutputFile) {
        this.redirectOutputFile = redirectOutputFile;
        return this;
    }

    public String getRedirectInputFile() {
        return redirectInputFile;
    }

    public NutsExecCommand setRedirectInputFile(String redirectInputFile) {
        this.redirectInputFile = redirectInputFile;
        return this;
    }

    public String getOutputString0() {
        checkSession();
        NutsPrintStream o = getOut();
        if (o instanceof SPrintStream) {
            return o.toString();
        }
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.ofPlain("no buffer was configured; should call grabOutputString"));
    }

    protected String getExtraErrorMessage() {
        if (isRedirectErrorStream()) {
            if (isGrabOutputString()) {
                return getOutputString();
            }
        } else {
            if (isGrabErrorString()) {
                return getErrorString();
            }
            if (isGrabOutputString()) {
                return getOutputString();
            }
        }
        return null;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek().orNull();
        if (a == null) {
            return false;
        }
        if (command == null) {
            command = new ArrayList<>();
        }
        if (!command.isEmpty()) {
            command.add(a.asString().get(getSession()));
            return true;
        }
        boolean enabled = a.isActive();
        switch (a.key()) {
            case "--external":
            case "--spawn":
            case "-x": {
                cmdLine.skip();
                if (enabled) {
                    setExecutionType(NutsExecutionType.SPAWN);
                }
                return true;
            }
            case "--embedded":
            case "-b": {
                cmdLine.skip();
                if (enabled) {
                    setExecutionType(NutsExecutionType.EMBEDDED);
                }
                return true;
            }
            case "--open-file": {
                cmdLine.skip();
                if (enabled) {
                    setExecutionType(NutsExecutionType.OPEN);
                }
                return true;
            }
            case "--user-cmd":
            case "--system": {
                cmdLine.skip();
                if (enabled) {
                    setExecutionType(NutsExecutionType.SYSTEM);
                }
                return true;
            }
            case "--current-user": {
                cmdLine.skip();
                if (enabled) {
                    setRunAs(NutsRunAs.currentUser());
                }
                return true;
            }
            case "--as-root": {
                cmdLine.skip();
                if (enabled) {
                    setRunAs(NutsRunAs.ROOT);
                }
                return true;
            }
            case "--run-as": {
                NutsArgument s = cmdLine.nextString().get(session);
                if (enabled) {
                    setRunAs(NutsRunAs.user(s.getStringValue().ifBlankNull().get(session)));
                }
                return true;
            }
            case "--sudo": {
                cmdLine.skip();
                if (enabled) {
                    setRunAs(NutsRunAs.sudo());
                }
                return true;
            }
            case "--inherit-system-io": {
                NutsArgument val = cmdLine.nextBoolean().get(session);
                if (enabled) {
                    setInheritSystemIO(val.getBooleanValue().get(session));
                }
                return true;
            }
            case "-dry":
            case "-d": {
                boolean val = cmdLine.nextBoolean().get(session).getBooleanValue().get(session);
                if (enabled) {
                    getSession().setDry(val);
                }
                return true;
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                cmdLine.skip();
                if (a.isOption()) {
                    addExecutorOption(a.asString().get(session));
                } else {
                    addCommand(a.asString().get(session));
                    addCommand(cmdLine.toStringArray());
                    cmdLine.skipAll();
                }
                return true;
            }
        }
    }

    public boolean isGrabOutputString() {
        return out instanceof SPrintStream;
    }

    public boolean isGrabErrorString() {
        return err instanceof SPrintStream;
    }

    public String getCommandString() {
        return getCommandString(null);
    }

    public String getCommandString(ProcessBuilder2.CommandStringFormat f) {
        StringBuilder sb = new StringBuilder();
        if (env != null) {
            for (Map.Entry<String, String> e : env.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                if (k == null) {
                    k = "";
                }
                if (v == null) {
                    v = "";
                }
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
                sb.append(NutsStringUtils.formatStringLiteral(k)).append("=").append(NutsStringUtils.formatStringLiteral(v));
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
            sb.append(NutsStringUtils.formatStringLiteral(s));
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

    public String toString() {
        return getCommandString();
    }

    protected static class SPrintStream extends NutsByteArrayPrintStream {

        public SPrintStream(NutsSession session) {
            super(session);
        }

        protected SPrintStream(ByteArrayOutputStream bos, NutsSession session) {
            super(bos, session);
        }

        @Override
        public NutsPrintStream setSession(NutsSession session) {
            if (session == null || session == this.session) {
                return this;
            }
            return new SPrintStream((ByteArrayOutputStream) out, session);
        }
    }

}
