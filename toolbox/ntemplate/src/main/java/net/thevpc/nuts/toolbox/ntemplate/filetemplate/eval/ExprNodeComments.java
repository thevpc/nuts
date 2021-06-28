package net.thevpc.nuts.toolbox.ntemplate.filetemplate.eval;

public class ExprNodeComments implements ExprNode {

    private String type;
    private String value;

    public ExprNodeComments(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "#"+value;
    }
}
