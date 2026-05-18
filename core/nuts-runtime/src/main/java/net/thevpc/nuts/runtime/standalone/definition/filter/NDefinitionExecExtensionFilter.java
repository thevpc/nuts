/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.*;
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
        if(other.descriptor().getIdType()!= NIdType.EXTENSION){
            return false;
        }
        for (NDependency dependency : other.descriptor().getDependencies()) {
            if(dependency.toId().shortName().equals(this.apiId.shortName())){
                if(apiId==null){
                    return true;
                }
                if(apiId.version().equals(dependency.toId().version())){
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
        return "extension("+ apiId.version()+")";
    }

}
