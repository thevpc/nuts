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

import net.thevpc.nuts.boot.NutsApiUtils;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
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

    /**
     * run the given application and call System.exit(?)
     *
     * @param application application
     * @param args        arguments
     */
    public static void runApplicationAndExit(NutsApplication application, String[] args) {
        try {
            application.run(args);
        } catch (Exception ex) {
            System.exit(NutsApplications.processThrowable(ex));
            return;
        }
        System.exit(0);
    }

    /**
     * creates application instance by calling
     *
     * @param appType application type
     * @param <T>     application type
     * @return new instance
     */
    public static <T extends NutsApplication> T createApplicationInstance(Class<T> appType) {
        T newInstance;
        try {
            newInstance = appType.getConstructor().newInstance();
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
        } catch (InvocationTargetException ex) {
            throw new NutsBootException(NutsMessage.cstyle("invocation exception for %s", appType.getName()), ex);
        } catch (NoSuchMethodException ex) {
            throw new NutsBootException(NutsMessage.cstyle("missing default constructor for %s", appType.getName()), ex);
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
        ctxSession.setTerminal(ctxSession.term().createTerminal(session.getTerminal()));
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
        boolean inherited = ws.boot().getBootOptions().isInherited();
        ws.log().of(NutsApplications.class).with().level(Level.FINE).verb(NutsLogVerb.START).formatted()
                .log("running application {0}: {1} {2}", inherited ? "(inherited)" : "",
                        applicationInstance.getClass().getName(), ws.commandLine().create(args)
                );
        switch (applicationContext.getMode()) {
            /**
             * both RUN and AUTO_COMPLETE execute the save branch. Later
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

    /**
     * process throwable and extract exit code
     *
     * @param ex throwable
     * @return exit code
     */
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
        return NutsApiUtils.processThrowable(ex, out);
    }


}
