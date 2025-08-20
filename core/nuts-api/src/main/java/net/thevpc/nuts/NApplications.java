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
package net.thevpc.nuts;


import net.thevpc.nuts.boot.NBootException;
import net.thevpc.nuts.boot.reserved.util.NBootMsg;
import net.thevpc.nuts.core.NI18n;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;

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

    public static NAppDefinition resolveApplicationAnnotation(Class appClass) {
        Class<?> validAppClass = unproxyType(appClass);
        return validAppClass.getAnnotation(NAppDefinition.class);
    }

    public static boolean isAnnotatedApplicationClass(Class appClass) {
        return resolveApplicationAnnotation(appClass) != null;
    }

    public static NApplication createApplicationInstanceFromAnnotatedInstance(Object appInstance) {
        NAssert.requireNonNull(appInstance, "appInstance");
        if (appInstance instanceof NApplication) {
            return (NApplication) appInstance;
        }
        Class<?> appClass = unproxyType(appInstance.getClass());
        NAppDefinition appAnnotation = appClass.getAnnotation(NAppDefinition.class);
        if (appAnnotation == null) {
            throw new NBootException(NBootMsg.ofC("class %s is missing annotation @"+NAppDefinition.class.getSimpleName(), appClass.getName()));
        }
        NAssert.requireNonNull(appAnnotation, "@NAppDefinition annotation");
        List<Method> runMethods = new ArrayList<>();
        List<Method> installMethods = new ArrayList<>();
        List<Method> uninstallMethods = new ArrayList<>();
        List<Method> updateMethods = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Class cc = appClass;
        while (cc != null) {
            for (Method m : cc.getMethods()) {
                // only public methods
                if (m.getParameterCount() == 0) {
                    if (visited.add(m.getName())) {
                        if (m.getAnnotation(NAppRunner.class) != null) {
                            runMethods.add(m);
                        }
                        if (m.getAnnotation(NAppUpdater.class) != null) {
                            updateMethods.add(m);
                        }
                        if (m.getAnnotation(NAppInstaller.class) != null) {
                            installMethods.add(m);
                        }
                        if (m.getAnnotation(NAppUninstaller.class) != null) {
                            uninstallMethods.add(m);
                        }
                    }
                }
            }
            try {
                for (Method m : cc.getDeclaredMethods()) {
                    checkAllowedMethodWithNutsAnnotation(m, NAppRunner.class);
                    checkAllowedMethodWithNutsAnnotation(m, NAppInstaller.class);
                    checkAllowedMethodWithNutsAnnotation(m, NAppUninstaller.class);
                    checkAllowedMethodWithNutsAnnotation(m, NAppUpdater.class);
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
//            throw new NBootException(NBootMsg.ofC("class %s has annotation @NAppDefinition. it should define a public no arg @NAppRunner method", appClass.getName()));
//        }
        return new AnnotationClassNApplication(runMethods, installMethods, updateMethods, uninstallMethods, appInstance);
    }

    private static boolean checkAllowedMethodWithNutsAnnotation(Method m, Class annClass) {
        Annotation u = m.getAnnotation(annClass);
        if (u != null) {
            if (m.getParameterCount() != 0) {
                throw new NBootException(NBootMsg.ofC("method %s has annotation @%s. it should not have parameters", m, annClass.getName()));
            }
            if (!Modifier.isPublic(m.getModifiers())) {
                throw new NBootException(NBootMsg.ofC("method %s has annotation @%s. it should be public"));
            }
            return true;
        }
        return false;
    }

    private static boolean isProxyType(Class<?> aClass) {
        if (aClass == null) {
            return false;
        }
        String simpleName = aClass.getSimpleName();
        if (simpleName.contains("$$EnhancerBySpringCGLIB$$")
                || simpleName.contains("$$CGLIB$$")
                || simpleName.contains("$$SpringCGLIB$$")
        ) {
            return true;
        }
        return false;
    }

    public static Class<?> unproxyType(Class<?> aClass) {
        if (aClass == null) {
            return null;
        }
        if (isProxyType(aClass)) {
            Class<?> u = unproxyType(aClass.getSuperclass());
            if (u != null) {
                return u;
            }
        }
        return aClass;
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
                            Object o = declaredMethod.invoke(null, (Object) args);
                            if (o != null) {
                                return appType.cast(o);
                            }
                        } else {
                            throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC(NI18n.of("createApplicationInstance must return an instance of type %s"), appType.getName()));
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
            throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC(NI18n.of("unable to instantiate application %s"), appType.getName()), ex);
        } catch (IllegalAccessException ex) {
            throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC(NI18n.of("illegal access to default constructor for %s"), appType.getName()), ex);
        } catch (InvocationTargetException ex) {
            throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC(NI18n.of("invocation exception for %s"), appType.getName()), ex);
        }
        throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC(NI18n.of("missing application constructor for %s from of : \n\t static createApplicationInstance(NSession,String[])\n\t Constructor(NSession,String[])\n\t Constructor()"), appType.getName()));
    }

    public static NAppBuilder builder() {
        return new NAppBuilder();
    }

    static class AnnotationClassNApplication implements NApplication {
        private final List<Method> runMethods;
        private final List<Method> installMethods;
        private final List<Method> updateMethods;
        private final List<Method> uninstallMethods;
        private final Object appInstance;

        public AnnotationClassNApplication(List<Method> runMethods, List<Method> installMethods, List<Method> updateMethods, List<Method> uninstallMethods, Object appInstance) {
            this.runMethods = runMethods;
            this.installMethods = installMethods;
            this.updateMethods = updateMethods;
            this.uninstallMethods = uninstallMethods;
            this.appInstance = appInstance;
        }

        public Object getAppInstance() {
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
