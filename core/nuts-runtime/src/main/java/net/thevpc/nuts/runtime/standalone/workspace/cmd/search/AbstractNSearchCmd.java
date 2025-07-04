/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
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
import net.thevpc.nuts.NConstants;


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinitionBuilder2;
import net.thevpc.nuts.runtime.standalone.definition.NDefinitionFilterUtils;
import net.thevpc.nuts.runtime.standalone.util.ValueSupplier;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NDescribableElementSupplier;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.util.NIteratorBuilder;
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

    protected final List<NId> ids = new ArrayList<>();
    protected NComparator comparator;
    protected NDefinitionFilter definitionFilter;
    protected boolean latest = false;
    protected boolean distinct = false;
    protected boolean includeBasePackage = true;
    protected boolean sorted = false;
    protected boolean ignoreCurrentEnvironment;
    protected boolean describe;
    protected SearchExecType execType = null;
    protected NVersion targetApiVersion = null;

    public AbstractNSearchCmd() {
        super("search");
    }

    @Override
    public boolean isIgnoreCurrentEnvironment() {
        return ignoreCurrentEnvironment;
    }

    @Override
    public NSearchCmd setIgnoreCurrentEnvironment(boolean ignoreCurrentEnvironment) {
        this.ignoreCurrentEnvironment = ignoreCurrentEnvironment;
        return this;
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
        if (!NBlankable.isBlank(id)) {
            ids.add(NId.get(id).get());
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
        if (values != null) {
            for (String s : values) {
                if (!NBlankable.isBlank(s)) {
                    ids.add(NId.get(s).get());
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
        ids.remove(NId.get(id).get());
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
        return SearchExecType.RUNTIME == execType;
    }

    @Override
    public NSearchCmd setRuntime(boolean enable) {
        this.execType = enable ? SearchExecType.RUNTIME : null;
        return this;
    }

    @Override
    public boolean isCompanion() {
        return execType == SearchExecType.COMPANION;
    }

    @Override
    public NSearchCmd setCompanion(boolean enable) {
        this.execType = enable ? SearchExecType.COMPANION : null;
        return this;
    }

    @Override
    public boolean isExtension() {
        return SearchExecType.EXTENSION == execType;
    }

    @Override
    public NSearchCmd setExtension(boolean enable) {
        this.execType = enable ? SearchExecType.EXTENSION : null;
        return this;
    }

    @Override
    public boolean isExec() {
        return SearchExecType.EXEC == execType;
    }

    @Override
    public NSearchCmd setExec(boolean enable) {
        this.execType = enable ? SearchExecType.EXEC : null;
        return this;
    }

    @Override
    public boolean isNutsApplication() {
        return SearchExecType.NUTS_APPLICATION == execType;
    }

    @Override
    public NSearchCmd setNutsApplication(boolean enable) {
        this.execType = enable ? SearchExecType.NUTS_APPLICATION : null;
        return this;
    }

    @Override
    public boolean isPlatformApplication() {
        return SearchExecType.PLATFORM_APPLICATION == execType;
    }

    @Override
    public NSearchCmd setPlatformApplication(boolean enable) {
        this.execType = enable ? SearchExecType.PLATFORM_APPLICATION : null;
        return this;
    }

    @Override
    public boolean isLib() {
        return SearchExecType.LIB == execType;
    }

    @Override
    public NSearchCmd setLib(boolean enable) {
        this.execType = enable ? SearchExecType.LIB : null;
        return this;
    }



    @Override
    public NSearchCmd sort(Comparator<?> comparator) {
        this.comparator = NComparator.of(comparator);
        this.sorted = true;
        return this;
    }

    @Override
    public NSearchCmd copyFrom(NSearchCmd other) {
        super.copyFromDefaultNQueryBaseOptions((DefaultNQueryBaseOptions) other);
        if (other != null) {
            NSearchCmd o = other;
            this.ignoreCurrentEnvironment = o.isIgnoreCurrentEnvironment();
            this.comparator = o.getComparator();
            this.definitionFilter = o.getDefinitionFilter();
            this.latest = o.isLatest();
            this.distinct = (o.isDistinct());
            this.includeBasePackage = o.isBasePackage();
            this.sorted = o.isSorted();
            this.ids.clear();
            this.ids.addAll(o.getIds());
        }
        return this;
    }

    @Override
    public NSearchCmd copyFrom(NFetchCmd other) {
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
    public NDefinitionFilter getDefinitionFilter() {
        return definitionFilter;
    }

    @Override
    public NSearchCmd setDefinitionFilter(NDefinitionFilter filter) {
        this.definitionFilter = filter;
        return this;
    }

    @Override
    public NSearchCmd addDefinitionFilter(NDefinitionFilter filter) {
        if (filter != null) {
            if (this.definitionFilter == null) {
                this.definitionFilter = filter;
            } else {
                this.definitionFilter.and(filter);
            }
        }
        return this;
    }

    @Override
    public NComparator getComparator() {
        return comparator;
    }

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
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(NFunction.of((NDefinition x) -> x.getDependencies().get())
                        .redescribe(NDescribableElementSupplier.of("getDependencies")))
        );
    }

    @Override
    public NStream<NDependency> getResultInlineDependencies() {
        return buildCollectionResult(
                NIteratorBuilder.of(getResultIdIteratorBase(true)).map(
                                NFunction.of(NId::toDependency)
                                        .redescribe(NDescribableElementSupplier.of("Id->Dependency")))
                        .build()
        );
    }

    @Override
    public NStream<NDefinition> getResultDefinitions() {
        return buildCollectionResult(getResultDefinitionIteratorBase());
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
//        setContent(true);
//        setDependencies(true);

        List<NDefinition> nDefinitions = getResultDefinitions().toList();
        URL[] allURLs = new URL[nDefinitions.size()];
        NId[] allIds = new NId[nDefinitions.size()];
        for (int i = 0; i < allURLs.length; i++) {
            NDefinition d = nDefinitions.get(i);
            allURLs[i] = d.getContent().flatMap(NPath::toURL).orNull();
            allIds[i] = d.getId();
        }
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
        NIterator<NDefinition> it = getResultDefinitionIteratorBase();
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

    @Override
    public NStream<String> getResultPaths() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(
                        NFunction.of((NDefinition x) -> x.getContent().map(Object::toString).orNull())
                                .redescribe(NDescribableElementSupplier.of("getPath"))
                )
                .notBlank()
        );
    }

    @Override
    public NStream<String> getResultPathNames() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(NFunction.of((NDefinition x) -> x.getContent().map(NPath::getName).orNull())
                        .redescribe(NDescribableElementSupplier.of("getName")))
                .notBlank());
    }

    @Override
    public NStream<Instant> getResultInstallDates() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(NFunction.of((NDefinition x) -> x.getInstallInformation().map(NInstallInformation::getCreatedInstant).orNull()).redescribe(NDescribableElementSupplier.of("getCreatedInstant")))
                .notNull());
    }

    @Override
    public NStream<String> getResultInstallUsers() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(NFunction.of((NDefinition x) -> x.getInstallInformation().map(NInstallInformation::getInstallUser).orNull()).redescribe(NDescribableElementSupplier.of("getInstallUser")))
                .notBlank());
    }

    @Override
    public NStream<NPath> getResultInstallFolders() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(NFunction.of((NDefinition x) -> x.getInstallInformation().map(NInstallInformation::getInstallFolder).orNull())
                        .redescribe(NDescribableElementSupplier.of("getInstallFolder"))
                )
                .notNull());
    }

    @Override
    public NStream<NPath> getResultStoreLocations(NStoreType location) {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(NFunction.of((NDefinition x) -> NWorkspace.of().getStoreLocation(x.getId(), location))
                        .redescribe(NDescribableElementSupplier.of("getStoreLocation(" + location.id() + ")"))
                )
                .notNull());
    }

    @Override
    public NStream<String[]> getResultStrings(String[] columns) {
        NFetchDisplayOptions oo = new NFetchDisplayOptions();
        oo.addDisplay(columns);
        oo.setIdFormat(getDisplayOptions().getIdFormat());
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(NFunction.of(x
                                -> NIdFormatHelper.of(x)
                                .buildLong().getMultiColumnRowStrings(oo)
                        ).redescribe(NDescribableElementSupplier.of("getColumns"))
                ));
    }

    @Override
    public NStream<String> getResultNames() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getName()))
                        .redescribe(NDescribableElementSupplier.of("getDescriptorName"))
                )
                .notBlank());
    }

    @Override
    public NStream<String> getResultOs() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getCondition().getOs()))
                        .redescribe(NDescribableElementSupplier.of("getOs")))
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<NExecutionEntry> getResultExecutionEntries() {
        NIteratorBuilder<NDefinition> defIter = NIteratorBuilder.of(getResultDefinitionIteratorBase());
        return postProcessResult(defIter
                .mapMulti(
                        NFunction.of(
                                (NDefinition x) -> x.getContent().map(NExecutionEntry::parse).orElse(Collections.emptyList())
                        ).redescribe(NDescribableElementSupplier.of("getFile"))
                ));
    }

    @Override
    public NStream<String> getResultOsDist() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getCondition().getOsDist()))
                        .redescribe(NDescribableElementSupplier.of("getOsDist"))
                )
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultPackaging() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getPackaging()))
                        .redescribe(NDescribableElementSupplier.of("getPackaging"))
                )
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultPlatform() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getCondition().getPlatform()))
                        .redescribe(NDescribableElementSupplier.of("getPlatform"))
                )
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultProfile() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getCondition().getProfiles()))
                        .redescribe(NDescribableElementSupplier.of("getProfile"))
                )
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultDesktopEnvironment() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getCondition().getDesktopEnvironment()))
                        .redescribe(NDescribableElementSupplier.of("getDesktopEnvironment"))
                )
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultArch() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.getDescriptor().getCondition().getArch()))
                        .redescribe(NDescribableElementSupplier.of("getArch"))
                )
                .notBlank());
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

    public SearchExecType getExecType() {
        return execType;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isNonCommented();
        switch (a.key()) {
            case "--inline-dependencies": {
                cmdLine.withNextFlag((v) -> this.setInlineDependencies(v.booleanValue()));
                return true;
            }
            case "--describe": {
                cmdLine.withNextFlag((v) -> this.describe=v.booleanValue());
                return true;
            }
            case "-L":
            case "--latest":
            case "--latest-versions": {
                cmdLine.withNextFlag((v) -> this.setLatest(v.booleanValue()));
                return true;
            }
            case "--repo": {
                cmdLine.withNextEntry((v) -> this.setRepositoryFilter(NRepositoryFilters.of().bySelector(NStringUtils.split(v.stringValue(), ";,|", true, true).toArray(new String[0]))));
                return true;
            }
            case "--distinct": {
                cmdLine.withNextFlag((v) -> this.setDistinct(v.booleanValue()));
                return true;
            }
            case "--default":
            case "--default-versions": {
                cmdLine.selector().withNextFlag((v) -> this.addDefinitionFilter(NDefinitionFilters.of().byDefaultVersion(v.getBooleanValue().ifError(false).orElse(null))));
                return true;
            }
            case "--duplicates": {
                cmdLine.withNextFlag((v) -> this.setDistinct(!v.booleanValue()));
                return true;
            }
            case "-s":
            case "--sort": {
                cmdLine.withNextFlag((v) -> this.setSorted(v.booleanValue()));
                return true;
            }
            case "--base": {
                cmdLine.withNextFlag((v) -> this.includeBasePackage = v.booleanValue());
                return true;
            }
            case "--lib":
            case "--libs": {
                cmdLine.withNextFlag((v) -> this.setLib(v.booleanValue()));
                return true;
            }
            case "--app":
            case "--apps": {
                cmdLine.withNextFlag((v) -> this.setExec(v.booleanValue()));
                return true;
            }
            case "--companion":
            case "--companions": {
                cmdLine.withNextFlag((v) -> this.setCompanion(v.booleanValue()));
                return true;
            }
            case "--extension":
            case "--extensions": {
                cmdLine.withNextFlag((v) -> this.setExtension(v.booleanValue()));
                return true;
            }
            case "--runtime": {
                cmdLine.withNextFlag((v) -> this.setRuntime(v.booleanValue()));
                return true;
            }
            case "--api-version": {
                cmdLine.withNextEntry((v) -> this.setTargetApiVersion(NVersion.get(v.stringValue()).get()));
                return true;
            }
            case "--nuts-app":
            case "--nuts-apps": {
                cmdLine.withNextFlag((v) -> this.setNutsApplication(v.booleanValue()));
                return true;
            }
            case "--arch": {
                cmdLine.withNextEntry((v) -> this.addDefinitionFilter(
                        NDefinitionFilters.of().nonnull(this.getDefinitionFilter()).and(NDefinitionFilters.of().byArch(v.stringValue()))
                ));
                return true;
            }
            case "--packaging": {
                cmdLine.withNextEntry((v) -> this.addDefinitionFilter(
                        NDefinitionFilters.of().nonnull(this.getDefinitionFilter()).and(NDefinitionFilters.of().byPackaging(v.stringValue()))
                ));
                return true;
            }
            case "--id": {
                cmdLine.withNextEntry((v) -> this.addId(v.stringValue()));
                return true;
            }
            case "--locked-id": {
                cmdLine.withNextEntry((v) -> setDefinitionFilter(NDefinitionFilterUtils.addLockedIds(getDefinitionFilter(), NId.of(v.stringValue()))));
                return true;
            }
            case "--deployed": {
                NArg b = cmdLine.nextFlag().get();
                if (enabled) {
                    this.addDefinitionFilter(NDefinitionFilters.of().byDefaultValue(b.getBooleanValue().get()).and(getDefinitionFilter()));
                }
                return true;
            }
            case "-i":
            case "--installed": {
                NArg b = cmdLine.nextFlag().get();
                if (enabled) {
                    this.addDefinitionFilter(NDefinitionFilters.of().byInstalled(b.getBooleanValue().get()).and(getDefinitionFilter()));
                }
                return true;
            }
            case "--required": {
                NArg b = cmdLine.nextFlag().get();
                if (enabled) {
                    this.addDefinitionFilter(NDefinitionFilters.of().byRequired(b.getBooleanValue().get()).and(getDefinitionFilter()));
                }
                return true;
            }
            case "--obsolete": {
                NArg b = cmdLine.nextFlag().get();
                if (enabled) {
                    this.addDefinitionFilter(NDefinitionFilters.of().byObsolete(b.getBooleanValue().get()).and(getDefinitionFilter()));
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
//                + ", content=" + isContent()
                + ", inlineDependencies=" + isInlineDependencies()
//                + ", dependencies=" + isDependencies()
//                + ", effective=" + isEffective()
                + ", displayOptions=" + getDisplayOptions()
                + ", comparator=" + getComparator()
                + ", dependencyFilter=" + getDependencyFilter()
                + ", descriptorFilter=" + getDefinitionFilter()
                + ", repositoryFilter=" + getRepositoryFilter()
                + ", latest=" + isLatest()
                + ", distinct=" + isDistinct()
                + ", includeMain=" + isBasePackage()
                + ", sorted=" + isSorted()
                + ", ids=" + getIds()
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
        if (/*isDependencies() && */!isInlineDependencies()) {
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
                    return (NIterator) NIteratorBuilder.of(getResultDefinitionIteratorBase())
                            .flatMap(NFunction.of((NDefinition x) -> x.getDependencies().get().transitiveNodes().iterator())
                                    .redescribe(NDescribableElementSupplier.of("getDependencies"))
                            )
                            .map(NFunction.of((NDependencyTreeNode x) -> dependenciesToElement(x))
                                    .redescribe(NDescribableElementSupplier.of("dependenciesToElement"))
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
//                boolean _content = isContent();
//                boolean _effective = isEffective();
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
                        getResultDefinitionIteratorBase()
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
        if(describe){
            NOut.println(getResultQueryPlan());
            return this;
        }
        NIterator<Object> it = runIterator();
        NSession session = NSession.of();
        NIteratorBuilder.of(it)
                .map(x -> {
//                    if (x instanceof NDefinition) {
//                        return new NDefinitionDelegate() {
//                            @Override
//                            protected NDefinition getBase() {
//                                return (NDefinition) x;
//                            }
//
////                            @Override
////                            public NOptional<NInstallInformation> getInstallInformation() {
////                                return super.getInstallInformation();
////                            }
//
////                            @Override
////                            public NOptional<NDescriptor> getEffectiveDescriptor() {
////                                if (isEffective() /*|| isDependencies()*/) {
////                                    return super.getEffectiveDescriptor();
////                                }
////                                return NOptional.ofNamedEmpty("effectiveDescriptor");
////                            }
//
////                            @Override
////                            public NOptional<NPath> getContent() {
////                                if (isContent()) {
////                                    return super.getContent();
////                                }
////                                return NOptional.ofNamedEmpty("content");
////                            }
//
////                            @Override
////                            public NOptional<Set<NDescriptorFlag>> getEffectiveFlags() {
////                                if (isContent()) {
////                                    return super.getEffectiveFlags();
////                                }
////                                return NOptional.ofNamedEmpty("effectiveFlags");
////                            }
//
////                            @Override
////                            public NOptional<NDependencies> getDependencies() {
////                                if (/*isDependencies() || */isInlineDependencies()) {
////                                    return super.getDependencies();
////                                }
////                                return NOptional.ofNamedEmpty("dependencies");
////                            }
//                        }.builder().build();
//                    }
                    return x;
                });

        if (session.isDry()) {
            displayDryQueryPlan(it);
        } else {
            it = NWorkspaceUtils.of().decoratePrint(it, getDisplayOptions());
            long count=0;
            while (it.hasNext()) {
                it.next();
                count++;
            }
            NErr.resetLine();
            if(count==0){
                throw new NExecutionException(NMsg.ofC("No results found."),1 );
            }
        }
        return this;
    }

    private NElement toQueryPlan(NIterator it) {
        return
                NElement.ofObjectBuilder()
                        .set("SearchQueryPlan",
                                NDescribableElementSupplier.describeResolveOrDestruct(it))
                        .build();
    }

    private void displayDryQueryPlan(NIterator it) {
        NElement n = toQueryPlan(it);
        NSession session = NSession.of();
        NContentType f = session.getOutputFormat().orDefault();
        if (f == NContentType.PLAIN) {
            f = NContentType.TREE;
        }
        NSession session2 = session.copy().setOutputFormat(f);
        session2.out().resetLine().println(n);
    }

    private NDefinition loadedIdToDefinition(NId next) {
        NFetchCmd fetch = toFetch();
        NEnvCondition condition = next.getCondition();
        NDependency dep = next.toDependency();
        NDefinition d = null;
        try {
            d = fetch.setId(next).getResultDefinition();
        } catch (NNotFoundException e) {
            if (dep.isOptional()) {
                return null;
            }
        }
        if (d == null) {
            if (isFailFast()) {
                throw new NNotFoundException(next);
            }
            return d;
        }
        if (!NBlankable.isBlank(d) && !NBlankable.isBlank(condition)) {
            DefaultNDefinitionBuilder2 db = new DefaultNDefinitionBuilder2(d);
            db.setDependency(new ValueSupplier<>(dep));
            if (false) {
                //TODO fix me later,
                // We need to to apply this "AND" op when needed and not here !!
                db.setDescriptor(
                        () -> {
                            NDescriptor oldDesc = db.getDescriptor().get();
                            NDescriptor newdesc = oldDesc.builder().setCondition(
                                    oldDesc.getCondition().builder().and(condition).build()
                            ).build();
                            return newdesc;
                        }

                );
                db.setEffectiveDescriptor(
                        () -> {
                            NDescriptor oldDesc = db.getEffectiveDescriptor().get();
                            NDescriptor newdesc = oldDesc.builder().setCondition(
                                    oldDesc.getCondition().builder().and(condition).build()
                            ).build();
                            return newdesc;
                        }
                );
            }
            d = db.build();
        }
        return d;
    }

    public NIterator<NDefinition> getResultDefinitionIteratorBase() {
        return NIteratorBuilder.of(getResultIdIteratorBase(null))
                .map(NFunction.of((NId next) -> loadedIdToDefinition(next)).redescribe(NDescribableElementSupplier.of("Id->Definition")))
                .notNull().build();
    }

    protected <T> NStream<T> buildCollectionResult(NIterator<T> o) {
        return new NStreamFromNIterator<T>(resolveFindIdBase(), o);
    }

    protected String resolveFindIdBase() {
        return ids.isEmpty() ? null : ids.get(0) == null ? null : ids.get(0).toString();
    }

    public NStream<String> getResultStatuses() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(NFunction.of(
                                x -> NIdFormatHelper.of(x)
                                        .buildLong().getStatusString())
                        .redescribe(NDescribableElementSupplier.of("getStatusString"))
                )
                .notBlank());
    }

    protected abstract NIterator<NId> getResultIdIteratorBase(Boolean forceInlineDependencies);

    protected NStream<NId> getResultIdsBase(boolean sort) {
        return buildCollectionResult(getResultIdIteratorBase(null));
    }

    protected <T> NStream<T> postProcessResult(NIteratorBuilder<T> a) {
        if (isSorted()) {
            a = a.sort(null, isDistinct());
        }
        return buildCollectionResult(a.build());
    }

    protected NSession getSearchSession() {
        NSession session = NSession.of();
        return session;
    }

    protected NIterator<NId> applyPrintDecoratorIterOfNutsId(NIterator<NId> curr, boolean print) {
        return print ? NWorkspaceUtils.of().decoratePrint(curr, getDisplayOptions()) : curr;
    }
}
