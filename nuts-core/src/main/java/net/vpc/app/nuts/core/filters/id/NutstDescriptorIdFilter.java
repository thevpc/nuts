/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.filters.id;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.Simplifiable;

/**
 *
 * @author vpc
 */
public class NutstDescriptorIdFilter implements NutsIdFilter, Simplifiable<NutsIdFilter> {

    private static final Logger LOG = Logger.getLogger(NutstDescriptorIdFilter.class.getName());
    private final NutsDescriptorFilter filter;

    public NutstDescriptorIdFilter(NutsDescriptorFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean acceptSearchId(NutsSearchId sid, NutsWorkspace ws, NutsSession session) {
        return filter == null ? true : filter.acceptSearchId(sid, ws, session);
    }

    @Override
    public boolean accept(NutsId id, NutsWorkspace ws, NutsSession session) {
        if (filter == null) {
            return true;
        }
        NutsDescriptor descriptor = null;
        try {
//                descriptor = repository.fetchDescriptor().setId(id).session(session).run().getResult();
            descriptor = ws.fetch().id(id).session(session).getResultDescriptor();
            if (!CoreNutsUtils.isEffectiveId(descriptor.getId())) {
                NutsDescriptor nutsDescriptor = null;
                try {
                    //NutsWorkspace ws = repository.getWorkspace();
                    nutsDescriptor = NutsWorkspaceExt.of(ws).resolveEffectiveDescriptor(descriptor, session);
                } catch (Exception e) {
                    //throw new NutsException(e);
                }
                descriptor = nutsDescriptor;
            }
        } catch (Exception ex) {
            //suppose we cannot retrieve descriptor
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Unable to fetch Descriptor for " + id  + " : " + ex.toString());
            }
            return false;
        }
        if (!filter.accept(descriptor, ws, session)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.filter);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutstDescriptorIdFilter other = (NutstDescriptorIdFilter) obj;
        if (!Objects.equals(this.filter, other.filter)) {
            return false;
        }
        return true;
    }

    @Override
    public NutsIdFilter simplify() {
        NutsDescriptorFilter f2 = CoreNutsUtils.simplify(filter);
        if (f2 == null) {
            return null;
        }
        if(f2==filter){
            return this;
        }
        return new NutstDescriptorIdFilter(f2);
    }

    @Override
    public String toString() {
        return String.valueOf(filter);
    }

}
