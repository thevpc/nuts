/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.search;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.lib.common.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNClassLoader;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.DefaultNQueryBaseOptions;
import net.thevpc.nuts.runtime.standalone.format.NDisplayProperty;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.NIdFormatHelper;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNExtensions;
import net.thevpc.nuts.runtime.standalone.dependency.util.NClassLoaderUtils;
import net.thevpc.nuts.runtime.standalone.util.stream.NStreamFromNIterator;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;

import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public abstract class AbstractNSearchCmd extends DefaultNQueryBaseOptions<NSearchCmd> implements NSearchCmd {

    protected final List<String> arch = new ArrayList<>();
    protected final List<NId> ids = new ArrayList<>();
    protected final List<NId> lockedIds = new ArrayList<>();
    protected final List<String> scripts = new ArrayList<>();
    protected final List<String> packaging = new ArrayList<>();
    protected NComparator comparator;
    protected NDescriptorFilter descriptorFilter;
    protected NIdFilter idFilter;
    protected boolean latest = false;
    protected boolean distinct = false;
    protected boolean includeBasePackage = true;
    protected boolean sorted = false;
    protected Boolean defaultVersions = null;
    protected String execType = null;
    protected NVersion targetApiVersion = null;
    protected NInstallStatusFilter installStatus;

    public AbstractNSearchCmd(NWorkspace workspace) {
        super(workspace, "search");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NSearchCmd clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public NSearchCmd addId(String id) {
        NSession session=getWorkspace().currentSession();
        if (!NBlankable.isBlank(id)) {
            ids.add(NId.of(id).get());
        }
        return this;
    }

    @Override
    public NSearchCmd addId(NId id) {
        if (id != null) {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NSearchCmd addIds(String... values) {
        NSession session=getWorkspace().currentSession();
        if (values != null) {
            for (String s : values) {
                if (!NBlankable.isBlank(s)) {
                    ids.add(NId.of(s).get());
                }
            }
        }
        return this;
    }

    @Override
    public NSearchCmd addIds(NId... value) {
        if (value != null) {
            for (NId s : value) {
                if (s != null) {
                    ids.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NSearchCmd removeId(String id) {
        NSession session=getWorkspace().currentSession();
        ids.remove(NId.of(id).get());
        return this;
    }

    @Override
    public NSearchCmd removeId(NId id) {
        if (id != null) {
            removeId(id.toString());
        }
        return this;
    }

    @Override
    public boolean isRuntime() {
        return "runtime".equals(execType);
    }

    @Override
    public NSearchCmd setRuntime(boolean enable) {
        this.execType = enable ? "runtime" : null;
        return this;
    }

    @Override
    public boolean isCompanion() {
        return "companion".equals(execType);
    }

    @Override
    public NSearchCmd setCompanion(boolean enable) {
        this.execType = enable ? "companion" : null;
        return this;
    }

    @Override
    public boolean isExtension() {
        return "extension".equals(execType);
    }

    @Override
    public NSearchCmd setExtension(boolean enable) {
        this.execType = enable ? "extension" : null;
        return this;
    }

    @Override
    public boolean isExec() {
        return "exec".equals(execType);
    }

    @Override
    public NSearchCmd setExec(boolean enable) {
        this.execType = enable ? "exec" : null;
        return this;
    }

    @Override
    public boolean isApplication() {
        return "app".equals(execType);
    }

    @Override
    public NSearchCmd setApplication(boolean enable) {
        this.execType = enable ? "app" : null;
        return this;
    }

    @Override
    public boolean isLib() {
        return "lib".equals(execType);
    }

    @Override
    public NSearchCmd setLib(boolean enable) {
        this.execType = enable ? "lib" : null;
        return this;
    }

    @Override
    public NSearchCmd addScript(String value) {
        if (value != null) {
            scripts.add(value);
        }
        return this;
    }

    @Override
    public NSearchCmd removeScript(String value) {
        scripts.remove(value);
        return this;
    }

    @Override
    public NSearchCmd addScripts(Collection<String> value) {
        if (value != null) {
            addScripts(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NSearchCmd addScripts(String... value) {
        if (value != null) {
            scripts.addAll(Arrays.asList(value));
        }
        return this;

    }

    @Override
    public NSearchCmd clearScripts() {
        scripts.clear();
        return this;
    }

    @Override
    public List<String> getScripts() {
        return scripts;
    }

    @Override
    public NSearchCmd clearArch() {
        this.arch.clear();
        return this;
    }

    @Override
    public NSearchCmd addLockedIds(String... values) {
        NSession session=getWorkspace().currentSession();
        if (values != null) {
            for (String s : values) {
                if (!NBlankable.isBlank(s)) {
                    lockedIds.add(NId.of(s).get());
                }
            }
        }
        return this;
    }

    @Override
    public NSearchCmd addLockedIds(List<NId> values) {
        return addLockedIds(values.toArray(new NId[0]));
    }

    @Override
    public NSearchCmd addLockedIds(NId... values) {
        if (values != null) {
            for (NId s : values) {
                if (s != null) {
                    lockedIds.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NSearchCmd clearLockedIds() {
        lockedIds.clear();
        return this;
    }

    @Override
    public NSearchCmd addArch(String value) {
        if (!NBlankable.isBlank(value)) {
            this.arch.add(value);
        }
        return this;
    }

    @Override
    public NSearchCmd removeArch(String value) {
        this.arch.remove(value);
        return this;
    }

    @Override
    public NSearchCmd addArch(Collection<String> values) {
        if (values != null) {
            addArch(values.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NSearchCmd addArch(String... values) {
        if (values != null) {
            arch.addAll(Arrays.asList(values));
        }
        return this;
    }

    @Override
    public NSearchCmd clearPackaging() {
        packaging.clear();
        return this;
    }

    @Override
    public NSearchCmd addPackaging(Collection<String> values) {
        if (values != null) {
            addPackaging(values.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NSearchCmd addPackaging(String... values) {
        if (values != null) {
            this.packaging.addAll(Arrays.asList(values));
        }
        return this;
    }

    @Override
    public NSearchCmd addPackaging(String value) {
        if (value != null) {
            packaging.add(value);
        }
        return this;
    }

    @Override
    public NSearchCmd removePackaging(String value) {
        packaging.remove(value);
        return this;
    }

    @Override
    public NSearchCmd addLockedId(NId id) {
        if (id != null) {
            addLockedId(id.toString());
        }
        return this;
    }

    @Override
    public NSearchCmd removeLockedId(NId id) {
        if (id != null) {
            removeLockedId(id.toString());
        }
        return this;
    }

    @Override
    public NSearchCmd removeLockedId(String id) {
        NSession session=getWorkspace().currentSession();
        lockedIds.remove(NId.of(id).get());
        return this;
    }

    @Override
    public NSearchCmd addLockedId(String id) {
        NSession session=getWorkspace().currentSession();
        if (!NBlankable.isBlank(id)) {
            lockedIds.add(NId.of(id).get());
        }
        return this;
    }

    @Override
    public List<NId> getLockedIds() {
        return this.lockedIds;
    }


    @Override
    public NSearchCmd sort(Comparator<?> comparator) {
        this.comparator = NComparator.of(comparator);
        this.sorted = true;
        return this;
    }

    @Override
    public NSearchCmd setAll(NSearchCmd other) {
        super.copyFromDefaultNQueryBaseOptions((DefaultNQueryBaseOptions) other);
        if (other != null) {
            NSearchCmd o = other;
            this.comparator = o.getComparator();
            this.descriptorFilter = o.getDescriptorFilter();
            this.idFilter = o.getIdFilter();
            this.latest = o.isLatest();
            this.distinct = (o.isDistinct());
            this.includeBasePackage = o.isBasePackage();
            this.sorted = o.isSorted();
            this.arch.clear();
            this.arch.addAll(o.getArch());
            this.ids.clear();
            this.ids.addAll(o.getIds());
            this.scripts.clear();
            this.scripts.addAll(o.getScripts());
            this.packaging.clear();
            this.packaging.addAll(o.getPackaging());
            this.installStatus = other.getInstallStatus();
        }
        return this;
    }

    @Override
    public NSearchCmd setAll(NFetchCmd other) {
        super.copyFromDefaultNQueryBaseOptions((DefaultNQueryBaseOptions) other);
        return this;
    }

    @Override
    public List<NId> getIds() {
        return this.ids;
    }

    @Override
    public NSearchCmd setIds(String... ids) {
        clearIds();
        addIds(ids);
        return this;
    }

    @Override
    public NSearchCmd setIds(NId... ids) {
        clearIds();
        addIds(ids);
        return this;
    }

    @Override
    public boolean isSorted() {
        return sorted;
    }

    @Override
    public NSearchCmd setSorted(boolean sort) {
        this.sorted = sort;
        return this;
    }

    @Override
    public NDescriptorFilter getDescriptorFilter() {
        return descriptorFilter;
    }

    @Override
    public NSearchCmd setDescriptorFilter(NDescriptorFilter filter) {
        this.descriptorFilter = filter;
        return this;
    }

    @Override
    public NSearchCmd setDescriptorFilter(String filter) {
        NSession session=getWorkspace().currentSession();
        this.descriptorFilter = NDescriptorFilters.of().parse(filter);
        return this;
    }

    @Override
    public NIdFilter getIdFilter() {
        return idFilter;
    }

    @Override
    public NSearchCmd setIdFilter(NIdFilter filter) {
        this.idFilter = filter;
        return this;
    }

    @Override
    public NSearchCmd setIdFilter(String filter) {
        NSession session=getWorkspace().currentSession();
        this.idFilter = NIdFilters.of().parse(filter);
        return this;
    }

    @Override
    public List<String> getArch() {
        return arch;
    }

    @Override
    public List<String> getPackaging() {
        return this.packaging;
    }

    @Override
    public NComparator getComparator() {
        return comparator;
    }

    //    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder("NutsSearch{");
//        sb.append(getScope());
//        if (ids.size() > 0) {
//            sb.append(",ids=").append(ids);
//        }
//        if (lockedIds.size() > 0) {
//            sb.append(",lockedIds=").append(lockedIds);
//        }
//        if (idFilter != null) {
//            sb.append(",idFilter=").append(idFilter);
//        }
//        if (dependencyFilter != null) {
//            sb.append(",dependencyFilter=").append(dependencyFilter);
//        }
//        if (repositoryFilter != null) {
//            sb.append(",repositoryFilter=").append(repositoryFilter);
//        }
//        if (descriptorFilter != null) {
//            sb.append(",descriptorFilter=").append(descriptorFilter);
//        }
//        sb.append('}');
//        return sb.toString();
//    }
    @Override
    public boolean isDistinct() {
        return distinct;
    }

    @Override
    public NSearchCmd setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    @Override
    public NSearchCmd distinct() {
        return setDistinct(true);
    }

    @Override
    public NVersion getTargetApiVersion() {
        return targetApiVersion;
    }

    @Override
    public NSearchCmd setTargetApiVersion(NVersion targetApiVersion) {
        this.targetApiVersion = targetApiVersion;
        return this;
    }

    @Override
    public boolean isBasePackage() {
        return includeBasePackage;
    }

    @Override
    public NSearchCmd setBasePackage(boolean includeBasePackage) {
        this.includeBasePackage = includeBasePackage;
        return this;
    }

    @Override
    public boolean isLatest() {
        return latest;
    }

    @Override
    public NSearchCmd setLatest(boolean enable) {
        this.latest = enable;
        return this;
    }

    @Override
    public NSearchCmd latest() {
        return setLatest(true);
    }

    @Override
    public NStream<NId> getResultIds() {
        return buildCollectionResult(getResultIdIteratorBase(null));
    }

    @Override
    public NStream<NDependencies> getResultDependencies() {
        NSession session=getWorkspace().currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(true, isEffective()))
                .map(NFunction.of((NDefinition x) -> x.getDependencies().get())
                        .withDesc(NEDesc.of("getDependencies")))
        );
    }

    @Override
    public NStream<NDependency> getResultInlineDependencies() {
        NSession session=getWorkspace().currentSession();
        return buildCollectionResult(
                IteratorBuilder.of(getResultIdIteratorBase(true)).map(
                                NFunction.of(NId::toDependency)
                                        .withDesc(NEDesc.of("Id->Dependency")))
                        .build()
        );
    }

    @Override
    public NStream<NDefinition> getResultDefinitions() {
        return buildCollectionResult(getResultDefinitionIteratorBase(isContent(), isEffective()));
    }

    @Override
    public NStream<NDescriptor> getResultDescriptors() {
        return getResultDefinitions().map(NDefinition::getDescriptor);
    }

    @Override
    public ClassLoader getResultClassLoader() {
        return getResultClassLoader(null);
    }

    @Override
    public ClassLoader getResultClassLoader(ClassLoader parent) {
        //force content and dependencies!
        setContent(true);
        setDependencies(true);

        List<NDefinition> nDefinitions = getResultDefinitions().toList();
        URL[] allURLs = new URL[nDefinitions.size()];
        NId[] allIds = new NId[nDefinitions.size()];
        for (int i = 0; i < allURLs.length; i++) {
            NDefinition d = nDefinitions.get(i);
            allURLs[i] = d.getContent().flatMap(NPath::toURL).orNull();
            allIds[i] = d.getId();
        }
        NSession session=getWorkspace().currentSession();
        DefaultNClassLoader cl = ((DefaultNExtensions) NExtensions.of())
                .getModel().getNutsURLClassLoader("SEARCH-" + UUID.randomUUID(), parent);
        for (NDefinition def : nDefinitions) {
            cl.add(NClassLoaderUtils.definitionToClassLoaderNode(def, getRepositoryFilter()));
        }
        return cl;
    }

    @Override
    public String getResultNutsPath() {
        return getResultIds().toList().stream().map(NId::getLongName).collect(Collectors.joining(";"));
    }

    @Override
    public String getResultClassPath() {
        StringBuilder sb = new StringBuilder();
        NIterator<NDefinition> it = getResultDefinitionIteratorBase(true, isEffective());
        while (it.hasNext()) {
            NDefinition nDefinition = it.next();
            if (nDefinition.getContent().isPresent()) {
                if (sb.length() > 0) {
                    sb.append(File.pathSeparator);
                }
                sb.append(nDefinition.getContent().orNull());
            }
        }
        return sb.toString();
    }

    /**
     * @return default version or null
     * @since 0.5.5
     */
    @Override
    public Boolean getDefaultVersions() {
        return defaultVersions;
    }

    /**
     * @param acceptDefaultVersion acceptDefaultVersion
     * @return {@code this} instance
     * @since 0.5.5
     */
    @Override
    public NSearchCmd setDefaultVersions(Boolean acceptDefaultVersion) {
        this.defaultVersions = acceptDefaultVersion;
        return this;
    }

    @Override
    public NStream<String> getResultPaths() {
        NSession session=workspace.currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(true, isEffective()))
                .map(
                        NFunction.of((NDefinition x) -> x.getContent().map(Object::toString).orNull())
                                .withDesc(NEDesc.of("getPath"))
                )
                .notBlank()
        );
    }

    @Override
    public NStream<String> getResultPathNames() {
        NSession session=workspace.currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(true, isEffective()))
                .map(NFunction.of((NDefinition x) -> x.getContent().map(NPath::getName).orNull())
                        .withDesc(NEDesc.of("getName")))
                .notBlank());
    }

    @Override
    public NStream<Instant> getResultInstallDates() {
        NSession session=workspace.currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()))
                .map(NFunction.of((NDefinition x) -> x.getInstallInformation().map(NInstallInformation::getCreatedInstant).orNull()).withDesc(NEDesc.of("getCreatedInstant")))
                .notNull());
    }

    @Override
    public NStream<String> getResultInstallUsers() {
        NSession session=workspace.currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()))
                .map(NFunction.of((NDefinition x) -> x.getInstallInformation().map(NInstallInformation::getInstallUser).orNull()).withDesc(NEDesc.of("getInstallUser")))
                .notBlank());
    }

    @Override
    public NStream<NPath> getResultInstallFolders() {
        NSession session=workspace.currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()))
                .map(NFunction.of((NDefinition x) -> x.getInstallInformation().map(NInstallInformation::getInstallFolder).orNull())
                        .withDesc(NEDesc.of("getInstallFolder"))
                )
                .notNull());
    }

    @Override
    public NStream<NPath> getResultStoreLocations(NStoreType location) {
        NSession session=workspace.currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()))
                .map(NFunction.of((NDefinition x) -> NLocations.of().getStoreLocation(x.getId(), location))
                        .withDesc(NEDesc.of("getStoreLocation(" + location.id() + ")"))
                )
                .notNull());
    }

    @Override
    public NStream<String[]> getResultStrings(String[] columns) {
        NSession session=workspace.currentSession();
        NFetchDisplayOptions oo = new NFetchDisplayOptions(workspace);
        oo.addDisplay(columns);
        oo.setIdFormat(getDisplayOptions().getIdFormat());
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()))
                .map(NFunction.of(x
                                -> NIdFormatHelper.of(x)
                                .buildLong().getMultiColumnRowStrings(oo)
                        ).withDesc(NEDesc.of("getColumns"))
                ));
    }

    @Override
    public NStream<String> getResultNames() {
        NSession session=workspace.currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()))
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getName()))
                        .withDesc(NEDesc.of("getDescriptorName"))
                )
                .notBlank());
    }

    @Override
    public NStream<String> getResultOs() {
        NSession session=workspace.currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()))
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getCondition().getOs()))
                        .withDesc(NEDesc.of("getOs")))
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<NExecutionEntry> getResultExecutionEntries() {
        NSession session=workspace.currentSession();
        IteratorBuilder<NDefinition> defIter = IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()));
        return postProcessResult(defIter
                .mapMulti(
                        NFunction.of(
                                (NDefinition x) -> x.getContent().map(NExecutionEntry::parse).orElse(Collections.emptyList())
                        ).withDesc(NEDesc.of("getFile"))
                ));
    }

    @Override
    public NStream<String> getResultOsDist() {
        NSession session=workspace.currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()))
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getCondition().getOsDist()))
                        .withDesc(NEDesc.of("getOsDist"))
                )
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultPackaging() {
        NSession session=workspace.currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()))
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getPackaging()))
                        .withDesc(NEDesc.of("getPackaging"))
                )
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultPlatform() {
        NSession session=workspace.currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()))
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getCondition().getPlatform()))
                        .withDesc(NEDesc.of("getPlatform"))
                )
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultProfile() {
        NSession session=workspace.currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()))
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getCondition().getProfile()))
                        .withDesc(NEDesc.of("getProfile"))
                )
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultDesktopEnvironment() {
        NSession session=workspace.currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()))
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getCondition().getDesktopEnvironment()))
                        .withDesc(NEDesc.of("getDesktopEnvironment"))
                )
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultArch() {
        NSession session=workspace.currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()))
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getCondition().getArch()))
                        .withDesc(NEDesc.of("getArch"))
                )
                .notBlank());
    }

    @Override
    public NInstallStatusFilter getInstallStatus() {
        return installStatus;
    }

    @Override
    public NSearchCmd setInstallStatus(NInstallStatusFilter installStatus) {
        this.installStatus = installStatus;
        return this;
    }

    @Override
    public NSearchCmd setId(String id) {
        clearIds();
        addId(id);
        return this;
    }

    @Override
    public NSearchCmd setId(NId id) {
        clearIds();
        addId(id);
        return this;
    }

    public String getExecType() {
        return execType;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NSession session=workspace.currentSession();
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch (a.key()) {
            case "--inline-dependencies": {
                cmdLine.withNextFlag((v, r) -> this.setInlineDependencies(v));
                return true;
            }
            case "-L":
            case "--latest":
            case "--latest-versions": {
                cmdLine.withNextFlag((v, r) -> this.setLatest(v));
                return true;
            }
            case "--distinct": {
                cmdLine.withNextFlag((v, r) -> this.setDistinct(v));
                return true;
            }
            case "--default":
            case "--default-versions": {
                cmdLine.withNextOptionalFlag((v, r) -> this.setDefaultVersions(v.ifError(false).orElse(null)));
                return true;
            }
            case "--duplicates": {
                cmdLine.withNextFlag((v, r) -> this.setDistinct(!v));
                return true;
            }
            case "-s":
            case "--sort": {
                cmdLine.withNextFlag((v, r) -> this.setSorted(v));
                return true;
            }
            case "--base": {
                cmdLine.withNextFlag((v, r) -> this.includeBasePackage = v);
                return true;
            }
            case "--lib":
            case "--libs": {
                cmdLine.withNextFlag((v, r) -> this.setLib(v));
                return true;
            }
            case "--app":
            case "--apps": {
                cmdLine.withNextFlag((v, r) -> this.setExec(v));
                return true;
            }
            case "--companion":
            case "--companions": {
                cmdLine.withNextFlag((v, r) -> this.setCompanion(v));
                return true;
            }
            case "--extension":
            case "--extensions": {
                cmdLine.withNextFlag((v, r) -> this.setExtension(v));
                return true;
            }
            case "--runtime": {
                cmdLine.withNextFlag((v, r) -> this.setRuntime(v));
                return true;
            }
            case "--api-version": {
                cmdLine.withNextEntry((v, r) -> this.setTargetApiVersion(NVersion.of(v).get()));
                return true;
            }
            case "--nuts-app":
            case "--nuts-apps": {
                cmdLine.withNextFlag((v, r) -> this.setApplication(v));
                return true;
            }
            case "--arch": {
                cmdLine.withNextEntry((v, r) -> this.addArch(v));
                return true;
            }
            case "--packaging": {
                cmdLine.withNextEntry((v, r) -> this.addPackaging(v));
                return true;
            }
            case "--optional": {
                NArg val = cmdLine.nextEntry().get();
                if (enabled) {
                    this.setOptional(val.getValue().asBoolean().orNull());
                }
                return true;
            }
            case "--script": {
                cmdLine.withNextEntry((v, r) -> this.addScripts(v));
                return true;
            }
            case "--id": {
                cmdLine.withNextEntry((v, r) -> this.addId(v));
                return true;
            }
            case "--locked-id": {
                cmdLine.withNextEntry((v, r) -> this.addLockedId(v));
                return true;
            }
            case "--deployed": {
                NArg b = cmdLine.nextFlag().get();
                if (enabled) {
                    this.setInstallStatus(NInstallStatusFilters.of().byDeployed(b.getBooleanValue().get()));
                }
                return true;
            }
            case "-i":
            case "--installed": {
                NArg b = cmdLine.nextFlag().get();
                if (enabled) {
                    this.setInstallStatus(
                            NInstallStatusFilters.of().byInstalled(b.getBooleanValue().get())
                    );
                }
                return true;
            }
            case "--required": {
                NArg b = cmdLine.nextFlag().get();
                if (enabled) {
                    this.setInstallStatus(NInstallStatusFilters.of().byRequired(b.getBooleanValue().get()));
                }
                return true;
            }
            case "--obsolete": {
                NArg b = cmdLine.nextFlag().get();
                if (enabled) {
                    this.setInstallStatus(NInstallStatusFilters.of().byObsolete(b.getBooleanValue().get()));
                }
                return true;
            }
            case "--status": {
                NArg aa = cmdLine.nextEntry().get();
                if (enabled) {
                    this.setInstallStatus(NInstallStatusFilters.of().parse(aa.getStringValue().get()));
                }
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
                    addId(a.asString().get());
                    return true;
                }
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{"
                + "failFast=" + isFailFast()
                + ", optional=" + getOptional()
                + ", scope=" + getScope()
                + ", content=" + isContent()
                + ", inlineDependencies=" + isInlineDependencies()
                + ", dependencies=" + isDependencies()
                + ", effective=" + isEffective()
                + ", displayOptions=" + getDisplayOptions()
                + ", comparator=" + getComparator()
                + ", dependencyFilter=" + getDependencyFilter()
                + ", descriptorFilter=" + getDescriptorFilter()
                + ", idFilter=" + getIdFilter()
                + ", repositoryFilter=" + getRepositoryFilter()
                + ", latest=" + isLatest()
                + ", distinct=" + isDistinct()
                + ", includeMain=" + isBasePackage()
                + ", sorted=" + isSorted()
                + ", arch=" + getArch()
                + ", ids=" + getIds()
                + ", lockedIds=" + getLockedIds()
                + ", scripts=" + getScripts()
                + ", packaging=" + getPackaging()
                + ", defaultVersions=" + getDefaultVersions()
                + ", execType='" + getExecType() + '\''
                + ", targetApiVersion='" + getTargetApiVersion() + '\''
                + '}';
    }

    private Object dependenciesToElement(NDependencyTreeNode d) {
        NId id
                = //                getSearchSession().getWorkspace().text().parse(d.getDependency().formatter().setSession(getSearchSession()).setNtf(false).format())
                d.getDependency().toId();
        if (d.isPartial()) {
            id = id.builder().setProperty("partial", "true").build();
        }
        List<Object> li = d.getChildren().stream().map(x -> dependenciesToElement(x)).collect(Collectors.toList());
        if (li.isEmpty()) {
            return id;
        }
        Map<Object, Object> o = new HashMap<>();
        o.put(id, li);
        return o;
    }

    public <T> NIterator<T> runIterator() {
        NDisplayProperty[] a = getDisplayOptions().getDisplayProperties();
        NStream r = null;
        if (isDependencies() && !isInlineDependencies()) {
            NContentType of = getSearchSession().getOutputFormat().orDefault();
            if (of == null) {
                of = NContentType.TREE;
            }
            switch (of) {
                case JSON:
                case TSON:
                case XML:
                case YAML:
                case TREE: {
                    NSession session=workspace.currentSession();
                    return (NIterator) IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()))
                            .flatMap(NFunction.of((NDefinition x) -> x.getDependencies().get().transitiveNodes().iterator())
                                    .withDesc(NEDesc.of("getDependencies"))
                            )
                            .map(NFunction.of((NDependencyTreeNode x) -> dependenciesToElement(x))
                                    .withDesc(NEDesc.of("dependenciesToElement"))
                            )
                            .build();
                }

                default: {
                    NStream<NDependency> rr = getResultInlineDependencies();
                    return (NIterator) rr.iterator();
                }
            }
        } else {
            if (a.length == 0) {
                r = getResultIds();
            } else if (a.length == 1) {
                //optimized case
                switch (a[0]) {
                    case ARCH: {
                        r = getResultArch();
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
                        r = getResultPackaging();
                        break;
                    }
                    case PLATFORM: {
                        r = getResultPlatform();
                        break;
                    }
                    case DESKTOP_ENVIRONMENT: {
                        r = getResultDesktopEnvironment();
                        break;
                    }
                    case EXEC_ENTRY: {
                        r = getResultExecutionEntries();
                        break;
                    }
                    case OS: {
                        r = getResultOs();
                        break;
                    }
                    case OSDIST: {
                        r = getResultOsDist();
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
                    case BIN_FOLDER: {
                        r = getResultStoreLocations(NStoreType.BIN);
                        break;
                    }
                    case CACHE_FOLDER: {
                        r = getResultStoreLocations(NStoreType.CACHE);
                        break;
                    }
                    case CONF_FOLDER: {
                        r = getResultStoreLocations(NStoreType.CONF);
                        break;
                    }
                    case LIB_FOLDER: {
                        r = getResultStoreLocations(NStoreType.LIB);
                        break;
                    }
                    case LOG_FOLDER: {
                        r = getResultStoreLocations(NStoreType.LOG);
                        break;
                    }
                    case TEMP_FOLDER: {
                        r = getResultStoreLocations(NStoreType.TEMP);
                        break;
                    }
                    case VAR_LOCATION: {
                        r = getResultStoreLocations(NStoreType.VAR);
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
                for (NDisplayProperty display : getDisplayOptions().getDisplayProperties()) {
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
//                        _content = true;
                            break;
                        }
                        case INSTALL_DATE:
                        case INSTALL_USER: {
                            break;
                        }
                        case STATUS: {
//                        _content = true;
                            break;
                        }
                    }
                }
                r = buildCollectionResult(
                        getResultDefinitionIteratorBase(_content, _effective)
                );
            }
            return r.iterator();
        }
    }

    @Override
    public NElement getResultQueryPlan() {
        return toQueryPlan(runIterator());
    }

    @Override
    public NSearchCmd run() {
        NIterator<Object> it = runIterator();
        NSession session=workspace.currentSession();
        if (session.isDry()) {
            displayDryQueryPlan(it);
        } else {
            it = NWorkspaceUtils.of(getWorkspace()).decoratePrint(it, getSearchSession(), getDisplayOptions());
            while (it.hasNext()) {
                it.next();
            }
        }
        return this;
    }

    private NElement toQueryPlan(NIterator it) {
        NSession session=workspace.currentSession();
        NElements elem = NElements.of();
        return
                elem.ofObject()
                        .set("SearchQueryPlan",
                                NEDesc.describeResolveOrDestruct(it))
                        .build();
    }

    private void displayDryQueryPlan(NIterator it) {
        NElement n = toQueryPlan(it);
        NSession session=workspace.currentSession();
        NContentType f = session.getOutputFormat().orDefault();
        if (f == NContentType.PLAIN) {
            f = NContentType.TREE;
        }
        NSession session2 = session.copy().setOutputFormat(f);
        session2.out().println(n);
    }


    public NIterator<NDefinition> getResultDefinitionIteratorBase(boolean content, boolean effective) {
        NFetchCmd fetch = toFetch().setContent(content).setEffective(effective);
        NSession session=workspace.currentSession();
//        NFetchCmd ofetch = toFetch().setContent(content).setEffective(effective)
//                .setSession(session.copy().setFetchStrategy(NFetchStrategy.OFFLINE));
        final boolean hasRemote = session.getFetchStrategy().orDefault() == null
                || session.getFetchStrategy().orDefault().modes().stream()
                .anyMatch(x -> x == NFetchMode.REMOTE);
        return IteratorBuilder.of(getResultIdIteratorBase(null))
                .map(NFunction.of((NId next) -> {
//                    NutsDefinition d = null;
//                    if (isContent()) {
                    NDefinition d = fetch.setId(next).getResultDefinition();
                    if (d == null) {
                        if (isFailFast()) {
                            throw new NNotFoundException(next);
                        }
                        return d;
                    }
                    return d;
//                    } else {
//                        if (hasRemote) {
//                            fetch.setId(next).getResultDescriptor();
//                        }
//                        d = ofetch.setId(next).getResultDefinition();
//                        if(d==null){
//                            _LOGOP(session)
//                                    .verb(NutsLogVerb.FAIL)
//                                    .log("inconsistent repository. id %s was found but its definition could not be resolved!",next);
//                        }
//                    }
//                    return d;
                }).withDesc(NEDesc.of("Id->Definition")))
                .notNull().build();
    }

    protected <T> NStream<T> buildCollectionResult(NIterator<T> o) {
        NSession ss = getSearchSession();
        return new NStreamFromNIterator<T>(resolveFindIdBase(), o);
    }

    protected String resolveFindIdBase() {
        return ids.isEmpty() ? null : ids.get(0) == null ? null : ids.get(0).toString();
    }

    public NStream<String> getResultStatuses() {
        NSession session=workspace.currentSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()))
                .map(NFunction.of(
                                x -> NIdFormatHelper.of(x)
                                        .buildLong().getStatusString())
                        .withDesc(NEDesc.of("getStatusString"))
                )
                .notBlank());
    }

    //    protected NutsStream<NutsDefinition> getResultDefinitionsBase(boolean print, boolean sort, boolean content, boolean effective) {
//        checkSession();
//        return new NutsDefinitionNutsResult(session, resolveFindIdBase(), print, sort, content, effective);
//    }
//
    protected abstract NIterator<NId> getResultIdIteratorBase(Boolean forceInlineDependencies);

    protected NStream<NId> getResultIdsBase(boolean sort) {
        return buildCollectionResult(getResultIdIteratorBase(null));
    }

    protected <T> NStream<T> postProcessResult(IteratorBuilder<T> a) {
        if (isSorted()) {
            a = a.sort(null, isDistinct());
        }
        return buildCollectionResult(a.build());
    }

    protected NSession getSearchSession() {
        NSession session=getWorkspace().currentSession();
        return session;
    }

    protected NIterator<NId> applyPrintDecoratorIterOfNutsId(NIterator<NId> curr, boolean print) {
        return print ? NWorkspaceUtils.of(getWorkspace()).decoratePrint(curr, getSearchSession(), getDisplayOptions()) : curr;
    }
}
