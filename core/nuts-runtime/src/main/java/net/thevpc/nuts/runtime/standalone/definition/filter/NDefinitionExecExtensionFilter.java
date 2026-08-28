/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class NDefinitionExecExtensionFilter extends DefinitionFilterBase {
    private NId apiId;
    public NDefinitionExecExtensionFilter(NId apiId) {
        super(NFilterOp.CUSTOM);
        this.apiId=apiId;
    }

    @Override
    public boolean acceptDefinition(NDefinition other) {
        if(other.descriptor().idType()!= NIdType.EXTENSION){
            return false;
        }
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NDefinitionExecExtensionFilter that = (NDefinitionExecExtensionFilter) o;
        return Objects.equals(apiId, that.apiId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), apiId);
    }
}
