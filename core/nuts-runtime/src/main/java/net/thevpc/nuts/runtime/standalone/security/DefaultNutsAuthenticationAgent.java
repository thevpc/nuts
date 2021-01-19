package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.NutsSingleton;
import net.thevpc.nuts.runtime.bundles.io.CoreSecurityUtils;

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
