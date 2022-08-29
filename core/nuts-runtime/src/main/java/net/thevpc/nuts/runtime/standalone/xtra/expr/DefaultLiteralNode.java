package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.util.*;

import java.util.Collections;
import java.util.List;

public class DefaultLiteralNode implements NutsExprLiteralNode {
    private final Object lit;

    @Override
    public NutsExprNodeType getType() {
        return NutsExprNodeType.LITERAL;
    }

    @Override
    public Object getValue() {
        return lit;
    }

    @Override
    public List<NutsExprNode> getChildren() {
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
    public NutsOptional<Object> eval(NutsExprDeclarations context) {
        return NutsOptional.ofNullable(lit);
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
