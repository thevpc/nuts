package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.text.*;

import java.util.*;

public class CSharpCodeHighlighter implements NCodeHighlighter {

    private Set<String> reservedWords = new HashSet<>();

    public CSharpCodeHighlighter() {
        reservedWords.addAll(NCodeHighlighterHelper.loadNames("csharp.kw1",getClass()));
    }

    @Override
    public String id() {
        return "csharp";
    }

    @Override
    public NText tokenToText(String text, String nodeType) {
        return NText.ofPlain(text);
    }


    @NScore(fixed = NScorable.DEFAULT_SCORE)
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if(s==null){
            return NScorable.DEFAULT_SCORE;
        }
        switch (s){
            case "cs":
            case "csx":
            case "csharp":
            case "text/csharp":
            case "text/x-csharp":
            case "application/x-csharp":
            {
                return NScorable.DEFAULT_SCORE;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    @Override
    public NText stringToText(String text) {
        List<NText> all = new ArrayList<>();
        StringReaderExt ar = new StringReaderExt(text);
        while (ar.hasNext()) {
            switch (ar.peekChar()) {
                case '{':
                case '}':
                case '(':
                case ')':
                case '[':
                case ']':
                case '=':
                case '+':
                case '*':
                case '%':
                case ':':
                case '?':
                case '<':
                case '>':
                case '!':
                case ';': {
                    all.add(NText.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    break;
                }
                case '\'': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readJSSimpleQuotes(ar)));
                    break;
                }
                case '"': {
                    all.addAll(readStringBody(ar, "", false, false));
                    break;
                }
                case '@':
                case '$': {
                    if (isCSharpStringPrefix(ar)) {
                        all.addAll(readPrefixedString(ar));
                    } else {
                        all.add(NText.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    }
                    break;
                }
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readNumber(ar)));
                    break;
                }
                case '.':
                case '-': {
                    NText[] d = StringReaderExtUtils.readNumber(ar);
                    if (d != null) {
                        all.addAll(Arrays.asList(d));
                    } else {
                        all.add(NText.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    }
                    break;
                }
                case '/': {
                    if (ar.peekChars("//")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashSlashComments(ar)));
                    } else if (ar.peekChars("/*")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashStarComments(ar)));
                    } else {
                        all.add(NText.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    }
                    break;
                }
                default: {
                    if (Character.isWhitespace(ar.peekChar())) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(ar)));
                    } else {
                        NText[] d = StringReaderExtUtils.readJSIdentifier(ar);
                        if (d != null) {
                            if (d.length == 1 && d[0].type() == NTextType.PLAIN) {
                                String txt2 = ((NTextPlain) d[0]).value();
                                if (reservedWords.contains(txt2)) {
                                    d[0] = NText.ofStyled(d[0], NTextStyle.keyword());
                                }
                            }
                            all.addAll(Arrays.asList(d));
                        } else {
                            all.add(NText.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                        }
                    }
                    break;
                }
            }
        }
        return NText.ofList(all.toArray(new NText[0]));
    }

    private boolean isCSharpStringPrefix(StringReaderExt ar) {
        int i = 0;
        while (ar.hasNext(i)) {
            char c = ar.peekChar(i);
            if (c == '@' || c == '$') {
                i++;
                continue;
            }
            return c == '"';
        }
        return false;
    }

    private List<NText> readPrefixedString(StringReaderExt ar) {
        StringBuilder prefix = new StringBuilder();
        while (ar.hasNext() && (ar.peekChar() == '@' || ar.peekChar() == '$')) {
            prefix.append(ar.readChar());
        }
        boolean verbatim = prefix.indexOf("@") >= 0;
        boolean interpolated = prefix.indexOf("$") >= 0;
        return readStringBody(ar, prefix.toString(), verbatim, interpolated);
    }

    /**
     * Reads a C# string literal, including verbatim ({@code @"..."}) and
     * interpolated ({@code $"..."}) forms. Interpolation holes are highlighted
     * as separators without recursively parsing the embedded expression.
     */
    private List<NText> readStringBody(StringReaderExt ar, String prefix, boolean verbatim, boolean interpolated) {
        List<NText> all = new ArrayList<>();
        if (!prefix.isEmpty()) {
            all.add(NText.ofStyled(prefix, NTextStyle.annotation()));
        }
        StringBuilder sb = new StringBuilder();
        boolean triple = ar.peekChars(3).equals("\"\"\"");
        if (triple) {
            sb.append(ar.nextChars(3));
            while (ar.hasNext()) {
                if (ar.peekChars(3).equals("\"\"\"")) {
                    sb.append(ar.nextChars(3));
                    break;
                }
                if (interpolated && ar.peekChar() == '{') {
                    if (ar.hasNext(1) && ar.peekChar(1) == '{') {
                        sb.append(ar.readChar());
                        sb.append(ar.readChar());
                        continue;
                    }
                    flushString(sb, all);
                    all.addAll(readInterpolationHole(ar));
                    continue;
                }
                sb.append(ar.readChar());
            }
            flushString(sb, all);
            return all;
        }

        sb.append(ar.readChar()); // opening quote
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (verbatim) {
                if (c == '"') {
                    sb.append(ar.readChar());
                    if (ar.hasNext() && ar.peekChar() == '"') {
                        sb.append(ar.readChar()); // escaped ""
                        continue;
                    }
                    break;
                }
            } else {
                if (c == '\\') {
                    flushString(sb, all);
                    sb.append(ar.readChar());
                    if (ar.hasNext()) {
                        sb.append(ar.readChar());
                    }
                    all.add(NText.ofStyled(sb.toString(), NTextStyle.separator()));
                    sb.setLength(0);
                    continue;
                }
                if (c == '"') {
                    sb.append(ar.readChar());
                    break;
                }
                if (c == '\r' || c == '\n') {
                    break;
                }
            }
            if (interpolated && c == '{') {
                if (ar.hasNext(1) && ar.peekChar(1) == '{') {
                    sb.append(ar.readChar());
                    sb.append(ar.readChar());
                    continue;
                }
                flushString(sb, all);
                all.addAll(readInterpolationHole(ar));
                continue;
            }
            sb.append(ar.readChar());
        }
        flushString(sb, all);
        return all;
    }

    private List<NText> readInterpolationHole(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // {
        int depth = 1;
        while (ar.hasNext() && depth > 0) {
            char c = ar.readChar();
            sb.append(c);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
            }
        }
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.separator()));
    }

    private void flushString(StringBuilder sb, List<NText> all) {
        if (sb.length() > 0) {
            all.add(NText.ofStyled(sb.toString(), NTextStyle.string()));
            sb.setLength(0);
        }
    }
}
