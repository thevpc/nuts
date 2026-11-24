package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorableContext;

public class JavaCodeHighlighter extends AbstractCodeHighlighter {

    public JavaCodeHighlighter() {
        super("java");
        keywords.addAll(NCodeHighlighterHelper.loadNames("java.kw1", getClass()));
        booleanLiterals.addAll(java.util.Arrays.asList("true", "false"));
        nullLiterals.add("null");
        lineComments.add("//");
        blockCommentOpen = "/*";
        blockCommentClose = "*/";
        stringQuotes.add('\'');
        charLiterals = true;
        tripleQuotes = true;
        annotations = true;
        numberSuffixChars = "uUlLfFdD";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        return NCodeHighlighterHelper.score(context,
                "java", "jav", "text/x-java", "text/x-java-source", "text/java", "application/x-java");
    }
}
