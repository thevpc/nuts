package net.thevpc.nuts.security;

import java.util.function.Function;

public interface NSecretRunner {
    void run(NCredentialId id, char[] secretm, Function<String, String> env);
}
