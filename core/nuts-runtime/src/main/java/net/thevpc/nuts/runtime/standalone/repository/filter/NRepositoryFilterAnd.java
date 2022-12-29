package net.thevpc.nuts.runtime.standalone.repository.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NRepositoryFilterAnd extends AbstractRepositoryFilter{

    private NRepositoryFilter[] all;

    public NRepositoryFilterAnd(NSession session, NRepositoryFilter... all) {
        super(session, NFilterOp.AND);
        List<NRepositoryFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NRepositoryFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NRepositoryFilter[0]);
    }

    @Override
    public boolean acceptRepository(NRepository id) {
        if (all.length == 0) {
            return true;
        }
        for (NRepositoryFilter filter : all) {
            if (!filter.acceptRepository(id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NRepositoryFilter simplify() {
        return CoreFilterUtils.simplifyFilterAnd(getSession(), NRepositoryFilter.class,this,all);
    }

    @Override
    public String toString() {
        return String.join(" and ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NRepositoryFilterAnd that = (NRepositoryFilterAnd) o;
        return Arrays.equals(all, that.all);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(all);
    }
    public List<NFilter> getSubFilters() {
        return Arrays.asList(all);
    }
}
