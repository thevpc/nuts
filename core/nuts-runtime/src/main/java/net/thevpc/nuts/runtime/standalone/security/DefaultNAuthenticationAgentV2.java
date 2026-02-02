package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtilsV2;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScore;

import java.util.function.Function;

@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNAuthenticationAgentV2 extends AbstractNAuthenticationAgent {

    public DefaultNAuthenticationAgentV2() {
        super("default", NVersion.of("2"));
    }

    @Override
    protected char[] decryptChars(char[] data, Function<String, String> env) {
        return CoreSecurityUtilsV2.INSTANCE.defaultDecryptChars(data, getPassphrase(env));
    }

    @Override
    protected char[] encryptChars(char[] data, Function<String, String> env) {
        return CoreSecurityUtilsV2.INSTANCE.defaultEncryptChars(data, getPassphrase(env));
    }

    @Override
    protected char[] oneWayChars(char[] data, Function<String, String> env) {
        return CoreSecurityUtilsV2.INSTANCE.defaultHashChars(data);
    }

    protected boolean verifyOneWayImpl(char[] candidate, char[] storedHash, Function<String, String> env) {
        return CoreSecurityUtilsV2.INSTANCE.verifyOneWay(candidate, storedHash);
    }

}
