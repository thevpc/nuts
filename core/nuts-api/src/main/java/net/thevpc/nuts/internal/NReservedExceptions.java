package net.thevpc.nuts.internal;

import net.thevpc.nuts.cmdline.NCmdLineException;
import net.thevpc.nuts.cmdline.NDetachedCmdLineException;
import net.thevpc.nuts.concurrent.NInterruptedException;
import net.thevpc.nuts.boot.core.NExceptionWithExitCodeBase;
import net.thevpc.nuts.text.NI18n;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

/**
 * NReservedExceptions class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NReservedExceptions {
    public static NOptionalExceptionFactory defaultExceptionFactory;

    /**
     * Returns the default exception factory.
     *
     * @return get default exception factory result
     */
    public static NOptionalExceptionFactory getDefaultExceptionFactory() {
        return defaultExceptionFactory;
    }

    /**
     * Sets the default exception factory.
     *
     * @param defaultExceptionFactory default exception factory
     */
    public static void setDefaultExceptionFactory(NOptionalExceptionFactory defaultExceptionFactory) {
        NReservedExceptions.defaultExceptionFactory = defaultExceptionFactory;
    }

    /**
     * Creates a new instance of of safe illegal argument exception.
     *
     * @param e e
     * @return of safe illegal argument exception result
     */
    public static RuntimeException ofSafeIllegalArgumentException(NMsg e) {
        if (!NWorkspace.get().isPresent()) {
            return new IllegalArgumentException(e.toString());
        }
        return new NIllegalArgumentException(e);
    }

    /**
     * Creates a new instance of of safe io exception.
     *
     * @param e e
     * @return of safe io exception result
     */
    public static RuntimeException ofSafeIOException(Throwable e) {
        if (!NWorkspace.get().isPresent()) {
            if (e == null) {
                return new UncheckedIOException(new IOException());
            }
            if (e instanceof IOException) {
                return new UncheckedIOException((IOException) e);
            }
            return new UncheckedIOException(new IOException(NMsg.ofC("%s", getErrorMessage(e)).toString(), e));
        }
        if (e == null) {
            return new NIOException(NMsg.ofC("io error"));
        }
        return new NIOException(NMsg.ofC("%s", getErrorMessage(e)), e);
    }

    /**
     * Creates a new instance of of safe io exception.
     *
     * @param msg msg
     * @param e e
     * @return of safe io exception result
     */
    public static RuntimeException ofSafeIOException(NMsg msg, Throwable e) {
        if (!NWorkspace.get().isPresent()) {
            if (msg == null && e == null) {
                return new UncheckedIOException(new IOException());
            }
            if (msg == null) {
                msg = NMsg.ofC("%s", getErrorMessage(e));
            }
            if (e instanceof IOException) {
                return new UncheckedIOException(msg.toString(), (IOException) e);
            }
            return new UncheckedIOException(new IOException(msg.toString(), e));
        }
        if (msg == null && e == null) {
            return new NIOException(NMsg.ofC("io error"));
        }
        if (msg == null) {
            msg = NMsg.ofC("%s", getErrorMessage(e));
        }
        return new NIOException(msg, e);
    }

    /**
     * Creates a new instance of of safe io exception.
     *
     * @param e e
     * @return of safe io exception result
     */
    public static RuntimeException ofSafeIOException(NMsg e) {
        if (!NWorkspace.get().isPresent()) {
            return new UncheckedIOException(new IOException(e.toString()));
        }
        return new NIOException(e);
    }

    /**
     * Creates a new instance of of safe illegal argument exception.
     *
     * @param message message
     * @param ex ex
     * @return of safe illegal argument exception result
     */
    public static RuntimeException ofSafeIllegalArgumentException(NMsg message, Throwable ex) {
        if (!NWorkspace.get().isPresent()) {
            return new IllegalArgumentException(message.toString(), ex);
        }
        return new NIllegalArgumentException(message, ex);
    }

    /**
     * Creates a new instance of of safe assert exception.
     *
     * @param msg msg
     * @return of safe assert exception result
     */
    public static RuntimeException ofSafeAssertException(NMsg msg) {
        /**
         * Creates a new instance of of safe assert exception.
         *
         * @param msg msg
         * @param null null
         * @return of safe assert exception result
         */
        return ofSafeAssertException(msg, null);
    }

    /**
     * Creates a new instance of of safe assert exception.
     *
     * @param msg msg
     * @param ex ex
     * @return of safe assert exception result
     */
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

    /**
     * Creates a new instance of of safe cmd line exception.
     *
     * @param msg msg
     * @return of safe cmd line exception result
     */
    public static RuntimeException ofSafeCmdLineException(NMsg msg) {
        /**
         * Creates a new instance of of safe cmd line exception.
         *
         * @param msg msg
         * @param null null
         * @return of safe cmd line exception result
         */
        return ofSafeCmdLineException(msg, null);
    }

    /**
     * Creates a new instance of of safe cmd line exception.
     *
     * @param msg msg
     * @param ex ex
     * @return of safe cmd line exception result
     */
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

    /**
     * Creates a new instance of of safe no such element exception.
     *
     * @param message message
     * @return of safe no such element exception result
     */
    public static RuntimeException ofSafeNoSuchElementException(NMsg message) {
        if (!NWorkspace.get().isPresent()) {
            return new NoSuchElementException(message.toString());
        }
        return new NNoSuchElementException(message);
    }

    /**
     * Creates a new instance of of safe unexpected exception.
     *
     * @param message message
     * @return of safe unexpected exception result
     */
    public static RuntimeException ofSafeUnexpectedException(NMsg message) {
        if (!NWorkspace.get().isPresent()) {
            return new NoSuchElementException(message.toString());
        }
        return new NNoSuchElementException(message);
    }

    /**
     * Creates a new instance of of safe unsupported enum exception.
     *
     * @param e e
     * @return of safe unsupported enum exception result
     */
    public static RuntimeException ofSafeUnsupportedEnumException(Enum e) {
        if (!NWorkspace.get().isPresent()) {
            return new NoSuchElementException(NMsg.ofC(NI18n.of("unsupported enum value %s"), e).toString());
        }
        return new NUnsupportedEnumException(e);
    }

    /**
     * Resolve exception base.
     *
     * @param th th
     * @return resolve exception base result
     */
    public static NOptional<NExceptionBase> resolveExceptionBase(Throwable th) {
        return NReservedLangUtils.findThrowable(th, NExceptionBase.class, null);
    }

    /**
     * Resolve with exit code exception base.
     *
     * @param th th
     * @return resolve with exit code exception base result
     */
    public static NOptional<NExceptionWithExitCodeBase> resolveWithExitCodeExceptionBase(Throwable th) {
        return NReservedLangUtils.findThrowable(th, NExceptionWithExitCodeBase.class, null);
    }

    /**
     * Resolve exit code.
     *
     * @param th th
     * @return resolve exit code result
     */
    public static NOptional<Integer> resolveExitCode(Throwable th) {
        /**
         * Resolve with exit code exception base.
         *
         * @param th).map(NExceptionWithExitCodeBase::exitCode th).map(n exception with exit code base::exit code
         * @return resolve with exit code exception base result
         */
        return resolveWithExitCodeExceptionBase(th).map(NExceptionWithExitCodeBase::exitCode);
    }

    /**
     * Returns the error message.
     *
     * @param ex ex
     * @return get error message result
     */
    public static String getErrorMessage(Throwable ex) {
        /**
         * Returns the error message.
         *
         * @param ex ex
         * @param 128 128
         * @return get error message result
         */
        return getErrorMessage(ex, 128);
    }

    /**
     * Returns the error message.
     *
     * @param ex ex
     * @param maxDepth max depth
     * @return get error message result
     */
    private static String getErrorMessage(Throwable ex, int maxDepth) {
        if(ex==null){
            return null;
        }
        if (ex instanceof InvocationTargetException) {
            if (maxDepth > 0) {
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

    /**
     * Creates a new instance of of unchecked exception.
     *
     * @param e e
     * @return of unchecked exception result
     */
    public static RuntimeException ofUncheckedException(Throwable e) {
        if (e == null) {
            return new NullPointerException("null exception");
        }
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        if (e instanceof InvocationTargetException) {
            Throwable c = e.getCause();
            if (c != null) {
                /**
                 * Creates a new instance of of unchecked exception.
                 *
                 * @param c c
                 * @return of unchecked exception result
                 */
                return ofUncheckedException(c);
            }
        }
        if (e instanceof IOException) {
            if (!NWorkspace.get().isPresent()) {
                return new UncheckedIOException((IOException) e);
            }
            return new NIOException(e);
        }
        if (e instanceof InterruptedException) {
            if (!NWorkspace.get().isPresent()) {
                return new NInterruptedException(NMsg.ofC("%s", e.getMessage()), e);
            }
            return new UncheckedException(getErrorMessage(e), e);
        }
        return new UncheckedException(getErrorMessage(e), e);
    }
}
