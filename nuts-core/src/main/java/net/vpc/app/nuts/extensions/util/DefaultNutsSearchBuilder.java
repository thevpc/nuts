/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.core.DefaultNutsSearch;
import net.vpc.app.nuts.extensions.filters.dependency.NutsDependencyJavascriptFilter;
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

import java.util.*;

import static net.vpc.app.nuts.extensions.util.CoreNutsUtils.And;
import static net.vpc.app.nuts.extensions.util.CoreNutsUtils.simplify;

/**
 *
 * @author vpc
 */
public class DefaultNutsSearchBuilder implements NutsSearchBuilder{

    private List<String> ids = new ArrayList<>();
    private NutsIdFilter idFilter;
    private NutsDependencyFilter dependencyFilter;
    private NutsRepositoryFilter repositoryFilter;
    private NutsVersionFilter versionFilter;
    private NutsDescriptorFilter descriptorFilter;
    private NutsDependencyScope scope = NutsDependencyScope.RUN;
    private boolean latestVersions;
    private boolean sort = true;
    private final List<String> js = new ArrayList<>();
    private final List<String> arch = new ArrayList<>();
    private final List<String> packagings = new ArrayList<>();
    private final List<String> repos = new ArrayList<>();

    public DefaultNutsSearchBuilder() {

    }

    public DefaultNutsSearchBuilder(String... ids) {
        addIds(ids);
    }

    public DefaultNutsSearchBuilder(NutsId... ids) {
        addIds(ids);
    }

    public DefaultNutsSearchBuilder(DefaultNutsSearchBuilder other) {
        copyFrom(other);
    }

