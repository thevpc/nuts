/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.filters.id;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.util.common.Simplifiable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author vpc
 */
public class NutsExecCompanionFilter extends AbstractNutsFilter implements NutsDescriptorFilter, Simplifiable<NutsDescriptorFilter> {
    private NutsId apiId;
    private Set<String> companions;
    public NutsExecCompanionFilter(NutsWorkspace ws, NutsId apiId, String[] shortIds) {
        super(ws, NutsFilterOp.CUSTOM);
        this.apiId=apiId;
        this.companions=new HashSet<>(Arrays.asList(shortIds));
    }

    @Override
    public boolean acceptDescriptor(NutsDescriptor other, NutsSession session) {
        if(companions.contains(other.getId().getShortName())){
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
