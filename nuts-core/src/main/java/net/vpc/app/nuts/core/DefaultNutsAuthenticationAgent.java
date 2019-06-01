package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.io.CoreSecurityUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

import java.util.Arrays;

@NutsSingleton
public class DefaultNutsAuthenticationAgent implements NutsAuthenticationAgent {

    private NutsWorkspace ws;
    private NutsEnvProvider envProvider;

    @Override
    public void setEnv(NutsEnvProvider envProvider) {
        this.envProvider = envProvider;
    }

    @Override
    public boolean removeCredentials(char[] credentialsId) {
        return false;
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
        //check old password
        if (CoreStringUtils.isBlank(credentialsId) || Arrays.equals(credentialsId, CoreIOUtils.evalSHA1(password))) {
            throw new NutsSecurityException(ws, "Invalid password");
        }

        if (!CoreStringUtils.isBlank(credentialsId)) {
            if ((CoreStringUtils.isBlank(password) && CoreStringUtils.isBlank(credentialsId))
                    || (!CoreStringUtils.isBlank(password) && !CoreStringUtils.isBlank(credentialsId)
                    && Arrays.equals(credentialsId, CoreIOUtils.evalSHA1(password)))) {
                return;
            }
        }
        throw new NutsSecurityException(ws, "Invalid login or password");
    }

    @Override
    public char[] getCredentials(char[] credentialsId) {
        //credentials are already encrypted with default passphrase!
        if (!CoreStringUtils.isBlank(credentialsId)) {
            credentialsId = CoreIOUtils.bytesToChars(
                    CoreSecurityUtils.httpDecrypt(CoreIOUtils.charsToBytes(credentialsId), CoreSecurityUtils.DEFAULT_PASSPHRASE)
            );
        }
        return credentialsId;
    }

    @Override
    public char[] setCredentials(char[] credentials, char[] credentialId) {
        if (CoreStringUtils.isBlank(credentials)) {
            credentials = null;
        } else {
            credentials = CoreIOUtils.evalSHA1(credentials);
        }
        return credentials;
    }

}
