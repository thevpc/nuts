package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NOperatorAssociativity;
import net.thevpc.nuts.internal.expr.NExprRPI;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.expr.*;

import java.util.*;
import java.util.function.Supplier;

public class NExprMutableContextImpl extends NExprContextBase implements NExprMutableContext {

    private NExprContext parent;
    private NExprContextAlteration alteration=new NExprContextAlteration();

    public NExprMutableContextImpl(NExprRPI nExprRPI, NExprContext parent) {
        super(nExprRPI);
        this.parent = parent;
    }

    public NExprMutableContextImpl(NExprRPI nExprRPI, NExprContextAlteration alteration,NExprContext parent) {
        super(nExprRPI);
        this.parent = parent;
        this.alteration.addAll(alteration);
    }


    public NExprMutableContext setVarValue(String varName, Object value) {
        getVar(varName).get().set(value, this);
        return this;
    }

    @Override
    public NExprVar declareVar(NExprVar varImpl) {
        alteration.declareVar(varImpl);
        return varImpl;
    }

    @Override
    public NExprVar declareVar(String name) {
        return alteration.declareVar(name);
    }

    @Override
    public NExprVar declareConstant(String name, Object value) {
        return declareVar(NExprVar.ofConst(name,value));
    }

    @Override
    public NExprFunction declareFunction(String name, NExprCallHandler fctImpl) {
        return alteration.declareFunction(name,fctImpl);
    }

    @Override
    public NExprFunction declareFunction(NExprFunction fctImpl) {
        return alteration.declareFunction(fctImpl);
    }

    @Override
    public NExprFunction declareConstruct(NExprFunction constructImpl) {
        return alteration.declareConstruct(constructImpl);
    }

    @Override
    public NExprFunction declareConstruct(String name, NExprCallHandler constructImpl) {
        return alteration.declareConstruct(name,constructImpl);
    }

    @Override
    public NExprOperator declareOperator(String name, NExprCallHandler impl) {
        return alteration.declareOperator(name,impl);
    }

    @Override
    public NExprOperator declareOperator(NExprOperator impl) {
        return alteration.declareOperator(impl);
    }

    @Override
    public NExprOperator declareOperator(String name, NExprOpType type, NExprCallHandler impl) {
        return alteration.declareOperator(name, type,impl);
    }

    public NExprOperator declareOperator(String name, NExprOpType type, int precedence, NOperatorAssociativity associativity, NExprCallHandler impl) {
        return alteration.declareOperator(name, type,precedence,associativity,impl);
    }



    @Override
    public NExprMutableContext undeclareVar(NExprVar member) {
        alteration.undeclareVar(member);
        return this;
    }

    @Override
    public NExprMutableContext removeVar(NExprVar member) {
        alteration.removeVar(member);
        return this;
    }

    @Override
    public NExprMutableContext removeVar(String name) {
        alteration.removeVar(name);
        return this;
    }

    @Override
    public NExprMutableContext removeFunction(String name) {
        alteration.removeFunction(name);
        return this;
    }

    @Override
    public NExprMutableContext removeConstruct(String name) {
        alteration.removeConstruct(name);
        return this;
    }

    @Override
    public NExprMutableContext removeOperator(String name, NExprOpType type) {
        alteration.removeOperator(name,type);
        return this;
    }

    @Override
    public NExprMutableContext undeclareFunction(NExprFunction member) {
        alteration.undeclareFunction(member);
        return this;
    }

    @Override
    public NExprMutableContext removeFunction(NExprFunction member) {
        alteration.removeFunction(member);
        return this;
    }

    @Override
    public NExprMutableContext undeclareConstruct(NExprFunction member) {
        alteration.undeclareConstruct(member);
        return this;
    }

    @Override
    public NExprMutableContext removeConstruct(NExprFunction member) {
       alteration.removeConstruct(member);
       return this;
    }

    @Override
    public NExprMutableContext undeclareOperator(NExprOperator member) {
        alteration.undeclareOperator(member);
        return this;
    }

    @Override
    public NExprMutableContext removeOperator(NExprOperator member) {
        alteration.removeOperator(member);
        return this;
    }


    ///////////////////////////////////////////////////
    @Override
    public NOptional<NExprOperator> getOperator(String opName, NExprOpType type, NExprNodeValue... nodes) {
        return alteration.getOperator(this,parent,opName, type,nodes);
    }

    @Override
    public List<NExprOperator> getOperators() {
        List<NExprOperator> all = new ArrayList<>();
        if (parent != null) {
            for (NExprOperator o : parent.getOperators()) {
                if(alteration.userOperators!=null) {
                    NExprContextAlteration.DecInfo<NExprOperator> y = alteration.userOperators.get(new NExprOpNameAndType(o.name(), o.operatorType()));
                    if (y == null) {
                        all.add(o);
                    }
                }
            }
        }
        if(alteration.userOperators!=null) {
            for (NExprContextAlteration.DecInfo<NExprOperator> value : alteration.userOperators.values()) {
                if (value.value != null) {
                    all.add(value.value);
                }
            }
        }
        return all;
    }
    @Override
    public NOptional<NExprVar> getVar(String name) {
        return alteration.getVar(this,parent,name);
    }

    @Override
    public NExprVar getOrDeclareVar(String name, Supplier<Object> value) {
        return alteration.getOrDeclareVar(this,parent,name,value);
    }

    @Override
    public NOptional<NExprFunction> getFunction(String name, NExprNodeValue... args) {
        return alteration.getFunction(this,parent,name,args);
    }

    @Override
    public NOptional<NExprFunction> getConstruct(String name, NExprNodeValue... args) {
        return alteration.getConstruct(this,parent,name,args);
    }

}
