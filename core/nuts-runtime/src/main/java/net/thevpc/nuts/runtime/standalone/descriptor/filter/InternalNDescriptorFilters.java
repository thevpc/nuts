package net.thevpc.nuts.runtime.standalone.descriptor.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.filter.*;
import net.thevpc.nuts.runtime.standalone.util.filters.InternalNTypedFilters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NAssert;

public class InternalNDescriptorFilters extends InternalNTypedFilters<NDescriptorFilter>
        implements NDescriptorFilters {


    public InternalNDescriptorFilters(NSession session) {
        super(session, NDescriptorFilter.class);
//        localModel = model.getShared(LocalModel.class, () -> new LocalModel(ws));
    }

    @Override
    public NDescriptorFilter always() {
        checkSession();
        return new NDescriptorFilterTrue(getSession());
    }

    @Override
    public NDescriptorFilter never() {
        checkSession();
        return new NDescriptorFilterFalse(getSession());
    }

    @Override
    public NDescriptorFilter not(NFilter other) {
        checkSession();
        return new NDescriptorFilterNone(getSession(), (NDescriptorFilter) other);
    }

    @Override
    public NDescriptorFilter byPackaging(String... values) {
        checkSession();
        if (values == null || values.length == 0) {
            return always();
        }
        List<NDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NDescriptorFilterPackaging(getSession(), v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NDescriptorFilter[0]));
    }

    @Override
    public NDescriptorFilter byArch(String... values) {
        checkSession();
        if (values == null || values.length == 0) {
            return always();
        }
        List<NDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NDescriptorFilterArch(getSession(), v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NDescriptorFilter[0]));
    }

    @Override
    public NDescriptorFilter byOsDist(String... values) {
        checkSession();
        if (values == null || values.length == 0) {
            return always();
        }
        List<NDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NDescriptorFilterOsDist(getSession(), v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NDescriptorFilter[0]));
    }

    @Override
    public NDescriptorFilter byOs(String... values) {
        checkSession();
        if (values == null || values.length == 0) {
            return always();
        }
        List<NDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NDescriptorFilterOs(getSession(), v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NDescriptorFilter[0]));
    }

    @Override
    public NDescriptorFilter byPlatform(String... values) {
        checkSession();
        if (values == null || values.length == 0) {
            return always();
        }
        List<NDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NDescriptorFilterPlatform(getSession(), v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NDescriptorFilter[0]));
    }

    @Override
    public NDescriptorFilter byDesktopEnvironment(String... values) {
        checkSession();
        if (values == null || values.length == 0) {
            return always();
        }
        List<NDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NDescriptorFilterDesktopEnvironment(getSession(), v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NDescriptorFilter[0]));
    }

    @Override
    public NDescriptorFilter byFlag(NDescriptorFlag... flags) {
        return new NDescriptorFlagsIdFilter(getSession(),flags);
    }

    @Override
    public NDescriptorFilter byExtension(NVersion targetApiVersion) {
        checkSession();
        return new NExecExtensionFilter(getSession(),
                targetApiVersion == null ? null : NId.of(NConstants.Ids.NUTS_API).get(getSession()).builder().setVersion(targetApiVersion).build()
        );
    }

    @Override
    public NDescriptorFilter byRuntime(NVersion targetApiVersion) {
        checkSession();
        return new NExecRuntimeFilter(getSession(),
                targetApiVersion == null ? null : NId.of(NConstants.Ids.NUTS_API).get(getSession()).builder().setVersion(targetApiVersion).build(),
                false
        );
    }

    @Override
    public NDescriptorFilter byCompanion(NVersion targetApiVersion) {
        checkSession();
        return new NExecCompanionFilter(getSession(),
                targetApiVersion == null ? null : NId.of(NConstants.Ids.NUTS_API).get(getSession()).builder().setVersion(targetApiVersion).build(),
                getSession().extensions().getCompanionIds().stream().map(NId::getShortName).toArray(String[]::new)
        );
    }

    @Override
    public NDescriptorFilter byApiVersion(NVersion apiVersion) {
        checkSession();
        if (apiVersion == null) {
            apiVersion = getSession().getWorkspace().getApiVersion();
        }
        return new BootAPINDescriptorFilter(
                getSession(),
                NId.of(NConstants.Ids.NUTS_API).get(getSession()).builder().setVersion(apiVersion).build().getVersion()
        );
    }

    @Override
    public NDescriptorFilter byLockedIds(String... ids) {
        checkSession();
        return new NLockedIdExtensionFilter(getSession(),
                Arrays.stream(ids).map(x -> NId.of(x).get(getSession())).toArray(NId[]::new)
        );
    }

    @Override
    public NDescriptorFilter as(NFilter a) {
        checkSession();
        if (a instanceof NDescriptorFilter) {
            return (NDescriptorFilter) a;
        }
        if (a instanceof NIdFilter) {
            return new NDescriptorFilterById((NIdFilter) a,getSession());
        }
        return null;
    }

    @Override
    public NDescriptorFilter from(NFilter a) {
        checkSession();
        if (a == null) {
            return null;
        }
        NDescriptorFilter t = as(a);
        NSession session = getSession();
        NAssert.requireNonNull(t, "DescriptorFilter", session);
        return t;
    }

    @Override
    public NDescriptorFilter all(NFilter... others) {
        checkSession();
        List<NDescriptorFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NDescriptorFilterAnd(getSession(), all.toArray(new NDescriptorFilter[0]));
    }

    @Override
    public NDescriptorFilter any(NFilter... others) {
        checkSession();
        List<NDescriptorFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NDescriptorFilterOr(getSession(), all.toArray(new NDescriptorFilter[0]));
    }

    @Override
    public NDescriptorFilter none(NFilter... others) {
        checkSession();
        List<NDescriptorFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NDescriptorFilterNone(getSession(), all.toArray(new NDescriptorFilter[0]));
    }

    @Override
    public NDescriptorFilter parse(String expression) {
        checkSession();
        return new NDescriptorFilterParser(expression, getSession()).parse();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NDescriptorFilter byPackaging(List<String> values) {
        return byPackaging(values.toArray(new String[0]));
    }

    @Override
    public NDescriptorFilter byArch(List<String> values) {
        return byArch(values.toArray(new String[0]));
    }

    @Override
    public NDescriptorFilter byOs(List<String> values) {
        return byOs(values.toArray(new String[0]));
    }

    @Override
    public NDescriptorFilter byOsDist(List<String> values) {
        return byOsDist(values.toArray(new String[0]));
    }

    @Override
    public NDescriptorFilter byPlatform(List<String> values) {
        return byPlatform(values.toArray(new String[0]));
    }

    @Override
    public NDescriptorFilter byDesktopEnvironment(List<String> values) {
        return byDesktopEnvironment(values.toArray(new String[0]));
    }

    @Override
    public NDescriptorFilter byFlag(List<NDescriptorFlag> flags) {
        return byFlag(flags.toArray(new NDescriptorFlag[0]));
    }
}
