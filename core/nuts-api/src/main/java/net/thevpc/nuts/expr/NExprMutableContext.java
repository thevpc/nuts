package net.thevpc.nuts.expr;

import java.util.function.Supplier;

public interface NExprMutableContext extends NExprContext {
    NExprFunction declareFunction(NExprFunction fctImpl);

    NExprFunction declareFunction(String name, NExprCallHandler fctImpl);

    NExprFunction declareConstruct(NExprFunction constructImpl);

    NExprFunction declareConstruct(String name, NExprCallHandler constructImpl);

    NExprVar declareVar(String name);

    NExprVar declareConstant(String name, Object value);

    NExprVar declareVar(NExprVar varImpl);

    NExprOperator declareOperator(String name, NExprOpType type, int precedence, NOperatorAssociativity associativity, NExprCallHandler impl);

    NExprOperator declareOperator(String name, NExprCallHandler impl);

    NExprOperator declareOperator(NExprOperator impl);

    NExprOperator declareOperator(String name, NExprOpType type, NExprCallHandler impl);

    NExprMutableContext setVarValue(String varName, Object value);

    NExprVar getOrDeclareVar(String name, Supplier<Object> initialValue);

    NExprMutableContext undeclareVar(NExprVar member);

    NExprMutableContext undeclareFunction(NExprFunction member);

    NExprMutableContext undeclareConstruct(NExprFunction member);

    NExprMutableContext undeclareOperator(NExprOperator member);

    NExprMutableContext removeVar(NExprVar member);

    NExprMutableContext removeFunction(NExprFunction member);

    NExprMutableContext removeConstruct(NExprFunction member);

    NExprMutableContext removeOperator(NExprOperator member);


    NExprMutableContext removeVar(String name);

    NExprMutableContext removeFunction(String name);

    NExprMutableContext removeConstruct(String name);

    NExprMutableContext removeOperator(String name, NExprOpType type);
}
