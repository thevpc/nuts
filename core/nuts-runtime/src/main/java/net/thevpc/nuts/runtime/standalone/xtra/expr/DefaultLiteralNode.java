package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprLiteralNode;
import net.thevpc.nuts.expr.NExprNode;
import net.thevpc.nuts.expr.NExprNodeType;

import java.util.Collections;
import java.util.List;

public class DefaultLiteralNode implements NExprLiteralNode {
    private final Object lit;

    @Override
    public NExprNodeType getType() {
        return NExprNodeType.LITERAL;
    }

    @Override
    public Object getValue() {
        return lit;
    }

    @Override
    public List<NExprNode> getChildren() {
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
    public NOptional<Object> eval(NExprDeclarations context) {
        return NOptional.ofNullable(lit);
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
