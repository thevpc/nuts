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
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.folder.NutsFolderRepositoryBase;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.solrsearch.MavenSolrSearchCommand;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MvnClient;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsConstants;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenFolderRepository extends NutsFolderRepositoryBase {

    private final NutsLogger LOG;
    private MvnClient wrapper;

    public MavenFolderRepository(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parentRepository) {
        super(options, session, parentRepository,null,false, NutsConstants.RepoTypes.MAVEN,false);
        repoIter = new MavenRepoIter(this);
        LOG = NutsLogger.of(MavenFolderRepository.class, session);
    }

    @Override
    public NutsIterator<NutsId> searchCore(final NutsIdFilter filter, NutsPath[] basePaths, NutsId[] baseIds, NutsFetchMode fetchMode, NutsSession session) {
        if (!acceptedFetchNoCache(fetchMode)) {
            return null;
        }
        MavenSolrSearchCommand cmd=new MavenSolrSearchCommand(this);
        NutsIterator<NutsId> aa=cmd.search(filter, baseIds, fetchMode, session);
        if(aa!=null){
            return aa;
        }
        return super.searchCore(filter, basePaths, baseIds, fetchMode, session);
    }


    public NutsContent fetchContentCoreUsingRepoHelper(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session) {
        NutsContent cc = fetchContentCoreUsingWrapper(id, descriptor, localPath, fetchMode, session);
        if (cc != null) {
            return cc;
        }
        return super.fetchContentCoreUsingRepoHelper(id, descriptor, localPath, fetchMode, session);
    }

    public NutsIterator<NutsId> findNonSingleVersionImpl(final NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, final NutsSession session) {
        MavenSolrSearchCommand cmd = new MavenSolrSearchCommand(this);
        NutsIterator<NutsId> aa = cmd.search(idFilter, new NutsId[]{id}, fetchMode, session);
        if (aa != null) {
            return aa;
        }
        return super.findNonSingleVersionImpl(id, idFilter, fetchMode,session);
    }

    private NutsRepository getLocalMavenRepo(NutsSession session) {
        for (NutsRepository nutsRepository : session.repos().setSession(session).getRepositories()) {
            if (nutsRepository.getRepositoryType().equals(NutsConstants.RepoTypes.MAVEN)
                    && nutsRepository.config().getLocationPath() != null
                    && nutsRepository.config().getLocationPath().toString()
                    .equals(
                            Paths.get(NutsPath.of("~/.m2", session).toAbsolute(session.locations().getWorkspaceLocation()).toString()).toString()
                    )) {
                return nutsRepository;
            }
        }
        return null;
    }

    protected NutsPath getMavenLocalFolderContent(NutsId id, NutsSession session) {
        NutsPath p = getIdRelativePath(id, session);
        if (p != null) {
            return NutsPath.ofUserHome(session).resolve(".m2").resolve(p);
        }
        return null;
    }
    private MvnClient getWrapper(NutsSession session) {
        if (true) {
            return null;
        }
        return new MvnClient(session);
    }

    public NutsContent fetchContentCoreUsingWrapper(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session) {
        if (wrapper == null) {
            wrapper = getWrapper(session);
        }
        if (wrapper != null && wrapper.get(id, config().setSession(session).getLocationPath().toString(), session)) {
            NutsRepository repo = getLocalMavenRepo(session);
            if (repo != null) {
                NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(session).repoSPI(repo);
                return repoSPI.fetchContent()
                        .setId(id)
                        .setDescriptor(descriptor)
                        .setLocalPath(localPath)
                        .setSession(session)
                        .setFetchMode(NutsFetchMode.LOCAL)
                        .run()
                        .getResult();
            }
            //should be already downloaded to m2 folder
            NutsPath content = getMavenLocalFolderContent(id, session);
            if (content != null && content.exists()) {
                if (localPath == null) {
                    return new NutsDefaultContent(
                            content, true, false);
                } else {
                    String tempFile = NutsTmp.of(session)
                            .setRepositoryId(getUuid())
                            .createTempFile(content.getName()).toString();
                    NutsCp.of(session)
                            .from(content).to(tempFile).addOptions(NutsPathOption.SAFE).run();
                    return new NutsDefaultContent(
                            NutsPath.of(tempFile, session), true, false);
                }
            }
        }
        return null;
    }


    public String getIdExtension(NutsId id, NutsSession session) {
        checkSession(session);
        Map<String, String> q = id.getProperties();
        String f = NutsUtilStrings.trim(q.get(NutsConstants.IdProperties.FACE));
        switch (f) {
            case NutsConstants.QueryFaces.DESCRIPTOR: {
                return ".pom";
            }
            case NutsConstants.QueryFaces.DESCRIPTOR_HASH: {
                return ".pom.sha1";
            }
            case CoreNutsConstants.QueryFaces.CATALOG: {
                return ".catalog";
            }
            case NutsConstants.QueryFaces.CONTENT_HASH: {
                return getIdExtension(id.builder().setFaceContent().build(), session) + ".sha1";
            }
            case NutsConstants.QueryFaces.CONTENT: {
                String packaging = q.get(NutsConstants.IdProperties.PACKAGING);
                return session.locations().getDefaultIdContentExtension(packaging);
            }
            default: {
                throw new NutsUnsupportedArgumentException(session, NutsMessage.cstyle("unsupported fact %s", f));
            }
        }
    }

    public NutsDescriptor fetchDescriptorCore(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        checkSession(session);
        if (!acceptedFetchNoCache(fetchMode)) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        InputStream stream = null;
        try {
            NutsDescriptor nutsDescriptor = null;
            byte[] bytes = null;
            String name = null;
            NutsId idDesc = id.builder().setFaceDescriptor().build();
            try {
                stream = getStream(idDesc, "artifact descriptor", "retrieve", session);
                bytes = CoreIOUtils.loadByteArray(stream, true, session);
                name = NutsStreamMetadata.of(stream).getName();
                nutsDescriptor = MavenUtils.of(session).parsePomXmlAndResolveParents(
                        CoreIOUtils.createBytesStream(bytes, name == null ? null : NutsMessage.formatted(name), "text/xml", "pom.xml", session)
                        , fetchMode, getIdRemotePath(id, session).toString(), this);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            checkSHA1Hash(id.builder().setFace(NutsConstants.QueryFaces.DESCRIPTOR_HASH).build(),
                    CoreIOUtils.createBytesStream(bytes, name == null ? null : NutsMessage.formatted(name), "text/xml", "pom.xml", session)
                    , "artifact descriptor", session);
            return nutsDescriptor;
        } catch (IOException | UncheckedIOException | NutsIOException ex) {
            throw new NutsNotFoundException(session, id, ex);
        }
    }
}
