package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NutsExpr;

public class DefaultLiteralNode implements NutsExpr.Node {
    private final Object lit;

    @Override
    public NutsExpr.NodeType getType() {
        return NutsExpr.NodeType.LITERAL;
    }


    @Override
    public NutsExpr.Node[] getChildren() {
        return new NutsExpr.Node[0];
    }

    @Override
    public String getName() {
        return null;
    }

    public DefaultLiteralNode(Object lit) {
        this.lit = lit;
    }

    @Override
    public Object eval(NutsExpr context) {
        return lit;
    }

    @Override
    public String toString() {
        if (lit == null) {
            return "null";
        }
        if (lit instanceof String) {
            StringBuilder sb = new StringBuilder("\"");
            for (char c : lit.toString().toCharArray()) {
                switch (c) {
                    case '"': {
                        sb.append("\\\"");
                        break;
                    }
                    case '\\': {
                        sb.append("\\\\");
                        break;
                    }
                    case '\n': {
                        sb.append("\\n");
                        break;
                    }
                    case '\r': {
                        sb.append("\\r");
                        break;
                    }
                    default: {
                        sb.append(c);
                    }
                }
            }
            sb.append("\"");
            return sb.toString();
        }
        return String.valueOf(lit);
    }
}
