package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.*;

import java.util.List;

public class DefaultNExprOpDeclaration implements NExprOpDeclaration {
    private final String name;
    private final NExprOp op;

    public DefaultNExprOpDeclaration(String name, NExprOp op) {
        this.name = name;
        this.op = op;
    }

    @Override
    public NExprOpAssociativity getAssociativity() {
        return op.getAssociativity();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NExprOpType getType() {
        return op.getType();
    }

    @Override
    public int getPrecedence() {
        return op.getPrecedence();
    }

    @Override
    public Object eval(List<NExprNode> args, NExprDeclarations context) {
        return op.eval(name, args, context);
    }
}
