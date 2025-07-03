package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.util.NPlatformArgsSignature;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.*;

public class DefaultNExprs implements NExprs {

    private final DefaultNExprsCommonOps defaultNExprsCommonOps = new DefaultNExprsCommonOps();

    public DefaultNExprs() {
    }


    public NExprDeclarations newDeclarations(boolean includeDefaults) {
        return includeDefaults ? new DefaultRootDeclarations(this) : new EmptyRootDeclarations(this);
    }

    @Override
    public NExprDeclarations newDeclarations() {
        return newDeclarations(true);
    }

    public NExprVar newVar(String var) {
        return new ReservedNExprVar(var,null);
    }

    @Override
    public NExprVar newVar(String var, Object value) {
        return new ReservedNExprVar(var,value);
    }

    public NExprVar newConst(String name, Object value) {
        return new ReservedNExprConst(name, value);
    }

    public NExprDeclarations newDeclarations(boolean includeDefaults, NExprEvaluator evaluator) {
        NExprDeclarations r = newDeclarations(includeDefaults);
        if (evaluator != null) {
            r = r.newDeclarations(evaluator);
        }
        return r;
    }

    @Override
    public NExprMutableDeclarations newMutableDeclarations(boolean includeDefaults) {
        return newDeclarations(includeDefaults).newMutableDeclarations();
    }

    @Override
    public NExprMutableDeclarations newMutableDeclarations(NExprEvaluator evaluator) {
        return newDeclarations(true, evaluator).newMutableDeclarations();
    }

    @Override
    public NExprMutableDeclarations newMutableDeclarations() {
        return newDeclarations(true).newMutableDeclarations();
    }

    public NExprMutableDeclarations newMutableDeclarations(boolean includeDefaults, NExprEvaluator evaluator) {
        return newDeclarations(includeDefaults, evaluator).newMutableDeclarations();
    }

    @Override
    public <A, B> NOptional<NFunction2<A, B, ?>> findCommonInfixOp(NExprCommonOp op, Class<? extends A> firstArgType, Class<? extends B> secondArgType) {
        return (NOptional) defaultNExprsCommonOps.findFunction2(op, NExprOpType.INFIX, NPlatformArgsSignature.of(firstArgType, secondArgType));
    }

    @Override
    public <A> NOptional<NFunction<A, ?>> findCommonPrefixOp(NExprCommonOp op, Class<? extends A> argType) {
        return (NOptional) defaultNExprsCommonOps.findFunction1(op, NExprOpType.PREFIX, NPlatformArgsSignature.of(argType));
    }

    @Override
    public <A> NOptional<NFunction<A, ?>> findCommonPostfixOp(NExprCommonOp op, Class<? extends A> argType) {
        return (NOptional) defaultNExprsCommonOps.findFunction1(op, NExprOpType.POSTFIX, NPlatformArgsSignature.of(argType));
    }


    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}

