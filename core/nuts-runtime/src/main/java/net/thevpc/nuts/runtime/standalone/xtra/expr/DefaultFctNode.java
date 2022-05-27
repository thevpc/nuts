package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultFctNode implements NutsExprNode {
    private final String name;
    private final NutsExprNode[] args;

    public DefaultFctNode(String name, NutsExprNode[] args) {
        this.name = name;
        this.args = args;
    }

    public NutsExprNode getArg(int index) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Missing argument " + (index + 1) + " for " + name);
        }
        return args[index];
    }

    public List<NutsExprNode> getArgs() {
        return Arrays.asList(args);
    }

    @Override
    public NutsExprNodeType getType() {
        return NutsExprNodeType.FUNCTION;
    }

    @Override
    public List<NutsExprNode> getChildren() {
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
    public Object eval(NutsExprDeclarations context) {
        return context.evalFunction(getName(),
                Arrays.stream(args).map(x -> x.eval(context)).toArray()
        );
    }
}
