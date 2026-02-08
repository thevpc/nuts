package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.security.NSecureString;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NIllegalStateException;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class NAbstractSecureString implements NSecureString {
    protected final char[] value;
    /** * Global lock used ONLY when two different objects have the same identityHashCode.
     */
    private static final Object TIE_BREAKER_LOCK = new Object();

    public NAbstractSecureString(char[] value) {
        NAssert.requireNamedNonNull(value, "value");
        this.value = value;
    }

    @Override
    public <R> R callWithContent(Function<char[], R> mapper) {
        NAssert.requireNamedNonNull(mapper, "mapper");
        synchronized (this) {
            if (isDestroyed()) {
                throw new NIllegalStateException(NMsg.ofC("secure string already destroyed"));
            }
            char[] chars = Arrays.copyOf(value, value.length);
            try {
                return mapper.apply(chars);
            } finally {
                Arrays.fill(chars, '\0');
            }
        }
    }

    @Override
    public NSecureString doWithContent(Consumer<char[]> consumer) {
        NAssert.requireNamedNonNull(consumer, "mapper");
        synchronized (this) {
            if (isDestroyed()) {
                throw new NIllegalStateException(NMsg.ofC("secure string already destroyed"));
            }
            char[] chars = Arrays.copyOf(value, value.length);
            try {
                consumer.accept(chars);
            } finally {
                Arrays.fill(chars, '\0');
            }
        }
        return this;
    }

    public void close() {
        destroy();
    }

    @Override
    public boolean constantTimeEquals(NSecureString other) {
        return constantTimeEquals(this, other, 1024);
    }

    static boolean constantTimeEquals(NSecureString a, NSecureString b, int floor) {
        if (a == null || b == null) {
            return false;
        }
        if (a == b) {
            return !a.isDestroyed();
        }
        if (a.isDestroyed()) {
            return false;
        }
        if (b.isDestroyed()) {
            return false;
        }
        // Determine lock order based on memory identity
        int h1 = System.identityHashCode(a);
        int h2 = System.identityHashCode(b);

        if (h1 == h2) {
            synchronized (TIE_BREAKER_LOCK) {
                return handleConstantTimeEqualsDefault(a,b,floor);
            }
        }
        final NSecureString first = h1 < h2 ? a : b;
        final NSecureString second = h1 < h2 ? b : a;
        return handleConstantTimeEqualsDefault(first,second,floor);
    }

    private static boolean handleConstantTimeEqualsDefault(NSecureString a, NSecureString b, int floor) {
        synchronized (a) {
            synchronized (b) {
                if (a.isDestroyed()) {
                    return false;
                }
                if (b.isDestroyed()) {
                    return false;
                }
                return a.callWithContent(achars -> b.callWithContent(bchars -> {
                    int maxLoop = Math.max(floor, Math.max(achars.length, bchars.length));
                    int diff = achars.length ^ bchars.length;
                    for (int i = 0; i < maxLoop; i++) {
                        char ca = (i < achars.length) ? achars[i] : '\0';
                        char cb = (i < bchars.length) ? bchars[i] : '\0';
                        diff |= ca ^ cb;
                    }
                    return diff==0;
                }));
            }
        }
    }
}
