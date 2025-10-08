/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.maven.util;

import java.io.*;
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

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NFetchMode;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NElementDescribables;


import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.repository.NIdPathIterator;
import net.thevpc.nuts.runtime.standalone.repository.NIdPathIteratorBase;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NDigestUtils;

import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.text.NMsg;

/**
 *
 * @author thevpc
 */
public class MavenRepositoryFolderHelper {

    private NRepository repo;
    private NPath rootPath;

    public MavenRepositoryFolderHelper(NRepository repo, NPath rootPath) {
        this.repo = repo;
        this.rootPath = rootPath;
    }

    protected NLog _LOG() {
        return NLog.of(MavenRepositoryFolderHelper.class);
    }

    public NPath getIdLocalFile(NId id) {
        return getStoreLocation().resolve(NRepositoryExt.of(repo).getIdBasedir(id))
                .resolve(NWorkspace.of().getDefaultIdFilename(id));
    }

    public NPath fetchContentImpl(NId id, Path localPath) {
        NPath cacheContent = getIdLocalFile(id);
        if (cacheContent != null && cacheContent.exists()) {
            return cacheContent.setUserCache(true).setUserTemporary(false);
        }
        return null;
    }

    protected String getIdFilename(NId id) {
        if (repo == null) {
            return NWorkspace.of().getDefaultIdFilename(id);
        }
        return NRepositoryExt.of(repo).getIdFilename(id);
    }

    public NPath getLocalGroupAndArtifactFile(NId id) {
        CoreNIdUtils.checkShortId(id);
        return getStoreLocation().resolve(id.getShortId().getMavenFolder());
    }

    public Iterator<NId> searchVersions(NId id, final NDefinitionFilter filter, boolean deep) {
        String singleVersion = id.getVersion().asSingleValue().orNull();
        if (singleVersion != null) {
            NId id1 = id.builder().setVersion(singleVersion).setFaceDescriptor().build();
            NPath localFile = getIdLocalFile(id1);
            if (localFile != null && localFile.isRegularFile()) {
                return Collections.singletonList(id.builder().setRepository(repo == null ? null : repo.getName()).build()).iterator();
            }
            return null;
        }
        return searchInFolder(getLocalGroupAndArtifactFile(id), filter,
                deep ? Integer.MAX_VALUE : 1
        );
    }

    public Iterator<NId> searchInFolder(NPath folder, final NDefinitionFilter filter, int maxDepth) {
        return new NIdPathIterator(repo, rootPath.normalize(), folder, filter, new NIdPathIteratorBase() {
            @Override
            public NWorkspace getWorkspace() {
                return repo.getWorkspace();
            }

            @Override
            public void undeploy(NId id) {
                throw new NIllegalArgumentException(NMsg.ofPlain("unsupported undeploy"));
            }

            @Override
            public boolean isDescFile(NPath pathname) {
                return pathname.getName().equals("pom.xml");
            }

            @Override
            public NDescriptor parseDescriptor(NPath pathname, InputStream in, NFetchMode fetchMode, NRepository repository, NPath rootURL) {
                return MavenUtils.of().parsePomXmlAndResolveParents(pathname, NFetchMode.LOCAL, repo);
            }
        }, maxDepth, "core", null, true);
    }

    public NPath getStoreLocation() {
        return rootPath;
    }

    public NId searchLatestVersion(NId id, NDefinitionFilter filter) {
        NId bestId = null;
        NPath file = getLocalGroupAndArtifactFile(id);
        if (file.exists()) {
            NPath[] versionFolders = file.stream().filter(NPath::isDirectory).redescribe(NElementDescribables.ofDesc("isDirectory"))
                    .toArray(NPath[]::new);
            for (NPath versionFolder : versionFolders) {
                NId id2 = id.builder().setVersion(versionFolder.getName()).build();
                if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                    bestId = id2;
                }
            }
        }
        return bestId;
    }

    public void reindexFolder() {
        reindexFolder(getStoreLocation(), true);
    }

    private void reindexFolder(NPath path, boolean applyRawNavigation) {
        try {
            Files.walkFileTree(path.toPath().get(), new FileVisitor<Path>() {
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
                                    old = new MavenMetadataParser().parseMavenMetaData(metadataxml);
                                }
                            } catch (Exception ex) {
                                _LOG().log(NMsg.ofJ("failed to parse metadata xml for {0} : {1}", metadataxml, ex).asFinestFail(ex));
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
                                    //reverse order, enforce maven comparator
                                    return -NVersionComparator.ofMaven().compare(NVersion.of(o1), NVersion.of(o2));
                                }
                            });
                            m.setVersions(ll);
                            if (m.getLastUpdated() == null) {
                                m.setLastUpdated(new Date());
                            }
//                            println(MavenMetadataParser.toXmlString(m));
                            new MavenMetadataParser().writeMavenMetaData(m, metadataxml);
                            String md5 = NDigestUtils.evalMD5Hex(metadataxml).toLowerCase();
                            Files.write(metadataxml.resolveSibling("maven-metadata.xml.md5"), md5.getBytes());
                            String sha1 = NDigestUtils.evalSHA1Hex(NPath.of(metadataxml)).toLowerCase();
                            Files.write(metadataxml.resolveSibling("maven-metadata.xml.sha1"), sha1.getBytes());
                        }
                        if (applyRawNavigation) {
                            for (File child : children) {
                                //&& !DefaultNVersion.isBlank(child.getName())
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
                        try (PrintStream p = new PrintStream(new File(folder, CoreNConstants.Files.DOT_FILES))) {
                            p.println("#version=" + NWorkspace.of().getApiVersion());
                            for (String file : folders) {
                                p.println(file + "/");
                            }
                            for (String file : files) {
                                p.println(file);
                            }
                        } catch (FileNotFoundException e) {
                            throw new NIOException(e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new NIOException(ex);
        }

    }
}
