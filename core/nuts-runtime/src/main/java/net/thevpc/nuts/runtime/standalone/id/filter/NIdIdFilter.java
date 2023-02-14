/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.Simplifiable;
import net.thevpc.nuts.util.NLog;

import java.util.Map;
import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class NIdIdFilter extends AbstractIdFilter implements NIdFilter, Simplifiable<NIdFilter> {

    private NLog LOG;
    private final NId filter;

    public NIdIdFilter(NId filter, NSession session) {
        super(session, NFilterOp.CUSTOM);
        this.filter = filter;
    }

    @Override
    public boolean acceptSearchId(NSearchId sid, NSession session) {
        return filter == null || acceptId(sid.getId(session), session);
    }

    @Override
    public boolean acceptId(NId id, NSession session) {
        if (filter == null) {
            return true;
        }
        if(LOG==null){
            LOG= NLog.of(NIdIdFilter.class,session);
        }
        if(id.getShortName().equals(filter.getShortName())){
            if (!filter.getVersion().filter(session).acceptVersion(id.getVersion(), session)) {
                return false;
            }
            Map<String, String> e = filter.getProperties();
            Map<String, String> m = id.getProperties();
            for (Map.Entry<String, String> v : e.entrySet()) {
                if(!Objects.equals(
                        v.getValue(),m.get(v.getKey())
                )){
                    return false;
                }
            }
            return true;
        }
        return false;
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
        final NIdIdFilter other = (NIdIdFilter) obj;
        if (!Objects.equals(this.filter, other.filter)) {
            return false;
        }
        return true;
    }

    @Override
    public NIdFilter simplify() {
        if(filter==null){
            return null;
        }
        return this;
    }

    @Override
    public String toString() {
        return String.valueOf(filter);
    }

}
