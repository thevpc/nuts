package net.thevpc.nuts.runtime.standalone.security;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.thevpc.nuts.*;


import net.thevpc.nuts.security.NAuthenticationAgent;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

class WrapperNAuthenticationAgent {

    protected NWorkspace ws;
    protected NAuthenticationAgentProvider provider;
    protected Supplier<Map<String,String>> envProvider;
    private final Map<String, NAuthenticationAgent> cache = new HashMap<>();

    public WrapperNAuthenticationAgent(NWorkspace ws, Supplier<Map<String,String>> envProvider, NAuthenticationAgentProvider agentProvider) {
        this.envProvider = envProvider;
        this.provider = agentProvider;
        this.ws = ws;
    }

    public NAuthenticationAgent getCachedAuthenticationAgent(String name) {
        name = NStringUtils.trim(name);
        NAuthenticationAgent a = cache.get(name);
        if (a == null) {
            a = provider.create(name);
            cache.put(name, a);
            if (!a.getId().equals(name)) {
                cache.put(a.getId(), a);
            }
        }
        return a;
    }

    public boolean removeCredentials(char[] credentialsId) {
        return getCachedAuthenticationAgent(extractId(credentialsId)).removeCredentials(credentialsId, envProvider.get());
    }

    public void checkCredentials(char[] credentialsId, char[] password) {
        getCachedAuthenticationAgent(extractId(credentialsId)).checkCredentials(credentialsId, password, envProvider.get());
    }

    protected String extractId(char[] a) {
        String b = new String(a);
        int x = b.indexOf(':');
        if (x <= 0) {
            if (NWorkspace.get().getBootOptions().getRecover().orElse(false)) {
                //All stored passwords will be reset to 'secret'
                NSession.get().err().println("```error RECOVER MODE : Password could no be parsed due a change in encryption spec. WIll use new default agent```");
                return null;
            }
            throw new NSecurityException(NMsg.ofPlain("credential id must start with authentication agent id"));
        }
        return b.substring(0, x);
    }

    public char[] getCredentials(char[] credentialsId) {
        return getCachedAuthenticationAgent(extractId(credentialsId)).getCredentials(credentialsId, envProvider.get());
    }

    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId) {
        if (credentialId != null) {
            return getCachedAuthenticationAgent(extractId(credentialId)).createCredentials(credentials, allowRetrieve, credentialId, envProvider.get());
        } else {
            return getCachedAuthenticationAgent("").createCredentials(credentials, allowRetrieve, credentialId, envProvider.get());
        }
    }
}
