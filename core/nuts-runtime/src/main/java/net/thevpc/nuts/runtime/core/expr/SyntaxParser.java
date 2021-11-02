package net.thevpc.nuts.runtime.core.expr;

import net.thevpc.nuts.NutsExpr;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;

import java.io.StringReader;
import java.util.*;

public class SyntaxParser {
    TokenIterator tokens;
    NutsExpr evaluator;
    NutsExprWithCache withCache;

    public SyntaxParser(String anyStr, NutsExprWithCache withCache) {
        this(new TokenIterator(new StringReader(anyStr == null ? "" : anyStr)), withCache);
    }


    public SyntaxParser(TokenIterator tokens, NutsExprWithCache withCache) {
        this.tokens = tokens;
        this.withCache = withCache;
        this.evaluator = withCache.getEvaluator();
    }

    public NutsExpr.Node parse() {
        NutsExpr.Node e = nextExpr();
        if(tokens.peek()!=null){
            throw new NutsIllegalArgumentException(evaluator.getSession(),NutsMessage.cstyle("unexpected token %s, after reading %s",tokens.peek(),e));
        }
        return e;
    }

    private NutsExpr.Node nextExpr() {
        return nextNonTerminal(0);
    }

    String opName(NutsToken t){
        if(t==null){
            return null;
        }
        switch (t.ttype){
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
        String s=t.sval;
        if(s==null){
            s=String.valueOf((char)t.ttype);
        }
        return s;
    }

    boolean isOp(NutsToken t, NutsExpr.OpType opType, int precedenceIndex){
        if(t==null){
            return false;
        }
        switch (t.ttype){
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
        String s=t.sval;
        if(s==null){
            s=String.valueOf((char)t.ttype);
        }
        return withCache.isOp(s,opType, precedenceIndex);
    }
    private NutsExpr.Node nextNonTerminal(int precedenceIndex) {
        if (precedenceIndex == withCache.precedences.length) {
            return nextTerminal();
        }
        NutsToken t = tokens.peek();
        if (t == null) {
            return null;
        }
        NutsExpr.Node first = null;
        if (isOp(t, NutsExpr.OpType.PREFIX, precedenceIndex)) {
            tokens.next();
            NutsExpr.Node q = nextNonTerminal(precedenceIndex);
            if (q == null) {
                //error ?
                //q=null
                if (true) {
                    throw new NutsIllegalArgumentException(evaluator.getSession(), NutsMessage.cstyle("expected expression"));
                }
            }
            first = new DefaultOpNode(opName(t), NutsExpr.OpType.PREFIX, withCache.precedences[precedenceIndex], new NutsExpr.Node[]{q});
        } else {
            first = nextNonTerminal(precedenceIndex + 1);
        }
        if (first == null) {
            throw new NutsIllegalArgumentException(evaluator.getSession(), NutsMessage.cstyle("expected expression"));
        }
        NutsToken infixOp = tokens.peek();
        if (isOp(infixOp, NutsExpr.OpType.INFIX, precedenceIndex)) {
            tokens.next();
            //if right associative
//            NutsExpr.Node q = nextNonTerminal(precedenceIndex);
//            if (q == null) {
//                throw new NutsIllegalArgumentException(evaluator.getSession(), NutsMessage.cstyle("expected expression"));
//            }
//            return new DefaultOpNode(opName(infixOp), NutsExpr.OpType.INFIX, withCache.precedences[precedenceIndex], new NutsExpr.Node[]{first,q});
            //else
            NutsExpr.Node q = nextNonTerminal(precedenceIndex+1);
            if (q == null) {
                throw new NutsIllegalArgumentException(evaluator.getSession(), NutsMessage.cstyle("expected expression"));
            }
            first= new DefaultOpNode(opName(infixOp), NutsExpr.OpType.INFIX, withCache.precedences[precedenceIndex], new NutsExpr.Node[]{first,q});
            infixOp = tokens.peek();
            while(infixOp!=null && isOp(infixOp, NutsExpr.OpType.INFIX, precedenceIndex)){
                tokens.next();
                q = nextNonTerminal(precedenceIndex+1);
                if (q == null) {
                    throw new NutsIllegalArgumentException(evaluator.getSession(), NutsMessage.cstyle("expected expression"));
                }
                first= new DefaultOpNode(opName(infixOp), NutsExpr.OpType.INFIX, withCache.precedences[precedenceIndex], new NutsExpr.Node[]{first,q});
                infixOp = tokens.peek();
            }
            return first;
        } else if (isOp(infixOp, NutsExpr.OpType.POSTFIX, precedenceIndex)) {
            tokens.next();
            return new DefaultOpNode(opName(infixOp), NutsExpr.OpType.POSTFIX, withCache.precedences[precedenceIndex], new NutsExpr.Node[]{first});
        } else {
            return first;
        }
    }

    private NutsExpr.Node nextTerminal() {
        NutsToken t = tokens.peek();
        if (t == null) {
            return null;
        }
        switch (t.ttype) {
            case '(': {
                tokens.next();
                t = tokens.peek();
                if (t.ttype == ')') {
                    throw new IllegalArgumentException("empty par");
                }
                NutsExpr.Node e = nextExpr();
                if (e == null) {
                    throw new IllegalArgumentException("expected expr");
                }
                return e;
            }
            case NutsToken.TT_WORD: {
                String n = t.sval;
                tokens.next();
                t = tokens.peek();
                if (t.ttype == '(') {
                    //function
                    List<NutsExpr.Node> functionParams = new ArrayList<>();
                    tokens.next();
                    t = tokens.peek();
                    if (t.ttype == ')') {
                        tokens.next();
                        //okkay
                    } else {
                        NutsExpr.Node e = nextExpr();
                        if (e == null) {
                            throw new IllegalArgumentException("expected expr");
                        }
                        functionParams.add(e);
                        while (true) {
                            t = tokens.peek();
                            if (t.ttype == ')') {
                                tokens.next();
                                break;
                            }
                            if (t.ttype == ',') {
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
                    return new DefaultFctNode(n, functionParams.toArray(new NutsExpr.Node[0]));
                } else {
                    return new DefaultVarNode(n);
                }
            }
            case NutsToken.TT_INT:
            case NutsToken.TT_LONG:
            case NutsToken.TT_BIG_INT:
            case NutsToken.TT_FLOAT:
            case NutsToken.TT_DOUBLE:
            case NutsToken.TT_BIG_DECIMAL: {
                tokens.next();
                return new DefaultLiteralNode(t.nval);
            }
            case NutsToken.TT_STRING_LITERAL: {
                tokens.next();
                return new DefaultLiteralNode(t.sval);
            }
        }
        throw new NutsIllegalArgumentException(this.evaluator.getSession(),NutsMessage.cstyle("unsupported %s",t));
    }

}
