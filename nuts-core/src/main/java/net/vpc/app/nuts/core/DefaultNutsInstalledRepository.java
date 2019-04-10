/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsNotInstallableException;
import net.vpc.app.nuts.NutsRepositorySession;
import net.vpc.app.nuts.NutsStoreLocation;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsVersionFilter;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.bridges.maven.MavenFolderRepository;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.FolderNutIdIterator;
import net.vpc.app.nuts.core.util.bundledlibs.util.IteratorBuilder;
import net.vpc.app.nuts.core.util.bundledlibs.util.LazyIterator;

/**
 *
 * @author vpc
 */
public class DefaultNutsInstalledRepository {

    public static class InstallInfo {

        private NutsId id;
        private Date installDate;

        public NutsId getId() {
            return id;
        }

        public void setId(NutsId id) {
            this.id = id;
        }

        public Date getInstallDate() {
            return installDate;
        }

        public void setInstallDate(Date installDate) {
            this.installDate = installDate;
        }

    }
    private static final String NUTS_INSTALL_FILE = "nuts-install.json";

    private final NutsWorkspace ws;

    public DefaultNutsInstalledRepository(NutsWorkspace ws) {
        this.ws = ws;
    }

    public boolean isInstalled(NutsId id) {
        return contains(id, NUTS_INSTALL_FILE);
    }

    protected Iterator<NutsId> findInFolder(Path folder, final NutsIdFilter filter, boolean deep, NutsRepositorySession session) {
        if (folder == null || !Files.exists(folder) || !Files.isDirectory(folder)) {
            return null;//Collections.emptyIterator();
        }
        return new FolderNutIdIterator(ws, "installed", folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsRepositorySession session) {
                //MavenFolderRepository.this.undeploy(id, session);
            }

            @Override
            public boolean isDescFile(Path pathname) {
                return pathname.getFileName().toString().equals("nuts-install.json");
            }

            @Override
            public NutsDescriptor parseDescriptor(Path pathname, NutsRepositorySession session) throws IOException {
                Map<String, Object> m = ws.io().readJson(pathname, Map.class);
                if (m != null) {
                    String id = (String) m.get("id");
                    if (id != null) {
                        return ws.fetch().id(id).local().session(session.getSession())
                                .setTransitive(session.isTransitive())
                                .setIndexed(session.isIndexed())
                                .setCached(session.isCached())
                                .getResultDescriptor();
                    }
                }
                return null;
            }
        }, deep);
    }

    public Iterator<NutsId> findAll(NutsIdFilter all, NutsRepositorySession session) {
        final Path path = ws.config().getStoreLocation(NutsStoreLocation.CONFIG);
        return findInFolder(path, all, true, session);
//        try {
//            return Files.walk(path)
//                    .map(p -> p.resolve(".nuts-install.log")).filter(p -> Files.exists(p))
//                    .map(p -> {
//                        try {
//                            return ws.io().readJson(p, InstallInfo.class);
//
//                        } catch (Exception e) {
//                            return null;
//                        }
//                    })
//                    .filter(p -> p != null && p.getId() != null)
//                    .map(p -> p.getId())
//                    .toArray(NutsId[]::new);
//        } catch (IOException ex) {
//            throw new UncheckedIOException(ex);
//        }
    }

    public Iterator<NutsId> findVersions(NutsId id, NutsIdFilter filter) {
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
                                        NutsVersion vv = ws.parser().parseVersion(folder.getName());
                                        if (filter0.accept(vv) && (filter == null || filter.accept(id.setVersion(vv)))) {
                                            return (id.setVersion(folder.getName()));
                                        }
                                    }
                                    return null;
                                }

                            })
                            .nonNull().iterator();
                }
                //ok.sort((a, b) -> CoreVersionUtils.compareVersions(a, b));
                return Collections.emptyIterator();
            }
        };
    }

    public Iterator<NutsId> findVersions(NutsId id, NutsVersionFilter filter) {
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
                                        NutsVersion vv = ws.parser().parseVersion(folder.getName());
                                        if (filter0.accept(vv) && (filter == null || filter.accept(vv))) {
                                            return (id.setVersion(folder.getName()));
                                        }
                                    }
                                    return null;
                                }

                            }).nonNull().iterator();

                }
                //ok.sort((a, b) -> CoreVersionUtils.compareVersions(a, b));
                return Collections.emptyIterator();
            }
        };
    }

    public NutsId[] findInstalledVersions(NutsId id) {
        Path installFolder = ws.config().getStoreLocation(id.setVersion("ANY"), NutsStoreLocation.CONFIG).getParent();
        List<NutsId> ok = new ArrayList<>();
        final NutsVersionFilter filter = id.getVersion().toFilter();
        if (Files.isDirectory(installFolder)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(installFolder)) {
                for (Path folder : ds) {
                    if (Files.isDirectory(folder) && Files.isRegularFile(folder.resolve(NUTS_INSTALL_FILE))) {
                        if (filter.accept(ws.parser().parseVersion(folder.getFileName().toString()))) {
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
        CoreNutsUtils.checkReadOnly(ws);
        remove(id, NUTS_INSTALL_FILE);
    }

    public boolean install(NutsId id) {
        if (!isInstalled(id)) {
            CoreNutsUtils.checkReadOnly(ws);
            try {
                InstallInfo ii = new InstallInfo();
                ii.setId(id);
                ii.setInstallDate(new Date());
                addJson(id, NUTS_INSTALL_FILE, ii);
            } catch (UncheckedIOException ex) {
                throw new NutsNotInstallableException(id.toString(), "Unable to install "
                        + id.setNamespace(null) + " : " + ex.getMessage(), ex);
            }
            return true;
        }
        return false;
    }

    public void addString(NutsId id, String name, String value) {
        try {
            Files.write(getPath(id, name), value.getBytes());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public <T> T readJson(NutsId id, String name, Class<T> clazz) {
        return ws.io().readJson(getPath(id, name), clazz);
    }

    public void addJson(NutsId id, String name, InstallInfo value) {
        ws.io().writeJson(value, getPath(id, name), true);
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
