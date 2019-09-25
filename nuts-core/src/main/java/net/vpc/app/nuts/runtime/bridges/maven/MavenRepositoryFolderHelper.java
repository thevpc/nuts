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
package net.vpc.app.nuts.runtime.bridges.maven;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.CoreNutsConstants;
import net.vpc.app.nuts.runtime.DefaultNutsVersion;
import net.vpc.app.nuts.core.repos.NutsRepositoryExt;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.util.io.FolderNutIdIterator;
import net.vpc.app.nuts.runtime.bridges.maven.mvnutil.MavenMetadata;
import net.vpc.app.nuts.runtime.bridges.maven.mvnutil.MavenMetadataParser;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;

/**
 *
 * @author vpc
 */
public class MavenRepositoryFolderHelper {
    private NutsLogger LOG;
    private NutsRepository repo;
    private NutsWorkspace ws;
    private Path rootPath;

    public MavenRepositoryFolderHelper(NutsRepository repo, NutsWorkspace ws, Path rootPath) {
        this.repo = repo;
        this.ws = ws != null ? ws : repo == null ? null : repo.getWorkspace();
        if(repo==null && ws==null){
            throw new NutsIllegalArgumentException(null,"Both Ws and repo are null");
        }
        this.rootPath = rootPath;
        LOG=this.ws.log().of(MavenRepositoryFolderHelper.class);
    }

    public Path getIdLocalFile(NutsId id) {
        return getStoreLocation().resolve(NutsRepositoryExt.of(repo).getIdBasedir(id))
                .resolve(ws.config().getDefaultIdFilename(id));
    }

