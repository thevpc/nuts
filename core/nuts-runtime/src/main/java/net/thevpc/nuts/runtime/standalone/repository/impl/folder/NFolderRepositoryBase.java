package net.thevpc.nuts.runtime.standalone.repository.impl.folder;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NTreeVisitResult;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.repository.NIdPathIterator;
import net.thevpc.nuts.runtime.standalone.repository.NIdPathIteratorBase;
import net.thevpc.nuts.runtime.standalone.repository.impl.NCachedRepository;
import net.thevpc.nuts.runtime.standalone.repository.util.NIdLocationUtils;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NDigestUtils;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class NFolderRepositoryBase extends NCachedRepository {
    protected NIdPathIteratorBase repoIter;

    public NFolderRepositoryBase(NAddRepositoryOptions options,
                                 NSession session, NRepository parent, NSpeedQualifier speed,
                                 boolean supportedMirroring, String repositoryType, boolean supportsDeploy) {
        super(options, session, parent,
                speed == null ? (NPath.of(options.getConfig().getLocation().getPath()
                        , session).isRemote() ? NSpeedQualifier.SLOW : NSpeedQualifier.FASTER) : speed
                , supportedMirroring, repositoryType,supportsDeploy);
        NPath locationPath = config().setSession(initSession).getLocationPath();
        if (!isRemote()) {
            if (options.getConfig().getStoreStrategy() != NStoreStrategy.STANDALONE) {
                cache.setWriteEnabled(false);
                cache.setReadEnabled(false);
            }
        }
    }

    @Override
    protected boolean isAvailableImpl(NSession session) {
        long now = System.currentTimeMillis();
        try {
            NPath loc = config().setSession(initSession).getLocationPath();
            try {
                return loc.exists();
            } finally {
                LOG.with().level(Level.FINEST).verb(NLogVerb.SUCCESS)
                        .time(System.currentTimeMillis() - now)
                        .log(NMsg.ofC("check available %s : success", getName()));
            }
        } catch (Exception e) {
            LOG.with().level(Level.FINEST).verb(NLogVerb.FAIL)
                    .time(System.currentTimeMillis() - now)
                    .log(NMsg.ofC("check available %s : failed", getName()));
            return false;
        }
    }

    @Override
    public NIterator<NId> searchVersionsCore(final NId id, NIdFilter idFilter, NFetchMode fetchMode, final NSession session) {
        if (!acceptedFetchNoCache(fetchMode)) {
            return null;
        }
        NIdFilter filter2 = NIdFilters.of(session).nonnull(idFilter).and(
                NIdFilters.of(session).byName(id.getShortName())
        );
        if (id.getVersion().isSingleValue()) {
            return findSingleVersionImpl(id, filter2, fetchMode, session);
        }
        return findNonSingleVersionImpl(id, filter2, fetchMode, session);
    }


    @Override
    public NPath fetchContentCore(NId id, NDescriptor descriptor, String localPath, NFetchMode fetchMode, NSession session) {
        if (!acceptedFetchNoCache(fetchMode)) {
            throw new NNotFoundException(session, id, new NFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        if (NIdLocationUtils.fetch(id, descriptor.getLocations(), localPath, session)) {
            return NPath.of(localPath, session);
        }
        return fetchContentCoreUsingRepoHelper(id, descriptor, localPath, fetchMode, session);
    }

    @Override
    public boolean isRemote() {
        return config().setSession(initSession).getLocationPath().isRemote();
    }


    @Override
    public NIterator<NId> searchCore(final NIdFilter filter, NPath[] basePaths, NId[] baseIds, NFetchMode fetchMode, NSession session) {
        if (!acceptedFetchNoCache(fetchMode)) {
            return null;
        }

        NPath repoRoot = config().setSession(session).getLocationPath();
        List<NIterator<? extends NId>> list = new ArrayList<>();
        for (NPath basePath : basePaths) {
            //,"https://search.maven.org/solrsearch",
            //                                                "maven.solrsearch.enable","true"
            list.add(
                    (NIterator) IteratorBuilder.ofRunnable(
                            () -> session.getTerminal().printProgress(NMsg.ofC("%-14s %-8s %s", getName(), "browse",
                                    (basePath == null ? repoRoot : repoRoot.resolve(basePath)).toCompressedForm()
                            )),
                            "Log",

                            session).build());
            if (basePath.getName().equals("*")) {
                list.add(new NIdPathIterator(this, repoRoot, basePath.getParent(), filter, session, repoIter, Integer.MAX_VALUE, "core", null,true));
            } else {
                list.add(new NIdPathIterator(this, repoRoot, basePath, filter, session, repoIter, 2, "core", null,true));
            }
        }
        return IteratorUtils.concat(list);
    }

    @Override
    public void updateStatisticsImpl(NSession session) {
        config().setSession(session).getLocationPath()
                .walkDfs(new NTreeVisitor<NPath>() {
                             @Override
                             public NTreeVisitResult preVisitDirectory(NPath dir, NSession session) {

                                 return NTreeVisitResult.CONTINUE;
                             }

                             @Override
                             public NTreeVisitResult visitFile(NPath file, NSession session) {
                                 throw new NIOException(session, NMsg.ofPlain("updateStatistics Not supported."));
                             }

                             @Override
                             public NTreeVisitResult visitFileFailed(NPath file, Exception exc, NSession session) {
                                 throw new NIOException(session, NMsg.ofPlain("updateStatistics Not supported."));
                             }

                             @Override
                             public NTreeVisitResult postVisitDirectory(NPath dir, Exception exc, NSession session) {
                                 throw new NIOException(session, NMsg.ofPlain("updateStatistics Not supported."));
                             }
                         }
                );
    }

    @Override
    public boolean isAcceptFetchMode(NFetchMode mode, NSession session) {
        return isRemote() || mode == NFetchMode.LOCAL;
    }

    public NPath fetchContentCoreUsingRepoHelper(NId id, NDescriptor descriptor, String localPath, NFetchMode fetchMode, NSession session) {
        if (localPath == null) {
            NPath p = getIdRemotePath(id, session);
            if (p.isLocal()) {
                if (p.exists()) {
                    return p.copy();
                } else {
                    throw new NNotFoundException(session, id);
                }
            } else {
                String tempFile = NPath
                        .ofTempRepositoryFile(p.getName(),getUuid(),session).toString();
                try {
                    NCp.of(session)
                            .from(getStream(id, "artifact binaries", "retrieve", session)).to(NPath.of(tempFile,session)).setValidator(new NCpValidator() {
                                @Override
                                public void validate(InputStream in) throws IOException {
                                    checkSHA1Hash(id.builder().setFace(NConstants.QueryFaces.CONTENT_HASH).build(), in, "artifact binaries", session);
                                }
                            }).run();
                } catch (UncheckedIOException | NIOException ex) {
                    throw new NNotFoundException(session, id, null, ex);
                }
                return NPath.of(tempFile, session).setUserTemporary( true).setUserCache(true);
            }
        } else {
            try {
                NCp.of(session)
                        .from(getIdRemotePath(id, session))
                        .to(NPath.of(localPath,session))
                        .setValidator(in -> checkSHA1Hash(
                                        id.builder().setFace(NConstants.QueryFaces.CONTENT_HASH).build(),
                                        in, "artifact binaries", session
                                )
                        ).addOptions(NPathOption.LOG, NPathOption.TRACE, NPathOption.SAFE)
                        .run();
            } catch (UncheckedIOException | NIOException ex) {
                throw new NNotFoundException(session, id, null, ex);
            }
            return NPath.of(localPath, session).setUserCache(true).setUserTemporary(false);
        }
    }

    public NIterator<NId> findNonSingleVersionImpl(final NId id, NIdFilter idFilter, NFetchMode fetchMode, final NSession session) {
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        NPath foldersFileUrl = config().setSession(session).getLocationPath().resolve(groupId.replace('.', '/') + "/" + artifactId + "/");

        return IteratorBuilder.ofSupplier(
                () -> {
                    List<NId> ret = new ArrayList<>();
                    session.getTerminal().printProgress(NMsg.ofC("looking for versions of %s at %s", id, foldersFileUrl.toCompressedForm()));
                    NPath[] all = foldersFileUrl.stream().filter(
                            NPath::isDirectory, "isDirectory"
                    ).toArray(NPath[]::new);
                    for (NPath version : all) {
                        final NId nutsId = id.builder().setVersion(version.getName()).build();
                        if (idFilter != null && !idFilter.acceptId(nutsId, session)) {
                            continue;
                        }
                        ret.add(NIdBuilder.of(groupId,artifactId).setVersion(version.getName()).build());
                    }
                    return NIterator.of(ret.iterator(), "findNonSingleVersion");
                }
                , e -> NElements.of(e).ofObject()
                        .set("type", "NonSingleVersion")
                        .set("path", foldersFileUrl.toString())
                        .build(),
                session).build();
    }

    public NIterator<NId> findSingleVersionImpl(final NId id, NIdFilter idFilter, NFetchMode fetchMode, final NSession session) {
        String singleVersion = id.getVersion().asSingleValue().orNull();
        if (singleVersion!=null) {
            String groupId = id.getGroupId();
            String artifactId = id.getArtifactId();
            NPath metadataURL = config().setSession(session).getLocationPath()
                    .resolve(groupId.replace('.', '/') + "/" + artifactId + "/" + singleVersion + "/"
                    + getIdFilename(id.builder().setFaceDescriptor().build(), session)
            );
            return IteratorBuilder.ofSupplier(
                    () -> {
                        List<NId> ret = new ArrayList<>();
                        session.getTerminal().printProgress(NMsg.ofC("%-14s %-8s %s", getName(), "search", metadataURL.toCompressedForm()));
                        if (metadataURL.isRegularFile()) {
                            // ok found!!
                            ret.add(id);
                        }
                        return ret.iterator();
                    }
                    , e -> NElements.of(e).ofObject()
                            .set("type", "SingleVersion")
                            .set("path", metadataURL.toString())
                            .build(),
                    session).build();
        } else {
            throw new NIllegalArgumentException(session, NMsg.ofC("expected single version in %s", id));
        }
    }

    protected boolean acceptedFetchNoCache(NFetchMode fetchMode) {
        return (fetchMode == NFetchMode.REMOTE) == isRemote();
    }

    public InputStream getStream(NId id, String typeName, String action, NSession session) {
        NPath url = getIdRemotePath(id, session);
        return openStream(id, url, id, typeName, action, session);
    }

    public String getStreamAsString(NId id, String typeName, String action, NSession session) {
        byte[] barr = NCp.of(session)
                .addOptions(NPathOption.LOG, NPathOption.TRACE, NPathOption.SAFE)
                .from(getIdRemotePath(id, session))
                .setSourceOrigin(id)
                .setActionMessage(action==null?null: NMsg.ofPlain(action))
                .setSourceTypeName(action)
                .getByteArrayResult()
                ;
        return new String(barr);
//        return CoreIOUtils.loadString(openStream(id, url, id, typeName, action, session), true, session);
    }

    public void checkSHA1Hash(NId id, InputStream stream, String typeName, NSession session) throws IOException {
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
                _LOGOP(session).level(Level.SEVERE).error(new NIllegalArgumentException(session, NMsg.ofC("unsupported Hash Type %s", id.getFace())))
                        .log(NMsg.ofJ("[BUG] unsupported Hash Type {0}", id.getFace()));
                throw new IOException("unsupported hash type " + id.getFace());
            }
        }
        try {
            String rhash = null;
            try {
                rhash = getStreamSHA1(id, session, typeName);
            } catch (UncheckedIOException | NIOException ex) {
                //sha is not provided... so do not check anything!
                return;
            }
            String lhash = NDigestUtils.evalSHA1Hex(stream, true, session);
            if (!rhash.equalsIgnoreCase(lhash)) {
                throw new IOException("invalid file hash " + id);
            }
        } finally {
            stream.close();
        }
    }

    protected String getStreamSHA1(NId id, NSession session, String typeName) {
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

    public InputStream openStream(NId id, NPath path, Object source, String typeName, String action, NSession session) {
        session.getTerminal().printProgress(NMsg.ofC("%-14s %-8s %s",getName(), action, path.toCompressedForm()));
        return NInputStreamMonitor.of(session).setSource(path).setOrigin(source).setSourceTypeName(typeName).create();
    }

}
