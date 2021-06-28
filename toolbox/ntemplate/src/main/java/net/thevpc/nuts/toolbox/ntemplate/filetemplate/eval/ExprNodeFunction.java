package net.thevpc.nuts.toolbox.ntemplate.filetemplate.eval;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ExprNodeFunction implements ExprNode {

    private String name;
    private ExprNode[] args;

    public ExprNodeFunction(String name, ExprNode[] args) {
        this.name = name;
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public ExprNode[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        if (";".equals(name)) {
            return Arrays.stream(args).map(x -> x.toString()).collect(Collectors.joining(";\n"));
        }
        if ("set".equals(name) && args.length == 2) {
            return args[0] + "=" + args[1];
        }
        return name + "(" + Arrays.stream(args).map(x -> x.toString()).collect(Collectors.joining(",")) + ")";
    }
}
