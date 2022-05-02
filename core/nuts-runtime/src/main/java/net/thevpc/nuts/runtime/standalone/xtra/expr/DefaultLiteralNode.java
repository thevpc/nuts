package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.NutsExpr;

import java.util.Collections;
import java.util.List;

public class DefaultLiteralNode implements NutsExpr.Node {
    private final Object lit;

    @Override
    public NutsExpr.NodeType getType() {
        return NutsExpr.NodeType.LITERAL;
    }


    @Override
    public List<NutsExpr.Node> getChildren() {
        return Collections.emptyList();
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
