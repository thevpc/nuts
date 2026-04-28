package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.elem.NOperatorAssociativity;
import net.thevpc.nuts.internal.expr.NExprRPI;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.expr.*;

import java.util.*;
import java.util.function.Supplier;

public class NExprMutableContextImpl extends NExprContextBase implements NExprMutableContext {
    private static DecInfo REMOVED = new DecInfo(null);

    private final Map<String, DecInfo<NExprFunction>> userFunctions = new LinkedHashMap<>();
    private final Map<String, DecInfo<NExprFunction>> userConstructs = new LinkedHashMap<>();
    private final Map<NExprOpNameAndType, DecInfo<NExprOperator>> ops = new LinkedHashMap<>();
    private final Map<String, DecInfo<NExprVar>> userVars = new LinkedHashMap<>();


    private NExprContext parent;

    public NExprMutableContextImpl(NExprRPI nExprRPI, NExprContext parent) {
        super(nExprRPI);
        this.parent = parent;
    }

    @Override
    public NOptional<NExprVar> getVar(String name) {
        DecInfo<NExprVar> f = userVars.get(name);
        if (f != null) {
            if (f.value != null) {
                return NOptional.of(f.value);
            }
        } else if (parent != null) {
            return parent.getVar(name);
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("expr var not found %s", name));
    }

    @Override
    public NExprVar getOrDeclareVar(String name, Supplier<Object> value) {
        NExprVar o = getVar(name).orNull();
        if(o!=null){
            return o;
        }
        NExprVar e = declareVar(name);
        if(value!=null){
            e.set(value.get(),this);
        }
        return e;
    }

    @Override
    public NOptional<NExprFunction> getFunction(String name, NExprNodeValue... args) {
        DecInfo<NExprFunction> f = userFunctions.get(name);
        if (f != null) {
            if (f.value != null) {
                return NOptional.of(f.value);
            }
        } else if (parent != null) {
            return parent.getFunction(name, args);
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("function not found %s", name));
    }

    @Override
    public NOptional<NExprFunction> getConstruct(String name, NExprNodeValue... args) {
        DecInfo<NExprFunction> f = userConstructs.get(name);
        if (f != null) {
            if (f.value != null) {
                return NOptional.of(f.value);
            }
        } else if (parent != null) {
            return parent.getConstruct(name, args);
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("construct not found %s", name));
    }

    @Override
    public NExprVar declareVar(NExprVar varImpl) {
        NAssert.requireNamedNonNull(varImpl,"variable");
        String name = varImpl.getName();
        userVars.put(name, new DecInfo<>(varImpl));
        return varImpl;
    }

    @Override
    public NExprVar declareVar(String name) {
        return declareVar(NExprVar.ofVar(name));
    }

    @Override
    public NExprVar declareConstant(String name, Object value) {
        return declareVar(NExprVar.ofConst(name,value));
    }

