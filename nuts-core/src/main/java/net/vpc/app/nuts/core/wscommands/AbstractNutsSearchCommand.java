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
package net.vpc.app.nuts.core.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsQueryBaseOptions;
import net.vpc.app.nuts.core.DefaultNutsSearch;
import net.vpc.app.nuts.core.DefaultNutsWorkspaceExtensionManager;
import net.vpc.app.nuts.core.NutsPatternIdFilter;
import net.vpc.app.nuts.core.filters.CoreFilterUtils;
import net.vpc.app.nuts.core.filters.dependency.NutsDependencyJavascriptFilter;
import net.vpc.app.nuts.core.filters.descriptor.NutsDescriptorFilterArch;
import net.vpc.app.nuts.core.filters.descriptor.NutsDescriptorFilterPackaging;
import net.vpc.app.nuts.core.filters.descriptor.NutsDescriptorJavascriptFilter;
import net.vpc.app.nuts.core.filters.id.*;
import net.vpc.app.nuts.core.filters.repository.DefaultNutsRepositoryFilter;
import net.vpc.app.nuts.core.filters.repository.ExprNutsRepositoryFilter;
import net.vpc.app.nuts.core.impl.def.wscommands.DefaultNutsFetchCommand;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.util.*;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static net.vpc.app.nuts.core.util.CoreNutsUtils.simplify;

/**
 * @author vpc
 */
public abstract class AbstractNutsSearchCommand extends DefaultNutsQueryBaseOptions<NutsSearchCommand> implements NutsSearchCommand {

    protected Comparator comparator;
    protected NutsDependencyFilter dependencyFilter;
    protected NutsDescriptorFilter descriptorFilter;
    protected NutsIdFilter idFilter;
    protected NutsRepositoryFilter repositoryFilter;
    protected boolean latest = false;
    protected boolean distinct = false;
    protected boolean includeMain = true;
    protected boolean sorted = false;
    protected final List<String> arch = new ArrayList<>();
    protected final List<NutsId> ids = new ArrayList<>();
    protected final List<String> scripts = new ArrayList<>();
    protected final List<String> packaging = new ArrayList<>();
    protected Boolean defaultVersions = null;
    protected String execType = null;
    protected String targetApiVersion = null;

