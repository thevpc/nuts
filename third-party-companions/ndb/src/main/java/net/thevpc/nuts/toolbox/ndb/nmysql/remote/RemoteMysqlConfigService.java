package net.thevpc.nuts.toolbox.ndb.nmysql.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.toolbox.ndb.nmysql.NMySqlConfigVersions;
import net.thevpc.nuts.toolbox.ndb.nmysql.remote.config.RemoteMysqlConfig;
import net.thevpc.nuts.toolbox.ndb.nmysql.remote.config.RemoteMysqlDatabaseConfig;
import net.thevpc.nuts.toolbox.ndb.nmysql.util.MysqlUtils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class RemoteMysqlConfigService {

    public static final String CLIENT_CONFIG_EXT = ".remote-config";
    RemoteMysqlConfig config;
    NutsApplicationContext context;
    NutsPath sharedConfigFolder;
    private String name;

    public RemoteMysqlConfigService(String name, NutsApplicationContext context) {
        setName(name);
        this.context = context;
        sharedConfigFolder = context.getVersionFolder(NutsStoreLocation.CONFIG, NMySqlConfigVersions.CURRENT);
    }

    public String getName() {
        return name;
    }

    public RemoteMysqlConfigService setName(String name) {
        this.name = MysqlUtils.toValidFileName(name, "default");
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
        NutsPath f = getConfigPath();
        NutsSession session = context.getSession();
        NutsElements.of(session).json().setValue(config)
                .setNtf(false).print(f);
        return this;
    }

    private NutsPath getConfigPath() {
        return sharedConfigFolder.resolve(name + CLIENT_CONFIG_EXT);
    }

    public boolean existsConfig() {
        NutsPath f = getConfigPath();
        return (f.exists());
    }

    public RemoteMysqlConfigService loadConfig() {
        NutsSession session = context.getSession();
        if (name == null) {
            throw new NutsExecutionException(session, NutsMessage.cstyle("missing config name"), 2);
        }
        NutsPath f = getConfigPath();
        if (f.exists()) {
            config = NutsElements.of(session).json()
                    .setNtf(false)
                    .parse(f, RemoteMysqlConfig.class);
            return this;
        }
        throw new NoSuchElementException("config not found : " + name);
    }

    public RemoteMysqlConfigService removeConfig() {
        NutsPath f = getConfigPath();
        f.delete();
        return this;
    }

    public RemoteMysqlConfigService write(PrintStream out) {
        NutsSession session = context.getSession();
        NutsElements.of(session).json().setValue(getConfig())
                .setNtf(false).print(out);
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
        dbName = MysqlUtils.toValidFileName(dbName, "default");
        RemoteMysqlDatabaseConfig a = getConfig().getDatabases().get(dbName);
        if (a == null) {
            switch (action) {
                case OPEN_OR_NULL:
                    return null;
                case OPEN_OR_ERROR:
                    throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("remote instance not found: %s@%s", dbName, getName()));
                case CREATE_OR_ERROR:
                case OPEN_OR_CREATE: {
                    a = new RemoteMysqlDatabaseConfig();
                    getConfig().getDatabases().put(dbName, a);
                    return new RemoteMysqlDatabaseConfigService(dbName, a, this);
                }
                default: {
                    throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("unexpected error"));
                }
            }
        }
        switch (action) {
            case CREATE_OR_ERROR: {
                throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("remote instance not found: %s@%s", dbName, getName()));
            }
            case OPEN_OR_ERROR:
            case OPEN_OR_NULL:
            case OPEN_OR_CREATE: {
                return new RemoteMysqlDatabaseConfigService(dbName, a, this);
            }
            default: {
                throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("unexpected error"));
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
