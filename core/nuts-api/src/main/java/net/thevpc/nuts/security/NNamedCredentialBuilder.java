package net.thevpc.nuts.security;

public interface NNamedCredentialBuilder {
    static NNamedCredentialBuilder of() {
        return NSecurityManager.of().createNamedCredentialBuilder();
    }

    /**
     * Human-readable name (e.g., "github-personal", "nexus-corp").
     * Used in .nops files: include(url, credential:"github-personal")
     */
    String name();

    /**
     * Nuts workspace user who owns this credential (permission scoping).
     * Only this user (or admin) can use/reference this credential.
     */
    String userName();

    NSecureToken credentialId();

    /**
     * Optional URL pattern for auto-resolution (e.g., "https://github.com/*").
     * When null, credential must be explicitly referenced by name.
     */
    String resource();

    NNamedCredentialBuilder resource(String resource);

    NNamedCredentialBuilder credentialId(NSecureToken credentialId);

    NNamedCredentialBuilder userName(String user);

    NNamedCredentialBuilder name(String name);

    String authType();

    NNamedCredentialBuilder authType(String authType);

    NNamedCredential build();
}
