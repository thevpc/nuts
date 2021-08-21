package net.thevpc.nuts.runtime.core.filters.id;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.InternalNutsTypedFilters;
import net.thevpc.nuts.runtime.core.filters.NutsPatternIdFilter;
import net.thevpc.nuts.runtime.bundles.io.NutsInstallStatusIdFilter;

import java.util.List;
import net.thevpc.nuts.runtime.core.filters.DefaultNutsFilterModel;

public class InternalNutsIdFilterManager extends InternalNutsTypedFilters<NutsIdFilter> implements NutsIdFilterManager {

    private static class LocalModel {

        private NutsIdFilterTrue nutsIdFilterTrue;
        private NutsIdFilterFalse nutsIdFilterFalse;
        private NutsWorkspace ws;

        public LocalModel(NutsWorkspace ws) {
            this.ws = ws;
        }
//
//        public NutsIdFilter always() {
//            if (nutsIdFilterTrue == null) {
//                nutsIdFilterTrue = new NutsIdFilterTrue(ws);
//            }
//            return nutsIdFilterTrue;
//        }
//
//        public NutsIdFilter never() {
//            if (nutsIdFilterFalse == null) {
//                nutsIdFilterFalse = new NutsIdFilterFalse(ws);
//            }
//            return nutsIdFilterFalse;
//        }

    }
    private final LocalModel localModel;

    public InternalNutsIdFilterManager(DefaultNutsFilterModel model) {
        super(model, NutsIdFilter.class);
        localModel = model.getShared(LocalModel.class, () -> new LocalModel(ws));
    }

    @Override
    public InternalNutsIdFilterManager setSession(NutsSession session) {
        super.setSession(session);
        return this;
    }

    @Override
    public NutsIdFilter always() {
        checkSession();
        return new NutsIdFilterTrue(getSession());
    }

    @Override
    public NutsIdFilter not(NutsFilter other) {
        return new NutsIdFilterNone(getSession(), (NutsIdFilter) other);
    }

    @Override
    public NutsIdFilter never() {
        checkSession();
        return new NutsIdFilterFalse(getSession());
    }

    @Override
    public NutsIdFilter byExpression(String expression) {
        if (NutsUtilStrings.isBlank(expression)) {
            return always();
        }
        return NutsJavascriptIdFilter.valueOf(expression, getSession());
    }

    @Override
    public NutsIdFilter byDefaultVersion(Boolean defaultVersion) {
        if (defaultVersion == null) {
            return always();
        }
        return new NutsDefaultVersionIdFilter(getSession(), defaultVersion);
    }

    @Override
    public NutsIdFilter byInstallStatus(NutsInstallStatusFilter installStatus) {
        return new NutsInstallStatusIdFilter(getSession(), installStatus);
    }

    @Override
    public NutsIdFilter byName(String... names) {
        checkSession();
        if (names == null || names.length == 0) {
            return always();
        }
        NutsIdFilter f = null;
        for (String wildcardId : names) {
            if (f == null) {
                f = new NutsPatternIdFilter(getSession(), getSession().getWorkspace().id().parser().parse(wildcardId));
            } else {
                f = (NutsIdFilter) f.or(new NutsPatternIdFilter(getSession(), getSession().getWorkspace().id().parser().parse(wildcardId)));
            }
        }
        return f;
    }

    @Override
    public NutsIdFilter as(NutsFilter a) {
        checkSession();
        if (a instanceof NutsIdFilter) {
            return (NutsIdFilter) a;
        }
        if (a instanceof NutsDescriptorFilter) {
            return new NutsDescriptorIdFilter((NutsDescriptorFilter) a,getSession());
        }
        if (a instanceof NutsVersionFilter) {
            return new NutstVersionIdFilter((NutsVersionFilter) a,getSession());
        }
        return null;
    }

    @Override
    public NutsIdFilter from(NutsFilter a) {
        checkSession();
        if (a == null) {
            return null;
        }
        NutsIdFilter t = as(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("not a IdFilter"));
        }
        return t;
    }

    @Override
    public NutsIdFilter all(NutsFilter... others) {
        List<NutsIdFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsIdFilterAnd(getSession(), all.toArray(new NutsIdFilter[0]));
    }

    @Override
    public NutsIdFilter any(NutsFilter... others) {
        List<NutsIdFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsIdFilterOr(getSession(), all.toArray(new NutsIdFilter[0]));
    }

    @Override
    public NutsIdFilter none(NutsFilter... others) {
        List<NutsIdFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NutsIdFilterNone(getSession(), all.toArray(new NutsIdFilter[0]));
    }

    @Override
    public NutsIdFilter parse(String expression) {
        return new NutsIdFilterParser(expression, getSession()).parse();
    }
}
