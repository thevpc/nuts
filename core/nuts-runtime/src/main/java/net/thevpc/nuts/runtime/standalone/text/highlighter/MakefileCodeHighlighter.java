package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MakefileCodeHighlighter implements NCodeHighlighter {

    @Override
    public String id() {
        return "makefile";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        return NCodeHighlighterHelper.score(context,
                "make", "makefile", "mk", "gnumake",
                "text/x-makefile", "text/makefile", "application/x-makefile");
    }

    @Override
    public NText tokenToText(String text, String nodeType) {
        return NText.ofPlain(text);
    }

    @Override
    public NText stringToText(String text) {
        List<NText> all = new ArrayList<>();
        StringReaderExt ar = new StringReaderExt(text);
        while (ar.hasNext()) {
            StringBuilder line = new StringBuilder();
            while (ar.hasNext() && ar.peekChar() != '\n' && ar.peekChar() != '\r') {
                line.append(ar.readChar());
            }
            highlightLine(line.toString(), all);
            if (ar.hasNext()) {
                char c = ar.readChar();
                if (c == '\r' && ar.hasNext() && ar.peekChar() == '\n') {
                    all.add(NText.ofPlain("\r\n"));
                    ar.readChar();
                } else {
                    all.add(NText.ofPlain(String.valueOf(c)));
                }
            }
        }
        return NText.ofList(all.toArray(new NText[0]));
    }

    private void highlightLine(String line, List<NText> all) {
        int i = 0;
        while (i < line.length() && Character.isWhitespace(line.charAt(i))) {
            i++;
        }
        if (i > 0) {
            all.add(NText.ofPlain(line.substring(0, i)));
        }
        if (i >= line.length()) {
            return;
        }
        if (line.charAt(i) == '#') {
            all.add(NText.ofStyled(line.substring(i), NTextStyle.comments()));
            return;
        }
        // Rule or assignment? Ignore ':' / '=' inside $(...) and ${...}
        int depth = 0;
        int colon = -1;
        int equal = -1;
        for (int k = i; k < line.length(); k++) {
            char c = line.charAt(k);
            if ((c == '(' || c == '{') && k > 0 && line.charAt(k - 1) == '$') {
                depth++;
            } else if ((c == ')' || c == '}') && depth > 0) {
                depth--;
            } else if (depth == 0) {
                if (c == ':' && colon < 0) {
                    colon = k;
                } else if (c == '=' && equal < 0) {
                    equal = k;
                }
            }
        }
        boolean assignment = equal >= 0 && (colon < 0 || equal <= colon || equal == colon + 1);
        if (assignment) {
            int opStart = (colon == equal - 1) ? colon : equal;
            all.add(NText.ofStyled(line.substring(i, opStart).trim(), NTextStyle.option()));
            String between = line.substring(i, opStart);
            if (between.length() > between.trim().length()) {
                // preserve trailing spaces after the variable name
                String trimmed = between.trim();
                all.remove(all.size() - 1);
                all.add(NText.ofStyled(trimmed, NTextStyle.option()));
                all.add(NText.ofPlain(between.substring(trimmed.length())));
            }
            all.add(NText.ofStyled(line.substring(opStart, equal + 1), NTextStyle.operator()));
            highlightValue(line.substring(equal + 1), all);
            return;
        }
        if (colon >= 0) {
            all.add(NText.ofStyled(line.substring(i, colon).trim(), NTextStyle.keyword()));
            String between = line.substring(i, colon);
            if (between.length() > between.trim().length()) {
                String trimmed = between.trim();
                all.remove(all.size() - 1);
                all.add(NText.ofStyled(trimmed, NTextStyle.keyword()));
                all.add(NText.ofPlain(between.substring(trimmed.length())));
            }
            all.add(NText.ofStyled(":", NTextStyle.separator()));
            highlightValue(line.substring(colon + 1), all);
            return;
        }
        highlightValue(line.substring(i), all);
    }

    private void highlightValue(String text, List<NText> all) {
        StringReaderExt ar = new StringReaderExt(text);
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == '#') {
                all.add(NText.ofStyled(ar.nextChars(text.length()), NTextStyle.comments()));
            } else if (c == '$') {
                all.addAll(readVariable(ar));
            } else if (c == '"' || c == '\'') {
                all.addAll(readString(ar, c));
            } else if (c == ':' || c == '=' || c == ';' || c == '@' || c == '\\') {
                all.add(NText.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
            } else if (Character.isWhitespace(c)) {
                all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(ar)));
            } else {
                all.add(NText.ofPlain(String.valueOf(ar.readChar())));
            }
        }
    }

    private List<NText> readVariable(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // $
        if (ar.hasNext() && (ar.peekChar() == '(' || ar.peekChar() == '{')) {
            char open = ar.readChar();
            char close = open == '(' ? ')' : '}';
            sb.append(open);
            int depth = 1;
            while (ar.hasNext() && depth > 0) {
                char c = ar.readChar();
                sb.append(c);
                if (c == open) {
                    depth++;
                } else if (c == close) {
                    depth--;
                }
            }
        } else if (ar.hasNext()) {
            // automatic variables: $@ $< $^ $? $*
            sb.append(ar.readChar());
        }
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.variable()));
    }

    private List<NText> readString(StringReaderExt ar, char quote) {
        List<NText> all = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar());
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == '\\') {
                sb.append(ar.readChar());
                if (ar.hasNext()) {
                    sb.append(ar.readChar());
                }
            } else if (c == quote) {
                sb.append(ar.readChar());
                break;
            } else if (c == '$') {
                if (sb.length() > 0) {
                    all.add(NText.ofStyled(sb.toString(), NTextStyle.string()));
                    sb.setLength(0);
                }
                all.addAll(readVariable(ar));
            } else {
                sb.append(ar.readChar());
            }
        }
        if (sb.length() > 0) {
            all.add(NText.ofStyled(sb.toString(), NTextStyle.string()));
        }
        return all;
    }
}
