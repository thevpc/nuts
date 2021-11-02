/**
 * ====================================================================
 * vpc-common-io : common reusable library for
 * input/output
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.bundles.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.app.DefaultNutsArgument;
import net.thevpc.nuts.runtime.core.app.NutsCommandLineShellOptions;
import net.thevpc.nuts.runtime.core.shell.NutsShellHelper;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.io.*;
import java.util.*;

public class ProcessBuilder2 {

    private List<String> command = new ArrayList<>();
    private Map<String, String> env;
    private File directory;
    private ProcessBuilder base = new ProcessBuilder();
    private InputStream in;
    private PrintStream out;
    private PrintStream err;
    private int result;
    private boolean baseIO;
    private boolean failFast;
    private Process proc;
    private long sleepMillis = 1000;
    private NutsSession session;

    public ProcessBuilder2(NutsSession session) {
        this.session = session;
    }

    private static String formatArg(String s, NutsSession session) {
        DefaultNutsArgument a = new DefaultNutsArgument(s);
        StringBuilder sb = new StringBuilder();
        NutsTexts factory = NutsTexts.of(session);
        if (a.isKeyValue()) {
            if (a.isOption()) {
                sb.append(factory.ofStyled(CoreStringUtils.enforceDoubleQuote(a.getKey().getString(), session), NutsTextStyle.option()));
                sb.append("=");
                sb.append(CoreStringUtils.enforceDoubleQuote(a.getValue().getString(), session));
            } else {
                sb.append(factory.ofStyled(CoreStringUtils.enforceDoubleQuote(a.getKey().getString(), session), NutsTextStyle.primary4()));
                sb.append("=");
                sb.append(CoreStringUtils.enforceDoubleQuote(a.getValue().getString(), session));
            }
        } else {
            if (a.isOption()) {
                sb.append(factory.ofStyled(CoreStringUtils.enforceDoubleQuote(a.getString(), session), NutsTextStyle.option()));
            } else {
                sb.append(CoreStringUtils.enforceDoubleQuote(a.getString(), session));
            }
        }
        return sb.toString();
    }

    public long getSleepMillis() {
        return sleepMillis;
    }

    public ProcessBuilder2 setSleepMillis(long sleepMillis) {
        this.sleepMillis = sleepMillis;
        return this;
    }

    public Process getProc() {
        return proc;
    }

    public List<String> getCommand() {
        return command;
    }

    public ProcessBuilder2 setCommand(String... command) {
        setCommand(Arrays.asList(command));
        return this;
    }

    public ProcessBuilder2 setCommand(List<String> command) {
        this.command = command == null ? null : new ArrayList<>(command);
        return this;
    }

    public ProcessBuilder2 addCommand(String... command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        this.command.addAll(Arrays.asList(command));
        return this;
    }

    public ProcessBuilder2 addCommand(List<String> command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        this.command.addAll(command);
        return this;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public ProcessBuilder2 setEnv(Map<String, String> env) {
        this.env = env == null ? null : new HashMap<>(env);
        return this;
    }

    public ProcessBuilder2 addEnv(Map<String, String> env) {
        if (env != null) {
            if (this.env == null) {
                this.env = new HashMap<>(env);
            } else {
                this.env.putAll(env);
            }
        }
        return this;
    }

    public ProcessBuilder2 setEnv(String k, String val) {
        if (env == null) {
            env = new HashMap<>();
        }
        env.put(k, val);
        return this;
    }

    public File getDirectory() {
        return directory;
    }

    public ProcessBuilder2 setDirectory(File directory) {
        this.directory = directory;
        base.directory(directory);
        return this;
    }

    public ProcessBuilder2 setRedirectFileOutput(File file) {
        base.redirectOutput(file);
        return this;
    }

    public ProcessBuilder2 setRedirectFileInput(File file) {
        base.redirectInput(file);
        return this;
    }

    public InputStream getIn() {
        return in;
    }

    public ProcessBuilder2 setIn(InputStream in) {
        if (baseIO) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("already used base IO redirection"));
        }
        this.in = in;
        return this;
    }

    public PrintStream getOut() {
        return out;
    }

    public ProcessBuilder2 grabOutputString() {
        setOutput(new SPrintStream());
        return this;
    }

    public ProcessBuilder2 grabErrorString() {
        setOutput(new SPrintStream());
        return this;
    }

    public String getOutputString() {
        PrintStream o = getOut();
        if (o instanceof SPrintStream) {
            return ((SPrintStream) o).getStringBuffer();
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("no buffer was configured; should call setOutString"));
    }

    public String getErrorString() {
        if (base.redirectErrorStream()) {
            return getOutputString();
        }
        PrintStream o = getErr();
        if (o instanceof SPrintStream) {
            return ((SPrintStream) o).getStringBuffer();
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("no buffer was configured; should call setErrString"));
    }

    public ProcessBuilder2 setOutput(PrintStream out) {
        if (baseIO) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("already used base IO redirection"));
        }
        this.out = out;
        return this;
    }

    public PrintStream getErr() {
        return err;
    }

    public ProcessBuilder2 setErr(PrintStream err) {
        if (baseIO) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("already used base IO redirection"));
        }
        this.err = err;
        return this;
    }

    public ProcessBuilder2 start() throws IOException {
        if (proc != null) {
            throw new IOException("Already started");
        }
        base.command(command);
        if (env != null) {
            Map<String, String> environment = base.environment();
            for (Map.Entry<String, String> e : env.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                if (k != null) {
                    if (v == null) {
                        v = "";
                    }
                    environment.put(k, v);
                }
            }
        }
        proc = base.start();
        return this;
    }

    public ProcessBuilder2 waitFor() throws IOException {
        if (proc == null) {
            start();
        }
        if (proc == null) {
            throw new IOException("Not started");
        }
        if (!baseIO) {
            NonBlockingInputStreamAdapter procInput;
            NonBlockingInputStreamAdapter procError;
            NonBlockingInputStreamAdapter termIn = null;
            List<PipeThread> pipes = new ArrayList<>();
            String procString = proc.toString();
            if (out != null) {
                procInput = new NonBlockingInputStreamAdapter("pipe-out-proc-" + procString, proc.getInputStream());
                pipes.add(pipe("pipe-out-proc-" + procString, procInput, out));
            }
            if (err != null) {
                procError = new NonBlockingInputStreamAdapter("pipe-err-proc-" + procString, proc.getErrorStream());
                if (base.redirectErrorStream()) {
                    pipes.add(pipe("pipe-err-proc-" + procString, procError, out));
                } else {
                    pipes.add(pipe("pipe-err-proc-" + procString, procError, err));
                }
            }
            if (in != null) {
                termIn = new NonBlockingInputStreamAdapter("pipe-in-proc-" + procString, in);
                pipes.add(pipe("pipe-in-proc-" + procString, termIn, proc.getOutputStream()));
            }
            while (proc.isAlive()) {
                if (termIn != null) {
                    if (!termIn.hasMoreBytes() && termIn.available() == 0) {
                        termIn.close();
                    }
                }
                boolean allFinished = true;
                for (PipeThread pipe : pipes) {
                    if (!pipe.isStopped()) {
                        allFinished = false;
                    } else {
                        pipe.getOut().close();
                    }
                }
                if (allFinished) {
                    break;
                }
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException e) {
                    throw new IOException(CoreStringUtils.exceptionToString(e));
                }
            }

            proc.getInputStream().close();
            proc.getErrorStream().close();
            proc.getOutputStream().close();

            waitFor0();
            for (PipeThread pipe : pipes) {
                pipe.requestStop();
            }
        } else {
            waitFor0();
        }
        return this;
    }

    private void waitFor0() throws IOException {
        try {
            result = proc.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(CoreStringUtils.exceptionToString(e));
        }
        if (result != 0) {
            if (isFailFast()) {
                if (base.redirectErrorStream()) {
                    if (isGrabOutputString()) {
                        throw new NutsExecutionException(session,
                                NutsMessage.cstyle("execution failed with code %d and message : %s. Command was %s", result, getOutputString(),
                                        NutsCommandLine.of(getCommand(),session))
                                , result);
                    }
                } else {
                    if (isGrabErrorString()) {
                        throw new NutsExecutionException(session,
                                NutsMessage.cstyle("execution failed with code %d and message : %s. Command was %s", result, getOutputString(),
                                        NutsCommandLine.of(getCommand(),session))
                                , result);
                    }
                    if (isGrabOutputString()) {
                        throw new NutsExecutionException(session, NutsMessage.cstyle(
                                "execution failed with code %d and message : %s. Command was %s", result, getOutputString(),
                                NutsCommandLine.of(getCommand(),session)
                                ), result);
                    }
                }
                throw new NutsExecutionException(session, NutsMessage.cstyle("execution failed with code %d. Command was %s", result,
                        NutsCommandLine.of(getCommand(),session)
                        ), result);
            }
        }
    }

    public boolean isGrabOutputString() {
        return !baseIO && (out instanceof SPrintStream);
    }

    public boolean isGrabErrorString() {
        return !baseIO && (err instanceof SPrintStream);
    }

    private ProcessBuilder2 waitFor2() throws IOException {
        if (proc == null) {
            start();
        }
        if (proc == null) {
            throw new IOException("Not started");
        }
        NonBlockingInputStreamAdapter procInput = null;
        NonBlockingInputStreamAdapter procError = null;
        NonBlockingInputStreamAdapter termIn = null;
        MultiPipeThread mp = new MultiPipeThread("pipe-out-proc-" + proc.toString());
        if (out != null) {
            procInput = new NonBlockingInputStreamAdapter("pipe-out-proc-" + proc.toString(), proc.getInputStream());
            mp.add("pipe-out-proc-" + proc.toString(), procInput, out);
        }
        if (!base.redirectErrorStream()) {
            if (err != null) {
                procError = new NonBlockingInputStreamAdapter("pipe-err-proc-" + proc.toString(), proc.getErrorStream());
                mp.add("pipe-err-proc-" + proc.toString(), procError, err);
            }
        }
        if (in != null) {
            termIn = new NonBlockingInputStreamAdapter("pipe-in-proc-" + proc.toString(), in);
            mp.add("pipe-in-proc-" + proc.toString(), termIn, proc.getOutputStream());
        }
        mp.start();
        while (proc.isAlive()) {
            if (termIn != null) {
                if (!termIn.hasMoreBytes() && termIn.available() == 0) {
                    termIn.close();
                }
            }
            if (mp.isEmpty()) {
                break;
            }
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
        proc.getInputStream().close();
        proc.getErrorStream().close();
        proc.getOutputStream().close();
        try {
            result = proc.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        mp.requestStop();
        return this;
    }

    public int getResult() {
        return result;
    }

    public Process getProcess() {
        return proc;
    }

    public ProcessBuilder2 inheritIO() {
        this.baseIO = true;
        base.inheritIO();
        return this;
    }

    public ProcessBuilder2 redirectInput(ProcessBuilder.Redirect source) {
        base.redirectInput(source);
        baseIO = true;
        return this;
    }

    public ProcessBuilder2 redirectOutput(ProcessBuilder.Redirect source) {
        base.redirectOutput(source);
        baseIO = true;
        return this;
    }

    public ProcessBuilder2 redirectInput(File source) {
        base.redirectInput(source);
        baseIO = true;
        return this;
    }

    public ProcessBuilder2 redirectOutput(File source) {
        base.redirectOutput(source);
        baseIO = true;
        return this;
    }

    public ProcessBuilder2 redirectError(File source) {
        base.redirectError(source);
        baseIO = true;
        return this;
    }

    public ProcessBuilder.Redirect getRedirectInput() {
        return base.redirectInput();
    }

    public ProcessBuilder.Redirect getRedirectOutput() {
        return base.redirectOutput();
    }

    public ProcessBuilder.Redirect getRedirectError() {
        return base.redirectError();
    }

    public boolean isRedirectErrorStream() {
        return base.redirectErrorStream();
    }

    public ProcessBuilder2 setRedirectErrorStream(boolean redirectErrorStream) {
        base.redirectErrorStream(redirectErrorStream);
        return this;
    }

    public ProcessBuilder2 setRedirectErrorStream() {
        return setRedirectErrorStream(true);
    }

    public String getCommandString() {
        return getCommandString(null);
    }

    public String getCommandString(CommandStringFormat f) {
        List<String> fullCommandString=new ArrayList<>();
        File ff = getDirectory();
        if (ff == null) {
            ff = new File(".");
        }
        try {
            ff = ff.getCanonicalFile();
        } catch (Exception ex) {
            ff = ff.getAbsoluteFile();
        }
        fullCommandString.add("cwd="+ff.getPath());
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
                fullCommandString.add(k+"="+v);
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
            fullCommandString.add(s);
        }
        StringBuilder sb=new StringBuilder()
                .append(
                        NutsShellHelper.of(NutsShellFamily.getCurrent())
                                .escapeArguments(fullCommandString.toArray(new String[0]),
                                        new NutsCommandLineShellOptions()
                                                .setSession(session)
                                                .setExpectEnv(true)
                                                .setFormatStrategy(NutsCommandLineFormatStrategy.SUPPORT_QUOTES)
                                )
                );
        if (baseIO) {
            ProcessBuilder.Redirect r;
            if (f == null || f.acceptRedirectOutput()) {
                r = base.redirectOutput();
                if (null == r.type()) {
                    sb.append(" > ").append("{?}");
                } else {
                    switch (r.type()) {
                        //sb.append(" > ").append("{inherited}");
                        case INHERIT:
                            break;
                        case PIPE:
                            break;
                        case WRITE:
                            sb.append(" > ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                            break;
                        case APPEND:
                            sb.append(" >> ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                            break;
                        default:
                            sb.append(" > ").append("{?}");
                            break;
                    }
                }
            }
            if (f == null || f.acceptRedirectError()) {
                if (base.redirectErrorStream()) {
                    sb.append(" 2>&1");
                } else {
                    if (f == null || f.acceptRedirectError()) {
                        r = base.redirectError();
                        if (null == r.type()) {
                            sb.append(" 2> ").append("{?}");
                        } else {
                            switch (r.type()) {
                                //sb.append(" 2> ").append("{inherited}");
                                case INHERIT:
                                    break;
                                case PIPE:
                                    break;
                                case WRITE:
                                    sb.append(" 2> ").append(r.file().getPath());
                                    break;
                                case APPEND:
                                    sb.append(" 2>> ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                                    break;
                                default:
                                    sb.append(" 2> ").append("{?}");
                                    break;
                            }
                        }
                    }
                }
            }
            if (f == null || f.acceptRedirectInput()) {
                r = base.redirectInput();
                if (null == r.type()) {
                    sb.append(" < ").append("{?}");
                } else {
                    switch (r.type()) {
                        //sb.append(" < ").append("{inherited}");
                        case INHERIT:
                            break;
                        case PIPE:
                            break;
                        case READ:
                            sb.append(" < ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                            break;
                        default:
                            sb.append(" < ").append("{?}");
                            break;
                    }
                }
            }
        } else if (base.redirectErrorStream()) {
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

    public String getFormattedCommandString(NutsSession session) {
        return getFormattedCommandString(session, null);
    }

    private String escape(NutsSession session, String f) {
        return NutsTexts.of(session).ofPlain(f).toString();
    }

    public String getFormattedCommandString(NutsSession session, CommandStringFormat f) {
//        NutsFormatManager tf = session.formats();
//        StringBuilder sb = new StringBuilder();
        File ff = getDirectory();
        if (ff == null) {
            ff = new File(".");
        }
        try {
            ff = ff.getCanonicalFile();
        } catch (Exception ex) {
            ff = ff.getAbsoluteFile();
        }
        List<String> fullCommandString=new ArrayList<>();
        fullCommandString.add("cwd="+ff.getPath());
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
                fullCommandString.add(k+"="+v);
            }
        }
        boolean commandFirstTokenVisited = false;
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
            fullCommandString.add(s);
        }

        StringBuilder sb=new StringBuilder()
                .append("```system ").append(
                        NutsShellHelper.of(NutsShellFamily.getCurrent())
                        .escapeArguments(fullCommandString.toArray(new String[0]),
                                new NutsCommandLineShellOptions()
                                        .setSession(session)
                                        .setFormatStrategy(NutsCommandLineFormatStrategy.SUPPORT_QUOTES)
                                        .setExpectEnv(true)
                                )
        ).append(" ```");

        if (baseIO) {
            ProcessBuilder.Redirect r;
            if (f == null || f.acceptRedirectOutput()) {
                r = base.redirectOutput();
                if (null == r.type()) {
                    sb.append(" ##:separator:").append(escape(session, "> ")).append("## ").append("##:pale:{?}##");
                } else {
                    switch (r.type()) {
                        //sb.append(" > ").append("{inherited}");
                        case INHERIT:
                            break;
                        case PIPE:
                            break;
                        case WRITE:
                            sb.append(" ##:separator:").append(escape(session, ">")).append("## ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                            break;
                        case APPEND:
                            sb.append(" ##:separator:").append(escape(session, ">>")).append("## ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                            break;
                        default:
                            sb.append(" ##:separator:").append(escape(session, ">")).append("## ").append("##:pale:{?}##");
                            break;
                    }
                }
            }
            if (f == null || f.acceptRedirectError()) {
                if (base.redirectErrorStream()) {
                    sb.append(" ##:separator:").append(escape(session, "2>&1")).append("##");
                } else {
                    if (f == null || f.acceptRedirectError()) {
                        r = base.redirectError();
                        if (null == r.type()) {
                            sb.append(" ##:separator:").append(escape(session, "2>")).append("## ").append("##:pale:{?}##");
                        } else {
                            switch (r.type()) {
                                //sb.append(" 2> ").append("{inherited}");
                                case INHERIT:
                                    break;
                                case PIPE:
                                    break;
                                case WRITE:
                                    sb.append(" ##:separator:").append(escape(session, "2>")).append("## ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                                    break;
                                case APPEND:
                                    sb.append(" ##:separator:").append(escape(session, "2>>")).append("## ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                                    break;
                                default:
                                    sb.append(" ##:separator:").append(escape(session, "2>")).append("## ").append("##:pale:{?}##");
                                    break;
                            }
                        }
                    }
                }
            }
            if (f == null || f.acceptRedirectInput()) {
                r = base.redirectInput();
                if (null == r.type()) {
                    sb.append(" ##:separator:").append(escape(session, "<")).append("## ").append("##:pale:{?}##");
                } else {
                    switch (r.type()) {
                        //sb.append(" < ").append("{inherited}");
                        case INHERIT:
                            break;
                        case PIPE:
                            break;
                        case READ:
                            sb.append(" ##:separator:").append(escape(session, "<")).append("## ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                            break;
                        default:
                            sb.append(" ##:separator:").append(escape(session, "<")).append("## ").append("##:pale:{?}##");
                            break;
                    }
                }
            }
        } else if (base.redirectErrorStream()) {
            if (out != null) {
                if (f == null || f.acceptRedirectOutput()) {
                    sb.append(" ##:separator:").append(escape(session, "> ")).append("## ").append("##:pale:{stream}##");
                }
                if (f == null || f.acceptRedirectError()) {
                    sb.append(" ##:separator:").append(escape(session, "2>&1")).append("##");
                }
            }
            if (in != null) {
                if (f == null || f.acceptRedirectInput()) {
                    sb.append(" ##:separator:").append(escape(session, "<")).append("## ").append("##:pale:{stream}##");
                }
            }
        } else {
            if (out != null) {
                if (f == null || f.acceptRedirectOutput()) {
                    sb.append(" ##:separator:").append(escape(session, ">")).append("## ").append("##:pale:{stream}##");
                }
            }
            if (err != null) {
                if (f == null || f.acceptRedirectError()) {
                    sb.append(" ##:separator:").append(escape(session, "2>")).append("## ").append("##:pale:{stream}##");
                }
            }
            if (in != null) {
                if (f == null || f.acceptRedirectInput()) {
                    sb.append(" ##:separator:").append(escape(session, "<")).append("## ").append("##:pale:{stream}##");
                }
            }
        }
        return sb.toString();
    }

    public boolean isFailFast() {
        return failFast;
    }

    public ProcessBuilder2 setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    public ProcessBuilder2 setFailFast() {
        return setFailFast(true);
    }

    private PipeThread pipe(String name, final NonBlockingInputStream in, final OutputStream out) {
        PipeThread p = new PipeThread(name, in, out, session);
        p.start();
        return p;
    }

    @Override
    public String toString() {
        return "ProcessBuilder2{" + "command=" + command + ", env=" + env + ", directory=" + directory + ", base=" + base + ", in=" + in + ", out=" + out + ", err=" + err + ", result=" + result + ", baseIO=" + baseIO + ", failFast=" + failFast + ", proc=" + proc + ", sleepMillis=" + sleepMillis + ", session=" + session + '}';
    }

    public interface CommandStringFormat {

        default boolean acceptArgument(int argIndex, String arg) {
            return true;
        }

        default String replaceArgument(int argIndex, String arg) {
            return null;
        }

        default boolean acceptEnvName(String envName, String envValue) {
            return true;
        }

        default boolean acceptRedirectInput() {
            return true;
        }

        default boolean acceptRedirectOutput() {
            return true;
        }

        default boolean acceptRedirectError() {
            return true;
        }

        default String replaceEnvName(String envName, String envValue) {
            return null;
        }

        default String replaceEnvValue(String envName, String envValue) {
            return null;
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

}
