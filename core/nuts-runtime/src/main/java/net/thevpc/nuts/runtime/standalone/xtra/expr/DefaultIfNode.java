package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.Collections;
import java.util.List;

public class DefaultIfNode implements NExprIfNode {
    private final NExprNode conditionNode;
    private final NExprNode trueNode;
    private final NExprNode falseNode;

    @Override
    public NExprNodeType getType() {
        return NExprNodeType.IF;
    }


    @Override
    public List<NExprNode> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "if";
    }

    public DefaultIfNode(NExprNode conditionNode, NExprNode trueNode, NExprNode falseNode) {
        this.conditionNode = conditionNode;
        this.trueNode = trueNode;
        this.falseNode = falseNode;
    }

    public NExprNode getConditionNode() {
        return conditionNode;
    }

    public NExprNode getTrueNode() {
        return trueNode;
    }

    public NExprNode getFalseNode() {
        return falseNode;
    }

    @Override
    public NOptional<Object> eval(NExprDeclarations context) {
        try {
            NOptional<Object> c = conditionNode.eval(context);
            boolean bc = false;
            if (c.isPresent()) {
                Object y = c.get();
                if (y instanceof Boolean) {
                    bc = (boolean) y;
                } else {
                    bc = y != null;
                }
            } else {
                bc = false;
            }
            if (bc) {
                if (trueNode != null) {
                    return trueNode.eval(context);
                } else {
                    return NOptional.of(null);
                }
            } else {
                if (falseNode != null) {
                    return falseNode.eval(context);
                } else {
                    return NOptional.of(null);
                }
            }
        } catch (Exception ex) {
            return NOptional.ofError(x -> NMsg.ofC("error %s ", ex));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("if");
        sb.append("(").append(conditionNode == null ? "false" : conditionNode.toString()).append(")");
        sb.append("{ ");
        if (trueNode != null) {
            sb.append(trueNode);
        }
        sb.append("}");
        if (falseNode != null) {
            sb.append(" else { ");
            sb.append(falseNode);
            sb.append("}");
        }
        return sb.toString();
    }
}
