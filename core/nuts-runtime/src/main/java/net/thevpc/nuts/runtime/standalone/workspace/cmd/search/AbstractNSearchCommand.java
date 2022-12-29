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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.search;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNClassLoader;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.DefaultNQueryBaseOptions;
import net.thevpc.nuts.runtime.standalone.format.NDisplayProperty;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.NIdFormatHelper;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNExtensions;
import net.thevpc.nuts.runtime.standalone.dependency.util.NClassLoaderUtils;
import net.thevpc.nuts.runtime.standalone.stream.NIteratorStream;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;

import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public abstract class AbstractNSearchCommand extends DefaultNQueryBaseOptions<NSearchCommand> implements NSearchCommand {

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

    public AbstractNSearchCommand(NSession ws) {
        super(ws, "search");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NSearchCommand clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public NSearchCommand addId(String id) {
        checkSession();
        if (!NBlankable.isBlank(id)) {
            ids.add(NId.of(id).get(getSession()));
        }
        return this;
    }

    @Override
    public NSearchCommand addId(NId id) {
        if (id != null) {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NSearchCommand addIds(String... values) {
        checkSession();
        if (values != null) {
            for (String s : values) {
                if (!NBlankable.isBlank(s)) {
                    ids.add(NId.of(s).get(getSession()));
                }
            }
        }
        return this;
    }

    @Override
    public NSearchCommand addIds(NId... value) {
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
    public NSearchCommand removeId(String id) {
        checkSession();
        ids.remove(NId.of(id).get(getSession()));
        return this;
    }

    @Override
    public NSearchCommand removeId(NId id) {
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
    public NSearchCommand setRuntime(boolean enable) {
        this.execType = enable ? "runtime" : null;
        return this;
    }

    @Override
    public boolean isCompanion() {
        return "companion".equals(execType);
    }

    @Override
    public NSearchCommand setCompanion(boolean enable) {
        this.execType = enable ? "companion" : null;
        return this;
    }

    @Override
    public boolean isExtension() {
        return "extension".equals(execType);
    }

    @Override
    public NSearchCommand setExtension(boolean enable) {
        this.execType = enable ? "extension" : null;
        return this;
    }

    @Override
    public boolean isExec() {
        return "exec".equals(execType);
    }

    @Override
    public NSearchCommand setExec(boolean enable) {
        this.execType = enable ? "exec" : null;
        return this;
    }

    @Override
    public boolean isApplication() {
        return "app".equals(execType);
    }

    @Override
    public NSearchCommand setApplication(boolean enable) {
        this.execType = enable ? "app" : null;
        return this;
    }

    @Override
    public boolean isLib() {
        return "lib".equals(execType);
    }

    @Override
    public NSearchCommand setLib(boolean enable) {
        this.execType = enable ? "lib" : null;
        return this;
    }

    @Override
    public NSearchCommand addScript(String value) {
        if (value != null) {
            scripts.add(value);
        }
        return this;
    }

    @Override
    public NSearchCommand removeScript(String value) {
        scripts.remove(value);
        return this;
    }

    @Override
    public NSearchCommand addScripts(Collection<String> value) {
        if (value != null) {
            addScripts(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NSearchCommand addScripts(String... value) {
        if (value != null) {
            scripts.addAll(Arrays.asList(value));
        }
        return this;

    }

    @Override
    public NSearchCommand clearScripts() {
        scripts.clear();
        return this;
    }

    @Override
    public List<String> getScripts() {
        return scripts;
    }

    @Override
    public NSearchCommand clearArch() {
        this.arch.clear();
        return this;
    }

    @Override
    public NSearchCommand addLockedIds(String... values) {
        checkSession();
        if (values != null) {
            for (String s : values) {
                if (!NBlankable.isBlank(s)) {
                    lockedIds.add(NId.of(s).get(getSession()));
                }
            }
        }
        return this;
    }

    @Override
    public NSearchCommand addLockedIds(List<NId> values) {
        return addLockedIds(values.toArray(new NId[0]));
    }

    @Override
    public NSearchCommand addLockedIds(NId... values) {
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
    public NSearchCommand clearLockedIds() {
        lockedIds.clear();
        return this;
    }

    @Override
    public NSearchCommand addArch(String value) {
        if (!NBlankable.isBlank(value)) {
            this.arch.add(value);
        }
        return this;
    }

    @Override
    public NSearchCommand removeArch(String value) {
        this.arch.remove(value);
        return this;
    }

    @Override
    public NSearchCommand addArch(Collection<String> values) {
        if (values != null) {
            addArch(values.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NSearchCommand addArch(String... values) {
        if (values != null) {
            arch.addAll(Arrays.asList(values));
        }
        return this;
    }

    @Override
    public NSearchCommand clearPackaging() {
        packaging.clear();
        return this;
    }

    @Override
    public NSearchCommand addPackaging(Collection<String> values) {
        if (values != null) {
            addPackaging(values.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NSearchCommand addPackaging(String... values) {
        if (values != null) {
            this.packaging.addAll(Arrays.asList(values));
        }
        return this;
    }

    @Override
    public NSearchCommand addPackaging(String value) {
        if (value != null) {
            packaging.add(value);
        }
        return this;
    }

    @Override
    public NSearchCommand removePackaging(String value) {
        packaging.remove(value);
        return this;
    }

    @Override
    public NSearchCommand addLockedId(NId id) {
        if (id != null) {
            addLockedId(id.toString());
        }
        return this;
    }

    @Override
    public NSearchCommand removeLockedId(NId id) {
        if (id != null) {
            removeLockedId(id.toString());
        }
        return this;
    }

    @Override
    public NSearchCommand removeLockedId(String id) {
        checkSession();
        lockedIds.remove(NId.of(id).get(getSession()));
        return this;
    }

    @Override
    public NSearchCommand addLockedId(String id) {
        checkSession();
        if (!NBlankable.isBlank(id)) {
            lockedIds.add(NId.of(id).get(getSession()));
        }
        return this;
    }

    @Override
    public List<NId> getLockedIds() {
        return this.lockedIds;
    }


    @Override
    public NSearchCommand sort(NComparator comparator) {
        this.comparator = comparator;
        this.sorted = true;
        return this;
    }

    @Override
    public NSearchCommand setAll(NSearchCommand other) {
        super.copyFromDefaultNQueryBaseOptions((DefaultNQueryBaseOptions) other);
        if (other != null) {
            NSearchCommand o = other;
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
    public NSearchCommand setAll(NFetchCommand other) {
        super.copyFromDefaultNQueryBaseOptions((DefaultNQueryBaseOptions) other);
        return this;
    }

    @Override
    public List<NId> getIds() {
        return this.ids;
    }

    @Override
    public NSearchCommand setIds(String... ids) {
        clearIds();
        addIds(ids);
        return this;
    }

    @Override
    public NSearchCommand setIds(NId... ids) {
        clearIds();
        addIds(ids);
        return this;
    }

    @Override
    public boolean isSorted() {
        return sorted;
    }

    @Override
    public NSearchCommand setSorted(boolean sort) {
        this.sorted = sort;
        return this;
    }

    @Override
    public NDescriptorFilter getDescriptorFilter() {
        return descriptorFilter;
    }

    @Override
    public NSearchCommand setDescriptorFilter(NDescriptorFilter filter) {
        this.descriptorFilter = filter;
        return this;
    }

    @Override
    public NSearchCommand setDescriptorFilter(String filter) {
        checkSession();
        this.descriptorFilter = NDescriptorFilters.of(session).parse(filter);
        return this;
    }

    @Override
    public NIdFilter getIdFilter() {
        return idFilter;
    }

    @Override
    public NSearchCommand setIdFilter(NIdFilter filter) {
        this.idFilter = filter;
        return this;
    }

    @Override
    public NSearchCommand setIdFilter(String filter) {
        checkSession();
        this.idFilter = NIdFilters.of(getSession()).parse(filter);
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
    public NSearchCommand setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    @Override
    public NVersion getTargetApiVersion() {
        return targetApiVersion;
    }

    @Override
    public NSearchCommand setTargetApiVersion(NVersion targetApiVersion) {
        this.targetApiVersion = targetApiVersion;
        return this;
    }

    @Override
    public boolean isBasePackage() {
        return includeBasePackage;
    }

    @Override
    public NSearchCommand setBasePackage(boolean includeBasePackage) {
        this.includeBasePackage = includeBasePackage;
        return this;
    }

    @Override
    public boolean isLatest() {
        return latest;
    }

    @Override
    public NSearchCommand setLatest(boolean enable) {
        this.latest = enable;
        return this;
    }

    @Override
    public NStream<NId> getResultIds() {
        return buildCollectionResult(getResultIdIteratorBase(null));
    }

    @Override
    public NStream<NDependencies> getResultDependencies() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(true, isEffective()), session)
                .map(NFunction.of(x -> x.getDependencies().get(session), "getDependencies"))
        );
    }

    @Override
    public NStream<NDependency> getResultInlineDependencies() {
        return buildCollectionResult(
                IteratorBuilder.of(getResultIdIteratorBase(true), session).map(
                                NFunction.of(NId::toDependency, "Id->Dependency"))
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
        checkSession();

        //force content and dependencies!
        setContent(true);
        setDependencies(true);

        List<NDefinition> nDefinitions = getResultDefinitions().toList();
        URL[] allURLs = new URL[nDefinitions.size()];
        NId[] allIds = new NId[nDefinitions.size()];
        for (int i = 0; i < allURLs.length; i++) {
            NDefinition d = nDefinitions.get(i);
            allURLs[i] = d.getContent().map(NPath::asURL).orNull();
            allIds[i] = d.getId();
        }
        DefaultNClassLoader cl = ((DefaultNExtensions) getSession().extensions())
                .getModel().getNutsURLClassLoader("SEARCH-" + UUID.randomUUID(), parent, getSession());
        for (NDefinition def : nDefinitions) {
            cl.add(NClassLoaderUtils.definitionToClassLoaderNode(def, getSession()));
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
    public NSearchCommand setDefaultVersions(Boolean acceptDefaultVersion) {
        this.defaultVersions = acceptDefaultVersion;
        return this;
    }

    @Override
    public NStream<String> getResultPaths() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(true, isEffective()), session)
                .map(
                        NFunction.of(x -> x.getContent().map(Object::toString).orNull(), "getPath")
                )
                .notBlank()
        );
    }

    @Override
    public NStream<String> getResultPathNames() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(true, isEffective()), session)
                .map(NFunction.of(x -> x.getContent().map(NPath::getName).orNull(), "getName"))
                .notBlank());
    }

    @Override
    public NStream<Instant> getResultInstallDates() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .map(NFunction.of(x -> x.getInstallInformation().map(NInstallInformation::getCreatedInstant).orNull(), "getCreatedInstant"))
                .notNull());
    }

    @Override
    public NStream<String> getResultInstallUsers() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .map(NFunction.of(x -> x.getInstallInformation().map(NInstallInformation::getInstallUser).orNull(), "getInstallUser"))
                .notBlank());
    }

    @Override
    public NStream<NPath> getResultInstallFolders() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .map(NFunction.of(x -> x.getInstallInformation().map(NInstallInformation::getInstallFolder).orNull(), "getInstallFolder"))
                .notNull());
    }

    @Override
    public NStream<NPath> getResultStoreLocations(NStoreLocation location) {
        checkSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .map(NFunction.of(x -> getSession().locations().getStoreLocation(x.getId(), location), "getStoreLocation(" + location.id() + ")"))
                .notNull());
    }

    @Override
    public NStream<String[]> getResultStrings(String[] columns) {
        NFetchDisplayOptions oo = new NFetchDisplayOptions(getSession());
        oo.addDisplay(columns);
        oo.setIdFormat(getDisplayOptions().getIdFormat());
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .map(NFunction.of(x
                                -> NIdFormatHelper.of(x, getSearchSession())
                                .buildLong().getMultiColumnRowStrings(oo),
                        "getColumns")
                ));
    }

    @Override
    public NStream<String> getResultNames() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NFunction.of(x -> Arrays.asList(x.getDescriptor().getName()), "getDescriptorName"))
                .notBlank());
    }

    @Override
    public NStream<String> getResultOs() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NFunction.of(x -> Arrays.asList(x.getDescriptor().getCondition().getOs()), "getOs"))
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<NExecutionEntry> getResultExecutionEntries() {
        checkSession();
        IteratorBuilder<NDefinition> defIter = IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session);
        return postProcessResult(defIter
                .mapMulti(
                        NFunction.of(
                                x -> x.getContent().map(y -> NExecutionEntries.of(getSession()).parse(y)).orElse(Collections.emptyList()),
                                "getFile"
                        )));
    }

    @Override
    public NStream<String> getResultOsDist() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NFunction.of(x -> Arrays.asList(x.getDescriptor().getCondition().getOsDist()), "getOsDist"))
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultPackaging() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NFunction.of(x -> Arrays.asList(x.getDescriptor().getPackaging()), "getPackaging"))
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultPlatform() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NFunction.of(x -> Arrays.asList(x.getDescriptor().getCondition().getPlatform()), "getPlatform"))
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultProfile() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NFunction.of(x -> Arrays.asList(x.getDescriptor().getCondition().getProfile()), "getProfile"))
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultDesktopEnvironment() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NFunction.of(x -> Arrays.asList(x.getDescriptor().getCondition().getDesktopEnvironment()), "getDesktopEnvironment"))
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NStream<String> getResultArch() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NFunction.of(x -> Arrays.asList(x.getDescriptor().getCondition().getArch()), "getArch"))
                .notBlank());
    }

    @Override
    public NInstallStatusFilter getInstallStatus() {
        return installStatus;
    }

    @Override
    public NSearchCommand setInstallStatus(NInstallStatusFilter installStatus) {
        this.installStatus = installStatus;
        return this;
    }

    @Override
    public NSearchCommand setId(String id) {
        clearIds();
        addId(id);
        return this;
    }

    @Override
    public NSearchCommand setId(NId id) {
        clearIds();
        addId(id);
        return this;
    }

    public String getExecType() {
        return execType;
    }

    @Override
    public boolean configureFirst(NCommandLine cmdLine) {
        NArgument a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch (a.key()) {
            case "--inline-dependencies": {
                cmdLine.withNextBoolean((v, r, s) -> this.setInlineDependencies(v));
                return true;
            }
            case "-L":
            case "--latest":
            case "--latest-versions": {
                cmdLine.withNextBoolean((v, r, s) -> this.setLatest(v));
                return true;
            }
            case "--distinct": {
                cmdLine.withNextBoolean((v, r, s) -> this.setDistinct(v));
                return true;
            }
            case "--default":
            case "--default-versions": {
                cmdLine.withNextOptionalBoolean((v, r, s) -> this.setDefaultVersions(v.ifError(false).orElse(null)));
                return true;
            }
            case "--duplicates": {
                cmdLine.withNextBoolean((v, r, s) -> this.setDistinct(!v));
                return true;
            }
            case "-s":
            case "--sort": {
                cmdLine.withNextBoolean((v, r, s) -> this.setSorted(v));
                return true;
            }
            case "--base": {
                cmdLine.withNextBoolean((v, r, s) -> this.includeBasePackage = v);
                return true;
            }
            case "--lib":
            case "--libs": {
                cmdLine.withNextBoolean((v, r, s) -> this.setLib(v));
                return true;
            }
            case "--app":
            case "--apps": {
                cmdLine.withNextBoolean((v, r, s) -> this.setExec(v));
                return true;
            }
            case "--companion":
            case "--companions": {
                cmdLine.withNextBoolean((v, r, s) -> this.setCompanion(v));
                return true;
            }
            case "--extension":
            case "--extensions": {
                cmdLine.withNextBoolean((v, r, s) -> this.setExtension(v));
                return true;
            }
            case "--runtime": {
                cmdLine.withNextBoolean((v, r, s) -> this.setRuntime(v));
                return true;
            }
            case "--api-version": {
                cmdLine.withNextString((v, r, s) -> this.setTargetApiVersion(NVersion.of(v).get(getSession())));
                return true;
            }
            case "--nuts-app":
            case "--nuts-apps": {
                cmdLine.withNextBoolean((v, r, s) -> this.setApplication(v));
                return true;
            }
            case "--arch": {
                cmdLine.withNextString((v, r, s) -> this.addArch(v));
                return true;
            }
            case "--packaging": {
                cmdLine.withNextString((v, r, s) -> this.addPackaging(v));
                return true;
            }
            case "--optional": {
                NArgument val = cmdLine.nextString().get(session);
                if (enabled) {
                    this.setOptional(val.getValue().asBoolean().orNull());
                }
                return true;
            }
            case "--script": {
                cmdLine.withNextString((v, r, s) -> this.addScripts(v));
                return true;
            }
            case "--id": {
                cmdLine.withNextString((v, r, s) -> this.addId(v));
                return true;
            }
            case "--locked-id": {
                cmdLine.withNextString((v, r, s) -> this.addLockedId(v));
                return true;
            }
            case "--deployed": {
                NArgument b = cmdLine.nextBoolean().get(session);
                if (enabled) {
                    checkSession();
                    this.setInstallStatus(NInstallStatusFilters.of(session).byDeployed(b.getBooleanValue().get(session)));
                }
                return true;
            }
            case "-i":
            case "--installed": {
                NArgument b = cmdLine.nextBoolean().get(session);
                if (enabled) {
                    checkSession();
                    this.setInstallStatus(
                            NInstallStatusFilters.of(session).byInstalled(b.getBooleanValue().get(session))
                    );
                }
                return true;
            }
            case "--required": {
                NArgument b = cmdLine.nextBoolean().get(session);
                if (enabled) {
                    checkSession();
                    this.setInstallStatus(NInstallStatusFilters.of(session).byRequired(b.getBooleanValue().get(session)));
                }
                return true;
            }
            case "--obsolete": {
                NArgument b = cmdLine.nextBoolean().get(session);
                if (enabled) {
                    checkSession();
                    this.setInstallStatus(NInstallStatusFilters.of(session).byObsolete(b.getBooleanValue().get(session)));
                }
                return true;
            }
            case "--status": {
                NArgument aa = cmdLine.nextString().get(session);
                if (enabled) {
                    checkSession();
                    this.setInstallStatus(NInstallStatusFilters.of(session).parse(aa.getStringValue().get(session)));
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
                    addId(a.asString().get(session));
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
                + ", location=" + getLocation()
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
        checkSession();
        NDisplayProperty[] a = getDisplayOptions().getDisplayProperties();
        NStream r = null;
        if (isDependencies() && !isInlineDependencies()) {
            NContentType of = getSearchSession().getOutputFormat();
            if (of == null) {
                of = NContentType.TREE;
            }
            switch (of) {
                case JSON:
                case TSON:
                case XML:
                case YAML:
                case TREE: {
                    return (NIterator) IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                            .flatMap(NFunction.of(x -> x.getDependencies().get(session).transitiveNodes().iterator(), "getDependencies"))
                            .map(NFunction.of(x -> dependenciesToElement(x), "dependenciesToElement"))
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
                    case APPS_FOLDER: {
                        r = getResultStoreLocations(NStoreLocation.APPS);
                        break;
                    }
                    case CACHE_FOLDER: {
                        r = getResultStoreLocations(NStoreLocation.CACHE);
                        break;
                    }
                    case CONFIG_FOLDER: {
                        r = getResultStoreLocations(NStoreLocation.CONFIG);
                        break;
                    }
                    case LIB_FOLDER: {
                        r = getResultStoreLocations(NStoreLocation.LIB);
                        break;
                    }
                    case LOG_FOLDER: {
                        r = getResultStoreLocations(NStoreLocation.LOG);
                        break;
                    }
                    case TEMP_FOLDER: {
                        r = getResultStoreLocations(NStoreLocation.TEMP);
                        break;
                    }
                    case VAR_LOCATION: {
                        r = getResultStoreLocations(NStoreLocation.VAR);
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
    public NSearchCommand run() {
        NIterator<Object> it = runIterator();
        if (session.isDry()) {
            displayDryQueryPlan(it);
        } else {
            it = NWorkspaceUtils.of(getSearchSession()).decoratePrint(it, getSearchSession(), getDisplayOptions());
            while (it.hasNext()) {
                it.next();
            }
        }
        return this;
    }

    private NElement toQueryPlan(NIterator it) {
        NElements elem = NElements.of(session);
        return
                elem.ofObject()
                        .set("SearchQueryPlan",
                                NDescribables.resolveOrDestruct(it, session))
                        .build();
    }

    private void displayDryQueryPlan(NIterator it) {
        NElement n = toQueryPlan(it);
        NContentType f = session.getOutputFormat();
        if (f == NContentType.PLAIN) {
            f = NContentType.TREE;
        }
        NSession session2 = session.copy().setOutputFormat(f);
        session2.out().printlnf(n);
    }


    public NIterator<NDefinition> getResultDefinitionIteratorBase(boolean content, boolean effective) {
        NFetchCommand fetch = toFetch().setContent(content).setEffective(effective);
        NFetchCommand ofetch = toFetch().setContent(content).setEffective(effective).setSession(getSession().copy().setFetchStrategy(NFetchStrategy.OFFLINE));
        final boolean hasRemote = getSession().getFetchStrategy() == null
                || getSession().getFetchStrategy().modes().stream()
                .anyMatch(x -> x == NFetchMode.REMOTE);
        return IteratorBuilder.of(getResultIdIteratorBase(null), session)
                .map(NFunction.of(next -> {
//                    NutsDefinition d = null;
//                    if (isContent()) {
                    NDefinition d = fetch.setId(next).getResultDefinition();
                    if (d == null) {
                        if (isFailFast()) {
                            throw new NNotFoundException(getSession(), next);
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
//                            _LOGOP(getSession())
//                                    .verb(NutsLogVerb.FAIL)
//                                    .log("inconsistent repository. id %s was found but its definition could not be resolved!",next);
//                        }
//                    }
//                    return d;
                }, "Id->Definition"))
                .notNull().build();
    }

    protected <T> NStream<T> buildCollectionResult(NIterator<T> o) {
        NSession ss = getSearchSession();
        return new NIteratorStream<T>(ss, resolveFindIdBase(), o);
    }

    protected String resolveFindIdBase() {
        return ids.isEmpty() ? null : ids.get(0) == null ? null : ids.get(0).toString();
    }

    public NStream<String> getResultStatuses() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .map(NFunction.of(
                        x -> NIdFormatHelper.of(x, getSearchSession())
                                .buildLong().getStatusString(),
                        "getStatusString")
                )
                .notBlank());
    }

    //    protected NutsStream<NutsDefinition> getResultDefinitionsBase(boolean print, boolean sort, boolean content, boolean effective) {
//        checkSession();
//        return new NutsDefinitionNutsResult(getSession(), resolveFindIdBase(), print, sort, content, effective);
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
        return getSession();
    }

    protected NIterator<NId> applyPrintDecoratorIterOfNutsId(NIterator<NId> curr, boolean print) {
        return print ? NWorkspaceUtils.of(getSearchSession()).decoratePrint(curr, getSearchSession(), getDisplayOptions()) : curr;
    }
}
