package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.*;

import java.util.Arrays;
import java.util.Map;

import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtils;
import net.thevpc.nuts.security.NAuthenticationAgent;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

public abstract class AbstractNAuthenticationAgent implements NAuthenticationAgent {

    private final String name;
    private int supportLevel;

    public AbstractNAuthenticationAgent(String name, int supportLevel) {
        this.name = name;
        this.supportLevel = supportLevel;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public boolean removeCredentials(char[] credentialsId, Map<String, String> envProvider) {
        extractId(credentialsId);
        return true;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext authenticationAgent) {
        return supportLevel;
    }

    @Override
    public void checkCredentials(char[] credentialsId, char[] password, Map<String, String> envProvider) {
        if (password==null || NBlankable.isBlank(new String(password))) {
            throw new NSecurityException(NMsg.ofPlain("missing old password"));
        }
        CredentialsId iid = extractId(credentialsId);
        switch (iid.type) {
            case 'H': {
                if (Arrays.equals(iid.value, hashChars(password, getPassphrase(envProvider)))) {
                    return;
                }
                break;
            }
            case 'B': {
                char[] encPwd = encryptChars(password, getPassphrase(envProvider));
                if (Arrays.equals(iid.value, encPwd)) {
                    return;
                }
            }
        }
        throw new NSecurityException(NMsg.ofPlain("invalid login or password"));
    }

    private static class CredentialsId {

        char type;
        char[] value;

        public CredentialsId(char type, char[] value) {
            this.type = type;
            this.value = value;
        }

    }

    private CredentialsId extractId(char[] a) {
        if (!(a==null || NBlankable.isBlank(new String(a)))) {
            char[] idc = (getId() + ":").toCharArray();
            if (a.length > idc.length + 1) {
                boolean ok = true;
                for (int i = 0; i < idc.length; i++) {
                    if (a[i] != idc[i]) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    if (a[idc.length] == 'H' || a[idc.length] == 'B') {
                        return new CredentialsId(a[idc.length], Arrays.copyOfRange(a, idc.length + 1, a.length));
                    }
                }
            }
        }
        throw new NSecurityException(NMsg.ofC("credential id must start with '%s:'",getId()));
    }

    @Override
    public char[] getCredentials(char[] credentialsId, Map<String, String> envProvider) {
        //credentials are already encrypted with default passphrase!
        CredentialsId validCredentialsId = extractId(credentialsId);
        if (validCredentialsId.type == 'B') {
            return decryptChars(validCredentialsId.value, getPassphrase(envProvider));
        }
        throw new NSecurityException(NMsg.ofPlain("credential is hashed and cannot be retrived"));
    }

    @Override
    public char[] createCredentials(
            char[] credentials,
            boolean allowRetrieve,
            char[] credentialId,
            Map<String, String> envProvider) {
        if (credentials==null || NBlankable.isBlank(new String(credentials))) {
            return null;
        } else {
            char[] val;
            char type;
            if (allowRetrieve) {
                val = encryptChars(credentials, getPassphrase(envProvider));
                type = 'B';
            } else {
                val = hashChars(credentials, getPassphrase(envProvider));
                type = 'H';
            }
            String id = getId();
            char[] r = new char[id.length() + 2 + val.length];
            System.arraycopy(id.toCharArray(), 0, r, 0, id.length());
            r[id.length()] = ':';
            r[id.length() + 1] = type;
            System.arraycopy(val, 0, r, id.length() + 2, val.length);
            return r;
        }
    }

    public String getPassphrase(Map<String,String> envProvider) {
        String defVal = CoreSecurityUtils.DEFAULT_PASSPHRASE;
        if (envProvider != null) {
            String r = envProvider.get("nuts.authentication-agent.simple.passphrase");
            if (r == null) {
                r=defVal;
            }
            if (r == null || r.isEmpty()) {
                r = defVal;
            }
            return r;
        }
        return defVal;
    }

    protected abstract char[] decryptChars(char[] data, String passphrase);

    protected abstract char[] encryptChars(char[] data, String passphrase);

    protected abstract char[] hashChars(char[] data, String passphrase);

}
