package net.vpc.toolbox.worky.fileprocessors.nodes;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ExprNodeFunction implements ExprNode{
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
        return name +"("+Arrays.stream(args).map(x->x.toString()).collect(Collectors.joining(","))+")";
    }
}
