/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.core.*;
import net.vpc.app.nuts.extensions.filters.dependency.NutsDependencyJavascriptFilter;
import net.vpc.app.nuts.extensions.filters.dependency.NutsDependencyOptionFilter;
import net.vpc.app.nuts.extensions.filters.dependency.NutsDependencyScopeFilter;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorFilterArch;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorFilterPackaging;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorJavascriptFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsIdFilterOr;
import net.vpc.app.nuts.extensions.filters.id.NutsJavascriptIdFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsPatternIdFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsSimpleIdFilter;
import net.vpc.app.nuts.extensions.filters.repository.DefaultNutsRepositoryFilter;
import net.vpc.app.nuts.extensions.filters.repository.ExprNutsRepositoryFilter;
import net.vpc.app.nuts.extensions.filters.version.NutsVersionJavascriptFilter;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.CollectionUtils;

import java.util.*;

import static net.vpc.app.nuts.extensions.util.CoreNutsUtils.And;
import static net.vpc.app.nuts.extensions.util.CoreNutsUtils.simplify;

/**
 * @author vpc
 */
public class DefaultNutsQuery implements NutsQuery {

    private List<String> ids = new ArrayList<>();
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
    private DefaultNutsWorkspace ws;
    private boolean includeMain = true;
    private boolean includeDependencies = false;
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
            ids.addAll(Arrays.asList(value));
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
    public void copyFrom(NutsQuery other) {
        setAll(other);
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
    public NutsQuery setScope(Set<NutsDependencyScope> scope) {
        this.scope = scope==null?EnumSet.noneOf(NutsDependencyScope.class):EnumSet.<NutsDependencyScope>copyOf(scope);
        return this;
    }

    @Override
    public NutsQuery addScope(Collection<NutsDependencyScope> scope) {
        this.scope=NutsDependencyScope.add(this.scope,scope);
        return this;
    }

    @Override
    public NutsQuery addScope(NutsDependencyScope scope) {
        this.scope=NutsDependencyScope.add(this.scope,scope);
        return this;
    }

    @Override
    public NutsQuery addScope(NutsDependencyScope... scope) {
        this.scope=NutsDependencyScope.add(this.scope,scope);
        return this;
    }

    @Override
    public NutsQuery removeScope(Collection<NutsDependencyScope> scope) {
        this.scope=NutsDependencyScope.remove(this.scope,scope);
        return this;
    }

    @Override
    public NutsQuery removeScope(NutsDependencyScope scope) {
        this.scope=NutsDependencyScope.remove(this.scope,scope);
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
    public NutsQuery setIds(List<String> ids) {
        this.ids = ids;
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


        NutsDescriptorFilter descriptorFilter = null;
        NutsIdFilter idFilter = null;
        NutsDependencyFilter depFilter = null;
        DefaultNutsRepositoryFilter rfilter = null;
        for (String j : this.getJs()) {
            if (!StringUtils.isEmpty(j)) {
                if (CoreStringUtils.containsTopWord(j, "descriptor")) {
                    descriptorFilter = simplify(And(descriptorFilter, NutsDescriptorJavascriptFilter.valueOf(j)));
                } else if (CoreStringUtils.containsTopWord(j, "dependency")) {
                    depFilter = simplify(And(depFilter, NutsDependencyJavascriptFilter.valueOf(j)));
                } else {
                    idFilter = simplify(And(idFilter, NutsJavascriptIdFilter.valueOf(j)));
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

        descriptorFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(descriptorFilter, packs, archs));

        if (this.getRepos().length > 0) {
            rfilter = new DefaultNutsRepositoryFilter(new HashSet<>(Arrays.asList(this.getRepos())));
        }


        NutsRepositoryFilter repositoryFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(rfilter, this.getRepositoryFilter()));
        NutsVersionFilter versionFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(null, this.getVersionFilter()));
        descriptorFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(descriptorFilter, this.getDescriptorFilter()));
        idFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(idFilter, idFilter0));

        if (!wildcardIds.isEmpty()) {
            NutsPatternIdFilter ff = new NutsPatternIdFilter(wildcardIds.toArray(new String[0]));
            idFilter = CoreNutsUtils.simplify(new NutsIdFilterOr(idFilter, ff));
        }

        return new DefaultNutsSearch(goodIds.toArray(new String[0]), repositoryFilter, versionFilter, sort, idFilter, latestVersions, descriptorFilter, ws, session);
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
            r.sort(NutsIdComparator.INSTANCE);
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
            NutsDefinition r = ws.fetchSimple(nutsId, s);
            li.add(r);
        }
        return li;
    }

    @Override
    public Iterator<NutsId> findIterator() {
        if (includeDependencies) {
            return find().iterator();
        } else {
            return ws.findIterator(build());
        }
    }

    @Override
    public Iterator<NutsDefinition> fetchIterator() {
        Iterator<NutsId> base = findIterator();
        NutsSession s = ws.createSession();
        return new Iterator<NutsDefinition>() {
            @Override
            public boolean hasNext() {
                return base.hasNext();
            }

            @Override
            public NutsDefinition next() {
                NutsId p = base.next();
                return ws.fetchSimple(p, s);
            }
        };
    }

    private NutsId[] findDependencies(List<NutsId> ids) {
        NutsSession session = this.session == null ? ws.createSession() : this.session;
        NutsDependencyFilter dependencyFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(
                new NutsDependencyScopeFilter(getScope()),
                getAcceptOptional()==null?null:NutsDependencyOptionFilter.valueOf(getAcceptOptional()),
                getDependencyFilter()
        ));
        NutsIdGraph graph = new NutsIdGraph(ws, session);
        graph.push(ids, dependencyFilter);
        return graph.collect(ids, ids);
    }


    private static class NutsIdComparator implements Comparator<NutsId> {
        public static final NutsIdComparator INSTANCE = new NutsIdComparator();

        @Override
        public int compare(NutsId o1, NutsId o2) {
            int x = o1.getSimpleName().compareTo(o2.getSimpleName());
            if (x != 0) {
                return x;
            }
            //latests versions first
            x = o1.getVersion().compareTo(o2.getVersion());
            return -x;
        }
    }

    @Override
    public NutsQuery dependenciesOnly() {
        includeMain = false;
        includeDependencies = true;
        return this;
    }

    @Override
    public NutsQuery includeDependencies() {
        includeMain = true;
        includeDependencies = true;
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
        return setAcceptOptional(includeOptional?null:false);
    }
}
