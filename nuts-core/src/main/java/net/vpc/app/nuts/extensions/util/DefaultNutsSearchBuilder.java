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

import net.vpc.app.nuts.NutsDependencyFilter;
import net.vpc.app.nuts.NutsDescriptorFilter;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsSearch;
import net.vpc.app.nuts.extensions.filters.dependency.NutsDependencyJavascriptFilter;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorFilterArch;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorFilterPackaging;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorJavascriptFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsJavascriptIdFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsPatternIdFilter;
import net.vpc.app.nuts.extensions.filters.repository.DefaultNutsRepositoryFilter;
import net.vpc.common.strings.StringUtils;

import java.util.*;

import static net.vpc.app.nuts.extensions.util.CoreNutsUtils.And;
import static net.vpc.app.nuts.extensions.util.CoreNutsUtils.simplify;

/**
 *
 * @author vpc
 */
public class DefaultNutsSearchBuilder implements net.vpc.app.nuts.NutsSearchBuilder{

    private final List<String> js = new ArrayList<>();
    private final List<String> ids = new ArrayList<>();
    private final List<String> arch = new ArrayList<>();
    private final List<String> packagings = new ArrayList<>();
    private final List<String> repos = new ArrayList<>();

    @Override
    public DefaultNutsSearchBuilder addJs(Collection<String> value) {
        if (value != null) {
            addJs(value.toArray(new String[value.size()]));
        }
        return this;
    }

    @Override
    public DefaultNutsSearchBuilder addJs(String... value) {
        if (value != null) {
            js.addAll(Arrays.asList(value));
        }
        return this;

    }

    @Override
    public DefaultNutsSearchBuilder addId(Collection<String> value) {
        if (value != null) {
            addId(value.toArray(new String[value.size()]));
        }
        return this;
    }

    @Override
    public DefaultNutsSearchBuilder addId(String... value) {
        if (value != null) {
            ids.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public DefaultNutsSearchBuilder addArch(Collection<String> value) {
        if (value != null) {
            addArch(value.toArray(new String[value.size()]));
        }
        return this;
    }

    @Override
    public DefaultNutsSearchBuilder addArch(String... value) {
        if (value != null) {
            arch.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public DefaultNutsSearchBuilder addPackaging(Collection<String> value) {
        if (value != null) {
            addPackaging(value.toArray(new String[value.size()]));
        }
        return this;
    }

    @Override
    public DefaultNutsSearchBuilder addPackaging(String... value) {
        if (value != null) {
            packagings.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public DefaultNutsSearchBuilder addRepository(Collection<String> value) {
        if (value != null) {
            addRepository(value.toArray(new String[value.size()]));
        }
        return this;
    }

    @Override
    public DefaultNutsSearchBuilder addRepository(String... value) {
        if (value != null) {
            repos.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsSearch build() {
        NutsSearch search = new NutsSearch();

        NutsDescriptorFilter dFilter = null;
        NutsIdFilter idFilter = null;
        NutsDependencyFilter depFilter = null;
        for (String j : js) {
            if (!StringUtils.isEmpty(j)) {
                if (CoreStringUtils.containsTopWord(j, "descriptor")) {
                    dFilter = simplify(And(dFilter, NutsDescriptorJavascriptFilter.valueOf(j)));
                } else if (CoreStringUtils.containsTopWord(j, "dependency")) {
                    depFilter = simplify(And(depFilter, NutsDependencyJavascriptFilter.valueOf(j)));
                } else {
                    idFilter = simplify(And(idFilter, NutsJavascriptIdFilter.valueOf(j)));
                }
            }
        }
        if (!ids.isEmpty()) {
            idFilter = simplify(And(idFilter, new NutsPatternIdFilter(ids.toArray(new String[ids.size()]))));
        }
        NutsDescriptorFilter packs = null;
        for (String v : packagings) {
            packs = CoreNutsUtils.simplify(CoreNutsUtils.Or(packs, new NutsDescriptorFilterPackaging(v)));
        }
        NutsDescriptorFilter archs = null;
        for (String v : arch) {
            archs = CoreNutsUtils.simplify(CoreNutsUtils.Or(archs, new NutsDescriptorFilterArch(v)));
        }

        dFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(dFilter, packs, archs));

//        search.setDependencyFilter(null);
        search.setDescriptorFilter(dFilter);
        search.setIdFilter(idFilter);

        if (!repos.isEmpty()) {
            search.setRepositoryFilter(new DefaultNutsRepositoryFilter(new HashSet<>(repos)));
        }
        return search;
    }
}
