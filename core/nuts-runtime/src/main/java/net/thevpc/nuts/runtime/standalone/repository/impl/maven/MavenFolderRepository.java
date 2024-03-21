/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.folder.NFolderRepositoryBase;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.solrsearch.MavenSolrSearchCommand;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MvnClient;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenFolderRepository extends NFolderRepositoryBase {

    private final NLog LOG;
    private MvnClient wrapper;

    public MavenFolderRepository(NAddRepositoryOptions options, NSession session, NRepository parentRepository) {
        super(options, session, parentRepository,null,false, NConstants.RepoTypes.MAVEN,false);
        repoIter = new MavenRepoIter(this);
        LOG = NLog.of(MavenFolderRepository.class, session);
    }

    @Override
    public NIterator<NId> searchCore(final NIdFilter filter, NPath[] basePaths, NId[] baseIds, NFetchMode fetchMode, NSession session) {
        if (!acceptedFetchNoCache(fetchMode)) {
            return null;
        }
        MavenSolrSearchCommand cmd=new MavenSolrSearchCommand(this);
        NIterator<NId> aa=cmd.search(filter, baseIds, fetchMode, session);
        if(aa!=null){
            return aa;
        }
        return super.searchCore(filter, basePaths, baseIds, fetchMode, session);
    }


    public NPath fetchContentCoreUsingRepoHelper(NId id, NDescriptor descriptor, NFetchMode fetchMode, NSession session) {
        NPath cc = fetchContentCoreUsingWrapper(id, descriptor, fetchMode, session);
        if (cc != null) {
            return cc;
        }
        return super.fetchContentCoreUsingRepoHelper(id, descriptor, fetchMode, session);
    }

    public NIterator<NId> findNonSingleVersionImpl(final NId id, NIdFilter idFilter, NFetchMode fetchMode, final NSession session) {
        MavenSolrSearchCommand cmd = new MavenSolrSearchCommand(this);
        NIterator<NId> aa = cmd.search(idFilter, new NId[]{id}, fetchMode, session);
        if (aa != null) {
            return aa;
        }
        return super.findNonSingleVersionImpl(id, idFilter, fetchMode,session);
    }

    private NRepository getLocalMavenRepo(NSession session) {
        for (NRepository nRepository : NRepositories.of(session).getRepositories()) {
            if (nRepository.getRepositoryType().equals(NConstants.RepoTypes.MAVEN)
                    && nRepository.config().getLocationPath() != null
                    && nRepository.config().getLocationPath().toString()
                    .equals(
                            Paths.get(NPath.of("~/.m2", session).toAbsolute(NLocations.of(session).getWorkspaceLocation()).toString()).toString()
                    )) {
                return nRepository;
            }
        }
        return null;
    }

    protected NPath getMavenLocalFolderContent(NId id, NSession session) {
        NPath p = getIdRelativePath(id, session);
        if (p != null) {
            return NPath.ofUserHome(session).resolve(".m2").resolve(p);
        }
        return null;
    }
    private MvnClient getWrapper(NSession session) {
        if (true) {
            return null;
        }
        return new MvnClient(session);
    }

    public NPath fetchContentCoreUsingWrapper(NId id, NDescriptor descriptor, NFetchMode fetchMode, NSession session) {
        if (wrapper == null) {
            wrapper = getWrapper(session);
        }
        if (wrapper != null && wrapper.get(id, config().setSession(session).getLocationPath().toString(), session)) {
            NRepository repo = getLocalMavenRepo(session);
            if (repo != null) {
                NRepositorySPI repoSPI = NWorkspaceUtils.of(session).repoSPI(repo);
                return repoSPI.fetchContent()
                        .setId(id)
                        .setDescriptor(descriptor)
                        .setSession(session)
                        .setFetchMode(NFetchMode.LOCAL)
                        .run()
                        .getResult();
            }
            //should be already downloaded to m2 folder
            NPath content = getMavenLocalFolderContent(id, session);
            if (content != null && content.exists()) {
                return content.setUserCache(true).setUserTemporary(false);
            }
        }
        return null;
    }


    public String getIdExtension(NId id, NSession session) {
        checkSession(session);
        Map<String, String> q = id.getProperties();
        String f = NStringUtils.trim(q.get(NConstants.IdProperties.FACE));
        switch (f) {
            case NConstants.QueryFaces.DESCRIPTOR: {
                return ".pom";
            }
            case NConstants.QueryFaces.DESCRIPTOR_HASH: {
                return ".pom.sha1";
            }
            case CoreNConstants.QueryFaces.CATALOG: {
                return ".catalog";
            }
            case NConstants.QueryFaces.CONTENT_HASH: {
                return getIdExtension(id.builder().setFaceContent().build(), session) + ".sha1";
            }
            case NConstants.QueryFaces.CONTENT: {
                String packaging = q.get(NConstants.IdProperties.PACKAGING);
                return NLocations.of(session).getDefaultIdContentExtension(packaging);
            }
            default: {
                throw new NUnsupportedArgumentException(session, NMsg.ofC("unsupported fact %s", f));
            }
        }
    }

    public NDescriptor fetchDescriptorCore(NId id, NFetchMode fetchMode, NSession session) {
        checkSession(session);
        if (!acceptedFetchNoCache(fetchMode)) {
            throw new NNotFoundException(session, id, new NFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        InputStream stream = null;
        try {
            NDescriptor nutsDescriptor = null;
            byte[] bytes = null;
            String name = null;
            NId idDesc = id.builder().setFaceDescriptor().build();
            try {
                stream = getStream(idDesc, "artifact descriptor", "retrieve", session);
                name = NIO.of(session).ofInputSource(stream).getMetaData().getName().orElse("no-name");
                bytes = CoreIOUtils.loadByteArray(stream, true, session);
                nutsDescriptor = MavenUtils.of(session).parsePomXmlAndResolveParents(
                        CoreIOUtils.createBytesStream(bytes, name == null ? null : NMsg.ofNtf(name), "text/xml", StandardCharsets.UTF_8.name(), "pom.xml", session)
                        , fetchMode, getIdRemotePath(id, session).toString(), this);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            checkSHA1Hash(id.builder().setFace(NConstants.QueryFaces.DESCRIPTOR_HASH).build(),
                    CoreIOUtils.createBytesStream(bytes, name == null ? null : NMsg.ofNtf(name), "text/xml", StandardCharsets.UTF_8.name(), "pom.xml", session)
                    , "artifact descriptor", session);
            return nutsDescriptor;
        } catch (IOException | UncheckedIOException | NIOException ex) {
            throw new NNotFoundException(session, id, ex);
        }
    }
}
