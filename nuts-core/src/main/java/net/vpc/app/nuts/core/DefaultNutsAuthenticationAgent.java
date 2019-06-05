package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.spi.NutsAuthenticationAgentSpi;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.io.CoreSecurityUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

import java.util.Arrays;

@NutsSingleton
public class DefaultNutsAuthenticationAgent implements NutsAuthenticationAgent, NutsAuthenticationAgentSpi {

    private NutsWorkspace ws;
    private NutsEnvProvider envProvider;

    @Override
    public String getId() {
        return "plain";
    }

    @Override
    public void setEnv(NutsEnvProvider envProvider) {
        this.envProvider = envProvider;
    }

    @Override
    public void setWorkspace(NutsWorkspace workspace) {
        this.ws = workspace;
    }

    @Override
    public boolean removeCredentials(char[] credentialsId) {
        extractId(credentialsId);
        return true;
    }

    @Override
    public int getSupportLevel(String authenticationAgent) {
        if (authenticationAgent == null || authenticationAgent.trim().isEmpty()
                || authenticationAgent.trim().equals("password")
                || authenticationAgent.trim().equals("passphrase")) {
            return DEFAULT_SUPPORT;
        }
        return NO_SUPPORT;
    }

    @Override
    public void checkCredentials(char[] credentialsId, char[] password) {
        if (CoreStringUtils.isBlank(password)) {
            throw new NutsSecurityException(ws, "Missing old password");
        }
        CredentialsId iid = extractId(credentialsId);
        switch (iid.type) {
            case 'H': {
                if (Arrays.equals(iid.value, CoreIOUtils.evalSHA1(password))) {
                    return;
                }
                break;
            }
            case 'B': {
                char[] encPwd = CoreSecurityUtils.httpEncryptChars(password, getPassphrase());
                if (Arrays.equals(iid.value, encPwd)) {
                    return;
                }
            }
        }
        throw new NutsSecurityException(ws, "Invalid login or password");
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
        if (!CoreStringUtils.isBlank(a)) {
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
        throw new NutsSecurityException(ws, "credential id must start with " + getId() + ":");
    }

    @Override
    public char[] getCredentials(char[] credentialsId
    ) {
        //credentials are already encrypted with default passphrase!
        CredentialsId validCredentialsId = extractId(credentialsId);
        if (validCredentialsId.type == 'B') {
            return CoreSecurityUtils.httpDecryptChars(validCredentialsId.value, getPassphrase());
        }
        throw new NutsSecurityException(ws, "credential is hashed and cannot be retrived");
    }

    @Override
    public char[] setCredentials(char[] credentials, boolean allowRetreive, char[] credentialId
    ) {
        if (CoreStringUtils.isBlank(credentials)) {
            return null;
        } else {
            char[] val;
            char type;
            if (allowRetreive) {
                val = CoreSecurityUtils.httpEncryptChars(credentials, getPassphrase());
                type = 'B';
            } else {
                val = CoreIOUtils.evalSHA1(credentials);
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

    public String getPassphrase() {
        String defVal = CoreSecurityUtils.DEFAULT_PASSPHRASE;
        if (envProvider != null) {
            String r = envProvider.getEnv("nuts.authrntication-agent.simple.passphrase", defVal);
            if (r == null || r.isEmpty()) {
                r = defVal;
            }
            return r;
        }
        return defVal;
    }

}
