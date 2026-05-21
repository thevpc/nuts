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
    private NDuration sleepMillis = NDuration.ofSeconds(1);
    private int maxLines = -1;
    private int maxBytes = -1;
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
    public NExec bot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public NExec maxLines(int maxLines) {
        this.maxLines = maxLines;
        return this;
    }

    @Override
    public NExec maxBytes(int maxBytes) {
        this.maxBytes = maxBytes;
        return this;
    }

    @Override
    public Boolean bot() {
        return bot;
    }

    @Override
    public NExec failFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public List<String> command() {
        return NCollections.unmodifiableList(command);
    }

    @Override
    public NExec command(String... command) {
        this.command = null;
        return addCommand(command);
    }

    @Override
    public NExec command(Collection<String> command) {
        this.command = null;
        return addCommand(command);
    }

    @Override
    public NExec commandDefinition(NDefinition definition) {
        this.commandDefinition = definition;
        if (this.commandDefinition != null) {
//            this.commandDefinition.getContent().get();
//            this.commandDefinition.getDependencies().get();
//            this.commandDefinition.getEffectiveDescriptor().get();
//            this.commandDefinition.getInstallInformation().get(session);
        }
        return this;
    }

    public NDefinition commandDefinition() {
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
    public NExec executorOptions(Collection<String> executorOptions) {
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
    public List<String> workspaceOptions() {
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
    public Map<String, String> env() {
        return env;
    }

    @Override
    public NExec env(Map<String, String> env) {
        clearEnv();
        addEnv(env);
        return this;
    }

    @Override
    public NExec addEnv(Map<String, String> env) {
        if (env != null) {
            for (Map.Entry<String, String> entry : env.entrySet()) {
                env(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public NExec env(String key, String value) {
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
    public NPath directory() {
        return directory;
    }

    @Override
    public NExec directory(NPath directory) {
        this.directory = directory;
        return this;
    }

    @Override
    public NExecInput in() {
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
    public NExec in(NExecInput in) {
        this.in = in == null ? NExecInput.ofInherit() : in;
        return this;
    }

    @Override
    public NExecOutput out() {
        return out;
    }

    @Override
    public NExec out(NExecOutput out) {
        this.out = out == null ? NExecOutput.ofInherit() : out;
        return this;
    }

    @Override
    public NExec grabOut() {
        // DO NOT CALL setOut :: setOut(new SPrintStream());
        this.out = NExecOutput.ofGrabMem(maxBytes, maxLines);
        return this;
    }

    @Override
    public NExec grabAll() {
        return grabOut().redirectErr();
    }

    @Override
    public NExec grabOutOnly() {
        return grabOut().err(NExecOutput.ofNull());
    }

    @Override
    public NExec grabErr() {
        err(NExecOutput.ofGrabMem(maxBytes, maxLines));
        return this;
    }

    @Override
    public String grabbedAll() {
        if (!executed) {
            grabAll();
        }
        return grabbedOut();
    }

    @Override
    public String grabbedOutOnly() {
        if (!executed) {
            if (out.type() != NRedirectType.GRAB_STREAM) {
                grabOutOnly();
            }
        }
        return grabbedOut();
    }

    @Override
    public String grabbedOut() {
        return new String(grabbedOutBytes());
    }

    @Override
    public byte[] grabbedOutBytes() {
        if (!executed) {
            if (out.type() != NRedirectType.GRAB_STREAM) {
                grabOut();
            }
            run();
        }
        if (out() == null) {
            throw new NIllegalArgumentException(NMsg.ofPlain("no buffer was configured; should call grabOut"));
        }
        if (out().resultSource().isNotPresent()) {
            if (out().type() == NRedirectType.GRAB_FILE || out().type() == NRedirectType.GRAB_STREAM) {
                if (resultException().isPresent()) {
                    throw resultException().get();
                }
            }
        }
        return out().resultBytes();
    }

    @Override
    public byte[] grabbedErrBytes() {
        if (!executed) {
            if (err.type() != NRedirectType.GRAB_STREAM) {
                grabErr();
            }
            run();
        }
        if (err() == null) {
            throw new NIllegalArgumentException(NMsg.ofPlain("no buffer was configured; should call grabErr"));
        }
        if (err().type() == NRedirectType.REDIRECT) {
            return grabbedOutBytes();
        }
        if (err().resultSource().isNotPresent()) {
            if (err().type() == NRedirectType.GRAB_FILE || err().type() == NRedirectType.GRAB_STREAM) {
                if (resultException().isPresent()) {
                    throw resultException().get();
                }
            }
        }
        return err().resultBytes();
    }

    @Override
    public String grabbedErr() {
        return new String(grabbedErrBytes());
    }

    @Override
    public NExecOutput err() {
        return err;
    }

    //    @Override
//    public NutsExecCommand err(PrintStream err) {
//        return setErr(err);
//    }
    @Override
    public NExec err(NExecOutput err) {
        this.err = err;
        return this;
    }

    @Override
    public NExecutionType executionType() {
        return executionType;
    }

    @Override
    public NExec executionType(NExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public NExec system() {
        return executionType(NExecutionType.SYSTEM);
    }

    @Override
    public NExec embedded() {
        return executionType(NExecutionType.EMBEDDED);
    }

    @Override
    public NExec spawn() {
        return executionType(NExecutionType.SPAWN);
    }

    @Override
    public NExec open() {
        return executionType(NExecutionType.OPEN);
    }

    @Override
    public NRunAs runAs() {
        return runAs;
    }

    @Override
    public NExec sudo() {
        return runAs(NRunAs.SUDO);
    }

    @Override
    public NExec root() {
        return runAs(NRunAs.ROOT);
    }

    @Override
    public NExec currentUser() {
        return runAs(NRunAs.CURRENT_USER);
    }

    @Override
    public NExec runAs(NRunAs runAs) {
        this.runAs = runAs == null ? NRunAs.currentUser() : runAs;
        return this;
    }

    public Boolean dry() {
        return dry;
    }

    public NExec dry(Boolean dry) {
        this.dry = dry;
        return this;
    }

    @Override
    public NExec copyFrom(NExec other) {
        if (other == null) {
            return this;
        }
        super.copyFromWorkspaceCommandBase((NWorkspaceCmdBase) other);
        addCommand(other.command());
        addEnv(other.env());
        addExecutorOptions(other.executorOptions());
        directory(other.directory());
        in(other.in());
        out(other.out());
        err(other.err());
        failFast(other.isFailFast());
        executionType(other.executionType());
        runAs(other.runAs());
        connectionString(other.connectionString());
        dry(other.dry());
        bot(other.bot());
        rawCommand(other.isRawCommand());
        return this;
    }

    @Override
    public NExec copy() {
        return NExec.of().copyFrom(this);
    }

    @Override
    public int exitCode() {
        if (!executed) {
//            try {
            run();
//            } catch (Exception ex) {
//                // ignore;
//            }
        }
        if (resultException != null && resultException.exitCode() != NExecutionException.SUCCESS && failFast) {
            throw resultException;
//            checkFailFast(result.getExitCode());
        }
        return resultException == null ? NExecutionException.SUCCESS : resultException.exitCode();
    }

    @Override
    public List<String> executorOptions() {
        return NCollections.unmodifiableList(executorOptions);
    }

    @Override
    public NOptional<NExecutionException> resultException() {
        if (!executed) {
            run();
        }
        return NOptional.ofNamed(resultException, "result-exception");
    }

    public NDuration sleepDuration() {
        return sleepMillis;
    }

    public NExec sleepDuration(NDuration sleepMillis) {
        this.sleepMillis = sleepMillis;
        return this;
    }

    protected String getExtraErrorMessage() {
        if (err().type() == NRedirectType.REDIRECT) {
            if (out().type() == NRedirectType.GRAB_FILE
                    || out().type() == NRedirectType.GRAB_STREAM) {
                if (out() != null && out().resultSource().isPresent()) {
                    return grabbedOut();
                }
            }
        } else {
            if (err().type() == NRedirectType.GRAB_FILE
                    || err().type() == NRedirectType.GRAB_STREAM) {
                if (err() != null && err().resultSource().isPresent()) {
                    return grabbedErr();
                }
            }
            if (out().type() == NRedirectType.GRAB_FILE
                    || out().type() == NRedirectType.GRAB_STREAM) {
                if (out() != null && out().resultSource().isPresent()) {
                    return grabbedOut();
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
                    executionType(NExecutionType.SPAWN);
                }
                return true;
            }
            case "--embedded":
            case "-b": {
                cmdLine.skip();
                if (enabled) {
                    executionType(NExecutionType.EMBEDDED);
                }
                return true;
            }
            case "--open-file": {
                cmdLine.skip();
                if (enabled) {
                    executionType(NExecutionType.OPEN);
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
                    runAs(NRunAs.currentUser());
                }
                return true;
            }
            case "--as-root": {
                cmdLine.skip();
                if (enabled) {
                    runAs(NRunAs.ROOT);
                }
                return true;
            }
            case "--run-as": {
                NArg s = cmdLine.nextEntry().get();
                if (enabled) {
                    runAs(NRunAs.user(s.getStringValue().onBlankEmpty().get()));
                }
                return true;
            }
            case "--sudo": {
                cmdLine.skip();
                if (enabled) {
                    runAs(NRunAs.sudo());
                }
                return true;
            }
            case "--dry":
            case "-d": {
                return cmdLine.matcher().withAny().matchFlag((v) -> dry(v.booleanValue())).anyMatch();
            }
            case "--target": {
                return cmdLine.matcher().withAny().matchEntry((v) -> this.connectionString(v.stringValue())).anyMatch();
            }
            case "--rerun": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.multipleRuns = v.booleanValue()).anyMatch();
            }
            case "--rerun-min-time": {
                return cmdLine.matcher().withAny().matchEntry((v) -> this.multipleRunsMinTimeMs = NLiteral.of(v).asLong().get()).anyMatch();
            }
            case "--rerun-safe-time": {
                return cmdLine.matcher().withAny().matchEntry((v) -> this.multipleRunsSafeTimeMs = NLiteral.of(v).asLong().get()).anyMatch();
            }
            case "--rerun-max-count": {
                return cmdLine.matcher().withAny().matchEntry((v) -> this.multipleRunsMaxCount = NLiteral.of(v).asInt().get()).anyMatch();
            }
            case "--cron": {
                return cmdLine.matcher().withAny().matchEntry((v) -> this.multipleRunsCron = v.stringValue()).anyMatch();
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
        NDefinition d = commandDefinition();
        if (d != null) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(NStringUtils.formatStringLiteral(d.id().toString()));
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
        switch (out().type()) {
            case PATH: {
                if (out().options().stream().anyMatch(x -> x == NPathOption.APPEND)) {
                    sb.append(" >> ");
                } else {
                    sb.append(" > ");
                }
                sb.append(out().path());
                break;
            }
            case NULL: {
                sb.append(" > /dev/null ");
                break;
            }
        }
        switch (err().type()) {
            case PATH: {
                if (out().options().stream().anyMatch(x -> x == NPathOption.APPEND)) {
                    sb.append(" 2>> ");
                } else {
                    sb.append(" 2> ");
                }
                sb.append(out().path());
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
        switch (in().type()) {
            case PATH: {
                sb.append(" < ");
                sb.append(out().path());
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

    public NConnectionString connectionString() {
        return connectionString;
    }

    public NExec connectionString(String connectionString) {
        this.connectionString = NBlankable.isBlank(connectionString) ? null : NConnectionString.of(connectionString);
        return this;
    }

    @Override
    public NExec at(String connectionString) {
        return connectionString(connectionString);
    }

    @Override
    public NExec at(NConnectionString connectionString) {
        return connectionString(connectionString);
    }

    @Override
    public NExec connectionString(NConnectionString connectionString) {
        if (!NBlankable.isBlank(connectionString)) {
            this.connectionString = connectionString;
        } else {
            this.connectionString = null;
        }
        return this;
    }

    @Override
    public NExec redirectErr() {
        return err(NExecOutput.ofRedirect());
    }

    @Override
    public boolean isRawCommand() {
        return rawCommand;
    }

    @Override
    public NExec rawCommand(boolean rawCommand) {
        this.rawCommand = rawCommand;
        return this;
    }

}
