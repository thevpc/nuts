package net.thevpc.nuts.runtime.core.expr;

import net.thevpc.nuts.NutsExpr;

public class OpImpl implements NutsExpr.Op {
    private final String name;
    private NutsExpr.OpType type;
    private int precedence;
    private boolean rightAssociative;
    private final NutsExpr.Fct fct;

    public OpImpl(String name, NutsExpr.OpType type, int precedence, boolean rightAssociative,NutsExpr.Fct fct) {
        this.name = name;
        this.type = type;
        this.precedence = precedence;
        this.rightAssociative = rightAssociative;
        this.fct = fct;
    }

    public boolean isLeftAssociative() {
        return !rightAssociative;
    }

    public boolean isRightAssociative() {
        return rightAssociative;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NutsExpr.OpType getType() {
        return type;
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }

    @Override
    public NutsExpr.Fct getFct() {
        return fct;
    }
}
