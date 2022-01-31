package net.thevpc.nuts.installer.util;

public class VerInfo {
    public boolean stable;
    public boolean valid;
    public String api = null;
    public String runtime = null;
    public String location = null;

    public VerInfo(boolean stable) {
        this.stable = stable;
    }
}
