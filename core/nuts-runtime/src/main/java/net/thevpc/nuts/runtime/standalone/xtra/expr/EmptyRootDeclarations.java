package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.util.*;

import java.util.Collections;
import java.util.List;

public class EmptyRootDeclarations extends NutsExprDeclarationsBase {
    public EmptyRootDeclarations(NutsSession session) {
        setSession(session);
    }

    @Override
    public NutsOptional<NutsExprFctDeclaration> getFunction(String fctName, Object... args) {
        return NutsOptional.ofEmpty(session -> NutsMessage.ofCstyle("function not found %s", fctName));
    }

    @Override
    public NutsOptional<NutsExprConstructDeclaration> getConstruct(String constructName, NutsExprNode... args) {
        return NutsOptional.ofEmpty(session -> NutsMessage.ofCstyle("construct not found %s", constructName));
    }

    @Override
    public NutsOptional<NutsExprOpDeclaration> getOperator(String opName, NutsExprOpType type, NutsExprNode... args) {
        return NutsOptional.ofEmpty(session -> NutsMessage.ofCstyle("operator not found %s", opName));
    }

    @Override
    public NutsOptional<NutsExprVarDeclaration> getVar(String varName) {
        return NutsOptional.ofEmpty(session -> NutsMessage.ofCstyle("var not found %s", varName));
    }

    @Override
    public List<NutsExprOpDeclaration> getOperators() {
        return Collections.emptyList();
    }

    @Override
    public int[] getOperatorPrecedences() {
        return new int[0];
    }
}
