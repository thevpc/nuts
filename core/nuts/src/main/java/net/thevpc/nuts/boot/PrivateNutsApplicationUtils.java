package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

import java.io.PrintStream;
import java.util.logging.Level;

public class PrivateNutsApplicationUtils {

    /**
     * process throwable and return exit code
     *
     * @param ex  exception
     * @param out out stream
     * @return exit code
     */
    public static int processThrowable(Throwable ex, PrintStream out) {
        if (ex == null) {
            return 0;
        }

        NutsSession session = NutsExceptionBase.detectSession(ex);
        NutsWorkspaceOptionsBuilder bo = null;
        if (session != null) {
            bo = session.getWorkspace().env().getBootOptions().builder();
            if(bo.isGui()) {
                if (!session.getWorkspace().env().isGraphicalDesktopEnvironment()) {
                    bo.setGui(false);
                }
            }
        } else {
            NutsWorkspaceOptionsBuilder options = NutsWorkspaceOptionsBuilder.of();
            //load inherited
            String nutsArgs = NutsUtilStrings.trim(
                    NutsUtilStrings.trim(System.getProperty("nuts.boot.args"))
                            + " " + NutsUtilStrings.trim(System.getProperty("nuts.args"))
            );
            try {
                options.parseArguments(NutsApiUtils.parseCommandLineArray(nutsArgs));
            } catch (Exception e) {
                //any, ignore...
            }
            bo = options;
            if(bo.isGui()) {
                if (!NutsApiUtils.isGraphicalDesktopEnvironment()) {
                    bo.setGui(false);
                }
            }
        }

        boolean bot = bo.isBot();
        boolean gui = bo.isGui();
        boolean showTrace = bo.isDebug();
        showTrace |= (bo.getLogConfig() != null
                && bo.getLogConfig().getLogTermLevel() != null
                && bo.getLogConfig().getLogTermLevel().intValue() < Level.INFO.intValue());
        if (!showTrace) {
            showTrace = NutsApiUtils.getSysBoolNutsProperty("debug", false);
        }
        if (bot) {
            showTrace = false;
            gui = false;
        }
        return NutsApiUtils.processThrowable(ex, out, true, showTrace, gui);
    }

    public static int processThrowable(Throwable ex, PrintStream out, boolean showMessage, boolean showTrace, boolean showGui) {
        if (ex == null) {
            return 0;
        }
        NutsExceptionBase neb = NutsExceptionBase.detectExceptionBase(ex);
        NutsBootException nbe = neb != null ? null : NutsBootException.detectBootException(ex);
        int errorCode = 204;
        if (nbe != null) {
            errorCode = nbe.getExitCode();
        } else if (neb instanceof NutsExecutionException) {
            NutsExecutionException ex2 = (NutsExecutionException) ex;
            errorCode = ex2.getExitCode();
        }
        if (errorCode == 0) {
            return 0;
        }
        NutsSession session = null;
        NutsString fm = null;
        if (neb != null) {
            session = neb.getSession();
            fm = neb.getFormattedString();
        }
        String m = ex.getMessage();
        if (m == null || m.length() < 5) {
            m = ex.toString();
        }

        NutsPrintStream fout = null;
        if (out == null) {
            if (session != null) {
                try {
                    fout = session.getWorkspace().term().getSystemTerminal().getErr();
                    if (fm != null) {
                        fm = session.getWorkspace().text().forStyled(fm, NutsTextStyle.error());
                    } else {
                        fm = session.getWorkspace().text().forStyled(m, NutsTextStyle.error());
                    }
                } catch (Exception ex2) {
                    session.getWorkspace().log().of(NutsApplications.class).with().level(Level.FINE).error(ex2).log("unable to get system terminal");
                    //
                }
            } else {
                if (fm != null) {
                    // session is null but the exception is of NutsException type
                    // This is kind of odd, so will ignore message fm
                    fm = null;
                } else {
                    out = System.err;
                }
            }
        } else {
            if (session != null) {
                fout = session.getWorkspace().io().createPrintStream(out, NutsTerminalMode.FORMATTED);
            } else {
                fout = null;
            }
        }
        if (showMessage) {

            if (fout != null) {
                if (session.getOutputFormat() == NutsContentType.PLAIN) {
                    if (fm != null) {
                        fout.println(fm);
                    } else {
                        fout.println(m);
                    }
                    if (showTrace) {
                        ex.printStackTrace(fout.asPrintStream());
                    }
                    fout.flush();
                } else {
                    if (fm != null) {
                        session.eout().add(session.getWorkspace().elem().forObject()
                                .set("app-id", session.getAppId() == null ? "" : session.getAppId().toString())
                                .set("error", fm.filteredText())
                                .build()
                        );
                        if (showTrace) {
                            session.eout().add(session.getWorkspace().elem().forObject().set("error-trace",
                                    session.getWorkspace().elem().forArray().addAll(PrivateNutsUtils.stacktraceToArray(ex)).build()
                            ).build());
                        }
                        NutsArrayElementBuilder e = session.eout();
                        if (e.size() > 0) {
                            session.getWorkspace().formats().object(e.build()).println(fout);
                            e.clear();
                        }
                        fout.flush();
                    } else {
                        session.eout().add(session.getWorkspace().elem().forObject()
                                .set("app-id", session.getAppId() == null ? "" : session.getAppId().toString())
                                .set("error", m)
                                .build());
                        if (showTrace) {
                            session.eout().add(session.getWorkspace().elem().forObject().set("error-trace",
                                    session.getWorkspace().elem().forArray().addAll(PrivateNutsUtils.stacktraceToArray(ex)).build()
                            ).build());
                        }
                        NutsArrayElementBuilder e = session.eout();
                        if (e.size() > 0) {
                            session.getWorkspace().formats().object(e.build()).println(fout);
                            e.clear();
                        }
                        fout.flush();
                    }
                    fout.flush();
                }
            } else {
                if (out == null) {
                    out = System.err;
                }
                if (fm != null) {
                    out.println(fm);
                } else {
                    out.println(m);
                }
                if (showTrace) {
                    ex.printStackTrace(out);
                }
                out.flush();
            }
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
                    sb.append(PrivateNutsUtils.stacktrace(ex));
                }
            }
            if (session != null) {
                //TODO show we delegate to the workspace implementation?
                PrivateNutsGui.showMessage(NutsMessage.plain(sb.toString()).toString(), "Nuts Package Manager - Error");
            } else {
                PrivateNutsGui.showMessage(NutsMessage.plain(sb.toString()).toString(), "Nuts Package Manager - Error");
            }
        }
        return (errorCode);
    }
}
