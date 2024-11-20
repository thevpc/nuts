package net.thevpc.nuts.runtime.standalone.definition.installstatus.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.InternalNTypedFilters;

import java.util.List;

import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NFilter;

public class InternalNInstallStatusFilters extends InternalNTypedFilters<NInstallStatusFilter>
        implements NInstallStatusFilters {

    public InternalNInstallStatusFilters(NWorkspace workspace) {
        super(workspace, NInstallStatusFilter.class);
//        localModel = model.getShared(LocalModel.class, () -> new LocalModel(ws));
    }

    @Override
    public NInstallStatusFilter not(NFilter other) {
        NInstallStatusFilter r = other.to(NInstallStatusFilter.class);
        return new NInstallStatusFilterNone(ws, r);
    }

    @Override
    public NInstallStatusFilter always() {
        return new NInstallStatusFilter2(ws, 0, 0, 0, 0);
    }

    @Override
    public NInstallStatusFilter never() {
        return new NInstallStatusFilterFalse(ws);
    }

    @Override
    public NInstallStatusFilter all(NFilter... others) {
        List<NInstallStatusFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NInstallStatusFilterAnd(ws, all.toArray(new NInstallStatusFilter[0]));
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
        return new NInstallStatusFilterOr(ws, all.toArray(new NInstallStatusFilter[0]));
    }

    @Override
    public NInstallStatusFilter none(NFilter... others) {
        List<NInstallStatusFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NInstallStatusFilterNone(ws, all.toArray(new NInstallStatusFilter[0]));
    }

    @Override
    public NInstallStatusFilter from(NFilter a) {
        if (a == null) {
            return null;
        }
        NInstallStatusFilter t = as(a);
        NAssert.requireNonNull(t, "InstallStatusFilter");
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
        return new NInstallStatusFilterParser(expression, getWorkspace()).parse();
    }

    @Override
    public NInstallStatusFilter byInstalled(boolean value) {
        return value ? new NInstallStatusFilter2(ws, 1, 0, 0, 0) : new NInstallStatusFilter2(ws, -1, 0, 0, 0);
    }

    @Override
    public NInstallStatusFilter byRequired(boolean value) {
        return value ? new NInstallStatusFilter2(ws, 0, 1, 0, 0) : new NInstallStatusFilter2(ws, 0, -1, 0, 0);
    }

    @Override
    public NInstallStatusFilter byDefaultValue(boolean value) {
        return value ? new NInstallStatusFilter2(ws, 0, 0, 0, 1) : new NInstallStatusFilter2(ws, 0, 0, 0, -1);
    }

    @Override
    public NInstallStatusFilter byObsolete(boolean value) {
        return value ? new NInstallStatusFilter2(ws, 0, 0, 1, 0) : new NInstallStatusFilter2(ws, 0, 0, -1, 0);
    }

    @Override
    public NInstallStatusFilter byDeployed(boolean value) {
        return value
                ? byInstalled(true).or(byRequired(true)).to(NInstallStatusFilter.class)
                : byDeployed(true).neg().to(NInstallStatusFilter.class);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
