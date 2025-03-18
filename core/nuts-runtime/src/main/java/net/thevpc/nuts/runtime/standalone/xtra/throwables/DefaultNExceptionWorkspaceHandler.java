package net.thevpc.nuts.runtime.standalone.xtra.throwables;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.elem.NElements;


import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.reserved.NApiUtilsRPI;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.io.PrintStream;
import java.util.logging.Level;

@NComponentScope(NScopeType.SESSION)
public class DefaultNExceptionWorkspaceHandler implements NExceptionWorkspaceHandler {

    public DefaultNExceptionWorkspaceHandler() {

    }



    @Override
    public int processThrowable(String[] args, Throwable throwable) {
        NSession session = NSession.of();
        NWorkspaceOptionsBuilder bo = null;
        bo = NWorkspace.of().getBootOptions().toWorkspaceOptions().builder();
        if (!NWorkspace.of().isGraphicalDesktopEnvironment()) {
            bo.setGui(false);
        }

        boolean showGui = NApiUtilsRPI.resolveGui(bo);
        boolean showTrace = NApiUtilsRPI.resolveShowStackTrace(bo);
        int errorCode = NExceptionHandler.resolveExitCode(throwable).orElse(204);
        NMsg fm = NSessionAwareExceptionBase.resolveSessionAwareExceptionBase(throwable)
                .map(NSessionAwareExceptionBase::getFormattedMessage).orNull();
        String m = throwable.getMessage();
        if (m == null || m.length() < 5) {
            m = throwable.toString();
        }

        NPrintStream fout = null;
        try {
            fout = NIO.of().getSystemTerminal().getErr();
            if (fm != null) {
                fm = NMsg.ofStyledError(fm);
            } else {
                fm = NMsg.ofStyledError(m);
            }
        } catch (Exception ex2) {
            NLogOp.of(NApplications.class).level(Level.FINE).error(ex2).log(
                    NMsg.ofPlain("unable to get system terminal")
            );
        }
        if (fout != null) {
            if (session.getOutputFormat().orDefault() == NContentType.PLAIN) {
                if (fm != null) {
                    fout.println(fm);
                } else {
                    fout.println(m);
                }
                if (showTrace) {
                    throwable.printStackTrace(fout.asPrintStream());
                }
                fout.flush();
            } else {
                if (fm != null) {
                    session.eout().add(NElements.of().ofObjectBuilder()
                            .set("app-id", NStringUtils.toStringOrEmpty(NApp.of().getId().orNull()))
                            .set("error", NText.of(fm).filteredText())
                            .build()
                    );
                    if (showTrace) {
                        session.eout().add(NElements.of().ofObjectBuilder().set("errorTrace",
                                NElements.of().ofArrayBuilder().addAll(NStringUtils.stacktraceArray(throwable)).build()
                        ).build());
                    }
                    NArrayElementBuilder e = session.eout();
                    if (e.size() > 0) {
                        fout.println(e.build());
                        e.clear();
                    }
                    fout.flush();
                } else {
                    session.eout().add(NElements.of().ofObjectBuilder()
                            .set("app-id", NStringUtils.toStringOrEmpty(NApp.of().getId().orNull()))
                            .set("error", m)
                            .build());
                    if (showTrace) {
                        session.eout().add(NElements.of().ofObjectBuilder().set("errorTrace",
                                NElements.of().ofArrayBuilder().addAll(NStringUtils.stacktraceArray(throwable)).build()
                        ).build());
                    }
                    NArrayElementBuilder e = session.eout();
                    if (e.size() > 0) {
                        fout.println(e.build());
                        e.clear();
                    }
                    fout.flush();
                }
                fout.flush();
            }
        } else {
            PrintStream out = System.err;
            if (fm != null) {
                out.println(fm);
            } else {
                out.println(m);
            }
            if (showTrace) {
                throwable.printStackTrace(out);
            }
            out.flush();
        }
        if (showGui) {
            StringBuilder sb = new StringBuilder();
            if (fm != null) {
                sb.append(NText.of(fm).filteredText());
            } else {
                sb.append(m);
            }
            if (showTrace) {
                if (sb.length() > 0) {
                    sb.append("\n");
                    sb.append(NStringUtils.stacktrace(throwable));
                }
            }
            String title = "Nuts Package Manager - Error";
            try {
                javax.swing.JOptionPane.showMessageDialog(null, NMsg.ofPlain(sb.toString()).toString());
            } catch (UnsatisfiedLinkError e) {
                //exception may occur if the sdk is built in headless mode
                System.err.printf("[Graphical Environment Unsupported] %s%n", title);
            }
        }
        return (errorCode);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
