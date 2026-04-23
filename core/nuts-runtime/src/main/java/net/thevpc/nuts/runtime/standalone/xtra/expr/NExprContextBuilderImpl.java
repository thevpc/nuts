package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.elem.NOperatorAssociativity;
import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.*;

public class NExprContextBuilderImpl implements NExprContextBuilder {
    public static final NExprResolverFromBuilderEmpty EMPTY_RESOLVER = new NExprResolverFromBuilderEmpty();
    private final NExprs exprs;
    private boolean autoDeclareVariables;
    private List<NExprResolver> evaluators;
    private List<NExprOperatorResolver> opEvaluators;
    private List<NExprFunctionResolver> fctEvaluators;
    private List<NExprConstructResolver> consEvaluators;
    private List<NExprVarResolver> varEvaluators;
    private Map<String, NExprFct> userFunctions;
    private Map<String, NExprConstruct> userConstructs;
    private Map<NExprOpNameAndType, NExprOp> ops;
    private Map<String, NExprVar> userVars;
    private NExprContext parent;

    public NExprContextBuilderImpl(NExprs exprs,NExprContext parent) {
        this.exprs = exprs;
        this.parent = parent;
    }

    public boolean isAutoDeclareVariables() {
        return autoDeclareVariables;
    }

    @Override
    public NExprContextBuilder setAutoDeclareVariables(boolean autoDeclareVariables) {
        this.autoDeclareVariables = autoDeclareVariables;
        return this;
    }

    private boolean hasNoCustomDeclarations() {
        if (userFunctions != null && !userFunctions.isEmpty()) {
            return false;
        }
        if (userConstructs != null && !userConstructs.isEmpty()) {
            return false;
        }
        if (ops != null && !ops.isEmpty()) {
            return false;
        }
        if (userVars != null && !userVars.isEmpty()) {
            return false;
        }
        if (evaluators != null && !evaluators.isEmpty()) {
            return false;
        }
        if (opEvaluators != null && !opEvaluators.isEmpty()) {
            return false;
        }
        if (fctEvaluators != null && !fctEvaluators.isEmpty()) {
            return false;
        }
        if (consEvaluators != null && !consEvaluators.isEmpty()) {
            return false;
        }
        return varEvaluators == null || varEvaluators.isEmpty();
    }



    @Override
    public NExprContextBuilder declareVars(Map<String, Object> variables) {
        return declareVars(new NExprVarResolver() {
            @Override
            public NOptional<NExprVar> getVar(String varName, NExprContext context) {
                if (variables.containsKey(varName)) {
                    return NOptional.of(NExprVar.ofMap(variables));
                }
                return NOptional.ofNamedEmpty(NMsg.ofC("variable %s", varName));
            }
        });
    }

    public NExprMutableContext buildMutable() {
        return (NExprMutableContext) build(true);
    }
    public NExprContext build() {
        return build(false);
    }

    public NExprContext build(boolean mutable) {
        if (hasNoCustomDeclarations()) {
            if(mutable){
                return new NExprMutableContextImpl(exprs, parent);
            }
            // wrap in child to enforce immutability over a potentially mutable parent
            return new NExprChildContextImpl(exprs, EMPTY_RESOLVER, parent);
        }
        NExprChildContextImpl c = new NExprChildContextImpl(exprs, new NExprResolverFromBuilder(this), parent);
        if(mutable){
            return new NExprMutableContextImpl(exprs, c);
        }
        return c;
    }

    @Override
    public NExprContextBuilder declareVars(NExprVarResolver resolver) {
        if (resolver != null) {
            if (varEvaluators == null) {
                varEvaluators = new ArrayList<>();
            }
        }
        varEvaluators.add(resolver);
        return this;
    }

    @Override
    public NExprContextBuilder declareConstructs(NExprConstructResolver resolver) {
        if (resolver != null) {
            if (consEvaluators == null) {
                consEvaluators = new ArrayList<>();
            }
        }
        consEvaluators.add(resolver);
        return this;
    }

