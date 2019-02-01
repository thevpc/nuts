/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.bridges.maven;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.repos.AbstractNutsRepository;
import net.vpc.app.nuts.core.util.CorePlatformUtils;
import net.vpc.app.nuts.core.util.CoreSecurityUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.strings.StringUtils;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 2/20/17.
 */
public abstract class AbstractMavenRepository extends AbstractNutsRepository {

    private static final Logger log = Logger.getLogger(AbstractMavenRepository.class.getName());

    public AbstractMavenRepository(NutsRepositoryConfig config, NutsWorkspace workspace, NutsRepository parentRepository, String repositoryRoot, int slowness) {
        super(config, workspace, parentRepository, repositoryRoot, slowness);
        extensions.put("src", "-src.zip");
        extensions.put("pom", ".pom");
    }

    protected abstract String getIdPath(NutsId id);

    protected InputStream getStream(NutsId id, NutsSession session) {
        String url = getIdPath(id);
        return openStream(id, url, id, session);
    }

    protected String getStreamAsString(NutsId id, NutsSession session) {
        String url = getIdPath(id);
        return IOUtils.loadString(openStream(id, url, id, session), true);
    }

    protected void checkSHA1Hash(NutsId id, InputStream stream, NutsSession session) throws IOException {
        switch (StringUtils.trim(id.getFace())) {
            case NutsConstants.FACE_COMPONENT_HASH:
            case NutsConstants.FACE_DESC_HASH: {
                break;
            }
            default: {
                log.log(Level.SEVERE, "[BUG] Unsupported Hash Type " + id.getFace(), new RuntimeException());
                throw new IOException("Unsupported hash type " + id.getFace());
            }
        }
        try {
            String rhash = getStreamSHA1(id, session);
            String lhash = CoreSecurityUtils.evalSHA1(stream, true);
            if (!rhash.equals(lhash)) {
                throw new IOException("Invalid file hash " + id);
            }
        } finally {
            stream.close();
        }
    }

    protected String getStreamSHA1(NutsId id, NutsSession session) {
        String hash = getStreamAsString(id, session).toUpperCase();
        for (String s : hash.split("[ \n\r]")) {
            if (s.length() > 0) {
                return s;
            }
        }
        return hash.split("[ \n\r]")[0];
    }

    protected abstract InputStream openStream(NutsId id, String path, Object source, NutsSession session);

    @Override
    public boolean isSupportedMirroring() {
        return false;
    }

    @Override
    public void pushImpl(NutsId id, String repoId, NutsConfirmAction foundAction, NutsSession session) {
        throw new NutsUnsupportedOperationException();
    }

    @Override
    protected NutsId deployImpl(NutsId id, NutsDescriptor descriptor, String file, NutsConfirmAction foundAction, NutsSession context) {
        throw new NutsUnsupportedOperationException();
    }

    @Override
    protected void undeployImpl(NutsId id, NutsSession session) {
        throw new NutsUnsupportedOperationException();
    }

    @Override
    protected NutsDescriptor fetchDescriptorImpl(NutsId id, NutsSession session) {
        InputStream stream = null;
        try {
            NutsDescriptor nutsDescriptor = null;
            byte[] bytes = null;
            NutsId idDesc = id.setFaceDescriptor();
            try {
                stream = getStream(idDesc, session);
                bytes = IOUtils.loadByteArray(stream, true);
                nutsDescriptor = MavenUtils.parsePomXml(new ByteArrayInputStream(bytes), getWorkspace(), session, getIdPath(id));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            checkSHA1Hash(id.setFace(NutsConstants.FACE_DESC_HASH), new ByteArrayInputStream(bytes), session);
            File jar = new File(getIdPath(getWorkspace().getConfigManager().createComponentFaceId(idDesc, nutsDescriptor)));
            nutsDescriptor = annotateExecDesc(nutsDescriptor, jar);
            return nutsDescriptor;
        } catch (IOException ex) {
            throw new NutsNotFoundException(id, null, ex);
        } catch (NutsIOException ex) {
            throw new NutsNotFoundException(id, null, ex);
        }
    }

    //TODO call this in workspace!!!
    public NutsDescriptor annotateExecDesc(NutsDescriptor nutsDescriptor, File jar) {
        boolean executable = nutsDescriptor.isExecutable();
        boolean nutsApp = nutsDescriptor.isNutsApplication();
        if (jar.getName().toLowerCase().endsWith(".jar") && jar.isFile()) {
            File f = new File(jar.getPath() + ".info");
            Map<String, String> map = null;
            try {
                if (f.isFile()) {
                    map = getWorkspace().getIOManager().readJson(f, Map.class);
                }
            } catch (Exception ex) {
                //
            }
            if (map != null) {
                executable = "true".equals(map.get("executable"));
                nutsApp = "true".equals(map.get("nutsApplication"));
            } else {
                try {
                    NutsExecutionEntry[] t = CorePlatformUtils.parseMainClasses(jar);
                    if (t.length > 0) {
                        executable = true;
                        if (t[0].isApp()) {
                            nutsApp = true;
                        }
                    }
                    try {
                        map = new LinkedHashMap<>();
                        map.put("executable", String.valueOf(executable));
                        map.put("nutsApplication", String.valueOf(nutsApp));
                        getWorkspace().getIOManager().writeJson(map, f, true);
                    } catch (Exception ex) {
                        //
                    }
                } catch (Exception ex) {
                    //
                }
            }
        }
        nutsDescriptor = nutsDescriptor.setExecutable(executable);
        nutsDescriptor = nutsDescriptor.setNutsApplication(nutsApp);
        return nutsDescriptor;
    }


    protected String getIdExtension(NutsId id) {
        Map<String, String> q = id.getQueryMap();
        String f = StringUtils.trim(q.get(NutsConstants.QUERY_FACE));
        switch (f) {
            case NutsConstants.FACE_DESCRIPTOR: {
                return "pom";
            }
            case NutsConstants.FACE_DESC_HASH: {
                return "pom.sha1";
            }
            case NutsConstants.FACE_CATALOG: {
                return "catalog";
            }
            case NutsConstants.FACE_COMPONENT_HASH: {
                return getIdExtension(id.setFaceComponent()) + ".sha1";
            }
            case NutsConstants.FACE_COMPONENT: {
                String packaging = q.get(NutsConstants.QUERY_PACKAGING);
                if (StringUtils.isEmpty(packaging)) {
                    throw new NutsIllegalArgumentException("Unsupported empty Packaging");
                }
                if (!StringUtils.isEmpty(packaging)) {
                    switch (packaging) {
                        case "bundle":
                        case "nuts-extension":
                        case "maven-archetype":
                            return "jar";
                    }
                    return packaging;
                }
                return packaging;
            }
            default: {
                throw new IllegalArgumentException("Unsupported fact " + f);
            }
        }
    }
}
