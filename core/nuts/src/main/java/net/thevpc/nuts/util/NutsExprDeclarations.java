package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;

import java.util.List;

public interface NutsExprDeclarations {

    NutsSession getSession();

    NutsExprDeclarations setSession(NutsSession session);

    NutsOptional<NutsExprFctDeclaration> getFunction(String fctName, Object... args);

    NutsOptional<NutsExprConstructDeclaration> getConstruct(String constructName, NutsExprNode... args);

    NutsOptional<NutsExprOpDeclaration> getOperator(String opName, NutsExprOpType type, NutsExprNode... args);

    List<NutsExprOpDeclaration> getOperators();


    NutsOptional<NutsExprVarDeclaration> getVar(String varName);

    NutsExprDeclarations newDeclarations(NutsExprEvaluator evaluator);

    NutsExprMutableDeclarations newMutableDeclarations();


    Object evalFunction(String fctName, Object... args);

    Object evalConstruct(String constructName, NutsExprNode... args);

    Object evalOperator(String opName, NutsExprOpType type, NutsExprNode... args);

    Object evalSetVar(String varName, Object value);

    Object evalGetVar(String varName);


    /**
     * parse node
     *
     * @param expression expression to parse
     * @return parsed node
     */
    NutsOptional<NutsExprNode> parse(String expression);

    int[] getOperatorPrecedences();
}
