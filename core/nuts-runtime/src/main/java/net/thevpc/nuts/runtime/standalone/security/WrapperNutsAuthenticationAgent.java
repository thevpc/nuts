package net.thevpc.nuts.runtime.standalone.security;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.spi.NutsAuthenticationAgent;

class WrapperNutsAuthenticationAgent {

    protected NutsWorkspace ws;
    protected NutsAuthenticationAgentProvider provider;
    protected Function<NutsSession,Map<String,String>> envProvider;
    private final Map<String, NutsAuthenticationAgent> cache = new HashMap<>();

    public WrapperNutsAuthenticationAgent(NutsWorkspace ws, Function<NutsSession,Map<String,String>> envProvider, NutsAuthenticationAgentProvider agentProvider) {
        this.envProvider = envProvider;
        this.provider = agentProvider;
        this.ws = ws;
    }

    public NutsAuthenticationAgent getCachedAuthenticationAgent(String name,NutsSession session) {
        NutsSessionUtils.checkSession(ws, session);
        name = NutsUtilStrings.trim(name);
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
        NutsSessionUtils.checkSession(ws, session);
        return getCachedAuthenticationAgent(extractId(credentialsId,session),session).removeCredentials(credentialsId, envProvider.apply(session), session);
    }

    public void checkCredentials(char[] credentialsId, char[] password, NutsSession session) {
        NutsSessionUtils.checkSession(ws, session);
        getCachedAuthenticationAgent(extractId(credentialsId,session),session).checkCredentials(credentialsId, password, envProvider.apply(session), session);
    }

    protected String extractId(char[] a,NutsSession session) {
        String b = new String(a);
        int x = b.indexOf(':');
        if (x <= 0) {
            if (session.boot().getBootOptions().getRecover().orElse(false)) {
                //All stored passwords will be reset to 'secret'
                session.err().println("```error RECOVER MODE : Password could no be parsed due a change in encryption spec. WIll use new default agent```");
                return null;
            }
            throw new NutsSecurityException(session, NutsMessage.plain("credential id must start with authentication agent id"));
        }
        return b.substring(0, x);
    }

    public char[] getCredentials(char[] credentialsId, NutsSession session) {
        return getCachedAuthenticationAgent(extractId(credentialsId,session),session).getCredentials(credentialsId, envProvider.apply(session), session);
    }

    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId, NutsSession session) {
        NutsSessionUtils.checkSession(ws, session);
        if (credentialId != null) {
            return getCachedAuthenticationAgent(extractId(credentialId,session),session).createCredentials(credentials, allowRetrieve, credentialId, envProvider.apply(session), session);
        } else {
            return getCachedAuthenticationAgent("",session).createCredentials(credentials, allowRetrieve, credentialId, envProvider.apply(session), session);
        }
    }
}
