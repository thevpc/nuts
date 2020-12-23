package net.thevpc.nuts;

/**
 * @category Format
 */
public interface NutsTextNodeBuilder extends NutsStringBase{
    NutsTextNodeWriteConfiguration getWriteConfiguration();

    NutsTextNodeBuilder setWriteConfiguration(NutsTextNodeWriteConfiguration writeConfiguration);

    NutsTextNodeBuilder appendCommand(String command, String args);

    NutsTextNodeBuilder appendCode(String lang, String text);

    NutsTextNodeBuilder appendPlain(String text);

    NutsTextNodeBuilder appendStyled(String text, NutsTextNodeStyle... decos);

    NutsTextNodeBuilder appendHashedStyle(Object text);

    NutsTextNodeBuilder appendHashedStyle(Object text, Object hash);

    NutsTextNodeBuilder appendStyled(NutsTextNode text, NutsTextNodeStyle... decos);

    NutsTextNode build();

    String toString();

}