    @Override
    public NExprContextBuilder removeOperators(NExprOperatorResolver resolver) {
        if (resolver != null) {
            if (opEvaluators != null) {
                opEvaluators.remove(resolver);
            }
        }
        return this;
    }

    @Override
    public NExprContextBuilder declareFunctions(NExprFunctionResolver resolver) {
        if (resolver != null) {
            if (fctEvaluators == null) {
                fctEvaluators = new ArrayList<>();
            }
        }
        fctEvaluators.add(resolver);
        return this;
    }

    @Override
    public NExprContextBuilder declareResolver(NExprResolver resolver) {
        if (resolver != null) {
            if (evaluators == null) {
                evaluators = new ArrayList<>();
            }
        }
        evaluators.add(resolver);
        return this;
    }

    @Override
    public NExprContextBuilder removeVars(NExprVarResolver resolver) {
        if (resolver != null) {
            if (varEvaluators != null) {
                varEvaluators.remove(resolver);
            }
        }
        varEvaluators.add(resolver);
        return this;
    }

    @Override
    public NExprContextBuilder removeConstructs(NExprConstructResolver resolver) {
        if (resolver != null) {
            if (consEvaluators != null) {
                consEvaluators.remove(resolver);
            }
        }
        return this;
    }

    @Override
    public NExprContextBuilder removeFunctions(NExprFunctionResolver resolver) {
        if (resolver != null) {
            if (fctEvaluators != null) {
                fctEvaluators.remove(resolver);
            }
        }
        return this;
    }

    @Override
    public NExprContextBuilder removeResolver(NExprResolver resolver) {
        if (resolver != null) {
            if (evaluators != null) {
                evaluators.remove(resolver);
            }
        }
        return this;
    }


    @Override
    public NExprContextBuilder declareOperators(NExprOperatorResolver resolver) {
        return null;
    }

    @Override
    public NExprContextBuilder declareConstruct(String name, NExprConstruct constructImpl) {
        if (constructImpl == null) {
            if (userConstructs != null) {
                userConstructs.remove(name);
            }
        } else {
            if (userConstructs == null) {
                userConstructs = new HashMap<>();
            }
            userConstructs.put(name, constructImpl);
        }
        return this;
    }

    @Override
    public NExprContextBuilder declareConstant(String name, Object value) {
        if (userVars == null) {
            userVars = new HashMap<>();
        }
        userVars.put(name, new ReservedNExprConst(name, value));
        return this;
    }


    @Override
    public NExprContextBuilder declareFunction(String name, NExprFct fctImpl) {
        if (fctImpl == null) {
            if (userFunctions != null) {
                userFunctions.remove(name);
            }
        } else {
            if (userFunctions == null) {
                userFunctions = new HashMap<>();
            }
            userFunctions.put(name, fctImpl);
        }
        return this;
    }

    @Override
    public NExprContextBuilder declareVar(String name) {
        if (userVars == null) {
            userVars = new HashMap<>();
        }
        if (!userVars.containsKey(name)) {
            userVars.put(name, new ReservedNExprVar(name, null));
        }
        return this;
    }

    @Override
    public NExprContextBuilder declareVar(String name, NExprVar varImpl) {
        if (varImpl == null) {
            if (userVars != null) {
                userVars.remove(name);
            }
        } else {
            if (userVars == null) {
                userVars = new HashMap<>();
            }
            userVars.put(name, varImpl);
        }
        return this;
    }

    @Override
    public NExprContextBuilder declareOperator(String name, NExprConstruct impl) {
        return declareOperator(name, null, impl);
    }

    @Override
    public NExprContextBuilder declareOperator(String name, NExprOpType type, NExprConstruct impl) {
        type = ExprOpHelper.resolveOpDefaultType(name, type);
        int prec = ExprOpHelper.resolveOpPrecedence(name, type, -1);
        NOperatorAssociativity ass = ExprOpHelper.resolveOpDefaultAssociativity(name, type, null);
        return declareOperator(name, type, prec, ass, impl);
    }

