package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.*;

public abstract class AbstractOp implements NutsExprOp {
    private final NutsExprOpType type;
    private final String name;
    private final int precedence;
    private final NutsExprOpAssociativity associativity;

    public AbstractOp(String name, int precedence, NutsExprOpAssociativity associativity, NutsExprOpType type) {
        this.name = name;
        this.type = type;
        this.precedence = precedence;
        this.associativity = associativity;
    }

    public NutsExprOpType getOpType() {
        return type;
    }

    public int getPrecedence() {
        return precedence;
    }

    public NutsExprOpType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public NutsExprOpAssociativity getAssociativity() {
        return associativity;
    }
}
