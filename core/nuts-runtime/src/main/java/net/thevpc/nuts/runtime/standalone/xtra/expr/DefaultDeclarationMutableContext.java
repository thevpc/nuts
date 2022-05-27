package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DefaultDeclarationMutableContext extends NutsExprDeclarationsBase implements NutsExprMutableDeclarations {
    private static DecInfo REMOVED = new DecInfo(null);

    private final Map<String, DecInfo<NutsExprFctDeclaration>> userFunctions = new LinkedHashMap<>();
    private final Map<String, DecInfo<NutsExprConstructDeclaration>> userConstructs = new LinkedHashMap<>();
    private final Map<NutsExprOpNameAndType, DecInfo<NutsExprOpDeclaration>> ops = new LinkedHashMap<>();
    private final Map<String, DecInfo<NutsExprVarDeclaration>> userVars = new LinkedHashMap<>();


    private NutsExprDeclarations parent;

    public DefaultDeclarationMutableContext(NutsExprDeclarations parent) {
        this.parent = parent;
        setSession(parent.getSession());
    }

    @Override
    public NutsOptional<NutsExprVarDeclaration> getVar(String name) {
        DecInfo<NutsExprVarDeclaration> f = userVars.get(name);
        if (f != null) {
            if (f.value != null) {
                return NutsOptional.of(f.value);
            }
        } else if (parent != null) {
            return parent.getVar(name);
        }
        return NutsOptional.ofEmpty(s -> NutsMessage.ofCstyle("var not found %s", name));
    }

    @Override
    public NutsOptional<NutsExprFctDeclaration> getFunction(String name, Object... args) {
        DecInfo<NutsExprFctDeclaration> f = userFunctions.get(name);
        if (f != null) {
            if (f.value != null) {
                return NutsOptional.of(f.value);
            }
        } else if (parent != null) {
            return parent.getFunction(name, args);
        }
        return NutsOptional.ofEmpty(s -> NutsMessage.ofCstyle("function not found %s", name));
    }

    @Override
    public NutsOptional<NutsExprConstructDeclaration> getConstruct(String name, NutsExprNode... args) {
        DecInfo<NutsExprConstructDeclaration> f = userConstructs.get(name);
        if (f != null) {
            if (f.value != null) {
                return NutsOptional.of(f.value);
            }
        } else if (parent != null) {
            return parent.getConstruct(name, args);
        }
        return NutsOptional.ofEmpty(s -> NutsMessage.ofCstyle("construct not found %s", name));
    }

    @Override
    public NutsExprVarDeclaration declareVar(String name, NutsExprVar varImpl) {
        if (!NutsBlankable.isBlank(name)) {
            if (varImpl == null) {
                userFunctions.put(name, REMOVED);
            } else {
                DefaultNutsExprVarDeclaration r = new DefaultNutsExprVarDeclaration(name, varImpl);
                userVars.put(name, new DecInfo<>(r));
                return r;
            }
        }
        return null;
    }

    @Override
    public NutsExprVarDeclaration declareVar(String name) {
        return declareVar(name, new DefaultNutsExprVarImpl());
    }

    @Override
    public NutsExprVarDeclaration declareConstant(String name, Object value) {
        return declareVar(name, new DefaultNutsExprConstImpl(value));
    }

    @Override
    public NutsExprFctDeclaration declareFunction(String name, NutsExprFct fctImpl) {
        if (!NutsBlankable.isBlank(name)) {
            if (fctImpl == null) {
                userFunctions.put(name, REMOVED);
            } else {
                DefaultNutsExprFctDeclaration r = new DefaultNutsExprFctDeclaration(name, fctImpl);
                userFunctions.put(name, new DecInfo<>(r));
                return r;
            }
        }
        return null;
    }

    @Override
    public NutsExprConstructDeclaration declareConstruct(String name, NutsExprConstruct constructImpl) {
        if (!NutsBlankable.isBlank(name)) {
            if (constructImpl == null) {
                userConstructs.put(name, REMOVED);
            } else {
                NutsExprConstructDeclaration r = new DefaultNutsExprConstructDeclaration(name, constructImpl);
                userConstructs.put(name, new DecInfo<>(r));
                return r;
            }
        }
        return null;
    }

//    @Override
//    public List<String> getFunctionNames() {
//        LinkedHashSet<String> all = new LinkedHashSet<>();
//        for (Map.Entry<String, NutsExprFct> e : userFunctions.entrySet()) {
//            all.add(e.getKey());
//        }
//        if (parent != null) {
//            for (String f : parent.getFunctionNames()) {
//                Boolean s = userFunctionsFlag.get(f);
//                if (s == null || s.booleanValue()) {
//                    all.add(f);
//                }
//            }
//        } else {
//            for (String f : DefaultNutsExpr.defaultFunctions.keySet()) {
//                Boolean s = userFunctionsFlag.get(f);
//                if (s == null || s.booleanValue()) {
//                    all.add(f);
//                }
//            }
//        }
//        return new ArrayList<>(all);
//    }


//    @Override
//    public List<String> getOperatorNames(NutsExprOpType type) {
//        if (type == null) {
//            LinkedHashSet<String> all = new LinkedHashSet<>();
//            for (NutsExprOpType value : NutsExprOpType.values()) {
//                all.addAll((getOperatorNames(value)));
//            }
//            return new ArrayList<>(all);
//        }
//        LinkedHashSet<String> all = new LinkedHashSet<>();
//        Map<String, NutsExprOp> ops = getOps(type);
//
//        Map<String, Boolean> opsFlag = getOpsFlag(type);
//
//        for (Map.Entry<String, NutsExprOp> e : ops.entrySet()) {
//            all.add(e.getKey());
//        }
//        if (parent != null) {
//            for (String f : parent.getFunctionNames()) {
//                Boolean s = opsFlag.get(f);
//                if (s == null || s.booleanValue()) {
//                    all.add(f);
//                }
//            }
//        } else {
//            for (String f :
//                    getStaticOps(type)
//                            .keySet()) {
//                Boolean s = opsFlag.get(f);
//                if (s == null || s.booleanValue()) {
//                    all.add(f);
//                }
//            }
//        }
//        return new ArrayList<>(all);
//    }


    public NutsExprOpDeclaration declareOperator(String name, NutsExprOpType type, int precedence, NutsExprOpAssociativity associativity, NutsExprConstruct impl) {
        if (!NutsBlankable.isBlank(name) && type != null) {
            if (impl == null) {
                this.ops.put(new NutsExprOpNameAndType(name, type), REMOVED);
            } else {
                DefaultNutsExprOpDeclaration r = new DefaultNutsExprOpDeclaration(name, new NutsExprOp() {
                    @Override
                    public NutsExprOpAssociativity getAssociativity() {
                        return associativity;
                    }

                    @Override
                    public NutsExprOpType getType() {
                        return type;
                    }

                    @Override
                    public int getPrecedence() {
                        return precedence;
                    }

                    @Override
                    public Object eval(String name, List<NutsExprNode> args, NutsExprDeclarations context) {
                        return impl.eval(name, args, context);
                    }
                });
                this.ops.put(new NutsExprOpNameAndType(name, type), new DecInfo<>(r));
                return r;
            }
        }
        return null;
    }

    @Override
    public NutsOptional<NutsExprOpDeclaration> getOperator(String opName, NutsExprOpType type, NutsExprNode... nodes) {
        DecInfo<NutsExprOpDeclaration> f = this.ops.get(new NutsExprOpNameAndType(opName, type));
        if (f != null) {
            if (f.value != null) {
                return NutsOptional.of(f.value);
            }
        } else if (parent != null) {
            return parent.getOperator(opName, type);
        }
        return NutsOptional.ofEmpty(s -> NutsMessage.ofCstyle("operator not found %s", opName));
    }

    @Override
    public void resetDeclaration(NutsExprVarDeclaration member) {
        if (member != null) {
            userVars.remove(member.getName());
        }
    }

    @Override
    public void removeDeclaration(NutsExprVarDeclaration member) {
        if (member != null) {
            userVars.put(member.getName(), REMOVED);
        }
    }

    @Override
    public void resetDeclaration(NutsExprFctDeclaration member) {
        if (member != null) {
            userFunctions.remove(member.getName());
        }
    }

    @Override
    public void removeDeclaration(NutsExprFctDeclaration member) {
        if (member != null) {
            userFunctions.put(member.getName(), REMOVED);
        }
    }

    @Override
    public void resetDeclaration(NutsExprConstructDeclaration member) {
        if (member != null) {
            userConstructs.remove(member.getName());
        }
    }

    @Override
    public void removeDeclaration(NutsExprConstructDeclaration member) {
        if (member != null) {
            userConstructs.put(member.getName(), REMOVED);
        }
    }

    @Override
    public void resetDeclaration(NutsExprOpDeclaration member) {
        if (member != null) {
            this.ops.remove(t(member));
        }
    }

    @Override
    public void removeDeclaration(NutsExprOpDeclaration member) {
        if (member != null) {
            this.ops.put(t(member), REMOVED);
        }
    }

    private NutsExprOpNameAndType t(NutsExprOpDeclaration member) {
        return new NutsExprOpNameAndType(member.getName(), member.getType());
    }


    private static class DecInfo<T> {
        final T value;

        public DecInfo(T value) {
            this.value = value;
        }
    }

    @Override
    public List<NutsExprOpDeclaration> getOperators() {
        List<NutsExprOpDeclaration> all = new ArrayList<>();
        if (parent != null) {
            for (NutsExprOpDeclaration o : parent.getOperators()) {
                DecInfo<NutsExprOpDeclaration> y = this.ops.get(new NutsExprOpNameAndType(o.getName(), o.getType()));
                if (y == null) {
                    all.add(o);
                }
            }
        }
        for (NutsExprOpType t : NutsExprOpType.values()) {
            for (DecInfo<NutsExprOpDeclaration> value : this.ops.values()) {
                if (value.value != null) {
                    all.add(value.value);
                }
            }
        }
        return all;
    }

    private static class DefaultNutsExprConstImpl implements NutsExprVar {
        private Object value;

        public DefaultNutsExprConstImpl(Object value) {
            this.value = value;
        }

        @Override
        public Object get(String name, NutsExprDeclarations context) {
            return value;
        }

        @Override
        public Object set(String name, Object value, NutsExprDeclarations context) {
            return this.value;
        }
    }

    private static class DefaultNutsExprVarImpl implements NutsExprVar {
        private Object value;

        @Override
        public Object get(String name, NutsExprDeclarations context) {
            return value;
        }

        @Override
        public Object set(String name, Object value, NutsExprDeclarations context) {
            Object old = this.value;
            this.value = value;
            return old;
        }
    }

    public int[] getOperatorPrecedences() {
        return Stream.concat(
                ops.values().stream().filter(x -> x.value != null).map(x -> x.value.getPrecedence()),
                IntStream.of(parent.getOperatorPrecedences()).boxed()
        ).sorted().distinct().mapToInt(x -> x).toArray();
    }
}
