/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.filters.id;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.Simplifiable;

/**
 *
 * @author vpc
 */
public class NutsExecExtensionFilter implements NutsDescriptorFilter, Simplifiable<NutsDescriptorFilter> {
    private NutsId apiId;
    public NutsExecExtensionFilter(NutsId apiId) {
        this.apiId=apiId;
    }

    @Override
    public boolean accept(NutsDescriptor other, NutsSession session) {
        if(!CoreCommonUtils.parseBoolean(other.getProperties().get("nuts-extension"),false)){
            return false;
        }
        for (NutsDependency dependency : other.getDependencies()) {
            if(dependency.getId().getShortName().equals(NutsConstants.Ids.NUTS_API)){
                if(apiId==null){
                    return true;
                }
                if(apiId.getVersion().equals(dependency.getId().getVersion())){
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
