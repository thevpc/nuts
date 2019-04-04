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

import net.vpc.app.nuts.core.util.Basket;
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
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static net.vpc.app.nuts.core.util.CoreNutsUtils.And;
import static net.vpc.app.nuts.core.util.CoreNutsUtils.simplify;
import net.vpc.common.util.Converter;
import net.vpc.common.util.IteratorBuilder;
import net.vpc.common.util.IteratorUtils;

/**
 * @author vpc
 */
public class DefaultNutsQuery extends DefaultNutsQueryBaseOptions<NutsQuery> implements NutsQuery {

    private Comparator<NutsId> idComparator;
    private NutsDependencyFilter dependencyFilter;
    private NutsDescriptorFilter descriptorFilter;
    private NutsIdFilter idFilter;
    private NutsRepositoryFilter repositoryFilter;
    private NutsVersionFilter versionFilter;
    private boolean ignoreNotFound = false;
    private boolean includeAllVersions = true;
    private boolean includeDuplicatedVersions = true;
    private boolean includeMain = true;
    private boolean sort = false;
    private final DefaultNutsWorkspace ws;
    private final List<String> arch = new ArrayList<>();
    private final List<String> ids = new ArrayList<>();
    private final List<String> js = new ArrayList<>();
    private final List<String> packaging = new ArrayList<>();
    private final List<String> repos = new ArrayList<>();

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
        super.copyFrom(other);
        if (other != null) {
            NutsQuery o = other;
            this.idComparator = o.getSortIdComparator();
            this.dependencyFilter = o.getDependencyFilter();
            this.descriptorFilter = o.getDescriptorFilter();
            this.idFilter = o.getIdFilter();
            this.repositoryFilter = o.getRepositoryFilter();
            this.versionFilter = o.getVersionFilter();
            this.ignoreNotFound = o.isIgnoreNotFound();
            this.includeAllVersions = o.isIncludeAllVersions();
            this.includeDuplicatedVersions = o.isIncludeDuplicatedVersions();
            this.includeMain = o.isIncludeMain();
            this.sort = o.isSort();
            this.arch.clear();
            this.arch.addAll(Arrays.asList(o.getArch()));
            this.ids.clear();
            this.ids.addAll(Arrays.asList(o.getIds()));
            this.js.clear();
            this.js.addAll(Arrays.asList(o.getJs()));
            this.packaging.clear();
            this.packaging.addAll(Arrays.asList(o.getPackaging()));
            this.repos.clear();
            this.repos.addAll(Arrays.asList(o.getRepos()));
        }
        return this;
    }

    @Override
    public boolean isSort() {
        return sort;
    }

    @Override
    public NutsQuery sort() {
        return setSort(true);
    }

    @Override
    public NutsQuery sort(Comparator<NutsId> comparator) {
        this.idComparator = comparator;
        this.sort = true;
        return this;
    }

    @Override
    public NutsQuery setSort(boolean sort) {
        this.sort = sort;
        return this;
    }

    @Override
    public boolean isIncludeAllVersions() {
        return includeAllVersions;
    }

    @Override
    public NutsQuery latestVersions() {
        return setIncludeAllVersions(false);
    }

    @Override
    public NutsQuery allVersions() {
        return setIncludeAllVersions(true);
    }

    public NutsQuery setIncludeAllVersions(boolean includeAllVersions) {
        this.includeAllVersions = includeAllVersions;
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
    public String toString() {
        StringBuilder sb = new StringBuilder("NutsSearch{");
        sb.append(getScope());
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

        return new DefaultNutsSearch(
                goodIds.toArray(new String[0]), _repositoryFilter,
                _versionFilter, _idFilter, _descriptorFilter,
                ws,
                toOptions()
        );
    }

    public NutsQueryOptions toOptions() {
        DefaultNutsQueryOptions o = new DefaultNutsQueryOptions();
        o.copyFrom((NutsQueryBaseOptions)this);
        return o;
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
        return findBasket().list();
    }

    @Override
    public Stream<NutsDefinition> fetchStream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator<NutsDefinition>) fetchIterator(), Spliterator.ORDERED), false);
    }

    @Override
    public Stream<NutsId> findStream() {
        return findBasket().stream();
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
    public List<NutsDefinition> fetch() {
        List<NutsId> mi = find();
        List<NutsDefinition> li = new ArrayList<>(mi.size());
        NutsSession s = evalSession(true);
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

    private NutsQueryOptions creationFetchOptions() {
        return CoreNutsUtils.createQueryOptions()
                .setIncludeFile(isIncludeFile())
                .setCached(isCached())
                .setIncludeEffective(isIncludeEffective())
                .setIncludeInstallInformation(isIncludeInstallInformation());
    }

    @Override
    public Iterator<NutsId> findIterator() {
        return findBasket().iterator();
    }

    @Override
    public Iterable<NutsId> findIterable() {
        return new FindNutsIdIterable();
    }

    private Basket<NutsId> applyVersionFlagFilters(Iterator<NutsId> curr) {
        if (includeAllVersions && includeDuplicatedVersions) {
            return new Basket<NutsId>(curr);
            //nothind
        } else if (includeAllVersions && !includeDuplicatedVersions) {
            return new Basket<NutsId>(
                    IteratorBuilder.of(curr).unique(new Converter<NutsId,String>() {
                @Override
                public String convert(NutsId nutsId) {
                    return nutsId.getLongNameId().setAlternative(nutsId.getAlternative()).toString();
                }
            }).iterator());
        } else if (!includeAllVersions && !includeDuplicatedVersions) {
            Map<String, NutsId> visited = new LinkedHashMap<>();
            while (curr.hasNext()) {
                NutsId nutsId = curr.next();
                String k = nutsId.getSimpleNameId().setAlternative(nutsId.getAlternative()).toString();
                NutsId old = visited.get(k);
                if (old == null || old.getVersion().isEmpty() || old.getVersion().compareTo(nutsId.getVersion()) < 0) {
                    visited.put(k, nutsId);
                }
            }
            return new Basket<NutsId>(visited.values());
        } else if (!includeAllVersions && includeDuplicatedVersions) {
            Map<String, List<NutsId>> visited = new LinkedHashMap<>();
            while (curr.hasNext()) {
                NutsId nutsId = curr.next();
                String k = nutsId.getSimpleNameId().setAlternative(nutsId.getAlternative()).toString();
                List<NutsId> oldList = visited.get(k);
                if (oldList == null || oldList.get(0).getVersion().isEmpty() || oldList.get(0).getVersion().compareTo(nutsId.getVersion()) < 0) {
                    visited.put(k, new ArrayList<>(Arrays.asList(nutsId)));
                } else if (oldList.get(0).getVersion().compareTo(nutsId.getVersion()) == 0) {
                    oldList.add(nutsId);
                }
            }
            List<NutsId> list = new ArrayList<>();
            for (List<NutsId> li : visited.values()) {
                list.addAll(li);
            }
            return new Basket<NutsId>(list);
        }
        throw new IllegalArgumentException("Unexpected");
    }

    private Basket<NutsId> findBasket() {
        Iterator<NutsId> base0 = ws.findIterator(build());
        if (includeAllVersions && includeDuplicatedVersions && !sort && !isIncludeDependencies()) {
            return new Basket<NutsId>(base0);
        }
        Basket<NutsId> a = applyVersionFlagFilters(base0);
        Iterator<NutsId> curr = a.iterator();
        if (isIncludeDependencies()) {
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
        Basket<NutsId> curr2 = applyVersionFlagFilters(curr);
        if (sort) {
            List<NutsId> listToSort = curr2.list();
            listToSort.sort(idComparator == null ? DefaultNutsIdComparator.INSTANCE : idComparator);
            curr2 = new Basket<NutsId>(listToSort);
        }
        return curr2;
    }

//    private class IdToDefConverter implements ObjectConverter<NutsId, NutsDefinition> {
//
//        private NutsSession s;
//
//        public IdToDefConverter(NutsSession s) {
//            this.s = s;
//        }
//
//        @Override
//        public NutsDefinition convert(NutsId from) {
//            try {
//                return ws.fetchDefinition(from, creationFetchOptions(), s);
//            } catch (NutsNotFoundException ex) {
//                if (!ignoreNotFound) {
//                    throw ex;
//                }
//            }
//            return null;
//        }
//    }
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
        NutsSession _session = this.getSession() == null ? ws.createSession() : this.getSession();
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
        includeDependencies(true);
        return this;
    }

    @Override
    public NutsQuery mainAndDependencies() {
        includeMain = true;
        includeDependencies(true);
        return this;
    }

    @Override
    public NutsQuery mainOnly() {
        includeMain = true;
        includeDependencies(false);
        return this;
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
            if (nutsDefinition.getContent().getPath() != null) {
                if (sb.length() > 0) {
                    sb.append(File.pathSeparator);
                }
                sb.append(nutsDefinition.getContent().getPath());
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
    public boolean isIncludeDuplicatedVersions() {
        return includeDuplicatedVersions;
    }

    @Override
    public NutsQuery setIncludeDuplicateVersions(boolean includeDuplicateVersion) {
        this.includeDuplicatedVersions = includeDuplicateVersion;
        return this;
    }

    @Override
    public Comparator<NutsId> getSortIdComparator() {
        return idComparator;
    }

    @Override
    public boolean isIncludeMain() {
        return includeMain;
    }

    private class FindNutsIdIterable implements Iterable<NutsId> {

        public FindNutsIdIterable() {
        }

        @Override
        public Iterator<NutsId> iterator() {
            return findBasket().iterator();
        }
    }

}
