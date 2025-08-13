package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NToken;

import java.io.StringReader;
import java.util.*;

public class SyntaxParser {
    NTokenIterator tokens;
    //    NutsExpr evaluator;
    NExprWithCache withCache;

    public SyntaxParser(String anyStr, NExprWithCache withCache) {
        this(new NTokenIterator(new StringReader(anyStr == null ? "" : anyStr)), withCache);
    }


    public SyntaxParser(NTokenIterator tokens, NExprWithCache withCache) {
        this.tokens = tokens;
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
                    () -> NMsg.ofC("unexpected token %s, after reading %s", peeked, e.get())
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

//    int comparePrecedenceIndex(NExprOpDeclaration op, int precedenceIndex) {
//        int v = withCache.precedenceIndex(op.getPrecedence());
//        return Integer.compare(v, precedenceIndex);
//    }

    boolean isOpenPar(NToken t, NExprOpType opType) {
        if (t == null) {
            return false;
        }
        switch (t.ttype) {
            case '(':
            case '[':
            case '{':
                return withCache.getOp(t, opType) != null;
        }
        return false;
    }

    boolean isOpIgnoresMissingSecondOperand(NToken t, NExprOpType opType) {
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

    boolean isOpAcceptsMissingSecondOperand(NToken t, NExprOpType opType) {
        if (t == null) {
            return false;
        }
//        switch (t.ttype) {
//        }
        return false;
    }


    private NOptional<NExprNode> nextNonTerminalIf() {
        NToken t = peekSkipSpace();
        if (t == null) {
            return NOptional.ofError(() -> NMsg.ofPlain("expected if"));
        }
        if (!isNonTerminalIf(t)) {
            return NOptional.ofError(() -> NMsg.ofPlain("expected if"));
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
                return NOptional.ofError(() -> NMsg.ofPlain("expected else statement"));
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
        while (true) {
            NToken t = tokens.peek();
            if (t != null) {
                if (t.ttype != NToken.TT_SPACE) {
                    return t;
                } else {
                    //skip space
                    tokens.next();
                }
            } else {
                return null;
            }
        }
    }

    private NOptional<NExprNode> nextTerminalOrStmt() {
        NToken t = peekSkipSpace();
        if (t == null) {
            return NOptional.ofError(() -> NMsg.ofPlain("expected non terminal"));
        }
        if (isNonTerminalIf(t)) {
            return nextNonTerminalIf();
        }
        return nextTerminal();
    }

    private NOptional<NExprNode> _nextOpenPar(NToken t) {
        if (isOpenParStart(t)) {
            NToken startPar = tokens.next();
            NToken p2 = peekSkipSpace();
            if (p2 == null) {
                NToken finalT = t;
                return NOptional.ofError(() -> NMsg.ofPlain("expected closing " + finalT.sval));
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
                    return NOptional.ofError(() -> NMsg.ofPlain("expected closing " + finalT.sval));
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
                    return NOptional.ofError(() -> NMsg.ofPlain("expected ','"));
                }
            }
        }
        return null;
    }

    private NOptional<NExprNode> _nextPrefixOp(NToken t, int precedence) {
        NExprOpDeclaration op = withCache.getOp(t, NExprOpType.PREFIX);
        if (op != null && !(op.getPrecedence() < precedence)) {
            tokens.next();
            NOptional<NExprNode> q = nextNonTerminal(precedence);
            if (q.isEmpty()) {
                return NOptional.ofError(() -> NMsg.ofPlain("expected expression"));
            }
            if (q.isError()) {
                return q;
            }
            return NOptional.of(
                    new DefaultOpNode(t.image, opName(t), NExprOpType.PREFIX, op.getPrecedence(), Arrays.asList(q.get()))
            );
        }
        return nextTerminalOrStmt();
//        return nextNonTerminal(precedenceIndex + 1);
    }

    private NOptional<NExprNode> _nextPostfixOp(NOptional<NExprNode> first, int precedence) {
        while (true) {
            NToken t = peekSkipSpace();
            if (t == null) {
                break;
            }
            if (isOpenPar(t, NExprOpType.POSTFIX)) {
                NOptional<NExprNode> e = nextTerminalOrStmt();
                NOptional<NExprNode> finalFirst = first;
                NToken finalInfixOp = t;
                first = e.map(x -> {
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
            } else {
                NExprOpDeclaration op = withCache.getOp(t, NExprOpType.POSTFIX);
                if (op != null && !(op.getPrecedence() < precedence)) {
                    tokens.next();
                    first = NOptional.of(new DefaultOpNode(t.sval, opName(t), NExprOpType.POSTFIX, op.getPrecedence(), Arrays.asList(first.get())));
                } else {
                    break;
                }
            }
        }
        return first;
    }


    private NOptional<NExprNode> nextNonTerminal(int precedence) {
//        if (precedence < 0) {
//            return NOptional.ofNamedEmpty("non-terminal");
//        }
//        if (precedence >= Integer.MAX_VALUE) {
//            return nextTerminalOrStmt();
//        }
        NToken t = peekSkipSpace();
        if (t == null) {
            return NOptional.ofError(() -> NMsg.ofPlain("expected non terminal"));
        }
        NOptional<NExprNode> first = null;
        if (precedence == 0) {
            NOptional<NExprNode> v = _nextOpenPar(t);
            if (v != null) {
                first = v;
                if (first != null) {
                    if (first.isEmpty()) {
                        return NOptional.ofError(() -> NMsg.ofPlain("expected expression"));
                    }
                    if (first.isError()) {
                        return first;
                    }
                }
            }
        }

        if (first == null) {
            first = _nextPrefixOp(t, precedence);
            if (first.isEmpty()) {
                return NOptional.ofError(() -> NMsg.ofPlain("expected expression"));
            }
            if (first.isError()) {
                return first;
            }
        }

        first = _nextInfixOp(first, precedence);
        if (first.isError()) {
            return first;
        }

        return _nextPostfixOp(first, precedence);
    }

    private NOptional<NExprNode> _nextInfixOp(NOptional<NExprNode> first, int precedence) {
        while (true) {
            NToken infixOp = peekSkipSpace();
            if (infixOp == null) {
                break;
            }
            NExprOpDeclaration op = withCache.getOp(infixOp, NExprOpType.INFIX);
            if (op == null) {
                break;
            }
            if (op.getPrecedence() < precedence) {
                break;
            }
            tokens.next();
            int nextPrecedence = op.getPrecedence();
            if (op.getAssociativity() == NExprOpAssociativity.LEFT) {
                nextPrecedence--;
            }
            NOptional<NExprNode> q = nextNonTerminal(nextPrecedence);

            if (q.isEmpty()) {
                if (isOpIgnoresMissingSecondOperand(infixOp, NExprOpType.INFIX)) {
                    //do nothing
                } else if (isOpAcceptsMissingSecondOperand(infixOp, NExprOpType.INFIX)) {
                    first = NOptional.of(createInfixOpNodeOrCombine(infixOp.sval, opName(infixOp), op.getPrecedence(), first.get(), null));
                } else {
                    return NOptional.ofError(() -> NMsg.ofPlain("expected expression"));
                }
            } else if (q.isError()) {
                if (isOpIgnoresMissingSecondOperand(infixOp, NExprOpType.INFIX)) {
                    //do nothing
                } else if (isOpAcceptsMissingSecondOperand(infixOp, NExprOpType.INFIX)) {
                    first = NOptional.of(createInfixOpNodeOrCombine(infixOp.sval, opName(infixOp), op.getPrecedence(), first.get(), null));
                } else {
                    return q;
                }
            } else {
                first = NOptional.of(createInfixOpNodeOrCombine(infixOp.sval, opName(infixOp), op.getPrecedence(), first.get(), q.get()));
            }
        }
        return first;
    }

    private NExprOpNode createInfixOpNodeOrCombine(String name, String uniformName, int precedence, NExprNode a, NExprNode b) {
        if (isInfixOpZipped(name, uniformName)) {
            if ((a != null && a.getName().equals(name)) || (b != null && b.getName().equals(name))) {
                List<NExprNode> aa = new ArrayList<>();
                if ((a != null && a.getName().equals(name))) {
                    aa.addAll(a.getChildren());
                } else {
                    aa.add(a);
                }
                if ((b != null && b.getName().equals(name))) {
                    aa.addAll(b.getChildren());
                } else {
                    aa.add(b);
                }
                return new DefaultOpNode(name, uniformName, NExprOpType.INFIX, precedence, aa);
            }
        }
        return new DefaultOpNode(name, uniformName, NExprOpType.INFIX, precedence, new ArrayList<>(Arrays.asList(a, b)));
    }


    private NOptional<NExprNode> nextTerminal() {
        NToken t = peekSkipSpace();
        if (t == null) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("expected terminal"));
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
                            return NOptional.ofError(() -> NMsg.ofPlain("empty expression"));
                        }
                        all.add(e.get());
                        t = peekSkipSpace();
                        if (t.ttype == ')') {
                            t = tokens.next();
                            break;
                        } else if (t.ttype == ',') {
                            tokens.next();
                        } else {
                            return NOptional.ofError(() -> NMsg.ofPlain("expected ',' or ')'"));
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
                            return NOptional.ofError(() -> NMsg.ofPlain("empty expression"));
                        }
                        all.add(e.get());
                        t = peekSkipSpace();
                        if (t.ttype == ']') {
                            t = tokens.next();
                            break;
                        } else if (t.ttype == ',') {
                            tokens.next();
                        } else {
                            return NOptional.ofError(() -> NMsg.ofPlain("expected ',' or ']'"));
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
                            return NOptional.ofError(() -> NMsg.ofPlain("empty expression"));
                        }
                        all.add(e.get());
                        t = peekSkipSpace();
                        if (t.ttype == '}') {
                            t = tokens.next();
                            break;
                        } else if (t.ttype == ',') {
                            tokens.next();
                        } else {
                            return NOptional.ofError(() -> NMsg.ofPlain("expected ',' or '}'"));
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
                            return NOptional.ofError(() -> NMsg.ofPlain("expected expression"));
                        }
                        if (e.isError()) {
                            return e;
                        }
                        functionParams.add(e.get());
                        while (true) {
                            t = peekSkipSpace();
                            if(t==null){
                                break;
                            }
                            if (t.ttype == ')') {
                                tokens.next();
                                break;
                            }
                            if (t.ttype == ',') {
                                tokens.next();
                                e = nextExpr();
                                if (e.isEmpty()) {
                                    return NOptional.ofError(() -> NMsg.ofPlain("expected expression"));
                                }
                                if (e.isError()) {
                                    return e;
                                }
                                functionParams.add(e.get());
                            } else {
                                return NOptional.ofError(() -> NMsg.ofPlain("expected ',' or ')'"));
                            }
                        }
                    }
                    return NOptional.of(new DefaultFunctionNode(n, functionParams.toArray(new NExprNode[0])));
                } else {
                    return NOptional.of(new DefaultWordNode(n));
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
                if (t.image.charAt(0) == '$') {
                    return NOptional.of(new DefaultNExprInterpolatedStrNode(t.sval));
                }
                return NOptional.of(new DefaultLiteralNode(t.sval));
            }
        }
        NToken ftok = t;
        return NOptional.ofError(() -> NMsg.ofC("unsupported %s", ftok));
    }

}
