package net.vpc.toolbox.worky.fileprocessors.nodes;

public class ExprNodeLiteral implements ExprNode{
    private Object value;
    public ExprNodeLiteral(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        if(value==null){
            return "null";
        }
        if(value instanceof String){
            return "\""+value+"\"";
        }
        return String.valueOf(value);
    }
}