    @Override
    public NutsSearchBuilder addJs(Collection<String> value) {
        if (value != null) {
            addJs(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsSearchBuilder addJs(String... value) {
        if (value != null) {
            js.addAll(Arrays.asList(value));
        }
        return this;

    }

    @Override
    public NutsSearchBuilder addId(Collection<String> value) {
        if (value != null) {
            addId(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsSearchBuilder addId(String... value) {
        if (value != null) {
            ids.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsSearchBuilder addArch(Collection<String> value) {
        if (value != null) {
            addArch(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsSearchBuilder addArch(String... value) {
        if (value != null) {
            arch.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsSearchBuilder addPackaging(Collection<String> value) {
        if (value != null) {
            addPackaging(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsSearchBuilder addPackaging(String... value) {
        if (value != null) {
            packagings.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsSearchBuilder addRepository(Collection<String> value) {
        if (value != null) {
            addRepository(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsSearchBuilder addRepository(String... value) {
        if (value != null) {
            repos.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsSearchBuilder copy() {
        return new DefaultNutsSearchBuilder(this);
    }

    @Override
    public void copyFrom(NutsSearchBuilder other) {
        setAll(other);
    }


    @Override
    public NutsSearchBuilder setAll(NutsSearch other) {
        if (other != null) {
            ids.addAll(Arrays.asList(other.getIds()));
            idFilter = other.getIdFilter();
//            dependencyFilter = other.getDependencyFilter();
            repositoryFilter = other.getRepositoryFilter();
            versionFilter = other.getVersionFilter();
            descriptorFilter = other.getDescriptorFilter();
//            scope = other.getScope();
            latestVersions = other.isLatestVersions();
            sort = other.isSort();
        }
        return this;
    }

    @Override
    public NutsSearchBuilder setAll(NutsSearchBuilder other) {
        if (other != null) {
            ids.addAll(Arrays.asList(other.getIds()));
            idFilter = other.getIdFilter();
            dependencyFilter = other.getDependencyFilter();
            repositoryFilter = other.getRepositoryFilter();
            versionFilter = other.getVersionFilter();
            descriptorFilter = other.getDescriptorFilter();
            scope = other.getScope();
            latestVersions = other.isLatestVersions();
            sort = other.isSort();
        }
        return this;
    }

    @Override
    public boolean isSort() {
        return sort;
    }

    @Override
    public NutsSearchBuilder setSort(boolean sort) {
        this.sort = sort;
        return this;
    }

    @Override
    public boolean isLatestVersions() {
        return latestVersions;
    }

    @Override
    public NutsSearchBuilder setLatestVersions(boolean latestVersions) {
        this.latestVersions = latestVersions;
        return this;
    }

    @Override
    public NutsSearchBuilder addIds(String... ids) {
        if (ids != null) {
            for (String id : ids) {
                addId(id);
            }
        }
        return this;
    }

    @Override
    public NutsSearchBuilder addIds(NutsId... ids) {
        if (ids != null) {
            for (NutsId id : ids) {
                addId(id == null ? null : id.toString());
            }
        }
        return this;
    }

    @Override
    public NutsSearchBuilder setIds(String... ids) {
        this.ids.clear();
        addIds(ids);
        return this;
    }

    @Override
    public NutsSearchBuilder addId(String id) {
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
    public NutsDependencyScope getScope() {
        return scope;
    }

    @Override
    public NutsSearchBuilder setScope(NutsDependencyScope scope) {
        this.scope = scope;
        return this;
    }

    //    public NutsSearchBuilder setDependencyFilter(TypedObject filter) {
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
    public NutsSearchBuilder setDependencyFilter(NutsDependencyFilter filter) {
        this.dependencyFilter = filter;
        return this;
    }

    @Override
    public NutsDependencyFilter getDependencyFilter() {
        return dependencyFilter;
    }

    @Override
    public NutsSearchBuilder setDependencyFilter(String filter) {
        this.dependencyFilter = StringUtils.isEmpty(filter) ? null : new NutsDependencyJavascriptFilter(filter);
        return this;
    }

    @Override
    public NutsSearchBuilder setRepositoryFilter(NutsRepositoryFilter filter) {
        this.repositoryFilter = filter;
        return this;
    }

    @Override
    public NutsRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    @Override
    public NutsSearchBuilder setRepositoryFilter(String filter) {
        this.repositoryFilter = StringUtils.isEmpty(filter) ? null : new ExprNutsRepositoryFilter(filter);
        return this;
    }

    @Override
    public NutsSearchBuilder setVersionFilter(NutsVersionFilter filter) {
        this.versionFilter = filter;
        return this;
    }

    @Override
    public NutsVersionFilter getVersionFilter() {
        return versionFilter;
    }

    @Override
    public NutsSearchBuilder setVersionFilter(String filter) {
        this.versionFilter = StringUtils.isEmpty(filter) ? null : new NutsVersionJavascriptFilter(filter);
        return this;
    }

    @Override
    public NutsSearchBuilder setDescriptorFilter(NutsDescriptorFilter filter) {
        this.descriptorFilter = filter;
        return this;
    }

    @Override
    public NutsDescriptorFilter getDescriptorFilter() {
        return descriptorFilter;
    }

    @Override
    public NutsSearchBuilder setDescriptorFilter(String filter) {
        this.descriptorFilter = StringUtils.isEmpty(filter) ? null : new NutsDescriptorJavascriptFilter(filter);
        return this;
    }

    @Override
    public NutsSearchBuilder setIdFilter(NutsIdFilter filter) {
        this.idFilter = filter;
        return this;
    }

    @Override
    public NutsIdFilter getIdFilter() {
        return idFilter;
    }

    @Override
    public NutsSearchBuilder setIdFilter(String filter) {
        this.idFilter = StringUtils.isEmpty(filter) ? null : new NutsJavascriptIdFilter(filter);
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.ids);
        hash = 41 * hash + Objects.hashCode(this.idFilter);
        hash = 41 * hash + Objects.hashCode(this.dependencyFilter);
        hash = 41 * hash + Objects.hashCode(this.repositoryFilter);
        hash = 41 * hash + Objects.hashCode(this.versionFilter);
        hash = 41 * hash + Objects.hashCode(this.descriptorFilter);
        hash = 41 * hash + Objects.hashCode(this.scope);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultNutsSearchBuilder other = (DefaultNutsSearchBuilder) obj;
        if (!Objects.equals(this.ids, other.ids)) {
            return false;
        }
        if (!Objects.equals(this.idFilter, other.idFilter)) {
            return false;
        }
        if (!Objects.equals(this.dependencyFilter, other.dependencyFilter)) {
            return false;
        }
        if (!Objects.equals(this.repositoryFilter, other.repositoryFilter)) {
            return false;
        }
        if (!Objects.equals(this.versionFilter, other.versionFilter)) {
            return false;
        }
        if (!Objects.equals(this.descriptorFilter, other.descriptorFilter)) {
            return false;
        }
        if (this.scope != other.scope) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NutsSearch{");
        sb.append(scope);
        if (ids != null && ids.size() > 0) {
            sb.append(",ids=" + ids);
        }
        if (idFilter != null) {
            sb.append(",idFilter=" + idFilter);
        }
        if (dependencyFilter != null) {
            sb.append(",dependencyFilter=" + dependencyFilter);
        }
        if (repositoryFilter != null) {
            sb.append(",repositoryFilter=" + repositoryFilter);
        }
        if (versionFilter != null) {
            sb.append(",versionFilter=" + versionFilter);
        }
        if (descriptorFilter != null) {
            sb.append(",descriptorFilter=" + descriptorFilter);
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
    public String[] getPackagings() {
        return packagings.toArray(new String[0]);
    }

    @Override
    public String[] getRepos() {
        return repos.toArray(new String[0]);
    }

    @Override
    public NutsSearch build() {


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
        DefaultNutsRepositoryFilter rfilter =null;
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
        for (String v : this.getPackagings()) {
            packs = CoreNutsUtils.simplify(CoreNutsUtils.Or(packs, new NutsDescriptorFilterPackaging(v)));
        }
        NutsDescriptorFilter archs = null;
        for (String v : this.getArch()) {
            archs = CoreNutsUtils.simplify(CoreNutsUtils.Or(archs, new NutsDescriptorFilterArch(v)));
        }

        descriptorFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(descriptorFilter, packs, archs));

        if (this.getRepos().length>0) {
            rfilter = new DefaultNutsRepositoryFilter(new HashSet<>(Arrays.asList(this.getRepos())));
        }


        NutsRepositoryFilter repositoryFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(rfilter,this.getRepositoryFilter()));
        NutsVersionFilter versionFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(null,this.getVersionFilter()));
        descriptorFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(descriptorFilter,this.getDescriptorFilter()));
        idFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(idFilter,idFilter0));

        if (!wildcardIds.isEmpty()) {
            NutsPatternIdFilter ff = new NutsPatternIdFilter(wildcardIds.toArray(new String[0]));
            idFilter = CoreNutsUtils.simplify(new NutsIdFilterOr(idFilter, ff));
        }

        return new DefaultNutsSearch(goodIds.toArray(new String[0]),repositoryFilter, versionFilter, sort, idFilter, latestVersions, descriptorFilter);
    }
}
