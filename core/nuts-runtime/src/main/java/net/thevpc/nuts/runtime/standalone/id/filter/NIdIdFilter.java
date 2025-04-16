/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.base.AbstractIdFilter;
import net.thevpc.nuts.util.NSimplifiable;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Map;
import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class NIdIdFilter extends AbstractIdFilter implements NIdFilter, NSimplifiable<NIdFilter> {

    private NLog LOG;
    private final NId filter;

    public NIdIdFilter(NId filter) {
        super(NFilterOp.CUSTOM);
        this.filter = filter;
    }


    @Override
    public boolean acceptId(NId id) {
        if (filter == null) {
            return true;
        }
        if(LOG==null){
            LOG= NLog.of(NIdIdFilter.class);
        }
        if(id.getShortName().equals(filter.getShortName())){
            if (!filter.getVersion().filter().acceptVersion(id.getVersion())) {
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
