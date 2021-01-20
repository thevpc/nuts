package net.thevpc.nuts;

import java.util.Collection;

/**
 * @category Format
 */
public interface NutsTextNodeFactory {
    NutsTextNodeFactory setSession(NutsSession session);

    NutsSession getSession();

    NutsTextNode blank();
    NutsTextNode nodeFor(Object t);

    NutsTextNode plain(String t);

    NutsTextNode list(NutsTextNode... nodes);

    NutsTextNode list(Collection<NutsTextNode> nodes);

    NutsTextNode styled(String other, NutsTextNodeStyle... decorations);

    NutsTextNode styled(NutsString other, NutsTextNodeStyle... decorations);

    NutsTextNode styled(NutsTextNode other, NutsTextNodeStyle... decorations);

    NutsTextNode command(String command, String args);
    NutsTextNode command(String command);
    NutsTextNode code(String lang, String text);

    NutsTitleNumberSequence createTitleNumberSequence();

    NutsTitleNumberSequence createTitleNumberSequence(String pattern);

}
