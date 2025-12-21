package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.ext.NFactoryException;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.text.NMsg;

import java.lang.reflect.Constructor;
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

        Constructor<?> best = null;
        int[] bestPositions = null;
        int bestScore = Integer.MAX_VALUE;

        for (Constructor<?> c : implType.getDeclaredConstructors()) {
            Class<?>[] params = c.getParameterTypes();

            int[] positions = matchAndValidate(params, userArgTypes, ctx);
            if (positions == null) {
                continue;
            }

            int score = params.length * 10 + (params.length - userArgTypes.length);
            if (score < bestScore) {
                best = c;
                bestPositions = positions;
                bestScore = score;
            }
        }

        if (best == null) {
            NMsg errorMessage = NMsg.ofC("error when instantiating %S as %s(%s). No constructor found. All %s available constructors are : %s",
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

        best.setAccessible(true);
        Constructor<?> ctor = best;
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
