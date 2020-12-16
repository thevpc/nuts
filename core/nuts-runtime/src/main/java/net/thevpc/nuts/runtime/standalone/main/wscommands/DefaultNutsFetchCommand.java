package net.thevpc.nuts.runtime.standalone.main.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsRepositorySupportedAction;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.repos.NutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.main.repos.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.DefaultNutsDefinition;
import net.thevpc.nuts.runtime.standalone.DefaultNutsDependencyTreeNode;
import net.thevpc.nuts.runtime.standalone.DefaultNutsInstallInfo;
import net.thevpc.nuts.runtime.standalone.DefaultNutsQueryBaseOptions;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsDependency;
import net.thevpc.nuts.runtime.core.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.log.NutsLogVerb;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.runtime.standalone.util.common.CoreCommonUtils;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.common.TraceResult;
import net.thevpc.nuts.runtime.standalone.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.AbstractNutsFetchCommand;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

public class DefaultNutsFetchCommand extends AbstractNutsFetchCommand {

    public final NutsLogger LOG;

    public DefaultNutsFetchCommand(NutsWorkspace ws) {
        super(ws);
        LOG = ws.log().of(DefaultNutsFetchCommand.class);
    }

    @Override
    public NutsContent getResultContent() {
        try {
            NutsDefinition def = fetchDefinition(getId(), copy().setContent(true).setEffective(false), true, false);
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
            NutsDefinition def = fetchDefinition(getId(), this, false, false);
            if (isEffective()) {
                return NutsWorkspaceExt.of(ws).resolveEffectiveId(def.getEffectiveDescriptor(), getSession());
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
    public NutsDefinition getResultDefinition() {
        try {
            return fetchDefinition(getId(), this, true, true);
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
            NutsDefinition def = fetchDefinition(getId(), copy().setContent(false), false, false);
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
    public NutsInstallInformation getResultInstallInformation() {
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsInstallInformation ii = dws.getInstalledRepository().getInstallInformation(getId(), session);
        if (ii != null) {
            return ii;
        } else {
            return DefaultNutsInstallInfo.notInstalled(getId());
        }
    }

    public Path getResultPath() {
        try {
            NutsDefinition def = fetchDefinition(getId(), copy().setContent(true).setEffective(false), true, false);
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

    @Override
    public NutsFetchCommand run() {
        getResultDefinition();
        return this;
    }

    protected DefaultNutsDefinition fetchDefinitionBase(NutsId id, NutsSession session) {
        long startTime = System.currentTimeMillis();
        DefaultNutsDefinition result = null;
        NutsFetchStrategy nutsFetchModes = NutsWorkspaceHelper.validate(session.getFetchStrategy());
        Path cachePath = null;
        {
            cachePath = ws.locations().getStoreLocation(id, NutsStoreLocation.CACHE)
                    .resolve(ws.locations().getDefaultIdFilename(id.builder().setFace("def.cache").build()));
            if (Files.isRegularFile(cachePath)) {
                try {
                    if (CoreIOUtils.isObsoleteInstant(session, Files.getLastModifiedTime(cachePath).toInstant())) {
                        //this is invalid cache!
                        Files.delete(cachePath);
                    } else {
                        DefaultNutsDefinition d = ws.formats().element().setContentType(NutsContentType.JSON).parse(cachePath, DefaultNutsDefinition.class);
                        if (d != null) {
                            NutsRepository repositoryById = ws.repos().findRepositoryById(d.getRepositoryUuid(), session.copy().setTransitive(true));
                            NutsRepository repositoryByName = ws.repos().findRepositoryByName(d.getRepositoryName(), session.copy().setTransitive(true));
                            if (repositoryById == null || repositoryByName == null) {
                                //this is invalid cache!
                                Files.delete(cachePath);
                            } else {
                                NutsWorkspaceUtils.of(ws).traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.CACHED, "Fetch definition", startTime);
                                return d;
                            }
                        }
                    }
                } catch (Exception ex) {
                    //
                }
            }
        }
        for (NutsFetchMode mode : nutsFetchModes) {
            try {
                result = fetchDescriptorAsDefinition(id, session, mode);
                if (result != null) {
                    break;
                }
            } catch (NutsNotFoundException ex) {
                //ignore
            } catch (Exception ex) {
                //ignore
                LOG.with().error(ex).level(Level.SEVERE).log("Unexpected error while fetching descriptor for {0}", id);
                if (LOG.isLoggable(Level.FINEST)) {
                    NutsWorkspaceUtils.of(ws).traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.FAIL, "Fetch def", startTime);
                }
            }
        }
//        if (result == null
//                && nutsFetchModes == NutsFetchStrategy.INSTALLED
//                && NutsWorkspaceExt.of(ws).getInstalledRepository().getInstallStatus(id, session)
//        ) {
//            //this happens if a component is installed but the
//            // corresponding file was removed for any reason...
//            //that said, will search remote!
//            for (NutsFetchMode mode : NutsFetchStrategy.REMOTE) {
//                NutsSession s2=session.copy().yes();
//                try {
//                    result = fetchDescriptorAsDefinition(id, s2, mode);
//                    if (result != null) {
//                        break;
//                    }
//                } catch (NutsNotFoundException | UncheckedIOException ex) {
//                    //ignore
//                } catch (Exception ex) {
//                    //ignore
//                    if (LOG.isLoggable(Level.FINEST)) {
//                        NutsWorkspaceUtils.of(ws).traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.FAIL, "Fetch def", startTime);
//                    }
//                }
//            }
//        }
        if (result != null) {
            try {
                ws.formats().element().setContentType(NutsContentType.JSON).setValue(result).print(cachePath);
            } catch (Exception ex) {
                //
            }
        }
        return result;
    }

    public NutsDefinition fetchDefinition(NutsId id, NutsFetchCommand options, boolean includeContent, boolean includeInstallInfo) {
        long startTime = System.currentTimeMillis();
        NutsWorkspaceUtils.of(ws).checkLongNameNutsId(id);
        options = NutsWorkspaceUtils.of(ws).validateSession(options);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsFetchStrategy nutsFetchModes = NutsWorkspaceHelper.validate(options.getSession().getFetchStrategy());
        NutsId effectiveId = null;
        DefaultNutsDefinition foundDefinition = null;
        List<Exception> reasons = new ArrayList<>();
        try {
            //add env parameters to fetch adequate nuts
            id = NutsWorkspaceUtils.of(ws).configureFetchEnv(id);
            foundDefinition = fetchDefinitionBase(id, options.getSession());
            if (foundDefinition != null) {
                if (options.isEffective()) {
                    try {
                        foundDefinition.setEffectiveDescriptor(dws.resolveEffectiveDescriptor(foundDefinition.getDescriptor(), options.getSession()));
                    } catch (NutsNotFoundException ex) {
                        //ignore
                        LOG.with().level(Level.WARNING).verb(NutsLogVerb.WARNING).log("Nuts Descriptor found, but its parent is not: {0} with parent {1}", id.getLongName(), Arrays.toString(foundDefinition.getDescriptor().getParents()));
                        foundDefinition = null;
                    }
                }
                if (foundDefinition != null) {
                    if (isDependenciesTree()) {
                        NutsDependencyTreeNode[] tree = null;
//                        NutsDependencyTreeNodesCache cache = new NutsDependencyTreeNodesCache();
//                        cache.scopes = getScope().toArray(new NutsDependencyScope[0]);
//                        cache.optional = getOptional();
//
//                        if (effectiveId == null) {
//                            effectiveId = dws.resolveEffectiveId(foundDefinition.getDescriptor(), options.getSession());
//                        }
//                        Path cachePath = null;
//                        {
//                            cachePath = ws.locations().getStoreLocation(effectiveId, NutsStoreLocation.CACHE)
//                                    .resolve(ws.locations().getDefaultIdFilename(effectiveId
//                                            .builder()
//                                            .setFace(Integer.toHexString(cache.keyHashCode()) + ".dep-tree.cache").build()
//                                    ));
//                            if (Files.isRegularFile(cachePath)) {
//                                try {
//                                    NutsDependencyTreeNodesCache d = ws.formats().json().parse(cachePath, NutsDependencyTreeNodesCache.class);
//                                    if (d != null && d.dependencies != null) {
//                                        tree = d.dependencies;
//                                    }
//                                } catch (Exception ex) {
//                                    //
//                                }
//                            }
//                        }
//                        if (tree == null) {
                            NutsDependencyFilter scope = ws.dependency().filter().byScope(getScope());
                            tree = buildTreeNode(null,
                                    ws.dependency().builder().setId(id).build(),
                                    foundDefinition, new HashSet<NutsId>(), CoreNutsUtils.silent(getSession()), buildActualDependencyFilter())
                                    .getChildren();
//                            try {
//                                cache.dependencies = tree;
//                                ws.formats().json().setValue(cache).print(cachePath);
//                            } catch (Exception ex) {
//                                //
//                            }
//                        }
                        foundDefinition.setDependencyNodes(tree);
                    }
                    if (isDependencies()) {
                        //cache disabled, too complex to support!
//                        DefaultNutsDependenciesCache dependencyScopeCache = new DefaultNutsDependenciesCache();
//                        dependencyScopeCache.scopes = getScope().toArray(new NutsDependencyScope[0]);
//                        dependencyScopeCache.optional = getOptional();
//                        if (effectiveId == null) {
//                            effectiveId = dws.resolveEffectiveId(foundDefinition.getDescriptor(), options.getSession());
//                        }
//                        DefaultNutsDependency[] list = null;
//                        Path cachePath = ws.locations().getStoreLocation(effectiveId, NutsStoreLocation.CACHE)
//                                .resolve(ws.locations().getDefaultIdFilename(effectiveId
//                                        .builder()
//                                        .setFace(Integer.toHexString(dependencyScopeCache.keyHashCode()) + ".dep-list.cache")
//                                        .build()
//                                ));
//                        if (Files.isRegularFile(cachePath)) {
//                            try {
//                                DefaultNutsDependenciesCache d = ws.formats().json().parse(cachePath, DefaultNutsDependenciesCache.class);
//                                if (d != null && d.dependencies != null) {
//                                    list = d.dependencies;
//                                }
//                            } catch (Exception ex) {
//                                //
//                            }
//                        }
//                        if (list == null) {
                        NutsDependency[] list=null;
                            NutsSession _session = this.getSession() == null ? ws.createSession() : this.getSession();
                            NutsIdGraph graph = new NutsIdGraph(CoreNutsUtils.silent(_session), isFailFast());
                            NutsId[] pp = graph.resolveDependencies(id, buildActualDependencyFilter());
                            list = Arrays.stream(pp).map(x->ws.dependency().builder().setId(x).build()).toArray(NutsDependency[]::new);
//                            try {
//                                dependencyScopeCache.dependencies = (DefaultNutsDependency[]) list;
//                                ws.formats().json().setValue(dependencyScopeCache).print(cachePath);
//                            } catch (Exception ex) {
//                                //
//                            }
//                        }
//                        NutsDependencyFilter dependencyFilter = ;
//                        if(dependencyFilter!=null){
//                            list=Arrays.asList()
//                        }
                        foundDefinition.setDependencies(list);
                    }
                    //boolean includeContent = shouldIncludeContent(options);
                    // always ok for content, if 'content' flag is not armed, try find 'local' path
                    NutsInstalledRepository installedRepository = dws.getInstalledRepository();
                    if (includeContent) {
                        NutsId id1 = ws.config().createContentFaceId(foundDefinition.getId(), foundDefinition.getDescriptor());
                        Path copyTo = options.getLocation();
                        if (copyTo != null && Files.isDirectory(copyTo)) {
                            copyTo = copyTo.resolve(ws.locations().getDefaultIdFilename(id1));
                        }
//                        boolean escalateMode = false;
                        boolean contentSuccessful = false;
                        boolean includedRemote = false;
                        String repositoryUuid = foundDefinition.getRepositoryUuid();
                        NutsRepository repo0 = ws.repos().getRepository(repositoryUuid, session.copy().setTransitive(true));
                        NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(ws).repoSPI(repo0);
                        for (NutsFetchMode mode : nutsFetchModes) {
                            try {
                                if (mode == NutsFetchMode.REMOTE) {
                                    includedRemote = true;
                                }
                                NutsContent content = repoSPI.fetchContent()
                                        .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                                        .setLocalPath(copyTo)
                                        .setSession(options.getSession())
                                        .setFetchMode(mode)
                                        .getResult();
                                if (content != null) {
                                    if (content.getPath() == null) {
                                        content = repoSPI.fetchContent()
                                                .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                                                .setLocalPath(copyTo)
                                                .setSession(options.getSession())
                                                .setFetchMode(mode)
                                                .getResult();
                                    }
                                    foundDefinition.setContent(content);
                                    contentSuccessful = true;
                                    foundDefinition.setDescriptor(resolveExecProperties(foundDefinition.getDescriptor(), content.getPath()));
                                    break;
                                }
                            } catch (NutsNotFoundException ex) {
                                reasons.add(ex);
                                //
                            }
                        }
                        if (!contentSuccessful && DefaultNutsInstalledRepository.INSTALLED_REPO_UUID.equals(foundDefinition.getRepositoryUuid())) {
                            NutsWorkspaceUtils.of(ws).traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.FAIL, "Fetched Descriptor but failed to fetch installed Component for " + id, startTime);
                            //this happens if the jar content is no more installed while its descriptor is still installed.
                            for (NutsFetchMode mode : nutsFetchModes) {

                                if (mode == NutsFetchMode.REMOTE) {
                                    includedRemote = true;
                                }

                                for (NutsRepository repo : NutsWorkspaceUtils.of(ws)
                                        .filterRepositories(NutsRepositorySupportedAction.SEARCH, id, null, mode, session,
                                                getInstalledVsNonInstalledSearch()
                                )) {
                                    try {
                                        NutsRepositorySPI repoSPI2 = NutsWorkspaceUtils.of(ws).repoSPI(repo);
                                        NutsContent content = repoSPI2.fetchContent()
                                                .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                                                .setLocalPath(copyTo)
                                                .setSession(options.getSession())
                                                .setFetchMode(mode)
                                                .getResult();
                                        if (content != null) {
                                            foundDefinition.setContent(content);
                                            contentSuccessful = true;
                                            foundDefinition.setDescriptor(resolveExecProperties(foundDefinition.getDescriptor(), content.getPath()));

                                            NutsWorkspaceUtils.of(ws).traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.FAIL, "Re-deploy installed component for " + id, startTime);
                                            NutsRepositorySPI installedRepositorySPI = NutsWorkspaceUtils.of(ws).repoSPI(installedRepository);
                                            installedRepositorySPI.deploy()
                                                    .setId(foundDefinition.getId())
                                                    .setDescriptor(foundDefinition.getDescriptor())
                                                    .setSession(session.copy().setConfirm(NutsConfirmationMode.YES))
                                                    //.setFetchMode(mode)
                                                    .setContent(content.getPath())
                                                    .run();
                                            break;
                                        }
                                    } catch (NutsNotFoundException ex) {
                                        //
                                        reasons.add(ex);
                                    }
                                    if (contentSuccessful) {
                                        break;
                                    }
                                }

                            }
                        }
                        if (!contentSuccessful && includedRemote) {
                            NutsWorkspaceUtils.of(ws).traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.FAIL, "Fetched Descriptor but failed to fetch Component", startTime);
                        }
                    }
                    if (foundDefinition != null && includeInstallInfo) {
                        //will always load install information
                        NutsInstallInformation ii = installedRepository.getInstallInformation(id, session);
                        if (ii != null) {
//                            ((DefaultNutsInstalledRepository) (dws.getInstalledRepository())).updateInstallInfoConfigInstallDate(id, Instant.now(), session);
                            foundDefinition.setInstallInformation(ii);
                        } else {
                            foundDefinition.setInstallInformation(DefaultNutsInstallInfo.notInstalled(id));
                        }
                    }
                }
            }
        } catch (NutsNotFoundException ex) {
            reasons.add(ex);
            NutsWorkspaceUtils.of(ws).traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.FAIL, "Fetch definition", startTime);
            throw ex;
        } catch (RuntimeException ex) {
            NutsWorkspaceUtils.of(ws).traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.FAIL, "[Unexpected] Fetch definition", startTime);
            throw ex;
        }
        if (foundDefinition != null) {
//            if (getSession().isTrace()) {
//                NutsIterableOutput ff = CoreNutsUtils.getValidOutputFormat(getSession())
//                        .session(getSession());
//                ff.start();
//                ff.next(foundDefinition);
//                ff.complete();
//            }
            return foundDefinition;
        }
        throw new NutsNotFoundException(ws, id);
    }

