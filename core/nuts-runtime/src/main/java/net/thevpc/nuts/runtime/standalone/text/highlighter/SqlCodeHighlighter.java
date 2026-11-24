package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.text.NTextStyles;

public class SqlCodeHighlighter extends AbstractCodeHighlighter {

    public SqlCodeHighlighter() {
        super("sql");
        for (String s : NCodeHighlighterHelper.loadNames("sql.kw1", getClass())) {
            keywords.add(s.toLowerCase());
        }
        for (String s : NCodeHighlighterHelper.loadNames("sql.kw2", getClass())) {
            types.add(s.toLowerCase());
        }
        booleanLiterals.addAll(java.util.Arrays.asList("true", "false"));
        nullLiterals.add("null");
        lineComments.add("--");
        lineComments.add("#");
        blockCommentOpen = "/*";
        blockCommentClose = "*/";
        stringQuotes.add('\'');
        stringQuotes.add('`');
        numberSuffixChars = "";
        separatorChars = "()[]{},.;";
        operatorChars = "+-*/%=<>!&|^~?:";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        return NCodeHighlighterHelper.score(context,
                "sql", "text/sql", "text/x-sql", "application/sql", "application/x-sql");
    }

    @Override
    protected NTextStyles styleForWord(String word) {
        // SQL keywords are case insensitive
        return super.styleForWord(word.toLowerCase());
    }
}
