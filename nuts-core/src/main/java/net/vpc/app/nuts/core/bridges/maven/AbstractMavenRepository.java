/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.bridges.maven;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.repos.AbstractNutsRepository;
import net.vpc.app.nuts.core.util.io.CoreSecurityUtils;

import java.io.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.InputSource;

/**
 * Created by vpc on 2/20/17.
 */
public abstract class AbstractMavenRepository extends AbstractNutsRepository {

    private static final Logger log = Logger.getLogger(AbstractMavenRepository.class.getName());

    public AbstractMavenRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository, int speed, String repositoryType) {
        super(options, workspace, parentRepository, speed, false, repositoryType);
        extensions.put("src", "-src.zip");
        extensions.put("pom", ".pom");
    }

    protected abstract String getIdPath(NutsId id);

    protected InputSource getStream(NutsId id, NutsRepositorySession session) {
        String url = getIdPath(id);
        return openStream(id, url, id, session);
    }

    protected String getStreamAsString(NutsId id, NutsRepositorySession session) {
        String url = getIdPath(id);
        return CoreIOUtils.loadString(openStream(id, url, id, session).open(), true);
    }

    protected void checkSHA1Hash(NutsId id, InputStream stream, NutsRepositorySession session) throws IOException {
        switch (CoreStringUtils.trim(id.getFace())) {
            case NutsConstants.QueryFaces.COMPONENT_HASH:
            case NutsConstants.QueryFaces.DESC_HASH: {
                break;
            }
            default: {
                log.log(Level.SEVERE, "[BUG] Unsupported Hash Type " + id.getFace(), new RuntimeException());
                throw new IOException("Unsupported hash type " + id.getFace());
            }
        }
        try {
            String rhash = getStreamSHA1(id, session);
            String lhash = CoreIOUtils.evalSHA1(stream, true);
            if (!rhash.equalsIgnoreCase(lhash)) {
                throw new IOException("Invalid file hash " + id);
            }
        } finally {
            stream.close();
        }
    }

    protected String getStreamSHA1(NutsId id, NutsRepositorySession session) {
        String hash = getStreamAsString(id, session).toUpperCase();
        for (String s : hash.split("[ \n\r]")) {
            if (s.length() > 0) {
                return s;
            }
        }
        return hash.split("[ \n\r]")[0];
    }

    protected abstract InputSource openStream(NutsId id, String path, Object source, NutsRepositorySession session);

    @Override
    public void pushImpl(NutsId id, NutsPushCommand options, NutsRepositorySession session) {
        throw new NutsUnsupportedOperationException();
    }

    @Override
    protected void deployImpl(NutsRepositoryDeploymentOptions deployment, NutsRepositorySession context) {
        throw new NutsUnsupportedOperationException();
    }

    @Override
    protected void undeployImpl(NutsRepositoryUndeploymentOptions options, NutsRepositorySession session) {
        throw new NutsUnsupportedOperationException();
    }

    @Override
    protected NutsDescriptor fetchDescriptorImpl(NutsId id, NutsRepositorySession session) {
        InputSource stream = null;
        try {
            NutsDescriptor nutsDescriptor = null;
            byte[] bytes = null;
            NutsId idDesc = id.setFaceDescriptor();
            try {
                stream = getStream(idDesc, session);
                bytes = CoreIOUtils.loadByteArray(stream.open(), true);
                nutsDescriptor = MavenUtils.parsePomXml(new ByteArrayInputStream(bytes), getWorkspace(), session, getIdPath(id));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            checkSHA1Hash(id.setFace(NutsConstants.QueryFaces.DESC_HASH), new ByteArrayInputStream(bytes), session);
            return nutsDescriptor;
        } catch (IOException ex) {
            throw new NutsNotFoundException(id, null, ex);
        } catch (UncheckedIOException ex) {
            throw new NutsNotFoundException(id, null, ex);
        }
    }

    @Override
    protected String getIdExtension(NutsId id) {
        Map<String, String> q = id.getQueryMap();
        String f = CoreStringUtils.trim(q.get(NutsConstants.QueryKeys.FACE));
        switch (f) {
            case NutsConstants.QueryFaces.DESCRIPTOR: {
                return ".pom";
            }
            case NutsConstants.QueryFaces.DESC_HASH: {
                return ".pom.sha1";
            }
            case NutsConstants.QueryFaces.CATALOG: {
                return ".catalog";
            }
            case NutsConstants.QueryFaces.COMPONENT_HASH: {
                return getIdExtension(id.setFaceComponent()) + ".sha1";
            }
            case NutsConstants.QueryFaces.COMPONENT: {
                String packaging = q.get(NutsConstants.QueryKeys.PACKAGING);
                return getIdComponentExtension(packaging);
            }
            default: {
                throw new NutsUnsupportedArgumentException("Unsupported fact " + f);
            }
        }
    }
}