    @Override
    public NExprContextBuilder declareOperator(String name, NExprOpType type, int precedence, NOperatorAssociativity associativity, NExprConstruct impl) {
        NExprOpType typeOk = ExprOpHelper.resolveOpDefaultType(name, type);
        if (impl == null) {
            if (this.ops != null) {
                this.ops.remove(new NExprOpNameAndType(name, type));
            }
        } else {
            int prec = ExprOpHelper.resolveOpPrecedence(name, type, -1);
            NOperatorAssociativity ass = ExprOpHelper.resolveOpDefaultAssociativity(name, type, null);
            if (ops == null) {
                ops = new HashMap<>();
            }
            this.ops.put(new NExprOpNameAndType(name, type), new NExprOp() {
                @Override
                public NOperatorAssociativity getAssociativity() {
                    return ass;
                }

                @Override
                public NExprOpType getType() {
                    return typeOk;
                }

                @Override
                public int getPrecedence() {
                    return prec;
                }

                @Override
                public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                    return impl.eval(name, args, context);
                }
            });
        }
        return this;
    }

    @Override
    public NExprContextBuilder removeVar(String name) {
        if (userVars != null) {
            userVars.remove(name);
        }
        return this;
    }

    @Override
    public NExprContextBuilder removeFunction(String name) {
        if (userFunctions != null) {
            userFunctions.remove(name);
        }
        return this;
    }

    @Override
    public NExprContextBuilder removeConstruct(String name) {
        if (userConstructs != null) {
            userConstructs.remove(name);
        }
        return this;
    }

    @Override
    public NExprContextBuilder removeOperator(String name, NExprOpType type) {
        if (ops != null) {
            ops.remove(new NExprOpNameAndType(name, type));
        }
        return this;
    }

    private static class NExprResolverFromBuilderEmpty implements NExprResolver {
        @Override
        public NOptional<NExprFct> getFunction(String fctName, NExprNodeValue[] args, NExprContext context) {
            return NExprResolver.super.getFunction(fctName, args, context);
        }

        @Override
        public NOptional<NExprConstruct> getConstruct(String constructName, NExprNodeValue[] args, NExprContext context) {
            return NExprResolver.super.getConstruct(constructName, args, context);
        }

        @Override
        public NOptional<NExprOp> getOperator(String opName, NExprOpType type, NExprNodeValue[] args, NExprContext context) {
            return NExprResolver.super.getOperator(opName, type, args, context);
        }

        @Override
        public NOptional<NExprVar> getVar(String varName, NExprContext context) {
            return NExprResolver.super.getVar(varName, context);
        }
    }
    private static class NExprResolverFromBuilder implements NExprResolver {
        private final boolean autoDeclareVariables;
        private final List<NExprResolver> evaluators;
        private final List<NExprOperatorResolver> opEvaluators;
        private final List<NExprFunctionResolver> fctEvaluators;
        private final List<NExprConstructResolver> consEvaluators;
        private final List<NExprVarResolver> varEvaluators;
        private final Map<String, NExprFct> userFunctions;
        private final Map<String, NExprConstruct> userConstructs;
        private final Map<NExprOpNameAndType, NExprOp> ops;
        private final Map<String, NExprVar> userVars;

        public NExprResolverFromBuilder(NExprContextBuilderImpl a) {
            this.autoDeclareVariables = a.autoDeclareVariables;
            this.evaluators = a.evaluators == null ? null : new ArrayList<>(a.evaluators);
            this.opEvaluators = a.opEvaluators == null ? null : new ArrayList<>(a.opEvaluators);
            this.fctEvaluators = a.fctEvaluators == null ? null : new ArrayList<>(a.fctEvaluators);
            this.consEvaluators = a.consEvaluators == null ? null : new ArrayList<>(a.consEvaluators);
            this.varEvaluators = a.varEvaluators == null ? null : new ArrayList<>(a.varEvaluators);
            this.userFunctions = a.userFunctions == null ? null : new HashMap<>(a.userFunctions);
            this.userConstructs = a.userConstructs == null ? null : new HashMap<>(a.userConstructs);
            this.ops = a.ops == null ? null : new HashMap<>(a.ops);
            this.userVars = a.userVars == null ? null : new HashMap<>(a.userVars);
        }

        @Override
        public NOptional<NExprFct> getFunction(String fctName, NExprNodeValue[] args, NExprContext context) {
            if(userFunctions!=null) {
                NExprFct v = userFunctions.get(fctName);
                if (v != null) {
                    return NOptional.of(v);
                }
            }
            if (fctEvaluators != null) {
                for (NExprFunctionResolver fctEvaluator : fctEvaluators) {
                    NOptional<NExprFct> r = fctEvaluator.getFunction(fctName, args, context);
                    if (r != null && r.isPresent()) {
                        return r;
                    }
                }
            }
            if (evaluators != null) {
                for (NExprResolver fctEvaluator : evaluators) {
                    NOptional<NExprFct> r = fctEvaluator.getFunction(fctName, args, context);
                    if (r != null && r.isPresent()) {
                        return r;
                    }
                }
            }
            return NExprResolver.super.getFunction(fctName, args, context);
        }

        @Override
        public NOptional<NExprConstruct> getConstruct(String constructName, NExprNodeValue[] args, NExprContext context) {
            if(userConstructs!=null) {
                NExprConstruct v = userConstructs.get(constructName);
                if (v != null) {
                    return NOptional.of(v);
                }
            }
            if (consEvaluators != null) {
                for (NExprConstructResolver fctEvaluator : consEvaluators) {
                    NOptional<NExprConstruct> r = fctEvaluator.getConstruct(constructName, args, context);
                    if (r != null && r.isPresent()) {
                        return r;
                    }
                }
            }
            if (evaluators != null) {
                for (NExprResolver fctEvaluator : evaluators) {
                    NOptional<NExprConstruct> r = fctEvaluator.getConstruct(constructName, args, context);
                    if (r != null && r.isPresent()) {
                        return r;
                    }
                }
            }
            return NExprResolver.super.getConstruct(constructName, args, context);
        }

        @Override
        public NOptional<NExprOp> getOperator(String opName, NExprOpType type, NExprNodeValue[] args, NExprContext context) {
            if(ops!=null) {
                NExprOp v = ops.get(new NExprOpNameAndType(opName, type));
                if (v != null) {
                    return NOptional.of(v);
                }
            }
            if (opEvaluators != null) {
                for (NExprOperatorResolver fctEvaluator : opEvaluators) {
                    NOptional<NExprOp> r = fctEvaluator.getOperator(opName, type, args, context);
                    if (r != null && r.isPresent()) {
                        return r;
                    }
                }
            }
            if (evaluators != null) {
                for (NExprResolver fctEvaluator : evaluators) {
                    NOptional<NExprOp> r = fctEvaluator.getOperator(opName, type, args, context);
                    if (r != null && r.isPresent()) {
                        return r;
                    }
                }
            }
            return NExprResolver.super.getOperator(opName, type, args, context);
        }

        @Override
        public NOptional<NExprVar> getVar(String varName, NExprContext context) {
            if(userVars!=null) {
                NExprVar v = userVars.get(varName);
                if (v != null) {
                    if (context instanceof NExprMutableContext) {
                        ((NExprMutableContext) context).declareVar(varName, v);
                    }
                    return NOptional.of(v);
                }
            }
            if (varEvaluators != null) {
                for (NExprVarResolver fctEvaluator : varEvaluators) {
                    NOptional<NExprVar> r = fctEvaluator.getVar(varName, context);
                    if (r != null && r.isPresent()) {
                        if (context instanceof NExprMutableContext) {
                            ((NExprMutableContext) context).declareVar(varName, r.get());
                        }
                        return r;
                    }
                }
            }
            if (evaluators != null) {
                for (NExprResolver fctEvaluator : evaluators) {
                    NOptional<NExprVar> r = fctEvaluator.getVar(varName, context);
                    if (r != null && r.isPresent()) {
                        if (context instanceof NExprMutableContext) {
                            ((NExprMutableContext) context).declareVar(varName, r.get());
                        }
                        return r;
                    }
                }
            }
            if (autoDeclareVariables) {
                return NOptional.of(NExprVar.ofVar(varName));
            }
            return NExprResolver.super.getVar(varName, context);
        }
    }
}
