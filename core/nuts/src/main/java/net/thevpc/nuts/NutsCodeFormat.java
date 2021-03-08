package net.thevpc.nuts;

public interface NutsCodeFormat {

    public int getSupportLevel(NutsSupportLevelContext<String> criteria);

    NutsTextNode textToNode(String text);

    NutsTextNode tokenToNode(String text, String tokenType);
}
