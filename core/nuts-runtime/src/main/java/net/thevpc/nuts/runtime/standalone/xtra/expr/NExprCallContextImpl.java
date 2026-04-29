package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

public class NExprCallContextImpl implements NExprCallContext {
    private final String name;
    private final List<NExprNodeValue> args;
    private final NExprContext context;
    private final NExprCallContextType contextType;
    private final NExprOpType operatorType;
    private final int operatorPrecedence;
    private final NOperatorAssociativity operatorAssociativity;

    public static NExprCallContextImpl ofFunction(String name, List<NExprNodeValue> args, NExprContext context) {
        return new NExprCallContextImpl(name, args, context, NExprCallContextType.FUNCTION, null, 0, NOperatorAssociativity.LEFT);
    }

    public static NExprCallContextImpl ofConstruct(String name, List<NExprNodeValue> args, NExprContext context) {
        return new NExprCallContextImpl(name, args, context, NExprCallContextType.CONSTRUCT, null, 0, NOperatorAssociativity.LEFT);
    }

    public static NExprCallContextImpl ofOperator(String name, List<NExprNodeValue> args, NExprContext context, NExprOpType operatorType, int operatorPrecedence, NOperatorAssociativity operatorAssociativity) {
        return new NExprCallContextImpl(name, args, context, NExprCallContextType.OPERATOR, operatorType, operatorPrecedence, operatorAssociativity);
    }

    public NExprCallContextImpl(String name, List<NExprNodeValue> args, NExprContext context, NExprCallContextType contextType, NExprOpType operatorType, int operatorPrecedence, NOperatorAssociativity operatorAssociativity) {
        this.name = name;
        this.args = args;
        this.context = context;
        this.contextType = contextType;
        this.operatorType = operatorType;
        this.operatorPrecedence = operatorPrecedence;
        this.operatorAssociativity = operatorAssociativity;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<NExprNodeValue> args() {
        return args;
    }

    @Override
    public NOptional<NExprNodeValue> arg(int index) {
        if (index < 0 || index >= args.size()) {
            return NOptional.ofNullable(args.get(index));
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("argument index out of range %d", index));
    }

    @Override
    public NExprContext context() {
        return context;
    }

    @Override
    public NExprCallContextType contextType() {
        return contextType;
    }

    @Override
    public NExprOpType operatorType() {
        requireOperatorContext();
        return operatorType;
    }

    private void requireOperatorContext() {
        if(!(contextType==NExprCallContextType.OPERATOR)){
            throw new NIllegalArgumentException(NMsg.ofC("operatorType is only available for operator context"));
        }
    }

    @Override
    public int operatorPrecedence() {
        requireOperatorContext();
        return operatorPrecedence;
    }

    @Override
    public NOperatorAssociativity operatorAssociativity() {
        requireOperatorContext();
        return operatorAssociativity;
    }
}
