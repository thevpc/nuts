package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.executor.system.ProcessBuilder2;
import net.thevpc.nuts.runtime.standalone.io.printstream.NByteArrayPrintStream;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCommandBase;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NStringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public abstract class AbstractNExecCommand extends NWorkspaceCommandBase<NExecCommand> implements NExecCommand, NFormattable {

    protected NDefinition commandDefinition;
    protected List<String> command;
    protected List<String> executorOptions;
    protected List<String> workspaceOptions;
    protected Map<String, String> env;
    protected NExecutionException result;
    protected boolean executed;
    protected String directory;
    protected NPrintStream out;
    protected NPrintStream err;
    protected InputStream in;
    protected NExecutionType executionType = NExecutionType.SPAWN;
    protected NRunAs runAs = NRunAs.CURRENT_USER;
    protected boolean redirectErrorStream;
    protected boolean failFast;
    private boolean inheritSystemIO;
    private NPath redirectOutputFile;
    private NPath redirectInputFile;
    private long sleepMillis = 1000;

    public AbstractNExecCommand(NSession ws) {
        super(ws, "exec");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
    @Override
    public NExecCommandFormat formatter() {
        return NExecCommandFormat.of(getSession()).setValue(this);
    }

    @Override
    public NString format() {
        return formatter().format();
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public NExecCommand setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public List<String> getCommand() {
        return CoreCollectionUtils.unmodifiableList(command);
    }

    @Override
    public NExecCommand setCommand(String... command) {
        this.command = null;
        return addCommand(command);
    }

    @Override
    public NExecCommand setCommand(Collection<String> command) {
        this.command = null;
        return addCommand(command);
    }

    @Override
    public NExecCommand setCommand(NDefinition definition) {
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
    public NExecCommand addCommand(String... command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        if(command!=null){
            for (String s : command) {
                if(s!=null){
                    this.command.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NExecCommand addCommand(Collection<String> command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        if(command!=null){
            for (String s : command) {
                if(s!=null){
                    this.command.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NExecCommand clearCommand() {
        this.command = null;
        return this;
    }

    @Override
    public NExecCommand addExecutorOption(String executorOption) {
        if (executorOption != null) {
            if (this.executorOptions == null) {
                this.executorOptions = new ArrayList<>();
            }
            this.executorOptions.add(executorOption);
        }
        return this;
    }

    @Override
    public NExecCommand addExecutorOptions(String... executorOptions) {
        if (executorOptions != null) {
            for (String executorOption : executorOptions) {
                addExecutorOption(executorOption);
            }
        }
        return this;
    }

    @Override
    public NExecCommand addExecutorOptions(Collection<String> executorOptions) {
        if (executorOptions != null) {
            for (String executorOption : executorOptions) {
                addExecutorOption(executorOption);
            }
        }
        return this;
    }

    @Override
    public NExecCommand clearExecutorOptions() {
        this.executorOptions = null;
        return this;
    }

    @Override
    public List<String> getWorkspaceOptions() {
        return CoreCollectionUtils.unmodifiableList(workspaceOptions);
    }

    @Override
    public NExecCommand clearWorkspaceOptions(String workspaceOptions) {
        this.workspaceOptions = null;
        return this;
    }

    @Override
    public NExecCommand addWorkspaceOptions(NWorkspaceOptions workspaceOptions) {
        if (workspaceOptions != null) {
            addWorkspaceOptions(workspaceOptions.toCommandLine().toString());
        }
        return this;
    }

    @Override
    public NExecCommand addWorkspaceOptions(String workspaceOptions) {
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
    public NExecCommand setEnv(Map<String, String> env) {
        clearEnv();
        addEnv(env);
        return this;
    }

    @Override
    public NExecCommand addEnv(Map<String, String> env) {
        if (env != null) {
            for (Map.Entry<String, String> entry : env.entrySet()) {
                setEnv(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public NExecCommand setEnv(String key, String value) {
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
    public NExecCommand clearEnv() {
        this.env = null;
        return this;
    }

    @Override
    public String getDirectory() {
        return directory;
    }

    @Override
    public NExecCommand setDirectory(String directory) {
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
    public NExecCommand setIn(InputStream in) {
        this.in = in;
        return this;
    }

    @Override
    public NPrintStream getOut() {
        return out;
    }

    //    @Override
//    public NutsExecCommand out(PrintStream out) {
//        return setOut(out);
//    }
    @Override
    public NExecCommand setOut(NPrintStream out) {
        checkSession();
        NWorkspace ws = getSession().getWorkspace();
        this.out = out;
        return this;
    }
//
//    @Override
//    public PrintStream out() {
//        return getOut();
//    }

    @Override
    public NExecCommand grabOutputString() {
        checkSession();
        // DO NOT CALL setOut :: setOut(new SPrintStream());
        this.out = new SPrintStream(getSession());
        return this;
    }

    @Override
    public NExecCommand grabErrorString() {
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
        NPrintStream o = getOut();
        if (o instanceof SPrintStream) {
            return o.toString();
        }
        throw new NIllegalArgumentException(getSession(), NMsg.ofPlain("no buffer was configured; should call grabOutputString"));
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
        NPrintStream o = getErr();
        if (o instanceof SPrintStream) {
            return o.toString();
        }
        throw new NIllegalArgumentException(getSession(), NMsg.ofPlain("no buffer was configured; should call grabErrorString"));
    }

    @Override
    public NPrintStream getErr() {
        return err;
    }

    //    @Override
//    public NutsExecCommand err(PrintStream err) {
//        return setErr(err);
//    }
    @Override
    public NExecCommand setErr(NPrintStream err) {
        checkSession();
        NWorkspace ws = getSession().getWorkspace();
        this.err = err;
        return this;
    }

    @Override
    public NExecutionType getExecutionType() {
        return executionType;
    }

    @Override
    public NExecCommand setExecutionType(NExecutionType executionType) {
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
    public NExecCommand setRedirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    @Override
    public NRunAs getRunAs() {
        return runAs;
    }

    @Override
    public NExecCommand setRunAs(NRunAs runAs) {
        this.runAs = runAs == null ? NRunAs.currentUser() : runAs;
        return this;
    }

    @Override
    public NExecCommand setAll(NExecCommand other) {
        super.copyFromWorkspaceCommandBase((NWorkspaceCommandBase) other);
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
    public NExecCommand copy() {
        return NExecCommand.of(session).setAll(this);
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
    public NExecutionException getResultException() {
        if (!executed) {
            run();
        }
        return result;
    }

    public long getSleepMillis() {
        return sleepMillis;
    }

    public NExecCommand setSleepMillis(long sleepMillis) {
        this.sleepMillis = sleepMillis;
        return this;
    }

    @Override
    public boolean isInheritSystemIO() {
        return inheritSystemIO;
    }

    @Override
    public NExecCommand setInheritSystemIO(boolean inheritSystemIO) {
        this.inheritSystemIO = inheritSystemIO;
        return this;
    }

    public NPath getRedirectOutputFile() {
        return redirectOutputFile;
    }

    public NExecCommand setRedirectOutputFile(NPath redirectOutputFile) {
        this.redirectOutputFile = redirectOutputFile;
        return this;
    }

    public NPath getRedirectInputFile() {
        return redirectInputFile;
    }

    public NExecCommand setRedirectInputFile(NPath redirectInputFile) {
        this.redirectInputFile = redirectInputFile;
        return this;
    }

    public String getOutputString0() {
        checkSession();
        NPrintStream o = getOut();
        if (o instanceof SPrintStream) {
            return o.toString();
        }
        throw new NIllegalArgumentException(getSession(), NMsg.ofPlain("no buffer was configured; should call grabOutputString"));
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
    public boolean configureFirst(NCommandLine cmdLine) {
        NArg a = cmdLine.peek().orNull();
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
                    setExecutionType(NExecutionType.SPAWN);
                }
                return true;
            }
            case "--embedded":
            case "-b": {
                cmdLine.skip();
                if (enabled) {
                    setExecutionType(NExecutionType.EMBEDDED);
                }
                return true;
            }
            case "--open-file": {
                cmdLine.skip();
                if (enabled) {
                    setExecutionType(NExecutionType.OPEN);
                }
                return true;
            }
            case "--user-cmd":
            case "--system": {
                cmdLine.skip();
                if (enabled) {
                    setExecutionType(NExecutionType.SYSTEM);
                }
                return true;
            }
            case "--current-user": {
                cmdLine.skip();
                if (enabled) {
                    setRunAs(NRunAs.currentUser());
                }
                return true;
            }
            case "--as-root": {
                cmdLine.skip();
                if (enabled) {
                    setRunAs(NRunAs.ROOT);
                }
                return true;
            }
            case "--run-as": {
                NArg s = cmdLine.nextString().get(session);
                if (enabled) {
                    setRunAs(NRunAs.user(s.getStringValue().ifBlankEmpty().get(session)));
                }
                return true;
            }
            case "--sudo": {
                cmdLine.skip();
                if (enabled) {
                    setRunAs(NRunAs.sudo());
                }
                return true;
            }
            case "--inherit-system-io": {
                NArg val = cmdLine.nextBoolean().get(session);
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
                sb.append(NStringUtils.formatStringLiteral(k)).append("=").append(NStringUtils.formatStringLiteral(v));
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
            sb.append(NStringUtils.formatStringLiteral(s));
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

    protected static class SPrintStream extends NByteArrayPrintStream {

        public SPrintStream(NSession session) {
            super(session);
        }

        protected SPrintStream(ByteArrayOutputStream bos, NSession session) {
            super(bos, session);
        }

        @Override
        public NPrintStream setSession(NSession session) {
            if (session == null || session == this.session) {
                return this;
            }
            return new SPrintStream((ByteArrayOutputStream) out, session);
        }
    }

    @Override
    public NFormat formatter(NSession session) {
        return NFormat.of(session, new NFormatSPI() {

            @Override
            public String getName() {
                return "NutsExecCommand";
            }

            @Override
            public void print(NPrintStream out) {
                out.print(NCommandLine.of(command));
            }

            @Override
            public boolean configureFirst(NCommandLine commandLine) {
                return false;
            }
        });
    }
}
