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

public class LocalMysqlConfigService {

    public static final String SERVER_CONFIG_EXT = ".local-config";
    private String name;
    private LocalMysqlConfig config;
    private NApplicationContext context;
    private NDefinition catalinaNDefinition;
    private String catalinaVersion;
    private NPath sharedConfigFolder;

    public LocalMysqlConfigService(NPath file, NApplicationContext context) {
        this(
                file.getName().toString().substring(0, file.getName().length() - LocalMysqlConfigService.SERVER_CONFIG_EXT.length()),
                context
        );
        loadConfig();
    }

    public LocalMysqlConfigService(String name, NApplicationContext context) {
        setName(name);
        this.context = context;
        sharedConfigFolder = getContext().getVersionFolder(NStoreLocation.CONFIG, NMySqlConfigVersions.CURRENT);
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
        NElements.of(context.getSession()).setNtf(false).json().setValue(config).print(f);
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
        NSession session = context.getSession();
        if (f.exists()) {
            config = NElements.of(session).json().parse(f, LocalMysqlConfig.class);
            return this;
        } else if ("default".equals(name)) {
            //auto create default config
            config = new LocalMysqlConfig();
            saveConfig();
            return this;
        }
        throw new NIllegalArgumentException(session, NMsg.ofC("no such mysql config : %s",name));
    }

    public LocalMysqlConfigService removeConfig() {
        getServerConfigPath().delete();
        return this;
    }

    public LocalMysqlConfigService write(PrintStream out) {
        NSession session = context.getSession();
        NElements.of(session).json().setValue(getConfig()).setNtf(false).print(out);
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
                    throw new NIllegalArgumentException(context.getSession(), NMsg.ofC("local instance not found:%s@%s" ,dbName, getName()));
                case CREATE_OR_ERROR:
                case OPEN_OR_CREATE: {
                    a = new LocalMysqlDatabaseConfig();
                    getConfig().getDatabases().put(dbName, a);
                    return new LocalMysqlDatabaseConfigService(dbName, a, this);
                }
                default: {
                    throw new NIllegalArgumentException(context.getSession(), NMsg.ofPlain("unexpected error"));
                }
            }
        }
        switch (action) {
            case CREATE_OR_ERROR: {
                throw new NIllegalArgumentException(context.getSession(), NMsg.ofC("local instance not found:%s@%s",dbName,getName()));
            }
            case OPEN_OR_ERROR:
            case OPEN_OR_NULL:
            case OPEN_OR_CREATE: {
                return new LocalMysqlDatabaseConfigService(dbName, a, this);
            }
            default: {
                throw new NIllegalArgumentException(context.getSession(), NMsg.ofPlain("unexpected error"));
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

    public NApplicationContext getContext() {
        return context;
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
