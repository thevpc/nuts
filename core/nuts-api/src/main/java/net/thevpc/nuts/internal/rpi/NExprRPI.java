package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NFunction2;
import net.thevpc.nuts.util.NOptional;

import java.util.Map;
import java.util.function.Function;

/**
 * NExprRPI interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprRPI extends NComponent {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NExprRPI of() {
        /**
         * Returns the get.
         *
         * @param ).get( ).get(
         * @return get result
         */
        return get().get();
    }

    /**
     * Returns the get.
     *
     * @return get result
     */
    static NOptional<NExprRPI> get() {
        return NExtensions.get(NExprRPI.class);
    }

    /**
     * Creates a new instance of create lazy const resolver.
     *
     * @param vars vars
     * @return create lazy const resolver result
     */
    NExprVarResolver createLazyConstResolver(Function<String, Object> vars);

    /**
     * Creates a new instance of create read only var resolver.
     *
     * @param vars vars
     * @return create read only var resolver result
     */
    NExprVarResolver createReadOnlyVarResolver(Function<String, Object> vars);

    /**
     * Creates a new instance of create lazy const.
     *
     * @param name name
     * @param vars vars
     * @return create lazy const result
     */
    NExprVar createLazyConst(String name, NExprVarReader vars);

    /**
     * Creates a new instance of create read only var.
     *
     * @param name name
     * @param vars vars
     * @return create read only var result
     */
    NExprVar createReadOnlyVar(String name, NExprVarReader vars);

    /**
     * Creates a new instance of create const.
     *
     * @param name name
     * @param value value
     * @return create const result
     */
    NExprVar createConst(String name, Object value);

    /**
     * Creates a new instance of create var.
     *
     * @param name name
     * @param value value
     * @return create var result
     */
    NExprVar createVar(String name, Object value);

    /**
     * Creates a new instance of create var.
     *
     * @param name name
     * @param reader reader
     * @param writer writer
     * @return create var result
     */
    NExprVar createVar(String name, NExprVarReader reader, NExprVarWriter writer);

    /**
     * Creates a new instance of create map var resolver.
     *
     * @param variables variables
     * @return create map var resolver result
     */
    NExprVarResolver createMapVarResolver(Map<String, Object> variables);

    /**
     * Creates a new instance of create read only map var resolver.
     *
     * @param variables variables
     * @return create read only map var resolver result
     */
    NExprVarResolver createReadOnlyMapVarResolver(Map<String, Object> variables);

    /**
     * Creates a new instance of create expr word node.
     *
     * @param a a
     * @return create expr word node result
     */
    NExprWordNode createExprWordNode(String a);

    /**
     * Creates a new instance of create expr literal node.
     *
     * @param a a
     * @return create expr literal node result
     */
    NExprLiteralNode createExprLiteralNode(Object a);

    /**
     * Finds the find common infix op.
     *
     * @param op op
     * @param firstArgType first arg type
     * @param secondArgType second arg type
     * @return find common infix op result
     */
    <A, B> NOptional<NFunction2<A, B, ?>> findCommonInfixOp(NExprCommonOp op, Class<? extends A> firstArgType, Class<? extends B> secondArgType);

    /**
     * Finds the find common prefix op.
     *
     * @param op op
     * @param argType arg type
     * @return find common prefix op result
     */
    <A> NOptional<NFunction<A, ?>> findCommonPrefixOp(NExprCommonOp op, Class<? extends A> argType);

    /**
     * Finds the find common postfix op.
     *
     * @param op op
     * @param argType arg type
     * @return find common postfix op result
     */
    <A> NOptional<NFunction<A, ?>> findCommonPostfixOp(NExprCommonOp op, Class<? extends A> argType);

    /**
     * Creates a new instance of create empty context.
     *
     * @return create empty context result
     */
    NExprContext createEmptyContext();

    /**
     * Creates a new instance of create default context.
     *
     * @return create default context result
     */
    NExprContext createDefaultContext();

    /**
     * Creates a new instance of create function.
     *
     * @param name name
     * @param handler handler
     * @return create function result
     */
    NExprFunction createFunction(String name, NExprCallHandler handler);

    /**
     * Creates a new instance of create operator.
     *
     * @param name name
     * @param operatorType operator type
     * @param operatorPrecedence operator precedence
     * @param associativity associativity
     * @param handler handler
     * @return create operator result
     */
    NExprOperator createOperator(String name, NFixity operatorType, int operatorPrecedence, NOperatorAssociativity associativity, NExprCallHandler handler);
}
