package net.thevpc.nuts.runtime.standalone.security;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import net.thevpc.nuts.NutsAuthenticationAgent;
import net.thevpc.nuts.NutsSecurityException;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;

class WrapperNutsAuthenticationAgent {

    protected NutsWorkspace ws;
    protected NutsAuthenticationAgentProvider provider;
    protected Supplier<Map<String,String>> envProvider;
    private final Map<String, NutsAuthenticationAgent> cache = new HashMap<>();

    public WrapperNutsAuthenticationAgent(NutsWorkspace ws, Supplier<Map<String,String>> envProvider, NutsAuthenticationAgentProvider agentProvider) {
        this.envProvider = envProvider;
        this.provider = agentProvider;
        this.ws = ws;
    }

    public NutsAuthenticationAgent getCachedAuthenticationAgent(String name,NutsSession session) {
        session= NutsWorkspaceUtils.of(ws).validateSession(session);
        name = CoreStringUtils.trim(name);
        NutsAuthenticationAgent a = cache.get(name);
        if (a == null) {
            a = provider.create(name,session);
            cache.put(name, a);
            if (!a.getId().equals(name)) {
                cache.put(a.getId(), a);
            }
        }
        return a;
    }

    public boolean removeCredentials(char[] credentialsId, NutsSession session) {
        session= NutsWorkspaceUtils.of(ws).validateSession(session);
        return getCachedAuthenticationAgent(extractId(credentialsId),session).removeCredentials(credentialsId, envProvider.get(), session);
    }

    public void checkCredentials(char[] credentialsId, char[] password, NutsSession session) {
        session= NutsWorkspaceUtils.of(ws).validateSession(session);
        getCachedAuthenticationAgent(extractId(credentialsId),session).checkCredentials(credentialsId, password, envProvider.get(), session);
    }

    protected String extractId(char[] a) {
        String b = new String(a);
        int x = b.indexOf(':');
        if (x <= 0) {
            if (ws.config().options().isRecover()) {
                //All stored passwords will be reset to 'secret'
                ws.createSession().err().println("```error RECOVER MODE : Password could no be parsed due a change in encryption spec. WIll use new default agent```");
                return null;
            }
            throw new NutsSecurityException(ws, "credential id must start with authentication agent id");
        }
        return b.substring(0, x);
    }

    public char[] getCredentials(char[] credentialsId, NutsSession session) {
        return getCachedAuthenticationAgent(extractId(credentialsId),session).getCredentials(credentialsId, envProvider.get(), session);
    }

    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId, NutsSession session) {
        session= NutsWorkspaceUtils.of(ws).validateSession(session);
        if (credentialId != null) {
            return getCachedAuthenticationAgent(extractId(credentialId),session).createCredentials(credentials, allowRetrieve, credentialId, envProvider.get(), session);
        } else {
            return getCachedAuthenticationAgent("",session).createCredentials(credentials, allowRetrieve, credentialId, envProvider.get(), session);
        }
    }
}
