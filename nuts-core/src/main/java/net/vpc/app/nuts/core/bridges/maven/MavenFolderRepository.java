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

import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.CommonRootsHelper;
import net.vpc.app.nuts.core.util.io.InputSource;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.*;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultNutsRepositoryUndeploymentOptions;
import net.vpc.app.nuts.core.util.common.IteratorUtils;

/**
 * Created by vpc on 1/5/17.
 */
public class MavenFolderRepository extends AbstractMavenRepository {

    public static final Logger LOG = Logger.getLogger(MavenFolderRepository.class.getName());

    public MavenFolderRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options, workspace, parentRepository, SPEED_FAST, NutsConstants.RepoTypes.MAVEN);
    }

    @Override
    protected InputSource openStream(NutsId id, String path, Object source, NutsRepositorySession session) {
        return CoreIOUtils.createInputSource(getWorkspace().io().path(path));
    }

    @Override
    protected String getStreamSHA1(NutsId id, NutsRepositorySession session) {
        return CoreIOUtils.evalSHA1(getStream(id.setFace(NutsConstants.QueryFaces.COMPONENT_HASH), session).open(), true);
    }

    @Override
    protected void checkSHA1Hash(NutsId id, InputStream stream, NutsRepositorySession session) {
        try {
            stream.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path getLocationAsPath() {
        return getWorkspace().io().path(config().getLocation(true));
    }

    @Override
    protected String getIdPath(NutsId id) {
        return getLocationAsPath().resolve(CoreIOUtils.syspath(getIdRelativePath(id))).toString();
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
    protected NutsContent fetchContentImpl(NutsId id, Path localPath, NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            Path f = getIdFile(id);
            if (f != null && Files.exists(f)) {
                if (localPath == null) {
                    return new NutsContent(f, true, false);
                } else {
                    getWorkspace().io().copy().from(f).to(localPath).safeCopy().run();
                    return new NutsContent(localPath, true, false);
                }
            }
        }
        throw new NutsNotFoundException(id);
    }

    protected Path getLocalGroupAndArtifactFile(NutsId id) {
        if (CoreStringUtils.isBlank(id.getGroup())) {
            return null;
        }
        if (CoreStringUtils.isBlank(id.getName())) {
            return null;
        }
        Path groupFolder = getLocationAsPath().resolve(id.getGroup().replace('.', File.separatorChar));
        return groupFolder.resolve(id.getName());
    }

    @Override
    protected Iterator<NutsId> findVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {

        Iterator<NutsId> namedNutIdIterator = null;
//        StringBuilder errors = new StringBuilder();
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            if (id.getVersion().isSingleValue()) {
                Path f = getIdFile(id.setFaceDescriptor());
                if (f != null && Files.exists(f)) {
                    NutsDescriptor d = null;
                    try {
                        d = parsePomDescriptor(f, session);
                    } catch (IOException ex) {
                        //
                    }
                    if (d != null) {
                        return Collections.singletonList(id.setNamespace(config().getName())).iterator();
                    }
                }
//                return Collections.emptyIterator();
                return null;
            }
            try {
                namedNutIdIterator = findInFolder(getLocalGroupAndArtifactFile(id), idFilter, Integer.MAX_VALUE, session);
            } catch (NutsNotFoundException ex) {
//                errors.append(ex).append(" \n");
            }
        }
//        if (namedNutIdIterator == null) {
//            return Collections.emptyIterator();
//        }
        return namedNutIdIterator;

    }

    protected NutsDescriptor parsePomDescriptor(Path pathname, NutsRepositorySession session) throws IOException {
        try (InputStream is = Files.newInputStream(pathname)) {
            NutsDescriptor nutsDescriptor = MavenUtils.parsePomXml(is, getWorkspace(), session, pathname.toString());
            if (nutsDescriptor.getId().getName() == null) {
                //why name is null ? should checkout!
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Unable to fetch Valid Nuts from " + pathname + " : resolved id was " + nutsDescriptor.getId());
                }
                return null;
            }
            return nutsDescriptor;
        }
    }

    @Override
    protected NutsId findLatestVersion(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        if (id.getVersion().isBlank() && filter == null) {
            Path file = getLocalGroupAndArtifactFile(id);
            NutsId bestId = null;
            if (Files.exists(file)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(file, CoreIOUtils.DIR_FILTER)) {
                    for (Path versionPath : stream) {
                        NutsId id2 = id.setVersion(versionPath.getFileName().toString());
                        String fn = getIdFilename(id2.setFaceDescriptor());
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
        return super.findLatestVersion(id, filter, session);
    }

    protected Iterator<NutsId> findInFolder(Path folder, final NutsIdFilter filter, int maxDepth, NutsRepositorySession session) {
        if (folder == null || !Files.exists(folder) || !Files.isDirectory(folder)) {
            return null;//Collections.emptyIterator();
        }
        return new FolderNutIdIterator(getWorkspace(), config().getName(), folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsRepositorySession session) {
                MavenFolderRepository.this.undeploy(new DefaultNutsRepositoryUndeploymentOptions().id(id), session);
            }

            @Override
            public boolean isDescFile(Path pathname) {
                return pathname.getFileName().toString().endsWith(".pom");
            }

            @Override
            public NutsDescriptor parseDescriptor(Path pathname, NutsRepositorySession session) throws IOException {
                return parsePomDescriptor(pathname, session);
            }
        }, maxDepth);
    }

    @Override
    protected Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsRepositorySession session) {
        List<CommonRootsHelper.PathBase> roots = CommonRootsHelper.resolveRootPaths(filter);

        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            Path locationFolder = getLocationAsPath();
            List<Iterator<NutsId>> list = new ArrayList<>();

            for (CommonRootsHelper.PathBase root : roots) {
                list.add(findInFolder(locationFolder.resolve(root.getName()), filter, root.isDeep() ? Integer.MAX_VALUE : 2, session));
            }

            return IteratorUtils.concat(list);
        }
//        return Collections.emptyIterator();
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
}
