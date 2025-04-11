package net.thevpc.nuts.runtime.standalone.store;

import net.thevpc.nuts.*;
import net.thevpc.nuts.concurrent.NLock;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NDescriptorFormat;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathPermission;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.NLocationKey;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.util.FolderObjectIterator;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNInstalledRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.InstallInfoConfig;
import net.thevpc.nuts.runtime.standalone.repository.index.NanoDBNIdSerializer;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.CompatUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.NVersionCompat;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.file.NanoDBOnDisk;
import net.thevpc.nuts.util.*;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;

public class NWorkspaceStoreOnDisk extends AbstractNWorkspaceStore {
    private NanoDB db;
    private NanoDB cacheb;

    public NWorkspaceStoreOnDisk() {
    }

    @Override
    public NanoDB cacheDB() {
        if (cacheb == null) {
            cacheb = new NanoDBOnDisk(
                    NWorkspace.of().getStoreLocation(
                            NWorkspace.of().getApiId().builder().setVersion("SHARED").build()
                            ,
                            NStoreType.CACHE
                    ).resolve("cachedb").toFile().get()
            );
            cacheb.getSerializers().setSerializer(NId.class, () -> new NanoDBNIdSerializer());
        }
        return cacheb;
    }

    @Override
    public NanoDB varDB() {
        if (db == null) {
            db = new NanoDBOnDisk(
                    NWorkspace.of().getStoreLocation(
                            NWorkspace.of().getApiId().builder().setVersion("SHARED").build(),
                            NStoreType.VAR
                    ).resolve("vardb").toFile().get()
            );
            db.getSerializers().setSerializer(NId.class, () -> new NanoDBNIdSerializer());
        }
        return db;
    }

    @Override
    public boolean isValidWorkspaceFolder() {
        Path file = NWorkspace.of().getWorkspaceLocation().toPath().get().resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        return Files.isRegularFile(file);
    }

    @Override
    public NWorkspaceConfigBoot loadWorkspaceConfigBoot() {
        return loadWorkspaceConfigBoot(NWorkspace.of().getWorkspaceLocation());
    }


    @Override
    public void saveWorkspaceConfigBoot(NWorkspaceConfigBoot value) {
        Path file = NWorkspace.of().getWorkspaceLocation().toPath().get().resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        storeObject(value, file.toString());
    }

