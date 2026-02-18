package net.thevpc.nuts.security;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NToElement;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NExceptions;
import net.thevpc.nuts.util.NStringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * An opaque handle representing a secret.
 * * The payload may contain the encrypted secret itself (stateless),
 * or it may be a simple identifier/key used by the Agent to
 * look up the secret in an external store (stateful).
 */
public class NSecureToken implements Serializable, NToElement {
    private final String agentId;   // e.g., "default", "keychain"
    private final String payload;   // encrypted ciphertext (agent-specific format)

    public NSecureToken(String agentId, String payload) {
        NAssert.requireNamedNonBlank(agentId, "agentId");
        NAssert.requireNamedNonNull(payload, "payload");
        if (agentId.contains(":")) {
            throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC("agentId must not contain '#' or ':'"));
        }
        this.agentId = NStringUtils.trim(agentId);
        this.payload = payload;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getPayload() {
        return payload;
    }

    /**
     * Serialize to canonical string format: {@code agentId#version:payload}
     */
    @Override
    public String toString() {
        return agentId + ":" + payload;
    }

    /**
     * Parse from canonical string format.
     *
     * @throws IllegalArgumentException if format invalid
     */
    public static NSecureToken parse(String s) {
        int colon = s.indexOf(':');
        if (colon <= 0) {
            throw new IllegalArgumentException("Invalid credential ID format: " + s);
        }
        String agentId = s.substring(0, colon);
        String payload = s.substring(colon + 1);
        return new NSecureToken(agentId, payload);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NSecureToken that = (NSecureToken) o;
        return Objects.equals(agentId, that.agentId) && Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentId, payload);
    }

    @Override
    public NElement toElement() {
        return NElement.ofNamedObject("NSecureToken", NElement.ofPair("agentId", agentId), NElement.ofString("payload"), NElement.ofString(payload));
    }
}
