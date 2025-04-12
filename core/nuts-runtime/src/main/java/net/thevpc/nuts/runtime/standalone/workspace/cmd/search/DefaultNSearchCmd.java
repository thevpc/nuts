/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.search;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;


import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.runtime.standalone.definition.NDefinitionHelper;
import net.thevpc.nuts.runtime.standalone.definition.filter.NDefinitionFilterOr;
import net.thevpc.nuts.runtime.standalone.definition.filter.NPatternDefinitionFilter;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.id.filter.NPatternIdFilter;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.util.NIteratorBuilder;
import net.thevpc.nuts.util.NIteratorUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch.DefaultNFetchCmd;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NRepositoryAndFetchMode;
import net.thevpc.nuts.spi.NRepositorySPI;

import java.util.*;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;

/**
 * @author thevpc
 */
public class DefaultNSearchCmd extends AbstractNSearchCmd {

    public DefaultNSearchCmd() {
        super();
    }

    @Override
    public NSearchCmd copy() {
        DefaultNSearchCmd b = new DefaultNSearchCmd();
        b.copyFrom(this);
        return b;
    }

    @Override
    public NFetchCmd toFetch() {
        NFetchCmd t = new DefaultNFetchCmd().copyFromDefaultNQueryBaseOptions(this);
        t.setIgnoreCurrentEnvironment(isIgnoreCurrentEnvironment());
        if (getDisplayOptions().isRequireDefinition()) {
            t.setContent(true);
        }
        //update RepositoryFilter with effective one that takes into consideration
        // id filters and status filters
        DefaultNSearchInfo bs = new DefaultNSearchInfoBuilder(this).build();
        t.setRepositoryFilter(bs.getRepositoryFilter());
        return t;
    }


    //@Override


