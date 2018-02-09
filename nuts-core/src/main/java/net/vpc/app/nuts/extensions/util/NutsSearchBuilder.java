/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import net.vpc.app.nuts.NutsDependencyFilter;
import net.vpc.app.nuts.NutsDescriptorFilter;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsSearch;
import net.vpc.app.nuts.extensions.filters.repository.DefaultNutsRepositoryFilter;
import net.vpc.app.nuts.extensions.filters.dependency.NutsDependencyJavascriptFilter;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorFilterArch;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorFilterPackaging;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorJavascriptFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsIdJavascriptFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsIdPatternFilter;
import static net.vpc.app.nuts.extensions.util.CoreNutsUtils.And;
import static net.vpc.app.nuts.extensions.util.CoreNutsUtils.simplify;

/**
 *
 * @author vpc
 */
public class NutsSearchBuilder {

    private final List<String> js = new ArrayList<>();
    private final List<String> ids = new ArrayList<>();
    private final List<String> arch = new ArrayList<>();
    private final List<String> packagings = new ArrayList<>();
    private final List<String> repos = new ArrayList<>();

    public NutsSearchBuilder addJs(Collection<String> value) {
        if (value != null) {
            addJs(value.toArray(new String[value.size()]));
        }
        return this;
    }

    public NutsSearchBuilder addJs(String... value) {
        if (value != null) {
            js.addAll(Arrays.asList(value));
        }
        return this;

    }

    public NutsSearchBuilder addId(Collection<String> value) {
        if (value != null) {
            addId(value.toArray(new String[value.size()]));
        }
        return this;
    }

    public NutsSearchBuilder addId(String... value) {
        if (value != null) {
            ids.addAll(Arrays.asList(value));
        }
        return this;
    }

    public NutsSearchBuilder addArch(Collection<String> value) {
        if (value != null) {
            addArch(value.toArray(new String[value.size()]));
        }
        return this;
    }

    public NutsSearchBuilder addArch(String... value) {
        if (value != null) {
            arch.addAll(Arrays.asList(value));
        }
        return this;
    }

    public NutsSearchBuilder addPackaging(Collection<String> value) {
        if (value != null) {
            addPackaging(value.toArray(new String[value.size()]));
        }
        return this;
    }

    public NutsSearchBuilder addPackaging(String... value) {
        if (value != null) {
            packagings.addAll(Arrays.asList(value));
        }
        return this;
    }

    public NutsSearchBuilder addRepository(Collection<String> value) {
        if (value != null) {
            addRepository(value.toArray(new String[value.size()]));
        }
        return this;
    }

    public NutsSearchBuilder addRepository(String... value) {
        if (value != null) {
            repos.addAll(Arrays.asList(value));
        }
        return this;
    }

    public NutsSearch build() {
        NutsSearch search = new NutsSearch();

        NutsDescriptorFilter dFilter = null;
        NutsIdFilter idFilter = null;
        NutsDependencyFilter depFilter = null;
        for (String j : js) {
            if (!CoreStringUtils.isEmpty(j)) {
                if (CoreStringUtils.containsTopWord(j, "descriptor")) {
                    dFilter = simplify(And(dFilter, NutsDescriptorJavascriptFilter.valueOf(j)));
                } else if (CoreStringUtils.containsTopWord(j, "dependency")) {
                    depFilter = simplify(And(depFilter, NutsDependencyJavascriptFilter.valueOf(j)));
                } else {
                    idFilter = simplify(And(idFilter, NutsIdJavascriptFilter.valueOf(j)));
                }
            }
        }
        if (!ids.isEmpty()) {
            idFilter = simplify(And(idFilter, new NutsIdPatternFilter(ids.toArray(new String[ids.size()]))));
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
