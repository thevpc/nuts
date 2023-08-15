package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPath;

import java.io.*;

public class DefaultNExecCommandExtensionContext implements NExecCommandExtensionContext, Closeable {
    private String target;
    private String[] command;
    private NSession session;
    private NExecInput xin;
    private NExecOutput xout;
    private NExecOutput xerr;
    private InHolder hin;
    private OutHolder hout;
    private OutHolder herr;
    private NExecCommand execCommand;

    public DefaultNExecCommandExtensionContext(String target, String[] command, NSession session, NExecInput in, NExecOutput out, NExecOutput err, NExecCommand execCommand) {
        this.target = target;
        this.command = command;
        this.session = session;
        this.xin = in;
        this.xout = out;
        this.xerr = err;
        this.execCommand = execCommand;
        switch (in.getType()) {
            case NULL: {
                hin = new MyInHolder(session, NIO.of(session).ofNullRawInputStream(), false, null);
                break;
            }
            case PATH: {
                hin = new MyInHolder(session, in.getPath().getInputStream(), true, null);
                break;
            }
            case INHERIT:
            case PIPE: {
                hin = new MyInHolder(session, session.in(), false, null);
                break;
            }
            case STREAM: {
                hin = new MyInHolder(session, in.getStream(), false, null);
                break;
            }
        }
        switch (out.getType()) {
            case NULL: {
                hout = new MyOutHolder(session, NIO.of(session).ofNullRawOutputStream(), false, null);
                break;
            }
            case PATH: {
                hout = new MyOutHolder(session, in.getPath().getOutputStream(), true, null);
                break;
            }
            case INHERIT:
            case PIPE: {
                hout = new MyOutHolder(session, session.out().asOutputStream(), false, null);
                break;
            }
            case STREAM: {
                hout = new MyOutHolder(session, out.getStream(), false, null);
                break;
            }
            case GRAB_STREAM: {
                ByteArrayOutputStream grabbed = new ByteArrayOutputStream();
                hout = new MyOutHolder(session, grabbed, false, () -> {
                    out.setResult(NIO.of(session).ofInputSource(grabbed.toByteArray()));
                });
                break;
            }
            case GRAB_FILE: {
                NPath temp = NPath.ofTempFile(session);
                temp.setDeleteOnDispose(true);
                temp.setUserTemporary(true);
                hout = new MyOutHolder(session, temp.getOutputStream(), true, () -> {
                    out.setResult(temp);
                });
                break;
            }
        }
        switch (err.getType()) {
            case NULL: {
                herr = new MyOutHolder(session, NIO.of(session).ofNullRawOutputStream(), false, null);
                break;
            }
            case PATH: {
                herr = new MyOutHolder(session, in.getPath().getOutputStream(), true, null);
                break;
            }
            case INHERIT:
            case PIPE: {
                herr = new MyOutHolder(session, session.err().asOutputStream(), false, null);
                break;
            }
            case STREAM: {
                herr = new MyOutHolder(session, err.getStream(), false, null);
                break;
            }
            case GRAB_STREAM: {
                ByteArrayOutputStream grabbed = new ByteArrayOutputStream();
                herr = new MyOutHolder(session, grabbed, false, () -> {
                    err.setResult(NIO.of(session).ofInputSource(grabbed.toByteArray()));
                });
                break;
            }
            case GRAB_FILE: {
                NPath temp = NPath.ofTempFile(session);
                temp.setDeleteOnDispose(true);
                temp.setUserTemporary(true);
                herr = new MyOutHolder(session, temp.getOutputStream(), true, () -> {
                    err.setResult(temp);
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
    public String getTarget() {
        return target;
    }

    @Override
    public String[] getCommand() {
        return command;
    }

    @Override
    public NSession getSession() {
        return session;
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
        private final NSession session;
        private final InputStream in;
        private final boolean close;
        private final Runnable onClose;

        public MyInHolder(NSession session, InputStream in, boolean close, Runnable onClose) {
            this.session = session;
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
        private final NSession session;
        private final OutputStream out;
        private final boolean close;
        private final Runnable onClose;

        public MyOutHolder(NSession session, OutputStream out, boolean close, Runnable onClose) {
            this.session = session;
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

    public NExecCommand getExecCommand() {
        return execCommand;
    }
}
