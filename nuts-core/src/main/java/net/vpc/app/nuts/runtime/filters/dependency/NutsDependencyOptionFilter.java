package net.vpc.app.nuts.runtime.filters.dependency;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.filters.AbstractNutsFilter;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

public class NutsDependencyOptionFilter extends AbstractNutsFilter implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {

    private final Boolean optional;

    public NutsDependencyOptionFilter(NutsWorkspace ws,Boolean optional) {
        super(ws,NutsFilterOp.CUSTOM);
        this.optional = optional;
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        if (optional == null) {
            return false;
        }
//        String o = dependency.getOptional();
//        if (o == null) {
//            o = "";
//        }
//        o = o.trim().toLowerCase();
//        return optional == (o.isEmpty() || o.equals("true"));
        return optional == dependency.isOptional();
    }

    @Override
    public NutsDependencyFilter simplify() {
        if (optional == null) {
            return null;
        }
        return this;
    }

    @Override
    public String toString() {
        if(optional==null){
            return "any optional";
        }else{
            return optional?"optional":"not(optional)";
        }
    }
    
}
