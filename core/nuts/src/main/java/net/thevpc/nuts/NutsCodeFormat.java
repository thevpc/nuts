package net.thevpc.nuts;

public interface NutsCodeFormat {

    public int getSupportLevel(NutsSupportLevelContext<String> criteria);

    NutsText stringToText(String text, NutsSession session);

    NutsText tokenToText(String text, String tokenType, NutsSession session);
}
