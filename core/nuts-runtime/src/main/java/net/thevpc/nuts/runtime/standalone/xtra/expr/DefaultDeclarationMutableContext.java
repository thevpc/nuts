package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DefaultDeclarationMutableContext extends NExprDeclarationsBase implements NExprMutableDeclarations {
    private static DecInfo REMOVED = new DecInfo(null);

    private final Map<String, DecInfo<NExprFctDeclaration>> userFunctions = new LinkedHashMap<>();
    private final Map<String, DecInfo<NExprConstructDeclaration>> userConstructs = new LinkedHashMap<>();
    private final Map<NExprOpNameAndType, DecInfo<NExprOpDeclaration>> ops = new LinkedHashMap<>();
    private final Map<String, DecInfo<NExprVarDeclaration>> userVars = new LinkedHashMap<>();


    private NExprDeclarations parent;

    public DefaultDeclarationMutableContext(NExprDeclarations parent) {
        this.parent = parent;
        setSession(parent.getSession());
    }

    @Override
    public NOptional<NExprVarDeclaration> getVar(String name) {
        DecInfo<NExprVarDeclaration> f = userVars.get(name);
        if (f != null) {
            if (f.value != null) {
                return NOptional.of(f.value);
            }
        } else if (parent != null) {
            return parent.getVar(name);
        }
        return NOptional.ofEmpty(s -> NMsg.ofC("var not found %s", name));
    }

    @Override
    public NOptional<NExprFctDeclaration> getFunction(String name, Object... args) {
        DecInfo<NExprFctDeclaration> f = userFunctions.get(name);
        if (f != null) {
            if (f.value != null) {
                return NOptional.of(f.value);
            }
        } else if (parent != null) {
            return parent.getFunction(name, args);
        }
        return NOptional.ofEmpty(s -> NMsg.ofC("function not found %s", name));
    }

    @Override
    public NOptional<NExprConstructDeclaration> getConstruct(String name, NExprNode... args) {
        DecInfo<NExprConstructDeclaration> f = userConstructs.get(name);
        if (f != null) {
            if (f.value != null) {
                return NOptional.of(f.value);
            }
        } else if (parent != null) {
            return parent.getConstruct(name, args);
        }
        return NOptional.ofEmpty(s -> NMsg.ofC("construct not found %s", name));
    }

    @Override
    public NExprVarDeclaration declareVar(String name, NExprVar varImpl) {
        if (!NBlankable.isBlank(name)) {
            if (varImpl == null) {
                userFunctions.put(name, REMOVED);
            } else {
                DefaultNExprVarDeclaration r = new DefaultNExprVarDeclaration(name, varImpl);
                userVars.put(name, new DecInfo<>(r));
                return r;
            }
        }
        return null;
    }

    @Override
    public NExprVarDeclaration declareVar(String name) {
        return declareVar(name, new DefaultNExprVarImpl());
    }

    @Override
    public NExprVarDeclaration declareConstant(String name, Object value) {
        return declareVar(name, new DefaultNExprConstImpl(value));
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
        switch (name){
            case "+":{
                if(type==null){
                    type= NExprOpType.INFIX;
                }
                switch (type){
                    case INFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.PLUS, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case "-":{
                if(type==null){
                    type= NExprOpType.INFIX;
                }
                switch (type){
                    case INFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.MINUS, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case "*":{
                if(type==null){
                    type= NExprOpType.INFIX;
                }
                switch (type){
                    case INFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.MUL, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case "/":{
                if(type==null){
                    type= NExprOpType.INFIX;
                }
                switch (type){
                    case INFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.DIV, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case "%":{
                if(type==null){
                    type= NExprOpType.INFIX;
                }
                switch (type){
                    case INFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.MOD, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case "(":
            case "()":
            {
                if(type==null){
                    type= NExprOpType.POSTFIX;
                }
                switch (type){
                    case POSTFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.PARS, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case "[":
            case "[]":
            {
                if(type==null){
                    type= NExprOpType.POSTFIX;
                }
                switch (type){
                    case POSTFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.BRACKETS, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case "{":
            case "{}":
            {
                if(type==null){
                    type= NExprOpType.POSTFIX;
                }
                switch (type){
                    case POSTFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.BRACES, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case ".":
            case "?.":
            {
                if(type==null){
                    type= NExprOpType.INFIX;
                }
                switch (type){
                    case INFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.DOT, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case "<<":
            case ">>":
            case ">>>":
            case "<<<":
            {
                if(type==null){
                    type= NExprOpType.INFIX;
                }
                switch (type){
                    case INFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.SHIFT, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case "<":
            case ">":
            case "<=":
            case ">=":
            {
                if(type==null){
                    type= NExprOpType.INFIX;
                }
                switch (type){
                    case INFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.CMP, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case "=":
            case "==":
            case "!=":
            case "<>":
            {
                if(type==null){
                    type= NExprOpType.INFIX;
                }
                switch (type){
                    case INFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.EQ, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case "&&":
            {
                if(type==null){
                    type= NExprOpType.INFIX;
                }
                switch (type){
                    case INFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.AND, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case "&":
            {
                if(type==null){
                    type= NExprOpType.INFIX;
                }
                switch (type){
                    case INFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.AMP, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case "||":
            {
                if(type==null){
                    type= NExprOpType.INFIX;
                }
                switch (type){
                    case INFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.OR, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case "|":
            {
                if(type==null){
                    type= NExprOpType.INFIX;
                }
                switch (type){
                    case INFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.PIPE, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
            case "??":
            {
                if(type==null){
                    type= NExprOpType.INFIX;
                }
                switch (type){
                    case INFIX:{
                        return declareOperator(name,type, NExprOpPrecedence.COALESCE, NExprOpAssociativity.LEFT, impl);
                    }
                }
                break;
            }
        }
        throw new IllegalArgumentException("unsupported operator "+name);
    }

    public NExprOpDeclaration declareOperator(String name, NExprOpType type, int precedence, NExprOpAssociativity associativity, NExprConstruct impl) {
        if (!NBlankable.isBlank(name) && type != null) {
            if (impl == null) {
                this.ops.put(new NExprOpNameAndType(name, type), REMOVED);
            } else {
                DefaultNExprOpDeclaration r = new DefaultNExprOpDeclaration(name, new NExprOp() {
                    @Override
                    public NExprOpAssociativity getAssociativity() {
                        return associativity;
                    }

                    @Override
                    public NExprOpType getType() {
                        return type;
                    }

                    @Override
                    public int getPrecedence() {
                        return precedence;
                    }

                    @Override
                    public Object eval(String name, List<NExprNode> args, NExprDeclarations context) {
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
    public NOptional<NExprOpDeclaration> getOperator(String opName, NExprOpType type, NExprNode... nodes) {
        DecInfo<NExprOpDeclaration> f = this.ops.get(new NExprOpNameAndType(opName, type));
        if (f != null) {
            if (f.value != null) {
                return NOptional.of(f.value);
            }
        } else if (parent != null) {
            return parent.getOperator(opName, type);
        }
        return NOptional.ofEmpty(s -> NMsg.ofC("operator not found %s", opName));
    }

    @Override
    public void resetDeclaration(NExprVarDeclaration member) {
        if (member != null) {
            userVars.remove(member.getName());
        }
    }

    @Override
    public void removeDeclaration(NExprVarDeclaration member) {
        if (member != null) {
            userVars.put(member.getName(), REMOVED);
        }
    }

    @Override
    public void resetDeclaration(NExprFctDeclaration member) {
        if (member != null) {
            userFunctions.remove(member.getName());
        }
    }

    @Override
    public void removeDeclaration(NExprFctDeclaration member) {
        if (member != null) {
            userFunctions.put(member.getName(), REMOVED);
        }
    }

    @Override
    public void resetDeclaration(NExprConstructDeclaration member) {
        if (member != null) {
            userConstructs.remove(member.getName());
        }
    }

    @Override
    public void removeDeclaration(NExprConstructDeclaration member) {
        if (member != null) {
            userConstructs.put(member.getName(), REMOVED);
        }
    }

    @Override
    public void resetDeclaration(NExprOpDeclaration member) {
        if (member != null) {
            this.ops.remove(t(member));
        }
    }

    @Override
    public void removeDeclaration(NExprOpDeclaration member) {
        if (member != null) {
            this.ops.put(t(member), REMOVED);
        }
    }

    private NExprOpNameAndType t(NExprOpDeclaration member) {
        return new NExprOpNameAndType(member.getName(), member.getType());
    }


    private static class DecInfo<T> {
        final T value;

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

    private static class DefaultNExprConstImpl implements NExprVar {
        private Object value;

        public DefaultNExprConstImpl(Object value) {
            this.value = value;
        }

        @Override
        public Object get(String name, NExprDeclarations context) {
            return value;
        }

        @Override
        public Object set(String name, Object value, NExprDeclarations context) {
            return this.value;
        }
    }

    private static class DefaultNExprVarImpl implements NExprVar {
        private Object value;

        @Override
        public Object get(String name, NExprDeclarations context) {
            return value;
        }

        @Override
        public Object set(String name, Object value, NExprDeclarations context) {
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
