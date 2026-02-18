package net.thevpc.nuts.security;

public interface NNamedCredentialBuilder {
    static NNamedCredentialBuilder of() {
        return NSecurityManager.of().createNamedCredentialBuilder();
    }

    /**
     * Human-readable name (e.g., "github-personal", "nexus-corp").
     * Used in .nops files: include(url, credential:"github-personal")
     */
    String getName();

    /**
     * Nuts workspace user who owns this credential (permission scoping).
     * Only this user (or admin) can use/reference this credential.
     */
    String getUserName();

    NSecureToken getCredentialId();

    /**
     * Optional URL pattern for auto-resolution (e.g., "https://github.com/*").
     * When null, credential must be explicitly referenced by name.
     */
    String getResource();

    NNamedCredentialBuilder setResource(String resource);

    NNamedCredentialBuilder setCredentialId(NSecureToken credentialId);

    NNamedCredentialBuilder setUserName(String user);

    NNamedCredentialBuilder setName(String name);

    String getAuthType();

    NNamedCredentialBuilder setAuthType(String authType);

    NNamedCredential build();
}
