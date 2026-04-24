package net.thevpc.nuts.expr;

import net.thevpc.nuts.elem.NOperatorAssociativity;
import net.thevpc.nuts.util.NOptional;

import java.util.function.Supplier;

public interface NExprMutableContext extends NExprContext {
    NExprFctDeclaration declareFunction(String name, NExprFct fctImpl);

    NExprConstructDeclaration declareConstruct(String name, NExprConstruct constructImpl);

    NExprVar declareVar(String name);

    NExprVar declareConstant(String name, Object value);

    NExprVar declareVar(NExprVar varImpl);

    NExprOpDeclaration declareOperator(String name, NExprOpType type, int precedence, NOperatorAssociativity associativity, NExprConstruct impl);

    NExprOpDeclaration declareOperator(String name, NExprConstruct impl);

    NExprOpDeclaration declareOperator(String name, NExprOpType type, NExprConstruct impl);

    void setVarValue(String varName, Object value);

    NExprVar getOrDeclareVar(String name, Supplier<Object> initialValue);

    void undeclare(NExprVar member);

    void undeclare(NExprFctDeclaration member);

    void undeclare(NExprConstructDeclaration member);

    void undeclare(NExprOpDeclaration member);

    void remove(NExprVar member);

    void remove(NExprFctDeclaration member);

    void remove(NExprConstructDeclaration member);

    void remove(NExprOpDeclaration member);


}
