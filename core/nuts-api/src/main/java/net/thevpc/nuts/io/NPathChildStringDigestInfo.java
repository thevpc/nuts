package net.thevpc.nuts.io;

public class NPathChildStringDigestInfo {
    private String name;
    private String digest;

    public String getName() {
        return name;
    }

    public NPathChildStringDigestInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getDigest() {
        return digest;
    }

    public NPathChildStringDigestInfo setDigest(String digest) {
        this.digest = digest;
        return this;
    }
}
