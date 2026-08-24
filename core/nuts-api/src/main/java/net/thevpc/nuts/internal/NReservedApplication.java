/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
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
package net.thevpc.nuts.internal;


import net.thevpc.nuts.app.*;
import net.thevpc.nuts.reflect.NReflectUtils;
import net.thevpc.nuts.util.NException;
import net.thevpc.nuts.boot.NBootException;
import net.thevpc.nuts.boot.internal.util.NBootMsg;
import net.thevpc.nuts.text.NI18n;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.text.NMsg;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Helper class for Nuts Applications
 *
 * @author thevpc
 * @app.category Application
 * @since 0.5.5
 */
public final class NReservedApplication {

    private static final ThreadLocal<Map<String, Object>> sharedMap = new ThreadLocal<>();

    /**
     * private constructor
     */
    private NReservedApplication() {
    }

    /**
     * a thread local map used to share information between workspace and
     * embedded applications.
     *
     * @return thread local map
     */
    public static Map<String, Object> sharedMap() {
        Map<String, Object> m = sharedMap.get();
        if (m == null) {
            m = new LinkedHashMap<>();
            sharedMap.set(m);
        }
        return m;
    }


    /**
     * creates application instance by calling
     *
     * @param appType application type
     * @param <T>     application type
     * @return new instance
     */
    @Deprecated
    public static <T extends NApplicationHandler> T createApplicationInstance(Class<T> appType) {
        String[] args = null;
        return createApplicationInstance(appType, args);
    }

    public static NApp resolveApplicationAnnotation(Class appClass) {
        Class<?> validAppClass = NReflectUtils.unproxyType(appClass);
        return validAppClass.getAnnotation(NApp.class);
    }

    public static boolean isAnnotatedApplicationClass(Class appClass) {
        return resolveApplicationAnnotation(appClass) != null;
    }

