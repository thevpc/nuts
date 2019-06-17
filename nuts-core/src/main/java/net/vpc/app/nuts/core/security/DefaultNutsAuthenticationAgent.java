package net.vpc.app.nuts.core.security;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.io.CoreSecurityUtils;

@NutsSingleton
public class DefaultNutsAuthenticationAgent extends AbstractNutsAuthenticationAgent {

    public DefaultNutsAuthenticationAgent() {
        super("default#1", DEFAULT_SUPPORT);
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