    private NutsDependencyFilter buildActualDependencyFilter() {
        return ws.dependency().filter().byScope(getScope()).and(
                ws.dependency().filter().byOptional(getOptional())
        ).and(getDependencyFilter());
    }

    private InstalledVsNonInstalledSearch getInstalledVsNonInstalledSearch() {
        return new InstalledVsNonInstalledSearch(installedOrNot == null || installedOrNot, true);
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
        boolean partial = visited.contains(root.toId().getLongNameId());
        if (!partial) {
            visited.add(root.toId().getLongNameId());
            NutsDependency[] d = def.getDescriptor().getDependencies();
            for (NutsDependency nutsDependency : d) {
                if (dependencyFilter == null || dependencyFilter.acceptDependency(null, nutsDependency, session)) {
                    NutsDefinition def2 = ws.search()
                            .addId(nutsDependency.toId()).setSession(session.copy().setTrace(false)
                                    .setProperty("monitor-allowed", false)).setEffective(true)
                            .setContent(shouldIncludeContent(this))
                            .setLatest(true).getResultDefinitions().first();
                    if (def2 != null) {
                        NutsDependency[] dependencies = CoreFilterUtils.filterDependencies(def2.getDescriptor().getId(), def2.getDescriptor().getDependencies(),
                                dependencyFilter, session);
                        for (NutsDependency dd : dependencies) {
                            if (dd.getVersion().equals(nutsDependency.getVersion())) {
                                dd = dd.builder().setProperty("resolved-version", dd.getVersion().getValue()).build();
                            }
                            all.add(buildTreeNode(root.toId(), dd, def2, visited, session, dependencyFilter));
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
            Path cachePath = ws.locations().getStoreLocation(nutsDescriptor.getId(), NutsStoreLocation.CACHE)
                    .resolve(ws.locations().getDefaultIdFilename(nutsDescriptor.getId()
                                    .builder()
                                    .setFace("info.cache")
                                    .build()
                            )
                    );
            Map<String, String> map = null;
            try {
                if (Files.isRegularFile(cachePath)) {
                    map = ws.formats().element().setContentType(NutsContentType.JSON).parse(cachePath, Map.class);
                }
            } catch (Exception ex) {
                //
            }
            if (map != null) {
                executable = "true".equals(map.get("executable"));
                nutsApp = "true".equals(map.get("nutsApplication"));
            } else {
                try {
                    NutsExecutionEntry[] t = ws.apps().execEntries().parse(jar);
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
                        ws.formats().element().setContentType(NutsContentType.JSON).setValue(map).print(cachePath);
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

    protected DefaultNutsDefinition fetchDescriptorAsDefinition(NutsId id, NutsSession session, NutsFetchMode mode) {
        session = NutsWorkspaceUtils.of(ws).validateSession(session);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsRepositoryFilter repositoryFilter = ws.repos().filter().byName(getRepositories());
//        if (mode == NutsFetchMode.INSTALLED) {
//            if (id.getVersion().isBlank()) {
//                String v = dws.getInstalledRepository().getDefaultVersion(id, session);
//                if (v != null) {
//                    id = id.builder().setVersion(v).build();
//                } else {
//                    id = id.builder().setVersion("").build();
//                }
//            }
//            NutsVersionFilter versionFilter = id.getVersion().isBlank() ? null : id.getVersion().filter();
//            List<NutsVersion> all = IteratorBuilder.of(dws.getInstalledRepository()
//                    .searchVersions().setId(id).setFilter( CoreFilterUtils.idFilterOf(versionFilter))
//                    .setSession(NutsWorkspaceHelper.createRepositorySession(getSession(),dws.getInstalledRepository(),NutsFetchMode.INSTALLED)).getResult()
//            ).convert(NutsId::getVersion,"version").list();
//            if (all.size() > 0) {
//                all.sort(null);
//                id = id.builder().setVersion(all.get(all.size() - 1)).build();
//                mode = NutsFetchMode.LOCAL;
//            } else {
//                throw new NutsNotFoundException(ws, id);
//            }
//        }
        for (NutsRepository repo : NutsWorkspaceUtils.of(ws).filterRepositories(NutsRepositorySupportedAction.SEARCH, id, repositoryFilter, mode, session, getInstalledVsNonInstalledSearch())) {
            try {
                NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(ws).repoSPI(repo);
                NutsDescriptor descriptor = repoSPI.fetchDescriptor().setId(id)
                        .setSession(session).setFetchMode(mode)
                        .getResult();
                if (descriptor != null) {
                    NutsId nutsId = dws.resolveEffectiveId(descriptor, session);
                    NutsIdBuilder newIdBuilder = nutsId.builder();
                    if (CoreStringUtils.isBlank(newIdBuilder.getNamespace())) {
                        newIdBuilder.setNamespace(repo.getName());
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
                            if (dependency.toId().getShortName().equals(NutsConstants.Ids.NUTS_API)
                                    &&
                                    NutsDependencyScopes.isCompileScope(dependency.getScope())) {
                                apiId0 = dependency.toId().getLongNameId();
                            }
                        }
                        if (apiId0 != null) {
                            if (getId().getShortName().equals(NutsConstants.Ids.NUTS_RUNTIME)) {
                                idType = NutsIdType.RUNTIME;
                                apiId = apiId0;
                            } else {
                                if (CoreCommonUtils.parseBoolean(descriptor.getProperties().get("nuts-runtime"), false)) {
                                    idType = NutsIdType.RUNTIME;
                                } else if (CoreCommonUtils.parseBoolean(descriptor.getProperties().get("nuts-extension"), false)) {
                                    idType = NutsIdType.EXTENSION;
                                    apiId = apiId0;
                                }
                            }
                            if (idType == NutsIdType.REGULAR) {
                                for (NutsId companionTool : ws.companionIds()) {
                                    if (companionTool.getShortName().equals(getId().getShortName())) {
                                        idType = NutsIdType.COMPANION;
                                        apiId = apiId0;
                                    }
                                }
                            }
                        }
                    }

                    return new DefaultNutsDefinition(
                            repo.getUuid(),
                            repo.getName(),
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
