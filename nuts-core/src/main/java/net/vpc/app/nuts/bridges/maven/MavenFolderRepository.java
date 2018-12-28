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
package net.vpc.app.nuts.bridges.maven;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.*;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.CollectionUtils;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/5/17.
 */
public class MavenFolderRepository extends AbstractMavenRepository {

    public static final Logger log = Logger.getLogger(MavenFolderRepository.class.getName());

    public MavenFolderRepository(String repositoryId, String repositoryLocation, NutsWorkspace workspace, NutsRepository parentRepository, String root) {
        super(new NutsRepositoryConfig(repositoryId, repositoryLocation, NutsConstants.REPOSITORY_TYPE_NUTS_MAVEN), workspace, parentRepository,
                CoreIOUtils.resolvePath(repositoryId,
                        root != null ? new File(root) : CoreIOUtils.createFile(
                                workspace.getConfigManager().getWorkspaceLocation(), NutsConstants.FOLDER_NAME_REPOSITORIES),
                        workspace.getConfigManager().getHomeLocation()).getPath(),
                SPEED_FAST);
    }

    @Override
    protected int getSupportLevelCurrent(NutsId id, NutsSession session) {
        switch (session.getFetchMode()) {
            case REMOTE:
                return 0;
        }
        return super.getSupportLevelCurrent(id, session);
    }

    @Override
    protected InputStream openStream(NutsId id, String path, Object source, NutsSession session) {
        try {
            return new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw new NutsIOException(e);
        }
    }

    @Override
    protected String getStreamSHA1(NutsId id, String extension, String face, NutsSession session) {
        return CoreSecurityUtils.evalSHA1(getStream(id, extension, CoreNutsUtils.FACE_PACKAGE_HASH, session), true);
    }

