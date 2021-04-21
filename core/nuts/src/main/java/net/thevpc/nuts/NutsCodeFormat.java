package net.thevpc.nuts;

public interface NutsCodeFormat {

    public int getSupportLevel(NutsSupportLevelContext<String> criteria);

    NutsText textToNode(String text, NutsSession session);

    NutsText tokenToNode(String text, String tokenType, NutsSession session);
}
