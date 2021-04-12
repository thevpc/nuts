package net.thevpc.nuts.runtime.core.filters.descriptor;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.InternalNutsTypedFilters;
import net.thevpc.nuts.runtime.core.filters.id.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.thevpc.nuts.runtime.core.filters.DefaultNutsFilterModel;

public class InternalNutsDescriptorFilterManager extends InternalNutsTypedFilters<NutsDescriptorFilter> implements NutsDescriptorFilterManager {

//    private static class LocalModel {
//
//        private NutsDescriptorFilterTrue nutsDescriptorFilterTrue;
//        private NutsDescriptorFilterFalse nutsDescriptorFilterFalse;
//        private NutsWorkspace ws;
//
//        public LocalModel(NutsWorkspace ws) {
//            this.ws = ws;
//        }
//
//        public NutsDescriptorFilter always() {
//            if (nutsDescriptorFilterTrue == null) {
//                nutsDescriptorFilterTrue = new NutsDescriptorFilterTrue(ws);
//            }
//            return nutsDescriptorFilterTrue;
//        }
//
//        public NutsDescriptorFilter never() {
//            if (nutsDescriptorFilterFalse == null) {
//                nutsDescriptorFilterFalse = new NutsDescriptorFilterFalse(ws);
//            }
//            return nutsDescriptorFilterFalse;
//        }
//
//    }
//    private final LocalModel localModel;

    public InternalNutsDescriptorFilterManager(DefaultNutsFilterModel model) {
        super(model, NutsDescriptorFilter.class);
//        localModel = model.getShared(LocalModel.class, () -> new LocalModel(ws));
    }

    @Override
    public InternalNutsDescriptorFilterManager setSession(NutsSession session) {
        super.setSession(session);
        return this;
    }

    @Override
    public NutsDescriptorFilter always() {
        checkSession();
        return new NutsDescriptorFilterTrue(getSession());
    }

    @Override
    public NutsDescriptorFilter never() {
        checkSession();
        return new NutsDescriptorFilterFalse(getSession());
    }

    @Override
    public NutsDescriptorFilter not(NutsFilter other) {
        checkSession();
        return new NutsDescriptorFilterNone(getSession(), (NutsDescriptorFilter) other);
    }

    @Override
    public NutsDescriptorFilter byExpression(String expression) {
        checkSession();
        if (CoreStringUtils.isBlank(expression)) {
            return always();
        }
        return NutsDescriptorJavascriptFilter.valueOf(expression, getSession());
    }

    @Override
    public NutsDescriptorFilter byPackaging(String... values) {
        checkSession();
        if (values == null || values.length == 0) {
            return always();
        }
        List<NutsDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NutsDescriptorFilterPackaging(getSession(), v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter byArch(String... values) {
        checkSession();
        if (values == null || values.length == 0) {
            return always();
        }
        List<NutsDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NutsDescriptorFilterArch(getSession(), v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter byOsdist(String... values) {
        checkSession();
        if (values == null || values.length == 0) {
            return always();
        }
        List<NutsDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NutsDescriptorFilterOsdist(getSession(), v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter byPlatform(String... values) {
        checkSession();
        if (values == null || values.length == 0) {
            return always();
        }
        List<NutsDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NutsDescriptorFilterPlatform(getSession(), v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter byExec(Boolean value) {
        checkSession();
        if (value == null) {
            return always();
        }
        return new NutsExecStatusIdFilter(getSession(), value, null);
    }

    @Override
    public NutsDescriptorFilter byApp(Boolean value) {
        checkSession();
        if (value == null) {
            return always();
        }
        return new NutsExecStatusIdFilter(getSession(), null, value);
    }

    @Override
    public NutsDescriptorFilter byExtension(String targetApiVersion) {
        checkSession();
        return new NutsExecExtensionFilter(getSession(),
                targetApiVersion == null ? null : ws.id().parser().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build()
        );
    }

    @Override
    public NutsDescriptorFilter byRuntime(String targetApiVersion) {
        checkSession();
        return new NutsExecRuntimeFilter(getSession(),
                targetApiVersion == null ? null : ws.id().parser().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build(),
                false
        );
    }

    @Override
    public NutsDescriptorFilter byCompanion(String targetApiVersion) {
        checkSession();
        return new NutsExecCompanionFilter(getSession(),
                targetApiVersion == null ? null : ws.id().parser().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build(),
                ws.getCompanionIds(getSession()).stream().map(NutsId::getShortName).toArray(String[]::new)
        );
    }

    @Override
    public NutsDescriptorFilter byApiVersion(String apiVersion) {
        checkSession();
        if (apiVersion == null) {
            apiVersion = getSession().getWorkspace().getApiVersion();
        }
        return new BootAPINutsDescriptorFilter(
                getSession(),
                getSession().getWorkspace().id().parser().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(apiVersion).build().getVersion()
        );
    }

    @Override
    public NutsDescriptorFilter byLockedIds(String... ids) {
        checkSession();
        return new NutsLockedIdExtensionFilter(getSession(),
                Arrays.stream(ids).map(x -> ws.id().parser().setLenient(false).parse(x)).toArray(NutsId[]::new)
        );
    }

    @Override
    public NutsDescriptorFilter as(NutsFilter a) {
        checkSession();
        if (a instanceof NutsDescriptorFilter) {
            return (NutsDescriptorFilter) a;
        }
        if (a instanceof NutsIdFilter) {
            return new NutsDescriptorFilterById((NutsIdFilter) a,getSession());
        }
        return null;
    }

    @Override
    public NutsDescriptorFilter from(NutsFilter a) {
        checkSession();
        if (a == null) {
            return null;
        }
        NutsDescriptorFilter t = as(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(getSession(), "not a DescriptorFilter");
        }
        return t;
    }

    @Override
    public NutsDescriptorFilter all(NutsFilter... others) {
        checkSession();
        List<NutsDescriptorFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsDescriptorFilterAnd(getSession(), all.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter any(NutsFilter... others) {
        checkSession();
        List<NutsDescriptorFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsDescriptorFilterOr(getSession(), all.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter none(NutsFilter... others) {
        checkSession();
        List<NutsDescriptorFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NutsDescriptorFilterNone(getSession(), all.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter parse(String expression) {
        checkSession();
        return new NutsDescriptorFilterParser(expression, getSession()).parse();
    }
}