    public static NApplicationHandler createApplicationInstanceFromAnnotatedInstance(Object appInstance) {
        NAssert.requireNamedNonNull(appInstance, "appInstance");
        if (appInstance instanceof NApplicationHandler) {
            return (NApplicationHandler) appInstance;
        }
        Class<?> appClass = NReflectUtils.unproxyType(appInstance.getClass());
        NApp appAnnotation = appClass.getAnnotation(NApp.class);
        if (appAnnotation == null) {
            throw new NBootException(NBootMsg.ofC("class %s is missing annotation @" + NApp.class.getSimpleName(), appClass.getName()));
        }
        NAssert.requireNamedNonNull(appAnnotation, "@NApp annotation");
        List<Method> runMethods = new ArrayList<>();
        List<Method> installMethods = new ArrayList<>();
        List<Method> uninstallMethods = new ArrayList<>();
        List<Method> updateMethods = new ArrayList<>();
        List<Method> completeMethods = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Class cc = appClass;
        while (cc != null) {
            for (Method m : cc.getMethods()) {
                // only public methods
                if (m.getParameterCount() == 0) {
                    if (visited.add(m.getName())) {
                        if (m.getAnnotation(NAppRun.class) != null) {
                            runMethods.add(m);
                        }
                        if (m.getAnnotation(NAppUpdate.class) != null) {
                            updateMethods.add(m);
                        }
                        if (m.getAnnotation(NAppInstall.class) != null) {
                            installMethods.add(m);
                        }
                        if (m.getAnnotation(NAppUninstall.class) != null) {
                            uninstallMethods.add(m);
                        }
                        if (m.getAnnotation(NAppComplete.class) != null) {
                            completeMethods.add(m);
                        }
                    }
                }
            }
            try {
                for (Method m : cc.getDeclaredMethods()) {
                    checkAllowedMethodWithNutsAnnotation(m, NAppRun.class);
                    checkAllowedMethodWithNutsAnnotation(m, NAppInstall.class);
                    checkAllowedMethodWithNutsAnnotation(m, NAppUninstall.class);
                    checkAllowedMethodWithNutsAnnotation(m, NAppUpdate.class);
                    checkAllowedMethodWithNutsAnnotation(m, NAppComplete.class);
                }
            } catch (NBootException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            cc = cc.getSuperclass();
        }
//        if (runMethods.isEmpty()) {
//            throw new NBootException(NBootMsg.ofC("class %s has annotation @NApp. it should define a public no arg @NAppRun method", appClass.getName()));
//        }
        return new AnnotationClassNApplicationHandler(runMethods, installMethods, updateMethods, uninstallMethods, completeMethods, appInstance);
    }

    private static boolean checkAllowedMethodWithNutsAnnotation(Method m, Class annClass) {
        Annotation u = m.getAnnotation(annClass);
        if (u != null) {
            if (m.getParameterCount() != 0) {
                throw new NBootException(NBootMsg.ofC("method %s has annotation @%s. it should not have parameters", m, annClass.getName()));
            }
            if (!Modifier.isPublic(m.getModifiers())) {
                throw new NBootException(NBootMsg.ofC("method %s has annotation @%s. it should be public", m, annClass.getName()));
            }
            return true;
        }
        return false;
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
    public static <T extends NApplicationHandler> T createApplicationInstance(Class<T> appType, String[] args) {
        try {
            for (Method declaredMethod : appType.getDeclaredMethods()) {
                if (Modifier.isStatic(declaredMethod.getModifiers())) {
                    if (declaredMethod.getName().equals("createApplicationInstance")
                            && declaredMethod.getParameterCount() == 1
                            && declaredMethod.getParameterTypes()[0].equals(String[].class)
                    ) {
                        if (appType.isAssignableFrom(declaredMethod.getReturnType())) {
                            declaredMethod.setAccessible(true);
                            Object o = declaredMethod.invoke(null, (Object) args);
                            if (o != null) {
                                return appType.cast(o);
                            }
                        } else {
                            throw NException.ofSafeIllegalArgumentException(NMsg.ofC(NI18n.of("createApplicationInstance must return an instance of type %s"), appType.getName()));
                        }
                        break;
                    }
                }
            }
            Constructor<T> dconstructor = null;
            for (Constructor<?> constructor : appType.getConstructors()) {
                if (constructor.getParameterCount() == 1
                        && constructor.getParameterTypes()[0].equals(String[].class)) {
                    return (T) constructor.newInstance((Object) args);
                } else if (constructor.getParameterCount() == 0) {
                    dconstructor = (Constructor<T>) constructor;
                    return dconstructor.newInstance();
                }
            }
        } catch (InstantiationException ex) {
            Throwable c = ex.getCause();
            if (c instanceof RuntimeException) {
                throw (RuntimeException) c;
            }
            if (c instanceof Error) {
                throw (Error) c;
            }
            throw NException.ofSafeIllegalArgumentException(NMsg.ofC(NI18n.of("unable to instantiate application %s"), appType.getName()), ex);
        } catch (IllegalAccessException ex) {
            throw NException.ofSafeIllegalArgumentException(NMsg.ofC(NI18n.of("illegal access to default constructor for %s"), appType.getName()), ex);
        } catch (InvocationTargetException ex) {
            throw NException.ofSafeIllegalArgumentException(NMsg.ofC(NI18n.of("invocation exception for %s"), appType.getName()), ex);
        }
        throw NException.ofSafeIllegalArgumentException(NMsg.ofC(NI18n.of("missing application constructor for %s from of : \n\t static createApplicationInstance(NSession,String[])\n\t Constructor(NSession,String[])\n\t Constructor()"), appType.getName()));
    }

    static class AnnotationClassNApplicationHandler implements NApplicationHandler {
        private final List<Method> runMethods;
        private final List<Method> installMethods;
        private final List<Method> updateMethods;
        private final List<Method> uninstallMethods;
        private final List<Method> completeMethods;
        private final Object appInstance;

        public AnnotationClassNApplicationHandler(List<Method> runMethods, List<Method> installMethods, List<Method> updateMethods, List<Method> uninstallMethods, List<Method> completeMethods, Object appInstance) {
            this.runMethods = runMethods;
            this.installMethods = installMethods;
            this.updateMethods = updateMethods;
            this.uninstallMethods = uninstallMethods;
            this.completeMethods = completeMethods;
            this.appInstance = appInstance;
        }

        public Object appInstance() {
            return appInstance;
        }

        @Override
        public void run() {
            for (Method runMethod : runMethods) {
                doRunThis(runMethod);
            }
        }

        @Override
        public void onInstallApplication() {
            for (Method runMethod : installMethods) {
                doRunThis(runMethod);
            }
        }

        @Override
        public void onUpdateApplication() {
            for (Method runMethod : updateMethods) {
                doRunThis(runMethod);
            }
        }

        @Override
        public void onUninstallApplication() {
            for (Method runMethod : uninstallMethods) {
                doRunThis(runMethod);
            }
        }

        @Override
        public void onCompleteApplication() {
            for (Method runMethod : completeMethods) {
                doRunThis(runMethod);
            }
        }

        private void doRunThis(Method m) {
            try {
                if (Modifier.isStatic(m.getModifiers())) {
                    m.invoke(null);
                } else {
                    m.invoke(appInstance);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof RuntimeException) {
                    throw (RuntimeException) e.getTargetException();
                }
                if (e.getTargetException() != null) {
                    throw new RuntimeException(e.getTargetException());
                }
                throw new RuntimeException(e);
            }
        }
    }
}
