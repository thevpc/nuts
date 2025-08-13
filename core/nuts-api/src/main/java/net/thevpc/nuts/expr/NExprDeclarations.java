package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NFunction2;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

public interface NExprDeclarations {

    NOptional<NExprFctDeclaration> getFunction(String fctName, NExprNodeValue... args);

    NOptional<NExprConstructDeclaration> getConstruct(String constructName, NExprNodeValue... args);

    NOptional<NExprOpDeclaration> getOperator(String opName, NExprOpType type, NExprNodeValue... args);

    List<NExprOpDeclaration> getOperators();


    NOptional<NExprVarDeclaration> getVar(String varName);

    NExprDeclarations newDeclarations(NExprEvaluator evaluator);

    NExprMutableDeclarations newMutableDeclarations();


    NOptional<Object> evalFunction(String fctName, NExprNodeValue... args);

    NOptional<Object> evalConstruct(String constructName, NExprNodeValue... args);

    NOptional<Object> evalOperator(String opName, NExprOpType type, NExprNodeValue... args);

    NOptional<Object> evalInfixOperator(String opName, NExprNodeValue first, NExprNodeValue second);

    NOptional<Object> evalPrefixOperator(String opName, NExprNodeValue arg);

    NOptional<Object> evalPostfixOperator(String opName, NExprNodeValue arg);

    NOptional<Object> setVarValue(String varName, Object value);

    NOptional<Object> getVarValue(String varName);


    /**
     * parse node
     *
     * @param expression expression to parse
     * @return parsed node
     */
    NOptional<NExprNode> parse(String expression);

    NExprNodeValue literalAsValue(Object any);

    NExprNodeValue nodeAsValue(NExprNode any);

    NExprNode literalAsNode(Object any);

    <A, B> NOptional<NFunction2<A, B, ?>> findCommonInfixOp(NExprCommonOp op, Class<? extends A> firstArgType, Class<? extends B> secondArgType);

    <A> NOptional<NFunction<A, ?>> findCommonPrefixOp(NExprCommonOp op, Class<? extends A> argType);

    <A> NOptional<NFunction<A, ?>> findCommonPostfixOp(NExprCommonOp op, Class<? extends A> argType);

    NExprWordNode ofWord(String a);

    NExprLiteralNode ofLiteral(Object a);

    NExprVar ofConst(String name, Object a);

    NExprVar ofVar(String name, Object a);

    NExprVar getOrDeclareVar(String name, Object a);

    NExprInterpolatedStrNode ofInterpolatedStr(String a);

    NExprTemplate ofTemplate();
}
