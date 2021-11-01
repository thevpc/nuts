package net.thevpc.nuts;

/**
 * @app.category Format
 */
public interface NutsTextStyled extends NutsText {
    static NutsTextStyled of(String str, NutsTextStyle style, NutsSession session) {
        return NutsTexts.of(session).ofStyled(str, style);
    }

    static NutsTextStyled of(NutsString str, NutsTextStyle style, NutsSession session) {
        return NutsTexts.of(session).ofStyled(str, style);
    }

    static NutsTextStyled of(NutsText str, NutsTextStyle style, NutsSession session) {
        return NutsTexts.of(session).ofStyled(str, style);
    }

    static NutsTextStyled of(String str, NutsTextStyles styles, NutsSession session) {
        return NutsTexts.of(session).ofStyled(str, styles);
    }

    static NutsTextStyled of(NutsString str, NutsTextStyles styles, NutsSession session) {
        return NutsTexts.of(session).ofStyled(str, styles);
    }

    static NutsTextStyled of(NutsText str, NutsTextStyles styles, NutsSession session) {
        return NutsTexts.of(session).ofStyled(str, styles);
    }

    NutsText getChild();

    NutsTextStyles getStyles();
}
