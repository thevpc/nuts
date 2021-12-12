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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.search;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NutsDescribables;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NutsRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsIdFilterOr;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsPatternIdFilter;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.io.util.NutsInstallStatusIdFilter;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch.DefaultNutsFetchCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NutsInstallStatuses;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NutsRepositoryAndFetchMode;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.util.*;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;

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

    @Override
    public NutsFetchCommand toFetch() {
        checkSession();
        NutsFetchCommand t = new DefaultNutsFetchCommand(ws).copyFromDefaultNutsQueryBaseOptions(this)
                .setSession(getSession());
        if (getDisplayOptions().isRequireDefinition()) {
            t.setContent(true);
        }
        //update RepositoryFilter with effective one that takes into consideration
        // id filters and status filters
        DefaultNutsSearch bs = build();
        t.setRepositoryFilter(bs.getRepositoryFilter());
        return t;
    }

    private NutsRepositoryFilter createRepositoryFilter(NutsInstallStatusFilter status, NutsIdFilter _idFilter, NutsSession session) {
//        if(status==null){
//            return null;
//        }
        boolean searchInInstalled = true;
        boolean searchInOtherRepositories = true;
        if (status != null && Arrays.stream(NutsInstallStatuses.ALL_DEPLOYED).noneMatch(
                x -> status.acceptInstallStatus(x, session)
        )) {
            searchInInstalled = false;
        }
        if (status != null && Arrays.stream(NutsInstallStatuses.ALL_UNDEPLOYED).noneMatch(
                x -> status.acceptInstallStatus(x, session)
        )) {
            searchInOtherRepositories = false;
        }
        List<NutsRepositoryFilter> otherFilters = new ArrayList<>();

        if (_idFilter != null && _idFilter.getFilterOp() == NutsFilterOp.AND) {
            searchInOtherRepositories = true;
            for (NutsFilter subFilter : _idFilter.getSubFilters()) {
                if (subFilter instanceof NutsInstallStatusIdFilter) {
                    NutsInstallStatusFilter status2 = ((NutsInstallStatusIdFilter) subFilter).getInstallStatus();
                    if (searchInInstalled) {
                        if (status != null && Arrays.stream(NutsInstallStatuses.ALL_DEPLOYED).noneMatch(
                                x -> status2.acceptInstallStatus(x, session)
                        )) {
                            searchInInstalled = false;
                        }
                    }
                    if (searchInOtherRepositories) {
                        if (status != null && Arrays.stream(NutsInstallStatuses.ALL_UNDEPLOYED).noneMatch(
                                x -> status2.acceptInstallStatus(x, session)
                        )) {
                            searchInOtherRepositories = false;
                        }
                    }
                }
                if (subFilter instanceof NutsRepositoryFilter) {
                    otherFilters.add((NutsRepositoryFilter) subFilter);
                }
            }
        }

        NutsRepositoryFilters repository = NutsRepositoryFilters.of(session);
        if (!searchInInstalled && searchInOtherRepositories) {
            otherFilters.add(repository.installedRepo().neg());
        } else if (searchInInstalled && !searchInOtherRepositories) {
            otherFilters.add(repository.installedRepo());
        } else if (!searchInInstalled && !searchInOtherRepositories) {
            otherFilters.add(repository.never());
        }
        if (otherFilters.isEmpty()) {
            return null;
        }
        NutsRepositoryFilter r = otherFilters.get(0);
        for (int i = 1; i < otherFilters.size(); i++) {
            r = r.and(otherFilters.get(i));
        }
        return r;
    }

    //@Override
    private DefaultNutsSearch build() {
        checkSession();
        NutsSession session = getSession();
        HashSet<String> someIds = new HashSet<>();
        for (NutsId id : this.getIds()) {
            someIds.add(id.toString());
        }
        if (this.getIds().length == 0 && isCompanion()) {
            someIds.addAll(this.session.extensions().getCompanionIds().stream().map(NutsId::getShortName).collect(Collectors.toList()));
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
                    idFilter0 = NutsIdFilters.of(this.session).any(oo.toArray(new NutsIdFilter[0]));
                }
            }
        }

        NutsDescriptorFilters dfilter = NutsDescriptorFilters.of(session);
        NutsDescriptorFilter _descriptorFilter = dfilter.always();
        NutsIdFilter _idFilter = NutsIdFilters.of(this.session).always();
        NutsDependencyFilter depFilter = NutsDependencyFilters.of(session).always();
        NutsRepositoryFilter rfilter = this.session.repos().filter().always();
        for (String j : this.getScripts()) {
            if (!NutsBlankable.isBlank(j)) {
                if (CoreStringUtils.containsTopWord(j, "descriptor")) {
                    _descriptorFilter = _descriptorFilter.and(dfilter.parse(j));
                } else if (CoreStringUtils.containsTopWord(j, "dependency")) {
                    depFilter = depFilter.and(NutsDependencyFilters.of(session).parse(j));
                } else {
                    _idFilter = _idFilter.and(NutsIdFilters.of(session).parse(j));
                }
            }
        }
        NutsDescriptorFilter packs = dfilter.byPackaging(getPackaging());
        NutsDescriptorFilter archs = dfilter.byArch(getArch());
        _descriptorFilter = _descriptorFilter.and(packs).and(archs);

        NutsRepositoryFilter _repositoryFilter = rfilter.and(this.getRepositoryFilter());
        _descriptorFilter = _descriptorFilter.and(this.getDescriptorFilter());

        _idFilter = _idFilter.and(idFilter0);
        if (getInstallStatus() != null) {
            _idFilter = _idFilter.and(NutsIdFilters.of(session).byInstallStatus(getInstallStatus()));
        }
        if (getDefaultVersions() != null) {
            _idFilter = _idFilter.and(NutsIdFilters.of(session).byDefaultVersion(getDefaultVersions()));
        }
        if (execType != null) {
            switch (execType) {
                case "lib": {
                    _descriptorFilter = _descriptorFilter.and(dfilter.byFlag(NutsDescriptorFlag.EXEC).neg());
                    break;
                }
                case "exec": {
                    _descriptorFilter = _descriptorFilter.and(dfilter.byFlag(NutsDescriptorFlag.EXEC));
                    break;
                }
                case "app": {
                    _descriptorFilter = _descriptorFilter.and(dfilter.byFlag(NutsDescriptorFlag.APP));
                    break;
                }
                case "extension": {
                    _descriptorFilter = _descriptorFilter.and(dfilter.byExtension(targetApiVersion));
                    break;
                }
                case "runtime": {
                    _descriptorFilter = _descriptorFilter.and(dfilter.byRuntime(targetApiVersion));
                    break;
                }
                case "companions": {
                    _descriptorFilter = _descriptorFilter.and(dfilter.byCompanion(targetApiVersion));
                    break;
                }
            }
        } else {
            if (targetApiVersion != null) {
                _descriptorFilter = _descriptorFilter.and(dfilter.byApiVersion(targetApiVersion));
            }
        }
        if (!lockedIds.isEmpty()) {
            _descriptorFilter = _descriptorFilter.and(dfilter.byLockedIds(
                    lockedIds.stream().map(NutsId::getFullName).toArray(String[]::new)
            ));
        }
        if (!wildcardIds.isEmpty()) {
            _idFilter = _idFilter.and(NutsIdFilters.of(session).byName(wildcardIds.toArray(new String[0])));
        }
        NutsRepositoryFilter extraRepositoryFilter = createRepositoryFilter(installStatus, _idFilter, getSession());
        if (extraRepositoryFilter != null) {
            _repositoryFilter = _repositoryFilter.and(extraRepositoryFilter);
        }
