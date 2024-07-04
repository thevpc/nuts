package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TsonCodeHighlighter implements NCodeHighlighter {
    private NWorkspace ws;

    public TsonCodeHighlighter(NWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String getId() {
        return "tson";
    }

    @Override
    public NText tokenToText(String text, String nodeType, NTexts txt, NSession session) {
        return txt.ofPlain(text);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        String s = context.getConstraints();
        if (s == null) {
            return NConstants.Support.DEFAULT_SUPPORT;
        }
        switch (s) {
            case "tson":
            case "application/tson":
            case "text/tson": {
                return NConstants.Support.DEFAULT_SUPPORT;
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }

    @Override
    public NText stringToText(String text, NTexts txt, NSession session) {
        List<NText> all = new ArrayList<>();
        StringReaderExt ar = new StringReaderExt(text);
        while (ar.hasNext()) {
            switch (ar.peekChar()) {
                case '{':
                case '}':
                case '[':
                case ']':
                case '(':
                case ')':
                case '@':
                case '^':
                case ':': {
                    all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    break;
                }
                case '\'':
                case '"':
                case '`': {
                    all.addAll(Arrays.asList(parseRawString(session, txt, ar)));
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
                    all.addAll(Arrays.asList(readNumber(session, txt, ar)));
                    break;
                }
                case '.':
                case '-':
                case '+': {
                    NText[] d = readNumber(session, txt, ar);
                    if (d != null) {
                        all.addAll(Arrays.asList(d));
                    } else {
                        all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    }
                    break;
                }
                case '/': {
                    if (ar.peekChars("//")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashSlashComments(session, ar)));
                    } else if (ar.peekChars("/*")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashStarComments(session, ar)));
                    } else {
                        all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    }
                    break;
                }
                default: {
                    if (Character.isWhitespace(ar.peekChar())) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(session, ar)));
                    } else {
                        NText[] d = readIdentifier(session, txt, ar);
                        if (d != null) {
                            if (d.length == 1 && d[0].getType() == NTextType.PLAIN) {
                                String txt2 = ((NTextPlain) d[0]).getText();
                                NTextStyle t = resolveTokenStyle(txt2);
                                if (t != null) {
                                    d[0] = txt.ofStyled(d[0], t);
                                }
                            }
                            all.addAll(Arrays.asList(d));
                        } else {
                            all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                        }
                    }
                    break;
                }
            }
        }
        return txt.ofList(all.toArray(new NText[0]));
    }


    public static String readNumberStr(NSession session, StringReaderExt ar) {
        ar.mark();
        StringBuilder sb = new StringBuilder();
        if (ar.readString("0u")) {
            sb.append("0u");
            char c = ar.readChar();
            int u = 1;
            switch (c) {
                case '1':
                case '2':
                case '4':
                case '8': {
                    u = Integer.parseInt(String.valueOf(c));
                    sb.append(c);
                    break;
                }
                default: {
                    ar.reset();
                    ar.readChar();
                    return "0";
                }
            }
            c = ar.readChar();
            int radix = 10;
            switch (c) {
                case '_': {
                    sb.append(c);
                    sb.append(ar.readWhile((cc, p) -> (cc >= '0' && cc <= '9') || cc == '_'));
                    break;
                }
                case 'x': {
                    radix = 16;
                    sb.append(c);
                    sb.append(ar.readWhile((cc, p) -> (cc >= '0' && cc <= '9') || (cc >= 'a' && cc <= 'f') || (cc >= 'A' && cc <= 'F') || cc == '_'));
                    break;
                }
                case 'o': {
                    radix = 8;
                    sb.append(c);
                    sb.append(ar.readWhile((cc, p) -> (cc >= '0' && cc <= '7') || cc == '_'));
                    break;
                }
                case 'b': {
                    radix = 2;
                    sb.append(c);
                    sb.append(ar.readWhile((cc, p) -> (cc >= '0' && cc <= '1') || cc == '_'));
                    break;
                }
                default: {
                    if (ar.readString("Max")) {
                        sb.append("Max");
                    } else if (ar.readString("Min")) {
                        sb.append("Min");
                    } else if (ar.readString("Inf")) {
                        sb.append("Inf");
                        return sb.toString();
                    } else if (ar.readString("NaN")) {
                        sb.append("NaN");
                    } else {
                        ar.reset();
                        ar.readChar();
                        return "0";
                    }
                }
            }
        } else if (ar.readString("0x")) {
            sb.append("0x");
            sb.append(ar.readWhile((cc, p) -> (cc >= '0' && cc <= '9') || (cc >= 'a' && cc <= 'f') || (cc >= 'A' && cc <= 'F') || cc == '_'));
        } else if (ar.readString("0b")) {
            sb.append("0b");
            sb.append(ar.readWhile((cc, p) -> (cc >= '0' && cc <= '1') || cc == '_'));
        } else if (ar.readString("0")) {
            sb.append("0");
            sb.append(ar.readWhile((cc, p) -> (cc >= '0' && cc <= '7') || cc == '_'));
        } else {
            sb.append(ar.readWhile((cc, p) -> (cc >= '0' && cc <= '9') || cc == '_' || cc == '-' || cc == '+' || cc == 'e' || cc == 'E' || cc == 'i' || cc == '^' || cc == '.'));
        }

        if (ar.readString("LL")) {
            sb.append("LL");
        }else if (ar.readString("L")) {
            sb.append("L");
        }else if(ar.readString("f")){
            sb.append("f");
        }else if(ar.readString("F")){
            sb.append("F");
        }
        if (ar.readString("%")) {
            sb.append("%");
            sb.append(ar.readWhile((cc, p) -> Character.isLetter(cc)));
        } else if (ar.readString("_")) {
            sb.append("_");
            sb.append(ar.readWhile((cc, p) -> Character.isLetter(cc)));
        }else{
            if(sb.toString().matches(".*_[a-zA-Z]+")){
                sb.append(ar.readWhile((cc, p) -> Character.isLetter(cc)));
            }
        }
        return sb.toString();
    }

    public static NText[] readNumber(NSession session, NTexts txt, StringReaderExt ar) {
        String s = readNumberStr(session, ar);
        if (s.length() > 0) {
            return new NText[]{
                    txt.ofStyled(s, NTextStyle.number())
            };
        }
        return new NText[0];
    }

    protected NTextStyle resolveTokenStyle(String txt2) {
        switch (txt2) {
            case "true":
            case "false": {
                return NTextStyle.bool();
            }
        }
        return null;
    }

    protected boolean isIdentifierStart(char c) {
        switch (c) {
            case '$': {
                return true;
            }
        }
        return Character.isJavaIdentifierStart(c);
    }

    protected boolean isJavaIdentifierPart(char c) {
        switch (c) {
            case '$':
            case '-': {
                return true;
            }
        }
        return Character.isJavaIdentifierStart(c);
    }

    private NText[] readIdentifier(NSession session, NTexts txt, StringReaderExt ar) {
        List<NText> all = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if (!ar.hasNext() || !isIdentifierStart(ar.peekChar())) {
            return null;
        }
        sb.append(ar.readChar());
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == '-') {
                String cc = ar.peekChars(2);
                if (cc.length() > 1 && isJavaIdentifierPart(cc.charAt(0))) {
                    sb.append(ar.readChar());
                } else {
                    break;
                }
            }
            if (isJavaIdentifierPart(c)) {
                sb.append(ar.readChar());
            } else {
                break;
            }
        }
        all.add(txt.ofPlain(sb.toString()));
        return all.toArray(new NText[0]);
    }

    public NText[] parseRawString(NSession session, NTexts txt, StringReaderExt chars) {
        List<NText> all = new ArrayList<>();
        for (String border : new String[]{
                "\"\"\"",
                "'''",
                "```",
                "\"",
                "'",
                "`",
        }) {
            if (chars.readString(border)) {
                all.add(txt.ofStyled(border, NTextStyle.string()));
                StringBuilder sb = new StringBuilder();
                while (chars.hasNext()) {
                    if (chars.readString("\\" + border)) {
                        if (sb.length() > 0) {
                            all.add(txt.ofStyled(sb.toString(), NTextStyle.string()));
                            sb.setLength(0);
                        }
                        all.add(txt.ofStyled("\\" + border, NTextStyle.separator()));
                    } else if (chars.readString(border)) {
                        if (sb.length() > 0) {
                            all.add(txt.ofStyled(sb.toString(), NTextStyle.string()));
                            sb.setLength(0);
                        }
                        all.add(txt.ofStyled(border, NTextStyle.string()));
                        break;
                    } else {
                        sb.append(chars.readChar());
                    }
                }
                if (sb.length() > 0) {
                    all.add(txt.ofStyled(sb.toString(), NTextStyle.string()));
                    sb.setLength(0);
                }
                return all.toArray(new NText[0]);
            }
        }
        return new NText[0];
    }
}
