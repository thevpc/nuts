package net.thevpc.nuts.runtime.standalone.repository.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.dependency.util.NComplexExpressionString;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NRepositoryFilterOr extends AbstractRepositoryFilter implements NComplexExpressionString {

    private NRepositoryFilter[] all;

    public NRepositoryFilterOr(NRepositoryFilter... all) {
        super(NFilterOp.OR);
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
            if (filter.acceptRepository(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NRepositoryFilter simplify() {
        return CoreFilterUtils.simplifyFilterOr(NRepositoryFilter.class,this,all);
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrOr(Arrays.stream(all).map(NComplexExpressionString::toString).collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NRepositoryFilterOr that = (NRepositoryFilterOr) o;
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
