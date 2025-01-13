package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Collections;
import java.util.List;

public class DefaultNExprInterpolatedStrNode implements NExprInterpolatedStrNode {
    private final String expression;

    @Override
    public NExprNodeType getType() {
        return NExprNodeType.LITERAL;
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public List<NExprNode> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return null;
    }

    public DefaultNExprInterpolatedStrNode(String expression) {
        this.expression = expression;
    }

    @Override
    public NOptional<Object> eval(NExprDeclarations context) {
        return NOptional.of(NMsg.ofV(expression == null ? "" : expression, new NFunction<String, Object>() {
            @Override
            public Object apply(String s) {
                return context.getVarValue(s).get();
            }
        }).toString());
    }

    @Override
    public String toString() {
        return "$" + NStringUtils.formatStringLiteral(expression == null ? "" : expression);
    }
}
