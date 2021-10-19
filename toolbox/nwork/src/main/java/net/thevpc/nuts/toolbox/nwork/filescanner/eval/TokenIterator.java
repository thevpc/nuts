package net.thevpc.nuts.toolbox.nwork.filescanner.eval;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Iterator;

class TokenIterator implements Iterator<Token> {
    private final StreamTokenizer st;
    private Token previous;
    private boolean returnSpace=false;
    private boolean returnComment=false;
    private boolean doReplay;

    public TokenIterator(Reader r) {
        this.st=new StreamTokenizer(r);
    }

    public void pushBack() {
        doReplay = true;
    }

    public Token peek() {
        if (doReplay) {
            return previous;
        }
        if (hasNext()) {
            Token n = next();
            doReplay = true;
            return n;
        }
        return null;
    }

    public Token read() {
        if (hasNext()) {
            return next();
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        if (doReplay) {
            return true;
        }
        while (true) {
            int nt = StreamTokenizer.TT_EOF;
            try {
                nt = st.nextToken();
            } catch (IOException e) {
                return false;
            }
            switch (nt) {
                case StreamTokenizer.TT_EOF: {
                    previous = null;
                    return false;
                }
                case ' ':
                case '\t':
                case StreamTokenizer.TT_EOL: {
                    if(returnSpace) {
                        previous = new Token(Token.TT_SPACE, st.sval, st.nval, st.lineno());
                        return true;
                    }
                    break;
                }
                default: {
                    switch (st.ttype) {
                        case ' ':
                        case '\n':
                        case '\r':
                        case '\t': {
                            if(returnSpace) {
                                previous = new Token(Token.TT_SPACE, st.sval, st.nval, st.lineno());
                                return true;
                            }
                            break;
                        }
                        case '(': {
                            previous = new Token(Token.TT_OPEN_PAR, st.sval, st.nval, st.lineno());
                            return true;
                        }
                        case ')': {
                            previous = new Token(Token.TT_CLOSE_PAR, st.sval, st.nval, st.lineno());
                            return true;
                        }
                        case '&': {
                            previous = new Token(Token.TT_AND, "&", st.nval, st.lineno());
                            return true;
                        }
                        case '|': {
                            previous = new Token(Token.TT_OR, "|", st.nval, st.lineno());
                            return true;
                        }
                        case ',': {
                            previous = new Token(Token.TT_COMMA, ",", st.nval, st.lineno());
                            return true;
                        }
                        case '\"': {
                            String sval = st.sval;
                            previous = new Token(Token.TT_STRING_LITERAL, sval, st.nval, st.lineno());
                            return true;
                        }
                        case '\'': {
                            String sval = st.sval;
                            previous = new Token(Token.TT_STRING_LITERAL, sval, st.nval, st.lineno());
                            return true;
                        }
                        case Token.TT_WORD: {
                            switch (String.valueOf(st.sval).trim().toLowerCase()) {
                                case "and": {
                                    previous = new Token(Token.TT_AND, st.sval, st.nval, st.lineno());
                                    return true;
                                }
                                case "or": {
                                    previous = new Token(Token.TT_OR, st.sval, st.nval, st.lineno());
                                    return true;
                                }
                                case "not": {
                                    previous = new Token(Token.TT_NOT, st.sval, st.nval, st.lineno());
                                    return true;
                                }
                            }
                        }
                        default:{
                            previous = new Token(st.ttype, st.sval, st.nval, st.lineno());
                            return true;
                        }
                    }
                }
            }
        }
    }

    @Override
    public Token next() {
        if (doReplay) {
            doReplay = false;
        }
        return previous;
    }
}
