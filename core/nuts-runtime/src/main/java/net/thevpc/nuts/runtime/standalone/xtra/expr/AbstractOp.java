package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprOp;
import net.thevpc.nuts.expr.NExprOpAssociativity;
import net.thevpc.nuts.expr.NExprOpType;

public abstract class AbstractOp implements NExprOp {
    private final NExprOpType type;
    private final String name;
    private final int precedence;
    private final NExprOpAssociativity associativity;

    public AbstractOp(String name, int precedence, NExprOpAssociativity associativity, NExprOpType type) {
        this.name = name;
        this.type = type;
        this.precedence = precedence;
        this.associativity = associativity;
    }

    public NExprOpType getOpType() {
        return type;
    }

    public int getPrecedence() {
        return precedence;
    }

    public NExprOpType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public NExprOpAssociativity getAssociativity() {
        return associativity;
    }
}
