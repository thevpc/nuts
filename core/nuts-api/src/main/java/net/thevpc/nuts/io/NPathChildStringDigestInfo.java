package net.thevpc.nuts.io;

public class NPathChildStringDigestInfo {
    private String name;
    private String digest;

    public String name() {
        return name;
    }

    public NPathChildStringDigestInfo name(String name) {
        this.name = name;
        return this;
    }

    public String digest() {
        return digest;
    }

    public NPathChildStringDigestInfo digest(String digest) {
        this.digest = digest;
        return this;
    }
}
