package net.thevpc.nuts.runtime.standalone.definition.installstatus.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.InternalNTypedFilters;

import java.util.List;

import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NUtils;

public class InternalNInstallStatusFilters extends InternalNTypedFilters<NInstallStatusFilter>
        implements NInstallStatusFilters {

    public InternalNInstallStatusFilters(NSession session) {
        super(session, NInstallStatusFilter.class);
//        localModel = model.getShared(LocalModel.class, () -> new LocalModel(ws));
    }

    @Override
    public NInstallStatusFilter not(NFilter other) {
        checkSession();
        NInstallStatusFilter r = other.to(NInstallStatusFilter.class);
        return new NInstallStatusFilterNone(getSession(), r);
    }

    @Override
    public NInstallStatusFilter always() {
        checkSession();
        return new NInstallStatusFilter2(getSession(), 0, 0, 0, 0);
    }

    @Override
    public NInstallStatusFilter never() {
        checkSession();
        return new NInstallStatusFilterFalse(getSession());
    }

    @Override
    public NInstallStatusFilter all(NFilter... others) {
        checkSession();
        List<NInstallStatusFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NInstallStatusFilterAnd(getSession(), all.toArray(new NInstallStatusFilter[0]));
    }

    @Override
    public NInstallStatusFilter any(NFilter... others) {
        List<NInstallStatusFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NInstallStatusFilterOr(getSession(), all.toArray(new NInstallStatusFilter[0]));
    }

    @Override
    public NInstallStatusFilter none(NFilter... others) {
        List<NInstallStatusFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NInstallStatusFilterNone(getSession(), all.toArray(new NInstallStatusFilter[0]));
    }

    @Override
    public NInstallStatusFilter from(NFilter a) {
        checkSession();
        if (a == null) {
            return null;
        }
        NInstallStatusFilter t = as(a);
        NSession session = getSession();
        NUtils.requireNonNull(t, "InstallStatusFilter", session);
        return t;
    }

    @Override
    public NInstallStatusFilter as(NFilter a) {
        if (a instanceof NInstallStatusFilter) {
            return (NInstallStatusFilter) a;
        }
        return null;
    }

    @Override
    public NInstallStatusFilter parse(String expression) {
        checkSession();
        return new NInstallStatusFilterParser(expression, getSession()).parse();
    }

    @Override
    public NInstallStatusFilter byInstalled(boolean value) {
        checkSession();
        return value ? new NInstallStatusFilter2(getSession(), 1, 0, 0, 0) : new NInstallStatusFilter2(getSession(), -1, 0, 0, 0);
    }

    @Override
    public NInstallStatusFilter byRequired(boolean value) {
        checkSession();
        return value ? new NInstallStatusFilter2(getSession(), 0, 1, 0, 0) : new NInstallStatusFilter2(getSession(), 0, -1, 0, 0);
    }

    @Override
    public NInstallStatusFilter byDefaultValue(boolean value) {
        checkSession();
        return value ? new NInstallStatusFilter2(getSession(), 0, 0, 0, 1) : new NInstallStatusFilter2(getSession(), 0, 0, 0, -1);
    }

    @Override
    public NInstallStatusFilter byObsolete(boolean value) {
        return value ? new NInstallStatusFilter2(getSession(), 0, 0, 1, 0) : new NInstallStatusFilter2(getSession(), 0, 0, -1, 0);
    }

    @Override
    public NInstallStatusFilter byDeployed(boolean value) {
        return value
                ? byInstalled(true).or(byRequired(true)).to(NInstallStatusFilter.class)
                : byDeployed(true).neg().to(NInstallStatusFilter.class);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
