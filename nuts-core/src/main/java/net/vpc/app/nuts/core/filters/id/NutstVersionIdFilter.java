/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.filters.id;

import net.vpc.app.nuts.*;

import java.util.Objects;

import net.vpc.app.nuts.core.util.common.Simplifiable;

/**
 *
 * @author vpc
 */
public class NutstVersionIdFilter implements NutsIdFilter, Simplifiable<NutsIdFilter> {

    private final NutsVersionFilter filter;

    public NutstVersionIdFilter(NutsVersionFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean acceptSearchId(NutsSearchId sid, NutsWorkspace ws, NutsSession session) {
        return filter == null ? true : filter.acceptSearchId(sid, ws, session);
    }

    @Override
    public boolean accept(NutsId other, NutsWorkspace ws, NutsSession session) {
        if (filter == null) {
            return true;
        }
        return filter.accept(other.getVersion(), ws, session);
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
        if (filter == null) {
            return null;
        }
        return this;
    }

    @Override
    public String toString() {
        return String.valueOf(filter);
    }

}
