package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.*;

import java.util.Arrays;
import java.util.Map;

@NutsSingleton
public class PlainNutsAuthenticationAgent implements NutsAuthenticationAgent {

    private NutsWorkspace ws;

    @Override
    public String getId() {
        return "plain";
    }

    @Override
    public boolean removeCredentials(char[] credentialsId, Map<String, String> envProvider, NutsSession session) {
        extractId(credentialsId,session);
        return true;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> authenticationAgent) {
        return DEFAULT_SUPPORT - 1;
    }

    @Override
    public void checkCredentials(char[] credentialsId, char[] password, Map<String, String> envProvider, NutsSession session) {
        if (password==null || NutsUtilStrings.isBlank(new String(password))) {
            throw new NutsSecurityException(session, "missing old password");
        }
        char[] iid = extractId(credentialsId,session);
        if (Arrays.equals(iid, password)) {
            return;
        }
        throw new NutsSecurityException(session, "invalid login or password");
    }

    private char[] extractId(char[] a,NutsSession session) {
        if (!(a==null || NutsUtilStrings.isBlank(new String(a)))) {
            char[] idc = (getId()+":").toCharArray();
            if (a.length > idc.length + 1) {
                boolean ok = true;
                for (int i = 0; i < idc.length; i++) {
                    if (a[i] != idc[i]) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    return Arrays.copyOfRange(a, idc.length, a.length);
                }
            }
        }
        throw new NutsSecurityException(session, "credential id must start with " + getId() + ":");
    }

    @Override
    public char[] getCredentials(char[] credentialsId, Map<String, String> envProvider, NutsSession session) {
        return extractId(credentialsId,session);
    }

    @Override
    public char[] createCredentials(
            char[] credentials,
            boolean allowRetrieve,
            char[] credentialId,
            Map<String, String> envProvider,
            NutsSession session) {
        if (credentials==null || NutsUtilStrings.isBlank(new String(credentials))) {
            return null;
        } else {
            char[] val = credentials;
            String id = getId();
            char[] r = new char[id.length() + 1 + val.length];
            System.arraycopy(id.toCharArray(), 0, r, 0, id.length());
            r[id.length()] = ':';
            System.arraycopy(val, 0, r, id.length() + 1, val.length);
            return r;
        }
    }
}
