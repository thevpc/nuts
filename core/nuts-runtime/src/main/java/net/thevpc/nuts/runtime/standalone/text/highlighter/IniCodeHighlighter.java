package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * INI / Java properties highlighter: sections, keys, values and the usual
 * comment prefixes.
 */
public class IniCodeHighlighter implements NCodeHighlighter {

    @Override
    public String id() {
        return "ini";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        return NCodeHighlighterHelper.score(context,
                "ini", "cfg", "conf", "config", "properties", "props", "java-properties",
                "text/x-ini", "text/ini", "text/x-properties", "application/x-properties");
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
        char first = line.charAt(i);
        if (first == '#' || first == ';' || first == '!') {
            all.add(NText.ofStyled(line.substring(i), NTextStyle.comments()));
            return;
        }
        if (first == '[') {
            int close = line.indexOf(']', i + 1);
            if (close >= 0) {
                all.add(NText.ofStyled(line.substring(i, close + 1), NTextStyle.annotation()));
                if (close + 1 < line.length()) {
                    highlightValue(line.substring(close + 1), all);
                }
                return;
            }
        }
        int sep = findSeparator(line, i);
        if (sep < 0) {
            all.add(NText.ofPlain(line.substring(i)));
            return;
        }
        String rawKey = line.substring(i, sep);
        int keyTrimEnd = rawKey.length();
        while (keyTrimEnd > 0 && Character.isWhitespace(rawKey.charAt(keyTrimEnd - 1))) {
            keyTrimEnd--;
        }
        all.add(NText.ofStyled(rawKey.substring(0, keyTrimEnd), NTextStyle.option()));
        if (keyTrimEnd < rawKey.length()) {
            all.add(NText.ofPlain(rawKey.substring(keyTrimEnd)));
        }
        all.add(NText.ofStyled(String.valueOf(line.charAt(sep)), NTextStyle.separator()));
        highlightValue(line.substring(sep + 1), all);
    }

    private int findSeparator(String line, int from) {
        boolean inSingle = false;
        boolean inDouble = false;
        for (int i = from; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
            } else if (c == '"' && !inSingle) {
                inDouble = !inDouble;
            } else if (!inSingle && !inDouble && (c == '=' || c == ':')) {
                return i;
            }
        }
        return -1;
    }

    private void highlightValue(String value, List<NText> all) {
        if (value.isEmpty()) {
            return;
        }
        int i = 0;
        while (i < value.length() && Character.isWhitespace(value.charAt(i))) {
            i++;
        }
        if (i > 0) {
            all.add(NText.ofPlain(value.substring(0, i)));
        }
        if (i >= value.length()) {
            return;
        }
        char first = value.charAt(i);
        if (first == '\'' || first == '"') {
            int end = value.indexOf(first, i + 1);
            if (end < 0) {
                end = value.length() - 1;
            }
            all.add(NText.ofStyled(value.substring(i, end + 1), NTextStyle.string()));
            if (end + 1 < value.length()) {
                all.add(NText.ofPlain(value.substring(end + 1)));
            }
            return;
        }
        String rest = value.substring(i);
        String trimmed = rest.trim();
        if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)
                || "yes".equalsIgnoreCase(trimmed) || "no".equalsIgnoreCase(trimmed)) {
            all.add(NText.ofStyled(rest, NTextStyle.bool()));
        } else if (trimmed.matches("[-+]?\\d+([.][\\d]+)?")) {
            all.add(NText.ofStyled(rest, NTextStyle.number()));
        } else {
            all.add(NText.ofPlain(rest));
        }
    }
}
