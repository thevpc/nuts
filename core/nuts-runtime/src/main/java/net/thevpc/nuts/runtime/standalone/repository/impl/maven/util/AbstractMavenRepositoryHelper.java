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
package net.thevpc.nuts.runtime.standalone.repository.impl.maven.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsConstants;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NutsDigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by vpc on 2/20/17.
 */
public abstract class AbstractMavenRepositoryHelper {

    private NutsLogger LOG;
    private final NutsRepository repository;

    public AbstractMavenRepositoryHelper(NutsRepository repository) {
        this.repository = repository;
//        LOG=repository.getWorkspace().log().of(AbstractMavenRepositoryHelper.class);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(AbstractMavenRepositoryHelper.class, session);
        }
        return LOG;
    }

    public abstract NutsPath getIdPath(NutsId id, NutsSession session);

    public InputStream getStream(NutsId id, String typeName, String action, NutsSession session) {
        NutsPath url = getIdPath(id, session);
        return openStream(id, url, id, typeName, action, session);
    }

    public String getStreamAsString(NutsId id, String typeName, String action, NutsSession session) {
        byte[] barr = NutsCp.of(session)
                .addOptions(NutsPathOption.LOG, NutsPathOption.TRACE)
                .from(getIdPath(id, session))
                .setSourceOrigin(id)
                .setActionMessage(action==null?null:NutsMessage.plain(action))
                .setSourceTypeName(action)
                .getByteArrayResult()
                ;
        return new String(barr);
//        return CoreIOUtils.loadString(openStream(id, url, id, typeName, action, session), true, session);
    }

    public void checkSHA1Hash(NutsId id, InputStream stream, String typeName, NutsSession session) throws IOException {
        if (!repository.isRemote()) {
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
        session.getTerminal().printProgress("%-14s %-8s %s",repository.getName(), action, path.toCompressedForm());
        return NutsInputStreamMonitor.of(session).setSource(path).setOrigin(source).setSourceTypeName(typeName).create();
    }

    private void checkSession(NutsSession session) {
        NutsWorkspaceUtils.checkSession(repository.getWorkspace(), session);
    }

    public NutsDescriptor fetchDescriptorImpl(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        checkSession(session);
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
                        , fetchMode, getIdPath(id, session).toString(), repository);
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
}
