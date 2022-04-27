/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.DefaultNutsWorkspaceBootOptionsBuilder;

import java.io.PrintStream;
import java.util.logging.Level;

public class PrivateNutsUtilApplication {

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

        NutsSession session = NutsSessionAwareExceptionBase.resolveSession(ex).orNull();
        NutsWorkspaceBootOptionsBuilder bo = null;
        if (session != null) {
            bo = session.boot().getBootOptions().builder();
            if (bo.isGui()) {
                if (!session.env().isGraphicalDesktopEnvironment()) {
                    bo.setGui(false);
                }
            }
        } else {
            PrivateNutsBootLog log = new PrivateNutsBootLog(new NutsBootTerminal(null,out,out));
            NutsWorkspaceBootOptionsBuilder options = new DefaultNutsWorkspaceBootOptionsBuilder();
            //load inherited
            String nutsArgs = NutsUtilStrings.trim(
                    NutsUtilStrings.trim(System.getProperty("nuts.boot.args"))
                            + " " + NutsUtilStrings.trim(System.getProperty("nuts.args"))
            );
            try {
                options.setCommandLine(NutsCommandLine.parseDefault(nutsArgs).get().toStringArray(),null);
            } catch (Exception e) {
                //any, ignore...
            }
            bo = options;
            if (bo.isGui()) {
                if (!NutsApiUtils.isGraphicalDesktopEnvironment()) {
                    bo.setGui(false);
                }
            }
        }

        boolean bot = bo.isBot();
        boolean gui = bo.isGui();
        boolean showTrace = bo.getDebug()!=null;
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
        NutsSession session = NutsSessionAwareExceptionBase.resolveSession(ex).orNull();
        NutsString fm = NutsSessionAwareExceptionBase.resolveSessionAwareExceptionBase(ex).map(NutsSessionAwareExceptionBase::getFormattedString)
                .orNull();
        int errorCode = NutsExceptionWithExitCodeBase.resolveExitCode(ex).orElse(204);
        if (errorCode == 0) {
            return 0;
        }
        String m = PrivateNutsUtilErrors.getErrorMessage(ex);
        NutsPrintStream fout = null;
        if (out == null) {
            if (session != null) {
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
                fout = NutsPrintStream.of(out, NutsTerminalMode.FORMATTED,null, session);
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
                        session.eout().add(NutsElements.of(session).ofObject()
                                .set("app-id", session.getAppId() == null ? "" : session.getAppId().toString())
                                .set("error", fm.filteredText())
                                .build()
                        );
                        if (showTrace) {
                            session.eout().add(NutsElements.of(session).ofObject().set("errorTrace",
                                    NutsElements.of(session).ofArray().addAll(PrivateNutsUtilErrors.stacktraceToArray(ex)).build()
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
                                    NutsElements.of(session).ofArray().addAll(PrivateNutsUtilErrors.stacktraceToArray(ex)).build()
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
                    sb.append(PrivateNutsUtilErrors.stacktrace(ex));
                }
            }
            if (session != null) {
                //TODO show we delegate to the workspace implementation?
                PrivateNutsUtilGui.showMessage(NutsMessage.plain(sb.toString()).toString(), "Nuts Package Manager - Error", out);
            } else {
                PrivateNutsUtilGui.showMessage(NutsMessage.plain(sb.toString()).toString(), "Nuts Package Manager - Error", out);
            }
        }
        return (errorCode);
    }
}
