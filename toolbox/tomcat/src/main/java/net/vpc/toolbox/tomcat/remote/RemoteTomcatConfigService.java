package net.vpc.toolbox.tomcat.remote;

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsIOManager;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.tomcat.remote.config.RemoteTomcatAppConfig;
import net.vpc.toolbox.tomcat.remote.config.RemoteTomcatConfig;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import net.vpc.toolbox.tomcat.local.LocalTomcatConfigService;

public class RemoteTomcatConfigService extends RemoteTomcatServiceBase {

    public static final String REMOTE_CONFIG_EXT = ".remote-config";
    private String name;
    RemoteTomcatConfig config;
    NutsApplicationContext context;
    RemoteTomcat client;

    public RemoteTomcatConfigService(String name, RemoteTomcat client) {
        setName(name);
        this.client = client;
        this.context = client.context;
    }

    public RemoteTomcatConfigService(Path file, RemoteTomcat client) {
        this(
                file.getFileName().toString().substring(0, file.getFileName().toString().length() - LocalTomcatConfigService.LOCAL_CONFIG_EXT.length()),
                client
        );
    }

    public RemoteTomcatConfigService setName(String name) {
        this.name = TomcatUtils.toValidFileName(name, "default");
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RemoteTomcatConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    public Path getConfigPath() {
        return context.getConfigFolder().resolve(name + REMOTE_CONFIG_EXT);
    }

    public RemoteTomcatConfigService save() {
        NutsIOManager io = context.getWorkspace().io();
        Path f = getConfigPath();
        io.writeJson(config, f, true);
        return this;
    }

    public boolean existsConfig() {
        Path f = getConfigPath();
        return (Files.exists(f));
    }

    public void printStatus() {
        execRemoteNuts(
                "net.vpc.app.nuts.toolbox:tomcat",
                "--status",
                "--instance",
                getRemoteInstanceName()
        );
    }

    public void start(String[] redeploy, boolean deleteOutLog) {
        List<String> arg = new ArrayList<>();
        arg.add("net.vpc.app.nuts.toolbox:tomcat");
        arg.add("--start");
        arg.add("--instance");
        arg.add(getRemoteInstanceName());
        StringBuilder sb = new StringBuilder();
        for (String s : redeploy) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(s);
        }
        if (sb.length() > 0) {
            arg.add("--deploy");
            arg.add(sb.toString());
        }
        if (deleteOutLog) {
            arg.add("--deleteOutLog");
        }
        execRemoteNuts(arg.toArray(new String[0]));
    }

    public void shutdown() {
        execRemoteNuts(
                "net.vpc.app.nuts.toolbox:tomcat",
                "--stop",
                "--instance",
                getRemoteInstanceName()
        );
    }

    public String getRemoteInstanceName() {
        String n = getConfig().getRemoteName();
        return StringUtils.isEmpty(n) ? "default" : n;
    }

    public void restart(String[] redeploy, boolean deleteOutLog) {
        List<String> arg = new ArrayList<>();
        arg.add("net.vpc.app.nuts.toolbox:tomcat");
        arg.add("restart");
        arg.add("--instance");
        arg.add(getRemoteInstanceName());
        StringBuilder sb = new StringBuilder();
        for (String s : redeploy) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(s);
        }
        if (sb.length() > 0) {
            arg.add("--deploy");
            arg.add(sb.toString());
        }
        if (deleteOutLog) {
            arg.add("--deleteOutLog");
        }
        execRemoteNuts(arg.toArray(new String[0]));
    }

    public RemoteTomcatConfigService loadConfig() {
        if (name == null) {
            throw new NutsExecutionException("Missing config name", 2);
        }
        Path f = getConfigPath();
        if (Files.exists(f)) {
            NutsIOManager io = context.getWorkspace().io();
            config = io.readJson(f, RemoteTomcatConfig.class);
            return this;
        }
        throw new NoSuchElementException("Config not found : " + name);
    }

    public RemoteTomcatConfigService remove() {
        Path f = getConfigPath();
        try {
            Files.delete(f);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return this;
    }

    public RemoteTomcatConfigService write(PrintStream out) {
        NutsIOManager jsonSerializer = context.getWorkspace().io();
        PrintWriter w = new PrintWriter(out);
        jsonSerializer.writeJson(getConfig(), new PrintWriter(out), true);
        w.flush();
        return this;
    }

    public RemoteTomcatConfigService setConfig(RemoteTomcatConfig config) {
        this.config = config;
        return this;
    }

    public RemoteTomcatAppConfigService getApp(String appName) {
        return getAppOrError(appName);
    }

    public RemoteTomcatAppConfigService getAppOrNull(String appName) {
        RemoteTomcatAppConfig a = getConfig().getApps().get(appName);
        if (a == null) {
            return null;
        }
        return new RemoteTomcatAppConfigService(appName, a, this);
    }

    public RemoteTomcatAppConfigService getAppOrError(String appName) {
        RemoteTomcatAppConfig a = getConfig().getApps().get(appName);
        if (a == null) {
            throw new NutsExecutionException("App not found :" + appName, 2);
        }
        return new RemoteTomcatAppConfigService(appName, a, this);
    }

    public RemoteTomcatAppConfigService getAppOrCreate(String appName) {
        RemoteTomcatAppConfig a = getConfig().getApps().get(appName);
        if (a == null) {
            a = new RemoteTomcatAppConfig();
            getConfig().getApps().put(appName, a);
        }
        return new RemoteTomcatAppConfigService(appName, a, this);
    }

    public void deleteOutLog() {
        execRemoteNuts(
                "net.vpc.app.nuts.toolbox:tomcat",
                "--deleteOutLog",
                "--instance",
                getRemoteInstanceName()
        );
    }

    public List<RemoteTomcatAppConfigService> getApps() {
        List<RemoteTomcatAppConfigService> a = new ArrayList<>();
        for (String s : getConfig().getApps().keySet()) {
            a.add(new RemoteTomcatAppConfigService(s, getConfig().getApps().get(s), this));
        }
        return a;
    }

    public void execRemoteNuts(String... cmd) {
        RemoteTomcatConfig cconfig = getConfig();
        List<String> cmdList = new ArrayList<>(Arrays.asList(
                "nsh",
                "ssh",
                "--nuts"
        ));
        cmdList.add("--verbose");
        cmdList.add(this.config.getServer());
        cmdList.addAll(Arrays.asList(cmd));
        context.getWorkspace().exec()
                .session(context.getSession())
                .command(cmdList)
                .failFast()
                .run();

    }
}