//        boolean searchInInstalled = false;
//        boolean searchInOtherRepositories = false;
//
//        if (getInstallStatus() != null && this.getRepositories().length > 0) {
//            for (NutsInstallStatus x : NutsInstallStatuses.ALL_DEPLOYED) {
//                if (getInstallStatus().acceptInstallStatus(x, getSession())) {
//                    searchInInstalled = true;
//                    break;
//                }
//            }
//            searchInOtherRepositories = true;
//        } else if (getInstallStatus() == null && this.getRepositories().length > 0) {
//            searchInInstalled = false;
//            searchInOtherRepositories = true;
//        } else if (getInstallStatus() != null && this.getRepositories().length == 0) {
//            for (NutsInstallStatus x : NutsInstallStatuses.ALL_DEPLOYED) {
//                if (getInstallStatus().acceptInstallStatus(x, getSession())) {
//                    searchInInstalled = true;
//                    break;
//                }
//            }
//            if (getInstallStatus().acceptInstallStatus(NutsInstallStatuses.ALL_UNDEPLOYED, getSession())) {
//                searchInOtherRepositories = true;
//            }
//        } else if (getInstallStatus() == null && this.getRepositories().length == 0) {
//            searchInInstalled = true;
//            searchInOtherRepositories = true;
//            if (_idFilter.getFilterOp() == NutsFilterOp.AND) {
//                searchInOtherRepositories = true;
//                for (NutsFilter subFilter : _idFilter.getSubFilters()) {
//                    if (subFilter instanceof NutsInstallStatusIdFilter) {
//                        NutsInstallStatusFilter f = ((NutsInstallStatusIdFilter) subFilter).getInstallStatus();
//                        if (!f.acceptInstallStatus(NutsInstallStatuses.ALL_UNDEPLOYED, getSession())) {
//                            searchInOtherRepositories = false;
//                        }
//                    }
//                }
//            } else {
//                searchInOtherRepositories = true;
//            }
//        } else {
//            searchInInstalled = true;
//            searchInOtherRepositories = true;
//        }
//        NutsIdFilter filter = _idFilter.and(_descriptorFilter).to(NutsIdFilter.class);
//        InstalledVsNonInstalledSearch includeInstalledRepository = CoreFilterUtils.getTopLevelInstallRepoInclusion(filter);
//        searchInInstalled |= includeInstalledRepository.isSearchInInstalled();
//        searchInOtherRepositories |= includeInstalledRepository.isSearchInOtherRepositories();
        return new DefaultNutsSearch(
                goodIds.toArray(new String[0]),
                _repositoryFilter,
                _idFilter, _descriptorFilter,
                getSession());
    }

    //    private Collection<NutsId> applyPrintDecoratorCollectionOfNutsId(Collection<NutsId> curr, boolean print) {
