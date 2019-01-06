package net.vpc.app.nuts;


public interface NutsAuthenticationAgent extends NutsComponent<String>{

    void checkCredentials(String credentialsId, String authenticationAgent, String password, NutsEnvProvider envProvider);

    /**
     * get the credentials for the given id
     * @param credentialsId credentials-id
     * @param authenticationAgent agent id
     * @return credentials
     */
    String getCredentials(String credentialsId, String authenticationAgent, NutsEnvProvider envProvider);

    /**
     * store credentials in the agent and return the credential
     * id to store into the config
     * @param credentials credential
     * @param authenticationAgent agent id
     * @return credentials-id
     */
    String setCredentials(String credentials, String authenticationAgent, NutsEnvProvider envProvider);
}
