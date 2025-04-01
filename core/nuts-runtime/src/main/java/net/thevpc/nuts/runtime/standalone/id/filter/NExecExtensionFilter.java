/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.filter.AbstractDescriptorFilter;
import net.thevpc.nuts.util.NFilterOp;

/**
 *
 * @author thevpc
 */
public class NExecExtensionFilter extends AbstractDescriptorFilter {
    private NId apiId;
    public NExecExtensionFilter(NId apiId) {
        super(NFilterOp.CUSTOM);
        this.apiId=apiId;
    }

    @Override
    public boolean acceptDescriptor(NDescriptor other) {
        if(other.getIdType()!= NIdType.EXTENSION){
            return false;
        }
        for (NDependency dependency : other.getDependencies()) {
            if(dependency.toId().getShortName().equals(this.apiId.getShortName())){
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
    public NDescriptorFilter simplify() {
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
