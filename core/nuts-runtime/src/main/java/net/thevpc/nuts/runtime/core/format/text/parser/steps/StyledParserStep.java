package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsTextNodeStyleType;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextNodeFactory;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.runtime.standalone.util.common.StringReaderExt;

import java.util.ArrayList;
import java.util.List;

public class StyledParserStep extends ParserStep {

    boolean spreadLines;
    boolean lineStart;
    boolean started = false;
    boolean complete = false;
    StringBuilder start = new StringBuilder();
    StringBuilder atStr = new StringBuilder();
    StringBuilder end = new StringBuilder();
    List<ParserStep> children = new ArrayList<>();
    int maxSize = 10;
    private NutsWorkspace ws;
    private StyleMode styleMode = StyleMode.SIMPLE;
    private boolean atPresentEnded = false;
    private List<NutsTextNodeStyle> atVals = new ArrayList<>();
    private NutsTextNode atInvalid;
    private boolean parsedAt = false;

    public StyledParserStep(char c, boolean spreadLines, boolean lineStart, NutsWorkspace ws) {
        start.append(c);
        this.spreadLines = spreadLines;
        this.lineStart = lineStart;
        this.ws = ws;
    }

    public StyledParserStep(String c, boolean spreadLines, boolean lineStart, NutsWorkspace ws) {
        start.append(c);
        this.spreadLines = spreadLines;
        this.lineStart = lineStart;
        this.ws = ws;
    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State state) {
        if (complete) {
            if (c == start.charAt(0)) {
                end.append(c);
                String e2 = end.toString();
                end.delete(0, e2.length());
                complete = false;
                state.applyPush(new StyledParserStep(
                        e2, spreadLines, false, ws
                ));
            } else if (c == 'ø') {
                state.applyPop();
            } else {
                state.applyPopReject(c);
            }
            return;
        }
        if (!spreadLines && (c == '\n' || c == '\r')) {
            state.applyPopReject(c);
            return;
        }
        if (c == 'ø') {
            if (!started) {
                started = true;
                state.applyPush(new DispatchAction(false, false));
            } else {
                state.applyPop();
            }
            return;
        }
        if (!started) {
            if (c == start.charAt(0)) {
                if (start.length() <= maxSize) {
                    start.append(c);
                } else {
                    started = true;
                    state.applyStart(c, spreadLines, false);
                }
            } else {
                char startChar = start.charAt(0);
                char endChar = endOf(startChar);
                started = true;
                if (c == endChar) {
                    end.append(c);
                    if (end.length() >= start.length()) {
                        state.applyPop();
                    }

                } else if (lineStart && startChar == '#' && c == ')') {
                    //this is a title
                    state.applyDropReplace(new TitleParserStep(start.toString() + c, ws));
                } else if (startChar == '#' && start.length() == 2 && c == '&') {
                    styleMode = StyleMode.AT;
                    atStr.append(c);
                    //this is a title ##&
                } else {
                    state.applyStart(c, spreadLines, false);
                }
            }
        } else {
            boolean processNormal = false;
            if (styleMode == StyleMode.AT && !atPresentEnded) {
                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                        || (c == '/') // italic
                        || (c == '_') // underlined
                        || (c == '+') // bold
                        || (c == '!') // reverse
                        || (c == '-') // striked
                        || (c == '%') // blink
                ) {
                    atStr.append(c);
                } else if (c == ' ' || c == ':') {
                    atStr.append(c);
                    atPresentEnded = true;
                } else {
                    //rollback
                    String s = atStr.toString() + c;
                    atStr.setLength(0);
                    styleMode = StyleMode.SIMPLE;
                    state.applyPush(new PlainParserStep(s, spreadLines, false, ws, state));
                    atPresentEnded = false;
                    return;
                }
            } else {
                processNormal = true;
            }
            if (processNormal) {
                char endChar = endOf(start.charAt(0));
                if (c == endChar) {
                    if (end.length() >= start.length()) {
                        state.applyPopReject(c);
                    } else {
                        end.append(c);
                        if (end.length() >= start.length()) {
                            complete = true;
                        }
                    }
                } else {
                    if (end.length() == 0) {
                        state.applyStart(c, spreadLines, false);
                    } else {
                        String y = end.toString();
                        end.delete(0, end.length());
                        if (y.length() > 1) {
                            state.applyPush(new StyledParserStep(y, spreadLines, lineStart, ws));
                        } else {
                            state.applyPush(new PlainParserStep(y, spreadLines, lineStart, ws, state));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void appendChild(ParserStep tt) {
        children.add(tt);
    }

    @Override
    public NutsTextNode toNode() {
        DefaultNutsTextNodeFactory factory0 = (DefaultNutsTextNodeFactory) ws.formats().text().factory();
        String start = this.start.toString();
        String end = this.end.toString();
        List<NutsTextNodeStyle> all = new ArrayList<>();
        if (styleMode == StyleMode.AT) {
            if (!parsedAt) {
                parsedAt = true;
                StringReaderExt r = new StringReaderExt(atStr.toString());
                List<NutsTextNodeStyle> parsedStyles=new ArrayList<>();
                if(r.hasNext() && r.peekChar()=='&'){
                    r.nextChar();//skip '&'
                    while(true){
                        if(readEnd(r)){
                            break;
                        }
                        NutsTextNodeStyle s = readNext(r);
                        if(s==null){
                            //this is an invalid style string hence add
                            atInvalid=ws.formats().text().factory().plain(atStr.toString());
                            break;
                        }else{
                            parsedStyles.add(s);
                        }
                    }
                }else{
                    atInvalid=ws.formats().text().factory().plain(atStr.toString());
                }
                if(atInvalid==null){
                    atVals.addAll(parsedStyles);
                }
            }
        }
        if (styleMode == StyleMode.AT) {
            all.addAll(atVals);
        } else {
            switch (start.charAt(0)) {
                case '#': {
                    all.add(NutsTextNodeStyle.primary(start.length() - 1));
                    break;
                }
//                case '@': {
//                    all.add(NutsTextNodeStyle.secondary(start.length() - 1));
//                    break;
//                }
//                default: {
//                    switch (start) {
//                        case "~~": {
//                            all.add(NutsTextNodeStyle.underlined());
//                            break;
//                        }
//                        case "~~~": {
//                            all.add(NutsTextNodeStyle.italic());
//                            break;
//                        }
//                        case "~~~~": {
//                            all.add(NutsTextNodeStyle.striked());
//                            break;
//                        }
//                        case "~~~~~~": {
//                            all.add(NutsTextNodeStyle.reversed());
//                            break;
//                        }
//                        case "~~~~~~~": {
//                            all.add(NutsTextNodeStyle.bold());
//                            break;
//                        }
//                        case "~~~~~~~~": {
//                            all.add(NutsTextNodeStyle.blink());
//                            break;
//                        }
//                    }
//                }
            }
        }

        NutsTextNode child = null;
        if (children.size() == 1) {
            child = children.get(0).toNode();
        } else {
            List<NutsTextNode> allChildren = new ArrayList<>();
            for (ParserStep a : children) {
                allChildren.add(a.toNode());
            }
            child = ws.formats().text().factory().list(allChildren.toArray(new NutsTextNode[0]));
        }
        if(atInvalid!=null){
            child=ws.formats().text().factory().list(atInvalid,child);
        }
        if (all.isEmpty()) {
            all.add(NutsTextNodeStyle.primary(1));
        }
        for (NutsTextNodeStyle s : all) {
            child = factory0.createStyled(
                    child, s,
                    isComplete());
        }
        return child;
    }

    @Override
    public void end(DefaultNutsTextNodeParser.State p) {
        if (!isComplete()) {
            while (end.length() < start.length()) {
                end.append(endOf(start.charAt(0)));
            }
        }
        p.applyPop();
    }

    public boolean isComplete() {
        return started && end.length() == start.length();
    }

    private boolean isHexaChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private boolean isDigitChar(char c) {
        return (c >= '0' && c <= '9');
    }

    private NutsTextNodeStyle readWordNumber(StringReaderExt r) {
        if(r.hasNext()) {
            char c=r.peekChar();
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                int x = 1;
                StringBuilder sb = new StringBuilder();
                sb.append(c);
                String variantString="";
                while (r.hasNext(x)) {
                    char c2 = r.peekChar(x);
                    if ((c2 >= 'a' && c2 <= 'z') || (c2 >= 'A' && c2 <= 'Z') || (c2 == '_') || (c2 == '-')) {
                        sb.append(c2);
                        x++;
                    } else if (c2 == ' ' || c2 == ':') {
                        break;
                    } else if ((c2 >= '0' && c2 <= '9')) {
                        String s = readInt(r, x, 3);
                        if (s != null) {
                            variantString=s;
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
                        return NutsTextNodeStyle.keyword(variant);
                    }
                    case "p": {
                        r.nextChars(sb.toString().length()+variantString.length());
                        return NutsTextNodeStyle.primary(variant);
                    }
                    case "s": {
                        r.nextChars(sb.toString().length()+variantString.length());
                        return NutsTextNodeStyle.secondary(variant);
                    }
                    case "bool": {
                        r.nextChars(sb.toString().length()+variantString.length());
                        return NutsTextNodeStyle.bool(variant);
                    }
                    default: {
                        String sb2 = sb.toString().toUpperCase();
                        sb2=sb2.replace('-', '_');
                        try {
                            NutsTextNodeStyleType st = NutsTextNodeStyleType.valueOf(sb2);
                            r.nextChars(sb.toString().length()+variantString.length());
                            return NutsTextNodeStyle.of(st, variant);
                        }catch (Exception ex){
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }
    private String readInt(StringReaderExt r, int from, int maxChars) {
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

    private NutsTextNodeStyle readNext(StringReaderExt r) {
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
                        return NutsTextNodeStyle.foregroundTrueColor(Integer.parseInt(s.substring(2), 16));
                    } else {
                        String variant = readInt(r, 1, 3);
                        if (variant != null) {
                            r.nextChars(1+variant.length());
                            return NutsTextNodeStyle.foregroundColor(Integer.parseInt(variant));
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
                        return NutsTextNodeStyle.backgroundTrueColor(Integer.parseInt(s.substring(2), 16));
                    } else {
                        String variant = readInt(r, 1, 3);
                        if (variant != null) {
                            r.nextChars(1+variant.length());
                            return NutsTextNodeStyle.backgroundColor(Integer.parseInt(variant));
                        }
                    }
                    break;
                }
                case '/':{
                    r.nextChar();//skip
                    return NutsTextNodeStyle.italic();
                }
                case '+':{
                    r.nextChar();//skip
                    return NutsTextNodeStyle.bold();
                }
                case '%':{
                    r.nextChar();//skip
                    return NutsTextNodeStyle.blink();
                }
                case '_':{
                    r.nextChar();//skip
                    return NutsTextNodeStyle.underlined();
                }
                case '-':{
                    r.nextChar();//skip
                    return NutsTextNodeStyle.striked();
                }
                case '!':{
                    r.nextChar();//skip
                    return NutsTextNodeStyle.reversed();
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
                    String s = readInt(r, 0, 3);
                    if(s!=null){
                        r.nextChars(s.length());
                        return NutsTextNodeStyle.primary(Integer.parseInt(s));
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

    @Override
    public String toString() {
//        StringBuilder sb = new StringBuilder("Typed(" + CoreStringUtils.dblQuote(start.toString()));
//        if (!started) {
//            sb.append(",<NEW>");
//        }
//        for (ParserStep parserStep : children) {
//            sb.append(",");
//            sb.append(parserStep.toString());
//        }
//        sb.append(",END(").append(CoreStringUtils.dblQuote(end.toString())).append(")");
//        sb.append(isComplete() ? "" : ",incomplete");
//        return sb.append(")").toString();
        StringBuilder sb = new StringBuilder();
        sb.append(start);
        for (ParserStep parserStep : children) {
            sb.append(parserStep.toString());
        }
        sb.append(end);
        return sb.toString();
    }

    public char endOf(char c) {
        switch (c) {
            case '<':
                return '>';
            case '(':
                return ')';
            case '[':
                return ']';
            case '{':
                return '}';
        }
        return c;
    }

    enum StyleMode {
        SIMPLE, // ##anything##
        AT,     // ##&12:anything##
        // ##&sh:anything##
    }

}
