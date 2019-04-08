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

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.vpc.app.nuts.core.filters.DefaultNutsIdMultiFilter;

import static net.vpc.app.nuts.core.util.CoreNutsUtils.And;
import static net.vpc.app.nuts.core.util.CoreNutsUtils.simplify;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.bundledlibs.util.IteratorBuilder;
import net.vpc.app.nuts.core.util.bundledlibs.util.IteratorUtils;
import net.vpc.app.nuts.core.util.bundledlibs.util.LazyIterator;

/**
 * @author vpc
 */
public class DefaultNutsFindCommand extends DefaultNutsQueryBaseOptions<NutsFindCommand> implements NutsFindCommand {

    private Comparator<NutsId> idComparator;
    private NutsDependencyFilter dependencyFilter;
    private NutsDescriptorFilter descriptorFilter;
    private NutsIdFilter idFilter;
    private NutsRepositoryFilter repositoryFilter;
    private NutsVersionFilter versionFilter;
    private boolean lenient = false;
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

    public DefaultNutsFindCommand(DefaultNutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsFindCommand addJs(Collection<String> value) {
        if (value != null) {
            addJs(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsFindCommand addJs(String... value) {
        if (value != null) {
            js.addAll(Arrays.asList(value));
        }
        return this;

    }

    @Override
    public NutsFindCommand addId(Collection<String> value) {
        if (value != null) {
            addId(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsFindCommand addId(String... value) {
        if (value != null) {
            for (String s : value) {
                if (!CoreStringUtils.isBlank(s)) {
                    ids.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NutsFindCommand addId(NutsId... value) {
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
    public NutsFindCommand setId(String value) {
        ids.clear();
        if (!CoreStringUtils.isBlank(value)) {
            ids.add(value);
        }
        return this;
    }

    @Override
    public NutsFindCommand setId(NutsId value) {
        if (value != null) {
            ids.clear();
            ids.add(value.toString());
        }
        return this;
    }

    @Override
    public NutsFindCommand addArch(Collection<String> value) {
        if (value != null) {
            addArch(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsFindCommand addArch(String... value) {
        if (value != null) {
            arch.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsFindCommand addPackaging(Collection<String> value) {
        if (value != null) {
            addPackaging(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsFindCommand addPackaging(String... value) {
        if (value != null) {
            this.packaging.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsFindCommand addRepository(Collection<String> value) {
        if (value != null) {
            addRepository(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsFindCommand addRepository(String... value) {
        if (value != null) {
            repos.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsFindCommand copy() {
        DefaultNutsFindCommand b = new DefaultNutsFindCommand(ws);
        b.copyFrom(this);
        return b;
    }

    @Override
    public NutsFindCommand copyFrom(NutsFetchCommand other) {
        super.copyFrom0((DefaultNutsQueryBaseOptions) other);
        return this;
    }

    @Override
    public NutsFindCommand copyFrom(NutsFindCommand other) {
        super.copyFrom0((DefaultNutsQueryBaseOptions) other);
        if (other != null) {
            NutsFindCommand o = other;
            this.idComparator = o.getSortIdComparator();
            this.dependencyFilter = o.getDependencyFilter();
            this.descriptorFilter = o.getDescriptorFilter();
            this.idFilter = o.getIdFilter();
            this.repositoryFilter = o.getRepositoryFilter();
            this.versionFilter = o.getVersionFilter();
            this.lenient = o.isLenient();
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
    public NutsFindCommand sort() {
        return setSort(true);
    }

    @Override
    public NutsFindCommand sort(Comparator<NutsId> comparator) {
        this.idComparator = comparator;
        this.sort = true;
        return this;
    }

    @Override
    public NutsFindCommand setSort(boolean sort) {
        this.sort = sort;
        return this;
    }

    @Override
    public boolean isIncludeAllVersions() {
        return includeAllVersions;
    }

    @Override
    public NutsFindCommand latestVersions() {
        return setIncludeAllVersions(false);
    }

    @Override
    public NutsFindCommand allVersions() {
        return setIncludeAllVersions(true);
    }

    public NutsFindCommand setIncludeAllVersions(boolean includeAllVersions) {
        this.includeAllVersions = includeAllVersions;
        return this;
    }

    @Override
    public NutsFindCommand addIds(String... ids) {
        if (ids != null) {
            for (String id : ids) {
                addId(id);
            }
        }
        return this;
    }

    @Override
    public NutsFindCommand addId(NutsId id) {
        if (id != null) {
            addId(id.toString());
        }
        return this;
    }

    @Override
    public NutsFindCommand addIds(NutsId... ids) {
        if (ids != null) {
            for (NutsId id : ids) {
                addId(id == null ? null : id.toString());
            }
        }
        return this;
    }

    @Override
    public NutsFindCommand setIds(String... ids) {
        this.ids.clear();
        addIds(ids);
        return this;
    }

    @Override
    public NutsFindCommand id(String id) {
        return setId(id);
    }

    @Override
    public NutsFindCommand addId(String id) {
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
    public NutsFindCommand setDependencyFilter(NutsDependencyFilter filter) {
        this.dependencyFilter = filter;
        return this;
    }

    @Override
    public NutsDependencyFilter getDependencyFilter() {
        return dependencyFilter;
    }

    @Override
    public NutsFindCommand setDependencyFilter(String filter) {
        this.dependencyFilter = CoreStringUtils.isBlank(filter) ? null : new NutsDependencyJavascriptFilter(filter);
        return this;
    }

    @Override
    public NutsFindCommand setRepositoryFilter(NutsRepositoryFilter filter) {
        this.repositoryFilter = filter;
        return this;
    }

    @Override
    public NutsRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    @Override
    public NutsFindCommand setRepositoryFilter(String filter) {
        this.repositoryFilter = CoreStringUtils.isBlank(filter) ? null : new ExprNutsRepositoryFilter(filter);
        return this;
    }

    @Override
    public NutsFindCommand setVersionFilter(NutsVersionFilter filter) {
        this.versionFilter = filter;
        return this;
    }

    @Override
    public NutsVersionFilter getVersionFilter() {
        return versionFilter;
    }

    @Override
    public NutsFindCommand setVersionFilter(String filter) {
        this.versionFilter = CoreStringUtils.isBlank(filter) ? null : new NutsVersionJavascriptFilter(filter);
        return this;
    }

    @Override
    public NutsFindCommand setDescriptorFilter(NutsDescriptorFilter filter) {
        this.descriptorFilter = filter;
        return this;
    }

    @Override
    public NutsDescriptorFilter getDescriptorFilter() {
        return descriptorFilter;
    }

    @Override
    public NutsFindCommand setDescriptorFilter(String filter) {
        this.descriptorFilter = CoreStringUtils.isBlank(filter) ? null : new NutsDescriptorJavascriptFilter(filter);
        return this;
    }

    @Override
    public NutsFindCommand setIdFilter(NutsIdFilter filter) {
        this.idFilter = filter;
        return this;
    }

    @Override
    public NutsIdFilter getIdFilter() {
        return idFilter;
    }

    @Override
    public NutsFindCommand setIdFilter(String filter) {
        this.idFilter = CoreStringUtils.isBlank(filter) ? null : new NutsJavascriptIdFilter(filter);
        return this;
    }

    @Override
    public NutsFindCommand setIds(Collection<String> ids) {
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
            if (!CoreStringUtils.isBlank(j)) {
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
                toFetch()
        );
    }

    public NutsFetchCommand toFetch() {
        return new DefaultNutsFetch(ws).copyFrom0((DefaultNutsQueryBaseOptions) this).setSession(evalSession(true));
    }

    @Override
    public NutsFindResult<NutsId> getResultIds() {
        return findBasket();
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
    public NutsFindResult<NutsDefinition> getResultDefinitions() {
        return new NutsDefinitionNutsFindResult();
    }

    private DefaultNutsFindResult<NutsId> applyVersionFlagFilters(Iterator<NutsId> curr) {
        if (includeAllVersions && includeDuplicatedVersions) {
            return new DefaultNutsFindResult<NutsId>(curr);
            //nothind
        } else if (includeAllVersions && !includeDuplicatedVersions) {
            return new DefaultNutsFindResult<NutsId>(
                    IteratorBuilder.of(curr).unique(new Function<NutsId, String>() {
                        @Override
                        public String apply(NutsId nutsId) {
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
            return new DefaultNutsFindResult<NutsId>(visited.values());
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
            return new DefaultNutsFindResult<NutsId>(list);
        }
        throw new IllegalArgumentException("Unexpected");
    }

    private DefaultNutsFindResult<NutsId> findBasket() {
        Iterator<NutsId> base0 = findIterator(build());
        if (includeAllVersions && includeDuplicatedVersions && !sort && !isIncludeDependencies()) {
            return new DefaultNutsFindResult<NutsId>(base0);
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
        DefaultNutsFindResult<NutsId> curr2 = applyVersionFlagFilters(curr);
        if (sort) {
            List<NutsId> listToSort = curr2.list();
            listToSort.sort(idComparator == null ? DefaultNutsIdComparator.INSTANCE : idComparator);
            curr2 = new DefaultNutsFindResult<NutsId>(listToSort);
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
//    @Override
//    public Iterable<NutsDefinition> fetchIterable() {
//        return new Iterable<NutsDefinition>() {
//            @Override
//            public Iterator<NutsDefinition> iterator() {
//                return fetchIterator();
//            }
//        };
//    }
    private NutsId[] findDependencies(List<NutsId> ids) {
        NutsSession _session = this.getSession() == null ? ws.createSession() : this.getSession();
        NutsDependencyFilter _dependencyFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(
                new NutsDependencyScopeFilter(getScope()),
                getAcceptOptional() == null ? null : NutsDependencyOptionFilter.valueOf(getAcceptOptional()),
                getDependencyFilter()
        ));
        NutsIdGraph graph = new NutsIdGraph(ws, _session, lenient);
        graph.push(ids, _dependencyFilter);
        return graph.collect(ids, ids);
    }

    @Override
    public NutsFindCommand dependenciesOnly() {
        includeMain = false;
        includeDependencies(true);
        return this;
    }

    @Override
    public NutsFindCommand mainAndDependencies() {
        includeMain = true;
        includeDependencies(true);
        return this;
    }

    @Override
    public NutsFindCommand mainOnly() {
        includeMain = true;
        includeDependencies(false);
        return this;
    }

    @Override
    public String getResultNutsPath() {
        StringBuilder sb = new StringBuilder();
        for (NutsId nutsDefinition : getResultIds()) {
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
    public String getResultClassPath() {
        StringBuilder sb = new StringBuilder();
        for (NutsDefinition nutsDefinition : getResultDefinitions()) {
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
    public boolean isLenient() {
        return lenient;
    }

    @Override
    public DefaultNutsFindCommand setLenient(boolean ignoreNotFound) {
        this.lenient = ignoreNotFound;
        return this;
    }

    @Override
    public NutsFindCommand lenient() {
        return setLenient(true);
    }

    @Override
    public NutsFindCommand lenient(boolean lenient) {
        return setLenient(lenient);
    }

    @Override
    public boolean isIncludeDuplicatedVersions() {
        return includeDuplicatedVersions;
    }

    @Override
    public NutsFindCommand setIncludeDuplicateVersions(boolean includeDuplicateVersion) {
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

    private static class DefaultNutsFindResult<T> extends Basket<T> implements NutsFindResult<T> {

        public DefaultNutsFindResult(Iterator<T> o) {
            super(o);
        }

        public DefaultNutsFindResult(Collection<T> o) {
            super(o);
        }

        public DefaultNutsFindResult(List<T> o) {
            super(o);
        }

    }

    private class NutsDefinitionNutsFindResult extends AbstractNutsFindResult<NutsDefinition> {

        public NutsDefinitionNutsFindResult() {
        }

        @Override
        public List<NutsDefinition> list() {
            List<NutsId> mi = getResultIds().list();
            List<NutsDefinition> li = new ArrayList<>(mi.size());
//            NutsSession s = evalSession(true);
            NutsFetchCommand fetch = toFetch();
            for (NutsId nutsId : mi) {
                li.add(fetch.id(nutsId).getResultDefinition());
            }
            return li;
        }

        @Override
        public Stream<NutsDefinition> stream() {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator<NutsDefinition>) iterator(), Spliterator.ORDERED), false);
        }

        @Override
        public Iterator<NutsDefinition> iterator() {
            Iterator<NutsId> base = getResultIds().iterator();
            NutsSession s = ws.createSession();
            NutsFetchCommand fetch = toFetch();
            return new Iterator<NutsDefinition>() {
                private NutsDefinition n = null;

                @Override
                public boolean hasNext() {
                    while (base.hasNext()) {
                        NutsDefinition d = fetch.id(base.next()).getResultDefinition();
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

        }

    }

    public Iterator<NutsId> findIterator(DefaultNutsSearch search) {
        NutsSession session = CoreNutsUtils.validateSession(search.getOptions().getSession(), ws);
        NutsVersionFilter versionFilter = search.getVersionFilter();
        NutsIdFilter idFilter = search.getIdFilter();
        NutsRepositoryFilter repositoryFilter = search.getRepositoryFilter();
        NutsDescriptorFilter descriptorFilter = search.getDescriptorFilter();
        String[] goodIds = search.getIds();
        NutsFetchStrategy fetchMode = NutsWorkspaceHelper.validate(search.getOptions().getFetchStrategy());
        if (goodIds.length > 0) {
            List<Iterator<NutsId>> result = new ArrayList<>();
            for (String id : goodIds) {
                NutsId nutsId = ws.parser().parseId(id);
                if (nutsId != null) {
                    List<NutsId> nutsId2 = new ArrayList<>();
                    if (nutsId.getGroup() == null) {
                        for (String aImport : ws.config().getImports()) {
                            nutsId2.add(nutsId.setGroup(aImport));
                        }
                    } else {
                        nutsId2.add(nutsId);
                    }
                    List<Iterator<NutsId>> coalesce = new ArrayList<>();
                    for (NutsFetchMode mode : fetchMode) {
                        List<Iterator<NutsId>> all = new ArrayList<>();
                        for (NutsId nutsId1 : nutsId2) {
                            if (mode == NutsFetchMode.INSTALLED) {
                                all.add(
                                        IteratorBuilder.ofLazy(new Iterable<NutsId>() {
                                            @Override
                                            public Iterator<NutsId> iterator() {
                                                NutsIdFilter filter = new DefaultNutsIdMultiFilter(
                                                        nutsId1.getQueryMap(), idFilter,
                                                        versionFilter, descriptorFilter, null,
                                                        NutsWorkspaceHelper.createNoRepositorySession(session, mode,
                                                                search.getOptions())
                                                ).simplify();
                                                return ws.getInstalledRepository().findVersions(nutsId1, filter);
                                            }
                                        }).safeIgnore().iterator());
                            } else {
                                for (NutsRepository repo : ws.getEnabledRepositories(NutsWorkspaceHelper.FilterMode.FIND, nutsId1, repositoryFilter, session, mode, search.getOptions())) {
                                    if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                                        NutsIdFilter filter = new DefaultNutsIdMultiFilter(nutsId1.getQueryMap(), idFilter,
                                                versionFilter, descriptorFilter, repo, NutsWorkspaceHelper.createRepositorySession(session, repo, mode, search.getOptions())).simplify();
                                        all.add(
                                                IteratorBuilder.ofLazy(new Iterable<NutsId>() {
                                                    @Override
                                                    public Iterator<NutsId> iterator() {
                                                        return repo.findVersions(nutsId1, filter, NutsWorkspaceHelper.createRepositorySession(session, repo, mode, search.getOptions()));
                                                    }
                                                }).safeIgnore().iterator()
                                        );
                                    }
                                }
                            }
                        }
                        coalesce.add(IteratorUtils.concat(all));
                    }
                    if (nutsId.getGroup() == null) {
                        //now will look with *:artifactId pattern
                        NutsFindCommand search2 = ws.find()
                                .copyFrom(search.getOptions())
                                .addId(search.getIds())
                                .setIdFilter(search.getIdFilter())
                                .setRepositoryFilter(search.getRepositoryFilter())
                                .setVersionFilter(search.getVersionFilter())
                                .setDescriptorFilter(search.getDescriptorFilter())
                                .setFetchStratery(search.getOptions().getFetchStrategy())
                                .setSession(session)
                                .setIds();
                        search2.setIdFilter(new NutsIdFilterOr(
                                new NutsPatternIdFilter(new String[]{nutsId.setGroup("*").toString()}),
                                CoreNutsUtils.simplify(search2.getIdFilter())
                        ));
                        coalesce.add(search2.getResultIds().iterator());
                    }
                    result.add(fetchMode.isStopFast()
                            ? IteratorUtils.coalesce(coalesce)
                            : IteratorUtils.concat(coalesce)
                    );
                }
            }
            return IteratorUtils.concat(result);
        }

        if (idFilter instanceof NutsPatternIdFilter) {
            String[] ids = ((NutsPatternIdFilter) idFilter).getIds();
            if (ids.length == 1) {
                String id = ids[0];
                if (id.indexOf('*') < 0 && id.indexOf(':') > 0) {
                    NutsId nid = ws.parser().parseId(id);
                    if (nid != null) {
                        List<Iterator<NutsId>> coalesce = new ArrayList<>();
                        for (NutsFetchMode mode : fetchMode) {
                            if (mode == NutsFetchMode.INSTALLED) {
                                coalesce.add(new LazyIterator<>(new Iterable<NutsId>() {
                                    @Override
                                    public Iterator<NutsId> iterator() {
                                        NutsIdFilter filter = new DefaultNutsIdMultiFilter(nid.getQueryMap(), idFilter, versionFilter,
                                                descriptorFilter, null, NutsWorkspaceHelper.createNoRepositorySession(session, mode, search.getOptions())).simplify();
                                        return ws.getInstalledRepository().findVersions(nid, filter);
                                    }
                                }));

                            } else {
                                List<Iterator<NutsId>> all = new ArrayList<>();
                                for (NutsRepository repo : ws.getEnabledRepositories(repositoryFilter)) {
                                    try {
                                        if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                                            NutsRepositorySession ss = NutsWorkspaceHelper.createRepositorySession(session, repo, mode, search.getOptions());
                                            DefaultNutsIdMultiFilter filter = new DefaultNutsIdMultiFilter(nid.getQueryMap(), idFilter,
                                                    versionFilter, descriptorFilter, repo, ss);
                                            all.add(new LazyIterator<>(new Iterable<NutsId>() {
                                                @Override
                                                public Iterator<NutsId> iterator() {
                                                    return repo.findVersions(nid, filter, ss);
                                                }
                                            }));
                                        }
                                    } catch (Exception exc) {
                                        //
                                    }
                                }
                                coalesce.add(IteratorUtils.concat(all));
                            }
                        }
                        return fetchMode.isStopFast()
                                ? IteratorUtils.coalesce(coalesce)
                                : IteratorUtils.concat(coalesce);
                    }
                }
            }
        }

        List<Iterator<NutsId>> coalesce = new ArrayList<>();
        for (NutsFetchMode mode : fetchMode) {
            List<Iterator<NutsId>> all = new ArrayList<>();
            for (NutsRepository repo : ws.getEnabledRepositories(repositoryFilter)) {
                if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                    NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, repo, mode, search.getOptions());
                    NutsIdFilter filter = new DefaultNutsIdMultiFilter(null, idFilter, versionFilter, descriptorFilter, repo, rsession).simplify();
                    all.add(
                            IteratorBuilder.ofLazy(new Iterable<NutsId>() {
                                @Override
                                public Iterator<NutsId> iterator() {
                                    return repo.find(filter, rsession);
                                }
                            }).safeIgnore().iterator()
                    );
                }

            }
            coalesce.add(IteratorUtils.concat(all));
        }
        return fetchMode.isStopFast() ? IteratorUtils.coalesce(coalesce) : IteratorUtils.concat(coalesce);
    }

}
