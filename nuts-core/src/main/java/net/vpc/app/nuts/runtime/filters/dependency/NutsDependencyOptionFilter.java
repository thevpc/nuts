package net.vpc.app.nuts.runtime.filters.dependency;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

public class NutsDependencyOptionFilter implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {

    public static final NutsDependencyFilter OPTIONAL = new NutsDependencyOptionFilter(true);
    public static final NutsDependencyFilter NON_OPTIONAL = new NutsDependencyOptionFilter(false);

    public static final NutsDependencyFilter valueOf(boolean b) {
        return b ? OPTIONAL : NON_OPTIONAL;
    }

    private final Boolean optional;

    public NutsDependencyOptionFilter(Boolean optional) {
        this.optional = optional;
    }

    @Override
    public boolean accept(NutsId from, NutsDependency dependency, NutsSession session) {
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
