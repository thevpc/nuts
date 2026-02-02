package net.thevpc.nuts.security;

public interface NNamedCredential {
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

    String getAuthType();

    /**
     * Opaque credential identifier in {@code agent#version:payload} format.
     * This is the exact value that can be passed to
     * {@link NSecurityManager#addSecret(char[])} for decryption.
     * <p>
     * Example: {@code "default#1:HaT9KSjgfqdBFSgshPDh6UcFJ5T/ly2q0s7fUMGiRzHFPfhkYi5Ovr9DxsGj4ZbLwBm1w9UhCGYzz749lIFLtug=="}
     * </p>
     * <p>
     * <strong>Security note:</strong> This is encrypted ciphertext — not the raw secret.
     * It is safe to store in config files but MUST NOT be treated as the actual password/token.
     * </p>
     *
     * @return opaque credential identifier (agent#version:payload)
     */
    NCredentialId getCredential();

    /**
     * Optional URL pattern for auto-resolution (e.g., "https://github.com/*").
     * When null, credential must be explicitly referenced by name.
     */
    String getResource();

    NNamedCredentialBuilder builder();
}
