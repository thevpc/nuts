package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.NutsTextStyle;
import net.thevpc.nuts.NutsTextStyleType;
import net.thevpc.nuts.runtime.bundles.parsers.StringReaderExt;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NutsTextStyles;

public class StyledParserStepCommandParser {

    public boolean isCommandEnd(char c){
        return c==' ' || c==':';
    }

    public boolean isCommandPart(char c){
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                || (c == '/') // italic
                || (c == '_') // underlined
                || (c == '+') // bold
                || (c == '!') // reverse
                || (c == '-') // striked
                || (c == '%') // blink
                ;
    }

    public NutsTextStyles parse(String atStr){
        StringReaderExt r = new StringReaderExt(atStr.toString());
        List<NutsTextStyle> parsedStyles=new ArrayList<>();
        if(r.hasNext() && r.peekChar()==':'){
            r.nextChar();//skip '!'
            while(true){
                if(readEnd(r)){
                    break;
                }
                NutsTextStyle s = readNext(r);
                if(s==null){
                    //this is an invalid style string hence add
                    return null;
                }else{
                    parsedStyles.add(s);
                }
            }
        }else{
            return null;
        }
        return NutsTextStyles.NONE.append(parsedStyles.toArray(new NutsTextStyle[0]));
    }

    private boolean isHexaChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private boolean isDigitChar(char c) {
        return (c >= '0' && c <= '9');
    }

