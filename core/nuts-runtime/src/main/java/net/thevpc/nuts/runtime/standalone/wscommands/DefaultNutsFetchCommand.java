package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.commands.repo.NutsRepositorySupportedAction;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDefinition;
import net.thevpc.nuts.runtime.core.repos.NutsInstalledRepository;
import net.thevpc.nuts.runtime.core.util.CoreBooleanUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.DefaultNutsInstallInfo;
import net.thevpc.nuts.runtime.standalone.repos.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.util.NutsDependencyScopes;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceHelper;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

public class DefaultNutsFetchCommand extends AbstractNutsFetchCommand {


    public DefaultNutsFetchCommand(NutsWorkspace ws) {
        super(ws);
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
            checkSession();
            NutsWorkspace ws = getSession().getWorkspace();
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
            checkSession();
            NutsWorkspace ws = getSession().getWorkspace();
            Path f = getResultDefinition().getPath();
            return ws.io().hash().setSource(f).computeString();
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
            checkSession();
            return getSession().getWorkspace().io().hash().setSource(getResultDescriptor()).computeString();
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
            return fetchDefinition(getId(), this, isContent(), true);
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
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
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
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        DefaultNutsFetchCommand b = new DefaultNutsFetchCommand(ws);
        b.copyFrom(this);
        return b;
    }

    @Override
    public NutsFetchCommand run() {
        getResultDefinition();
        return this;
    }

    public NutsDefinition fetchDefinition(NutsId id, NutsFetchCommand options, boolean includeContent, boolean includeInstallInfo) {
        NutsDefinition d = fetchDefinitionNoCache(id, options, includeContent, includeInstallInfo);
        return d;
    }

