package net.thevpc.nuts.runtime.core.filters.installstatus;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.InternalNutsTypedFilters;

import java.util.List;

import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class InternalNutsInstallStatusFilters extends InternalNutsTypedFilters<NutsInstallStatusFilter>
        implements NutsInstallStatusFilters {

    public InternalNutsInstallStatusFilters(NutsSession session) {
        super(session, NutsInstallStatusFilter.class);
//        localModel = model.getShared(LocalModel.class, () -> new LocalModel(ws));
    }

    @Override
    public NutsInstallStatusFilter not(NutsFilter other) {
        checkSession();
        NutsInstallStatusFilter r = other.to(NutsInstallStatusFilter.class);
        return new NutsInstallStatusFilterNone(getSession(), r);
    }

    @Override
    public NutsInstallStatusFilter always() {
        checkSession();
        return new NutsInstallStatusFilter2(getSession(), 0, 0, 0, 0);
    }

    @Override
    public NutsInstallStatusFilter never() {
        checkSession();
        return new NutsInstallStatusFilterFalse(getSession());
    }

    @Override
    public NutsInstallStatusFilter all(NutsFilter... others) {
        checkSession();
        List<NutsInstallStatusFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsInstallStatusFilterAnd(getSession(), all.toArray(new NutsInstallStatusFilter[0]));
    }

    @Override
    public NutsInstallStatusFilter any(NutsFilter... others) {
        List<NutsInstallStatusFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsInstallStatusFilterOr(getSession(), all.toArray(new NutsInstallStatusFilter[0]));
    }

    @Override
    public NutsInstallStatusFilter none(NutsFilter... others) {
        List<NutsInstallStatusFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NutsInstallStatusFilterNone(getSession(), all.toArray(new NutsInstallStatusFilter[0]));
    }

    @Override
    public NutsInstallStatusFilter from(NutsFilter a) {
        checkSession();
        if (a == null) {
            return null;
        }
        NutsInstallStatusFilter t = as(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("not a InstallStatusFilter"));
        }
        return t;
    }

    @Override
    public NutsInstallStatusFilter as(NutsFilter a) {
        if (a instanceof NutsInstallStatusFilter) {
            return (NutsInstallStatusFilter) a;
        }
        return null;
    }

    @Override
    public NutsInstallStatusFilter parse(String expression) {
        checkSession();
        return new NutsInstallStatusFilterParser(expression, getSession()).parse();
    }

    @Override
    public NutsInstallStatusFilter byInstalled(boolean value) {
        checkSession();
        return value ? new NutsInstallStatusFilter2(getSession(), 1, 0, 0, 0) : new NutsInstallStatusFilter2(getSession(), -1, 0, 0, 0);
    }

    @Override
    public NutsInstallStatusFilter byRequired(boolean value) {
        checkSession();
        return value ? new NutsInstallStatusFilter2(getSession(), 0, 1, 0, 0) : new NutsInstallStatusFilter2(getSession(), 0, -1, 0, 0);
    }

    @Override
    public NutsInstallStatusFilter byDefaultValue(boolean value) {
        checkSession();
        return value ? new NutsInstallStatusFilter2(getSession(), 0, 0, 0, 1) : new NutsInstallStatusFilter2(getSession(), 0, 0, 0, -1);
    }

    @Override
    public NutsInstallStatusFilter byObsolete(boolean value) {
        return value ? new NutsInstallStatusFilter2(getSession(), 0, 0, 1, 0) : new NutsInstallStatusFilter2(getSession(), 0, 0, -1, 0);
    }

    @Override
    public NutsInstallStatusFilter byDeployed(boolean value) {
        return value
                ? byInstalled(true).or(byRequired(true)).to(NutsInstallStatusFilter.class)
                : byDeployed(true).neg().to(NutsInstallStatusFilter.class);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
