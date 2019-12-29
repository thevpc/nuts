/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.filters.id;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 *
 * @author vpc
 */
public class NutsLockedIdExtensionFilter implements NutsDescriptorFilter, Simplifiable<NutsDescriptorFilter> {
    private NutsId[] lockedIds;
    public NutsLockedIdExtensionFilter(NutsId[] lockedIds) {
        this.lockedIds =lockedIds;
    }

    public boolean acceptId(NutsId id, NutsSession session) {
        for (NutsId nutsId : lockedIds) {
            if(nutsId.getShortNameId().equalsShortName(id.getShortNameId())){
                return (id.getVersion().filter().accept(nutsId.getVersion(),session));
            }
        }
        return true;
    }

    @Override
    public boolean accept(NutsDescriptor other, NutsSession session) {
        if(!acceptId(other.getId(),session)){
            return false;
        }
        for (NutsDependency dependency : other.getDependencies()) {
            if(!acceptId(dependency.getId(),session)){
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsDescriptorFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        return "LockedIds("+ Arrays.stream(lockedIds).map(NutsId::getLongName).collect(Collectors.joining(","))+")";
    }

}
