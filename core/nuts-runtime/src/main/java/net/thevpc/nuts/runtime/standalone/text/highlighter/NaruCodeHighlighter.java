package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NaruCodeHighlighter implements NCodeHighlighter {

    @Override
    public String id() {
        return "naru";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if (s == null) {
            return NScorable.DEFAULT_SCORE;
        }
        switch (s) {
            case "naru":
            case "naru-script":
            case "text/x-naru":
            {
                return NScorable.DEFAULT_SCORE;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    @Override
    public NText tokenToText(String text, String nodeType, NTexts txt) {
        return txt.ofPlain(text);
    }

    @Override
    public NText stringToText(String text, NTexts txt) {
        List<NText> all = new ArrayList<>();
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (i > 0) {
                all.add(txt.ofPlain("\n"));
            }
            if (line.isEmpty()) {
                continue;
            }
            highlightLine(line, txt, all);
        }
        return txt.ofList(all.toArray(new NText[0]));
    }

    private void highlightLine(String line, NTexts txt, List<NText> all) {
        String trimmed = NStringUtils.trimLeft(line);
        int indent = line.length() - trimmed.length();
        if (indent > 0) {
            all.add(txt.ofPlain(line.substring(0, indent)));
        }

        if (trimmed.isEmpty()) {
            return;
        }

        char first = trimmed.charAt(0);

        // Comment line
        if (first == '#') {
            all.add(txt.ofStyled(trimmed, NTextStyle.comments()));
            return;
        }

        // Directive line: starts with /
        if (first == '/') {
            highlightDirectiveLine(trimmed, txt, all);
            return;
        }

        // Routine edit line: starts with a digit or '+'
        if (first == '+' || Character.isDigit(first)) {
            highlightRoutineLine(trimmed, txt, all);
            return;
        }

        // Plain prompt line: italicize
        all.add(txt.ofStyled(trimmed, NTextStyle.italic()));
    }

    /**
     * Directive: /command [args...]
     * Style: keyword for the command word, then args as a command-line
     */
    private void highlightDirectiveLine(String line, NTexts txt, List<NText> all) {
        StringReaderExt ar = new StringReaderExt(line);
        // leading slash
        ar.readChar(); // consume '/'
        // read command name (until whitespace)
        StringBuilder cmd = new StringBuilder("/");
        while (ar.hasNext() && !Character.isWhitespace(ar.peekChar())) {
            cmd.append(ar.readChar());
        }
        all.add(txt.ofStyled(cmd.toString(), NTextStyle.keyword()));

        // remainder: treat as command-line args (no $var expansion in directives)
        if (ar.hasNext()) {
            highlightArgs(ar, txt, all, false);
        }
    }

    /**
     * Routine edit line: [number|+] [args...]
     * The line number / '+' is styled as a number/operator,
     * then the rest is a command-line with full $var support.
     */
    private void highlightRoutineLine(String line, NTexts txt, List<NText> all) {
        StringReaderExt ar = new StringReaderExt(line);

        // read line number or '+'
        if (ar.peekChar() == '+') {
            all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.operator()));
        } else {
            StringBuilder num = new StringBuilder();
            while (ar.hasNext() && Character.isDigit(ar.peekChar())) {
                num.append(ar.readChar());
            }
            all.add(txt.ofStyled(num.toString(), NTextStyle.number()));
        }

        // space after number
        if (ar.hasNext()) {
            highlightArgs(ar, txt, all, true);
        }
    }

    /**
     * Tokenize a command-line argument list.
     * Options starting with '-' or '+' (followed by letter) are styled as separator/option.
     * String literals support $var / ${var} interpolation when allowVars=true.
     * '_' is styled as keyword when allowVars=true.
     */
    private void highlightArgs(StringReaderExt ar, NTexts txt, List<NText> all, boolean allowVars) {
        while (ar.hasNext()) {
            char c = ar.peekChar();

            if (Character.isWhitespace(c)) {
                all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(ar)));
                continue;
            }

            // inline comment
            if (c == '#') {
                StringBuilder rest = new StringBuilder();
                while (ar.hasNext()) rest.append(ar.readChar());
                all.add(txt.ofStyled(rest.toString(), NTextStyle.comments()));
                return;
            }

            // option flag: -x, --long, +x
            if ((c == '-' || (c == '+' && ar.hasNext(1) && Character.isLetter(ar.peekChar(1))))
                    && ar.hasNext(1) && (ar.peekChar(1) == '-' || Character.isLetter(ar.peekChar(1)))) {
                StringBuilder opt = new StringBuilder();
                while (ar.hasNext() && !Character.isWhitespace(ar.peekChar())) {
                    opt.append(ar.readChar());
                }
                all.add(txt.ofStyled(opt.toString(), NTextStyle.separator()));
                continue;
            }

            // double-quoted string
            if (c == '"') {
                if (allowVars) {
                    highlightDoubleQuotedString(ar, txt, all);
                } else {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readJSDoubleQuotesString(ar)));
                }
                continue;
            }

            // single-quoted string (no interpolation ever)
            if (c == '\'') {
                all.addAll(Arrays.asList(StringReaderExtUtils.readJSSimpleQuotes(ar)));
                continue;
            }

            // bare word / identifier
            StringBuilder word = new StringBuilder();
            while (ar.hasNext() && !Character.isWhitespace(ar.peekChar()) && ar.peekChar() != '"' && ar.peekChar() != '\'') {
                word.append(ar.readChar());
            }
            String w = word.toString();
            if (allowVars && w.equals("_")) {
                all.add(txt.ofStyled(w, NTextStyle.keyword()));
            } else {
                all.add(txt.ofPlain(w));
            }
        }
    }

    /**
     * Double-quoted string with $var / ${var} interpolation.
     * The surrounding quotes and literal text are styled as string,
     * variable references are styled as keyword.
     */
    private void highlightDoubleQuotedString(StringReaderExt ar, NTexts txt, List<NText> all) {
        StringBuilder current = new StringBuilder();
        current.append(ar.readChar()); // opening "

        while (ar.hasNext()) {
            char c = ar.peekChar();

            if (c == '\\') {
                current.append(ar.readChar());
                if (ar.hasNext()) current.append(ar.readChar());
                continue;
            }

            if (c == '"') {
                current.append(ar.readChar());
                break;
            }

            if (c == '$') {
                // flush current string segment
                if (current.length() > 0) {
                    all.add(txt.ofStyled(current.toString(), NTextStyle.string()));
                    current = new StringBuilder();
                }
                // ${var} or $var
                if (ar.hasNext(1) && ar.peekChar(1) == '{') {
                    StringBuilder varRef = new StringBuilder();
                    varRef.append(ar.readChar()); // $
                    varRef.append(ar.readChar()); // {
                    while (ar.hasNext() && ar.peekChar() != '}') {
                        varRef.append(ar.readChar());
                    }
                    if (ar.hasNext()) varRef.append(ar.readChar()); // }
                    all.add(txt.ofStyled(varRef.toString(), NTextStyle.keyword()));
                } else {
                    StringBuilder varRef = new StringBuilder();
                    varRef.append(ar.readChar()); // $
                    while (ar.hasNext() && (Character.isLetterOrDigit(ar.peekChar()) || ar.peekChar() == '_')) {
                        varRef.append(ar.readChar());
                    }
                    all.add(txt.ofStyled(varRef.toString(), NTextStyle.keyword()));
                }
                continue;
            }

            current.append(ar.readChar());
        }

        if (current.length() > 0) {
            all.add(txt.ofStyled(current.toString(), NTextStyle.string()));
        }
    }
}