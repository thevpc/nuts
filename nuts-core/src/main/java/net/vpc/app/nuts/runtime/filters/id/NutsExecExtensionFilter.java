/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.filters.id;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.filters.AbstractNutsFilter;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

/**
 *
 * @author vpc
 */
public class NutsExecExtensionFilter extends AbstractNutsFilter implements NutsDescriptorFilter, Simplifiable<NutsDescriptorFilter> {
    private NutsId apiId;
    public NutsExecExtensionFilter(NutsWorkspace ws,NutsId apiId) {
        super(ws,NutsFilterOp.CUSTOM);
        this.apiId=apiId;
    }

    @Override
    public boolean acceptDescriptor(NutsDescriptor other, NutsSession session) {
        if(!CoreCommonUtils.parseBoolean(other.getProperties().get("nuts-extension"),false)){
            return false;
        }
        for (NutsDependency dependency : other.getDependencies()) {
            if(dependency.toId().getShortName().equals(NutsConstants.Ids.NUTS_API)){
                if(apiId==null){
                    return true;
                }
                if(apiId.getVersion().equals(dependency.toId().getVersion())){
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public NutsDescriptorFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        if(apiId==null){
            return "extension";
        }
        return "extension("+ apiId.getVersion()+")";
    }

}
