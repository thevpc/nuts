package net.thevpc.nuts.runtime.standalone.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;


import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.security.NAuthenticationAgent;
import net.thevpc.nuts.security.NCredentialId;
import net.thevpc.nuts.security.NSecretCaller;
import net.thevpc.nuts.security.NSecretRunner;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

public class WrapperNAuthenticationAgent {

    protected NWorkspace ws;
    protected NAuthenticationAgentProvider provider;
    protected Supplier<Function<String, String>> envProvider;
    private final Map<String, NAuthenticationAgent> cache = new HashMap<>();

    public WrapperNAuthenticationAgent(NWorkspace ws, Supplier<Function<String, String>> envProvider, NAuthenticationAgentProvider agentProvider) {
        this.envProvider = envProvider;
        this.provider = agentProvider;
        this.ws = ws;
    }

    public NAuthenticationAgent getCachedAuthenticationAgent(NCredentialId name) {
        if (name == null) {
            return getCachedAuthenticationAgent("");
        }
        return getCachedAuthenticationAgent(name.getAgentId());
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

    public void runWithSecret(NCredentialId id, NSecretRunner consumer) {
        getCachedAuthenticationAgent(id).withSecret(id, new NSecretCaller<Object>() {
            @Override
            public Object call(NCredentialId id, char[] secretm, Function<String, String> env) {
                consumer.run(id, secretm, env);
                return null;
            }
        }, envProvider.get());
    }

    public <T> T callWithSecret(NCredentialId id, NSecretCaller<T> consumer) {
        return getCachedAuthenticationAgent(id).withSecret(id, consumer, envProvider.get());
    }

    public boolean verify(NCredentialId credentialsId, char[] candidate) {
        return getCachedAuthenticationAgent(credentialsId).verify(credentialsId, candidate, envProvider.get());
    }

    public boolean removeCredentials(NCredentialId credentialsId) {
        return getCachedAuthenticationAgent(credentialsId).removeCredentials(credentialsId, envProvider.get());
    }

    public NCredentialId storeSecret(char[] credentials, String agent) {
        return getCachedAuthenticationAgent(agent).addSecret(credentials, envProvider.get());
    }

    public NCredentialId updateSecret(NCredentialId old, char[] credentials, String agent) {
        if (NBlankable.isBlank(agent) || Objects.equals(old.getAgentId(), agent)) {
            return getCachedAuthenticationAgent(old).updateSecret(old, credentials, envProvider.get());
        } else {
            NAuthenticationAgent na = getCachedAuthenticationAgent(agent);
            getCachedAuthenticationAgent(old).removeCredentials(old, envProvider.get());
            return na.addSecret(credentials, envProvider.get());
        }
    }

    public NCredentialId storeOneWay(char[] password, String agent) {
        return getCachedAuthenticationAgent(agent).addOneWayCredential(password, envProvider.get());
    }

    public NCredentialId updateOneWay(NCredentialId old, char[] credentials, String agent) {
        if (NBlankable.isBlank(agent) || Objects.equals(old.getAgentId(), agent)) {
            return getCachedAuthenticationAgent(old).updateOneWay(old, credentials, envProvider.get());
        } else {
            NAuthenticationAgent na = getCachedAuthenticationAgent(agent);
            getCachedAuthenticationAgent(old).removeCredentials(old, envProvider.get());
            return na.addOneWayCredential(credentials, envProvider.get());
        }
    }
}
