package net.thevpc.nuts.security;

import java.util.function.Function;

public interface NSecretRunner {
    void run(NSecureToken id, NSecureString secretm, Function<String, String> env);
}
