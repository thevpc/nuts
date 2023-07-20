package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NConnexionString;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.util.NLogVerb;

import java.io.*;
import java.util.logging.Level;

public class SshNExecCommandExtension implements NExecCommandExtension {

    @Override
    public int exec(NExecCommandExtensionContext context) {
        String host = context.getHost();
        NAssert.requireNonBlank(host, "host");
        NConnexionString z = NConnexionString.of(host).orNull();
        NAssert.requireNonBlank(z, "host");
        NSession session = context.getSession();
        NLog log = NLog.of(SshNExecCommandExtension.class, session);
        try (FDHolder h = createFDHolder(context)) {
            log.with().level(Level.FINER).verb(NLogVerb.START).log(NMsg.ofC("[%s] %s", z, NCmdLine.of(context.getCommand(), session)));
            try (SShConnection c = new SShConnection(
                    host,
                    h.in.get(),
                    h.out.get(),
                    h.err.get(),
                    session)) {
                String[] command = context.getCommand();
                return c.execStringCommand(NCmdLine.of(command).toString());
            }
        }
    }

    private FDHolder createFDHolder(NExecCommandExtensionContext context) {
        FDHolder h = new FDHolder();
        NExecInput in = context.getIn();
        NExecOutput out = context.getOut();
        NExecOutput err = context.getErr();
        NSession session = context.getSession();
        switch (in.getType()) {
            case NULL: {
                h.in = new MyInHolder(session, NIO.of(session).ofNullRawInputStream(), false, null);
                break;
            }
            case PATH: {
                h.in = new MyInHolder(session, in.getPath().getInputStream(), true, null);
                break;
            }
            case INHERIT:
            case PIPE: {
                h.in = new MyInHolder(session, session.in(), false, null);
                break;
            }
            case STREAM: {
                h.in = new MyInHolder(session, in.getStream(), false, null);
                break;
            }
        }
        switch (out.getType()) {
            case NULL: {
                h.out = new MyOutHolder(session, NIO.of(session).ofNullRawOutputStream(), false, null);
                break;
            }
            case PATH: {
                h.out = new MyOutHolder(session, in.getPath().getOutputStream(), true, null);
                break;
            }
            case INHERIT:
            case PIPE: {
                h.out = new MyOutHolder(session, session.out().asOutputStream(), false, null);
                break;
            }
            case STREAM: {
                h.out = new MyOutHolder(session, out.getStream(), false, null);
                break;
            }
            case GRAB_STREAM: {
                ByteArrayOutputStream grabbed = new ByteArrayOutputStream();
                h.out = new MyOutHolder(session, grabbed, false, () -> {
                    out.setResult(NIO.of(session).ofInputSource(grabbed.toByteArray()));
                });
                break;
            }
            case GRAB_FILE: {
                NPath temp = NPath.ofTempFile(session);
                temp.setDeleteOnDispose(true);
                temp.setUserTemporary(true);
                h.out = new MyOutHolder(session, temp.getOutputStream(), true, () -> {
                    out.setResult(temp);
                });
                break;
            }
        }
        switch (err.getType()) {
            case NULL: {
                h.err = new MyOutHolder(session, NIO.of(session).ofNullRawOutputStream(), false, null);
                break;
            }
            case PATH: {
                h.err = new MyOutHolder(session, in.getPath().getOutputStream(), true, null);
                break;
            }
            case INHERIT:
            case PIPE: {
                h.err = new MyOutHolder(session, session.err().asOutputStream(), false, null);
                break;
            }
            case STREAM: {
                h.err = new MyOutHolder(session, err.getStream(), false, null);
                break;
            }
            case GRAB_STREAM: {
                ByteArrayOutputStream grabbed = new ByteArrayOutputStream();
                h.err = new MyOutHolder(session, grabbed, false, () -> {
                    err.setResult(NIO.of(session).ofInputSource(grabbed.toByteArray()));
                });
                break;
            }
            case GRAB_FILE: {
                NPath temp = NPath.ofTempFile(session);
                temp.setDeleteOnDispose(true);
                temp.setUserTemporary(true);
                h.err = new MyOutHolder(session, temp.getOutputStream(), true, () -> {
                    err.setResult(temp);
                });
                break;
            }
            case REDIRECT: {
                h.err = new OutHolder() {
                    @Override
                    public OutputStream get() {
                        return h.out.get();
                    }

                    @Override
                    public void close() {

                    }
                };
            }
        }
        return h;
    }

    private static class FDHolder implements Closeable {
        InHolder in;
        OutHolder out;
        OutHolder err;

        @Override
        public void close() {
            in.close();
            out.close();
            err.close();
        }
    }

    private interface InHolder extends Closeable {
        InputStream get();

        void close();
    }

    private interface OutHolder extends Closeable {
        OutputStream get();

        void close();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        Object c = context.getConstraints();
        if (c instanceof String) {
            NConnexionString z = NConnexionString.of((String) c).orNull();
            if (z != null && "ssh".equals(z.getProtocol())) {
                return NSupported.DEFAULT_SUPPORT;
            }
        }
        if (c instanceof NConnexionString) {
            NConnexionString z = (NConnexionString) c;
            if ("ssh".equals(z.getProtocol())) {
                return NSupported.DEFAULT_SUPPORT;
            }
        }
        return NSupported.NO_SUPPORT;
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
}
