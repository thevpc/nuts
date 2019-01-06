package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.NutsAuthenticationAgent;
import net.vpc.app.nuts.NutsEnvProvider;
import net.vpc.app.nuts.NutsSecurityException;
import net.vpc.app.nuts.Singleton;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CoreSecurityUtils;
import net.vpc.common.strings.StringUtils;

@Singleton
public class DefaultNutsAuthenticationAgent implements NutsAuthenticationAgent {
    @Override
    public int getSupportLevel(String authenticationAgent) {
        if(authenticationAgent==null|| authenticationAgent.trim().isEmpty() || authenticationAgent.trim().equals("password")) {
            return DEFAULT_SUPPORT;
        }
        return NO_SUPPORT;
    }

    public void checkCredentials(String credentialsId, String authenticationAgent,String password, NutsEnvProvider envProvider) {
        if (StringUtils.isEmpty(password)) {
            throw new NutsSecurityException("Missing old password");
        }
        //check old password
        if (StringUtils.isEmpty(credentialsId) || credentialsId.equals(CoreSecurityUtils.evalSHA1(password))) {
            throw new NutsSecurityException("Invalid password");
        }


        if(!StringUtils.isEmpty(credentialsId)){
            if ((StringUtils.isEmpty(password) && StringUtils.isEmpty(credentialsId))
                    || (!StringUtils.isEmpty(password) && !StringUtils.isEmpty(credentialsId)
                    && credentialsId.equals(CoreSecurityUtils.evalSHA1(password)))) {
                return;
            }
        }
        throw new NutsSecurityException("Invalid login or password");
    }

    @Override
    public String getCredentials(String credentialsId, String authenticationAgent, NutsEnvProvider envProvider) {
        //credentials are already encrypted with default passphrase!
        if (!StringUtils.isEmpty(credentialsId)) {
            credentialsId = new String(CoreSecurityUtils.httpDecrypt(credentialsId.getBytes(), CoreNutsUtils.DEFAULT_PASSPHRASE));
        }
        return credentialsId;
    }

    @Override
    public String setCredentials(String credentials, String authenticationAgent, NutsEnvProvider envProvider) {
        if (StringUtils.isEmpty(credentials)) {
            credentials = null;
        } else {
            credentials = CoreSecurityUtils.evalSHA1(credentials);
        }
        return credentials;
    }
}
