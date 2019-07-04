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
import net.vpc.app.nuts.core.filters.id.NutsIdFilterOr;
import net.vpc.app.nuts.core.filters.id.NutsJavascriptIdFilter;
import net.vpc.app.nuts.core.filters.repository.DefaultNutsRepositoryFilter;
import net.vpc.app.nuts.core.filters.repository.ExprNutsRepositoryFilter;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

import java.io.File;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.vpc.app.nuts.core.filters.CoreFilterUtils;
import net.vpc.app.nuts.core.filters.id.NutsDefaultVersionIdFilter;
import net.vpc.app.nuts.core.filters.id.NutsExecStatusIdFilter;
import net.vpc.app.nuts.core.format.FormattableNutsId;
import net.vpc.app.nuts.core.format.NutsDisplayProperty;
import net.vpc.app.nuts.core.format.NutsFetchDisplayOptions;

import static net.vpc.app.nuts.core.util.CoreNutsUtils.simplify;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.iter.IteratorBuilder;
import net.vpc.app.nuts.core.util.iter.IteratorUtils;

/**
 * @author vpc
 */
public class DefaultNutsSearchCommand extends DefaultNutsQueryBaseOptions<NutsSearchCommand> implements NutsSearchCommand {

    private Comparator<NutsId> idComparator;
    private NutsDependencyFilter dependencyFilter;
    private NutsDescriptorFilter descriptorFilter;
    private NutsIdFilter idFilter;
    private NutsRepositoryFilter repositoryFilter;
    private boolean latest = false;
    private boolean includeDuplicatedVersions = true;
    private boolean includeMain = true;
    private boolean sort = false;
    private final List<String> arch = new ArrayList<>();
    private final List<NutsId> ids = new ArrayList<>();
    private final List<String> scripts = new ArrayList<>();
    private final List<String> packaging = new ArrayList<>();
    private Boolean defaultVersions = null;
    private String execType = null;

    public DefaultNutsSearchCommand(NutsWorkspace ws) {
        super(ws, "search");
    }

    /**
     *
     * @since 0.5.5
     * @return
     */
    @Override
    public Boolean getDefaultVersions() {
        return defaultVersions;
    }

    /**
     *
     * @since 0.5.5
     * @return
     */
    @Override
    public NutsSearchCommand defaultVersions() {
        return defaultVersions(true);
    }

    /**
     *
     * @since 0.5.5
     * @param acceptDefaultVersion
     * @return
     */
    @Override
    public NutsSearchCommand defaultVersions(Boolean acceptDefaultVersion) {
        return setDefaultVersions(acceptDefaultVersion);
    }

    /**
     *
     * @since 0.5.5
     * @param acceptDefaultVersion
     * @return
     */
    @Override
    public NutsSearchCommand setDefaultVersions(Boolean acceptDefaultVersion) {
        this.defaultVersions = acceptDefaultVersion;
        return this;
    }

    @Override
    public NutsSearchCommand clearScripts() {
        scripts.clear();
        return this;
    }

    @Override
    public NutsSearchCommand scripts(Collection<String> value) {
        return addScripts(value);
    }

    @Override
    public NutsSearchCommand scripts(String... value) {
        return addScripts(value);
    }

