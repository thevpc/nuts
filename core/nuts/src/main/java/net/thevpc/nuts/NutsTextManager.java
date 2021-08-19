package net.thevpc.nuts;

import java.util.Collection;

/**
 * @app.category Format
 */
public interface NutsTextManager {

    NutsTextManager setSession(NutsSession session);

    NutsSession getSession();

    NutsTextBuilder builder();

    NutsText forBlank();

    NutsText toText(Object t);

    NutsTextPlain forPlain(String t);

    NutsTextList forList(NutsText... nodes);

    NutsTextList forList(Collection<NutsText> nodes);

    NutsTextStyled forStyled(String other, NutsTextStyles decorations);

    NutsTextStyled forStyled(NutsString other, NutsTextStyles decorations);

    NutsTextStyled forStyled(NutsText other, NutsTextStyles decorations);

    NutsTextStyled forStyled(String other, NutsTextStyle decorations);

    NutsTextStyled forStyled(NutsString other, NutsTextStyle decorations);

    NutsTextStyled forStyled(NutsText other, NutsTextStyle decorations);

    NutsTextCommand forCommand(NutsTerminalCommand command);

    NutsTextCode forCode(String lang, String text);

    NutsTextNumbering forNumbering();

    NutsTextNumbering forTitleNumberSequence(String pattern);

    NutsTextAnchor forAnchor(String anchorName);

    NutsTextLink forLink(NutsText value);

    NutsTextFormatTheme getTheme();

    NutsTextManager setTheme(NutsTextFormatTheme theme);
    NutsTextManager setTheme(String themeName);

    NutsCodeFormat getCodeFormat(String kind);

    NutsTextManager addCodeFormat(NutsCodeFormat format);

    NutsTextManager removeCodeFormat(NutsCodeFormat format);

    NutsCodeFormat[] getCodeFormats();

    NutsText parse(String t);

    NutsTextParser parser();

}
