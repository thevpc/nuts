package net.thevpc.nuts;

public interface NutsTextStyleGenerator {
    NutsTextNodeStyle[] hash(Object i);

    NutsTextNodeStyle[] hash(int i);

    NutsTextNodeStyle[] random();

    boolean isIncludePlain();

    NutsTextStyleGenerator setIncludePlain(boolean includePlain);

    boolean isIncludeBold();

    NutsTextStyleGenerator setIncludeBold(boolean includeBold);

    boolean isIncludeBlink();

    NutsTextStyleGenerator setIncludeBlink(boolean includeBlink);

    boolean isIncludeReversed();

    NutsTextStyleGenerator setIncludeReversed(boolean includeReversed);

    boolean isIncludeItalic();

    NutsTextStyleGenerator setIncludeItalic(boolean includeItalic);

    boolean isIncludeUnderlined();

    NutsTextStyleGenerator setIncludeUnderlined(boolean includeUnderlined);

    boolean isIncludeStriked();

    NutsTextStyleGenerator setIncludeStriked(boolean includeStriked);

    boolean isIncludeForeground();

    NutsTextStyleGenerator setIncludeForeground(boolean includeForeground);

    boolean isIncludeBackground();

    NutsTextStyleGenerator setIncludeBackground(boolean includeBackground);

    boolean isUseThemeColors();

    boolean isUsePaletteColors();

    boolean isUseTrueColors();

    NutsTextStyleGenerator setUseThemeColors();

    NutsTextStyleGenerator setUsePaletteColors();

    NutsTextStyleGenerator setUseTrueColors();
}
