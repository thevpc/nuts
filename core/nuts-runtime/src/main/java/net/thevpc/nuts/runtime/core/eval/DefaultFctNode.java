package net.thevpc.nuts.runtime.core.eval;

import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsExpr;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DefaultFctNode implements NutsExpr.Node {
    private final String name;
    private final NutsExpr.Node[] args;
    public DefaultFctNode(String name,NutsExpr.Node[] args) {
        this.name = name;
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
        return NutsExpr.NodeType.FUNCTION;
    }

    @Override
    public NutsExpr.Node[] getChildren() {
        return new NutsExpr.Node[0];
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
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
