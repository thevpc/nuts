package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.expr.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class NExprEvaluatorAsContext extends NExprDeclarationsBase {
    private NExprEvaluator eval;
    private NExprDeclarations parent;
    private Map<String,DefaultNExprVarDeclaration> varToDeclaration=new ConcurrentHashMap<>();

    public NExprEvaluatorAsContext(NExprs exprs, NExprEvaluator eval, NExprDeclarations parent) {
        super(exprs);
        this.eval = eval;
        this.parent = parent;
    }


    @Override
    public NOptional<NExprFctDeclaration> getFunction(String fctName, NExprNodeValue... args) {
        return eval.getFunction(fctName, args, this)
                .<NExprFctDeclaration>map(x -> new DefaultNExprFctDeclaration(fctName, x))
                .orElseGetOptionalFrom(() -> parent.getFunction(fctName, args))
                ;
    }

    @Override
    public NOptional<NExprConstructDeclaration> getConstruct(String constructName, NExprNodeValue... args) {
        return eval.getConstruct(constructName, args, this)
                .<NExprConstructDeclaration>map(x -> new DefaultNExprConstructDeclaration(constructName, x))
                .orElseGetOptionalFrom(() -> parent.getConstruct(constructName, args))
                ;
    }

    @Override
    public NOptional<NExprOpDeclaration> getOperator(String opName, NExprOpType type, NExprNodeValue... args) {
        return eval.getOperator(opName, type, args, this)
                .<NExprOpDeclaration>map(x -> new DefaultNExprOpDeclaration(opName, x))
                .orElseGetOptionalFrom(() -> parent.getOperator(opName, type, args))
                ;
    }

    @Override
    public NOptional<NExprVarDeclaration> getVar(String varName) {
        DefaultNExprVarDeclaration d = varToDeclaration.get(varName);
        if(d!=null){
            return NOptional.of(d);
        }
        NOptional<NExprVar> vv = eval.getVar(varName, this);
        if(vv.isPresent()){
            DefaultNExprVarDeclaration dec = new DefaultNExprVarDeclaration(varName, vv.get());
            varToDeclaration.put(varName,dec);
            return NOptional.of(dec);
        }
        return parent.getVar(varName);
    }

    @Override
    public NExprVar getOrDeclareVar(String name, Supplier<Object> initialValue) {
        DefaultNExprVarDeclaration d = varToDeclaration.get(name);
        if(d!=null){
            return d.asVar();
        }
        NExprDeclarations c=this;
        if(c!=null){
            NOptional<NExprVarDeclaration> dd = c.getVar(name);
            if(dd.isPresent()){
               return dd.get().asVar();
            }
        }
        c=parent;
        if(c!=null){
            NOptional<NExprVarDeclaration> dd = c.getVar(name);
            if(dd.isPresent()){
               return dd.get().asVar();
            }
        }
        DefaultNExprVarDeclaration newDecl = new DefaultNExprVarDeclaration(name, new ReservedNExprVar(name, initialValue == null ? null : initialValue.get()));
        varToDeclaration.put(name, newDecl);
        return newDecl.asVar();
    }

    @Override
    public List<NExprOpDeclaration> getOperators() {
        return parent.getOperators();
    }
}
