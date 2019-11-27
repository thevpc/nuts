/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.filters.id;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author vpc
 */
public class NutsExecCompanionFilter implements NutsDescriptorFilter, Simplifiable<NutsDescriptorFilter> {
    private NutsId apiId;
    private Set<String> companions;
    public NutsExecCompanionFilter(NutsId apiId,String[] shortIds) {
        this.apiId=apiId;
        this.companions=new HashSet<>(Arrays.asList(shortIds));
    }

    @Override
    public boolean accept(NutsDescriptor other, NutsSession session) {
        if(companions.contains(other.getId().getShortName())){
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
        return false;
    }

    @Override
    public NutsDescriptorFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        if(apiId==null){
            return "companion";
        }
        return "companion("+ apiId.getVersion()+")";
    }

}
