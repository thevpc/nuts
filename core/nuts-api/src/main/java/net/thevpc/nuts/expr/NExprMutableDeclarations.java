package net.thevpc.nuts.expr;

public interface NExprMutableDeclarations extends NExprDeclarations {
    NExprFctDeclaration declareFunction(String name, NExprFct fctImpl);

    NExprConstructDeclaration declareConstruct(String name, NExprConstruct constructImpl);

    NExprVarDeclaration declareVar(String name);

    NExprVarDeclaration declareConstant(String name, Object value);

    NExprVarDeclaration declareVar(String name, NExprVar varImpl);

    NExprOpDeclaration declareOperator(String name, NExprOpType type, int precedence, NExprOpAssociativity associativity, NExprConstruct impl);
    NExprOpDeclaration declareOperator(String name, NExprConstruct impl);
    NExprOpDeclaration declareOperator(String name, NExprOpType type, NExprConstruct impl);

    void resetDeclaration(NExprVarDeclaration member);

    void resetDeclaration(NExprFctDeclaration member);

    void resetDeclaration(NExprConstructDeclaration member);

    void resetDeclaration(NExprOpDeclaration member);

    void removeDeclaration(NExprVarDeclaration member);

    void removeDeclaration(NExprFctDeclaration member);

    void removeDeclaration(NExprConstructDeclaration member);

    void removeDeclaration(NExprOpDeclaration member);


}
