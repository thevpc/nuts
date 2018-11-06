package net.vpc.toolbox.tomcat.client;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.toolbox.tomcat.client.config.TomcatClientConfig;
import net.vpc.toolbox.tomcat.util.NutsContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class TomcatClient {
    public NutsContext context;

    public TomcatClient(NutsWorkspace ws) {
        this(new NutsContext(ws));
    }

    public TomcatClient(NutsContext context) {
        this.context = context;
    }

    public void runArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-l") || args[i].equals("--list")) {
                List<String> names = new ArrayList<>();
                for (int j = i + 1; j < args.length; j++) {
                    if (args[j].startsWith("-")) {
                        throw new IllegalArgumentException("Unsupported " + args[i]);
                    } else {
                        names.add(args[j]);
                    }
                }
                if (names.isEmpty()) {
                    for (TomcatClientConfigService tomcatConfig : listConfig()) {
                        context.out.println(tomcatConfig.getName());
                    }
                } else {
                    for (String n : names) {
                        String confName = null;
                        String propName1 = null;
                        String propName2 = null;
                        if (n.contains(".")) {
                            confName = n;
                            propName1 = null;
                            propName2 = null;
                        } else {
                            String[] split = n.split("\\.");
                            confName = split[0];
                            propName1 = split[1];
                            if (split.length > 2) {
                                propName2 = split[2];
                            }
                        }
                        try {
                            TomcatClientConfigService c = loadTomcatConfig(confName);
                            if (propName1 == null) {
                                c.write(context.out);
                            } else {
                                try {
                                    Object o1 = TomcatUtils.getPropertyValue(c, propName1);
                                    if (propName2 != null) {
                                        o1 = TomcatUtils.getPropertyValue(o1, propName2);
                                    }
                                    context.out.println(o1);
                                } catch (Exception ex) {
                                    context.err.println("Property Not Found " + n);
                                }
                            }
                        } catch (Exception ex) {
                            context.err.println(n + "   :  Not found");
                        }
                    }
                }
                return;
            } else if (args[i].equals("-a") || args[i].equals("--add") || args[i].equals("--set")) {
                i++;
                String n = args[i];
                TomcatClientConfigService c = loadOrCreateTomcatConfig(n);
                String appName = null;
                for (int j = i + 1; j < args.length; j++) {
                    if (args[j].equals("--server")) {
                        j++;
                        c.getConfig().setServer(args[j]);
                    } else if (args[j].equals("--cert")) {
                        j++;
                        c.getConfig().setServerCertificateFile(args[j]);
                    } else if (args[j].equals("--password")) {
                        j++;
                        c.getConfig().setServerPassword(args[j]);
                    } else if (args[j].equals("--temp-path")) {
                        j++;
                        c.getConfig().setServerTempPath(args[j]);
                    } else if (args[j].equals("--instance")) {
                        j++;
                        c.getConfig().setServerConfName(args[j]);
                    } else if (args[j].equals("--app")) {
                        j++;
                        appName = args[j];
                        c.getAppOrCreate(appName);
                    } else if (args[j].equals("--app.path")) {
                        j++;
                        String value = args[j];
                        TomcatClientAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                        tomcatAppConfig.getConfig().setPath(value);
                    } else if (args[j].equals("--app.version")) {
                        j++;
                        String value = args[j];
                        TomcatClientAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                        tomcatAppConfig.getConfig().setVersion(value);
                    } else {
                        throw new IllegalArgumentException("Unsupported " + args[i]);
                    }
                }
                c.saveConfig();
                return;
            } else if (args[i].equals("--remove")) {
                String conf = null;
                String appName = null;
                for (int j = i + 1; j < args.length; j++) {
                    if (args[j].equals("--app")) {
                        j++;
                        appName = args[j];
                    } else if (args[j].equals("--instance")) {
                        j++;
                        conf = args[j];
                    } else {
                        throw new IllegalArgumentException("Unsupported " + args[i]);
                    }
                }
                if (appName == null) {
                    loadTomcatConfig(conf).removeConfig();
                } else if (appName != null) {
                    TomcatClientConfigService c = loadTomcatConfig(conf);
                    try {
                        c.getAppOrError(appName).remove();
                        c.saveConfig();
                    } catch (Exception ex) {
                        //
                    }
                } else {
                    throw new IllegalArgumentException("Invalid parameters");
                }
                return;
            } else if (args[i].equals("-s") || args[i].equals("--start")) {
                restart(args, false, i + 1);
                return;
            } else if (args[i].equals("-x") || args[i].equals("--stop") || args[i].equals("--shutdown")) {
                String name = null;
                for (int j = i + 1; j < args.length; j++) {
                    name = args[j];
                }
                TomcatClientConfigService c = loadTomcatConfig(name);
                c.shutdown();
                return;
            } else if (args[i].equals("--restart")) {
                restart(args, true, i + 1);
                return;
            } else if (args[i].equals("--install")) {
                String conf = null;
                String app = null;
                for (int j = i + 1; j < args.length; j++) {
                    if (args[j].equals("--instance")) {
                        j++;
                        conf = args[j];
                    } else if (args[j].equals("--app")) {
                        j++;
                        app = args[j];
                    } else {
                        throw new IllegalArgumentException("Unsupported " + args[i]);
                    }
                }
                TomcatClientConfigService c = loadTomcatConfig(conf);
                c.getApp(app).install();
                return;
            } else if (args[i].equals("--remove-all-configs")) {
                removeAllConfigs();
            }
        }
    }

    public void restart(String[] args, boolean shutdown, int i) {
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
            c.restart(apps.toArray(new String[apps.size()]), deleteLog);
        }else{
            c.start(apps.toArray(new String[apps.size()]), deleteLog);
        }
        return;
    }

    public void removeAllConfigs() {
        for (TomcatClientConfigService tomcatConfig : listConfig()) {
            tomcatConfig.removeConfig();
        }
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
