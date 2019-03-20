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
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.filters.dependency.NutsDependencyJavascriptFilter;
import net.vpc.app.nuts.core.filters.dependency.NutsDependencyOptionFilter;
import net.vpc.app.nuts.core.filters.dependency.NutsDependencyScopeFilter;
import net.vpc.app.nuts.core.filters.descriptor.NutsDescriptorFilterArch;
import net.vpc.app.nuts.core.filters.descriptor.NutsDescriptorFilterPackaging;
import net.vpc.app.nuts.core.filters.descriptor.NutsDescriptorJavascriptFilter;
import net.vpc.app.nuts.core.filters.id.NutsIdFilterOr;
import net.vpc.app.nuts.core.filters.id.NutsJavascriptIdFilter;
import net.vpc.app.nuts.core.filters.id.NutsPatternIdFilter;
import net.vpc.app.nuts.core.filters.id.NutsSimpleIdFilter;
import net.vpc.app.nuts.core.filters.repository.DefaultNutsRepositoryFilter;
import net.vpc.app.nuts.core.filters.repository.ExprNutsRepositoryFilter;
import net.vpc.app.nuts.core.filters.version.NutsVersionJavascriptFilter;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.CoreStringUtils;
import net.vpc.app.nuts.core.util.NutsIdListBuilder;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.CollectionUtils;

import java.io.File;
import java.util.*;

import static net.vpc.app.nuts.core.util.CoreNutsUtils.And;
import static net.vpc.app.nuts.core.util.CoreNutsUtils.simplify;

/**
 * @author vpc
 */
public class DefaultNutsQuery implements NutsQuery {

    private final List<String> ids = new ArrayList<>();
    private NutsIdFilter idFilter;
    private NutsDependencyFilter dependencyFilter;
    private NutsRepositoryFilter repositoryFilter;
    private NutsVersionFilter versionFilter;
    private NutsDescriptorFilter descriptorFilter;
    private Set<NutsDependencyScope> scope = EnumSet.noneOf(NutsDependencyScope.class);
    private boolean latestVersions;
    private boolean sort = true;
    private NutsSession session;
    private final List<String> js = new ArrayList<>();
    private final List<String> arch = new ArrayList<>();
    private final List<String> packaging = new ArrayList<>();
    private final List<String> repos = new ArrayList<>();
    private final DefaultNutsWorkspace ws;
    private boolean includeMain = true;
    private boolean ignoreNotFound = false;
    private boolean includeDependencies = false;
    private boolean includeContent = true;
    private boolean includeInstallInfo = true;
    private boolean includeEffectiveDesc = false;
    private boolean ignoreCache = false;
    private boolean preferInstalled = false;
    private boolean installedOnly = false;
    private Boolean acceptOptional = null;

