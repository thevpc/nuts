package net.thevpc.nuts.expr;

import net.thevpc.nuts.elem.NOperatorAssociativity;

import java.util.function.Supplier;

public interface NExprMutableContext extends NExprContext {
    NExprFunction declareFunction(NExprFunction fctImpl);

    NExprFunction declareFunction(String name, NExprFunctionHandler fctImpl);

    NExprFunction declareConstruct(NExprFunction constructImpl);

    NExprFunction declareConstruct(String name, NExprFunctionHandler constructImpl);

    NExprVar declareVar(String name);

    NExprVar declareConstant(String name, Object value);

    NExprVar declareVar(NExprVar varImpl);

    NExprOperator declareOperator(String name, NExprOpType type, int precedence, NOperatorAssociativity associativity, NExprFunctionHandler impl);

    NExprOperator declareOperator(String name, NExprFunctionHandler impl);

    NExprOperator declareOperator(NExprOperator impl);

    NExprOperator declareOperator(String name, NExprOpType type, NExprFunctionHandler impl);

    void setVarValue(String varName, Object value);

    NExprVar getOrDeclareVar(String name, Supplier<Object> initialValue);

    void undeclareVar(NExprVar member);

    void undeclareFunction(NExprFunction member);

    void undeclareConstruct(NExprFunction member);

    void undeclareOperator(NExprOperator member);

    void removeVar(NExprVar member);

    void removeFunction(NExprFunction member);

    void removeConstruct(NExprFunction member);

    void removeOperator(NExprOperator member);


}
