/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.filters.id;

import net.thevpc.nuts.*;

import java.util.Objects;

import net.thevpc.nuts.runtime.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.util.common.Simplifiable;

/**
 *
 * @author thevpc
 */
public class NutstVersionIdFilter extends AbstractNutsFilter implements NutsIdFilter, Simplifiable<NutsIdFilter> {

    private final NutsVersionFilter filter;

    public NutstVersionIdFilter(NutsVersionFilter filter) {
        super(filter.getWorkspace(), NutsFilterOp.CONVERT);
        this.filter = filter;
    }

    @Override
    public boolean acceptSearchId(NutsSearchId sid, NutsSession session) {
        return filter == null ? true : filter.acceptSearchId(sid, session);
    }

    @Override
    public boolean acceptId(NutsId other, NutsSession session) {
        if (filter == null) {
            return true;
        }
        return filter.acceptVersion(other.getVersion(), session);
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
        final NutstVersionIdFilter other = (NutstVersionIdFilter) obj;
        if (!Objects.equals(this.filter, other.filter)) {
            return false;
        }
        return true;
    }

    @Override
    public NutsIdFilter simplify() {
        NutsVersionFilter f2 = CoreNutsUtils.simplify(filter);
        if (f2 == null) {
            return null;
        }
        if (f2 == filter) {
            return this;
        }
        return new NutstVersionIdFilter(f2);
    }

    @Override
    public String toString() {
        return String.valueOf(filter);
    }

}
