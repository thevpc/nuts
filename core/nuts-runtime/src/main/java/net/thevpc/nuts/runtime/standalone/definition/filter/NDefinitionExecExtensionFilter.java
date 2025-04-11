/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NFilterOp;

/**
 *
 * @author thevpc
 */
public class NDefinitionExecExtensionFilter extends AbstractDefinitionFilter {
    private NId apiId;
    public NDefinitionExecExtensionFilter(NId apiId) {
        super(NFilterOp.CUSTOM);
        this.apiId=apiId;
    }

    @Override
    public boolean acceptDefinition(NDefinition other) {
        if(other.getDescriptor().getIdType()!= NIdType.EXTENSION){
            return false;
        }
        for (NDependency dependency : other.getDescriptor().getDependencies()) {
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
    public NDefinitionFilter simplify() {
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
