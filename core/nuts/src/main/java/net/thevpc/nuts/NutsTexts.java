package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;

import java.io.PrintStream;
import java.util.Collection;

/**
 * @app.category Format
 */
public interface NutsTexts extends NutsComponent {
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

    NutsTextStyled ofStyled(String other, NutsTextStyles styles);

    NutsTextStyled ofStyled(NutsString other, NutsTextStyles styles);

    NutsTextStyled ofStyled(NutsText other, NutsTextStyles styles);

    /**
     * apply style to the given text or return it as is if no style is to be applied.
     * @param other text to apply style to.
     * @param styles styles to apply
     * @return the given text with the applied style
     */
    NutsText applyStyles(NutsText other, NutsTextStyles styles);

    NutsText applyStyles(NutsText other, NutsTextStyle ...styles);

    /**
     * apply style to the given text or return it as is if no style is to be applied.
     * @param other text to apply style to.
     * @param styles styles to apply
     * @return the given text with the applied style
     */
    NutsText applyStyles(NutsString other, NutsTextStyles styles);

    NutsText applyStyles(NutsString other, NutsTextStyle ... styles);

    NutsTextStyled ofStyled(String other, NutsTextStyle styles);

    NutsTextStyled ofStyled(NutsString other, NutsTextStyle styles);

    NutsTextStyled ofStyled(NutsText other, NutsTextStyle styles);

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
