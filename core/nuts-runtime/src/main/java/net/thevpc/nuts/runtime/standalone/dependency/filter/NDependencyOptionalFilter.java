package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.util.NFilterOp;

public class NDependencyOptionalFilter extends AbstractDependencyFilter{

    private final Boolean optional;

    public NDependencyOptionalFilter(Boolean optional) {
        super(NFilterOp.CUSTOM);
        this.optional = optional;
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
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
    public NDependencyFilter simplify() {
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