    private NutsTextStyle readWordNumber(StringReaderExt r) {
        if(r.hasNext()) {
            char c=r.peekChar();
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                int x = 1;
                StringBuilder sb = new StringBuilder();
                sb.append(c);
                String variantString="";
                while (r.hasNext(x)) {
                    char c2 = r.peekChar(x);
                    if(sb.toString().equals("foreground")){
                        if (r.hasNext(x+7)
                                && r.peekChar(x+0) == 'x'
                                && isHexaChar(r.peekChar(x+1))
                                && isHexaChar(r.peekChar(x+2))
                                && isHexaChar(r.peekChar(x+3))
                                && isHexaChar(r.peekChar(x+4))
                                && isHexaChar(r.peekChar(x+5))
                                && isHexaChar(r.peekChar(x+6))) {
                            r.nextChars(sb.length());
                            String s = r.nextChars(7);
                            return NutsTextStyle.foregroundTrueColor(Integer.parseInt(s.substring(1), 16));
                        } else {
                            String variant = peekInt(r, x, 3);
                            if (variant != null) {
                                r.nextChars(sb.length());
                                r.nextChars(variant.length());
                                return NutsTextStyle.foregroundColor(Integer.parseInt(variant));
                            }
                        }
                    }else if(sb.toString().equals("background")){
                        if (r.hasNext(x+7)
                                && r.peekChar(x+0) == 'x'
                                && isHexaChar(r.peekChar(x+1))
                                && isHexaChar(r.peekChar(x+2))
                                && isHexaChar(r.peekChar(x+3))
                                && isHexaChar(r.peekChar(x+4))
                                && isHexaChar(r.peekChar(x+5))
                                && isHexaChar(r.peekChar(x+6))) {
                            r.nextChars(sb.length());
                            String s = r.nextChars(7);
                            return NutsTextStyle.backgroundTrueColor(Integer.parseInt(s.substring(1), 16));
                        } else {
                            String variant = peekInt(r, x, 3);
                            if (variant != null) {
                                r.nextChars(sb.length());
                                r.nextChars(variant.length());
                                return NutsTextStyle.backgroundColor(Integer.parseInt(variant));
                            }
                        }
                    }
                    if ((c2 >= 'a' && c2 <= 'z') || (c2 >= 'A' && c2 <= 'Z') || (c2 == '_') || (c2 == '-')) {
                        sb.append(c2);
                        x++;
                    } else if (c2 == ' ' || c2 == ':') {
                        break;
                    } else if ((c2 >= '0' && c2 <= '9')) {
                        String s = peekInt(r, x, 3);
                        if (s != null) {
                            variantString = s;
                        }
                        break;
                    } else {
                        break;
                    }
                }
                int variant = variantString.isEmpty()?0:Integer.parseInt(variantString);
                switch (sb.toString()) {
                    case "kw": {
                        r.nextChars(sb.toString().length()+variantString.length());
                        return NutsTextStyle.keyword(variant);
                    }
                    case "p": {
                        r.nextChars(sb.toString().length()+variantString.length());
                        return NutsTextStyle.primary(variant);
                    }
                    case "s": {
                        r.nextChars(sb.toString().length()+variantString.length());
                        return NutsTextStyle.secondary(variant);
                    }
                    case "bool": {
                        r.nextChars(sb.toString().length()+variantString.length());
                        return NutsTextStyle.bool(variant);
                    }
                    default: {
                        String sb2 = sb.toString().toUpperCase();
                        sb2=sb2.replace('-', '_');
                        try {
                            NutsTextStyleType st = NutsTextStyleType.valueOf(sb2);
                            r.nextChars(sb.toString().length()+variantString.length());
                            return NutsTextStyle.of(st, variant);
                        }catch (Exception ex){
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }
    private String peekInt(StringReaderExt r, int from, int maxChars) {
        int x = 0;
        StringBuilder s=new StringBuilder();
        while (r.hasNext(from+x)) {
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
            if (!r.hasNext(x) || !isDigitChar(r.peekChar(from+x))) {
                try {
                    Integer.parseInt(s.toString());
                    return s.toString();
                }catch (Exception any){
                    //ignore
                }
            }
        }
        return null;
    }

    private boolean readEnd(StringReaderExt r) {
        if(!r.hasNext()){
            return true;
        }
        if(r.peekChar()==' ' ||r.peekChar()==':'){
            if(r.hasNext(1)){
                return false;
            }
            r.nextChars(1);
            return true;
        }
        return false;
    }

    private NutsTextStyle readNext(StringReaderExt r) {
        if (r.hasNext()) {
            char c = r.peekChar();
            switch (c){
                case 'f':
                case 'F':{
                    boolean fx = r.hasNext(8)
                            && r.peekChar(0) == 'f'
                            && r.peekChar(1) == 'x'
                            && isHexaChar(r.peekChar(2))
                            && isHexaChar(r.peekChar(3))
                            && isHexaChar(r.peekChar(4))
                            && isHexaChar(r.peekChar(5))
                            && isHexaChar(r.peekChar(6))
                            && isHexaChar(r.peekChar(7));
                    if (fx) {
                        String s = r.nextChars(8);
                        return NutsTextStyle.foregroundTrueColor(Integer.parseInt(s.substring(2), 16));
                    } else {
                        String variant = peekInt(r, 1, 3);
                        if (variant != null) {
                            r.nextChars(1+variant.length());
                            return NutsTextStyle.foregroundColor(Integer.parseInt(variant));
                        }
                    }
                    break;
                }
                case 'b':
                case 'B':{
                    boolean fx = r.hasNext(8)
                            && r.peekChar(0) == 'f'
                            && r.peekChar(1) == 'x'
                            && isHexaChar(r.peekChar(2))
                            && isHexaChar(r.peekChar(3))
                            && isHexaChar(r.peekChar(4))
                            && isHexaChar(r.peekChar(5))
                            && isHexaChar(r.peekChar(6))
                            && isHexaChar(r.peekChar(7));
                    if (fx) {
                        String s = r.nextChars(8);
                        return NutsTextStyle.backgroundTrueColor(Integer.parseInt(s.substring(2), 16));
                    } else {
                        String variant = peekInt(r, 1, 3);
                        if (variant != null) {
                            r.nextChars(1+variant.length());
                            return NutsTextStyle.backgroundColor(Integer.parseInt(variant));
                        }
                    }
                    break;
                }
                case '/':{
                    r.nextChar();//skip
                    return NutsTextStyle.italic();
                }
                case '+':{
                    r.nextChar();//skip
                    return NutsTextStyle.bold();
                }
                case '%':{
                    r.nextChar();//skip
                    return NutsTextStyle.blink();
                }
                case '_':{
                    r.nextChar();//skip
                    return NutsTextStyle.underlined();
                }
                case '-':{
                    r.nextChar();//skip
                    return NutsTextStyle.striked();
                }
                case '!':{
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
                case '9':{
                    String s = peekInt(r, 0, 3);
                    if(s!=null){
                        r.nextChars(s.length());
                        return NutsTextStyle.primary(Integer.parseInt(s));
                    }
                    break;
                }
            }
            if( (c>='a' && c<='z') || (c>='A' && c<='Z')){
                return readWordNumber(r);
            }
        }
        return null;
    }


}