    @Override
    protected void checkSHA1Hash(NutsId id, String extension, String face, InputStream stream, NutsSession session) {
        try {
            stream.close();
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
    }

    @Override
    protected String getPath(NutsId id, String extension) {
        String groupId = id.getGroup();
        String artifactId = id.getName();
        String version = id.getVersion().getValue();
        File locationFolder = getPrivateStoreRoot();

        return new File(locationFolder, groupId.replaceAll("\\.", File.separator) + File.separator + artifactId + File.separator + version + File.separator + artifactId + "-" + version + extension)
                .getPath();
    }

    @Override
    public String getStoreRoot() {
        return null;
    }

    private File getPrivateStoreRoot() {
        return CoreIOUtils.resolvePath(getConfigManager().getLocation(),
                new File(getConfigManager().getLocationFolder()),
                getWorkspace().getConfigManager().getHomeLocation());
    }

    @Override
    protected NutsDefinition fetchImpl(NutsId id, NutsSession session) {
        NutsDefinition nutsDefinition = getNutsFile(id, session);
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            if (nutsDefinition != null && nutsDefinition.getFile() != null && new File(nutsDefinition.getFile()).exists()) {
                NutsDescriptor desc = nutsDefinition.getDescriptor();
                if (desc != null) {
                    NutsId id2 = getWorkspace().resolveEffectiveId(desc, session);
                    id2 = id2.setFace(id.getFace());
                    return new NutsDefinition(id2, desc, nutsDefinition.getFile(), true, false, null,null);
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
//        File groupFolder = new File(getPrivateStoreRoot(), id.getGroup().replaceAll("\\.", File.separator));
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
    protected NutsDefinition getNutsFile(NutsId id, NutsSession session) {
        if (StringUtils.isEmpty(id.getGroup())) {
            return null;
        }
        if (StringUtils.isEmpty(id.getName())) {
            return null;
        }
        if (id.getVersion().isEmpty()) {
            return null;
        }
        File groupFolder = new File(getPrivateStoreRoot(), id.getGroup().replaceAll("\\.", File.separator));
        File artifactFolder = new File(groupFolder, id.getName());
        if (id.getVersion().isEmpty()) {
            return null;
        }
        File versionFolder = new File(artifactFolder, id.getVersion().getValue());

        String name = id.getName() + "-" + id.getVersion().getValue();
        String ext = ".pom";
        File descFile = new File(versionFolder, name + ext);

        if (descFile.isFile()) {
            NutsDescriptor nutsDescriptor = null;
            try {
                nutsDescriptor = MavenUtils.parsePomXml(new FileInputStream(descFile), getWorkspace(), session, descFile.getPath());
            } catch (IOException e) {
                throw new NutsIOException(e);
            }

            String ext2 = nutsDescriptor == null ? null : nutsDescriptor.getExt();
            if (StringUtils.isEmpty(ext)) {
                ext2 = "jar";
            }
            File localFile = nutsDescriptor == null ? new File(versionFolder,
                    id.getName() + "-" + id.getVersion().getValue() + "." + ext2
            ) : new File(versionFolder, getQueryFilename(id, nutsDescriptor));
            if (localFile.isFile()) {
                nutsDescriptor = annotateExecDesc(nutsDescriptor,localFile);
                return new NutsDefinition(id, nutsDescriptor, localFile.getPath(), true, false, null,null);
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
//        File groupFolder = new File(getPrivateStoreRoot(), id.getGroup().replaceAll("\\.", File.separator));
//        File artifactFolder = new File(groupFolder, id.getName());
//        if (id.getVersion().isEmpty()) {
//            if (id.getQueryMap().isEmpty()) {
//                return artifactFolder;
//            } else {
//                throw new NutsIllegalArgumentsException("Invalid id " + id);
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
//                throw new NutsIllegalArgumentsException("Checik this later");
//            }
//            //return new File(versionFolder, getQueryFilename(fid, d));
//        }
//        NutsDescriptor d = getLocalNutDescriptor(id, true, session);
//        if (d == null) {
//            return null;
//            //throw new NutsIllegalArgumentsException("Invalid nuts " + id);
//        }
//
//        return new File(versionFolder, getQueryFilename(fid, d));
//    }
    protected File getLocalGroupAndArtifactFile(NutsId id) {
        if (StringUtils.isEmpty(id.getGroup())) {
            return null;
        }
        if (StringUtils.isEmpty(id.getName())) {
            return null;
        }
        File groupFolder = new File(getPrivateStoreRoot(), id.getGroup().replaceAll("\\.", File.separator));
        return new File(groupFolder, id.getName());
    }

    @Override
    protected List<NutsId> findVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsSession session) {
        List<NutsId> namedNutIdIterator = null;
//        StringBuilder errors = new StringBuilder();
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            if (id.getVersion().isSingleValue()) {
                final NutsDescriptor d = parsePomDescriptor(id, session);
                if (d != null) {
                    return new ArrayList<>(Collections.singletonList(id.setNamespace(getRepositoryId())));
                }
                return Collections.emptyList();
            }
            try {
                namedNutIdIterator = CollectionUtils.toList(findInFolder(getLocalGroupAndArtifactFile(id), idFilter, session));
            } catch (NutsNotFoundException ex) {
//                errors.append(ex).append(" \n");
            }
        }
        if (namedNutIdIterator == null) {
            return Collections.emptyList();
        }
        return namedNutIdIterator;
    }

    protected NutsDescriptor parsePomDescriptor(File pathname, NutsSession session) throws IOException {
        NutsDescriptor nutsDescriptor = MavenUtils.parsePomXml(new FileInputStream(pathname), getWorkspace(), session, pathname.getPath());
        if (nutsDescriptor.getId().getName() == null) {
            //why?
            if(log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Unable to fetch Valid Nuts from " + pathname + " : resolved id was " + nutsDescriptor.getId());
            }
            return null;
        }
        if (pathname.getName().endsWith(".pom")) {
            File loc = new File(pathname.getPath().substring(0, pathname.getPath().length() - 4) + ".jar");
            nutsDescriptor = annotateExecDesc(nutsDescriptor,loc);
        }
        return nutsDescriptor;
    }

    protected NutsDescriptor parsePomDescriptor(NutsId id, NutsSession session) {
        File file = new File(getLocalGroupAndArtifactFile(id), id.getVersion().getValue() + File.separatorChar + id.getName() + '-' + id.getVersion().getValue() + ".pom");
        if (file.exists()) {
            try {
                return parsePomDescriptor(file, session);
            } catch (Exception any) {
                //ignore
            }
        }
        return null;
    }

    protected NutsId findLatestVersion(NutsId id, NutsIdFilter filter, NutsSession session) {
        if (id.getVersion().isEmpty() && filter == null) {
            File file = getLocalGroupAndArtifactFile(id);
            NutsId bestId=null;
            if (file.exists()) {
                File[] versionFolders = file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });
                if (versionFolders != null) {
                    for (File versionFolder : versionFolders) {
                        String fn=id.getName() + "-" + versionFolder.getName()+".pom";
                        if(new File(versionFolder,fn).isFile()) {
                            NutsId id2 = id.setVersion(versionFolder.getName());
                            if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                                bestId = id2;
                            }
                        }
                    }
                }
            }
            return bestId;
        }
        return super.findLatestVersion(id, filter, session);
    }

    protected Iterator<NutsId> findInFolder(File folder, final NutsIdFilter filter, NutsSession session) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return Collections.emptyIterator();
        }
        return new FolderNutIdIterator(getWorkspace(), getRepositoryId(), folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsSession session) {
                MavenFolderRepository.this.undeploy(id, session);
            }

            @Override
            public boolean isDescFile(File pathname) {
                return pathname.getName().endsWith(".pom");
            }

            @Override
            public NutsDescriptor parseDescriptor(File pathname, NutsSession session) throws IOException {
                return parsePomDescriptor(pathname, session);
            }
        });
    }

    @Override
    protected Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsSession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            File locationFolder = getPrivateStoreRoot();
            return findInFolder(locationFolder, filter, session);
        }
        return Collections.emptyIterator();
    }

//    @Override
//    protected NutsDescriptor fetchDescriptorImpl(NutsId id, NutsSession session) {
//        InputStream stream = null;
//        try {
//            NutsDescriptor nutsDescriptor = null;//parsePomXml(getStream(id, ".pom"), session);
//            byte[] bytes = null;
//            try {
//                stream = getStream(id, ".pom");
//                bytes = IOUtils.loadByteArray(stream, true);
//                nutsDescriptor = MavenUtils.parsePomXml(new ByteArrayInputStream(bytes), getWorkspace(), session);
//            } finally {
//                if (stream != null) {
//                    stream.close();
//                }
//            }
//            checkSHA1Hash(id, ".pom", new ByteArrayInputStream(bytes));
//            String ext = resolveExtension(nutsDescriptor);
//            File jar = new File(getPath(id, ext));
//            nutsDescriptor = nutsDescriptor.setExecutable(PlatformUtils.isExecutableJar(jar));
//            return nutsDescriptor;
//        } catch (IOException ex) {
//            throw new NutsNotFoundException(id.toString(), null, ex);
//        }
//    }

}
