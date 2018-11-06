package net.vpc.toolbox.tomcat.server;

import net.vpc.app.nuts.*;
import net.vpc.toolbox.tomcat.util.NutsContext;
import net.vpc.toolbox.tomcat.server.config.TomcatServerConfig;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TomcatServer {
    NutsContext context;


    public TomcatServer(NutsWorkspace ws) {
        this(new NutsContext(ws));
    }

    public TomcatServer(NutsContext ws) {
        this.context = ws;
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
                    for (TomcatServerConfigService tomcatConfig : listConfig()) {
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
                            TomcatServerConfigService c = loadTomcatConfig(confName);
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
                TomcatServerConfigService c = loadOrCreateTomcatConfig(n);
                String appName = null;
                String domainName = null;
                for (int j = i + 1; j < args.length; j++) {
                    if (args[j].equals("--catalinaVersion")) {
                        j++;
                        c.getConfig().setCatalinaVersion(args[j]);
                    } else if (args[j].equals("--catalinaBase")) {
                        j++;
                        c.getConfig().setCatalinaBase(args[j]);
                    } else if (args[j].equals("--shutdownWaitTime")) {
                        j++;
                        c.getConfig().setShutdownWaitTime(Integer.parseInt(args[j]));
                    } else if (args[j].equals("--app")) {
                        j++;
                        appName = args[j];
                        c.getAppOrCreate(appName);
                    } else if (args[j].equals("--domain")) {
                        j++;
                        domainName = args[j];
                        c.getDomainOrCreate(domainName);
                    } else if (args[j].equals("--domain.log")) {
                        j++;
                        c.getDomainOrCreate(domainName).getConfig().setLogFile(args[j]);

                    } else if (args[j].equals("--app.source")) {
                        j++;
                        String value = args[j];
                        TomcatServerAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                        if (tomcatAppConfig == null) {
                            throw new IllegalArgumentException("Missing --app.name");
                        }
                        tomcatAppConfig.getConfig().setSourceFilePath(value);
                    } else if (args[j].equals("--app.deploy")) {
                        j++;
                        String value = args[j];
                        TomcatServerAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                        tomcatAppConfig.getConfig().setDeployName(value);
                    } else if (args[j].equals("--app.domain")) {
                        j++;
                        String value = args[j];
                        TomcatServerAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                        tomcatAppConfig.getConfig().setDomain(value);
                    } else if (args[j].equals("--archiveFolder")) {
                        j++;
                        c.getConfig().setArchiveFolder(args[j]);
                    } else if (args[j].equals("--runningFolder")) {
                        j++;
                        c.getConfig().setRunningFolder(args[j]);
                    } else {
                        throw new IllegalArgumentException("Unsupported " + args[i]);
                    }
                }
                c.saveConfig();
                return;
            } else if (args[i].equals("--remove")) {
                String conf = null;
                String appName = null;
                String domName = null;
                for (int j = i + 1; j < args.length; j++) {
                    if (args[j].equals("--app")) {
                        j++;
                        appName = args[j];
                    } else if (args[j].equals("--instance")) {
                        j++;
                        conf = args[j];
                    } else if (args[j].equals("--domain")) {
                        j++;
                        domName = args[j];
                    } else {
                        throw new IllegalArgumentException("Unsupported " + args[i]);
                    }
                }
                if (appName == null && domName == null) {
                    loadTomcatConfig(conf).removeConfig();
                } else if (appName != null && domName == null) {
                    TomcatServerConfigService c = loadTomcatConfig(conf);
                    try {
                        c.getAppOrError(appName).remove();
                        c.saveConfig();
                    } catch (Exception ex) {
                        //
                    }
                } else if (appName == null && domName != null) {
                    TomcatServerConfigService c = loadTomcatConfig(conf);
                    try {
                        c.getDomainOrError(domName).remove();
                        for (TomcatServerAppConfigService a : c.getApps()) {
                            if (domName.equals(a.getConfig().getDomain())) {
                                a.remove();
                            }
                        }
                        c.saveConfig();
                    } catch (Exception ex) {
                        //
                    }
                } else {
                    throw new IllegalArgumentException("Invalid parameters");
                }
                return;
            } else if (args[i].equals("-s") || args[i].equals("--start")) {

                restart(Arrays.copyOfRange(args,i+1,args.length), false);
                return;
            } else if (args[i].equals("-x") || args[i].equals("--stop") || args[i].equals("--shutdown")) {
                String name = null;
                for (int j = i + 1; j < args.length; j++) {
                    name = args[j];
                }
                TomcatServerConfigService c = loadTomcatConfig(name);
                c.shutdown();
                return;
            } else if (args[i].equals("--restart")) {
                restart(Arrays.copyOfRange(args,i+1,args.length), true);
                return;
            } else if (args[i].equals("--install")) {
                String conf = null;
                String app = null;
                String version = null;
                String file = null;
                List<String> apps = new ArrayList<>();
                for (int j = i + 1; j < args.length; j++) {
                    if (args[j].equals("--version")) {
                        j++;
                        version = args[j];
                    } else if (args[j].equals("--file")) {
                        j++;
                        file = args[j];
                    } else if (args[j].equals("--instance")) {
                        j++;
                        conf = args[j];
                    } else if (args[j].equals("--app")) {
                        j++;
                        app = args[j];
                    } else {
                        throw new IllegalArgumentException("Unsupported " + args[i]);
                    }
                }
                TomcatServerConfigService c = loadTomcatConfig(conf);
                c.getApp(app).install(version, file, true);
                return;
            } else if (args[i].equals("--remove-all-configs")) {
                removeAllConfigs();
            }
        }
    }

    public void restart(String[] args, boolean shutdown) {
        String name = null;
        boolean deleteLog = false;
        List<String> apps = new ArrayList<>();
        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("--deleteOutLog")) {
                deleteLog = true;
            } else if (args[j].equals("--deploy")) {
                j++;
                apps.add(args[j]);
            } else {
                name = args[j];
            }
        }
        TomcatServerConfigService c = loadTomcatConfig(name);
        if(shutdown) {
            c.restart(apps.toArray(new String[apps.size()]),deleteLog);
        }else{
            c.start(apps.toArray(new String[apps.size()]),deleteLog);
        }
        return;
    }

    public void removeAllConfigs() {
        for (TomcatServerConfigService tomcatConfig : listConfig()) {
            tomcatConfig.removeConfig();
        }
    }


    public TomcatServerConfigService[] listConfig() {
        List<TomcatServerConfigService> all = new ArrayList<>();
        File[] configFiles = new File(context.configFolder).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".config");
            }
        });
        if (configFiles != null) {
            for (File file1 : configFiles) {
                try {
                    TomcatServerConfigService c = loadTomcatConfig(file1.getName().substring(0, file1.getName().length() - ".config".length()));
                    all.add(c);
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        return all.toArray(new TomcatServerConfigService[all.size()]);
    }


    public TomcatServerConfigService loadTomcatConfig(String name) {
        TomcatServerConfigService t = new TomcatServerConfigService(name, this);
        t.loadConfig();
        return t;
    }

    public TomcatServerConfigService createTomcatConfig(String name) {
        TomcatServerConfigService t = new TomcatServerConfigService(name, this);
        t.setConfig(new TomcatServerConfig());
        return t;
    }

    public TomcatServerConfigService loadOrCreateTomcatConfig(String name) {
        TomcatServerConfigService t = new TomcatServerConfigService(name, this);
        if (t.existsConfig()) {
            t.loadConfig();
        } else {
            t.setConfig(new TomcatServerConfig());
        }
        return t;
    }
}
