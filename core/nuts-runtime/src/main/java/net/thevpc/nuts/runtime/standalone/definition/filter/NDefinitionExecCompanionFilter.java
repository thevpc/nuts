/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author thevpc
 */
public class NDefinitionExecCompanionFilter extends DefinitionFilterBase {
    private NId apiId;
    private Set<String> companions;
    public NDefinitionExecCompanionFilter(NId apiId, String[] shortIds) {
        super(NFilterOp.CUSTOM);
        this.apiId=apiId;
        this.companions=new HashSet<>(Arrays.asList(shortIds));
    }

    @Override
    public boolean acceptDefinition(NDefinition other) {
        if(companions.contains(other.id().shortName())){
            for (NDependency dependency : other.descriptor().dependencies()) {
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
        return "companion("+ apiId.version()+")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NDefinitionExecCompanionFilter that = (NDefinitionExecCompanionFilter) o;
        return Objects.equals(apiId, that.apiId) && Objects.equals(companions, that.companions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), apiId, companions);
    }
}
