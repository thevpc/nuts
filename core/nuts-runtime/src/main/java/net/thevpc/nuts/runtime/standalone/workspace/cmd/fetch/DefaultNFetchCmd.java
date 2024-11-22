package net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.dependency.util.NDependencyUtils;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.log.NLogUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
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
import java.util.logging.Level;

public class DefaultNFetchCmd extends AbstractNFetchCmd {


    public DefaultNFetchCmd(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    public NPath getResultContent() {
        try {
            NSession session=getWorkspace().currentSession();
            NDefinition def = fetchDefinition(getId(), copy().setContent(true).setEffective(false), true, false);
            return def.getContent().get();
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
            NSession session=getWorkspace().currentSession();
            NDefinition def = fetchDefinition(getId(), this, false, false);
            if (isEffective()) {
                return NWorkspaceExt.of().resolveEffectiveId(def.getEffectiveDescriptor().get());
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
            NSession session=getWorkspace().currentSession();
            Path f = getResultDefinition().getContent().flatMap(NPath::toPath).get();
            return NDigest.of().setSource(f).computeString();
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
            NSession session=getWorkspace().currentSession();
            return NDigest.of().setSource(getResultDescriptor()).computeString();
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
            NSession session=getWorkspace().currentSession();
            if (isEffective()) {
                return def.getEffectiveDescriptor().get();
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
        NSession session=getWorkspace().currentSession();
        NWorkspaceExt dws = NWorkspaceExt.of();
        NInstallInformation ii = dws.getInstalledRepository().getInstallInformation(getId());
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
    public NFetchCmd copy() {
        DefaultNFetchCmd b = new DefaultNFetchCmd(getWorkspace());
        b.setAll(this);
        return b;
    }

    @Override
    public NFetchCmd run() {
        getResultDefinition();
        return this;
    }

    public NDefinition fetchDefinition(NId id, NFetchCmd options, boolean includeContent, boolean includeInstallInfo) {
        NDefinition d = fetchDefinitionNoCache(id, options, includeContent, includeInstallInfo);
        return d;
    }

    public NDefinition fetchDefinitionNoCache(NId id, NFetchCmd options, boolean includeContent, boolean includeInstallInfo) {
        long startTime = System.currentTimeMillis();
        NWorkspace workspace = getWorkspace();
        NSession session= workspace.currentSession();
        NWorkspaceUtils wu = NWorkspaceUtils.of(workspace);
        CoreNIdUtils.checkLongId(id);
//        checkSession();
        NWorkspaceExt dws = NWorkspaceExt.of();
        NFetchStrategy nutsFetchModes = NWorkspaceHelper.validate(session.getFetchStrategy().orDefault());
        NRepositoryFilter repositoryFilter = this.getRepositoryFilter();
        if (!NBlankable.isBlank(id.getRepository())) {
            NRepositoryFilter repositoryFilter2 = NRepositoryFilters.of().byName(id.getRepository());
            repositoryFilter = repositoryFilter2.and(repositoryFilter);
        }
        NRepositoryAndFetchModeTracker descTracker = new NRepositoryAndFetchModeTracker(
                wu.filterRepositoryAndFetchModes(NRepositorySupportedAction.SEARCH, id, repositoryFilter,
                        nutsFetchModes)
        );

        DefaultNDefinition foundDefinition = null;
        List<Exception> reasons = new ArrayList<>();
        NRepositoryAndFetchMode successfulDescriptorLocation = null;
        NRepositoryAndFetchMode successfulContentLocation = null;
        try {
            //add env parameters to fetch adequate nuts
            id = wu.configureFetchEnv(id);
            DefaultNDefinition result = null;
            for (NRepositoryAndFetchMode location : descTracker.available()) {
                try {
                    result = fetchDescriptorAsDefinition(id, nutsFetchModes, location.getFetchMode(), location.getRepository());
                    successfulDescriptorLocation = location;
                    break;
                } catch (NNotFoundException exc) {
                    //
                    descTracker.addFailure(location);
                } catch (Exception ex) {
                    //ignore
                    _LOGOP().error(ex).level(Level.SEVERE)
                            .log(NMsg.ofC("unexpected error while fetching descriptor for %s", id));
                    if (_LOG().isLoggable(Level.FINEST)) {
                        NLogUtils.traceMessage(_LOG(), nutsFetchModes, id.getLongId(), NLogVerb.FAIL, "fetch def", startTime);
                    }
                    descTracker.addFailure(location);
                }
            }
            foundDefinition = result;
            if (foundDefinition != null) {
                if (options.isEffective() || isDependencies()) {
                    try {
                        foundDefinition.setEffectiveDescriptor(dws.resolveEffectiveDescriptor(foundDefinition.getDescriptor()));
                    } catch (NNotFoundException ex) {
                        //ignore
                        _LOGOP().level(Level.WARNING).verb(NLogVerb.WARNING)
                                .log(NMsg.ofC("artifact descriptor found, but one of its parents or dependencies is not: %s : missing %s", id,
                                        ex.getId()));
                        foundDefinition = null;
                    }
                }
                if (foundDefinition != null) {
                    if (isDependencies()) {
                        foundDefinition.setDependencies(
                                NDependencySolver.of()
                                        .setDependencyFilter(buildActualDependencyFilter())
                                        .add(id.toDependency(), foundDefinition)
                                        .setRepositoryFilter(this.getRepositoryFilter())
                                        .solve()
                        );
                    }
                    //boolean includeContent = shouldIncludeContent(options);
                    // always ok for content, if 'content' flag is not armed, try find 'local' path
                    NInstalledRepository installedRepository = dws.getInstalledRepository();
                    if (includeContent) {
                        if (!NDescriptorUtils.isNoContent(foundDefinition.getDescriptor())) {
                            boolean loadedFromInstallRepo = DefaultNInstalledRepository.INSTALLED_REPO_UUID.equals(successfulDescriptorLocation
                                    .getRepository().getUuid());
                            NId id1 = CoreNIdUtils.createContentFaceId(foundDefinition.getId(), foundDefinition.getDescriptor());
//                        boolean escalateMode = false;
                            boolean contentSuccessful = false;
                            NRepositoryAndFetchModeTracker contentTracker = new NRepositoryAndFetchModeTracker(descTracker.available());
                            NPath fetchedPath = fetchContent(id1, foundDefinition, successfulDescriptorLocation, reasons);
                            contentSuccessful = fetchedPath != null;
                            if (contentSuccessful) {
                                successfulContentLocation = successfulDescriptorLocation;
                            } else {
                                contentTracker.addFailure(successfulDescriptorLocation);
                            }
                            if (!contentSuccessful && !loadedFromInstallRepo) {
                                if (successfulDescriptorLocation.getFetchMode() == NFetchMode.LOCAL) {
                                    NRepositoryAndFetchMode finalSuccessfulDescriptorLocation = successfulDescriptorLocation;
                                    NRepositoryAndFetchMode n = contentTracker.available().stream()
                                            .filter(x -> x.getRepository().getUuid().equals(finalSuccessfulDescriptorLocation.getRepository().getUuid()) &&
                                                    x.getFetchMode() == NFetchMode.REMOTE).findFirst().orElse(null);
                                    if (n != null/* && contentTracker.accept(n)*/) {
                                        fetchedPath = fetchContent(id1, foundDefinition, n, reasons);
                                        contentSuccessful = fetchedPath != null;
                                        if (contentSuccessful) {
                                            successfulContentLocation = n;
                                        } else {
                                            contentTracker.addFailure(n);
                                        }
                                    }
                                }
                            }
                            if (!contentSuccessful) {
                                for (NRepositoryAndFetchMode repoAndMode : contentTracker.available()) {
                                    fetchedPath = fetchContent(id1, foundDefinition, repoAndMode, reasons);
                                    contentSuccessful = fetchedPath != null;
                                    if (contentSuccessful) {
                                        successfulContentLocation = repoAndMode;
                                        break;
                                    } else {
                                        contentTracker.addFailure(repoAndMode);
                                    }
                                }
                            }
                            if (contentSuccessful) {
                                if (loadedFromInstallRepo && successfulContentLocation != successfulDescriptorLocation) {
                                    //this happens if the jar content is no more installed while its descriptor is still installed.
                                    NRepositorySPI installedRepositorySPI = wu.repoSPI(installedRepository);

                                    DefaultNDefinition finalFoundDefinition = foundDefinition;
                                    session.copy().setConfirm(NConfirmationMode.YES).runWith(()->{
                                        installedRepositorySPI.deploy()
                                                .setId(finalFoundDefinition.getId())
                                                .setDescriptor(finalFoundDefinition.getDescriptor())
                                                //.setFetchMode(mode)
                                                .setContent(finalFoundDefinition.getContent().get())
                                                .run();
                                    });

                                }
                            }
                            if (!contentSuccessful /*&& includedRemote*/) {
                                NLogUtils.traceMessage(_LOG(), nutsFetchModes, id.getLongId(), NLogVerb.FAIL,
                                        "fetched descriptor but failed to fetch artifact binaries", startTime);
                            }
                        }
                    }
                    if (includeInstallInfo) {
                        //will always load install information
                        NInstallInformation ii = installedRepository.getInstallInformation(id);
                        if (ii != null) {
//                            ((DefaultNInstalledRepository) (dws.getInstalledRepository())).updateInstallInfoConfigInstallDate(id, Instant.now(), session);
                            foundDefinition.setInstallInformation(ii);
                        } else {
                            foundDefinition.setInstallInformation(DefaultNInstallInfo.notInstalled(id));
                        }
                    }
                }
            }
        } catch (NNotFoundException ex) {
            reasons.add(ex);
            NLogUtils.traceMessage(_LOG(), nutsFetchModes, id.getLongId(), NLogVerb.FAIL, "fetch definition", startTime);
            throw ex;
        } catch (RuntimeException ex) {
            NLogUtils.traceMessage(_LOG(), nutsFetchModes, id.getLongId(), NLogVerb.FAIL, "[unexpected] fetch definition", startTime);
            throw ex;
        }
        if (foundDefinition != null) {
//            if (session.isTrace()) {
//                NutsIterableOutput ff = CoreNutsUtils.getValidOutputFormat(session)
//                        .session(session);
//                ff.start();
//                ff.next(foundDefinition);
//                ff.complete();
//            }
            return foundDefinition;
        }
        throw new NNotFoundException(id);
    }

    private NDependencyFilter buildActualDependencyFilter() {
        NSession session=getWorkspace().currentSession();
        NDependencyFilters ff = NDependencyFilters.of();
        return ff.byScope(getScope())
                .and(ff.byOptional(getOptional())
                ).and(getDependencyFilter());
    }

    protected NPath fetchContent(NId id1, DefaultNDefinition foundDefinition, NRepository repo0, NFetchStrategy nutsFetchModes, List<Exception> reasons) {
        NSession session=getWorkspace().currentSession();
        NRepositorySPI repoSPI = NWorkspaceUtils.of(getWorkspace()).repoSPI(repo0);
        for (NFetchMode mode : nutsFetchModes) {
            try {
                NPath content = repoSPI.fetchContent()
                        .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                        .setFetchMode(mode)
                        .getResult();
                if (content != null) {
                    content = repoSPI.fetchContent()
                            .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                            .setFetchMode(mode)
                            .getResult();
                    foundDefinition.setContent(content);
                    foundDefinition.setDescriptor(resolveExecProperties(foundDefinition.getDescriptor(), content));
                    return content;
                }
            } catch (NNotFoundException ex) {
                reasons.add(ex);
                //
            }
        }
        return null;
    }

    protected NPath fetchContent(NId id1, DefaultNDefinition foundDefinition, NRepositoryAndFetchMode repo, List<Exception> reasons) {
        NSession session=getWorkspace().currentSession();
        NRepositorySPI repoSPI = NWorkspaceUtils.of(getWorkspace()).repoSPI(repo.getRepository());
        try {
            NPath content = repoSPI.fetchContent()
                    .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                    .setFetchMode(repo.getFetchMode())
                    .getResult();
            if (content != null) {
                foundDefinition.setContent(content);
                foundDefinition.setDescriptor(resolveExecProperties(foundDefinition.getDescriptor(), content));
                return content;
            }
        } catch (NNotFoundException ex) {
            reasons.add(ex);
            //
        }
        return null;
    }

    protected NDescriptor resolveExecProperties(NDescriptor nutsDescriptor, NPath jar) {
        NSession session=getWorkspace().currentSession();
        boolean executable = nutsDescriptor.isExecutable();
        boolean nutsApp = nutsDescriptor.isApplication();
        if (jar.getName().toLowerCase().endsWith(".jar") && jar.isRegularFile()) {
            NPath cachePath = NLocations.of().getStoreLocation(nutsDescriptor.getId(), NStoreType.CACHE)
                    .resolve(NLocations.of().getDefaultIdFilename(nutsDescriptor.getId()
                                    .builder()
                                    .setFace("info.cache")
                                    .build()
                            )
                    );
            Map<String, String> map = null;
            NElements elem = NElements.of();
            try {
                if (cachePath.isRegularFile()) {
                    map = elem
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
                    List<NExecutionEntry> t = NExecutionEntry.parse(jar);
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
                        elem.json().setValue(map)
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

    protected DefaultNDefinition fetchDescriptorAsDefinition(NId id, NFetchStrategy nutsFetchModes, NFetchMode mode, NRepository repo) {
        NSession session=getWorkspace().currentSession();
        NWorkspaceExt dws = NWorkspaceExt.of();
        boolean withCache = !(repo instanceof DefaultNInstalledRepository) && session.isCached();
        NPath cachePath = null;
        NWorkspaceUtils wu = NWorkspaceUtils.of(getWorkspace());
        NElements elem = NElements.of();
        if (withCache) {
            cachePath = NLocations.of().getStoreLocation(id, NStoreType.CACHE, repo.getUuid())
                    .resolve(NLocations.of().getDefaultIdFilename(id.builder().setFace("def.cache").build()));
            if (cachePath.isRegularFile()) {
                try {
                    if (CoreIOUtils.isObsoletePath(cachePath)) {
                        //this is invalid cache!
                        cachePath.delete();
                    } else {
                        DefaultNDefinition d = elem
                                .json().parse(cachePath, DefaultNDefinition.class);
                        if (d != null) {
                            NRepositories rr = NRepositories.of();
                            NRepository repositoryById = rr.findRepositoryById(d.getRepositoryUuid()).orNull();
                            NRepository repositoryByName = rr.findRepositoryByName(d.getRepositoryName()).orNull();
                            if (repositoryById == null || repositoryByName == null) {
                                //this is invalid cache!
                                cachePath.delete();
                            } else {
                                NLogUtils.traceMessage(_LOG(), nutsFetchModes, id.getLongId(), NLogVerb.CACHE, "fetch definition", 0);
                                return d;
                            }
                        }
                    }
                } catch (Exception ex) {
                    //
                }
            }
        }

        NRepositorySPI repoSPI = wu.repoSPI(repo);
        NDescriptor descriptor = repoSPI.fetchDescriptor().setId(id)
                .setFetchMode(mode)
                .getResult();
        if (descriptor != null) {
            NId nutsId = dws.resolveEffectiveId(descriptor);
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
                    apiId, getWorkspace()
            );
            if (withCache) {
                try {
                    elem.json().setValue(result)
                            .setNtf(false).print(cachePath);
                } catch (Exception ex) {
                    //
                }
            }
            return result;
        }
        throw new NNotFoundException(id, new NNotFoundException.NIdInvalidDependency[0], new NNotFoundException.NIdInvalidLocation[]{
                new NNotFoundException.NIdInvalidLocation(repo.getName(), null, id + " not found")
        }, null);
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
}
