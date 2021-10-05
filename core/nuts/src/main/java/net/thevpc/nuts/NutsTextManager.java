package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

import java.util.Collection;

/**
 * @app.category Format
 */
public interface NutsTextManager {
    static NutsTextManager of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.text();
    }

    NutsSession getSession();

    NutsTextManager setSession(NutsSession session);

    NutsTextBuilder builder();

    NutsText ofBlank();

    NutsText toText(Object t);

    NutsTextPlain ofPlain(String t);

    NutsTextList ofList(NutsText... nodes);

    NutsTextList ofList(Collection<NutsText> nodes);

    NutsTextStyled ofStyled(String other, NutsTextStyles decorations);

    NutsTextStyled ofStyled(NutsString other, NutsTextStyles decorations);

    NutsTextStyled ofStyled(NutsText other, NutsTextStyles decorations);

    NutsTextStyled ofStyled(String other, NutsTextStyle decorations);

    NutsTextStyled ofStyled(NutsString other, NutsTextStyle decorations);

    NutsTextStyled ofStyled(NutsText other, NutsTextStyle decorations);

    NutsTextCommand ofCommand(NutsTerminalCommand command);

    NutsTextCode ofCode(String lang, String text);

    NutsTextNumbering ofNumbering();

    NutsTextNumbering ofTitleNumberSequence(String pattern);

    NutsTextAnchor ofAnchor(String anchorName);

    NutsTextLink ofLink(NutsText value);

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
