package net.thevpc.nuts;

import java.util.Collection;

/**
 * @category Format
 */
public interface NutsTextManager {

    NutsTextManager setSession(NutsSession session);

    NutsSession getSession();

    NutsTextNode blank();

    NutsTextNodeBuilder builder();

    NutsTextNode nodeFor(Object t);

    NutsTextNodePlain plain(String t);

    NutsTextNodeList list(NutsTextNode... nodes);

    NutsTextNodeList list(Collection<NutsTextNode> nodes);

    NutsTextNodeStyled styled(String other, NutsTextNodeStyles decorations);

    NutsTextNodeStyled styled(NutsString other, NutsTextNodeStyles decorations);

    NutsTextNodeStyled styled(NutsTextNode other, NutsTextNodeStyles decorations);

    NutsTextNodeStyled styled(String other, NutsTextNodeStyle decorations);

    NutsTextNodeStyled styled(NutsString other, NutsTextNodeStyle decorations);

    NutsTextNodeStyled styled(NutsTextNode other, NutsTextNodeStyle decorations);

    NutsTextNodeCommand command(NutsTerminalCommand command);

    NutsTextNodeCode code(String lang, String text);

    NutsTitleNumberSequence createTitleNumberSequence();

    NutsTitleNumberSequence createTitleNumberSequence(String pattern);

    public NutsTextNode parse(String t);

    public NutsTextNodeParser parser();

    NutsTextNodeAnchor anchor(String anchorName);

    NutsTextNodeLink link(NutsTextNode value);

    NutsTextFormatTheme getTheme();

    NutsTextManager setTheme(NutsTextFormatTheme theme);

    NutsCodeFormat getCodeFormat(String kind);

    NutsTextManager addCodeFormat(NutsCodeFormat format);

    NutsTextManager removeCodeFormat(NutsCodeFormat format);

    NutsCodeFormat[] getCodeFormats();
}
