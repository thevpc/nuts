package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;

import java.util.Arrays;
import java.util.Map;

@NutsSingleton
public class PlainNutsAuthenticationAgent implements NutsAuthenticationAgent, NutsSessionAware {

    private NutsWorkspace ws;
    private NutsSession session;

    @Override
    public String getId() {
        return "plain";
    }

    @Override
    public void setSession(NutsSession session) {
        this.session=session;
        this.ws=session==null?null:session.getWorkspace();
    }

    @Override
    public boolean removeCredentials(char[] credentialsId, Map<String, String> envProvider, NutsSession session) {
        extractId(credentialsId);
        return true;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> authenticationAgent) {
        return DEFAULT_SUPPORT - 1;
    }

    @Override
    public void checkCredentials(char[] credentialsId, char[] password, Map<String, String> envProvider, NutsSession session) {
        if (CoreStringUtils.isBlank(password)) {
            throw new NutsSecurityException(ws, "missing old password");
        }
        char[] iid = extractId(credentialsId);
        if (Arrays.equals(iid, password)) {
            return;
        }
        throw new NutsSecurityException(ws, "invalid login or password");
    }

    private char[] extractId(char[] a) {
        if (!CoreStringUtils.isBlank(a)) {
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
        throw new NutsSecurityException(ws, "credential id must start with " + getId() + ":");
    }

    @Override
    public char[] getCredentials(char[] credentialsId, Map<String, String> envProvider, NutsSession session) {
        return extractId(credentialsId);
    }

    @Override
    public char[] createCredentials(
            char[] credentials,
            boolean allowRetrieve,
            char[] credentialId,
            Map<String, String> envProvider,
            NutsSession session) {
        if (CoreStringUtils.isBlank(credentials)) {
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
