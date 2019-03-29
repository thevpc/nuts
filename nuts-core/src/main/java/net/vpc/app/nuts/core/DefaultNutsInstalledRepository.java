/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsNotInstallableException;
import net.vpc.app.nuts.NutsStoreLocation;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsVersionFilter;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.common.util.Converter;
import net.vpc.common.util.IteratorBuilder;
import net.vpc.common.util.LazyIterator;

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

    public NutsId[] findAll(NutsIdFilter all) {
        final Path path = new File(ws.getConfigManager().getStoreLocation(NutsStoreLocation.CONFIG)).toPath();
        try {
            return Files.walk(path)
                    .map(p -> new File(p.toFile(), ".nuts-install.log")).filter(p -> p.exists())
                    .map(p -> {
                        try {
                            return ws.getIOManager().readJson(p, InstallInfo.class);

                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(p -> p != null && p.getId() != null)
                    .map(p -> p.getId())
                    .toArray(NutsId[]::new);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Iterator<NutsId> findVersions(NutsId id, NutsIdFilter filter) {
        return new LazyIterator<NutsId>() {
            @Override
            protected Iterator<NutsId> iterator() {
                File installFolder = new File(ws.getConfigManager().getStoreLocation(id.setVersion("ANY"), NutsStoreLocation.CONFIG)).getParentFile();
                if (installFolder.isDirectory()) {
                    final NutsVersionFilter filter0 = id.getVersion().toFilter();
                    return IteratorBuilder.of(Arrays.asList(installFolder.listFiles()).iterator())
                            .map(new Converter<File, NutsId>() {
                                @Override
                                public NutsId convert(File folder) {
                                    if (folder.isDirectory()
                                            && new File(folder, NUTS_INSTALL_FILE).isFile()) {
                                        NutsVersion vv = ws.getParseManager().parseVersion(folder.getName());
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
                File installFolder = new File(ws.getConfigManager().getStoreLocation(id.setVersion("ANY"), NutsStoreLocation.CONFIG)).getParentFile();
                if (installFolder.isDirectory()) {
                    final NutsVersionFilter filter0 = id.getVersion().toFilter();
                    
                    return IteratorBuilder.of(Arrays.asList(installFolder.listFiles()).iterator())
                            .map(new Converter<File, NutsId>() {
                                @Override
                                public NutsId convert(File folder) {
                                    if (folder.isDirectory()
                                            && new File(folder, NUTS_INSTALL_FILE).isFile()) {
                                        NutsVersion vv = ws.getParseManager().parseVersion(folder.getName());
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
        File installFolder = new File(ws.getConfigManager().getStoreLocation(id.setVersion("ANY"), NutsStoreLocation.CONFIG)).getParentFile();
        List<NutsId> ok = new ArrayList<>();
        final NutsVersionFilter filter = id.getVersion().toFilter();
        if (installFolder.isDirectory()) {
            for (File folder : installFolder.listFiles()) {
                if (folder.isDirectory() && new File(folder, NUTS_INSTALL_FILE).isFile()) {
                    if (filter.accept(ws.getParseManager().parseVersion(folder.getName()))) {
                        ok.add(id.setVersion(folder.getName()));
                    }
                }
            }
        }
        //ok.sort((a, b) -> CoreVersionUtils.compareVersions(a, b));
        return ok.toArray(new NutsId[0]);
    }

    public boolean uninstall(NutsId id) {
        CoreNutsUtils.checkReadOnly(ws);
        return remove(id, NUTS_INSTALL_FILE);
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
        File installFolder = new File(ws.getConfigManager().getStoreLocation(id, NutsStoreLocation.CONFIG));
        File log = new File(installFolder, name);
        CoreNutsUtils.copy(new ByteArrayInputStream(String.valueOf(new Date()).getBytes()), log, true, true);
    }

    public <T> T readJson(NutsId id, String name, Class<T> clazz) {
        File installFolder = new File(ws.getConfigManager().getStoreLocation(id, NutsStoreLocation.CONFIG));
        File log = new File(installFolder, name);
        return ws.getIOManager().readJson(log, clazz);
    }

    public void addJson(NutsId id, String name, InstallInfo value) {
        File installFolder = new File(ws.getConfigManager().getStoreLocation(id, NutsStoreLocation.CONFIG));
        File log = new File(installFolder, name);
        ws.getIOManager().writeJson(value, log, true);
    }

    public boolean remove(NutsId id, String name) {
        return new File(ws.getConfigManager().getStoreLocation(id, NutsStoreLocation.CONFIG), name).delete();
    }

    public boolean contains(NutsId id, String name) {
        return new File(ws.getConfigManager().getStoreLocation(id, NutsStoreLocation.CONFIG), name).isFile();
    }
}
