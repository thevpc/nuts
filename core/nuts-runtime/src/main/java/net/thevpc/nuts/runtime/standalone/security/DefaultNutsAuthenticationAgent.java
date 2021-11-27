package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtils;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;

@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class DefaultNutsAuthenticationAgent extends AbstractNutsAuthenticationAgent {

    public DefaultNutsAuthenticationAgent() {
        super("default#1", DEFAULT_SUPPORT);
    }

    @Override
    protected char[] decryptChars(char[] data, String passphrase, NutsSession session) {
        return CoreSecurityUtils.defaultDecryptChars(data, passphrase,session);
    }

    @Override
    protected char[] encryptChars(char[] data, String passphrase, NutsSession session) {
        return CoreSecurityUtils.defaultEncryptChars(data, passphrase,session);
    }

    @Override
    protected char[] hashChars(char[] data, String passphrase, NutsSession session) {
        return CoreSecurityUtils.defaultHashChars(data, passphrase,session);
    }

}
