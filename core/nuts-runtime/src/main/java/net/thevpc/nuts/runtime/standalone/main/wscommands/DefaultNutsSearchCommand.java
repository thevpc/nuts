/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.main.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.commands.repo.NutsRepositorySupportedAction;
import net.thevpc.nuts.runtime.core.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.core.filters.NutsPatternIdFilter;
import net.thevpc.nuts.runtime.core.filters.id.NutsIdFilterOr;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.DefaultNutsSearch;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;
import net.thevpc.nuts.runtime.standalone.util.iter.NamedIterator;
import net.thevpc.nuts.runtime.standalone.wscommands.AbstractNutsSearchCommand;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultNutsSearchCommand extends AbstractNutsSearchCommand {

    public DefaultNutsSearchCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsSearchCommand copy() {
        DefaultNutsSearchCommand b = new DefaultNutsSearchCommand(ws);
        b.copyFrom(this);
        return b;
    }


    //@Override
    private DefaultNutsSearch build() {
        HashSet<String> someIds = new HashSet<>();
        for (NutsId id : this.getIds()) {
            someIds.add(id.toString());
        }
        if (this.getIds().length == 0 && isCompanion()) {
            someIds.addAll(ws.getCompanionIds().stream().map(NutsId::getShortName).collect(Collectors.toList()));
        }
        if (this.getIds().length == 0 && isRuntime()) {
            someIds.add(NutsConstants.Ids.NUTS_RUNTIME);
        }
        HashSet<String> goodIds = new HashSet<>();
        HashSet<String> wildcardIds = new HashSet<>();
        for (String someId : someIds) {
            if (NutsPatternIdFilter.containsWildcad(someId)) {
                wildcardIds.add(someId);
            } else {
                goodIds.add(someId);
            }
        }
        NutsIdFilter idFilter0 = getIdFilter();
        if (idFilter0 instanceof NutsPatternIdFilter) {
            NutsPatternIdFilter f = (NutsPatternIdFilter) idFilter0;
            if (!f.isWildcard()) {
                goodIds.add(f.getId().toString());
                idFilter0 = null;
            }
        }
        if (idFilter0 instanceof NutsIdFilterOr) {
            List<NutsIdFilter> oo = new ArrayList<>(Arrays.asList(((NutsIdFilterOr) idFilter0).getChildren()));
            boolean someChange = false;
            for (Iterator<NutsIdFilter> it = oo.iterator(); it.hasNext(); ) {
                NutsIdFilter curr = it.next();
                if (curr instanceof NutsPatternIdFilter) {
                    NutsPatternIdFilter f = (NutsPatternIdFilter) curr;
                    if (!f.isWildcard()) {
                        goodIds.add(f.getId().toString());
                        it.remove();
                        someChange = true;
                    }
                }
            }
            if (someChange) {
                if (oo.isEmpty()) {
                    idFilter0 = null;
                } else {
                    idFilter0 = ws.id().filter().any(oo.toArray(new NutsIdFilter[0]));
                }
            }
        }

        NutsDescriptorFilter _descriptorFilter = ws.descriptor().filter().always();
        NutsIdFilter _idFilter = ws.id().filter().always();
        NutsDependencyFilter depFilter = ws.dependency().filter().always();
        NutsRepositoryFilter rfilter = ws.repos().filter().always();
        for (String j : this.getScripts()) {
            if (!CoreStringUtils.isBlank(j)) {
                if (CoreStringUtils.containsTopWord(j, "descriptor")) {
                    _descriptorFilter = _descriptorFilter.and(ws.descriptor().filter().byExpression(j));
                } else if (CoreStringUtils.containsTopWord(j, "dependency")) {
                    depFilter = depFilter.and(ws.dependency().filter().byExpression(j));
                } else {
                    _idFilter = _idFilter.and(ws.id().filter().byExpression(j));
                }
            }
        }
        NutsDescriptorFilter packs = ws.descriptor().filter().byPackaging(getPackaging());
        NutsDescriptorFilter archs = ws.descriptor().filter().byArch(getArch());
        _descriptorFilter = _descriptorFilter.and(packs).and(archs);

        if (this.getRepositories().length > 0) {
            rfilter = rfilter.and(ws.repos().filter().byName(this.getRepositories()));
        }

        NutsRepositoryFilter _repositoryFilter = rfilter.and(this.getRepositoryFilter());
        _descriptorFilter = _descriptorFilter.and(this.getDescriptorFilter());

        _idFilter = _idFilter.and(idFilter0);
        if (getInstallStatus() != null) {
            _idFilter = _idFilter.and(ws.id().filter().byInstallStatus(getInstallStatus()));
        }
        if (getDefaultVersions() != null) {
            _idFilter = _idFilter.and(ws.id().filter().byDefaultVersion(getDefaultVersions()));
        }
        if (execType != null) {
            switch (execType) {
                case "lib": {
                    _descriptorFilter = _descriptorFilter.and(ws.descriptor().filter().byApp(false)).and(ws.descriptor().filter().byExec(false));
                    break;
                }
                case "exec": {
                    _descriptorFilter = _descriptorFilter.and(ws.descriptor().filter().byExec(true));
                    break;
                }
                case "app": {
                    _descriptorFilter = _descriptorFilter.and(ws.descriptor().filter().byApp(true));
                    break;
                }
                case "extension": {
                    _descriptorFilter = _descriptorFilter.and(ws.descriptor().filter().byExtension(targetApiVersion));
                    break;
                }
                case "runtime": {
                    _descriptorFilter = _descriptorFilter.and(ws.descriptor().filter().byRuntime(targetApiVersion));
                    break;
                }
                case "companions": {
                    _descriptorFilter = _descriptorFilter.and(ws.descriptor().filter().byCompanion(targetApiVersion));
                    break;
                }
            }
        } else {
            if (targetApiVersion != null) {
                _descriptorFilter = _descriptorFilter.and(ws.descriptor().filter().byApiVersion(targetApiVersion));
            }
        }
        if (!lockedIds.isEmpty()) {
            _descriptorFilter = _descriptorFilter.and(ws.descriptor().filter().byLockedIds(
                    lockedIds.stream().map(NutsId::getFullName).toArray(String[]::new)
            ));
        }
        if (!wildcardIds.isEmpty()) {
            _idFilter = _idFilter.and(ws.id().filter().byName(wildcardIds.toArray(new String[0])));
        }
        boolean searchInInstalled = false;
        boolean searchInOtherRepositories = false;

        if (getInstallStatus() != null && this.getRepositories().length > 0) {
            for (NutsInstallStatus x : NutsInstallStatuses.ALL_DEPLOYED) {
                if (getInstallStatus().test(x)) {
                    searchInInstalled = true;
                    break;
                }
            }
            searchInOtherRepositories = true;
        } else if (getInstallStatus() == null && this.getRepositories().length > 0) {
            searchInInstalled = false;
            searchInOtherRepositories = true;
        } else if (getInstallStatus() != null && this.getRepositories().length == 0) {
            for (NutsInstallStatus x : NutsInstallStatuses.ALL_DEPLOYED) {
                if (getInstallStatus().test(x)) {
                    searchInInstalled = true;
                    break;
                }
            }
            if (getInstallStatus().test(NutsInstallStatuses.ALL_UNDEPLOYED)) {
                searchInOtherRepositories = true;
            }
        } else if (getInstallStatus() == null && this.getRepositories().length == 0) {
            searchInInstalled = true;
            searchInOtherRepositories = true;
        } else {
            searchInInstalled = true;
            searchInOtherRepositories = true;
        }
        NutsIdFilter filter = _idFilter.and(_descriptorFilter).to(NutsIdFilter.class);
//        InstalledVsNonInstalledSearch includeInstalledRepository = CoreFilterUtils.getTopLevelInstallRepoInclusion(filter);
//        searchInInstalled |= includeInstalledRepository.isSearchInInstalled();
//        searchInOtherRepositories |= includeInstalledRepository.isSearchInOtherRepositories();
        return new DefaultNutsSearch(
                goodIds.toArray(new String[0]),
                _repositoryFilter,
                _idFilter, _descriptorFilter,
                searchInInstalled,
                searchInOtherRepositories,
                getValidWorkspaceSession());
    }


    //    private Collection<NutsId> applyPrintDecoratorCollectionOfNutsId(Collection<NutsId> curr, boolean print) {
//        if (!print) {
//            return curr;
//        }
//        return CoreCommonUtils.toList(applyPrintDecoratorIterOfNutsId(curr.iterator(), print));
//    }

    private NutsCollectionResult<NutsId> applyVersionFlagFilters(Iterator<NutsId> curr, boolean print) {
        if (!isLatest() && !isDistinct()) {
            return buildNutsCollectionSearchResult(curr, print);
            //nothing
        } else if (!isLatest() && isDistinct()) {
            return buildNutsCollectionSearchResult(IteratorBuilder.of(curr).distinct((NutsId nutsId) -> nutsId.getLongNameId()
                    //                            .setAlternative(nutsId.getAlternative())
                    .toString()).iterator(), print);
        } else if (isLatest() && isDistinct()) {
            Iterator<NutsId> nn = IteratorUtils.supplier(() -> {
                Map<String, NutsId> visited = new LinkedHashMap<>();
                while (curr.hasNext()) {
                    NutsId nutsId = curr.next();
                    String k = nutsId.getShortNameId()
                            //                        .setAlternative(nutsId.getAlternative())
                            .toString();
                    NutsId old = visited.get(k);
                    if (old == null || old.getVersion().isBlank() || old.getVersion().compareTo(nutsId.getVersion()) < 0) {
                        visited.put(k, nutsId);
                    }
                }
                return visited.values().iterator();
            }, "latestAndDistinct");
            return buildNutsCollectionSearchResult(nn, print);
        } else if (isLatest() && !isDistinct()) {
            Iterator<NutsId> nn = IteratorUtils.supplier(() -> {
                Map<String, List<NutsId>> visited = new LinkedHashMap<>();
                while (curr.hasNext()) {
                    NutsId nutsId = curr.next();
                    String k = nutsId.getShortNameId()
                            //                        .setAlternative(nutsId.getAlternative())
                            .toString();
                    List<NutsId> oldList = visited.get(k);
                    if (oldList == null || oldList.get(0).getVersion().isBlank() || oldList.get(0).getVersion().compareTo(nutsId.getVersion()) < 0) {
                        visited.put(k, new ArrayList<>(Arrays.asList(nutsId)));
                    } else if (oldList.get(0).getVersion().compareTo(nutsId.getVersion()) == 0) {
                        oldList.add(nutsId);
                    }
                }
                return IteratorUtils.name("latestAndDuplicate", IteratorUtils.flatten((Iterator) visited.values().iterator()));
            }, "latestAndDuplicate");
            return buildNutsCollectionSearchResult(nn, print);
        }
        throw new NutsUnexpectedException(ws);
    }


    protected NutsCollectionResult<NutsId> getResultIdsBase(boolean print, boolean sort) {
        DefaultNutsSearch build = build();
//        build.getOptions().session(build.getOptions().getSession().copy().trace(print));
        Iterator<NutsId> base0 = findIterator(build);
        if (base0 == null) {
            return buildNutsCollectionSearchResult(IteratorUtils.emptyIterator(), print);
        }
        if (!isLatest() && !isDistinct() && !sort && !isInlineDependencies()) {
            return buildNutsCollectionSearchResult(base0, print);
        }
        NutsCollectionResult<NutsId> a = applyVersionFlagFilters(base0, false);
        Iterator<NutsId> curr = a.iterator();
        if (isInlineDependencies()) {
            if (!isBasePackage()) {
                curr = Arrays.asList(findDependencies(a.list())).iterator();
            } else {
                List<Iterator<NutsId>> it = new ArrayList<>();
                Iterator<NutsId> a0 = a.iterator();
                List<NutsId> base = new ArrayList<>();
                it.add(new NamedIterator<NutsId>("tee(" + a0 + ")") {
                    @Override
                    public boolean hasNext() {
                        return a0.hasNext();
                    }

                    @Override
                    public NutsId next() {
                        NutsId x = a0.next();
                        base.add(x);
                        return x;
                    }
                });
                it.add(new NamedIterator<NutsId>("ResolveDependencies") {
                    Iterator<NutsId> deps = null;

                    @Override
                    public boolean hasNext() {
                        if (deps == null) {
                            //will be called when base is already filled up!
                            deps = Arrays.asList(findDependencies(base)).iterator();
                        }
                        return deps.hasNext();
                    }

                    @Override
                    public NutsId next() {
                        return deps.next();
                    }
                });
                curr = IteratorUtils.concat(it);
            }
        }
        if (sort) {
            return buildNutsCollectionSearchResult(
                    IteratorUtils.sort(applyVersionFlagFilters(curr, false).iterator(), comparator, false),
                    print);
        } else {
            return applyVersionFlagFilters(curr, print);
        }
    }

    private NutsId[] findDependencies(List<NutsId> ids) {
        NutsSession _session = this.getValidWorkspaceSession();
        NutsDependencyFilter _dependencyFilter = ws.dependency().filter().byScope(getScope())
                .and(ws.dependency().filter().byOptional(getOptional()))
                .and(getDependencyFilter());
        for (NutsDependencyFilter ff : CoreFilterUtils.getTopLevelFilters(getIdFilter(), NutsDependencyFilter.class, getWorkspace())) {
            _dependencyFilter = _dependencyFilter.and(ff);
        }
        NutsIdGraph graph = new NutsIdGraph(CoreNutsUtils.silent(_session), isFailFast());
        return graph.resolveDependencies(ids, _dependencyFilter);
    }


    public Iterator<NutsId> findIterator(DefaultNutsSearch search) {

        List<Iterator<NutsId>> allResults = new ArrayList<>();

        NutsSession session = search.getSession();
        session = NutsWorkspaceUtils.of(ws).validateSession(session);
        NutsIdFilter sIdFilter = search.getIdFilter();
        NutsRepositoryFilter sRepositoryFilter = search.getRepositoryFilter();
        NutsDescriptorFilter sDescriptorFilter = search.getDescriptorFilter();
        String[] regularIds = search.getRegularIds();
        NutsFetchStrategy fetchMode = NutsWorkspaceHelper.validate(session.getFetchStrategy());
        InstalledVsNonInstalledSearch installedVsNonInstalledSearch = new InstalledVsNonInstalledSearch(
                search.isSearchInInstalled(),
                search.isSearchInOtherRepositories()
        );

        if (regularIds.length > 0) {
            for (String id : regularIds) {
                NutsId nutsId = ws.id().parser().parse(id);
                if (nutsId != null) {
                    List<NutsId> nutsId2 = new ArrayList<>();
                    if (CoreStringUtils.isBlank(nutsId.getGroupId())) {
                        if (nutsId.getArtifactId().equals("nuts")) {
                            nutsId2.add(nutsId.builder().setGroupId("net.thevpc.nuts").build());
                        } else {
                            for (String aImport : ws.imports().getAll()) {
                                nutsId2.add(nutsId.builder().setGroupId(aImport).build());
                            }
                        }
                    } else {
                        nutsId2.add(nutsId);
                    }
                    List<Iterator<NutsId>> coalesce = new ArrayList<>();
                    NutsSession finalSession = session;
                    for (NutsFetchMode mode : fetchMode) {
                        List<Iterator<NutsId>> all = new ArrayList<>();
                        for (NutsId nutsId1 : nutsId2) {
                            NutsIdFilter idFilter2 = ws.filters().all(sIdFilter,
                                    ws.id().filter().byName(nutsId1.getFullName())
                            );
                            NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(nutsId1.getProperties(), idFilter2, sDescriptorFilter, ws));
//                            boolean includeInstalledRepository=filter0.accept()
                            for (NutsRepository repo : NutsWorkspaceUtils.of(ws).filterRepositories(
                                    NutsRepositorySupportedAction.SEARCH, nutsId1, sRepositoryFilter, mode, session,
                                    installedVsNonInstalledSearch
                            )) {
                                NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(ws).repoSPI(repo);
                                if (sRepositoryFilter == null || sRepositoryFilter.acceptRepository(repo)) {
                                    all.add(IteratorBuilder.ofLazyNamed("searchVersions(" + repo.getName() + "," + mode + "," + sRepositoryFilter + "," + finalSession + ")", ()
                                            -> repoSPI.searchVersions().setId(nutsId1).setFilter(filter)
                                            .setSession(finalSession)
                                            .setFetchMode(mode)
                                            .getResult()).safeIgnore().iterator()
                                    );
                                }
                            }
                        }
                        coalesce.add(IteratorUtils.concat(all));
                    }
                    if (nutsId.getGroupId() == null) {
                        //now will look with *:artifactId pattern
                        NutsSearchCommand search2 = ws.search()
                                .setSession(session)
                                .setRepositoryFilter(search.getRepositoryFilter())
                                .setDescriptorFilter(search.getDescriptorFilter());
                        search2.setIdFilter(
                                ws.id().filter().byName(nutsId.builder().setGroupId("*").build().toString())
                                        .and(search.getIdFilter())
                        );
                        Iterator<NutsId> extraResult = search2.getResultIds().iterator();
                        if (fetchMode.isStopFast()) {
                            coalesce.add(extraResult);
                            allResults.add(IteratorUtils.coalesce(coalesce));
                        } else {
                            allResults.add(
                                    IteratorUtils.coalesce(
                                            Arrays.asList(
                                                    IteratorUtils.concat(coalesce),
                                                    extraResult
                                            )
                                    )
                            );
                        }
                    } else {
                        allResults.add(fetchMode.isStopFast()
                                ? IteratorUtils.coalesce(coalesce)
                                : IteratorUtils.concat(coalesce)
                        );
                    }
                }
            }
        } else {
            NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(null, sIdFilter, sDescriptorFilter, ws));

            List<Iterator<NutsId>> coalesce = new ArrayList<>();
            for (NutsFetchMode mode : fetchMode) {
                List<Iterator<NutsId>> all = new ArrayList<>();
                for (NutsRepository repo : NutsWorkspaceUtils.of(ws).filterRepositories(NutsRepositorySupportedAction.SEARCH, null, sRepositoryFilter, mode, session,
                        installedVsNonInstalledSearch
                )) {
                    NutsSession finalSession1 = session;
                    all.add(
                            IteratorBuilder.ofLazyNamed("search(" + repo.getName() + "," + mode + "," + sRepositoryFilter + "," + session + ")",
                                    () -> NutsWorkspaceUtils.of(ws).repoSPI(repo).search().setFilter(filter).setSession(finalSession1)
                                            .setFetchMode(mode)
                                            .getResult()).safeIgnore().iterator()
                    );
                }
                coalesce.add(IteratorUtils.concat(all));
            }
            allResults.add(fetchMode.isStopFast() ? IteratorUtils.coalesce(coalesce) : IteratorUtils.concat(coalesce));
        }
        return IteratorUtils.concat(allResults);
    }


}
