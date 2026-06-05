package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Collections;
import java.util.List;

public class DefaultNExprDollarInterpolatedStringNode implements NExprInterpolatedStringNode {
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

    public DefaultNExprDollarInterpolatedStringNode(String expression) {
        this.expression = expression;
    }

    @Override
    public NOptional<Object> eval(NExprContext context) {
        return NOptional.of(NMsg.ofV(expression == null ? "" : expression, new NFunction<String, Object>() {
            @Override
            public Object apply(String s) {
                NExprNode n = context.parse(s).orNull();
                if(n==null){
                    return s;
                }
                NOptional<Object> z = n.eval(context);
                if(z.isError()){
                    return s;
                }
                return z.orNull();
            }
        }).toString());
    }

    @Override
    public String toString() {
        return "$" + NStringUtils.formatStringLiteral(expression == null ? "" : expression);
    }
}
