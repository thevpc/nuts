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

import net.thevpc.nuts.util.NApiUtils;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.util.NClock;
import net.thevpc.nuts.util.NLogger;
import net.thevpc.nuts.util.NLoggerVerb;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
public final class NApplications {

    private static final ThreadLocal<Map<String, Object>> sharedMap = new ThreadLocal<>();

    /**
     * private constructor
     */
    private NApplications() {
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
    @Deprecated
    public static void runApplicationAndExit(NApplication application, String[] args) {
        runApplicationAndExit(application, null, args);
    }

    /**
     * run the given application and call System.exit(?)
     *
     * @param application application
     * @param session     session
     * @param args        arguments
     */
    public static void runApplicationAndExit(NApplication application, NSession session, String[] args) {
        try {
            application.run(session, args);
        } catch (Exception ex) {
            System.exit(NApplications.processThrowable(ex));
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
    @Deprecated
    public static <T extends NApplication> T createApplicationInstance(Class<T> appType) {
        NSession session = null;
        String[] args = null;
        return createApplicationInstance(appType, session, args);
    }

    /**
     * creates application instance by calling
     *
     * @param appType application type
     * @param session session
     * @param args    args
     * @param <T>     application type
     * @return new instance
     */
    @SuppressWarnings("unchecked")
    public static <T extends NApplication> T createApplicationInstance(Class<T> appType, NSession session, String[] args) {
        try {
            for (Method declaredMethod : appType.getDeclaredMethods()) {
                if (Modifier.isStatic(declaredMethod.getModifiers())) {
                    if (declaredMethod.getName().equals("createApplicationInstance")
                            && declaredMethod.getParameterCount() == 2
                            && declaredMethod.getParameterTypes()[0].equals(NSession.class)
                            && declaredMethod.getParameterTypes()[1].equals(String[].class)
                    ) {
                        if (appType.isAssignableFrom(declaredMethod.getReturnType())) {
                            declaredMethod.setAccessible(true);
                            Object o = declaredMethod.invoke(null, session, args);
                            if (o != null) {
                                return appType.cast(o);
                            }
                        } else {
                            throw new NBootException(NMsg.ofCstyle("createApplicationInstance must return %s", appType.getName()));
                        }
                        break;
                    }
                }
            }
            Constructor<T> dconstructor = null;
            for (Constructor<?> constructor : appType.getConstructors()) {
                if (constructor.getParameterCount() == 2
                        && constructor.getParameterTypes()[0].equals(NSession.class)
                        && constructor.getParameterTypes()[1].equals(String[].class)) {
                    return (T) constructor.newInstance(session, args);
                } else if (constructor.getParameterCount() == 0) {
                    dconstructor = (Constructor<T>) constructor;
                }
            }
            if (dconstructor != null) {
                return dconstructor.newInstance();
            }
        } catch (InstantiationException ex) {
            Throwable c = ex.getCause();
            if (c instanceof RuntimeException) {
                throw (RuntimeException) c;
            }
            if (c instanceof Error) {
                throw (Error) c;
            }
            throw new NBootException(NMsg.ofCstyle("unable to instantiate %s", appType.getName()), ex);
        } catch (IllegalAccessException ex) {
            throw new NBootException(NMsg.ofCstyle("illegal access to default constructor for %s", appType.getName()), ex);
        } catch (InvocationTargetException ex) {
            throw new NBootException(NMsg.ofCstyle("invocation exception for %s", appType.getName()), ex);
        }
        throw new NBootException(NMsg.ofCstyle("missing application constructor one of : \n\t static createApplicationInstance(NutsSession,String[])\n\t Constructor(NutsSession,String[])\n\t Constructor()", appType.getName()));
    }

    /**
     * create an Application Context instance for the given arguments. The session can be null.
     *
     * @param applicationInstance application instance (to resolve appId)
     * @param nutsArgs            nutsArgs arguments
     * @param appArgs             application arguments
     * @param session             caller workspace session (or null to create an inherited workspace)
     * @return NutsApplicationContext instance
     */
    public static NApplicationContext createApplicationContext(NApplication applicationInstance, String[] nutsArgs, String[] appArgs, NSession session) {
        NClock startTime = NClock.now();
        if (applicationInstance == null) {
            throw new NullPointerException("null application");
        }
        if (session == null) {
            session = Nuts.openInheritedWorkspace(nutsArgs, appArgs);
        }
        NApplicationContext applicationContext;
        applicationContext = applicationInstance.createApplicationContext(session, nutsArgs, appArgs, startTime);
        if (applicationContext == null) {
            applicationContext = NApplicationContext.of(appArgs, startTime, applicationInstance.getClass(), null, session);
        }

        //copy inter-process parameters only
        NSession ctxSession = applicationContext.getSession();
        ctxSession.setFetchStrategy(session.getFetchStrategy());
        ctxSession.setOutputFormat(session.getOutputFormat());
        ctxSession.setConfirm(session.getConfirm());
        ctxSession.setTrace(session.isTrace());
        ctxSession.setIndexed(session.isIndexed());
        ctxSession.setCached(session.isCached());
        ctxSession.setTransitive(session.isTransitive());
        ctxSession.setTerminal(NSessionTerminal.of(session.getTerminal(), ctxSession));
        return applicationContext;
    }

    /**
     * run application with given life cycle.
     *
     * @param applicationInstance application
     * @param session             session
     * @param nutsArgs            nuts arguments
     * @param args                application arguments
     */
    public static void runApplication(NApplication applicationInstance, NSession session, String[] nutsArgs, String[] args) {
        runApplication(applicationInstance, createApplicationContext(applicationInstance, nutsArgs, args, session));
    }

    public static void runApplication(NApplication applicationInstance, NApplicationContext applicationContext) {
        NSession session = applicationContext.getSession();
        boolean inherited = NBootManager.of(session).getBootOptions().getInherited().orElse(false);
        NLogger.of(NApplications.class, session).with().level(Level.FINE).verb(NLoggerVerb.START)
                .log(
                        NMsg.ofJstyle(
                                "running application {0}: {1} {2}", inherited ? "(inherited)" : "",
                                applicationInstance.getClass().getName(), applicationContext.getCommandLine()
                        )
                );
        switch (applicationContext.getMode()) {
            //both RUN and AUTO_COMPLETE execute the run branch. Later
            //applicationContext.isExecMode()
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
        throw new NExecutionException(session, NMsg.ofCstyle("unsupported execution mode %s", applicationContext.getMode()), 204);
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
    public static int processThrowable(Throwable ex, NLogger out) {
        return NApiUtils.processThrowable(ex, out);
    }


}