//        if (!print) {
//            return curr;
//        }
//        return NutsTextUtils.toList(applyPrintDecoratorIterOfNutsId(curr.iterator(), print));
//    }
//    private NutsCollectionStream<NutsId> applyVersionFlagFilters(NutsIterator<NutsId> curr, boolean print) {
//        if (!isLatest() && !isDistinct()) {
//            return buildCollectionResult(curr, print);
//            //nothing
//        } else if (!isLatest() && isDistinct()) {
//            return buildCollectionResult(IteratorBuilder.of(curr).distinct((NutsId nutsId) -> nutsId.getLongNameId()
//                    //                            .setAlternative(nutsId.getAlternative())
//                    .toString()).iterator(), print);
//        } else if (isLatest() && isDistinct()) {
//            NutsIterator<NutsId> nn = IteratorUtils.supplier(() -> {
//                Map<String, NutsId> visited = new LinkedHashMap<>();
//                while (curr.hasNext()) {
//                    NutsId nutsId = curr.next();
//                    String k = nutsId.getShortNameId()
//                            //                        .setAlternative(nutsId.getAlternative())
//                            .toString();
//                    NutsId old = visited.get(k);
//                    if (old == null || old.getVersion().isBlank() || old.getVersion().compareTo(nutsId.getVersion()) < 0) {
//                        visited.put(k, nutsId);
//                    }
//                }
//                return visited.values().iterator();
//            }, "latestAndDistinct");
//            return buildCollectionResult(nn, print);
//        } else if (isLatest() && !isDistinct()) {
//            NutsIterator<NutsId> nn = IteratorUtils.supplier(() -> {
//                Map<String, List<NutsId>> visited = new LinkedHashMap<>();
//                while (curr.hasNext()) {
//                    NutsId nutsId = curr.next();
//                    String k = nutsId.getShortNameId()
//                            //                        .setAlternative(nutsId.getAlternative())
//                            .toString();
//                    List<NutsId> oldList = visited.get(k);
//                    if (oldList == null || oldList.get(0).getVersion().isBlank() || oldList.get(0).getVersion().compareTo(nutsId.getVersion()) < 0) {
//                        visited.put(k, new ArrayList<>(Arrays.asList(nutsId)));
//                    } else if (oldList.get(0).getVersion().compareTo(nutsId.getVersion()) == 0) {
//                        oldList.add(nutsId);
//                    }
//                }
//                return IteratorUtils.name("latestAndDuplicate", IteratorUtils.flatCollection((NutsIterator) visited.values().iterator()));
//            }, "latestAndDuplicate");
//            return buildCollectionResult(nn, print);
//        }
//        throw new NutsUnexpectedException(getSession());
//    }
//    private NutsCollectionStream<NutsDependency> applyVersionFlagFilters2(NutsIterator<NutsDependency> curr, boolean print) {
//        if (!isLatest() && !isDistinct()) {
//            return buildCollectionResult(curr, print);
//            //nothing
//        } else if (!isLatest() && isDistinct()) {
//            return buildCollectionResult(IteratorBuilder.of(curr).distinct((NutsDependency nutsId) -> nutsId.toId().getLongNameId()
//                    //                            .setAlternative(nutsId.getAlternative())
//                    .toString()).iterator(), print);
//        } else if (isLatest() && isDistinct()) {
//            NutsIterator<NutsDependency> nn = IteratorUtils.supplier(() -> {
//                Map<String, NutsDependency> visited = new LinkedHashMap<>();
//                while (curr.hasNext()) {
//                    NutsDependency nutsId = curr.next();
//                    String k = nutsId.toId().getShortNameId()
//                            //                        .setAlternative(nutsId.getAlternative())
//                            .toString();
//                    NutsDependency old = visited.get(k);
//                    if (old == null || old.getVersion().isBlank() || old.getVersion().compareTo(nutsId.getVersion()) < 0) {
//                        visited.put(k, nutsId);
//                    }
//                }
//                return visited.values().iterator();
//            }, "latestAndDistinct");
//            return buildCollectionResult(nn, print);
//        } else if (isLatest() && !isDistinct()) {
//            NutsIterator<NutsDependency> nn = IteratorUtils.supplier(() -> {
//                Map<String, List<NutsDependency>> visited = new LinkedHashMap<>();
//                while (curr.hasNext()) {
//                    NutsDependency nutsId = curr.next();
//                    String k = nutsId.toId().getShortNameId()
//                            //                        .setAlternative(nutsId.getAlternative())
//                            .toString();
//                    List<NutsDependency> oldList = visited.get(k);
//                    if (oldList == null || oldList.get(0).getVersion().isBlank() || oldList.get(0).getVersion().compareTo(nutsId.getVersion()) < 0) {
//                        visited.put(k, new ArrayList<>(Arrays.asList(nutsId)));
//                    } else if (oldList.get(0).getVersion().compareTo(nutsId.getVersion()) == 0) {
//                        oldList.add(nutsId);
//                    }
//                }
//                return IteratorUtils.name("latestAndDuplicate", IteratorUtils.flatCollection((NutsIterator) visited.values().iterator()));
//            }, "latestAndDuplicate");
//            return buildCollectionResult(nn, print);
//        }
//        throw new NutsUnexpectedException(getSession());
//    }
//    protected NutsCollectionStream<NutsDependency> getResultDependenciesBase(boolean print, boolean sort) {
//        DefaultNutsSearch build = build();
////        build.getOptions().session(build.getOptions().getSession().copy().trace(print));
//        NutsIterator<NutsDependency> base0 = findIterator2(build);
//        if (base0 == null) {
//            return buildCollectionResult(IteratorUtils.emptyIterator(), print);
//        }
//        if (!isLatest() && !isDistinct() && !sort && !isInlineDependencies()) {
//            return buildCollectionResult(base0, print);
//        }
//        NutsCollectionStream<NutsDependency> a = applyVersionFlagFilters2(base0, false);
//        NutsIterator<NutsDependency> curr = a.iterator();
//        if (isInlineDependencies()) {
//            if (!isBasePackage()) {
//                curr = Arrays.asList(findDependencies2(a.list())).iterator();
//            } else {
//                List<NutsIterator<NutsDependency>> it = new ArrayList<>();
//                NutsIterator<NutsDependency> a0 = a.iterator();
//                List<NutsDependency> base = new ArrayList<>();
//                it.add(new AbstractNamedIterator<NutsDependency>("tee(" + a0 + ")") {
//                    @Override
//                    public boolean hasNext() {
//                        return a0.hasNext();
//                    }
//
//                    @Override
//                    public NutsDependency next() {
//                        NutsDependency x = a0.next();
//                        base.add(x);
//                        return x;
//                    }
//                });
//                it.add(new AbstractNamedIterator<NutsDependency>("ResolveDependencies") {
//                    NutsIterator<NutsDependency> deps = null;
//
//                    @Override
//                    public boolean hasNext() {
//                        if (deps == null) {
//                            //will be called when base is already filled up!
//                            deps = Arrays.asList(findDependencies2(base)).iterator();
//                        }
//                        return deps.hasNext();
//                    }
//
//                    @Override
//                    public NutsDependency next() {
//                        return deps.next();
//                    }
//                });
//                curr = IteratorUtils.concat(it);
//            }
//        }
//        if (sort) {
//            return buildCollectionResult(
//                    IteratorUtils.sort(applyVersionFlagFilters2(curr, false).iterator(), comparator, false),
//                    print);
//        } else {
//            return applyVersionFlagFilters2(curr, print);
//        }
//    }


    //    private NutsId[] findDependencies(List<NutsId> ids) {
