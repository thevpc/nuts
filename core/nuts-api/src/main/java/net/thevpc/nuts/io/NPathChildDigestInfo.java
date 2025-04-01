package net.thevpc.nuts.io;

public class NPathChildDigestInfo {
    private String name;
    private byte[] digest;

    public String getName() {
        return name;
    }

    public NPathChildDigestInfo setName(String name) {
        this.name = name;
        return this;
    }

    public byte[] getDigest() {
        return digest;
    }

    public NPathChildDigestInfo setDigest(byte[] digest) {
        this.digest = digest;
        return this;
    }
}
