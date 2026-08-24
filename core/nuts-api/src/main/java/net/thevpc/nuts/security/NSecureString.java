package net.thevpc.nuts.security;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * NSecureString interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NSecureString extends AutoCloseable{

    /**
     * Creates a new instance of of empty.
     *
     * @return of empty result
     */
    static NSecureString ofEmpty() {
        return NSecurityManager.of().createEmptySecureString();
    }

    /**
     * Creates a new instance of of secure.
     *
     * @param value value
     * @return of secure result
     */
    static NSecureString ofSecure(char[] value) {
        return NSecurityManager.of().createSecureString(value);
    }

    /**
     * Creates a new instance of of unsecure.
     *
     * @param value value
     * @return of unsecure result
     */
    static NSecureString ofUnsecure(String value) {
        return NSecurityManager.of().createUnsecureString(value);
    }

    /**
     * Call with content.
     *
     * @param consumer consumer
     * @return call with content result
     */
    <R> R callWithContent(Function<char[], R> consumer);

    /**
     * Do with content.
     *
     * @param consumer consumer
     * @return do with content result
     */
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

    /**
     * Constant time equals.
     *
     * @param other other
     * @return constant time equals result
     */
    boolean constantTimeEquals(NSecureString other) ;

    @Override
    default void close(){
      /**
       * Destroy.
       */
        destroy();
    }

    /**
     * Copy.
     *
     * @return copy result
     */
    NSecureString copy();
}
