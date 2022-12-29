package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.util.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultFunctionNode implements NExprFunctionNode {
    private final String name;
    private final NExprNode[] args;

    public DefaultFunctionNode(String name, NExprNode[] args) {
        this.name = name;
        this.args = args;
    }

    public NExprNode getArgument(int index) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Missing argument " + (index + 1) + " for " + name);
        }
        return args[index];
    }

    public List<NExprNode> getArguments() {
        return Arrays.asList(args);
    }

    @Override
    public NExprNodeType getType() {
        return NExprNodeType.FUNCTION;
    }

    @Override
    public List<NExprNode> getChildren() {
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
    public NOptional<Object> eval(NExprDeclarations context) {
        try {
            return context.evalFunction(getName(),
                    Arrays.stream(args).map(x -> x.eval(context).get()).toArray()
            );
        }catch (Exception ex){
            return NOptional.ofError(x -> NMsg.ofCstyle("error %s ", ex));
        }
    }
}
