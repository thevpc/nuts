/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.bridges.maven;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.repos.AbstractNutsRepository;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CorePlatformUtils;
import net.vpc.app.nuts.extensions.util.CoreSecurityUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.strings.StringUtils;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by vpc on 2/20/17.
 */
public abstract class AbstractMavenRepository extends AbstractNutsRepository {

    private static final Logger log = Logger.getLogger(AbstractMavenRepository.class.getName());

    public AbstractMavenRepository(NutsRepositoryConfig config, NutsWorkspace workspace, NutsRepository parentRepository, String root, int slowness) {
        super(config, workspace, parentRepository, root, slowness);
        extensions.put("src", "-src.zip");
        extensions.put("pom", ".pom");
    }

//    protected String getPathName(NutsId id, String extension){
//        String artifactId = id.getName();
//        String classifier = id.getClassifier();
//        String version = id.getVersion().getValue();
//        String classifierNamePart = ".pom".equals(extension)?"":
//                (StringUtils.isEmpty(classifier) ? "" : ("-") + classifier);
//        if (StringUtils.isEmpty(extension)) {
//            extension = ".jar";
//        }
//        return artifactId
//                + classifierNamePart
//                + "-" + version
//                + extension;
//    }

    protected abstract String getPath(NutsId id, String extension);

    protected InputStream getStream(NutsId id, String extension, String face, NutsSession session) {
        String url = getPath(id, extension);
        return openStream(id, url, id.setFace(face), session);
    }

    protected String getStreamAsString(NutsId id, String extension, String face, NutsSession session) {
        String url = getPath(id, extension);
        return IOUtils.loadString(openStream(id, url, id.setFace(face), session), true);
    }

    protected void checkSHA1Hash(NutsId id, String extension, String face, InputStream stream, NutsSession session) throws IOException {
        try {
            String rhash = getStreamSHA1(id, extension, face, session);
            String lhash = CoreSecurityUtils.evalSHA1(stream, true);
            if (!rhash.equals(lhash)) {
                throw new IOException("Invalid file hash " + id);
            }
        } finally {
            stream.close();
        }
    }

    protected String getStreamSHA1(NutsId id, String extension, String face, NutsSession session) {
        String hash = getStreamAsString(id, extension + ".sha1", face, session).toUpperCase();
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
            NutsDescriptor nutsDescriptor = null;//parsePomXml(getStream(id, ".pom"), session);
            byte[] bytes = null;
            try {
                stream = getStream(id, ".pom", CoreNutsUtils.FACE_PACKAGE, session);
                bytes = IOUtils.loadByteArray(stream, true);
                nutsDescriptor = MavenUtils.parsePomXml(new ByteArrayInputStream(bytes), getWorkspace(), session, getPath(id, ".pom"));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            checkSHA1Hash(id, ".pom", CoreNutsUtils.FACE_DESC_HASH, new ByteArrayInputStream(bytes), session);
            File jar = new File(getPath(id, resolveExtension(nutsDescriptor)));
            nutsDescriptor = annotateExecDesc(nutsDescriptor,jar);
            return nutsDescriptor;
        } catch (IOException ex) {
            throw new NutsNotFoundException(id, null, ex);
        } catch (NutsIOException ex) {
            throw new NutsNotFoundException(id, null, ex);
        }
    }

    @Override
    protected String copyToImpl(NutsId id, String localPath, NutsSession session) {
        try {
            NutsDescriptor d = getWorkspace().fetchDescriptor(id, true, session);
            String ext = resolveExtension(d);
            if (new File(localPath).isDirectory()) {
                localPath = new File(localPath, CoreNutsUtils.getNutsFileName(id, ext)).getPath();
            }
            IOUtils.copy(getStream(id, ext, CoreNutsUtils.FACE_PACKAGE, session), new File(localPath), true, true);
            checkSHA1Hash(id, ext, CoreNutsUtils.FACE_PACKAGE_HASH, new FileInputStream(localPath), session);
            return localPath;
        } catch (NutsIOException ex) {
            throw new NutsNotFoundException(id, null, ex);
        } catch (IOException ex) {
            throw new NutsNotFoundException(id, null, ex);
        }
    }



    @Override
    public String copyDescriptorToImpl(NutsId id, String localPath, NutsSession session) {
        NutsDescriptor nutsDescriptor = fetchDescriptor(id, session);
        if (new File(localPath).isDirectory()) {
            localPath = new File(localPath, CoreNutsUtils.getNutsFileName(id, "pom")).getPath();
        }
        nutsDescriptor.write(new File(localPath));
        return localPath;
    }

    @Override
    public String fetchHashImpl(NutsId id, NutsSession session) {
        return getStreamSHA1(id, ".jar", CoreNutsUtils.FACE_PACKAGE_HASH, null);
    }

    @Override
    public String fetchDescriptorHashImpl(NutsId id, NutsSession session) {
        InputStream stream = null;
        NutsDescriptor nutsDescriptor = null;
        try {
            try {
                stream = getStream(id, ".pom", CoreNutsUtils.FACE_DESC, session);
                nutsDescriptor = MavenUtils.parsePomXml(stream,getPath(id, ".pom"));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (NutsIOException ex) {
            throw new NutsNotFoundException(id, null, ex);
        } catch (IOException ex) {
            throw new NutsNotFoundException(id, null, ex);
        }
        return nutsDescriptor.getSHA1();
    }

//    @Override
//    public NutsId resolveIdImpl(NutsId id, NutsSession session) {
//        try {
//            NutsSession transitiveSession = session.copy().setTransitive(true);
//            NutsDescriptor d = fetchDescriptor(id, transitiveSession);
//            if (d != null) {
//                return id;
//            }
//        } catch (Exception ex) {
//            //not found
//        }
//        throw new NutsNotFoundException(id);
//    }

    public NutsDescriptor annotateExecDesc(NutsDescriptor nutsDescriptor,File jar) {
        boolean executable=nutsDescriptor.isExecutable();
        boolean nutsApp= nutsDescriptor.isNutsApplication();
        if(jar.getName().toLowerCase().endsWith(".jar") && resolveExtension(nutsDescriptor).equals(".jar") && jar.isFile()){
            File f = new File(getPath(nutsDescriptor.getId(), ".desc-annotation"));
            Map<String,String> map = null;
            try{
                if(f.isFile()) {
                    map = getWorkspace().getIOManager().readJson(f, Map.class);
                }
            }catch (Exception ex){
                //
            }
            if(map!=null){
                executable="true".equals(map.get("executable"));
                nutsApp="true".equals(map.get("nutsApplication"));
            }else {
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
                        getWorkspace().getIOManager().writeJson(map, f,true);
                    }catch (Exception ex){
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

    public static String resolveExtension(NutsDescriptor d) {
        String ee = d.getExt();
        if (!StringUtils.isEmpty(ee)) {
            if ("bundle".equals(ee)) {
                return ".jar";
            }
            if ("nuts-extension".equals(ee)) {
                return ".jar";
            }
            return "." + ee;
        }
        String ext = "";
        if (StringUtils.isEmpty(d.getPackaging())) {
            ext = ".jar";
        } else if (d.getPackaging().equals("maven-archetype")) {
            ext = ".jar";
        } else if (d.getPackaging().equals("bundle")) {
            ext = ".jar";
        } else {
            ext = ("." + d.getPackaging());
        }
        return "."+ext;
    }

}
