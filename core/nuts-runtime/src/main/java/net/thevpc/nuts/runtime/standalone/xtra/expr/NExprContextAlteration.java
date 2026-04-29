package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

import java.util.*;
import java.util.function.Supplier;

public class NExprContextAlteration {
    public static DecInfo REMOVED = new DecInfo(null);
    public static final NExprResolverFromBuilderEmpty EMPTY_RESOLVER = new NExprResolverFromBuilderEmpty();
    private boolean autoDeclareVariables;
    private List<NExprResolver> evaluators;
    private List<NExprOperatorResolver> opEvaluators;
    private List<NExprFunctionResolver> fctEvaluators;
    private List<NExprFunctionResolver> consEvaluators;
    private List<NExprVarResolver> varEvaluators;

    public Map<String, DecInfo<NExprFunction>> userFunctions;
    public Map<String, DecInfo<NExprFunction>> userConstructs;
    public Map<NExprOpNameAndType, DecInfo<NExprOperator>> userOperators;
    public Map<String, DecInfo<NExprVar>> userVars;


    public void addAll(NExprContextAlteration alteration) {
        if (alteration != null) {
            if (alteration.isAutoDeclareVariables()) {
                autoDeclareVariables = true;
            }
            if (alteration.userFunctions != null && !alteration.userFunctions.isEmpty()) {
                if (userFunctions == null) {
                    userFunctions = new HashMap<>();
                }
                userFunctions.putAll(alteration.userFunctions);
            }
            if (alteration.userConstructs != null && !alteration.userConstructs.isEmpty()) {
                if (userConstructs == null) {
                    userConstructs = new HashMap<>();
                }
                userConstructs.putAll(alteration.userConstructs);
            }
            if (alteration.userOperators != null && !alteration.userOperators.isEmpty()) {
                if (userOperators == null) {
                    userOperators = new HashMap<>();
                }
                userOperators.putAll(alteration.userOperators);
            }
            if (alteration.userVars != null && !alteration.userVars.isEmpty()) {
                if (userVars == null) {
                    userVars = new HashMap<>();
                }
                userVars.putAll(alteration.userVars);
            }
            if (alteration.evaluators != null && !alteration.evaluators.isEmpty()) {
                if (evaluators == null) {
                    evaluators = new ArrayList<>();
                }
                evaluators.addAll(alteration.evaluators);
            }
            if (alteration.opEvaluators != null && !alteration.opEvaluators.isEmpty()) {
                if (opEvaluators == null) {
                    opEvaluators = new ArrayList<>();
                }
                opEvaluators.addAll(alteration.opEvaluators);
            }
            if (alteration.fctEvaluators != null && !alteration.fctEvaluators.isEmpty()) {
                if (fctEvaluators == null) {
                    fctEvaluators = new ArrayList<>();
                }
                fctEvaluators.addAll(alteration.fctEvaluators);
            }
            if (alteration.consEvaluators != null && !alteration.consEvaluators.isEmpty()) {
                if (consEvaluators == null) {
                    consEvaluators = new ArrayList<>();
                }
                consEvaluators.addAll(alteration.consEvaluators);
            }
            if (alteration.varEvaluators != null && !alteration.varEvaluators.isEmpty()) {
                if (varEvaluators == null) {
                    varEvaluators = new ArrayList<>();
                }
                varEvaluators.addAll(alteration.varEvaluators);
            }
        }
    }

    public static class DecInfo<T> {
        public final T value;

        public DecInfo(T value) {
            this.value = value;
        }
    }

    public boolean isAutoDeclareVariables() {
        return autoDeclareVariables;
    }

    public void setAutoDeclareVariables(boolean autoDeclareVariables) {
        this.autoDeclareVariables = autoDeclareVariables;
    }

    public void removeOperator(String name, NExprOpType type) {
        if (userOperators != null) {
            userOperators.remove(new NExprOpNameAndType(name, type));
        }
    }

