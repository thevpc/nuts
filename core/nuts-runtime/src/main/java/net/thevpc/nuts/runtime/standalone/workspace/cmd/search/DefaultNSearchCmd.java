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
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.id.filter.NIdFilterOr;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.id.filter.NPatternIdFilter;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.io.util.NInstallStatusIdFilter;
import net.thevpc.nuts.util.NIteratorBuilder;
import net.thevpc.nuts.util.NIteratorUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch.DefaultNFetchCmd;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NInstallStatuses;
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
        t.setFilterCurrentEnvironment(isFilterCurrentEnvironment());
        if (getDisplayOptions().isRequireDefinition()) {
            t.setContent(true);
        }
        //update RepositoryFilter with effective one that takes into consideration
        // id filters and status filters
        DefaultNSearch bs = build();
        t.setRepositoryFilter(bs.getRepositoryFilter());
        return t;
    }

    private NRepositoryFilter createRepositoryFilter(NInstallStatusFilter status, NIdFilter _idFilter) {
//        if(status==null){
//            return null;
//        }
        boolean searchInInstalled = true;
        boolean searchInOtherRepositories = true;
        if (status != null && Arrays.stream(NInstallStatuses.ALL_DEPLOYED).noneMatch(
                x -> status.acceptInstallStatus(x)
        )) {
            searchInInstalled = false;
        }
        if (status != null && Arrays.stream(NInstallStatuses.ALL_UNDEPLOYED).noneMatch(
                x -> status.acceptInstallStatus(x)
        )) {
            searchInOtherRepositories = false;
        }
        List<NRepositoryFilter> otherFilters = new ArrayList<>();

        if (_idFilter != null && _idFilter.getFilterOp() == NFilterOp.AND) {
            searchInOtherRepositories = true;
            for (NFilter subFilter : _idFilter.getSubFilters()) {
                if (subFilter instanceof NInstallStatusIdFilter) {
                    NInstallStatusFilter status2 = ((NInstallStatusIdFilter) subFilter).getInstallStatus();
                    if (searchInInstalled) {
                        if (status != null && Arrays.stream(NInstallStatuses.ALL_DEPLOYED).noneMatch(
                                x -> status2.acceptInstallStatus(x)
                        )) {
                            searchInInstalled = false;
                        }
                    }
                    if (searchInOtherRepositories) {
                        if (status != null && Arrays.stream(NInstallStatuses.ALL_UNDEPLOYED).noneMatch(
                                x -> status2.acceptInstallStatus(x)
                        )) {
                            searchInOtherRepositories = false;
                        }
                    }
                }
                if (subFilter instanceof NRepositoryFilter) {
                    otherFilters.add((NRepositoryFilter) subFilter);
                }
            }
        }

        NRepositoryFilters repository = NRepositoryFilters.of();
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
        NRepositoryFilter r = otherFilters.get(0);
        for (int i = 1; i < otherFilters.size(); i++) {
            r = r.and(otherFilters.get(i));
        }
        return r;
    }

    //@Override
    private DefaultNSearch build() {
        HashSet<String> someIds = new HashSet<>();
        for (NId id : this.getIds()) {
            someIds.add(id.toString());
        }
        if (this.getIds().size() == 0 && isCompanion()) {
            someIds.addAll(NExtensions.of().getCompanionIds().stream().map(NId::getShortName).collect(Collectors.toList()));
        }
        if (this.getIds().size() == 0 && isRuntime()) {
            someIds.add(NConstants.Ids.NUTS_RUNTIME);
        }
        HashSet<String> goodIds = new HashSet<>();
        HashSet<String> wildcardIds = new HashSet<>();
        for (String someId : someIds) {
            if (NPatternIdFilter.containsWildcad(someId)) {
                wildcardIds.add(someId);
            } else {
                goodIds.add(someId);
            }
        }
        NIdFilter idFilter0 = getIdFilter();
        if (idFilter0 instanceof NPatternIdFilter) {
            NPatternIdFilter f = (NPatternIdFilter) idFilter0;
            if (!f.isWildcard()) {
                goodIds.add(f.getId().toString());
                idFilter0 = null;
            }
        }
        if (idFilter0 instanceof NIdFilterOr) {
            List<NIdFilter> oo = new ArrayList<>(Arrays.asList(((NIdFilterOr) idFilter0).getChildren()));
            boolean someChange = false;
            for (Iterator<NIdFilter> it = oo.iterator(); it.hasNext(); ) {
                NIdFilter curr = it.next();
                if (curr instanceof NPatternIdFilter) {
                    NPatternIdFilter f = (NPatternIdFilter) curr;
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
                    idFilter0 = NIdFilters.of().any(oo.toArray(new NIdFilter[0]));
                }
            }
        }

        NDescriptorFilters dfilter = NDescriptorFilters.of();
        NDescriptorFilter _descriptorFilter = dfilter.always();
        NIdFilter _idFilter = NIdFilters.of().always();
        NDependencyFilter depFilter = NDependencyFilters.of().always();
        NRepositoryFilter rfilter = NRepositoryFilters.of().always();
        for (String j : this.getScripts()) {
            if (!NBlankable.isBlank(j)) {
                if (CoreStringUtils.containsTopWord(j, "descriptor")) {
                    _descriptorFilter = _descriptorFilter.and(dfilter.parse(j));
                } else if (CoreStringUtils.containsTopWord(j, "dependency")) {
                    depFilter = depFilter.and(NDependencyFilters.of().parse(j));
                } else {
                    _idFilter = _idFilter.and(NIdFilters.of().parse(j));
                }
            }
        }
        NDescriptorFilter packs = dfilter.byPackaging(getPackaging());
        NDescriptorFilter archs = dfilter.byArch(getArch());
        _descriptorFilter = _descriptorFilter.and(packs).and(archs);

        NRepositoryFilter _repositoryFilter = rfilter.and(this.getRepositoryFilter());
        _descriptorFilter = _descriptorFilter.and(this.getDescriptorFilter());

        _idFilter = _idFilter.and(idFilter0);
        if (getInstallStatus() != null) {
            _idFilter = _idFilter.and(NIdFilters.of().byInstallStatus(getInstallStatus()));
        }
        if (getDefaultVersions() != null) {
            _idFilter = _idFilter.and(NIdFilters.of().byDefaultVersion(getDefaultVersions()));
        }
        if (execType != null) {
            switch (execType) {
                case "lib": {
                    _descriptorFilter = _descriptorFilter.and(dfilter.byFlag(NDescriptorFlag.EXEC).neg());
                    break;
                }
                case "exec": {
                    _descriptorFilter = _descriptorFilter.and(dfilter.byFlag(NDescriptorFlag.EXEC));
                    break;
                }
                case "app": {
                    _descriptorFilter = _descriptorFilter.and(dfilter.byFlag(NDescriptorFlag.APP));
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
                    lockedIds.stream().map(NId::getFullName).toArray(String[]::new)
            ));
        }
        if (!wildcardIds.isEmpty()) {
            _idFilter = _idFilter.and(NIdFilters.of().byName(wildcardIds.toArray(new String[0])));
        }
        NRepositoryFilter extraRepositoryFilter = createRepositoryFilter(installStatus, _idFilter);
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
        return new DefaultNSearch(
                goodIds.toArray(new String[0]),
                _repositoryFilter,
                _idFilter, _descriptorFilter
        );
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
//        DefaultNSearch build = build();

    /// /        build.getOptions().session(build.getOptions().getSession().copy().trace(print));
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
//        NutsWorkspace ws = getSession().getWorkspace();
//        NSession _session = this.getSession();
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
//        NutsWorkspace ws = getSession().getWorkspace();
//        NSession _session = this.getSession();
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
    public NIterator<NId> getResultIdIteratorBase(Boolean forceInlineDependencies) {
        boolean inlineDependencies = forceInlineDependencies == null ? isInlineDependencies() : forceInlineDependencies;
        DefaultNSearch search = build();

        List<NIterator<? extends NId>> allResults = new ArrayList<>();
        NSession session = NSession.of();
        NIdFilter sIdFilter = search.getIdFilter();
        NRepositoryFilter sRepositoryFilter = search.getRepositoryFilter();
        NDescriptorFilter sDescriptorFilter = search.getDescriptorFilter();
        String[] regularIds = search.getRegularIds();
        NFetchStrategy fetchMode = NWorkspaceHelper.validate(session.getFetchStrategy().orDefault());
//        InstalledVsNonInstalledSearch installedVsNonInstalledSearch = new InstalledVsNonInstalledSearch(
//                search.isSearchInInstalled(),
//                search.isSearchInOtherRepositories()
//        );
        Set<NRepository> consideredRepos = new HashSet<>();
        NWorkspaceUtils wu = NWorkspaceUtils.of();
        NElements elems = NElements.of();
        if (regularIds.length > 0) {
            for (String id : regularIds) {
                NId nutsId = NId.get(id).get();
                if (nutsId != null) {
                    List<NId> nutsId2 = new ArrayList<>();
                    if (NBlankable.isBlank(nutsId.getGroupId())) {
                        if (nutsId.getArtifactId().equals("nuts")) {
                            nutsId2.add(nutsId.builder().setGroupId("net.thevpc.nuts").build());
                        } else {
                            //check if It's already installed
                            List<NId> installedIds = Collections.emptyList();
                            if (!nutsId.getArtifactId().contains("*")) {
                                NRepositorySPI repoSPI = wu
                                        .repoSPI(NWorkspaceExt.of().getInstalledRepository());
                                NIterator<NId> it = repoSPI.search().setFetchMode(NFetchMode.LOCAL).setFilter(NIdFilters.of().byName(
                                        nutsId.builder().setGroupId("*").build().toString()
                                )).getResult();
                                installedIds = NIteratorUtils.toList(it);
                            }
                            if (!installedIds.isEmpty()) {
                                nutsId2.addAll(installedIds);
                            } else {
                                for (String aImport : NWorkspace.of().getAllImports()) {
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
                    List<NIterator<? extends NId>> toConcat = new ArrayList<>();
                    for (NId nutsId1 : nutsId2) {
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
                        NIdFilter idFilter2 = NFilters.of().all(sIdFilter,
                                NIdFilters.of().byName(nutsIdNonLatest.getFullName())
                        );
                        NIdFilter filter = CoreFilterUtils.simplify(CoreFilterUtils.idFilterOf(nutsIdNonLatest.getProperties(),
                                idFilter2, sDescriptorFilter));
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
                    if (nutsId.getGroupId() == null) {
                        //now will look with *:artifactId pattern
                        NSearchCmd search2 = NSearchCmd.of()
                                .setRepositoryFilter(search.getRepositoryFilter())
                                .setDescriptorFilter(search.getDescriptorFilter());
                        search2.setIdFilter(
                                NIdFilters.of().byName(nutsId.builder().setGroupId("*").build().toString())
                                        .and(search.getIdFilter())
                        );
                        NIterator<NId> extraResult = search2.getResultIds().iterator();
                        allResults.add(
                                fetchMode.isStopFast() ?
                                        NIteratorUtils.coalesce(NIteratorUtils.concat(toConcat), extraResult)
                                        : NIteratorUtils.concat(NIteratorUtils.concat(toConcat), extraResult)
                        );
                    } else {
                        allResults.add(NIteratorUtils.concat(toConcat));
                    }
                }
            }
        } else {
            NIdFilter filter = CoreFilterUtils.simplify(CoreFilterUtils.idFilterOf(null, sIdFilter, sDescriptorFilter));

            List<NIterator<? extends NId>> all = new ArrayList<>();
            for (NRepositoryAndFetchMode repoAndMode : wu.filterRepositoryAndFetchModes(
                    NRepositorySupportedAction.SEARCH, null, sRepositoryFilter,
                    fetchMode
            )) {
                consideredRepos.add(repoAndMode.getRepository());
//                NSession finalSession1 = session;
                all.add(
                        NIteratorBuilder.of(wu.repoSPI(repoAndMode.getRepository()).search()
                                        .setFilter(filter)
                                        .setFetchMode(repoAndMode.getFetchMode())
                                        .getResult()).safeIgnore()
                                .named(
                                        elems.ofObjectBuilder()
                                                .set("description", "searchRepository")
                                                .set("repository", repoAndMode.getRepository().getName())
                                                .set("fetchMode", repoAndMode.getFetchMode().id())
                                                .set("filter", NEDesc.describeResolveOrDestruct(filter))
                                                .build()
                                ).iterator()
                );
            }
            allResults.add(
                    fetchMode.isStopFast()
                            ? NIteratorUtils.coalesce(all)
                            : NIteratorUtils.concat(all)
            );
        }
        NIterator<NId> baseIterator = NIteratorUtils.concat(allResults);
        NElement described = baseIterator.describe();
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

//    public NutsIterator<NutsDependency> findIterator2(DefaultNSearch search) {
//        List<NutsIterator<NutsDependency>> allResults = new ArrayList<>();
//        NutsWorkspace ws = getSession().getWorkspace();
//        NSession session = search.getSession();
//        NutsIdFilter sIdFilter = search.getIdFilter();
//        NutsRepositoryFilter sRepositoryFilter = search.getRepositoryFilter();
//        NutsDescriptorFilter sDescriptorFilter = search.getDescriptorFilter();
//        String[] regularIds = search.getRegularIds();
//        NutsFetchStrategy fetchMode = NutsWorkspaceHelper.validate(session.getFetchStrategy());
//        InstalledVsNonInstalledSearch installedVsNonInstalledSearch = new InstalledVsNonInstalledSearch(
//                search.isSearchInInstalled(),
//                search.isSearchInOtherRepositories()
//        );
//        NutsWorkspaceUtils wu = NutsWorkspaceUtils.of();
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
//                    NSession finalSession = session;
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
//                NSession finalSession1 = session;
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
