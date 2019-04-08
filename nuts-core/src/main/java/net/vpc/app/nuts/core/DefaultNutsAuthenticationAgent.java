package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsAuthenticationAgent;
import net.vpc.app.nuts.NutsEnvProvider;
import net.vpc.app.nuts.NutsSecurityException;
import net.vpc.app.nuts.NutsSingleton;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.CoreSecurityUtils;
import net.vpc.app.nuts.core.util.CoreStringUtils;

@NutsSingleton
public class DefaultNutsAuthenticationAgent implements NutsAuthenticationAgent {
    @Override
    public int getSupportLevel(String authenticationAgent) {
        if(authenticationAgent==null|| authenticationAgent.trim().isEmpty() || authenticationAgent.trim().equals("password")) {
            return DEFAULT_SUPPORT;
        }
        return NO_SUPPORT;
    }

    public void checkCredentials(String credentialsId, String authenticationAgent,String password, NutsEnvProvider envProvider) {
        if (CoreStringUtils.isBlank(password)) {
            throw new NutsSecurityException("Missing old password");
        }
        //check old password
        if (CoreStringUtils.isBlank(credentialsId) || credentialsId.equals(CoreSecurityUtils.evalSHA1(password))) {
            throw new NutsSecurityException("Invalid password");
        }


        if(!CoreStringUtils.isBlank(credentialsId)){
            if ((CoreStringUtils.isBlank(password) && CoreStringUtils.isBlank(credentialsId))
                    || (!CoreStringUtils.isBlank(password) && !CoreStringUtils.isBlank(credentialsId)
                    && credentialsId.equals(CoreSecurityUtils.evalSHA1(password)))) {
                return;
            }
        }
        throw new NutsSecurityException("Invalid login or password");
    }

    @Override
    public String getCredentials(String credentialsId, String authenticationAgent, NutsEnvProvider envProvider) {
        //credentials are already encrypted with default passphrase!
        if (!CoreStringUtils.isBlank(credentialsId)) {
            credentialsId = new String(CoreSecurityUtils.httpDecrypt(credentialsId.getBytes(), CoreNutsUtils.DEFAULT_PASSPHRASE));
        }
        return credentialsId;
    }

    @Override
    public String setCredentials(String credentials, String authenticationAgent, NutsEnvProvider envProvider) {
        if (CoreStringUtils.isBlank(credentials)) {
            credentials = null;
        } else {
            credentials = CoreSecurityUtils.evalSHA1(credentials);
        }
        return credentials;
    }
}
