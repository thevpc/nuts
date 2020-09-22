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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.main.wscommands;

import net.vpc.app.nuts.core.NutsRepositorySupportedAction;
import net.vpc.app.nuts.runtime.AbstractNutsResultList;
import net.vpc.app.nuts.runtime.DefaultNutsSearch;
import net.vpc.app.nuts.runtime.ext.DefaultNutsWorkspaceExtensionManager;
import net.vpc.app.nuts.runtime.NutsPatternIdFilter;
import net.vpc.app.nuts.runtime.filters.id.*;
import net.vpc.app.nuts.core.NutsWorkspaceExt;
import net.vpc.app.nuts.runtime.util.*;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.filters.dependency.NutsDependencyJavascriptFilter;
import net.vpc.app.nuts.runtime.filters.dependency.NutsDependencyOptionFilter;
import net.vpc.app.nuts.runtime.filters.dependency.NutsDependencyScopeFilter;
import net.vpc.app.nuts.runtime.filters.descriptor.NutsDescriptorFilterArch;
import net.vpc.app.nuts.runtime.filters.descriptor.NutsDescriptorFilterPackaging;
import net.vpc.app.nuts.runtime.filters.descriptor.NutsDescriptorJavascriptFilter;
import net.vpc.app.nuts.runtime.filters.repository.DefaultNutsRepositoryFilter;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

import java.io.File;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.vpc.app.nuts.runtime.filters.CoreFilterUtils;
import net.vpc.app.nuts.runtime.format.NutsIdFormatHelper;
import net.vpc.app.nuts.runtime.format.NutsDisplayProperty;
import net.vpc.app.nuts.runtime.format.NutsFetchDisplayOptions;

import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import net.vpc.app.nuts.runtime.util.io.NutsIdFilterTopInstalled;
import net.vpc.app.nuts.runtime.util.iter.IteratorBuilder;
import net.vpc.app.nuts.runtime.util.iter.IteratorUtils;
import net.vpc.app.nuts.runtime.util.iter.NamedIterator;
import net.vpc.app.nuts.runtime.wscommands.AbstractNutsSearchCommand;

