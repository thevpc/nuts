package net.thevpc.nuts.security;

import java.util.function.Function;

public interface NSecretCaller<T> {
    T call(NCredentialId id, char[] secretm, Function<String, String> env);
}
