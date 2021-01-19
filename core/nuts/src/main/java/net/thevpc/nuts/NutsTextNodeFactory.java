package net.thevpc.nuts;

import java.util.Collection;

/**
 * @category Format
 */
public interface NutsTextNodeFactory {
    NutsTextNode blank();
    NutsTextNode formatted(Object t);

    NutsTextNode formatted(NutsFormattable t);

    NutsTextNode plain(String t);

    NutsTextNode list(NutsTextNode... nodes);

    NutsTextNode list(Collection<NutsTextNode> nodes);

    NutsTextNode styled(String other, NutsTextNodeStyle... decorations);

    NutsTextNode styled(NutsString other, NutsTextNodeStyle... decorations);

    NutsTextNode styled(NutsTextNode other, NutsTextNodeStyle... decorations);

    NutsTextNode command(String command, String args);
    NutsTextNode command(String command);
    NutsTextNode code(String lang, String text);
    NutsTextNode parseBloc(String lang, String text);

    NutsTextNodeStyle[] toBasicStyles(NutsTextNodeStyle style);
}
