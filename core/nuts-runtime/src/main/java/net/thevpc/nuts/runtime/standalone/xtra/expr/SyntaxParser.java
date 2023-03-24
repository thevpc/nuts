package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.expr.NExprNode;
import net.thevpc.nuts.expr.NExprOpType;

import java.io.StringReader;
import java.util.*;

public class SyntaxParser {
    TokenIterator tokens;
    NSession session;
    //    NutsExpr evaluator;
    NExprWithCache withCache;

    public SyntaxParser(String anyStr, NExprWithCache withCache, NSession session) {
        this(new TokenIterator(new StringReader(anyStr == null ? "" : anyStr), session), withCache, session);
    }


    public SyntaxParser(TokenIterator tokens, NExprWithCache withCache, NSession session) {
        this.tokens = tokens;
        this.session = session;
        this.withCache = withCache;
    }

    public NOptional<NExprNode> parse() {
        NOptional<NExprNode> e = nextExpr();
        if (!e.isPresent()) {
            return e;
        }
        NToken peeked = tokens.peek();
        if (peeked != null) {
            return NOptional.ofError(
                    s -> NMsg.ofC("unexpected token %s, after reading %s", peeked, e.get())
            );
        }
        return e;
    }

    private NOptional<NExprNode> nextExpr() {
        return nextNonTerminal(0);
    }

    String opName(NToken t) {
        if (t == null) {
            return null;
        }
        switch (t.ttype) {
            case '(':
            case ')':
            case '[':
            case ']':
            case '{':
            case '}':
            case NToken.TT_SPACE:
            case NToken.TT_STRING_LITERAL:
            case NToken.TT_EOL:
            case NToken.TT_EOF:
            case NToken.TT_INT:
            case NToken.TT_LONG:
            case NToken.TT_BIG_INT:
            case NToken.TT_FLOAT:
            case NToken.TT_DOUBLE:
            case NToken.TT_BIG_DECIMAL:
                return null;
        }
        String s = t.sval;
        if (s == null) {
            s = String.valueOf((char) t.ttype);
        }
        return s;
    }

    boolean isCloseParStart(NToken t, int ttype) {
        if (t == null) {
            return false;
        }
        switch (ttype) {
            case '(':
                return t.ttype == ')';
            case '[':
                return t.ttype == ']';
            case '{':
                return t.ttype == '}';
        }
        return false;
    }

    boolean isOpenParStart(NToken t) {
        if (t == null) {
            return false;
        }
        switch (t.ttype) {
            case '(':
            case '[':
            case '{':
                return true;
        }
        return false;
    }

    boolean isOpenPar(NToken t, NExprOpType opType, int precedenceIndex) {
        if (t == null) {
            return false;
        }
        switch (t.ttype) {
            case '(':
            case '[':
            case '{':
                return withCache.isOp(t.sval, opType, precedenceIndex);
        }
        return false;
    }

    boolean isOp(NToken t, NExprOpType opType, int precedenceIndex) {
        if (t == null) {
            return false;
        }
        switch (t.ttype) {
            case '(':
            case ')':
            case '[':
            case ']':
            case '{':
            case '}':
            case NToken.TT_SPACE:
            case NToken.TT_STRING_LITERAL:
            case NToken.TT_EOL:
            case NToken.TT_EOF:
            case NToken.TT_INT:
            case NToken.TT_LONG:
            case NToken.TT_BIG_INT:
            case NToken.TT_FLOAT:
            case NToken.TT_DOUBLE:
            case NToken.TT_BIG_DECIMAL:
                return false;
        }
        String s = t.sval;
        if (s == null) {
            s = String.valueOf((char) t.ttype);
        }
        return withCache.isOp(s, opType, precedenceIndex);
    }

