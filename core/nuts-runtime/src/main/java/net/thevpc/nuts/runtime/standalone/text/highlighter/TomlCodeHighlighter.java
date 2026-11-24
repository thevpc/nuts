package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TomlCodeHighlighter extends AbstractCodeHighlighter {

    public TomlCodeHighlighter() {
        super("toml");
        booleanLiterals.addAll(Arrays.asList("true", "false"));
        lineComments.add("#");
        stringQuotes.add('\'');
        tripleQuotes = true;
        numberSuffixChars = "";
        separatorChars = "[]{},.";
        operatorChars = "=+:";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        return NCodeHighlighterHelper.score(context,
                "toml", "text/x-toml", "text/toml", "application/toml", "application/x-toml");
    }

    @Override
    protected boolean handleSpecial(StringReaderExt ar, List<NText> all) {
        if (ar.peekChar() == '[') {
            String header = readTableHeader(ar);
            if (header != null) {
                all.add(NText.ofStyled(header, NTextStyle.annotation()));
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<NText> readString(StringReaderExt ar) {
        if (ar.peekChar() == '\'' && !ar.peekChars(3).equals("'''")) {
            // TOML literal strings do not process escape sequences
            return readQuotedString(ar, '\'', false);
        }
        return super.readString(ar);
    }

    @Override
    protected boolean isNumberStart(StringReaderExt ar) {
        if (super.isNumberStart(ar)) {
            return true;
        }
        // TOML local dates/time: 1979-05-27, 07:32:00
        char c = ar.peekChar();
        return Character.isDigit(c) && ar.peekChars(5).matches("\\d{4}-.*");
    }

    @Override
    protected List<NText> readNumber(StringReaderExt ar) {
        // date-time keeps its '-' ':' 'T' 'Z' '.' characters in one token
        if (Character.isDigit(ar.peekChar()) && ar.peekChars(5).matches("\\d{4}-.*")) {
            StringBuilder sb = new StringBuilder();
            while (ar.hasNext() && isDateTimeChar(ar.peekChar())) {
                sb.append(ar.readChar());
            }
            return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.number()));
        }
        return super.readNumber(ar);
    }

    private boolean isDateTimeChar(char c) {
        return Character.isDigit(c) || c == '-' || c == ':' || c == 'T' || c == 't'
                || c == 'Z' || c == 'z' || c == '+' || c == '.';
    }

    private String readTableHeader(StringReaderExt ar) {
        int i = 1;
        boolean doubleBracket = false;
        if (ar.hasNext(i) && ar.peekChar(i) == '[') {
            doubleBracket = true;
            i++;
        }
        if (!ar.hasNext(i) || !isIdentifierStart(ar.peekChar(i))) {
            return null;
        }
        int start = i;
        while (ar.hasNext(i) && isTableNameChar(ar.peekChar(i))) {
            i++;
        }
        if (i == start || !ar.hasNext(i) || ar.peekChar(i) != ']') {
            return null;
        }
        i++;
        if (doubleBracket) {
            if (!ar.hasNext(i) || ar.peekChar(i) != ']') {
                return null;
            }
            i++;
        }
        return ar.nextChars(i);
    }

    private boolean isTableNameChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.' || c == '"' || c == '\'';
    }
}
