package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtilsV1;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.util.function.Function;

@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE -1)
public class DefaultNAuthenticationAgentV1 extends AbstractNAuthenticationAgent {

    public DefaultNAuthenticationAgentV1() {
        super("default", NVersion.of("1"));
    }

    @Override
    protected char[] decryptChars(char[] data, Function<String, String> env) {
        return CoreSecurityUtilsV1.INSTANCE.defaultDecryptChars(data, getPassphrase(env));
    }

    @Override
    protected char[] encryptChars(char[] data, Function<String, String> env) {
        return CoreSecurityUtilsV1.INSTANCE.defaultEncryptChars(data, getPassphrase(env));
    }

    @Override
    protected char[] oneWayChars(char[] data, Function<String, String> env) {
        return CoreSecurityUtilsV1.INSTANCE.defaultHashChars(data, getPassphrase(env));
    }

}
