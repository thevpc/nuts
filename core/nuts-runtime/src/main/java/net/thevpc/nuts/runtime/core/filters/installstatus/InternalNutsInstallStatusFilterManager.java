package net.thevpc.nuts.runtime.core.filters.installstatus;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.InternalNutsTypedFilters;

import java.util.List;

import net.thevpc.nuts.runtime.core.filters.DefaultNutsFilterModel;

public class InternalNutsInstallStatusFilterManager extends InternalNutsTypedFilters<NutsInstallStatusFilter> implements NutsInstallStatusFilterManager {

//    private static class LocalModel {
//
//        NutsInstallStatusFilter ANY;
//        NutsInstallStatusFilter NEVER;
//
//        NutsInstallStatusFilter INSTALLED;
//        NutsInstallStatusFilter NOT_INSTALLED;
//        NutsInstallStatusFilter REQUIRED;
//        NutsInstallStatusFilter NOT_REQUIRED;
//        NutsInstallStatusFilter OBSOLETE;
//        NutsInstallStatusFilter NOT_OBSOLETE;
//
//        NutsInstallStatusFilter DEFAULT_VALUE;
//        NutsInstallStatusFilter NOT_DEFAULT_VALUE;
//
//        NutsInstallStatusFilter DEPLOYED;
//
//        NutsInstallStatusFilter NOT_DEPLOYED;
//        private NutsWorkspace ws;
//
//        public LocalModel(NutsWorkspace ws) {
//            this.ws = ws;
//            ANY = new NutsInstallStatusFilter2(ws, 0, 0, 0, 0);
////            INSTALLED = new NutsInstallStatusFilter2(ws, 1, 0, 0, 0);
////            NOT_INSTALLED = new NutsInstallStatusFilter2(ws, -1, 0, 0, 0);
////            REQUIRED = new NutsInstallStatusFilter2(ws, 0, 1, 0, 0);
////            NOT_REQUIRED = new NutsInstallStatusFilter2(ws, 0, -1, 0, 0);
////            OBSOLETE = new NutsInstallStatusFilter2(ws, 0, 0, 1, 0);
////            NOT_OBSOLETE = new NutsInstallStatusFilter2(ws, 0, 0, -1, 0);
//            DEFAULT_VALUE = new NutsInstallStatusFilter2(ws, 0, 0, 0, 1);
//            NOT_DEFAULT_VALUE = new NutsInstallStatusFilter2(ws, 0, 0, 0, -1);
//            NEVER = new NutsInstallStatusFilterFalse(ws);
//        }
//
//    }
//    private LocalModel localModel;
    public InternalNutsInstallStatusFilterManager(DefaultNutsFilterModel model) {
        super(model, NutsInstallStatusFilter.class);
//        localModel = model.getShared(LocalModel.class, () -> new LocalModel(ws));
    }

    @Override
    public InternalNutsInstallStatusFilterManager setSession(NutsSession session) {
        super.setSession(session);
        return this;
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

}
