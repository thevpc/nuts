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
package net.vpc.app.nuts.bridges.maven;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.filters.DefaultNutsIdMultiFilter;
import net.vpc.app.nuts.extensions.repos.AbstractNutsRepository;
import net.vpc.app.nuts.extensions.util.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 2/20/17.
 */
public abstract class AbstractMavenRepository extends AbstractNutsRepository {

    private static final Logger log = Logger.getLogger(AbstractMavenRepository.class.getName());

    public AbstractMavenRepository(NutsRepositoryConfig config, NutsWorkspace workspace, File root, int slowness) {
        super(config, workspace, root, slowness);
        extensions.put("src", "-src.zip");
        extensions.put("pom", ".pom");
    }

    protected abstract String getPath(NutsId id, String extension);

    protected InputStream getStream(NutsId id, String extension) {
        String url = getPath(id, extension);
//        if (url.startsWith("http")) {
//            System.out.printf("Why");
//        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " downloading maven " + CoreStringUtils.alignLeft("\'" + extension + "\'", 20) + " url " + url);
        return openStream(url);
    }

    protected String getStreamAsString(NutsId id, String extension) {
        String url = getPath(id, extension);
//        if (url.startsWith("http")) {
//            System.out.printf("Why");
//        }
        log.log(Level.FINEST, CoreStringUtils.alignLeft(getRepositoryId(), 20) + " downloading maven " + CoreStringUtils.alignLeft("\'" + extension + "\'", 20) + " url " + url);
        return CoreIOUtils.readStreamAsString(openStream(url), true);
    }

    protected void checkSHA1Hash(NutsId id, String extension, InputStream stream) throws IOException {
        try {
            String rhash = getStreamSHA1(id, extension);
            String lhash = CoreSecurityUtils.evalSHA1(stream, true);
            if (!rhash.equals(lhash)) {
                throw new IOException("Invalid file hash " + id);
            }
        } finally {
            stream.close();
        }
    }

    protected String getStreamSHA1(NutsId id, String extension) {
        String hash = getStreamAsString(id, extension + ".sha1").toUpperCase();
        for (String s : hash.split("[ \n\r]")) {
            if (s.length() > 0) {
                return s;
            }
        }
        return hash.split("[ \n\r]")[0];
    }

    protected abstract InputStream openStream(String path);

    @Override
    public boolean isSupportedMirroring() {
        return false;
    }

    @Override
    public void pushImpl(NutsId id, String repoId, NutsSession session) {
        throw new NutsUnsupportedOperationException();
    }

    @Override
    protected NutsId deployImpl(NutsId id, NutsDescriptor descriptor, File file, NutsSession context) {
        throw new NutsUnsupportedOperationException();
    }

    protected void undeployImpl(NutsId id, NutsSession session) {
        throw new NutsUnsupportedOperationException();
    }

