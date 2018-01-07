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
package net.vpc.app.nuts.boot.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.boot.FolderNutIdIterator;
import net.vpc.app.nuts.util.*;

import java.io.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/5/17.
 */
public class MavenFolderRepository extends AbstractMavenRepository {

    public static final Logger log = Logger.getLogger(MavenFolderRepository.class.getName());

    public MavenFolderRepository(String repositoryId, String repositoryLocation, NutsWorkspace workspace, File root) {
        super(new NutsRepositoryConfig(repositoryId, repositoryLocation, "maven"), workspace, root, SPEED_FAST);
    }

    @Override
    protected InputStream openStream(String path) throws IOException {
        return new FileInputStream(path);
    }

    @Override
    protected String getStreamSHA1(NutsId id, String extension) throws IOException {
        return SecurityUtils.evalSHA1(getStream(id, extension));
    }

    @Override
    protected void checkSHA1Hash(NutsId id, String extension, InputStream stream) throws IOException {
        stream.close();
    }

    @Override
    protected String getPath(NutsId id, String extension) throws IOException {
        String groupId = id.getGroup();
        String artifactId = id.getName();
        String version = id.getVersion().getValue();
        File locationFolder = getStoreRoot();

        return new File(locationFolder, groupId.replaceAll("\\.", File.separator) + File.separator + artifactId + File.separator + version + File.separator + artifactId + "-" + version + extension)
                .getPath();
    }

    private File getStoreRoot() {
        return IOUtils.resolvePath(getConfig().getLocation(), getRoot(),getWorkspace().getWorkspaceRootLocation());
    }

    @Override
    protected NutsFile fetchImpl(NutsId id, NutsSession session) throws IOException {
        NutsFile nutsFile = getNutsFile(id, session);
        if (session.getFetchMode() != FetchMode.REMOTE) {
            if (nutsFile != null && nutsFile.getFile()!=null && nutsFile.getFile().exists()) {
                NutsDescriptor desc = nutsFile.getDescriptor();
                if (desc!=null) {
                    NutsId id2 = getWorkspace().fetchEffectiveId(desc, session);
                    id2 = id2.setFace(id.getFace());
                    return new NutsFile(id2, desc, nutsFile.getFile(), true, false);
                }
            }
        }
        throw new NutsNotFoundException(id);
    }

    //    protected NutsDescriptor getLocalNutDescriptor(NutsId id, boolean nullIfInvalidName, NutsSession session) throws IOException {
//        File localNutDescriptorFile = getLocalNutDescriptorFile(id, nullIfInvalidName);
//        if (localNutDescriptorFile == null || !localNutDescriptorFile.exists()) {
//            return null;
//        }
//        NutsDescriptor nutsDescriptor = parsePomXml(new FileInputStream(localNutDescriptorFile), session);
//        File localFile = getLocalGroupAndArtifactAndVersionFile(id, false, session);
//        if (localFile != null && localFile.isFile()) {
//            File localDescFile = getLocalGroupAndArtifactAndVersionFile(id, true, session);
//            if (localDescFile != null && localDescFile.exists()) {
//                nutsDescriptor = nutsDescriptor.setExecutable(isExecutableJar(localFile));
//            }
//        }
//        return nutsDescriptor;
//    }

//    protected File getLocalNutDescriptorFile(NutsId id, boolean nullIfInvalidName) {
//        if (StringUtils.isEmpty(id.getGroup())) {
//            if (nullIfInvalidName) {
//                return null;
//            }
//            throw new NutsIdInvalidFormatException("Missing group for " + id);
//        }
//        File groupFolder = new File(getStoreRoot(), id.getGroup().replaceAll("\\.", File.separator));
//        if (StringUtils.isEmpty(id.getName())) {
//            if (nullIfInvalidName) {
//                return null;
//            }
//            throw new NutsIdInvalidFormatException("Missing name for " + id.toString());
//        }
//        File artifactFolder = new File(groupFolder, id.getName());
//        if (id.getVersion().isEmpty()) {
//            if (nullIfInvalidName) {
//                return null;
//            }
//            throw new NutsIdInvalidFormatException("Missing version for " + id.toString());
//        }
//        File versionFolder = new File(artifactFolder, id.getVersion().getValue());
//        //check default face
//
//        String name = id.getName() + "-" + id.getVersion().getValue();
//        String ext = ".pom";
//        return new File(versionFolder, name + ext);
//    }

