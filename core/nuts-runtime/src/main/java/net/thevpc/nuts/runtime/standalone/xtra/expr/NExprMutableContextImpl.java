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

    private final Map<String, DecInfo<NExprFctDeclaration>> userFunctions = new LinkedHashMap<>();
    private final Map<String, DecInfo<NExprConstructDeclaration>> userConstructs = new LinkedHashMap<>();
    private final Map<NExprOpNameAndType, DecInfo<NExprOpDeclaration>> ops = new LinkedHashMap<>();
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
    public NOptional<NExprFctDeclaration> getFunction(String name, NExprNodeValue... args) {
        DecInfo<NExprFctDeclaration> f = userFunctions.get(name);
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
    public NOptional<NExprConstructDeclaration> getConstruct(String name, NExprNodeValue... args) {
        DecInfo<NExprConstructDeclaration> f = userConstructs.get(name);
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
    public NExprFctDeclaration declareFunction(String name, NExprFct fctImpl) {
        if (!NBlankable.isBlank(name)) {
            if (fctImpl == null) {
                userFunctions.put(name, REMOVED);
            } else {
                DefaultNExprFctDeclaration r = new DefaultNExprFctDeclaration(name, fctImpl);
                userFunctions.put(name, new DecInfo<>(r));
                return r;
            }
        }
        return null;
    }

    @Override
    public NExprConstructDeclaration declareConstruct(String name, NExprConstruct constructImpl) {
        if (!NBlankable.isBlank(name)) {
            if (constructImpl == null) {
                userConstructs.put(name, REMOVED);
            } else {
                NExprConstructDeclaration r = new DefaultNExprConstructDeclaration(name, constructImpl);
                userConstructs.put(name, new DecInfo<>(r));
                return r;
            }
        }
        return null;
    }

    @Override
    public NExprOpDeclaration declareOperator(String name, NExprConstruct impl) {
        return declareOperator(name,null,impl);
    }

    @Override
    public NExprOpDeclaration declareOperator(String name, NExprOpType type, NExprConstruct impl) {
        type=ExprOpHelper.resolveOpDefaultType(name,type);
        int prec=ExprOpHelper.resolveOpPrecedence(name,type,-1);
        NOperatorAssociativity ass=ExprOpHelper.resolveOpDefaultAssociativity(name,type,null);
        return declareOperator(name,type, prec, ass, impl);
    }

    public NExprOpDeclaration declareOperator(String name, NExprOpType type, int precedence, NOperatorAssociativity associativity, NExprConstruct impl) {
        if (!NBlankable.isBlank(name)) {
            NExprOpType typeOk=ExprOpHelper.resolveOpDefaultType(name,type);
            if (impl == null) {
                this.ops.put(new NExprOpNameAndType(name, type), REMOVED);
            } else {
                int prec=ExprOpHelper.resolveOpPrecedence(name,type,-1);
                NOperatorAssociativity ass=ExprOpHelper.resolveOpDefaultAssociativity(name,type,null);

                DefaultNExprOpDeclaration r = new DefaultNExprOpDeclaration(name, new NExprOp() {
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
    public NOptional<NExprOpDeclaration> getOperator(String opName, NExprOpType type, NExprNodeValue... nodes) {
        DecInfo<NExprOpDeclaration> f = this.ops.get(new NExprOpNameAndType(opName, type));
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
    public void undeclare(NExprVar member) {
        if (member != null) {
            userVars.remove(member.getName());
        }
    }

    @Override
    public void remove(NExprVar member) {
        if (member != null) {
            userVars.put(member.getName(), REMOVED);
        }
    }

    @Override
    public void undeclare(NExprFctDeclaration member) {
        if (member != null) {
            userFunctions.remove(member.getName());
        }
    }

    @Override
    public void remove(NExprFctDeclaration member) {
        if (member != null) {
            userFunctions.put(member.getName(), REMOVED);
        }
    }

    @Override
    public void undeclare(NExprConstructDeclaration member) {
        if (member != null) {
            userConstructs.remove(member.getName());
        }
    }

    @Override
    public void remove(NExprConstructDeclaration member) {
        if (member != null) {
            userConstructs.put(member.getName(), REMOVED);
        }
    }

    @Override
    public void undeclare(NExprOpDeclaration member) {
        if (member != null) {
            this.ops.remove(t(member));
        }
    }

    @Override
    public void remove(NExprOpDeclaration member) {
        if (member != null) {
            this.ops.put(t(member), REMOVED);
        }
    }

    private NExprOpNameAndType t(NExprOpDeclaration member) {
        return new NExprOpNameAndType(member.getName(), member.getType());
    }


    public static class DecInfo<T> {
        public final T value;

        public DecInfo(T value) {
            this.value = value;
        }
    }

    @Override
    public List<NExprOpDeclaration> getOperators() {
        List<NExprOpDeclaration> all = new ArrayList<>();
        if (parent != null) {
            for (NExprOpDeclaration o : parent.getOperators()) {
                DecInfo<NExprOpDeclaration> y = this.ops.get(new NExprOpNameAndType(o.getName(), o.getType()));
                if (y == null) {
                    all.add(o);
                }
            }
        }
        for (NExprOpType t : NExprOpType.values()) {
            for (DecInfo<NExprOpDeclaration> value : this.ops.values()) {
                if (value.value != null) {
                    all.add(value.value);
                }
            }
        }
        return all;
    }


}
