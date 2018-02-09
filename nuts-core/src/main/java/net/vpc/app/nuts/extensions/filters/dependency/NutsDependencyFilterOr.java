package net.vpc.app.nuts.extensions.filters.dependency;

import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDependencyFilter;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.Simplifiable;

public class NutsDependencyFilterOr implements NutsDependencyFilter,Simplifiable<NutsDependencyFilter>{
    private final NutsDependencyFilter[] all;

    public NutsDependencyFilterOr(NutsDependencyFilter... all) {
        this.all = all;
    }

    @Override
    public boolean accept(NutsDependency value) {
        boolean one=false;
        for (NutsDependencyFilter nutsDependencyFilter : all) {
            if(nutsDependencyFilter!=null){
                one=true;
                if(nutsDependencyFilter.accept(value)) {
                    return true;
                }
            }
        }
        return one?false:true;
    }

    public NutsDependencyFilter simplify(){
        NutsDependencyFilter[] newValues= CoreNutsUtils.simplifyAndShrink(NutsDependencyFilter.class,all);
        if(newValues!=null){
            if(newValues.length==0){
                return null;
            }
            if(newValues.length==1){
                return newValues[0];
            }
            return new NutsDependencyFilterOr(newValues);
        }
        return this;
    }

}
