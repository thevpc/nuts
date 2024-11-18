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
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.reserved.NApiUtilsRPI;
import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NMsg;

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
    public static void runApplicationAndExit(NApplication application, String[] args) {
        try {
            application.run(args);
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
        String[] args = null;
        return createApplicationInstance(appType, args);
    }

    /**
     * creates application instance by calling
     *
     * @param <T>     application type
     * @param appType application type
     * @param args    args
     * @return new instance
     */
    @SuppressWarnings("unchecked")
    public static <T extends NApplication> T createApplicationInstance(Class<T> appType, String[] args) {
        try {
            for (Method declaredMethod : appType.getDeclaredMethods()) {
                if (Modifier.isStatic(declaredMethod.getModifiers())) {
                    if (declaredMethod.getName().equals("createApplicationInstance")
                            && declaredMethod.getParameterCount() == 1
                            && declaredMethod.getParameterTypes()[0].equals(String[].class)
                    ) {
                        if (appType.isAssignableFrom(declaredMethod.getReturnType())) {
                            declaredMethod.setAccessible(true);
                            Object o = declaredMethod.invoke(null, (Object)args);
                            if (o != null) {
                                return appType.cast(o);
                            }
                        } else {
                            throw new NBootException(NMsg.ofC("createApplicationInstance must return %s", appType.getName()));
                        }
                        break;
                    }
                }
            }
            Constructor<T> dconstructor = null;
            for (Constructor<?> constructor : appType.getConstructors()) {
                if (constructor.getParameterCount() == 1
                        && constructor.getParameterTypes()[0].equals(String[].class)) {
                    return (T) constructor.newInstance((Object)args);
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
            throw new NBootException(NMsg.ofC("unable to instantiate application %s", appType.getName()), ex);
        } catch (IllegalAccessException ex) {
            throw new NBootException(NMsg.ofC("illegal access to default constructor for %s", appType.getName()), ex);
        } catch (InvocationTargetException ex) {
            throw new NBootException(NMsg.ofC("invocation exception for %s", appType.getName()), ex);
        }
        throw new NBootException(NMsg.ofC("missing application constructor for %s from of : \n\t static createApplicationInstance(NutsSession,String[])\n\t Constructor(NutsSession,String[])\n\t Constructor()", appType.getName()));
    }

    /**
     * run application with given life cycle.
     *
     * @param applicationInstance application
     * @param nutsArgs            nuts arguments
     * @param args                application arguments
     */
    public static void runApplication(NApplication applicationInstance, String[] nutsArgs, String[] args) {
        NClock now = NClock.now();
        NWorkspace ws = NWorkspace.of().orNull();
        if (ws == null) {
            ws = Nuts.openInheritedWorkspace(nutsArgs, args).getWorkspace();
            NWorkspace finalWs = ws;
            ws.runWith(()->{
                finalWs.currentSession().prepareApplication(args, applicationInstance.getClass(), null, now);
                runApplication(applicationInstance);
            });
        }else {
            ws.currentSession().prepareApplication(args, applicationInstance.getClass(), null, now);
            runApplication(applicationInstance);
        }
    }

    public static void runApplication(NApplication applicationInstance) {
        NWorkspace ws = NWorkspace.of().orNull();
        if (ws == null) {
            ws = Nuts.openInheritedWorkspace(new String[0], new String[0]).getWorkspace();
            ws.runWith(() -> {
                runApplication(applicationInstance);
            });
            return;
        }
        NSession session=NSession.of().get();
        boolean inherited = NBootManager.of().getBootOptions().getInherited().orElse(false);
        NLog.of(NApplications.class).with().level(Level.FINE).verb(NLogVerb.START)
                .log(
                        NMsg.ofC(
                                "running application %s: %s %s",
                                inherited ? "(inherited)" : "",
                                applicationInstance.getClass().getName(),
                                session.getAppCmdLine()
                        )
                );
        try {
            switch (session.getAppMode()) {
                //both RUN and AUTO_COMPLETE execute the run branch. Later
                //session.isExecMode()
                case RUN:
                case AUTO_COMPLETE: {
                    applicationInstance.run();
                    return;
                }
                case INSTALL: {
                    applicationInstance.onInstallApplication();
                    return;
                }
                case UPDATE: {
                    applicationInstance.onUpdateApplication();
                    return;
                }
                case UNINSTALL: {
                    applicationInstance.onUninstallApplication();
                    return;
                }
            }
        } catch (NExecutionException e) {
            if (e.getExitCode() == NExecutionException.SUCCESS) {
                return;
            }
            throw e;
        }
        throw new NExecutionException(NMsg.ofC("unsupported execution mode %s", session.getAppMode()), NExecutionException.ERROR_255);
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
    public static int processThrowable(Throwable ex, NLog out) {
        return NApiUtilsRPI.processThrowable(ex, out);
    }


}