    @Override
    protected NutsDescriptor fetchDescriptorImpl(NutsId id, NutsSession session) {
        InputStream stream = null;
        try {
            NutsDescriptor nutsDescriptor = null;//parsePomXml(getStream(id, ".pom"), session);
            byte[] bytes = null;
            try {
                stream = getStream(id, ".pom");
                bytes = CoreIOUtils.readStreamAsBytes(stream, true);
                nutsDescriptor = MavenUtils.parsePomXml(new ByteArrayInputStream(bytes), getWorkspace(), session, getPath(id, ".pom"));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            checkSHA1Hash(id, ".pom", new ByteArrayInputStream(bytes));
            String ext = resolveExtension(nutsDescriptor);
            File jar = new File(getPath(id, ext));
            nutsDescriptor = nutsDescriptor.setExecutable(CorePlatformUtils.isExecutableJar(jar));
            return nutsDescriptor;
        } catch (IOException ex) {
            throw new NutsNotFoundException(id.toString(), null, ex);
        } catch (NutsIOException ex) {
            throw new NutsNotFoundException(id.toString(), null, ex);
        }
    }

    @Override
    protected File copyToImpl(NutsId id, NutsSession session, File localPath) {
        try {
            NutsDescriptor d = getWorkspace().fetchDescriptor(id.toString(), true, session);
            String ext = resolveExtension(d);
            if (localPath.isDirectory()) {
                localPath = new File(localPath, CoreNutsUtils.getNutsFileName(id, ext));
            }
            CoreIOUtils.copy(getStream(id, ext), localPath, true, true);
            checkSHA1Hash(id, ext, new FileInputStream(localPath));
            return localPath;
        } catch (NutsIOException ex) {
            throw new NutsNotFoundException(id.toString(), null, ex);
        } catch (IOException ex) {
            throw new NutsNotFoundException(id.toString(), null, ex);
        }
    }

    protected String resolveExtension(NutsDescriptor d) {
        String ee = d.getExt();
        if (!CoreStringUtils.isEmpty(ee)) {
            if ("bundle".equals(ee)) {
                return ".jar";
            }
            if ("nuts-extension".equals(ee)) {
                return ".jar";
            }
            return "." + ee;
        }
        String ext = "";
        if (CoreStringUtils.isEmpty(d.getPackaging())) {
            ext = ".jar";
        } else if (d.getPackaging().equals("maven-archetype")) {
            ext = ".jar";
        } else if (d.getPackaging().equals("bundle")) {
            ext = ".jar";
        } else {
            ext = ("." + d.getPackaging());
        }
        return ext;
    }

    public File copyDescriptorToImpl(NutsId id, NutsSession session, File localPath) {
        NutsDescriptor nutsDescriptor = fetchDescriptor(id, session);
        if (localPath.isDirectory()) {
            localPath = new File(localPath, CoreNutsUtils.getNutsFileName(id, "pom"));
        }
        nutsDescriptor.write(localPath);
        return localPath;
    }

    @Override
    public String fetchHashImpl(NutsId id, NutsSession session) {
        return getStreamSHA1(id, ".jar");
    }

    @Override
    public String fetchDescriptorHashImpl(NutsId id, NutsSession session) {
        InputStream stream = null;
        NutsDescriptor nutsDescriptor = null;
        try {
            try {
                stream = getStream(id, ".pom");
                nutsDescriptor = MavenUtils.parsePomXml(stream);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (NutsIOException ex) {
            throw new NutsNotFoundException(id.toString(), null, ex);
        } catch (IOException ex) {
            throw new NutsNotFoundException(id.toString(), null, ex);
        }
        return nutsDescriptor.getSHA1();
    }

    @Override
    public NutsId resolveIdImpl(NutsId id, NutsSession session) {
        String versionString = id.getVersion().getValue();
        if (CoreVersionUtils.isStaticVersionPattern(versionString)) {
            try {
                NutsSession transitiveSession = session.copy().setTransitive(true);
                NutsDescriptor d = fetchDescriptor(id, transitiveSession);
                if (d != null) {
                    return id;
                }
            } catch (Exception ex) {
                //not found
            }
            throw new NutsNotFoundException(id);
        } else {
//            CoreNutsUtils.And(
//                    CoreNutsUtils.createNutsDescriptorFilter(id.getQueryMap())
//            )
            DefaultNutsIdMultiFilter filter = new DefaultNutsIdMultiFilter(id.getQueryMap(), null, CoreVersionUtils.createNutsVersionFilter(versionString), null, this, session);
            Iterator<NutsId> allVersions = findVersions(id, filter, session);
            NutsId a = null;
            while (allVersions.hasNext()) {
                NutsId next = allVersions.next();
                if (a == null || next.getVersion().compareTo(a.getVersion()) > 0) {
                    a = next;
                }
            }
            if (a == null) {
                throw new NutsNotFoundException(id);
            }
            return a;
        }
    }

}
