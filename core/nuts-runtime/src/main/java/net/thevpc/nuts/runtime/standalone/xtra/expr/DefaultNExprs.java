package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.runtime.standalone.reflect.NPlatformSignatureImpl;
import net.thevpc.nuts.util.*;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNExprs implements NExprs {

    private final DefaultNExprsCommonOps defaultNExprsCommonOps = new DefaultNExprsCommonOps();

    DefaultRootContext defaultContext;
    EmptyRootContext emptyContext;
    public DefaultNExprs() {
        defaultContext=new DefaultRootContext(this);
        emptyContext= new EmptyRootContext(this);
    }

    @Override
    public NExprContext emptyContext() {
        return emptyContext;
    }

    @Override
    public NExprContext defaultContext() {
        return defaultContext;
    }

    @Override
    public NExprWordNode newWord(String a) {
        return new DefaultWordNode(a);
    }

    @Override
    public NExprLiteralNode newLiteral(Object a) {
        return new DefaultLiteralNode(a);
    }


    public NExprVar newVar(String var) {
        return new ReservedNExprVar(var, null);
    }

    @Override
    public NExprVar newVar(String var, Object value) {
        return new ReservedNExprVar(var, value);
    }

    public NExprVar newConst(String name, Object value) {
        return new ReservedNExprConst(name, value);
    }

    @Override
    public <A, B> NOptional<NFunction2<A, B, ?>> findCommonInfixOp(NExprCommonOp op, Class<? extends A> firstArgType, Class<? extends B> secondArgType) {
        return (NOptional) defaultNExprsCommonOps.findFunction2(op, NExprOpType.INFIX, NPlatformSignatureImpl.of(firstArgType, secondArgType));
    }

    @Override
    public <A> NOptional<NFunction<A, ?>> findCommonPrefixOp(NExprCommonOp op, Class<? extends A> argType) {
        return (NOptional) defaultNExprsCommonOps.findFunction1(op, NExprOpType.PREFIX, NPlatformSignatureImpl.of(argType));
    }

    @Override
    public <A> NOptional<NFunction<A, ?>> findCommonPostfixOp(NExprCommonOp op, Class<? extends A> argType) {
        return (NOptional) defaultNExprsCommonOps.findFunction1(op, NExprOpType.POSTFIX, NPlatformSignatureImpl.of(argType));
    }


}

