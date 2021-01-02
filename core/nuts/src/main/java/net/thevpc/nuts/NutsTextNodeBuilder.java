package net.thevpc.nuts;

import java.util.List;

/**
 * @category Format
 */
public interface NutsTextNodeBuilder extends NutsString {
    NutsTextNodeWriteConfiguration getWriteConfiguration();

    NutsTextNodeBuilder setWriteConfiguration(NutsTextNodeWriteConfiguration writeConfiguration);

    NutsTextNodeBuilder appendCommand(String command, String args);

    NutsTextNodeBuilder appendCode(String lang, String text);

    NutsTextNodeBuilder append(String text, NutsTextNodeStyle... styles);

    NutsTextNodeBuilder appendHash(Object text);

    NutsTextNodeBuilder appendHash(Object text, Object hash);

    NutsTextNodeBuilder append(NutsTextNode text, NutsTextNodeStyle... styles);

    NutsTextNodeBuilder append(NutsTextNode node);

    NutsTextNodeBuilder append(NutsString str);

    NutsTextNodeBuilder append(NutsFormattable str);

    NutsTextNodeBuilder appendJoined(NutsTextNode separator, List<NutsTextNode> others);

    NutsTextNode build();

    String toString();
    int size();

}
