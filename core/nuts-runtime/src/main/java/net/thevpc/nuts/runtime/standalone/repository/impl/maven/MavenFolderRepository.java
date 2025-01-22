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
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;


import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.folder.NFolderRepositoryBase;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.solrsearch.MavenSolrSearchCommand;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MvnClient;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.io.NIOUtils;
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

    private MvnClient wrapper;

    public MavenFolderRepository(NAddRepositoryOptions options, NWorkspace workspace, NRepository parentRepository) {
        super(options, workspace, parentRepository,null,false, NConstants.RepoTypes.MAVEN,false);
        repoIter = new MavenRepoIter(this);
    }

    protected NLog _LOG(){
        return NLog.of(MavenFolderRepository.class);
    }

    @Override
    public NIterator<NId> searchCore(final NIdFilter filter, NPath[] basePaths, NId[] baseIds, NFetchMode fetchMode) {
        if (!acceptedFetchNoCache(fetchMode)) {
            return null;
        }
        MavenSolrSearchCommand cmd=new MavenSolrSearchCommand(this);
        NIterator<NId> aa=cmd.search(filter, baseIds, fetchMode);
        if(aa!=null){
            return aa;
        }
        return super.searchCore(filter, basePaths, baseIds, fetchMode);
    }


    public NPath fetchContentCoreUsingRepoHelper(NId id, NDescriptor descriptor, NFetchMode fetchMode) {
        NPath cc = fetchContentCoreUsingWrapper(id, descriptor, fetchMode);
        if (cc != null) {
            return cc;
        }
        return super.fetchContentCoreUsingRepoHelper(id, descriptor, fetchMode);
    }

    public NIterator<NId> findNonSingleVersionImpl(final NId id, NIdFilter idFilter, NFetchMode fetchMode) {
        MavenSolrSearchCommand cmd = new MavenSolrSearchCommand(this);
        NIterator<NId> aa = cmd.search(idFilter, new NId[]{id}, fetchMode);
        if (aa != null) {
            return aa;
        }
        return super.findNonSingleVersionImpl(id, idFilter, fetchMode);
    }

    private NRepository getLocalMavenRepo() {
        for (NRepository nRepository : workspace.getRepositories()) {
            if (nRepository.getRepositoryType().equals(NConstants.RepoTypes.MAVEN)
                    && nRepository.config().getLocationPath() != null
                    && nRepository.config().getLocationPath().toString()
                    .equals(
                            Paths.get(NPath.of("~/.m2").toAbsolute(NWorkspace.of().getWorkspaceLocation()).toString()).toString()
                    )) {
                return nRepository;
            }
        }
        return null;
    }

    protected NPath getMavenLocalFolderContent(NId id) {
        NPath p = getIdRelativePath(id);
        if (p != null) {
            return NPath.ofUserHome().resolve(".m2").resolve(p);
        }
        return null;
    }
    private MvnClient getWrapper() {
        if (true) {
            return null;
        }
        return new MvnClient(getWorkspace());
    }

    public NPath fetchContentCoreUsingWrapper(NId id, NDescriptor descriptor, NFetchMode fetchMode) {
        if (wrapper == null) {
            wrapper = getWrapper();
        }
        if (wrapper != null && wrapper.get(id, config().getLocationPath().toString())) {
            NRepository repo = getLocalMavenRepo();
            if (repo != null) {
                NRepositorySPI repoSPI = NWorkspaceUtils.of(workspace).repoSPI(repo);
                return repoSPI.fetchContent()
                        .setId(id)
                        .setDescriptor(descriptor)
                        .setFetchMode(NFetchMode.LOCAL)
                        .run()
                        .getResult();
            }
            //should be already downloaded to m2 folder
            NPath content = getMavenLocalFolderContent(id);
            if (content != null && content.exists()) {
                return content.setUserCache(true).setUserTemporary(false);
            }
        }
        return null;
    }


    public String getIdExtension(NId id) {
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
                return getIdExtension(id.builder().setFaceContent().build()) + ".sha1";
            }
            case NConstants.QueryFaces.CONTENT: {
                String packaging = q.get(NConstants.IdProperties.PACKAGING);
                return NWorkspace.of().getDefaultIdContentExtension(packaging);
            }
            default: {
                throw new NUnsupportedArgumentException(NMsg.ofC("unsupported fact %s", f));
            }
        }
    }

    public NDescriptor fetchDescriptorCore(NId id, NFetchMode fetchMode) {
        if (!acceptedFetchNoCache(fetchMode)) {
            throw new NNotFoundException(id, new NFetchModeNotSupportedException(this, fetchMode, id.toString(), null));
        }
        InputStream stream = null;
        try {
            NDescriptor nutsDescriptor = null;
            byte[] bytes = null;
            String name = null;
            NId idDesc = id.builder().setFaceDescriptor().build();
            try {
                stream = getStream(idDesc, "artifact descriptor", "retrieve");
                name = NInputSource.of(stream).getMetaData().getName().orElse("no-name");
                bytes = NIOUtils.loadByteArray(stream, true);
                nutsDescriptor = MavenUtils.of().parsePomXmlAndResolveParents(
                        CoreIOUtils.createBytesStream(bytes, name == null ? null : NMsg.ofNtf(name), "text/xml", StandardCharsets.UTF_8.name(), "pom.xml")
                        , fetchMode, getIdRemotePath(id).toString(), this);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            checkSHA1Hash(id.builder().setFace(NConstants.QueryFaces.DESCRIPTOR_HASH).build(),
                    CoreIOUtils.createBytesStream(bytes, name == null ? null : NMsg.ofNtf(name), "text/xml", StandardCharsets.UTF_8.name(), "pom.xml")
                    , "artifact descriptor");
            return nutsDescriptor;
        } catch (IOException | UncheckedIOException | NIOException ex) {
            throw new NNotFoundException(id, ex);
        }
    }
}
