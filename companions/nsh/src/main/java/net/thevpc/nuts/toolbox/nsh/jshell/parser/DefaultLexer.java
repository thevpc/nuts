package net.thevpc.nuts.toolbox.nsh.jshell.parser;

import java.util.*;

public class DefaultLexer extends AbstractLexer {
    private final JShellParser jShellParser;
    public Stack<Context> ctx = new Stack<>();
    private LinkedList<Token> tokensBuffer = new LinkedList<>();

    public DefaultLexer(JShellParser jShellParser) {
        this.jShellParser = jShellParser;
    }

    public Token continueReadDollarWord() {
        StrReader reader = jShellParser.strReader();
        int char0 = reader.peekChar();
        if (char0 == '$') {
            StringBuilder sb = new StringBuilder();
            reader.read();
            while (true) {
                int n = reader.read();
                if (n < 0) {
                    break;
                }
                char nc = (char) n;
                boolean doBreak = false;
                switch (n) {
                    case '\\':
                    case '/':
                    case '-': {
                        reader.pushBackChar(nc);
                        doBreak = true;
                        break;
                    }
                    default: {
                        if (reader.isWordChar(n)) {
                            sb.append((char) n);
                        } else {
                            reader.pushBackChar(nc);
                            doBreak = true;
                            break;
                        }
                    }
                }
                if (doBreak) {
                    break;
                }
            }
            return new Token("$WORD", sb.toString(), "$"+ sb);
        }
        throw new IllegalArgumentException("unsupported");
    }

    public Token continueReadWord() {
        StrReader reader = jShellParser.strReader();
        int char0 = reader.peekChar();
        if (reader.isWordChar(char0)) {
            StringBuilder sb = new StringBuilder();
            reader.read();
            sb.append((char) char0);
            while (true) {
                int n = reader.read();
                if (n < 0) {
                    break;
                }
                char nc = (char) n;
                boolean doBreak = false;
                switch (n) {
                    case '\\': {
                        n = reader.read();
                        if (n >= 0) {
                            sb.append((char) n);
                        } else {
                            doBreak = true;
                        }
                        break;
                    }
                    default: {
                        if (reader.isWordChar(n)) {
                            sb.append((char) n);
                        } else {
                            reader.pushBackChar(nc);
                            doBreak = true;
                            break;
                        }
                    }
                }
                if (doBreak) {
                    break;
                }
            }
            return new Token("WORD", sb.toString(), sb.toString());
        }
        throw new IllegalArgumentException("unsupported");
    }


    public Token continueReadWhite() {
        StrReader reader = jShellParser.strReader();
        int r = reader.peekChar();
        if (r == '\n') {
            StringBuilder sb = new StringBuilder();
            reader.read();
            sb.append((char) r);
            return new Token("NEWLINE", sb.toString(), sb.toString());
        }
        if (r == '\r') {
            StringBuilder sb = new StringBuilder();
            sb.append((char) reader.read());
            r = reader.peekChar();
            if (r == '\n') {
                sb.append((char) reader.read());
            }
            return new Token("NEWLINE", sb.toString(), sb.toString());
        }
        if (r <= 32) {
            StringBuilder sb = new StringBuilder();
            reader.read();
            sb.append((char) r);
            while (true) {
                int r2 = reader.read();
                if (r2 < 0) {
                    break;
                } else if (r2 <= 32 && r2 != '\n' && r2 != '\r') {
                    sb.append((char) r2);
                } else {
                    reader.pushBackChar((char) r2);
                    break;
                }
            }
            return new Token("WHITE", sb.toString(), sb.toString());
        }
        throw new IllegalArgumentException("Unsupported");

    }

    public Token processContext(String prefix, Context t) {
        StrReader reader = jShellParser.strReader();
        List<Token> all = new ArrayList<>();
        Context c = ctx.peek();
        int before = ctx.size();
        ctx.push(t);
        StringBuilder sb=new StringBuilder();
        while (jShellParser.lexer().ctx.peek() != c) {
            Token tt = jShellParser.lexer().nextToken(before);
            if (tt != null) {
                all.add(tt);
                sb.append(tt.getImage());
            } else {
                break;
            }
        }
        return new Token(prefix, all,sb.toString());
    }

    public void popContext() {
        ctx.pop();
    }

    public Iterable<Token> tokens() {
        return new Iterable<Token>() {
            @Override
            public Iterator<Token> iterator() {
                return new Iterator<Token>() {
                    Token t;

                    @Override
                    public boolean hasNext() {
                        t = nextToken();
                        return t != null;
                    }

                    @Override
                    public Token next() {
                        return t;
                    }
                };
            }
        };
    }

    public void pushBackToken(Token t) {
        if (t != null) {
            tokensBuffer.addFirst(t);
        }
    }


    public boolean skipWhitesNewLinesAndComments() {
        boolean some = false;
        while (true) {
            Token t = peekToken();
            if (t == null) {
                return some;
            }
            if (!t.isWhite()
                    && !t.isNewline()
                    && !t.isSharp()
            ) {
                return some;
            }
            some = true;
            nextToken();
        }
    }

    public Token nextNonWhiteToken() {
        while (true) {
            Token t = nextToken();
            if (t == null) {
                return null;
            }
            if (!t.isWhite()) {
                return t;
            }
        }
    }

    public Token peedTokenSafe() {
        Token t = peekToken();
        if (t == null) {
            return new Token("", "", "");
        }
        return t;
    }

    @Override
    public Token peekToken() {
        if (!tokensBuffer.isEmpty()) {
            return tokensBuffer.getFirst();
        } else {
            while (true) {
                if (ctx.isEmpty()) {
                    return null;
                }
                Token u = ctx.peek().nextToken();
                if (u != null) {
                    tokensBuffer.add(u);
                    return u;
                }
                ctx.pop();
            }
        }
    }

    @Override
    public Token nextToken() {
        if (!tokensBuffer.isEmpty()) {
            return tokensBuffer.removeFirst();
        } else {
            while (true) {
                if (ctx.isEmpty()) {
                    return null;
                }
                Token u = ctx.peek().nextToken();
                if (u != null) {
                    return u;
                }
                ctx.pop();
            }
        }
    }

    public Token nextToken(int before) {
        while (true) {
            if (ctx.isEmpty()) {
                return null;
            }
            Token u = ctx.peek().nextToken();
            if (u != null) {
                return u;
            }
            ctx.pop();
            if (ctx.size() <= before) {
                return null;
            }
        }
    }
}
