package net.thevpc.nuts.runtime.standalone.text.parser.v1;

import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyleType;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;

import java.util.ArrayList;
import java.util.List;

public class StyledParserStepCmdParser {

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

    public NTextStyles parse(String atStr) {
        StringReaderExt r = new StringReaderExt(atStr);
        List<NTextStyle> parsedStyles = new ArrayList<>();
        if (r.hasNext() && r.peekChar() == ':') {
            r.readChar();//skip '!'
            while (true) {
                if (readEnd(r)) {
                    break;
                }
                NTextStyles s = readNextStyles(r);
                if (s == null) {
                    //this is an invalid style string hence add
                    return null;
                } else {
                    for (NTextStyle ss : s) {
                        parsedStyles.add(ss);
                    }
                }
            }
        } else {
            return null;
        }
        return NTextStyles.PLAIN.append(parsedStyles.toArray(new NTextStyle[0]));
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

    public NTextStyles parseSimpleNutsTextStyles(String str) {
        StringReaderExt e = new StringReaderExt(str);
        NTextStyles a = readNextStyles(e);
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

    private NTextStyles readNextStyles(StringReaderExt r) {
        List<NTextStyle> all=new ArrayList<>();
        NTextStyle s=readNextStyle(r);
        if(s!=null) {
            all.add(s);
            while (s!=null && r.hasNext()) {
                if (',' == r.peekChar()) {
                    r.readChar();
                }
                s = readNextStyle(r);
            }
        }
        if(all.isEmpty()){
            return null;
        }
        return NTextStyles.of(all.toArray(all.toArray(new NTextStyle[0])));
    }

    private NTextStyle readNextStyle(StringReaderExt r) {
        if (r.hasNext()) {
            char c = r.peekChar();
            switch (c) {
                case 'f':
                case 'F': {
                    Integer ii = readNextPrefixedHexString("fx", r);
                    if (ii != null) {
                        return NTextStyle.foregroundTrueColor(ii);
                    }
                    ii = readNextPrefixedHexString("foregroundx", r);
                    if (ii != null) {
                        return NTextStyle.foregroundTrueColor(ii);
                    }
                    ii = readNextPrefixedInt8("f", r);
                    if (ii != null) {
                        return NTextStyle.foregroundColor(ii);
                    }
                    ii = readNextPrefixedInt8("foreground", r);
                    if (ii != null) {
                        return NTextStyle.foregroundColor(ii);
                    }
                    break;
                }
                case 'b':
                case 'B': {
                    Integer ii = readNextPrefixedHexString("bx", r);
                    if (ii != null) {
                        return NTextStyle.backgroundTrueColor(ii);
                    }
                    ii = readNextPrefixedHexString("backgroundx", r);
                    if (ii != null) {
                        return NTextStyle.backgroundTrueColor(ii);
                    }
                    ii = readNextPrefixedInt8("b", r);
                    if (ii != null) {
                        return NTextStyle.backgroundColor(ii);
                    }
                    ii = readNextPrefixedInt8("background", r);
                    if (ii != null) {
                        return NTextStyle.backgroundColor(ii);
                    }
                    break;
                }
                case '/': {
                    r.readChar();//skip
                    return NTextStyle.italic();
                }
                case '+': {
                    r.readChar();//skip
                    return NTextStyle.bold();
                }
                case '%': {
                    r.readChar();//skip
                    return NTextStyle.blink();
                }
                case '_': {
                    r.readChar();//skip
                    return NTextStyle.underlined();
                }
                case '-': {
                    r.readChar();//skip
                    return NTextStyle.striked();
                }
                case '!': {
                    r.readChar();//skip
                    return NTextStyle.reversed();
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
                        return NTextStyle.primary(Integer.parseInt(s));
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
                        return NTextStyle.keyword(variant);
                    }
                    case "p": {
                        r.nextChars(totLen);
                        return NTextStyle.primary(variant);
                    }
                    case "s": {
                        r.nextChars(totLen);
                        return NTextStyle.secondary(variant);
                    }
                    case "bool": {
                        r.nextChars(totLen);
                        return NTextStyle.bool(variant);
                    }
                    default: {
                        String sb2 = n.toString().toUpperCase();
                        sb2 = sb2.replace('-', '_');
                        NTextStyleType st = NTextStyleType.parse(sb2).orElse(NTextStyleType.ERROR);
                        r.nextChars(totLen);
                        return NTextStyle.of(st, variant);
                    }
                }
            }
        }
        return null;
    }


}
