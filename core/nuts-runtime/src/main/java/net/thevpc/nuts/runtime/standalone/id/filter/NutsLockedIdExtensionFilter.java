/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;

import java.util.Arrays;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.standalone.descriptor.filter.AbstractDescriptorFilter;

/**
 *
 * @author thevpc
 */
public class NutsLockedIdExtensionFilter extends AbstractDescriptorFilter {
    private NutsId[] lockedIds;
    public NutsLockedIdExtensionFilter(NutsSession session, NutsId[] lockedIds) {
        super(session, NutsFilterOp.CUSTOM);
        this.lockedIds =lockedIds;
    }

    public boolean acceptId(NutsId id, NutsSession session) {
        for (NutsId nutsId : lockedIds) {
            if(nutsId.getShortId().equalsShortId(id.getShortId())){
                return (id.getVersion().filter(session).acceptVersion(nutsId.getVersion(),session));
            }
        }
        return true;
    }

    @Override
    public boolean acceptDescriptor(NutsDescriptor other, NutsSession session) {
        if(!acceptId(other.getId(),session)){
            return false;
        }
        for (NutsDependency dependency : other.getDependencies()) {
            if(!acceptId(dependency.toId(),session)){
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
