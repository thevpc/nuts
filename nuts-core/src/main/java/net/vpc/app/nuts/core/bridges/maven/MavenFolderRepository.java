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
import net.vpc.app.nuts.core.util.*;
import net.vpc.common.io.IOUtils;
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

    public MavenFolderRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options,workspace,parentRepository,SPEED_FAST);
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
    protected String getStreamSHA1(NutsId id, NutsSession session) {
        return CoreSecurityUtils.evalSHA1(getStream(id.setFace(NutsConstants.FACE_COMPONENT_HASH), session), true);
    }

    @Override
    protected void checkSHA1Hash(NutsId id, InputStream stream, NutsSession session) {
        try {
            stream.close();
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
    }

    @Override
    protected String getIdPath(NutsId id) {
        return new File(getConfigManager().getLocation(true), CoreNutsUtils.syspath(getIdRelativePath(id))).getPath();
    }

    @Override
    public String getStoreLocation() {
        return null;
    }

    public File getIdFile(NutsId id) {
        String p = getIdRelativePath(id);
        if (p != null) {
            return new File(getConfigManager().getLocation(true), p);
        }
        return null;
    }

    @Override
    protected NutsContent fetchContentImpl(NutsId id, String localPath, NutsSession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            File f = getIdFile(id);
            if (f != null && f.exists()) {
                if (localPath == null) {
                    return new NutsContent(f.getPath(), true, false);
                } else {
                    IOUtils.copy(f, new File(localPath), true);
                    return new NutsContent(localPath, true, false);
                }
            }
        }
        throw new NutsNotFoundException(id);
    }

    protected File getLocalGroupAndArtifactFile(NutsId id) {
        if (StringUtils.isEmpty(id.getGroup())) {
            return null;
        }
        if (StringUtils.isEmpty(id.getName())) {
            return null;
        }
        File groupFolder = new File(getConfigManager().getLocation(true), id.getGroup().replace('.', File.separatorChar));
        return new File(groupFolder, id.getName());
    }

    @Override
    protected List<NutsId> findVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsSession session) {
        List<NutsId> namedNutIdIterator = null;
//        StringBuilder errors = new StringBuilder();
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            if (id.getVersion().isSingleValue()) {
                File f = getIdFile(id.setFaceDescriptor());
                if (f != null && f.exists()) {
                    NutsDescriptor d = null;
                    try {
                        d = parsePomDescriptor(f, session);
                    } catch (IOException ex) {
                        //
                    }
                    if (d != null) {
                        return new ArrayList<>(Collections.singletonList(id.setNamespace(getName())));
                    }
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
            //why name is null ? should checkout!
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Unable to fetch Valid Nuts from " + pathname + " : resolved id was " + nutsDescriptor.getId());
            }
            return null;
        }
        return nutsDescriptor;
    }

    protected NutsId findLatestVersion(NutsId id, NutsIdFilter filter, NutsSession session) {
        if (id.getVersion().isEmpty() && filter == null) {
            File file = getLocalGroupAndArtifactFile(id);
            NutsId bestId = null;
            if (file.exists()) {
                File[] versionFolders = file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });
                if (versionFolders != null) {
                    for (File versionFolder : versionFolders) {
                        NutsId id2 = id.setVersion(versionFolder.getName());
                        String fn = getIdFilename(id2.setFaceDescriptor());
                        if (new File(versionFolder, fn).isFile()) {
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
        return new FolderNutIdIterator(getWorkspace(), this, folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
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
            String locationFolder = getConfigManager().getLocation(true);
            return findInFolder(new File(locationFolder), filter, session);
        }
        return Collections.emptyIterator();
    }

    public String getStoreLocation(NutsStoreFolder folderType) {
        switch (folderType) {
            case LIB: {
                return getStoreLocation();
            }
            //cache not supported!
            case CACHE: {
                return null;
            }
        }
        return super.getStoreLocation(folderType);
    }
}
