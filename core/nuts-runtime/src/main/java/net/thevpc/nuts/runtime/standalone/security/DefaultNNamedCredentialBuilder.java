package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.security.NSecureToken;
import net.thevpc.nuts.security.NNamedCredential;
import net.thevpc.nuts.security.NNamedCredentialBuilder;

import java.util.function.Supplier;

public class DefaultNNamedCredentialBuilder implements NNamedCredentialBuilder {
    private String name;
    private String userName;
    private NSecureToken credentialId;
    private String authType;
    private String resource;
    private Supplier<NSecureToken> credentialIdSupplier;

    public DefaultNNamedCredentialBuilder() {
    }

    public DefaultNNamedCredentialBuilder(String name, String userName, NSecureToken credentialId, String authType, String resource) {
        this.name = name;
        this.userName = userName;
        this.authType = authType;
        this.credentialId = credentialId;
        this.resource = resource;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NNamedCredentialBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public NNamedCredentialBuilder setUserName(String user) {
        this.userName = user;
        return this;
    }

    public String getAuthType() {
        return authType;
    }

    public NNamedCredentialBuilder setAuthType(String authType) {
        this.authType = authType;
        return this;
    }

    @Override
    public NSecureToken getCredentialId() {
        return credentialId;
    }

    @Override
    public String getResource() {
        return resource;
    }

    @Override
    public NNamedCredentialBuilder setResource(String resource) {
        this.resource = resource;
        return this;
    }

    @Override
    public NNamedCredentialBuilder setCredentialId(NSecureToken credentialId) {
        this.credentialId = credentialId;
        credentialIdSupplier = () -> credentialId;
        return this;
    }

    @Override
    public NNamedCredential build() {
        return new DefaultNNamedCredential(name, userName, credentialId, authType, resource);
    }
}
