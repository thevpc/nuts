package net.thevpc.nuts.util;

public interface NutsExprMutableDeclarations extends NutsExprDeclarations {
    NutsExprFctDeclaration declareFunction(String name, NutsExprFct fctImpl);

    NutsExprConstructDeclaration declareConstruct(String name, NutsExprConstruct constructImpl);

    NutsExprVarDeclaration declareVar(String name);

    NutsExprVarDeclaration declareConstant(String name,Object value);

    NutsExprVarDeclaration declareVar(String name, NutsExprVar varImpl);

    NutsExprOpDeclaration declareOperator(String name, NutsExprOpType type, int precedence, NutsExprOpAssociativity associativity, NutsExprConstruct impl);

    void resetDeclaration(NutsExprVarDeclaration member);

    void resetDeclaration(NutsExprFctDeclaration member);

    void resetDeclaration(NutsExprConstructDeclaration member);

    void resetDeclaration(NutsExprOpDeclaration member);

    void removeDeclaration(NutsExprVarDeclaration member);

    void removeDeclaration(NutsExprFctDeclaration member);

    void removeDeclaration(NutsExprConstructDeclaration member);

    void removeDeclaration(NutsExprOpDeclaration member);


}