    public NIterator<NId> getResultIdIteratorBase(Boolean forceInlineDependencies) {
        boolean inlineDependencies = forceInlineDependencies == null ? isInlineDependencies() : forceInlineDependencies;
        DefaultNSearchInfo search = new DefaultNSearchInfoBuilder(this).build();

        List<NIterator<? extends NId>> allResults = new ArrayList<>();
        NSession session = NSession.of();
        NRepositoryFilter sRepositoryFilter = search.getRepositoryFilter();
        DefaultNSearchInfo.RegularId[] regularIds = search.getRegularIds();
        NFetchStrategy fetchMode = NWorkspaceHelper.validate(session.getFetchStrategy().orDefault());
        Set<NRepository> consideredRepos = new HashSet<>();
        NWorkspaceUtils wu = NWorkspaceUtils.of();
        NElements elems = NElements.of();
        if (regularIds.length > 0) {
            for (DefaultNSearchInfo.RegularId rid : regularIds) {
                NId[] nutsId2 = rid.expandedIds;

                List<NIterator<? extends NId>> toConcat = new ArrayList<>();
                for (NId nutsId1 : nutsId2) {
                    if (NDependencyScope.parse(nutsId1.toDependency().getScope()).orNull() == NDependencyScope.SYSTEM) {
                        // TODO, fix me
                        //just ignore or should we still support it?
                    } else {
                        NId nutsIdNonLatest = nutsId1;
                        boolean latestVersion = false;
                        boolean releaseVersion = false;
                        if (nutsIdNonLatest.getVersion().isLatestVersion()) {
                            latestVersion = true;
                            nutsIdNonLatest = nutsIdNonLatest.builder().setVersion("").build();
                        } else if (nutsIdNonLatest.getVersion().isReleaseVersion()) {
                            releaseVersion = true;
                            nutsIdNonLatest = nutsIdNonLatest.builder().setVersion("").build();
                        }
                        NDefinitionFilters dd = NDefinitionFilters.of();
                        NDefinitionFilter filter = (
                                dd.byName(nutsIdNonLatest.getFullName())
                                        .and(dd.byEnv(nutsIdNonLatest.getProperties()))
                                        .and(search.getDefinitionFilter())
                        );

                        List<NRepositoryAndFetchMode> repositoryAndFetchModes = wu.filterRepositoryAndFetchModes(
                                NRepositorySupportedAction.SEARCH, nutsIdNonLatest, sRepositoryFilter, fetchMode
                        );

                        List<NIterator<? extends NId>> idLocal = new ArrayList<>();
                        List<NIterator<? extends NId>> idRemote = new ArrayList<>();
                        for (NFetchMode fm : new NFetchMode[]{NFetchMode.LOCAL, NFetchMode.REMOTE}) {
                            List<NIterator<? extends NId>> idLookup = fm == NFetchMode.LOCAL ? idLocal : idRemote;
                            for (NRepositoryAndFetchMode repoAndMode : repositoryAndFetchModes) {
                                if (repoAndMode.getFetchMode() == fm) {
                                    consideredRepos.add(repoAndMode.getRepository());
                                    NRepositorySPI repoSPI = wu.repoSPI(repoAndMode.getRepository());

                                    NIterator<NId> z = NIteratorBuilder.of(repoSPI.searchVersions().setId(nutsIdNonLatest).setFilter(filter)
                                                    .setFetchMode(repoAndMode.getFetchMode())
                                                    .getResult())
                                            .named(
                                                    elems.ofObjectBuilder()
                                                            .set("description", "searchVersions")
                                                            .set("repository", repoAndMode.getRepository().getName())
                                                            .set("filter", NEDesc.describeResolveOrDestruct(filter))
                                                            .build()
                                            ).safeIgnore().iterator();
                                    z = filterLatestAndDuplicatesThenSort(z, isLatest() || latestVersion || releaseVersion, isDistinct(), false);
                                    idLookup.add(z);
                                }
                            }
                        }
                        toConcat.add(fetchMode.isStopFast()
                                ? NIteratorUtils.coalesce(NIteratorUtils.concat(idLocal), NIteratorUtils.concat(idRemote))
                                : NIteratorUtils.concatLists(idLocal, idRemote)
                        );
                    }
                }
                allResults.add(NIteratorUtils.concat(toConcat));
//                if (nutsId.getGroupId() == null) {
//                    //now will look with *:artifactId pattern
//                    NSearchCmd search2 = NSearchCmd.of()
//                            .setRepositoryFilter(search.getRepositoryFilter())
//                            .setDefinitionFilter(
//                                    NDefinitionFilters.of().byName(nutsId.builder().setGroupId("*").build().toString())
//                                            .and(search.getDefinitionFilter())
//                            );
//                    NIterator<NId> extraResult = search2.getResultIds().iterator();
//                    allResults.add(
//                            fetchMode.isStopFast() ?
//                                    NIteratorUtils.coalesce(NIteratorUtils.concat(toConcat), extraResult)
//                                    : NIteratorUtils.concat(NIteratorUtils.concat(toConcat), extraResult)
//                    );
//                } else {
//                    allResults.add(NIteratorUtils.concat(toConcat));
//                }

            }
        } else {
            NDefinitionFilter filter = search.getDefinitionFilter();
            List<NIterator<? extends NId>> all = new ArrayList<>();
            for (NRepositoryAndFetchMode repoAndMode : wu.filterRepositoryAndFetchModes(
                    NRepositorySupportedAction.SEARCH, null, sRepositoryFilter,
                    fetchMode
            )) {
                consideredRepos.add(repoAndMode.getRepository());
//                NSession finalSession1 = session;
                all.add(
                        NIteratorBuilder.ofSupplier(() -> wu.repoSPI(repoAndMode.getRepository()).search()
                                                .setFilter(filter)
                                                .setFetchMode(repoAndMode.getFetchMode())
                                                .getResult(),
                                        NEDesc.of(elems.ofObjectBuilder()
                                                .set("description", "searchRepository")
                                                .set("repository", repoAndMode.getRepository().getName())
                                                .set("fetchMode", repoAndMode.getFetchMode().id())
                                                .set("filter", NEDesc.describeResolveOrDestruct(filter))
                                                .build())
                                )
                                .safeIgnore()
                                .iterator()
                );
            }
            allResults.add(
                    fetchMode.isStopFast()
                            ? NIteratorUtils.coalesce(all)
                            : NIteratorUtils.concat(all)
            );
        }
        NIterator<NId> baseIterator = NIteratorUtils.concat(allResults);
        if (inlineDependencies) {
            //optimize by applying latest and distinct when asking for dependencies
            baseIterator = filterLatestAndDuplicatesThenSort(baseIterator, isLatest(), isDistinct(), false);
            //now include dependencies
            NIterator<NId> curr = baseIterator;
            baseIterator = NIteratorBuilder.of(curr)
                    .flatMap(
                            NFunction.of(
                                            (NId x) -> NIteratorBuilder.of(
                                                    toFetch().setId(x).setContent(false)
                                                            .setDependencies(true).getResultDefinition().getDependencies().get().transitiveWithSource().iterator()
                                            ).build())
                                    .withDesc(NEDesc.of("getDependencies"))
                    ).map(NFunction.of(NDependency::toId)
                            .withDesc(NEDesc.of("DependencyToId"))
                    )
                    .build();
        }
        return filterLatestAndDuplicatesThenSort(baseIterator, isLatest(), isDistinct(), isSorted());
    }