    public AbstractNutsSearchCommand(NutsWorkspace ws) {
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
    public NutsSearchCommand copyFrom(NutsFetchCommand other) {
        super.copyFromDefaultNutsQueryBaseOptions((DefaultNutsQueryBaseOptions) other);
        return this;
    }

    @Override
    public NutsSearchCommand copyFrom(NutsSearchCommand other) {
        super.copyFromDefaultNutsQueryBaseOptions((DefaultNutsQueryBaseOptions) other);
        if (other != null) {
            NutsSearchCommand o = other;
            this.comparator = o.getComparator();
            this.dependencyFilter = o.getDependencyFilter();
            this.descriptorFilter = o.getDescriptorFilter();
            this.idFilter = o.getIdFilter();
            this.latest = o.isLatest();
            this.distinct(o.isDistinct());
            this.includeMain = o.isMain();
            this.sorted = o.isSorted();
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
    public boolean isSorted() {
        return sorted;
    }

    @Override
    public NutsSearchCommand sort() {
        return setSorted(true);
    }

    @Override
    public NutsSearchCommand sort(Comparator comparator) {
        this.comparator = comparator;
        this.sorted = true;
        return this;
    }

    @Override
    public NutsSearchCommand sort(boolean sort) {
        return setSorted(sort);
    }

    @Override
    public NutsSearchCommand setSorted(boolean sort) {
        this.sorted = sort;
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
    public NutsSearchCommand libs() {
        return libs(true);
    }

    @Override
    public NutsSearchCommand libs(boolean enable) {
        return setLibs(enable);
    }

    @Override
    public NutsSearchCommand setLibs(boolean enable) {
        this.execType = enable ? "libs" : null;
        return this;
    }

    @Override
    public NutsSearchCommand apps() {
        return apps(true);
    }

    @Override
    public NutsSearchCommand apps(boolean enable) {
        return setApps(enable);
    }

    @Override
    public NutsSearchCommand setApps(boolean enable) {
        this.execType = enable ? "apps" : null;
        return this;
    }


    @Override
    public NutsSearchCommand extensions() {
        return extensions(true);
    }

    @Override
    public NutsSearchCommand extensions(boolean enable) {
        return setExtensions(enable);
    }

    @Override
    public NutsSearchCommand setExtensions(boolean enable) {
        this.execType = enable ? "extensions" : null;
        return this;
    }

    @Override
    public NutsSearchCommand companions() {
        return companions(true);
    }

    @Override
    public NutsSearchCommand companions(boolean enable) {
        return setCompanions(enable);
    }

    @Override
    public NutsSearchCommand setCompanions(boolean enable) {
        this.execType = enable ? "companions" : null;
        return this;
    }

    @Override
    public NutsSearchCommand runtime() {
        return runtime(true);
    }

    @Override
    public NutsSearchCommand runtime(boolean enable) {
        return setRuntime(enable);
    }

    @Override
    public NutsSearchCommand setRuntime(boolean enable) {
        this.execType = enable ? "runtime" : null;
        return this;
    }

    @Override
    public NutsSearchCommand nutsApps() {
        return nutsApps(true);
    }

    @Override
    public NutsSearchCommand nutsApps(boolean enable) {
        return setNutsApps(enable);
    }

    @Override
    public NutsSearchCommand setNutsApps(boolean enable) {
        this.execType = enable ? "nuts-apps" : null;
        return this;
    }

    @Override
    public boolean isRuntime() {
        return "runtime".equals(execType);
    }

    @Override
    public boolean isCompanions() {
        return "companions".equals(execType);
    }

    @Override
    public boolean isExtensions() {
        return "extensions".equals(execType);
    }

    @Override
    public boolean isApps() {
        return "apps".equals(execType);
    }

    @Override
    public boolean isNutsApps() {
        return "nuts-apps".equals(execType);
    }

    @Override
    public boolean isLibs() {
        return "libs".equals(execType);
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
                case "libs": {
                    _descriptorFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(_descriptorFilter, new NutsExecStatusIdFilter(false, false)));
                    break;
                }
                case "apps": {
                    _descriptorFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(_descriptorFilter, new NutsExecStatusIdFilter(true, null)));
                    break;
                }
                case "nuts-apps": {
                    _descriptorFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(_descriptorFilter, new NutsExecStatusIdFilter(null, true)));
                    break;
                }
                case "extensions": {
                    _descriptorFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(_descriptorFilter, new NutsExecExtensionFilter(
                            targetApiVersion==null?null:ws.id().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build()))
                    );
                    break;
                }
                case "runtime": {
                    _descriptorFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(_descriptorFilter, new NutsExecRuntimeFilter(
                            targetApiVersion==null?null:ws.id().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build()
                            ,false
                            ))
                    );
                    break;
                }
                case "companions": {
                    _descriptorFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(_descriptorFilter, new NutsExecCompanionFilter(
                            targetApiVersion==null?null:ws.id().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build(),
                            NutsWorkspaceExt.of(ws).getCompanionIds()
                            ))
                    );
                    break;
                }
            }
        }else{
            if(targetApiVersion!=null) {
                _descriptorFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(_descriptorFilter, new BootAPINutsDescriptorFilter(
                                ws.id().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build().getVersion()
                        ))
                );
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
    public boolean isDistinct() {
        return distinct;
    }

    @Override
    public NutsSearchCommand distinct() {
        return distinct(true);
    }

    @Override
    public NutsSearchCommand distinct(boolean distinct) {
        return setDistinct(distinct);
    }

    @Override
    public NutsSearchCommand setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    @Override
    public Comparator getComparator() {
        return comparator;
    }

    @Override
    public String getTargetApiVersion() {
        return targetApiVersion;
    }

    @Override
    public NutsSearchCommand setTargetApiVersion(String targetApiVersion) {
        this.targetApiVersion = targetApiVersion;
        return this;
    }

    @Override
    public NutsSearchCommand targetApiVersion(String targetApiVersion) {
        return setTargetApiVersion(targetApiVersion);
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
            case "--distinct": {
                this.distinct(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--default":
            case "--default-versions": {
                this.defaultVersions(cmdLine.nextBoolean().getBoolean(null));
                return true;
            }
            case "--duplicates": {
                this.distinct(!cmdLine.nextBoolean().getBooleanValue());
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
            case "--libs": {
                this.libs(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--apps": {
                this.apps(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--companions": {
                this.companions(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--extensions": {
                this.extensions(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--runtime": {
                this.runtime(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--api-version": {
                this.targetApiVersion(cmdLine.nextBoolean().getStringValue());
                return true;
            }
            case "--nuts-apps": {
                this.nutsApps(cmdLine.nextBoolean().getBooleanValue());
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
