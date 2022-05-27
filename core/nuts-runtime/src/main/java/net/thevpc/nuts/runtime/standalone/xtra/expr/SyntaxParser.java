package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.util.NutsExprNode;
import net.thevpc.nuts.util.NutsExprOpType;

import java.io.StringReader;
import java.util.*;

public class SyntaxParser {
    TokenIterator tokens;
    NutsSession session;
    //    NutsExpr evaluator;
    NutsExprWithCache withCache;

    public SyntaxParser(String anyStr, NutsExprWithCache withCache, NutsSession session) {
        this(new TokenIterator(new StringReader(anyStr == null ? "" : anyStr), session), withCache, session);
    }


    public SyntaxParser(TokenIterator tokens, NutsExprWithCache withCache, NutsSession session) {
        this.tokens = tokens;
        this.session = session;
        this.withCache = withCache;
    }

    public NutsOptional<NutsExprNode> parse() {
        NutsOptional<NutsExprNode> e = nextExpr();
        if (!e.isPresent()) {
            return e;
        }
        NutsToken peeked = tokens.peek();
        if (peeked != null) {
            return NutsOptional.ofError(
                    s -> NutsMessage.ofCstyle("unexpected token %s, after reading %s", peeked, e.get())
            );
        }
        return e;
    }

    private NutsOptional<NutsExprNode> nextExpr() {
        return nextNonTerminal(0);
    }

    String opName(NutsToken t) {
        if (t == null) {
            return null;
        }
        switch (t.ttype) {
            case '(':
            case ')':
            case NutsToken.TT_SPACE:
            case NutsToken.TT_STRING_LITERAL:
            case NutsToken.TT_EOL:
            case NutsToken.TT_EOF:
            case NutsToken.TT_INT:
            case NutsToken.TT_LONG:
            case NutsToken.TT_BIG_INT:
            case NutsToken.TT_FLOAT:
            case NutsToken.TT_DOUBLE:
            case NutsToken.TT_BIG_DECIMAL:
                return null;
        }
        String s = t.sval;
        if (s == null) {
            s = String.valueOf((char) t.ttype);
        }
        return s;
    }

    boolean isOp(NutsToken t, NutsExprOpType opType, int precedenceIndex) {
        if (t == null) {
            return false;
        }
        switch (t.ttype) {
            case '(':
            case ')':
            case NutsToken.TT_SPACE:
            case NutsToken.TT_STRING_LITERAL:
            case NutsToken.TT_EOL:
            case NutsToken.TT_EOF:
            case NutsToken.TT_INT:
            case NutsToken.TT_LONG:
            case NutsToken.TT_BIG_INT:
            case NutsToken.TT_FLOAT:
            case NutsToken.TT_DOUBLE:
            case NutsToken.TT_BIG_DECIMAL:
                return false;
        }
        String s = t.sval;
        if (s == null) {
            s = String.valueOf((char) t.ttype);
        }
        return withCache.isOp(s, opType, precedenceIndex);
    }

