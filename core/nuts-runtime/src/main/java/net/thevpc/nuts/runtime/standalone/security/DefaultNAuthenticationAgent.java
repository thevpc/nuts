package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtils;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;

@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNAuthenticationAgent extends AbstractNAuthenticationAgent {

    public DefaultNAuthenticationAgent() {
        super("default#1", NConstants.Support.DEFAULT_SUPPORT);
    }

    @Override
    protected char[] decryptChars(char[] data, String passphrase) {
        return CoreSecurityUtils.defaultDecryptChars(data, passphrase);
    }

    @Override
    protected char[] encryptChars(char[] data, String passphrase) {
        return CoreSecurityUtils.defaultEncryptChars(data, passphrase);
    }

    @Override
    protected char[] hashChars(char[] data, String passphrase) {
        return CoreSecurityUtils.defaultHashChars(data, passphrase);
    }

}
