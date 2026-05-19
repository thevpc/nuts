package net.thevpc.nuts.runtime.standalone.workspace.cmd.search;

import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.artifact.NDefinitionFilters;
import net.thevpc.nuts.artifact.NDescriptorFlag;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NFetchMode;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.core.NRepositoryFilters;
import net.thevpc.nuts.runtime.standalone.definition.NDefinitionFilterUtils;
import net.thevpc.nuts.runtime.standalone.id.filter.NPatternIdFilter;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.runtime.standalone.util.collections.NIteratorUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultNSearchInfoBuilder {
    private DefaultNSearch defaultNSearchCmd;

    public DefaultNSearchInfoBuilder(DefaultNSearch defaultNSearchCmd) {
        this.defaultNSearchCmd = defaultNSearchCmd;
    }

    private boolean isIncludedShortName(NId id,Set<String> someIds) {
        for (String o : someIds) {
            if(NBlankable.isBlank(o)) {
                NId c = NId.of(o);
                if(c.shortName().equals(id.shortName())) {
                    return true;
                }
            }
        }
        for (String o : someIds) {
            if(NBlankable.isBlank(o)) {
                NId c = NId.of(o);
                if(NBlankable.isBlank(c.groupId()) && c.artifactId().equals(id.artifactId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public DefaultNSearchInfo build() {
        LinkedHashSet<String> someIds = new LinkedHashSet<>();
        for (NId id : defaultNSearchCmd.ids()) {
            someIds.add(id.toString());
        }
        if (defaultNSearchCmd.isCompanion()) {
            for (NId s : NExtensions.of().companionIds()) {
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
                Arrays.stream(NDefinitionFilterUtils.asPatternDefinitionFilterOrList(defaultNSearchCmd.definitionFilter()))
                        .filter(x -> !x.isWildcard())
                        .map(x->new DefaultNSearchInfo.RegularId(
                                x.getId(),
                                expandRegularIdPossibilities(x.getId().toString())
                        )).collect(Collectors.toList())
        );

        NDefinitionFilters d = NDefinitionFilters.of();
        NDefinitionFilter _defFilter = d.always().and(defaultNSearchCmd.definitionFilter());

        if (defaultNSearchCmd.execType() != null) {
            switch (defaultNSearchCmd.execType()) {
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
                    _defFilter = _defFilter.and(d.byExtension(defaultNSearchCmd.targetApiVersion()));
                    break;
                }
                case RUNTIME: {
                    _defFilter = _defFilter.and(d.byRuntime(defaultNSearchCmd.targetApiVersion()));
                    break;
                }
                case COMPANION: {
                    _defFilter = _defFilter.and(d.byCompanion(defaultNSearchCmd.targetApiVersion()));
                    break;
                }
            }
        } else {
            if (defaultNSearchCmd.targetApiVersion() != null) {
                _defFilter = _defFilter.and(d.byApiVersion(defaultNSearchCmd.targetApiVersion()));
            }
        }
        return new DefaultNSearchInfo(
                regularIds.toArray(new DefaultNSearchInfo.RegularId[0]),
                NRepositoryFilters.of().always()
                        .and(defaultNSearchCmd.repositoryFilter())
                        .and(NDefinitionFilterUtils.toRepositoryFilter(_defFilter)),
                _defFilter
        );
    }


    private NId[] expandRegularIdPossibilities(String id) {
        NId nutsId = NId.get(id).get();
        Set<NId> nutsId2 = new LinkedHashSet<>();
        if (NBlankable.isBlank(nutsId.groupId())) {
            if (nutsId.artifactId().equals("nuts")) {
                nutsId2.add(nutsId.builder().groupId("net.thevpc.nuts").build());
            } else {
                //check if It's already installed
                List<NId> installedIds = Collections.emptyList();
                if (!nutsId.artifactId().contains("*")) {
                    NRepositorySPI repoSPI = NWorkspaceUtils.of()
                            .toRepositorySPI(NWorkspaceExt.of().getInstalledRepository());
                    NIterator<NId> it = repoSPI.search().setFetchMode(NFetchMode.LOCAL).setFilter(NDefinitionFilters.of().byName(
                            nutsId.builder().groupId("").build().toString()
                    )).getResult();
                    installedIds = NIteratorUtils.toList(it);
                }
                if (!installedIds.isEmpty()) {
                    nutsId2.addAll(installedIds);
                } else {
                    for (String aImport : NWorkspace.of().allImports()) {
                        nutsId2.add(nutsId.builder().groupId(aImport + "." + nutsId.artifactId()).build());
                        nutsId2.add(nutsId.builder().groupId(aImport).build());
                    }
                }
            }
            nutsId2.add(nutsId.builder().groupId("").build());
        } else {
            nutsId2.add(nutsId);
        }
        //remove duplicates
        return nutsId2.toArray(new NId[0]);
    }
}