    @Override
    public void saveConfigSecurity(NWorkspaceConfigSecurity value) {
        NPath configVersionSpecificLocation = NWorkspace.of().getStoreLocation(NWorkspace.of().getApiId(), NStoreType.CONF);
        NPath file = configVersionSpecificLocation.resolve(CoreNConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
        storeObject(value, file.toString());
    }

    @Override
    public void saveConfigMain(NWorkspaceConfigMain value) {
        NPath configVersionSpecificLocation = NWorkspace.of().getStoreLocation(NWorkspace.of().getApiId(), NStoreType.CONF);
        NPath file = configVersionSpecificLocation.resolve(CoreNConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
        storeObject(value, file.toString());
    }

    @Override
    public void saveConfigApi(NWorkspaceConfigApi value) {
        NWorkspace workspace = NWorkspace.of();
        NPath apiVersionSpecificLocation = workspace.getStoreLocation(workspace.getApiId(), NStoreType.CONF);
        NPath afile = apiVersionSpecificLocation.resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME);
        storeObject(value, afile.toString());
    }

    @Override
    public void saveConfigRuntime(NWorkspaceConfigRuntime value) {
        NWorkspace workspace = NWorkspace.of();
        NPath conf = workspace.getStoreLocation(NStoreType.CONF)
                .resolve(NConstants.Folders.ID).resolve(workspace.getDefaultIdBasedir(workspace.getRuntimeId()));
        NPath file = conf.resolve(NConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
        storeObject(value, file.toString());
    }

    public void storeObject(Object anyObject, String file) {
        NElements.of().json().setValue(anyObject).setNtf(false).print(NPath.of(file));
    }

    public NWorkspaceConfigBoot loadWorkspaceConfigBoot(NPath workspacePath) {
        Path file = workspacePath.toPath().get().resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(file);
        if (bytes == null) {
            return null;
        }
        try {
            Map<String, Object> a_config0 = NElements.of().json().parse(bytes, Map.class);
            NVersion version = NVersion.get((String) a_config0.get("configVersion")).ifBlankEmpty().orNull();
            if (version == null) {
                version = NVersion.get((String) a_config0.get("createApiVersion")).ifBlankEmpty().orNull();
                if (version == null) {
                    version = Nuts.getVersion();
                }
            }
            return NVersionCompat.of(version).parseConfig(bytes);
        } catch (Exception ex) {
            _LOG().with().level(Level.SEVERE).verb(NLogVerb.FAIL)
                    .log(NMsg.ofC("erroneous workspace config file. Unable to load file %s : %s",
                            file, ex));
            throw new NIOException(NMsg.ofC("unable to load config file %s", file), ex);
        }
    }

    @Override
    public NWorkspaceConfigApi loadConfigApi(NId apiId) {
        NWorkspace workspace = NWorkspace.of();
        if (apiId == null) {
            apiId = workspace.getApiId();
        }
        NPath path = workspace.getStoreLocation(apiId, NStoreType.CONF)
                .resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigApi c = bytes == null ? null : NElements.of()
                .json().parse(bytes, NWorkspaceConfigApi.class);
//        if (c != null) {
//            c.setApiVersion(getApiVersion());
//        }
        return c;
    }

    @Override
    public NWorkspaceConfigRuntime loadConfigRuntime() {
        NWorkspace workspace = NWorkspace.of();
        NPath path = workspace.getStoreLocation(workspace.getRuntimeId(), NStoreType.CONF)
                .resolve(NConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigRuntime c = bytes == null ? null : NElements.of()
                .json().parse(bytes, NWorkspaceConfigRuntime.class);
        return c;
    }

    @Override
    public NWorkspaceConfigSecurity loadConfigSecurity(NId apiId) {
        NWorkspace workspace = NWorkspace.of();
        if (apiId == null) {
            apiId = workspace.getApiId();
        }
        NPath path = workspace.getStoreLocation(apiId
                        , NStoreType.CONF)
                .resolve(CoreNConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigSecurity c = bytes == null ? null : NElements.of()
                .json().parse(bytes, NWorkspaceConfigSecurity.class);
        return c;
    }

    @Override
    public NWorkspaceConfigMain loadConfigMain(NId apiId) {
        NWorkspace workspace = NWorkspace.of();
        if (apiId == null) {
            apiId = workspace.getApiId();
        }
        NPath path = workspace.getStoreLocation(apiId, NStoreType.CONF)
                .resolve(CoreNConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigMain c = bytes == null ? null : NElements.of()
                .json().parse(bytes, NWorkspaceConfigMain.class);
        return c;
    }

    @Override
    public boolean saveRepoConfig(NRepository repository, NRepositoryConfig config) {
        NPath file = repository.config().getStoreLocation().resolve(NConstants.Files.REPOSITORY_CONFIG_FILE_NAME);
        boolean created = false;
        if (!file.exists()) {
            created = true;
        }
        repository.config().getStoreLocation().mkdirs();
        NElements.of().json().setValue(config)
                .setNtf(false)
                .print(file);
        return created;
    }

    @Override
    public NRepositoryConfig loadRepoConfig(String location, String name) {
        NWorkspace workspace = NWorkspace.of();
        NPath file = NPath.of(location).resolve(NConstants.Files.REPOSITORY_CONFIG_FILE_NAME);
        NRepositoryConfig conf = null;
        if (file.isRegularFile() && file.getPermissions().contains(NPathPermission.CAN_READ)) {
            byte[] bytes = file.readBytes();
            try {
                NElements elem = NElements.of();
                Map<String, Object> a_config0 = elem.json().parse(bytes, Map.class);
                NVersion version = NVersion.get((String) a_config0.get("configVersion")).orNull();
                if (version == null || version.isBlank()) {
                    version = workspace.getApiVersion();
                }
                int buildNumber = CoreNUtils.getApiVersionOrdinalNumber(version);
                if (buildNumber < 506) {

                }
                conf = elem.json().parse(file, NRepositoryConfig.class);
            } catch (RuntimeException ex) {
                if (workspace.getBootOptions().getRecover().orElse(false)) {
                    onLoadRepositoryError(file, name, null, ex);
                } else {
                    throw ex;
                }
            }
        }
        return conf;
    }

    private void onLoadRepositoryError(NPath file, String name, String uuid, Throwable ex) {
        NWorkspace workspace = NWorkspace.of();
        if (workspace.isReadOnly()) {
            throw new NIOException(NMsg.ofC("error loading repository %s", file), ex);
        }
        NLogOp _LOG = _LOG().with();
        String fileName = "nuts-repository" + (name == null ? "" : ("-") + name) + (uuid == null ? "" : ("-") + uuid) + "-" + Instant.now().toString();
        _LOG.level(Level.SEVERE).verb(NLogVerb.FAIL).log(
                NMsg.ofC("erroneous repository config file. Unable to load file %s : %s", file, ex));
        NPath logError = workspace.getStoreLocation(workspace.getApiId(), NStoreType.LOG)
                .resolve("invalid-config");
        try {
            logError.mkParentDirs();
        } catch (Exception ex1) {
            throw new NIOException(NMsg.ofC("unable to log repository error while loading config file %s : %s", file, ex1), ex);
        }
        NPath newfile = logError.resolve(fileName + ".json");
        _LOG.level(Level.SEVERE).verb(NLogVerb.FAIL)
                .log(NMsg.ofC("erroneous repository config file will be replaced by a fresh one. Old config is copied to %s", newfile));
        try {
            Files.move(file.toPath().get(), newfile.toPath().get());
        } catch (IOException e) {
            throw new NIOException(NMsg.ofC("nable to load and re-create repository config file %s : %s", file, e), ex);
        }

        try (PrintStream o = new PrintStream(logError.resolve(fileName + ".error").getOutputStream())) {
            o.printf("workspace.path:%s%n", workspace.getWorkspaceLocation());
            o.printf("repository.path:%s%n", file);
            o.printf("workspace.options:%s%n", workspace.getBootOptions().toCmdLine(new NWorkspaceOptionsConfig().setCompact(false)));
            for (NStoreType location : NStoreType.values()) {
                o.printf("location." + location.id() + ":%s%n", workspace.getStoreLocation(location));
            }
            o.printf("java.class.path:%s%n", System.getProperty("java.class.path"));
            o.println();
            ex.printStackTrace(o);
        } catch (Exception ex2) {
            //ignore
        }
    }

    @Override
    public void saveInstallInfoConfig(InstallInfoConfig installInfoConfig) {
        NWorkspace workspace = NWorkspace.of();
        NPath path = workspace.getStoreLocation(installInfoConfig.getId(), NStoreType.CONF).resolve(DefaultNInstalledRepository.NUTS_INSTALL_FILE);
        NElements.of().setNtf(false)
                .json().setValue(installInfoConfig)
                .print(path);
    }

    @Override
    public Iterator<NVersion> searchInstalledVersions(NId id) {
        NWorkspace workspace = NWorkspace.of();
        NPath installFolder
                = workspace.getStoreLocation(id.builder().setVersion("ANY").build(), NStoreType.CONF).getParent();
        if (installFolder.isDirectory()) {
            final NVersionFilter filter0 = id.getVersion().filter();
            return NIteratorBuilder.of(installFolder.stream().iterator())
                    .map(NFunction.of(
                            new Function<NPath, NVersion>() {
                                @Override
                                public NVersion apply(NPath folder) {
                                    if (folder.isDirectory()
                                            && folder.resolve(DefaultNInstalledRepository.NUTS_INSTALL_FILE).isRegularFile()) {
                                        NVersion vv = NVersion.get(folder.getName()).get();
                                        if (filter0.acceptVersion(vv)) {
                                            return vv;
                                        }
                                    }
                                    return null;
                                }
                            }).withDesc(NEDesc.of("FileToVersion")))
                    .notNull().iterator();
        } else {
            //ok.sort((a, b) -> CoreVersionUtils.compareVersions(a, b));
            return NIteratorBuilder.emptyIterator();
        }
    }

    @Override
    public Iterator<InstallInfoConfig> searchInstalledVersions() {
        NWorkspace workspace = NWorkspace.of();
        NPath rootFolder = workspace.getStoreLocation(NStoreType.CONF).resolve(NConstants.Folders.ID);
        return new FolderObjectIterator<InstallInfoConfig>("InstallInfoConfig",
                rootFolder,
                null, -1, new FolderObjectIterator.FolderIteratorModel<InstallInfoConfig>() {
            @Override
            public boolean isObjectFile(NPath pathname) {
                return pathname.getName().equals(DefaultNInstalledRepository.NUTS_INSTALL_FILE);
            }

            @Override
            public InstallInfoConfig parseObject(NPath path) {
                try {
                    InstallInfoConfig u = NElements.of().json().parse(path, InstallInfoConfig.class);
                    if(u!=null && u.getId()!=null){
                        return loadInstallInfoConfig(u.getId());
                    }
                } catch (Exception ex) {
                    _LOG().with().error(ex)
                            .log(NMsg.ofC("unable to parse %s", path));
                }
                return null;
            }
        }
        );
    }

    @Override
    public void deleteInstallInfoConfig(NId id) {
        remove(id, DefaultNInstalledRepository.NUTS_INSTALL_FILE);
    }

    public void remove(NId id, String name) {
        NPath path = getPath(id, name);
        path.delete();
    }

    public NPath getPath(NId id, String name) {
        return NWorkspace.of().getStoreLocation(id, NStoreType.CONF).resolve(name);
    }

    @Override
    public InstallInfoConfig loadInstallInfoConfig(NId id) {
        CoreNIdUtils.checkShortId(id);
        NWorkspace workspace = NWorkspace.of();
        NPath path = getPath(id, DefaultNInstalledRepository.NUTS_INSTALL_FILE);
//        if (id == null) {
//            path = getPath(id, NUTS_INSTALL_FILE);
//        }
        if (path.isRegularFile()) {
            NElements elem = NElements.of();
            InstallInfoConfig c = NLock.ofIdPath(id,DefaultNInstalledRepository.NUTS_INSTALL_FILE).callWith(
                    () -> elem.json().parse(path, InstallInfoConfig.class),
                    CoreNUtils.LOCK_TIME, CoreNUtils.LOCK_TIME_UNIT
            ).orNull();
            if (c != null) {
                boolean changeStatus = false;
                NVersion v = c.getConfigVersion();
                if (NBlankable.isBlank(v)) {
                    c.setInstalled(true);
                    c.setConfigVersion(NVersion.get("0.5.8").get()); //last version before 0.6
                    changeStatus = true;
                }
                NId idOk = c.getId();
                if (idOk == null) {
                    if (id != null) {
                        c.setId(id);
                        changeStatus = true;
                    } else {
                        NId idOk2 = pathToId(path);
                        if (idOk2 != null) {
                            c.setId(idOk2);
                            changeStatus = true;
                        } else {
                            return null;
                        }
                    }
                }
                if (changeStatus && !workspace.isReadOnly()) {
                    NLock.ofPath(path).callWith(() -> {
                                _LOG().with().level(Level.CONFIG)
                                        .log(NMsg.ofC("install-info upgraded %s", path));
                                c.setConfigVersion(workspace.getApiVersion());
                                elem.json().setValue(c)
                                        .setNtf(false)
                                        .print(path);
                                return null;
                            },
                            CoreNUtils.LOCK_TIME, CoreNUtils.LOCK_TIME_UNIT
                    ).get();
                }
            }
            return c;
        }
        return null;
    }

    public NId pathToId(NPath path) {
        NPath rootFolder = NWorkspace.of().getStoreLocation(NStoreType.CONF).resolve(NConstants.Folders.ID);
        String p = path.toString().substring(rootFolder.toString().length());
        List<String> split = StringTokenizerUtils.split(p, "/\\");
        if (split.size() >= 4) {
            return NIdBuilder.of()
                    .setGroupId(String.join(".", split.subList(0, split.size() - 3)))
                    .setArtifactId(split.get(split.size() - 3))
                    .setVersion(split.get(split.size() - 2)).build();

        }
        return null;
    }

    @Override
    public String loadInstalledDefaultVersion(NId id) {
        NPath pp = NWorkspace.of().getStoreLocation(id
                        //.setAlternative("")
                        .builder().setVersion("ANY").build(), NStoreType.CONF)
                .resolveSibling("default-version");
        String defaultVersion = "";
        if (pp.isRegularFile()) {
            try {
                defaultVersion = new String(pp.readBytes()).trim();
            } catch (Exception ex) {
                defaultVersion = "";
            }
        }
        return defaultVersion;
    }

    @Override
    public void saveInstalledDefaultVersion(NId id) {
        String version = id.getVersion().getValue();
        NPath pp = NWorkspace.of().getStoreLocation(id
                        //                .setAlternative("")
                        .builder().setVersion("ANY").build(), NStoreType.CONF)
                .resolveSibling("default-version");
        if (NBlankable.isBlank(version)) {
            if (pp.isRegularFile()) {
                pp.delete();
            }
        } else {
            pp.mkParentDirs();
            pp.writeString(version.trim());
        }
    }

    private static NLog _LOG() {
        return NLog.of(NWorkspaceStoreOnDisk.class);
    }

    @Override
    public void saveLocationKey(NLocationKey k, Object value) {
        NPath path = NWorkspace.of().getStoreLocation(k);
        if(value==null) {
            path.delete();
        }else if(value instanceof String) {
            path.mkParentDirs().writeString((String) value);
        }else if(value instanceof NDescriptor) {
            try {
                NDescriptorFormat.of((NDescriptor) value).setNtf(false).print(path);
            } catch (Exception ex) {
                _LOG().with().level(Level.FINE).error(ex)
                        .log(NMsg.ofC("failed to print %s", path));
                //
            }
        }else{
            NElements.of().json().setNtf(false).setValue(value).print(path);
        }
    }

    @Override
    public <T> T loadLocationKey(NLocationKey k, Class<T> type) {
        NPath path = NWorkspace.of().getStoreLocation(k);
        invalidateIfObsolete(k, path);
        if(path.isRegularFile()) {
            if (String.class.equals(type)) {
                return (T) path.readString();
            } else if (NDescriptor.class.equals(type)) {
                return (T) NDescriptorParser.of().parse(path).orNull();
            } else {
                return NElements.of() .json().parse(path, type);
            }
        }
        return null;
    }

    @Override
    public boolean deleteLocationKey(NLocationKey k) {
        NPath path = NWorkspace.of().getStoreLocation(k);
        if(path.isRegularFile()) {
            path.delete();
            return true;
        }
        return false;
    }

    private static void invalidateIfObsolete(NLocationKey k, NPath cachePath) {
        try {
            if (k.getStoreType() == NStoreType.CACHE && cachePath.isRegularFile() && CoreIOUtils.isObsoletePath(cachePath)) {
                cachePath.delete();
            }
        } catch (Exception e) {
            //
        }
    }


}
