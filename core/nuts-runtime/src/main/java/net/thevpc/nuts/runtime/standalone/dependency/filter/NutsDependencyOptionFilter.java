package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;

public class NutsDependencyOptionFilter extends AbstractDependencyFilter{

    private final Boolean optional;

    public NutsDependencyOptionFilter(NutsSession session, Boolean optional) {
        super(session, NutsFilterOp.CUSTOM);
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
