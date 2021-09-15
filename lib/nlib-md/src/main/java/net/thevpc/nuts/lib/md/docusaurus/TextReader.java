package net.thevpc.nuts.lib.md.docusaurus;

import net.thevpc.nuts.lib.md.MdXml;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextReader {
    private PushbackReader reader;
    private Map<String, Pattern> cachedPatterns = new HashMap<>();

    public TextReader(Reader r) {
        this.reader = new PushbackReader(r, 4096);
    }

    public static boolean isHexDigit(int current) {
        return current >= '0' && current <= '9'
                || current >= 'a' && current <= 'f'
                || current >= 'A' && current <= 'F';
    }

    public void unread(int c) {
        if (c < 0) {
            return;
        }
        try {
            reader.unread(c);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void unread(String c) {
        unread(c.toCharArray());
    }

    public void unread(char[] c) {
        if (c.length == 0) {
            return;
        }
        try {
            reader.unread(c);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String readCommentsSlashSlash() {
        return readRegexp("//[^\\R]\\R");
    }

    public String readCommentsSlashStar() {
        return readRegexp("/[*](([*][^/])|[^*])*[*]/");
    }

    public String peekRegexp(String regexp) {
        String s = readRegexp(regexp);
        if(s==null || s.length()==0){
            return null;
        }
        unread(s);
        return s;
    }

    public String readRegexp(String regexp) {
//        String ss=peekString(20);
        Pattern p = cachedPatterns.computeIfAbsent(regexp, Pattern::compile);
        return readStringOrNull(new Globber() {
            @Override
            public GlobberRet accept(StringBuilder visited, char next) {
                String s = visited.toString() + next;
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    return GlobberRet.ACCEPT;
                }
                if (m.hitEnd()) {
                    return GlobberRet.WAIT_FOR_MORE;
                }
                return GlobberRet.REJECT_LAST;
            }
        });
    }

    public String readSpacesOrNewline() {
        StringBuilder sb = new StringBuilder();
        int r;
        while ((r = readChar()) > 0) {
            if (r <= 0x20) {
                sb.append((char) r);
            } else {
                unread(r);
                break;
            }
        }
        return sb.toString();
    }

    public String readSpaces() {
        StringBuilder sb = new StringBuilder();
        int r;
        while ((r = peekChar()) > 0) {
            if (r==' ' || r=='\t') {
                readChar();
                sb.append((char) r);
            } else {
                break;
            }
        }
        return sb.toString();
    }

    public Token readQuotedString() {
        int r = peekChar();
        switch (r) {
            case '"':
            case '\'':
            case '`': {
                return readQuotedString((char) r);
            }
            default: {
                return null;
            }
        }
    }

    public Token readQuotedString(char quote) {
        switch (quote) {
            case '"':
            case '\'':
            case '`': {
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid quote " + quote);
            }
        }
        if (readChar(quote)) {
            StringBuilder image = new StringBuilder();
            image.append(quote);
            StringBuilder sb = new StringBuilder();
            int r;
            while ((r = readChar()) != -1) {
                if (r == quote) {
                    image.append(r);
                    break;
                } else if (r == '\\') {
                    image.append((char) r);
                    r = readChar();
                    if (r < 0) {
                        break;
                    }
                    switch (r) {
                        case '\'':
                        case '"':
                        case '/':
                        case '\\':
                            image.append((char) r);
                            sb.append((char) r);
                            break;
                        case 'b':
                            image.append((char) r);
                            sb.append('\b');
                            break;
                        case 'f':
                            image.append((char) r);
                            sb.append('\f');
                            break;
                        case 'n':
                            image.append((char) r);
                            sb.append('\n');
                            break;
                        case 'r':
                            image.append((char) r);
                            sb.append('\r');
                            break;
                        case 't':
                            image.append((char) r);
                            sb.append('\t');
                            break;
                        case 'u':
                            image.append((char) r);
                            char[] hexChars = new char[4];
                            for (int i = 0; i < 4; i++) {
                                r = readChar();
                                if (r >= 0) {
                                    image.append((char) r);
                                }
                                if (!isHexDigit(r)) {
                                    expected("hexadecimal digit");
                                    hexChars[i] = '0';
                                } else {
                                    hexChars[i] = (char) r;
                                }
                            }
                            sb.append((char) Integer.parseInt(new String(hexChars), 16));
                            break;
                        default: {
                            image.append((char) r);
                            expected("valid escape sequence");
                        }
                    }
                } else if (r < 0x20) {
                    image.append((char) r);
                    sb.append((char) r);
                    expected("valid string character");
                } else {
                    image.append((char) r);
                    sb.append((char) r);
                }
            }
            return new Token(
                    image.toString(),
                    sb.toString()
            );
        }
        return null;
    }

    private void expected(String expected) {
        if (false) {
            throw new IllegalArgumentException(expected);
        }
    }

    public String readStringOrNull(Globber whileCond) {
        return readString(whileCond,true);
    }

    public String readStringOrEmpty(Globber whileCond) {
        return readString(whileCond,false);
    }

    public String readString(Globber whileCond,boolean returnNull) {
        StringBuilder sb = new StringBuilder();
        String lastAccept = null;
        int r;
        while ((r = readChar()) > 0) {
            GlobberRet a = whileCond.accept(sb, (char) r);
            if (a == GlobberRet.ACCEPT_END) {
                sb.append(((char) r));
                return sb.toString();
            }else if (a == GlobberRet.ACCEPT) {
                sb.append(((char) r));
                lastAccept = sb.toString();
            } else if (a == GlobberRet.WAIT_FOR_MORE) {
                sb.append(((char) r));
            } else if (a == GlobberRet.REJECT_ALL) {
                unread(r);
                unread(sb.toString());
                if(returnNull){
                    return null;
                }
                return "";
            } else if (a == GlobberRet.REJECT_LAST) {
                unread(r);
                break;
            }
        }
        if (lastAccept != null) {
            String ur = sb.substring(lastAccept.length());
            unread(ur);
            return lastAccept;
        }
        unread(sb.toString());
        if(returnNull){
            return null;
        }
        return "";
    }

    public int readChar() {
        try {
            return reader.read();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }


    public String readNewline() {
        return readRegexp("\\R");
    }

    public String readChars(char c, int min, int max) {
        if (min <= 0) {
            min = 1;
        }
        int finalMin = min;
        if (max < 0) {
            return readStringOrEmpty((visited, next) -> {
                int len = visited.length();
                if (next == c) {
                    if (len + 1 >= finalMin) {
                        return GlobberRet.ACCEPT;
                    }
                    return GlobberRet.WAIT_FOR_MORE;
                } else {
                    return GlobberRet.REJECT_LAST;
                }
            });
        }
        if (max < min) {
            throw new IllegalArgumentException("invalid max");
        }
        return readStringOrEmpty((visited, next) -> {
            int len = visited.length();
            if (len == max) {
                if (next == c) {
                    return GlobberRet.REJECT_ALL;
                } else {
                    return GlobberRet.REJECT_LAST;
                }
            } else if (len < max) {
                if (next == c) {
                    if (len + 1 >= finalMin) {
                        return GlobberRet.ACCEPT;
                    }
                    return GlobberRet.WAIT_FOR_MORE;
                } else {
                    return GlobberRet.REJECT_LAST;
                }
            }
            return GlobberRet.REJECT_ALL;
        });
    }

    public boolean hasMore() {
        return peekChar() >= 0;
    }

    public boolean peekNewlineChar() {
        int c = peekChar();
        return c == '\n' || c == '\r';
    }

    public int peekChar() {
        int r = readChar();
        if (r < 0) {
            return r;
        }
        unread(r);
        return r;
    }

    public boolean peekChar(char c) {
        return peekChar() == c;
    }

    public boolean readChar(char c) {
        int r = readChar();
        if (r < 0) {
            return false;
        }
        if (r == c) {
            return true;
        }
        unread(r);
        return false;
    }


//    public static class XmlTag{
//        public String name;
//        public XmlTagType mode;
//        public Map<String,String> props=new HashMap<>();
//    }

    public MdXml readXmlTag() {
        if (readChar('<')) {
            Map<String, String> props = new HashMap<>();
            MdXml.XmlTagType mode = MdXml.XmlTagType.OPEN;
            if (readChar('/')) {
                mode = MdXml.XmlTagType.CLOSE;
            }
            String s = readTagName();
            if (s.length() > 0) {
                while (hasMore()) {
                    readSpacesOrNewline();
                    if (readString("/>")) {
                        if (mode == MdXml.XmlTagType.OPEN) {
                            mode = MdXml.XmlTagType.AUTO_CLOSE;
                        }
                        break;
                    }
                    if (readChar('>')) {
                        break;
                    }
                    String pn = readTagParamName();
                    readSpacesOrNewline();
                    String pv = null;
                    if (readChar('=')) {
                        boolean someSpacesAfterEq = readSpacesOrNewline().length() > 0;
                        pv = readTagParamValue(!someSpacesAfterEq);
                    }
                    props.put(pn, pv);
                }
                return new MdXml(
                        mode, s, props, null
                );
            } else {
                if (mode == MdXml.XmlTagType.CLOSE) {
                    unread('/');
                }
                unread('<');
                return null;
            }
        }
        return null;
    }

    public boolean readString(String expected) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < expected.length(); i++) {
            int c = readChar();
            if (c < 0) {
                unread(sb.toString().toCharArray());
                return false;
            } else if (c != expected.charAt(i)) {
                sb.append((char) c);
                unread(sb.toString().toCharArray());
                return false;
            } else {
                sb.append((char) c);
            }
        }
        return true;
    }

    public String readLine(boolean consumeNewLine) {
        int r;
        StringBuilder sb = new StringBuilder();
        while ((r = readChar()) != -1) {
            if (r == '\n' || r == '\r') {
                if (consumeNewLine) {
                    sb.append((char) r);
                    if (r == '\r') {
                        if (readChar('\n')) {
                            sb.append('\n');
                        }
                    }
                } else {
                    unread(r);
                }
                break;
            } else {
                sb.append((char) r);
            }
        }
        return sb.toString();
    }

    public String peekLine() {
        int r;
        StringBuilder sb = new StringBuilder();
        while ((r = readChar()) != -1) {
            if (r == '\n' || r == '\r') {
                unread(r);
                break;
            } else {
                sb.append((char) r);
            }
        }
        unread(sb.toString().toCharArray());
        return sb.toString();
    }

    public String peekString(int max) {
        int count = 0;
        int r;
        StringBuilder sb = new StringBuilder();
        while (count < max && (r = readChar()) != -1) {
            sb.append((char) r);
            count++;
        }
        unread(sb.toString().toCharArray());
        return sb.toString();
    }

    public String readTagParamName() {
        TextReader.Token pnt = readQuotedString();
        if (pnt != null) {
            return pnt.sval;
        } else {
            return readStringOrEmpty(new TextReader.Globber() {
                @Override
                public TextReader.GlobberRet accept(StringBuilder visited, char next) {
                    boolean b = next > 0x32 && next != '=' && next != '>';
                    return b ? TextReader.GlobberRet.ACCEPT : TextReader.GlobberRet.REJECT_LAST;
                }
            });
        }
    }

    public String readTagParamValue(boolean acceptUnquoted) {
        TextReader.Token pnt = readQuotedString();
        if (pnt != null) {
            return pnt.sval;
        } else {
            if (acceptUnquoted) {
                return readStringOrEmpty(new ExpressionGlobber());
            }
            return "";
        }
    }

    public String readTagName() {
        return readStringOrEmpty(new Globber() {
            @Override
            public GlobberRet accept(StringBuilder visited, char next) {
                if (visited.length() == 0) {
                    boolean ok =
                            (next >= 'a' && next <= 'z')
                                    || (next >= 'A' && next <= 'Z')
                                    || next == '_'
                                    || next == '-';
                    return ok ? GlobberRet.ACCEPT : GlobberRet.REJECT_LAST;
                }
                boolean ok =
                        (next >= 'a' && next <= 'z')
                                || (next >= 'A' && next <= 'Z')
                                || (next >= '0' && next <= '9')
                                || next == '_'
                                || next == '-'
                                || next == ':';
                return ok ? GlobberRet.ACCEPT : GlobberRet.REJECT_LAST;
            }
        });
    }


    public enum GlobberRet {
        ACCEPT_END,
        ACCEPT,
        REJECT_LAST,
        REJECT_ALL,
        WAIT_FOR_MORE,
    }

    public interface Globber {
        GlobberRet accept(StringBuilder visited, char next);
    }

    private static class ExpressionGlobber implements Globber {
        final int PLAIN = 0;
        final int QUOTE1 = 1;
        final int QUOTE2 = 2;
        final int AQUOTE = 3;
        int pars = 0;
        int sqAcc = 0;
        int acc = 0;
        int mode = PLAIN;
        boolean wasEscape = false;

        @Override
        public GlobberRet accept(StringBuilder visited, char next) {
            switch (mode) {
                case PLAIN: {
                    switch (next) {
                        case '>':
                            return GlobberRet.REJECT_LAST;
                        case '(': {
                            pars++;
                            return GlobberRet.WAIT_FOR_MORE;
                        }
                        case '{': {
                            acc++;
                            return GlobberRet.WAIT_FOR_MORE;
                        }
                        case '[': {
                            sqAcc++;
                            return GlobberRet.WAIT_FOR_MORE;
                        }
                        case ')': {
                            pars--;
                            if (pars == 0 && acc == 0 && sqAcc == 0) {
                                return GlobberRet.ACCEPT;
                            }
                            return GlobberRet.WAIT_FOR_MORE;
                        }
                        case '}': {
                            acc--;
                            if (pars == 0 && acc == 0 && sqAcc == 0) {
                                return GlobberRet.ACCEPT;
                            }
                            return GlobberRet.WAIT_FOR_MORE;
                        }
                        case ']': {
                            sqAcc--;
                            if (pars == 0 && acc == 0 && sqAcc == 0) {
                                return GlobberRet.ACCEPT;
                            }
                            return GlobberRet.WAIT_FOR_MORE;
                        }
                        case '\'': {
                            mode = QUOTE1;
                            wasEscape = false;
                            return GlobberRet.WAIT_FOR_MORE;
                        }
                        case '"': {
                            mode = QUOTE2;
                            wasEscape = false;
                            return GlobberRet.WAIT_FOR_MORE;
                        }
                        case '`': {
                            mode = AQUOTE;
                            wasEscape = false;
                            return GlobberRet.WAIT_FOR_MORE;
                        }
                        default: {
                            if (next > 0x32) {
                                if (pars == 0 && acc == 0 && sqAcc == 0) {
                                    return GlobberRet.ACCEPT;
                                }
                                return GlobberRet.WAIT_FOR_MORE;
                            } else {
                                if (pars == 0 && acc == 0 && sqAcc == 0) {
                                    return GlobberRet.REJECT_LAST;
                                }
                                return GlobberRet.WAIT_FOR_MORE;
                            }
                        }
                    }
                }
                case QUOTE1: {
                    switch (next) {
                        case '\\': {
                            if (wasEscape) {
                                wasEscape = false;
                                return GlobberRet.WAIT_FOR_MORE;
                            } else {
                                wasEscape = true;
                            }
                            break;
                        }
                        case '\'': {
                            if (wasEscape) {
                                wasEscape=false;
                                return GlobberRet.WAIT_FOR_MORE;
                            }else{
                                mode=PLAIN;
                                if(pars==0 && acc==0 && sqAcc==0) {
                                    return GlobberRet.ACCEPT;
                                }
                                return GlobberRet.WAIT_FOR_MORE;
                            }
                        }
                        default:{
                            return GlobberRet.WAIT_FOR_MORE;
                        }
                    }
                }
                case QUOTE2: {
                    switch (next) {
                        case '\\': {
                            if (wasEscape) {
                                wasEscape = false;
                                return GlobberRet.WAIT_FOR_MORE;
                            } else {
                                wasEscape = true;
                            }
                            break;
                        }
                        case '"': {
                            if (wasEscape) {
                                wasEscape=false;
                                return GlobberRet.WAIT_FOR_MORE;
                            }else{
                                mode=PLAIN;
                                if(pars==0 && acc==0 && sqAcc==0) {
                                    return GlobberRet.ACCEPT;
                                }
                                return GlobberRet.WAIT_FOR_MORE;
                            }
                        }
                        default:{
                            return GlobberRet.WAIT_FOR_MORE;
                        }
                    }
                }
                case AQUOTE: {
                    switch (next) {
                        case '\\': {
                            if (wasEscape) {
                                wasEscape = false;
                                return GlobberRet.WAIT_FOR_MORE;
                            } else {
                                wasEscape = true;
                            }
                            break;
                        }
                        case '`': {
                            if (wasEscape) {
                                wasEscape=false;
                                return GlobberRet.WAIT_FOR_MORE;
                            }else{
                                mode=PLAIN;
                                if(pars==0 && acc==0 && sqAcc==0) {
                                    return GlobberRet.ACCEPT;
                                }
                                return GlobberRet.WAIT_FOR_MORE;
                            }
                        }
                        default:{
                            return GlobberRet.WAIT_FOR_MORE;
                        }
                    }
                }
                default:{
                    throw new IllegalArgumentException("impossible");
                }
            }
        }
    }

    public class Token {
        public final String image;
        public final String sval;

        public Token(String image, String sval) {
            this.image = image;
            this.sval = sval;
        }
    }
}