//        checkSession();
//        NutsWorkspace ws = getSession().getWorkspace();
//        NutsSession _session = this.getSession();
//        NutsDependencyFilter _dependencyFilter = ws.dependency().filter().byScope(getScope())
//                .and(ws.dependency().filter().byOptional(getOptional()))
//                .and(getDependencyFilter());
//        for (NutsDependencyFilter ff : CoreFilterUtils.getTopLevelFilters(getIdFilter(), NutsDependencyFilter.class, getWorkspace())) {
//            _dependencyFilter = _dependencyFilter.and(ff);
//        }
//        NutsDependenciesResolver nutsDependenciesResolver = new NutsDependenciesResolver(CoreNutsUtils.silent(_session))
//                .setDependencyFilter(_dependencyFilter)
//                .setFailFast(isFailFast());
//        for (NutsId id : ids) {
//            nutsDependenciesResolver.addRootId(id);
//        }
//        return nutsDependenciesResolver.resolve()
//                .all().stream().map(NutsDependency::toId).toArray(NutsId[]::new);
//    }
//    private NutsDependency[] findDependencies2(List<NutsDependency> ids) {
//        checkSession();
//        NutsWorkspace ws = getSession().getWorkspace();
//        NutsSession _session = this.getSession();
//        NutsDependencyFilter _dependencyFilter = ws.dependency().filter().byScope(getScope())
//                .and(ws.dependency().filter().byOptional(getOptional()))
//                .and(getDependencyFilter());
//        for (NutsDependencyFilter ff : CoreFilterUtils.getTopLevelFilters(getIdFilter(), NutsDependencyFilter.class, getWorkspace())) {
//            _dependencyFilter = _dependencyFilter.and(ff);
//        }
//        NutsDependenciesResolver nutsDependenciesResolver = new NutsDependenciesResolver(CoreNutsUtils.silent(_session))
//                .setDependencyFilter(_dependencyFilter)
//                .setFailFast(isFailFast());
//        for (NutsDependency dep : ids) {
//            nutsDependenciesResolver.addRootDefinition(dep);
//        }
//        return nutsDependenciesResolver.resolve().all().toArray(new NutsDependency[0]);
//    }
    public NutsIterator<NutsId> getResultIdIteratorBase(Boolean forceInlineDependencies) {
        boolean inlineDependencies = forceInlineDependencies == null ? isInlineDependencies() : forceInlineDependencies;
        DefaultNutsSearch search = build();

        List<NutsIterator<? extends NutsId>> allResults = new ArrayList<>();
        checkSession();
        NutsSession session = search.getSession();
        NutsWorkspaceUtils.checkSession(session.getWorkspace(), session);
        NutsIdFilter sIdFilter = search.getIdFilter();
        NutsRepositoryFilter sRepositoryFilter = search.getRepositoryFilter();
        NutsDescriptorFilter sDescriptorFilter = search.getDescriptorFilter();
        String[] regularIds = search.getRegularIds();
        NutsFetchStrategy fetchMode = NutsWorkspaceHelper.validate(session.getFetchStrategy());
//        InstalledVsNonInstalledSearch installedVsNonInstalledSearch = new InstalledVsNonInstalledSearch(
//                search.isSearchInInstalled(),
//                search.isSearchInOtherRepositories()
//        );
        Set<NutsRepository> consideredRepos = new HashSet<>();
        NutsWorkspaceUtils wu = NutsWorkspaceUtils.of(session);
        NutsElements elems = NutsElements.of(session);
        if (regularIds.length > 0) {
            for (String id : regularIds) {
                NutsId nutsId = NutsId.of(id, session);
                if (nutsId != null) {
                    List<NutsId> nutsId2 = new ArrayList<>();
                    if (NutsBlankable.isBlank(nutsId.getGroupId())) {
                        if (nutsId.getArtifactId().equals("nuts")) {
                            nutsId2.add(nutsId.builder().setGroupId("net.thevpc.nuts").build());
                        } else {
                            //check if it is already installed
                            List<NutsId> installedIds = Collections.emptyList();
                            if (!nutsId.getArtifactId().contains("*")) {
                                NutsRepositorySPI repoSPI = wu
                                        .repoSPI(NutsWorkspaceExt.of(getWorkspace()).getInstalledRepository());
                                NutsIterator<NutsId> it = repoSPI.search().setFetchMode(NutsFetchMode.LOCAL).setFilter(NutsIdFilters.of(session).byName(
                                        nutsId.builder().setGroupId("*").build().toString()
                                )).setSession(getSession()).getResult();
                                installedIds = IteratorUtils.toList(it);
                            }
                            if (!installedIds.isEmpty()) {
                                nutsId2.addAll(installedIds);
                            } else {
                                for (String aImport : session.imports().getAllImports()) {
                                    //example import(net.thevpc),search(pnote) ==>net.thevpc:pnote
                                    nutsId2.add(nutsId.builder().setGroupId(aImport).build());
                                    //example import(net.thevpc),search(pnote) ==>net.thevpc.pnote:pnote
                                    nutsId2.add(nutsId.builder().setGroupId(aImport + "." + nutsId.getArtifactId()).build());
                                }
                            }
                        }
                    } else {
                        nutsId2.add(nutsId);
                    }
                    List<NutsIterator<? extends NutsId>> toConcat = new ArrayList<>();
                    for (NutsId nutsId1 : nutsId2) {
                        NutsIdFilter idFilter2 = NutsFilters.of(session).all(sIdFilter,
                                NutsIdFilters.of(session).byName(nutsId1.getFullName())
                        );
                        NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(nutsId1.getProperties(),
                                idFilter2, sDescriptorFilter, session));
                        List<NutsRepositoryAndFetchMode> repositoryAndFetchModes = wu.filterRepositoryAndFetchModes(
                                NutsRepositorySupportedAction.SEARCH, nutsId1, sRepositoryFilter, fetchMode, session
                        );

                        List<NutsIterator<? extends NutsId>> idLocal = new ArrayList<>();
                        List<NutsIterator<? extends NutsId>> idRemote = new ArrayList<>();
                        for (NutsFetchMode fm : new NutsFetchMode[]{NutsFetchMode.LOCAL, NutsFetchMode.REMOTE}) {
                            List<NutsIterator<? extends NutsId>> idLookup = fm == NutsFetchMode.LOCAL ? idLocal : idRemote;
                            for (NutsRepositoryAndFetchMode repoAndMode : repositoryAndFetchModes) {
                                if (repoAndMode.getFetchMode() == fm) {
                                    consideredRepos.add(repoAndMode.getRepository());
                                    NutsRepositorySPI repoSPI = wu.repoSPI(repoAndMode.getRepository());

                                    idLookup.add(

                                            IteratorBuilder.of(repoSPI.searchVersions().setId(nutsId1).setFilter(filter)
                                                            .setSession(session)
                                                            .setFetchMode(repoAndMode.getFetchMode())
                                                            .getResult(), session)
                                                    .named(
                                                            elems.ofObject()
                                                                    .set("description", "searchVersions")
                                                                    .set("repository", repoAndMode.getRepository().getName())
                                                                    .set("filter", NutsDescribables.resolveOrDestruct(filter, elems))
                                                                    .build()
                                                    ).safeIgnore().iterator()
                                    );
                                }
                            }
                        }
                        toConcat.add(fetchMode.isStopFast()
                                ? IteratorUtils.coalesce(IteratorUtils.concat(idLocal), IteratorUtils.concat(idRemote))
                                : IteratorUtils.concatLists(idLocal, idRemote)
                        );
                    }
                    if (nutsId.getGroupId() == null) {
                        //now will look with *:artifactId pattern
                        NutsSearchCommand search2 = session.search()
                                .setSession(session)
                                .setRepositoryFilter(search.getRepositoryFilter())
                                .setDescriptorFilter(search.getDescriptorFilter());
                        search2.setIdFilter(
                                NutsIdFilters.of(session).byName(nutsId.builder().setGroupId("*").build().toString())
                                        .and(search.getIdFilter())
                        );
                        NutsIterator<NutsId> extraResult = search2.getResultIds().iterator();
                        allResults.add(IteratorUtils.coalesce(
                                IteratorUtils.concat(toConcat),
                                extraResult
                        ));
                    } else {
                        allResults.add(IteratorUtils.concat(toConcat));
                    }
                }
            }
        } else {
            NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(null, sIdFilter, sDescriptorFilter, session));

            List<NutsIterator<? extends NutsId>> all = new ArrayList<>();
            for (NutsRepositoryAndFetchMode repoAndMode : wu.filterRepositoryAndFetchModes(
                    NutsRepositorySupportedAction.SEARCH, null, sRepositoryFilter,
                    fetchMode, session
            )) {
                consideredRepos.add(repoAndMode.getRepository());
                NutsSession finalSession1 = session;
                all.add(
                        IteratorBuilder.of(wu.repoSPI(repoAndMode.getRepository()).search()
                                        .setFilter(filter).setSession(finalSession1)
                                        .setFetchMode(repoAndMode.getFetchMode())
                                        .getResult(), session).safeIgnore()
                                .named(
                                        elems.ofObject()
                                                .set("description", "searchRepository")
                                                .set("repository", repoAndMode.getRepository().getName())
                                                .set("fetchMode", repoAndMode.getFetchMode().id())
                                                .set("filter", NutsDescribables.resolveOrDestruct(filter, elems))
                                                .build()
                                ).iterator()
                );
            }
            allResults.add(
                    fetchMode.isStopFast()
                            ? IteratorUtils.coalesce(all)
                            : IteratorUtils.concat(all)
            );
        }
        NutsIterator<NutsId> baseIterator = IteratorUtils.concat(allResults);

        if (inlineDependencies) {
            //optimize by applying latest and distinct when asking for dependencies

            if (!isLatest() && !isDistinct()) {
                //nothing
            } else if (!isLatest() && isDistinct()) {
                baseIterator = IteratorBuilder.of(baseIterator, session).distinct(
                        NutsFunction.of(
                                (NutsId nutsId) -> nutsId.getLongId()
                                        .toString(), "getLongId")).iterator();
            } else if (isLatest() && isDistinct()) {
                NutsIterator<NutsId> curr = baseIterator;
                baseIterator = IteratorBuilder.ofSupplier(() -> {
                            Map<String, NutsId> visited = new LinkedHashMap<>();
                            while (curr.hasNext()) {
                                NutsId nutsId = curr.next();
                                String k = nutsId.getShortName();
                                NutsId old = visited.get(k);
                                if (old == null || old.getVersion().isBlank() || old.getVersion().compareTo(nutsId.getVersion()) < 0) {
                                    visited.put(k, nutsId);
                                }
                            }
                            return visited.values().iterator();
                        }, e->NutsDescribables.resolveOrDestruct(curr,elems)
                                .asSafeObject(true).builder()
                                .set("latest", true)
                                .set("distinct", true)
                                .build(),
                        session).build();
            } else if (isLatest() && !isDistinct()) {
                NutsIterator<NutsId> curr = baseIterator;
                baseIterator = IteratorBuilder.ofSupplier(() -> {
                            Map<String, List<NutsId>> visited = new LinkedHashMap<>();
                            while (curr.hasNext()) {
                                NutsId nutsId = curr.next();
                                String k = nutsId.getShortName();
                                List<NutsId> oldList = visited.get(k);
                                NutsId old = oldList == null ? null : oldList.get(0);
                                if (old == null || old.getVersion().isBlank() || old.getVersion().compareTo(nutsId.getVersion()) < 0) {
                                    visited.put(k, new ArrayList<>(Arrays.asList(nutsId)));
                                } else if (old.getVersion().compareTo(nutsId.getVersion()) == 0) {
                                    oldList.add(nutsId);
                                }
                            }
                            return IteratorBuilder.ofFlatMap(NutsIterator.of(visited.values().iterator(), "visited"), session).build();
                        }, e -> NutsDescribables.resolveOrDestruct(curr,elems)
                                .asSafeObject(true).builder()
                                .set("latest", true)
                                .set("duplicates", true)
                                .build(),
                                session)
                        .build();
            }

            //now include dependencies
            NutsIterator<NutsId> curr = baseIterator;
            baseIterator = IteratorBuilder.of(curr, session)
                    .flatMap(
                            NutsFunction.of(
                                    x -> IteratorBuilder.of(
                                            toFetch().setId(x).setContent(false)
                                                    .setDependencies(true).getResultDefinition().getDependencies().transitiveWithSource().iterator(),
                                            session).build(), "getDependencies")
                    ).map(NutsFunction.of(NutsDependency::toId, "DependencyToId"))
                    .build();
        }

        if (!isLatest() && !isDistinct()) {
            //nothing
        } else if (!isLatest() && isDistinct()) {
            baseIterator = IteratorBuilder.of(baseIterator, session).distinct(
                    NutsFunction.of((NutsId nutsId) -> nutsId.getLongId()
                            .toString(), "getLongId()")
            ).iterator();
        } else if (isLatest() && isDistinct()) {
            NutsIterator<NutsId> curr = baseIterator;
            String fromName = curr.toString();
            baseIterator = IteratorBuilder.ofSupplier(() -> {
                        Map<String, NutsId> visited = new LinkedHashMap<>();
                        while (curr.hasNext()) {
                            NutsId nutsId = curr.next();
                            String k = nutsId.getShortName();
                            NutsId old = visited.get(k);
                            if (old == null || old.getVersion().isBlank() || old.getVersion().compareTo(nutsId.getVersion()) < 0) {
                                visited.put(k, nutsId);
                            }
                        }
                        return visited.values().iterator();
                    },
                    e -> NutsDescribables.resolveOrDestruct(curr,elems)
                            .asSafeObject(true).builder()
                            .set("latest", true)
                            .set("distinct", true)
                            .build(), session).build();
        } else if (isLatest() && !isDistinct()) {
            NutsIterator<NutsId> curr = baseIterator;
            baseIterator = IteratorBuilder.ofSupplier(() -> {
                        Map<String, List<NutsId>> visited = new LinkedHashMap<>();
                        while (curr.hasNext()) {
                            NutsId nutsId = curr.next();
                            String k = nutsId.getShortName();
                            List<NutsId> oldList = visited.get(k);
                            NutsId old = oldList == null ? null : oldList.get(0);
                            if (old == null || old.getVersion().isBlank() || old.getVersion().compareTo(nutsId.getVersion()) < 0) {
                                visited.put(k, new ArrayList<>(Arrays.asList(nutsId)));
                            } else if (old.getVersion().compareTo(nutsId.getVersion()) == 0) {
                                oldList.add(nutsId);
                            }
                        }
                        return IteratorBuilder.ofFlatMap(NutsIterator.of(visited.values().iterator(), "visited"), session).build();
                    },
                    e -> NutsDescribables.resolveOrDestruct(curr,elems)
                            .asSafeObject(true).builder()
                            .set("latest", true)
                            .set("duplicates", true)
                            .build(),
                    session).build();
        }

        if (isSorted()) {
            baseIterator = IteratorUtils.sort(baseIterator, comparator, false);
        }

        return baseIterator;
    }