    @Override
    public NutsSearchCommand addScripts(Collection<String> value) {
        if (value != null) {
            addScripts(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsSearchCommand removeScript(String value) {
        scripts.remove(value);
        return this;
    }

    @Override
    public NutsSearchCommand script(String value) {
        return addScript(value);
    }

    @Override
    public NutsSearchCommand addScript(String value) {
        if (value != null) {
            scripts.add(value);
        }
        return this;
    }

    @Override
    public NutsSearchCommand addScripts(String... value) {
        if (value != null) {
            scripts.addAll(Arrays.asList(value));
        }
        return this;

    }

    @Override
    public NutsSearchCommand ids(String... values) {
        return addIds(values);
    }

    @Override
    public NutsSearchCommand addIds(String... values) {
        if (values != null) {
            for (String s : values) {
                if (!CoreStringUtils.isBlank(s)) {
                    ids.add(ws.id().parseRequired(s));
                }
            }
        }
        return this;
    }

    @Override
    public NutsSearchCommand ids(NutsId... values) {
        return addIds(values);
    }

    @Override
    public NutsSearchCommand addIds(NutsId... value) {
        if (value != null) {
            for (NutsId s : value) {
                if (s != null) {
                    ids.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NutsSearchCommand clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public NutsSearchCommand addArch(String value) {
        if (!CoreStringUtils.isBlank(value)) {
            this.arch.add(value);
        }
        return this;
    }

    @Override
    public NutsSearchCommand removeArch(String value) {
        this.arch.remove(value);
        return this;
    }

    @Override
    public NutsSearchCommand arch(String value) {
        return addArch(value);
    }

    @Override
    public NutsSearchCommand archs(Collection<String> value) {
        return addArchs(value);
    }

    @Override
    public NutsSearchCommand clearArchs() {
        this.arch.clear();
        return this;
    }

    @Override
    public NutsSearchCommand addArchs(Collection<String> value) {
        if (value != null) {
            addArchs(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsSearchCommand archs(String... value) {
        return addArchs(arch);
    }

    @Override
    public NutsSearchCommand addArchs(String... value) {
        if (value != null) {
            arch.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsSearchCommand packaging(String value) {
        return addPackaging(value);
    }

    @Override
    public NutsSearchCommand addPackaging(String value) {
        if (value != null) {
            packaging.add(value);
        }
        return this;
    }

    @Override
    public NutsSearchCommand removePackaging(String value) {
        packaging.remove(value);
        return this;
    }

    @Override
    public NutsSearchCommand clearPackagings() {
        packaging.clear();
        return this;
    }

    @Override
    public NutsSearchCommand addPackagings(Collection<String> value) {
        if (value != null) {
            addPackagings(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsSearchCommand packagings(Collection<String> value) {
        return addPackagings(value);
    }

    @Override
    public NutsSearchCommand packagings(String... value) {
        return addPackagings(value);
    }

    @Override
    public NutsSearchCommand addPackagings(String... value) {
        if (value != null) {
            this.packaging.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsSearchCommand copy() {
        DefaultNutsSearchCommand b = new DefaultNutsSearchCommand(ws);
        b.copyFrom(this);
        return b;
    }

    @Override
    public NutsSearchCommand copyFrom(NutsFetchCommand other) {
        super.copyFromDefaultNutsQueryBaseOptions((DefaultNutsQueryBaseOptions) other);
        return this;
    }

    @Override
    public NutsSearchCommand copyFrom(NutsSearchCommand other) {
        super.copyFromDefaultNutsQueryBaseOptions((DefaultNutsQueryBaseOptions) other);
        if (other != null) {
            NutsSearchCommand o = other;
            this.idComparator = o.getSortIdComparator();
            this.dependencyFilter = o.getDependencyFilter();
            this.descriptorFilter = o.getDescriptorFilter();
            this.idFilter = o.getIdFilter();
            this.latest = o.isLatest();
            this.includeDuplicatedVersions = o.isDuplicates();
            this.includeMain = o.isMain();
            this.sort = o.isSort();
            this.arch.clear();
            this.arch.addAll(Arrays.asList(o.getArch()));
            this.ids.clear();
            this.ids.addAll(Arrays.asList(o.getIds()));
            this.scripts.clear();
            this.scripts.addAll(Arrays.asList(o.getScripts()));
            this.packaging.clear();
            this.packaging.addAll(Arrays.asList(o.getPackaging()));
            this.repositoryFilter = o.getRepositoryFilter();
        }
        return this;
    }

    @Override
    public boolean isSort() {
        return sort;
    }

    @Override
    public NutsSearchCommand sort() {
        return setSort(true);
    }

    @Override
    public NutsSearchCommand sort(Comparator<NutsId> comparator) {
        this.idComparator = comparator;
        this.sort = true;
        return this;
    }

    @Override
    public NutsSearchCommand sort(boolean sort) {
        return setSort(sort);
    }

    @Override
    public NutsSearchCommand setSort(boolean sort) {
        this.sort = sort;
        return this;
    }

    @Override
    public boolean isLatest() {
        return latest;
    }

    @Override
    public NutsSearchCommand latest() {
        return latest(true);
    }

    @Override
    public NutsSearchCommand latest(boolean enable) {
        return setLatest(enable);
    }

    @Override
    public NutsSearchCommand setLatest(boolean enable) {
        this.latest = enable;
        return this;
    }

    @Override
    public NutsSearchCommand lib() {
        return lib(true);
    }

    @Override
    public NutsSearchCommand lib(boolean enable) {
        return setLib(enable);
    }

    @Override
    public NutsSearchCommand setLib(boolean enable) {
        this.execType = enable ? "lib" : null;
        return this;
    }

    @Override
    public NutsSearchCommand app() {
        return app(true);
    }

    @Override
    public NutsSearchCommand app(boolean enable) {
        return setApp(enable);
    }

    @Override
    public NutsSearchCommand setApp(boolean enable) {
        this.execType = enable ? "app" : null;
        return this;
    }

    @Override
    public NutsSearchCommand nutsApp() {
        return nutsApp(true);
    }

    @Override
    public NutsSearchCommand nutsApp(boolean enable) {
        return setApp(enable);
    }

    @Override
    public NutsSearchCommand setNutsApp(boolean enable) {
        this.execType = enable ? "nuts-app" : null;
        return this;
    }

    @Override
    public boolean isApp() {
        return "app".equals(execType);
    }

    @Override
    public boolean isNutsApp() {
        return "nuts-app".equals(execType);
    }

    @Override
    public boolean isLib() {
        return "lib".equals(execType);
    }

    @Override
    public NutsSearchCommand id(NutsId id) {
        return addId(id);
    }

    @Override
    public NutsSearchCommand addId(NutsId id) {
        if (id != null) {
            addId(id.toString());
        }
        return this;
    }

    @Override
    public NutsSearchCommand removeId(NutsId id) {
        if (id != null) {
            removeId(id.toString());
        }
        return this;
    }

    @Override
    public NutsSearchCommand id(String id) {
        return addId(id);
    }

    @Override
    public NutsSearchCommand removeId(String id) {
        ids.remove(ws.id().parse(id));
        return this;
    }

    @Override
    public NutsSearchCommand addId(String id) {
        if (!CoreStringUtils.isBlank(id)) {
            ids.add(ws.id().parseRequired(id));
        }
        return this;
    }

    @Override
    public NutsId[] getIds() {
        return this.ids.toArray(new NutsId[0]);
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
    public NutsSearchCommand setDependencyFilter(NutsDependencyFilter filter) {
        this.dependencyFilter = filter;
        return this;
    }

    @Override
    public NutsDependencyFilter getDependencyFilter() {
        return dependencyFilter;
    }

    @Override
    public NutsSearchCommand setDependencyFilter(String filter) {
        this.dependencyFilter = CoreStringUtils.isBlank(filter) ? null : new NutsDependencyJavascriptFilter(filter);
        return this;
    }

    @Override
    public NutsSearchCommand setRepositoryFilter(NutsRepositoryFilter filter) {
        this.repositoryFilter = filter;
        return this;
    }

    @Override
    public NutsRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    @Override
    public NutsSearchCommand setRepository(String filter) {
        this.repositoryFilter = CoreStringUtils.isBlank(filter) ? null : new ExprNutsRepositoryFilter(filter);
        return this;
    }

    @Override
    public NutsSearchCommand setDescriptorFilter(NutsDescriptorFilter filter) {
        this.descriptorFilter = filter;
        return this;
    }

    @Override
    public NutsDescriptorFilter getDescriptorFilter() {
        return descriptorFilter;
    }

    @Override
    public NutsSearchCommand setDescriptorFilter(String filter) {
        this.descriptorFilter = CoreStringUtils.isBlank(filter) ? null : new NutsDescriptorJavascriptFilter(filter);
        return this;
    }

    @Override
    public NutsSearchCommand setIdFilter(NutsIdFilter filter) {
        this.idFilter = filter;
        return this;
    }

    @Override
    public NutsIdFilter getIdFilter() {
        return idFilter;
    }

    @Override
    public NutsSearchCommand setIdFilter(String filter) {
        this.idFilter = CoreStringUtils.isBlank(filter) ? null : new NutsJavascriptIdFilter(filter);
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
        if (descriptorFilter != null) {
            sb.append(",descriptorFilter=").append(descriptorFilter);
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String[] getScripts() {
        return scripts.toArray(new String[0]);
    }

    @Override
    public String[] getArch() {
        return arch.toArray(new String[0]);
    }

    @Override
    public String[] getPackaging() {
        return this.packaging.toArray(new String[0]);
    }

    //@Override
    private DefaultNutsSearch build() {
        HashSet<String> someIds = new HashSet<>();
        for (NutsId id : this.getIds()) {
            someIds.add(id.toString());
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
                    _descriptorFilter = simplify(CoreFilterUtils.And(_descriptorFilter, NutsDescriptorJavascriptFilter.valueOf(j)));
                } else if (CoreStringUtils.containsTopWord(j, "dependency")) {
                    depFilter = simplify(CoreFilterUtils.And(depFilter, NutsDependencyJavascriptFilter.valueOf(j)));
                } else {
                    _idFilter = simplify(CoreFilterUtils.And(_idFilter, NutsJavascriptIdFilter.valueOf(j)));
                }
            }
        }
        NutsDescriptorFilter packs = null;
        for (String v : this.getPackaging()) {
            packs = CoreNutsUtils.simplify(CoreFilterUtils.Or(packs, new NutsDescriptorFilterPackaging(v)));
        }
        NutsDescriptorFilter archs = null;
        for (String v : this.getArch()) {
            archs = CoreNutsUtils.simplify(CoreFilterUtils.Or(archs, new NutsDescriptorFilterArch(v)));
        }

        _descriptorFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(_descriptorFilter, packs, archs));

        if (this.getRepositories().length > 0) {
            rfilter = new DefaultNutsRepositoryFilter(Arrays.asList(this.getRepositories())).simplify();
        }

        NutsRepositoryFilter _repositoryFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(rfilter, this.getRepositoryFilter()));
        _descriptorFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(_descriptorFilter, this.getDescriptorFilter()));
        _idFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(_idFilter, idFilter0));
        if (getDefaultVersions() != null) {
            _idFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(_idFilter, new NutsDefaultVersionIdFilter(getDefaultVersions())));
        }
        if (execType != null) {
            switch (execType) {
                case "lib": {
                    _descriptorFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(_descriptorFilter, new NutsExecStatusIdFilter(false, false)));
                    break;
                }
                case "app": {
                    _descriptorFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(_descriptorFilter, new NutsExecStatusIdFilter(true, null)));
                    break;
                }
                case "nuts-app": {
                    _descriptorFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(_descriptorFilter, new NutsExecStatusIdFilter(null, true)));
                    break;
                }
            }
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
        return getResultIdsBase(getValidSession().isTrace(), sort);
    }

    private NutsSearchResult<NutsDefinition> getResultDefinitionsBase(boolean trace, boolean sort, boolean content, boolean install, boolean effective) {
        return new NutsDefinitionNutsSearchResult(ws, resolveFindIdBase(), trace, sort, content, install, effective);
    }

    @Override
    public NutsSearchResult<NutsDefinition> getResultDefinitions() {
        return getResultDefinitionsBase(getValidSession().isTrace(), sort, isContent(), isInstallInformation(), isEffective());
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
        if (!isLatest() && includeDuplicatedVersions) {
            return new NutsCollectionSearchResult<NutsId>(ws, resolveFindIdBase(), applyTraceDecoratorIterOfNutsId(curr, trace));
            //nothind
        } else if (!isLatest() && !includeDuplicatedVersions) {
            return new NutsCollectionSearchResult<NutsId>(ws, resolveFindIdBase(),
                    applyTraceDecoratorIterOfNutsId(IteratorBuilder.of(curr).unique(new Function<NutsId, String>() {
                        @Override
                        public String apply(NutsId nutsId) {
                            return nutsId.getLongNameId().setAlternative(nutsId.getAlternative()).toString();
                        }
                    }).iterator(), trace));
        } else if (isLatest() && !includeDuplicatedVersions) {
            Map<String, NutsId> visited = new LinkedHashMap<>();
            while (curr.hasNext()) {
                NutsId nutsId = curr.next();
                String k = nutsId.getShortNameId().setAlternative(nutsId.getAlternative()).toString();
                NutsId old = visited.get(k);
                if (old == null || old.getVersion().isBlank() || old.getVersion().compareTo(nutsId.getVersion()) < 0) {
                    visited.put(k, nutsId);
                }
            }
            return new NutsCollectionSearchResult<NutsId>(ws, resolveFindIdBase(), applyTraceDecoratorCollectionOfNutsId(visited.values(), trace));
        } else if (isLatest() && includeDuplicatedVersions) {
            Map<String, List<NutsId>> visited = new LinkedHashMap<>();
            while (curr.hasNext()) {
                NutsId nutsId = curr.next();
                String k = nutsId.getShortNameId().setAlternative(nutsId.getAlternative()).toString();
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
            return new NutsCollectionSearchResult<NutsId>(ws, resolveFindIdBase(), applyTraceDecoratorListOfNutsId(list, trace));
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
        if (!isLatest() && includeDuplicatedVersions && !sort && !isInlineDependencies()) {
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
            listToSort.sort(idComparator == null ? DefaultNutsIdComparator.INSTANCE : idComparator);
            return new NutsCollectionSearchResult<NutsId>(ws, resolveFindIdBase(), applyTraceDecoratorListOfNutsId(listToSort, trace));
        } else {
            return applyVersionFlagFilters(curr, trace);
        }
    }

    private NutsId[] findDependencies(List<NutsId> ids) {
        NutsSession _session = this.getSession() == null ? ws.createSession() : this.getSession();
        NutsDependencyFilter _dependencyFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(
                new NutsDependencyScopeFilter().addScopes(getScope()),
                getOptional() == null ? null : NutsDependencyOptionFilter.valueOf(getOptional()),
                getDependencyFilter()
        ));
        NutsIdGraph graph = new NutsIdGraph(_session, isFailFast());
        graph.push(ids, _dependencyFilter);
        return graph.collect(ids, ids);
    }

    @Override
    public String getResultNutsPath() {
        StringBuilder sb = new StringBuilder();
        for (NutsId nutsDefinition : getResultIds()) {
            if (nutsDefinition != null) {
                if (sb.length() > 0) {
                    sb.append(";");
                }
                sb.append(nutsDefinition.getLongNameId().toString());
            }
        }
        return sb.toString();
    }

    private <T> NutsSearchResult<T> postProcessResult(IteratorBuilder<T> a) {
        if (isSort()) {
            a = a.sort(null, !isDuplicates());
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

    @Override
    public boolean isDuplicates() {
        return includeDuplicatedVersions;
    }

    @Override
    public NutsSearchCommand duplicates() {
        return duplicates(true);
    }

    @Override
    public NutsSearchCommand duplicates(boolean includeDuplicateVersions) {
        return setDuplicateVersions(includeDuplicateVersions);
    }

    @Override
    public NutsSearchCommand setDuplicateVersions(boolean includeDuplicateVersion) {
        this.includeDuplicatedVersions = includeDuplicateVersion;
        return this;
    }

    @Override
    public Comparator<NutsId> getSortIdComparator() {
        return idComparator;
    }

    @Override
    public boolean isMain() {
        return includeMain;
    }

    @Override
    public NutsSearchCommand setMain(boolean includeMain) {
        this.includeMain = includeMain;
        return this;
    }

    @Override
    public NutsSearchCommand main(boolean includeMain) {
        return setMain(includeMain);
    }

    @Override
    public NutsSearchCommand main() {
        return main(true);
    }

    private class NutsDefinitionNutsSearchResult extends AbstractNutsSearchResult<NutsDefinition> {

        private boolean trace;
        private boolean sort;
        private boolean installInformation;
        private boolean content;
        private boolean effective;

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
            NutsFetchCommand fetch = toFetch();
            fetch.content(content);
            fetch.effective(effective);
            fetch.installInformation(installInformation);
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
            NutsSession s = ws.createSession();
            NutsFetchCommand fetch = toFetch();
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
        NutsIdFilter idFilter = search.getIdFilter();
        NutsRepositoryFilter repositoryFilter = search.getRepositoryFilter();
        NutsDescriptorFilter descriptorFilter = search.getDescriptorFilter();
        String[] regularIds = search.getRegularIds();
        NutsFetchStrategy fetchMode = NutsWorkspaceHelper.validate(search.getOptions().getFetchStrategy());
        if (regularIds.length > 0) {
            for (String id : regularIds) {
                NutsId nutsId = ws.id().parse(id);
                if (nutsId != null) {
                    List<NutsId> nutsId2 = new ArrayList<>();
                    if (CoreStringUtils.isBlank(nutsId.getGroup())) {
                        if (nutsId.getName().equals("nuts")) {
                            if (nutsId.getVersion().isBlank() || nutsId.getVersion().ge("0.5")) {
                                nutsId2.add(nutsId.setGroup("net.vpc.app.nuts"));
                            } else {
                                //older versions
                                nutsId2.add(nutsId.setGroup("net.vpc.app"));
                            }
                        } else {
                            for (String aImport : ws.config().getImports()) {
                                nutsId2.add(nutsId.setGroup(aImport));
                            }
                        }
                    } else {
                        nutsId2.add(nutsId);
                    }
                    List<Iterator<NutsId>> coalesce = new ArrayList<>();
                    for (NutsFetchMode mode : fetchMode) {
                        List<Iterator<NutsId>> all = new ArrayList<>();
                        for (NutsId nutsId1 : nutsId2) {
                            NutsIdFilter idFilter2 = CoreNutsUtils.simplify(CoreFilterUtils.And(idFilter, nutsId1.toFilter()));
                            if (mode == NutsFetchMode.INSTALLED) {
                                all.add(
                                        IteratorBuilder.ofLazy(new Iterable<NutsId>() {
                                            @Override
                                            public Iterator<NutsId> iterator() {
                                                NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(nutsId1.getQueryMap(), idFilter2, descriptorFilter));
                                                NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(getValidSession(), null, NutsFetchMode.INSTALLED, new DefaultNutsFetchCommand(ws));
                                                return NutsWorkspaceExt.of(ws)
                                                        .getInstalledRepository().findVersions(nutsId1, filter, rsession);
                                            }
                                        }).safeIgnore().iterator());
                            } else {
                                for (NutsRepository repo : NutsWorkspaceUtils.filterRepositories(ws, NutsRepositorySupportedAction.SEARCH, nutsId1, repositoryFilter, mode, search.getOptions())) {
                                    if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                                        NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(nutsId1.getQueryMap(), idFilter2, descriptorFilter));
                                        all.add(
                                                IteratorBuilder.ofLazy(new Iterable<NutsId>() {
                                                    @Override
                                                    public Iterator<NutsId> iterator() {
                                                        return repo.searchVersions().id(nutsId1).filter(filter).session(NutsWorkspaceHelper.createRepositorySession(session, repo, mode, search.getOptions()))
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
                    if (nutsId.getGroup() == null) {
                        //now will look with *:artifactId pattern
                        NutsSearchCommand search2 = ws.search()
                                .copyFrom(search.getOptions())
                                .setRepositoryFilter(search.getRepositoryFilter())
                                .setDescriptorFilter(search.getDescriptorFilter())
                                .setFetchStratery(search.getOptions().getFetchStrategy())
                                .setSession(session);
                        search2.setIdFilter(new NutsIdFilterOr(
                                new NutsPatternIdFilter(nutsId.setGroup("*")),
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
                    NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(null, idFilter, descriptorFilter));
                    coalesce.add(NutsWorkspaceExt.of(ws).getInstalledRepository().findAll(filter, rsession));
                } else {
                    List<Iterator<NutsId>> all = new ArrayList<>();
                    for (NutsRepository repo : NutsWorkspaceUtils.filterRepositories(ws, NutsRepositorySupportedAction.SEARCH, null, repositoryFilter, mode, search.getOptions())) {
                        if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, repo, mode, search.getOptions());
                            NutsIdFilter filter = CoreNutsUtils.simplify(CoreFilterUtils.idFilterOf(null, idFilter, descriptorFilter));
                            all.add(
                                    IteratorBuilder.ofLazy(new Iterable<NutsId>() {
                                        @Override
                                        public Iterator<NutsId> iterator() {
                                            return repo.search().filter(filter).session(rsession).run().getResult();
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
            r = getResultDefinitionsBase(getValidSession().isTrace(), isSort(), _content, _installInformation, _effective);
        }
        for (Object any : r) {
            //just iterator over
        }
        return this;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "--inline-dependencies": {
                this.inlineDependencies(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "-L":
            case "--latest":
            case "--latest-versions": {
                cmdLine.skip();
                this.latest();
                return true;
            }
            case "--single": {
                this.duplicates(!cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--default":
            case "--default-versions": {
                this.defaultVersions(cmdLine.nextBoolean().getBoolean(null));
                return true;
            }
            case "--duplicates": {
                this.duplicates(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "-s":
            case "--sort": {
                this.sort(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--main": {
                this.includeMain = cmdLine.nextBoolean().getBooleanValue();
                return true;
            }
            case "--lib": {
                this.lib(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--app": {
                this.app(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--nuts-app": {
                this.nutsApp(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--arch": {
                this.addArch(cmdLine.nextString().getStringValue());
                return true;
            }
            case "--packaging": {
                this.addPackaging(cmdLine.nextString().getStringValue());
                return true;
            }
            case "--optional": {
                NutsArgument s = cmdLine.nextString();
                this.setOptional(CoreCommonUtils.parseBoolean(s.getStringValue(), null));
                return true;
            }
            case "--script": {
                this.addScript(cmdLine.nextString().getStringValue());
                return true;
            }
            case "--id": {
                this.addId(cmdLine.nextString().getStringValue());
                return true;
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                if (a.isOption()) {
                    return false;
                } else {
                    cmdLine.skip();
                    id(a.getString());
                    return true;
                }
            }
        }
    }

    @Override
    public NutsSearchCommand dependencyFilter(NutsDependencyFilter filter) {
        return setDependencyFilter(filter);
    }

    @Override
    public NutsSearchCommand dependencyFilter(String filter) {
        return setDependencyFilter(filter);
    }

    @Override
    public NutsSearchCommand repositoryFilter(NutsRepositoryFilter filter) {
        return setRepositoryFilter(filter);
    }

    @Override
    public NutsSearchCommand descriptorFilter(NutsDescriptorFilter filter) {
        return setDescriptorFilter(filter);
    }

    @Override
    public NutsSearchCommand descriptorFilter(String filter) {
        return setDescriptorFilter(filter);
    }

    @Override
    public NutsSearchCommand idFilter(NutsIdFilter filter) {
        return setIdFilter(filter);
    }

    @Override
    public NutsSearchCommand idFilter(String filter) {
        return setIdFilter(filter);
    }

}
