package net.vpc.app.nuts.main.wscommands;

import java.io.UncheckedIOException;

import net.vpc.app.nuts.runtime.DefaultNutsDefinition;
import net.vpc.app.nuts.runtime.config.DefaultNutsDependency;
import net.vpc.app.nuts.runtime.DefaultNutsDependencyTreeNode;
import net.vpc.app.nuts.runtime.DefaultNutsQueryBaseOptions;
import net.vpc.app.nuts.main.DefaultNutsWorkspace;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;
import net.vpc.app.nuts.core.NutsWorkspaceExt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.filters.CoreFilterUtils;
import net.vpc.app.nuts.runtime.filters.dependency.NutsDependencyOptionFilter;
import net.vpc.app.nuts.runtime.filters.dependency.NutsDependencyScopeFilter;
import net.vpc.app.nuts.runtime.filters.repository.DefaultNutsRepositoryFilter;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.NutsDependencyScopes;
import net.vpc.app.nuts.runtime.util.NutsIdGraph;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.iter.IteratorBuilder;
import net.vpc.app.nuts.runtime.util.common.TraceResult;
import net.vpc.app.nuts.runtime.wscommands.AbstractNutsFetchCommand;

public class DefaultNutsFetchCommand extends AbstractNutsFetchCommand {

    public final NutsLogger LOG;

    public DefaultNutsFetchCommand(NutsWorkspace ws) {
        super(ws);
        LOG=ws.log().of(DefaultNutsFetchCommand.class);
    }

