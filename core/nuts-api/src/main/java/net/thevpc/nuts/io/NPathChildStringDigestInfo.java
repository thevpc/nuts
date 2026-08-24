package net.thevpc.nuts.io;

/**
 * NPathChildStringDigestInfo class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NPathChildStringDigestInfo {
    private String name;
    private String digest;

    /**
     * Name.
     *
     * @return name result
     */
    public String name() {
        return name;
    }

    /**
     * Name.
     *
     * @param name name
     * @return name result
     */
    public NPathChildStringDigestInfo name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Digest.
     *
     * @return digest result
     */
    public String digest() {
        return digest;
    }

    /**
     * Digest.
     *
     * @param digest digest
     * @return digest result
     */
    public NPathChildStringDigestInfo digest(String digest) {
        this.digest = digest;
        return this;
    }
}