    public NutsDefinition fetchDefinitionNoCache(NutsId id, NutsFetchCommand options, boolean includeContent, boolean includeInstallInfo) {
        long startTime = System.currentTimeMillis();
        checkSession();
        NutsWorkspaceUtils wu = NutsWorkspaceUtils.of(session);
        wu.checkLongNameNutsId(id, session);
        checkSession();
        NutsWorkspace _ws = getSession().getWorkspace();
        NutsWorkspaceUtils.checkSession(_ws, options.getSession());
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(_ws);
        NutsFetchStrategy nutsFetchModes = NutsWorkspaceHelper.validate(session.getFetchStrategy());
        NutsRepositoryFilter repositoryFilter = this.getRepositoryFilter();

        NutsRepositoryAndFetchModeTracker descTracker = new NutsRepositoryAndFetchModeTracker(
                wu.filterRepositoryAndFetchModes(NutsRepositorySupportedAction.SEARCH, id, repositoryFilter,
                        nutsFetchModes, session)
        );

        DefaultNutsDefinition foundDefinition = null;
        List<Exception> reasons = new ArrayList<>();
        NutsRepositoryAndFetchMode successfulDescriptorLocation = null;
        NutsRepositoryAndFetchMode successfulContentLocation = null;
        try {
            //add env parameters to fetch adequate nuts
            id = wu.configureFetchEnv(id);
            DefaultNutsDefinition result = null;
            for (NutsRepositoryAndFetchMode location : descTracker.available()) {
                try {
                    result = fetchDescriptorAsDefinition(id, session, nutsFetchModes, location.getFetchMode(), location.getRepository());
                    successfulDescriptorLocation = location;
                    break;
                } catch (NutsNotFoundException exc) {
                    //
                    descTracker.addFailure(location);
                } catch (Exception ex) {
                    //ignore
                    _LOGOP(getSession()).error(ex).level(Level.SEVERE).log("unexpected error while fetching descriptor for {0}", id);
                    if (_LOG(getSession()).isLoggable(Level.FINEST)) {
                        wu.traceMessage(nutsFetchModes, id.getLongNameId(), NutsLogVerb.FAIL, "fetch def", startTime);
                    }
                    descTracker.addFailure(location);
                }
            }
            foundDefinition = result;
            if (foundDefinition != null) {
                if (options.isEffective() || isDependencies()) {
                    try {
                        foundDefinition.setEffectiveDescriptor(dws.resolveEffectiveDescriptor(foundDefinition.getDescriptor(), session));
                    } catch (NutsNotFoundException ex) {
                        //ignore
                        _LOGOP(getSession()).level(Level.WARNING).verb(NutsLogVerb.WARNING).log("artifact descriptor found, but its parent is not: {0} with parent {1}", id.getLongName(), Arrays.toString(foundDefinition.getDescriptor().getParents()));
                        foundDefinition = null;
                    }
                }
                if (foundDefinition != null) {
                    if (isDependencies()) {
                        foundDefinition.setDependencies(
                                new NutsDependenciesResolver(CoreNutsUtils.silent(getSession()))
                                        .setDependencyFilter(buildActualDependencyFilter())
                                        .addRootDefinition(id.toDependency(), foundDefinition)
                                        .resolve()
                        );
                    }
                    //boolean includeContent = shouldIncludeContent(options);
                    // always ok for content, if 'content' flag is not armed, try find 'local' path
                    NutsInstalledRepository installedRepository = dws.getInstalledRepository();
                    if (includeContent) {
                        boolean loadedFromInstallRepo = DefaultNutsInstalledRepository.INSTALLED_REPO_UUID.equals(successfulDescriptorLocation
                                .getRepository().getUuid());
                        NutsId id1 = _ws.config().createContentFaceId(foundDefinition.getId(), foundDefinition.getDescriptor());
                        Path copyTo = options.getLocation();
                        if (copyTo != null && Files.isDirectory(copyTo)) {
                            copyTo = copyTo.resolve(_ws.locations().getDefaultIdFilename(id1));
                        }
//                        boolean escalateMode = false;
                        boolean contentSuccessful = false;
                        NutsRepositoryAndFetchModeTracker contentTracker = new NutsRepositoryAndFetchModeTracker(descTracker.available());

                        contentSuccessful = fetchContent(id1, foundDefinition, successfulDescriptorLocation, copyTo, reasons);
                        if (contentSuccessful) {
                            successfulContentLocation = successfulDescriptorLocation;
                        } else {
                            contentTracker.addFailure(successfulDescriptorLocation);
                        }
                        if (!contentSuccessful && !loadedFromInstallRepo) {
                            if (successfulDescriptorLocation.getFetchMode() == NutsFetchMode.LOCAL) {
                                NutsRepositoryAndFetchMode finalSuccessfulDescriptorLocation = successfulDescriptorLocation;
                                NutsRepositoryAndFetchMode n = contentTracker.available().stream()
                                        .filter(x -> x.getRepository().getUuid().equals(finalSuccessfulDescriptorLocation.getRepository().getUuid()) &&
                                                x.getFetchMode() == NutsFetchMode.REMOTE).findFirst().orElse(null);
                                if (n != null/* && contentTracker.accept(n)*/) {
                                    contentSuccessful = fetchContent(id1, foundDefinition, n, copyTo, reasons);
                                    if (contentSuccessful) {
                                        successfulContentLocation = n;
                                    } else {
                                        contentTracker.addFailure(n);
                                    }
                                }
                            }
                        }
                        if (!contentSuccessful) {
                            for (NutsRepositoryAndFetchMode repoAndMode : contentTracker.available()) {
                                contentSuccessful = fetchContent(id1, foundDefinition, repoAndMode, copyTo, reasons);
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
                                NutsRepositorySPI installedRepositorySPI = wu.repoSPI(installedRepository);
                                installedRepositorySPI.deploy()
                                        .setId(foundDefinition.getId())
                                        .setDescriptor(foundDefinition.getDescriptor())
                                        .setSession(this.session.copy().setConfirm(NutsConfirmationMode.YES))
                                        //.setFetchMode(mode)
                                        .setContent(foundDefinition.getContent().getFilePath())
                                        .run();

                            }
                        }
                        if (!contentSuccessful /*&& includedRemote*/) {
                            wu.traceMessage(nutsFetchModes, id.getLongNameId(), NutsLogVerb.FAIL,
                                    "fetched descriptor but failed to fetch artifact binaries", startTime);
                        }
                    }
                    if (foundDefinition != null && includeInstallInfo) {
                        //will always load install information
                        NutsInstallInformation ii = installedRepository.getInstallInformation(id, this.session);
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
            wu.traceMessage(nutsFetchModes, id.getLongNameId(), NutsLogVerb.FAIL, "fetch definition", startTime);
            throw ex;
        } catch (RuntimeException ex) {
            wu.traceMessage(nutsFetchModes, id.getLongNameId(), NutsLogVerb.FAIL, "[unexpected] fetch definition", startTime);
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
        throw new NutsNotFoundException(getSession(), id);
    }

    private NutsDependencyFilter buildActualDependencyFilter() {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        NutsDependencyFilterManager ff = ws.dependency().filter();
        return ff.byScope(getScope())
                .and(ff.byOptional(getOptional())
                ).and(getDependencyFilter());
    }

//    private boolean shouldIncludeContent(NutsFetchCommand options) {
//        boolean includeContent = options.isContent();
//        if (options instanceof DefaultNutsQueryBaseOptions) {
//            if (((DefaultNutsQueryBaseOptions) options).getDisplayOptions().isRequireDefinition()) {
//                includeContent = true;
//            }
//        }
//        return includeContent;
//    }

    protected boolean fetchContent(NutsId id1, DefaultNutsDefinition foundDefinition, NutsRepository repo0, NutsFetchStrategy nutsFetchModes, Path copyTo, List<Exception> reasons) {
        NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(session).repoSPI(repo0);
        for (NutsFetchMode mode : nutsFetchModes) {
            try {
                NutsContent content = repoSPI.fetchContent()
                        .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                        .setLocalPath(copyTo == null ? null : copyTo.toString())
                        .setSession(session)
                        .setFetchMode(mode)
                        .getResult();
                if (content != null) {
                    if (content.getFilePath() == null) {
                        content = repoSPI.fetchContent()
                                .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                                .setLocalPath(copyTo == null ? null : copyTo.toString())
                                .setSession(session)
                                .setFetchMode(mode)
                                .getResult();
                    }
                    foundDefinition.setContent(content);
                    foundDefinition.setDescriptor(resolveExecProperties(foundDefinition.getDescriptor(), content.getFilePath()));
                    return true;
                }
            } catch (NutsNotFoundException ex) {
                reasons.add(ex);
                //
            }
        }
        return false;
    }

    protected boolean fetchContent(NutsId id1, DefaultNutsDefinition foundDefinition, NutsRepositoryAndFetchMode repo, Path copyTo, List<Exception> reasons) {
        NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(getSession()).repoSPI(repo.getRepository());
        try {
            NutsContent content = repoSPI.fetchContent()
                    .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                    .setLocalPath(copyTo == null ? null : copyTo.toString())
                    .setSession(session)
                    .setFetchMode(repo.getFetchMode())
                    .getResult();
            if (content != null) {
                if (content.getFilePath() == null) {
                    content = repoSPI.fetchContent()
                            .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                            .setLocalPath(copyTo == null ? null : copyTo.toString())
                            .setSession(session)
                            .setFetchMode(repo.getFetchMode())
                            .getResult();
                }
                foundDefinition.setContent(content);
                foundDefinition.setDescriptor(resolveExecProperties(foundDefinition.getDescriptor(), content.getFilePath()));
                return true;
            }
        } catch (NutsNotFoundException ex) {
            reasons.add(ex);
            //
        }
        return false;
    }

    protected NutsDescriptor resolveExecProperties(NutsDescriptor nutsDescriptor, Path jar) {
        checkSession();
        boolean executable = nutsDescriptor.isExecutable();
        boolean nutsApp = nutsDescriptor.isApplication();
        NutsWorkspace ws = getSession().getWorkspace();
        if (jar.getFileName().toString().toLowerCase().endsWith(".jar") && Files.isRegularFile(jar)) {
            Path cachePath = Paths.get(ws.locations().getStoreLocation(nutsDescriptor.getId(), NutsStoreLocation.CACHE))
                    .resolve(ws.locations().getDefaultIdFilename(nutsDescriptor.getId()
                                    .builder()
                                    .setFace("info.cache")
                                    .build()
                            )
                    );
            Map<String, String> map = null;
            try {
                if (Files.isRegularFile(cachePath)) {
                    map = ws.elem().setSession(session)
                            .setContentType(NutsContentType.JSON).parse(cachePath, Map.class);
                }
            } catch (Exception ex) {
                //
            }
            if (map != null) {
                executable = "true".equals(map.get("executable"));
                nutsApp = "true".equals(map.get("nutsApplication"));
            } else {
                try {
                    NutsExecutionEntry[] t = ws.apps().execEntries().setSession(getSession()).parse(jar);
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
                        ws.elem().setContentType(NutsContentType.JSON).setSession(getSession()).setValue(map).print(cachePath);
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

    protected DefaultNutsDefinition fetchDescriptorAsDefinition(NutsId id, NutsSession session, NutsFetchStrategy nutsFetchModes, NutsFetchMode mode, NutsRepository repo) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        NutsWorkspaceUtils.checkSession(ws, session);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        boolean withCache = !(repo instanceof DefaultNutsInstalledRepository);
        Path cachePath = null;
        NutsWorkspaceUtils wu = NutsWorkspaceUtils.of(session);
        if (withCache) {
            cachePath = Paths.get(ws.locations().getStoreLocation(id, NutsStoreLocation.CACHE, repo.getUuid()))
                    .resolve(ws.locations().getDefaultIdFilename(id.builder().setFace("def.cache").build()));
            if (Files.isRegularFile(cachePath)) {
                try {
                    if (CoreIOUtils.isObsoleteInstant(session, Files.getLastModifiedTime(cachePath).toInstant())) {
                        //this is invalid cache!
                        Files.delete(cachePath);
                    } else {
                        DefaultNutsDefinition d = ws.elem().setSession(session).setContentType(NutsContentType.JSON).parse(cachePath, DefaultNutsDefinition.class);
                        if (d != null) {
                            NutsRepositoryManager rr = ws.repos().setSession(session.copy().setTransitive(true));
                            NutsRepository repositoryById = rr.findRepositoryById(d.getRepositoryUuid());
                            NutsRepository repositoryByName = rr.findRepositoryByName(d.getRepositoryName());
                            if (repositoryById == null || repositoryByName == null) {
                                //this is invalid cache!
                                Files.delete(cachePath);
                            } else {
                                wu.traceMessage(nutsFetchModes, id.getLongNameId(), NutsLogVerb.CACHE, "fetch definition", 0);
                                return d;
                            }
                        }
                    }
                } catch (Exception ex) {
                    //
                }
            }
        }

        NutsRepositorySPI repoSPI = wu.repoSPI(repo);
        NutsDescriptor descriptor = repoSPI.fetchDescriptor().setId(id)
                .setSession(session).setFetchMode(mode)
                .getResult();
        if (descriptor != null) {
            NutsId nutsId = dws.resolveEffectiveId(descriptor, session);
            NutsIdBuilder newIdBuilder = nutsId.builder();
            if (NutsUtilStrings.isBlank(newIdBuilder.getRepository())) {
                newIdBuilder.setRepository(repo.getName());
            }
            //inherit classifier from requested parse
            String classifier = id.getClassifier();
            if (!NutsUtilStrings.isBlank(classifier)) {
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
                            && NutsDependencyScopes.isCompileScope(dependency.getScope())) {
                        apiId0 = dependency.toId().getLongNameId();
                    }
                }
                if (apiId0 != null) {
                    if (getId().getShortName().equals(NutsConstants.Ids.NUTS_RUNTIME)) {
                        idType = NutsIdType.RUNTIME;
                        apiId = apiId0;
                    } else {
                        if (CoreBooleanUtils.parseBoolean(descriptor.getProperties().get("nuts-runtime"), false, false)) {
                            idType = NutsIdType.RUNTIME;
                        } else if (CoreBooleanUtils.parseBoolean(descriptor.getProperties().get("nuts-extension"), false, false)) {
                            idType = NutsIdType.EXTENSION;
                            apiId = apiId0;
                        }
                    }
                    if (idType == NutsIdType.REGULAR) {
                        for (NutsId companionTool : ws.getCompanionIds(session)) {
                            if (companionTool.getShortName().equals(getId().getShortName())) {
                                idType = NutsIdType.COMPANION;
                                apiId = apiId0;
                            }
                        }
                    }
                }
            }

            DefaultNutsDefinition result = new DefaultNutsDefinition(
                    repo.getUuid(),
                    repo.getName(),
                    newId,
                    descriptor,
                    null,
                    null,
                    idType, apiId, session
            );
            if (withCache) {
                try {
                    ws.elem().setContentType(NutsContentType.JSON).setSession(session).setValue(result).setNtf(false).print(cachePath);
                } catch (Exception ex) {
                    //
                }
            }
            return result;
        }
        throw new NutsNotFoundException(session, id);
    }

    public static class ScopePlusOptionsCache {

        public NutsDependencyScope[] scopes;
        public Boolean optional;

        public int keyHashCode() {
            int s = 0;
            if (scopes != null) {
                Arrays.sort(scopes);
                for (NutsDependencyScope element : scopes) {
                    s = 31 * s + (element == null ? 0 : element.id().hashCode());
                }
            }
            return s * 31 + (optional == null ? 0 : optional.hashCode());
        }
    }
}