    @Override
    public NutsDefinition getResultDefinition() {
        try {
            return fetchDefinition(getId(), this,true,true);
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
            NutsDefinition def = fetchDefinition(getId(), copy().content().setEffective(false),true,false);
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
            NutsDefinition def = fetchDefinition(getId(), this,false,false);
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
            NutsDefinition def = fetchDefinition(getId(), copy().setContent(false),false,false);
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
            NutsDefinition def = fetchDefinition(getId(), copy().content().setEffective(false),true,false);
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
                    .resolve(ws.config().getDefaultIdFilename(id.builder().setFace("def.cache").build()));
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
                            NutsWorkspaceUtils.of(ws).traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.CACHED, "Fetch definition", startTime);
                            return d;
                        }
                    }
                } catch (Exception ex) {
                    //
                }
            }
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
                    NutsWorkspaceUtils.of(ws).traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.FAIL, "Fetch def", startTime);
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
                NutsSession session = NutsWorkspaceUtils.of(ws).validateSession( options.getSession());
                options.session(session.copy().yes());
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
                        NutsWorkspaceUtils.of(ws).traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.FAIL, "Fetch def", startTime);
                    }
                } finally {
                    options.session(session);
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

    public NutsDefinition fetchDefinition(NutsId id, NutsFetchCommand options,boolean includeContent,boolean includeInstallInfo) {
        long startTime = System.currentTimeMillis();
        NutsWorkspaceUtils.of(ws).checkLongNameNutsId( id);
        options = NutsWorkspaceUtils.of(ws).validateSession( options);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsFetchStrategy nutsFetchModes = NutsWorkspaceHelper.validate(options.getFetchStrategy());
        NutsId effectiveId = null;
        DefaultNutsDefinition foundDefinition = null;
        try {
            //add env parameters to fetch adequate nuts
            id = NutsWorkspaceUtils.of(ws).configureFetchEnv( id);
            foundDefinition = fetchDefinitionBase(id, options);
            if (foundDefinition != null) {
                if (options.isEffective()) {
                    try {
                        foundDefinition.setEffectiveDescriptor(dws.resolveEffectiveDescriptor(foundDefinition.getDescriptor(), options.getSession()));
                    } catch (NutsNotFoundException ex) {
                        //ignore
                        LOG.log(Level.WARNING, NutsLogVerb.WARNING, "Nuts Descriptor Found, but its parent is not: {0} with parent {1}", new Object[]{id.getLongName(), Arrays.toString(foundDefinition.getDescriptor().getParents())});
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
                                    .resolve(ws.config().getDefaultIdFilename(effectiveId
                                            .builder()
                                            .setFace(Integer.toHexString(cache.keyHashCode()) + ".dep-tree.cache").build()
                                    ));
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
                                    ws.dependency().builder().id(id).build(),
                                    foundDefinition, new HashSet<NutsId>(), CoreNutsUtils.silent(getSession()), scope).getChildren();
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
                            String nn = ws.config().getDefaultIdFilename(effectiveId
                                    .builder()
                                    .setFace(Integer.toHexString(dependencyScopeCache.keyHashCode()) + ".dep-list.cache")
                                    .build()
                            );
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
                            NutsIdGraph graph = new NutsIdGraph(CoreNutsUtils.silent(_session), isFailFast());
                            NutsId[] pp = graph.resolveDependencies(id, _dependencyFilter);
                            list = new DefaultNutsDependency[pp.length];
                            for (int i = 0; i < list.length; i++) {
                                list[i] = (DefaultNutsDependency) ws.dependency().builder().id(pp[i]).build();
                            }
                            try {
                                dependencyScopeCache.dependencies = (DefaultNutsDependency[]) list;
                                ws.json().setValue(dependencyScopeCache).print(cachePath);
                            } catch (Exception ex) {
                                //
                            }
                        }
                        foundDefinition.setDependencies(list);
                    }
                    //boolean includeContent = shouldIncludeContent(options);
                    // always ok for content, if 'content' flag is not armed, try find 'local' path
                    if (includeContent) {
                        NutsId id1 = ws.config().createContentFaceId(foundDefinition.getId(), foundDefinition.getDescriptor());
                        Path copyTo = options.getLocation();
                        if (copyTo != null && Files.isDirectory(copyTo)) {
                            copyTo = copyTo.resolve(ws.config().getDefaultIdFilename(id1));
                        }
//                        boolean escalateMode = false;
                        boolean contentSuccessful = false;
                        boolean includedRemote = false;
                        for (NutsFetchMode mode : nutsFetchModes) {
                            try {
                                if(mode==NutsFetchMode.REMOTE){
                                    includedRemote=true;
                                }
                                NutsRepository repo = ws.config().getRepository(foundDefinition.getRepositoryUuid(), true);
                                NutsContent content = repo.fetchContent()
                                        .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                                        .setLocalPath(copyTo)
                                        .setSession(NutsWorkspaceHelper.createRepositorySession(options.getSession(), repo, mode, options))
                                        .run().getResult();
                                if (content != null) {
                                    foundDefinition.setContent(content);
                                    contentSuccessful = true;
                                    foundDefinition.setDescriptor(resolveExecProperties(foundDefinition.getDescriptor(), content.getPath()));
                                    break;
                                }
                            } catch (NutsNotFoundException ex) {
                                //
                            }
                        }
                        if (!contentSuccessful && includedRemote) {
                            NutsWorkspaceUtils.of(ws).traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.FAIL, "Fetched Descriptor but failed to fetch Component", startTime);
                        }
                    }
                    if (foundDefinition != null && includeInstallInfo) {
                        //will always load install information
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
            NutsWorkspaceUtils.of(ws).traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.FAIL, "Fetch definition", startTime);
            throw ex;
        } catch (RuntimeException ex) {
            NutsWorkspaceUtils.of(ws).traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.FAIL, "[Unexpected] Fetch definition", startTime);
            throw ex;
        }
        if (foundDefinition != null) {
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
                            .id(nutsDependency.getId()).session(session.copy().silent().setProperty("monitor-allowed", false)).effective()
                            .content(shouldIncludeContent(this))
                            .latest().getResultDefinitions().first();
                    if (def2 != null) {
                        NutsDependency[] dependencies = CoreFilterUtils.filterDependencies(def2.getDescriptor().getId(), def2.getDescriptor().getDependencies(),
                                dependencyFilter, session);
                        for (NutsDependency dd : dependencies) {
                            if (dd.getVersion().equals(nutsDependency.getVersion())) {
                                dd = dd.builder().setProperty("resolved-version", dd.getVersion().getValue()).build();
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
        boolean nutsApp = nutsDescriptor.isApplication();
        if (jar.getFileName().toString().toLowerCase().endsWith(".jar") && Files.isRegularFile(jar)) {
            Path cachePath = ws.config().getStoreLocation(nutsDescriptor.getId(), NutsStoreLocation.CACHE)
                    .resolve(ws.config().getDefaultIdFilename(nutsDescriptor.getId()
                                    .builder()
                                    .setFace("info.cache")
                                    .build()
                            )
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
        nutsDescriptor = nutsDescriptor.builder().setApplication(nutsApp).build();

        return nutsDescriptor;
    }

    protected DefaultNutsDefinition fetchDescriptorAsDefinition(NutsId id, NutsFetchCommand options, NutsFetchMode mode) {
        options = NutsWorkspaceUtils.of(ws).validateSession( options);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsRepositoryFilter repositoryFilter = new DefaultNutsRepositoryFilter(Arrays.asList(getRepositories())).simplify();
        if (mode == NutsFetchMode.INSTALLED) {
            if (id.getVersion().isBlank()) {
                String v = dws.getInstalledRepository().getDefaultVersion(id);
                if (v != null) {
                    id = id.builder().setVersion(v).build();
                } else {
                    id = id.builder().setVersion("").build();
                }
            }
            NutsVersionFilter versionFilter = id.getVersion().isBlank() ? null : id.getVersion().filter();
            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(getValidSession(), null, NutsFetchMode.INSTALLED, new DefaultNutsFetchCommand(ws));
            List<NutsVersion> all = IteratorBuilder.of(dws.getInstalledRepository().findVersions(id, CoreFilterUtils.idFilterOf(versionFilter), rsession))
                    .convert(NutsId::getVersion,"version").list();
            if (all.size() > 0) {
                all.sort(null);
                id = id.builder().setVersion(all.get(all.size() - 1)).build();
                mode = NutsFetchMode.LOCAL;
            } else {
                throw new NutsNotFoundException(ws, id);
            }
        }
        for (NutsRepository repo : NutsWorkspaceUtils.of(ws).filterRepositories( NutsRepositorySupportedAction.SEARCH, id, repositoryFilter, mode, options)) {
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
                        newIdBuilder.setProperty(NutsConstants.IdProperties.SCOPE, q.get(NutsConstants.IdProperties.SCOPE));
                    }
                    if (!CoreNutsUtils.isDefaultOptional(q.get(NutsConstants.IdProperties.OPTIONAL))) {
                        newIdBuilder.setProperty(NutsConstants.IdProperties.OPTIONAL, q.get(NutsConstants.IdProperties.OPTIONAL));
                    }
                    NutsId newId = newIdBuilder.build();

                    NutsIdType idType = NutsIdType.REGULAR;
                    NutsId apiId0 = null;
                    NutsId apiId = null;

                    if (getId().getShortName().equals(NutsConstants.Ids.NUTS_API)) {
                        idType = NutsIdType.API;
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
                                idType = NutsIdType.RUNTIME;
                                apiId = apiId0;
                            } else {
                                if(CoreCommonUtils.parseBoolean(descriptor.getProperties().get("nuts-runtime"), false)){
                                    idType = NutsIdType.RUNTIME;
                                }else if(CoreCommonUtils.parseBoolean(descriptor.getProperties().get("nuts-extension"), false)){
                                    idType = NutsIdType.EXTENSION;
                                    apiId = apiId0;
                                }
                            }
                            if (idType==NutsIdType.REGULAR) {
                                for (String companionTool : NutsWorkspaceExt.of(ws).getCompanionIds()) {
                                    if (companionTool.equals(getId().getShortName())) {
                                        idType=NutsIdType.COMPANION;
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
                            idType, apiId
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
