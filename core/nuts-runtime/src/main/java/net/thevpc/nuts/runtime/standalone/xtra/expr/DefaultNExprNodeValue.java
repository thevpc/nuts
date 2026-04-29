package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprContext;
import net.thevpc.nuts.expr.NExprNode;
import net.thevpc.nuts.expr.NExprNodeType;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

public class DefaultNExprNodeValue implements NExprNodeValue {
    private NExprNode node;
    private NExprContext boundContext;
    private NOptional<Object> value;

    public DefaultNExprNodeValue(NExprNode node, NExprContext boundContext) {
        this.node = node;
        this.boundContext = boundContext;
    }

    @Override
    public NOptional<Object> value() {
        if (value != null) {
            return value;
        }
        return value = node.eval(boundContext);
    }

    @Override
    public NExprNode node() {
        return node;
    }

    public NExprContext getBoundContext() {
        return boundContext;
    }

    @Override
    public NOptional<Object> eval(NExprContext context) {
        //ignore context and use bound context!!
        return value();
    }

    @Override
    public NExprNodeType nodeType() {
        return node.nodeType();
    }

    @Override
    public List<NExprNode> children() {
        return node.children();
    }

    @Override
    public String name() {
        return node.name();
    }

    @Override
    public String toString() {
        return node.toString();
    }
}
