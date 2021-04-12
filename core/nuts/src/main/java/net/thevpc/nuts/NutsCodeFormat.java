package net.thevpc.nuts;

public interface NutsCodeFormat {

    public int getSupportLevel(NutsSupportLevelContext<String> criteria);

    NutsTextNode textToNode(String text, NutsSession session);

    NutsTextNode tokenToNode(String text, String tokenType, NutsSession session);
}
