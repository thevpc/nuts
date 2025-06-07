package net.thevpc.nuts.expr;

public final class NExprOpPrecedence {
    private NExprOpPrecedence() {
    }

    public static final int ASSIGN = 100;
    public static final int TERNARY_CMP = 200;
    public static final int OR = 300;
    public static final int AND = 400;

    // |
    public static final int PIPE = 500;

    // ^
    public static final int COMPLEMENT = 600;

    // &
    public static final int AMP = 700;

    public static final int EQ = 800;
    public static final int NEQ = EQ;
    public static final int CMP = 900;
    public static final int LT = CMP;
    public static final int LTE = CMP;
    public static final int GT = CMP;
    public static final int GTE = CMP;

    public static final int SHIFT = 1000;
    public static int PLUS = 1100;
    public static int MINUS = PLUS;
    public static int MUL = 1200;
    public static int DIV = MUL;
    public static int MOD = MUL;

    public static final int POW = 1250;
    public static final int COALESCE = MUL+10;

    public static final int NOT = 1300;

    public static final int UNARY_PRE = 1300;
    public static final int UNARY_POST = 1400;
    public static final int PARS = 1600;
    public static final int BRACKETS = PARS;
    public static final int BRACES = PARS;
    public static final int DOT = PARS;
    public static final int STATEMENT_SEPARATOR = 50;
}
