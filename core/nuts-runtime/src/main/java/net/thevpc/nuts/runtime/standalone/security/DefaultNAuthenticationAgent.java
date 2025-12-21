package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtils;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNAuthenticationAgent extends AbstractNAuthenticationAgent {

    public DefaultNAuthenticationAgent() {
        super("default#1");
    }

    @Override
    protected char[] decryptChars(char[] data, String passphrase) {
        return CoreSecurityUtils.INSTANCE.defaultDecryptChars(data, passphrase);
    }

    @Override
    protected char[] encryptChars(char[] data, String passphrase) {
        return CoreSecurityUtils.INSTANCE.defaultEncryptChars(data, passphrase);
    }

    @Override
    protected char[] hashChars(char[] data, String passphrase) {
        return CoreSecurityUtils.INSTANCE.defaultHashChars(data, passphrase);
    }

}
