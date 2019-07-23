/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.filters.id;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.Simplifiable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author vpc
 */
public class NutsFrozenIdExtensionFilter implements NutsDescriptorFilter, Simplifiable<NutsDescriptorFilter> {
    private NutsId[] frozen;
    public NutsFrozenIdExtensionFilter(NutsId[] frozen) {
        this.frozen=frozen;
    }

    public boolean acceptId(NutsId id, NutsSession session) {
        for (NutsId nutsId : frozen) {
            if(nutsId.getShortNameId().equalsSimpleName(id.getShortNameId())){
                return (id.getVersion().toFilter().accept(nutsId.getVersion(),session));
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
        return "frozen("+ Arrays.stream(frozen).map(NutsId::getLongName).collect(Collectors.joining(","))+")";
    }

}
