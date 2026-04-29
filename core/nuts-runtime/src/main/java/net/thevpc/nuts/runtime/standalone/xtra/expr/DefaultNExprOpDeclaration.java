package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NOperatorAssociativity;
import net.thevpc.nuts.expr.*;

import java.util.List;

public class DefaultNExprOpDeclaration implements NExprOperator {
    private final String name;
    private final NExprOpType operatorType;
    private final int operatorPrecedence;
    private final NOperatorAssociativity associativity;
    private final NExprCallHandler op;


    public DefaultNExprOpDeclaration(String name, NExprOpType operatorType, int operatorPrecedence, NOperatorAssociativity associativity, NExprCallHandler op) {
        this.name = name;
        this.operatorType = operatorType;
        this.operatorPrecedence = operatorPrecedence;
        this.associativity = associativity;
        this.op = op;
    }

//    public DefaultNExprOpDeclaration(String name, NExprOperatorHandler op) {
//        this.name = name;
//        this.op = op;
//    }

    @Override
    public NOperatorAssociativity operatorAssociativity() {
        return associativity;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public NExprOpType operatorType() {
        return operatorType;
    }

    @Override
    public int operatorPrecedence() {
        return operatorPrecedence;
    }

    @Override
    public Object eval(NExprCallContext callContext) {
        return op.eval(callContext);
    }

    @Override
    public String toString() {
        return "DefaultNExprOpDeclaration{" +
                "name='" + name + '\'' +
                ", op=" + op +
                '}';
    }
}
