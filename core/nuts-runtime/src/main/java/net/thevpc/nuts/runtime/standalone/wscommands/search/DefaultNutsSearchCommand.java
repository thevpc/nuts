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
package net.thevpc.nuts.runtime.standalone.wscommands.search;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.commands.repo.NutsRepositorySupportedAction;
import net.thevpc.nuts.runtime.core.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.core.filters.NutsPatternIdFilter;
import net.thevpc.nuts.runtime.core.filters.id.NutsIdFilterOr;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.DefaultNutsSearch;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.runtime.bundles.io.NutsInstallStatusIdFilter;
import net.thevpc.nuts.runtime.bundles.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.bundles.iter.IteratorUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.fetch.DefaultNutsFetchCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.NutsInstallStatuses;
import net.thevpc.nuts.runtime.standalone.wscommands.NutsRepositoryAndFetchMode;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.util.*;
import java.util.stream.Collectors;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;

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
    private NutsRepositoryFilter createRepositoryFilter(NutsInstallStatusFilter status,NutsIdFilter _idFilter,NutsSession session){
//        if(status==null){
//            return null;
//        }
        boolean searchInInstalled = true;
        boolean searchInOtherRepositories =true;
        if(status!=null && Arrays.stream(NutsInstallStatuses.ALL_DEPLOYED).noneMatch(
                x->status.acceptInstallStatus(x, session)
        )){
            searchInInstalled=false;
        }
        if(status!=null && Arrays.stream(NutsInstallStatuses.ALL_UNDEPLOYED).noneMatch(
                x->status.acceptInstallStatus(x, session)
        )){
            searchInOtherRepositories=false;
        }
        List<NutsRepositoryFilter> otherFilters=new ArrayList<>();

        if (_idFilter!=null && _idFilter.getFilterOp() == NutsFilterOp.AND) {
            searchInOtherRepositories = true;
            for (NutsFilter subFilter : _idFilter.getSubFilters()) {
                if (subFilter instanceof NutsInstallStatusIdFilter) {
                    NutsInstallStatusFilter status2 = ((NutsInstallStatusIdFilter) subFilter).getInstallStatus();
                    if(searchInInstalled){
                        if(status!=null && Arrays.stream(NutsInstallStatuses.ALL_DEPLOYED).noneMatch(
                                x->status2.acceptInstallStatus(x, session)
                        )){
                            searchInInstalled=false;
                        }
                    }
                    if(searchInOtherRepositories) {
                        if (status != null && Arrays.stream(NutsInstallStatuses.ALL_UNDEPLOYED).noneMatch(
                                x -> status2.acceptInstallStatus(x, session)
                        )) {
                            searchInOtherRepositories = false;
                        }
                    }
                }
                if(subFilter instanceof NutsRepositoryFilter){
                    otherFilters.add((NutsRepositoryFilter) subFilter);
                }
            }
        }

        if(!searchInInstalled && searchInOtherRepositories) {
            otherFilters.add(session.filters().repository().installedRepo().neg());
        }else if(searchInInstalled && !searchInOtherRepositories){
            otherFilters.add(session.filters().repository().installedRepo());
        }else if(!searchInInstalled && !searchInOtherRepositories){
            otherFilters.add(session.filters().repository().never());
        }
        if(otherFilters.isEmpty()){
            return null;
        }
        NutsRepositoryFilter r = otherFilters.get(0);
        for (int i = 1; i < otherFilters.size(); i++) {
            r=r.and(otherFilters.get(i));
        }
        return r;
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


    //@Override
    private DefaultNutsSearch build() {
        checkSession();
        NutsSession ws = getSession();
        HashSet<String> someIds = new HashSet<>();
        for (NutsId id : this.getIds()) {
            someIds.add(id.toString());
        }
        if (this.getIds().length == 0 && isCompanion()) {
            someIds.addAll(session.extensions().getCompanionIds().stream().map(NutsId::getShortName).collect(Collectors.toList()));
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
            for (Iterator<NutsIdFilter> it = oo.iterator(); it.hasNext();) {
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
                    idFilter0 = session.id().filter().any(oo.toArray(new NutsIdFilter[0]));
                }
            }
        }

        NutsDescriptorFilter _descriptorFilter = ws.descriptor().filter().always();
        NutsIdFilter _idFilter = session.id().filter().always();
        NutsDependencyFilter depFilter = ws.dependency().filter().always();
        NutsRepositoryFilter rfilter = session.repos().filter().always();
        for (String j : this.getScripts()) {
            if (!NutsBlankable.isBlank(j)) {
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
        NutsRepositoryFilter extraRepositoryFilter = createRepositoryFilter(installStatus, _idFilter, getSession());
        if(extraRepositoryFilter!=null){
            _repositoryFilter=_repositoryFilter.and(extraRepositoryFilter);
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
//        return CoreCommonUtils.toList(applyPrintDecoratorIterOfNutsId(curr.iterator(), print));
//    }
//    private NutsCollectionStream<NutsId> applyVersionFlagFilters(Iterator<NutsId> curr, boolean print) {
//        if (!isLatest() && !isDistinct()) {
//            return buildCollectionResult(curr, print);
//            //nothing
//        } else if (!isLatest() && isDistinct()) {
//            return buildCollectionResult(IteratorBuilder.of(curr).distinct((NutsId nutsId) -> nutsId.getLongNameId()
//                    //                            .setAlternative(nutsId.getAlternative())
//                    .toString()).iterator(), print);
//        } else if (isLatest() && isDistinct()) {
//            Iterator<NutsId> nn = IteratorUtils.supplier(() -> {
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
//            Iterator<NutsId> nn = IteratorUtils.supplier(() -> {
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
//                return IteratorUtils.name("latestAndDuplicate", IteratorUtils.flatCollection((Iterator) visited.values().iterator()));
//            }, "latestAndDuplicate");
//            return buildCollectionResult(nn, print);
//        }
//        throw new NutsUnexpectedException(getSession());
//    }
//    private NutsCollectionStream<NutsDependency> applyVersionFlagFilters2(Iterator<NutsDependency> curr, boolean print) {
//        if (!isLatest() && !isDistinct()) {
//            return buildCollectionResult(curr, print);
//            //nothing
//        } else if (!isLatest() && isDistinct()) {
//            return buildCollectionResult(IteratorBuilder.of(curr).distinct((NutsDependency nutsId) -> nutsId.toId().getLongNameId()
//                    //                            .setAlternative(nutsId.getAlternative())
//                    .toString()).iterator(), print);
//        } else if (isLatest() && isDistinct()) {
//            Iterator<NutsDependency> nn = IteratorUtils.supplier(() -> {
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
//            Iterator<NutsDependency> nn = IteratorUtils.supplier(() -> {
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
//                return IteratorUtils.name("latestAndDuplicate", IteratorUtils.flatCollection((Iterator) visited.values().iterator()));
//            }, "latestAndDuplicate");
//            return buildCollectionResult(nn, print);
//        }
//        throw new NutsUnexpectedException(getSession());
//    }
//    protected NutsCollectionStream<NutsDependency> getResultDependenciesBase(boolean print, boolean sort) {
//        DefaultNutsSearch build = build();
////        build.getOptions().session(build.getOptions().getSession().copy().trace(print));
//        Iterator<NutsDependency> base0 = findIterator2(build);
//        if (base0 == null) {
//            return buildCollectionResult(IteratorUtils.emptyIterator(), print);
//        }
//        if (!isLatest() && !isDistinct() && !sort && !isInlineDependencies()) {
//            return buildCollectionResult(base0, print);
//        }
//        NutsCollectionStream<NutsDependency> a = applyVersionFlagFilters2(base0, false);
//        Iterator<NutsDependency> curr = a.iterator();
//        if (isInlineDependencies()) {
//            if (!isBasePackage()) {
//                curr = Arrays.asList(findDependencies2(a.list())).iterator();
//            } else {
//                List<Iterator<NutsDependency>> it = new ArrayList<>();
//                Iterator<NutsDependency> a0 = a.iterator();
//                List<NutsDependency> base = new ArrayList<>();
//                it.add(new NamedIterator<NutsDependency>("tee(" + a0 + ")") {
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
//                it.add(new NamedIterator<NutsDependency>("ResolveDependencies") {
//                    Iterator<NutsDependency> deps = null;
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
    public Iterator<NutsId> getResultIdIteratorBase(Boolean forceInlineDependencies) {
        boolean inlineDependencies=forceInlineDependencies==null?isInlineDependencies():forceInlineDependencies;
        DefaultNutsSearch search = build();

        List<Iterator<? extends NutsId>> allResults = new ArrayList<>();
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
        if (regularIds.length > 0) {
            for (String id : regularIds) {
                NutsId nutsId = session.id().parser().parse(id);
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
                                Iterator<NutsId> it = repoSPI.search().setFetchMode(NutsFetchMode.LOCAL).setFilter(session.filters().id().byName(
                                        nutsId.builder().setGroupId("*").build().toString()
                                )).setSession(getSession()).getResult();
                                installedIds = IteratorUtils.toList(it);
                            }
                            if (!installedIds.isEmpty()) {
                                nutsId2.addAll(installedIds);
                            } else {
                                for (String aImport : session.imports().getAll()) {
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
                    List<Iterator<? extends NutsId>> toConcat = new ArrayList<>();
                    for (NutsId nutsId1 : nutsId2) {
                        NutsIdFilter idFilter2 = session.filters().all(sIdFilter,
                                session.id().filter().byName(nutsId1.getFullName())
                        );
                        NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(nutsId1.getProperties(),
                                idFilter2, sDescriptorFilter, session));
                        List<NutsRepositoryAndFetchMode> repositoryAndFetchModes = wu.filterRepositoryAndFetchModes(
                                NutsRepositorySupportedAction.SEARCH, nutsId1, sRepositoryFilter, fetchMode, session
                        );

                        List<Iterator<? extends NutsId>> idLocal = new ArrayList<>();
                        List<Iterator<? extends NutsId>> idRemote = new ArrayList<>();
                        for (NutsFetchMode fm : new NutsFetchMode[]{NutsFetchMode.LOCAL, NutsFetchMode.REMOTE}) {
                            List<Iterator<? extends NutsId>> idLookup=fm==NutsFetchMode.LOCAL?idLocal:idRemote;
                            for (NutsRepositoryAndFetchMode repoAndMode : repositoryAndFetchModes) {
                                if(repoAndMode.getFetchMode()==fm) {
                                    consideredRepos.add(repoAndMode.getRepository());
                                    NutsRepositorySPI repoSPI = wu.repoSPI(repoAndMode.getRepository());
                                    idLookup.add(IteratorBuilder.ofLazyNamed("searchVersions("
                                                    + repoAndMode.getRepository().getName() + ","
                                                    + repoAndMode.getFetchMode() + "," + sRepositoryFilter + "," + session + ")",
                                            () -> repoSPI.searchVersions().setId(nutsId1).setFilter(filter)
                                                    .setSession(session)
                                                    .setFetchMode(repoAndMode.getFetchMode())
                                                    .getResult()).safeIgnore().iterator()
                                    );
                                }
                            }
                        }
                        toConcat.add(fetchMode.isStopFast()
                                ? IteratorUtils.coalesce(IteratorUtils.concat(idLocal),IteratorUtils.concat(idRemote))
                                : IteratorUtils.concatLists(idLocal,idRemote)
                        );
                    }
                    if (nutsId.getGroupId() == null) {
                        //now will look with *:artifactId pattern
                        NutsSearchCommand search2 = session.search()
                                .setSession(session)
                                .setRepositoryFilter(search.getRepositoryFilter())
                                .setDescriptorFilter(search.getDescriptorFilter());
                        search2.setIdFilter(
                                session.id().filter().byName(nutsId.builder().setGroupId("*").build().toString())
                                        .and(search.getIdFilter())
                        );
                        Iterator<NutsId> extraResult = search2.getResultIds().iterator();
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

            List<Iterator<? extends NutsId>> all = new ArrayList<>();
            for (NutsRepositoryAndFetchMode repoAndMode : wu.filterRepositoryAndFetchModes(
                    NutsRepositorySupportedAction.SEARCH, null, sRepositoryFilter,
                    fetchMode, session
            )) {
                consideredRepos.add(repoAndMode.getRepository());
                NutsSession finalSession1 = session;
                all.add(IteratorBuilder.ofLazyNamed("search(" + repoAndMode.getRepository().getName() + ","
                        + repoAndMode.getFetchMode() + "," + sRepositoryFilter + "," + session + ")",
                        () -> wu.repoSPI(repoAndMode.getRepository()).search()
                                .setFilter(filter).setSession(finalSession1)
                                .setFetchMode(repoAndMode.getFetchMode())
                                .getResult()).safeIgnore().iterator()
                );
            }
            allResults.add(
                    fetchMode.isStopFast()
                    ? IteratorUtils.coalesce(all)
                    : IteratorUtils.concat(all)
            );
        }
        Iterator<NutsId> baseIterator = IteratorUtils.concat(allResults);

        if (inlineDependencies) {
            //optimize by applying latest and distinct when asking for dependencies

            if (!isLatest() && !isDistinct()) {
                //nothing
            } else if (!isLatest() && isDistinct()) {
                baseIterator = IteratorBuilder.of(baseIterator).distinct((NutsId nutsId) -> nutsId.getLongId()
                        .toString()).iterator();
            } else if (isLatest() && isDistinct()) {
                Iterator<NutsId> curr = baseIterator;
                String fromName=curr.toString();
                baseIterator = IteratorUtils.supplier(() -> {
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
                }, "latestAndDistinct("+fromName+")");
            } else if (isLatest() && !isDistinct()) {
                Iterator<NutsId> curr = baseIterator;
                String fromName=curr.toString();
                baseIterator = IteratorUtils.supplier(() -> {
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
                    Iterator from = IteratorUtils.flatCollection((Iterator) visited.values().iterator());
                    return IteratorUtils.name("latestAndDuplicate("+fromName+")", from);
                });
            }

            //now include dependencies
            Iterator<NutsId> curr = baseIterator;
            baseIterator = IteratorUtils.flatMap(curr,
                    x -> IteratorUtils.map(
                            toFetch().setId(x).setContent(false).setDependencies(true).getResultDefinition().getDependencies().mergedDependencies().iterator(),
                            IteratorUtils.namedFunction(NutsDependency::toId, "DependencyToId")
                    ));
        }

        if (!isLatest() && !isDistinct()) {
            //nothing
        } else if (!isLatest() && isDistinct()) {
            baseIterator = IteratorBuilder.of(baseIterator).distinct((NutsId nutsId) -> nutsId.getLongId()
                    .toString()).iterator();
        } else if (isLatest() && isDistinct()) {
            Iterator<NutsId> curr = baseIterator;
            String fromName=curr.toString();
            baseIterator = IteratorUtils.supplier(() -> {
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
            }, "LatestAndDistinct("+fromName+")");
        } else if (isLatest() && !isDistinct()) {
            Iterator<NutsId> curr = baseIterator;
            String fromName=curr.toString();
            baseIterator = IteratorUtils.supplier(() -> {
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
                return IteratorUtils.flatCollection((Iterator) visited.values().iterator());
            }, "LatestAndDuplicate("+fromName+")");
        }

        if (isSorted()) {
            baseIterator = IteratorUtils.sort(baseIterator, comparator, false);
        }

        return baseIterator;
    }

//    public Iterator<NutsDependency> findIterator2(DefaultNutsSearch search) {
//        List<Iterator<NutsDependency>> allResults = new ArrayList<>();
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
//                    List<Iterator<NutsDependency>> coalesce = new ArrayList<>();
//                    NutsSession finalSession = session;
//                    for (NutsId nutsId1 : nutsId2) {
//                        List<Iterator<NutsDependency>> idLookup = new ArrayList<>();
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
//                        Iterator<NutsDependency> extraResult = CoreNutsUtils.itIdToDep(search2.getResultIds().iterator(), nutsId.toDependency());
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
//            List<Iterator<NutsDependency>> all = new ArrayList<>();
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
