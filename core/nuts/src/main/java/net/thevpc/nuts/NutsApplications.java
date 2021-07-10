/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Helper class for Nuts Applications
 *
 * @author thevpc
 * @since 0.5.5
 * @category Application
 */
public final class NutsApplications {

    private static final ThreadLocal<Map<String, Object>> sharedMap = new ThreadLocal<>();

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

    /**
     * private constructor
     */
    private NutsApplications() {
    }

    public static void runApplicationAndExit(NutsApplication application, String[] args) {
        try {
            application.run(args);
        } catch (Exception ex) {
            System.exit(NutsApplications.processThrowable(ex, args, null));
            return;
        }
        System.exit(0);
    }

    public static <T extends NutsApplication> T createApplicationInstance(Class<T> appType){
        T newInstance;
        try {
            newInstance = appType.newInstance();
            return newInstance;
        } catch (InstantiationException ex) {
            Throwable c = ex.getCause();
            if(c instanceof RuntimeException){
                throw (RuntimeException)c;
            }
            if(c instanceof Error){
                throw (Error)c;
            }
            throw new NutsBootException("unable to instantiate "+appType.getName(),ex);
        } catch (IllegalAccessException ex) {
            throw new NutsBootException("illegal access to default constructor for "+appType.getName(),ex);
        }
    }
    /**
     * run application with given life cycle.
     *
     * @param args application arguments
     * @param session session
     * @param applicationInstance application
     */
    public static void runApplication(String[] args, NutsSession session, NutsApplication applicationInstance) {
        long startTimeMillis = System.currentTimeMillis();
        if (applicationInstance == null) {
            throw new NullPointerException("null application");
        }
        boolean inherited = false;
        NutsWorkspace ws = session == null ? null : session.getWorkspace();
        if (ws == null) {
            inherited = true;
            ws = Nuts.openInheritedWorkspace(args);
        }
        if (session == null) {
            session = ws.createSession();
        }
        ws.log().setSession(session).of(NutsApplications.class).with().session(session).level(Level.FINE).verb(NutsLogVerb.START).formatted()
                .log("running application {0}: {1} {2}", inherited ? "(inherited)" : "",
                        applicationInstance, ws.commandLine().setSession(session).create(args)
                );
        NutsApplicationContext applicationContext = null;
        applicationContext = applicationInstance.createApplicationContext(ws, args, startTimeMillis);
        if (applicationContext == null) {
            applicationContext = ws.apps().createApplicationContext(args, applicationInstance.getClass(), null, startTimeMillis, session);
        }
        if (session != null) {
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
        }
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
        throw new NutsExecutionException(session, "unsupported execution mode " + applicationContext.getMode(), 204);
    }

    /**
     * process throwable and return exit code
     *
     * @param ex exception
     * @param args application arguments to check from if a '--verbose' or
     * '--debug' option is armed
     * @param out out stream
     * @return exit code
     */
    public static int processThrowable(Throwable ex, String[] args, PrintStream out) {
        if (ex == null) {
            return 0;
        }
        int errorCode = 204;
        boolean showTrace = false;
        String nutsArgs = System.getProperty("nuts.args");
        if (nutsArgs != null) {
            String[] aargs = PrivateNutsCommandLine.parseCommandLineArray(nutsArgs);
            for (String arg : aargs) {
                if (arg.equals("--verbose") || arg.equals("--debug")) {
                    showTrace = true;
                    break;
                }
            }
        }
        if (!showTrace) {
            showTrace = PrivateNutsUtils.getSysBoolNutsProperty("debug", false);
        }
        if (!showTrace && args != null) {
            for (String arg : args) {
                if (arg.startsWith("-")) {
                    if (arg.equals("--verbose") || arg.equals("--debug")) {
                        showTrace = true;
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        if (ex instanceof NutsBootException) {
            NutsBootException ex2 = (NutsBootException) ex;
            if (ex2.getExitCode() == 0) {
                return 0;
            } else {
                errorCode = ex2.getExitCode();
            }
        }else if (ex instanceof NutsExecutionException) {
            NutsExecutionException ex2 = (NutsExecutionException) ex;
            if (ex2.getExitCode() == 0) {
                return 0;
            } else {
                errorCode = ex2.getExitCode();
            }
        }
        String m = ex.getMessage();
        if (m == null || m.length() < 5) {
            m = ex.toString();
        }
        NutsSession session = null;
        if (ex instanceof NutsException) {
            session = ((NutsException) ex).getSession();
        }
        if (session == null) {
            if (ex instanceof NutsSecurityException) {
                session = ((NutsSecurityException) ex).getSession();
            }
        }
        if (session != null) {
            try {
                NutsWorkspaceConfigManager cfg = session.getWorkspace().config();
                showTrace = cfg.getOptions().isDebug()
                        || (cfg.getOptions().getLogConfig() != null
                        && cfg.getOptions().getLogConfig() != null
                        && cfg.getOptions().getLogConfig().getLogTermLevel() != null
                        && cfg.getOptions().getLogConfig().getLogTermLevel().intValue() <= Level.FINE.intValue());
            } catch (Exception ex2) {
                session.getWorkspace().log().of(NutsApplications.class).with().level(Level.FINE).error(ex2).log("unable to check if option debug is enabled");
            }
        }
//        if (showTrace) {
//            LOG.log(Level.SEVERE, m, ex);
//        }
        if (out == null && session != null) {
            try {
                out = session.getWorkspace().term().getSystemTerminal().getOut().asPrintStream();
                m = "```error " + m + "```";
            } catch (Exception ex2) {
                session.getWorkspace().log().of(NutsApplications.class).with().level(Level.FINE).error(ex2).log("unable to get system terminal");
                //
            }
        }
        if (out == null) {
            out = System.err;
        }
        out.println(m);
        if (showTrace) {
            ex.printStackTrace(out);
        }
        out.flush();
        return (errorCode);
    }
}
