package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NFormat;
import net.thevpc.nuts.format.NFormattable;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.executor.system.ProcessBuilder2;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NString;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public abstract class AbstractNExecCmd extends NWorkspaceCmdBase<NExecCmd> implements NExecCmd, NFormattable {

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
    protected boolean failFast;
    private long sleepMillis = 1000;
    private String target;

    public AbstractNExecCmd(NSession session) {
        super(session, "exec");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NExecCmdFormat formatter() {
        return NExecCmdFormat.of(getSession()).setValue(this);
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
    public NExecCmd setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public NExecCmd failFast() {
        return setFailFast(true);
    }

    @Override
    public List<String> getCommand() {
        return CoreCollectionUtils.unmodifiableList(command);
    }

    @Override
    public NExecCmd setCommand(String... command) {
        this.command = null;
        return addCommand(command);
    }

    @Override
    public NExecCmd setCommand(Collection<String> command) {
        this.command = null;
        return addCommand(command);
    }

    @Override
    public NExecCmd setCommandDefinition(NDefinition definition) {
        this.commandDefinition = definition;
        if (this.commandDefinition != null) {
            this.commandDefinition.getContent().get(session);
            this.commandDefinition.getDependencies().get(session);
            this.commandDefinition.getEffectiveDescriptor().get(session);
//            this.commandDefinition.getInstallInformation().get(session);
        }
        return this;
    }

    public NDefinition getCommandDefinition() {
        return commandDefinition;
    }

    @Override
    public NExecCmd addCommand(NPath path) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        if (path != null) {
            this.command.add(path.toString());
        }
        return this;
    }

    @Override
    public NExecCmd addCommand(String... command) {
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
    public NExecCmd addCommand(Collection<String> command) {
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
    public NExecCmd clearCommand() {
        this.command = null;
        return this;
    }

    @Override
    public NExecCmd addExecutorOption(String executorOption) {
        if (executorOption != null) {
            if (this.executorOptions == null) {
                this.executorOptions = new ArrayList<>();
            }
            this.executorOptions.add(executorOption);
        }
        return this;
    }

    @Override
    public NExecCmd addExecutorOptions(String... executorOptions) {
        if (executorOptions != null) {
            for (String executorOption : executorOptions) {
                addExecutorOption(executorOption);
            }
        }
        return this;
    }

    @Override
    public NExecCmd setExecutorOptions(Collection<String> executorOptions) {
        this.executorOptions = new ArrayList<>();
        if (executorOptions != null) {
            for (String executorOption : executorOptions) {
                addExecutorOption(executorOption);
            }
        }
        return this;
    }

    @Override
    public NExecCmd addExecutorOptions(Collection<String> executorOptions) {
        if (executorOptions != null) {
            for (String executorOption : executorOptions) {
                addExecutorOption(executorOption);
            }
        }
        return this;
    }

    @Override
    public NExecCmd clearExecutorOptions() {
        this.executorOptions = null;
        return this;
    }

    @Override
    public List<String> getWorkspaceOptions() {
        return CoreCollectionUtils.unmodifiableList(workspaceOptions);
    }

    @Override
    public NExecCmd clearWorkspaceOptions(String workspaceOptions) {
        this.workspaceOptions = null;
        return this;
    }

    @Override
    public NExecCmd addWorkspaceOptions(NWorkspaceOptions workspaceOptions) {
        if (workspaceOptions != null) {
            addWorkspaceOptions(workspaceOptions.toCmdLine().toString());
        }
        return this;
    }

    @Override
    public NExecCmd addWorkspaceOptions(String workspaceOptions) {
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
    public NExecCmd setEnv(Map<String, String> env) {
        clearEnv();
        addEnv(env);
        return this;
    }

    @Override
    public NExecCmd addEnv(Map<String, String> env) {
        if (env != null) {
            for (Map.Entry<String, String> entry : env.entrySet()) {
                setEnv(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public NExecCmd setEnv(String key, String value) {
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
    public NExecCmd clearEnv() {
        this.env = null;
        return this;
    }

    @Override
    public NPath getDirectory() {
        return directory;
    }

    @Override
    public NExecCmd setDirectory(NPath directory) {
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
    public NExecCmd setIn(NExecInput in) {
        this.in = in == null ? NExecInput.ofInherit() : in;
        return this;
    }

    @Override
    public NExecOutput getOut() {
        return out;
    }

    @Override
    public NExecCmd setOut(NExecOutput out) {
        checkSession();
        this.out = out == null ? NExecOutput.ofInherit() : out;
        return this;
    }

    @Override
    public NExecCmd grabOut() {
        checkSession();
        // DO NOT CALL setOut :: setOut(new SPrintStream());
        this.out = NExecOutput.ofGrabMem();
        return this;
    }

    @Override
    public NExecCmd grabAll() {
        return grabOut().redirectErr();
    }

    @Override
    public NExecCmd grabOutOnly() {
        return grabOut().setErr(NExecOutput.ofNull());
    }

    @Override
    public NExecCmd grabErr() {
        checkSession();
        setErr(NExecOutput.ofGrabMem());
        return this;
    }

    @Override
    public String getGrabbedAllString() {
        checkSession();
        if (!executed) {
            grabAll();
        }
        return getGrabbedOutString();
    }

    @Override
    public String getGrabbedOutOnlyString() {
        checkSession();
        if (!executed) {
            if (out.getType() != NRedirectType.GRAB_STREAM) {
                grabOutOnly();
            }
        }
        return getGrabbedOutString();
    }

    @Override
    public String getGrabbedOutString() {
        checkSession();
        if (!executed) {
            if (out.getType() != NRedirectType.GRAB_STREAM) {
                grabOut();
            }
            run();
        }
        if (getOut() == null) {
            throw new NIllegalArgumentException(getSession(), NMsg.ofPlain("no buffer was configured; should call grabOut"));
        }
        if (getOut().getResultSource().isNotPresent()) {
            if (getOut().getType() == NRedirectType.GRAB_FILE || getOut().getType() == NRedirectType.GRAB_STREAM) {
                if (getResultException().isPresent()) {
                    throw getResultException().get();
                }
            }
        }
        return getOut().getResultString();
    }

    @Override
    public String getGrabbedErrString() {
        checkSession();
        if (!executed) {
            if (err.getType() != NRedirectType.GRAB_STREAM) {
                grabErr();
            }
            run();
        }
        if (getErr() == null) {
            throw new NIllegalArgumentException(getSession(), NMsg.ofPlain("no buffer was configured; should call grabErr"));
        }
        if (getErr().getType() == NRedirectType.REDIRECT) {
            return getGrabbedOutString();
        }
        if (getErr().getResultSource().isNotPresent()) {
            if (getErr().getType() == NRedirectType.GRAB_FILE || getErr().getType() == NRedirectType.GRAB_STREAM) {
                if (getResultException().isPresent()) {
                    throw getResultException().get();
                }
            }
        }
        return getErr().getResultString();
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
    public NExecCmd setErr(NExecOutput err) {
        checkSession();
        this.err = err;
        return this;
    }

    @Override
    public NExecutionType getExecutionType() {
        return executionType;
    }

    @Override
    public NExecCmd setExecutionType(NExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public NExecCmd system() {
        return setExecutionType(NExecutionType.SYSTEM);
    }

    @Override
    public NExecCmd embedded() {
        return setExecutionType(NExecutionType.EMBEDDED);
    }

    @Override
    public NExecCmd spawn() {
        return setExecutionType(NExecutionType.SPAWN);
    }

    @Override
    public NExecCmd open() {
        return setExecutionType(NExecutionType.OPEN);
    }

    @Override
    public NRunAs getRunAs() {
        return runAs;
    }

    @Override
    public NExecCmd sudo() {
        return setRunAs(NRunAs.SUDO);
    }

    @Override
    public NExecCmd root() {
        return setRunAs(NRunAs.ROOT);
    }

    @Override
    public NExecCmd currentUser() {
        return setRunAs(NRunAs.CURRENT_USER);
    }

    @Override
    public NExecCmd setRunAs(NRunAs runAs) {
        this.runAs = runAs == null ? NRunAs.currentUser() : runAs;
        return this;
    }

    @Override
    public NExecCmd setAll(NExecCmd other) {
        super.copyFromWorkspaceCommandBase((NWorkspaceCmdBase) other);
        addCommand(other.getCommand());
        addEnv(other.getEnv());
        addExecutorOptions(other.getExecutorOptions());
        setDirectory(other.getDirectory());
        setIn(other.getIn());
        setOut(other.getOut());
        setErr(other.getErr());
        setSession(other.getSession());
        setFailFast(other.isFailFast());
        setExecutionType(other.getExecutionType());
        setRunAs(other.getRunAs());
        setTarget(other.getTarget());
        return this;
    }

    @Override
    public NExecCmd copy() {
        return NExecCmd.of(session).setAll(this);
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
        return CoreCollectionUtils.unmodifiableList(executorOptions);
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

    public NExecCmd setSleepMillis(long sleepMillis) {
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
                NArg s = cmdLine.nextEntry().get(session);
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
            case "-dry":
            case "-d": {
                boolean val = cmdLine.nextFlag().get(session).getBooleanValue().get(session);
                if (enabled) {
                    getSession().setDry(val);
                }
                return true;
            }
            case "--target": {
                cmdLine.withNextEntry((v, ar, s) -> this.setTarget(v));
                return true;
            }
            case "--rerun": {
                cmdLine.withNextFlag((v, ar, s) -> this.multipleRuns = v);
                return true;
            }
            case "--rerun-min-time": {
                cmdLine.withNextEntry((v, ar, s) -> this.multipleRunsMinTimeMs = NLiteral.of(v).asLong().get(s));
                return true;
            }
            case "--rerun-safe-time": {
                cmdLine.withNextEntry((v, ar, s) -> this.multipleRunsSafeTimeMs = NLiteral.of(v).asLong().get(s));
                return true;
            }
            case "--rerun-max-count": {
                cmdLine.withNextEntry((v, ar, s) -> this.multipleRunsMaxCount = NLiteral.of(v).asInt().get(s));
                return true;
            }
            case "--cron": {
                cmdLine.withNextEntry((v, ar, s) -> this.multipleRunsCron = v);
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

    @Override
    public NFormat formatter(NSession session) {
        return NFormat.of(session, new NFormatSPI() {

            @Override
            public String getName() {
                return "NutsExecCommand";
            }

            @Override
            public void print(NPrintStream out) {
                out.print(NCmdLine.of(command));
            }

            @Override
            public boolean configureFirst(NCmdLine cmdLine) {
                return false;
            }
        });
    }

    public String getTarget() {
        return target;
    }

    public NExecCmd setTarget(String host) {
        this.target = host;
        return this;
    }

    @Override
    public NExecCmd redirectErr() {
        return setErr(NExecOutput.ofRedirect());
    }
}
