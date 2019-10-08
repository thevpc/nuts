/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.filters.id;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

/**
 *
 * @author vpc
 */
public class NutsExecRuntimeFilter implements NutsDescriptorFilter, Simplifiable<NutsDescriptorFilter> {
    private NutsId apiId;
    private boolean communityRuntime;
    public NutsExecRuntimeFilter(NutsId apiId,boolean communityRuntime) {
        this.apiId=apiId;
        this.communityRuntime = communityRuntime;
    }

    @Override
    public boolean accept(NutsDescriptor other, NutsSession session) {
        if(other.getId().getShortName().equals(NutsConstants.Ids.NUTS_RUNTIME)){
            if(apiId==null){
                return true;
            }
            for (NutsDependency dependency : other.getDependencies()) {
                if (dependency.getId().getShortName().equals(NutsConstants.Ids.NUTS_API)) {
                    if (apiId.getVersion().equals(dependency.getId().getVersion())) {
                        return true;
                    }
                    return false;
                }
            }
        }
        if(communityRuntime) {
            if (!CoreCommonUtils.parseBoolean(other.getProperties().get("nuts-runtime"), false)) {
                return false;
            }
            for (NutsDependency dependency : other.getDependencies()) {
                if (dependency.getId().getShortName().equals(NutsConstants.Ids.NUTS_API)) {
                    if (apiId == null) {
                        return true;
                    }
                    if (apiId.getVersion().equals(dependency.getId().getVersion())) {
                        return true;
                    }
                    return false;
                }
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
            return "runtime";
        }
        return "runtime("+ apiId.getVersion()+")";
    }

}
