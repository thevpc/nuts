package net.thevpc.nuts.security;

import java.util.function.Consumer;
import java.util.function.Function;

public interface NSecureString extends AutoCloseable{

    static NSecureString ofEmpty() {
        return NSecurityManager.of().createEmptySecureString();
    }

    static NSecureString ofSecure(char[] value) {
        return NSecurityManager.of().createSecureString(value);
    }

    static NSecureString ofUnsecure(String value) {
        return NSecurityManager.of().createUnsecureString(value);
    }

    <R> R callWithContent(Function<char[], R> consumer);

    NSecureString doWithContent(Consumer<char[]> consumer);

    /**
     * Clears the sensitive data from memory if possible.
     * For String-backed versions, this is a no-op.
     */
    NSecureString destroy();

    /**
     * Returns true if the data has been wiped.
     */
    boolean isDestroyed();

    boolean constantTimeEquals(NSecureString other) ;

    @Override
    default void close(){
        destroy();
    }

    NSecureString copy();
}
