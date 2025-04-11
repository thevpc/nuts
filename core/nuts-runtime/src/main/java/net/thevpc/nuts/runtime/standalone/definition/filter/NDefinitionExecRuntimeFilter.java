/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NLiteral;

/**
 *
 * @author thevpc
 */
public class NDefinitionExecRuntimeFilter extends AbstractDefinitionFilter {
    private NId apiId;
    private boolean communityRuntime;
    public NDefinitionExecRuntimeFilter(NId apiId, boolean communityRuntime) {
        super(NFilterOp.CUSTOM);
        this.apiId=apiId;
        this.communityRuntime = communityRuntime;
    }

    @Override
    public boolean acceptDefinition(NDefinition other) {
        if(other.getId().getShortName().equals(NConstants.Ids.NUTS_RUNTIME)){
            if(apiId==null){
                return true;
            }
            for (NDependency dependency : other.getDescriptor().getDependencies()) {
                if (dependency.toId().getShortName().equals(this.apiId.getShortName())) {
                    if (apiId.getVersion().equals(dependency.toId().getVersion())) {
                        return true;
                    }
                    return false;
                }
            }
        }
        if(communityRuntime) {
            if (!other.getDescriptor().getPropertyValue("nuts-runtime").flatMap(NLiteral::asBoolean).orElse(false)) {
                return false;
            }
            for (NDependency dependency : other.getDescriptor().getDependencies()) {
                if (dependency.toId().getShortName().equals(this.apiId.getShortName())) {
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
    public NDefinitionFilter simplify() {
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
