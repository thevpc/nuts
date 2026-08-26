package net.thevpc.nuts.security;

import java.util.function.Function;

/**
 * NSecretCaller interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NSecretCaller<T> {
    /**
     * Call.
     *
     * @param id id
     * @param secretm secretm
     * @param env env
     * @return call result
     */
    T call(NSecureToken id, NSecureString secretm, Function<String, String> env);
}