    private NOptional<NExprNode> nextNonTerminal(int precedenceIndex) {
        if (precedenceIndex == withCache.precedences.length) {
            return nextTerminal();
        }
        NToken t = tokens.peek();
        if (t == null) {
            return NOptional.ofError(s -> NMsg.ofPlain("expected non terminal"));
        }
        NOptional<NExprNode> first = null;
        if (isOpenParStart(t)) {
            NToken startPar = tokens.next();
            NToken p2 = tokens.peek();
            if (p2==null){
                NToken finalT = t;
                return NOptional.ofError(s -> NMsg.ofPlain("expected closing "+ finalT.sval));
            }
            if(isCloseParStart(p2,t.ttype)){
                tokens.next();
                return NOptional.of(new DefaultOpNode(t.sval, t.sval+p2.sval, NExprOpType.PREFIX, -1, new ArrayList<>()));
            }
            List<NExprNode> args=new ArrayList<>();
            NOptional<NExprNode> e = nextExpr();
            if(!e.isPresent()){
                return e;
            }
            args.add(e.get());
            while(true){
                p2 = tokens.peek();
                if (p2==null){
                    NToken finalT = t;
                    return NOptional.ofError(s -> NMsg.ofPlain("expected closing "+ finalT.sval));
                }else if(isCloseParStart(p2,t.ttype)){
                    tokens.next();
                    return NOptional.of(new DefaultOpNode(t.sval, t.sval+p2.sval, NExprOpType.PREFIX, -1, args));
                }else if (p2.sval.equals(",")) {
                    tokens.next();
                    e = nextExpr();
                    if(!e.isPresent()){
                        return e;
                    }
                    args.add(e.get());
                }else{
                    return NOptional.ofError(s -> NMsg.ofPlain("expected ','"));
                }
            }
        }
        if (isOp(t, NExprOpType.PREFIX, precedenceIndex)) {
            tokens.next();
            NOptional<NExprNode> q = nextNonTerminal(precedenceIndex);
            if (q.isEmpty()) {
                return NOptional.ofError(s -> NMsg.ofPlain("expected expression"));
            }
            if (q.isError()) {
                return q;
            }
            first = NOptional.of(
                    new DefaultOpNode(t.sval, opName(t), NExprOpType.PREFIX, withCache.precedences[precedenceIndex], Arrays.asList(q.get()))
            );
        } else {
            first = nextNonTerminal(precedenceIndex + 1);
        }
        if (first.isEmpty()) {
            return NOptional.ofError(s -> NMsg.ofPlain("expected expression"));
        }
        if (first.isError()) {
            return first;
        }
        NToken infixOp = tokens.peek();
        if (isOpenPar(infixOp, NExprOpType.POSTFIX, precedenceIndex)) {
            NOptional<NExprNode> e = nextTerminal();
            NOptional<NExprNode> finalFirst = first;
            NToken finalInfixOp = infixOp;
            return e.map(x -> {
                List<NExprNode> cc = new ArrayList<>();
                cc.add(finalFirst.get());
                cc.addAll(x.getChildren());
                String opName = "()";
                switch (finalInfixOp.ttype) {
                    case '[': {
                        opName = "[]";
                        break;
                    }
                    case '(': {
                        opName = "()";
                        break;
                    }
                    case '{': {
                        opName = "{}";
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("unsupported");
                    }
                }
                return new DefaultOpNode(finalInfixOp.sval, opName, NExprOpType.POSTFIX, -1, cc);
            });
        }
        if (isOp(infixOp, NExprOpType.INFIX, precedenceIndex)) {
            tokens.next();
            //if right associative
//            NutsExpr.Node q = nextNonTerminal(precedenceIndex);
//            if (q == null) {
//                throw new NutsIllegalArgumentException(session, NMsg.ofC("expected expression"));
//            }
//            return new DefaultOpNode(opName(infixOp), NutsExpr.OpType.INFIX, withCache.precedences[precedenceIndex], new NutsExpr.Node[]{first,q});
            //else
            NOptional<NExprNode> q = nextNonTerminal(precedenceIndex + 1);
            if (q.isEmpty()) {
                return NOptional.ofError(s -> NMsg.ofPlain("expected expression"));
            }
            if (q.isError()) {
                return q;
            }
            first = NOptional.of(new DefaultOpNode(infixOp.sval, opName(infixOp), NExprOpType.INFIX, withCache.precedences[precedenceIndex], Arrays.asList(first.get(), q.get())));
            infixOp = tokens.peek();
            while (infixOp != null && isOp(infixOp, NExprOpType.INFIX, precedenceIndex)) {
                tokens.next();
                q = nextNonTerminal(precedenceIndex + 1);
                if (q.isEmpty()) {
                    return NOptional.ofError(s -> NMsg.ofPlain("expected expression"));
                }
                if (q.isError()) {
                    return q;
                }
                first = NOptional.of(new DefaultOpNode(infixOp.sval, opName(infixOp), NExprOpType.INFIX, withCache.precedences[precedenceIndex], Arrays.asList(first.get(), q.get())));
                infixOp = tokens.peek();
            }
            return first;
        } else if (isOp(infixOp, NExprOpType.POSTFIX, precedenceIndex)) {
            tokens.next();
            return NOptional.of(new DefaultOpNode(infixOp.sval, opName(infixOp), NExprOpType.POSTFIX, withCache.precedences[precedenceIndex], Arrays.asList(first.get())));
        } else {
            return first;
        }
    }