    @Override
    public NExprFunction declareFunction(String name, NExprFunctionHandler fctImpl) {
        if (!NBlankable.isBlank(name)) {
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

    @Override
    public NExprFunction declareFunction(NExprFunction fctImpl) {
        if (fctImpl!=null) {
            userFunctions.put(fctImpl.getName(), new DecInfo<>(fctImpl));
            return fctImpl;
        }
        return null;
    }

    @Override
    public NExprFunction declareConstruct(NExprFunction constructImpl) {
        if (constructImpl!=null) {
            userConstructs.put(constructImpl.getName(), new DecInfo<>(constructImpl));
            return constructImpl;
        }
        return null;
    }

    @Override
    public NExprFunction declareConstruct(String name, NExprFunctionHandler constructImpl) {
        if (!NBlankable.isBlank(name)) {
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

    @Override
    public NExprOperator declareOperator(String name, NExprFunctionHandler impl) {
        return declareOperator(name,null,impl);
    }

    @Override
    public NExprOperator declareOperator(NExprOperator impl) {
        String name = impl.getName();
        NExprOpType type=ExprOpHelper.resolveOpDefaultType(name,impl.getType());
        int prec=ExprOpHelper.resolveOpPrecedence(name,type,-1);
        NExprOperator d2=new DefaultNExprOpDeclaration(name, new NExprOperatorHandler() {
            NExprOpType type;
            @Override
            public NOperatorAssociativity getAssociativity() {
                return null;
            }

            @Override
            public NExprOpType getType() {
                return type;
            }

            @Override
            public int getPrecedence() {
                return prec;
            }

            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                return impl.eval(args,  context);
            }
        });
        this.ops.put(new NExprOpNameAndType(name, type), new DecInfo<>(d2));
        return d2;
    }

    @Override
    public NExprOperator declareOperator(String name, NExprOpType type, NExprFunctionHandler impl) {
        type=ExprOpHelper.resolveOpDefaultType(name,type);
        int prec=ExprOpHelper.resolveOpPrecedence(name,type,-1);
        NOperatorAssociativity ass=ExprOpHelper.resolveOpDefaultAssociativity(name,type,null);
        return declareOperator(name,type, prec, ass, impl);
    }

    public NExprOperator declareOperator(String name, NExprOpType type, int precedence, NOperatorAssociativity associativity, NExprFunctionHandler impl) {
        if (!NBlankable.isBlank(name)) {
            NExprOpType typeOk=ExprOpHelper.resolveOpDefaultType(name,type);
            if (impl == null) {
                this.ops.put(new NExprOpNameAndType(name, type), REMOVED);
            } else {
                int prec=ExprOpHelper.resolveOpPrecedence(name,type,-1);
                NOperatorAssociativity ass=ExprOpHelper.resolveOpDefaultAssociativity(name,type,null);

                DefaultNExprOpDeclaration r = new DefaultNExprOpDeclaration(name, new NExprOperatorHandler() {
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
                this.ops.put(new NExprOpNameAndType(name, type), new DecInfo<>(r));
                return r;
            }
        }
        return null;
    }

    @Override
    public NOptional<NExprOperator> getOperator(String opName, NExprOpType type, NExprNodeValue... nodes) {
        DecInfo<NExprOperator> f = this.ops.get(new NExprOpNameAndType(opName, type));
        if (f != null) {
            if (f.value != null) {
                return NOptional.of(f.value);
            }
        } else if (parent != null) {
            return parent.getOperator(opName, type);
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("operator not found %s", opName));
    }

    @Override
    public void undeclareVar(NExprVar member) {
        if (member != null) {
            userVars.remove(member.getName());
        }
    }

    @Override
    public void removeVar(NExprVar member) {
        if (member != null) {
            userVars.put(member.getName(), REMOVED);
        }
    }

    @Override
    public void undeclareFunction(NExprFunction member) {
        if (member != null) {
            userFunctions.remove(member.getName());
        }
    }

    @Override
    public void removeFunction(NExprFunction member) {
        if (member != null) {
            userFunctions.put(member.getName(), REMOVED);
        }
    }

    @Override
    public void undeclareConstruct(NExprFunction member) {
        if (member != null) {
            userConstructs.remove(member.getName());
        }
    }

    @Override
    public void removeConstruct(NExprFunction member) {
        if (member != null) {
            userConstructs.put(member.getName(), REMOVED);
        }
    }

    @Override
    public void undeclareOperator(NExprOperator member) {
        if (member != null) {
            this.ops.remove(t(member));
        }
    }

    @Override
    public void removeOperator(NExprOperator member) {
        if (member != null) {
            this.ops.put(t(member), REMOVED);
        }
    }

    private NExprOpNameAndType t(NExprOperator member) {
        return new NExprOpNameAndType(member.getName(), member.getType());
    }


    public static class DecInfo<T> {
        public final T value;

        public DecInfo(T value) {
            this.value = value;
        }
    }

    @Override
    public List<NExprOperator> getOperators() {
        List<NExprOperator> all = new ArrayList<>();
        if (parent != null) {
            for (NExprOperator o : parent.getOperators()) {
                DecInfo<NExprOperator> y = this.ops.get(new NExprOpNameAndType(o.getName(), o.getType()));
                if (y == null) {
                    all.add(o);
                }
            }
        }
        for (NExprOpType t : NExprOpType.values()) {
            for (DecInfo<NExprOperator> value : this.ops.values()) {
                if (value.value != null) {
                    all.add(value.value);
                }
            }
        }
        return all;
    }


}
