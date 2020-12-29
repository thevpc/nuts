package net.thevpc.nuts.toolbox.nmysql.local;

import net.thevpc.common.io.FileUtils;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsContentType;
import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.NutsExecutionException;
import net.thevpc.common.strings.StringUtils;
import net.thevpc.nuts.toolbox.nmysql.local.config.LocalMysqlDatabaseConfig;
import net.thevpc.nuts.toolbox.nmysql.local.config.LocalMysqlConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LocalMysqlConfigService {

    public static final String SERVER_CONFIG_EXT = ".local-config";
    private String name;
    private LocalMysql app;
    private LocalMysqlConfig config;
    private NutsApplicationContext context;
    private NutsDefinition catalinaNutsDefinition;
    private String catalinaVersion;

    public LocalMysqlConfigService(Path file, LocalMysql app) {
        this(
                file.getFileName().toString().substring(0, file.getFileName().toString().length() - LocalMysqlConfigService.SERVER_CONFIG_EXT.length()),
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
        Path f = getServerConfigPath();
        context.getWorkspace().formats().element().setContentType(NutsContentType.JSON).setValue(config).print(f);
        return this;
    }

    public boolean existsConfig() {
        Path f = getServerConfigPath();
        return Files.exists(f);
    }

    private Path getServerConfigPath() {
        return Paths.get(context.getSharedConfigFolder()).resolve(getName() + SERVER_CONFIG_EXT);
    }

    public String[] parseApps(String[] args) {
        List<String> apps = new ArrayList<>();
        if (args != null) {
            for (String arg : args) {
                if (!StringUtils.isBlank(arg)) {
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
            config = context.getWorkspace().formats().element().setContentType(NutsContentType.JSON).parse(f, LocalMysqlConfig.class);
            return this;
        } else if ("default".equals(name)) {
            //auto create default config
            config = new LocalMysqlConfig();
            saveConfig();
            return this;
        }
        throw new NoSuchElementException("Config not found : " + name);
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
        context.getWorkspace().formats().element().setContentType(NutsContentType.JSON).setValue(getConfig()).print(out);
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
            throw new NutsExecutionException(context.getWorkspace(), "Database not found :" + appName, 2);
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
        if (StringUtils.isBlank(s)) {
            s = "mysql";
        }
        return s;
    }

    public String getMysqldumpCommand() {
        String s = getConfig().getMysqldumpCommand();
        if (StringUtils.isBlank(s)) {
            s = "mysqldump";
        }
        return s;
    }
}
