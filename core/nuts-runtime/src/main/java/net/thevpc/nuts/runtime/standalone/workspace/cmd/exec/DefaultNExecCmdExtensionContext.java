package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;

import java.io.*;

public class DefaultNExecCmdExtensionContext implements NExecCmdExtensionContext, Closeable {
    private String target;
    private String[] command;
    private NSession session;
    private NExecInput xin;
    private NExecOutput xout;
    private NExecOutput xerr;
    private InHolder hin;
    private OutHolder hout;
    private OutHolder herr;
    private NExecCmd execCommand;

    public DefaultNExecCmdExtensionContext(String target, String[] command, NSession session, NExecInput in, NExecOutput out, NExecOutput err, NExecCmd execCommand) {
        this.target = target;
        this.command = command;
        this.session = session;
        this.xin = in;
        this.xout = out;
        this.xerr = err;
        this.execCommand = execCommand;
        switch (in.getType()) {
            case NULL: {
                hin = new MyInHolder(NIO.ofNullRawInputStream(), false, null);
                break;
            }
            case PATH: {
                hin = new MyInHolder(in.getPath().getInputStream(), true, null);
                break;
            }
            case INHERIT:
            case PIPE: {
                hin = new MyInHolder(session.in(), false, null);
                break;
            }
            case STREAM: {
                hin = new MyInHolder(in.getStream(), false, null);
                break;
            }
        }
        switch (out.getType()) {
            case NULL: {
                hout = new MyOutHolder(NIO.ofNullRawOutputStream(), false, null);
                break;
            }
            case PATH: {
                hout = new MyOutHolder(in.getPath().getOutputStream(), true, null);
                break;
            }
            case INHERIT:
            case PIPE: {
                hout = new MyOutHolder(NOut.asOutputStream(), false, null);
                break;
            }
            case STREAM: {
                hout = new MyOutHolder(out.getStream(), false, null);
                break;
            }
            case GRAB_STREAM: {
                ByteArrayOutputStream grabbed = new ByteArrayOutputStream();
                hout = new MyOutHolder(grabbed, false, () -> {
                    out.setResult(NInputSource.of(grabbed.toByteArray()));
                });
                break;
            }
            case GRAB_FILE: {
                NPath temp = NPath.ofTempFile();
                temp.setDeleteOnDispose(true);
                temp.setUserTemporary(true);
                hout = new MyOutHolder(temp.getOutputStream(), true, () -> {
                    out.setResult(temp);
                });
                break;
            }
        }
        switch (err.getType()) {
            case NULL: {
                herr = new MyOutHolder(NIO.ofNullRawOutputStream(), false, null);
                break;
            }
            case PATH: {
                herr = new MyOutHolder(in.getPath().getOutputStream(), true, null);
                break;
            }
            case INHERIT:
            case PIPE: {
                herr = new MyOutHolder(session.err().asOutputStream(), false, null);
                break;
            }
            case STREAM: {
                herr = new MyOutHolder(err.getStream(), false, null);
                break;
            }
            case GRAB_STREAM: {
                ByteArrayOutputStream grabbed = new ByteArrayOutputStream();
                herr = new MyOutHolder(grabbed, false, () -> {
                    err.setResult(NInputSource.of(grabbed.toByteArray()));
                });
                break;
            }
            case GRAB_FILE: {
                NPath temp = NPath.ofTempFile();
                temp.setDeleteOnDispose(true);
                temp.setUserTemporary(true);
                herr = new MyOutHolder(temp.getOutputStream(), true, () -> {
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

    public NExecCmd getExecCommand() {
        return execCommand;
    }
}
