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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.executor.system;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.DefaultNArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineFormatStrategy;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCmdLineShellOptions;
import net.thevpc.nuts.runtime.standalone.shell.NShellHelper;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.util.NLogVerb;
import net.thevpc.nuts.util.NStringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ProcessBuilder2 {

    private List<String> command = new ArrayList<>();
    private Map<String, String> env;
    private File directory;
    private boolean failFast;
    private long sleepMillis = 1000;
    private NSession session;

    private NExecInput2 in = new NExecInput2(NExecInput.ofInherit());
    private NExecOutput2 out = new NExecOutput2(NExecOutput.ofInherit());
    private NExecOutput2 err = new NExecOutput2(NExecOutput.ofInherit());


    ////////////////////// EXEC VARS

    private ProcessBuilder base = new ProcessBuilder();
    private List<PipeRunnable> pipesList = new ArrayList<>();
    private ExecutorService pipes = null;
    private int result;
    private Process proc;
    private long pid;
    private NLog nLog;

    public ProcessBuilder2(NSession session) {
        this.session = session;
        this.nLog = NLog.of(ProcessBuilder2.class,session);
    }

    private static String formatArg(String s, NSession session) {
        DefaultNArg a = new DefaultNArg(s);
        StringBuilder sb = new StringBuilder();
        NTexts factory = NTexts.of(session);
        if (a.isKeyValue()) {
            if (a.isOption()) {
                sb.append(factory.ofStyled(NStringUtils.formatStringLiteral(a.key()), NTextStyle.option()));
                sb.append("=");
                sb.append(NStringUtils.formatStringLiteral(a.getStringValue().get(session)));
            } else {
                sb.append(factory.ofStyled(NStringUtils.formatStringLiteral(a.key()), NTextStyle.primary4()));
                sb.append("=");
                sb.append(NStringUtils.formatStringLiteral(a.getStringValue().get(session)));
            }
        } else {
            if (a.isOption()) {
                sb.append(factory.ofStyled(NStringUtils.formatStringLiteral(a.asString().get(session)), NTextStyle.option()));
            } else {
                sb.append(NStringUtils.formatStringLiteral(a.asString().get(session)));
            }
        }
        return sb.toString();
    }

    public static long getProcessId(Process p) {
        try {
            //for windows
            if (p.getClass().getName().equals("java.lang.Win32Process") || p.getClass().getName().equals("java.lang.ProcessImpl")) {
                Field f = p.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                long handle = f.getLong(p);
//                Kernel32 kernel = Kernel32.INSTANCE;
//                WinNT.HANDLE hand = new WinNT.HANDLE();
//                hand.setPointer(Pointer.createConstant(handl));
//                pid = kernel.GetProcessId(hand);
            } else if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
                //for unix based operating systems
                Field f = p.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                return f.getLong(p);
            }
        } catch (Exception anyException) {
            //ignore
        }
        return -1;
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
        return this;
    }

    ////////////////// OUT

    public NExecInput getIn() {
        return in.base;
    }

    public ProcessBuilder2 setIn(NExecInput in) {
        this.in.base = in == null ? NExecInput.ofInherit() : in;
        return this;
    }

    public NExecOutput getOut() {
        return out.base;
    }

    public ProcessBuilder2 setOut(NExecOutput out) {
        this.out.base = out == null ? NExecOutput.ofInherit() : out;
        return this;
    }

    public NExecOutput getErr() {
        return err.base;
    }

    public ProcessBuilder2 setErr(NExecOutput err) {
        this.err.base = err == null ? NExecOutput.ofInherit() : err;
        return this;
    }


    ////////////////// RESULTS

    public byte[] getOutputBytes() {
        return out.base.getResultBytes();
    }

    public byte[] getErrorBytes() {
        switch (err.base.getType()) {
            case REDIRECT: {
                return getOutputBytes();
            }
        }
        return err.base.getResultBytes();
    }

    public String getOutputString() {
        return new String(getOutputBytes());
    }

    public String getErrorString() {
        return new String(getErrorBytes());
    }

    public ProcessBuilder2 start() throws IOException {
        if (proc != null) {
            throw new NIllegalStateException(session, NMsg.ofPlain("already started"));
        }
        nLog.with().verb(NLogVerb.START).level(Level.FINEST).log(
                NMsg.ofNtf(NTexts.of(session).ofCode("system", getCommandString()))
        );
        switch (in.base.getType()) {
            case PIPE:
            case STREAM:
            case NULL: {
                base.redirectInput(ProcessBuilder.Redirect.PIPE);
                break;
            }

            case PATH: {
                NPath path = in.base.getPath();
                Path file = path.toPath().get();
                if (file == null) {
                    in.tempPath = NPath.ofTempFile(session);
                    in.file = in.tempPath.toFile().get();
                    path.copyTo(in.tempPath);
                } else {
                    in.file = file.toFile();
                }
                base.redirectInput(ProcessBuilder.Redirect.from(in.file));
            }
            case INHERIT: {
                base.redirectInput(ProcessBuilder.Redirect.INHERIT);
                break;
            }
            case GRAB_STREAM:
            case GRAB_FILE:
            case REDIRECT: {
                throw new NIllegalArgumentException(session, NMsg.ofC("unsupported in mode : %s", in.base.getType()));
            }
        }

        switch (out.base.getType()) {
            case PIPE:
            case STREAM:
            case NULL: {
                base.redirectOutput(ProcessBuilder.Redirect.PIPE);
                break;
            }
            case INHERIT: {
                base.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                break;
            }
            case GRAB_STREAM: {
                base.redirectOutput(ProcessBuilder.Redirect.PIPE);
                out.tempStream = new ByteArrayOutputStream();
                break;
            }
            case GRAB_FILE: {
                out.tempPath = NPath.ofTempFile(session);
                out.file = out.tempPath.toFile().get();
                base.redirectOutput(ProcessBuilder.Redirect.to(out.file));
            }
            case PATH: {
                NPath path = out.base.getPath();
                Path file = path.toPath().get();
                Set<NPathOption> options = Arrays.stream(out.base.getOptions()).filter(Objects::nonNull).collect(Collectors.toSet());
                if (file == null) {
                    base.redirectOutput(ProcessBuilder.Redirect.PIPE);
                    out.tempStream = out.base.getPath().getOutputStream(options.toArray(new NPathOption[0]));
                } else {
                    if (options.isEmpty()) {
                        in.file = file.toFile();
                        base.redirectOutput(ProcessBuilder.Redirect.to(out.file));
                    } else if (options.size() == 1 && options.contains(NPathOption.APPEND)) {
                        in.file = file.toFile();
                        base.redirectOutput(ProcessBuilder.Redirect.appendTo(out.file));
                    } else {
                        base.redirectOutput(ProcessBuilder.Redirect.PIPE);
                        out.tempStream = out.base.getPath().getOutputStream(options.toArray(new NPathOption[0]));
                    }
                }
                break;
            }
            case REDIRECT: {
                throw new NIllegalArgumentException(session, NMsg.ofC("unsupported in mode : %s", out.base.getType()));
            }
        }

        switch (err.base.getType()) {
            case PIPE:
            case STREAM:
            case NULL: {
                base.redirectError(ProcessBuilder.Redirect.PIPE);
                break;
            }
            case INHERIT: {
                base.redirectError(ProcessBuilder.Redirect.INHERIT);
                break;
            }
            case GRAB_STREAM: {
                base.redirectError(ProcessBuilder.Redirect.PIPE);
                err.tempStream = new ByteArrayOutputStream();
                break;
            }
            case GRAB_FILE: {
                err.tempPath = NPath.ofTempFile(session);
                err.file = err.tempPath.toFile().get();
                base.redirectError(ProcessBuilder.Redirect.to(err.file));
            }
            case PATH: {
                NPath path = err.base.getPath();
                Path file = path.toPath().get();
                Set<NPathOption> options = Arrays.stream(err.base.getOptions()).filter(Objects::nonNull).collect(Collectors.toSet());
                if (file == null) {
                    base.redirectError(ProcessBuilder.Redirect.PIPE);
                    err.tempStream = err.base.getPath().getOutputStream(options.toArray(new NPathOption[0]));
                } else {
                    if (options.isEmpty()) {
                        in.file = file.toFile();
                        base.redirectError(ProcessBuilder.Redirect.to(err.file));
                    } else if (options.size() == 1 && options.contains(NPathOption.APPEND)) {
                        in.file = file.toFile();
                        base.redirectError(ProcessBuilder.Redirect.appendTo(err.file));
                    } else {
                        base.redirectError(ProcessBuilder.Redirect.PIPE);
                        err.tempStream = err.base.getPath().getOutputStream(options.toArray(new NPathOption[0]));
                    }
                }
                break;
            }
            case REDIRECT: {
                base.redirectErrorStream(true);
                break;
            }
        }

        base.directory(directory);
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
        pid = getProcessId(proc);
        return this;
    }

    public ProcessBuilder2 waitFor() throws IOException {
        if (proc == null) {
            start();
        }
        if (proc == null) {
            throw new IOException("Not started");
        }
        String procString = NPath.of(command.get(0), session).getName()
                + "-" + (pid < 0 ? ("unknown-pid" + String.valueOf(-pid)) : String.valueOf(pid));
        String cmdStr = String.join(" ", command);
        switch (in.base.getType()) {
            case NULL: {
                String pname = "pipe-in-proc-" + procString;
                in.termIn = createNonBlockingInput(NIO.of(session).ofNullRawInputStream(), pname);
                PipeRunnable t = NSysExecUtils.pipe(pname, cmdStr, "in", in.termIn, proc.getOutputStream(), session);
                if (pipes == null) {
                    pipes = Executors.newCachedThreadPool();
                }
                pipes.submit(t);
                pipesList.add(t);
                break;
            }
            case STREAM: {
                String pname = "pipe-in-proc-" + procString;
                in.termIn = createNonBlockingInput(in.base.getStream(), pname);
                PipeRunnable t = NSysExecUtils.pipe(pname, cmdStr, "in", in.termIn, proc.getOutputStream(), session);
                if (pipes == null) {
                    pipes = Executors.newCachedThreadPool();
                }
                pipes.submit(t);
                pipesList.add(t);
                break;
            }
        }
        switch (out.base.getType()) {
            case NULL: {
                String pname = "pipe-out-proc-" + procString;
                NNonBlockingInputStream procInput = createNonBlockingInput(proc.getInputStream(), pname);
                PipeRunnable t = NSysExecUtils.pipe(pname, cmdStr, "out", procInput,
                        NIO.of(session).ofNullRawOutputStream()
                        , session);
                if (pipes == null) {
                    pipes = Executors.newCachedThreadPool();
                }
                pipes.submit(t);
                pipesList.add(t);
                break;
            }
            case STREAM: {
                String pname = "pipe-out-proc-" + procString;
                NNonBlockingInputStream procInput = createNonBlockingInput(proc.getInputStream(), pname);
                PipeRunnable t = NSysExecUtils.pipe(pname, cmdStr, "out", procInput, out.base.getStream(), session);
                if (pipes == null) {
                    pipes = Executors.newCachedThreadPool();
                }
                pipes.submit(t);
                pipesList.add(t);
                break;
            }
            case GRAB_STREAM: {
                String pname = "pipe-out-proc-" + procString;
                NNonBlockingInputStream procInput = createNonBlockingInput(proc.getInputStream(), pname);
                PipeRunnable t = NSysExecUtils.pipe(pname, cmdStr, "out", procInput, out.tempStream, session);
                if (pipes == null) {
                    pipes = Executors.newCachedThreadPool();
                }
                pipes.submit(t);
                pipesList.add(t);
                break;
            }
            case PATH: {
                if (out.tempStream != null) {
                    //this happens when the path is not local
                    String pname = "pipe-out-proc-" + procString;
                    NNonBlockingInputStream procInput = createNonBlockingInput(proc.getInputStream(), pname);
                    PipeRunnable t = NSysExecUtils.pipe(pname, cmdStr, "out", procInput, out.tempStream, session);
                    if (pipes == null) {
                        pipes = Executors.newCachedThreadPool();
                    }
                    pipes.submit(t);
                    pipesList.add(t);
                }
                break;
            }
        }
        switch (err.base.getType()) {
            case STREAM: {
                String pname = "pipe-err-proc-" + procString;
                NNonBlockingInputStream procInput = createNonBlockingInput(proc.getErrorStream(), pname);
                PipeRunnable t = NSysExecUtils.pipe(pname, cmdStr, "err", procInput, err.base.getStream(), session);
                if (pipes == null) {
                    pipes = Executors.newCachedThreadPool();
                }
                pipes.submit(t);
                pipesList.add(t);
                break;
            }
            case GRAB_STREAM: {
                String pname = "pipe-err-proc-" + procString;
                NNonBlockingInputStream procInput = createNonBlockingInput(proc.getErrorStream(), pname);
                PipeRunnable t = NSysExecUtils.pipe(pname, cmdStr, "err", procInput, err.tempStream, session);
                if (pipes == null) {
                    pipes = Executors.newCachedThreadPool();
                }
                pipes.submit(t);
                pipesList.add(t);
                break;
            }
            case PATH: {
                if (err.tempStream != null) {
                    //this happens when the path is not local
                    String pname = "pipe-err-proc-" + procString;
                    NNonBlockingInputStream procInput = createNonBlockingInput(proc.getErrorStream(), pname);
                    PipeRunnable t = NSysExecUtils.pipe(pname, cmdStr, "err", procInput, err.tempStream, session);
                    if (pipes == null) {
                        pipes = Executors.newCachedThreadPool();
                    }
                    pipes.submit(t);
                    pipesList.add(t);
                }
                break;
            }
        }
        if (in.termIn != null || pipesList.isEmpty()) {
            while (proc.isAlive()) {
                if (in.termIn != null) {
                    if (!in.termIn.hasMoreBytes() && in.termIn.available() == 0) {
                        in.termIn.close();
                        in.termIn = null;
                    }
                }
                boolean allFinished = true;
                for (PipeRunnable pipe : pipesList) {
                    if (!pipe.isStopped()) {
                        allFinished = false;
                    } else {
                        pipe.getOut().close();
                    }
                }
                if (allFinished) {
                    break;
                }
                if (sleepMillis > 0) {
                    try {
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException e) {
                        throw new IOException(CoreStringUtils.exceptionToString(e));
                    }
                }
            }
        }

        try {
            result = proc.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(CoreStringUtils.exceptionToString(e));
        }

        if (pipes != null) {
            for (PipeRunnable pipe : pipesList) {
                pipe.requestStop();
            }
            pipes.shutdown();
            try {
                pipes.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                throw new NUnexpectedException(session, NMsg.ofPlain("unable to await termination"));
            }
        }
        proc.getInputStream().close();
        proc.getErrorStream().close();
        proc.getOutputStream().close();
        switch (out.base.getType()) {
            case PATH: {
                if (out.tempStream != null) {
                    out.tempStream.close();
                }
                break;
            }
            case GRAB_STREAM: {
                out.tempStream.close();
                out.base.setResult(NIO.of(session).ofInputSource(((ByteArrayOutputStream) out.tempStream).toByteArray()));
                break;
            }
            case GRAB_FILE: {
                if (out.tempPath != null) {
                    out.tempStream.close();
                    out.tempPath.setUserTemporary(true);
                    out.tempPath.setDeleteOnDispose(true);
                    out.base.setResult(out.tempPath);
                }
                break;
            }
        }
        switch (err.base.getType()) {
            case PATH: {
                if (err.tempStream != null) {
                    err.tempStream.close();
                }
                break;
            }
            case GRAB_STREAM: {
                err.tempStream.close();
                err.base.setResult(NIO.of(session).ofInputSource(((ByteArrayOutputStream) err.tempStream).toByteArray()));
                break;
            }
            case GRAB_FILE: {
                if (err.tempPath != null) {
                    err.tempStream.close();
                    err.tempPath.setUserTemporary(true);
                    err.tempPath.setDeleteOnDispose(true);
                    err.base.setResult(err.tempPath);
                }
                break;
            }
        }
        if (result != NExecutionException.SUCCESS) {
            if (isFailFast()) {
                if (base.redirectErrorStream()) {
                    if (out.base.getType() == NExecRedirectType.GRAB_FILE || out.base.getType() == NExecRedirectType.GRAB_STREAM) {
                        throw new NExecutionException(session,
                                NMsg.ofC("execution failed with code %d and message : %s. Command was %s", result, getOutputString(),
                                        NCmdLine.of(getCommand())),
                                result);
                    }
                } else {
                    if (err.base.getType() == NExecRedirectType.GRAB_FILE || err.base.getType() == NExecRedirectType.GRAB_STREAM) {
                        throw new NExecutionException(session,
                                NMsg.ofC("execution failed with code %d and message : %s. Command was %s", result, getOutputString(),
                                        NCmdLine.of(getCommand())),
                                result);
                    }
                    if (out.base.getType() == NExecRedirectType.GRAB_FILE || out.base.getType() == NExecRedirectType.GRAB_STREAM) {
                        throw new NExecutionException(session, NMsg.ofC(
                                "execution failed with code %d and message : %s. Command was %s", result, getOutputString(),
                                NCmdLine.of(getCommand())
                        ), result);
                    }
                }
                throw new NExecutionException(session, NMsg.ofC("execution failed with code %d. Command was %s", result,
                        NCmdLine.of(getCommand())
                ), result);
            }
        }
        return this;
    }

    private NNonBlockingInputStream createNonBlockingInput(InputStream proc, String pname) {
        return NIO.of(session)
                .ofInputStreamBuilder(proc)
                .setMetadata(new DefaultNContentMetadata().setMessage(NMsg.ofPlain(pname)))
                .createNonBlockingInputStream()
                ;
    }

    public int getResult() {
        return result;
    }

    public Process getProcess() {
        return proc;
    }

    public String getCommandString() {
        return getCommandString(null);
    }

    public String getCommandString(CommandStringFormat f) {
        List<String> fullCommandString = new ArrayList<>();
        File ff = getDirectory();
        if (ff == null) {
            ff = new File(".");
        }
        try {
            ff = ff.getCanonicalFile();
        } catch (Exception ex) {
            ff = ff.getAbsoluteFile();
        }
        fullCommandString.add("cwd=" + ff.getPath());
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
                fullCommandString.add(k + "=" + v);
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
        StringBuilder sb = new StringBuilder()
                .append(
                        NShellHelper.of(NShellFamily.getCurrent())
                                .escapeArguments(fullCommandString.toArray(new String[0]),
                                        new NCmdLineShellOptions()
                                                .setSession(session)
                                                .setExpectEnv(true)
                                                .setFormatStrategy(NCmdLineFormatStrategy.SUPPORT_QUOTES)
                                )
                );

        switch (out.base.getType()) {
            case PATH: {
                if (Arrays.stream(out.base.getOptions()).anyMatch(x -> x == NPathOption.APPEND)) {
                    sb.append(" >> ");
                } else {
                    sb.append(" > ");
                }
                sb.append(NStringUtils.formatStringLiteral(out.base.getPath().toString()));
                break;
            }
        }

        switch (out.base.getType()) {
            case REDIRECT:
                sb.append(" 2>&1");
                break;
            case PATH:
                if (Arrays.stream(err.base.getOptions()).anyMatch(x -> x == NPathOption.APPEND)) {
                    sb.append(" 2>> ");
                } else {
                    sb.append(" 2> ");
                }
                sb.append(NStringUtils.formatStringLiteral(err.base.getPath().toString()));
                break;
        }

        switch (out.base.getType()) {
            case PATH:
                sb.append(" < ").append(NStringUtils.formatStringLiteral(out.base.getPath().toString()));
                break;
        }
        return sb.toString();
    }

    public String getFormattedCommandString(NSession session) {
        return getFormattedCommandString(session, null);
    }

    private String escape(NSession session, String f) {
        return NTexts.of(session).ofPlain(f).toString();
    }

    public String getFormattedCommandString(NSession session, CommandStringFormat f) {
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
        List<String> fullCommandString = new ArrayList<>();
        fullCommandString.add("cwd=" + ff.getPath());
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
                fullCommandString.add(k + "=" + v);
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
        NTexts txt = NTexts.of(session);
        NTextBuilder sb = txt.ofBlank().builder()
                .append(txt.ofCode("system",
                        NShellHelper.of(NShellFamily.getCurrent())
                                .escapeArguments(fullCommandString.toArray(new String[0]),
                                        new NCmdLineShellOptions()
                                                .setSession(session)
                                                .setFormatStrategy(NCmdLineFormatStrategy.SUPPORT_QUOTES)
                                                .setExpectEnv(true)
                                )
                ));
        switch (out.base.getType()) {
            case PATH: {
                sb.append(" ");
                if (Arrays.stream(out.base.getOptions()).anyMatch(x -> x == NPathOption.APPEND)) {
                    sb.append(">>", NTextStyle.separator());
                } else {
                    sb.append(">", NTextStyle.separator());
                }
                sb.append(" ");
                sb.append(out.base.getPath());
                break;
            }
        }

        switch (err.base.getType()) {
            case PATH: {
                sb.append(" ");
                if (Arrays.stream(out.base.getOptions()).anyMatch(x -> x == NPathOption.APPEND)) {
                    sb.append(">>", NTextStyle.separator());
                } else {
                    sb.append(">", NTextStyle.separator());
                }
                sb.append(" ");
                sb.append(err.base.getPath(), NTextStyle.path());
                break;
            }
            case REDIRECT: {
                sb.append(" ");
                sb.append("2", NTextStyle.number());
                sb.append(">", NTextStyle.separator());
                sb.append("&1", NTextStyle.number());
                break;
            }
        }
        switch (in.base.getType()) {
            case PATH: {
                sb.append(" ");
                sb.append("<", NTextStyle.separator());
                sb.append(" ");
                sb.append(in.base.getPath(), NTextStyle.path());
                break;
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


    @Override
    public String toString() {
        return "ProcessBuilder2{" +
                "command=" + command +
                ", env=" + env +
                ", directory=" + directory +
                ", failFast=" + failFast +
                ", sleepMillis=" + sleepMillis +
                ", session=" + session +
                ", in=" + in +
                ", out=" + out +
                ", err=" + err +
                ", result=" + result +
                ", pid=" + pid +
                '}';
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

}