    public NutsContent fetchContentImpl(NutsId id, Path localPath, NutsRepositorySession session) {
        Path cacheContent = getIdLocalFile(id);
        if (cacheContent != null && Files.exists(cacheContent)) {
            return new NutsDefaultContent(cacheContent, true, false);
        }
        return null;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    protected String getIdFilename(NutsId id) {
        if (repo == null) {
            return ws.config().getDefaultIdFilename(id);
        }
        return NutsRepositoryExt.of(repo).getIdFilename(id);
    }

    public Path getLocalGroupAndArtifactFile(NutsId id) {
        NutsWorkspaceUtils.of(getWorkspace()).checkSimpleNameNutsId(id);
        Path groupFolder = getStoreLocation().resolve(id.getGroupId().replace('.', File.separatorChar));
        return groupFolder.resolve(id.getArtifactId());
    }

    public Iterator<NutsId> searchVersions(NutsId id, final NutsIdFilter filter, boolean deep, NutsRepositorySession session) {
        if (id.getVersion().isSingleValue()) {
            NutsId id1 = id.builder().setFaceDescriptor().build();
            Path localFile = getIdLocalFile(id1);
            if (localFile != null && Files.isRegularFile(localFile)) {
                return Collections.singletonList(id.builder().setNamespace(repo == null ? null : repo.config().getName()).build()).iterator();
            }
            return null;
        }
        return searchInFolder(getLocalGroupAndArtifactFile(id), filter,
                deep ? Integer.MAX_VALUE : 1,
                session);
    }

    public Iterator<NutsId> searchInFolder(Path folder, final NutsIdFilter filter, int maxDepth, NutsRepositorySession session) {
        folder = rootPath.resolve(folder);
        if (folder == null || !Files.exists(folder) || !Files.isDirectory(folder)) {
            //            return IteratorUtils.emptyIterator();
            return null;
        }
        return new FolderNutIdIterator(getWorkspace(), repo == null ? null : repo.config().getName(), folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsRepositorySession session) {
                throw new IllegalArgumentException("Unsupported");
            }

            @Override
            public boolean isDescFile(Path pathname) {
                return pathname.getFileName().toString().equals("pom.xml");
            }

            @Override
            public NutsDescriptor parseDescriptor(Path pathname, NutsRepositorySession session) throws IOException {
                return MavenUtils.of(session.getWorkspace()).parsePomXml(pathname, session);
            }
        }, maxDepth);
    }

    public Path getStoreLocation() {
        return rootPath;
    }

    public NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        NutsId bestId = null;
        File file = getLocalGroupAndArtifactFile(id).toFile();
        if (file.exists()) {
            File[] versionFolders = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            if (versionFolders != null) {
                for (File versionFolder : versionFolders) {
                    NutsId id2 = id.builder().setVersion(versionFolder.getName()).build();
                    if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                        bestId = id2;
                    }
                }
            }
        }
        return bestId;
    }

    public void reindexFolder() {
        reindexFolder(getStoreLocation(), true);
    }

    private void reindexFolder(Path path, boolean applyRawNavigation) {
        try {
            Files.walkFileTree(path, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    File folder = dir.toFile();
                    File[] children = folder.listFiles();
                    TreeSet<String> files = new TreeSet<>();
                    TreeSet<String> folders = new TreeSet<>();
                    String subPath = dir.toString().equals(path.toString()) ? ""
                            : dir.toString().substring(path.toString().length() + 1).replace('\\', '/');
                    int iii = subPath.lastIndexOf('/');
                    String artifactId = null;
                    String groupId = null;
                    if (iii > 0) {
                        artifactId = subPath.substring(iii + 1);
                        groupId = subPath.substring(0, iii).replace('/', '.');
                    } else {
                        artifactId = subPath;
                        groupId = "";
                    }
                    if (children != null && children.length > 0) {
                        List<File> versions = new ArrayList<>();
                        for (File c : children) {
                            File[] pomFiles = c.listFiles(x -> x.getName().endsWith(".pom"));
                            if (pomFiles != null && pomFiles.length > 0) {
                                //this is package folder!
                                versions.add(c);
                            }
                        }
                        if (versions.size() > 0) {
                            Path metadataxml = dir.resolve("maven-metadata.xml");
                            MavenMetadata old = null;
                            try {
                                if (Files.exists(metadataxml)) {
                                    old = MavenMetadataParser.of(ws).parseMavenMetaData(metadataxml);
                                }
                            } catch (Exception ex) {
                                LOG.log(Level.FINE, "Failed to parse metadata xml for " + metadataxml,ex);
                                //ignore any error!
                            }
                            MavenMetadata m = new MavenMetadata();
                            m.setArtifactId(artifactId);
//                            m.setArtifactId(artifactId);
                            m.setGroupId(groupId);
                            m.setLastUpdated(old == null ? null : old.getLastUpdated());
                            m.setRelease(old == null ? null : old.getRelease());
                            m.setLatest(old == null ? null : old.getLatest());
                            LinkedHashSet<String> sversions = new LinkedHashSet<>();
                            if (old != null) {
                                sversions.addAll(old.getVersions());
                            }
                            for (File version : versions) {
                                sversions.add(version.getName());
                            }
                            ArrayList<String> ll = new ArrayList<>(sversions);
                            ll.sort(new Comparator<String>() {
                                @Override
                                public int compare(String o1, String o2) {
                                    //reverse order
                                    return -DefaultNutsVersion.compareVersions(o1, o2);
                                }
                            });
                            m.setVersions(ll);
                            if (m.getLastUpdated() == null) {
                                m.setLastUpdated(new Date());
                            }
//                            System.out.println(MavenMetadataParser.toXmlString(m));
                            MavenMetadataParser.of(ws).writeMavenMetaData(m, metadataxml);
                            String md5 = CoreIOUtils.evalMD5Hex(metadataxml).toLowerCase();
                            Files.write(metadataxml.resolveSibling("maven-metadata.xml.md5"), md5.getBytes());
                            String sha1 = CoreIOUtils.evalSHA1Hex(metadataxml).toLowerCase();
                            Files.write(metadataxml.resolveSibling("maven-metadata.xml.sha1"), sha1.getBytes());
                        }
                        if (applyRawNavigation) {
                            for (File child : children) {
                                //&& !DefaultNutsVersion.isBlank(child.getName())
                                if (!child.getName().startsWith(".")) {
                                    if (child.isDirectory()) {
                                        folders.add(child.getName());
                                    } else if (child.isFile()) {
                                        files.add(child.getName());
                                    }
                                }
                            }
                        }
                    }
                    if (applyRawNavigation) {
//                        try (PrintStream p = new PrintStream(new File(folder, CoreNutsConstants.Files.DOT_FILES))) {
//                            for (String file : files) {
//                                p.println(file);
//                            }
//                        } catch (FileNotFoundException e) {
//                            throw new UncheckedIOException(e);
//                        }
                        try (PrintStream p = new PrintStream(new File(folder, CoreNutsConstants.Files.DOT_FILES))) {
                            p.println("#version="+ws.config().getApiVersion());
                            for (String file : folders) {
                                p.println(file+"/");
                            }
                            for (String file : files) {
                                p.println(file);
                            }
                        } catch (FileNotFoundException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

    }
}
