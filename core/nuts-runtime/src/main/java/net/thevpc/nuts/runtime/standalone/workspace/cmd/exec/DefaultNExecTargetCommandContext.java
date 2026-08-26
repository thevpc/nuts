package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.spi.NExecTargetCommandContext;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.net.NConnectionString;

import java.io.*;

public class DefaultNExecTargetCommandContext implements NExecTargetCommandContext, Closeable {
    private NConnectionString connectionString;
    private String[] command;
    private NExecInput xin;
    private NExecOutput xout;
    private NExecOutput xerr;
    private InHolder hin;
    private OutHolder hout;
    private OutHolder herr;
    private NExec execCommand;
    private boolean rawCommand;

    public DefaultNExecTargetCommandContext(NConnectionString connectionString, String[] command, NExecInput in, NExecOutput out, NExecOutput err, NExec execCommand) {
        this.connectionString = connectionString;
        this.command = command;
        this.xin = in;
        this.xout = out;
        this.xerr = err;
        this.execCommand = execCommand;
        this.rawCommand = execCommand.isRawCommand();
        NSession session = NSession.of();
        switch (in.type()) {
            case NULL: {
                hin = new MyInHolder(NIO.ofNullRawInputStream(), false, null);
                break;
            }
            case PATH: {
                hin = new MyInHolder(in.path().inputStream(), true, null);
                break;
            }
            case INHERIT:
            case PIPE: {
                hin = new MyInHolder(session.in(), false, null);
                break;
            }
            case STREAM: {
                hin = new MyInHolder(in.inputStream(), false, null);
                break;
            }
        }
        switch (out.type()) {
            case NULL: {
                hout = new MyOutHolder(NIO.ofNullRawOutputStream(), false, null);
                break;
            }
            case PATH: {
                hout = new MyOutHolder(in.path().outputStream(), true, null);
                break;
            }
            case INHERIT:
            case PIPE: {
                hout = new MyOutHolder(NOut.asOutputStream(), false, null);
                break;
            }
            case STREAM: {
                hout = new MyOutHolder(out.outputStream(), false, null);
                break;
            }
            case GRAB_STREAM: {
                ByteArrayOutputStream grabbed = new ByteArrayOutputStream();
                hout = new MyOutHolder(grabbed, false, () -> {
                    out.result(NInputSource.of(grabbed.toByteArray()));
                });
                break;
            }
            case GRAB_FILE: {
                NPath temp = NPath.ofTempFile();
                temp.deleteOnDispose(true);
                temp.userTemporary(true);
                hout = new MyOutHolder(temp.outputStream(), true, () -> {
                    out.result(temp);
                });
                break;
            }
        }
        switch (err.type()) {
            case NULL: {
                herr = new MyOutHolder(NIO.ofNullRawOutputStream(), false, null);
                break;
            }
            case PATH: {
                herr = new MyOutHolder(in.path().outputStream(), true, null);
                break;
            }
            case INHERIT:
            case PIPE: {
                herr = new MyOutHolder(session.err().asOutputStream(), false, null);
                break;
            }
            case STREAM: {
                herr = new MyOutHolder(err.outputStream(), false, null);
                break;
            }
            case GRAB_STREAM: {
                ByteArrayOutputStream grabbed = new ByteArrayOutputStream();
                herr = new MyOutHolder(grabbed, false, () -> {
                    err.result(NInputSource.of(grabbed.toByteArray()));
                });
                break;
            }
            case GRAB_FILE: {
                NPath temp = NPath.ofTempFile();
                temp.deleteOnDispose(true);
                temp.userTemporary(true);
                herr = new MyOutHolder(temp.outputStream(), true, () -> {
                    err.result(temp);
                });
                break;
            }
            case REDIRECT: {
                herr = new OutHolder() {
                    @Override
                    public OutputStream get() {
                        return hout.get();
                    }

                    @Override
                    public void close() {

                    }
                };
            }
        }
    }

    @Override
    public boolean isRawCommand() {
        return rawCommand;
    }

    @Override
    public InputStream in() {
        return hin.get();
    }

    @Override
    public OutputStream out() {
        return hout.get();
    }

    @Override
    public OutputStream err() {
        return herr.get();
    }

    @Override
    public NConnectionString connectionString() {
        return connectionString;
    }

    @Override
    public String[] command() {
        return command;
    }

    @Override
    public void close() throws IOException {
        hin.close();
        hout.close();
        herr.close();
    }


    private interface InHolder extends Closeable {
        InputStream get();

        void close();
    }

    private interface OutHolder extends Closeable {
        OutputStream get();

        void close();
    }


    private static class MyInHolder implements InHolder {
        private final InputStream in;
        private final boolean close;
        private final Runnable onClose;

        public MyInHolder(InputStream in, boolean close, Runnable onClose) {
            this.in = in;
            this.close = close;
            this.onClose = onClose;
        }

        @Override
        public InputStream get() {
            return in;
        }

        @Override
        public void close() {
            if (close) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (onClose != null) {
                onClose.run();
            }
        }
    }

    private static class MyOutHolder implements OutHolder {
        private final OutputStream out;
        private final boolean close;
        private final Runnable onClose;

        public MyOutHolder(OutputStream out, boolean close, Runnable onClose) {
            this.out = out;
            this.close = close;
            this.onClose = onClose;
        }

        @Override
        public OutputStream get() {
            return out;
        }

        @Override
        public void close() {
            if (close) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (onClose != null) {
                onClose.run();
            }
        }
    }

    public NExec execCommand() {
        return execCommand;
    }
}