    public DefaultNutsQuery(DefaultNutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsQuery addJs(Collection<String> value) {
        if (value != null) {
            addJs(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsQuery addJs(String... value) {
        if (value != null) {
            js.addAll(Arrays.asList(value));
        }
        return this;

    }

    @Override
    public NutsQuery addId(Collection<String> value) {
        if (value != null) {
            addId(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsQuery addId(String... value) {
        if (value != null) {
            for (String s : value) {
                if (!StringUtils.isEmpty(s)) {
                    ids.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NutsQuery addId(NutsId... value) {
        if (value != null) {
            for (NutsId s : value) {
                if (s != null) {
                    ids.add(s.toString());
                }
            }
        }
        return this;
    }

    @Override
    public NutsQuery setId(String value) {
        ids.clear();
        if (!StringUtils.isEmpty(value)) {
            ids.add(value);
        }
        return this;
    }

    @Override
    public NutsQuery setId(NutsId value) {
        if (value != null) {
            ids.clear();
            ids.add(value.toString());
        }
        return this;
    }

    @Override
    public NutsQuery addArch(Collection<String> value) {
        if (value != null) {
            addArch(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsQuery addArch(String... value) {
        if (value != null) {
            arch.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsQuery addPackaging(Collection<String> value) {
        if (value != null) {
            addPackaging(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsQuery addPackaging(String... value) {
        if (value != null) {
            this.packaging.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsQuery addRepository(Collection<String> value) {
        if (value != null) {
            addRepository(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsQuery addRepository(String... value) {
        if (value != null) {
            repos.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsQuery copy() {
        DefaultNutsQuery b = new DefaultNutsQuery(ws);
        b.copyFrom(this);
        return b;
    }

    @Override
    public NutsQuery copyFrom(NutsQuery other) {
        setAll(other);
        return this;
    }

//    @Override
//    public NutsQuery setAll(NutsSearch other) {
//        if (other != null) {
//            ids.addAll(Arrays.asList(other.getIds()));
//            idFilter = other.getIdFilter();
////            dependencyFilter = other.getDependencyFilter();
//            repositoryFilter = other.getRepositoryFilter();
//            versionFilter = other.getVersionFilter();
//            descriptorFilter = other.getDescriptorFilter();
////            scope = other.getScope();
//            latestVersions = other.isLatestVersions();
//            sort = other.isSort();
//        }
//        return this;
//    }
    @Override
    public NutsQuery setAll(NutsQuery other) {
        if (other != null) {
            ids.addAll(Arrays.asList(other.getIds()));
            idFilter = other.getIdFilter();
            dependencyFilter = other.getDependencyFilter();
            repositoryFilter = other.getRepositoryFilter();
            versionFilter = other.getVersionFilter();
            descriptorFilter = other.getDescriptorFilter();
            scope = EnumSet.copyOf(other.getScope());
            latestVersions = other.isLatestVersions();
            sort = other.isSort();
            session = other.getSession();
        }
        return this;
    }

    @Override
    public boolean isSort() {
        return sort;
    }

    @Override
    public NutsQuery setSort(boolean sort) {
        this.sort = sort;
        return this;
    }

    @Override
    public boolean isLatestVersions() {
        return latestVersions;
    }

    @Override
    public NutsQuery setLatestVersions(boolean latestVersions) {
        this.latestVersions = latestVersions;
        return this;
    }

    @Override
    public NutsQuery addIds(String... ids) {
        if (ids != null) {
            for (String id : ids) {
                addId(id);
            }
        }
        return this;
    }

    @Override
    public NutsQuery addId(NutsId id) {
        if (id != null) {
            addId(id.toString());
        }
        return this;
    }

    @Override
    public NutsQuery addIds(NutsId... ids) {
        if (ids != null) {
            for (NutsId id : ids) {
                addId(id == null ? null : id.toString());
            }
        }
        return this;
    }

    @Override
    public NutsQuery setIds(String... ids) {
        this.ids.clear();
        addIds(ids);
        return this;
    }

    @Override
    public NutsQuery addId(String id) {
        if (id != null && !id.isEmpty()) {
            ids.add(id);
        }
        return this;
    }

    @Override
    public String[] getIds() {
        return this.ids.toArray(new String[0]);
    }

    @Override
    public Set<NutsDependencyScope> getScope() {
        return scope;
    }

    @Override
    public NutsQuery setScope(NutsDependencyScope scope) {
        return setScope(scope == null ? null : EnumSet.of(scope));
    }

    @Override
    public NutsQuery setScope(NutsDependencyScope... scope) {
        return setScope(scope == null ? null : EnumSet.<NutsDependencyScope>copyOf(Arrays.asList(scope)));
    }

    @Override
    public NutsQuery setScope(Collection<NutsDependencyScope> scope) {
        this.scope = scope == null ? EnumSet.noneOf(NutsDependencyScope.class) : EnumSet.<NutsDependencyScope>copyOf(scope);
        return this;
    }

    @Override
    public NutsQuery addScope(Collection<NutsDependencyScope> scope) {
        this.scope = NutsDependencyScope.add(this.scope, scope);
        return this;
    }

    @Override
    public NutsQuery addScope(NutsDependencyScope scope) {
        this.scope = NutsDependencyScope.add(this.scope, scope);
        return this;
    }

    @Override
    public NutsQuery addScope(NutsDependencyScope... scope) {
        this.scope = NutsDependencyScope.add(this.scope, scope);
        return this;
    }

    @Override
    public NutsQuery removeScope(Collection<NutsDependencyScope> scope) {
        this.scope = NutsDependencyScope.remove(this.scope, scope);
        return this;
    }

    @Override
    public NutsQuery removeScope(NutsDependencyScope scope) {
        this.scope = NutsDependencyScope.remove(this.scope, scope);
        return this;
    }

    //    public NutsQuery setDependencyFilter(TypedObject filter) {
//        if (filter == null) {
//            this.dependencyFilter = null;
//        } else if (NutsDependencyFilter.class.equals(filter.getType()) || String.class.equals(filter.getType())) {
//            this.dependencyFilter = filter;
//        } else {
//            throw new IllegalArgumentException("Invalid Object");
//        }
//        return this;
//    }
//
    @Override
    public NutsQuery setDependencyFilter(NutsDependencyFilter filter) {
        this.dependencyFilter = filter;
        return this;
    }

    @Override
    public NutsDependencyFilter getDependencyFilter() {
        return dependencyFilter;
    }

    @Override
    public NutsQuery setDependencyFilter(String filter) {
        this.dependencyFilter = StringUtils.isEmpty(filter) ? null : new NutsDependencyJavascriptFilter(filter);
        return this;
    }

    @Override
    public NutsQuery setRepositoryFilter(NutsRepositoryFilter filter) {
        this.repositoryFilter = filter;
        return this;
    }

    @Override
    public NutsRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    @Override
    public NutsQuery setRepositoryFilter(String filter) {
        this.repositoryFilter = StringUtils.isEmpty(filter) ? null : new ExprNutsRepositoryFilter(filter);
        return this;
    }

    @Override
    public NutsQuery setVersionFilter(NutsVersionFilter filter) {
        this.versionFilter = filter;
        return this;
    }

    @Override
    public NutsVersionFilter getVersionFilter() {
        return versionFilter;
    }

    @Override
    public NutsQuery setVersionFilter(String filter) {
        this.versionFilter = StringUtils.isEmpty(filter) ? null : new NutsVersionJavascriptFilter(filter);
        return this;
    }

    @Override
    public NutsQuery setDescriptorFilter(NutsDescriptorFilter filter) {
        this.descriptorFilter = filter;
        return this;
    }

    @Override
    public NutsDescriptorFilter getDescriptorFilter() {
        return descriptorFilter;
    }

    @Override
    public NutsQuery setDescriptorFilter(String filter) {
        this.descriptorFilter = StringUtils.isEmpty(filter) ? null : new NutsDescriptorJavascriptFilter(filter);
        return this;
    }

    @Override
    public NutsQuery setIdFilter(NutsIdFilter filter) {
        this.idFilter = filter;
        return this;
    }

    @Override
    public NutsIdFilter getIdFilter() {
        return idFilter;
    }

    @Override
    public NutsQuery setIdFilter(String filter) {
        this.idFilter = StringUtils.isEmpty(filter) ? null : new NutsJavascriptIdFilter(filter);
        return this;
    }

    @Override
    public NutsQuery setIds(Collection<String> ids) {
        this.ids.clear();
        addId(ids);
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsQuery setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NutsSearch{");
        sb.append(scope);
        if (ids != null && ids.size() > 0) {
            sb.append(",ids=").append(ids);
        }
        if (idFilter != null) {
            sb.append(",idFilter=").append(idFilter);
        }
        if (dependencyFilter != null) {
            sb.append(",dependencyFilter=").append(dependencyFilter);
        }
        if (repositoryFilter != null) {
            sb.append(",repositoryFilter=").append(repositoryFilter);
        }
        if (versionFilter != null) {
            sb.append(",versionFilter=").append(versionFilter);
        }
        if (descriptorFilter != null) {
            sb.append(",descriptorFilter=").append(descriptorFilter);
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String[] getJs() {
        return js.toArray(new String[0]);
    }

    @Override
    public String[] getArch() {
        return arch.toArray(new String[0]);
    }

    @Override
    public String[] getPackaging() {
        return this.packaging.toArray(new String[0]);
    }

    @Override
    public String[] getRepos() {
        return repos.toArray(new String[0]);
    }

    //@Override
    private DefaultNutsSearch build() {

        HashSet<String> someIds = new HashSet<>(Arrays.asList(this.getIds()));
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
            for (String id : f.getIds()) {
                if (NutsPatternIdFilter.containsWildcad(id)) {
                    wildcardIds.add(id);
                } else {
                    goodIds.add(id);
                }
            }
            idFilter0 = null;
        }
        if (idFilter0 instanceof NutsSimpleIdFilter) {
            NutsSimpleIdFilter f = (NutsSimpleIdFilter) idFilter0;
            goodIds.add(f.getId().toString());
            idFilter0 = null;
        }

        NutsDescriptorFilter _descriptorFilter = null;
        NutsIdFilter _idFilter = null;
        NutsDependencyFilter depFilter = null;
        DefaultNutsRepositoryFilter rfilter = null;
        for (String j : this.getJs()) {
            if (!StringUtils.isEmpty(j)) {
                if (CoreStringUtils.containsTopWord(j, "descriptor")) {
                    _descriptorFilter = simplify(And(_descriptorFilter, NutsDescriptorJavascriptFilter.valueOf(j)));
                } else if (CoreStringUtils.containsTopWord(j, "dependency")) {
                    depFilter = simplify(And(depFilter, NutsDependencyJavascriptFilter.valueOf(j)));
                } else {
                    _idFilter = simplify(And(_idFilter, NutsJavascriptIdFilter.valueOf(j)));
                }
            }
        }
        NutsDescriptorFilter packs = null;
        for (String v : this.getPackaging()) {
            packs = CoreNutsUtils.simplify(CoreNutsUtils.Or(packs, new NutsDescriptorFilterPackaging(v)));
        }
        NutsDescriptorFilter archs = null;
        for (String v : this.getArch()) {
            archs = CoreNutsUtils.simplify(CoreNutsUtils.Or(archs, new NutsDescriptorFilterArch(v)));
        }

        _descriptorFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(_descriptorFilter, packs, archs));

        if (this.getRepos().length > 0) {
            rfilter = new DefaultNutsRepositoryFilter(new HashSet<>(Arrays.asList(this.getRepos())));
        }

        NutsRepositoryFilter _repositoryFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(rfilter, this.getRepositoryFilter()));
        NutsVersionFilter _versionFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(null, this.getVersionFilter()));
        _descriptorFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(_descriptorFilter, this.getDescriptorFilter()));
        _idFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(_idFilter, idFilter0));

        if (!wildcardIds.isEmpty()) {
            NutsPatternIdFilter ff = new NutsPatternIdFilter(wildcardIds.toArray(new String[0]));
            _idFilter = CoreNutsUtils.simplify(new NutsIdFilterOr(_idFilter, ff));
        }

        return new DefaultNutsSearch(goodIds.toArray(new String[0]), _repositoryFilter, _versionFilter, sort, _idFilter, latestVersions, _descriptorFilter, preferInstalled, installedOnly, ws, session);
    }

    @Override
    public NutsId findOne() {
        List<NutsId> r = find();
        if (r.isEmpty()) {
            return null;
        }
        if (r.size() > 1) {
            throw new IllegalArgumentException("Too many results (" + r.size() + " but expected one only)");
        }
        return r.get(0);
    }

    @Override
    public NutsId findFirst() {
        List<NutsId> r = find();
        if (r.isEmpty()) {
            return null;
        }
        return r.get(0);
    }

    @Override
    public NutsDefinition fetchOne() {
        List<NutsDefinition> r = fetch();
        if (r.isEmpty()) {
            return null;
        }
        if (r.size() > 1) {
            throw new IllegalArgumentException("Too many results (" + r.size() + " but expected one only)");
        }
        return r.get(0);
    }

    @Override
    public NutsDefinition fetchFirst() {
        List<NutsDefinition> r = fetch();
        if (r.isEmpty()) {
            return null;
        }
        return r.get(0);
    }

    @Override
    public List<NutsId> find() {
        List<NutsId> mi = CollectionUtils.toList(ws.findIterator(build()));
        NutsIdListBuilder li = new NutsIdListBuilder(true);
        if (includeDependencies) {
            if (includeMain) {
                for (NutsId nutsId : mi) {
                    li.add(nutsId);
                }
            }
            for (NutsId nutsFile : findDependencies(mi)) {
                li.add(nutsFile);
            }
        } else {
            for (NutsId nutsId : mi) {
                li.add(nutsId);
            }
        }
        List<NutsId> r = li.build();
        if (this.isSort()) {
            r.sort(DefaultNutsIdComparator.INSTANCE);
        }
        return r;
    }

    @Override
    public List<NutsDefinition> fetch() {
        List<NutsId> mi = find();
        List<NutsDefinition> li = new ArrayList<>(mi.size());
        NutsSession s = session;
        if (s == null) {
            s = ws.createSession();
        }
        for (NutsId nutsId : mi) {
            NutsDefinition r = null;
            try {
                r = ws.fetchDefinition(nutsId, creationFetchOptions(), s);
            } catch (NutsNotFoundException ex) {
                if (!isIgnoreNotFound()) {
                    if (!nutsId.isOptional()) {
                        throw ex;
                    }
                }
            }
            if (r != null) {
                li.add(r);
            }
        }
        return li;
    }

    private DefaultFetchOptions creationFetchOptions() {
        return new DefaultFetchOptions()
                .setContent(isIncludeFile())
                .setEffectiveDesc(isIncludeEffective())
                .setInstallInfo(isIncludeInstallInformation())
                .setIgnoreCache(isIgnoreCache())
                .setPreferInstalled(isPreferInstalled())
                .setInstalledOnly(isInstalledOnly());
    }

    @Override
    public Iterator<NutsId> findIterator() {
        final Iterator<NutsId> base = (includeDependencies) ? find().iterator() : ws.findIterator(build());
        if (ignoreNotFound) {
            return new Iterator<NutsId>() {
                NutsId n;

                @Override
                public boolean hasNext() {
                    while (base.hasNext()) {
                        try {
                            n = base.next();
                            return n != null;
                        } catch (NutsNotFoundException ex) {
                            //
                        }
                    }
                    return false;
                }

                @Override
                public NutsId next() {
                    return n;
                }
            };
        } else {
            return base;
        }
    }

    @Override
    public Iterator<NutsDefinition> fetchIterator() {
        Iterator<NutsId> base = findIterator();
        NutsSession s = ws.createSession();
        return new Iterator<NutsDefinition>() {
            private NutsDefinition n = null;

            @Override
            public boolean hasNext() {
                while (base.hasNext()) {
                    try {
                        NutsId p = base.next();
                        n = ws.fetchDefinition(p, creationFetchOptions(), s);
                        return n != null;
                    } catch (NutsNotFoundException ex) {
                        if (!ignoreNotFound) {
                            throw ex;
                        }
                    }
                }
                return false;
            }

            @Override
            public NutsDefinition next() {
                return n;
            }
        };
    }

    private NutsId[] findDependencies(List<NutsId> ids) {
        NutsSession _session = this.session == null ? ws.createSession() : this.session;
        NutsDependencyFilter _dependencyFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(
                new NutsDependencyScopeFilter(getScope()),
                getAcceptOptional() == null ? null : NutsDependencyOptionFilter.valueOf(getAcceptOptional()),
                getDependencyFilter()
        ));
        NutsIdGraph graph = new NutsIdGraph(ws, _session, ignoreNotFound);
        graph.push(ids, _dependencyFilter);
        return graph.collect(ids, ids);
    }

    @Override
    public NutsQuery dependenciesOnly() {
        includeMain = false;
        includeDependencies = true;
        return this;
    }

    @Override
    public NutsQuery mainAndDependencies() {
        includeMain = true;
        includeDependencies = true;
        return this;
    }

    @Override
    public NutsQuery includeDependencies() {
        return setIncludeDependencies(true);
    }

    @Override
    public NutsQuery includeDependencies(boolean include) {
        return setIncludeDependencies(include);
    }

    @Override
    public NutsQuery setIncludeDependencies(boolean include) {
        includeDependencies = include;
        return this;
    }

    @Override
    public NutsQuery mainOnly() {
        includeMain = true;
        includeDependencies = false;
        return this;
    }

    @Override
    public Boolean getAcceptOptional() {
        return acceptOptional;
    }

    @Override
    public NutsQuery setAcceptOptional(Boolean acceptOptional) {
        this.acceptOptional = acceptOptional;
        return this;
    }

    @Override
    public NutsQuery setIncludeOptional(boolean includeOptional) {
        return setAcceptOptional(includeOptional ? null : false);
    }

    @Override
    public String findNutspathString() {
        StringBuilder sb = new StringBuilder();
        for (NutsId nutsDefinition : find()) {
            if (nutsDefinition != null) {
                if (sb.length() > 0) {
                    sb.append(";");
                }
                sb.append(nutsDefinition.setNamespace(null).toString());
            }
        }
        return sb.toString();
    }

    @Override
    public String findClasspathString() {
        StringBuilder sb = new StringBuilder();
        for (NutsDefinition nutsDefinition : fetch()) {
            if (nutsDefinition.getContent().getFile() != null) {
                if (sb.length() > 0) {
                    sb.append(File.pathSeparator);
                }
                sb.append(nutsDefinition.getContent().getFile());
            }
        }
        return sb.toString();
    }

    @Override
    public boolean isIgnoreNotFound() {
        return ignoreNotFound;
    }

    @Override
    public DefaultNutsQuery setIgnoreNotFound(boolean ignoreNotFound) {
        this.ignoreNotFound = ignoreNotFound;
        return this;
    }

    @Override
    public boolean isIncludeFile() {
        return includeContent;
    }

    @Override
    public NutsQuery setIncludeFile(boolean includeContent) {
        this.includeContent = includeContent;
        return this;
    }

    @Override
    public boolean isIncludeInstallInformation() {
        return includeInstallInfo;
    }

    @Override
    public NutsQuery setIncludeInstallInformation(boolean includeInstallInfo) {
        this.includeInstallInfo = includeInstallInfo;
        return this;
    }

    @Override
    public boolean isIncludeEffective() {
        return includeEffectiveDesc;
    }

    @Override
    public NutsQuery setIncludeEffective(boolean includeEffectiveDesc) {
        this.includeEffectiveDesc = includeEffectiveDesc;
        return this;
    }

    @Override
    public boolean isIgnoreCache() {
        return ignoreCache;
    }

    @Override
    public NutsQuery setIgnoreCache(boolean ignoreCache) {
        this.ignoreCache = ignoreCache;
        return this;
    }

    @Override
    public NutsQuery ignoreCache() {
        return setIgnoreCache(true);
    }

    public boolean isPreferInstalled() {
        return preferInstalled;
    }

    @Override
    public NutsQuery setPreferInstalled(boolean preferInstalled) {
        this.preferInstalled = preferInstalled;
        return this;
    }

    public boolean isInstalledOnly() {
        return installedOnly;
    }

    @Override
    public NutsQuery setInstalledOnly(boolean installedOnly) {
        this.installedOnly = installedOnly;
        return this;
    }

}
