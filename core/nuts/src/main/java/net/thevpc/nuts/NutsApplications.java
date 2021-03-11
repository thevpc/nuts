/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
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

    private static final ThreadLocal<Map<String,Object>> sharedMap=new ThreadLocal<>();

    /**
     * a thread local map used to share information between workspace
     * and embedded applications.
     * @return thread local map
     */
    public static Map<String, Object> getSharedMap() {
        Map<String, Object> m = sharedMap.get();
        if(m==null){
            m=new LinkedHashMap<>();
            sharedMap.set(m);
        }
        return m;
    }

    /**
     * private constructor
     */
    private NutsApplications() {
    }

    /**
     * run application with given life cycle.
     *
     * @param args application arguments
     * @param session session
     * @param appClass application class
     * @param lifeCycle application life cycle
     */
    public static void runApplication(String[] args, NutsSession session, Class appClass,NutsApplicationLifeCycle lifeCycle) {
        long startTimeMillis = System.currentTimeMillis();
        if (lifeCycle == null) {
            throw new NullPointerException("null application");
        }
        boolean inherited = false;
        NutsWorkspace ws=session==null?null:session.getWorkspace();
        if (ws == null) {
            inherited = true;
            ws = Nuts.openInheritedWorkspace(args);
        }
        if(session==null){
            session=ws.createSession();
        }
        if(appClass==null){
            appClass=lifeCycle.getClass();
        }
        ws.log().of(NutsApplications.class).with().session(session).level(Level.FINE).verb(NutsLogVerb.START).formatted()
                .log("running application {0}: {1} {2}", inherited ? "(inherited)" : "",
                lifeCycle, ws.commandLine().create(args)
        );
        NutsApplicationContext applicationContext = null;
        applicationContext = lifeCycle.createApplicationContext(ws, args, startTimeMillis);
        if (applicationContext == null) {
            applicationContext = ws.apps().createApplicationContext(args, appClass, null, startTimeMillis, session);
        }
        if(session!=null) {
            //copy inter-process parameters only
            NutsSession ctxSession = applicationContext.getSession();
            ctxSession.setFetchStrategy(session.getFetchStrategy());
            ctxSession.setOutputFormat(session.getOutputFormat());
            ctxSession.setConfirm(session.getConfirm());
            ctxSession.setTrace(session.isTrace());
            ctxSession.setIndexed(session.isIndexed());
            ctxSession.setCached(session.isCached());
            ctxSession.setTransitive(session.isTransitive());
            ctxSession.setTerminal(session.getTerminal());
        }
        switch (applicationContext.getMode()) {
            /**
             * both RUN and AUTO_COMPLETE executes the save branch. Later
             * applicationContext.isExecMode()
             */
            case RUN:
            case AUTO_COMPLETE: {
                lifeCycle.onRunApplication(applicationContext);
                return;
            }
            case INSTALL: {
                lifeCycle.onInstallApplication(applicationContext);
                return;
            }
            case UPDATE: {
                lifeCycle.onUpdateApplication(applicationContext);
                return;
            }
            case UNINSTALL: {
                lifeCycle.onUninstallApplication(applicationContext);
                return;
            }
        }
        throw new NutsExecutionException(ws, "unsupported execution mode " + applicationContext.getMode(), 204);
    }

    /**
     * process throwables and return exit code
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
        boolean showTrace=false;
        String nutsArgs = System.getProperty("nuts.args");
        if(nutsArgs!=null){
            String[] aargs = PrivateNutsCommandLine.parseCommandLineArray(nutsArgs);
            for (String arg : aargs) {
                if(arg.equals("--verbose") || arg.equals("--debug")){
                    showTrace = true;
                    break;
                }
            }
        }
        if(!showTrace) {
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
        if (ex instanceof NutsExecutionException) {
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
        NutsWorkspace ws = null;
        if (ex instanceof NutsException) {
            ws = ((NutsException) ex).getWorkspace();
        }
        if (ws == null) {
            if (ex instanceof NutsSecurityException) {
                ws = ((NutsSecurityException) ex).getWorkspace();
            }
        }
        if (ws != null) {
            try {
                showTrace = ws.config().getOptions().isDebug() || 
                        (
                        ws.config().getOptions().getLogConfig()!=null 
                        && ws.config().getOptions().getLogConfig()!=null
                        && ws.config().getOptions().getLogConfig().getLogTermLevel()!=null
                        && ws.config().getOptions().getLogConfig().getLogTermLevel().intValue()<=Level.FINE.intValue()
                        );
            } catch (Exception ex2) {
                ws.log().of(NutsApplications.class).with().session(ws.createSession()).level(Level.FINE).error(ex2).log("unable to check if option debug is enabled");
            }
        }
//        if (showTrace) {
//            LOG.log(Level.SEVERE, m, ex);
//        }
        if (out == null && ws != null) {
            try {
                out = ws.io().term().getSystemTerminal().getOut();
                m = "```error " + m + "```";
            } catch (Exception ex2) {
                ws.log().of(NutsApplications.class).with().session(ws.createSession()).level(Level.FINE).error(ex2).log("unable to get system terminal");
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