    protected NutsFile getNutsFile(NutsId id, NutsSession session) throws IOException {
        if (StringUtils.isEmpty(id.getGroup())) {
            return null;
        }
        if (StringUtils.isEmpty(id.getName())) {
            return null;
        }
        if (id.getVersion().isEmpty()) {
            return null;
        }
        File groupFolder = new File(getStoreRoot(), id.getGroup().replaceAll("\\.", File.separator));
        File artifactFolder = new File(groupFolder, id.getName());
        if (id.getVersion().isEmpty()) {
            return null;
        }
        File versionFolder = new File(artifactFolder, id.getVersion().getValue());

        String name = id.getName() + "-" + id.getVersion().getValue();
        String ext = ".pom";
        File descFile= new File(versionFolder, name + ext);


        if (descFile.isFile()) {
            NutsDescriptor nutsDescriptor = MavenUtils.parsePomXml(new FileInputStream(descFile), getWorkspace(),session);

            File localFile = nutsDescriptor==null? new File(versionFolder,
                    id.getName() + "-" + id.getVersion().getValue()+".jar"
                    ):new File(versionFolder, getQueryFilename(id, nutsDescriptor));
            if (localFile.isFile()) {
                if(nutsDescriptor!=null){
                    nutsDescriptor=nutsDescriptor.setExecutable(PlatformUtils.isExecutableJar(localFile));
                }
                return new NutsFile(id,nutsDescriptor,localFile,true,false);
            }
        }
        return null;
    }

//    protected File getLocalGroupAndArtifactAndVersionFile(NutsId id, boolean desc, NutsSession session) throws IOException {
//        if (StringUtils.isEmpty(id.getGroup())) {
//            return null;
//        }
//        if (StringUtils.isEmpty(id.getName())) {
//            return null;
//        }
//        if (id.getVersion().isEmpty()) {
//            return null;
//        }
//        File groupFolder = new File(getStoreRoot(), id.getGroup().replaceAll("\\.", File.separator));
//        File artifactFolder = new File(groupFolder, id.getName());
//        if (id.getVersion().isEmpty()) {
//            if (id.getQueryMap().isEmpty()) {
//                return artifactFolder;
//            } else {
//                throw new IllegalArgumentException("Invalid id " + id);
//            }
//        }
//        File versionFolder = new File(artifactFolder, id.getVersion().getValue());
//        Map<String, String> query = id.getQueryMap();
//        NutsId fid = id;
//        if (desc) {
//            query.put(NutsConstants.QUERY_FILE, "pom");
//            fid = fid.setQuery(query);
//            if(true){
//                // TODO
//                throw new IllegalArgumentException("Checik this later");
//            }
//            //return new File(versionFolder, getQueryFilename(fid, d));
//        }
//        NutsDescriptor d = getLocalNutDescriptor(id, true, session);
//        if (d == null) {
//            return null;
//            //throw new IllegalArgumentException("Invalid nuts " + id);
//        }
//
//        return new File(versionFolder, getQueryFilename(fid, d));
//    }

    protected File getLocalGroupAndArtifactFile(NutsId id) throws IOException {
        if (StringUtils.isEmpty(id.getGroup())) {
            return null;
        }
        if (StringUtils.isEmpty(id.getName())) {
            return null;
        }
        File groupFolder = new File(getStoreRoot(), id.getGroup().replaceAll("\\.", File.separator));
        return new File(groupFolder, id.getName());
    }

    @Override
    protected Iterator<NutsId> findVersionsImpl(NutsId id, NutsDescriptorFilter versionFilter, NutsSession session) throws IOException {
        Iterator<NutsId> namedNutIdIterator = null;
        StringBuilder errors = new StringBuilder();
        if (session.getFetchMode() != FetchMode.REMOTE) {
            try {
                namedNutIdIterator = findInFolder(getLocalGroupAndArtifactFile(id), new NutsDescriptorFilterAndEnvNutsDescriptorFilter(versionFilter, id.getQueryMap()), session);
            } catch (Exception ex) {
                errors.append(ex).append(" \n");
            }
        }
        if (namedNutIdIterator == null) {
            return Collections.emptyIterator();
        }
        return namedNutIdIterator;
    }

    protected Iterator<NutsId> findInFolder(File folder, final NutsDescriptorFilter filter, NutsSession transitive) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return Collections.emptyIterator();
        }
        return new FolderNutIdIterator(getWorkspace(), getRepositoryId(), folder, filter, transitive, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsSession session) throws IOException {
                MavenFolderRepository.this.undeploy(id, session);
            }

            @Override
            public boolean isDescFile(File pathname) {
                return pathname.getName().endsWith(".pom");
            }

            @Override
            public NutsDescriptor parseDescriptor(File pathname, NutsSession session) throws IOException {
//                System.out.println("parse "+pathname);
                NutsDescriptor nutsDescriptor = MavenUtils.parsePomXml(new FileInputStream(pathname), getWorkspace(),session);
                if(pathname.getName().endsWith(".pom")){
                    File loc=new File(pathname.getPath().substring(0,pathname.getPath().length()-4)+".jar");
                    nutsDescriptor=nutsDescriptor.setExecutable(PlatformUtils.isExecutableJar(loc));
                }
                return nutsDescriptor;
            }
        });
    }

    @Override
    protected Iterator<NutsId> findImpl(final NutsDescriptorFilter filter, NutsSession session) throws IOException {
        if (session.getFetchMode() != FetchMode.REMOTE) {
            File locationFolder = getStoreRoot();
            return findInFolder(locationFolder, filter, session);
        }
        return Collections.emptyIterator();
    }

    @Override
    protected NutsDescriptor fetchDescriptorImpl(NutsId id, NutsSession session) throws IOException {
        InputStream stream = null;
        try {
            NutsDescriptor nutsDescriptor = null;//parsePomXml(getStream(id, ".pom"), session);
            byte[] bytes = null;
            try {
                stream = getStream(id, ".pom");
                bytes = IOUtils.readStreamAsBytes(stream, true);
                nutsDescriptor = MavenUtils.parsePomXml(new ByteArrayInputStream(bytes), getWorkspace(),session);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            checkSHA1Hash(id, ".pom", new ByteArrayInputStream(bytes));
            String ext = resolveExtension(nutsDescriptor);
            File jar=new File(getPath(id, ext));
//            if(jar.isFile()) {
                nutsDescriptor = nutsDescriptor.setExecutable(PlatformUtils.isExecutableJar(jar));
//            }
            return nutsDescriptor;
        } catch (IOException ex) {
            throw new NutsNotFoundException(id.toString(), null, ex);
        }
    }

}
