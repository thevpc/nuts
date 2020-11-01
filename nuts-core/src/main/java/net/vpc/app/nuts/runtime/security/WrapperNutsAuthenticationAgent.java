package net.vpc.app.nuts.runtime.security;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

class WrapperNutsAuthenticationAgent {

    protected NutsWorkspace ws;
    protected Function<String, NutsAuthenticationAgent> provider;
    protected Supplier<Map<String,String>> envProvider;
    private final Map<String, NutsAuthenticationAgent> cache = new HashMap<>();

    public WrapperNutsAuthenticationAgent(NutsWorkspace ws, Supplier<Map<String,String>> envProvider, Function<String, NutsAuthenticationAgent> agentProvider) {
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

    public boolean removeCredentials(char[] credentialsId, NutsSession session) {
        return getCachedAuthenticationAgent(extractId(credentialsId)).removeCredentials(credentialsId, envProvider.get(), session);
    }

    public void checkCredentials(char[] credentialsId, char[] password, NutsSession session) {
        getCachedAuthenticationAgent(extractId(credentialsId)).checkCredentials(credentialsId, password, envProvider.get(), session);
    }

    protected String extractId(char[] a) {
        String b = new String(a);
        int x = b.indexOf(':');
        if (x <= 0) {
            if (ws.config().options().isRecover()) {
                //All stored passwords will be reset to 'secret'
                ws.createSession().err().println("@@RECOVER MODE : Password could no be parsed due a change in encryption spec. WIll use new default agent@@");
                return null;
            }
            throw new NutsSecurityException(ws, "credential id must start with authentication agent id");
        }
        return b.substring(0, x);
    }

    public char[] getCredentials(char[] credentialsId, NutsSession session) {
        return getCachedAuthenticationAgent(extractId(credentialsId)).getCredentials(credentialsId, envProvider.get(), session);
    }

    public char[] createCredentials(char[] credentials, boolean allowRetreive, char[] credentialId, NutsSession session) {
        if (credentialId != null) {
            return getCachedAuthenticationAgent(extractId(credentialId)).createCredentials(credentials, allowRetreive, credentialId, envProvider.get(), session);
        } else {
            return getCachedAuthenticationAgent("").createCredentials(credentials, allowRetreive, credentialId, envProvider.get(), session);
        }
    }
}
