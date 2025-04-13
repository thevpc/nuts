/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class NLockedIdExtensionDefinitionFilter extends AbstractDefinitionFilter {
    private NId[] lockedIds;

    public NLockedIdExtensionDefinitionFilter(NId[] lockedIds) {
        super(NFilterOp.CUSTOM);
        this.lockedIds = lockedIds;
    }

    public boolean acceptId(NId id) {
        for (NId nutsId : lockedIds) {
            if (nutsId.getShortId().equalsShortId(id.getShortId())) {
                return (id.getVersion().filter().acceptVersion(nutsId.getVersion()));
            }
        }
        return true;
    }

    @Override
    public boolean acceptDefinition(NDefinition other) {
        if (!acceptId(other.getId())) {
            return false;
        }
        for (NDependency dependency : other.getDescriptor().getDependencies()) {
            if (!acceptId(dependency.toId())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NDefinitionFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        return "LockedIds(" + Arrays.stream(lockedIds).map(NId::getLongName).collect(Collectors.joining(",")) + ")";
    }

    public NLockedIdExtensionDefinitionFilter addAll(NId[] others) {
        NId[] validIds = others == null ? new NId[0] : Arrays.stream(others).filter(x -> NBlankable.isBlank(x)).collect(Collectors.toSet()).toArray(new NId[0]);
        Set<NId> old0 = new HashSet<>(Arrays.asList(lockedIds));
        Set<NId> old1 = new HashSet<>(old0);
        old1.addAll(Arrays.asList(validIds));
        if (old1.equals(old0)) {
            return this;
        }
        return new NLockedIdExtensionDefinitionFilter(old1.toArray(new NId[0]));
    }
}
