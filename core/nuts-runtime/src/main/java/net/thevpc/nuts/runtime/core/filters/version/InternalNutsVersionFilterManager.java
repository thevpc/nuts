package net.thevpc.nuts.runtime.core.filters.version;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.InternalNutsTypedFilters;

import java.util.List;
import net.thevpc.nuts.runtime.core.filters.DefaultNutsFilterModel;

public class InternalNutsVersionFilterManager extends InternalNutsTypedFilters<NutsVersionFilter> implements NutsVersionFilterManager {

//    private static class LocalModel {
//
//        private NutsVersionFilterTrue nutsVersionFilterTrue;
//        private NutsVersionFilterFalse nutsVersionFilterFalse;
//        private NutsWorkspace ws;
//
//        public LocalModel(NutsWorkspace ws) {
//            this.ws = ws;
//        }
//        
//        public NutsVersionFilter always() {
//            if (nutsVersionFilterTrue == null) {
//                nutsVersionFilterTrue = new NutsVersionFilterTrue(ws);
//            }
//            return nutsVersionFilterTrue;
//        }
//
//        public NutsVersionFilter never() {
//            if (nutsVersionFilterFalse == null) {
//                nutsVersionFilterFalse = new NutsVersionFilterFalse(ws);
//            }
//            return nutsVersionFilterFalse;
//        }
//
//    }
//    private final LocalModel localModel;

    public InternalNutsVersionFilterManager(DefaultNutsFilterModel model) {
        super(model, NutsVersionFilter.class);
//        localModel = model.getShared(LocalModel.class, () -> new LocalModel(ws));
    }

    @Override
    public NutsVersionFilterManager setSession(NutsSession session) {
        super.setSession(session);
        return this;
    }

    public NutsVersionFilter byValue(String version) {
        checkSession();
        return DefaultNutsVersionFilter.parse(version, getSession());
    }

    @Override
    public NutsVersionFilter always() {
        checkSession();
        return new NutsVersionFilterTrue(getSession());
    }

    @Override
    public NutsVersionFilter never() {
        checkSession();
        return new NutsVersionFilterFalse(getSession());
    }

    @Override
    public NutsVersionFilter not(NutsFilter other) {
        checkSession();
        return new NutsVersionFilterNone(getSession(), (NutsVersionFilter) other);
    }

    @Override
    public NutsVersionFilter byExpression(String expression) {
        checkSession();
        if (NutsUtilStrings.isBlank(expression)) {
            return always();
        }
        return NutsVersionJavascriptFilter.valueOf(expression, getSession());
    }

    @Override
    public NutsVersionFilter as(NutsFilter a) {
        checkSession();
        if (a instanceof NutsVersionFilter) {
            return (NutsVersionFilter) a;
        }
        return null;
    }

    @Override
    public NutsVersionFilter from(NutsFilter a) {
        checkSession();
        if (a == null) {
            return null;
        }
        NutsVersionFilter t = as(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("not a VersionFilter"));
        }
        return t;
    }

    @Override
    public NutsVersionFilter all(NutsFilter... others) {
        checkSession();
        List<NutsVersionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsVersionFilterAnd(getSession(), all.toArray(new NutsVersionFilter[0]));
    }

    @Override
    public NutsVersionFilter any(NutsFilter... others) {
        checkSession();
        List<NutsVersionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsVersionFilterOr(getSession(), all.toArray(new NutsVersionFilter[0]));
    }

    @Override
    public NutsVersionFilter none(NutsFilter... others) {
        checkSession();
        List<NutsVersionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NutsVersionFilterNone(getSession(), all.toArray(new NutsVersionFilter[0]));
    }

    @Override
    public NutsVersionFilter parse(String expression) {
        checkSession();
        return new NutsVersionFilterParser(expression, getSession()).parse();
    }
}
