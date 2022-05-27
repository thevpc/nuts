package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.*;

import java.util.List;

public class DefaultNutsExprOpDeclaration implements NutsExprOpDeclaration {
    private final String name;
    private final NutsExprOp op;

    public DefaultNutsExprOpDeclaration(String name, NutsExprOp op) {
        this.name = name;
        this.op = op;
    }

    @Override
    public NutsExprOpAssociativity getAssociativity() {
        return op.getAssociativity();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NutsExprOpType getType() {
        return op.getType();
    }

    @Override
    public int getPrecedence() {
        return op.getPrecedence();
    }

    @Override
    public Object eval(List<NutsExprNode> args, NutsExprDeclarations context) {
        return op.eval(name, args, context);
    }
}
