package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.expr.*;

import java.util.Collections;
import java.util.List;

public class EmptyRootDeclarations extends NExprDeclarationsBase {
    public EmptyRootDeclarations(NSession session) {
        setSession(session);
    }

    @Override
    public NOptional<NExprFctDeclaration> getFunction(String fctName, Object... args) {
        return NOptional.ofEmpty(session -> NMsg.ofC("function not found %s", fctName));
    }

    @Override
    public NOptional<NExprConstructDeclaration> getConstruct(String constructName, NExprNode... args) {
        return NOptional.ofEmpty(session -> NMsg.ofC("construct not found %s", constructName));
    }

    @Override
    public NOptional<NExprOpDeclaration> getOperator(String opName, NExprOpType type, NExprNode... args) {
        return NOptional.ofEmpty(session -> NMsg.ofC("operator not found %s", opName));
    }

    @Override
    public NOptional<NExprVarDeclaration> getVar(String varName) {
        return NOptional.ofEmpty(session -> NMsg.ofC("var not found %s", varName));
    }

    @Override
    public List<NExprOpDeclaration> getOperators() {
        return Collections.emptyList();
    }

    @Override
    public int[] getOperatorPrecedences() {
        return new int[0];
    }
}
