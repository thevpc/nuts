package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.internal.expr.NExprRPI;
import net.thevpc.nuts.runtime.standalone.xtra.expr.template.NExprTemplateImpl;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NFunction2;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.expr.*;

import java.util.*;

public abstract class NExprContextBase implements NExprContext {
    protected NExprRPI rpi;

    public NExprContextBase(NExprRPI rpi) {
        this.rpi = rpi;
    }

    public NOptional<Object> evalFunction(String fctName, NExprNodeValue... args) {
        NExprCallContext  c=NExprCallContextImpl.ofFunction(fctName, Arrays.asList(args), this);
        return getFunction(fctName, args).flatMap(x -> NOptional.ofNullable(x.eval(c)));
    }

    public NOptional<Object> evalConstruct(String constructName, NExprNodeValue... args) {
        NExprCallContext  c=NExprCallContextImpl.ofConstruct(constructName, Arrays.asList(args), this);
        return getConstruct(constructName, args).flatMap(x -> NOptional.ofNullable(x.eval(c)));
    }

    public NOptional<Object> evalOperator(String opName, NExprOpType type, NExprNodeValue... args) {

        return getOperator(opName, type, args).flatMap(x -> NOptional.ofNullable(x.eval(
                NExprCallContextImpl.ofOperator(x.name(), Arrays.asList(args), this,x.operatorType(),x.operatorPrecedence(),x.operatorAssociativity())
        )));
    }

    @Override
    public NOptional<Object> evalInfixOperator(String opName, NExprNodeValue first, NExprNodeValue second) {
        return evalOperator(opName, NExprOpType.INFIX, first, second);
    }

    @Override
    public NOptional<Object> evalPrefixOperator(String opName, NExprNodeValue arg) {
        return evalOperator(opName, NExprOpType.PREFIX, arg);
    }

    @Override
    public NOptional<Object> evalPostfixOperator(String opName, NExprNodeValue arg) {
        return evalOperator(opName, NExprOpType.POSTFIX, arg);
    }



    public NOptional<Object> getVarValue(String varName) {
        NOptional<NExprVar> var = getVar(varName);
        if (!var.isPresent()) {
            return var.map(x -> null);
        }
        return NOptional.ofNullable(var.get().get(this));
    }

    @Override
    public NExprContextBuilder childContext() {
        return new NExprContextBuilderImpl(rpi, this);
    }

    @Override
    public NOptional<NExprNode> parse(String expression) {
        return new SyntaxParser(expression, new NExprWithCache(this)).parse();
    }

    @Override
    public NExprNodeValue bindLiteral(Object any) {
        return bindNode(NExprNode.ofLiteral(any));
    }

    @Override
    public NExprNodeValue bindNode(NExprNode any) {
        return new DefaultNExprNodeValue(any, this);
    }

    @Override
    public <A, B> NOptional<NFunction2<A, B, ?>> findCommonInfixOp(NExprCommonOp op, Class<? extends A> firstArgType, Class<? extends B> secondArgType) {
        return rpi.findCommonInfixOp(op, firstArgType, secondArgType);
    }

    @Override
    public <A> NOptional<NFunction<A, ?>> findCommonPrefixOp(NExprCommonOp op, Class<? extends A> argType) {
        return rpi.findCommonPrefixOp(op, argType);
    }

    @Override
    public <A> NOptional<NFunction<A, ?>> findCommonPostfixOp(NExprCommonOp op, Class<? extends A> argType) {
        return rpi.findCommonPostfixOp(op, argType);
    }

    @Override
    public NOptional<NExprFunction> getFunction(String fctName, NExprNodeValue... args) {
        return null;
    }

    @Override
    public NOptional<NExprFunction> getConstruct(String constructName, NExprNodeValue... args) {
        return null;
    }

    @Override
    public NOptional<NExprOperator> getOperator(String opName, NExprOpType type, NExprNodeValue... args) {
        return null;
    }

    @Override
    public List<NExprOperator> getOperators() {
        return Collections.emptyList();
    }

    @Override
    public NOptional<NExprVar> getVar(String varName) {
        return null;
    }


    @Override
    public NExprInterpolatedStrNode ofInterpolatedStr(String a) {
        return new DefaultNExprInterpolatedStrNode(a);
    }

    @Override
    public NExprTemplate ofTemplate() {
        return new NExprTemplateImpl(this);
    }
}
