package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.*;

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
    public Object eval(List<NExprNodeValue> args, NExprDeclarations context) {
        return op.eval(name, args, context);
    }

    @Override
    public String toString() {
        return "DefaultNExprOpDeclaration{" +
                "name='" + name + '\'' +
                ", op=" + op +
                '}';
    }
}
