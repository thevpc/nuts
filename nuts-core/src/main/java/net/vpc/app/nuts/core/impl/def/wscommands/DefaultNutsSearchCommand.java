/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.impl.def.wscommands;

import net.vpc.app.nuts.core.*;
import net.vpc.app.nuts.core.filters.id.*;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.util.NutsIdGraph;
import net.vpc.app.nuts.core.util.NutsCollectionSearchResult;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.filters.dependency.NutsDependencyJavascriptFilter;
import net.vpc.app.nuts.core.filters.dependency.NutsDependencyOptionFilter;
import net.vpc.app.nuts.core.filters.dependency.NutsDependencyScopeFilter;
import net.vpc.app.nuts.core.filters.descriptor.NutsDescriptorFilterArch;
import net.vpc.app.nuts.core.filters.descriptor.NutsDescriptorFilterPackaging;
import net.vpc.app.nuts.core.filters.descriptor.NutsDescriptorJavascriptFilter;
import net.vpc.app.nuts.core.filters.repository.DefaultNutsRepositoryFilter;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

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
import net.vpc.app.nuts.core.filters.CoreFilterUtils;
import net.vpc.app.nuts.core.format.FormattableNutsId;
import net.vpc.app.nuts.core.format.NutsDisplayProperty;
import net.vpc.app.nuts.core.format.NutsFetchDisplayOptions;

