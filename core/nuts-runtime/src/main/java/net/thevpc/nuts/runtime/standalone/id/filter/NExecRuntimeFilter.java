/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.filter.AbstractDescriptorFilter;

/**
 *
 * @author thevpc
 */
public class NExecRuntimeFilter extends AbstractDescriptorFilter {
    private NId apiId;
    private boolean communityRuntime;
    public NExecRuntimeFilter(NSession session, NId apiId, boolean communityRuntime) {
        super(session, NFilterOp.CUSTOM);
        this.apiId=apiId;
        this.communityRuntime = communityRuntime;
    }

    @Override
    public boolean acceptDescriptor(NDescriptor other, NSession session) {
        if(other.getId().getShortName().equals(NConstants.Ids.NUTS_RUNTIME)){
            if(apiId==null){
                return true;
            }
            for (NDependency dependency : other.getDependencies()) {
                if (dependency.toId().getShortName().equals(NConstants.Ids.NUTS_API)) {
                    if (apiId.getVersion().equals(dependency.toId().getVersion())) {
                        return true;
                    }
                    return false;
                }
            }
        }
        if(communityRuntime) {
            if (!other.getPropertyValue("nuts-runtime").flatMap(NLiteral::asBoolean).orElse(false)) {
                return false;
            }
            for (NDependency dependency : other.getDependencies()) {
                if (dependency.toId().getShortName().equals(NConstants.Ids.NUTS_API)) {
                    if (apiId == null) {
                        return true;
                    }
                    if (apiId.getVersion().equals(dependency.toId().getVersion())) {
                        return true;
                    }
                    return false;
                }
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
            return "runtime";
        }
        return "runtime("+ apiId.getVersion()+")";
    }

}
