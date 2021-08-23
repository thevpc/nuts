/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Helper class for Nuts Applications
 *
 * @author thevpc
 * @app.category Application
 * @since 0.5.5
 */
public final class NutsApplications {

    private static final ThreadLocal<Map<String, Object>> sharedMap = new ThreadLocal<>();

    /**
     * private constructor
     */
    private NutsApplications() {
    }

    /**
     * a thread local map used to share information between workspace and
     * embedded applications.
     *
     * @return thread local map
     */
    public static Map<String, Object> getSharedMap() {
        Map<String, Object> m = sharedMap.get();
        if (m == null) {
            m = new LinkedHashMap<>();
            sharedMap.set(m);
        }
        return m;
    }

    public static void runApplicationAndExit(NutsApplication application, String[] args) {
        try {
            application.run(args);
        } catch (Exception ex) {
            System.exit(NutsApplications.processThrowable(ex));
            return;
        }
        System.exit(0);
    }

    public static <T extends NutsApplication> T createApplicationInstance(Class<T> appType) {
        T newInstance;
        try {
            newInstance = appType.newInstance();
            return newInstance;
        } catch (InstantiationException ex) {
            Throwable c = ex.getCause();
            if (c instanceof RuntimeException) {
                throw (RuntimeException) c;
            }
            if (c instanceof Error) {
                throw (Error) c;
            }
            throw new NutsBootException(NutsMessage.cstyle("unable to instantiate %s", appType.getName()), ex);
        } catch (IllegalAccessException ex) {
            throw new NutsBootException(NutsMessage.cstyle("illegal access to default constructor for %s", appType.getName()), ex);
        }
    }

    /**
     * create an Application Context instance for the given arguments. The session can be null.
     *
     * @param applicationInstance application instance (to resolve appId)
     * @param args                application arguments
     * @param session             caller workspace session (or null to create an inherited workspace)
     * @return NutsApplicationContext instance
     */
    public static NutsApplicationContext createApplicationContext(NutsApplication applicationInstance, String[] args, NutsSession session) {
        long startTimeMillis = System.currentTimeMillis();
        if (applicationInstance == null) {
            throw new NullPointerException("null application");
        }
        if (session == null) {
            session = Nuts.openInheritedWorkspace(args);
        }
        NutsWorkspace ws = session.getWorkspace();
        NutsApplicationContext applicationContext = null;
        applicationContext = applicationInstance.createApplicationContext(session, args, startTimeMillis);
        if (applicationContext == null) {
            applicationContext = ws.apps().createApplicationContext(session, args, startTimeMillis, applicationInstance.getClass(), null);
        }

        //copy inter-process parameters only
        NutsSession ctxSession = applicationContext.getSession();
        ctxSession.setFetchStrategy(session.getFetchStrategy());
        ctxSession.setOutputFormat(session.getOutputFormat());
        ctxSession.setConfirm(session.getConfirm());
        ctxSession.setTrace(session.isTrace());
        ctxSession.setIndexed(session.isIndexed());
        ctxSession.setCached(session.isCached());
        ctxSession.setTransitive(session.isTransitive());
        ctxSession.setTerminal(ctxSession.getWorkspace().term().createTerminal(session.getTerminal()));
        return applicationContext;
    }

    /**
     * run application with given life cycle.
     *
     * @param applicationInstance application
     * @param args                application arguments
     * @param session             session
     */
    public static void runApplication(NutsApplication applicationInstance, String[] args, NutsSession session) {
        NutsApplicationContext applicationContext = createApplicationContext(applicationInstance, args, session);
        NutsWorkspace ws = applicationContext.getWorkspace();
        boolean inherited = ws.env().getBootOptions().isInherited();
        ws.log().of(NutsApplications.class).with().level(Level.FINE).verb(NutsLogVerb.START).formatted()
                .log("running application {0}: {1} {2}", inherited ? "(inherited)" : "",
                        applicationInstance.getClass().getName(), ws.commandLine().create(args)
                );
        switch (applicationContext.getMode()) {
            /**
             * both RUN and AUTO_COMPLETE executes the save branch. Later
             * applicationContext.isExecMode()
             */
            case RUN:
            case AUTO_COMPLETE: {
                applicationInstance.run(applicationContext);
                return;
            }
            case INSTALL: {
                applicationInstance.onInstallApplication(applicationContext);
                return;
            }
            case UPDATE: {
                applicationInstance.onUpdateApplication(applicationContext);
                return;
            }
            case UNINSTALL: {
                applicationInstance.onUninstallApplication(applicationContext);
                return;
            }
        }
        throw new NutsExecutionException(session, NutsMessage.cstyle("unsupported execution mode %s", applicationContext.getMode()), 204);
    }

    public static int processThrowable(Throwable ex) {
        return processThrowable(ex, null);
    }

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
            if (!session.getWorkspace().env().isGraphicalDesktopEnvironment()) {
                bo.setGui(false);
            }
        } else {
            NutsWorkspaceOptionsBuilder options = Nuts.createOptionsBuilder();
            //load inherited
            String nutsArgs = NutsUtilStrings.trim(
                    NutsUtilStrings.trim(System.getProperty("nuts.boot.args"))
                            + " " + NutsUtilStrings.trim(System.getProperty("nuts.args"))
            );
            try {
                options.parseArguments(PrivateNutsCommandLine.parseCommandLineArray(nutsArgs));
            } catch (Exception e) {
                //any, ignore...
            }
            bo = options;
            if (!isGraphicalDesktopEnvironment0()) {
                bo.setGui(false);
            }
        }

        boolean bot = bo.isBot();
        boolean gui = bo.isGui();
        boolean showTrace = bo.isDebug();
        showTrace |= (bo.getLogConfig() != null
                && bo.getLogConfig().getLogTermLevel() != null
                && bo.getLogConfig().getLogTermLevel().intValue() < Level.INFO.intValue());
        if (!showTrace) {
            showTrace = PrivateNutsUtils.getSysBoolNutsProperty("debug", false);
        }
        if (bot) {
            showTrace = false;
            gui = false;
        }
        return processThrowable(ex, out, true, showTrace, gui);
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
                                    session.getWorkspace().elem().forArray().addAll(stacktrace(ex)).build()
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
                                    session.getWorkspace().elem().forArray().addAll(stacktrace(ex)).build()
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
                javax.swing.JOptionPane.showMessageDialog(null, NutsMessage.plain(sb.toString()).toString());
            } else {
                javax.swing.JOptionPane.showMessageDialog(null, NutsMessage.plain(sb.toString()).toString());
            }
        }
        return (errorCode);
    }

    private static String[] stacktrace(Throwable th) {
        try {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                th.printStackTrace(pw);
            }
            BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
            List<String> s = new ArrayList<>();
            String line = null;
            while ((line = br.readLine()) != null) {
                s.add(line);
            }
            return s.toArray(new String[0]);
        } catch (Exception ex) {
            // ignore
        }
        return new String[0];
    }

    private static boolean isGraphicalDesktopEnvironment0() {
        try {
            return !java.awt.GraphicsEnvironment.isHeadless();
        } catch (Exception e) {
            //exception may occur if the sdk is build without awt package for instance!
            return false;
        }
    }
}
