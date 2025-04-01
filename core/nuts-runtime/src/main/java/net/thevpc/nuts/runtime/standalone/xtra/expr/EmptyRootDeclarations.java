package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.expr.*;

import java.util.Collections;
import java.util.List;

public class EmptyRootDeclarations extends NExprDeclarationsBase {
    public EmptyRootDeclarations(NExprs exprs, NWorkspace workspace) {
        super(exprs);
    }

    @Override
    public NOptional<NExprFctDeclaration> getFunction(String fctName, NExprNodeValue... args) {
        return NOptional.ofEmpty(() -> NMsg.ofC("function not found %s", fctName));
    }

    @Override
    public NOptional<NExprConstructDeclaration> getConstruct(String constructName, NExprNodeValue... args) {
        return NOptional.ofEmpty(() -> NMsg.ofC("construct not found %s", constructName));
    }

    @Override
    public NOptional<NExprOpDeclaration> getOperator(String opName, NExprOpType type, NExprNodeValue... args) {
        return NOptional.ofEmpty(() -> NMsg.ofC("operator not found %s", opName));
    }

    @Override
    public NOptional<NExprVarDeclaration> getVar(String varName) {
        return NOptional.ofEmpty(() -> NMsg.ofC("var not found %s", varName));
    }

    @Override
    public List<NExprOpDeclaration> getOperators() {
        return Collections.emptyList();
    }
}
