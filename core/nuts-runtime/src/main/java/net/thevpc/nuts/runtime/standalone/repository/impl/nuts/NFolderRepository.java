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
package net.thevpc.nuts.runtime.standalone.repository.impl.nuts;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.folder.NFolderRepositoryBase;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Created by vpc on 1/5/17.
 */
public class NFolderRepository extends NFolderRepositoryBase {


    public NFolderRepository(NAddRepositoryOptions options, NSession session, NRepository parentRepository) {
        super(options, session, parentRepository, null, true, NConstants.RepoTypes.NUTS, true);
        repoIter = new NRepoIter(this);
//        LOG = session.log().of(NutsFolderRepository.class);
        extensions.put("src", "-src.zip");
    }

    @Override
    public NId searchLatestVersionCore(NId id, NIdFilter filter, NFetchMode fetchMode, NSession session) {
        return null;
    }

    public String getIdExtension(NId id, NSession session) {
        checkSession(session);
        Map<String, String> q = id.getProperties();
        String f = NStringUtils.trim(q.get(NConstants.IdProperties.FACE));
        switch (f) {
            case NConstants.QueryFaces.DESCRIPTOR: {
                return ".nuts";
            }
            case NConstants.QueryFaces.DESCRIPTOR_HASH: {
                return ".nuts.sha1";
            }
            case CoreNConstants.QueryFaces.CATALOG: {
                return ".nuts.catalog";
            }
            case NConstants.QueryFaces.CONTENT_HASH: {
                return getIdExtension(id.builder().setFaceContent().build(), session) + ".sha1";
            }
            case NConstants.QueryFaces.CONTENT: {
                String packaging = q.get(NConstants.IdProperties.PACKAGING);
                return session.locations().getDefaultIdContentExtension(packaging);
            }
            default: {
                throw new NUnsupportedArgumentException(session, NMsg.ofCstyle("unsupported fact %s", f));
            }
        }
    }

    public NDescriptor fetchDescriptorCore(NId id, NFetchMode fetchMode, NSession session) {
        checkSession(session);
        if (!acceptedFetchNoCache(fetchMode)) {
            throw new NNotFoundException(session, id, new NFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        try {
            InputStream stream = null;
            try {
                NDescriptor nutsDescriptor = null;
                byte[] bytes = null;
                String name = null;
                NId idDesc = id.builder().setFaceDescriptor().build();
                try {
                    stream = getStream(idDesc, "artifact descriptor", "retrieve", session);
                    bytes = CoreIOUtils.loadByteArray(stream, true, session);
                    name = NIO.of(session).createInputSource(stream).getInputMetaData().getName().orElse("no-name");
                    nutsDescriptor = MavenUtils.of(session).parsePomXmlAndResolveParents(
                            CoreIOUtils.createBytesStream(bytes, NMsg.ofNtf(name), "application/json", "nuts.json", session)
                            , fetchMode, getIdRemotePath(id, session).toString(), this);
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
                checkSHA1Hash(id.builder().setFace(NConstants.QueryFaces.DESCRIPTOR_HASH).build(),
                        CoreIOUtils.createBytesStream(bytes, name == null ? null : NMsg.ofNtf(name), "application/json", "nuts.json", session)
                        , "artifact descriptor", session);
                return nutsDescriptor;
            } catch (IOException | UncheckedIOException | NIOException ex) {
                throw new NNotFoundException(session, id, ex);
            }
        } catch (NNotFoundException e) {
            //ignore
        }
        //now try pom file (maven!)
        checkSession(session);
        InputStream stream = null;
        try {
            NDescriptor nutsDescriptor = null;
            byte[] bytes = null;
            String name = null;
            try {
                NPath pomURL =
                        config().setSession(session).getLocationPath().resolve(
                                getIdBasedir(id, session).resolve(
                                        getIdFilename(id, ".pom", session)
                                )
                        );
                stream = openStream(id, pomURL, id, "artifact descriptor", "retrieve", session);
                bytes = CoreIOUtils.loadByteArray(stream, true, session);
                name = NIO.of(session).createInputSource(stream).getInputMetaData().getName().orElse("no-name");
                nutsDescriptor = MavenUtils.of(session).parsePomXmlAndResolveParents(
                        CoreIOUtils.createBytesStream(bytes, NMsg.ofNtf(name), "text/xml", "pom.xml", session)
                        , fetchMode, getIdRemotePath(id, session).toString(), this);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            checkSHA1Hash(id.builder().setFace(NConstants.QueryFaces.DESCRIPTOR_HASH).build(),
                    CoreIOUtils.createBytesStream(bytes, name == null ? null : NMsg.ofNtf(name), "text/xml", "pom.xml", session)
                    , "artifact descriptor", session);
            return nutsDescriptor;
        } catch (IOException | UncheckedIOException | NIOException ex) {
            throw new NNotFoundException(session, id, ex);
        }
    }
}
