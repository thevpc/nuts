package net.thevpc.nuts.runtime.core.filters.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NutsRepositoryFilterAnd extends AbstractRepositoryFilter{

    private NutsRepositoryFilter[] all;

    public NutsRepositoryFilterAnd(NutsSession session, NutsRepositoryFilter... all) {
        super(session, NutsFilterOp.AND);
        List<NutsRepositoryFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NutsRepositoryFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NutsRepositoryFilter[0]);
    }

    @Override
    public boolean acceptRepository(NutsRepository id) {
        if (all.length == 0) {
            return true;
        }
        for (NutsRepositoryFilter filter : all) {
            if (!filter.acceptRepository(id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsRepositoryFilter simplify() {
        return CoreNutsUtils.simplifyFilterAnd(getWorkspace(), NutsRepositoryFilter.class,this,all);
    }

    @Override
    public String toString() {
        return String.join(" and ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsRepositoryFilterAnd that = (NutsRepositoryFilterAnd) o;
        return Arrays.equals(all, that.all);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(all);
    }
    public NutsFilter[] getSubFilters() {
        return all;
    }
}