//    public NutsIterator<NutsDependency> findIterator2(DefaultNutsSearch search) {
//        List<NutsIterator<NutsDependency>> allResults = new ArrayList<>();
//        checkSession();
//        NutsWorkspace ws = getSession().getWorkspace();
//        NutsSession session = search.getSession();
//        NutsWorkspaceUtils.checkSession(ws, session);
//        NutsIdFilter sIdFilter = search.getIdFilter();
//        NutsRepositoryFilter sRepositoryFilter = search.getRepositoryFilter();
//        NutsDescriptorFilter sDescriptorFilter = search.getDescriptorFilter();
//        String[] regularIds = search.getRegularIds();
//        NutsFetchStrategy fetchMode = NutsWorkspaceHelper.validate(session.getFetchStrategy());
//        InstalledVsNonInstalledSearch installedVsNonInstalledSearch = new InstalledVsNonInstalledSearch(
//                search.isSearchInInstalled(),
//                search.isSearchInOtherRepositories()
//        );
//        NutsWorkspaceUtils wu = NutsWorkspaceUtils.of(session);
//
//        if (regularIds.length > 0) {
//            for (String id : regularIds) {
//                NutsId nutsId = ws.id().parser().parse(id);
//                if (nutsId != null) {
//                    NutsDependency dep = nutsId.toDependency();
//                    List<NutsId> nutsId2 = new ArrayList<>();
//                    if (NutsBlankable.isBlank(nutsId.getGroupId())) {
//                        if (nutsId.getArtifactId().equals("nuts")) {
//                            nutsId2.add(nutsId.builder().setGroupId("net.thevpc.nuts").build());
//                        } else {
//                            for (String aImport : ws.imports().getAll()) {
//                                nutsId2.add(nutsId.builder().setGroupId(aImport).build());
//                            }
//                        }
//                    } else {
//                        nutsId2.add(nutsId);
//                    }
//                    List<NutsIterator<NutsDependency>> coalesce = new ArrayList<>();
//                    NutsSession finalSession = session;
//                    for (NutsId nutsId1 : nutsId2) {
//                        List<NutsIterator<NutsDependency>> idLookup = new ArrayList<>();
//                        NutsIdFilter idFilter2 = ws.filters().all(sIdFilter,
//                                ws.id().filter().byName(nutsId1.getFullName())
//                        );
//                        NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(nutsId1.getProperties(),
//                                idFilter2, sDescriptorFilter, ws));
//                        for (NutsRepositoryAndFetchMode repoAndMode : wu.filterRepositoryAndFetchModes(
//                                NutsRepositorySupportedAction.SEARCH, nutsId1, sRepositoryFilter, fetchMode, session,
//                                installedVsNonInstalledSearch
//                        )) {
//                            NutsRepositorySPI repoSPI = wu.repoSPI(repoAndMode.getRepository());
//                            idLookup.add(
//                                    IteratorBuilder.ofLazyNamed("searchVersions("
//                                            + repoAndMode.getRepository().getName() + ","
//                                            + repoAndMode.getFetchMode() + "," + sRepositoryFilter + "," + finalSession + ")",
//                                            ()
//                                            -> CoreNutsUtils.itIdToDep(
//                                                    repoSPI.searchVersions().setId(nutsId1).setFilter(filter)
//                                                            .setSession(finalSession)
//                                                            .setFetchMode(repoAndMode.getFetchMode())
//                                                            .getResult(), dep)
//                                    ).safeIgnore().iterator()
//                            );
//                        }
//                        coalesce.add(fetchMode.isStopFast()
//                                ? IteratorUtils.coalesce(idLookup)
//                                : IteratorUtils.concat(idLookup)
//                        );
//                    }
//                    if (nutsId.getGroupId() == null) {
//                        //now will look with *:artifactId pattern
//                        NutsSearchCommand search2 = ws.search()
//                                .setSession(session)
//                                .setRepositoryFilter(search.getRepositoryFilter())
//                                .setDescriptorFilter(search.getDescriptorFilter());
//                        search2.setIdFilter(
//                                ws.id().filter().byName(nutsId.builder().setGroupId("*").build().toString())
//                                        .and(search.getIdFilter())
//                        );
//                        NutsIterator<NutsDependency> extraResult = CoreNutsUtils.itIdToDep(search2.getResultIds().iterator(), nutsId.toDependency());
//                        if (fetchMode.isStopFast()) {
//                            coalesce.add(extraResult);
//                            allResults.add(IteratorUtils.coalesce(coalesce));
//                        } else {
//                            allResults.add(
//                                    IteratorUtils.coalesce(
//                                            Arrays.asList(
//                                                    IteratorUtils.concat(coalesce),
//                                                    extraResult
//                                            )
//                                    )
//                            );
//                        }
//                    } else {
//                        allResults.add(fetchMode.isStopFast()
//                                ? IteratorUtils.coalesce(coalesce)
//                                : IteratorUtils.concat(coalesce)
//                        );
//                    }
//                }
//            }
//        } else {
//            NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(null, sIdFilter, sDescriptorFilter, ws));
//
//            List<NutsIterator<NutsDependency>> all = new ArrayList<>();
//            for (NutsRepositoryAndFetchMode repoAndMode : wu.filterRepositoryAndFetchModes(
//                    NutsRepositorySupportedAction.SEARCH, null, sRepositoryFilter,
//                    fetchMode, session,
//                    installedVsNonInstalledSearch
//            )) {
//                NutsSession finalSession1 = session;
//                all.add(IteratorBuilder.ofLazyNamed("search(" + repoAndMode.getRepository().getName() + ","
//                        + repoAndMode.getFetchMode() + "," + sRepositoryFilter + "," + session + ")",
//                        () -> CoreNutsUtils.itIdToDep(wu.repoSPI(repoAndMode.getRepository()).search()
//                                .setFilter(filter).setSession(finalSession1)
//                                .setFetchMode(repoAndMode.getFetchMode())
//                                .getResult())
//                ).safeIgnore().iterator()
//                );
//            }
//            allResults.add(
//                    fetchMode.isStopFast()
//                    ? IteratorUtils.coalesce(all)
//                    : IteratorUtils.concat(all)
//            );
//        }
//        return IteratorUtils.concat(allResults);
//    }
}
