package net.thevpc.nuts;

public interface NutsTextFormatTheme {
    String getName();
    NutsTextStyles toBasicStyles(NutsTextStyles style, NutsSession session);
}
