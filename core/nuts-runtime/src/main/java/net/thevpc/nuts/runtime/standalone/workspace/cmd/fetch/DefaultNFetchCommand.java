package net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.runtime.standalone.NLocationKey;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinitionRef;
import net.thevpc.nuts.runtime.standalone.io.cache.CachedSupplier;
import net.thevpc.nuts.runtime.standalone.io.cache.DefaultCachedSupplier;
import net.thevpc.nuts.runtime.standalone.util.collections.LRUMap;
import net.thevpc.nuts.security.NDigest;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.dependency.util.NDependencyUtils;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.log.NLogUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNInstallInfo;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNInstalledRepository;
import net.thevpc.nuts.runtime.standalone.dependency.NDependencyScopes;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NRepositoryAndFetchMode;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NRepositoryAndFetchModeTracker;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

public class DefaultNFetchCommand extends AbstractNFetchCommand {


    public DefaultNFetchCommand(NSession session) {
        super(session);
    }

    @Override
    public NPath getResultContent() {
        try {
            NDefinition def = fetchDefinition(getId(), copy().setContent(true).setEffective(false), true, false);
            return def.getContent().get(session);
        } catch (NNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NId getResultId() {
        try {
            checkSession();
            NWorkspace ws = getSession().getWorkspace();
            NDefinition def = fetchDefinition(getId(), this, false, false);
            if (isEffective()) {
                return NWorkspaceExt.of(ws).resolveEffectiveId(def.getEffectiveDescriptor().get(session), getSession());
            }
            return def.getId();
        } catch (NNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public String getResultContentHash() {
        try {
            checkSession();
            NSession session = getSession();
            Path f = getResultDefinition().getContent().flatMap(NPath::toPath).get(this.session);
            return NDigest.of(session).setSource(f).computeString();
        } catch (NNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public String getResultDescriptorHash() {
        try {
            checkSession();
            return NDigest.of(getSession()).setSource(getResultDescriptor()).computeString();
        } catch (NNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NDefinition getResultDefinition() {
        try {
            return fetchDefinition(getId(), this, isContent(), true);
        } catch (NNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NDescriptor getResultDescriptor() {
        try {
            NDefinition def = fetchDefinition(getId(), copy().setContent(false), false, false);
            if (isEffective()) {
                return def.getEffectiveDescriptor().get(session);
            }
            return def.getDescriptor();
        } catch (NNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NInstallInformation getResultInstallInformation() {
        checkSession();
        NWorkspace ws = getSession().getWorkspace();
        NWorkspaceExt dws = NWorkspaceExt.of(ws);
        NInstallInformation ii = dws.getInstalledRepository().getInstallInformation(getId(), session);
        if (ii != null) {
            return ii;
        } else {
            return DefaultNInstallInfo.notInstalled(getId());
        }
    }

    public NPath getResultPath() {
        try {
            NDefinition def = fetchDefinition(getId(), copy().setContent(true).setEffective(false), true, false);
            return def.getContent().orNull();
        } catch (NNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NFetchCommand copy() {
        checkSession();
        DefaultNFetchCommand b = new DefaultNFetchCommand(getSession());
        b.setAll(this);
        return b;
    }

    @Override
    public NFetchCommand run() {
        getResultDefinition();
        return this;
    }

    public NDefinition fetchDefinition(NId id, NFetchCommand options, boolean includeContent, boolean includeInstallInfo) {
        LRUMap<NId, CachedSupplier<NDefinition>> cachedDefs = NWorkspaceExt.of(ws).getModel().cachedDefs;
        NId longId = id.getLongId();
        Function<NId, CachedSupplier<NDefinition>> supp = id0 -> {
            Supplier<NDefinition> supplier = new Supplier<NDefinition>() {
                @Override
                public NDefinition get() {
                    long startTime = System.currentTimeMillis();
                    checkSession();
                    NWorkspaceUtils wu = NWorkspaceUtils.of(session);
                    CoreNIdUtils.checkLongId(id, session);
//        checkSession();
                    NSession _ws = getSession();
                    NSessionUtils.checkSession(ws, options.getSession());
                    NWorkspaceExt dws = NWorkspaceExt.of(_ws);
                    NFetchStrategy nutsFetchModes = NWorkspaceHelper.validate(_ws.getFetchStrategy());
                    NRepositoryFilter repositoryFilter = getRepositoryFilter();
                    if (!NBlankable.isBlank(id.getRepository())) {
                        NRepositoryFilter repositoryFilter2 = NRepositoryFilters.of(_ws).byName(id.getRepository());
                        repositoryFilter = repositoryFilter2.and(repositoryFilter);
                    }
                    NRepositoryAndFetchModeTracker descTracker = new NRepositoryAndFetchModeTracker(
                            wu.filterRepositoryAndFetchModes(NRepositorySupportedAction.SEARCH, id, repositoryFilter,
                                    nutsFetchModes, session)
                    );
                    List<Exception> reasons = new ArrayList<>();
                    try {
                        //add env parameters to fetch adequate nuts
                        NId id2 = wu.configureFetchEnv(id);
                        NDefinition result = null;
                        for (NRepositoryAndFetchMode fetchLocation : descTracker.available()) {
                            try {
                                result = fetchDescriptorAsDefinition(id2, session, nutsFetchModes, fetchLocation, descTracker);
                                if (result != null) {
                                    return result;
                                }
                                break;
                            } catch (NNotFoundException exc) {
                                //
                                descTracker.addFailure(fetchLocation);
                            } catch (Exception ex) {
                                //ignore
                                _LOGOP(getSession()).error(ex).level(Level.SEVERE)
                                        .log(NMsg.ofJ("unexpected error while fetching descriptor for {0}", id2));
                                if (_LOG(getSession()).isLoggable(Level.FINEST)) {
                                    NLogUtils.traceMessage(_LOG(getSession()), nutsFetchModes, id2.getLongId(), NLogVerb.FAIL, "fetch def", startTime);
                                }
                                descTracker.addFailure(fetchLocation);
                            }
                        }

                    } catch (NNotFoundException ex) {
                        reasons.add(ex);
                        NLogUtils.traceMessage(_LOG(getSession()), nutsFetchModes, longId, NLogVerb.FAIL, "fetch definition", startTime);
                        throw ex;
                    } catch (RuntimeException ex) {
                        NLogUtils.traceMessage(_LOG(getSession()), nutsFetchModes, longId, NLogVerb.FAIL, "[unexpected] fetch definition", startTime);
                        throw ex;
                    }
                    throw new NNotFoundException(getSession(), id);
                }
            };
            DefaultCachedSupplier.SimpleCacheValidator<NDefinition> validator = new DefaultCachedSupplier.SimpleCacheValidator<NDefinition>(session);
            return DefaultCachedSupplier.of(
                    CachedSupplier.NCacheLevel.MEM,
                    NDefinition.class,
                    NLocationKey.ofCache(id, "def", null), supplier, validator,
                    session
            );
        };
        if (cachedDefs.containsKey(longId)) {
            CachedSupplier<NDefinition> u = cachedDefs.get(longId);
            if (u == null) {
                throw new IllegalArgumentException("circular loading of " + longId);
            }
            return u.getValue();
        }
        cachedDefs.put(longId, null);
        CachedSupplier<NDefinition> uu = supp.apply(longId);
        cachedDefs.put(longId, uu);
        return uu.getValue();
    }

    private NDependencyFilter buildActualDependencyFilter() {
        checkSession();
        NDependencyFilters ff = NDependencyFilters.of(getSession());
        return ff.byScope(getScope())
                .and(ff.byOptional(getOptional())
                ).and(getDependencyFilter());
    }

    protected boolean fetchContent(NId id1, DefaultNDefinition foundDefinition, NRepository repo0, NFetchStrategy nutsFetchModes, Path copyTo, List<Exception> reasons) {
        NRepositorySPI repoSPI = NWorkspaceUtils.of(session).repoSPI(repo0);
        for (NFetchMode mode : nutsFetchModes) {
            try {
                NPath content = repoSPI.fetchContent()
                        .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                        .setLocalPath(copyTo == null ? null : copyTo.toString())
                        .setSession(session)
                        .setFetchMode(mode)
                        .getResult();
                if (content != null) {
                    content = repoSPI.fetchContent()
                            .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                            .setLocalPath(copyTo == null ? null : copyTo.toString())
                            .setSession(session)
                            .setFetchMode(mode)
                            .getResult();
                    foundDefinition.setContent(content);
                    foundDefinition.setDescriptor(resolveExecProperties(foundDefinition.getDescriptor(), content));
                    return true;
                }
            } catch (NNotFoundException ex) {
                reasons.add(ex);
                //
            }
        }
        return false;
    }


    protected NDescriptor resolveExecProperties(NDescriptor nutsDescriptor, NPath jar) {
        checkSession();
        boolean executable = nutsDescriptor.isExecutable();
        boolean nutsApp = nutsDescriptor.isApplication();
        NSession session = getSession();
        if (jar.getName().toLowerCase().endsWith(".jar") && jar.isRegularFile()) {
            NPath cachePath = NLocations.of(session).getStoreLocation(nutsDescriptor.getId(), NStoreType.CACHE)
                    .resolve(NLocations.of(session).getDefaultIdFilename(nutsDescriptor.getId()
                                    .builder()
                                    .setFace("info.cache")
                                    .build()
                            )
                    );
            Map<String, String> map = null;
            NElements elem = NElements.of(session);
            try {
                if (cachePath.isRegularFile()) {
                    map = elem.setSession(this.session)
                            .json().parse(cachePath, Map.class);
                }
            } catch (Exception ex) {
                //
            }
            if (map != null) {
                executable = "true".equals(map.get("executable"));
                nutsApp = "true".equals(map.get("nutsApplication"));
            } else {
                try {
                    List<NExecutionEntry> t = NExecutionEntries.of(session).setSession(getSession()).parse(jar);
                    if (t.size() > 0) {
                        executable = true;
                        if (t.get(0).isApp()) {
                            nutsApp = true;
                        }
                    }
                    try {
                        map = new LinkedHashMap<>();
                        map.put("executable", String.valueOf(executable));
                        map.put("nutsApplication", String.valueOf(nutsApp));
                        elem.json().setSession(getSession()).setValue(map)
                                .setNtf(false)
                                .print(cachePath);
                    } catch (Exception ex) {
                        //
                    }
                } catch (Exception ex) {
                    //
                }
            }
        }
        NDescriptorBuilder nb = nutsDescriptor.builder();
        if (executable) {
            nb.addFlag(NDescriptorFlag.EXEC);
        }
        if (nutsApp) {
            nb.addFlag(NDescriptorFlag.APP);
        }
        return nb.build();
    }

    protected NDefinition fetchDescriptorAsDefinition(NId id, NSession session, NFetchStrategy nutsFetchModes, NRepositoryAndFetchMode location, NRepositoryAndFetchModeTracker descTracker) {
        NFetchMode mode = location.getFetchMode();
        NRepository repo = location.getRepository();
        checkSession();
        NSessionUtils.checkSession(this.ws, session);
        NWorkspaceExt dws = NWorkspaceExt.of(session);
        boolean withCache = !(repo instanceof DefaultNInstalledRepository) && session.isCached();
        Supplier<NDefinition> supplier = () -> {
            NWorkspaceUtils wu = NWorkspaceUtils.of(session);
            NRepositorySPI repoSPI = wu.repoSPI(repo);
            NDescriptor descriptor = repoSPI.fetchDescriptor().setId(id)
                    .setSession(session).setFetchMode(mode)
                    .getResult();
            if (descriptor != null) {
                NId nutsId = dws.resolveEffectiveId(descriptor, session);
                NIdBuilder newIdBuilder = nutsId.builder();
                if (NBlankable.isBlank(newIdBuilder.getRepository())) {
                    newIdBuilder.setRepository(repo.getName());
                }
                //inherit classifier from requested parse
                String classifier = id.getClassifier();
                if (!NBlankable.isBlank(classifier)) {
                    newIdBuilder.setClassifier(classifier);
                }
                Map<String, String> q = id.getProperties();
                if (!NDependencyScopes.isDefaultScope(q.get(NConstants.IdProperties.SCOPE))) {
                    newIdBuilder.setProperty(NConstants.IdProperties.SCOPE, q.get(NConstants.IdProperties.SCOPE));
                }
                if (!NDependencyUtils.isDefaultOptional(q.get(NConstants.IdProperties.OPTIONAL))) {
                    newIdBuilder.setProperty(NConstants.IdProperties.OPTIONAL, q.get(NConstants.IdProperties.OPTIONAL));
                }
                NId newId = newIdBuilder.build();

                NId apiId0 = null;
                NId apiId = null;

                if (getId().getShortName().equals(NConstants.Ids.NUTS_API)) {
                    //
                } else {
                    apiId = null;
                    for (NDependency dependency : descriptor.getDependencies()) {
                        if (dependency.toId().getShortName().equals(NConstants.Ids.NUTS_API)
                                && NDependencyScopes.isCompileScope(dependency.getScope())) {
                            apiId0 = dependency.toId().getLongId();
                        }
                    }
                    if (apiId0 != null) {
                        if (getId().getShortName().equals(NConstants.Ids.NUTS_RUNTIME)) {
                            apiId = apiId0;
                        } else if (descriptor.getIdType() == NIdType.RUNTIME) {
                            apiId = apiId0;
                        } else if (descriptor.getIdType() == NIdType.EXTENSION) {
                            apiId = apiId0;
                        } else if (descriptor.getIdType() == NIdType.COMPANION) {
                            apiId = apiId0;
                        }
                    }
                }
                DefaultNDefinition result = new DefaultNDefinition(
                        repo.getUuid(),
                        repo.getName(),
                        newId.getLongId(),
                        descriptor,
                        null,
                        null,
                        apiId,
                        null,
                        session
                );
                String fRepoUuid = repo.getUuid();
                String fRepoName = repo.getName();
                NId fApiId = apiId;
                NInstalledRepository installedRepository = dws.getInstalledRepository();
                CachedSupplier<String> repositoryUuid = DefaultCachedSupplier.ofMem(() -> fRepoUuid, null);
                CachedSupplier<String> repositoryName = DefaultCachedSupplier.ofMem(() -> fRepoName, null);
                CachedSupplier<NId> capiId = DefaultCachedSupplier.ofMem(() -> fApiId, null);
                CachedSupplier<NDescriptor> cdescriptor = DefaultCachedSupplier.ofMem(() -> descriptor, null);
                CachedSupplier<NPath> content =
                        DefaultCachedSupplier.of(
                                withCache ? CachedSupplier.NCacheLevel.STORE : CachedSupplier.NCacheLevel.NONE,
                                NPath.class,
                                NLocationKey.ofCache(id, "contentPath", fRepoUuid),
                                new NContentDefaultLocationSupplier(id, result, location, nutsFetchModes, installedRepository,
                                        descTracker,
                                        session),
                                new DefaultCachedSupplier.SimpleCacheValidator<>(session),
                                session
                        );
                CachedSupplier<NDescriptor> effectiveDescriptor = DefaultCachedSupplier.of(withCache ? CachedSupplier.NCacheLevel.STORE : CachedSupplier.NCacheLevel.MEM, NDescriptor.class, NLocationKey.ofCache(id, "effective-descriptor", fRepoUuid),
                        new EffectiveDescriptorSupplier(dws, cdescriptor, content, id)
                        , new DefaultCachedSupplier.SimpleCacheValidator<>(session), session
                );
                CachedSupplier<NDependencies> dependencies = DefaultCachedSupplier.of(withCache ? CachedSupplier.NCacheLevel.STORE : CachedSupplier.NCacheLevel.MEM, NDependencies.class, NLocationKey.ofCache(id, "dependencies", fRepoUuid),
                        new DependenciesSupplier(id, result, effectiveDescriptor, session), new DefaultCachedSupplier.SimpleCacheValidator<>(session), session);
                CachedSupplier<NInstallInformation> install = DefaultCachedSupplier.ofNone(new InstallSupplierSupplier(installedRepository, id));
                return new DefaultNDefinitionRef(repositoryUuid, repositoryName, id.getLongId(), cdescriptor, content, install, capiId,
                        dependencies, effectiveDescriptor,
                        session);
            }
            throw new NNotFoundException(session, id, new NNotFoundException.NIdInvalidDependency[0], new NNotFoundException.NIdInvalidLocation[]{
                    new NNotFoundException.NIdInvalidLocation(repo.getName(), null, id + " not found")
            }, null);
        };
        DefaultCachedSupplier.SimpleCacheValidator<NDefinition> validator = new DefaultCachedSupplier.SimpleCacheValidator<NDefinition>(session) {
            @Override
            public boolean isValidValue(NDefinition d) {
                NRepositories rr = NRepositories.of(session.copy().setTransitive(true));
                NRepository repositoryById = rr.findRepositoryById(d.getRepositoryUuid()).orNull();
                NRepository repositoryByName = rr.findRepositoryByName(d.getRepositoryName()).orNull();
                if (repositoryById == null || repositoryByName == null) {
                    //this is invalid cache!
                    return false;
                } else {
                    NLogUtils.traceMessage(_LOG(getSession()), nutsFetchModes, id.getLongId(), NLogVerb.CACHE, "fetch definition", 0);
                    return true;
                }
            }
        };
        return DefaultCachedSupplier.of(
                withCache ? CachedSupplier.NCacheLevel.MEM : CachedSupplier.NCacheLevel.NONE,
                NDefinition.class,
                NLocationKey.ofCache(id, "def", repo.getUuid()), supplier, validator,
                session
        ).getValue();
    }

    public static class ScopePlusOptionsCache {

        public NDependencyScope[] scopes;
        public Boolean optional;

        public int keyHashCode() {
            int s = 0;
            if (scopes != null) {
                Arrays.sort(scopes);
                for (NDependencyScope element : scopes) {
                    s = 31 * s + (element == null ? 0 : element.id().hashCode());
                }
            }
            return s * 31 + (optional == null ? 0 : optional.hashCode());
        }
    }

    private class NContentCustomLocationSupplier implements Supplier<NPath> {
        CachedSupplier<NPath> s;
        NPath copyTo;
        DefaultNDefinition foundDefinition;

        public NContentCustomLocationSupplier(NId id, DefaultNDefinition foundDefinition,
                                              NRepositoryAndFetchMode successfulDescriptorLocation,
                                              NFetchStrategy nutsFetchModes,
                                              NPath copyTo,
                                              NInstalledRepository installedRepository,
                                              NRepositoryAndFetchModeTracker descTracker,
                                              String repoUuid,
                                              DefaultCachedSupplier.CacheValidator<NPath> cacheValidator,
                                              NSession session, boolean enableCache) {
            NContentDefaultLocationSupplier s0 = new NContentDefaultLocationSupplier(id, foundDefinition, successfulDescriptorLocation, nutsFetchModes, installedRepository,
                    descTracker,
                    session);
            this.foundDefinition = foundDefinition;
            this.copyTo = copyTo;
            s = enableCache ?
                    DefaultCachedSupplier.ofStore(
                            NPath.class,
                            NLocationKey.ofCache(id, "contentPath", repoUuid),
                            s0,
                            cacheValidator,
                            session
                    ) : DefaultCachedSupplier.ofMem(s0, cacheValidator);
        }

        @Override
        public NPath get() {
            if (copyTo == null) {
                return s.getValue();
            } else {
                NPath v = s.getValue();
                if (v != null) {
                    NPath copyTo2 = copyTo;
                    if (copyTo != null && copyTo.isDirectory()) {
                        NId id1 = CoreNIdUtils.createContentFaceId(foundDefinition.getId(), foundDefinition.getDescriptor(), session);
                        copyTo2 = copyTo.resolve(NLocations.of(session).getDefaultIdFilename(id1));
                    }
                    v.copyTo(copyTo2);
                    return copyTo2;
                }
                return null;
            }
        }
    }

    private class NContentDefaultLocationSupplier implements Supplier<NPath> {
        DefaultNDefinition foundDefinition;
        NRepositoryAndFetchMode successfulDescriptorLocation;
        NFetchStrategy nutsFetchModes;
        NInstalledRepository installedRepository;
        NId id;
        NSession session;
        NRepositoryAndFetchModeTracker descTracker;

        public NContentDefaultLocationSupplier(NId id, DefaultNDefinition foundDefinition, NRepositoryAndFetchMode successfulDescriptorLocation, NFetchStrategy nutsFetchModes, NInstalledRepository installedRepository,
                                               NRepositoryAndFetchModeTracker descTracker,
                                               NSession session) {
            this.id = id;
            this.foundDefinition = foundDefinition;
            this.successfulDescriptorLocation = successfulDescriptorLocation;
            this.nutsFetchModes = nutsFetchModes;
            this.installedRepository = installedRepository;
            this.session = session;
            this.descTracker = descTracker;
        }

        @Override
        public NPath get() {
            if (!NDescriptorUtils.isNoContent(foundDefinition.getDescriptor())) {
                List<Exception> reasons = new ArrayList<>();
                boolean loadedFromInstallRepo = DefaultNInstalledRepository.INSTALLED_REPO_UUID.equals(successfulDescriptorLocation
                        .getRepository().getUuid());
                NId id1 = CoreNIdUtils.createContentFaceId(foundDefinition.getId(), foundDefinition.getDescriptor(), session);

//                        boolean escalateMode = false;
                NRepositoryAndFetchModeTracker contentTracker = new NRepositoryAndFetchModeTracker(descTracker.available());

                NPath contentSuccessful = fetchContent(id1, foundDefinition, successfulDescriptorLocation, null, reasons, session);
                NRepositoryAndFetchMode successfulContentLocation = null;
                if (contentSuccessful != null) {
                    successfulContentLocation = successfulDescriptorLocation;
                } else {
                    contentTracker.addFailure(successfulDescriptorLocation);
                }
                if (contentSuccessful == null && !loadedFromInstallRepo) {
                    if (successfulDescriptorLocation.getFetchMode() == NFetchMode.LOCAL) {
                        NRepositoryAndFetchMode finalSuccessfulDescriptorLocation = successfulDescriptorLocation;
                        NRepositoryAndFetchMode n = contentTracker.available().stream()
                                .filter(x -> x.getRepository().getUuid().equals(finalSuccessfulDescriptorLocation.getRepository().getUuid()) &&
                                        x.getFetchMode() == NFetchMode.REMOTE).findFirst().orElse(null);
                        if (n != null/* && contentTracker.accept(n)*/) {
                            contentSuccessful = fetchContent(id1, foundDefinition, n, null, reasons, session);
                            if (contentSuccessful != null) {
                                successfulContentLocation = n;
                            } else {
                                contentTracker.addFailure(n);
                            }
                        }
                    }
                }
                if (contentSuccessful == null) {
                    for (NRepositoryAndFetchMode repoAndMode : contentTracker.available()) {
                        contentSuccessful = fetchContent(id1, foundDefinition, repoAndMode, null, reasons, session);
                        if (contentSuccessful != null) {
                            successfulContentLocation = repoAndMode;
                            break;
                        } else {
                            contentTracker.addFailure(repoAndMode);
                        }
                    }
                }
                if (contentSuccessful != null) {
                    if (loadedFromInstallRepo && successfulContentLocation != successfulDescriptorLocation) {
                        //this happens if the jar content is no more installed while its descriptor is still installed.
                        NRepositorySPI installedRepositorySPI = NWorkspaceUtils.of(session).repoSPI(installedRepository);
                        installedRepositorySPI.deploy()
                                .setId(foundDefinition.getId())
                                .setDescriptor(foundDefinition.getDescriptor())
                                .setSession(this.session.copy().setConfirm(NConfirmationMode.YES))
                                //.setFetchMode(mode)
                                .setContent(foundDefinition.getContent().get(session))
                                .run();

                    }
                }
                if (contentSuccessful == null /*&& includedRemote*/) {
                    NLogUtils.traceMessage(_LOG(session), nutsFetchModes, id.getLongId(), NLogVerb.FAIL,
                            "fetched descriptor but failed to fetch artifact binaries", 0);
                }
                return contentSuccessful;
            }
            return null;
        }

        protected NPath fetchContent(NId id1, DefaultNDefinition foundDefinition, NRepositoryAndFetchMode repo, Path copyTo, List<Exception> reasons, NSession session) {
            NRepositorySPI repoSPI = NWorkspaceUtils.of(session).repoSPI(repo.getRepository());
            try {
                NPath content = repoSPI.fetchContent()
                        .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                        .setLocalPath(copyTo == null ? null : copyTo.toString())
                        .setSession(session)
                        .setFetchMode(repo.getFetchMode())
                        .getResult();
                if (content != null) {
                    return content;
                }
            } catch (NNotFoundException ex) {
                reasons.add(ex);
                //
            }
            return null;
        }
    }

    private class EffectiveDescriptorSupplier implements Supplier<NDescriptor> {
        private final NWorkspaceExt dws;
        private final CachedSupplier<NDescriptor> descriptor;
        private final CachedSupplier<NPath> contentSupplier;
        private final NId id;

        public EffectiveDescriptorSupplier(NWorkspaceExt dws, CachedSupplier<NDescriptor> descriptor, CachedSupplier<NPath> contentSupplier, NId id) {
            this.dws = dws;
            this.descriptor = descriptor;
            this.contentSupplier = contentSupplier;
            this.id = id;
        }

        @Override
        public NDescriptor get() {
            try {
                NDescriptor d = dws.resolveEffectiveDescriptor(descriptor.getValue(), session);
                NPath path = contentSupplier.getValue();
                if (path != null) {
                    d = resolveExecProperties(d, path);
                }
                return d;
            } catch (NNotFoundException ex) {
                //ignore
                _LOGOP(getSession()).level(Level.WARNING).verb(NLogVerb.WARNING)
                        .log(NMsg.ofJ("artifact descriptor found, but one of its parents or dependencies is not: {0} : missing {1}", id,
                                ex.getId()));
            }
            return null;
        }
    }

    private class DependenciesSupplier implements Supplier<NDependencies> {
        private final NId id;
        private final DefaultNDefinition d;
        private final CachedSupplier<NDescriptor> effectiveDescriptor;
        private final NSession session;

        public DependenciesSupplier(NId id, DefaultNDefinition d, CachedSupplier<NDescriptor> effectiveDescriptor, NSession session) {
            this.id = id;
            this.d = d;
            this.effectiveDescriptor = effectiveDescriptor;
            this.session = session;
        }

        @Override
        public NDependencies get() {
            DefaultNDefinition nd = d;
            if (d.getEffectiveDescriptor().isNotPresent()) {
                NDescriptor descriptorValue = effectiveDescriptor.getValue();
                nd = new DefaultNDefinition(
                        d.getRepositoryUuid(), d.getRepositoryName(),
                        d.getId(),
                        d.getDescriptor(),
                        d.getContent().orNull(),
                        d.getInstallInformation().orNull(),
                        d.getApiId(),
                        descriptorValue,
                        session
                );
            }
            return NDependencySolver.of(getSession())
                    .setFilter(buildActualDependencyFilter())
                    .add(id.toDependency(), nd)
                    .solve();
        }
    }

    private class InstallSupplierSupplier implements Supplier<NInstallInformation> {
        private final NInstalledRepository installedRepository;
        private final NId id;

        public InstallSupplierSupplier(NInstalledRepository installedRepository, NId id) {
            this.installedRepository = installedRepository;
            this.id = id;
        }

        @Override
        public NInstallInformation get() {
            NInstallInformation ii = installedRepository.getInstallInformation(id, session);
            if (ii != null) {
//                            ((DefaultNInstalledRepository) (dws.getInstalledRepository())).updateInstallInfoConfigInstallDate(id, Instant.now(), session);
                return ii;
            } else {
                return DefaultNInstallInfo.notInstalled(id);
            }
        }
    }
}