    private NIterator<NId> filterLatestAndDuplicatesThenSort(NIterator<NId> baseIterator, boolean latest, boolean distinct, boolean sort) {
        //ff ft tt tf
        NIterator<NId> r;
        if (!latest && !distinct) {
            r = baseIterator;
        } else if (!latest && distinct) {
            r = NIteratorBuilder.of(baseIterator).distinct(
                    NFunction.of(
                                    (NId nutsId) -> nutsId.getLongId()
                                            .toString())
                            .withDesc(NEDesc.of("getLongId"))
            ).iterator();
        } else if (latest && distinct) {
            r = NIteratorBuilder.ofSupplier(() -> {
                        Map<String, NId> visited = new LinkedHashMap<>();
                        while (baseIterator.hasNext()) {
                            NId nutsId = baseIterator.next();
                            String k = nutsId.getShortName();
                            NId old = visited.get(k);
                            if (old == null || old.getVersion().isBlank() || old.getVersion().compareTo(nutsId.getVersion()) < 0) {
                                visited.put(k, nutsId);
                            }
                        }
                        return visited.values().iterator();
                    }, () -> NEDesc.describeResolveOrDestructAsObject(baseIterator)
                            .builder()
                            .set("latest", true)
                            .set("distinct", true)
                            .build()
            ).build();
        } else /*if (latest && !distinct)*/ {
            r = NIteratorBuilder.ofSupplier(() -> {
                                Map<String, List<NId>> visited = new LinkedHashMap<>();
                                while (baseIterator.hasNext()) {
                                    NId nutsId = baseIterator.next();
                                    String k = nutsId.getShortName();
                                    List<NId> oldList = visited.get(k);
                                    NId old = oldList == null ? null : oldList.get(0);
                                    if (old == null || old.getVersion().isBlank() || old.getVersion().compareTo(nutsId.getVersion()) < 0) {
                                        visited.put(k, new ArrayList<>(Arrays.asList(nutsId)));
                                    } else if (old.getVersion().compareTo(nutsId.getVersion()) == 0) {
                                        oldList.add(nutsId);
                                    }
                                }
                                return NIteratorBuilder.ofFlatMap(NIterator.of(visited.values().iterator()).withDesc(NEDesc.of("visited"))).build();
                            }, () -> NEDesc.describeResolveOrDestructAsObject(baseIterator)
                                    .builder()
                                    .set("latest", true)
                                    .set("duplicates", true)
                                    .build()
                    )
                    .build();
        }
        if (sort) {
            r = NIteratorUtils.sort(r, comparator, false);
        }
        return r;
    }
}
