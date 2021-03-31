package net.thevpc.nuts.toolbox.ndb.nmysql.remote;

import net.thevpc.common.io.FileUtils;
import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.ndb.nmysql.NMySqlConfigVersions;
import net.thevpc.nuts.toolbox.ndb.nmysql.remote.config.RemoteMysqlConfig;
import net.thevpc.nuts.toolbox.ndb.nmysql.remote.config.RemoteMysqlDatabaseConfig;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class RemoteMysqlConfigService {

    public static final String CLIENT_CONFIG_EXT = ".remote-config";
    RemoteMysqlConfig config;
    NutsApplicationContext context;
    Path sharedConfigFolder;
    private String name;

    public RemoteMysqlConfigService(String name, NutsApplicationContext context) {
        setName(name);
        this.context = context;
        sharedConfigFolder = Paths.get(context.getVersionFolderFolder(NutsStoreLocation.CONFIG, NMySqlConfigVersions.CURRENT));
    }

    public String getName() {
        return name;
    }

    public RemoteMysqlConfigService setName(String name) {
        this.name = FileUtils.toValidFileName(name, "default");
        return this;
    }

    public RemoteMysqlConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    public RemoteMysqlConfigService setConfig(RemoteMysqlConfig config) {
        this.config = config;
        return this;
    }

    public RemoteMysqlConfigService saveConfig() {
        Path f = getConfigPath();
        context.getWorkspace().formats().element().setContentType(NutsContentType.JSON).setValue(config).print(f);
        return this;
    }

    private Path getConfigPath() {
        return sharedConfigFolder.resolve(name + CLIENT_CONFIG_EXT);
    }

    public boolean existsConfig() {
        Path f = getConfigPath();
        return (Files.exists(f));
    }

    public RemoteMysqlConfigService loadConfig() {
        if (name == null) {
            throw new NutsExecutionException(context.getWorkspace(), "missing config name", 2);
        }
        Path f = getConfigPath();
        if (Files.exists(f)) {
            config = context.getWorkspace().formats().element().setContentType(NutsContentType.JSON).parse(f, RemoteMysqlConfig.class);
            return this;
        }
        throw new NoSuchElementException("config not found : " + name);
    }

    public RemoteMysqlConfigService removeConfig() {
        Path f = getConfigPath();
        try {
            Files.delete(f);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return this;
    }

    public RemoteMysqlConfigService write(PrintStream out) {
        context.getWorkspace().formats().element().setContentType(NutsContentType.JSON).setValue(getConfig()).print(out);
        out.flush();
        return this;
    }

    public RemoteMysqlDatabaseConfigService getDatabaseOrCreate(String appName) {
        RemoteMysqlDatabaseConfig a = getConfig().getDatabases().get(appName);
        if (a == null) {
            a = new RemoteMysqlDatabaseConfig();
            getConfig().getDatabases().put(appName, a);
        }
        return new RemoteMysqlDatabaseConfigService(appName, a, this);
    }

    public RemoteMysqlDatabaseConfigService getDatabase(String dbName, NutsOpenMode action) {
        dbName = FileUtils.toValidFileName(dbName, "default");
        RemoteMysqlDatabaseConfig a = getConfig().getDatabases().get(dbName);
        if (a == null) {
            switch (action) {
                case OPEN_OR_NULL:
                    return null;
                case OPEN_OR_ERROR:
                    throw new NutsIllegalArgumentException(context.getWorkspace(), "remote instance not found:" + dbName + "@" + getName());
                case CREATE_OR_ERROR:
                case OPEN_OR_CREATE: {
                    a = new RemoteMysqlDatabaseConfig();
                    getConfig().getDatabases().put(dbName, a);
                    return new RemoteMysqlDatabaseConfigService(dbName, a, this);
                }
                default: {
                    throw new NutsIllegalArgumentException(context.getWorkspace(), "unexpected error");
                }
            }
        }
        switch (action) {
            case CREATE_OR_ERROR: {
                throw new NutsIllegalArgumentException(context.getWorkspace(), "remote instance not found:" + dbName + "@" + getName());
            }
            case OPEN_OR_ERROR:
            case OPEN_OR_NULL:
            case OPEN_OR_CREATE: {
                return new RemoteMysqlDatabaseConfigService(dbName, a, this);
            }
            default: {
                throw new NutsIllegalArgumentException(context.getWorkspace(), "unexpected error");
            }
        }
    }

    public List<RemoteMysqlDatabaseConfigService> getDatabases() {
        List<RemoteMysqlDatabaseConfigService> a = new ArrayList<>();
        for (String s : getConfig().getDatabases().keySet()) {
            a.add(new RemoteMysqlDatabaseConfigService(s, getConfig().getDatabases().get(s), this));
        }
        return a;
    }

}
