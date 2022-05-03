package net.thevpc.nuts.runtime.standalone.executor.embedded;

import net.thevpc.nuts.NutsBootException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CoreNutsApplications {
    /**
     * creates application instance by calling
     *
     * @param appType application type
     * @param session session
     * @param args    args
     * @param <T>     application type
     * @return new instance
     */
    public static <T> T createApplicationInstance(Class<T> appType, NutsSession session, String[] args) {
        try {
            for (Method declaredMethod : appType.getDeclaredMethods()) {
                if (Modifier.isStatic(declaredMethod.getModifiers())) {
                    if (declaredMethod.getName().equals("createApplicationInstance")
                            && declaredMethod.getParameterCount() == 2
                            && declaredMethod.getParameterTypes()[0].equals(NutsSession.class)
                            && declaredMethod.getParameterTypes()[1].equals(String[].class)
                    ) {
                        if (appType.isAssignableFrom(declaredMethod.getReturnType())) {
                            declaredMethod.setAccessible(true);
                            Object o = declaredMethod.invoke(null, session, args);
                            if (o != null) {
                                return appType.cast(o);
                            }
                        } else {
                            throw new NutsBootException(NutsMessage.ofCstyle("createApplicationInstance must return %s", appType.getName()));
                        }
                        break;
                    }
                }
            }
            Constructor<T> dconstructor = null;
            for (Constructor<?> constructor : appType.getConstructors()) {
                if (constructor.getParameterCount() == 2
                        && constructor.getParameterTypes()[0].equals(NutsSession.class)
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
            throw new NutsBootException(NutsMessage.ofCstyle("unable to instantiate %s", appType.getName()), ex);
        } catch (IllegalAccessException ex) {
            throw new NutsBootException(NutsMessage.ofCstyle("illegal access to default constructor for %s", appType.getName()), ex);
        } catch (InvocationTargetException ex) {
            throw new NutsBootException(NutsMessage.ofCstyle("invocation exception for %s", appType.getName()), ex);
        }
        throw new NutsBootException(NutsMessage.ofCstyle("missing application constructor one of : \n\t static createApplicationInstance(NutsSession,String[])\n\t Constructor(NutsSession,String[])\n\t Constructor()", appType.getName()));
    }

    public static String getNutsAppVersion(Class cls) {
        Method method = null;
        try {
            method = cls.getDeclaredMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            //
        }
        if (method != null) {
            int m = method.getModifiers();
            if (Modifier.isStatic(m) && Modifier.isPublic(m)) {
                return getNutsAppVersion0(cls);
            }
        }
        return null;
    }

    private static String getNutsAppVersion0(Class cls) {
        switch (cls.getName()) {
            case "net.vpc.app.nuts.NutsApplication": {
                return "0.8.0";
            }
            case "net.thevpc.nuts.NutsApplication": {
                if (!cls.isInterface()) {
                    return "0.8.1";
                }
                return "0.8.3";
            }
        }
        for (Class<?> p : cls.getInterfaces()) {
            String y = getNutsAppVersion0(p);
            if (y != null) {
                return y;
            }
        }
        Class p = cls.getSuperclass();
        if (p != null) {
            String y = getNutsAppVersion0(p);
            return y;
        }
        return null;
    }
}
