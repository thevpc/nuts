package net.thevpc.nuts;

import java.util.Collection;

public interface NutsTextNodeFactory {
    NutsTextNode plain(String t);

    NutsTextNode title(String t, int level);

    NutsTextNode list(NutsTextNode... nodes);

    NutsTextNode list(Collection<NutsTextNode> nodes);

    NutsTextNode error(String image);

    NutsTextNode warn(String image);

    NutsTextNode styled(String other, NutsTextNodeStyle... decorations);
    NutsTextNode styled(NutsTextNode other, NutsTextNodeStyle... decorations);

    NutsTextNode command(String command, String args);
    NutsTextNode code(String lang, String text);
    NutsTextNode parseCode(String lang, String text);
}
