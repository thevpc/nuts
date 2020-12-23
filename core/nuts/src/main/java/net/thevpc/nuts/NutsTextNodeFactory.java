package net.thevpc.nuts;

import java.util.Collection;

/**
 * @category Format
 */
public interface NutsTextNodeFactory {
    NutsTextNode plain(String t);

    NutsTextNode title(String t, int level);

    NutsTextNode list(NutsTextNode... nodes);

    NutsTextNode list(Collection<NutsTextNode> nodes);

    NutsTextNode styled(String other, NutsTextNodeStyle... decorations);
    NutsTextNode styled(NutsTextNode other, NutsTextNodeStyle... decorations);

    NutsTextNode command(String command, String args);
    NutsTextNode code(String lang, String text);
    NutsTextNode parseBloc(String lang, String text);
}
