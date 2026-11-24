package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorableContext;

import java.util.Arrays;

public class CssCodeHighlighter extends AbstractCodeHighlighter {

    public CssCodeHighlighter() {
        super("css");
        types.addAll(Arrays.asList(
                "align-content", "align-items", "align-self", "animation", "background", "background-color",
                "background-image", "background-position", "background-size", "border", "border-bottom",
                "border-color", "border-left", "border-radius", "border-right", "border-top", "bottom",
                "box-shadow", "box-sizing", "color", "column-gap", "content", "cursor", "display", "fill",
                "flex", "flex-basis", "flex-direction", "flex-grow", "flex-shrink", "flex-wrap", "float",
                "font", "font-family", "font-size", "font-style", "font-weight", "gap", "grid",
                "grid-area", "grid-column", "grid-row", "grid-template-columns", "grid-template-rows",
                "height", "justify-content", "left", "letter-spacing", "line-height", "margin",
                "margin-bottom", "margin-left", "margin-right", "margin-top", "max-height", "max-width",
                "min-height", "min-width", "opacity", "outline", "overflow", "padding", "padding-bottom",
                "padding-left", "padding-right", "padding-top", "position", "right", "stroke", "text-align",
                "text-decoration", "text-transform", "top", "transform", "transition", "vertical-align",
                "visibility", "white-space", "width", "z-index"
        ));
        lineComments.add("//");
        blockCommentOpen = "/*";
        blockCommentClose = "*/";
        stringQuotes.add('\'');
        annotations = true;
        numberSuffixChars = "";
        separatorChars = "()[]{},;:";
        operatorChars = "+-*/%=<>!&|^~?#";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        return NCodeHighlighterHelper.score(context,
                "css", "scss", "sass", "less",
                "text/css", "text/x-scss", "text/x-sass", "text/x-less", "application/x-css");
    }
}
