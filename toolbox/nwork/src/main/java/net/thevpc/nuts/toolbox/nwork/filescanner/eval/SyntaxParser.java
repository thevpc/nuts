package net.thevpc.nuts.toolbox.nwork.filescanner.eval;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SyntaxParser {
    TokenIterator tokens;
    Evaluator evaluator;

    public SyntaxParser(String anyStr,Evaluator evaluator) {
        this(new TokenIterator(new StringReader(anyStr == null ? "" : anyStr)),evaluator);
    }

    public SyntaxParser(TokenIterator tokens,Evaluator evaluator) {
        this.tokens = tokens;
        this.evaluator = evaluator;
    }

    public Evaluator.Node parse() {
        return nextExpr();
    }


    private Evaluator.Node nextExpr() {
        return nextOr();
    }

    private Evaluator.Node nextOr() {
        Evaluator.Node i = nextAnd();
        if (i == null) {
            return null;
        }
        List<Evaluator.Node> all = new ArrayList<>();
        all.add(i);
        while (true) {
            Token t = tokens.peek();
            if (t != null && t.ttype == Token.TT_OR) {
                tokens.next();
                Evaluator.Node o = nextAnd();
                if (o != null) {
                    all.add(o);
                }else{
                    throw new IllegalArgumentException("expected expr");
                }
            } else {
                break;
            }
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return evaluator.createFunction("or", all.toArray(new Evaluator.Node[0]));
    }

    private Evaluator.Node nextAnd() {
        Evaluator.Node i = nextComp();
        if (i == null) {
            return null;
        }
        List<Evaluator.Node> all = new ArrayList<>();
        all.add(i);
        while (true) {
            Token t = tokens.peek();
            if (t != null && t.ttype == Token.TT_AND) {
                tokens.next();
                Evaluator.Node o = nextComp();
                if (o != null) {
                    all.add(o);
                }else{
                    throw new IllegalArgumentException("expected expr");
                }
            } else {
                break;
            }
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return evaluator.createFunction("and", all.toArray(new Evaluator.Node[0]));
    }

    private Evaluator.Node nextComp() {
        Evaluator.Node arg1 = nextPlus();
        if (arg1 == null) {
            return null;
        }
        Token t = tokens.peek();
        if (t == null) {
            return arg1;
        }
        if (t.ttype == '<' || t.ttype == '>' || t.ttype == '=') {
            tokens.next();
            String op = String.valueOf(t.ttype);
            t = tokens.peek();
            if (t != null && t.ttype == '=') {
                tokens.next();
                op += String.valueOf(t.ttype);
            }
            Evaluator.Node arg2 = nextPlus();
            if (arg2 == null) {
                throw new IllegalArgumentException("expected expr");
            }
            return evaluator.createFunction(op, new Evaluator.Node[]{arg1, arg2});
        }
        return arg1;
    }

    private Evaluator.Node nextPlus() {
        Evaluator.Node arg1 = nextMul();
        if (arg1 == null) {
            return null;
        }
        Token t = tokens.peek();
        if (t == null) {
            return arg1;
        }
        if (t.ttype == '+' || t.ttype == '-') {
            tokens.next();
            String op = String.valueOf(t.ttype);
            Evaluator.Node arg2 = nextMul();
            if (arg2 == null) {
                throw new IllegalArgumentException("expected expr");
            }
            return evaluator.createFunction(op, new Evaluator.Node[]{arg1, arg2});
        }
        return arg1;
    }

    private Evaluator.Node nextMul() {
        Evaluator.Node arg1 = nextNot();
        if (arg1 == null) {
            return null;
        }
        Token t = tokens.peek();
        if (t == null) {
            return arg1;
        }
        if (t.ttype == '*' || t.ttype == '/') {
            tokens.next();
            String op = String.valueOf(t.ttype);
            Evaluator.Node arg2 = nextNot();
            if (arg2 == null) {
                throw new IllegalArgumentException("expected expr");
            }
            return evaluator.createFunction(op, new Evaluator.Node[]{arg1, arg2});
        }
        return arg1;
    }

    private Evaluator.Node nextNot() {
        Token t = tokens.peek();
        if (t.ttype == Token.TT_NOT) {
            Evaluator.Node e = nextNot();
            if (e == null) {
                throw new IllegalArgumentException("expected expr");
            }
            return evaluator.createFunction("not", new Evaluator.Node[]{e});
        }
        return nextItem();
    }

    private Evaluator.Node nextItem() {
        Token t = tokens.peek();
        if (t == null) {
            return null;
        }
        switch (t.ttype) {
            case Token.TT_OPEN_PAR: {
                tokens.next();
                t = tokens.peek();
                if (t.ttype == Token.TT_CLOSE_PAR) {
                    throw new IllegalArgumentException("empty par");
                }
                Evaluator.Node e = nextExpr();
                if (e == null) {
                    throw new IllegalArgumentException("expected expr");
                }
                return e;
            }
            case Token.TT_WORD: {
                String n = t.sval;
                tokens.next();
                t = tokens.peek();
                if (t.ttype == Token.TT_OPEN_PAR) {
                    //function
                    List<Evaluator.Node> functionParams = new ArrayList<>();
                    tokens.next();
                    t = tokens.peek();
                    if (t.ttype == Token.TT_CLOSE_PAR) {
                        tokens.next();
                        //okkay
                    } else {
                        Evaluator.Node e = nextExpr();
                        if (e == null) {
                            throw new IllegalArgumentException("expected expr");
                        }
                        functionParams.add(e);
                        while (true) {
                            t = tokens.peek();
                            if (t.ttype == Token.TT_CLOSE_PAR) {
                                tokens.next();
                                break;
                            }
                            if (t.ttype == Token.TT_COMMA) {
                                tokens.next();
                                e = nextExpr();
                                if (e == null) {
                                    throw new IllegalArgumentException("expected expr");
                                }
                                functionParams.add(e);
                            } else {
                                throw new IllegalArgumentException("expected ',' or ')'");
                            }
                        }
                    }
                    return evaluator.createFunction(n, functionParams.toArray(new Evaluator.Node[0]));
                } else {
                    return evaluator.createVar(n);
                }
            }
            case Token.TT_NUMBER: {
                tokens.next();
                return evaluator.createLiteral(t.nval);
            }
            case Token.TT_STRING_LITERAL: {
                tokens.next();
                return evaluator.createLiteral(t.sval);
            }
        }
        throw new IllegalArgumentException("unsupported " + t);
    }

}