    private NutsOptional<NutsExprNode> nextNonTerminal(int precedenceIndex) {
        if (precedenceIndex == withCache.precedences.length) {
            return nextTerminal();
        }
        NutsToken t = tokens.peek();
        if (t == null) {
            return NutsOptional.ofError(s -> NutsMessage.ofPlain("expected non terminal"));
        }
        NutsOptional<NutsExprNode> first = null;
        if (isOp(t, NutsExprOpType.PREFIX, precedenceIndex)) {
            tokens.next();
            NutsOptional<NutsExprNode> q = nextNonTerminal(precedenceIndex);
            if (q.isEmpty()) {
                return NutsOptional.ofError(s -> NutsMessage.ofPlain("expected expression"));
            }
            if (q.isError()) {
                return q;
            }
            first = NutsOptional.of(
                    new DefaultOpNode(opName(t), NutsExprOpType.PREFIX, withCache.precedences[precedenceIndex], Arrays.asList(q.get()))
            );
        } else {
            first = nextNonTerminal(precedenceIndex + 1);
        }
        if (first.isEmpty()) {
            return NutsOptional.ofError(s -> NutsMessage.ofPlain("expected expression"));
        }
        if (first.isError()) {
            return first;
        }
        NutsToken infixOp = tokens.peek();
        if (isOp(infixOp, NutsExprOpType.INFIX, precedenceIndex)) {
            tokens.next();
            //if right associative
//            NutsExpr.Node q = nextNonTerminal(precedenceIndex);
//            if (q == null) {
//                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("expected expression"));
//            }
//            return new DefaultOpNode(opName(infixOp), NutsExpr.OpType.INFIX, withCache.precedences[precedenceIndex], new NutsExpr.Node[]{first,q});
            //else
            NutsOptional<NutsExprNode> q = nextNonTerminal(precedenceIndex + 1);
            if (q.isEmpty()) {
                return NutsOptional.ofError(s -> NutsMessage.ofPlain("expected expression"));
            }
            if (q.isError()) {
                return q;
            }
            first = NutsOptional.of(new DefaultOpNode(opName(infixOp), NutsExprOpType.INFIX, withCache.precedences[precedenceIndex], Arrays.asList(first.get(), q.get())));
            infixOp = tokens.peek();
            while (infixOp != null && isOp(infixOp, NutsExprOpType.INFIX, precedenceIndex)) {
                tokens.next();
                q = nextNonTerminal(precedenceIndex + 1);
                if (q.isEmpty()) {
                    return NutsOptional.ofError(s -> NutsMessage.ofPlain("expected expression"));
                }
                if (q.isError()) {
                    return q;
                }
                first = NutsOptional.of(new DefaultOpNode(opName(infixOp), NutsExprOpType.INFIX, withCache.precedences[precedenceIndex], Arrays.asList(first.get(), q.get())));
                infixOp = tokens.peek();
            }
            return first;
        } else if (isOp(infixOp, NutsExprOpType.POSTFIX, precedenceIndex)) {
            tokens.next();
            return NutsOptional.of(new DefaultOpNode(opName(infixOp), NutsExprOpType.POSTFIX, withCache.precedences[precedenceIndex], Arrays.asList(first.get())));
        } else {
            return first;
        }
    }

    private NutsOptional<NutsExprNode> nextTerminal() {
        NutsToken t = tokens.peek();
        if (t == null) {
            return NutsOptional.ofEmpty(s -> NutsMessage.ofPlain("expected terminal"));
        }
        switch (t.ttype) {
            case '(': {
                tokens.next();
                t = tokens.peek();
                NutsOptional<NutsExprNode> e = nextExpr();
                if (e.isEmpty()) {
                    return NutsOptional.ofError(s -> NutsMessage.ofPlain("empty expression"));
                }
                t = tokens.peek();
                if (t == null || t.ttype != ')') {
                    return NutsOptional.ofError(s -> NutsMessage.ofPlain("expected ')'"));
                }
                tokens.next();
                return e;
            }
            case NutsToken.TT_WORD: {
                String n = t.sval;
                tokens.next();
                t = tokens.peek();
                if (t != null && t.ttype == '(') {
                    //function
                    List<NutsExprNode> functionParams = new ArrayList<>();
                    tokens.next();
                    t = tokens.peek();
                    if (t.ttype == ')') {
                        tokens.next();
                        //okkay
                    } else {
                        NutsOptional<NutsExprNode> e = nextExpr();
                        if (e.isEmpty()) {
                            return NutsOptional.ofError(s -> NutsMessage.ofPlain("expected expression"));
                        }
                        if (e.isError()) {
                            return e;
                        }
                        functionParams.add(e.get());
                        while (true) {
                            t = tokens.peek();
                            if (t.ttype == ')') {
                                tokens.next();
                                break;
                            }
                            if (t.ttype == ',') {
                                tokens.next();
                                e = nextExpr();
                                if (e.isEmpty()) {
                                    return NutsOptional.ofError(s -> NutsMessage.ofPlain("expected expression"));
                                }
                                if (e.isError()) {
                                    return e;
                                }
                                functionParams.add(e.get());
                            } else {
                                return NutsOptional.ofError(s -> NutsMessage.ofPlain("expected ',' or ')'"));
                            }
                        }
                    }
                    return NutsOptional.of(new DefaultFctNode(n, functionParams.toArray(new NutsExprNode[0])));
                } else {
                    return NutsOptional.of(new DefaultVarNode(n));
                }
            }
            case NutsToken.TT_INT:
            case NutsToken.TT_LONG:
            case NutsToken.TT_BIG_INT:
            case NutsToken.TT_FLOAT:
            case NutsToken.TT_DOUBLE:
            case NutsToken.TT_BIG_DECIMAL: {
                tokens.next();
                return NutsOptional.of(new DefaultLiteralNode(t.nval));
            }
            case NutsToken.TT_STRING_LITERAL: {
                tokens.next();
                return NutsOptional.of(new DefaultLiteralNode(t.sval));
            }
        }
        NutsToken ftok = t;
        return NutsOptional.ofError(s -> NutsMessage.ofCstyle("unsupported %s", ftok));
    }

}
