package net.vpc.app.nuts.core.security;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

class WrapperNutsAuthenticationAgent {

    protected NutsWorkspace ws;
    protected Function<String, NutsAuthenticationAgent> provider;
    protected NutsEnvProvider envProvider;
    private final Map<String, NutsAuthenticationAgent> cache = new HashMap<>();

    public WrapperNutsAuthenticationAgent(NutsWorkspace ws, NutsEnvProvider envProvider, Function<String, NutsAuthenticationAgent> agentProvider) {
        this.envProvider = envProvider;
        this.provider = agentProvider;
        this.ws = ws;
    }

    public NutsAuthenticationAgent getCachedAuthenticationAgent(String name) {
        name = CoreStringUtils.trim(name);
        NutsAuthenticationAgent a = cache.get(name);
        if (a == null) {
            a = provider.apply(name);
            cache.put(name, a);
            if (!a.getId().equals(name)) {
                cache.put(a.getId(), a);
            }
        }
        return a;
    }

    public boolean removeCredentials(char[] credentialsId) {
        return getCachedAuthenticationAgent(extractId(credentialsId)).removeCredentials(credentialsId, envProvider);
    }

    public void checkCredentials(char[] credentialsId, char[] password) {
        getCachedAuthenticationAgent(extractId(credentialsId)).checkCredentials(credentialsId, password, envProvider);
    }

    protected String extractId(char[] a) {
        String b = new String(a);
        int x = b.indexOf(':');
        if (x <= 0) {
            if (ws.config().options().getBootCommand() == NutsBootCommand.RECOVER) {
                //All stored passwords will be reset to 'secret'
                ws.createSession().err().println("@@RECOVER MODE : Password could no be parsed due a change in encryption spec. WIll use new default agent@@");
                return null;
            }
            throw new NutsSecurityException(ws, "credential id must start with authentication agent id");
        }
        return b.substring(0, x);
    }

    public char[] getCredentials(char[] credentialsId) {
        return getCachedAuthenticationAgent(extractId(credentialsId)).getCredentials(credentialsId, envProvider);
    }

    public char[] createCredentials(char[] credentials, boolean allowRetreive, char[] credentialId) {
        if (credentialId != null) {
            return getCachedAuthenticationAgent(extractId(credentialId)).createCredentials(credentials, allowRetreive, credentialId, envProvider);
        } else {
            return getCachedAuthenticationAgent("").createCredentials(credentials, allowRetreive, credentialId, envProvider);
        }
    }
}
