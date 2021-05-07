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
package net.thevpc.nuts.runtime.standalone.bridges.maven;

import net.thevpc.nuts.*;

import java.io.*;
import java.util.Map;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.CoreNutsConstants;
import net.thevpc.nuts.runtime.standalone.io.NamedByteArrayInputStream;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 * Created by vpc on 2/20/17.
 */
public abstract class AbstractMavenRepositoryHelper {

    private NutsLogger LOG;
    private NutsRepository repository;

    public AbstractMavenRepositoryHelper(NutsRepository repository) {
        this.repository = repository;
//        LOG=repository.getWorkspace().log().of(AbstractMavenRepositoryHelper.class);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = this.repository.getWorkspace().log().setSession(session).of(AbstractMavenRepositoryHelper.class);
        }
        return LOG;
    }

    protected abstract String getIdPath(NutsId id, NutsSession session);

    protected NutsInput getStream(NutsId id, String typeName, NutsSession session) {
        String url = getIdPath(id, session);
        return openStream(id, url, id, typeName, session);
    }

    protected String getStreamAsString(NutsId id, String typeName, NutsSession session) {
        String url = getIdPath(id, session);
        return CoreIOUtils.loadString(openStream(id, url, id, typeName, session).open(), true);
    }

    protected void checkSHA1Hash(NutsId id, InputStream stream, String typeName, NutsSession session) throws IOException {
        switch (CoreStringUtils.trim(id.getFace())) {
            case NutsConstants.QueryFaces.CONTENT_HASH:
            case NutsConstants.QueryFaces.DESCRIPTOR_HASH: {
                break;
            }
            default: {
                _LOGOP(session).level(Level.SEVERE).error(new IllegalArgumentException("unsupported Hash Type " + id.getFace())).log("[BUG] Unsupported Hash Type {0}", id.getFace());
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
            String lhash = CoreIOUtils.evalSHA1Hex(stream, true);
            if (!rhash.equalsIgnoreCase(lhash)) {
                throw new IOException("invalid file hash " + id);
            }
        } finally {
            stream.close();
        }
    }

    protected String getStreamSHA1(NutsId id, NutsSession session, String typeName) {
        String hash = getStreamAsString(id, typeName + " SHA1", session).toUpperCase();
        for (String s : hash.split("[ \n\r]")) {
            if (s.length() > 0) {
                return s;
            }
        }
        return hash.split("[ \n\r]")[0];
    }

    protected abstract boolean exists(NutsId id, String path, Object source, String typeName, NutsSession session);
    protected abstract NutsInput openStream(NutsId id, String path, Object source, String typeName, NutsSession session);

    private void checkSession(NutsSession session) {
        NutsWorkspaceUtils.checkSession(repository.getWorkspace(), session);
    }

    public NutsDescriptor fetchDescriptorImpl(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        checkSession(session);
        NutsInput stream = null;
        try {
            NutsDescriptor nutsDescriptor = null;
            byte[] bytes = null;
            String name = null;
            NutsId idDesc = id.builder().setFaceDescriptor().build();
            try {
                stream = getStream(idDesc, "artifact descriptor", session);
                bytes = CoreIOUtils.loadByteArray(stream.open(), true);
                name = stream.getName();
                nutsDescriptor = MavenUtils.of(session).parsePomXml(new NamedByteArrayInputStream(bytes, name), fetchMode, getIdPath(id, session), repository, session);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            checkSHA1Hash(id.builder().setFace(NutsConstants.QueryFaces.DESCRIPTOR_HASH).build(), new NamedByteArrayInputStream(bytes, name), "package descriptor", session);
            return nutsDescriptor;
        } catch (IOException | UncheckedIOException | NutsIOException ex) {
            throw new NutsNotFoundException(session, id, ex);
        }
    }

    protected String getIdExtension(NutsId id, NutsSession session) {
        checkSession(session);
        Map<String, String> q = id.getProperties();
        String f = CoreStringUtils.trim(q.get(NutsConstants.IdProperties.FACE));
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
                return session.getWorkspace().locations().getDefaultIdContentExtension(packaging);
            }
            default: {
                throw new NutsUnsupportedArgumentException(session, "unsupported fact " + f);
            }
        }
    }
}
