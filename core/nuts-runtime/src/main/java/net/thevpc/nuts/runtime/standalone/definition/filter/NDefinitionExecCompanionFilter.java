/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author thevpc
 */
public class NDefinitionExecCompanionFilter extends AbstractDefinitionFilter {
    private NId apiId;
    private Set<String> companions;
    public NDefinitionExecCompanionFilter(NId apiId, String[] shortIds) {
        super(NFilterOp.CUSTOM);
        this.apiId=apiId;
        this.companions=new HashSet<>(Arrays.asList(shortIds));
    }

    @Override
    public boolean acceptDefinition(NDefinition other) {
        if(companions.contains(other.getId().getShortName())){
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
        return false;
    }

    @Override
    public NDefinitionFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        if(apiId==null){
            return "companion";
        }
        return "companion("+ apiId.getVersion()+")";
    }

}
