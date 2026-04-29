//package net.thevpc.nuts.runtime.standalone.xtra.expr;
//
//import net.thevpc.nuts.expr.NExprOperatorHandler;
//import net.thevpc.nuts.expr.NOperatorAssociativity;
//import net.thevpc.nuts.expr.NExprOpType;
//
//public abstract class AbstractOp implements NExprOperatorHandler {
//    private final NExprOpType type;
//    private final String name;
//    private final int precedence;
//    private final NOperatorAssociativity associativity;
//
//    public AbstractOp(String name, int precedence, NOperatorAssociativity associativity, NExprOpType type) {
//        this.name = name;
//        this.type = type;
//        this.precedence = precedence;
//        this.associativity = associativity;
//    }
//
//    public NExprOpType getOpType() {
//        return type;
//    }
//
//    public int operatorPrecedence() {
//        return precedence;
//    }
//
//    public NExprOpType operatorType() {
//        return type;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public NOperatorAssociativity getAssociativity() {
//        return associativity;
//    }
//}
