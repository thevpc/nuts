package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.EnvProvider;
import net.vpc.app.nuts.NutsSecurityException;
import net.vpc.app.nuts.NutsUserConfig;
import net.vpc.app.nuts.Singleton;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CoreSecurityUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

@Singleton
public class DefaultNutsAuthenticationAgent implements NutsAuthenticationAgent {
    @Override
    public int getSupportLevel(String authenticationAgent) {
        if(authenticationAgent==null|| authenticationAgent.trim().isEmpty() || authenticationAgent.trim().equals("password")) {
            return DEFAULT_SUPPORT;
        }
        return NO_SUPPORT;
    }

    public void checkCredentials(String credentialsId, String authenticationAgent,String password, EnvProvider envProvider) {
        if (CoreStringUtils.isEmpty(password)) {
            throw new NutsSecurityException("Missing old password");
        }
        //check old password
        if (CoreStringUtils.isEmpty(credentialsId) || credentialsId.equals(CoreSecurityUtils.evalSHA1(password))) {
            throw new NutsSecurityException("Invalid password");
        }


        if(!CoreStringUtils.isEmpty(credentialsId)){
            if ((CoreStringUtils.isEmpty(password) && CoreStringUtils.isEmpty(credentialsId))
                    || (!CoreStringUtils.isEmpty(password) && !CoreStringUtils.isEmpty(credentialsId)
                    && credentialsId.equals(CoreSecurityUtils.evalSHA1(password)))) {
                return;
            }
        }
        throw new NutsSecurityException("Invalid login or password");
    }

    @Override
    public String getCredentials(String credentialsId, String authenticationAgent, EnvProvider envProvider) {
        //credentials are already encrypted with default passphrase!
        if (!CoreStringUtils.isEmpty(credentialsId)) {
            credentialsId = new String(CoreSecurityUtils.httpDecrypt(credentialsId, CoreNutsUtils.DEFAULT_PASSPHRASE));
        }
        return credentialsId;
    }

    @Override
    public String setCredentials(String credentials, String authenticationAgent, EnvProvider envProvider) {
        if (CoreStringUtils.isEmpty(credentials)) {
            credentials = null;
        } else {
            credentials = CoreSecurityUtils.evalSHA1(credentials);
        }
        return credentials;
    }
}
