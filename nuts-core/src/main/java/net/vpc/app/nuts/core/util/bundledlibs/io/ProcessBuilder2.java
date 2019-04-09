/**
 * ====================================================================
 * vpc-common-io : common reusable library for
 * input/output
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.util.bundledlibs.io;

import java.io.*;
import java.util.*;
import net.vpc.app.nuts.NutsIllegalArgumentException;

//    public static void main(String[] args) {
//        try {
//            new ProcessBuilder2()
//                    .setCommand(
//                            "/usr/java/jdk1.8.0_171-amd64/bin/java",
//                            "-cp",
//                            "/home/vpc/.m2/repository/test/test-read/1.0-SNAPSHOT/test-read-1.0-SNAPSHOT.jar",
//                            "test.read.Main"
//                    )
//                    .setIn(System.in)
//                    .setOutput(System.out)
////                    .setErr(System.err)
//                    .waitFor();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
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
    //    private List<PipeThread> pipes = new ArrayList<>();
    private Process proc;
    private int sleepMillis = 1000;

    public int getSleepMillis() {
        return sleepMillis;
    }

    public ProcessBuilder2 setSleepMillis(int sleepMillis) {
        this.sleepMillis = sleepMillis;
        return this;
    }

    public Process getProc() {
        return proc;
    }

    public List<String> getCommand() {
        return command;
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

    public ProcessBuilder2 setCommand(String... command) {
        setCommand(Arrays.asList(command));
        return this;
    }

    public ProcessBuilder2 setCommand(List<String> command) {
        this.command = command == null ? null : new ArrayList<>(command);
        return this;
    }

    public Map<String, String> getEnv() {
        return env;
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

    public ProcessBuilder2 setEnv(Map<String, String> env) {
        this.env = env == null ? null : new HashMap<>(env);
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

    public InputStream getIn() {
        return in;
    }

    public ProcessBuilder2 setIn(InputStream in) {
        if (baseIO) {
            throw new NutsIllegalArgumentException("Already used Base IO Rediection");
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
        throw new NutsIllegalArgumentException("No Buffer was configured. Should call setOutString");
    }

    public String getErrorString() {
        if (base.redirectErrorStream()) {
            return getOutputString();
        }
        PrintStream o = getErr();
        if (o instanceof SPrintStream) {
            return ((SPrintStream) o).getStringBuffer();
        }
        throw new NutsIllegalArgumentException("No Buffer was configured. Should call setOutString");
    }

    public ProcessBuilder2 setOutput(PrintStream out) {
        if (baseIO) {
            throw new IllegalArgumentException("Already used Base IO Rediection");
        }
        this.out = out;
        return this;
    }

    public PrintStream getErr() {
        return err;
    }

    public ProcessBuilder2 setErr(PrintStream err) {
        if (baseIO) {
            throw new NutsIllegalArgumentException("Already used Base IO Rediection");
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
//        System.out.println("command="+command);
//        System.out.println("env="+env);
//        System.out.println("directory="+directory);
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
            NonBlockingInputStreamAdapter procInput = null;
            NonBlockingInputStreamAdapter procError = null;
            NonBlockingInputStreamAdapter termIn = null;
            List<PipeThread> pipes = new ArrayList<>();
            if (out != null) {
                procInput = new NonBlockingInputStreamAdapter("pipe-out-proc-" + proc.toString(), proc.getInputStream());
                pipes.add(pipe("pipe-out-proc-" + proc.toString(), procInput, out));
            }
            if (err != null) {
                procError = new NonBlockingInputStreamAdapter("pipe-err-proc-" + proc.toString(), proc.getErrorStream());
                if (base.redirectErrorStream()) {
                    pipes.add(pipe("pipe-err-proc-" + proc.toString(), procError, out));
                } else {
                    pipes.add(pipe("pipe-err-proc-" + proc.toString(), procError, err));
                }
            }
            if (in != null) {
                termIn = new NonBlockingInputStreamAdapter("pipe-in-proc-" + proc.toString(), in);
                pipes.add(pipe("pipe-in-proc-" + proc.toString(), termIn, proc.getOutputStream()));
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
                    throw new IOException(e.getMessage());
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

    private void waitFor0() throws IOException{
        try {
            result = proc.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage());
        }
        if (result != 0) {
            if (isFailFast()) {
                if (base.redirectErrorStream()) {
                    if (isGrabOutputString()) {
                        throw new IOException("Execution Failed with code " + result + " and message : " + getOutputString());
                    }
                } else {
                    if (isGrabErrorString()) {
                        throw new IOException("Execution Failed with code " + result + " and message : " + getErrorString());
                    }
                    if (isGrabOutputString()) {
                        throw new IOException("Execution Failed with code " + result + " and message : " + getOutputString());
                    }
                }
                throw new IOException("Execution Failed with code " + result);
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

    public ProcessBuilder2 setRedirectErrorStream() {
        return setRedirectErrorStream(true);
    }

    public ProcessBuilder2 setRedirectErrorStream(boolean redirectErrorStream) {
        base.redirectErrorStream(redirectErrorStream);
        return this;
    }

    public static class CommandStringFormatterAdapter implements CommandStringFormatter {

        @Override
        public boolean acceptArgument(int argIndex, String arg) {
            return true;
        }

        @Override
        public String replaceArgument(int argIndex, String arg) {
            return null;
        }

        @Override
        public boolean acceptEnvName(String envName, String envValue) {
            return true;
        }

        @Override
        public boolean acceptRedirectInput() {
            return true;
        }

        @Override
        public boolean acceptRedirectOutput() {
            return true;
        }

        @Override
        public boolean acceptRedirectError() {
            return true;
        }

        @Override
        public String replaceEnvName(String envName, String envValue) {
            return null;
        }

        @Override
        public String replaceEnvValue(String envName, String envValue) {
            return null;
        }
    }

    public interface CommandStringFormatter {

        boolean acceptArgument(int argIndex, String arg);

        String replaceArgument(int argIndex, String arg);

        boolean acceptEnvName(String envName, String envValue);

        boolean acceptRedirectInput();

        boolean acceptRedirectOutput();

        boolean acceptRedirectError();

        String replaceEnvName(String envName, String envValue);

        String replaceEnvValue(String envName, String envValue);
    }

    public String getCommandString() {
        return getCommandString(null);
    }

    public String getCommandString(CommandStringFormatter f) {
        StringBuilder sb = new StringBuilder();
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
            sb.append(enforceDoubleQuote(k)).append("=").append(enforceDoubleQuote(v));
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
        if (baseIO) {
            ProcessBuilder.Redirect r;
            if (f == null || f.acceptRedirectOutput()) {
                r = base.redirectOutput();
                if (r.type() == ProcessBuilder.Redirect.Type.INHERIT) {
                    sb.append(" > ").append("{inherited}");
                } else if (r.type() == ProcessBuilder.Redirect.Type.PIPE) {

                } else if (r.type() == ProcessBuilder.Redirect.Type.WRITE) {
                    sb.append(" > ").append(enforceDoubleQuote(r.file().getPath()));
                } else if (r.type() == ProcessBuilder.Redirect.Type.APPEND) {
                    sb.append(" >> ").append(enforceDoubleQuote(r.file().getPath()));
                } else {
                    sb.append(" > ").append("{?}");
                }
            }
            if (f == null || f.acceptRedirectError()) {
                if (base.redirectErrorStream()) {
                    sb.append(" 2>&1");
                } else {
                    if (f == null || f.acceptRedirectError()) {
                        r = base.redirectError();
                        if (r.type() == ProcessBuilder.Redirect.Type.INHERIT) {
                            sb.append(" 2> ").append("{inherited}");
                        } else if (r.type() == ProcessBuilder.Redirect.Type.PIPE) {

                        } else if (r.type() == ProcessBuilder.Redirect.Type.WRITE) {
                            sb.append(" 2> ").append(r.file().getPath());
                        } else if (r.type() == ProcessBuilder.Redirect.Type.APPEND) {
                            sb.append(" 2>> ").append(enforceDoubleQuote(r.file().getPath()));
                        } else {
                            sb.append(" 2> ").append("{?}");
                        }
                    }
                }
            }
            if (f == null || f.acceptRedirectInput()) {
                r = base.redirectInput();
                if (r.type() == ProcessBuilder.Redirect.Type.INHERIT) {
                    sb.append(" < ").append("{inherited}");
                } else if (r.type() == ProcessBuilder.Redirect.Type.PIPE) {

                } else if (r.type() == ProcessBuilder.Redirect.Type.READ) {
                    sb.append(" < ").append(enforceDoubleQuote(r.file().getPath()));
                } else {
                    sb.append(" < ").append("{?}");
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

    private static String enforceDoubleQuote(String s) {
        if (s.isEmpty() || s.contains(" ") || s.contains("\"")) {
            s = "\"" + s.replace("\"", "\\\"") + "\"";
        }
        return s;
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
    
    private static PipeThread pipe(String name, final NonBlockingInputStream in, final OutputStream out) {
        PipeThread p = new PipeThread(name, in, out);
        p.start();
        return p;
    }
    
}
