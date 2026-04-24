package net.thevpc.nuts.internal.expr;

import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NFunction2;
import net.thevpc.nuts.util.NOptional;

import java.util.Map;
import java.util.function.Function;

public interface NExprRPI extends NComponent {
    static NExprRPI of() {
        return get().get();
    }

    static NOptional<NExprRPI> get() {
        return NExtensions.get(NExprRPI.class);
    }

    NExprVarResolver createLazyConstResolver(Function<String, Object> vars);

    NExprVarResolver createReadOnlyVarResolver(Function<String, Object> vars);

    NExprVar createLazyConst(String name, NExprVarReader vars);

    NExprVar createReadOnlyVar(String name, NExprVarReader vars);

    NExprVar createConst(String name, Object value);

    NExprVar createVar(String name, Object value);

    NExprVar createVar(String name, NExprVarReader reader, NExprVarWriter writer);

    NExprVarResolver createMapVarResolver(Map<String, Object> variables);

    NExprWordNode createExprWordNode(String a);

    NExprLiteralNode createExprLiteralNode(Object a);

    <A, B> NOptional<NFunction2<A, B, ?>> findCommonInfixOp(NExprCommonOp op, Class<? extends A> firstArgType, Class<? extends B> secondArgType);

    <A> NOptional<NFunction<A, ?>> findCommonPrefixOp(NExprCommonOp op, Class<? extends A> argType);

    <A> NOptional<NFunction<A, ?>> findCommonPostfixOp(NExprCommonOp op, Class<? extends A> argType);

    NExprContext createEmptyContext();

    NExprContext createDefaultContext();
}
