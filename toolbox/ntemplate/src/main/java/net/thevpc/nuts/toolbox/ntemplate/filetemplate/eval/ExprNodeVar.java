package net.thevpc.nuts.toolbox.ntemplate.filetemplate.eval;

public class ExprNodeVar implements ExprNode {
    private String name;
    public ExprNodeVar(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }
}
