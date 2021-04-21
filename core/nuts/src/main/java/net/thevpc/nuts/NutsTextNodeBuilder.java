package net.thevpc.nuts;

import java.util.Collection;

/**
 * @category Format
 */
public interface NutsTextNodeBuilder extends NutsString {

    NutsTextStyleGenerator getStyleGenerator();

    NutsTextNodeBuilder setStyleGenerator(NutsTextStyleGenerator styleGenerator);

    NutsTextNodeWriteConfiguration getConfiguration();

    NutsTextNodeBuilder setConfiguration(NutsTextNodeWriteConfiguration writeConfiguration);

    NutsTextNodeBuilder appendCommand(NutsTerminalCommand command);

    NutsTextNodeBuilder appendCode(String lang, String text);

    NutsTextNodeBuilder appendHash(Object text);

    NutsTextNodeBuilder appendRandom(Object text);

    NutsTextNodeBuilder appendHash(Object text, Object hash);

    NutsTextNodeBuilder append(Object text, NutsTextNodeStyle style);

    NutsTextNodeBuilder append(Object text, NutsTextNodeStyles styles);

    NutsTextNodeBuilder append(Object node);

    NutsTextNodeBuilder append(NutsText node);

    NutsTextNodeBuilder appendJoined(Object separator, Collection<?> others);

    NutsTextNodeBuilder appendAll(Collection<?> others);

    NutsText build();

    NutsTextParser parser();

    String toString();

    int size();

}
