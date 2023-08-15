package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.*;

import java.util.Arrays;
import java.util.Map;

import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtils;
import net.thevpc.nuts.spi.NAuthenticationAgent;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NMsg;

public abstract class AbstractNAuthenticationAgent implements NAuthenticationAgent {

    private final String name;
    private NWorkspace ws;
    private int supportLevel;

    public AbstractNAuthenticationAgent(String name, int supportLevel) {
        this.name = name;
        this.supportLevel = supportLevel;
    }

//    @Override
//    public void setSession(NutsSession session) {
//        this.session=session;
//        this.ws=session==null?null:session.getWorkspace();
//    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public boolean removeCredentials(char[] credentialsId, Map<String, String> envProvider, NSession session) {
        extractId(credentialsId,session);
        return true;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext authenticationAgent) {
        return supportLevel;
    }

    @Override
    public void checkCredentials(char[] credentialsId, char[] password, Map<String, String> envProvider, NSession session) {
        if (password==null || NBlankable.isBlank(new String(password))) {
            throw new NSecurityException(session, NMsg.ofPlain("missing old password"));
        }
        CredentialsId iid = extractId(credentialsId,session);
        switch (iid.type) {
            case 'H': {
                if (Arrays.equals(iid.value, hashChars(password, getPassphrase(envProvider), session))) {
                    return;
                }
                break;
            }
            case 'B': {
                char[] encPwd = encryptChars(password, getPassphrase(envProvider), session);
                if (Arrays.equals(iid.value, encPwd)) {
                    return;
                }
            }
        }
        throw new NSecurityException(session, NMsg.ofPlain("invalid login or password"));
    }

    private static class CredentialsId {

        char type;
        char[] value;

        public CredentialsId(char type, char[] value) {
            this.type = type;
            this.value = value;
        }

    }

    private CredentialsId extractId(char[] a, NSession session) {
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
        throw new NSecurityException(session, NMsg.ofC("credential id must start with '%s:'",getId()));
    }

    @Override
    public char[] getCredentials(char[] credentialsId, Map<String, String> envProvider, NSession session) {
        //credentials are already encrypted with default passphrase!
        CredentialsId validCredentialsId = extractId(credentialsId,session);
        if (validCredentialsId.type == 'B') {
            return decryptChars(validCredentialsId.value, getPassphrase(envProvider), session);
        }
        throw new NSecurityException(session, NMsg.ofPlain("credential is hashed and cannot be retrived"));
    }

    @Override
    public char[] createCredentials(
            char[] credentials,
            boolean allowRetrieve,
            char[] credentialId,
            Map<String, String> envProvider,
            NSession session) {
        if (credentials==null || NBlankable.isBlank(new String(credentials))) {
            return null;
        } else {
            char[] val;
            char type;
            if (allowRetrieve) {
                val = encryptChars(credentials, getPassphrase(envProvider), session);
                type = 'B';
            } else {
                val = hashChars(credentials, getPassphrase(envProvider), session);
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

    protected abstract char[] decryptChars(char[] data, String passphrase, NSession session);

    protected abstract char[] encryptChars(char[] data, String passphrase, NSession session);

    protected abstract char[] hashChars(char[] data, String passphrase, NSession session);

}
