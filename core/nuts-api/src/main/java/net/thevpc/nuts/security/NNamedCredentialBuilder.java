package net.thevpc.nuts.security;

/**
 * NNamedCredentialBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NNamedCredentialBuilder {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
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

    /**
     * Credential id.
     *
     * @return credential id result
     */
    NSecureToken credentialId();

    /**
     * Optional URL pattern for auto-resolution (e.g., "https://github.com/*").
     * When null, credential must be explicitly referenced by name.
     */
    String resource();

    /**
     * Resource.
     *
     * @param resource resource
     * @return resource result
     */
    NNamedCredentialBuilder resource(String resource);

    /**
     * Credential id.
     *
     * @param credentialId credential id
     * @return credential id result
     */
    NNamedCredentialBuilder credentialId(NSecureToken credentialId);

    /**
     * User name.
     *
     * @param user user
     * @return user name result
     */
    NNamedCredentialBuilder userName(String user);

    /**
     * Name.
     *
     * @param name name
     * @return name result
     */
    NNamedCredentialBuilder name(String name);

    /**
     * Auth type.
     *
     * @return auth type result
     */
    String authType();

    /**
     * Auth type.
     *
     * @param authType auth type
     * @return auth type result
     */
    NNamedCredentialBuilder authType(String authType);

    /**
     * Build.
     *
     * @return build result
     */
    NNamedCredential build();
}
