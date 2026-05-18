package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NGetter;

public class NPathChildDigestInfo {
    private String name;
    private byte[] digest;

    @NGetter
    public String name() {
        return name;
    }

    public NPathChildDigestInfo name(String name) {
        this.name = name;
        return this;
    }

    @NGetter
    public byte[] digest() {
        return digest;
    }

    public NPathChildDigestInfo digest(byte[] digest) {
        this.digest = digest;
        return this;
    }
}
