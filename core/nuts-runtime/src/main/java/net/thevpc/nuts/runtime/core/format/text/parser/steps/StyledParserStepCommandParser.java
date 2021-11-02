package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.NutsTextStyle;
import net.thevpc.nuts.NutsTextStyleType;
import net.thevpc.nuts.NutsTextStyles;
import net.thevpc.nuts.runtime.core.expr.StringReaderExt;

import java.util.ArrayList;
import java.util.List;

public class StyledParserStepCommandParser {

    public boolean isCommandEnd(char c) {
        return c == ' ' || c == ':';
    }

    public boolean isCommandPart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                || (c == '/') // italic
                || (c == '_') // underlined
                || (c == '+') // bold
                || (c == '!') // reverse
                || (c == '-') // striked
                || (c == '%') // blink
                ;
    }

    public NutsTextStyles parse(String atStr) {
        StringReaderExt r = new StringReaderExt(atStr);
        List<NutsTextStyle> parsedStyles = new ArrayList<>();
        if (r.hasNext() && r.peekChar() == ':') {
            r.nextChar();//skip '!'
            while (true) {
                if (readEnd(r)) {
                    break;
                }
                NutsTextStyle s = readNext(r);
                if (s == null) {
                    //this is an invalid style string hence add
                    return null;
                } else {
                    parsedStyles.add(s);
                }
            }
        } else {
            return null;
        }
        return NutsTextStyles.PLAIN.append(parsedStyles.toArray(new NutsTextStyle[0]));
    }

    private boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private boolean isDigitChar(char c) {
        return (c >= '0' && c <= '9');
    }

    private String peekInt(StringReaderExt r, int from, int maxChars) {
        int x = 0;
        StringBuilder s = new StringBuilder();
        while (r.hasNext(from + x)) {
            char c = r.peekChar(from + x);
            if (isDigitChar(c)) {
                s.append(c);
                x++;
                if (x == maxChars) {
                    break;
                }
            } else {
                break;
            }
        }
        if (x > 0) {
            if (!r.hasNext(from + x) || !isDigitChar(r.peekChar(from + x))) {
                try {
                    Integer.parseInt(s.toString());
                    return s.toString();
                } catch (Exception any) {
                    //ignore
                }
            }
        }
        return null;
    }

    private boolean readEnd(StringReaderExt r) {
        if (!r.hasNext()) {
            return true;
        }
        if (r.peekChar() == ' ' || r.peekChar() == ':') {
            if (r.hasNext(1)) {
                return false;
            }
            r.nextChars(1);
            return true;
        }
        return false;
    }

    public NutsTextStyle parseSimpleNutsTextStyle(String str) {
        StringReaderExt e = new StringReaderExt(str);
        NutsTextStyle a = readNext(e);
        if (a == null) {
            return null;
        }
        if (e.hasNext()) {
            return null;
        }
        return a;
    }

    private Integer readNextPrefixedInt8(String prefix, StringReaderExt r) {
        int len = prefix.length();
        String ss = r.peekChars(len);
        if (ss.equalsIgnoreCase(prefix)) {
            String s = peekInt(r, len, len+3);
            if (s!=null && s.length()>0) {
                r.nextChars(len);
                r.nextChars(s.length());
                return Integer.parseInt(s, 10);
            }
        }
        return null;
    }

    private Integer readNextPrefixedHexString(String prefix, StringReaderExt r) {
        int len = prefix.length();
        String ss = r.peekChars(len);
        if (ss.equalsIgnoreCase(prefix)) {
            if (
                    r.hasNext(len + 5)
                            && isHexChar(r.peekChar(len + 0))
                            && isHexChar(r.peekChar(len + 1))
                            && isHexChar(r.peekChar(len + 2))
                            && isHexChar(r.peekChar(len + 3))
                            && isHexChar(r.peekChar(len + 4))
                            && isHexChar(r.peekChar(len + 5))
            ) {
                String s = r.nextChars(len);
                s = r.nextChars(8);
                return Integer.parseInt(s, 16);
            }
        }
        return null;
    }

    private NutsTextStyle readNext(StringReaderExt r) {
        if (r.hasNext()) {
            char c = r.peekChar();
            switch (c) {
                case 'f':
                case 'F': {
                    Integer ii = readNextPrefixedHexString("fx", r);
                    if (ii != null) {
                        return NutsTextStyle.foregroundTrueColor(ii);
                    }
                    ii = readNextPrefixedHexString("foregroundx", r);
                    if (ii != null) {
                        return NutsTextStyle.foregroundTrueColor(ii);
                    }
                    ii = readNextPrefixedInt8("f", r);
                    if (ii != null) {
                        return NutsTextStyle.foregroundColor(ii);
                    }
                    ii = readNextPrefixedInt8("foreground", r);
                    if (ii != null) {
                        return NutsTextStyle.foregroundColor(ii);
                    }
                    break;
                }
                case 'b':
                case 'B': {
                    Integer ii = readNextPrefixedHexString("bx", r);
                    if (ii != null) {
                        return NutsTextStyle.backgroundTrueColor(ii);
                    }
                    ii = readNextPrefixedHexString("backgroundx", r);
                    if (ii != null) {
                        return NutsTextStyle.backgroundTrueColor(ii);
                    }
                    ii = readNextPrefixedInt8("b", r);
                    if (ii != null) {
                        return NutsTextStyle.backgroundColor(ii);
                    }
                    ii = readNextPrefixedInt8("background", r);
                    if (ii != null) {
                        return NutsTextStyle.backgroundColor(ii);
                    }
                    break;
                }
                case '/': {
                    r.nextChar();//skip
                    return NutsTextStyle.italic();
                }
                case '+': {
                    r.nextChar();//skip
                    return NutsTextStyle.bold();
                }
                case '%': {
                    r.nextChar();//skip
                    return NutsTextStyle.blink();
                }
                case '_': {
                    r.nextChar();//skip
                    return NutsTextStyle.underlined();
                }
                case '-': {
                    r.nextChar();//skip
                    return NutsTextStyle.striked();
                }
                case '!': {
                    r.nextChar();//skip
                    return NutsTextStyle.reversed();
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
                    String s = peekInt(r, 0, 3);
                    if (s != null) {
                        r.nextChars(s.length());
                        return NutsTextStyle.primary(Integer.parseInt(s));
                    }
                    break;
                }
            }
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                int x=0;
                StringBuilder n=new StringBuilder();
                boolean expectN=false;
                while (r.hasNext(x)) {
                    char c2 = r.peekChar(x);
                    if(isDigitChar(c2)){
                        expectN=true;
                        break;
                    }else if((c2>='a' && c2<='z') || (c2>='A' && c2<='Z') || (c2=='_' || c2=='-')){
                        n.append(c2);
                        x++;
                    }else{
                        break;
                    }
                }
                String variantString="";
                if(expectN){
                    String s = peekInt(r, x, 3);
                    if(s!=null){
                        variantString=s;
                    }
                }
                int variant = variantString.isEmpty() ? 0 : Integer.parseInt(variantString);
                int totLen = n.toString().length() + variantString.length();
                switch (n.toString().toLowerCase()) {
                    case "kw": {
                        r.nextChars(totLen);
                        return NutsTextStyle.keyword(variant);
                    }
                    case "p": {
                        r.nextChars(totLen);
                        return NutsTextStyle.primary(variant);
                    }
                    case "s": {
                        r.nextChars(totLen);
                        return NutsTextStyle.secondary(variant);
                    }
                    case "bool": {
                        r.nextChars(totLen);
                        return NutsTextStyle.bool(variant);
                    }
                    default: {
                        String sb2 = n.toString().toUpperCase();
                        sb2 = sb2.replace('-', '_');
                        NutsTextStyleType st = NutsTextStyleType.parseLenient(sb2,NutsTextStyleType.ERROR);
                        r.nextChars(totLen);
                        return NutsTextStyle.of(st, variant);
                    }
                }
            }
        }
        return null;
    }


}
