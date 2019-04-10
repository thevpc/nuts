package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsAuthenticationAgent;
import net.vpc.app.nuts.NutsEnvProvider;
import net.vpc.app.nuts.NutsSecurityException;
import net.vpc.app.nuts.NutsSingleton;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreSecurityUtils;
import net.vpc.app.nuts.core.util.CoreStringUtils;

@NutsSingleton
public class DefaultNutsAuthenticationAgent implements NutsAuthenticationAgent {

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
    public void checkCredentials(String credentialsId, String password, NutsEnvProvider envProvider) {
        if (CoreStringUtils.isBlank(password)) {
            throw new NutsSecurityException("Missing old password");
        }
        //check old password
        if (CoreStringUtils.isBlank(credentialsId) || credentialsId.equals(CoreIOUtils.evalSHA1(password))) {
            throw new NutsSecurityException("Invalid password");
        }

        if (!CoreStringUtils.isBlank(credentialsId)) {
            if ((CoreStringUtils.isBlank(password) && CoreStringUtils.isBlank(credentialsId))
                    || (!CoreStringUtils.isBlank(password) && !CoreStringUtils.isBlank(credentialsId)
                    && credentialsId.equals(CoreIOUtils.evalSHA1(password)))) {
                return;
            }
        }
        throw new NutsSecurityException("Invalid login or password");
    }

    @Override
    public String getCredentials(String credentialsId, NutsEnvProvider envProvider) {
        //credentials are already encrypted with default passphrase!
        if (!CoreStringUtils.isBlank(credentialsId)) {
            credentialsId = new String(CoreSecurityUtils.httpDecrypt(credentialsId.getBytes(), CoreSecurityUtils.DEFAULT_PASSPHRASE));
        }
        return credentialsId;
    }

    @Override
    public String setCredentials(String credentials, NutsEnvProvider envProvider) {
        if (CoreStringUtils.isBlank(credentials)) {
            credentials = null;
        } else {
            credentials = CoreIOUtils.evalSHA1(credentials);
        }
        return credentials;
    }
}
