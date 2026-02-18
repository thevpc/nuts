package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtilsV2;
import net.thevpc.nuts.security.NSecureString;
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
    protected NSecureString decryptChars(NSecureString data, Function<String, String> env) {
        return data.callWithContent(cc -> {
            return NSecureString.ofSecure(CoreSecurityUtilsV2.INSTANCE.defaultDecryptChars(cc,getPassphrase(env)));
        });
    }

    @Override
    protected NSecureString encryptChars(NSecureString data, Function<String, String> env) {
        return data.callWithContent(cc -> {
            return NSecureString.ofSecure(CoreSecurityUtilsV2.INSTANCE.defaultEncryptChars(cc,getPassphrase(env)));
        });
    }

    @Override
    protected NSecureString oneWayChars(NSecureString data, Function<String, String> env) {
        return data.callWithContent(cc -> {
            return NSecureString.ofSecure(CoreSecurityUtilsV2.INSTANCE.defaultHashChars(cc));
        });
    }

    protected boolean verifyOneWayImpl(NSecureString candidate, NSecureString storedHash, Function<String, String> env) {
        return candidate.callWithContent(cc -> {
            return storedHash.callWithContent(hh -> {
                return CoreSecurityUtilsV2.INSTANCE.verifyOneWay(cc, hh);

            });
        });
    }

}
