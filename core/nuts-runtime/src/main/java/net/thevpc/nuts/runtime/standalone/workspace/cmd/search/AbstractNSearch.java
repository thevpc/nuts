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


import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.*;
import net.thevpc.nuts.core.NRepositoryFilters;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NStoreKey;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NErr;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinitionBuilder2;
import net.thevpc.nuts.runtime.standalone.definition.NDefinitionFilterUtils;
import net.thevpc.nuts.runtime.standalone.dependency.util.NClassLoaderUtils;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNClassLoader;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNExtensions;
import net.thevpc.nuts.runtime.standalone.format.NDisplayProperty;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.NIdFormatHelper;
import net.thevpc.nuts.runtime.standalone.util.ValueSupplier;
import net.thevpc.nuts.runtime.standalone.util.stream.NStreamBase;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.DefaultNQueryBaseOptions;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public abstract class AbstractNSearch extends DefaultNQueryBaseOptions<NSearch> implements NSearch {

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

    public AbstractNSearch() {
        super("search");
    }

    @Override
    public boolean isIgnoreCurrentEnvironment() {
        return ignoreCurrentEnvironment;
    }

    @Override
    public NSearch ignoreCurrentEnvironment(boolean ignoreCurrentEnvironment) {
        this.ignoreCurrentEnvironment = ignoreCurrentEnvironment;
        return this;
    }

    @Override
    public NSearch clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public NSearch addId(String id) {
        if (!NBlankable.isBlank(id)) {
            ids.add(NId.get(id).get());
        }
        return this;
    }

    @Override
    public NSearch addId(NId id) {
        if (id != null) {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NSearch addIds(String... values) {
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
    public NSearch addIds(NId... value) {
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
    public NSearch addIds(List<NId> value) {
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
    public NSearch removeId(String id) {
        ids.remove(NId.get(id).get());
        return this;
    }

    @Override
    public NSearch removeId(NId id) {
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
    public NSearch runtime(boolean enable) {
        this.execType = enable ? SearchExecType.RUNTIME : null;
        return this;
    }

    @Override
    public boolean isCompanion() {
        return execType == SearchExecType.COMPANION;
    }

    @Override
    public NSearch companion(boolean enable) {
        this.execType = enable ? SearchExecType.COMPANION : null;
        return this;
    }

    @Override
    public boolean isExtension() {
        return SearchExecType.EXTENSION == execType;
    }

    @Override
    public NSearch extension(boolean enable) {
        this.execType = enable ? SearchExecType.EXTENSION : null;
        return this;
    }

    @Override
    public boolean isExecutable() {
        return SearchExecType.EXEC == execType;
    }

    @Override
    public NSearch executable(boolean enable) {
        this.execType = enable ? SearchExecType.EXEC : null;
        return this;
    }

    @Override
    public boolean isNutsApplication() {
        return SearchExecType.NUTS_APPLICATION == execType;
    }

    @Override
    public NSearch nutsApplication(boolean enable) {
        this.execType = enable ? SearchExecType.NUTS_APPLICATION : null;
        return this;
    }

    @Override
    public boolean isPlatformApplication() {
        return SearchExecType.PLATFORM_APPLICATION == execType;
    }

    @Override
    public NSearch platformApplication(boolean enable) {
        this.execType = enable ? SearchExecType.PLATFORM_APPLICATION : null;
        return this;
    }

    @Override
    public boolean isLib() {
        return SearchExecType.LIB == execType;
    }

    @Override
    public NSearch setLib(boolean enable) {
        this.execType = enable ? SearchExecType.LIB : null;
        return this;
    }


    @Override
    public NSearch sort(Comparator<?> comparator) {
        this.comparator = NComparator.of(comparator);
        this.sorted = true;
        return this;
    }

    @Override
    public NSearch copyFrom(NSearch other) {
        super.copyFromDefaultNQueryBaseOptions((DefaultNQueryBaseOptions) other);
        if (other != null) {
            NSearch o = other;
            this.ignoreCurrentEnvironment = o.isIgnoreCurrentEnvironment();
            this.comparator = o.comparator();
            this.definitionFilter = o.definitionFilter();
            this.latest = o.isLatest();
            this.distinct = (o.isDistinct());
            this.includeBasePackage = o.isBasePackage();
            this.sorted = o.isSorted();
            this.ids.clear();
            this.ids.addAll(o.ids());
        }
        return this;
    }

    @Override
    public NSearch copyFrom(NFetch other) {
        super.copyFromDefaultNQueryBaseOptions((DefaultNQueryBaseOptions) other);
        return this;
    }

    @Override
    public List<NId> ids() {
        return this.ids;
    }

    @Override
    public NSearch ids(String... ids) {
        clearIds();
        addIds(ids);
        return this;
    }

    @Override
    public NSearch ids(NId... ids) {
        clearIds();
        addIds(ids);
        return this;
    }

    @Override
    public NSearch ids(List<NId> ids) {
        clearIds();
        addIds(ids);
        return this;
    }

    @Override
    public boolean isSorted() {
        return sorted;
    }

    @Override
    public NSearch sorted(boolean sort) {
        this.sorted = sort;
        return this;
    }

    @Override
    public NDefinitionFilter definitionFilter() {
        return definitionFilter;
    }

    @Override
    public NSearch definitionFilter(NDefinitionFilter filter) {
        this.definitionFilter = filter;
        return this;
    }

    @Override
    public NSearch addDefinitionFilter(NDefinitionFilter filter) {
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
    public NComparator comparator() {
        return comparator;
    }

    @Override
    public boolean isDistinct() {
        return distinct;
    }

    @Override
    public NSearch distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }


    @Override
    public NVersion targetApiVersion() {
        return targetApiVersion;
    }

    @Override
    public NSearch targetApiVersion(NVersion targetApiVersion) {
        this.targetApiVersion = targetApiVersion;
        return this;
    }

    @Override
    public boolean isBasePackage() {
        return includeBasePackage;
    }

    @Override
    public NSearch basePackage(boolean includeBasePackage) {
        this.includeBasePackage = includeBasePackage;
        return this;
    }

    @Override
    public boolean isLatest() {
        return latest;
    }

    @Override
    public NSearch latest(boolean enable) {
        this.latest = enable;
        return this;
    }

    @Override
    public NStream<NId> getResultIds() {
        return buildCollectionResult(getResultIdIteratorBase(null));
    }

    @Override
    public NStream<NDependencies> getResultDependencies() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(NFunction.of((NDefinition x) -> x.dependencies().get())
                        .withDescription(NDescribables.ofDesc("getDependencies")))
        );
    }

    @Override
    public NStream<NDependency> getResultInlineDependencies() {
        return buildCollectionResult(
                NIteratorBuilder.of(getResultIdIteratorBase(true)).map(
                                NFunction.of(NId::toDependency)
                                        .withDescription(NDescribables.ofDesc("Id->Dependency")))
                        .distinct(x->{
                            //always distinct by id and repo
                            NId _id = x.toId();
                            return _id.longName()+":"+NStringUtils.strip(_id.repository());
                        })
                        .build()
        );
    }

    @Override
    public NStream<NDefinition> getResultDefinitions() {
        return buildCollectionResult(getResultDefinitionIteratorBase());
    }

    @Override
    public NStream<NDescriptor> getResultDescriptors() {
        return getResultDefinitions().map(NDefinition::descriptor);
    }

    @Override
    public NClassLoader getResultClassLoader() {
        return getResultClassLoader(null);
    }

    @Override
    public NClassLoader getResultClassLoader(ClassLoader parent) {
        //force content and dependencies!
//        setContent(true);
//        setDependencies(true);

        List<NDefinition> nDefinitions = getResultDefinitions().toList();
        URL[] allURLs = new URL[nDefinitions.size()];
        NId[] allIds = new NId[nDefinitions.size()];
        for (int i = 0; i < allURLs.length; i++) {
            NDefinition d = nDefinitions.get(i);
            allURLs[i] = d.content().flatMap(NPath::toURL).orNull();
            allIds[i] = d.id();
        }
        DefaultNClassLoader cl = ((DefaultNExtensions) NExtensions.of())
                .getModel().getNutsURLClassLoader("SEARCH-" + UUID.randomUUID(), parent);
        for (NDefinition def : nDefinitions) {
            cl.add(NClassLoaderUtils.definitionToClassLoaderNode(def, repositoryFilter()));
        }
        return cl;
    }

    @Override
    public NClassLoader getResultIntoClassLoader(NClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = ((DefaultNExtensions) NExtensions.of())
                    .getModel().getNutsURLClassLoader("SEARCH-" + UUID.randomUUID(), null);
        }

        List<NDefinition> nDefinitions = getResultDefinitions().toList();
        URL[] allURLs = new URL[nDefinitions.size()];
        NId[] allIds = new NId[nDefinitions.size()];
        for (int i = 0; i < allURLs.length; i++) {
            NDefinition d = nDefinitions.get(i);
            allURLs[i] = d.content().flatMap(NPath::toURL).orNull();
            allIds[i] = d.id();
        }

        for (NDefinition def : nDefinitions) {
            classLoader.add(NClassLoaderUtils.definitionToClassLoaderNode(def, repositoryFilter()));
        }
        return classLoader;
    }

    @Override
    public String getResultNutsPath() {
        return getResultIds().toList().stream().map(NId::longName).collect(Collectors.joining(";"));
    }

    @Override
    public String getResultClassPath() {
        StringBuilder sb = new StringBuilder();
        NIterator<NDefinition> it = getResultDefinitionIteratorBase();
        while (it.hasNext()) {
            NDefinition nDefinition = it.next();
            if (nDefinition.content().isPresent()) {
                if (sb.length() > 0) {
                    sb.append(File.pathSeparator);
                }
                sb.append(nDefinition.content().orNull());
            }
        }
        return sb.toString();
    }

    @Override
    public NStream<String> getResultPaths() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(
                        NFunction.of((NDefinition x) -> x.content().map(Object::toString).orNull())
                                .withDescription(NDescribables.ofDesc("getPath"))
                )
                .notBlank()
        );
    }

    @Override
    public NStream<String> getResultPathNames() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(NFunction.of((NDefinition x) -> x.content().map(NPath::name).orNull())
                        .withDescription(NDescribables.ofDesc("getName")))
                .notBlank());
    }

    @Override
    public NStream<Instant> getResultInstallDates() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(NFunction.of((NDefinition x) -> x.installInformation().map(NInstallInformation::createdInstant).orNull()).withDescription(NDescribables.ofDesc("getCreatedInstant")))
                .notNull());
    }

    @Override
    public NStream<String> getResultInstallUsers() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(NFunction.of((NDefinition x) -> x.installInformation().map(NInstallInformation::installUser).orNull()).withDescription(NDescribables.ofDesc("getInstallUser")))
                .notBlank());
    }

    @Override
    public NStream<NPath> getResultInstallFolders() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(NFunction.of((NDefinition x) -> x.installInformation().map(NInstallInformation::installFolder).orNull())
                        .withDescription(NDescribables.ofDesc("getInstallFolder"))
                )
                .notNull());
    }

    @Override
    public NStream<NPath> getResultStoreLocations(NStoreType storeType) {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(NFunction.of((NDefinition x) -> NPath.of(NStoreKey.of(x.id()).type(storeType)))
                        .withDescription(NDescribables.ofDesc("getStoreLocation(" + storeType.id() + ")"))
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
                        ).withDescription(NDescribables.ofDesc("getColumns"))
                ));
    }

    @Override
    public NStream<String> getResultNames() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.descriptor().name()))
                        .withDescription(NDescribables.ofDesc("getDescriptorName"))
                )
                .notBlank());
    }

    @Override
    public NStream<String> getResultOs() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.descriptor().condition().os()))
                        .withDescription(NDescribables.ofDesc("getOs")))
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
                                (NDefinition x) -> x.content().map(NExecutionEntry::parse).orElse(Collections.emptyList())
                        ).withDescription(NDescribables.ofDesc("getFile"))
                ));
    }

    @Override
    public NStream<String> getResultOsDist() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.descriptor().condition().osDist()))
                        .withDescription(NDescribables.ofDesc("getOsDist"))
                )
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultPackaging() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.descriptor().packaging()))
                        .withDescription(NDescribables.ofDesc("getPackaging"))
                )
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultPlatform() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.descriptor().condition().platform()))
                        .withDescription(NDescribables.ofDesc("getPlatform"))
                )
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultProfile() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.descriptor().condition().profiles()))
                        .withDescription(NDescribables.ofDesc("getProfile"))
                )
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultDesktopEnvironment() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.descriptor().condition().desktopEnvironment()))
                        .withDescription(NDescribables.ofDesc("getDesktopEnvironment"))
                )
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultArch() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .mapMulti(NFunction.of((NDefinition x) -> Arrays.asList(x.descriptor().condition().arch()))
                        .withDescription(NDescribables.ofDesc("getArch"))
                )
                .notBlank());
    }

    @Override
    public NSearch id(String id) {
        clearIds();
        addId(id);
        return this;
    }

    @Override
    public NSearch id(NId id) {
        clearIds();
        addId(id);
        return this;
    }

    @NGetter
    public SearchExecType execType() {
        return execType;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isUncommented();
        switch (a.key()) {
            case "--inline-dependencies": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.inlineDependencies(v.booleanValue())).anyMatch();
            }
            case "--describe": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.describe = v.booleanValue()).anyMatch();
            }
            case "-L":
            case "--latest":
            case "--latest-versions": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.latest(v.booleanValue())).anyMatch();
            }
            case "--repo": {
                return cmdLine.matcher().withAny().matchEntry((v) -> this.repositoryFilter(NRepositoryFilters.of().bySelector(NStringUtils.split(v.stringValue(), ";,|", true, true).toArray(new String[0])))).anyMatch();
            }
            case "--distinct": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.distinct(v.booleanValue())).anyMatch();
            }
            case "--default":
            case "--default-versions": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.addDefinitionFilter(NDefinitionFilters.of().byDefaultVersion(v.getBooleanValue().onError(false).orElse(null)))).anyMatch();
            }
            case "--duplicates": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.distinct(!v.booleanValue())).anyMatch();
            }
            case "-s":
            case "--sort": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.sorted(v.booleanValue())).anyMatch();
            }
            case "--base": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.includeBasePackage = v.booleanValue()).anyMatch();
            }
            case "--lib":
            case "--libs": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.setLib(v.booleanValue())).anyMatch();
            }
            case "--app":
            case "--apps": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.executable(v.booleanValue())).anyMatch();
            }
            case "--companion":
            case "--companions": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.companion(v.booleanValue())).anyMatch();
            }
            case "--extension":
            case "--extensions": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.extension(v.booleanValue())).anyMatch();
            }
            case "--runtime": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.runtime(v.booleanValue())).anyMatch();
            }
            case "--api-version": {
                return cmdLine.matcher().withAny().matchEntry((v) -> this.targetApiVersion(NVersion.get(v.stringValue()).get())).anyMatch();
            }
            case "--nuts-app":
            case "--nuts-apps": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.nutsApplication(v.booleanValue())).anyMatch();
            }
            case "--arch": {
                return cmdLine.matcher().withAny().matchEntry((v) -> this.addDefinitionFilter(
                        NDefinitionFilters.of().nonnull(this.definitionFilter()).and(NDefinitionFilters.of().byArch(v.stringValue()))
                )).anyMatch();
            }
            case "--packaging": {
                return cmdLine.matcher().withAny().matchEntry((v) -> this.addDefinitionFilter(
                        NDefinitionFilters.of().nonnull(this.definitionFilter()).and(NDefinitionFilters.of().byPackaging(v.stringValue()))
                )).anyMatch();
            }
            case "--id": {
                return cmdLine.matcher().withAny().matchEntry((v) -> this.addId(v.stringValue())).anyMatch();
            }
            case "--locked-id": {
                return cmdLine.matcher().withAny().matchEntry((v) -> definitionFilter(NDefinitionFilterUtils.addLockedIds(definitionFilter(), NId.of(v.stringValue())))).anyMatch();
            }
            case "--deployed": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.addDefinitionFilter(NDefinitionFilters.of().byDeployed(a.booleanValue()).and(definitionFilter()))).anyMatch();
            }
            case "-i":
            case "--installed": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.addDefinitionFilter(NDefinitionFilters.of().byInstalled(a.booleanValue()).and(definitionFilter()))).anyMatch();
            }
            case "--required": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.addDefinitionFilter(NDefinitionFilters.of().byRequired(a.booleanValue()).and(definitionFilter()))).anyMatch();
            }
            case "--obsolete": {
                return cmdLine.matcher().withAny().matchFlag((v) -> this.addDefinitionFilter(NDefinitionFilters.of().byObsolete(a.booleanValue()).and(definitionFilter()))).anyMatch();
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
                + ", comparator=" + comparator()
                + ", dependencyFilter=" + dependencyFilter()
                + ", descriptorFilter=" + definitionFilter()
                + ", repositoryFilter=" + repositoryFilter()
                + ", latest=" + isLatest()
                + ", distinct=" + isDistinct()
                + ", includeMain=" + isBasePackage()
                + ", sorted=" + isSorted()
                + ", ids=" + ids()
                + ", execType='" + execType() + '\''
                + ", targetApiVersion='" + targetApiVersion() + '\''
                + '}';
    }

    private Object dependenciesToElement(NDependencyTreeNode d) {
        NId id
                = //                getSearchSession().getWorkspace().text().parse(d.getDependency().formatter().setSession(getSearchSession()).setNtf(false).format())
                d.dependency().toId();
        if (d.isPartial()) {
            id = id.builder().setProperty("partial", "true").build();
        }
        List<Object> li = d.children().stream().map(x -> dependenciesToElement(x)).collect(Collectors.toList());
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
        if (isInlineDependencies()) {
            NContentType of = getSearchSession().outputFormat().orDefault();
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
                            .flatMap(NFunction.of((NDefinition x) -> x.dependencies().get().transitiveNodes().iterator())
                                    .withDescription(NDescribables.ofDesc("getDependencies"))
                            )
                            .distinct(x->{
                                //always distinct by id and repo
                                NId _id = x.dependency().toId();
                                return _id.longName()+":"+NStringUtils.strip(_id.repository());
                            })
                            .map(NFunction.of((NDependencyTreeNode x) -> dependenciesToElement(x))
                                    .withDescription(NDescribables.ofDesc("dependenciesToElement"))
                            )
                            .build();
                }

                default: {
                    NStream<NDependency> rr = getResultInlineDependencies();
                    return (NIterator) rr.map(x->loadedIdToDefinition(x.toId())).iterator();
//                    return (NIterator) rr.iterator();
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
    public NSearch run() {
        if (describe) {
            NElementWriter.ofTson().formatter(NElementFormatterStyle.PRETTY).writeln(getResultQueryPlan());
//            NOut.println(getResultQueryPlan().format(NContentType.JSON, NElementFormatter.ofPretty()));
            return this;
        }
        NIterator<Object> it = runIterator();
        NSession session = NSession.of();
        NFetchDisplayOptions displayOptions = getDisplayOptions();
        boolean requireDef=false;
        if(displayOptions!=null){
            for (NDisplayProperty p : displayOptions.getDisplayProperties()) {
                switch (p) {
                    case NAME:{
                        break;
                    }
                    case DESKTOP_ENVIRONMENT:
                    case EXEC_ENTRY:
                    case INSTALL_FOLDER:
                    case LIB_FOLDER:
                    case LONG_STATUS:
                    case VAR_LOCATION:
                    case OS:
                    case CONF_FOLDER:
                    case FILE_NAME:
                    case INSTALL_DATE:
                    case INSTALL_USER:
                    case FILE:
                    case OSDIST:
                    case PROFILE:
                    case REPOSITORY:
                    case REPOSITORY_ID:
                    case LOG_FOLDER:
                    case ARCH:
                    case PLATFORM:
                    case PACKAGING:
                    case TEMP_FOLDER:
                    case STATUS:
                    case CACHE_FOLDER:
                    case ID:
                    case BIN_FOLDER:{
                        requireDef=true;
                        break;
                    }

                }
            }
        }
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
            it = NWorkspaceUtils.of().decoratePrint(it, displayOptions);
            long count = 0;
            while (it.hasNext()) {
                it.next();
                count++;
            }
            NErr.resetLine();
            if (count == 0) {
                throw new NExecutionException(NMsg.ofC("No results found."), 1);
            }
        }
        return this;
    }

    private NElement toQueryPlan(NIterator it) {
        return
                NElement.ofObjectBuilder()
                        .set("SearchQueryPlan",
                                NDescribables.describeResolveOrSimplify(it))
                        .build();
    }

    private void displayDryQueryPlan(NIterator it) {
        NElement n = toQueryPlan(it);
        NSession session = NSession.of();
        NContentType f = session.outputFormat().orDefault();
        if (f == NContentType.PLAIN) {
            f = NContentType.TREE;
        }
        NSession session2 = session.copy().outputFormat(f);
        session2.out().println(n);
    }

    private NDefinition loadedIdToDefinition(NId next) {
        NFetch fetch = toFetch();
        NEnvCondition condition = next.condition();
        NDependency dep = next.toDependency();
        NDefinition d = null;
        try {
            d = fetch.id(next).getResultDefinition();
        } catch (NArtifactNotFoundException e) {
            if (dep.isOptional()) {
                return null;
            }
        }
        if (d == null) {
            if (isFailFast()) {
                throw new NArtifactNotFoundException(next.longId());
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
                            NDescriptor newdesc = oldDesc.builder().condition(
                                    oldDesc.condition().builder().and(condition).build()
                            ).build();
                            return newdesc;
                        }

                );
                db.setEffectiveDescriptor(
                        () -> {
                            NDescriptor oldDesc = db.getEffectiveDescriptor().get();
                            NDescriptor newdesc = oldDesc.builder().condition(
                                    oldDesc.condition().builder().and(condition).build()
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
                .map(NFunction.of((NId next) -> loadedIdToDefinition(next)).withDescription(NDescribables.ofDesc("Id->Definition")))
                .notNull().build();
    }

    protected <T> NStream<T> buildCollectionResult(NIterator<T> o) {
        return NStreamBase.ofIterator(resolveFindIdBase(), o);
    }

    protected String resolveFindIdBase() {
        return ids.isEmpty() ? null : ids.get(0) == null ? null : ids.get(0).toString();
    }

    public NStream<String> getResultStatuses() {
        return postProcessResult(NIteratorBuilder.of(getResultDefinitionIteratorBase())
                .map(NFunction.of(
                                x -> NIdFormatHelper.of(x)
                                        .buildLong().getStatusString())
                        .withDescription(NDescribables.ofDesc("getStatusString"))
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
