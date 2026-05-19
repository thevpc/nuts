package net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch;

import net.thevpc.nuts.core.*;


import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.*;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.core.NRepositoryFilters;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinitionBuilder2;
import net.thevpc.nuts.runtime.standalone.dependency.util.NDependencyUtils;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.log.NLogUtils;
import net.thevpc.nuts.runtime.standalone.util.ValueSupplier;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
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
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.text.NMsg;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNFetch extends AbstractNFetch {


    public DefaultNFetch() {
        super();
    }

    @Override
    public NPath getResultContent() {
        try {
            NDefinition def = fetchDefinition(id());
            if (def.descriptor().isNoContent()) {
                return null;
            }
            if (!def.content().isPresent()) {
                if (!isFailFast()) {
                    return null;
                }
                throw new NArtifactNotFoundException(id(), NMsg.ofC("missing content for %s", id()));
            }
            return def.content().get();
        } catch (NArtifactNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NId getResultId() {
        try {
            NDefinition def = fetchDefinition(id());
//            if (isEffective()) {
//                return NWorkspaceExt.of().resolveEffectiveId(def.getEffectiveDescriptor().get());
//            }
            return def.id();
        } catch (NArtifactNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public String getResultContentHash() {
        try {
            Path f = getResultDefinition().content().flatMap(NPath::toPath).get();
            return NDigest.of().source(f).computeString();
        } catch (NArtifactNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public String getResultDescriptorHash() {
        try {
            return NDigest.of().source(getResultDescriptor()).computeString();
        } catch (NArtifactNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NDefinition getResultDefinition() {
        try {
            return fetchDefinition(id());
        } catch (NArtifactNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NDescriptor getResultDescriptor() {
        try {
            NDefinition def = fetchDefinition(id());
//            if (isEffective()) {
//                return def.getEffectiveDescriptor().get();
//            }
            return def.descriptor();
        } catch (NArtifactNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NDescriptor getResultEffectiveDescriptor() {
        try {
            NDefinition def = fetchDefinition(id());
            return def.effectiveDescriptor().get();
        } catch (NArtifactNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NInstallInformation getResultInstallInformation() {
        NWorkspaceExt dws = NWorkspaceExt.of();
        NInstallInformation ii = dws.getInstalledRepository().getInstallInformation(id());
        if (ii != null) {
            return ii;
        } else {
            return DefaultNInstallInfo.notInstalled(id());
        }
    }

    public NPath getResultPath() {
        try {
            NDefinition def = fetchDefinition(id());
            return def.content().orNull();
        } catch (NArtifactNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NFetch copy() {
        DefaultNFetch b = new DefaultNFetch();
        b.copyFrom(this);
        return b;
    }

    @Override
    public NFetch run() {
        getResultDefinition();
        return this;
    }

    public NDefinition fetchDefinition(NId id) {
        long startTime = System.currentTimeMillis();
        NSession session = NSession.of();
        NWorkspaceUtils wu = NWorkspaceUtils.of();
        CoreNIdUtils.checkLongId(id);
        if (NDependencyScope.parse(id.toDependency().scope()).orNull() == NDependencyScope.SYSTEM) {
            // TODO, fix me
            //just ignore or should we still support it?
            throw new NArtifactNotFoundException(id.longId());
        }
        NWorkspaceExt dws = NWorkspaceExt.of();
        NFetchStrategy nutsFetchModes = NWorkspaceHelper.validate(session.fetchStrategy().orDefault());
        NRepositoryFilter repositoryFilter = this.repositoryFilter();
        if (!NBlankable.isBlank(id.repository())) {
            NRepositoryFilter repositoryFilter2 = NRepositoryFilters.of().byName(id.repository());
            repositoryFilter = repositoryFilter2.and(repositoryFilter);
        }
        NRepositoryAndFetchModeTracker descTracker = new NRepositoryAndFetchModeTracker(
                wu.filterRepositoryAndFetchModes(NRepositorySupportedAction.SEARCH, id, repositoryFilter,
                        nutsFetchModes)
        );

        DefaultNDefinitionBuilder2 foundDefinitionBuilder = null;
        List<Exception> reasons = new ArrayList<>();
        NRepositoryAndFetchMode successfulDescriptorLocation = null;
        try {
            //add env parameters to fetch adequate nuts
            id = wu.configureFetchEnv(id);
            DefaultNDefinitionBuilder2 result = null;
            for (NRepositoryAndFetchMode location : descTracker.available()) {
                try {
                    result = fetchDescriptorAsDefinition(id, nutsFetchModes, location.getFetchMode(), location.getRepository());
                    successfulDescriptorLocation = location;
                    break;
                } catch (NArtifactNotFoundException exc) {
                    //
                    descTracker.addFailure(location);
                } catch (Exception ex) {
                    //ignore
                    _LOG().log(NMsg.ofC("unexpected error while fetching descriptor for %s : %s", id.longId(),ex).asError(ex));
                    if (_LOG().isLoggable(Level.FINEST)) {
                        NLogUtils.traceMessage(_LOG(), nutsFetchModes, id.longId(), NMsgIntent.FAIL, "fetch def", startTime);
                    }
                    descTracker.addFailure(location);
                }
            }
            foundDefinitionBuilder = result;
            if (foundDefinitionBuilder != null) {
                foundDefinitionBuilder.setEffectiveDescriptor(new NDefEffectiveDescriptorSupplier(foundDefinitionBuilder, id));
                foundDefinitionBuilder.setDependencies(new NDefDependenciesSupplier(id, foundDefinitionBuilder, repositoryFilter(), dependencyFilter(), isIgnoreCurrentEnvironment()));
                foundDefinitionBuilder.setContent(new NDefContentSupplier(foundDefinitionBuilder, successfulDescriptorLocation, descTracker, reasons, wu, nutsFetchModes, id, startTime));
                foundDefinitionBuilder.setInstallInformation(new NDefInstallInformationSupplier(dws, id));
                foundDefinitionBuilder.setEffectiveFlags(new NDefNDescriptorFlagSetSupplier(id, foundDefinitionBuilder));
            }
        } catch (NArtifactNotFoundException ex) {
            reasons.add(ex);
            NLogUtils.traceMessage(_LOG(), nutsFetchModes, id.longId(), NMsgIntent.FAIL, "fetch definition", startTime);
            throw ex;
        } catch (RuntimeException ex) {
            NLogUtils.traceMessage(_LOG(), nutsFetchModes, id.longId(), NMsgIntent.FAIL, "[unexpected] fetch definition", startTime);
            throw ex;
        }
        if (foundDefinitionBuilder != null) {
            return foundDefinitionBuilder.build();
        }
        throw new NArtifactNotFoundException(id.longId());
    }

    protected DefaultNDefinitionBuilder2 fetchDescriptorAsDefinition(NId id, NFetchStrategy nutsFetchModes, NFetchMode mode, NRepository repo) {
        NWorkspaceExt dws = NWorkspaceExt.of();
        NWorkspaceUtils wu = NWorkspaceUtils.of();
//        NWorkspaceStore wstore = ((NWorkspaceExt) workspace).store();
//        if (withCache) {
//            try {
//                NDefinition d = wstore.loadLocationKey(
//                        NLocationKey.ofCacheFaced(id, repo.getUuid(), "def.cache"),
//                        DefaultNDefinition.class
//                );
//                if (d != null) {
//                    NRepository repositoryById = workspace.findRepositoryById(d.getRepositoryUuid()).orNull();
//                    NRepository repositoryByName = workspace.findRepositoryByName(d.getRepositoryName()).orNull();
//                    if (repositoryById == null || repositoryByName == null) {
//                        //this is invalid cache!
//                        wstore.deleteLocationKey(NLocationKey.ofCacheFaced(id, repo.getUuid(), "def.cache"));
//                    } else {
//                        NLogUtils.traceMessage(_LOG(), nutsFetchModes, id.getLongId(), NMsgIntent.CACHE, "fetch definition", 0);
//                        return new DefaultNDefinitionBuilder2(d);
//                    }
//                }
//            } catch (Exception ex) {
//                _LOG().with().level(Level.SEVERE)
//                        .verb(NMsgIntent.CACHE)
//                        .error(ex)
//                        .log(NMsg.ofC("error loading cache %s",ex));
//            }
//        }

        NRepositorySPI repoSPI = wu.toRepositorySPI(repo);
        NDescriptor descriptor = repoSPI.fetchDescriptor().setId(id)
                .setFetchMode(mode)
                .getResult();
        if (descriptor != null) {
            NId nutsId = dws.resolveEffectiveId(descriptor);
            NIdBuilder newIdBuilder = nutsId.builder();
            if (NBlankable.isBlank(newIdBuilder.repository())) {
                newIdBuilder.repository(repo.name());
            }
            //inherit classifier from requested parse
            String classifier = id.classifier();
            if (!NBlankable.isBlank(classifier)) {
                newIdBuilder.classifier(classifier);
            }
            Map<String, String> q = id.properties();
            if (!NDependencyScopes.isDefaultScope(q.get(NConstants.IdProperties.SCOPE))) {
                newIdBuilder.setProperty(NConstants.IdProperties.SCOPE, q.get(NConstants.IdProperties.SCOPE));
            }
            if (!NDependencyUtils.isDefaultOptional(q.get(NConstants.IdProperties.OPTIONAL))) {
                newIdBuilder.setProperty(NConstants.IdProperties.OPTIONAL, q.get(NConstants.IdProperties.OPTIONAL));
            }
            NId newId = newIdBuilder.build();

            NId apiId0 = null;
            NId apiId = null;

            if (id().shortName().equals(NConstants.Ids.NUTS_API)) {
                //
            } else {
                apiId = null;
                for (NDependency dependency : descriptor.dependencies()) {
                    if (dependency.toId().shortName().equals(NConstants.Ids.NUTS_API)
                            && NDependencyScopes.isCompileScope(dependency.scope())) {
                        apiId0 = dependency.toId().longId();
                    }
                }
                if (apiId0 != null) {
                    if (id().shortName().equals(NConstants.Ids.NUTS_RUNTIME)) {
                        apiId = apiId0;
                    } else if (descriptor.idType() == NIdType.RUNTIME) {
                        apiId = apiId0;
                    } else if (descriptor.idType() == NIdType.EXTENSION) {
                        apiId = apiId0;
                    } else if (descriptor.idType() == NIdType.COMPANION) {
                        apiId = apiId0;
                    }
                }
            }

            DefaultNDefinitionBuilder2 result = new DefaultNDefinitionBuilder2()
                    .setId(new ValueSupplier<>(newId.longId()))
                    .setDependency(new ValueSupplier<>(id.toDependency()))
                    .setRepositoryUuid(new ValueSupplier<>(repo.uuid()))
                    .setRepositoryName(new ValueSupplier<>(repo.name()))
                    .setDescriptor(new ValueSupplier<>(descriptor))
                    .setApiId(new ValueSupplier<>(apiId));
//            if (withCache) {
//                try {
//                    wstore.saveLocationKey(
//                            NLocationKey.ofCacheFaced(id, repo.getUuid(), "def.cache"),
//                            result
//                    );
//                } catch (Exception ex) {
//                    //
//                }
//            }
            return result;
        }
        throw new NArtifactNotFoundException(id, new NArtifactNotFoundException.NIdInvalidDependency[0], new NArtifactNotFoundException.NIdInvalidLocation[]{
                new NArtifactNotFoundException.NIdInvalidLocation(repo.name(), null, id + " not found")
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

    private static class NDefInstallInformationSupplier implements Supplier<NInstallInformation> {
        private final NWorkspaceExt dws;
        private final NId id;

        public NDefInstallInformationSupplier(NWorkspaceExt dws, NId id) {
            this.dws = dws;
            this.id = id;
        }

        @Override
        public NInstallInformation get() {
            NInstalledRepository installedRepository = dws.getInstalledRepository();
            //will always load install information
            NInstallInformation ii = installedRepository.getInstallInformation(id);
            if (ii != null) {
//                            ((DefaultNInstalledRepository) (dws.getInstalledRepository())).updateInstallInfoConfigInstallDate(id, Instant.now(), session);
                return ii;
            } else {
                return DefaultNInstallInfo.notInstalled(id);
            }
        }
    }

    private class NDefEffectiveDescriptorSupplier implements Supplier<NDescriptor> {
        private final DefaultNDefinitionBuilder2 foundDefinitionBuilder;
        private final NId id;

        public NDefEffectiveDescriptorSupplier(DefaultNDefinitionBuilder2 foundDefinitionBuilder, NId id) {
            this.foundDefinitionBuilder = foundDefinitionBuilder;
            this.id = id;
        }

        @Override
        public NDescriptor get() {
            try {
                return (NWorkspace.of().resolveEffectiveDescriptor(foundDefinitionBuilder.getDescriptor().get(),
                        new NDescriptorEffectiveConfig().setIgnoreCurrentEnvironment(DefaultNFetch.this.isIgnoreCurrentEnvironment())));
            } catch (NArtifactNotFoundException ex) {
                //ignore
                DefaultNFetch.this._LOG()
                        .log(NMsg.ofC("artifact descriptor found, but one of its parents or dependencies is not: %s : missing %s", id,
                                ex.getId())
                                .withLevel(Level.WARNING).withIntent(NMsgIntent.ALERT)
                        );
            }
            return null;
        }
    }

    private class NDefDependenciesSupplier implements Supplier<NDependencies> {
        private final NId id;
        private final DefaultNDefinitionBuilder2 foundDefinitionBuilder;
        private final NDependencyFilter dependencyFilter;
        private final NRepositoryFilter repositoryFilter;
        private final boolean ignoreCurrentEnvironment;

        public NDefDependenciesSupplier(NId id, DefaultNDefinitionBuilder2 foundDefinitionBuilder,NRepositoryFilter repositoryFilter,NDependencyFilter dependencyFilter,boolean ignoreCurrentEnvironment) {
            this.id = id;
            this.foundDefinitionBuilder = foundDefinitionBuilder;
            this.dependencyFilter = dependencyFilter;
            this.repositoryFilter = repositoryFilter;
            this.ignoreCurrentEnvironment = ignoreCurrentEnvironment;
        }

        @Override
        public NDependencies get() {
            return NDependencySolver.of()
                    .ignoreCurrentEnvironment(ignoreCurrentEnvironment)
                    .dependencyFilter(dependencyFilter)
                    .add(id.toDependency(), foundDefinitionBuilder.build())
                    .repositoryFilter(repositoryFilter)
                    .solve();
        }
    }

    private class NDefContentSupplier implements Supplier<NPath> {
        private final DefaultNDefinitionBuilder2 foundDefinitionBuilder;
        private final NRepositoryAndFetchMode successfulDescriptorLocation;
        private final NRepositoryAndFetchModeTracker descTracker;
        private final List<Exception> reasons;
        private NRepositoryAndFetchMode successfulContentLocation;
        private final NWorkspaceUtils wu;
        private final NFetchStrategy nutsFetchModes;
        private final NId id;
        private final long startTime;

        public NDefContentSupplier(DefaultNDefinitionBuilder2 foundDefinitionBuilder, NRepositoryAndFetchMode successfulDescriptorLocation, NRepositoryAndFetchModeTracker descTracker, List<Exception> reasons, NWorkspaceUtils wu, NFetchStrategy nutsFetchModes, NId id, long startTime) {
            this.foundDefinitionBuilder = foundDefinitionBuilder;
            this.successfulDescriptorLocation = successfulDescriptorLocation;
            this.descTracker = descTracker;
            this.reasons = reasons;
            this.wu = wu;
            this.nutsFetchModes = nutsFetchModes;
            this.id = id;
            this.startTime = startTime;
        }

        @Override
        public NPath get() {
            NPath fetchedPath = null;
            if (!foundDefinitionBuilder.getDescriptor().get().isNoContent()) {
                boolean loadedFromInstallRepo = DefaultNInstalledRepository.INSTALLED_REPO_UUID.equals(successfulDescriptorLocation
                        .getRepository().uuid());
                NId id1 = CoreNIdUtils.createContentFaceId(foundDefinitionBuilder.getId().get(), foundDefinitionBuilder.getDescriptor().get());
//                        boolean escalateMode = false;
                boolean contentSuccessful = false;
                NRepositoryAndFetchModeTracker contentTracker = new NRepositoryAndFetchModeTracker(descTracker.available());
                fetchedPath = fetchContent(id1, successfulDescriptorLocation, reasons);
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
                                .filter(x -> x.getRepository().uuid().equals(finalSuccessfulDescriptorLocation.getRepository().uuid()) &&
                                        x.getFetchMode() == NFetchMode.REMOTE).findFirst().orElse(null);
                        if (n != null/* && contentTracker.accept(n)*/) {
                            fetchedPath = fetchContent(id1, n, reasons);
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
                        fetchedPath = fetchContent(id1, repoAndMode, reasons);
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
                        NInstalledRepository installedRepository = NWorkspaceExt.of().getInstalledRepository();
                        NRepositorySPI installedRepositorySPI = wu.toRepositorySPI(installedRepository);

                        NPath finalFetchedPath = fetchedPath;
                        NSession.of().copy().confirm(NConfirmationMode.YES).runWith(() -> {
                            installedRepositorySPI.deploy()
                                    .setId(foundDefinitionBuilder.getId().get())
                                    .setDescriptor(foundDefinitionBuilder.getDescriptor().get())
                                    //.setFetchMode(mode)
                                    .setContent(finalFetchedPath)
                                    .run();
                        });

                    }
                }
                if (!contentSuccessful /*&& includedRemote*/) {
                    NLogUtils.traceMessage(_LOG(), nutsFetchModes, id.longId(), NMsgIntent.FAIL,
                            "fetched descriptor but failed to fetch artifact binaries", startTime);
                }
            }
            return fetchedPath;
        }

        protected NPath fetchContent(NId id1, NRepositoryAndFetchMode repo, List<Exception> reasons) {
            NRepositorySPI repoSPI = NWorkspaceUtils.of().toRepositorySPI(repo.getRepository());
            try {
                NDescriptor baseDescriptor = foundDefinitionBuilder.getDescriptor().get();
                return repoSPI.fetchContent()
                        .setId(id1).setDescriptor(baseDescriptor)
                        .setFetchMode(repo.getFetchMode())
                        .getResult();
            } catch (NArtifactNotFoundException ex) {
                reasons.add(ex);
                //
            }
            return null;
        }
    }

    private class NDefNDescriptorFlagSetSupplier implements Supplier<Set<NDescriptorFlag>> {
        private final NId id;
        private final DefaultNDefinitionBuilder2 foundDefinitionBuilder;

        public NDefNDescriptorFlagSetSupplier(NId id, DefaultNDefinitionBuilder2 foundDefinitionBuilder) {
            this.id = id;
            this.foundDefinitionBuilder = foundDefinitionBuilder;
        }

        private Set<NDescriptorFlag> loadCache() {
            Map<String, String> map = null;
            try {
                map = (Map<String, String>) ((NWorkspaceExt) NWorkspace.of()).store().loadLocationKey(NStoreKey.ofCacheFaced(id, null, "info.cache"), Map.class);
            } catch (Exception ex) {
                //
            }
            if (map != null) {
                Set<NDescriptorFlag> nb = new HashSet<>();
                for (Map.Entry<String, String> e : map.entrySet()) {
                    String v = e.getValue();
                    if (NLiteral.of(v).asBoolean().orElse(false)) {
                        String k = e.getKey();
                        NDescriptorFlag kk = NDescriptorFlag.parse(k).orNull();
                        if (kk != null) {
                            nb.add(kk);
                        }
                    }
                }
                return nb;
            }
            return null;
        }

        private void storeCache(Set<NDescriptorFlag> nb) {
            Map<String, String> map = new HashMap<>();
            try {
                map = new LinkedHashMap<>();
                for (NDescriptorFlag n : nb) {
                    map.put(NNameFormat.VAR_NAME.format(n.name()), "true");
                }
                ((NWorkspaceExt) NWorkspace.of()).store().saveLocationKey(NStoreKey.ofCacheFaced(id, null, "info.cache"), map);
            } catch (Exception ex) {
                //
            }
        }

        @Override
        public Set<NDescriptorFlag> get() {
            Set<NDescriptorFlag> cc = loadCache();
            if (cc != null) {
                return cc;
            }
            NPath jar = foundDefinitionBuilder.getContent().get();
            Set<NDescriptorFlag> nb = new HashSet<>();
            if (jar == null) {
                //do nothing
            } else {
                if (jar.name().toLowerCase().endsWith(".jar") && jar.isRegularFile()) {
                    try {
                        List<NExecutionEntry> t = NExecutionEntry.parse(jar);
                        if (t.size() > 0) {
                            nb.add(NDescriptorFlag.EXEC);
                            if (t.get(0).isApp()) {
                                nb.add(NDescriptorFlag.NUTS_APP);
                            }
                        }
                    } catch (Exception ex) {
                        //
                    }
                }
            }
            Set<NDescriptorFlag> flags = foundDefinitionBuilder.getDescriptor().get().flags();
            nb.addAll(flags);
            if (nb.contains(NDescriptorFlag.NUTS_APP) || nb.contains(NDescriptorFlag.PLATFORM_APP)) {
                nb.add(NDescriptorFlag.EXEC);
            }
            storeCache(nb);
            return nb;
        }
    }
}
