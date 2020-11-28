package net.thevpc.nuts.runtime.format.text.special;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.format.text.TextFormat;
import net.thevpc.nuts.runtime.format.text.parser.*;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

public class ShellSynopsysSpecialTextFormatter implements SpecialTextFormatter {
    private NutsWorkspace ws;

    public ShellSynopsysSpecialTextFormatter(NutsWorkspace ws) {
        this.ws = ws;
    }

    private static boolean isOption(NutsTextNode n) {
        if (n instanceof DefaultNutsTextNodePlain) {
            NutsTextNodePlain n1 = ((NutsTextNodePlain) n);
            String t = n1.getText();
            return t.startsWith("-") || t.startsWith("+");
        }
        if (n instanceof DefaultNutsTextNodeStyled) {
            NutsTextNodeStyled n1 = ((NutsTextNodeStyled) n);
            return isOption(n1.getChild());
        }
        return false;
    }

    private static boolean isPlainString(NutsTextNode n, String name) {
        if (n instanceof DefaultNutsTextNodePlain) {
            NutsTextNodePlain n1 = ((NutsTextNodePlain) n);
            if (n1.getText().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSpace(NutsTextNode n) {
        if (n instanceof DefaultNutsTextNodePlain) {
            NutsTextNodePlain n1 = ((NutsTextNodePlain) n);
            if (n1.getText().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStyledString(NutsTextNode n, String name, TextFormat style) {
        if (n instanceof DefaultNutsTextNodeStyled) {
            DefaultNutsTextNodeStyled n1 = ((DefaultNutsTextNodeStyled) n);
            if (n1.getStyle() == style) {
                NutsTextNode n2 = n1.getChild();
                if (n2 instanceof DefaultNutsTextNodePlain && ((NutsTextNodePlain) n2).getText().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public NutsTextNode toNode(String text) {
        return new Parser(new PushbackReader(new StringReader(text))).parseFull();
    }

    private class Parser {
        PushbackReader r;

        public Parser(PushbackReader r) {
            this.r = r;
        }

        void unread(char c) {
            try {
                r.unread(c);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        int peek() {
            try {
                int r = this.r.read();
                if (r >= 0) {
                    unread((char) r);
                }
                return r;
            } catch (IOException e) {
                return -1;
            }
        }

        int read() {
            try {
                return r.read();
            } catch (IOException e) {
                return -1;
            }
        }

        NutsTextNode parseFull() {
            List<NutsTextNode> all = new ArrayList<>();
            boolean wasSpace = true;
            NutsTextNodeFactory factory = ws.formats().text().factory();
            while (true) {
                NutsTextNode n = parseNext();
                if (n == null) {
                    break;
                }
                if (all.isEmpty() && !isOption(n) && n instanceof DefaultNutsTextNodePlain) {
                    all.add(factory.styled(((NutsTextNodePlain) n).getText(),NutsTextNodeStyle.KEYWORD1));
                } else if (wasSpace && isOption(n)) {
                    all.add(factory.styled(((NutsTextNodePlain) n).getText(), NutsTextNodeStyle.OPTION1));
                } else {
                    all.add(n);
                }
                wasSpace = isSpace(n);
            }
            return factory.list(all);
        }

        NutsTextNode parseNext() {
            NutsTextNodeFactory factory = ws.formats().text().factory();
            int t = peek();
            if (t < 0) {
                return null;
            }
            if (Character.isWhitespace(t)) {
                StringBuilder sb = new StringBuilder();
                while (true) {
                    int y = read();
                    if (y == -1) {
                        break;
                    }
                    char c = (char) y;
                    if (Character.isWhitespace(c)) {
                        sb.append(c);
                    } else {
                        unread(c);
                        break;
                    }
                }
                return factory.plain(sb.toString());
            }
            switch (t) {
                case ')':
                case ']':
                case '}': {
                    read();
                    return factory.styled(String.valueOf((char) t),NutsTextNodeStyle.SEPARATOR1);
                }
                case '>': {
                    read();
                    return factory.styled(String.valueOf((char) t),NutsTextNodeStyle.USER_INPUT1);
                }
                case '(': {
                    read();
                    List<NutsTextNode> a = new ArrayList<>();
                    DefaultNutsTextNodeStyled separator = (DefaultNutsTextNodeStyled) factory.styled(String.valueOf((char) t),NutsTextNodeStyle.SEPARATOR1);
                    a.add(separator);
                    while (true) {
                        NutsTextNode n = parseNext();
                        if (n == null) {
                            break;
                        }
                        a.add(n);
                        if (isStyledString(n, ")", separator.getStyle())) {
                            break;
                        }
                    }
                    return factory.list(a);
                }
                case '[': {
                    read();
                    List<NutsTextNode> a = new ArrayList<>();
                    DefaultNutsTextNodeStyled separator = (DefaultNutsTextNodeStyled) factory.styled(String.valueOf((char) t),NutsTextNodeStyle.SEPARATOR1);
                    a.add(separator);
                    while (true) {
                        NutsTextNode n = parseNext();
                        if (n == null) {
                            break;
                        }
                        a.add(n);
                        if (isStyledString(n, "]", separator.getStyle())) {
                            break;
                        }
                    }
                    return factory.list(a);
                }
                case '{': {
                    read();
                    List<NutsTextNode> a = new ArrayList<>();
                    DefaultNutsTextNodeStyled separator = (DefaultNutsTextNodeStyled) factory.styled(String.valueOf((char) t),NutsTextNodeStyle.SEPARATOR1);
                    a.add(separator);
                    while (true) {
                        NutsTextNode n = parseNext();
                        if (n == null) {
                            break;
                        }
                        a.add(n);
                        if (isStyledString(n, "}", separator.getStyle())) {
                            break;
                        }
                    }
                    return factory.list(a);
                }
                case '<': {
                    read();
                    List<NutsTextNode> a = new ArrayList<>();
                    DefaultNutsTextNodeStyled n0 = (DefaultNutsTextNodeStyled) factory.styled(String.valueOf((char) t),NutsTextNodeStyle.SEPARATOR1);
                    a.add(n0);
                    while (true) {
                        NutsTextNode n = parseNext();
                        if (n == null) {
                            break;
                        }
                        if (isStyledString(n, ">", n0.getStyle())) {
                            a.add(n);
                            break;
                        }else if(n instanceof DefaultNutsTextNodePlain){
                            a.add(factory.styled(((NutsTextNodePlain) n).getText(),NutsTextNodeStyle.USER_INPUT1));
                        }else{
                            a.add(n);
                        }
                    }
                    return factory.list(a);
                }
                case '\"': {
                    read();
                    StringBuilder sb = new StringBuilder();
                    sb.append((char) t);
                    while (true) {
                        int r = read();
                        if (r < 0) {
                            break;
                        }
                        if (r == '\\') {
                            sb.append((char) r);
                            r = read();
                            if (r < 0) {
                                break;
                            }
                            sb.append((char) r);
                        } else if (r == '\"') {
                            sb.append((char) r);
                            break;
                        } else {
                            sb.append((char) r);
                        }
                    }
                    return factory.styled(sb.toString(),NutsTextNodeStyle.STRING1);
                }
                case '\'': {
                    read();
                    StringBuilder sb = new StringBuilder();
                    sb.append((char) t);
                    while (true) {
                        int r = read();
                        if (r < 0) {
                            break;
                        }
                        if (r == '\\') {
                            sb.append((char) r);
                            r = read();
                            if (r < 0) {
                                break;
                            }
                            sb.append((char) r);
                        } else if (r == '\'') {
                            sb.append((char) r);
                            break;
                        } else {
                            sb.append((char) r);
                        }
                    }
                    return factory.styled(sb.toString(),NutsTextNodeStyle.STRING1);
                }
                default: {
                    read();
                    StringBuilder sb = new StringBuilder();
                    sb.append((char) t);
                    while (true) {
                        int r = peek();
                        if (r < 0) {
                            break;
                        }
                        if (Character.isWhitespace(r)) {
                            break;
                        }
                        boolean end = false;
                        switch ((char) r) {
                            case '<':
                            case '>':
                            case '{':
                            case '}':
                            case '[':
                            case ']':
                            case '(':
                            case ')':
                            case '"':
                            case '\'': {
                                end = true;
                                break;
                            }
                            case '\\': {
                                read();
                                sb.append((char) r);
                                r = read();
                                if (r < 0) {
                                    end = true;
                                } else {
                                    sb.append((char) r);
                                }
                                break;
                            }
                            default: {
                                read();
                                sb.append((char) r);
                            }
                        }
                        if (end) {
                            break;
                        }
                    }

                    return factory.plain(sb.toString());
                }
            }
        }
    }
}
