package net.thevpc.nuts.runtime.standalone.xtra.throwables;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.runtime.standalone.log.NutsLogUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.PrintStream;
import java.util.logging.Level;

public class DefaultNutsApplicationExceptionHandler implements NutsApplicationExceptionHandler {
    @Override
    public int processThrowable(String[] args, Throwable throwable, NutsSession session) {
        NutsApiUtils.checkSession(session);
        NutsBootOptions bo = null;
        bo = session.boot().getBootOptions().builder().toBootOptions();
        if (!session.env().isGraphicalDesktopEnvironment()) {
            bo.setGui(false);
        }

        boolean bot = bo.isBot();
        boolean showGui = !bot && bo.isGui();
        boolean showTrace = bo.getDebug()!=null;
        showTrace |= (bo.getLogConfig() != null
                && bo.getLogConfig().getLogTermLevel() != null
                && bo.getLogConfig().getLogTermLevel().intValue() < Level.INFO.intValue());
        if (!showTrace) {
            showTrace = NutsApiUtils.getSysBoolNutsProperty("debug", false);
        }
        if (bot) {
            showTrace = false;
            showGui = false;
        }

        int errorCode = NutsExceptionWithExitCodeBase.resolveExitCode(throwable).orElse(204);
        NutsString fm = NutsSessionAwareExceptionBase.resolveSessionAwareExceptionBase(throwable)
                .map(NutsSessionAwareExceptionBase::getFormattedString).orNull();
        String m = throwable.getMessage();
        if (m == null || m.length() < 5) {
            m = throwable.toString();
        }

        NutsPrintStream fout = null;
        try {
            fout = session.config().getSystemTerminal().getErr();
            if (fm != null) {
                fm = NutsTexts.of(session).ofStyled(fm, NutsTextStyle.error());
            } else {
                fm = NutsTexts.of(session).ofStyled(m, NutsTextStyle.error());
            }
        } catch (Exception ex2) {
            NutsLoggerOp.of(NutsApplications.class, session).level(Level.FINE).error(ex2).log(
                    NutsMessage.jstyle("unable to get system terminal")
            );
        }
        boolean showMessage=true;
        if (fout != null) {
            if (session.getOutputFormat() == NutsContentType.PLAIN) {
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
                    session.eout().add(NutsElements.of(session).ofObject()
                            .set("app-id", session.getAppId() == null ? "" : session.getAppId().toString())
                            .set("error", fm.filteredText())
                            .build()
                    );
                    if (showTrace) {
                        session.eout().add(NutsElements.of(session).ofObject().set("errorTrace",
                                NutsElements.of(session).ofArray().addAll(NutsLogUtils.stacktraceToArray(throwable)).build()
                        ).build());
                    }
                    NutsArrayElementBuilder e = session.eout();
                    if (e.size() > 0) {
                        fout.printlnf(e.build());
                        e.clear();
                    }
                    fout.flush();
                } else {
                    session.eout().add(NutsElements.of(session).ofObject()
                            .set("app-id", session.getAppId() == null ? "" : session.getAppId().toString())
                            .set("error", m)
                            .build());
                    if (showTrace) {
                        session.eout().add(NutsElements.of(session).ofObject().set("errorTrace",
                                NutsElements.of(session).ofArray().addAll(NutsLogUtils.stacktraceToArray(throwable)).build()
                        ).build());
                    }
                    NutsArrayElementBuilder e = session.eout();
                    if (e.size() > 0) {
                        fout.printlnf(e.build());
                        e.clear();
                    }
                    fout.flush();
                }
                fout.flush();
            }
        } else {
            PrintStream out=System.err;
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
                sb.append(fm.filteredText());
            } else {
                sb.append(m);
            }
            if (showTrace) {
                if (sb.length() > 0) {
                    sb.append("\n");
                    sb.append(NutsLogUtils.stacktrace(throwable));
                }
            }
            String title = "Nuts Package Manager - Error";
            try {
                javax.swing.JOptionPane.showMessageDialog(null, NutsMessage.plain(sb.toString()).toString());
            } catch (UnsatisfiedLinkError e) {
                //exception may occur if the sdk is built in headless mode
                System.err.printf("[Graphical Environment Unsupported] %s%n", title);
            }
        }
        return (errorCode);
   }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return 1;
    }
}
