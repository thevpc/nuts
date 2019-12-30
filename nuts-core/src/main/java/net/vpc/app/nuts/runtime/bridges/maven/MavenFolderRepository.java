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
package net.vpc.app.nuts.runtime.bridges.maven;

import net.vpc.app.nuts.NutsLogger;
import net.vpc.app.nuts.runtime.util.io.FolderNutIdIterator;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.util.io.InputSource;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.*;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;

import net.vpc.app.nuts.NutsDefaultContent;
import net.vpc.app.nuts.runtime.NutsPatternIdFilter;
import net.vpc.app.nuts.runtime.filters.id.NutsIdFilterAnd;
import net.vpc.app.nuts.main.repos.NutsCachedRepository;
import net.vpc.app.nuts.runtime.util.iter.IteratorUtils;

/**
 * Created by vpc on 1/5/17.
 */
public class MavenFolderRepository extends NutsCachedRepository {

    protected final NutsLogger LOG;
    private final AbstractMavenRepositoryHelper helper = new AbstractMavenRepositoryHelper(this) {
        @Override
        protected String getIdPath(NutsId id) {
            return getLocationAsPath().resolve(CoreIOUtils.syspath(getIdRelativePath(id))).toString();
        }

        @Override
        protected InputSource openStream(NutsId id, String path, Object source, NutsRepositorySession session) {
            return CoreIOUtils.createInputSource(Paths.get(path));
        }

        @Override
        protected String getStreamSHA1(NutsId id, NutsRepositorySession session) {
            return CoreIOUtils.evalSHA1Hex(getStream(id.builder().setFace(NutsConstants.QueryFaces.CONTENT_HASH).build(), session).open(), true);
        }

        @Override
        protected void checkSHA1Hash(NutsId id, InputStream stream, NutsRepositorySession session) {
            try {
                stream.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    };

    public MavenFolderRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options, workspace, parentRepository, SPEED_FASTER, false, NutsConstants.RepoTypes.MAVEN);
        LOG=workspace.log().of(MavenFolderRepository.class);
        if (options.getConfig().getStoreLocationStrategy() != NutsStoreLocationStrategy.STANDALONE) {
            cache.setWriteEnabled(false);
            cache.setReadEnabled(false);
        }
    }

    private Path getLocationAsPath() {
        return Paths.get(config().getLocation(true));
    }

//    @Override
//    public Path getComponentsLocation() {
//        return null;
//    }
    public Path getIdFile(NutsId id) {
        String p = getIdRelativePath(id);
        if (p != null) {
            return getLocationAsPath().resolve(p);
        }
        return null;
    }

    @Override
    public NutsDescriptor fetchDescriptorCore(NutsId id, NutsRepositorySession session) {
        if (session.getFetchMode() == NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(getWorkspace(), id,new NutsFetchModeNotSupportedException(getWorkspace(),this,session.getFetchMode(),id.toString(),null));
        }
        return helper.fetchDescriptorImpl(id, session);
    }

    @Override
    public NutsContent fetchContentCore(NutsId id, NutsDescriptor descriptor, Path localPath, NutsRepositorySession session) {
        if (session.getFetchMode() == NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(getWorkspace(), id,new NutsFetchModeNotSupportedException(getWorkspace(),this,session.getFetchMode(),id.toString(),null));
        }
        Path f = getIdFile(id);
        if(f==null){
            throw new NutsNotFoundException(getWorkspace(), id,new RuntimeException("Invalid id"));
        }
        if(!Files.exists(f)){
            throw new NutsNotFoundException(getWorkspace(), id,new IOException("File not found : "+f));
        }
        if (localPath == null) {
            return new NutsDefaultContent(f, true, false);
        } else {
            getWorkspace().io().copy()
                    .session(session.getSession())
                    .from(f).to(localPath).safe().run();
            return new NutsDefaultContent(localPath, true, false);
        }
    }

    protected Path getLocalGroupAndArtifactFile(NutsId id) {
        NutsWorkspaceUtils.of(getWorkspace()).checkSimpleNameNutsId(id);
        Path groupFolder = getLocationAsPath().resolve(id.getGroupId().replace('.', File.separatorChar));
        return groupFolder.resolve(id.getArtifactId());
    }

