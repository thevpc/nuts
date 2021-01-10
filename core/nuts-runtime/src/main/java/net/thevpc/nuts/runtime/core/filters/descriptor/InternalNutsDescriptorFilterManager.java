package net.thevpc.nuts.runtime.core.filters.descriptor;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.DefaultNutsFilterManager;
import net.thevpc.nuts.runtime.core.filters.InternalNutsTypedFilters;
import net.thevpc.nuts.runtime.core.filters.dependency.NutsDependencyFilterAnd;
import net.thevpc.nuts.runtime.core.filters.descriptor.*;
import net.thevpc.nuts.runtime.core.filters.id.*;
import net.thevpc.nuts.runtime.core.filters.installstatus.NutsInstallStatusFilterParser;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InternalNutsDescriptorFilterManager extends InternalNutsTypedFilters<NutsDescriptorFilter> implements NutsDescriptorFilterManager {
    private final DefaultNutsFilterManager defaultNutsFilterManager;
    private NutsDescriptorFilterTrue nutsDescriptorFilterTrue;
    private NutsDescriptorFilterFalse nutsDescriptorFilterFalse;

    public InternalNutsDescriptorFilterManager(DefaultNutsFilterManager defaultNutsFilterManager) {
        super(defaultNutsFilterManager, NutsDescriptorFilter.class);
        this.defaultNutsFilterManager = defaultNutsFilterManager;
    }

    @Override
    public NutsDescriptorFilter always() {
        if (nutsDescriptorFilterTrue == null) {
            nutsDescriptorFilterTrue = new NutsDescriptorFilterTrue(ws);
        }
        return nutsDescriptorFilterTrue;
    }

    @Override
    public NutsDescriptorFilter never() {
        if (nutsDescriptorFilterFalse == null) {
            nutsDescriptorFilterFalse = new NutsDescriptorFilterFalse(ws);
        }
        return nutsDescriptorFilterFalse;
    }

    @Override
    public NutsDescriptorFilter not(NutsFilter other) {
        return new NutsDescriptorFilterNone(ws, (NutsDescriptorFilter) other);
    }

    @Override
    public NutsDescriptorFilter byExpression(String expression) {
        if (CoreStringUtils.isBlank(expression)) {
            return always();
        }
        return NutsDescriptorJavascriptFilter.valueOf(expression, ws);
    }

    @Override
    public NutsDescriptorFilter byPackaging(String... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        List<NutsDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NutsDescriptorFilterPackaging(ws, v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter byArch(String... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        List<NutsDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NutsDescriptorFilterArch(ws, v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter byOsdist(String... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        List<NutsDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NutsDescriptorFilterOsdist(ws, v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter byPlatform(String... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        List<NutsDescriptorFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NutsDescriptorFilterPlatform(ws, v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter byExec(Boolean value) {
        if (value == null) {
            return always();
        }
        return new NutsExecStatusIdFilter(ws, value, null);
    }

    @Override
    public NutsDescriptorFilter byApp(Boolean value) {
        if (value == null) {
            return always();
        }
        return new NutsExecStatusIdFilter(ws, null, value);
    }

    @Override
    public NutsDescriptorFilter byExtension(String targetApiVersion) {
        return new NutsExecExtensionFilter(ws,
                targetApiVersion == null ? null : ws.id().parser().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build()
        );
    }

    @Override
    public NutsDescriptorFilter byRuntime(String targetApiVersion) {
        return new NutsExecRuntimeFilter(ws,
                targetApiVersion == null ? null : ws.id().parser().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build(),
                false
        );
    }

    @Override
    public NutsDescriptorFilter byCompanion(String targetApiVersion) {
        return new NutsExecCompanionFilter(ws,
                targetApiVersion == null ? null : ws.id().parser().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build(),
                ws.getCompanionIds().stream().map(NutsId::getShortName).toArray(String[]::new)
        );
    }

    @Override
    public NutsDescriptorFilter byApiVersion(String apiVersion) {
        if (apiVersion == null) {
            apiVersion = ws.getApiVersion();
        }
        return new BootAPINutsDescriptorFilter(
                ws,
                ws.id().parser().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(apiVersion).build().getVersion()
        );
    }

    @Override
    public NutsDescriptorFilter byLockedIds(String... ids) {
        return new NutsLockedIdExtensionFilter(ws,
                Arrays.stream(ids).map(x -> ws.id().parser().setLenient(false).parse(x)).toArray(NutsId[]::new)
        );
    }

    @Override
    public NutsDescriptorFilter as(NutsFilter a) {
        if (a instanceof NutsDescriptorFilter) {
            return (NutsDescriptorFilter) a;
        }
        if (a instanceof NutsIdFilter) {
            return new NutsDescriptorFilterById((NutsIdFilter) a);
        }
        return null;
    }

    @Override
    public NutsDescriptorFilter from(NutsFilter a) {
        if (a == null) {
            return null;
        }
        NutsDescriptorFilter t = as(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(ws, "not a DescriptorFilter");
        }
        return t;
    }

    @Override
    public NutsDescriptorFilter all(NutsFilter... others) {
        List<NutsDescriptorFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsDescriptorFilterAnd(ws, all.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter any(NutsFilter... others) {
        List<NutsDescriptorFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsDescriptorFilterOr(ws, all.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter none(NutsFilter... others) {
        List<NutsDescriptorFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NutsDescriptorFilterNone(ws, all.toArray(new NutsDescriptorFilter[0]));
    }

    @Override
    public NutsDescriptorFilter parse(String expression) {
        return new NutsDescriptorFilterParser(expression, ws).parse();
    }
}
