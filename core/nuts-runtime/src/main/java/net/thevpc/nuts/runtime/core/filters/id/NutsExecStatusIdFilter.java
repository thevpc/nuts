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
public class NutsExecStatusIdFilter extends AbstractDescriptorFilter {

    private final Boolean executable;
    private final Boolean nutsApplication;

    public NutsExecStatusIdFilter(NutsSession ws, Boolean executable, Boolean nutsApplication) {
        super(ws, NutsFilterOp.CUSTOM);
        this.executable = executable;
        this.nutsApplication = nutsApplication;
    }

    @Override
    public boolean acceptDescriptor(NutsDescriptor other, NutsSession session) {
        if (executable != null) {
            return other.isExecutable() == executable.booleanValue();
        }
        if (nutsApplication != null) {
            return other.isApplication() == nutsApplication.booleanValue();
        }
        return true;
    }

    @Override
    public NutsDescriptorFilter simplify() {
        if (executable == null && nutsApplication == null) {
            return null;
        }
        return this;
    }

    @Override
    public String toString() {
        if (executable != null && nutsApplication != null) {
            if (executable && nutsApplication) {
                return "nuts-app";
            } else if (!executable && !nutsApplication) {
                return "not(exec)";
            } else if (executable) {
                return "exec";
            } else if (nutsApplication) {
                return "nuts-app";
            }
        } else if (executable != null) {
            return executable ? "exec" : "not(exec)";
        } else if (nutsApplication != null) {
            return nutsApplication ? "nuts-app" : "not(nuts-app)";
        }
        return "any";
    }

}
