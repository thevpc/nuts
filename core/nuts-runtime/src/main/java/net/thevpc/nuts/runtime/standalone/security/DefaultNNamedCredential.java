package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.security.NSecureToken;
import net.thevpc.nuts.security.NNamedCredential;
import net.thevpc.nuts.security.NNamedCredentialBuilder;

public class DefaultNNamedCredential implements NNamedCredential {
    private final String name;
    private final String userName;
    private final String authType;
    private final NSecureToken credentialId;
    private final String resource;

    public DefaultNNamedCredential(String name, String userName, NSecureToken credentialId, String authType, String resource) {
        this.name = name;
        this.userName = userName;
        this.credentialId = credentialId;
        this.authType = authType;
        this.resource = resource;
    }

    @Override
    public String getAuthType() {
        return authType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public NSecureToken getCredential() {
        return credentialId;
    }

    @Override
    public String getResource() {
        return resource;
    }

    @Override
    public NNamedCredentialBuilder builder() {
        return new DefaultNNamedCredentialBuilder(name, userName, credentialId, authType, resource);
    }
}
