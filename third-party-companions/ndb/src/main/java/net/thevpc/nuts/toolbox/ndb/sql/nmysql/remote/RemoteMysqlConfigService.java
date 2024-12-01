package net.thevpc.nuts.toolbox.ndb.sql.nmysql.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.NMySqlConfigVersions;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.remote.config.RemoteMysqlConfig;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.remote.config.RemoteMysqlDatabaseConfig;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.MysqlUtils;
import net.thevpc.nuts.util.NMsg;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class RemoteMysqlConfigService {

    public static final String CLIENT_CONFIG_EXT = ".remote-config";
    RemoteMysqlConfig config;
    NSession session;
    NPath sharedConfigFolder;
    private String name;

    public RemoteMysqlConfigService(String name, NSession session) {
        setName(name);
        this.session = session;
        sharedConfigFolder = NApp.of().getVersionFolder(NStoreType.CONF, NMySqlConfigVersions.CURRENT);
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
        NPath f = getConfigPath();
        NElements.of().json().setValue(config)
                .setNtf(false).print(f);
        return this;
    }

    private NPath getConfigPath() {
        return sharedConfigFolder.resolve(name + CLIENT_CONFIG_EXT);
    }

    public boolean existsConfig() {
        NPath f = getConfigPath();
        return (f.exists());
    }

    public RemoteMysqlConfigService loadConfig() {
        if (name == null) {
            throw new NExecutionException(NMsg.ofPlain("missing config name"), NExecutionException.ERROR_2);
        }
        NPath f = getConfigPath();
        if (f.exists()) {
            config = NElements.of().json()
                    .setNtf(false)
                    .parse(f, RemoteMysqlConfig.class);
            return this;
        }
        throw new NoSuchElementException("config not found : " + name);
    }

    public RemoteMysqlConfigService removeConfig() {
        NPath f = getConfigPath();
        f.delete();
        return this;
    }

    public RemoteMysqlConfigService write(PrintStream out) {
        NElements.of().json().setValue(getConfig())
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

    public RemoteMysqlDatabaseConfigService getDatabase(String dbName, NOpenMode action) {
        dbName = MysqlUtils.toValidFileName(dbName, "default");
        RemoteMysqlDatabaseConfig a = getConfig().getDatabases().get(dbName);
        if (a == null) {
            switch (action) {
                case OPEN_OR_NULL:
                    return null;
                case OPEN_OR_ERROR:
                    throw new NIllegalArgumentException(NMsg.ofC("remote instance not found: %s@%s", dbName, getName()));
                case CREATE_OR_ERROR:
                case OPEN_OR_CREATE: {
                    a = new RemoteMysqlDatabaseConfig();
                    getConfig().getDatabases().put(dbName, a);
                    return new RemoteMysqlDatabaseConfigService(dbName, a, this);
                }
                default: {
                    throw new NIllegalArgumentException(NMsg.ofPlain("unexpected error"));
                }
            }
        }
        switch (action) {
            case CREATE_OR_ERROR: {
                throw new NIllegalArgumentException(NMsg.ofC("remote instance not found: %s@%s", dbName, getName()));
            }
            case OPEN_OR_ERROR:
            case OPEN_OR_NULL:
            case OPEN_OR_CREATE: {
                return new RemoteMysqlDatabaseConfigService(dbName, a, this);
            }
            default: {
                throw new NIllegalArgumentException(NMsg.ofPlain("unexpected error"));
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
