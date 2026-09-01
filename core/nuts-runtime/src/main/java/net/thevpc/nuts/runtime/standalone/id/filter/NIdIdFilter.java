/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdFilter;
import net.thevpc.nuts.spi.base.NIdFilterBase;
import net.thevpc.nuts.util.NSimplifiable;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Map;
import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class NIdIdFilter extends NIdFilterBase implements NIdFilter, NSimplifiable<NIdFilter> {

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
        if(id.shortName().equals(filter.shortName())){
            if (!filter.version().toFilter().acceptVersion(id.version())) {
                return false;
            }
            Map<String, String> e = filter.properties();
            Map<String, String> m = id.properties();
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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NIdIdFilter that = (NIdIdFilter) o;
        return Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), filter);
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
