package net.thevpc.nuts.runtime.standalone.session;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NSessionAware;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NUnsupportedArgumentException;

public class NSessionUtils {
    public static boolean setSession(Object o, NSession session) {
        if (o instanceof NSessionAware) {
            ((NSessionAware) o).setSession(session);
            return true;
        }
        return false;
    }

    public static NSession configureCopyOfSession(NSession session, NExecInput in, NExecOutput out, NExecOutput err) {
        boolean copied = false;
        if (out != null) {
            switch (out.type()) {
                case NULL: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().out(NPrintStream.NULL);
                    break;
                }
                case PATH: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().out(NPrintStream.of(out.path().getOutputStream(
                            out.options().toArray(new NPathOption[0])
                    )));
                    break;
                }
                case STREAM: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().out(NPrintStream.of(out.outputStream()));
                    break;
                }
                case GRAB_STREAM: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    NMemoryPrintStream ps = NMemoryPrintStream.of();
                    out.result(ps.asInputSource());
                    session.getTerminal().out(ps);
                    break;
                }
                case GRAB_FILE: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    NPath nPath = NPath.ofTempFile("grabbed-file");
                    session.getTerminal().out(NPrintStream.of(nPath));
                    out.result(nPath.userTemporary(true));
                    break;
                }
                case PIPE:
                case INHERIT: {
                    //do nothing...
                    break;
                }
                case REDIRECT: {
                    throw new NUnsupportedArgumentException(NMsg.ofC("unsupported out %s", in));
                }
            }
        }
        if (err != null) {
            switch (err.type()) {
                case NULL: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().err(NPrintStream.NULL);
                    break;
                }
                case PATH: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().err(NPrintStream.of(err.path().getOutputStream(
                            err.options().toArray(new NPathOption[0])
                    )));
                    break;
                }
                case STREAM: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().err(NPrintStream.of(err.outputStream()));
                    break;
                }
                case GRAB_STREAM: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().err(NMemoryPrintStream.of());
                    break;
                }
                case GRAB_FILE: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    NPath nPath = NPath.ofTempFile("grabbed-file");
                    session.getTerminal().err(NPrintStream.of(nPath));
                    err.result(nPath.userTemporary(true));
                    break;
                }
                case PIPE:
                case INHERIT: {
                    //do nothing...
                    break;
                }
                case REDIRECT: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().err(session.getTerminal().out());
                    break;
                }
            }
        }
        if (in != null) {
            switch (in.type()) {
                case NULL: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().in(NIO.ofNullRawInputStream());
                    break;
                }
                case PATH: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().in(in.path().getInputStream(in.options().toArray(new NPathOption[0])));
                    break;
                }
                case STREAM: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().in(in.inputStream());
                    break;
                }
                case GRAB_STREAM:
                case GRAB_FILE: {
                    throw new NUnsupportedArgumentException(NMsg.ofC("unsupported in %s", in));
                }
                case PIPE:
                case INHERIT: {
                    //do nothing...
                    break;
                }
            }
        }
        return session;
    }
}
