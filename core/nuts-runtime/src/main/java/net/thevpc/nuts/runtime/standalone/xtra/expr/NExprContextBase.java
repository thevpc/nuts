package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.runtime.standalone.xtra.expr.template.NExprTemplateImpl;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NFunction2;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.expr.*;

import java.util.*;

public abstract class NExprContextBase implements NExprContext {
    protected NExprs exprs;

    public NExprContextBase(NExprs exprs) {
        this.exprs = exprs;
    }

    public NOptional<Object> evalFunction(String fctName, NExprNodeValue... args) {
        return getFunction(fctName, args).flatMap(x -> NOptional.ofNullable(x.eval(Arrays.asList(args), this)));
    }

    public NOptional<Object> evalConstruct(String constructName, NExprNodeValue... args) {
        return getConstruct(constructName, args).flatMap(x -> NOptional.ofNullable(x.eval(Arrays.asList(args), this)));
    }

    public NOptional<Object> evalOperator(String opName, NExprOpType type, NExprNodeValue... args) {
        return getOperator(opName, type, args).flatMap(x -> NOptional.ofNullable(x.eval(Arrays.asList(args), this)));
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

    public NOptional<Object> setVarValue(String varName, Object value) {
        return NOptional.of(getVar(varName).get().set(value, this));
    }

    public NOptional<Object> getVarValue(String varName) {
        NOptional<NExprVarDeclaration> var = getVar(varName);
        if (!var.isPresent()) {
            return var.map(x -> null);
        }
        return NOptional.ofNullable(var.get().get(this));
    }

    @Override
    public NExprContextBuilder childContext() {
        return new NExprContextBuilderImpl(exprs,this);
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
        return exprs.findCommonInfixOp(op, firstArgType, secondArgType);
    }

    @Override
    public <A> NOptional<NFunction<A, ?>> findCommonPrefixOp(NExprCommonOp op, Class<? extends A> argType) {
        return exprs.findCommonPrefixOp(op, argType);
    }

    @Override
    public <A> NOptional<NFunction<A, ?>> findCommonPostfixOp(NExprCommonOp op, Class<? extends A> argType) {
        return exprs.findCommonPostfixOp(op, argType);
    }

    @Override
    public NOptional<NExprFctDeclaration> getFunction(String fctName, NExprNodeValue... args) {
        return null;
    }

    @Override
    public NOptional<NExprConstructDeclaration> getConstruct(String constructName, NExprNodeValue... args) {
        return null;
    }

    @Override
    public NOptional<NExprOpDeclaration> getOperator(String opName, NExprOpType type, NExprNodeValue... args) {
        return null;
    }

    @Override
    public List<NExprOpDeclaration> getOperators() {
        return Collections.emptyList();
    }

    @Override
    public NOptional<NExprVarDeclaration> getVar(String varName) {
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
