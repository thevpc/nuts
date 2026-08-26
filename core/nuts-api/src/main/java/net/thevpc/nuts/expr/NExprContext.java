package net.thevpc.nuts.expr;

import net.thevpc.nuts.internal.rpi.NExprRPI;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NFunction2;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

/**
 * NExprContext interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprContext {


    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NExprContext of() {
        return NExprRPI.of().createEmptyContext();
    }

    /**
     * Returns the function.
     *
     * @param fctName fct name
     * @param args args
     * @return get function result
     */
    NOptional<NExprFunction> getFunction(String fctName, NExprNodeValue... args);

    /**
     * Returns the construct.
     *
     * @param constructName construct name
     * @param args args
     * @return get construct result
     */
    NOptional<NExprFunction> getConstruct(String constructName, NExprNodeValue... args);

    /**
     * Returns the operator.
     *
     * @param opName op name
     * @param type type
     * @param args args
     * @return get operator result
     */
    NOptional<NExprOperator> getOperator(String opName, NFixity type, NExprNodeValue... args);

    /**
     * Operators.
     *
     * @return operators result
     */
    List<NExprOperator> operators();


    /**
     * Returns the var.
     *
     * @param varName var name
     * @return get var result
     */
    NOptional<NExprVar> getVar(String varName);

    /**
     * Child context.
     *
     * @return child context result
     */
    NExprContextBuilder childContext();


    /**
     * Eval function.
     *
     * @param fctName fct name
     * @param args args
     * @return eval function result
     */
    NOptional<Object> evalFunction(String fctName, NExprNodeValue... args);

    /**
     * Eval construct.
     *
     * @param constructName construct name
     * @param args args
     * @return eval construct result
     */
    NOptional<Object> evalConstruct(String constructName, NExprNodeValue... args);

    /**
     * Eval operator.
     *
     * @param opName op name
     * @param type type
     * @param args args
     * @return eval operator result
     */
    NOptional<Object> evalOperator(String opName, NFixity type, NExprNodeValue... args);

    /**
     * Eval infix operator.
     *
     * @param opName op name
     * @param first first
     * @param second second
     * @return eval infix operator result
     */
    NOptional<Object> evalInfixOperator(String opName, NExprNodeValue first, NExprNodeValue second);

    /**
     * Eval prefix operator.
     *
     * @param opName op name
     * @param arg arg
     * @return eval prefix operator result
     */
    NOptional<Object> evalPrefixOperator(String opName, NExprNodeValue arg);

    /**
     * Eval postfix operator.
     *
     * @param opName op name
     * @param arg arg
     * @return eval postfix operator result
     */
    NOptional<Object> evalPostfixOperator(String opName, NExprNodeValue arg);

    /**
     * Returns the var value.
     *
     * @param varName var name
     * @return get var value result
     */
    NOptional<Object> getVarValue(String varName);

    /**
     * parse node
     *
     * @param expression expression to parse
     * @return parsed node
     */
    NOptional<NExprNode> parse(String expression);

    /**
     * Bind literal.
     *
     * @param any any
     * @return bind literal result
     */
    NExprNodeValue bindLiteral(Object any);

    /**
     * Bind node.
     *
     * @param any any
     * @return bind node result
     */
    NExprNodeValue bindNode(NExprNode any);


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
     * Creates a new instance of of dollar interpolated string.
     *
     * @param a a
     * @return of dollar interpolated string result
     */
    NExprInterpolatedStringNode ofDollarInterpolatedString(String a);

    /**
     * Creates a new instance of of moustache interpolated string.
     *
     * @param a a
     * @return of moustache interpolated string result
     */
    NExprInterpolatedStringNode ofMoustacheInterpolatedString(String a);

    /**
     * Creates a new instance of of template.
     *
     * @return of template result
     */
    NExprTemplate ofTemplate();

    /**
     * Literal mapper.
     *
     * @return literal mapper result
     */
    NExprLiteralMapper literalMapper();
}
