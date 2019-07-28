package net.vpc.app.nuts.core.impl.def.wscommands;

import java.io.UncheckedIOException;

import net.vpc.app.nuts.core.*;
import net.vpc.app.nuts.core.impl.def.DefaultNutsWorkspace;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.filters.CoreFilterUtils;
import net.vpc.app.nuts.core.filters.dependency.NutsDependencyOptionFilter;
import net.vpc.app.nuts.core.filters.dependency.NutsDependencyScopeFilter;
import net.vpc.app.nuts.core.filters.repository.DefaultNutsRepositoryFilter;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.NutsDependencyScopes;
import net.vpc.app.nuts.core.util.NutsIdGraph;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.iter.IteratorBuilder;
import net.vpc.app.nuts.core.util.common.TraceResult;
import net.vpc.app.nuts.core.wscommands.AbstractNutsFetchCommand;

public class DefaultNutsFetchCommand extends AbstractNutsFetchCommand {

    public static final Logger LOG = Logger.getLogger(DefaultNutsFetchCommand.class.getName());

    public DefaultNutsFetchCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsDefinition getResultDefinition() {
        try {
            NutsDefinition def = fetchDefinition(getId(), this);
            return def;
        } catch (NutsNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NutsContent getResultContent() {
        try {
            NutsDefinition def = fetchDefinition(getId(), copy().setContent(true).setEffective(false).setInstallInformation(false));
            return def.getContent();
        } catch (NutsNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NutsId getResultId() {
        try {
            NutsDefinition def = fetchDefinition(getId(), this);
            if (isEffective()) {
                return NutsWorkspaceExt.of(ws).resolveEffectiveId(def.getEffectiveDescriptor(), this);
            }
            return def.getId();
        } catch (NutsNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public String getResultContentHash() {
        try {
            Path f = getResultDefinition().getPath();
            return ws.io().hash().source(f).computeString();
        } catch (NutsNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public String getResultDescriptorHash() {
        try {
            return ws.io().hash().source(getResultDescriptor()).computeString();
        } catch (NutsNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NutsDescriptor getResultDescriptor() {
        try {
            NutsDefinition def = fetchDefinition(getId(), copy().setContent(false).setInstallInformation(false));
            if (isEffective()) {
                return def.getEffectiveDescriptor();
            }
            return def.getDescriptor();
        } catch (NutsNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public Path getResultPath() {
        try {
            NutsDefinition def = fetchDefinition(getId(), copy().setContent(true).setEffective(false).setInstallInformation(false));
            Path p = def.getPath();
            if (getLocation() != null) {
                return getLocation();
            }
            return p;
        } catch (NutsNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NutsFetchCommand copy() {
        DefaultNutsFetchCommand b = new DefaultNutsFetchCommand(ws);
        b.copyFrom(this);
        return b;
    }

    protected DefaultNutsDefinition fetchDefinitionBase(NutsId id, NutsFetchCommand options) {
        long startTime = System.currentTimeMillis();
        DefaultNutsDefinition result = null;
        NutsFetchStrategy nutsFetchModes = NutsWorkspaceHelper.validate(options.getFetchStrategy());
        Path cachePath = null;
        {
            cachePath = ws.config().getStoreLocation(id, NutsStoreLocation.CACHE)
                    .resolve(ws.config().getDefaultIdFilename(id.setFace("def.cache")));
            if (Files.isRegularFile(cachePath)) {
                try {
                    DefaultNutsDefinition d = ws.json().parse(cachePath, DefaultNutsDefinition.class);
                    if (d != null) {
                        NutsRepository repositoryById = ws.config().findRepositoryById(d.getRepositoryUuid(), true);
                        NutsRepository repositoryByName = ws.config().findRepositoryByName(d.getRepositoryUuid(), true);
                        if (repositoryById == null || repositoryByName == null) {
                            //this is invalid cache!
                            Files.delete(cachePath);
                        } else {
                            if (LOG.isLoggable(Level.FINEST)) {
                                CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.CACHED, "Fetch definition", startTime);
                            }
                            return d;
                        }
                    }
                } catch (Exception ex) {
                    //
                }
            }
        }
        if (LOG.isLoggable(Level.FINEST)) {
            CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.START, "Fetch definition", 0);
        }
        for (NutsFetchMode mode : nutsFetchModes) {
            try {
                result = fetchDescriptorAsDefinition(id, options, mode);
                if (result != null) {
                    break;
                }
            } catch (NutsNotFoundException | UncheckedIOException ex) {
                //ignore
            } catch (Exception ex) {
                //ignore
                if (LOG.isLoggable(Level.FINEST)) {
                    CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.ERROR, "Fetch def", startTime);
                }
            }
        }
        if (result == null
                && nutsFetchModes == NutsFetchStrategy.INSTALLED
                && NutsWorkspaceExt.of(ws).getInstalledRepository().isInstalled(id)
        ) {
            //this happens if a component is installed but the
            // corresponding file was removed for any reason...
            //that said, will search remote!
            for (NutsFetchMode mode : NutsFetchStrategy.REMOTE) {
                NutsSession session = NutsWorkspaceUtils.validateSession(ws, options.getSession());
                options.setSession(session.copy().yes());
                try {
                    result = fetchDescriptorAsDefinition(id, options, mode);
                    if (result != null) {
                        break;
                    }
                } catch (NutsNotFoundException | UncheckedIOException ex) {
                    //ignore
                } catch (Exception ex) {
                    //ignore
                    if (LOG.isLoggable(Level.FINEST)) {
                        CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.ERROR, "Fetch def", startTime);
                    }
                } finally {
                    options.setSession(session);
                }
            }
        }
        if (result != null) {
            try {
                ws.json().value(result).print(cachePath);
            } catch (Exception ex) {
                //
            }
        }
        return result;
    }

    public NutsDefinition fetchDefinition(NutsId id, NutsFetchCommand options) {
        long startTime = System.currentTimeMillis();
        NutsWorkspaceUtils.checkLongNameNutsId(ws, id);
        options = NutsWorkspaceUtils.validateSession(ws, options);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsFetchStrategy nutsFetchModes = NutsWorkspaceHelper.validate(options.getFetchStrategy());
        NutsId effectiveId = null;
        DefaultNutsDefinition foundDefinition = null;
        try {
            //add env parameters to fetch adequate nuts
            id = NutsWorkspaceUtils.configureFetchEnv(ws, id);
            foundDefinition = fetchDefinitionBase(id, options);
            if (foundDefinition != null) {
                if (options.isEffective()) {
                    try {
                        foundDefinition.setEffectiveDescriptor(dws.resolveEffectiveDescriptor(foundDefinition.getDescriptor(), options.getSession()));
                    } catch (NutsNotFoundException ex) {
                        //ignore
                        LOG.log(Level.WARNING, "Nuts Descriptor Found, but its parent is not: {0} with parent {1}", new Object[]{id.getLongName(), Arrays.toString(foundDefinition.getDescriptor().getParents())});
                        foundDefinition = null;
                    }
                }
                if (foundDefinition != null) {
                    if (isDependenciesTree()) {
                        NutsDependencyTreeNodesCache cache = new NutsDependencyTreeNodesCache();
                        cache.scopes = getScope().toArray(new NutsDependencyScope[0]);
                        cache.optional = getOptional();

                        if (effectiveId == null) {
                            effectiveId = dws.resolveEffectiveId(foundDefinition.getDescriptor(), ws.fetch().session(options.getSession()));
                        }
                        NutsDependencyTreeNode[] tree = null;
                        Path cachePath = null;
                        {
                            cachePath = ws.config().getStoreLocation(effectiveId, NutsStoreLocation.CACHE)
                                    .resolve(ws.config().getDefaultIdFilename(effectiveId.setFace(Integer.toHexString(cache.keyHashCode()) + ".dep-tree.cache")));
                            if (Files.isRegularFile(cachePath)) {
                                try {
                                    NutsDependencyTreeNodesCache d = ws.json().parse(cachePath, NutsDependencyTreeNodesCache.class);
                                    if (d != null && d.dependencies != null) {
                                        tree = d.dependencies;
                                    }
                                } catch (Exception ex) {
                                    //
                                }
                            }
                        }
                        if (tree == null) {
                            NutsDependencyFilter scope = getScope().isEmpty() ? null : new NutsDependencyScopeFilter().addScopes(getScope());
                            tree = buildTreeNode(null,
                                    new DefaultNutsDependency(id),
                                    foundDefinition, new HashSet<NutsId>(), getSession().copy().trace(false), scope).getChildren();
                            try {
                                cache.dependencies = tree;
                                ws.json().setValue(cache).print(cachePath);
                            } catch (Exception ex) {
                                //
                            }
                        }
                        foundDefinition.setDependencyNodes(tree);
                    }
                    if (isDependencies()) {
                        DefaultNutsDependenciesCache dependencyScopeCache = new DefaultNutsDependenciesCache();
                        dependencyScopeCache.scopes = getScope().toArray(new NutsDependencyScope[0]);
                        dependencyScopeCache.optional = getOptional();
                        if (effectiveId == null) {
                            effectiveId = dws.resolveEffectiveId(foundDefinition.getDescriptor(), ws.fetch().session(options.getSession()));
                        }
                        DefaultNutsDependency[] list = null;
                        Path cachePath = null;
                        {
                            Path l = ws.config().getStoreLocation(effectiveId, NutsStoreLocation.CACHE);
                            String nn = ws.config().getDefaultIdFilename(effectiveId.setFace(Integer.toHexString(dependencyScopeCache.keyHashCode()) + ".dep-list.cache"));
                            cachePath = l.resolve(nn);
                            if (Files.isRegularFile(cachePath)) {
                                try {
                                    DefaultNutsDependenciesCache d = ws.json().parse(cachePath, DefaultNutsDependenciesCache.class);
                                    if (d != null && d.dependencies != null) {
                                        list = d.dependencies;
                                    }
                                } catch (Exception ex) {
                                    //
                                }
                            }
                        }
                        if (list == null) {
                            NutsSession _session = this.getSession() == null ? ws.createSession() : this.getSession();
                            NutsDependencyFilter _dependencyFilter = CoreFilterUtils.AndSimplified(
                                    new NutsDependencyScopeFilter().addScopes(getScope()),
                                    getOptional() == null ? null : NutsDependencyOptionFilter.valueOf(getOptional()),
                                    null//getDependencyFilter()
                            );
                            NutsIdGraph graph = new NutsIdGraph(_session, isFailFast());
                            NutsId[] pp = graph.resolveDependencies(id, _dependencyFilter);
                            list = new DefaultNutsDependency[pp.length];
                            for (int i = 0; i < list.length; i++) {
                                list[i] = new DefaultNutsDependency(pp[i]);
                            }
                            try {
                                dependencyScopeCache.dependencies = list;
                                ws.json().setValue(dependencyScopeCache).print(cachePath);
                            } catch (Exception ex) {
                                //
                            }
                        }
                        foundDefinition.setDependencies(list);
                    }
                    boolean includeContent = shouldIncludeContent(options);
                    if (includeContent || options.isInstallInformation()) {
                        NutsId id1 = ws.config().createContentFaceId(foundDefinition.getId(), foundDefinition.getDescriptor());
                        Path copyTo = options.getLocation();
                        if (copyTo != null && Files.isDirectory(copyTo)) {
                            copyTo = copyTo.resolve(ws.config().getDefaultIdFilename(id1));
                        }
//                        boolean escalateMode = false;
                        boolean contentSuccessful=false;
                        for (NutsFetchMode mode : nutsFetchModes) {
                            try {
                                NutsRepository repo = ws.config().getRepository(foundDefinition.getRepositoryUuid(), true);
                                NutsContent content = repo.fetchContent()
                                        .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                                        .setLocalPath(copyTo)
                                        .setSession(NutsWorkspaceHelper.createRepositorySession(options.getSession(), repo, mode, options))
                                        .run().getResult();
                                if (content != null) {
                                    foundDefinition.setContent(content);
                                    contentSuccessful=true;
                                    foundDefinition.setDescriptor(resolveExecProperties(foundDefinition.getDescriptor(), content.getPath()));
                                    break;
                                }
                            } catch (NutsNotFoundException ex) {
//                                if (mode.ordinal() < modeForSuccessfulDescRetrieval.ordinal()) {
//                                    //ignore because actually there is more chance to find it in later modes!
//                                } else {
//                                    escalateMode = true;
//                                }
                            }
                        }
                        if (!contentSuccessful) {
                            CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.ERROR, "Fetched Descriptor but failed to fetch Component", startTime);
                            foundDefinition = null;
//                        } else if (escalateMode) {
//                            CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.ERROR, "Fetched Descriptor with mode escalation", startTime);
                        }
                    }
                    if (foundDefinition != null && options.isInstallInformation()) {
                        NutsInstallInformation ii = dws.getInstalledRepository().getInstallInfo(id);
                        if (ii != null) {
                            foundDefinition.setInstallInformation(ii);
                        } else {
                            foundDefinition.setInstallInformation(DefaultNutsWorkspace.NOT_INSTALLED);
                        }
                    }
                }
            }
        } catch (NutsNotFoundException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.ERROR, "Fetch definition", startTime);
            }
            throw ex;
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.ERROR, "Fetch definition", startTime);
            }
            throw ex;
        }
        if (foundDefinition != null) {
//            if (LOG.isLoggable(Level.FINEST)) {
//                CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.SUCCESS, "Fetch definition", startTime);
//            }
//            if (isInlineDependencies()) {
//                Set<NutsDependencyScope> s = getScope();
//                if (s == null || s.isEmpty()) {
//                    s = NutsDependencyScopePattern.RUN.expand();
//                }
//                ws.search().addId(id).session(getSession()).setFetchStratery(getFetchStrategy())
//                        .addScopes(s.toArray(new NutsDependencyScope[0]))
//                        .setOptional(getOptional())
//                        .main(false).inlineDependencies().getResultDefinitions();
//
//            }
            if (getValidSession().isTrace()) {
                NutsIterableOutput ff = CoreNutsUtils.getValidOutputFormat(getValidSession())
                        .session(getValidSession());
                ff.start();
                ff.next(foundDefinition);
                ff.complete();
            }
            return foundDefinition;
        }
        throw new NutsNotFoundException(ws, id);
    }

    private boolean shouldIncludeContent(NutsFetchCommand options) {
        boolean includeContent = options.isContent();
        if (options instanceof DefaultNutsQueryBaseOptions) {
            if (((DefaultNutsQueryBaseOptions) options).getDisplayOptions().isRequireDefinition()) {
                includeContent = true;
            }
        }
        return includeContent;
    }

    private NutsDependencyTreeNode buildTreeNode(NutsId from, NutsDependency root, NutsDefinition def, Set<NutsId> visited, NutsSession session, NutsDependencyFilter dependencyFilter) {
        List<NutsDependencyTreeNode> all = new ArrayList<NutsDependencyTreeNode>();
        boolean partial = visited.contains(root.getId().getLongNameId());
        if (!partial) {
            visited.add(root.getId().getLongNameId());
            NutsDependency[] d = def.getDescriptor().getDependencies();
            for (NutsDependency nutsDependency : d) {
                if (dependencyFilter == null || dependencyFilter.accept(null, nutsDependency, session)) {
                    NutsDefinition def2 = ws.search()
                            .id(nutsDependency.getId()).session(session.copy().trace(false).setProperty("monitor-allowed", false)).effective()
                            .content(shouldIncludeContent(this))
                            .latest().getResultDefinitions().first();
                    if (def2 != null) {
                        NutsDependency[] dependencies = CoreFilterUtils.filterDependencies(def2.getDescriptor().getId(), def2.getDescriptor().getDependencies(),
                                dependencyFilter, session);
                        for (NutsDependency dd : dependencies) {
                            if (dd.getVersion().equals(nutsDependency.getVersion())) {
                                dd = dd.setId(dd.getId().setProperty("resolved-version", dd.getVersion().getValue()));
                            }
                            all.add(buildTreeNode(root.getId(), dd, def2, visited, session, dependencyFilter));
                        }
                    }
                }
            }
        }
        return new DefaultNutsDependencyTreeNode(root, all.toArray(new NutsDependencyTreeNode[0]), partial);
    }

    protected NutsDescriptor resolveExecProperties(NutsDescriptor nutsDescriptor, Path jar) {
        boolean executable = nutsDescriptor.isExecutable();
        boolean nutsApp = nutsDescriptor.isNutsApplication();
        if (jar.getFileName().toString().toLowerCase().endsWith(".jar") && Files.isRegularFile(jar)) {
            Path cachePath = ws.config().getStoreLocation(nutsDescriptor.getId(), NutsStoreLocation.CACHE)
                    .resolve(ws.config().getDefaultIdFilename(nutsDescriptor.getId().setFace("info.cache"))
                    );
            Map<String, String> map = null;
            try {
                if (Files.isRegularFile(cachePath)) {
                    map = ws.json().parse(cachePath, Map.class);
                }
            } catch (Exception ex) {
                //
            }
            if (map != null) {
                executable = "true".equals(map.get("executable"));
                nutsApp = "true".equals(map.get("nutsApplication"));
            } else {
                try {
                    NutsExecutionEntry[] t = ws.io().parseExecutionEntries(jar);
                    if (t.length > 0) {
                        executable = true;
                        if (t[0].isApp()) {
                            nutsApp = true;
                        }
                    }
                    try {
                        map = new LinkedHashMap<>();
                        map.put("executable", String.valueOf(executable));
                        map.put("nutsApplication", String.valueOf(nutsApp));
                        ws.json().value(map).print(cachePath);
                    } catch (Exception ex) {
                        //
                    }
                } catch (Exception ex) {
                    //
                }
            }
        }
        nutsDescriptor = nutsDescriptor.builder().setExecutable(executable).build();
        nutsDescriptor = nutsDescriptor.builder().setNutsApplication(nutsApp).build();

        return nutsDescriptor;
    }

    protected DefaultNutsDefinition fetchDescriptorAsDefinition(NutsId id, NutsFetchCommand options, NutsFetchMode mode) {
        options = NutsWorkspaceUtils.validateSession(ws, options);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsRepositoryFilter repositoryFilter = new DefaultNutsRepositoryFilter(Arrays.asList(getRepositories())).simplify();
        if (mode == NutsFetchMode.INSTALLED) {
            if (id.getVersion().isBlank()) {
                String v = dws.getInstalledRepository().getDefaultVersion(id);
                if (v != null) {
                    id = id.setVersion(v);
                } else {
                    id = id.setVersion("");
                }
            }
            NutsVersionFilter versionFilter = id.getVersion().isBlank() ? null : id.getVersion().filter();
            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(getValidSession(), null, NutsFetchMode.INSTALLED, new DefaultNutsFetchCommand(ws));
            List<NutsVersion> all = IteratorBuilder.of(dws.getInstalledRepository().findVersions(id, CoreFilterUtils.idFilterOf(versionFilter), rsession))
                    .convert(x -> x.getVersion()).list();
            if (all.size() > 0) {
                all.sort(null);
                id = id.setVersion(all.get(all.size() - 1));
                mode = NutsFetchMode.LOCAL;
            } else {
                throw new NutsNotFoundException(ws, id);
            }
        }
        for (NutsRepository repo : NutsWorkspaceUtils.filterRepositories(ws, NutsRepositorySupportedAction.SEARCH, id, repositoryFilter, mode, options)) {
            try {
                NutsDescriptor descriptor = repo.fetchDescriptor().setId(id).setSession(NutsWorkspaceHelper.createRepositorySession(options.getSession(), repo, mode,
                        options
                )).run().getResult();
                if (descriptor != null) {
                    NutsId nutsId = dws.resolveEffectiveId(descriptor,
                            options);
                    NutsIdBuilder newIdBuilder = nutsId.builder();
                    if (CoreStringUtils.isBlank(newIdBuilder.getNamespace())) {
                        newIdBuilder.setNamespace(repo.config().getName());
                    }
                    //inherit classifier from requested parse
                    String classifier = id.getClassifier();
                    if (!CoreStringUtils.isBlank(classifier)) {
                        newIdBuilder.setClassifier(classifier);
                    }
                    Map<String, String> q = id.getProperties();
                    if (!NutsDependencyScopes.isDefaultScope(q.get(NutsConstants.IdProperties.SCOPE))) {
                        newIdBuilder.setScope(q.get(NutsConstants.IdProperties.SCOPE));
                    }
                    if (!CoreNutsUtils.isDefaultOptional(q.get(NutsConstants.IdProperties.OPTIONAL))) {
                        newIdBuilder.setOptional(q.get(NutsConstants.IdProperties.OPTIONAL));
                    }
                    NutsId newId = newIdBuilder.build();

                    boolean api = false;
                    boolean runtime = false;
                    boolean extension = false;
                    boolean companion = false;
                    NutsId apiId0 = null;
                    NutsId apiId = null;

                    if (getId().getShortName().equals(NutsConstants.Ids.NUTS_API)) {
                        api = true;
                    } else {
                        apiId = null;
                        for (NutsDependency dependency : descriptor.getDependencies()) {
                            if (dependency.getId().getShortName().equals(NutsConstants.Ids.NUTS_API)
                                    &&
                                    NutsDependencyScopes.isCompileScope(dependency.getScope())) {
                                apiId0 = dependency.getId().getLongNameId();
                            }
                        }
                        if (apiId0 != null) {
                            if (getId().getShortName().equals(NutsConstants.Ids.NUTS_RUNTIME)) {
                                runtime = true;
                                apiId = apiId0;
                            } else {
                                runtime = CoreCommonUtils.parseBoolean(descriptor.getProperties().get("nuts-runtime"), false);
                                if (!runtime) {
                                    extension = CoreCommonUtils.parseBoolean(descriptor.getProperties().get("nuts-extension"), false);
                                    apiId = apiId0;
                                }
                            }
                            if (!runtime && !extension) {
                                for (String companionTool : NutsWorkspaceExt.of(ws).getCompanionIds()) {
                                    if (companionTool.equals(getId().getShortName())) {
                                        companion = true;
                                        apiId = apiId0;
                                    }
                                }
                            }
                        }
                    }

                    return new DefaultNutsDefinition(
                            repo.getUuid(),
                            repo.config().name(),
                            newId,
                            descriptor,
                            null,
                            null,
                            api, runtime, extension, companion, apiId
                    );
                }
            } catch (NutsNotFoundException exc) {
                //
            }
        }
        throw new NutsNotFoundException(ws, id);
    }

    @Override
    public NutsFetchCommand run() {
        getResultDefinition();
        return this;
    }

    public static class ScopePlusOptionsCache {
        public NutsDependencyScope[] scopes;
        public Boolean optional;

        public int keyHashCode() {
            int s = 0;
            if (scopes != null) {
                Arrays.sort(scopes);
                for (NutsDependencyScope element : scopes)
                    s = 31 * s + (element == null ? 0 : element.id().hashCode());
            }
            return s * 31 + (optional == null ? 0 : optional.hashCode());
        }
    }

    public static class DefaultNutsDependenciesCache extends ScopePlusOptionsCache {
        public DefaultNutsDependency[] dependencies;
    }

    public static class NutsDependencyTreeNodesCache extends ScopePlusOptionsCache {
        public NutsDependencyTreeNode[] dependencies;
    }
}
