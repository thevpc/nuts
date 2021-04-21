package net.thevpc.nuts;

import java.util.Collection;

/**
 * @category Format
 */
public interface NutsTextManager {

    NutsTextManager setSession(NutsSession session);

    NutsSession getSession();

    NutsTextNodeBuilder builder();

    NutsText forBlank();

    NutsText toText(Object t);

    NutsTextPlain forPlain(String t);

    NutsTextList forList(NutsText... nodes);

    NutsTextList forList(Collection<NutsText> nodes);

    NutsTextStyled forStyled(String other, NutsTextNodeStyles decorations);

    NutsTextStyled forStyled(NutsString other, NutsTextNodeStyles decorations);

    NutsTextStyled forStyled(NutsText other, NutsTextNodeStyles decorations);

    NutsTextStyled forStyled(String other, NutsTextNodeStyle decorations);

    NutsTextStyled forStyled(NutsString other, NutsTextNodeStyle decorations);

    NutsTextStyled forStyled(NutsText other, NutsTextNodeStyle decorations);

    NutsTextCommand forCommand(NutsTerminalCommand command);

    NutsTextCode forCode(String lang, String text);

    NutsTextNumbering forNumbering();

    NutsTextNumbering forTitleNumberSequence(String pattern);

    NutsTextAnchor forAnchor(String anchorName);

    NutsTextLink forLink(NutsText value);

    NutsTextFormatTheme getTheme();

    NutsTextManager setTheme(NutsTextFormatTheme theme);

    NutsCodeFormat getCodeFormat(String kind);

    NutsTextManager addCodeFormat(NutsCodeFormat format);

    NutsTextManager removeCodeFormat(NutsCodeFormat format);

    NutsCodeFormat[] getCodeFormats();

    NutsText parse(String t);

    NutsTextParser parser();

}
