package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;

import java.util.Collection;

/**
 * @app.category Format
 */
public interface NutsTexts extends NutsComponent<Object> {
    static NutsTexts of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsTexts.class,true,null);
    }

    NutsSession getSession();

    NutsTexts setSession(NutsSession session);

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

    NutsTexts setTheme(NutsTextFormatTheme theme);

    NutsTexts setTheme(String themeName);

    NutsCodeHighlighter getCodeHighlighter(String kind);

    NutsTexts addCodeHighlighter(NutsCodeHighlighter format);

    NutsTexts removeCodeHighlighter(String id);

    NutsCodeHighlighter[] getCodeHighlighters();

    NutsText parse(String t);

    NutsTextParser parser();

}
