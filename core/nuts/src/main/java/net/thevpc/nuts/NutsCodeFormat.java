package net.thevpc.nuts;

public interface NutsCodeFormat {

    public int getSupportLevel(NutsSupportLevelContext<String> criteria);

    NutsTextNode toNode(String text);
}
