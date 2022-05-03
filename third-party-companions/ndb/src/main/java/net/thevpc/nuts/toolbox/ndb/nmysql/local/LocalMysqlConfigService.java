package net.thevpc.nuts.toolbox.ndb.nmysql.local;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.toolbox.ndb.nmysql.NMySqlConfigVersions;
import net.thevpc.nuts.toolbox.ndb.nmysql.local.config.LocalMysqlDatabaseConfig;
import net.thevpc.nuts.toolbox.ndb.nmysql.local.config.LocalMysqlConfig;

import java.io.*;
import java.util.*;

import net.thevpc.nuts.toolbox.ndb.nmysql.util.MysqlUtils;

public class LocalMysqlConfigService {

    public static final String SERVER_CONFIG_EXT = ".local-config";
    private String name;
    private LocalMysqlConfig config;
    private NutsApplicationContext context;
    private NutsDefinition catalinaNutsDefinition;
    private String catalinaVersion;
    private NutsPath sharedConfigFolder;

    public LocalMysqlConfigService(NutsPath file, NutsApplicationContext context) {
        this(
                file.getName().toString().substring(0, file.getName().length() - LocalMysqlConfigService.SERVER_CONFIG_EXT.length()),
                context
        );
        loadConfig();
    }

    public LocalMysqlConfigService(String name, NutsApplicationContext context) {
        setName(name);
        this.context = context;
        sharedConfigFolder = getContext().getVersionFolder(NutsStoreLocation.CONFIG, NMySqlConfigVersions.CURRENT);
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
        NutsPath f = getServerConfigPath();
        NutsElements.of(context.getSession()).setNtf(false).json().setValue(config).print(f);
        return this;
    }

    public boolean existsConfig() {
        NutsPath f = getServerConfigPath();
        return f.exists();
    }

    private NutsPath getServerConfigPath() {
        return sharedConfigFolder.resolve(getName() + SERVER_CONFIG_EXT);
    }

    public String[] parseApps(String[] args) {
        List<String> apps = new ArrayList<>();
        if (args != null) {
            for (String arg : args) {
                if (!NutsBlankable.isBlank(arg)) {
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
        NutsPath f = getServerConfigPath();
        NutsSession session = context.getSession();
        if (f.exists()) {
            config = NutsElements.of(session).json().parse(f, LocalMysqlConfig.class);
            return this;
        } else if ("default".equals(name)) {
            //auto create default config
            config = new LocalMysqlConfig();
            saveConfig();
            return this;
        }
        throw new NutsIllegalArgumentException(session,NutsMessage.ofCstyle("no such mysql config : %s",name));
    }

    public LocalMysqlConfigService removeConfig() {
        getServerConfigPath().delete();
        return this;
    }

    public LocalMysqlConfigService write(PrintStream out) {
        NutsSession session = context.getSession();
        NutsElements.of(session).json().setValue(getConfig()).setNtf(false).print(out);
        return this;
    }

    public LocalMysqlConfigService setConfig(LocalMysqlConfig config) {
        this.config = config;
        return this;
    }

    public LocalMysqlDatabaseConfigService getDatabase(String dbName, NutsOpenMode action) {
        dbName = MysqlUtils.toValidFileName(dbName, "default");
        LocalMysqlDatabaseConfig a = getConfig().getDatabases().get(dbName);
        if (a == null) {
            switch (action) {
                case OPEN_OR_NULL:
                    return null;
                case OPEN_OR_ERROR:
                    throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.ofCstyle("local instance not found:%s@%s" ,dbName, getName()));
                case CREATE_OR_ERROR:
                case OPEN_OR_CREATE: {
                    a = new LocalMysqlDatabaseConfig();
                    getConfig().getDatabases().put(dbName, a);
                    return new LocalMysqlDatabaseConfigService(dbName, a, this);
                }
                default: {
                    throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.ofPlain("unexpected error"));
                }
            }
        }
        switch (action) {
            case CREATE_OR_ERROR: {
                throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.ofCstyle("local instance not found:%s@%s",dbName,getName()));
            }
            case OPEN_OR_ERROR:
            case OPEN_OR_NULL:
            case OPEN_OR_CREATE: {
                return new LocalMysqlDatabaseConfigService(dbName, a, this);
            }
            default: {
                throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.ofPlain("unexpected error"));
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

    public NutsApplicationContext getContext() {
        return context;
    }

    public String getMysqlCommand() {
        String s = getConfig().getMysqlCommand();
        if (NutsBlankable.isBlank(s)) {
            s = "mysql";
        }
        return s;
    }

    public String getMysqldumpCommand() {
        String s = getConfig().getMysqldumpCommand();
        if (NutsBlankable.isBlank(s)) {
            s = "mysqldump";
        }
        return s;
    }
}
