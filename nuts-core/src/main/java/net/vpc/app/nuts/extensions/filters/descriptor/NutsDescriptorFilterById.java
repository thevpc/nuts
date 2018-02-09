/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.filters.descriptor;

import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsDescriptorFilter;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.extensions.filters.id.JsNutsIdFilter;
import net.vpc.app.nuts.extensions.util.Simplifiable;

/**
 *
 * @author vpc
 */
public class NutsDescriptorFilterById implements NutsDescriptorFilter, Simplifiable<NutsDescriptorFilter>, JsNutsDescriptorFilter {

    private NutsIdFilter id;

    public NutsDescriptorFilterById(NutsIdFilter id) {
        this.id = id;
    }

    @Override
    public boolean accept(NutsDescriptor descriptor) {
        if (id != null) {
            return id.accept(descriptor.getId());
        }
        return true;
    }

    @Override
    public NutsDescriptorFilter simplify() {
        if (id != null && id instanceof Simplifiable) {
            NutsIdFilter id2 = ((Simplifiable<NutsIdFilter>) id).simplify();
            if (id2 != id) {
                if (id2 == null) {
                    return null;
                }
                return new NutsDescriptorFilterById(id2);
            }
        }
        return this;
    }

    @Override
    public String toJsNutsDescriptorFilterExpr() {
        if (id == null) {
            return "true";
        }
        if (id instanceof JsNutsIdFilter) {
            return ((JsNutsIdFilter) id).toJsNutsIdFilterExpr();
        }
        return null;
    }

}
