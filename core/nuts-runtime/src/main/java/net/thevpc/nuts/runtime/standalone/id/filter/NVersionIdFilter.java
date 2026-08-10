/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.id.filter;

import java.util.Objects;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdFilter;
import net.thevpc.nuts.artifact.NVersionFilter;
import net.thevpc.nuts.spi.base.AbstractIdFilter;
import net.thevpc.nuts.util.NSimplifiable;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NFilterOp;

/**
 *
 * @author thevpc
 */
public class NVersionIdFilter extends AbstractIdFilter implements NIdFilter, NSimplifiable<NIdFilter> {

    private final NVersionFilter filter;

    public NVersionIdFilter(NVersionFilter filter) {
        super(NFilterOp.CONVERT);
        this.filter = filter;
    }


    @Override
    public boolean acceptId(NId other) {
        if (filter == null) {
            return true;
        }
        return filter.acceptVersion(other.version());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NVersionIdFilter that = (NVersionIdFilter) o;
        return Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), filter);
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
        return new NVersionIdFilter(f2);
    }

    @Override
    public String toString() {
        return String.valueOf(filter);
    }

}
