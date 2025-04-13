package net.thevpc.nuts.runtime.standalone.workspace.cmd.search;

import net.thevpc.nuts.*;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.runtime.standalone.definition.NDefinitionFilterUtils;
import net.thevpc.nuts.runtime.standalone.id.filter.NPatternIdFilter;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NIteratorUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultNSearchInfoBuilder {
    private DefaultNSearchCmd defaultNSearchCmd;

    public DefaultNSearchInfoBuilder(DefaultNSearchCmd defaultNSearchCmd) {
        this.defaultNSearchCmd = defaultNSearchCmd;
    }

    private boolean isIncludedShortName(NId id,Set<String> someIds) {
        for (String o : someIds) {
            if(NBlankable.isBlank(o)) {
                NId c = NId.of(o);
                if(c.getShortName().equals(id.getShortName())) {
                    return true;
                }
            }
        }
        for (String o : someIds) {
            if(NBlankable.isBlank(o)) {
                NId c = NId.of(o);
                if(NBlankable.isBlank(c.getGroupId()) && c.getArtifactId().equals(id.getArtifactId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public DefaultNSearchInfo build() {
        LinkedHashSet<String> someIds = new LinkedHashSet<>();
        for (NId id : defaultNSearchCmd.getIds()) {
            someIds.add(id.toString());
        }
        if (defaultNSearchCmd.isCompanion()) {
            for (NId s : NExtensions.of().getCompanionIds()) {
                if(!isIncludedShortName(s, someIds)) {
                    someIds.add(s.toString());
                }
            }
        }
        if (defaultNSearchCmd.isRuntime()) {
            NId nId = NId.of(NConstants.Ids.NUTS_RUNTIME);
            if(!isIncludedShortName(nId, someIds)) {
                someIds.add(nId.toString());
            }
        }
        Set<DefaultNSearchInfo.RegularId> regularIds = new LinkedHashSet<>();
        HashSet<String> wildcardIds = new HashSet<>();
        for (String someId : someIds) {
            if (NPatternIdFilter.containsWildcard(someId)) {
                wildcardIds.add(someId);
            } else {
                regularIds.add(new DefaultNSearchInfo.RegularId(
                        NId.of(someId),
                        expandRegularIdPossibilities(someId)
                ));
            }
        }
        regularIds.addAll(
                Arrays.stream(NDefinitionFilterUtils.asPatternDefinitionFilterOrList(defaultNSearchCmd.getDefinitionFilter()))
                        .filter(x -> !x.isWildcard())
                        .map(x->new DefaultNSearchInfo.RegularId(
                                x.getId(),
                                expandRegularIdPossibilities(x.getId().toString())
                        )).collect(Collectors.toList())
        );

        NDefinitionFilters dfilter = NDefinitionFilters.of();
        NDefinitionFilter _defFilter = dfilter.always();
        NDependencyFilter depFilter = NDependencyFilters.of().always();
        NRepositoryFilter rfilter = NRepositoryFilters.of().always();
        for (String j : defaultNSearchCmd.getScripts()) {
            if (!NBlankable.isBlank(j)) {
                if (CoreStringUtils.containsTopWord(j, "dependency")) {
                    depFilter = depFilter.and(NDependencyFilters.of().parse(j));
                } else {
                    _defFilter = _defFilter.and(dfilter.parse(j));
                }
            }
        }
        NRepositoryFilter _repositoryFilter = rfilter.and(defaultNSearchCmd.getRepositoryFilter());
        _defFilter = _defFilter.and(defaultNSearchCmd.getDefinitionFilter());

        if (defaultNSearchCmd.getDefaultVersions() != null) {
            _defFilter = _defFilter.and(NDefinitionFilters.of().byDefaultVersion(defaultNSearchCmd.getDefaultVersions()));
        }
        if (defaultNSearchCmd.getExecType() != null) {
            switch (defaultNSearchCmd.getExecType()) {
                case LIB: {
                    _defFilter = _defFilter.and(dfilter.byFlag(NDescriptorFlag.EXEC).neg());
                    break;
                }
                case EXEC: {
                    _defFilter = _defFilter.and(dfilter.byFlag(NDescriptorFlag.EXEC));
                    break;
                }
                case NUTS_APPLICATION: {
                    _defFilter = _defFilter.and(dfilter.byFlag(NDescriptorFlag.NUTS_APP));
                    break;
                }
                case PLATFORM_APPLICATION: {
                    _defFilter = _defFilter.and(dfilter.byFlag(NDescriptorFlag.PLATFORM_APP));
                    break;
                }
                case EXTENSION: {
                    _defFilter = _defFilter.and(dfilter.byExtension(defaultNSearchCmd.getTargetApiVersion()));
                    break;
                }
                case RUNTIME: {
                    _defFilter = _defFilter.and(dfilter.byRuntime(defaultNSearchCmd.getTargetApiVersion()));
                    break;
                }
                case COMPANION: {
                    _defFilter = _defFilter.and(dfilter.byCompanion(defaultNSearchCmd.getTargetApiVersion()));
                    break;
                }
            }
        } else {
            if (defaultNSearchCmd.getTargetApiVersion() != null) {
                _defFilter = _defFilter.and(dfilter.byApiVersion(defaultNSearchCmd.getTargetApiVersion()));
            }
        }
        if (!defaultNSearchCmd.lockedIds.isEmpty()) {
            _defFilter = _defFilter.and(dfilter.byLockedIds(
                    defaultNSearchCmd.lockedIds.stream().map(NId::getFullName).toArray(String[]::new)
            ));
        }
        NRepositoryFilter extraRepositoryFilter = createRepositoryFilter(_defFilter);
        if (extraRepositoryFilter != null) {
            _repositoryFilter = _repositoryFilter.and(extraRepositoryFilter);
        }
        return new DefaultNSearchInfo(
                regularIds.toArray(new DefaultNSearchInfo.RegularId[0]),
                _repositoryFilter,
                _defFilter
        );
    }

    private NRepositoryFilter createRepositoryFilter(NDefinitionFilter _idFilter) {
        Boolean installed = NDefinitionFilterUtils.resolveInstalled(_idFilter).orNull();
        Boolean required = NDefinitionFilterUtils.resolveRequired(_idFilter).orNull();
        Boolean deployed = NDefinitionFilterUtils.resolveRequired(_idFilter).orNull();
        List<NRepositoryFilter> otherFilters = new ArrayList<>();
        if (
                Boolean.TRUE.equals(installed)
                        || Boolean.TRUE.equals(required)
                        || Boolean.TRUE.equals(deployed)
        ) {
            otherFilters.add(NRepositoryFilters.of().installedRepo());
        } else if (
                (Boolean.FALSE.equals(installed) && Boolean.FALSE.equals(required))
                        || Boolean.FALSE.equals(deployed)
        ) {
            otherFilters.add(NRepositoryFilters.of().installedRepo().neg());
        }
        for (NDefinitionFilter nDefinitionFilter : NDefinitionFilterUtils.flattenAnd(_idFilter)) {
            if (nDefinitionFilter instanceof NRepositoryFilter) {
                otherFilters.add((NRepositoryFilter) nDefinitionFilter);
            }
        }

        if (otherFilters.isEmpty()) {
            return null;
        }
        NRepositoryFilter r = otherFilters.get(0);
        for (int i = 1; i < otherFilters.size(); i++) {
            r = r.and(otherFilters.get(i));
        }
        return r;
    }

    private NId[] expandRegularIdPossibilities(String id) {
        NId nutsId = NId.get(id).get();
        Set<NId> nutsId2 = new LinkedHashSet<>();
        if (NBlankable.isBlank(nutsId.getGroupId())) {
            if (nutsId.getArtifactId().equals("nuts")) {
                nutsId2.add(nutsId.builder().setGroupId("net.thevpc.nuts").build());
            } else {
                //check if It's already installed
                List<NId> installedIds = Collections.emptyList();
                if (!nutsId.getArtifactId().contains("*")) {
                    NRepositorySPI repoSPI = NWorkspaceUtils.of()
                            .repoSPI(NWorkspaceExt.of().getInstalledRepository());
                    NIterator<NId> it = repoSPI.search().setFetchMode(NFetchMode.LOCAL).setFilter(NDefinitionFilters.of().byName(
                            nutsId.builder().setGroupId("").build().toString()
                    )).getResult();
                    installedIds = NIteratorUtils.toList(it);
                }
                if (!installedIds.isEmpty()) {
                    nutsId2.addAll(installedIds);
                } else {
                    for (String aImport : NWorkspace.of().getAllImports()) {
                        nutsId2.add(nutsId.builder().setGroupId(aImport).build());
                        nutsId2.add(nutsId.builder().setGroupId(aImport + "." + nutsId.getArtifactId()).build());
                    }
                }
            }
            nutsId2.add(nutsId.builder().setGroupId("").build());
        } else {
            nutsId2.add(nutsId);
        }
        //remove duplicates
        return nutsId2.toArray(new NId[0]);
    }
}
