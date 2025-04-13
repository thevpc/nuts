package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.runtime.standalone.util.filters.InternalNTypedFilters;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NFilter;

import java.util.*;
import java.util.stream.Collectors;

public class InternalNDefinitionFilters extends InternalNTypedFilters<NDefinitionFilter>
        implements NDefinitionFilters {


    public InternalNDefinitionFilters(NWorkspace workspace) {
        super(NDefinitionFilter.class);
//        localModel = model.getShared(LocalModel.class, () -> new LocalModel(ws));
    }

    @Override
    public NDefinitionFilter always() {
        return new NDefinitionFilterTrue();
    }

    @Override
    public NDefinitionFilter never() {
        return new NDefinitionFilterFalse();
    }

    @Override
    public NDefinitionFilter not(NFilter other) {
        return new NDefinitionFilterNone((NDefinitionFilter) other);
    }

    @Override
    public NDefinitionFilter byPackaging(String... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        List<NDefinitionFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NDefinitionFilterPackaging(v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NDefinitionFilter[0]));
    }

    @Override
    public NDefinitionFilter byArch(String... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        List<NDefinitionFilter> packs = new ArrayList<>();
        for (String v : values) {
            packs.add(new NDefinitionFilterArch(v));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NDefinitionFilter[0]));
    }

    @Override
    public NDefinitionFilter byArch(NArchFamily... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        List<NDefinitionFilter> packs = new ArrayList<>();
        for (NArchFamily v : values) {
            packs.add(new NDefinitionFilterArch(v.id()));
        }
        if (packs.size() == 1) {
            return packs.get(0);
        }
        return all(packs.toArray(new NDefinitionFilter[0]));
    }

    @Override
    public NDefinitionFilter byOsDist(String... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        return new NDefinitionOsDistIdFilter(Arrays.stream(values).map(x -> x == null ? null : NId.of(x)).collect(Collectors.toList()));
    }

    @Override
    public NDefinitionFilter byOsFamily(NOsFamily... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        return new NDefinitionOsFamilyFilter(Arrays.asList(values));
    }

    @Override
    public NDefinitionFilter byArchFamily(NArchFamily... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        return new NDefinitionArchFamilyFilter(Arrays.asList(values));
    }

    @Override
    public NDefinitionFilter byOs(String... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        return new NDefinitionOsIdFilter(
                Arrays.stream(values).map(x -> x == null ? null : NId.get(x).orNull()).filter(x -> x != null).collect(Collectors.toList())
        );
    }

    @Override
    public NDefinitionFilter byOs(NId... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        return new NDefinitionOsIdFilter(
                Arrays.stream(values).filter(x -> x != null).collect(Collectors.toList())
        );
    }


    @Override
    public NDefinitionFilter byPlatform(NId... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        return new NDefinitionPlatformIdFilter(
                Arrays.stream(values).filter(x -> x != null).collect(Collectors.toList())
        );
    }


    @Override
    public NDefinitionFilter byPlatformFamily(NPlatformFamily... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        return new NDefinitionPlatformFamilyFilter(
                Arrays.stream(values).filter(x -> x != null).collect(Collectors.toList())
        );
    }


    @Override
    public NDefinitionFilter byPlatform(String... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        return new NDefinitionPlatformIdFilter(
                Arrays.stream(values).map(x -> x == null ? null : NId.get(x).orNull()).collect(Collectors.toList())
        );
    }

    @Override
    public NDefinitionFilter byDesktopEnvironmentFamily(NDesktopEnvironmentFamily... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        return new NDefinitionDesktopEnvironmentFamilyFilter(
                Arrays.stream(values).collect(Collectors.toList())
        );
    }

    @Override
    public NDefinitionFilter byDesktopEnvironment(String... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        return new NDefinitionDesktopEnvironmentIdFilter(
                Arrays.stream(values).map(x -> x == null ? null : NId.get(x).orNull()).collect(Collectors.toList())
        );
    }

    @Override
    public NDefinitionFilter byDesktopEnvironment(NId... values) {
        if (values == null || values.length == 0) {
            return always();
        }
        return new NDefinitionDesktopEnvironmentIdFilter(
                Arrays.stream(values).collect(Collectors.toList())
        );
    }

    @Override
    public NDefinitionFilter byExtension(NVersion targetApiVersion) {
        return new NDefinitionExecExtensionFilter(
                targetApiVersion == null ? null : NId.get(NConstants.Ids.NUTS_API).get().builder().setVersion(targetApiVersion).build()
        );
    }

    @Override
    public NDefinitionFilter byRuntime(NVersion targetApiVersion) {
        return new NDefinitionExecRuntimeFilter(
                targetApiVersion == null ? null : NId.get(NConstants.Ids.NUTS_API).get().builder().setVersion(targetApiVersion).build(),
                false
        );
    }

    @Override
    public NDefinitionFilter byCompanion(NVersion targetApiVersion) {
        return new NDefinitionExecCompanionFilter(
                targetApiVersion == null ? null : NId.get(NConstants.Ids.NUTS_API).get().builder().setVersion(targetApiVersion).build(),
                NExtensions.of().getCompanionIds().stream().map(NId::getShortName).toArray(String[]::new)
        );
    }

    @Override
    public NDefinitionFilter byApiVersion(NVersion apiVersion) {
        if (apiVersion == null) {
            apiVersion = NWorkspace.of().getApiVersion();
        }
        return new NutsAPIDefinitionFilter(
                apiVersion
        );
    }

    @Override
    public NDefinitionFilter byBootVersion(NVersion bootVersion) {
        if (bootVersion == null) {
            bootVersion = NWorkspace.of().getBootVersion();
        }
        return new NutsBootNDefinitionFilter(
                bootVersion
        );
    }

    @Override
    public NDefinitionFilter byLockedIds(String... ids) {
        return new NLockedIdExtensionDefinitionFilter(
                Arrays.stream(ids).filter(NBlankable::isNonBlank).map(x -> NId.get(x).get()).toArray(NId[]::new)
        );
    }

    @Override
    public NDefinitionFilter byLockedIds(NId... ids) {
        return new NLockedIdExtensionDefinitionFilter(
                Arrays.stream(ids).filter(Objects::nonNull).toArray(NId[]::new)
        );
    }

    @Override
    public NDefinitionFilter byVersion(String version) {
        return new NDefinitionFilterByVersion(version);
    }

    @Override
    public NDefinitionFilter byVersion(NVersion version) {
        return new NDefinitionFilterByMapVersion(version.filter());
    }

    @Override
    public NDefinitionFilter byVersion(NVersionFilter version) {
        return new NDefinitionFilterByMapVersion(version);
    }

    @Override
    public NDefinitionFilter as(NFilter a) {
        if (a instanceof NDefinitionFilter) {
            return (NDefinitionFilter) a;
        }
        if (a instanceof NIdFilter) {
            return new NDefinitionFilterById((NIdFilter) a);
        }
        if (a instanceof NVersionFilter) {
            return new NDefinitionFilterByMapVersion((NVersionFilter) a);
        }
        return null;
    }

    @Override
    public NDefinitionFilter from(NFilter a) {
        if (a == null) {
            return null;
        }
        NDefinitionFilter t = as(a);
        NAssert.requireNonNull(t, "DefinitionFilter");
        return t;
    }

    @Override
    public NDefinitionFilter all(NFilter... others) {
        List<NDefinitionFilter> all = convertList(others);
        for (int i = all.size() - 1; i >= 0; i--) {
            NDefinitionFilter c = (NDefinitionFilter) all.get(i).simplify();
            if (c != null) {
                if (c.equals(always())) {
                    if (all.size() > 1) {
                        all.remove(i);
                    }
                } else if (c.equals(never())) {
                    return never();
                }
            } else {
                all.remove(i);
            }
        }
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NDefinitionFilterAnd(all.toArray(new NDefinitionFilter[0]));
    }

    @Override
    public NDefinitionFilter any(NFilter... others) {
        List<NDefinitionFilter> all = convertList(others);
        for (int i = all.size() - 1; i >= 0; i--) {
            NDefinitionFilter c = (NDefinitionFilter) all.get(i).simplify();
            if (c != null) {
                if (c.equals(never())) {
                    if (all.size() > 1) {
                        all.remove(i);
                    }
                } else if (c.equals(always())) {
                    return always();
                }
            } else {
                all.remove(i);
            }
        }
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NDefinitionFilterOr(all.toArray(new NDefinitionFilter[0]));
    }

    @Override
    public NDefinitionFilter none(NFilter... others) {
        List<NDefinitionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NDefinitionFilterNone(all.toArray(new NDefinitionFilter[0]));
    }

    @Override
    public NDefinitionFilter parse(String expression) {
        return new NDefinitionFilterParser(expression).parse();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NDefinitionFilter byPackaging(Collection<String> values) {
        return byPackaging(values.toArray(new String[0]));
    }

    @Override
    public NDefinitionFilter byArch(Collection<NArchFamily> values) {
        return byArch(values.toArray(new NArchFamily[0]));
    }

    @Override
    public NDefinitionFilter byOsFamily(Collection<NOsFamily> values) {
        return byOsFamily(values.toArray(new NOsFamily[0]));
    }

    @Override
    public NDefinitionFilter byOsDist(Collection<String> values) {
        return byOsDist(values.toArray(new String[0]));
    }

    @Override
    public NDefinitionFilter byPlatform(Collection<String> values) {
        return byPlatform(values.toArray(new String[0]));
    }

    @Override
    public NDefinitionFilter byDesktopEnvironment(Collection<String> values) {
        return byDesktopEnvironment(values.toArray(new String[0]));
    }

    @Override
    public NDefinitionFilter byFlag(Collection<NDescriptorFlag> flags) {
        return byFlag(flags.toArray(new NDescriptorFlag[0]));
    }

    @Override
    public NDefinitionFilter byFlag(NDescriptorFlag... flags) {
        return new NDefinitionFlagsIdFilter(false, flags);
    }

    @Override
    public NDefinitionFilter byEffectiveFlag(Collection<NDescriptorFlag> flags) {
        return byEffectiveFlag(flags.toArray(new NDescriptorFlag[0]));
    }

    @Override
    public NDefinitionFilter byEffectiveFlag(NDescriptorFlag... flags) {
        return new NDefinitionFlagsIdFilter(true, flags);
    }

    @Override
    public NDefinitionFilter byCurrentDesktopEnvironmentFamily() {
        return byDesktopEnvironmentFamily(NWorkspace.of().getDesktopEnvironmentFamilies().toArray(new NDesktopEnvironmentFamily[0]));
    }

    public NDefinitionFilter byCurrentArch() {
        return byArch(NWorkspace.of().getArchFamily());
    }

    @Override
    public NDefinitionFilter byCurrentOsFamily() {
        return byOsFamily(NWorkspace.of().getOsFamily());
    }

    public NDefinitionFilter byCurrentEnv() {
        return byCurrentOsFamily()
                .and(byCurrentArch())
                .and(byCurrentDesktopEnvironmentFamily())
                ;
    }

    @Override
    public NDefinitionFilter byDefaultVersion(Boolean defaultVersion) {
        if (defaultVersion == null) {
            return always();
        }
        return new NDefaultVersionDefinitionFilter(defaultVersion);
    }

    @Override
    public NDefinitionFilter byName(String... names) {
        if (names == null || names.length == 0) {
            return always();
        }
        NDefinitionFilter f = null;
        for (String wildcardId : names) {
            if (f == null) {
                f = new NPatternDefinitionFilter(NId.get(wildcardId).get());
            } else {
                f = f.or(new NPatternDefinitionFilter(NId.get(wildcardId).get()));
            }
        }
        if (f == null) {
            return always();
        }
        return f;
    }

    @Override
    public NDefinitionFilter byEnv(Map<String, String> faceMap) {
        return byArch(faceMap.get(NConstants.IdProperties.ARCH))
                .and(byOs(faceMap.get(NConstants.IdProperties.OS)))
                .and(byOsDist(faceMap.get(NConstants.IdProperties.OS_DIST)))
                .and(byPlatform(faceMap.get(NConstants.IdProperties.PLATFORM)))
                .and(byDesktopEnvironment(faceMap.get(NConstants.IdProperties.DESKTOP)))
                ;
    }

    @Override
    public NDefinitionFilter byInstalled(boolean value) {
        return NInstallStatusDefinitionFilter2.ofInstalled(value);
    }

    @Override
    public NDefinitionFilter byInstalledOrRequired(boolean value) {
        return NInstallStatusDefinitionFilter2.ofInstalledOrRequired(value);
    }

    @Override
    public NDefinitionFilter byRequired(boolean value) {
        return NInstallStatusDefinitionFilter2.ofRequired(value);
    }

    @Override
    public NDefinitionFilter byDefaultValue(boolean value) {
        return NInstallStatusDefinitionFilter2.ofDeployed(value);
    }

    @Override
    public NDefinitionFilter byObsolete(boolean value) {
        return NInstallStatusDefinitionFilter2.ofObsolete(value);
    }

    @Override
    public NDefinitionFilter byDeployed(boolean value) {
        return NInstallStatusDefinitionFilter2.ofDeployed(value);
    }
}
