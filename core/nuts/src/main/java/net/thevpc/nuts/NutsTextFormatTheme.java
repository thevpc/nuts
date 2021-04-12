package net.thevpc.nuts;

public interface NutsTextFormatTheme {
    String getName();
    NutsTextNodeStyles toBasicStyles(NutsTextNodeStyles style, NutsSession session);
}
