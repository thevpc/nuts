package net.thevpc.nuts.runtime.standalone.session;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NSessionAware;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;

import java.util.Objects;

public class NSessionUtils {
    public static void checkSession(NWorkspace ws, NSession session) {
        NAssert.requireSession(session);
        if (!Objects.equals(session.getWorkspace().getUuid(), ws.getUuid())) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid session %s != %s ; %s != %s ; %s != %s ; ",
                    session.getWorkspace().getName(), ws.getName(),
                    session.getWorkspace().getLocation(), ws.getLocation(),
                    session.getWorkspace().getUuid(), ws.getUuid()
            ));
        }
    }

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
            switch (out.getType()) {
                case NULL: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().setOut(NPrintStream.NULL);
                    break;
                }
                case PATH: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().setOut(NPrintStream.of(out.getPath().getOutputStream(
                            out.getOptions()
                    )));
                    break;
                }
                case STREAM: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().setOut(NPrintStream.of(out.getStream()));
                    break;
                }
                case GRAB_STREAM: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    NMemoryPrintStream ps = NMemoryPrintStream.of();
                    out.setResult(ps.asInputSource());
                    session.getTerminal().setOut(ps);
                    break;
                }
                case GRAB_FILE: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    NPath nPath = NPath.ofTempFile("grabbed-file");
                    session.getTerminal().setOut(NPrintStream.of(nPath));
                    out.setResult(nPath.setUserTemporary(true));
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
            switch (err.getType()) {
                case NULL: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().setErr(NPrintStream.NULL);
                    break;
                }
                case PATH: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().setErr(NPrintStream.of(err.getPath().getOutputStream(
                            err.getOptions()
                    )));
                    break;
                }
                case STREAM: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().setErr(NPrintStream.of(err.getStream()));
                    break;
                }
                case GRAB_STREAM: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().setErr(NMemoryPrintStream.of());
                    break;
                }
                case GRAB_FILE: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    NPath nPath = NPath.ofTempFile("grabbed-file");
                    session.getTerminal().setErr(NPrintStream.of(nPath));
                    err.setResult(nPath.setUserTemporary(true));
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
                    session.getTerminal().setErr(session.getTerminal().getOut());
                    break;
                }
            }
        }
        if (in != null) {
            switch (in.getType()) {
                case NULL: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().setIn(NIO.ofNullRawInputStream());
                    break;
                }
                case PATH: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().setIn(in.getPath().getInputStream(in.getOptions()));
                    break;
                }
                case STREAM: {
                    if (!copied) {
                        copied = true;
                        session = session.copy();
                    }
                    session.getTerminal().setIn(in.getStream());
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
