package net.thevpc.nuts;

import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

public class NExceptions {
    public static NOptional.ExceptionFactory defaultExceptionFactory;

    public static NOptional.ExceptionFactory getDefaultExceptionFactory() {
        return defaultExceptionFactory;
    }

    public static void setDefaultExceptionFactory(NOptional.ExceptionFactory defaultExceptionFactory) {
        NExceptions.defaultExceptionFactory = defaultExceptionFactory;
    }

    public static RuntimeException ofSafeIllegalArgumentException(NMsg e) {
        if (!NWorkspace.get().isPresent()) {
            return new IllegalArgumentException(e.toString());
        }
        return new NIllegalArgumentException(e);
    }

    public static RuntimeException ofSafeIllegalArgumentException(NMsg message, Throwable ex) {
        if (!NWorkspace.get().isPresent()) {
            return new IllegalArgumentException(message.toString(), ex);
        }
        return new NIllegalArgumentException(message, ex);
    }

    public static RuntimeException ofSafeAssertException(NMsg msg) {
        return ofSafeAssertException(msg, null);
    }

    public static RuntimeException ofSafeAssertException(NMsg msg, Throwable ex) {
        if (defaultExceptionFactory != null) {
            RuntimeException r = defaultExceptionFactory.createAssertException(msg, ex);
            if (r != null) {
                return r;
            }
        }
        if (!NWorkspace.get().isPresent()) {
            return new NDetachedAssertException(msg, ex);
        }
        return new NAssertException(msg, null);
    }

    public static RuntimeException ofSafeCmdLineException(NMsg msg) {
        return ofSafeAssertException(msg, null);
    }

    public static RuntimeException ofSafeCmdLineException(NMsg msg, Throwable ex) {
        if (defaultExceptionFactory != null) {
            RuntimeException r = defaultExceptionFactory.createCmdLineException(msg, ex);
            if (r != null) {
                return r;
            }
        }
        if (!NWorkspace.get().isPresent()) {
            return new NDetachedCmdLineException(msg, ex);
        }
        return new NCmdLineException(msg, null);
    }

    public static RuntimeException ofSafeNoSuchElementException(NMsg message) {
        if (!NWorkspace.get().isPresent()) {
            return new NoSuchElementException(message.toString());
        }
        return new NNoSuchElementException(message);
    }

    public static RuntimeException ofSafeUnexpectedException(NMsg message) {
        if (!NWorkspace.get().isPresent()) {
            return new NoSuchElementException(message.toString());
        }
        return new NNoSuchElementException(message);
    }

    public static RuntimeException ofSafeUnsupportedEnumException(Enum e) {
        if (!NWorkspace.get().isPresent()) {
            return new NoSuchElementException(NMsg.ofC(NI18n.of("unsupported enum value %s"), e).toString());
        }
        return new NUnsupportedEnumException(e);
    }

    public static NOptional<NExceptionBase> resolveExceptionBase(Throwable th) {
        return NReservedLangUtils.findThrowable(th, NExceptionBase.class, null);
    }

    public static NOptional<NExceptionWithExitCodeBase> resolveWithExitCodeExceptionBase(Throwable th) {
        return NReservedLangUtils.findThrowable(th, NExceptionWithExitCodeBase.class, null);
    }

    public static NOptional<Integer> resolveExitCode(Throwable th) {
        return resolveWithExitCodeExceptionBase(th).map(NExceptionWithExitCodeBase::getExitCode);
    }

    public static String getErrorMessage(Throwable ex) {
        return getErrorMessage(ex,128);
    }

    private static String getErrorMessage(Throwable ex,int maxDepth) {
        if (ex instanceof InvocationTargetException) {
            if(maxDepth>0) {
                String e = getErrorMessage(((InvocationTargetException) ex).getTargetException(), maxDepth - 1);
                if (e != null) {
                    return e;
                }
            }
        }
        String m = ex.getMessage();
        if (m == null || m.length() < 5) {
            m = ex.toString();
        }
        return m;
    }
}
