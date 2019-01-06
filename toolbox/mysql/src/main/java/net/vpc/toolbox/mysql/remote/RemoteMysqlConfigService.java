package net.vpc.toolbox.mysql.remote;

import net.vpc.app.nuts.JsonIO;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsIOManager;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.io.FileUtils;
import net.vpc.toolbox.mysql.remote.config.RemoteMysqlDatabaseConfig;
import net.vpc.toolbox.mysql.remote.config.RemoteMysqlConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class RemoteMysqlConfigService {
    public static final String CLIENT_CONFIG_EXT = ".remote-config";
    private String name;
    RemoteMysqlConfig config;
    NutsApplicationContext context;
    RemoteMysql client;

    public RemoteMysqlConfigService(String name, RemoteMysql client) {
        setName(name);
        this.client = client;
        this.context = client.context;
    }

    public RemoteMysqlConfigService setName(String name) {
        this.name= FileUtils.toValidFileName(name,"default");
        return this;
    }

    public String getName() {
        return name;
    }

    public RemoteMysqlConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }


    public RemoteMysqlConfigService saveConfig() {
        NutsIOManager jsonSerializer = context.getWorkspace().getIOManager();
        File f = new File(context.getConfigFolder(), name + CLIENT_CONFIG_EXT);
        f.getParentFile().mkdirs();
        try (FileWriter r = new FileWriter(f)) {
            jsonSerializer.writeJson(config, r, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public boolean existsConfig() {
        File f = new File(context.getConfigFolder(), name + CLIENT_CONFIG_EXT);
        return (f.exists());
    }

    public RemoteMysqlConfigService loadConfig() {
        if (name == null) {
            throw new NutsExecutionException("Missing config name",2);
        }
        File f = new File(context.getConfigFolder(), name + CLIENT_CONFIG_EXT);
        if (f.exists()) {
            NutsIOManager jsonSerializer = context.getWorkspace().getIOManager();
            try (FileReader r = new FileReader(f)) {
                config = jsonSerializer.readJson(r, RemoteMysqlConfig.class);
                return this;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new NoSuchElementException("Config not found : " + name);
    }

    public RemoteMysqlConfigService removeConfig() {
        File f = new File(context.getConfigFolder(), name + CLIENT_CONFIG_EXT);
        f.delete();
        return this;
    }

    public RemoteMysqlConfigService write(PrintStream out) {
        NutsIOManager jsonSerializer = context.getWorkspace().getIOManager();
        PrintWriter w = new PrintWriter(out);
        jsonSerializer.writeJson(getConfig(), new PrintWriter(out), true);
        w.flush();
        return this;
    }

    public RemoteMysqlConfigService setConfig(RemoteMysqlConfig config) {
        this.config = config;
        return this;
    }


    public RemoteMysqlDatabaseConfigService getDatabase(String appName) {
        return getDatabaseOrError(appName);
    }

    public RemoteMysqlDatabaseConfigService getAppOrNull(String appName) {
        RemoteMysqlDatabaseConfig a = getConfig().getDatabases().get(appName);
        if (a == null) {
            return null;
        }
        return new RemoteMysqlDatabaseConfigService(appName, a, this);
    }

    public RemoteMysqlDatabaseConfigService getDatabaseOrError(String appName) {
        RemoteMysqlDatabaseConfig a = getConfig().getDatabases().get(appName);
        if (a == null) {
            throw new NutsExecutionException("App not found :" + appName,2);
        }
        return new RemoteMysqlDatabaseConfigService(appName, a, this);
    }

    public RemoteMysqlDatabaseConfigService getDatabaseOrCreate(String appName) {
        RemoteMysqlDatabaseConfig a = getConfig().getDatabases().get(appName);
        if (a == null) {
            a = new RemoteMysqlDatabaseConfig();
            getConfig().getDatabases().put(appName, a);
        }
        return new RemoteMysqlDatabaseConfigService(appName, a, this);
    }


    public List<RemoteMysqlDatabaseConfigService> getApps() {
        List<RemoteMysqlDatabaseConfigService> a = new ArrayList<>();
        for (String s : getConfig().getDatabases().keySet()) {
            a.add(new RemoteMysqlDatabaseConfigService(s, getConfig().getDatabases().get(s), this));
        }
        return a;
    }

}
