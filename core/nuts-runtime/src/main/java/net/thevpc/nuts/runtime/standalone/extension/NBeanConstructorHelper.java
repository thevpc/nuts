package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.ext.NFactoryException;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.reflect.NFactoryMethod;
import net.thevpc.nuts.text.NMsg;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class NBeanConstructorHelper {

    public static <T> NBeanConstructor<T> createConstructor(
            TypeAndArgTypes tt,
            NBeanConstructorContext ctx,
            NLog LOG
    ) {
        Class<T> apiType = tt.getApiType();
        Class<? extends T> implType = tt.getImplType();
        Class<?>[] userArgTypes = tt.getArgTypes();

        Object best = null;
        int[] bestPositions = null;
        int bestScore = Integer.MAX_VALUE;

        for (Constructor<?> c : implType.getDeclaredConstructors()) {
            Class<?>[] params = c.getParameterTypes();

            int[] positions = matchAndValidate(params, userArgTypes, ctx);
            if (positions == null) {
                continue;
            }

            int score = 100 + params.length * 10 + (params.length - userArgTypes.length);
            if (score < bestScore) {
                best = c;
                bestPositions = positions;
                bestScore = score;
            }
        }
        // static methods are always preferred to instance constructors
        for (Method declaredMethod : implType.getDeclaredMethods()) {
            if (declaredMethod.getAnnotation(NFactoryMethod.class) != null) {
                if (Modifier.isStatic(declaredMethod.getModifiers())) {
                    Class<?> rt = declaredMethod.getReturnType();
                    if (tt.getApiType().equals(rt) || tt.getApiType().isAssignableFrom(rt)) {
                        Class<?>[] params = declaredMethod.getParameterTypes();
                        int[] positions = matchAndValidate(params, userArgTypes, ctx);
                        if (positions == null) {
                            continue;
                        }

                        int score = params.length * 10 + (params.length - userArgTypes.length);
                        if (score < bestScore) {
                            best = declaredMethod;
                            bestPositions = positions;
                            bestScore = score;
                        }
                    } else {
                        NMsg errorMessage = NMsg.ofC("invalid factory method %s::%s(%s). expected static %s as return type. ignored.",
                                implType.getName(),
                                declaredMethod.getName(),
                                Arrays.stream(declaredMethod.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(",")),
                                tt.getApiType()
                        ).asFinest().withIntent(NMsgIntent.FAIL);
                        if (NExtensionUtils.isBootstrapLogType(apiType)) {
                            //do not use log. this is a bug that must be resolved fast!
                            NExtensionUtils.safeLog(errorMessage, null);
                        } else {
                            LOG.log(errorMessage);
                        }
                    }
                } else {
                    NMsg errorMessage = NMsg.ofC("invalid factory method %s::%s(%s). expected static modifier. ignored.",
                            implType.getName(),
                            declaredMethod.getName(),
                            Arrays.stream(declaredMethod.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(","))
                    ).asFinest().withIntent(NMsgIntent.FAIL);
                    if (NExtensionUtils.isBootstrapLogType(apiType)) {
                        //do not use log. this is a bug that must be resolved fast!
                        NExtensionUtils.safeLog(errorMessage, null);
                    } else {
                        LOG.log(errorMessage);
                    }
                }
            }
        }


        if (best instanceof Constructor) {
            Constructor<?> ctor = (Constructor) best;
            ctor.setAccessible(true);
            int[] positions = bestPositions;
            Class<?>[] paramTypes = ctor.getParameterTypes();

            return (userArgs) -> {
                Object[] finalArgs = new Object[paramTypes.length];

                // user args
                for (int i = 0; i < userArgs.length; i++) {
                    finalArgs[positions[i]] = userArgs[i];
                }

                // context args (may resolve to null)
                for (int i = 0; i < paramTypes.length; i++) {
                    if (finalArgs[i] == null) {
                        finalArgs[i] = ctx.resolve(paramTypes[i]);
                    }
                }

                try {
                    @SuppressWarnings("unchecked")
                    T t = (T) ctor.newInstance(finalArgs);
                    return t;
                } catch (Exception ex) {
                    NMsg errorMessage = NMsg.ofC("error when instantiating %s as %s(%s) : %s", apiType, implType,
                            Arrays.stream(userArgTypes).map(Class::getSimpleName).collect(Collectors.joining(",")),
                            ex).asFinest().withIntent(NMsgIntent.FAIL);
                    if (NExtensionUtils.isBootstrapLogType(apiType)) {
                        //do not use log. this is a bug that must be resolved fast!
                        NExtensionUtils.safeLog(errorMessage, null);
                    } else {
                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.log(errorMessage);
                        }
                    }
                    throw new NFactoryException(errorMessage, ex);
                }
            };
        } else if (best instanceof Method) {
            Method ctor = (Method) best;
            ctor.setAccessible(true);
            int[] positions = bestPositions;
            Class<?>[] paramTypes = ctor.getParameterTypes();

            return (userArgs) -> {
                Object[] finalArgs = new Object[paramTypes.length];

                // user args
                for (int i = 0; i < userArgs.length; i++) {
                    finalArgs[positions[i]] = userArgs[i];
                }

                // context args (may resolve to null)
                for (int i = 0; i < paramTypes.length; i++) {
                    if (finalArgs[i] == null) {
                        finalArgs[i] = ctx.resolve(paramTypes[i]);
                    }
                }

                try {
                    @SuppressWarnings("unchecked")
                    T t = (T) ctor.invoke(null, finalArgs);
                    return t;
                } catch (Exception ex) {
                    NMsg errorMessage = NMsg.ofC("error when instantiating %s as %s(%s) : %s", apiType, implType,
                            Arrays.stream(userArgTypes).map(Class::getSimpleName).collect(Collectors.joining(",")),
                            ex).asFinest().withIntent(NMsgIntent.FAIL);
                    if (NExtensionUtils.isBootstrapLogType(apiType)) {
                        //do not use log. this is a bug that must be resolved fast!
                        NExtensionUtils.safeLog(errorMessage, null);
                    } else {
                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.log(errorMessage);
                        }
                    }
                    throw new NFactoryException(errorMessage, ex);
                }
            };
        }


        NMsg errorMessage = NMsg.ofC("error when instantiating %s as %s(%s). No constructor found. All %s available constructors are : %s",
                apiType.getName(),
                implType.getName(),
                Arrays.stream(userArgTypes).map(Class::getSimpleName).collect(Collectors.joining(",")),
                implType.getDeclaredConstructors().length,
                Arrays.stream(implType.getDeclaredConstructors()).map(x -> String.valueOf(x)).collect(Collectors.joining(" ; "))
        ).asFinest().withIntent(NMsgIntent.FAIL);
        if (NExtensionUtils.isBootstrapLogType(apiType)) {
            //do not use log. this is a bug that must be resolved fast!
            NExtensionUtils.safeLog(errorMessage, null);
        } else {
            LOG.log(errorMessage);
        }
        throw new NFactoryException(errorMessage);
    }


    private static int[] matchAndValidate(
            Class<?>[] ctorParams,
            Class<?>[] userArgTypes,
            NBeanConstructorContext ctx
    ) {
        int[] positions = new int[userArgTypes.length];
        boolean[] used = new boolean[ctorParams.length];

        int i = 0, j = 0;

        while (i < ctorParams.length && j < userArgTypes.length) {
            if (ctorParams[i].isAssignableFrom(userArgTypes[j])) {
                positions[j] = i;
                used[i] = true;
                j++;
            }
            i++;
        }

        if (j != userArgTypes.length) {
            return null; // user args not embeddable
        }

        // validate remaining params via isSupported
        for (int k = 0; k < ctorParams.length; k++) {
            if (!used[k] && !ctx.isSupported(ctorParams[k])) {
                return null;
            }
        }

        return positions;
    }
}
