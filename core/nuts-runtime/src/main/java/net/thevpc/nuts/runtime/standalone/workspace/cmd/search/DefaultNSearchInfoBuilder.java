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

        NDefinitionFilters d = NDefinitionFilters.of();
        NDefinitionFilter _defFilter = d.always().and(defaultNSearchCmd.getDefinitionFilter());

        if (defaultNSearchCmd.getExecType() != null) {
            switch (defaultNSearchCmd.getExecType()) {
                case LIB: {
                    _defFilter = _defFilter.and(d.byFlag(NDescriptorFlag.EXEC).neg());
                    break;
                }
                case EXEC: {
                    _defFilter = _defFilter.and(d.byFlag(NDescriptorFlag.EXEC));
                    break;
                }
                case NUTS_APPLICATION: {
                    _defFilter = _defFilter.and(d.byFlag(NDescriptorFlag.NUTS_APP));
                    break;
                }
                case PLATFORM_APPLICATION: {
                    _defFilter = _defFilter.and(d.byFlag(NDescriptorFlag.PLATFORM_APP));
                    break;
                }
                case EXTENSION: {
                    _defFilter = _defFilter.and(d.byExtension(defaultNSearchCmd.getTargetApiVersion()));
                    break;
                }
                case RUNTIME: {
                    _defFilter = _defFilter.and(d.byRuntime(defaultNSearchCmd.getTargetApiVersion()));
                    break;
                }
                case COMPANION: {
                    _defFilter = _defFilter.and(d.byCompanion(defaultNSearchCmd.getTargetApiVersion()));
                    break;
                }
            }
        } else {
            if (defaultNSearchCmd.getTargetApiVersion() != null) {
                _defFilter = _defFilter.and(d.byApiVersion(defaultNSearchCmd.getTargetApiVersion()));
            }
        }
        return new DefaultNSearchInfo(
                regularIds.toArray(new DefaultNSearchInfo.RegularId[0]),
                NRepositoryFilters.of().always()
                        .and(defaultNSearchCmd.getRepositoryFilter())
                        .and(NDefinitionFilterUtils.toRepositoryFilter(_defFilter)),
                _defFilter
        );
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
                        nutsId2.add(nutsId.builder().setGroupId(aImport + "." + nutsId.getArtifactId()).build());
                        nutsId2.add(nutsId.builder().setGroupId(aImport).build());
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
