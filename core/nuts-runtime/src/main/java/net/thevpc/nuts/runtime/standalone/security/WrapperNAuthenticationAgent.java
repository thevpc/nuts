package net.thevpc.nuts.runtime.standalone.security;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.spi.NAuthenticationAgent;
import net.thevpc.nuts.util.NStringUtils;

class WrapperNAuthenticationAgent {

    protected NWorkspace ws;
    protected NAuthenticationAgentProvider provider;
    protected Function<NSession,Map<String,String>> envProvider;
    private final Map<String, NAuthenticationAgent> cache = new HashMap<>();

    public WrapperNAuthenticationAgent(NWorkspace ws, Function<NSession,Map<String,String>> envProvider, NAuthenticationAgentProvider agentProvider) {
        this.envProvider = envProvider;
        this.provider = agentProvider;
        this.ws = ws;
    }

    public NAuthenticationAgent getCachedAuthenticationAgent(String name, NSession session) {
        NSessionUtils.checkSession(ws, session);
        name = NStringUtils.trim(name);
        NAuthenticationAgent a = cache.get(name);
        if (a == null) {
            a = provider.create(name,session);
            cache.put(name, a);
            if (!a.getId().equals(name)) {
                cache.put(a.getId(), a);
            }
        }
        return a;
    }

    public boolean removeCredentials(char[] credentialsId, NSession session) {
        NSessionUtils.checkSession(ws, session);
        return getCachedAuthenticationAgent(extractId(credentialsId,session),session).removeCredentials(credentialsId, envProvider.apply(session), session);
    }

    public void checkCredentials(char[] credentialsId, char[] password, NSession session) {
        NSessionUtils.checkSession(ws, session);
        getCachedAuthenticationAgent(extractId(credentialsId,session),session).checkCredentials(credentialsId, password, envProvider.apply(session), session);
    }

    protected String extractId(char[] a, NSession session) {
        String b = new String(a);
        int x = b.indexOf(':');
        if (x <= 0) {
            if (session.boot().getBootOptions().getRecover().orElse(false)) {
                //All stored passwords will be reset to 'secret'
                session.err().println("```error RECOVER MODE : Password could no be parsed due a change in encryption spec. WIll use new default agent```");
                return null;
            }
            throw new NSecurityException(session, NMsg.ofPlain("credential id must start with authentication agent id"));
        }
        return b.substring(0, x);
    }

    public char[] getCredentials(char[] credentialsId, NSession session) {
        return getCachedAuthenticationAgent(extractId(credentialsId,session),session).getCredentials(credentialsId, envProvider.apply(session), session);
    }

    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId, NSession session) {
        NSessionUtils.checkSession(ws, session);
        if (credentialId != null) {
            return getCachedAuthenticationAgent(extractId(credentialId,session),session).createCredentials(credentials, allowRetrieve, credentialId, envProvider.apply(session), session);
        } else {
            return getCachedAuthenticationAgent("",session).createCredentials(credentials, allowRetrieve, credentialId, envProvider.apply(session), session);
        }
    }
}
