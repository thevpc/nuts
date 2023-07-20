package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NSupported;
import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtils;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;

@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNAuthenticationAgent extends AbstractNAuthenticationAgent {

    public DefaultNAuthenticationAgent() {
        super("default#1", NSupported.DEFAULT_SUPPORT);
    }

    @Override
    protected char[] decryptChars(char[] data, String passphrase, NSession session) {
        return CoreSecurityUtils.defaultDecryptChars(data, passphrase,session);
    }

    @Override
    protected char[] encryptChars(char[] data, String passphrase, NSession session) {
        return CoreSecurityUtils.defaultEncryptChars(data, passphrase,session);
    }

    @Override
    protected char[] hashChars(char[] data, String passphrase, NSession session) {
        return CoreSecurityUtils.defaultHashChars(data, passphrase,session);
    }

}
