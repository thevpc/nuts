package net.thevpc.nuts.runtime.standalone.wscommands.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.bundles.io.ProcessBuilder2;
import net.thevpc.nuts.runtime.core.format.DefaultNutsExecCommandFormat;
import net.thevpc.nuts.runtime.standalone.io.NutsByteArrayPrintStream;
import net.thevpc.nuts.runtime.standalone.wscommands.NutsWorkspaceCommandBase;

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
    protected boolean dry;
    private boolean inheritSystemIO;
    private String redirectOuputFile;
    private String redirectInpuFile;

    public AbstractNutsExecCommand(NutsWorkspace ws) {
        super(ws, "exec");
    }

    @Override
    public NutsExecCommandFormat formatter() {
        return (NutsExecCommandFormat) new DefaultNutsExecCommandFormat(ws).setValue(this).setSession(getSession());
    }

    @Override
    public NutsExecCommand setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public String[] getCommand() {
        return command == null ? new String[0] : command.toArray(new String[0]);
    }

    @Override
    public NutsExecCommand setCommand(NutsDefinition definition) {
        this.commandDefinition = definition;
        if (this.commandDefinition != null) {
            this.commandDefinition.getContent();
            this.commandDefinition.getDependencies();
            this.commandDefinition.getEffectiveDescriptor();
            this.commandDefinition.getInstallInformation();
        }
        return this;
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
    public NutsExecCommand addCommand(String... command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        this.command.addAll(Arrays.asList(command));
        return this;
    }

    @Override
    public NutsExecCommand clearCommand() {
        this.command = null;
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
    public Map<String, String> getEnv() {
        return env;
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
    public NutsExecCommand setEnv(Map<String, String> env) {
        clearEnv();
        addEnv(env);
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

    @Override
    public boolean isInheritSystemIO() {
        return inheritSystemIO;
    }

    @Override
    public NutsExecCommand setInheritSystemIO(boolean inheritSystemIO) {
        this.inheritSystemIO = inheritSystemIO;
        return this;
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
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("no buffer was configured; should call grabOutputString"));
    }

    public String getOutputString0() {
        checkSession();
        NutsPrintStream o = getOut();
        if (o instanceof SPrintStream) {
            return o.toString();
        }
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("no buffer was configured; should call grabOutputString"));
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
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("no buffer was configured; should call grabErrorString"));
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
    public NutsPrintStream getErr() {
        return err;
    }
//
//    @Override
//    public PrintStream err() {
//        return getErr();
//    }

    @Override
    public NutsExecutionType getExecutionType() {
        return executionType;
    }

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
    public NutsExecCommand setExecutionType(NutsExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public NutsRunAs getRunAs() {
        return runAs;
    }

    @Override
    public NutsExecCommand setRunAs(NutsRunAs runAs) {
        this.runAs = runAs==null?NutsRunAs.currentUser() : runAs;
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
        setRunAs(other.getRunAs());
        return this;
    }

    @Override
    public NutsExecCommand copy() {
        return ws.exec().copyFrom(this);
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
    public String[] getExecutorOptions() {
        return executorOptions == null ? new String[0] : executorOptions.toArray(new String[0]);
    }

    @Override
    public NutsExecutionException getResultException() {
        if (!executed) {
            run();
        }
        return result;
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
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        if (command == null) {
            command = new ArrayList<>();
        }
        if (!command.isEmpty()) {
            command.add(a.getString());
            return true;
        }
        boolean enabled = a.isEnabled();
        switch (a.getKey().getString()) {
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
            case "--open-file":{
                cmdLine.skip();
                if (enabled) {
                    setExecutionType(NutsExecutionType.OPEN);
                }
                return true;
            }
            case "--user-cmd":
            case "--system":
            {
                cmdLine.skip();
                if (enabled) {
                    setExecutionType(NutsExecutionType.SYSTEM);
                }
                return true;
            }
            case "--current-user":
            {
                cmdLine.skip();
                if (enabled) {
                    setRunAs(NutsRunAs.currentUser());
                }
                return true;
            }
            case "--as-root":
            {
                cmdLine.skip();
                if (enabled) {
                    setRunAs(NutsRunAs.ROOT);
                }
                return true;
            }
            case "--run-as":
            {
                NutsArgument s = cmdLine.nextString();
                if (enabled) {
                    if(NutsBlankable.isBlank(s.getValue().getString())){
                        throw new NutsIllegalArgumentException(getSession(),NutsMessage.cstyle("missing user name"));
                    }
                    setRunAs(NutsRunAs.user(s.getValue().getString()));
                }
                return true;
            }
            case "--sudo":
            {
                cmdLine.skip();
                if (enabled) {
                    setRunAs(NutsRunAs.sudo());
                }
                return true;
            }
            case "--inherit-system-io": {
                NutsArgument val = cmdLine.nextBoolean();
                if (enabled) {
                    setInheritSystemIO(val.getValue().getBoolean());
                }
                return true;
            }
            case "-dry":
            case "-d": {
                boolean val = cmdLine.nextBoolean().getValue().getBoolean();
                if (enabled) {
                    setDry(val);
                }
                return true;
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                cmdLine.skip();
                if (a.isOption()) {
                    addExecutorOption(a.getString());
                } else {
                    addCommand(a.getString());
                }
                return true;
            }
        }
    }

    @Override
    public boolean isDry() {
        return dry;
    }

    @Override
    public NutsExecCommand setDry(boolean value) {
        this.dry = value;
        return this;
    }

    public boolean isGrabOutputString() {
        return out instanceof SPrintStream;
    }

    public boolean isGrabErrorString() {
        return err instanceof SPrintStream;
    }

    protected static class SPrintStream extends NutsByteArrayPrintStream {

        public SPrintStream(NutsSession session) {
            super(session);
        }

        protected SPrintStream(ByteArrayOutputStream bos, NutsSession session) {
            super(bos, session);
        }

        @Override
        public NutsPrintStream convertSession(NutsSession session) {
            if(session==null || session==this.session){
                return this;
            }
            return new SPrintStream((ByteArrayOutputStream) out,session);
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
                sb.append(CoreStringUtils.enforceDoubleQuote(k)).append("=").append(CoreStringUtils.enforceDoubleQuote(v));
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
            sb.append(CoreStringUtils.enforceDoubleQuote(s));
        }
//        if (baseIO) {
////            ProcessBuilder.Redirect r;
//            if (f == null || f.acceptRedirectOutput()) {
//                sb.append(" > ").append("{?}");
////                r = base.redirectOutput();
////                if (null == r.type()) {
////                    sb.append(" > ").append("{?}");
////                } else {
////                    switch (r.type()) {
////                        //sb.append(" > ").append("{inherited}");
////                        case INHERIT:
////                            break;
////                        case PIPE:
////                            break;
////                        case WRITE:
////                            sb.append(" > ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
////                            break;
////                        case APPEND:
////                            sb.append(" >> ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
////                            break;
////                        default:
////                            sb.append(" > ").append("{?}");
////                            break;
////                    }
////                }
//            }
//            if (f == null || f.acceptRedirectError()) {
//                if (isRedirectErrorStream()) {
//                    sb.append(" 2>&1");
//                } else {
//                    if (f == null || f.acceptRedirectError()) {
//                        sb.append(" 2> ").append("{?}");
//                    }
////                    if (f == null || f.acceptRedirectError()) {
////                        r = base.redirectError();
////                        if (null == r.type()) {
////                            sb.append(" 2> ").append("{?}");
////                        } else {
////                            switch (r.type()) {
////                                //sb.append(" 2> ").append("{inherited}");
////                                case INHERIT:
////                                    break;
////                                case PIPE:
////                                    break;
////                                case WRITE:
////                                    sb.append(" 2> ").append(r.file().getPath());
////                                    break;
////                                case APPEND:
////                                    sb.append(" 2>> ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
////                                    break;
////                                default:
////                                    sb.append(" 2> ").append("{?}");
////                                    break;
////                            }
////                        }
////                    }
//                }
//            }
//            if (f == null || f.acceptRedirectInput()) {
//                sb.append(" < ").append("{?}");
////                r = base.redirectInput();
////                if (null == r.type()) {
////                    sb.append(" < ").append("{?}");
////                } else {
////                    switch (r.type()) {
////                        //sb.append(" < ").append("{inherited}");
////                        case INHERIT:
////                            break;
////                        case PIPE:
////                            break;
////                        case READ:
////                            sb.append(" < ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
////                            break;
////                        default:
////                            sb.append(" < ").append("{?}");
////                            break;
////                    }
////                }
//            }
//        } else if (isRedirectErrorStream()) {
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

    private int sleepMillis = 1000;

    public int getSleepMillis() {
        return sleepMillis;
    }

    public NutsExecCommand setSleepMillis(int sleepMillis) {
        this.sleepMillis = sleepMillis;
        return this;
    }

    public String getRedirectOuputFile() {
        return redirectOuputFile;
    }

    public NutsExecCommand setRedirectOuputFile(String redirectOuputFile) {
        this.redirectOuputFile = redirectOuputFile;
        return this;
    }

    public String getRedirectInpuFile() {
        return redirectInpuFile;
    }

    public NutsExecCommand setRedirectInpuFile(String redirectInpuFile) {
        this.redirectInpuFile = redirectInpuFile;
        return this;
    }

}
