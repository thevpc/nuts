package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.util.List;

public class CppCodeHighlighter extends AbstractCodeHighlighter {

    public CppCodeHighlighter() {
        super("cpp");
        keywords.addAll(NCodeHighlighterHelper.loadNames("cpp.kw1", getClass()));
        booleanLiterals.addAll(java.util.Arrays.asList("true", "false"));
        nullLiterals.add("nullptr");
        lineComments.add("//");
        blockCommentOpen = "/*";
        blockCommentClose = "*/";
        stringQuotes.add('\'');
        charLiterals = true;
        numberSuffixChars = "uUlLfFdD";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        return NCodeHighlighterHelper.score(context,
                "c", "h", "hpp", "hxx", "cc", "cpp", "cxx", "c++",
                "text/x-c", "text/x-c++", "text/x-csrc", "text/x-c++src", "text/x-cppsrc");
    }

    @Override
    protected boolean handleSpecial(StringReaderExt ar, List<NText> all) {
        if (ar.peekChar() == '#') {
            StringBuilder sb = new StringBuilder();
            sb.append(ar.readChar());
            while (ar.hasNext() && Character.isWhitespace(ar.peekChar())) {
                sb.append(ar.readChar());
            }
            while (ar.hasNext() && Character.isJavaIdentifierPart(ar.peekChar())) {
                sb.append(ar.readChar());
            }
            all.add(NText.ofStyled(sb.toString(), NTextStyle.annotation()));
            return true;
        }
        return false;
    }
}
