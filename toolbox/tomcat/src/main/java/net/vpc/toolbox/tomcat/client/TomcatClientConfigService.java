package net.vpc.toolbox.tomcat.client;

import net.vpc.app.nuts.JsonSerializer;
import net.vpc.common.io.RuntimeIOException;
import net.vpc.toolbox.tomcat.client.config.TomcatClientAppConfig;
import net.vpc.toolbox.tomcat.client.config.TomcatClientConfig;
import net.vpc.toolbox.tomcat.util.NutsContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class TomcatClientConfigService {
    public static final String CLIENT_CONFIG_EXT = ".client-config";
    private String name;
    TomcatClientConfig config;
    NutsContext context;
    TomcatClient client;

    public TomcatClientConfigService(String name, TomcatClient client) {
        setName(name);
        this.client = client;
        this.context = client.context;
    }

    public TomcatClientConfigService setName(String name) {
        this.name=TomcatUtils.toValidFileName(name,"default");
        return this;
    }

    public String getName() {
        return name;
    }

    public TomcatClientConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }


    public TomcatClientConfigService saveConfig() {
        JsonSerializer jsonSerializer = context.ws.getExtensionManager().createJsonSerializer();
        File f = new File(context.configFolder, name + CLIENT_CONFIG_EXT);
        f.getParentFile().mkdirs();
        try (FileWriter r = new FileWriter(f)) {
            jsonSerializer.write(config, r, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public boolean existsConfig() {
        File f = new File(context.configFolder, name + CLIENT_CONFIG_EXT);
        return (f.exists());
    }

    public void printStatus() {
        execRemoteNuts(
                "net.vpc.app.nuts.toolbox:tomcat",
                "--status",
                "--instance",
                getName()
        );
    }

    public int start(String[] redeploy,boolean deleteOutLog) throws RuntimeIOException {
        List<String> arg=new ArrayList<>();
        arg.add("net.vpc.app.nuts.toolbox:tomcat");
        arg.add("--start");
        arg.add("--instance");
        arg.add(getName());
        StringBuilder sb=new StringBuilder();
        for (String s : redeploy) {
            if(sb.length()>0){
                sb.append(",");
            }
            sb.append(s);
        }
        if(sb.length()>0) {
            arg.add("--deploy");
            arg.add(sb.toString());
        }
        if(deleteOutLog) {
            arg.add("--deleteOutLog");
        }
        return execRemoteNuts(arg.toArray(new String[arg.size()]));
    }

    public int shutdown() {
        return execRemoteNuts(
                "net.vpc.app.nuts.toolbox:tomcat",
                "--stop",
                "--instance",
                getName()
        );
    }

    public int restart(String[] redeploy,boolean deleteOutLog) {
        List<String> arg=new ArrayList<>();
        arg.add("net.vpc.app.nuts.toolbox:tomcat");
        arg.add("--restart");
        arg.add(getName());
        StringBuilder sb=new StringBuilder();
        for (String s : redeploy) {
            if(sb.length()>0){
                sb.append(",");
            }
            sb.append(s);
        }
        if(sb.length()>0) {
            arg.add("--deploy");
            arg.add(sb.toString());
        }
        if(deleteOutLog) {
            arg.add("--deleteOutLog");
        }
        return execRemoteNuts(arg.toArray(new String[arg.size()]));
    }


    public TomcatClientConfigService loadConfig() {
        if (name == null) {
            throw new IllegalArgumentException("Missing config name");
        }
        File f = new File(context.configFolder, name + CLIENT_CONFIG_EXT);
        if (f.exists()) {
            JsonSerializer jsonSerializer = context.ws.getExtensionManager().createJsonSerializer();
            try (FileReader r = new FileReader(f)) {
                TomcatClientConfig i = jsonSerializer.read(r, TomcatClientConfig.class);
                config = i;
                return this;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new NoSuchElementException("Config not found : " + name);
    }

    public TomcatClientConfigService removeConfig() {
        File f = new File(context.configFolder, name + CLIENT_CONFIG_EXT);
        f.delete();
        return this;
    }

    public TomcatClientConfigService write(PrintStream out) {
        JsonSerializer jsonSerializer = context.ws.getExtensionManager().createJsonSerializer();
        PrintWriter w = new PrintWriter(out);
        jsonSerializer.write(getConfig(), new PrintWriter(out), true);
        w.flush();
        return this;
    }

    public TomcatClientConfigService setConfig(TomcatClientConfig config) {
        this.config = config;
        return this;
    }


    public TomcatClientAppConfigService getApp(String appName) {
        return getAppOrError(appName);
    }

    public TomcatClientAppConfigService getAppOrNull(String appName) {
        TomcatClientAppConfig a = getConfig().getApps().get(appName);
        if (a == null) {
            return null;
        }
        return new TomcatClientAppConfigService(appName, a, this);
    }

    public TomcatClientAppConfigService getAppOrError(String appName) {
        TomcatClientAppConfig a = getConfig().getApps().get(appName);
        if (a == null) {
            throw new IllegalArgumentException("App not found :" + appName);
        }
        return new TomcatClientAppConfigService(appName, a, this);
    }

    public TomcatClientAppConfigService getAppOrCreate(String appName) {
        TomcatClientAppConfig a = getConfig().getApps().get(appName);
        if (a == null) {
            a = new TomcatClientAppConfig();
            getConfig().getApps().put(appName, a);
        }
        return new TomcatClientAppConfigService(appName, a, this);
    }


    public void deleteOutLog() {
        execRemoteNuts(
                "net.vpc.app.nuts.toolbox:tomcat",
                "--deleteOutLog",
                "--instance",
                getName()
        );
    }

    public List<TomcatClientAppConfigService> getApps() {
        List<TomcatClientAppConfigService> a = new ArrayList<>();
        for (String s : getConfig().getApps().keySet()) {
            a.add(new TomcatClientAppConfigService(s, getConfig().getApps().get(s), this));
        }
        return a;
    }

    public int execRemoteNuts(String... cmd) {
        TomcatClientConfig cconfig = getConfig();
        String serverPassword = TomcatUtils.isEmpty(cconfig.getServerPassword()) ? "" : cconfig.getServerPassword();
        String serverCertificate = TomcatUtils.isEmpty(cconfig.getServerCertificateFile()) ? "" : cconfig.getServerCertificateFile();
        List<String> cmdList = new ArrayList<>();
        cmdList.addAll(Arrays.asList(
                "nsh",
                "ssh",
                "--password",
                serverPassword,
                "--cert",
                serverCertificate,
                this.config.getServer(),
                "nuts"
        ));
        cmdList.addAll(Arrays.asList(cmd));
        return context.ws.exec(
                new String[]{

                }, null, null, context.session
        );

    }
}
