package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NGetter;

/**
 * NPathChildDigestInfo class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NPathChildDigestInfo {
    private String name;
    private byte[] digest;

    /**
     * Name.
     *
     * @return name result
     */
    @NGetter
    public String name() {
        return name;
    }

    /**
     * Name.
     *
     * @param name name
     * @return name result
     */
    public NPathChildDigestInfo name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Digest.
     *
     * @return digest result
     */
    @NGetter
    public byte[] digest() {
        return digest;
    }

    /**
     * Digest.
     *
     * @param digest digest
     * @return digest result
     */
    public NPathChildDigestInfo digest(byte[] digest) {
        this.digest = digest;
        return this;
    }
}