    private NOptional<NExprNode> nextTerminal() {
        NToken t = tokens.peek();
        if (t == null) {
            return NOptional.ofEmpty(s -> NMsg.ofPlain("expected terminal"));
        }
        switch (t.ttype) {
            case '(': {
                NToken t0 = t;
                tokens.next();
                t = tokens.peek();
                List<NExprNode> all = new ArrayList<>();
                if (t.ttype == ')') {
                    t = tokens.next();
                } else {
                    while (true) {
                        NOptional<NExprNode> e = nextExpr();
                        if (e.isEmpty()) {
                            return NOptional.ofError(s -> NMsg.ofPlain("empty expression"));
                        }
                        all.add(e.get());
                        t = tokens.peek();
                        if (t.ttype == ')') {
                            t = tokens.next();
                            break;
                        } else if (t.ttype == ',') {
                            tokens.next();
                        } else {
                            return NOptional.ofError(s -> NMsg.ofPlain("expected ',' or ')'"));
                        }
                    }
                }
                return NOptional.of(
                        new DefaultOpNode(t0.sval, "(",
                                NExprOpType.PREFIX,
                                -1,
                                all
                        )
                );
            }
            case '[': {
                NToken t0 = t;
                tokens.next();
                t = tokens.peek();
                List<NExprNode> all = new ArrayList<>();
                if (t.ttype == ']') {
                    t = tokens.next();

                } else {
                    while (true) {
                        NOptional<NExprNode> e = nextExpr();
                        if (e.isEmpty()) {
                            return NOptional.ofError(s -> NMsg.ofPlain("empty expression"));
                        }
                        all.add(e.get());
                        t = tokens.peek();
                        if (t.ttype == ']') {
                            t = tokens.next();
                            break;
                        } else if (t.ttype == ',') {
                            tokens.next();
                        } else {
                            return NOptional.ofError(s -> NMsg.ofPlain("expected ',' or ']'"));
                        }
                    }
                }
                return NOptional.of(
                        new DefaultOpNode(t0.sval, "[",
                                NExprOpType.PREFIX,
                                -1,
                                all
                        )
                );
            }
            case '{': {
                NToken t0 = t;
                tokens.next();
                t = tokens.peek();
                List<NExprNode> all = new ArrayList<>();
                if (t.ttype == '}') {
                    t = tokens.next();
                } else {
                    while (true) {
                        NOptional<NExprNode> e = nextExpr();
                        if (e.isEmpty()) {
                            return NOptional.ofError(s -> NMsg.ofPlain("empty expression"));
                        }
                        all.add(e.get());
                        t = tokens.peek();
                        if (t.ttype == '}') {
                            t = tokens.next();
                            break;
                        } else if (t.ttype == ',') {
                            tokens.next();
                        } else {
                            return NOptional.ofError(s -> NMsg.ofPlain("expected ',' or '}'"));
                        }
                    }
                }
                return NOptional.of(
                        new DefaultOpNode(t0.sval, "{",
                                NExprOpType.PREFIX,
                                -1,
                                all
                        )
                );
            }
            case NToken.TT_WORD: {
                String n = t.sval;
                tokens.next();
                t = tokens.peek();
                if (t != null && t.ttype == '(') {
                    //function
                    List<NExprNode> functionParams = new ArrayList<>();
                    tokens.next();
                    t = tokens.peek();
                    if (t.ttype == ')') {
                        tokens.next();
                        //okkay
                    } else {
                        NOptional<NExprNode> e = nextExpr();
                        if (e.isEmpty()) {
                            return NOptional.ofError(s -> NMsg.ofPlain("expected expression"));
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
                                    return NOptional.ofError(s -> NMsg.ofPlain("expected expression"));
                                }
                                if (e.isError()) {
                                    return e;
                                }
                                functionParams.add(e.get());
                            } else {
                                return NOptional.ofError(s -> NMsg.ofPlain("expected ',' or ')'"));
                            }
                        }
                    }
                    return NOptional.of(new DefaultFunctionNode(n, functionParams.toArray(new NExprNode[0])));
                } else {
                    return NOptional.of(new DefaultVarNode(n));
                }
            }
            case NToken.TT_INT:
            case NToken.TT_LONG:
            case NToken.TT_BIG_INT:
            case NToken.TT_FLOAT:
            case NToken.TT_DOUBLE:
            case NToken.TT_BIG_DECIMAL: {
                tokens.next();
                return NOptional.of(new DefaultLiteralNode(t.nval));
            }
            case NToken.TT_STRING_LITERAL: {
                tokens.next();
                return NOptional.of(new DefaultLiteralNode(t.sval));
            }
        }
        NToken ftok = t;
        return NOptional.ofError(s -> NMsg.ofC("unsupported %s", ftok));
    }

}
