package net.thevpc.nuts.util;

import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NSessionProvider;

import java.util.List;

public interface NExprDeclarations extends NSessionProvider {

    NExprDeclarations setSession(NSession session);

    NOptional<NExprFctDeclaration> getFunction(String fctName, Object... args);

    NOptional<NExprConstructDeclaration> getConstruct(String constructName, NExprNode... args);

    NOptional<NExprOpDeclaration> getOperator(String opName, NExprOpType type, NExprNode... args);

    List<NExprOpDeclaration> getOperators();


    NOptional<NExprVarDeclaration> getVar(String varName);

    NExprDeclarations newDeclarations(NExprEvaluator evaluator);

    NExprMutableDeclarations newMutableDeclarations();


    NOptional<Object> evalFunction(String fctName, Object... args);

    NOptional<Object> evalConstruct(String constructName, NExprNode... args);

    NOptional<Object> evalOperator(String opName, NExprOpType type, NExprNode... args);

    NOptional<Object> evalSetVar(String varName, Object value);

    NOptional<Object> evalGetVar(String varName);


    /**
     * parse node
     *
     * @param expression expression to parse
     * @return parsed node
     */
    NOptional<NExprNode> parse(String expression);

    int[] getOperatorPrecedences();
}
