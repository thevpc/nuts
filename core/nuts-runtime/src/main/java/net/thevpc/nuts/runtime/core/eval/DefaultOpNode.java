package net.thevpc.nuts.runtime.core.eval;

import net.thevpc.nuts.NutsExpr;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DefaultOpNode implements NutsExpr.Node {
    private final String name;
    private final NutsExpr.Node[] args;
    private final NutsExpr.OpType op;
    private final int precedence;

    public DefaultOpNode(String name, NutsExpr.OpType type, int precedence, NutsExpr.Node[] args) {
        this.op = type;
        this.name = name;
        this.precedence = precedence;
        this.args = args;
    }

    public NutsExpr.Node getArg(int index) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Missing argument " + (index + 1) + " for " + name);
        }
        return args[index];
    }

    public NutsExpr.Node[] getArgs() {
        return args;
    }

    @Override
    public NutsExpr.NodeType getType() {
        return NutsExpr.NodeType.OPERATOR;
    }

    @Override
    public NutsExpr.Node[] getChildren() {
        return args;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        switch (op) {
            case PREFIX: {
                return name + " " + DefaultNutsExpr.wrapPars(args[0]);
            }
            case POSTFIX: {
                return DefaultNutsExpr.wrapPars(args[0]) + name;
            }
            case INFIX: {
                if (args.length == 2) {
                    return DefaultNutsExpr.wrapPars(args[0]) + " " + name + " " + DefaultNutsExpr.wrapPars(args[1]);
                }
                if (args.length > 2) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < args.length; i++) {
                        if (i > 0) {
                            sb.append(" ").append(name).append(" ");
                        }
                        sb.append(DefaultNutsExpr.wrapPars(args[i]));
                    }
                    return sb.toString();
                }
            }
        }
        return name + "(" +
                Arrays.stream(args).map(Object::toString).collect(Collectors.joining(",")) +
                ')';
    }

    @Override
    public Object eval(NutsExpr context) {
        NutsExpr.Fct f = context.getFunction(getName());
        if (f != null) {
            return f.eval(getName(), getArgs(), context.newChild());
        }
        throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("function not found %s", getName()));
    }
}
