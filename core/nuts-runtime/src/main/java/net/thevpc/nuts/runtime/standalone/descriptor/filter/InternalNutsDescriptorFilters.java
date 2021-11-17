package net.thevpc.nuts.runtime.standalone.descriptor.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.filter.*;
import net.thevpc.nuts.runtime.standalone.util.filters.InternalNutsTypedFilters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class InternalNutsDescriptorFilters extends InternalNutsTypedFilters<NutsDescriptorFilter>
        implements NutsDescriptorFilters {


    public InternalNutsDescriptorFilters(NutsSession session) {
        super(session, NutsDescriptorFilter.class);
//        localModel = model.getShared(LocalModel.class, () -> new LocalModel(ws));
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
    public NutsDescriptorFilter byOsDist(String... values) {
        checkSession();
        if (values == null || values.length == 0) {
            return always();
        }
        List<NutsDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NutsDescriptorFilterOsDist(getSession(), v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter byOs(String... values) {
        checkSession();
        if (values == null || values.length == 0) {
            return always();
        }
        List<NutsDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NutsDescriptorFilterOs(getSession(), v));
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
    public NutsDescriptorFilter byDesktopEnvironment(String... values) {
        checkSession();
        if (values == null || values.length == 0) {
            return always();
        }
        List<NutsDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NutsDescriptorFilterDesktopEnvironment(getSession(), v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter byFlag(NutsDescriptorFlag... flags) {
        return new NutsDescriptorFlagsIdFilter(getSession(),flags);
    }

    @Override
    public NutsDescriptorFilter byExtension(NutsVersion targetApiVersion) {
        checkSession();
        return new NutsExecExtensionFilter(getSession(),
                targetApiVersion == null ? null : NutsId.of(NutsConstants.Ids.NUTS_API,getSession()).builder().setVersion(targetApiVersion).build()
        );
    }

    @Override
    public NutsDescriptorFilter byRuntime(NutsVersion targetApiVersion) {
        checkSession();
        return new NutsExecRuntimeFilter(getSession(),
                targetApiVersion == null ? null : NutsId.of(NutsConstants.Ids.NUTS_API,getSession()).builder().setVersion(targetApiVersion).build(),
                false
        );
    }

    @Override
    public NutsDescriptorFilter byCompanion(NutsVersion targetApiVersion) {
        checkSession();
        return new NutsExecCompanionFilter(getSession(),
                targetApiVersion == null ? null : NutsId.of(NutsConstants.Ids.NUTS_API,getSession()).builder().setVersion(targetApiVersion).build(),
                getSession().extensions().getCompanionIds().stream().map(NutsId::getShortName).toArray(String[]::new)
        );
    }

    @Override
    public NutsDescriptorFilter byApiVersion(NutsVersion apiVersion) {
        checkSession();
        if (apiVersion == null) {
            apiVersion = getSession().getWorkspace().getApiVersion();
        }
        return new BootAPINutsDescriptorFilter(
                getSession(),
                NutsId.of(NutsConstants.Ids.NUTS_API,getSession()).builder().setVersion(apiVersion).build().getVersion()
        );
    }

    @Override
    public NutsDescriptorFilter byLockedIds(String... ids) {
        checkSession();
        return new NutsLockedIdExtensionFilter(getSession(),
                Arrays.stream(ids).map(x -> NutsId.of(x,getSession())).toArray(NutsId[]::new)
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
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("not a DescriptorFilter"));
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

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
