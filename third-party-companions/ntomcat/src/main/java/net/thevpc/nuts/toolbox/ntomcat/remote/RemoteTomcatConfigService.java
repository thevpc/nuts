package net.thevpc.nuts.toolbox.ntomcat.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.toolbox.ntomcat.NTomcatConfigVersions;
import net.thevpc.nuts.toolbox.ntomcat.local.LocalTomcatConfigService;
import net.thevpc.nuts.toolbox.ntomcat.remote.config.RemoteTomcatAppConfig;
import net.thevpc.nuts.toolbox.ntomcat.remote.config.RemoteTomcatConfig;
import net.thevpc.nuts.toolbox.ntomcat.util.NamedItemNotFoundException;
import net.thevpc.nuts.toolbox.ntomcat.util.TomcatUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RemoteTomcatConfigService extends RemoteTomcatServiceBase {
    public static final String REMOTE_CONFIG_EXT = ".remote-config";
    private static final String NTOMCAT = "net.thevpc.nuts.toolbox:ntomcat";
    RemoteTomcatConfig config;
    NutsApplicationContext context;
    RemoteTomcat client;
    NutsPath sharedConfigFolder;
    private String name;

    public RemoteTomcatConfigService(String name, RemoteTomcat client) {
        setName(name);
        this.client = client;
        this.context = client.context;
        sharedConfigFolder = client.getContext().getVersionFolder(NutsStoreLocation.CONFIG, NTomcatConfigVersions.CURRENT);
    }

    public RemoteTomcatConfigService(NutsPath file, RemoteTomcat client) {
        this(
                file.getName().substring(0, file.getName().length() - LocalTomcatConfigService.LOCAL_CONFIG_EXT.length()),
                client
        );
    }

    @Override
    public RemoteTomcatConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    @Override
    public String getName() {
        return name;
    }

    public RemoteTomcatConfigService setName(String name) {
        this.name = TomcatUtils.toValidFileName(name, "default");
        return this;
    }

    @Override
    public RemoteTomcatConfigService print(NutsPrintStream out) {
        NutsSession session = context.getSession();
        NutsElements.of(session).json().setValue(getConfig()).print(out);
        out.flush();
        return this;
    }

    @Override
    public RemoteTomcatConfigService remove() {
        NutsPath f = getConfigPath();
        f.delete();
        return this;
    }

    public RemoteTomcatConfigService setConfig(RemoteTomcatConfig config) {
        this.config = config;
        return this;
    }

    public NutsPath getConfigPath() {
        return sharedConfigFolder.resolve(name + REMOTE_CONFIG_EXT);
    }

    public RemoteTomcatConfigService save() {
        NutsPath f = getConfigPath();
        NutsSession session = context.getSession();
        NutsElements.of(session).json().setValue(config).print(f);
        return this;
    }

    public boolean existsConfig() {
        NutsPath f = getConfigPath();
        return (f.exists());
    }

    public void printStatus() {
        execRemoteNuts(
                NTOMCAT,
                "--status",
                "--name",
                getRemoteInstanceName()
        );
    }

    public void start(String[] redeploy, boolean deleteOutLog) {
        List<String> arg = new ArrayList<>();
        arg.add(NTOMCAT);
        arg.add("--start");
        arg.add("--name");
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
        execRemoteNuts(NTOMCAT,
                "--stop",
                "--name",
                getRemoteInstanceName()
        );
    }

    public String getRemoteInstanceName() {
        String n = getConfig().getRemoteName();
        return NutsBlankable.isBlank(n) ? "default" : n;
    }

    public void restart(String[] redeploy, boolean deleteOutLog) {
        List<String> arg = new ArrayList<>();
        arg.add(NTOMCAT);
        arg.add("restart");
        arg.add("--name");
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
        NutsSession session = context.getSession();
        if (name == null) {
            throw new NutsExecutionException(session, NutsMessage.cstyle("missing instance name"), 2);
        }
        NutsPath f = getConfigPath();
        if (f.exists()) {
            config = NutsElements.of(session).json().parse(f, RemoteTomcatConfig.class);
            return this;
        }
        throw new NamedItemNotFoundException("instance not found : " + getName(), getName());
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
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("app not found :%s", appName), 2);
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
        execRemoteNuts(NTOMCAT,
                "--deleteOutLog",
                "--name",
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
                "-c",
                "ssh"
        ));
        cmdList.add(this.config.getServer());
        cmdList.add("nuts");
        cmdList.add("--bot");
        cmdList.addAll(Arrays.asList(cmd));
        context.getSession().exec()
                .addCommand(cmdList)
                .setFailFast(true)
                .run();

    }
}
