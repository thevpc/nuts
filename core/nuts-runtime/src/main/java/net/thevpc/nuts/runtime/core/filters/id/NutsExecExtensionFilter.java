/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.filters.id;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.descriptor.AbstractDescriptorFilter;

/**
 *
 * @author thevpc
 */
public class NutsExecExtensionFilter extends AbstractDescriptorFilter {
    private NutsId apiId;
    public NutsExecExtensionFilter(NutsSession session, NutsId apiId) {
        super(session, NutsFilterOp.CUSTOM);
        this.apiId=apiId;
    }

    @Override
    public boolean acceptDescriptor(NutsDescriptor other, NutsSession session) {
        if(!other.getFlags().contains(NutsDescriptorFlag.NUTS_EXTENSION)){
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
