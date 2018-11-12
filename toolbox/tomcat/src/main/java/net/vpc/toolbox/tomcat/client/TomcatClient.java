package net.vpc.toolbox.tomcat.client;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.toolbox.tomcat.client.config.TomcatClientConfig;
import net.vpc.toolbox.tomcat.util.NutsContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;
import net.vpc.toolbox.tomcat.util.UserCancelException;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TomcatClient {
    public NutsContext context;

    public TomcatClient(NutsWorkspace ws) {
        this(new NutsContext(ws));
    }

    public TomcatClient(NutsContext context) {
        this.context = context;
    }

    public int runArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "list":
                    return list(Arrays.copyOfRange(args, 1, args.length));
                case "add":
                case "set":
                    return add(Arrays.copyOfRange(args, 1, args.length));
                case "remove":
                    return remove(Arrays.copyOfRange(args, 1, args.length));
                case "start":
                    return restart(args, false, i + 1);
                case "stop":
                    return stop(Arrays.copyOfRange(args, 1, args.length));
                case "restart":
                    return restart(args, true, i + 1);
                case "install":
                    return install(Arrays.copyOfRange(args, 1, args.length));
                case "reset":
                    return reset();
                default:
                    throw new RuntimeException("Unsupported action " + args[i]);
            }
        }
        return 0;
    }

    public int list(String[] args) {
        boolean json = false;
        String instance = null;
        String app = null;
        String property = null;
        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("--json")) {
                json = true;
            } else if (args[j].equals("--instance")) {
                if (j + 1 < args.length && args[j + 1].startsWith("--")) {
                    j++;
                    instance = args[j];
                } else {
                    instance = "";
                }
            } else if (args[j].equals("--app")) {
                j++;
                app = args[j];
            } else if (args[j].equals("--property")) {
                j++;
                property = args[j];
            } else {
                throw new IllegalArgumentException("Unsupported " + args[j]);
            }
        }
        if (property == null) {
            if (app != null) {
                TomcatClientConfigService c = loadOrCreateTomcatConfig(instance);
                TomcatClientAppConfigService a = c.getApp(app);
                if (json) {
                    context.out.printf("[[%s]] :\n", a.getName());
                    a.write(context.out);
                    context.out.println();
                } else {
                    context.out.println(a.getName());
                }
            } else {
                for (TomcatClientConfigService tomcatConfig : listConfig()) {
                    if (json) {
                        context.out.printf("[[%s]] :\n", tomcatConfig.getName());
                        tomcatConfig.write(context.out);
                        context.out.println();
                    } else {
                        context.out.println(tomcatConfig.getName());
                    }
                }
            }
        } else {
            TomcatClientConfigService c = loadOrCreateTomcatConfig(instance);
            if (app != null) {
                context.out.printf("%s\n", TomcatUtils.getPropertyValue(c.getApp(app).getConfig(), property));
            } else {
                for (TomcatClientAppConfigService a : c.getApps()) {
                    context.out.printf("[%s] %s\n", TomcatUtils.getPropertyValue(a.getConfig(), property));
                }
            }
        }
        return 0;
    }

    private int add(String[] args) {
        TomcatClientConfigService c = null;
        String appName = null;
        String instanceName = null;
        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("--instance")) {
                j++;
                if(c==null){
                    instanceName = args[j];
                    c=loadOrCreateTomcatConfig(instanceName);
                }else{
                    throw new IllegalArgumentException("instance already defined");
                }
            }else if (args[j].equals("--server")) {
                j++;
                if(c==null){
                    c=loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setServer(args[j]);
            }else if (args[j].equals("--remote-instance")) {
                j++;
                if(c==null){
                    c=loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRemoteInstance(args[j]);
            }else if (args[j].equals("--remote-temp-path")) {
                j++;
                if(c==null){
                    c=loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRemoteTempPath(args[j]);
            } else if (args[j].equals("--cert")) {
                j++;
                if(c==null){
                    c=loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setServerCertificateFile(args[j]);
            } else if (args[j].equals("--password")) {
                j++;
                if(c==null){
                    c=loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setServerPassword(args[j]);
            } else if (args[j].equals("--app")) {
                j++;
                appName = args[j];
                if(c==null){
                    c=loadOrCreateTomcatConfig(null);
                }
                c.getAppOrCreate(appName);
            } else if (args[j].equals("--app.path")) {
                j++;
                String value = args[j];
                if(c==null){
                    c=loadOrCreateTomcatConfig(null);
                }
                TomcatClientAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setPath(value);
            } else if (args[j].equals("--app.version")) {
                j++;
                String value = args[j];
                if(c==null){
                    c=loadOrCreateTomcatConfig(null);
                }
                TomcatClientAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setVersionCommand(value);
            } else {
                throw new IllegalArgumentException("Unsupported " + args[j]);
            }
        }
        if(c==null){
            c=loadOrCreateTomcatConfig(null);
        }
        boolean ok=false;
        while (!ok) {
            try {
                ok = true;
                if (TomcatUtils.isEmpty(c.getConfig().getServer())) {
                    ok = false;
                    c.getConfig().setServer(context.readOrCancel("[instance=[[%s]]] Would you enter ==%s== value?","ssh://login@myserver",c.getName(),"--server"));
                }
                if (TomcatUtils.isEmpty(c.getConfig().getRemoteInstance())) {
                    ok = false;
                    c.getConfig().setRemoteInstance(context.readOrCancel("[instance=[[%s]]] Would you enter ==%s== value?","default",c.getName(),"--remote-instance"));
                }
                if (TomcatUtils.isEmpty(c.getConfig().getRemoteTempPath())) {
                    ok = false;
                    c.getConfig().setRemoteTempPath(context.readOrCancel("[instance=[[%s]]] Would you enter ==%s== value?","/tmp",c.getName(),"--remote-temp-path"));
                }
                for (TomcatClientAppConfigService a : c.getApps()) {
                    if (TomcatUtils.isEmpty(a.getConfig().getPath())) {
                        ok = false;
                        a.getConfig().setPath(context.readOrCancel("[instance=[[%s]]] [app=[[%s]]] Would you enter ==%s== value?",null,c.getName(),a.getName(),"-app.path"));
                    }
                    if (TomcatUtils.isEmpty(a.getConfig().getVersionCommand())) {
                        ok = false;
                        a.getConfig().setVersionCommand(context.readOrCancel("[instance=[[%s]]] [app=[[%s]]] Would you enter ==%s== value?","nsh file-version %file",c.getName(),a.getName(),"-app.version"));
                    }

                }
            }catch (UserCancelException ex){
                return 1;
            }
        }
        c.saveConfig();
        return 0;
    }


    private int remove(String[] args) {
        String instance = null;
        String appName = null;
        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("--app")) {
                j++;
                appName = args[j];
            } else if (args[j].equals("--instance")) {
                j++;
                instance = args[j];
            } else {
                throw new IllegalArgumentException("Unsupported " + args[j]);
            }
        }
        if (appName == null) {
            loadTomcatConfig(instance).removeConfig();
        } else {
            TomcatClientConfigService c = loadTomcatConfig(instance);
            try {
                c.getAppOrError(appName).remove();
                c.saveConfig();
            } catch (Exception ex) {
                //
            }
        }
        return 0;
    }
    private int install(String[] args) {
        String conf = null;
        String app = null;
        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("--instance")) {
                j++;
                conf = args[j];
            } else if (args[j].equals("--app")) {
                j++;
                app = args[j];
            } else {
                throw new IllegalArgumentException("Unsupported " + args[j]);
            }
        }
        TomcatClientConfigService c = loadTomcatConfig(conf);
        c.getApp(app).install();
        return 0;
    }

    private int stop(String[] args) {
        String name = null;
        for (int j = 0; j < args.length; j++) {
            name = args[j];
        }
        TomcatClientConfigService c = loadTomcatConfig(name);
        c.shutdown();
        return 0;
    }

    public int restart(String[] args, boolean shutdown, int i) {
        String name = null;
        boolean deleteLog = false;
        List<String> apps = new ArrayList<>();
        for (int j = i + 1; j < args.length; j++) {
            if (args[j].equals("--deleteLog")) {
                deleteLog = true;
            } else if (args[j].equals("--deploy")) {
                j++;
                apps.add(args[j]);
            } else {
                name = args[j];
            }
        }
        TomcatClientConfigService c = loadTomcatConfig(name);
        if(shutdown) {
            return c.restart(apps.toArray(new String[apps.size()]), deleteLog);
        }else{
            return c.start(apps.toArray(new String[apps.size()]), deleteLog);
        }
    }

    public int reset() {
        for (TomcatClientConfigService tomcatConfig : listConfig()) {
            tomcatConfig.removeConfig();
        }
        return 0;
    }


    public TomcatClientConfigService[] listConfig() {
        List<TomcatClientConfigService> all = new ArrayList<>();
        File[] configFiles = new File(context.configFolder).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".config");
            }
        });
        if (configFiles != null) {
            for (File file1 : configFiles) {
                try {
                    TomcatClientConfigService c = loadTomcatConfig(file1.getName().substring(0, file1.getName().length() - ".config".length()));
                    all.add(c);
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        return all.toArray(new TomcatClientConfigService[all.size()]);
    }


    public TomcatClientConfigService loadTomcatConfig(String name) {
        TomcatClientConfigService t = new TomcatClientConfigService(name, this);
        t.loadConfig();
        return t;
    }

    public TomcatClientConfigService createTomcatConfig(String name) {
        TomcatClientConfigService t = new TomcatClientConfigService(name, this);
        t.setConfig(new TomcatClientConfig());
        return t;
    }

    public TomcatClientConfigService loadOrCreateTomcatConfig(String name) {
        TomcatClientConfigService t = new TomcatClientConfigService(name, this);
        if (t.existsConfig()) {
            t.loadConfig();
        } else {
            t.setConfig(new TomcatClientConfig());
        }
        return t;
    }
}
