package net.thevpc.nuts.runtime.standalone.repository.impl.folder;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.NutsIdPathIterator;
import net.thevpc.nuts.runtime.standalone.repository.NutsIdPathIteratorBase;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsCachedRepository;
import net.thevpc.nuts.runtime.standalone.repository.util.NutsIdLocationUtils;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NutsDigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class NutsFolderRepositoryBase extends NutsCachedRepository {
    protected NutsIdPathIteratorBase repoIter;

    public NutsFolderRepositoryBase(NutsAddRepositoryOptions options,
                                    NutsSession session, NutsRepository parent, NutsSpeedQualifier speed,
                                    boolean supportedMirroring, String repositoryType, boolean supportsDeploy) {
        super(options, session, parent,
                speed == null ? (NutsPath.of(options.getConfig().getLocation().getPath()
                        , session).isRemote() ? NutsSpeedQualifier.SLOW : NutsSpeedQualifier.FASTER) : speed
                , supportedMirroring, repositoryType,supportsDeploy);
        if (!isRemote()) {
            if (options.getConfig().getStoreLocationStrategy() != NutsStoreLocationStrategy.STANDALONE) {
                cache.setWriteEnabled(false);
                cache.setReadEnabled(false);
            }
        }
    }

    @Override
    protected boolean isAvailableImpl(NutsSession session) {
        long now = System.currentTimeMillis();
        try {
            NutsPath loc = config().setSession(initSession).getLocationPath();
            try {
                return loc.exists();
            } finally {
                LOG.with().level(Level.FINEST).verb(NutsLogVerb.SUCCESS)
                        .time(System.currentTimeMillis() - now)
                        .log(NutsMessage.cstyle("check available %s : success", getName()));
            }
        } catch (Exception e) {
            LOG.with().level(Level.FINEST).verb(NutsLogVerb.FAIL)
                    .time(System.currentTimeMillis() - now)
                    .log(NutsMessage.cstyle("check available %s : failed", getName()));
            return false;
        }
    }

    @Override
    public NutsIterator<NutsId> searchVersionsCore(final NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, final NutsSession session) {
        if (!acceptedFetchNoCache(fetchMode)) {
            return null;
        }
        NutsIdFilter filter2 = NutsIdFilters.of(session).nonnull(idFilter).and(
                NutsIdFilters.of(session).byName(id.getShortName())
        );
        if (id.getVersion().isSingleValue()) {
            return findSingleVersionImpl(id, filter2, fetchMode, session);
        }
        return findNonSingleVersionImpl(id, filter2, fetchMode, session);
    }


    @Override
    public NutsContent fetchContentCore(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session) {
        if (!acceptedFetchNoCache(fetchMode)) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        if (NutsIdLocationUtils.fetch(id, descriptor.getLocations(), localPath, session)) {
            return new NutsDefaultContent(
                    NutsPath.of(localPath, session), false, false);
        }
        return fetchContentCoreUsingRepoHelper(id, descriptor, localPath, fetchMode, session);
    }

    @Override
    public boolean isRemote() {
        return config().setSession(initSession).getLocationPath().isRemote();
    }


    @Override
    public NutsIterator<NutsId> searchCore(final NutsIdFilter filter, NutsPath[] basePaths, NutsId[] baseIds, NutsFetchMode fetchMode, NutsSession session) {
        if (!acceptedFetchNoCache(fetchMode)) {
            return null;
        }

        NutsPath repoRoot = config().setSession(session).getLocationPath();
        List<NutsIterator<? extends NutsId>> list = new ArrayList<>();
        for (NutsPath basePath : basePaths) {
            //,"https://search.maven.org/solrsearch",
            //                                                "maven.solrsearch.enable","true"
            list.add(
                    (NutsIterator) IteratorBuilder.ofRunnable(
                            () -> session.getTerminal().printProgress("%-14s %-8s %s", getName(), "browse",
                                    (basePath == null ? repoRoot : repoRoot.resolve(basePath)).toCompressedForm()
                            ),
                            "Log",

                            session).build());
            if (basePath.getName().equals("*")) {
                list.add(new NutsIdPathIterator(this, repoRoot, basePath.getParent(), filter, session, repoIter, Integer.MAX_VALUE, "core", null));
            } else {
                list.add(new NutsIdPathIterator(this, repoRoot, basePath, filter, session, repoIter, 2, "core", null));
            }
        }
        return IteratorUtils.concat(list);
    }

    @Override
    public void updateStatistics2(NutsSession session) {
        config().setSession(session).getLocationPath()
                .walkDfs(new NutsTreeVisitor<NutsPath>() {
                             @Override
                             public NutsTreeVisitResult preVisitDirectory(NutsPath dir, NutsSession session) {

                                 return NutsTreeVisitResult.CONTINUE;
                             }

                             @Override
                             public NutsTreeVisitResult visitFile(NutsPath file, NutsSession session) {
                                 throw new NutsIOException(session, NutsMessage.cstyle("updateStatistics Not supported."));
                             }

                             @Override
                             public NutsTreeVisitResult visitFileFailed(NutsPath file, Exception exc, NutsSession session) {
                                 throw new NutsIOException(session, NutsMessage.cstyle("updateStatistics Not supported."));
                             }

                             @Override
                             public NutsTreeVisitResult postVisitDirectory(NutsPath dir, Exception exc, NutsSession session) {
                                 throw new NutsIOException(session, NutsMessage.cstyle("updateStatistics Not supported."));
                             }
                         }
                );
    }

    @Override
    public boolean isAcceptFetchMode(NutsFetchMode mode, NutsSession session) {
        return isRemote() || mode == NutsFetchMode.LOCAL;
    }

    public NutsContent fetchContentCoreUsingRepoHelper(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session) {
        if (localPath == null) {
            NutsPath p = getIdRemotePath(id, session);
            if (p.isLocal()) {
                if (p.exists()) {
                    return new NutsDefaultContent(p, false, false);
                } else {
                    throw new NutsNotFoundException(session, id);
                }
            } else {
                String tempFile = NutsTmp.of(session)
                        .setRepositoryId(getUuid())
                        .createTempFile(p.getName()).toString();
                try {
                    NutsCp.of(session)
                            .from(getStream(id, "artifact binaries", "retrieve", session)).to(tempFile).setValidator(new NutsIOCopyValidator() {
                                @Override
                                public void validate(InputStream in) throws IOException {
                                    checkSHA1Hash(id.builder().setFace(NutsConstants.QueryFaces.CONTENT_HASH).build(), in, "artifact binaries", session);
                                }
                            }).run();
                } catch (UncheckedIOException | NutsIOException ex) {
                    throw new NutsNotFoundException(session, id, null, ex);
                }
                return new NutsDefaultContent(NutsPath.of(tempFile, session), true, true);
            }
        } else {
            try {
                NutsCp.of(session)
                        .from(getIdRemotePath(id, session))
                        .to(localPath)
                        .setValidator(in -> checkSHA1Hash(
                                        id.builder().setFace(NutsConstants.QueryFaces.CONTENT_HASH).build(),
                                        in, "artifact binaries", session
                                )
                        ).addOptions(NutsPathOption.LOG, NutsPathOption.TRACE, NutsPathOption.SAFE)
                        .run();
            } catch (UncheckedIOException | NutsIOException ex) {
                throw new NutsNotFoundException(session, id, null, ex);
            }
            return new NutsDefaultContent(
                    NutsPath.of(localPath, session), true, false);
        }
    }

    public NutsIterator<NutsId> findNonSingleVersionImpl(final NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, final NutsSession session) {
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        NutsPath foldersFileUrl = config().setSession(session).getLocationPath().resolve(groupId.replace('.', '/') + "/" + artifactId + "/");

        return IteratorBuilder.ofSupplier(
                () -> {
                    List<NutsId> ret = new ArrayList<>();
                    session.getTerminal().printProgress("looking for versions of %s at %s", id, foldersFileUrl.toCompressedForm());
                    NutsPath[] all = foldersFileUrl.list().filter(
                            NutsPath::isDirectory, "isDirectory"
                    ).toArray(NutsPath[]::new);
                    for (NutsPath version : all) {
                        final NutsId nutsId = id.builder().setVersion(version.getName()).build();
                        if (idFilter != null && !idFilter.acceptId(nutsId, session)) {
                            continue;
                        }
                        ret.add(NutsIdBuilder.of(groupId,artifactId).setVersion(version.getName()).build());
                    }
                    return NutsIterator.of(ret.iterator(), "findNonSingleVersion");
                }
                , e -> e.ofObject()
                        .set("type", "NonSingleVersion")
                        .set("path", foldersFileUrl.toString())
                        .build(),
                session).build();
    }

    public NutsIterator<NutsId> findSingleVersionImpl(final NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, final NutsSession session) {
        if (id.getVersion().isSingleValue()) {
            String groupId = id.getGroupId();
            String artifactId = id.getArtifactId();
            NutsPath metadataURL = config().setSession(session).getLocationPath().resolve(groupId.replace('.', '/') + "/" + artifactId + "/" + id.getVersion().toString() + "/"
                    + getIdFilename(id.builder().setFaceDescriptor().build(), session)
            );
            return IteratorBuilder.ofSupplier(
                    () -> {
                        List<NutsId> ret = new ArrayList<>();
                        session.getTerminal().printProgress("%-14s %-8s %s", getName(), "search", metadataURL.toCompressedForm());
                        if (metadataURL.isRegularFile()) {
                            // ok found!!
                            ret.add(id);
                        }
                        return ret.iterator();
                    }
                    , e -> e.ofObject()
                            .set("type", "SingleVersion")
                            .set("path", metadataURL.toString())
                            .build(),
                    session).build();
        } else {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("expected single version in %s", id));
        }
    }

    protected boolean acceptedFetchNoCache(NutsFetchMode fetchMode) {
        return (fetchMode == NutsFetchMode.REMOTE) == isRemote();
    }

    public InputStream getStream(NutsId id, String typeName, String action, NutsSession session) {
        NutsPath url = getIdRemotePath(id, session);
        return openStream(id, url, id, typeName, action, session);
    }

    public String getStreamAsString(NutsId id, String typeName, String action, NutsSession session) {
        byte[] barr = NutsCp.of(session)
                .addOptions(NutsPathOption.LOG, NutsPathOption.TRACE, NutsPathOption.SAFE)
                .from(getIdRemotePath(id, session))
                .setSourceOrigin(id)
                .setActionMessage(action==null?null:NutsMessage.plain(action))
                .setSourceTypeName(action)
                .getByteArrayResult()
                ;
        return new String(barr);
//        return CoreIOUtils.loadString(openStream(id, url, id, typeName, action, session), true, session);
    }

    public void checkSHA1Hash(NutsId id, InputStream stream, String typeName, NutsSession session) throws IOException {
        if (!isRemote()) {
            //do not do any test
            stream.close();
            return;
        }
        switch (NutsUtilStrings.trim(id.getFace())) {
            case NutsConstants.QueryFaces.CONTENT_HASH:
            case NutsConstants.QueryFaces.DESCRIPTOR_HASH: {
                break;
            }
            default: {
                _LOGOP(session).level(Level.SEVERE).error(new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported Hash Type %s", id.getFace())))
                        .log(NutsMessage.jstyle("[BUG] unsupported Hash Type {0}", id.getFace()));
                throw new IOException("unsupported hash type " + id.getFace());
            }
        }
        try {
            String rhash = null;
            try {
                rhash = getStreamSHA1(id, session, typeName);
            } catch (UncheckedIOException | NutsIOException ex) {
                //sha is not provided... so do not check anything!
                return;
            }
            String lhash = NutsDigestUtils.evalSHA1Hex(stream, true, session);
            if (!rhash.equalsIgnoreCase(lhash)) {
                throw new IOException("invalid file hash " + id);
            }
        } finally {
            stream.close();
        }
    }

    protected String getStreamSHA1(NutsId id, NutsSession session, String typeName) {
//        if (!isRemoteRepository()) {
//            return CoreIOUtils.evalSHA1Hex(getStream(id.builder().setFace(NutsConstants.QueryFaces.CONTENT_HASH).build(), typeName, "verify", session), true, session);
//        }
        String hash = getStreamAsString(id, typeName + " SHA1", "verify", session).toUpperCase();
        for (String s : hash.split("[ \n\r]")) {
            if (s.length() > 0) {
                return s;
            }
        }
        return hash.split("[ \n\r]")[0];
    }

    public InputStream openStream(NutsId id, NutsPath path, Object source, String typeName, String action, NutsSession session) {
        session.getTerminal().printProgress("%-14s %-8s %s",getName(), action, path.toCompressedForm());
        return NutsInputStreamMonitor.of(session).setSource(path).setOrigin(source).setSourceTypeName(typeName).create();
    }

}