import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.iter.IteratorBuilder;
import net.vpc.app.nuts.core.util.iter.IteratorUtils;
import net.vpc.app.nuts.core.wscommands.AbstractNutsSearchCommand;

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
        if(this.getIds().length==0 && isCompanions()){
            someIds.addAll(Arrays.asList(NutsWorkspaceExt.of(ws).getCompanionIds()));
        }
        if(this.getIds().length==0 && isRuntime()){
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
        if (getDefaultVersions() != null) {
            _idFilter = CoreFilterUtils.AndSimplified(_idFilter, new NutsDefaultVersionIdFilter(getDefaultVersions()));
        }
        if (execType != null) {
            switch (execType) {
                case "libs": {
                    _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new NutsExecStatusIdFilter(false, false));
                    break;
                }
                case "apps": {
                    _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new NutsExecStatusIdFilter(true, null));
                    break;
                }
                case "nuts-apps": {
                    _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new NutsExecStatusIdFilter(null, true));
                    break;
                }
                case "extensions": {
                    _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new NutsExecExtensionFilter(
                            targetApiVersion==null?null:ws.id().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build())
                    );
                    break;
                }
                case "runtime": {
                    _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new NutsExecRuntimeFilter(
                            targetApiVersion==null?null:ws.id().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build()
                            ,false
                            )
                    );
                    break;
                }
                case "companions": {
                    _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new NutsExecCompanionFilter(
                            targetApiVersion==null?null:ws.id().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build(),
                            NutsWorkspaceExt.of(ws).getCompanionIds()
                            )
                    );
                    break;
                }
            }
        }else{
            if(targetApiVersion!=null) {
                _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new BootAPINutsDescriptorFilter(
                                ws.id().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build().getVersion()
                        )
                );
            }
        }
        if(!frozenIds.isEmpty()){
            _descriptorFilter = CoreFilterUtils.AndSimplified(_descriptorFilter, new NutsFrozenIdExtensionFilter(
                            frozenIds.toArray(new NutsId[0])
                    )
            );
        }
        if (!wildcardIds.isEmpty()) {
            for (String wildcardId : wildcardIds) {
                _idFilter = CoreNutsUtils.simplify(new NutsIdFilterOr(_idFilter, new NutsPatternIdFilter(ws.id().parse(wildcardId))));
            }
        }
        NutsFetchCommand k = toFetch();
        return new DefaultNutsSearch(
                goodIds.toArray(new String[0]),
                _repositoryFilter,
                _idFilter, _descriptorFilter, k);
    }

    @Override
    public NutsFetchCommand toFetch() {
        NutsFetchCommand t = new DefaultNutsFetchCommand(ws).copyFromDefaultNutsQueryBaseOptions(this)
                .setSession(evalSession(true));
        if (getDisplayOptions().isRequireDefinition()) {
            t.setContent(true);
        }
        return t;
    }

    private NutsSession evalSession(boolean create) {
        NutsSession s = getSession();
        if (create) {
            if (s == null) {
                s = ws.createSession();
            }
        }
        return s;
//        if (mode != null) {
//            if (s == null) {
//                s = ws.createSession();
//            }
//            s.setFetchMode(mode);
//            return s;
//        } else {
//            return s;
//        }
    }

    @Override
    public NutsSearchResult<NutsId> getResultIds() {
        return getResultIdsBase(getValidSession().isTrace(), sorted);
    }

    private NutsSearchResult<NutsDefinition> getResultDefinitionsBase(boolean trace, boolean sort, boolean content, boolean install, boolean effective) {
        return new NutsDefinitionNutsSearchResult(ws, resolveFindIdBase(), trace, sort, content, install, effective);
    }

    @Override
    public NutsSearchResult<NutsDefinition> getResultDefinitions() {
        return getResultDefinitionsBase(getValidSession().isTrace(), sorted, isContent(), isInstallInformation(), isEffective());
    }

    private String resolveFindIdBase() {
        return ids.isEmpty() ? null : ids.get(0) == null ? null : ids.get(0).toString();
    }

    private List<NutsId> applyTraceDecoratorListOfNutsId(List<NutsId> curr, boolean trace) {
        if (!trace) {
            return curr;
        }
        return CoreCommonUtils.toList(applyTraceDecoratorIterOfNutsId(curr.iterator(), trace));
    }

    private Collection<NutsId> applyTraceDecoratorCollectionOfNutsId(Collection<NutsId> curr, boolean trace) {
        if (!trace) {
            return curr;
        }
        return CoreCommonUtils.toList(applyTraceDecoratorIterOfNutsId(curr.iterator(), trace));
    }

    private Iterator<NutsId> applyTraceDecoratorIterOfNutsId(Iterator<NutsId> curr, boolean trace) {
        return trace ? NutsWorkspaceUtils.decorateTrace(ws, curr, getValidSession(), getDisplayOptions()) : curr;
    }

    private NutsCollectionSearchResult<NutsId> applyVersionFlagFilters(Iterator<NutsId> curr, boolean trace) {
        if (!isLatest() && !isDistinct()) {
            return new NutsCollectionSearchResult<>(ws, resolveFindIdBase(), applyTraceDecoratorIterOfNutsId(curr, trace));
            //nothind
        } else if (!isLatest() && isDistinct()) {
            return new NutsCollectionSearchResult<>(ws, resolveFindIdBase(),
                    applyTraceDecoratorIterOfNutsId(IteratorBuilder.of(curr).distinct((NutsId nutsId) -> nutsId.getLongNameId()
//                            .setAlternative(nutsId.getAlternative())
                            .toString()).iterator(), trace));
        } else if (isLatest() && isDistinct()) {
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
            return new NutsCollectionSearchResult<>(ws, resolveFindIdBase(), applyTraceDecoratorCollectionOfNutsId(visited.values(), trace));
        } else if (isLatest() && !isDistinct()) {
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
            List<NutsId> list = new ArrayList<>();
            for (List<NutsId> li : visited.values()) {
                list.addAll(li);
            }
            return new NutsCollectionSearchResult<>(ws, resolveFindIdBase(), applyTraceDecoratorListOfNutsId(list, trace));
        }
        throw new NutsUnexpectedException(ws);
    }

    private NutsCollectionSearchResult<NutsId> getResultIdsBase(boolean trace, boolean sort) {
        DefaultNutsSearch build = build();
        build.getOptions().setSession(build.getOptions().getSession().copy().trace(trace));
        Iterator<NutsId> base0 = findIterator(build);
        if (base0 == null) {
            return new NutsCollectionSearchResult<NutsId>(ws, resolveFindIdBase());
        }
        if (!isLatest() && !isDistinct() && !sort && !isInlineDependencies()) {
            return new NutsCollectionSearchResult<NutsId>(ws, resolveFindIdBase(), applyTraceDecoratorIterOfNutsId(base0, trace));
        }
        NutsCollectionSearchResult<NutsId> a = applyVersionFlagFilters(base0, false);
        Iterator<NutsId> curr = a.iterator();
        if (isInlineDependencies()) {
            if (!includeMain) {
                curr = Arrays.asList(findDependencies(a.list())).iterator();
            } else {
                List<Iterator<NutsId>> it = new ArrayList<>();
                Iterator<NutsId> a0 = a.iterator();
                List<NutsId> base = new ArrayList<>();
                it.add(new Iterator<NutsId>() {
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
                it.add(new Iterator<NutsId>() {
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
            List<NutsId> listToSort = applyVersionFlagFilters(curr, false).list();
            listToSort.sort(comparator);
            return new NutsCollectionSearchResult<>(ws, resolveFindIdBase(), applyTraceDecoratorListOfNutsId(listToSort, trace));
        } else {
            return applyVersionFlagFilters(curr, trace);
        }
    }

    private NutsId[] findDependencies(List<NutsId> ids) {
        NutsSession _session = this.getSession() == null ? ws.createSession() : this.getSession();
        NutsDependencyFilter _dependencyFilter = CoreFilterUtils.AndSimplified(
                new NutsDependencyScopeFilter().addScopes(getScope()),
                getOptional() == null ? null : NutsDependencyOptionFilter.valueOf(getOptional()),
                getDependencyFilter()
        );
        NutsIdGraph graph = new NutsIdGraph(_session, isFailFast());
        return graph.resolveDependencies(ids, _dependencyFilter);
    }

    @Override
    public String getResultNutsPath() {
        return getResultIds().list().stream().map(NutsId::getLongName).collect(Collectors.joining(";"));
    }

    private <T> NutsSearchResult<T> postProcessResult(IteratorBuilder<T> a) {
        if (isSorted()) {
            a = a.sort(null, isDistinct());
        }
        if (getValidSession().isTrace()) {
            a = IteratorBuilder.of(NutsWorkspaceUtils.decorateTrace(ws, a.build(), getValidSession(), getDisplayOptions()));
        }
        return new NutsCollectionSearchResult<>(ws, resolveFindIdBase(),
                a.build()
        );
    }

    @Override
    public NutsSearchResult<String> getResultPaths() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, true, false, isEffective()).iterator())
                .map(x -> (x.getContent() == null || x.getContent().getPath() == null) ? null : x.getContent().getPath().toString())
                .notBlank()
        );
    }

    @Override
    public NutsSearchResult<String> getResultPathNames() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, true, false, isEffective()).iterator())
                .map(x -> (x.getContent() == null || x.getContent().getPath() == null) ? null : x.getContent().getPath().getFileName().toString())
                .notBlank());
    }

    @Override
    public NutsSearchResult<Instant> getResultInstallDates() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, true, false).iterator())
                .map(x -> (x.getInstallInformation() == null) ? null : x.getInstallInformation().getInstallDate())
                .notNull());
    }

    @Override
    public NutsSearchResult<String> getResultInstallUsers() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, true, false).iterator())
                .map(x -> (x.getInstallInformation() == null) ? null : x.getInstallInformation().getInstallUser())
                .notBlank());
    }

    @Override
    public NutsSearchResult<Path> getResultInstallFolders() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, true, false).iterator())
                .map(x -> (x.getInstallInformation() == null) ? null : x.getInstallInformation().getInstallFolder())
                .notNull());
    }

    @Override
    public NutsSearchResult<Path> getResultStoreLocations(NutsStoreLocation location) {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, false, false).iterator())
                .map(x -> ws.config().getStoreLocation(x.getId(), location))
                .notNull());
    }

    public NutsSearchResult<String> getResultStatuses() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, true, true, isEffective()).iterator())
                .map(x
                        -> FormattableNutsId.of(x, getValidSession())
                        .buildLong().getStatusString()
                )
                .notBlank());
    }

    @Override
    public NutsSearchResult<String[]> getResultStrings(String[] columns) {
        NutsFetchDisplayOptions oo = new NutsFetchDisplayOptions(ws);
        oo.addDisplay(columns);
        oo.setIdFormat(getDisplayOptions().getIdFormat());
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, true, true, isEffective()).iterator())
                .map(x
                        -> FormattableNutsId.of(x, getValidSession())
                        .buildLong().getMultiColumnRow(oo)
                ));
    }

    @Override
    public NutsSearchResult<String> getResultPackagings() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, false, isEffective()).iterator())
                .mapMulti(x -> Arrays.asList(x.getDescriptor().getPackaging()))
                .notBlank());
    }

    @Override
    public NutsSearchResult<String> getResultArchs() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, false, isEffective()).iterator())
                .mapMulti(x -> Arrays.asList(x.getDescriptor().getArch()))
                .notBlank());
    }

    @Override
    public NutsSearchResult<String> getResultNames() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, false, isEffective()).iterator())
                .mapMulti(x -> Arrays.asList(x.getDescriptor().getName()))
                .notBlank());
    }

    @Override
    public NutsSearchResult<String> getResultOses() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, false, isEffective()).iterator())
                .mapMulti(x -> Arrays.asList(x.getDescriptor().getOs()))
                .notBlank());
    }

    @Override
    public NutsSearchResult<String> getResultOsdists() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, false, isEffective()).iterator())
                .mapMulti(x -> Arrays.asList(x.getDescriptor().getOsdist()))
                .notBlank());
    }

    @Override
    public NutsSearchResult<NutsExecutionEntry> getResultExecutionEntries() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, true, false, isEffective()).iterator())
                .mapMulti(x
                        -> (x.getContent() == null || x.getContent().getPath() == null) ? Collections.emptyList()
                : Arrays.asList(ws.io().parseExecutionEntries(x.getContent().getPath()))));
    }

    @Override
    public NutsSearchResult<String> getResultPlatforms() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionsBase(false, false, false, false, isEffective()).iterator())
                .mapMulti(x -> Arrays.asList(x.getDescriptor().getPlatform()))
                .notBlank());
    }

    @Override
    public String getResultClassPath() {
        StringBuilder sb = new StringBuilder();
        for (NutsDefinition nutsDefinition : getResultDefinitionsBase(false, false, true, false, isEffective())) {
            if (nutsDefinition.getPath() != null) {
                if (sb.length() > 0) {
                    sb.append(File.pathSeparator);
                }
                sb.append(nutsDefinition.getPath());
            }
        }
        if (getValidSession().isTrace()) {

        }
        return sb.toString();
    }


    private class NutsDefinitionNutsSearchResult extends AbstractNutsSearchResult<NutsDefinition> {

        private final boolean trace;
        private final boolean sort;
        private final boolean installInformation;
        private final boolean content;
        private final boolean effective;

        public NutsDefinitionNutsSearchResult(NutsWorkspace ws, String nutsBase, boolean trace,
                boolean sort, boolean content, boolean install, boolean effective
        ) {
            super(ws, nutsBase);
            this.trace = trace;
            this.sort = sort;
            this.content = content;
            this.installInformation = install;
            this.effective = effective;
        }

        @Override
        public List<NutsDefinition> list() {
            if (trace) {
                return CoreCommonUtils.toList(iterator());
            }
            List<NutsId> mi = getResultIdsBase(false, sort).list();
            List<NutsDefinition> li = new ArrayList<>(mi.size());
            NutsFetchCommand fetch = toFetch().content(content).effective(effective).installInformation(installInformation);
            for (NutsId nutsId : mi) {
                NutsDefinition y = fetch.id(nutsId).getResultDefinition();
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
            NutsFetchCommand fetch = toFetch().content(content).effective(effective).installInformation(installInformation);
            fetch.getSession().trace(false);
            Iterator<NutsDefinition> ii = new Iterator<NutsDefinition>() {
                private NutsDefinition n = null;

                @Override
                public boolean hasNext() {
                    while (base.hasNext()) {
                        NutsId next = base.next();
                        NutsDefinition d = fetch.id(next).getResultDefinition();
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
            if (!trace) {
                return ii;
            }
            return NutsWorkspaceUtils.decorateTrace(ws, ii, getValidSession(), getDisplayOptions());
        }

    }

    public Iterator<NutsId> findIterator(DefaultNutsSearch search) {

        List<Iterator<NutsId>> allResults = new ArrayList<>();

        NutsSession session = NutsWorkspaceUtils.validateSession(ws, search.getOptions().getSession());
        NutsIdFilter sIdFilter = search.getIdFilter();
        NutsRepositoryFilter sRepositoryFilter = search.getRepositoryFilter();
        NutsDescriptorFilter sDescriptorFilter = search.getDescriptorFilter();
        String[] regularIds = search.getRegularIds();
        NutsFetchStrategy fetchMode = NutsWorkspaceHelper.validate(search.getOptions().getFetchStrategy());
        if (regularIds.length > 0) {
            for (String id : regularIds) {
                NutsId nutsId = ws.id().parse(id);
                if (nutsId != null) {
                    List<NutsId> nutsId2 = new ArrayList<>();
                    if (CoreStringUtils.isBlank(nutsId.getGroupId())) {
                        if (nutsId.getArtifactId().equals("nuts")) {
                            if (nutsId.getVersion().isBlank() || nutsId.getVersion().ge("0.5")) {
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
                    for (NutsFetchMode mode : fetchMode) {
                        List<Iterator<NutsId>> all = new ArrayList<>();
                        for (NutsId nutsId1 : nutsId2) {
                            NutsIdFilter idFilter2 = CoreFilterUtils.AndSimplified(sIdFilter, nutsId1.filter());
                            if (mode == NutsFetchMode.INSTALLED) {
                                all.add(
                                        IteratorBuilder.ofLazy(() -> {
                                            NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(nutsId1.getProperties(), idFilter2, sDescriptorFilter));
                                            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(getValidSession(), null, NutsFetchMode.INSTALLED, new DefaultNutsFetchCommand(ws));
                                            return NutsWorkspaceExt.of(ws)
                                                    .getInstalledRepository().findVersions(nutsId1, filter, rsession);
                                        }).safeIgnore().iterator());
                            } else {
                                for (NutsRepository repo : NutsWorkspaceUtils.filterRepositories(ws, NutsRepositorySupportedAction.SEARCH, nutsId1, sRepositoryFilter, mode, search.getOptions())) {
                                    if (sRepositoryFilter == null || sRepositoryFilter.accept(repo)) {
                                        NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(nutsId1.getProperties(), idFilter2, sDescriptorFilter));
                                        all.add(
                                                IteratorBuilder.ofLazy(new Iterable<NutsId>() {
                                                    @Override
                                                    public Iterator<NutsId> iterator() {
                                                        return repo.searchVersions().setId(nutsId1).setFilter(filter).setSession(NutsWorkspaceHelper.createRepositorySession(session, repo, mode, search.getOptions()))
                                                                .run().getResult();
                                                    }
                                                }).safeIgnore().iterator()
                                        );
                                    }
                                }
                            }
                        }
                        coalesce.add(IteratorUtils.concat(all));
                    }
                    if (nutsId.getGroupId() == null) {
                        //now will look with *:artifactId pattern
                        NutsSearchCommand search2 = ws.search()
                                .copyFrom(search.getOptions())
                                .setRepositoryFilter(search.getRepositoryFilter())
                                .setDescriptorFilter(search.getDescriptorFilter())
                                .setFetchStratery(search.getOptions().getFetchStrategy())
                                .setSession(session);
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

            List<Iterator<NutsId>> coalesce = new ArrayList<>();
            for (NutsFetchMode mode : fetchMode) {
                if (mode == NutsFetchMode.INSTALLED) {
                    NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, null, mode, search.getOptions());
                    NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(null, sIdFilter, sDescriptorFilter));
                    coalesce.add(NutsWorkspaceExt.of(ws).getInstalledRepository().findAll(filter, rsession));
                } else {
                    List<Iterator<NutsId>> all = new ArrayList<>();
                    for (NutsRepository repo : NutsWorkspaceUtils.filterRepositories(ws, NutsRepositorySupportedAction.SEARCH, null, sRepositoryFilter, mode, search.getOptions())) {
                        if (sRepositoryFilter == null || sRepositoryFilter.accept(repo)) {
                            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, repo, mode, search.getOptions());
                            NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(null, sIdFilter, sDescriptorFilter));
                            all.add(
                                    IteratorBuilder.ofLazy(new Iterable<NutsId>() {
                                        @Override
                                        public Iterator<NutsId> iterator() {
                                            return repo.search().setFilter(filter).setSession(rsession).run().getResult();
                                        }
                                    }).safeIgnore().iterator()
                            );
                        }

                    }
                    coalesce.add(IteratorUtils.concat(all));
                }
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
        NutsSearchResult r = null;
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
            boolean _installInformation = isInstallInformation();
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
                        _content = true;
                        break;
                    }
                    case INSTALL_DATE:
                    case INSTALL_USER: {
                        _installInformation = true;
                        break;
                    }
                    case STATUS: {
                        _installInformation = true;
                        _content = true;
                        break;
                    }
                }
            }
            r = getResultDefinitionsBase(getValidSession().isTrace(), isSorted(), _content, _installInformation, _effective);
        }
        for (Object any : r) {
            //just iterator over
        }
        return this;
    }

}
