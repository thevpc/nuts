package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.*;

import java.util.Arrays;
import java.util.Map;

@NComponentScope(NComponentScopeType.WORKSPACE)
public class PlainNAuthenticationAgent implements NAuthenticationAgent {

    private NWorkspace ws;

    @Override
    public String getId() {
        return "plain";
    }

    @Override
    public void checkCredentials(char[] credentialsId, char[] password, Map<String, String> envProvider, NSession session) {
        if (password == null || NBlankable.isBlank(new String(password))) {
            throw new NSecurityException(session, NMsg.ofPlain("missing old password"));
        }
        char[] iid = extractId(credentialsId, session);
        if (Arrays.equals(iid, password)) {
            return;
        }
        throw new NSecurityException(session, NMsg.ofPlain("invalid login or password"));
    }

    @Override
    public char[] getCredentials(char[] credentialsId, Map<String, String> envProvider, NSession session) {
        return extractId(credentialsId, session);
    }

    @Override
    public boolean removeCredentials(char[] credentialsId, Map<String, String> envProvider, NSession session) {
        extractId(credentialsId, session);
        return true;
    }

    @Override
    public char[] createCredentials(
            char[] credentials,
            boolean allowRetrieve,
            char[] credentialId,
            Map<String, String> envProvider,
            NSession session) {
        if (credentials == null || NBlankable.isBlank(new String(credentials))) {
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

    @Override
    public int getSupportLevel(NSupportLevelContext authenticationAgent) {
        return DEFAULT_SUPPORT - 1;
    }

    private char[] extractId(char[] a, NSession session) {
        if (!(a == null || NBlankable.isBlank(new String(a)))) {
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
                    return Arrays.copyOfRange(a, idc.length, a.length);
                }
            }
        }
        throw new NSecurityException(session, NMsg.ofC("credential id must start with '%s:'", getId()));
    }
}
