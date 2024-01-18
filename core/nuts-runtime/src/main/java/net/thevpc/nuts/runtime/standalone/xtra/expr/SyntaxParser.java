package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprOpNode;
import net.thevpc.nuts.expr.NToken;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NMsg;
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
        NToken peeked = peekSkipSpace();
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

    boolean isNonTerminalIf(NToken t) {
        if (t == null) {
            return false;
        }
        if ("if".equals(t.sval)) {
            return true;
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

    boolean isOpIgnoresMissingSecondOperand(NToken t, NExprOpType opType, int precedenceIndex) {
        if (t == null) {
            return false;
        }
        switch (t.ttype) {
            case ';':
                return true;
        }
        return false;
    }
    private boolean isInfixOpZipped(String name, String uniformName) {
        if (name == null) {
            return false;
        }
        switch (name) {
            case ";":
                return true;
        }
        return false;
    }
    boolean isOpAcceptsMissingSecondOperand(NToken t, NExprOpType opType, int precedenceIndex) {
        if (t == null) {
            return false;
        }
//        switch (t.ttype) {
//        }
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

    private NOptional<NExprNode> nextNonTerminalIf() {
        NToken t = peekSkipSpace();
        if (t == null) {
            return NOptional.ofError(s -> NMsg.ofPlain("expected if"));
        }
        if (!isNonTerminalIf(t)) {
            return NOptional.ofError(s -> NMsg.ofPlain("expected if"));
        }
        NToken startPar = tokens.next();
        NOptional<NExprNode> cond = nextExpr();
        if (cond.isNotPresent()) {
            return NOptional.ofNamedEmpty("condition for if statement");
        }
        NOptional<NExprNode> trueNode = nextExpr();
        if (cond.isNotPresent()) {
            return NOptional.ofNamedEmpty("true statement for if statement");
        }
        t = peekSkipSpace();
        if (t != null && "else".equals(t.sval)) {
            tokens.next();
            t = peekSkipSpace();
            if (t != null) {
                NOptional<NExprNode> falseNode = nextExpr();
                if (falseNode.isNotPresent()) {
                    return NOptional.ofNamedEmpty("false statement for if statement");
                } else {
                    return NOptional.of(new DefaultIfNode(
                            cond.get(),
                            trueNode.get(),
                            falseNode.get()
                    ));
                }
            } else {
                return NOptional.ofError(s -> NMsg.ofPlain("expected else statement"));
            }
        } else {
            return NOptional.of(new DefaultIfNode(
                    cond.get(),
                    trueNode.get(),
                    null
            ));
        }
    }

    private NToken peekSkipSpace() {
        while (true){
            NToken t = tokens.peek();
            if(t!=null){
                if(t.ttype!=NToken.TT_SPACE){
                    return t;
                }else{
                    //skip space
                    tokens.next();
                }
            }else{
                return null;
            }
        }
    }
    private NOptional<NExprNode> nextTerminalOrStmt() {
        NToken t = peekSkipSpace();
        if (t == null) {
            return NOptional.ofError(s -> NMsg.ofPlain("expected non terminal"));
        }
        if (isNonTerminalIf(t)) {
            return nextNonTerminalIf();
        }
        return nextTerminal();
    }

    private NOptional<NExprNode> nextNonTerminal(int precedenceIndex) {
        if (precedenceIndex == withCache.precedences.length) {
            return nextTerminalOrStmt();
        }
        NToken t = peekSkipSpace();
        if (t == null) {
            return NOptional.ofError(s -> NMsg.ofPlain("expected non terminal"));
        }
        NOptional<NExprNode> first = null;
        if (isOpenParStart(t)) {
            NToken startPar = tokens.next();
            NToken p2 = peekSkipSpace();
            if (p2 == null) {
                NToken finalT = t;
                return NOptional.ofError(s -> NMsg.ofPlain("expected closing " + finalT.sval));
            }
            if (isCloseParStart(p2, t.ttype)) {
                tokens.next();
                return NOptional.of(new DefaultOpNode(t.sval, t.sval + p2.sval, NExprOpType.PREFIX, -1, new ArrayList<>()));
            }
            List<NExprNode> args = new ArrayList<>();
            NOptional<NExprNode> e = nextExpr();
            if (!e.isPresent()) {
                return e;
            }
            args.add(e.get());
            while (true) {
                p2 = peekSkipSpace();
                if (p2 == null) {
                    NToken finalT = t;
                    return NOptional.ofError(s -> NMsg.ofPlain("expected closing " + finalT.sval));
                } else if (isCloseParStart(p2, t.ttype)) {
                    tokens.next();
                    return NOptional.of(new DefaultOpNode(t.sval, t.sval + p2.sval, NExprOpType.PREFIX, -1, args));
                } else if (p2.sval.equals(",")) {
                    tokens.next();
                    e = nextExpr();
                    if (!e.isPresent()) {
                        return e;
                    }
                    args.add(e.get());
                } else {
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
        NToken infixOp = peekSkipSpace();
        if (isOpenPar(infixOp, NExprOpType.POSTFIX, precedenceIndex)) {
            NOptional<NExprNode> e = nextTerminalOrStmt();
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
                if(isOpIgnoresMissingSecondOperand(infixOp, NExprOpType.INFIX, precedenceIndex)){
                    return first;
                }else if(isOpAcceptsMissingSecondOperand(infixOp, NExprOpType.INFIX, precedenceIndex)){
                    first = NOptional.of(new DefaultOpNode(infixOp.sval, opName(infixOp), NExprOpType.INFIX, withCache.precedences[precedenceIndex], Arrays.asList(first.get(), null)));
                }else {
                    return NOptional.ofError(s -> NMsg.ofPlain("expected expression"));
                }
            }else if (q.isError()) {
                if(isOpIgnoresMissingSecondOperand(infixOp, NExprOpType.INFIX, precedenceIndex)){
                    //do nothing
                }else if(isOpAcceptsMissingSecondOperand(infixOp, NExprOpType.INFIX, precedenceIndex)){
                    first = NOptional.of(createInfixOpNodeOrCombine(infixOp.sval, opName(infixOp), withCache.precedences[precedenceIndex], first.get(), null));
                }else {
                    return q;
                }
            }else {
                first = NOptional.of(createInfixOpNodeOrCombine(infixOp.sval, opName(infixOp), withCache.precedences[precedenceIndex], first.get(), q.get()));
            }
            infixOp = peekSkipSpace();
            while (infixOp != null && isOp(infixOp, NExprOpType.INFIX, precedenceIndex)) {
                tokens.next();
                q = nextNonTerminal(precedenceIndex + 1);

                if (q.isEmpty()) {
                    if(isOpIgnoresMissingSecondOperand(infixOp, NExprOpType.INFIX, precedenceIndex)){
                        //do nothing
                    }else if(isOpAcceptsMissingSecondOperand(infixOp, NExprOpType.INFIX, precedenceIndex)){
                        first = NOptional.of(createInfixOpNodeOrCombine(infixOp.sval, opName(infixOp), withCache.precedences[precedenceIndex], first.get(), null));
                    }else {
                        return NOptional.ofError(s -> NMsg.ofPlain("expected expression"));
                    }
                }else if (q.isError()) {
                    if(isOpIgnoresMissingSecondOperand(infixOp, NExprOpType.INFIX, precedenceIndex)){
                        //do nothing
                    }else if(isOpAcceptsMissingSecondOperand(infixOp, NExprOpType.INFIX, precedenceIndex)){
                        first = NOptional.of(createInfixOpNodeOrCombine(infixOp.sval, opName(infixOp), withCache.precedences[precedenceIndex], first.get(), null));
                    }else {
                        return q;
                    }
                }else {
                    first = NOptional.of(createInfixOpNodeOrCombine(infixOp.sval, opName(infixOp), withCache.precedences[precedenceIndex], first.get(), q.get()));
                }
                infixOp = peekSkipSpace();
            }
            return first;
        } else if (isOp(infixOp, NExprOpType.POSTFIX, precedenceIndex)) {
            tokens.next();
            return NOptional.of(new DefaultOpNode(infixOp.sval, opName(infixOp), NExprOpType.POSTFIX, withCache.precedences[precedenceIndex], Arrays.asList(first.get())));
        } else {
            return first;
        }
    }

    private NExprOpNode createInfixOpNodeOrCombine(String name, String uniformName, int precedence, NExprNode a, NExprNode b) {
        if(isInfixOpZipped(name,uniformName)){
            if((a!=null && a.getName().equals(name)) || (b!=null && b.getName().equals(name))){
                List<NExprNode> aa=new ArrayList<>();
                if((a!=null && a.getName().equals(name))) {
                    aa.addAll(a.getChildren());
                }else{
                    aa.add(a);
                }
                if((b!=null && b.getName().equals(name))) {
                    aa.addAll(b.getChildren());
                }else{
                    aa.add(b);
                }
                return new DefaultOpNode(name, uniformName, NExprOpType.INFIX, precedence, aa);
            }
        }
        return new DefaultOpNode(name, uniformName, NExprOpType.INFIX, precedence, new ArrayList<>(Arrays.asList(a,b)));
    }



    private NOptional<NExprNode> nextTerminal() {
        NToken t = peekSkipSpace();
        if (t == null) {
            return NOptional.ofEmpty(s -> NMsg.ofPlain("expected terminal"));
        }
        switch (t.ttype) {
            case '(': {
                NToken t0 = t;
                tokens.next();
                t = peekSkipSpace();
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
                        t = peekSkipSpace();
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
                t = peekSkipSpace();
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
                        t = peekSkipSpace();
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
                t = peekSkipSpace();
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
                        t = peekSkipSpace();
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
                t = peekSkipSpace();
                if (t != null && t.ttype == '(') {
                    //function
                    List<NExprNode> functionParams = new ArrayList<>();
                    tokens.next();
                    t = peekSkipSpace();
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
                            t = peekSkipSpace();
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
