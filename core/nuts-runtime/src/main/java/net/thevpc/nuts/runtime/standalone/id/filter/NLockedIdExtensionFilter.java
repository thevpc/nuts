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
import net.thevpc.nuts.util.NFilterOp;

/**
 *
 * @author thevpc
 */
public class NLockedIdExtensionFilter extends AbstractDescriptorFilter {
    private NId[] lockedIds;
    public NLockedIdExtensionFilter(NSession session, NId[] lockedIds) {
        super(session, NFilterOp.CUSTOM);
        this.lockedIds =lockedIds;
    }

    public boolean acceptId(NId id, NSession session) {
        for (NId nutsId : lockedIds) {
            if(nutsId.getShortId().equalsShortId(id.getShortId())){
                return (id.getVersion().filter(session).acceptVersion(nutsId.getVersion(),session));
            }
        }
        return true;
    }

    @Override
    public boolean acceptDescriptor(NDescriptor other, NSession session) {
        if(!acceptId(other.getId(),session)){
            return false;
        }
        for (NDependency dependency : other.getDependencies()) {
            if(!acceptId(dependency.toId(),session)){
                return false;
            }
        }
        return true;
    }

    @Override
    public NDescriptorFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        return "LockedIds("+ Arrays.stream(lockedIds).map(NId::getLongName).collect(Collectors.joining(","))+")";
    }

}
