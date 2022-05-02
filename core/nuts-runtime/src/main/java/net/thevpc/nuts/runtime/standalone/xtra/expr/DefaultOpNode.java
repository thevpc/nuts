package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.NutsExpr;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;

import java.util.List;
import java.util.stream.Collectors;

public class DefaultOpNode implements NutsExpr.Node {
    private final String name;
    private final List<NutsExpr.Node> args;
    private final NutsExpr.OpType op;
    private final int precedence;

    public DefaultOpNode(String name, NutsExpr.OpType type, int precedence, List<NutsExpr.Node> args) {
        this.op = type;
        this.name = name;
        this.precedence = precedence;
        this.args = args;
    }

    public NutsExpr.Node getArg(int index) {
        if (index >= args.size()) {
            throw new IllegalArgumentException("Missing argument " + (index + 1) + " for " + name);
        }
        return args.get(index);
    }

    public List<NutsExpr.Node> getArgs() {
        return args;
    }

    @Override
    public NutsExpr.NodeType getType() {
        return NutsExpr.NodeType.OPERATOR;
    }

    @Override
    public List<NutsExpr.Node> getChildren() {
        return args;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        switch (op) {
            case PREFIX: {
                return name + " " + DefaultNutsExpr.wrapPars(args.get(0));
            }
            case POSTFIX: {
                return DefaultNutsExpr.wrapPars(args.get(0)) + name;
            }
            case INFIX: {
                if (args.size() == 2) {
                    return DefaultNutsExpr.wrapPars(args.get(0)) + " " + name + " " + DefaultNutsExpr.wrapPars(args.get(1));
                }
                if (args.size() > 2) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < args.size(); i++) {
                        if (i > 0) {
                            sb.append(" ").append(name).append(" ");
                        }
                        sb.append(DefaultNutsExpr.wrapPars(args.get(i)));
                    }
                    return sb.toString();
                }
            }
        }
        return name + "(" +
                args.stream().map(Object::toString).collect(Collectors.joining(",")) +
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