    @Override
    public Iterator<NutsId> searchVersionsCore(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {

        Iterator<NutsId> namedNutIdIterator = null;
//        StringBuilder errors = new StringBuilder();
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            if (id.getVersion().isSingleValue()) {
                Path f = getIdFile(id.builder().setFaceDescriptor().build());
                if (f != null && Files.exists(f)) {
                    NutsDescriptor d = null;
                    try {
                        d = MavenUtils.of(session.getWorkspace()).parsePomXml(f, session);
                    } catch (Exception ex) {
                        LOG.with().level(Level.SEVERE).error(ex).log("Failed to parse pom file {0} : {1}", f,ex.toString());
                        //
                    }
                    if (d != null) {
                        return Collections.singletonList(id.builder().setNamespace(config().getName()).build()).iterator();
                    }
                }
//                return IteratorUtils.emptyIterator();
                return null;
            }
            try {
                namedNutIdIterator = findInFolder(getLocalGroupAndArtifactFile(id),
                        new NutsIdFilterAnd(idFilter,
                                new NutsPatternIdFilter(id.getShortNameId())
                        ),
                        Integer.MAX_VALUE, session);
            } catch (NutsNotFoundException ex) {
//                errors.append(ex).append(" \n");
            }
        }
//        if (namedNutIdIterator == null) {
//            return IteratorUtils.emptyIterator();
//        }
        return namedNutIdIterator;

    }

    @Override
    public NutsId searchLatestVersionCore(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        if (id.getVersion().isBlank() && filter == null) {
            Path file = getLocalGroupAndArtifactFile(id);
            NutsId bestId = null;
            if (Files.isDirectory(file)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(file, CoreIOUtils.DIR_FILTER)) {
                    for (Path versionPath : stream) {
                        NutsId id2 = id.builder().setVersion(versionPath.getFileName().toString()).build();
                        String fn = getIdFilename(id2.builder().setFaceDescriptor().build());
                        if (Files.exists(versionPath.resolve(fn))) {
                            if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                                bestId = id2;
                            }
                        }
                    }
                } catch (IOException ex) {
                    //
                }
            }
            return bestId;
        }
        return super.searchLatestVersion(id, filter, session);
    }

    protected Iterator<NutsId> findInFolder(Path folder, final NutsIdFilter filter, int maxDepth, NutsRepositorySession session) {
        if (folder == null || !Files.exists(folder) || !Files.isDirectory(folder)) {
            return null;//IteratorUtils.emptyIterator();
        }
        return new FolderNutIdIterator(getWorkspace(), config().getName(), folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsRepositorySession session) {
                MavenFolderRepository.this.undeploy().setId(id).setSession(session).run();
            }

            @Override
            public boolean isDescFile(Path pathname) {
                return pathname.getFileName().toString().endsWith(".pom");
            }

            @Override
            public NutsDescriptor parseDescriptor(Path pathname, NutsRepositorySession session) throws IOException {
                return MavenUtils.of(session.getWorkspace()).parsePomXml(pathname, session);
            }
        }, maxDepth);
    }

    @Override
    public Iterator<NutsId> searchCore(final NutsIdFilter filter, String[] roots, NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            Path locationFolder = getLocationAsPath();
            List<Iterator<NutsId>> list = new ArrayList<>();

            for (String root : roots) {
                if(root.endsWith("/*")){
                    String name = root.substring(0, root.length() - 2);
                    list.add(findInFolder(locationFolder.resolve(name), filter, Integer.MAX_VALUE, session));
                }else{
                    list.add(findInFolder(locationFolder.resolve(root), filter, 2, session));
                }
            }

            return IteratorUtils.concat(list);
        }
//        return IteratorUtils.emptyIterator();
        return null;
    }

//    @Override
//    public Path getStoreLocation(NutsStoreLocation folderType) {
//        switch (folderType) {
//            case LIB: {
//                return null;
//            }
//            //cache not supported!
//            case CACHE: {
//                return null;
//            }
//        }
//        return super.getStoreLocation(folderType);
//    }
    @Override
    public void updateStatistics2() {
        try {
            Files.walkFileTree(Paths.get(config().getLocation(true)), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    throw new UnsupportedOperationException("updateStatistics Not supported.");
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    throw new UnsupportedOperationException("updateStatistics Not supported.");
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    throw new UnsupportedOperationException("updateStatistics Not supported.");
                }
            }
            );
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    protected String getIdExtension(NutsId id) {
        return helper.getIdExtension(id);
    }

}
