package net.thevpc.nuts.runtime.standalone.wscommands.fetch;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.commands.repo.NutsRepositorySupportedAction;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDefinition;
import net.thevpc.nuts.runtime.core.repos.NutsInstalledRepository;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.DefaultNutsInstallInfo;
import net.thevpc.nuts.runtime.standalone.repos.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.util.NutsDependencyScopes;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceHelper;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.NutsRepositoryAndFetchMode;
import net.thevpc.nuts.runtime.standalone.wscommands.NutsRepositoryAndFetchModeTracker;
import net.thevpc.nuts.spi.NutsDependencySolver;
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
            NutsSession ws = getSession();
            Path f = getResultDefinition().getPath();
            return NutsHash.of(ws).setSource(f).computeString();
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
            return NutsHash.of(getSession()).setSource(getResultDescriptor()).computeString();
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
        wu.checkLongId(id, session);
        checkSession();
        NutsSession _ws = getSession();
        NutsWorkspaceUtils.checkSession(this.ws, options.getSession());
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(_ws);
        NutsFetchStrategy nutsFetchModes = NutsWorkspaceHelper.validate(_ws.getFetchStrategy());
        NutsRepositoryFilter repositoryFilter = this.getRepositoryFilter();
        if(!NutsBlankable.isBlank(id.getRepository())){
            NutsRepositoryFilter repositoryFilter2=NutsRepositoryFilters.of(_ws).byName(id.getRepository());
            repositoryFilter=repositoryFilter2.and(repositoryFilter);
        }
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
                    _LOGOP(getSession()).error(ex).level(Level.SEVERE)
                            .log(NutsMessage.jstyle("unexpected error while fetching descriptor for {0}", id));
                    if (_LOG(getSession()).isLoggable(Level.FINEST)) {
                        wu.traceMessage(nutsFetchModes, id.getLongId(), NutsLogVerb.FAIL, "fetch def", startTime);
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
                        _LOGOP(getSession()).level(Level.WARNING).verb(NutsLogVerb.WARNING)
                                .log(NutsMessage.jstyle("artifact descriptor found, but its parent is not: {0} with parent {1}", id.getLongName(), Arrays.toString(foundDefinition.getDescriptor().getParents())));
                        foundDefinition = null;
                    }
                }
                if (foundDefinition != null) {
                    if (isDependencies()) {
                        foundDefinition.setDependencies(
                                NutsDependencySolver.of(getSession())
                                        .setFilter(buildActualDependencyFilter())
                                        .add(id.toDependency(), foundDefinition)
                                        .solve()
                        );
                    }
                    //boolean includeContent = shouldIncludeContent(options);
                    // always ok for content, if 'content' flag is not armed, try find 'local' path
                    NutsInstalledRepository installedRepository = dws.getInstalledRepository();
                    if (includeContent) {
                        boolean loadedFromInstallRepo = DefaultNutsInstalledRepository.INSTALLED_REPO_UUID.equals(successfulDescriptorLocation
                                .getRepository().getUuid());
                        NutsId id1 = session.config().createContentFaceId(foundDefinition.getId(), foundDefinition.getDescriptor());
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
                                        .setContent(foundDefinition.getContent().getFile())
                                        .run();

                            }
                        }
                        if (!contentSuccessful /*&& includedRemote*/) {
                            wu.traceMessage(nutsFetchModes, id.getLongId(), NutsLogVerb.FAIL,
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
            wu.traceMessage(nutsFetchModes, id.getLongId(), NutsLogVerb.FAIL, "fetch definition", startTime);
            throw ex;
        } catch (RuntimeException ex) {
            wu.traceMessage(nutsFetchModes, id.getLongId(), NutsLogVerb.FAIL, "[unexpected] fetch definition", startTime);
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
        NutsDependencyFilters ff = NutsDependencyFilters.of(getSession());
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
                    if (content.getFile() == null) {
                        content = repoSPI.fetchContent()
                                .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                                .setLocalPath(copyTo == null ? null : copyTo.toString())
                                .setSession(session)
                                .setFetchMode(mode)
                                .getResult();
                    }
                    foundDefinition.setContent(content);
                    foundDefinition.setDescriptor(resolveExecProperties(foundDefinition.getDescriptor(), content.getFile()));
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
                if (content.getFile() == null) {
                    content = repoSPI.fetchContent()
                            .setId(id1).setDescriptor(foundDefinition.getDescriptor())
                            .setLocalPath(copyTo == null ? null : copyTo.toString())
                            .setSession(session)
                            .setFetchMode(repo.getFetchMode())
                            .getResult();
                }
                foundDefinition.setContent(content);
                foundDefinition.setDescriptor(resolveExecProperties(foundDefinition.getDescriptor(), content.getFile()));
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
        NutsSession session = getSession();
        if (jar.getFileName().toString().toLowerCase().endsWith(".jar") && Files.isRegularFile(jar)) {
            Path cachePath = Paths.get(session.locations().getStoreLocation(nutsDescriptor.getId(), NutsStoreLocation.CACHE))
                    .resolve(session.locations().getDefaultIdFilename(nutsDescriptor.getId()
                                    .builder()
                                    .setFace("info.cache")
                                    .build()
                            )
                    );
            Map<String, String> map = null;
            NutsElements elem = NutsElements.of(session);
            try {
                if (Files.isRegularFile(cachePath)) {
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
                    NutsExecutionEntry[] t = NutsExecutionEntries.of(session).setSession(getSession()).parse(jar);
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
        NutsDescriptorBuilder nb = nutsDescriptor.builder();
        if(executable){
            nb.addFlag(NutsDescriptorFlag.EXEC);
        }
        if(nutsApp){
            nb.addFlag(NutsDescriptorFlag.APP);
        }
        return nb.build();
    }

    protected DefaultNutsDefinition fetchDescriptorAsDefinition(NutsId id, NutsSession session, NutsFetchStrategy nutsFetchModes, NutsFetchMode mode, NutsRepository repo) {
        checkSession();
        NutsWorkspaceUtils.checkSession(this.ws, session);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(session);
        boolean withCache = !(repo instanceof DefaultNutsInstalledRepository);
        Path cachePath = null;
        NutsWorkspaceUtils wu = NutsWorkspaceUtils.of(session);
        NutsElements elem = NutsElements.of(getSession());
        if (withCache) {
            cachePath = Paths.get(session.locations().getStoreLocation(id, NutsStoreLocation.CACHE, repo.getUuid()))
                    .resolve(session.locations().getDefaultIdFilename(id.builder().setFace("def.cache").build()));
            if (Files.isRegularFile(cachePath)) {
                try {
                    if (CoreIOUtils.isObsoleteInstant(session, Files.getLastModifiedTime(cachePath).toInstant())) {
                        //this is invalid cache!
                        Files.delete(cachePath);
                    } else {
                        DefaultNutsDefinition d = elem.setSession(session)
                                .json().parse(cachePath, DefaultNutsDefinition.class);
                        if (d != null) {
                            NutsRepositoryManager rr = session.copy().setTransitive(true).repos();
                            NutsRepository repositoryById = rr.findRepositoryById(d.getRepositoryUuid());
                            NutsRepository repositoryByName = rr.findRepositoryByName(d.getRepositoryName());
                            if (repositoryById == null || repositoryByName == null) {
                                //this is invalid cache!
                                Files.delete(cachePath);
                            } else {
                                wu.traceMessage(nutsFetchModes, id.getLongId(), NutsLogVerb.CACHE, "fetch definition", 0);
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
            if (NutsBlankable.isBlank(newIdBuilder.getRepository())) {
                newIdBuilder.setRepository(repo.getName());
            }
            //inherit classifier from requested parse
            String classifier = id.getClassifier();
            if (!NutsBlankable.isBlank(classifier)) {
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
                        apiId0 = dependency.toId().getLongId();
                    }
                }
                if (apiId0 != null) {
                    if (getId().getShortName().equals(NutsConstants.Ids.NUTS_RUNTIME)) {
                        idType = NutsIdType.RUNTIME;
                        apiId = apiId0;
                    } else {
                        if (descriptor.getFlags().contains(NutsDescriptorFlag.NUTS_RUNTIME)) {
                            idType = NutsIdType.RUNTIME;
                        } else if (descriptor.getFlags().contains(NutsDescriptorFlag.NUTS_EXTENSION)) {
                            idType = NutsIdType.EXTENSION;
                            apiId = apiId0;
                        }
                    }
                    if (idType == NutsIdType.REGULAR) {
                        for (NutsId companionTool : session.extensions().getCompanionIds()) {
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
                    newId.getLongId(),
                    descriptor,
                    null,
                    null,
                    idType, apiId, session
            );
            if (withCache) {
                try {
                    elem.json().setSession(session).setValue(result)
                            .setNtf(false).print(cachePath);
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
