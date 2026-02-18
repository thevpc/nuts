package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.security.NSecureString;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.util.Arrays;
import java.util.function.Function;

@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE -2)
public class PlainNAuthenticationAgent extends AbstractNAuthenticationAgent {

    public PlainNAuthenticationAgent() {
        super("plain", NVersion.of("1"));
    }

    @Override
    protected NSecureString oneWayChars(NSecureString data, Function<String, String> env) {
        return data.copy();
    }

    @Override
    protected NSecureString encryptChars(NSecureString data, Function<String, String> env) {
        return data.copy();
    }

    @Override
    protected NSecureString decryptChars(NSecureString data, Function<String, String> env) {
        return data.copy();
    }
}
