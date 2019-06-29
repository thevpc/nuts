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
package net.vpc.app.nuts.core;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsInstallInfo;
import net.vpc.app.nuts.NutsNotInstallableException;
import net.vpc.app.nuts.NutsRepositorySession;
import net.vpc.app.nuts.NutsStoreLocation;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsVersionFilter;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.FolderNutIdIterator;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.iter.IteratorBuilder;
import net.vpc.app.nuts.core.util.iter.IteratorUtils;
import net.vpc.app.nuts.core.util.common.LRUMap;
import net.vpc.app.nuts.core.util.common.LazyIterator;

/**
 *
 * @author vpc
 */
public class DefaultNutsInstalledRepository {

    public static class InstallInfoConfig {

        private NutsId id;
        private Instant installDate;
        private String installUser;

        public NutsId getId() {
            return id;
        }

        public String getInstallUser() {
            return installUser;
        }

        public void setInstallUser(String installUser) {
            this.installUser = installUser;
        }

        public void setId(NutsId id) {
            this.id = id;
        }

        public Instant getInstallDate() {
            return installDate;
        }

        public void setInstallDate(Instant installDate) {
            this.installDate = installDate;
        }

        @Override
        public String toString() {
            return "InstallInfoConfig{" + "id=" + id + ", installDate=" + installDate + ", installUser=" + installUser + '}';
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + Objects.hashCode(this.id);
            hash = 89 * hash + Objects.hashCode(this.installDate);
            hash = 89 * hash + Objects.hashCode(this.installUser);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final InstallInfoConfig other = (InstallInfoConfig) obj;
            if (!Objects.equals(this.installUser, other.installUser)) {
                return false;
            }
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            if (!Objects.equals(this.installDate, other.installDate)) {
                return false;
            }
            return true;
        }

    }

    private static final String NUTS_INSTALL_FILE = "nuts-install.json";

    private final NutsWorkspace ws;
    private final Map<NutsId, String> cachedDefaultVersions = new LRUMap<>(200);

    public DefaultNutsInstalledRepository(NutsWorkspace ws) {
        this.ws = ws;
    }

    public boolean isDefaultVersion(NutsId id) {
        String v = getDefaultVersion(id);
        return v.equals(id.getVersion().toString());
    }

    public String getDefaultVersion(NutsId id) {
        NutsId baseVersion = id.getSimpleNameId();
        synchronized (cachedDefaultVersions) {
            String p = cachedDefaultVersions.get(baseVersion);
            if (p != null) {
                return p;
            }
        }
        Path pp = ws.config().getStoreLocation(id.setAlternative("").setVersion("ANY"), NutsStoreLocation.CONFIG).resolveSibling("default-version");
        String defaultVersion = "";
        if (Files.isRegularFile(pp)) {
            try {
                defaultVersion = new String(Files.readAllBytes(pp)).trim();
            } catch (IOException ex) {
                defaultVersion = "";
            }
        }
        synchronized (cachedDefaultVersions) {
            cachedDefaultVersions.put(baseVersion, defaultVersion);
        }
        return defaultVersion;
    }

