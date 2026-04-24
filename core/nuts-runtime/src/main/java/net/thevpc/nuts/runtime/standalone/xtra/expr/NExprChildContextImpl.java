package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.internal.expr.NExprRPI;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.expr.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NExprChildContextImpl extends NExprContextBase {
    private NExprResolver resolver;
    private NExprContext parent;
    private Map<String,NExprVar> varToDeclaration=new ConcurrentHashMap<>();

    public NExprChildContextImpl(NExprRPI nExprRPI, NExprResolver resolver, NExprContext parent) {
        super(nExprRPI);
        this.resolver = resolver;
        this.parent = parent;
    }


    @Override
    public NOptional<NExprFctDeclaration> getFunction(String fctName, NExprNodeValue... args) {
        return resolver.getFunction(fctName, args, this)
                .<NExprFctDeclaration>map(x -> new DefaultNExprFctDeclaration(fctName, x))
                .orElseGetOptionalFrom(() -> parent.getFunction(fctName, args))
                ;
    }

    @Override
    public NOptional<NExprConstructDeclaration> getConstruct(String constructName, NExprNodeValue... args) {
        return resolver.getConstruct(constructName, args, this)
                .<NExprConstructDeclaration>map(x -> new DefaultNExprConstructDeclaration(constructName, x))
                .orElseGetOptionalFrom(() -> parent.getConstruct(constructName, args))
                ;
    }

    @Override
    public NOptional<NExprOpDeclaration> getOperator(String opName, NExprOpType type, NExprNodeValue... args) {
        return resolver.getOperator(opName, type, args, this)
                .<NExprOpDeclaration>map(x -> new DefaultNExprOpDeclaration(opName, x))
                .orElseGetOptionalFrom(() -> parent.getOperator(opName, type, args))
                ;
    }

    @Override
    public NOptional<NExprVar> getVar(String varName) {
        NExprVar d = varToDeclaration.get(varName);
        if(d!=null){
            return NOptional.of(d);
        }
        NOptional<NExprVar> vv = resolver.getVar(varName, this);
        if(vv.isPresent()){
            NExprVar dec = vv.get();
            varToDeclaration.put(varName,dec);
            return NOptional.of(dec);
        }
        return parent.getVar(varName);
    }

    @Override
    public List<NExprOpDeclaration> getOperators() {
        return parent.getOperators();
    }
}
