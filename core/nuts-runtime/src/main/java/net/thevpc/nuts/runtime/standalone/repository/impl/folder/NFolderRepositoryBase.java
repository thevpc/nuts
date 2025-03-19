package net.thevpc.nuts.runtime.standalone.repository.impl.folder;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.NSpeedQualifier;
import net.thevpc.nuts.NStoreStrategy;
import net.thevpc.nuts.format.NTreeVisitResult;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.repository.NIdPathIterator;
import net.thevpc.nuts.runtime.standalone.repository.NIdPathIteratorBase;
import net.thevpc.nuts.runtime.standalone.repository.impl.NCachedRepository;
import net.thevpc.nuts.runtime.standalone.repository.util.NIdLocationUtils;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NDigestUtils;
import net.thevpc.nuts.log.NLogVerb;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class NFolderRepositoryBase extends NCachedRepository {
    protected NIdPathIteratorBase repoIter;

    public NFolderRepositoryBase(NAddRepositoryOptions options,
                                 NWorkspace workspace, NRepository parent, NSpeedQualifier speed,
                                 boolean supportedMirroring, String repositoryType, boolean supportsDeploy) {
        super(options, workspace, parent,
                speed == null ? (NPath.of(options.getConfig().getLocation().getPath()
                ).isRemote() ? NSpeedQualifier.SLOW : NSpeedQualifier.FASTER) : speed
                , supportedMirroring, repositoryType, supportsDeploy);
        if (!isRemote()) {
            if (options.getConfig().getStoreStrategy() != NStoreStrategy.STANDALONE) {
                cache.setWriteEnabled(false);
                cache.setReadEnabled(false);
            }
        }
    }

    @Override
    protected boolean isAvailableImpl() {
        long now = System.currentTimeMillis();
        try {
            NPath loc = config().getLocationPath();
            try {
                return loc.exists();
            } finally {
                _LOG().with().level(Level.FINEST).verb(NLogVerb.SUCCESS)
                        .time(System.currentTimeMillis() - now)
                        .log(NMsg.ofC("check available %s : success", getName()));
            }
        } catch (Exception e) {
            _LOG().with().level(Level.FINEST).verb(NLogVerb.FAIL)
                    .time(System.currentTimeMillis() - now)
                    .log(NMsg.ofC("check available %s : failed", getName()));
            return false;
        }
    }

    @Override
    public NIterator<NId> searchVersionsCore(final NId id, NIdFilter idFilter, NFetchMode fetchMode) {
        if (!acceptedFetchNoCache(fetchMode)) {
            return null;
        }
        NIdFilter filter2 = NIdFilters.of().nonnull(idFilter).and(
                NIdFilters.of().byName(id.getShortName())
        );
        if (id.getVersion().isSingleValue()) {
            return findSingleVersionImpl(id, filter2, fetchMode);
        }
        return findNonSingleVersionImpl(id, filter2, fetchMode);
    }


    @Override
    public NPath fetchContentCore(NId id, NDescriptor descriptor, NFetchMode fetchMode) {
        if (!acceptedFetchNoCache(fetchMode)) {
            throw new NNotFoundException(id, new NFetchModeNotSupportedException(this, fetchMode, id.toString(), null));
        }
        NPath fetch = NIdLocationUtils.fetch(id, descriptor.getLocations(), this);
        if (fetch != null) {
            return fetch;
        }
        return fetchContentCoreUsingRepoHelper(id, descriptor, fetchMode);
    }

    @Override
    public boolean isRemote() {
        return config().getLocationPath().isRemote();
    }


    @Override
    public NIterator<NId> searchCore(final NIdFilter filter, NPath[] basePaths, NId[] baseIds, NFetchMode fetchMode) {
        if (!acceptedFetchNoCache(fetchMode)) {
            return null;
        }

        NPath repoRoot = config().getLocationPath();
        List<NIterator<? extends NId>> list = new ArrayList<>();
        NSession session = getWorkspace().currentSession();
        for (NPath basePath : basePaths) {
            //,"https://search.maven.org/solrsearch",
            //                                                "maven.solrsearch.enable","true"
            list.add(
                    (NIterator) NIteratorBuilder.ofRunnable(
                            () -> session.getTerminal().printProgress(NMsg.ofC("%-14s %-8s %s", getName(), "browse",
                                    (basePath == null ? repoRoot : repoRoot.resolve(basePath)).toCompressedForm()
                            )),
                            "Log"

                    ).build());
            if (basePath.getName().equals("*")) {
                list.add(new NIdPathIterator(this, repoRoot, basePath.getParent(), filter, repoIter, Integer.MAX_VALUE, "core", null, true));
            } else {
                list.add(new NIdPathIterator(this, repoRoot, basePath, filter, repoIter, 2, "core", null, true));
            }
        }
        return NIteratorUtils.concat(list);
    }

    @Override
    public void updateStatisticsImpl() {
        config().getLocationPath()
                .walkDfs(new NTreeVisitor<NPath>() {
                             @Override
                             public NTreeVisitResult preVisitDirectory(NPath dir) {

                                 return NTreeVisitResult.CONTINUE;
                             }

                             @Override
                             public NTreeVisitResult visitFile(NPath file) {
                                 throw new NIOException(NMsg.ofPlain("updateStatistics Not supported."));
                             }

                             @Override
                             public NTreeVisitResult visitFileFailed(NPath file, Exception exc) {
                                 throw new NIOException(NMsg.ofPlain("updateStatistics Not supported."));
                             }

                             @Override
                             public NTreeVisitResult postVisitDirectory(NPath dir, Exception exc) {
                                 throw new NIOException(NMsg.ofPlain("updateStatistics Not supported."));
                             }
                         }
                );
    }

    @Override
    public boolean isAcceptFetchMode(NFetchMode mode) {
        return isRemote() || mode == NFetchMode.LOCAL;
    }

    public NPath fetchContentCoreUsingRepoHelper(NId id, NDescriptor descriptor, NFetchMode fetchMode) {
        NPath p = getIdRemotePath(id);
        if (p.isLocal()) {
            if (p.exists()) {
                return p.copy();
            } else {
                throw new NNotFoundException(id);
            }
        } else {
            String tempFile = NPath
                    .ofTempRepositoryFile(p.getName(), this).toString();
            try {
                NCp.of()
                        .from(getStream(id, "artifact binaries", "retrieve")).to(NPath.of(tempFile)).setValidator(new NCpValidator() {
                            @Override
                            public void validate(InputStream in) throws IOException {
                                checkSHA1Hash(id.builder().setFace(NConstants.QueryFaces.CONTENT_HASH).build(), in, "artifact binaries");
                            }
                        }).run();
            } catch (UncheckedIOException | NIOException ex) {
                throw new NNotFoundException(id, null, ex);
            }
            return NPath.of(tempFile).setUserTemporary(true).setUserCache(true);
        }
    }

    public NIterator<NId> findNonSingleVersionImpl(final NId id, NIdFilter idFilter, NFetchMode fetchMode) {
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        NPath foldersFileUrl = config().getLocationPath().resolve(groupId.replace('.', '/') + "/" + artifactId + "/");
        NSession session = getWorkspace().currentSession();

        return NIteratorBuilder.ofSupplier(
                () -> {
//                    List<NId> ret = new ArrayList<>();
                    session.getTerminal().printProgress(NMsg.ofC("looking for versions of %s at %s", id, foldersFileUrl.toCompressedForm()));
                    try {
                        return NIterator.of(
                                foldersFileUrl.stream().filter(
                                        NPath::isDirectory
                                ).withDesc(NEDesc.of("isDirectory")).map(versionFolder -> {
                                    String versionName = versionFolder.getName();
                                    NId expectedId = NIdBuilder.of(groupId, artifactId).setVersion(versionName).build();
                                    if (isValidArtifactVersionFolder(expectedId, versionFolder)) {
                                        final NId nutsId = id.builder().setVersion(versionFolder.getName()).build();
                                        if (idFilter == null || idFilter.acceptId(nutsId)) {
                                            return expectedId;
                                        }
                                    }
                                    return null;
                                }).filterNonNull().iterator()
                        ).withDesc(NEDesc.of("findNonSingleVersion"));
                    }catch (UncheckedIOException | NIOException ex) {
                        return NIterator.ofEmpty();
                    }
                }
                , () -> NElements.of().ofObjectBuilder()
                        .set("type", "NonSingleVersion")
                        .set("path", foldersFileUrl.toString())
                        .build()
        ).build();
    }

    private boolean isValidArtifactVersionFolder(NId expectedId, NPath versionFolder) {
        String expectedFileName = getIdFilename(expectedId.builder().setFaceDescriptor().build());
        return versionFolder.resolve(expectedFileName).isRegularFile();
    }

    public NIterator<NId> findSingleVersionImpl(final NId id, NIdFilter idFilter, NFetchMode fetchMode) {
        String singleVersion = id.getVersion().asSingleValue().orNull();
        NSession session = getWorkspace().currentSession();
        if (singleVersion != null) {
            String groupId = id.getGroupId();
            String artifactId = id.getArtifactId();
            NPath metadataURL = config().getLocationPath()
                    .resolve(groupId.replace('.', '/') + "/" + artifactId + "/" + singleVersion + "/"
                            + getIdFilename(id.builder().setFaceDescriptor().build())
                    );
            return NIteratorBuilder.ofSupplier(
                    () -> {
                        List<NId> ret = new ArrayList<>();
                        if (metadataURL.isRegularFile()) {
                            session.getTerminal().printProgress(NMsg.ofC("%-14s %-8s %s", getName(), "found", metadataURL.toCompressedForm()));
                            // ok found!!
                            ret.add(id);
                        }else{
                            session.getTerminal().printProgress(NMsg.ofC("%-14s %-8s %s", getName(), "missing", metadataURL.toCompressedForm()));
                        }
                        return ret.iterator();
                    }
                    , () -> NElements.of().ofObjectBuilder()
                            .set("type", "SingleVersion")
                            .set("path", metadataURL.toString())
                            .build()
            ).build();
        } else {
            throw new NIllegalArgumentException(NMsg.ofC("expected single version in %s", id));
        }
    }

    protected boolean acceptedFetchNoCache(NFetchMode fetchMode) {
        return (fetchMode == NFetchMode.REMOTE) == isRemote();
    }

    public InputStream getStream(NId id, String typeName, String action) {
        NPath url = getIdRemotePath(id);
        return openStream(id, url, id, typeName, action);
    }

    public String getStreamAsString(NId id, String typeName, String action) {
        byte[] barr = NCp.of()
                .addOptions(NPathOption.LOG, NPathOption.TRACE, NPathOption.SAFE)
                .from(getIdRemotePath(id))
                .setSourceOrigin(id)
                .setActionMessage(action == null ? null : NMsg.ofPlain(action))
                .setSourceTypeName(action)
                .getByteArrayResult();
        return new String(barr);
//        return CoreIOUtils.loadString(openStream(id, url, id, typeName, action, session), true, session);
    }

    public void checkSHA1Hash(NId id, InputStream stream, String typeName) throws IOException {
        if (!isRemote()) {
            //do not do any test
            stream.close();
            return;
        }
        switch (NStringUtils.trim(id.getFace())) {
            case NConstants.QueryFaces.CONTENT_HASH:
            case NConstants.QueryFaces.DESCRIPTOR_HASH: {
                break;
            }
            default: {
                _LOGOP().level(Level.SEVERE).error(new NIllegalArgumentException(NMsg.ofC("unsupported Hash Type %s", id.getFace())))
                        .log(NMsg.ofC("[BUG] unsupported Hash Type %s", id.getFace()));
                throw new IOException("unsupported hash type " + id.getFace());
            }
        }
        try {
            String rhash = null;
            try {
                rhash = getStreamSHA1(id, typeName);
            } catch (UncheckedIOException | NIOException ex) {
                //sha is not provided... so do not check anything!
                return;
            }
            String lhash = NDigestUtils.evalSHA1Hex(stream, true);
            if (!rhash.equalsIgnoreCase(lhash)) {
                throw new IOException("invalid file hash " + id);
            }
        } finally {
            stream.close();
        }
    }

    protected String getStreamSHA1(NId id, String typeName) {
//        if (!isRemoteRepository()) {
//            return CoreIOUtils.evalSHA1Hex(getStream(id.builder().setFace(NutsConstants.QueryFaces.CONTENT_HASH).build(), typeName, "verify", session), true, session);
//        }
        String hash = getStreamAsString(id, typeName + " SHA1", "verify").toUpperCase();
        for (String s : hash.split("[ \n\r]")) {
            if (!s.isEmpty()) {
                return s;
            }
        }
        return hash.split("[ \n\r]")[0];
    }

    public InputStream openStream(NId id, NPath path, Object source, String typeName, String action) {
        NSession session = getWorkspace().currentSession();
        session.getTerminal().printProgress(NMsg.ofC("%-14s %-8s %s %s", getName(), action, NNameFormat.LOWER_KEBAB_CASE.format(typeName), path.toCompressedForm()));
        return NInputStreamMonitor.of().setSource(path).setOrigin(source).setSourceTypeName(typeName).create();
    }

}
