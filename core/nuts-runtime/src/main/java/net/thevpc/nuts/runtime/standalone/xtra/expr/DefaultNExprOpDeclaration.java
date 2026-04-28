package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.elem.NOperatorAssociativity;
import net.thevpc.nuts.expr.*;

import java.util.List;

public class DefaultNExprOpDeclaration implements NExprOperator {
    private final String name;
    private final NExprOperatorHandler op;

    public DefaultNExprOpDeclaration(String name, NExprOperatorHandler op) {
        this.name = name;
        this.op = op;
    }

    @Override
    public NOperatorAssociativity getAssociativity() {
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
    public Object eval(List<NExprNodeValue> args, NExprContext context) {
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
