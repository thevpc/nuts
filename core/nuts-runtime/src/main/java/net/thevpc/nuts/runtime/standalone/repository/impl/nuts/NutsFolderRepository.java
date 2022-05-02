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
 *
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
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsStreamMetadata;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.folder.NutsFolderRepositoryBase;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsConstants;
import net.thevpc.nuts.util.NutsUtilStrings;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Created by vpc on 1/5/17.
 */
public class NutsFolderRepository extends NutsFolderRepositoryBase {


    public NutsFolderRepository(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parentRepository) {
        super(options, session, parentRepository, null, true, NutsConstants.RepoTypes.NUTS,true);
        repoIter = new NutsRepoIter(this);
//        LOG = session.log().of(NutsFolderRepository.class);
        extensions.put("src", "-src.zip");
    }

    @Override
    public NutsId searchLatestVersionCore(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
        return null;
    }

    public String getIdExtension(NutsId id, NutsSession session) {
        checkSession(session);
        Map<String, String> q = id.getProperties();
        String f = NutsUtilStrings.trim(q.get(NutsConstants.IdProperties.FACE));
        switch (f) {
            case NutsConstants.QueryFaces.DESCRIPTOR: {
                return ".nuts";
            }
            case NutsConstants.QueryFaces.DESCRIPTOR_HASH: {
                return ".nuts.sha1";
            }
            case CoreNutsConstants.QueryFaces.CATALOG: {
                return ".nuts.catalog";
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
        try {
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
                            CoreIOUtils.createBytesStream(bytes, name == null ? null : NutsMessage.formatted(name), "application/json", "nuts.json", session)
                            , fetchMode, getIdRemotePath(id, session).toString(), this);
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
                checkSHA1Hash(id.builder().setFace(NutsConstants.QueryFaces.DESCRIPTOR_HASH).build(),
                        CoreIOUtils.createBytesStream(bytes, name == null ? null : NutsMessage.formatted(name), "application/json", "nuts.json", session)
                        , "artifact descriptor", session);
                return nutsDescriptor;
            } catch (IOException | UncheckedIOException | NutsIOException ex) {
                throw new NutsNotFoundException(session, id, ex);
            }
        }catch (NutsNotFoundException e){
            //ignore
        }
        //now try pom file (maven!)
        checkSession(session);
        InputStream stream = null;
        try {
            NutsDescriptor nutsDescriptor = null;
            byte[] bytes = null;
            String name = null;
            try {
                NutsPath pomURL =
                        config().setSession(session).getLocationPath().resolve(
                                getIdBasedir(id, session).resolve(
                                        getIdFilename(id,".pom", session)
                                )
                        );
                stream = openStream(id, pomURL, id, "artifact descriptor", "retrieve", session);
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
