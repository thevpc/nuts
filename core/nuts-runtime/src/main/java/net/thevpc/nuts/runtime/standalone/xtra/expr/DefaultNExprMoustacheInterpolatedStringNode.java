package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprContext;
import net.thevpc.nuts.expr.NExprInterpolatedStringNode;
import net.thevpc.nuts.expr.NExprNode;
import net.thevpc.nuts.expr.NExprNodeType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NOptional;

import java.util.Collections;
import java.util.List;

public class DefaultNExprMoustacheInterpolatedStringNode implements NExprInterpolatedStringNode {
    private final String expression;

    @Override
    public NExprNodeType nodeType() {
        return NExprNodeType.LITERAL;
    }

    @Override
    public String expression() {
        return expression;
    }

    @Override
    public List<NExprNode> children() {
        return Collections.emptyList();
    }

    @Override
    public String name() {
        return null;
    }

    public DefaultNExprMoustacheInterpolatedStringNode(String expression) {
        this.expression = expression;
    }

    @Override
    public NOptional<Object> eval(NExprContext context) {
        return NOptional.of(NMsg.ofM(expression == null ? "" : expression, new NFunction<String, Object>() {
            @Override
            public Object apply(String s) {
                return context.getVarValue(s).get();
            }
        }).toString());
    }

    @Override
    public String toString() {
        return "{{" + (expression == null ? "" : expression)+"}}";
    }
}
