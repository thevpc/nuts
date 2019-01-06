package net.vpc.toolbox.mysql.local;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.io.*;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.mysql.local.config.LocalMysqlDatabaseConfig;
import net.vpc.toolbox.mysql.local.config.LocalMysqlConfig;

import java.io.*;
import java.util.*;

public class LocalMysqlConfigService {
    public static final String SERVER_CONFIG_EXT = ".local-config";
    private String name;
    private LocalMysql app;
    private LocalMysqlConfig config;
    private NutsApplicationContext context;
    private NutsDefinition catalinaNutsDefinition;
    private String catalinaVersion;

    public LocalMysqlConfigService(File file, LocalMysql app) {
        this(
                file.getName().substring(0, file.getName().length() - LocalMysqlConfigService.SERVER_CONFIG_EXT.length()),
                app
        );
        loadConfig();
    }

    public LocalMysqlConfigService(String name, LocalMysql app) {
        this.app = app;
        setName(name);
        this.context = app.getContext();
    }

    public LocalMysqlConfigService setName(String name) {
        this.name = FileUtils.toValidFileName(name, "default");
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
        NutsIOManager jsonSerializer = context.getWorkspace().getIOManager();
        File f = new File(context.getConfigFolder(), getName() + SERVER_CONFIG_EXT);
        f.getParentFile().mkdirs();
        try (FileWriter r = new FileWriter(f)) {
            jsonSerializer.writeJson(config, r, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public boolean existsConfig() {
        File f = new File(context.getConfigFolder(), getName() + SERVER_CONFIG_EXT);
        return (f.exists());
    }

    public String[] parseApps(String[] args) throws RuntimeIOException {
        List<String> apps = new ArrayList<>();
        if (args != null) {
            for (String arg : args) {
                if (!StringUtils.isEmpty(arg)) {
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
        File f = new File(context.getConfigFolder(), name + SERVER_CONFIG_EXT);
        if (f.exists()) {
            NutsIOManager jsonSerializer = context.getWorkspace().getIOManager();
            try (FileReader r = new FileReader(f)) {
                config = jsonSerializer.readJson(r, LocalMysqlConfig.class);
                return this;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if ("default".equals(name)) {
            //auto create default config
            config = new LocalMysqlConfig();
            saveConfig();
            return this;
        }
        throw new NoSuchElementException("Config not found : " + name);
    }

    public LocalMysqlConfigService removeConfig() {
        File f = new File(context.getConfigFolder(), getName() + SERVER_CONFIG_EXT);
        f.delete();
        return this;
    }

    public LocalMysqlConfigService write(PrintStream out) {
        context.getWorkspace().getIOManager().writeJson(getConfig(), out,true);
        return this;
    }

    public LocalMysqlConfigService setConfig(LocalMysqlConfig config) {
        this.config = config;
        return this;
    }


    public LocalMysqlDatabaseConfigService getDatabase(String dbName) {
        return getDatabaseOrError(dbName);
    }

    public LocalMysqlDatabaseConfigService getDatabaseOrNull(String dbName) {
        dbName = FileUtils.toValidFileName(dbName, "default");
        LocalMysqlDatabaseConfig a = getConfig().getDatabases().get(dbName);
        if (a == null) {
            return null;
        }
        return new LocalMysqlDatabaseConfigService(dbName, a, this);
    }

    public LocalMysqlDatabaseConfigService getDatabaseOrError(String appName) {
        LocalMysqlDatabaseConfigService a = getDatabaseOrNull(appName);
        if (a == null) {
            throw new NutsExecutionException("Database not found :" + appName,2);
        }
        return a;
    }

    public LocalMysqlDatabaseConfigService getDatabaseOrCreate(String dbName) {
        dbName = FileUtils.toValidFileName(dbName, "default");
        LocalMysqlDatabaseConfigService a = getDatabaseOrNull(dbName);
        if (a == null) {
            LocalMysqlDatabaseConfig c = new LocalMysqlDatabaseConfig();
            getConfig().getDatabases().put(dbName, c);
            a = getDatabaseOrNull(dbName);
        }
        return a;
    }

    public List<LocalMysqlDatabaseConfigService> getDatabases() {
        List<LocalMysqlDatabaseConfigService> a = new ArrayList<>();
        for (String s : getConfig().getDatabases().keySet()) {
            a.add(new LocalMysqlDatabaseConfigService(s, getConfig().getDatabases().get(s), this));
        }
        return a;
    }


    public LocalMysql getMysqlServer() {
        return app;
    }

    public NutsApplicationContext getContext() {
        return context;
    }


    public String getMysqlCommand() {
        String s = getConfig().getMysqlCommand();
        if (StringUtils.isEmpty(s)) {
            s = "mysql";
        }
        return s;
    }

    public String getMysqldumpCommand() {
        String s = getConfig().getMysqldumpCommand();
        if (StringUtils.isEmpty(s)) {
            s = "mysqldump";
        }
        return s;
    }
}
