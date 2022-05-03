package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.util.NutsExpr;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    public List<NutsExpr.Node> getArgs() {
        return Arrays.asList(args);
    }

    @Override
    public NutsExpr.NodeType getType() {
        return NutsExpr.NodeType.FUNCTION;
    }

    @Override
    public List<NutsExpr.Node> getChildren() {
        return Collections.emptyList();
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
        throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.ofCstyle("function not found %s", getName()));
    }
}
