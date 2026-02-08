package net.thevpc.nuts.security;

import java.util.function.Function;

public interface NSecretCaller<T> {
    T call(NSecureToken id, NSecureString secretm, Function<String, String> env);
}
