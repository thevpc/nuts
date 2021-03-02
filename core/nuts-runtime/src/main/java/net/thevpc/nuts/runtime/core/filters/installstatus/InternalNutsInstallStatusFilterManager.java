package net.thevpc.nuts.runtime.core.filters.installstatus;

import net.thevpc.nuts.NutsFilter;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsInstallStatusFilter;
import net.thevpc.nuts.NutsInstallStatusFilterManager;
import net.thevpc.nuts.runtime.core.filters.DefaultNutsFilterManager;
import net.thevpc.nuts.runtime.core.filters.InternalNutsTypedFilters;

import java.util.List;

public class InternalNutsInstallStatusFilterManager extends InternalNutsTypedFilters<NutsInstallStatusFilter> implements NutsInstallStatusFilterManager {

    NutsInstallStatusFilter ANY;
    NutsInstallStatusFilter NEVER;

    NutsInstallStatusFilter INSTALLED;
    NutsInstallStatusFilter NOT_INSTALLED;
    NutsInstallStatusFilter REQUIRED;
    NutsInstallStatusFilter NOT_REQUIRED;
    NutsInstallStatusFilter OBSOLETE;
    NutsInstallStatusFilter NOT_OBSOLETE;

    NutsInstallStatusFilter DEFAULT_VALUE;
    NutsInstallStatusFilter NOT_DEFAULT_VALUE;

    NutsInstallStatusFilter DEPLOYED;

    NutsInstallStatusFilter NOT_DEPLOYED;

    public InternalNutsInstallStatusFilterManager(DefaultNutsFilterManager defaultNutsFilterManager) {
        super(defaultNutsFilterManager, NutsInstallStatusFilter.class);
        ANY = new NutsInstallStatusFilter2(ws, 0, 0, 0, 0);
        INSTALLED = new NutsInstallStatusFilter2(ws, 1, 0, 0, 0);
        NOT_INSTALLED = new NutsInstallStatusFilter2(ws, -1, 0, 0, 0);
        REQUIRED = new NutsInstallStatusFilter2(ws, 0, 1, 0, 0);
        NOT_REQUIRED = new NutsInstallStatusFilter2(ws, 0, -1, 0, 0);
        OBSOLETE = new NutsInstallStatusFilter2(ws, 0, 0, 1, 0);
        NOT_OBSOLETE = new NutsInstallStatusFilter2(ws, 0, 0, -1, 0);
        DEFAULT_VALUE = new NutsInstallStatusFilter2(ws, 0, 0, 0, 1);
        NOT_DEFAULT_VALUE = new NutsInstallStatusFilter2(ws, 0, 0, 0, -1);
        NEVER = new NutsInstallStatusFilterFalse(ws);
    }

    @Override
    public NutsInstallStatusFilter not(NutsFilter other) {
        NutsInstallStatusFilter r = other.to(NutsInstallStatusFilter.class);
        return new NutsInstallStatusFilterNone(ws, r);
    }
    

    @Override
    public NutsInstallStatusFilter always() {
        return ANY;
    }

    @Override
    public NutsInstallStatusFilter never() {
        return null;
    }

    @Override
    public NutsInstallStatusFilter all(NutsFilter... others) {
        List<NutsInstallStatusFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsInstallStatusFilterAnd(ws, all.toArray(new NutsInstallStatusFilter[0]));
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
        return new NutsInstallStatusFilterOr(ws, all.toArray(new NutsInstallStatusFilter[0]));
    }

    @Override
    public NutsInstallStatusFilter none(NutsFilter... others) {
        List<NutsInstallStatusFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NutsInstallStatusFilterNone(ws, all.toArray(new NutsInstallStatusFilter[0]));
    }

    @Override
    public NutsInstallStatusFilter from(NutsFilter a) {
        if (a == null) {
            return null;
        }
        NutsInstallStatusFilter t = as(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(ws, "not a InstallStatusFilter");
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
        return new NutsInstallStatusFilterParser(expression, ws).parse();
    }

    @Override
    public NutsInstallStatusFilter byInstalled(boolean value) {
        return value ? INSTALLED : NOT_INSTALLED;
    }

    @Override
    public NutsInstallStatusFilter byRequired(boolean value) {
        return value ? REQUIRED : NOT_REQUIRED;
    }

    @Override
    public NutsInstallStatusFilter byDefaultValue(boolean value) {
        return value ? DEFAULT_VALUE : NOT_DEFAULT_VALUE;
    }

    @Override
    public NutsInstallStatusFilter byObsolete(boolean value) {
        return value ? OBSOLETE : NOT_OBSOLETE;
    }

    @Override
    public NutsInstallStatusFilter byDeployed(boolean value) {
        if (DEPLOYED == null) {
            DEPLOYED = INSTALLED.or(REQUIRED).to(NutsInstallStatusFilter.class);
        }
        if (NOT_DEPLOYED == null) {
            NOT_DEPLOYED = DEPLOYED.neg().to(NutsInstallStatusFilter.class);
        }
        return value ? DEPLOYED : NOT_DEPLOYED;
    }

}
