package net.thevpc.nuts;

/**
 * @category Format
 */
public interface NutsTextFormatManager {

    NutsTextFormatTheme getTheme();

    NutsTextNodeFactory factory();

    NutsTextNodeBuilder builder();

    NutsTextNode parse(String t);

    NutsTextNodeParser parser();

    NutsTextFormatManager addCodeFormat(NutsCodeFormat format);

    NutsTextFormatManager removeCodeFormat(NutsCodeFormat format);

    NutsCodeFormat[] getCodeFormats();
}
