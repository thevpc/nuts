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
package net.thevpc.nuts.runtime.standalone.repository.impl.nuts;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.folder.NFolderRepositoryBase;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Created by vpc on 1/5/17.
 */
public class NFolderRepository extends NFolderRepositoryBase {


    public NFolderRepository(NAddRepositoryOptions options, NWorkspace workspace, NRepository parentRepository) {
        super(options, workspace, parentRepository, null, true, NConstants.RepoTypes.NUTS, true);
        repoIter = new NRepoIter(this);
//        LOG = session.log().of(NutsFolderRepository.class);
        extensions.put("src", "-src.zip");
    }

    @Override
    public NId searchLatestVersionCore(NId id, NIdFilter filter, NFetchMode fetchMode) {
        return null;
    }

    public String getIdExtension(NId id) {
        NSession session=getWorkspace().currentSession();
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
                return getIdExtension(id.builder().setFaceContent().build()) + ".sha1";
            }
            case NConstants.QueryFaces.CONTENT: {
                String packaging = q.get(NConstants.IdProperties.PACKAGING);
                return NLocations.of().getDefaultIdContentExtension(packaging);
            }
            default: {
                throw new NUnsupportedArgumentException(NMsg.ofC("unsupported fact %s", f));
            }
        }
    }

    public NDescriptor fetchDescriptorCore(NId id, NFetchMode fetchMode) {
        NSession session=getWorkspace().currentSession();
        if (!acceptedFetchNoCache(fetchMode)) {
            throw new NNotFoundException(id, new NFetchModeNotSupportedException(this, fetchMode, id.toString(), null));
        }
        NPath nutsPath = getIdRemotePath(id);
        NNotFoundException nutsPathEx = null;
        try {
            InputStream stream = null;
            NId idDesc = id.builder().setFaceDescriptor().build();
            try {
                NDescriptor nutsDescriptor = null;
                byte[] bytes = null;
                String name = null;
                try {
                    stream = getStream(idDesc, "artifact descriptor", "retrieve");
                    bytes = CoreIOUtils.loadByteArray(stream, true);
                    name = NInputSource.of(stream).getMetaData().getName().orElse("no-name");
                    nutsDescriptor = NDescriptorParser.of()
                            .setDescriptorStyle(NDescriptorStyle.NUTS)
                            .parse(CoreIOUtils.createBytesStream(bytes, NMsg.ofNtf(name), "application/json", StandardCharsets.UTF_8.name(), "nuts.json")).get();
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
                checkSHA1Hash(id.builder().setFace(NConstants.QueryFaces.DESCRIPTOR_HASH).build(),
                        CoreIOUtils.createBytesStream(bytes, name == null ? null : NMsg.ofNtf(name), "application/json", StandardCharsets.UTF_8.name(), "nuts.json")
                        , "artifact descriptor");
                return nutsDescriptor;
            } catch (IOException | UncheckedIOException | NIOException ex) {
                throw new NNotFoundException(id,
                        new NNotFoundException.NIdInvalidDependency[0],
                        new NNotFoundException.NIdInvalidLocation[]{
                                new NNotFoundException.NIdInvalidLocation(
                                        getName(),
                                        getIdRemotePath(idDesc).toString(),
                                        ex.getMessage()
                                )
                        },
                        ex);
            }
        } catch (NNotFoundException e) {
            nutsPathEx = e;
            //ignore
        }
        //now try pom file (maven!)
        InputStream stream = null;
        NPath pomURL =
                config().getLocationPath().resolve(
                        getIdBasedir(id).resolve(
                                getIdFilename(id, ".pom")
                        )
                );
        try {
            NDescriptor nutsDescriptor = null;
            byte[] bytes = null;
            String name = null;
            try {
                stream = openStream(id, pomURL, id, "artifact descriptor", "retrieve");
                bytes = CoreIOUtils.loadByteArray(stream, true);
                name = NInputSource.of(stream).getMetaData().getName().orElse("no-name");
                nutsDescriptor = NDescriptorParser.of()
                        .setDescriptorStyle(NDescriptorStyle.NUTS)
                        .parse(CoreIOUtils.createBytesStream(bytes, NMsg.ofNtf(name), "text/xml", StandardCharsets.UTF_8.name(), "pom.xml")).get();

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
            throw new NNotFoundException(id,
                    new NNotFoundException.NIdInvalidDependency[0],
                    new NNotFoundException.NIdInvalidLocation[]{
                            new NNotFoundException.NIdInvalidLocation(
                                    getName(), nutsPath.toString(), nutsPathEx.getMessage()
                            ),
                            new NNotFoundException.NIdInvalidLocation(
                                    getName(), pomURL.toString(), ex.getMessage()
                            )
                    },
                    ex);
        }
    }
}