    public boolean hasNoCustomDeclarations() {
        if (userFunctions != null && !userFunctions.isEmpty()) {
            return false;
        }
        if (userConstructs != null && !userConstructs.isEmpty()) {
            return false;
        }
        if (userOperators != null && !userOperators.isEmpty()) {
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

    /// /////////////////////////////////////

    public NExprVar declareVar(NExprVar varImpl) {
        NAssert.requireNamedNonNull(varImpl, "variable");
        String name = varImpl.getName();
        if (userVars == null) {
            userVars = new HashMap<>();
        }
        userVars.put(name, new NExprContextAlteration.DecInfo<>(varImpl));
        return varImpl;
    }

    public NExprFunction declareFunction(String name, NExprCallHandler fctImpl) {
        if (!NBlankable.isBlank(name)) {
            if (userFunctions == null) {
                userFunctions = new HashMap<>();
            }
            if (fctImpl == null) {
                userFunctions.put(name, REMOVED);
            } else {
                DefaultNExprFunction r = new DefaultNExprFunction(name, fctImpl);
                userFunctions.put(name, new DecInfo<>(r));
                return r;
            }
        }
        return null;
    }

    public NExprFunction declareFunction(NExprFunction fctImpl) {
        if (fctImpl != null) {
            if (userFunctions == null) {
                userFunctions = new HashMap<>();
            }
            userFunctions.put(fctImpl.name(), new DecInfo<>(fctImpl));
            return fctImpl;
        }
        return null;
    }


    public NExprFunction declareConstruct(NExprFunction constructImpl) {
        if (constructImpl != null) {
            if (userConstructs == null) {
                userConstructs = new HashMap<>();
            }
            userConstructs.put(constructImpl.name(), new DecInfo<>(constructImpl));
            return constructImpl;
        }
        return null;
    }

    public NExprFunction declareConstruct(String name, NExprCallHandler constructImpl) {
        if (!NBlankable.isBlank(name)) {
            if (userConstructs == null) {
                userConstructs = new HashMap<>();
            }
            if (constructImpl == null) {
                userConstructs.put(name, REMOVED);
            } else {
                NExprFunction r = new DefaultNExprFunction(name, constructImpl);
                userConstructs.put(name, new DecInfo<>(r));
                return r;
            }
        }
        return null;
    }

    public NExprOperator declareOperator(String name, NExprCallHandler impl) {
        return declareOperator(name, null, impl);
    }

    public NExprOperator declareOperator(NExprOperator impl) {
        NAssert.requireNamedNonBlank(impl.name(), "name");
        if (impl.operatorType() == null || impl.operatorPrecedence() < 0 || impl.operatorAssociativity() == null) {
            String name = impl.name();
            NExprOpType type = ExprOpHelper.resolveOpDefaultType(name, impl.operatorType());
            int prec = ExprOpHelper.resolveOpPrecedence(name, type, -1);
            NOperatorAssociativity associativity = ExprOpHelper.resolveOpDefaultAssociativity(name, type, impl.operatorAssociativity());
            NExprOperator finalImpl = impl;
            impl = new DefaultNExprOpDeclaration(name, type, prec, associativity, c -> finalImpl.eval(c));
        }
        if (this.userOperators == null) {
            this.userOperators = new HashMap<>();
        }
        this.userOperators.put(new NExprOpNameAndType(impl.name(), impl.operatorType()), new DecInfo<>(impl));
        return impl;
    }

    public NExprOperator declareOperator(String name, NExprOpType type, NExprCallHandler impl) {
        type = ExprOpHelper.resolveOpDefaultType(name, type);
        int prec = ExprOpHelper.resolveOpPrecedence(name, type, -1);
        NOperatorAssociativity ass = ExprOpHelper.resolveOpDefaultAssociativity(name, type, null);
        return declareOperator(name, type, prec, ass, impl);
    }

    public NExprOperator declareOperator(String name, NExprOpType type, int precedence, NOperatorAssociativity associativity, NExprCallHandler impl) {
        NAssert.requireNamedNonBlank(name, "name");
        NExprOpType typeOk = ExprOpHelper.resolveOpDefaultType(name, type);
        if (this.userOperators == null) {
            this.userOperators = new HashMap<>();
        }
        if (impl == null) {
            this.userOperators.put(new NExprOpNameAndType(name, type), REMOVED);
            return null;
        } else {
            int prec = ExprOpHelper.resolveOpPrecedence(name, typeOk, -1);
            NOperatorAssociativity associativityOk = ExprOpHelper.resolveOpDefaultAssociativity(name, typeOk, associativity);
            DefaultNExprOpDeclaration d = new DefaultNExprOpDeclaration(name, typeOk, prec, associativityOk, impl);
            this.userOperators.put(new NExprOpNameAndType(name, typeOk), new DecInfo<>(d));
            return d;
        }
    }

    public void undeclareVar(NExprVar member) {
        if (member != null) {
            if (userVars != null) {
                userVars.remove(member.getName());
            }
        }
    }

    public void removeVar(NExprVar member) {
        if (member != null) {
            if (userVars == null) {
                userVars = new HashMap<>();
            }
            userVars.put(member.getName(), REMOVED);
        }
    }

    public void removeVar(String name) {
        if (name != null) {
            if (userVars == null) {
                userVars = new HashMap<>();
            }
            userVars.put(name, REMOVED);
        }
    }

    public void undeclareFunction(NExprFunction member) {
        if (member != null) {
            if (userFunctions != null) {
                userFunctions.remove(member.name());
            }
        }
    }

    public void removeFunction(NExprFunction member) {
        if (member != null) {
            if (userFunctions == null) {
                userFunctions = new HashMap<>();
            }
            userFunctions.put(member.name(), REMOVED);
        }
    }

    public void removeFunction(String member) {
        if (member != null) {
            if (userFunctions == null) {
                userFunctions = new HashMap<>();
            }
            userFunctions.put(member, REMOVED);
        }
    }

    public void removeConstruct(String member) {
        if (member != null) {
            if (userConstructs == null) {
                userConstructs.put(member, REMOVED);
            }
        }
    }

    public void undeclareConstruct(NExprFunction member) {
        if (member != null) {
            if (userConstructs != null) {
                userConstructs.remove(member.name());
            }
        }
    }

    public void removeConstruct(NExprFunction member) {
        if (member != null) {
            if (userConstructs == null) {
                userConstructs = new HashMap<>();
            }
            userConstructs.put(member.name(), REMOVED);
        }
    }

    public void undeclareOperator(NExprOperator member) {
        if (member != null) {
            if (this.userOperators != null) {
                this.userOperators.remove(t(member));
            }
        }
    }

    public void removeOperator(NExprOperator member) {
        if (member != null) {
            if (this.userOperators == null) {
                this.userOperators = new HashMap<>();
            }
            this.userOperators.put(t(member), REMOVED);
        }
    }

    private NExprOpNameAndType t(NExprOperator member) {
        return new NExprOpNameAndType(member.name(), member.operatorType());
    }


    public NExprVar declareVar(String name) {
        return declareVar(NExprVar.ofVar(name));
    }

    /// ///////////////////


    public NOptional<NExprOperator> getOperator(NExprContext parent, String opName, NExprOpType type, NExprNodeValue... nodes) {
        if (userOperators != null) {
            NExprContextAlteration.DecInfo<NExprOperator> f = userOperators.get(new NExprOpNameAndType(opName, type));
            if (f != null) {
                if (f.value != null) {
                    return NOptional.of(f.value);
                }
                return NOptional.ofEmpty(() -> NMsg.ofC("operator not found %s", opName));
            }
        }
        if (parent != null) {
            return parent.getOperator(opName, type);
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("operator not found %s", opName));
    }


    public NOptional<NExprVar> getVar(NExprContext parent, String name) {
        if (this.userVars != null) {
            NExprContextAlteration.DecInfo<NExprVar> f = this.userVars.get(name);
            if (f != null) {
                if (f.value != null) {
                    return NOptional.of(f.value);
                }
                return NOptional.ofEmpty(() -> NMsg.ofC("expr var not found %s", name));
            }
        }
        if (parent != null) {
            return parent.getVar(name);
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("expr var not found %s", name));
    }

    public NExprVar getOrDeclareVar(NExprContext context, NExprContext parent, String name, Supplier<Object> value) {
        NExprVar o = getVar(parent, name).orNull();
        if (o != null) {
            return o;
        }
        NExprVar e = declareVar(name);
        if (value != null) {
            e.set(value.get(), context);
        }
        return e;
    }

    public NOptional<NExprFunction> getFunction(NExprContext parent, String name, NExprNodeValue... args) {
        if (this.userFunctions != null) {
            NExprContextAlteration.DecInfo<NExprFunction> f = this.userFunctions.get(name);
            if (f != null) {
                if (f.value != null) {
                    return NOptional.of(f.value);
                }
                return NOptional.ofEmpty(() -> NMsg.ofC("function not found %s", name));
            }
        }
        if (parent != null) {
            return parent.getFunction(name, args);
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("function not found %s", name));
    }

    public NOptional<NExprFunction> getConstruct(NExprContext parent, String name, NExprNodeValue... args) {
        if (this.userConstructs != null) {
            NExprContextAlteration.DecInfo<NExprFunction> f = this.userConstructs.get(name);
            if (f != null) {
                if (f.value != null) {
                    return NOptional.of(f.value);
                }
                return NOptional.ofEmpty(() -> NMsg.ofC("construct not found %s", name));
            }
        }
        if (parent != null) {
            return parent.getConstruct(name, args);
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("construct not found %s", name));
    }

    /// ////////////////////////////////////////////// RESOLVERS


    public void declareVars(NExprVarResolver resolver) {
        if (resolver != null) {
            if (varEvaluators == null) {
                varEvaluators = new ArrayList<>();
            }
            varEvaluators.add(resolver);
        }
    }

    public void declareConstructs(NExprFunctionResolver resolver) {
        if (resolver != null) {
            if (consEvaluators == null) {
                consEvaluators = new ArrayList<>();
            }
        }
        consEvaluators.add(resolver);
    }

    public void removeOperators(NExprOperatorResolver resolver) {
        if (resolver != null) {
            if (opEvaluators != null) {
                opEvaluators.remove(resolver);
            }
        }
    }

    public void declareFunctions(NExprFunctionResolver resolver) {
        if (resolver != null) {
            if (fctEvaluators == null) {
                fctEvaluators = new ArrayList<>();
            }
        }
        fctEvaluators.add(resolver);
    }

    public void declareResolver(NExprResolver resolver) {
        if (resolver != null) {
            if (evaluators == null) {
                evaluators = new ArrayList<>();
            }
            evaluators.add(resolver);
        }
    }

    public void removeVars(NExprVarResolver resolver) {
        if (resolver != null) {
            if (varEvaluators != null) {
                varEvaluators.remove(resolver);
            }
        }
    }

    public void removeConstructs(NExprFunctionResolver resolver) {
        if (resolver != null) {
            if (consEvaluators != null) {
                consEvaluators.remove(resolver);
            }
        }
    }

    public void removeFunctions(NExprFunctionResolver resolver) {
        if (resolver != null) {
            if (fctEvaluators != null) {
                fctEvaluators.remove(resolver);
            }
        }
    }

    public void removeResolver(NExprResolver resolver) {
        if (resolver != null) {
            if (evaluators != null) {
                evaluators.remove(resolver);
            }
        }
    }


    public void declareOperators(NExprOperatorResolver resolver) {
        if (resolver != null) {
            if (opEvaluators == null) {
                opEvaluators = new ArrayList<>();
            }
            opEvaluators.add(resolver);
        }
    }


    /// //////////////////////////////////////////////


    private static class NExprResolverFromBuilderEmpty implements NExprResolver {
        @Override
        public NOptional<NExprFunction> getFunction(String fctName, NExprNodeValue[] args, NExprContext context) {
            return NExprResolver.super.getFunction(fctName, args, context);
        }

        @Override
        public NOptional<NExprFunction> getConstruct(String constructName, NExprNodeValue[] args, NExprContext context) {
            return NExprResolver.super.getConstruct(constructName, args, context);
        }

        @Override
        public NOptional<NExprOperator> getOperator(String opName, NExprOpType type, NExprNodeValue[] args, NExprContext context) {
            return NExprResolver.super.getOperator(opName, type, args, context);
        }

        @Override
        public NOptional<NExprVar> getVar(String varName, NExprContext context) {
            return NExprResolver.super.getVar(varName, context);
        }
    }

    /// /////////////

    public NExprResolver toExprResolver() {
        return new NExprResolverFromAlteration(this);
    }

    private static class NExprResolverFromAlteration implements NExprResolver {
        private final boolean autoDeclareVariables;
        private final List<NExprResolver> evaluators;
        private final List<NExprOperatorResolver> opEvaluators;
        private final List<NExprFunctionResolver> fctEvaluators;
        private final List<NExprFunctionResolver> consEvaluators;
        private final List<NExprVarResolver> varEvaluators;
        private final Map<String, DecInfo<NExprFunction>> userFunctions;
        private final Map<String, DecInfo<NExprFunction>> userConstructs;
        private final Map<NExprOpNameAndType, DecInfo<NExprOperator>> ops;
        private final Map<String, DecInfo<NExprVar>> userVars;

        public NExprResolverFromAlteration(NExprContextAlteration a) {
            this.autoDeclareVariables = a.autoDeclareVariables;
            this.evaluators = a.evaluators == null ? null : new ArrayList<>(a.evaluators);
            this.opEvaluators = a.opEvaluators == null ? null : new ArrayList<>(a.opEvaluators);
            this.fctEvaluators = a.fctEvaluators == null ? null : new ArrayList<>(a.fctEvaluators);
            this.consEvaluators = a.consEvaluators == null ? null : new ArrayList<>(a.consEvaluators);
            this.varEvaluators = a.varEvaluators == null ? null : new ArrayList<>(a.varEvaluators);
            this.userFunctions = a.userFunctions == null ? null : new HashMap<>(a.userFunctions);
            this.userConstructs = a.userConstructs == null ? null : new HashMap<>(a.userConstructs);
            this.ops = a.userOperators == null ? null : new HashMap<>(a.userOperators);
            this.userVars = a.userVars == null ? null : new HashMap<>(a.userVars);
        }

        @Override
        public NOptional<NExprFunction> getFunction(String fctName, NExprNodeValue[] args, NExprContext context) {
            if (userFunctions != null) {
                DecInfo<NExprFunction> d = userFunctions.get(fctName);
                if (d != null) {
                    NExprFunction v = d.value;
                    if (v != null) {
                        return NOptional.of(v);
                    } else {
                        return NOptional.ofEmpty(() -> NMsg.ofC("function not found %s", fctName));
                    }
                }
            }
            if (fctEvaluators != null) {
                for (NExprFunctionResolver fctEvaluator : fctEvaluators) {
                    NOptional<NExprFunction> r = fctEvaluator.getFunction(fctName, args, context);
                    if (r != null && r.isPresent()) {
                        return r;
                    }
                }
            }
            if (evaluators != null) {
                for (NExprResolver fctEvaluator : evaluators) {
                    NOptional<NExprFunction> r = fctEvaluator.getFunction(fctName, args, context);
                    if (r != null && r.isPresent()) {
                        return r;
                    }
                }
            }
            return NExprResolver.super.getFunction(fctName, args, context);
        }

        @Override
        public NOptional<NExprFunction> getConstruct(String constructName, NExprNodeValue[] args, NExprContext context) {
            if (userConstructs != null) {
                DecInfo<NExprFunction> d = userConstructs.get(constructName);
                if (d != null) {
                    NExprFunction v = d.value;
                    if (v != null) {
                        return NOptional.of(v);
                    }
                    return NOptional.ofEmpty(() -> NMsg.ofC("construct not found %s", constructName));
                }
            }
            if (consEvaluators != null) {
                for (NExprFunctionResolver fctEvaluator : consEvaluators) {
                    NOptional<NExprFunction> r = fctEvaluator.getFunction(constructName, args, context);
                    if (r != null && r.isPresent()) {
                        return r;
                    }
                }
            }
            if (evaluators != null) {
                for (NExprResolver fctEvaluator : evaluators) {
                    NOptional<NExprFunction> r = fctEvaluator.getConstruct(constructName, args, context);
                    if (r != null && r.isPresent()) {
                        return r;
                    }
                }
            }
            return NExprResolver.super.getConstruct(constructName, args, context);
        }

        @Override
        public NOptional<NExprOperator> getOperator(String opName, NExprOpType type, NExprNodeValue[] args, NExprContext context) {
            if (ops != null) {
                DecInfo<NExprOperator> d = ops.get(new NExprOpNameAndType(opName, type));
                if (d != null) {
                    NExprOperator v = d.value;
                    if (v != null) {
                        return NOptional.of(v);
                    }
                    return NOptional.ofEmpty(() -> NMsg.ofC("operator not found %s", opName));
                }
            }
            if (opEvaluators != null) {
                for (NExprOperatorResolver fctEvaluator : opEvaluators) {
                    NOptional<NExprOperator> r = fctEvaluator.getOperator(opName, type, args, context);
                    if (r != null && r.isPresent()) {
                        return r;
                    }
                }
            }
            if (evaluators != null) {
                for (NExprResolver fctEvaluator : evaluators) {
                    NOptional<NExprOperator> r = fctEvaluator.getOperator(opName, type, args, context);
                    if (r != null && r.isPresent()) {
                        return r;
                    }
                }
            }
            return NExprResolver.super.getOperator(opName, type, args, context);
        }

        @Override
        public NOptional<NExprVar> getVar(String varName, NExprContext context) {
            if (userVars != null) {
                DecInfo<NExprVar> d = userVars.get(varName);
                if (d != null) {
                    NExprVar v = (NExprVar) d;
                    if (v != null) {
                        if (context instanceof NExprMutableContext) {
                            ((NExprMutableContext) context).declareVar(v);
                        }
                        return NOptional.of(v);
                    }
                    return NOptional.ofEmpty(() -> NMsg.ofC("variable not found %s", varName));
                }
            }
            if (varEvaluators != null) {
                for (NExprVarResolver fctEvaluator : varEvaluators) {
                    NOptional<NExprVar> r = fctEvaluator.getVar(varName, context);
                    if (r != null && r.isPresent()) {
                        if (context instanceof NExprMutableContext) {
                            ((NExprMutableContext) context).declareVar(r.get());
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
                            ((NExprMutableContext) context).declareVar(r.get());
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
