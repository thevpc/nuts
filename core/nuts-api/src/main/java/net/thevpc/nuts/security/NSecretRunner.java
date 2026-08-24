package net.thevpc.nuts.security;

import java.util.function.Function;

/**
 * NSecretRunner interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NSecretRunner {
    /**
     * Run.
     *
     * @param id id
     * @param secretm secretm
     * @param env env
     */
    void run(NSecureToken id, NSecureString secretm, Function<String, String> env);
}