    public void setDefaultVersion(NutsId id) {
        NutsId baseVersion = id.getSimpleNameId();
        String version = id.getVersion().getValue();
        Path pp = ws.config().getStoreLocation(id.setAlternative("").setVersion("ANY"), NutsStoreLocation.CONFIG).resolveSibling("default-version");
        if (CoreStringUtils.isBlank(version)) {
            if (Files.isRegularFile(pp)) {
                try {
                    Files.delete(pp);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        } else {
            try {
                Files.createDirectories(pp.getParent());
                Files.write(pp, version.trim().getBytes());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        synchronized (cachedDefaultVersions) {
            cachedDefaultVersions.put(baseVersion, version);
        }
    }

    public InstallInfoConfig getInstallInfoConfig(NutsId id) {
        Path p = getPath(id, NUTS_INSTALL_FILE);
        if (Files.isRegularFile(p)) {
            try {
                return readJson(id, NUTS_INSTALL_FILE, InstallInfoConfig.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public NutsInstallInfo getInstallInfo(NutsId id) {
        InstallInfoConfig ii = getInstallInfoConfig(id);
        if (ii != null) {
            return new DefaultNutsInstallInfo(true, isDefaultVersion(id),
                    ws.config().getStoreLocation(id, NutsStoreLocation.APPS),
                    ii.getInstallDate(),
                    ii.getInstallUser()
            );
        }
        return null;
    }

    public boolean isInstalled(NutsId id) {
        return contains(id, NUTS_INSTALL_FILE);
    }

    protected Iterator<NutsId> findInFolder(Path folder, final NutsIdFilter filter, int maxDepth, NutsRepositorySession session) {
        if (folder == null || !Files.exists(folder) || !Files.isDirectory(folder)) {
            return IteratorUtils.emptyIterator();
        }
        return new FolderNutIdIterator(ws, "installed", folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsRepositorySession session) {
                //MavenFolderRepository.this.undeploy(parseId, session);
            }

            @Override
            public boolean isDescFile(Path pathname) {
                return pathname.getFileName().toString().equals("nuts-install.json");
            }

            @Override
            public NutsDescriptor parseDescriptor(Path pathname, NutsRepositorySession session) throws IOException {
                Map<String, Object> m = ws.json().parse(pathname, Map.class);
                if (m != null) {
                    String id = (String) m.get("id");
                    if (id != null) {
                        return ws.fetch().id(id).offline().session(session.getSession().copy().trace(false))
                                .setTransitive(session.isTransitive())
                                .setIndexed(session.isIndexed())
                                .setCached(session.isCached())
                                .getResultDescriptor();
                    }
                }
                return null;
            }
        }, maxDepth);
    }

    public Iterator<NutsId> findAll(NutsIdFilter all, NutsRepositorySession session) {
        final Path path = ws.config().getStoreLocation(NutsStoreLocation.CONFIG);
        return findInFolder(path, all, Integer.MAX_VALUE, session);
    }

    public Iterator<NutsId> findVersions(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        return new LazyIterator<NutsId>() {
            @Override
            protected Iterator<NutsId> iterator() {
                File installFolder = ws.config().getStoreLocation(id.setVersion("ANY"), NutsStoreLocation.CONFIG).toFile().getParentFile();
                if (installFolder.isDirectory()) {
                    final NutsVersionFilter filter0 = id.getVersion().toFilter();
                    return IteratorBuilder.of(Arrays.asList(installFolder.listFiles()).iterator())
                            .map(new Function<File, NutsId>() {
                                @Override
                                public NutsId apply(File folder) {
                                    if (folder.isDirectory()
                                            && new File(folder, NUTS_INSTALL_FILE).isFile()) {
                                        NutsVersion vv = ws.version().parse(folder.getName());
                                        if (filter0.accept(vv, session.getSession()) && (filter == null || filter.accept(id.setVersion(vv), session.getSession()))) {
                                            return id.setVersion(folder.getName());
                                        }
                                    }
                                    return null;
                                }

                            })
                            .nonNull().iterator();
                }
                //ok.sort((a, b) -> CoreVersionUtils.compareVersions(a, b));
                return IteratorUtils.emptyIterator();
            }
        };
    }

    public NutsId[] findInstalledVersions(NutsId id, NutsRepositorySession session) {
        Path installFolder = ws.config().getStoreLocation(id.setVersion("ANY"), NutsStoreLocation.CONFIG).getParent();
        List<NutsId> ok = new ArrayList<>();
        final NutsVersionFilter filter = id.getVersion().toFilter();
        if (Files.isDirectory(installFolder)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(installFolder)) {
                for (Path folder : ds) {
                    if (Files.isDirectory(folder) && Files.isRegularFile(folder.resolve(NUTS_INSTALL_FILE))) {
                        if (filter.accept(ws.version().parse(folder.getFileName().toString()), session.getSession())) {
                            ok.add(id.setVersion(folder.getFileName().toString()));
                        }
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        //ok.sort((a, b) -> CoreVersionUtils.compareVersions(a, b));
        return ok.toArray(new NutsId[0]);
    }

    public void uninstall(NutsId id) {
        NutsWorkspaceUtils.checkReadOnly(ws);
        remove(id, NUTS_INSTALL_FILE);
    }

    public NutsInstallInfo install(NutsId id) {
        Instant now = Instant.now();
        String user = ws.security().getCurrentLogin();
        NutsWorkspaceUtils.checkReadOnly(ws);
        InstallInfoConfig ii;
        try {
            ii = new InstallInfoConfig();
            ii.setId(id);
            ii.setInstallDate(now);
            ii.setInstallUser(user);
            addJson(id, NUTS_INSTALL_FILE, ii);
        } catch (UncheckedIOException ex) {
            throw new NutsNotInstallableException(ws, id.toString(), "Unable to install "
                    + id.setNamespace(null) + " : " + ex.getMessage(), ex);
        }
        return new DefaultNutsInstallInfo(true, isDefaultVersion(id), ws.config().getStoreLocation(id, NutsStoreLocation.APPS), ii.getInstallDate(), ii.getInstallUser());
    }

    public void addString(NutsId id, String name, String value) {
        try {
            Files.write(getPath(id, name), value.getBytes());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public <T> T readJson(NutsId id, String name, Class<T> clazz) {
        return ws.json().parse(getPath(id, name), clazz);
    }

    public void addJson(NutsId id, String name, InstallInfoConfig value) {
        ws.json().value(value).print(getPath(id, name));
    }

    public void remove(NutsId id, String name) {
        try {
            Files.delete(getPath(id, name));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public boolean contains(NutsId id, String name) {
        return Files.isRegularFile(getPath(id, name));
    }

    public Path getPath(NutsId id, String name) {
        return ws.config().getStoreLocation(id, NutsStoreLocation.CONFIG).resolve(name);
    }
}
