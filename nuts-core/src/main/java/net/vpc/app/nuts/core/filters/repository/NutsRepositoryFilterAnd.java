package net.vpc.app.nuts.core.filters.repository;

import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryFilter;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.Simplifiable;

import java.util.ArrayList;
import java.util.List;

public class NutsRepositoryFilterAnd implements NutsRepositoryFilter, Simplifiable<NutsRepositoryFilter> {
    private NutsRepositoryFilter[] all;

    public NutsRepositoryFilterAnd(NutsRepositoryFilter... all) {
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
    public boolean accept(NutsRepository id) {
        if (all.length == 0) {
            return true;
        }
        for (NutsRepositoryFilter filter : all) {
            if (!filter.accept(id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsRepositoryFilter simplify() {
        NutsRepositoryFilter[] newValues = CoreNutsUtils.simplifyAndShrink(NutsRepositoryFilter.class, all);
        if (newValues != null) {
            if (newValues.length == 0) {
                return null;
            }
            if (newValues.length == 1) {
                return newValues[0];
            }
            return new NutsRepositoryFilterAnd(newValues);
        }
        return this;
    }

}
