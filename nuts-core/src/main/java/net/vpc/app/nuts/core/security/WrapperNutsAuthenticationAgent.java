package net.vpc.app.nuts.core.security;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

public class WrapperNutsAuthenticationAgent implements NutsAuthenticationAgent {

    private final String id;
    protected NutsWorkspace ws;
    protected Function<String, NutsAuthenticationAgent> provider;
    private final Map<String, NutsAuthenticationAgent> cache = new HashMap<>();

    public WrapperNutsAuthenticationAgent(NutsWorkspace ws, Function<String, NutsAuthenticationAgent> agentProvider) {
        this.id = "default";
        this.provider = agentProvider;
        this.ws = ws;
    }

    public NutsAuthenticationAgent getCached(String name) {
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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean removeCredentials(char[] credentialsId) {
        return getCached(extractId(credentialsId)).removeCredentials(credentialsId);
    }

    @Override
    public int getSupportLevel(String authenticationAgent) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public void checkCredentials(char[] credentialsId, char[] password) {
        getCached(extractId(credentialsId)).checkCredentials(credentialsId, password);
    }

    protected String extractId(char[] a) {
        String b = new String(a);
        int x = b.indexOf(':');
        if (x <= 0) {
            throw new NutsSecurityException(ws, "credential id must start with authentication agent id");
        }
        return b.substring(0, x);
    }

    @Override
    public char[] getCredentials(char[] credentialsId) {
        return getCached(extractId(credentialsId)).getCredentials(credentialsId);
    }

    @Override
    public char[] setCredentials(char[] credentials, boolean allowRetreive, char[] credentialId) {
        if (credentialId != null) {
            return getCached(extractId(credentialId)).setCredentials(credentials, allowRetreive, credentialId);
        } else {
            return getCached("").setCredentials(credentials, allowRetreive, credentialId);
        }
    }
}
