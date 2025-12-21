package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.*;
import net.thevpc.nuts.core.NRunAs;
import net.thevpc.nuts.core.NWorkspaceOptions;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.runtime.standalone.executor.system.ProcessBuilder2;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.time.NDuration;

import java.util.*;

/**
 * type: Command Class
 *
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public abstract class AbstractNExec extends NWorkspaceCmdBase<NExec> implements NExec {

    protected NDefinition commandDefinition;
    protected List<String> command;
    protected List<String> executorOptions;
    protected List<String> workspaceOptions;
    protected Map<String, String> env;
    protected NExecutionException resultException;
    protected boolean executed;
    protected NDuration executionTime;
    protected boolean multipleRuns;
    protected long multipleRunsMinTimeMs;
    protected long multipleRunsSafeTimeMs;
    protected int multipleRunsMaxCount;
    protected String multipleRunsCron;
    protected NPath directory;
    protected NExecOutput out = NExecOutput.ofInherit();
    protected NExecOutput err = NExecOutput.ofInherit();
    protected NExecInput in = NExecInput.ofInherit();
    protected NExecutionType executionType = NExecutionType.SPAWN;
    protected NRunAs runAs = NRunAs.CURRENT_USER;
    protected Boolean dry = null;
    protected boolean failFast;
    protected Boolean bot;
    private long sleepMillis = 1000;
    private NConnectionString connectionString;
    private boolean rawCommand;

    public AbstractNExec() {
        super("exec");
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public NExec setBot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public Boolean getBot() {
        return bot;
    }

    @Override
    public NExec setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public NExec failFast() {
        return setFailFast(true);
    }

    @Override
    public List<String> getCommand() {
        return NCollections.unmodifiableList(command);
    }

    @Override
    public NExec setCommand(String... command) {
        this.command = null;
        return addCommand(command);
    }

    @Override
    public NExec setCommand(Collection<String> command) {
        this.command = null;
        return addCommand(command);
    }

    @Override
    public NExec setCommandDefinition(NDefinition definition) {
        this.commandDefinition = definition;
        if (this.commandDefinition != null) {
//            this.commandDefinition.getContent().get();
//            this.commandDefinition.getDependencies().get();
//            this.commandDefinition.getEffectiveDescriptor().get();
//            this.commandDefinition.getInstallInformation().get(session);
        }
        return this;
    }

    public NDefinition getCommandDefinition() {
        return commandDefinition;
    }

    @Override
    public NExec addCommand(NPath path) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        if (path != null) {
            this.command.add(path.toString());
        }
        return this;
    }

    @Override
    public NExec addCommand(String... command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        if (command != null) {
            for (String s : command) {
                if (s != null) {
                    this.command.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NExec addCommand(Collection<String> command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        if (command != null) {
            for (String s : command) {
                if (s != null) {
                    this.command.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NExec clearCommand() {
        this.command = null;
        return this;
    }

    @Override
    public NExec addExecutorOption(String executorOption) {
        if (executorOption != null) {
            if (this.executorOptions == null) {
                this.executorOptions = new ArrayList<>();
            }
            this.executorOptions.add(executorOption);
        }
        return this;
    }

    @Override
    public NExec addExecutorOptions(String... executorOptions) {
        if (executorOptions != null) {
            for (String executorOption : executorOptions) {
                addExecutorOption(executorOption);
            }
        }
        return this;
    }

    @Override
    public NExec setExecutorOptions(Collection<String> executorOptions) {
        this.executorOptions = new ArrayList<>();
        if (executorOptions != null) {
            for (String executorOption : executorOptions) {
                addExecutorOption(executorOption);
            }
        }
        return this;
    }

    @Override
    public NExec addExecutorOptions(Collection<String> executorOptions) {
        if (executorOptions != null) {
            for (String executorOption : executorOptions) {
                addExecutorOption(executorOption);
            }
        }
        return this;
    }

    @Override
    public NExec clearExecutorOptions() {
        this.executorOptions = null;
        return this;
    }

    @Override
    public List<String> getWorkspaceOptions() {
        return NCollections.unmodifiableList(workspaceOptions);
    }

    @Override
    public NExec clearWorkspaceOptions(String workspaceOptions) {
        this.workspaceOptions = null;
        return this;
    }

    @Override
    public NExec addWorkspaceOptions(NWorkspaceOptions workspaceOptions) {
        if (workspaceOptions != null) {
            addWorkspaceOptions(workspaceOptions.toCmdLine().toString());
        }
        return this;
    }

    @Override
    public NExec addWorkspaceOptions(String workspaceOptions) {
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
    public NExec setEnv(Map<String, String> env) {
        clearEnv();
        addEnv(env);
        return this;
    }

    @Override
    public NExec addEnv(Map<String, String> env) {
        if (env != null) {
            for (Map.Entry<String, String> entry : env.entrySet()) {
                setEnv(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public NExec setEnv(String key, String value) {
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
    public NExec clearEnv() {
        this.env = null;
        return this;
    }

    @Override
    public NPath getDirectory() {
        return directory;
    }

    @Override
    public NExec setDirectory(NPath directory) {
        this.directory = directory;
        return this;
    }

    @Override
    public NExecInput getIn() {
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
    public NExec setIn(NExecInput in) {
        this.in = in == null ? NExecInput.ofInherit() : in;
        return this;
    }

    @Override
    public NExecOutput getOut() {
        return out;
    }

    @Override
    public NExec setOut(NExecOutput out) {
        this.out = out == null ? NExecOutput.ofInherit() : out;
        return this;
    }

    @Override
    public NExec grabOut() {
        // DO NOT CALL setOut :: setOut(new SPrintStream());
        this.out = NExecOutput.ofGrabMem();
        return this;
    }

    @Override
    public NExec grabAll() {
        return grabOut().redirectErr();
    }

    @Override
    public NExec grabOutOnly() {
        return grabOut().setErr(NExecOutput.ofNull());
    }

    @Override
    public NExec grabErr() {
        setErr(NExecOutput.ofGrabMem());
        return this;
    }

    @Override
    public String getGrabbedAllString() {
        if (!executed) {
            grabAll();
        }
        return getGrabbedOutString();
    }

    @Override
    public String getGrabbedOutOnlyString() {
        if (!executed) {
            if (out.getType() != NRedirectType.GRAB_STREAM) {
                grabOutOnly();
            }
        }
        return getGrabbedOutString();
    }

    @Override
    public String getGrabbedOutString() {
        return new String(getGrabbedOutBytes());
    }

    @Override
    public byte[] getGrabbedOutBytes() {
        if (!executed) {
            if (out.getType() != NRedirectType.GRAB_STREAM) {
                grabOut();
            }
            run();
        }
        if (getOut() == null) {
            throw new NIllegalArgumentException(NMsg.ofPlain("no buffer was configured; should call grabOut"));
        }
        if (getOut().getResultSource().isNotPresent()) {
            if (getOut().getType() == NRedirectType.GRAB_FILE || getOut().getType() == NRedirectType.GRAB_STREAM) {
                if (getResultException().isPresent()) {
                    throw getResultException().get();
                }
            }
        }
        return getOut().getResultBytes();
    }

    @Override
    public byte[] getGrabbedErrBytes() {
        if (!executed) {
            if (err.getType() != NRedirectType.GRAB_STREAM) {
                grabErr();
            }
            run();
        }
        if (getErr() == null) {
            throw new NIllegalArgumentException(NMsg.ofPlain("no buffer was configured; should call grabErr"));
        }
        if (getErr().getType() == NRedirectType.REDIRECT) {
            return getGrabbedOutBytes();
        }
        if (getErr().getResultSource().isNotPresent()) {
            if (getErr().getType() == NRedirectType.GRAB_FILE || getErr().getType() == NRedirectType.GRAB_STREAM) {
                if (getResultException().isPresent()) {
                    throw getResultException().get();
                }
            }
        }
        return getErr().getResultBytes();
    }

    @Override
    public String getGrabbedErrString() {
        return new String(getGrabbedErrBytes());
    }

    @Override
    public NExecOutput getErr() {
        return err;
    }

    //    @Override
//    public NutsExecCommand err(PrintStream err) {
//        return setErr(err);
//    }
    @Override
    public NExec setErr(NExecOutput err) {
        this.err = err;
        return this;
    }

    @Override
    public NExecutionType getExecutionType() {
        return executionType;
    }

    @Override
    public NExec setExecutionType(NExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public NExec system() {
        return setExecutionType(NExecutionType.SYSTEM);
    }

    @Override
    public NExec embedded() {
        return setExecutionType(NExecutionType.EMBEDDED);
    }

    @Override
    public NExec spawn() {
        return setExecutionType(NExecutionType.SPAWN);
    }

    @Override
    public NExec open() {
        return setExecutionType(NExecutionType.OPEN);
    }

    @Override
    public NRunAs getRunAs() {
        return runAs;
    }

    @Override
    public NExec sudo() {
        return setRunAs(NRunAs.SUDO);
    }

    @Override
    public NExec root() {
        return setRunAs(NRunAs.ROOT);
    }

    @Override
    public NExec currentUser() {
        return setRunAs(NRunAs.CURRENT_USER);
    }

    @Override
    public NExec setRunAs(NRunAs runAs) {
        this.runAs = runAs == null ? NRunAs.currentUser() : runAs;
        return this;
    }

    public Boolean getDry() {
        return dry;
    }

    public NExec setDry(Boolean dry) {
        this.dry = dry;
        return this;
    }

    @Override
    public NExec copyFrom(NExec other) {
        if (other == null) {
            return this;
        }
        super.copyFromWorkspaceCommandBase((NWorkspaceCmdBase) other);
        addCommand(other.getCommand());
        addEnv(other.getEnv());
        addExecutorOptions(other.getExecutorOptions());
        setDirectory(other.getDirectory());
        setIn(other.getIn());
        setOut(other.getOut());
        setErr(other.getErr());
        setFailFast(other.isFailFast());
        setExecutionType(other.getExecutionType());
        setRunAs(other.getRunAs());
        setConnectionString(other.getConnectionString());
        setDry(other.getDry());
        setBot(other.getBot());
        setRawCommand(other.isRawCommand());
        return this;
    }

    @Override
    public NExec copy() {
        return NExec.of().copyFrom(this);
    }

    @Override
    public int getResultCode() {
        if (!executed) {
//            try {
            run();
//            } catch (Exception ex) {
//                // ignore;
//            }
        }
        if (resultException != null && resultException.getExitCode() != NExecutionException.SUCCESS && failFast) {
            throw resultException;
//            checkFailFast(result.getExitCode());
        }
        return resultException == null ? NExecutionException.SUCCESS : resultException.getExitCode();
    }

    @Override
    public List<String> getExecutorOptions() {
        return NCollections.unmodifiableList(executorOptions);
    }

    @Override
    public NOptional<NExecutionException> getResultException() {
        if (!executed) {
            run();
        }
        return NOptional.ofNamed(resultException, "result-exception");
    }

    public long getSleepMillis() {
        return sleepMillis;
    }

    public NExec setSleepMillis(long sleepMillis) {
        this.sleepMillis = sleepMillis;
        return this;
    }

    protected String getExtraErrorMessage() {
        if (getErr().getType() == NRedirectType.REDIRECT) {
            if (getOut().getType() == NRedirectType.GRAB_FILE
                    || getOut().getType() == NRedirectType.GRAB_STREAM) {
                if (getOut() != null && getOut().getResultSource().isPresent()) {
                    return getGrabbedOutString();
                }
            }
        } else {
            if (getErr().getType() == NRedirectType.GRAB_FILE
                    || getErr().getType() == NRedirectType.GRAB_STREAM) {
                if (getErr() != null && getErr().getResultSource().isPresent()) {
                    return getGrabbedErrString();
                }
            }
            if (getOut().getType() == NRedirectType.GRAB_FILE
                    || getOut().getType() == NRedirectType.GRAB_STREAM) {
                if (getOut() != null && getOut().getResultSource().isPresent()) {
                    return getGrabbedOutString();
                }
            }
        }
        return null;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().orNull();
        if (a == null) {
            return false;
        }
        if (command == null) {
            command = new ArrayList<>();
        }
        if (!command.isEmpty()) {
            cmdLine.next();
            command.add(a.asString().get());
            return true;
        }
        boolean enabled = a.isUncommented();
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
            case "--user-cmd"://Deprecated as of 0.8.1
            case "--system": {
                cmdLine.skip();
                if (enabled) {
                    system();
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
                NArg s = cmdLine.nextEntry().get();
                if (enabled) {
                    setRunAs(NRunAs.user(s.getStringValue().ifBlankEmpty().get()));
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
            case "--dry":
            case "-d": {
                return cmdLine.matcher().matchFlag((v) -> setDry(v.booleanValue())).anyMatch();
            }
            case "--target": {
                return cmdLine.matcher().matchEntry((v) -> this.setConnectionString(v.stringValue())).anyMatch();
            }
            case "--rerun": {
                return cmdLine.matcher().matchFlag((v) -> this.multipleRuns = v.booleanValue()).anyMatch();
            }
            case "--rerun-min-time": {
                return cmdLine.matcher().matchEntry((v) -> this.multipleRunsMinTimeMs = NLiteral.of(v).asLong().get()).anyMatch();
            }
            case "--rerun-safe-time": {
                return cmdLine.matcher().matchEntry((v) -> this.multipleRunsSafeTimeMs = NLiteral.of(v).asLong().get()).anyMatch();
            }
            case "--rerun-max-count": {
                return cmdLine.matcher().matchEntry((v) -> this.multipleRunsMaxCount = NLiteral.of(v).asInt().get()).anyMatch();
            }
            case "--cron": {
                return cmdLine.matcher().matchEntry((v) -> this.multipleRunsCron = v.stringValue()).anyMatch();
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                cmdLine.skip();
                if (a.isOption()) {
                    addExecutorOption(a.asString().get());
                } else {
                    addCommand(a.asString().get());
                    addCommand(cmdLine.toStringArray());
                    cmdLine.skipAll();
                }
                return true;
            }
        }
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
        NDefinition d = getCommandDefinition();
        if (d != null) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(NStringUtils.formatStringLiteral(d.getId().toString()));
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
        switch (getOut().getType()) {
            case PATH: {
                if (Arrays.stream(getOut().getOptions()).anyMatch(x -> x == NPathOption.APPEND)) {
                    sb.append(" >> ");
                } else {
                    sb.append(" > ");
                }
                sb.append(getOut().getPath());
                break;
            }
            case NULL: {
                sb.append(" > /dev/null ");
                break;
            }
        }
        switch (getErr().getType()) {
            case PATH: {
                if (Arrays.stream(getOut().getOptions()).anyMatch(x -> x == NPathOption.APPEND)) {
                    sb.append(" 2>> ");
                } else {
                    sb.append(" 2> ");
                }
                sb.append(getOut().getPath());
                break;
            }
            case REDIRECT: {
                sb.append(" 2>&1 ");
                break;
            }
            case NULL: {
                sb.append(" 2> /dev/null ");
                break;
            }
        }
        switch (getIn().getType()) {
            case PATH: {
                sb.append(" < ");
                sb.append(getOut().getPath());
                break;
            }
            case NULL: {
                sb.append(" < /dev/null");
                break;
            }
        }
        return sb.toString();
    }

    public String toString() {
        return getCommandString();
    }

    public NConnectionString getConnectionString() {
        return connectionString;
    }

    public NExec setConnectionString(String connectionString) {
        this.connectionString = NBlankable.isBlank(connectionString) ? null : NConnectionString.of(connectionString);
        return this;
    }

    @Override
    public NExec at(String connectionString) {
        return setConnectionString(connectionString);
    }

    @Override
    public NExec at(NConnectionString connectionString) {
        return setConnectionString(connectionString);
    }

    @Override
    public NExec setConnectionString(NConnectionString connectionString) {
        if (!NBlankable.isBlank(connectionString)) {
            this.connectionString = connectionString;
        } else {
            this.connectionString = null;
        }
        return this;
    }

    @Override
    public NExec redirectErr() {
        return setErr(NExecOutput.ofRedirect());
    }

    @Override
    public boolean isRawCommand() {
        return rawCommand;
    }

    @Override
    public NExec setRawCommand(boolean rawCommand) {
        this.rawCommand = rawCommand;
        return this;
    }

}
