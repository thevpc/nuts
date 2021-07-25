package net.thevpc.nuts.toolbox.ndb.nmysql.local;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.ndb.nmysql.NMySqlConfigVersions;
import net.thevpc.nuts.toolbox.ndb.nmysql.local.config.LocalMysqlDatabaseConfig;
import net.thevpc.nuts.toolbox.ndb.nmysql.local.config.LocalMysqlConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import net.thevpc.nuts.toolbox.ndb.nmysql.util.MysqlUtils;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;

public class LocalMysqlConfigService {

    public static final String SERVER_CONFIG_EXT = ".local-config";
    private String name;
    private LocalMysqlConfig config;
    private NutsApplicationContext context;
    private NutsDefinition catalinaNutsDefinition;
    private String catalinaVersion;
    private Path sharedConfigFolder;

    public LocalMysqlConfigService(Path file, NutsApplicationContext context) {
        this(
                file.getFileName().toString().substring(0, file.getFileName().toString().length() - LocalMysqlConfigService.SERVER_CONFIG_EXT.length()),
                context
        );
        loadConfig();
    }

    public LocalMysqlConfigService(String name, NutsApplicationContext context) {
        setName(name);
        this.context = context;
        sharedConfigFolder = Paths.get(getContext().getVersionFolderFolder(NutsStoreLocation.CONFIG, NMySqlConfigVersions.CURRENT));
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
        Path f = getServerConfigPath();
        context.getWorkspace().elem().setContentType(NutsContentType.JSON).setValue(config).print(f);
        return this;
    }

    public boolean existsConfig() {
        Path f = getServerConfigPath();
        return Files.exists(f);
    }

    private Path getServerConfigPath() {
        return sharedConfigFolder.resolve(getName() + SERVER_CONFIG_EXT);
    }

    public String[] parseApps(String[] args) {
        List<String> apps = new ArrayList<>();
        if (args != null) {
            for (String arg : args) {
                if (!NdbUtils.isBlank(arg)) {
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
        Path f = getServerConfigPath();
        if (Files.exists(f)) {
            config = context.getWorkspace().elem().setContentType(NutsContentType.JSON).parse(f, LocalMysqlConfig.class);
            return this;
        } else if ("default".equals(name)) {
            //auto create default config
            config = new LocalMysqlConfig();
            saveConfig();
            return this;
        }
        throw new NutsIllegalArgumentException(context.getSession(),NutsMessage.cstyle("no such mysql config : %s",name));
    }

    public LocalMysqlConfigService removeConfig() {
        try {
            Files.delete(getServerConfigPath());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return this;
    }

    public LocalMysqlConfigService write(PrintStream out) {
        context.getWorkspace().elem().setContentType(NutsContentType.JSON).setValue(getConfig()).print(out);
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
                    throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("local instance not found:%s@%s" ,dbName, getName()));
                case CREATE_OR_ERROR:
                case OPEN_OR_CREATE: {
                    a = new LocalMysqlDatabaseConfig();
                    getConfig().getDatabases().put(dbName, a);
                    return new LocalMysqlDatabaseConfigService(dbName, a, this);
                }
                default: {
                    throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("unexpected error"));
                }
            }
        }
        switch (action) {
            case CREATE_OR_ERROR: {
                throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("local instance not found:%s@%s",dbName,getName()));
            }
            case OPEN_OR_ERROR:
            case OPEN_OR_NULL:
            case OPEN_OR_CREATE: {
                return new LocalMysqlDatabaseConfigService(dbName, a, this);
            }
            default: {
                throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("unexpected error"));
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
        if (NdbUtils.isBlank(s)) {
            s = "mysql";
        }
        return s;
    }

    public String getMysqldumpCommand() {
        String s = getConfig().getMysqldumpCommand();
        if (NdbUtils.isBlank(s)) {
            s = "mysqldump";
        }
        return s;
    }
}
