/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
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

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.bundles.mvn.MavenMetadataParser;
import net.thevpc.nuts.runtime.core.CoreNutsConstants;
import net.thevpc.nuts.runtime.core.model.DefaultNutsVersion;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.bundles.io.FolderNutIdIterator;
import net.thevpc.nuts.runtime.bundles.mvn.MavenMetadata;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 *
 * @author thevpc
 */
public class MavenRepositoryFolderHelper {

    private NutsLogger LOG;
    private NutsRepository repo;
    private NutsWorkspace ws;
    private Path rootPath;

    public MavenRepositoryFolderHelper(NutsRepository repo, NutsSession session, Path rootPath) {
        this.repo = repo;
        this.ws = session != null ? session.getWorkspace() : repo == null ? null : repo.getWorkspace();
        if (repo == null && session == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("both workspace and repo are null"));
        }
        this.rootPath = rootPath;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = session.log().of(MavenRepositoryFolderHelper.class);
        }
        return LOG;
    }

    public Path getIdLocalFile(NutsId id, NutsSession session) {
        return getStoreLocation().resolve(NutsRepositoryExt.of(repo).getIdBasedir(id, session))
                .resolve(session.locations().getDefaultIdFilename(id));
    }

    public NutsContent fetchContentImpl(NutsId id, Path localPath, NutsSession session) {
        Path cacheContent = getIdLocalFile(id, session);
        if (cacheContent != null && Files.exists(cacheContent)) {
            return new NutsDefaultContent(
                    session.io().path(cacheContent.toString()), true, false);
        }
        return null;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    protected String getIdFilename(NutsId id, NutsSession session) {
        if (repo == null) {
            return session.locations().getDefaultIdFilename(id);
        }
        return NutsRepositoryExt.of(repo).getIdFilename(id, session);
    }

    public Path getLocalGroupAndArtifactFile(NutsId id, NutsSession session) {
        NutsWorkspaceUtils.of(session).checkShortId(id);
        Path groupFolder = getStoreLocation().resolve(id.getGroupId().replace('.', File.separatorChar));
        return groupFolder.resolve(id.getArtifactId());
    }

    public Iterator<NutsId> searchVersions(NutsId id, final NutsIdFilter filter, boolean deep, NutsSession session) {
        if (id.getVersion().isSingleValue()) {
            NutsId id1 = id.builder().setFaceDescriptor().build();
            Path localFile = getIdLocalFile(id1, session);
            if (localFile != null && Files.isRegularFile(localFile)) {
                return Collections.singletonList(id.builder().setRepository(repo == null ? null : repo.getName()).build()).iterator();
            }
            return null;
        }
        return searchInFolder(getLocalGroupAndArtifactFile(id, session), filter,
                deep ? Integer.MAX_VALUE : 1,
                session);
    }

    public Iterator<NutsId> searchInFolder(Path folder, final NutsIdFilter filter, int maxDepth, NutsSession session) {
        folder = rootPath.resolve(folder);
        if (folder == null || !Files.exists(folder) || !Files.isDirectory(folder)) {
            //            return IteratorUtils.emptyIterator();
            return null;
        }
        return new FolderNutIdIterator(repo == null ? null : repo.getName(), folder, rootPath, filter, session, new FolderNutIdIterator.AbstractFolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsSession session) {
                throw new NutsIllegalArgumentException(session,NutsMessage.cstyle("unsupported undeploy"));
            }

            @Override
            public boolean isDescFile(Path pathname) {
                return pathname.getFileName().toString().equals("pom.xml");
            }

            @Override
            public NutsDescriptor parseDescriptor(Path pathname, NutsSession session) throws IOException {
                return MavenUtils.of(session).parsePomXmlAndResolveParents(pathname, NutsFetchMode.LOCAL, repo);
            }
        }, maxDepth);
    }

    public Path getStoreLocation() {
        return rootPath;
    }

    public NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsSession session) {
        NutsId bestId = null;
        File file = getLocalGroupAndArtifactFile(id, session).toFile();
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

    public void reindexFolder(NutsSession session) {
        reindexFolder(getStoreLocation(), true, session);
    }

    private void reindexFolder(Path path, boolean applyRawNavigation, NutsSession session) {
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
                                    old = new MavenMetadataParser(session).parseMavenMetaData(metadataxml);
                                }
                            } catch (Exception ex) {
                                _LOGOP(session).level(Level.SEVERE).error(ex)
                                        .log(NutsMessage.jstyle("failed to parse metadata xml for {0} : {1}", metadataxml, ex));
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
//                            println(MavenMetadataParser.toXmlString(m));
                            new MavenMetadataParser(session).writeMavenMetaData(m, metadataxml);
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
//                            throw new NutsIOException(getWorkspace(),e);
//                        }
                        try (PrintStream p = new PrintStream(new File(folder, CoreNutsConstants.Files.DOT_FILES))) {
                            p.println("#version=" + ws.getApiVersion());
                            for (String file : folders) {
                                p.println(file + "/");
                            }
                            for (String file : files) {
                                p.println(file);
                            }
                        } catch (FileNotFoundException e) {
                            throw new NutsIOException(session, e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }

    }
}
