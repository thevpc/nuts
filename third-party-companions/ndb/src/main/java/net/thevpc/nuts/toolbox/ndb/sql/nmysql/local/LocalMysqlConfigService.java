package net.thevpc.nuts.toolbox.ndb.sql.nmysql.local;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.NMySqlConfigVersions;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.local.config.LocalMysqlDatabaseConfig;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.local.config.LocalMysqlConfig;

import java.io.*;
import java.util.*;

import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.MysqlUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

public class LocalMysqlConfigService {

    public static final String SERVER_CONFIG_EXT = ".local-config";
    private String name;
    private LocalMysqlConfig config;
    private NSession session;
    private NDefinition catalinaNDefinition;
    private String catalinaVersion;
    private NPath sharedConfigFolder;

    public LocalMysqlConfigService(NPath file, NSession session) {
        this(
                file.getName().toString().substring(0, file.getName().length() - LocalMysqlConfigService.SERVER_CONFIG_EXT.length()),
                session
        );
        loadConfig();
    }

    public LocalMysqlConfigService(String name, NSession session) {
        setName(name);
        this.session = session;
        sharedConfigFolder = NApp.of().getVersionFolder(NStoreType.CONF, NMySqlConfigVersions.CURRENT);
    }

    public LocalMysqlConfigService setName(String name) {
        this.name = MysqlUtils.toValidFileName(name, "default");
        return this;
    }

    public String getName() {
        return name;
    }

    public LocalMysqlConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    public LocalMysqlConfigService saveConfig() {
        NPath f = getServerConfigPath();
        NElements.of().setNtf(false).json().setValue(config).print(f);
        return this;
    }

    public boolean existsConfig() {
        NPath f = getServerConfigPath();
        return f.exists();
    }

    private NPath getServerConfigPath() {
        return sharedConfigFolder.resolve(getName() + SERVER_CONFIG_EXT);
    }

    public String[] parseApps(String[] args) {
        List<String> apps = new ArrayList<>();
        if (args != null) {
            for (String arg : args) {
                if (!NBlankable.isBlank(arg)) {
                    for (String s : arg.split("[, ]")) {
                        if (!s.isEmpty()) {
                            apps.add(s);
                        }
                    }
                }
            }
        }
        return apps.toArray(new String[0]);
    }

    public LocalMysqlConfigService loadConfig() {
        String name = getName();
        NPath f = getServerConfigPath();
        if (f.exists()) {
            config = NElements.of().json().parse(f, LocalMysqlConfig.class);
            return this;
        } else if ("default".equals(name)) {
            //auto create default config
            config = new LocalMysqlConfig();
            saveConfig();
            return this;
        }
        throw new NIllegalArgumentException(NMsg.ofC("no such mysql config : %s",name));
    }

    public LocalMysqlConfigService removeConfig() {
        getServerConfigPath().delete();
        return this;
    }

    public LocalMysqlConfigService write(PrintStream out) {
        NElements.of().json().setValue(getConfig()).setNtf(false).print(out);
        return this;
    }

    public LocalMysqlConfigService setConfig(LocalMysqlConfig config) {
        this.config = config;
        return this;
    }

    public LocalMysqlDatabaseConfigService getDatabase(String dbName, NOpenMode action) {
        dbName = MysqlUtils.toValidFileName(dbName, "default");
        LocalMysqlDatabaseConfig a = getConfig().getDatabases().get(dbName);
        if (a == null) {
            switch (action) {
                case OPEN_OR_NULL:
                    return null;
                case OPEN_OR_ERROR:
                    throw new NIllegalArgumentException(NMsg.ofC("local instance not found:%s@%s" ,dbName, getName()));
                case CREATE_OR_ERROR:
                case OPEN_OR_CREATE: {
                    a = new LocalMysqlDatabaseConfig();
                    getConfig().getDatabases().put(dbName, a);
                    return new LocalMysqlDatabaseConfigService(dbName, a, this);
                }
                default: {
                    throw new NIllegalArgumentException(NMsg.ofPlain("unexpected error"));
                }
            }
        }
        switch (action) {
            case CREATE_OR_ERROR: {
                throw new NIllegalArgumentException(NMsg.ofC("local instance not found:%s@%s",dbName,getName()));
            }
            case OPEN_OR_ERROR:
            case OPEN_OR_NULL:
            case OPEN_OR_CREATE: {
                return new LocalMysqlDatabaseConfigService(dbName, a, this);
            }
            default: {
                throw new NIllegalArgumentException(NMsg.ofPlain("unexpected error"));
            }
        }
    }


    public List<LocalMysqlDatabaseConfigService> getDatabases() {
        List<LocalMysqlDatabaseConfigService> a = new ArrayList<>();
        for (String s : getConfig().getDatabases().keySet()) {
            a.add(new LocalMysqlDatabaseConfigService(s, getConfig().getDatabases().get(s), this));
        }
        return a;
    }

    public NSession getSession() {
        return session;
    }

    public String getMysqlCommand() {
        String s = getConfig().getMysqlCommand();
        if (NBlankable.isBlank(s)) {
            s = "mysql";
        }
        return s;
    }

    public String getMysqldumpCommand() {
        String s = getConfig().getMysqldumpCommand();
        if (NBlankable.isBlank(s)) {
            s = "mysqldump";
        }
        return s;
    }
}