/**
 * @author vpc
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
            someIds.addAll(Arrays.asList(NutsWorkspaceExt.of(ws).getCompanionIds()));
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
                    idFilter0 = new NutsIdFilterOr(oo.toArray(new NutsIdFilter[0]));
                }
            }
        }

        NutsDescriptorFilter _descriptorFilter = null;
        NutsIdFilter _idFilter = null;
        NutsDependencyFilter depFilter = null;
        NutsRepositoryFilter rfilter = null;
        for (String j : this.getScripts()) {
            if (!CoreStringUtils.isBlank(j)) {
                if (CoreStringUtils.containsTopWord(j, "descriptor")) {
                    _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, NutsDescriptorJavascriptFilter.valueOf(j));
                } else if (CoreStringUtils.containsTopWord(j, "dependency")) {
                    depFilter = CoreFilterUtils.AndSimplified(depFilter, NutsDependencyJavascriptFilter.valueOf(j));
                } else {
                    _idFilter = CoreFilterUtils.AndSimplified(_idFilter, NutsJavascriptIdFilter.valueOf(j));
                }
            }
        }
        NutsDescriptorFilter packs = null;
        for (String v : this.getPackaging()) {
            packs = CoreFilterUtils.OrSimplified(packs, new NutsDescriptorFilterPackaging(v));
        }
        NutsDescriptorFilter archs = null;
        for (String v : this.getArch()) {
            archs = CoreFilterUtils.OrSimplified(archs, new NutsDescriptorFilterArch(v));
        }

        _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, packs, archs);

        if (this.getRepositories().length > 0) {
            rfilter = new DefaultNutsRepositoryFilter(Arrays.asList(this.getRepositories())).simplify();
        }

        NutsRepositoryFilter _repositoryFilter = CoreFilterUtils.AndSimplified(rfilter, this.getRepositoryFilter());
        _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, this.getDescriptorFilter());

        _idFilter = CoreFilterUtils.AndSimplified(_idFilter, idFilter0);
        if (getInstallStatus() != null) {
            _idFilter = CoreFilterUtils.AndSimplified(_idFilter, new NutsIdFilterTopInstalled(getInstallStatus()));
        }
        if (getDefaultVersions() != null) {
            _idFilter = CoreFilterUtils.AndSimplified(_idFilter, new NutsDefaultVersionIdFilter(getDefaultVersions()));
        }
        if (execType != null) {
            switch (execType) {
                case "lib": {
                    _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new NutsExecStatusIdFilter(false, false));
                    break;
                }
                case "exec": {
                    _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new NutsExecStatusIdFilter(true, null));
                    break;
                }
                case "app": {
                    _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new NutsExecStatusIdFilter(null, true));
                    break;
                }
                case "extension": {
                    _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new NutsExecExtensionFilter(
                            targetApiVersion == null ? null : ws.id().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build())
                    );
                    break;
                }
                case "runtime": {
                    _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new NutsExecRuntimeFilter(
                            targetApiVersion == null ? null : ws.id().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build(),
                             false
                    )
                    );
                    break;
                }
                case "companions": {
                    _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new NutsExecCompanionFilter(
                            targetApiVersion == null ? null : ws.id().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build(),
                            NutsWorkspaceExt.of(ws).getCompanionIds()
                    )
                    );
                    break;
                }
            }
        } else {
            if (targetApiVersion != null) {
                _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new BootAPINutsDescriptorFilter(
                        ws.id().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build().getVersion()
                )
                );
            }
        }
        if (!lockedIds.isEmpty()) {
            _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new NutsLockedIdExtensionFilter(
                    lockedIds.toArray(new NutsId[0])
            )
            );
        }
        if (!wildcardIds.isEmpty()) {
            for (String wildcardId : wildcardIds) {
                _idFilter = CoreNutsUtils.simplify(new NutsIdFilterOr(_idFilter, new NutsPatternIdFilter(ws.id().parse(wildcardId))));
            }
        }
        return new DefaultNutsSearch(
                goodIds.toArray(new String[0]),
                _repositoryFilter,
                _idFilter, _descriptorFilter, getSession());
    }

    @Override
    public NutsFetchCommand toFetch() {
        NutsFetchCommand t = new DefaultNutsFetchCommand(ws).copyFromDefaultNutsQueryBaseOptions(this)
                .setSession(getSession());
        if (getDisplayOptions().isRequireDefinition()) {
            t.setContent(true);
        }
        return t;
    }

    @Override
    public NutsResultList<NutsId> getResultIds() {
        return getResultIdsBase(isPrintResult(), sorted);
    }

    private NutsResultList<NutsDefinition> getResultDefinitionsBase(boolean print, boolean sort, boolean content, boolean effective) {
        return new NutsDefinitionNutsResult(ws, resolveFindIdBase(), print, sort, content, effective);
    }

    @Override
    public NutsResultList<NutsDefinition> getResultDefinitions() {
        return getResultDefinitionsBase(isPrintResult(), sorted, isContent(), isEffective());
    }

    private String resolveFindIdBase() {
        return ids.isEmpty() ? null : ids.get(0) == null ? null : ids.get(0).toString();
    }

    private NutsSession getSearchSession() {
        return getSession();
    }
//    @Deprecated
//    private List<NutsId> applyPrintDecoratorListOfNutsId(List<NutsId> curr, boolean print) {
//        if (!print) {
//            return curr;
//        }
//        return CoreCommonUtils.toList(applyPrintDecoratorIterOfNutsId(curr.iterator(), print));
//    }

//    private Collection<NutsId> applyPrintDecoratorCollectionOfNutsId(Collection<NutsId> curr, boolean print) {
//        if (!print) {
//            return curr;
//        }
//        return CoreCommonUtils.toList(applyPrintDecoratorIterOfNutsId(curr.iterator(), print));
//    }
    private Iterator<NutsId> applyPrintDecoratorIterOfNutsId(Iterator<NutsId> curr, boolean print) {
        return print ? NutsWorkspaceUtils.of(ws).decoratePrint(curr, getSearchSession(), getDisplayOptions()) : curr;
    }

    private NutsCollectionResult<NutsId> applyVersionFlagFilters(Iterator<NutsId> curr, boolean print) {
        if (!isLatest() && !isDistinct()) {
            return buildNutsCollectionSearchResult(applyPrintDecoratorIterOfNutsId(curr, print));
            //nothing
        } else if (!isLatest() && isDistinct()) {
            return buildNutsCollectionSearchResult(
                    applyPrintDecoratorIterOfNutsId(IteratorBuilder.of(curr).distinct((NutsId nutsId) -> nutsId.getLongNameId()
                    //                            .setAlternative(nutsId.getAlternative())
                    .toString()).iterator(), print));
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
            return buildNutsCollectionSearchResult(applyPrintDecoratorIterOfNutsId(nn, print));
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
            return buildNutsCollectionSearchResult(applyPrintDecoratorIterOfNutsId(nn, print));
        }
        throw new NutsUnexpectedException(ws);
    }

    private <T> NutsCollectionResult<T> buildNutsCollectionSearchResult(Iterator<T> o) {
        NutsSession ss = getSearchSession();
//        if (isTraceMonitor()) {
//            o = IteratorUtils.onFinish(o, () -> {
//                        SearchTraceHelper.end(getSearchSession());
//                        ss.setProperty("traceMonitor", traceMonitor);
//                    }
//            );
//        }
        return new NutsCollectionResult(ws, resolveFindIdBase(), o);
    }

    private NutsCollectionResult<NutsId> getResultIdsBase(boolean print, boolean sort) {
        DefaultNutsSearch build = build();
//        build.getOptions().session(build.getOptions().getSession().copy().trace(print));
        Iterator<NutsId> base0 = findIterator(build);
        if (base0 == null) {
            return buildNutsCollectionSearchResult(IteratorUtils.emptyIterator());
        }
        if (!isLatest() && !isDistinct() && !sort && !isInlineDependencies()) {
            return buildNutsCollectionSearchResult(applyPrintDecoratorIterOfNutsId(base0, print));
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
            return buildNutsCollectionSearchResult(applyPrintDecoratorIterOfNutsId(
                    IteratorUtils.sort(applyVersionFlagFilters(curr, false).iterator(), comparator, false),
                     print));
        } else {
            return applyVersionFlagFilters(curr, print);
        }
    }

    private NutsId[] findDependencies(List<NutsId> ids) {
        NutsSession _session = this.getSession() == null ? ws.createSession() : this.getSession();
        NutsDependencyFilter _dependencyFilter = CoreFilterUtils.AndSimplified(
                new NutsDependencyScopeFilter().addScopes(getScope()),
                getOptional() == null ? null : NutsDependencyOptionFilter.valueOf(getOptional()),
                getDependencyFilter()
        );
        NutsIdGraph graph = new NutsIdGraph(CoreNutsUtils.silent(_session), isFailFast());
        return graph.resolveDependencies(ids, _dependencyFilter);
    }

    @Override
    public String getResultNutsPath() {
        return getResultIds().list().stream().map(NutsId::getLongName).collect(Collectors.joining(";"));
    }

    private <T> NutsResultList<T> postProcessResult(IteratorBuilder<T> a) {
        if (isSorted()) {
            a = a.sort(null, isDistinct());
        }
        if (isPrintResult()) {
            a = IteratorBuilder.of(NutsWorkspaceUtils.of(ws).decoratePrint(a.build(), getSearchSession(), getDisplayOptions()));
        }
        return buildNutsCollectionSearchResult(a.build());
    }

    @Override
    public NutsResultList<String> getResultPaths() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, true, isEffective()).iterator())
                .map(x -> (x.getContent() == null || x.getContent().getPath() == null) ? null : x.getContent().getPath().toString())
                .notBlank()
        );
    }

    @Override
    public NutsResultList<String> getResultPathNames() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, true, isEffective()).iterator())
                .map(x -> (x.getContent() == null || x.getContent().getPath() == null) ? null : x.getContent().getPath().getFileName().toString())
                .notBlank());
    }

    @Override
    public NutsResultList<Instant> getResultInstallDates() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, false).iterator())
                .map(x -> (x.getInstallInformation() == null) ? null : x.getInstallInformation().getInstallDate())
                .notNull());
    }

    @Override
    public NutsResultList<String> getResultInstallUsers() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, false).iterator())
                .map(x -> (x.getInstallInformation() == null) ? null : x.getInstallInformation().getInstallUser())
                .notBlank());
    }

    @Override
    public NutsResultList<Path> getResultInstallFolders() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, false).iterator())
                .map(x -> (x.getInstallInformation() == null) ? null : x.getInstallInformation().getInstallFolder())
                .notNull());
    }

    @Override
    public NutsResultList<Path> getResultStoreLocations(NutsStoreLocation location) {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, false).iterator())
                .map(x -> ws.config().getStoreLocation(x.getId(), location))
                .notNull());
    }

    public NutsResultList<String> getResultStatuses() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, true, isEffective()).iterator())
                .map(x
                        -> NutsIdFormatHelper.of(x, getSearchSession())
                        .buildLong().getStatusString()
                )
                .notBlank());
    }

    @Override
    public NutsResultList<String[]> getResultStrings(String[] columns) {
        NutsFetchDisplayOptions oo = new NutsFetchDisplayOptions(ws);
        oo.addDisplay(columns);
        oo.setIdFormat(getDisplayOptions().getIdFormat());
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, true, isEffective()).iterator())
                .map(x
                        -> NutsIdFormatHelper.of(x, getSearchSession())
                        .buildLong().getMultiColumnRow(oo)
                ));
    }

    @Override
    public NutsResultList<String> getResultPackagings() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, isEffective()).iterator())
                .mapMulti(x -> Arrays.asList(x.getDescriptor().getPackaging()))
                .notBlank());
    }

    @Override
    public NutsResultList<String> getResultArchs() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, isEffective()).iterator())
                .mapMulti(x -> Arrays.asList(x.getDescriptor().getArch()))
                .notBlank());
    }

    @Override
    public NutsResultList<String> getResultNames() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, isEffective()).iterator())
                .mapMulti(x -> Arrays.asList(x.getDescriptor().getName()))
                .notBlank());
    }

    @Override
    public NutsResultList<String> getResultOses() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, isEffective()).iterator())
                .mapMulti(x -> Arrays.asList(x.getDescriptor().getOs()))
                .notBlank());
    }

    @Override
    public NutsResultList<String> getResultOsdists() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, isEffective()).iterator())
                .mapMulti(x -> Arrays.asList(x.getDescriptor().getOsdist()))
                .notBlank());
    }

    @Override
    public NutsResultList<NutsExecutionEntry> getResultExecutionEntries() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, true, isEffective()).iterator())
                .mapMulti(x
                        -> (x.getContent() == null || x.getContent().getPath() == null) ? Collections.emptyList()
                : Arrays.asList(ws.io().parseExecutionEntries(x.getContent().getPath()))));
    }

    @Override
    public NutsResultList<String> getResultPlatforms() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, isEffective()).iterator())
                .mapMulti(x -> Arrays.asList(x.getDescriptor().getPlatform()))
                .notBlank());
    }

    @Override
    public String getResultClassPath() {
        StringBuilder sb = new StringBuilder();
        for (NutsDefinition nutsDefinition : getResultDefinitionsBase(false, false, true, isEffective())) {
            if (nutsDefinition.getPath() != null) {
                if (sb.length() > 0) {
                    sb.append(File.pathSeparator);
                }
                sb.append(nutsDefinition.getPath());
            }
        }
        return sb.toString();
    }

    private class NutsDefinitionNutsResult extends AbstractNutsResultList<NutsDefinition> {

        private final boolean print;
        private final boolean sort;
        private final boolean content;
        private final boolean effective;

        public NutsDefinitionNutsResult(NutsWorkspace ws, String nutsBase, boolean print,
                boolean sort, boolean content, boolean effective
        ) {
            super(ws, nutsBase);
            this.print = print;
            this.sort = sort;
            this.content = content;
            this.effective = effective;
        }

        @Override
        public List<NutsDefinition> list() {
            if (print) {
                return CoreCommonUtils.toList(iterator());
            }
            List<NutsId> mi = getResultIdsBase(false, sort).list();
            List<NutsDefinition> li = new ArrayList<>(mi.size());
            NutsFetchCommand fetch = toFetch().setContent(content).setEffective(effective);
            for (NutsId nutsId : mi) {
                NutsDefinition y = fetch.setId(nutsId).getResultDefinition();
                if (y != null) {
                    li.add(y);
                }
            }
            return li;
        }

        @Override
        public Stream<NutsDefinition> stream() {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator<NutsDefinition>) iterator(), Spliterator.ORDERED), false);
        }

        @Override
        public Iterator<NutsDefinition> iterator() {
            Iterator<NutsId> base = getResultIdsBase(false, sort).iterator();
            NutsFetchCommand fetch = toFetch().setContent(content).setEffective(effective);
            NutsFetchCommand ofetch = toFetch().setContent(content).setEffective(effective).copySession().setOffline();
            fetch.getSession().setSilent();
            final boolean hasRemote = fetch.getFetchStrategy() == null || Arrays.stream(fetch.getFetchStrategy().modes()).anyMatch(x -> x == NutsFetchMode.REMOTE);
            Iterator<NutsDefinition> ii = new NamedIterator<NutsDefinition>("Id->Definition") {
                private NutsDefinition n = null;

                @Override
                public boolean hasNext() {
                    while (base.hasNext()) {
                        NutsId next = base.next();
                        NutsDefinition d = null;
                        if (content) {
                            d = fetch.setId(next).getResultDefinition();
                        } else {
                            //load descriptor
                            if (hasRemote) {
                                fetch.setId(next).getResultDescriptor();
                            }
                            d = ofetch.setId(next).getResultDefinition();

                        }
                        if (d != null) {
                            n = d;
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public NutsDefinition next() {
                    return n;
                }
            };
            if (!print) {
                return ii;
            }
            return NutsWorkspaceUtils.of(ws).decoratePrint(ii, getSearchSession(), getDisplayOptions());
        }

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
        if (regularIds.length > 0) {
            for (String id : regularIds) {
                NutsId nutsId = ws.id().parse(id);
                if (nutsId != null) {
                    List<NutsId> nutsId2 = new ArrayList<>();
                    if (CoreStringUtils.isBlank(nutsId.getGroupId())) {
                        if (nutsId.getArtifactId().equals("nuts")) {
                            if (nutsId.getVersion().isBlank() || nutsId.getVersion().compareTo("0.5") >= 0) {
                                nutsId2.add(nutsId.builder().setGroupId("net.vpc.app.nuts").build());
                            } else {
                                //older versions
                                nutsId2.add(nutsId.builder().setGroupId("net.vpc.app").build());
                            }
                        } else {
                            for (String aImport : ws.config().getImports()) {
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
                            NutsIdFilter idFilter2 = CoreFilterUtils.AndSimplified(sIdFilter, nutsId1.filter());
                            NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(nutsId1.getProperties(), idFilter2, sDescriptorFilter));
                            NutsInstallStatus nutsInstallStatusFilter = CoreFilterUtils.getTopLevelFilterInstallStatus(filter);
                            for (NutsRepository repo : NutsWorkspaceUtils.of(ws).filterRepositories(NutsRepositorySupportedAction.SEARCH, nutsId1, sRepositoryFilter, mode, session,
                                    nutsInstallStatusFilter != NutsInstallStatus.NOT_INSTALLED,
                                    nutsInstallStatusFilter != NutsInstallStatus.INSTALLED_OR_INCLUDED
                                    && nutsInstallStatusFilter != NutsInstallStatus.INCLUDED
                                    && nutsInstallStatusFilter != NutsInstallStatus.INSTALLED
                            )) {
                                if (sRepositoryFilter == null || sRepositoryFilter.accept(repo)) {
                                    all.add(IteratorBuilder.ofLazyNamed("searchVersions(" + repo.config().name() + "," + mode + "," + sRepositoryFilter + "," + finalSession + ")", ()
                                            -> repo.searchVersions().setId(nutsId1).setFilter(filter)
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
                        search2.setIdFilter(new NutsIdFilterOr(
                                new NutsPatternIdFilter(nutsId.builder().setGroupId("*").build()),
                                CoreNutsUtils.simplify(search2.getIdFilter())
                        ));
                        coalesce.add(search2.getResultIds().iterator());
                    }
                    allResults.add(fetchMode.isStopFast()
                            ? IteratorUtils.coalesce(coalesce)
                            : IteratorUtils.concat(coalesce)
                    );
                }
            }
        } else {
            NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(null, sIdFilter, sDescriptorFilter));
            NutsInstallStatus nutsInstallStatusFilter = CoreFilterUtils.getTopLevelFilterInstallStatus(filter);
            boolean includeOtherReporisotries = 
                       nutsInstallStatusFilter != NutsInstallStatus.INSTALLED_OR_INCLUDED
                    && nutsInstallStatusFilter != NutsInstallStatus.INCLUDED
                    && nutsInstallStatusFilter != NutsInstallStatus.INSTALLED;

            List<Iterator<NutsId>> coalesce = new ArrayList<>();
            for (NutsFetchMode mode : fetchMode) {
                List<Iterator<NutsId>> all = new ArrayList<>();
                for (NutsRepository repo : NutsWorkspaceUtils.of(ws).filterRepositories(NutsRepositorySupportedAction.SEARCH, null, sRepositoryFilter, mode, session, nutsInstallStatusFilter != NutsInstallStatus.NOT_INSTALLED, includeOtherReporisotries)) {
                    NutsSession finalSession1 = session;
                    all.add(
                            IteratorBuilder.ofLazyNamed("search(" + repo.config().name() + "," + mode + "," + sRepositoryFilter + "," + session + ")",
                                    () -> repo.search().setFilter(filter).setSession(finalSession1)
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

    @Override
    public ClassLoader getResultClassLoader() {
        return getResultClassLoader(null);
    }

    @Override
    public ClassLoader getResultClassLoader(ClassLoader parent) {
        List<NutsDefinition> nutsDefinitions = getResultDefinitions().list();
        URL[] all = new URL[nutsDefinitions.size()];
        for (int i = 0; i < all.length; i++) {
            try {
                all[i] = nutsDefinitions.get(i).getPath().toUri().toURL();
            } catch (MalformedURLException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return ((DefaultNutsWorkspaceExtensionManager) ws.extensions()).getNutsURLClassLoader(all, parent);
    }

    @Override
    public NutsSearchCommand run() {
        NutsDisplayProperty[] a = getDisplayOptions().getDisplayProperties();
        NutsResultList r = null;
        if (a.length == 0) {
            r = getResultIds();
        } else if (a.length == 1) {
            //optimized case
            switch (a[0]) {
                case ARCH: {
                    r = getResultArchs();
                    break;
                }
                case FILE: {
                    r = getResultPaths();
                    break;
                }
                case FILE_NAME: {
                    r = getResultPathNames();
                    break;
                }
                case NAME: {
                    r = getResultNames();
                    break;
                }
                case PACKAGING: {
                    r = getResultPackagings();
                    break;
                }
                case PLATFORM: {
                    r = getResultPlatforms();
                    break;
                }
                case EXEC_ENTRY: {
                    r = getResultExecutionEntries();
                    break;
                }
                case OS: {
                    r = getResultOses();
                    break;
                }
                case OSDIST: {
                    r = getResultOsdists();
                    break;
                }
                case ID: {
                    r = getResultIds();
                    break;
                }
                case INSTALL_DATE: {
                    r = getResultInstallDates();
                    break;
                }
                case INSTALL_USER: {
                    r = getResultInstallUsers();
                    break;
                }
                case INSTALL_FOLDER: {
                    r = getResultInstallFolders();
                    break;
                }
                case APPS_FOLDER: {
                    r = getResultStoreLocations(NutsStoreLocation.APPS);
                    break;
                }
                case CACHE_FOLDER: {
                    r = getResultStoreLocations(NutsStoreLocation.CACHE);
                    break;
                }
                case CONFIG_FOLDER: {
                    r = getResultStoreLocations(NutsStoreLocation.CONFIG);
                    break;
                }
                case LIB_FOLDER: {
                    r = getResultStoreLocations(NutsStoreLocation.LIB);
                    break;
                }
                case LOG_FOLDER: {
                    r = getResultStoreLocations(NutsStoreLocation.LOG);
                    break;
                }
                case TEMP_FOLDER: {
                    r = getResultStoreLocations(NutsStoreLocation.TEMP);
                    break;
                }
                case VAR_LOCATION: {
                    r = getResultStoreLocations(NutsStoreLocation.VAR);
                    break;
                }
                case STATUS: {
                    r = getResultStatuses();
                    break;
                }
            }
        }
        if (r == null) {
            //this is custom case
            boolean _content = isContent();
            boolean _effective = isEffective();
            for (NutsDisplayProperty display : getDisplayOptions().getDisplayProperties()) {
                switch (display) {
                    case NAME:
                    case ARCH:
                    case PACKAGING:
                    case PLATFORM:
                    case OS:
                    case OSDIST: {
                        break;
                    }
                    case FILE:
                    case FILE_NAME:
                    case EXEC_ENTRY: {
//                        _content = true;
                        break;
                    }
                    case INSTALL_DATE:
                    case INSTALL_USER: {
                        break;
                    }
                    case STATUS: {
//                        _content = true;
                        break;
                    }
                }
            }
            r = getResultDefinitionsBase(isPrintResult(), isSorted(), _content, _effective);
        }
        for (Object any : r) {
            //just iterator over
        }
        return this;
    }

}
