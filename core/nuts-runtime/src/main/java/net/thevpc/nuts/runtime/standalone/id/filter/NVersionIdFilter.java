/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;

import java.util.Objects;

import net.thevpc.nuts.runtime.standalone.util.Simplifiable;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NFilterOp;

/**
 *
 * @author thevpc
 */
public class NVersionIdFilter extends AbstractIdFilter implements NIdFilter, Simplifiable<NIdFilter> {

    private final NVersionFilter filter;

    public NVersionIdFilter(NVersionFilter filter, NSession session) {
        super(session, NFilterOp.CONVERT);
        this.filter = filter;
    }

    @Override
    public boolean acceptSearchId(NSearchId sid, NSession session) {
        return filter == null ? true : filter.acceptSearchId(sid, session);
    }

    @Override
    public boolean acceptId(NId other, NSession session) {
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
        final NVersionIdFilter other = (NVersionIdFilter) obj;
        if (!Objects.equals(this.filter, other.filter)) {
            return false;
        }
        return true;
    }

    @Override
    public NIdFilter simplify() {
        NVersionFilter f2 = CoreFilterUtils.simplify(filter);
        if (f2 == null) {
            return null;
        }
        if (f2 == filter) {
            return this;
        }
        return new NVersionIdFilter(f2,getSession());
    }

    @Override
    public String toString() {
        return String.valueOf(filter);
    }

}
