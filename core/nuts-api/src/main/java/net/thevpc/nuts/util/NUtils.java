package net.thevpc.nuts.util;

import net.thevpc.nuts.NExceptionBase;
import net.thevpc.nuts.NExceptionWithExitCodeBase;
import net.thevpc.nuts.reserved.NReservedLangUtils;

import java.util.Arrays;
import java.util.List;

public class NUtils {
    static NOptional<NExceptionBase> resolveExceptionBase(Throwable th) {
        return NReservedLangUtils.findThrowable(th, NExceptionBase.class, null);
    }
    static NOptional<NExceptionWithExitCodeBase> resolveWithExitCodeExceptionBase(Throwable th) {
        return NReservedLangUtils.findThrowable(th, NExceptionWithExitCodeBase.class, null);
    }

    public static NOptional<Integer> resolveExitCode(Throwable th) {
        return resolveWithExitCodeExceptionBase(th).map(NExceptionWithExitCodeBase::getExitCode);
    }

    public static <T> T firstNonNull(T a, T b) {
        if (a != null) {
            return a;
        }
        if (b != null) {
            return b;
        }
        return null;
    }

    public static <T> T firstNonNull(T... values) {
        return firstNonNull(values == null ? null : Arrays.asList(values));
    }

    public static <T> T firstNonNull(List<T> values) {
        if (values != null) {
            for (T value : values) {
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

}
