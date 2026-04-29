package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.internal.expr.NExprRPI;
import net.thevpc.nuts.runtime.standalone.reflect.NPlatformSignatureImpl;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.Map;
import java.util.function.Function;

@NScore(fixed = NScorable.DEFAULT_SCORE)
@NComponentScope(NScopeType.WORKSPACE)
public class NExprRPIImpl implements NExprRPI {
    private final DefaultNExprsCommonOps defaultNExprsCommonOps = new DefaultNExprsCommonOps();
    DefaultRootContext defaultContext;
    EmptyRootContext emptyContext;

    public NExprRPIImpl() {
        defaultContext = new DefaultRootContext(this);
        emptyContext = new EmptyRootContext(this);
    }

    @Override
    public NExprContext createEmptyContext() {
        return emptyContext;
    }

    @Override
    public NExprContext createDefaultContext() {
        return defaultContext;
    }

    @Override
    public NExprVar createVar(String name, Object value) {
        return new ReservedNExprVar(name, value);
    }

    @Override
    public NExprVar createConst(String name, Object value) {
        return new ReservedNExprConst(name, value);
    }

    @Override
    public NExprVarResolver createLazyConstResolver(Function<String, Object> vars) {
        return new NExprVarResolver() {
            @Override
            public NOptional<NExprVar> getVar(String varName, NExprContext context) {
                if (vars == null) {
                    return NOptional.ofNamedEmpty(NMsg.ofC("variable %s", varName));
                }
                Object any=vars.apply(varName);
                if(any!=null){
                    return NOptional.of(createLazyConst(varName, c -> vars.apply(varName)));
                }
                return NOptional.ofNamedEmpty(NMsg.ofC("variable %s", varName));
            }
        };
    }

    @Override
    public NExprVarResolver createMapVarResolver(Map<String, Object> variables) {
        return new NExprVarResolver() {
            @Override
            public NOptional<NExprVar> getVar(String varName, NExprContext context) {
                if (variables != null) {
                    if (variables.containsKey(varName)) {
                        return NOptional.of(createVar(varName, c -> variables.get(varName), (c, v) -> variables.put(varName, v)));
                    }
                }
                return NOptional.ofNamedEmpty(NMsg.ofC("variable %s", varName));
            }
        };
    }

    @Override
    public NExprVarResolver createReadOnlyMapVarResolver(Map<String, Object> variables) {
        return new NExprVarResolver() {
            @Override
            public NOptional<NExprVar> getVar(String varName, NExprContext context) {
                if (variables != null) {
                    if (variables.containsKey(varName)) {
                        return NOptional.of(createReadOnlyVar(varName, c -> variables.get(varName)));
                    }
                }
                return NOptional.ofNamedEmpty(NMsg.ofC("variable %s", varName));
            }
        };
    }

    @Override
    public NExprVarResolver createReadOnlyVarResolver(Function<String, Object> vars) {
        return new NExprVarResolver() {
            @Override
            public NOptional<NExprVar> getVar(String varName, NExprContext context) {
                if (vars != null) {
                    return NOptional.of(createReadOnlyVar(varName, z -> vars.apply(varName)));
                }
                return NOptional.ofNamedEmpty(NMsg.ofC("variable %s", varName));
            }
        };
    }

    @Override
    public NExprVar createLazyConst(String name, NExprVarReader vars) {
        return new NExprVarConstFromFunction(name, vars);
    }

    @Override
    public NExprVar createReadOnlyVar(String name, NExprVarReader vars) {
        return new NExprVarReadOnlyFromFunction(name, vars);
    }

    @Override
    public NExprVar createVar(String name, NExprVarReader reader, NExprVarWriter writer) {
        return new NExprVarFromFunction(name, reader, writer);
    }

    @Override
    public <A, B> NOptional<NFunction2<A, B, ?>> findCommonInfixOp(NExprCommonOp op, Class<? extends A> firstArgType, Class<? extends B> secondArgType) {
        return (NOptional) defaultNExprsCommonOps.findFunction2(op, NExprOpType.INFIX, NPlatformSignatureImpl.of(firstArgType, secondArgType));
    }

    @Override
    public <A> NOptional<NFunction<A, ?>> findCommonPrefixOp(NExprCommonOp op, Class<? extends A> argType) {
        return (NOptional) defaultNExprsCommonOps.findFunction1(op, NExprOpType.PREFIX, NPlatformSignatureImpl.of(argType));
    }

    @Override
    public <A> NOptional<NFunction<A, ?>> findCommonPostfixOp(NExprCommonOp op, Class<? extends A> argType) {
        return (NOptional) defaultNExprsCommonOps.findFunction1(op, NExprOpType.POSTFIX, NPlatformSignatureImpl.of(argType));
    }

    @Override
    public NExprWordNode createExprWordNode(String a) {
        return new DefaultWordNode(a);
    }

    @Override
    public NExprLiteralNode createExprLiteralNode(Object a) {
        return new DefaultLiteralNode(a);
    }

    @Override
    public NExprFunction createFunction(String name, NExprCallHandler handler) {
        return new DefaultNExprFunction(name, handler);
    }

    @Override
    public NExprOperator createOperator(String name, NExprOpType operatorType, int operatorPrecedence, NOperatorAssociativity associativity,NExprCallHandler handler) {
        String nameOk=NAssert.requireNamedNonNull(NStringUtils.trimToNull(name),"name");
        NExprOpType typeOk = ExprOpHelper.resolveOpDefaultType(name, operatorType);
        NOperatorAssociativity assOk = ExprOpHelper.resolveOpDefaultAssociativity(name, typeOk,associativity);
        int precOk = ExprOpHelper.resolveOpPrecedence(name, typeOk,operatorPrecedence);
        return new DefaultNExprOpDeclaration(nameOk,typeOk, precOk, assOk, handler);
    }
}
