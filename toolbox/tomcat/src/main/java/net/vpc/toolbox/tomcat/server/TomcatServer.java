package net.vpc.toolbox.tomcat.server;

import net.vpc.app.nuts.*;
import net.vpc.toolbox.tomcat.util.NutsContext;
import net.vpc.toolbox.tomcat.server.config.TomcatServerConfig;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.*;
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


    public int runArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "list":
                    return list(Arrays.copyOfRange(args, i + 1, args.length));
                case "add":
                case "set":
                    return add(Arrays.copyOfRange(args, i + 1, args.length));
                case "remove":
                    return remove(Arrays.copyOfRange(args, i + 1, args.length));
                case "start":
                    return restart(Arrays.copyOfRange(args, i + 1, args.length), false);
                case "stop":
                    return stop(Arrays.copyOfRange(args, 1, args.length));
                case "status":
                    return status(Arrays.copyOfRange(args, 1, args.length));
                case "restart":
                    return restart(Arrays.copyOfRange(args, i + 1, args.length), true);
                case "install":
                    return install(Arrays.copyOfRange(args, 1, args.length));
                case "reset":
                    return reset();
                case "deploy":
                    return deploy(Arrays.copyOfRange(args, 1, args.length));
                case "delete-log":
                    return deleteLog(Arrays.copyOfRange(args, 1, args.length));
                case "show-log":
                    return showLog(Arrays.copyOfRange(args, 1, args.length));
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
                TomcatServerConfigService c = loadOrCreateTomcatConfig(instance);
                TomcatServerAppConfigService a = c.getApp(app);
                if (json) {
                    context.out.printf("[[%s]] :\n", a.getName());
                    a.write(context.out);
                    context.out.println();
                } else {
                    context.out.println(a.getName());
                }
            } else {
                for (TomcatServerConfigService tomcatConfig : listConfig()) {
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
            TomcatServerConfigService c = loadOrCreateTomcatConfig(instance);
            if (app != null) {
                context.out.printf("%s\n", TomcatUtils.getPropertyValue(c.getApp(app).getConfig(), property));
            } else {
                for (TomcatServerAppConfigService a : c.getApps()) {
                    context.out.printf("[%s] %s\n", TomcatUtils.getPropertyValue(a.getConfig(), property));
                }
            }
        }
        return 0;
    }

    public int add(String[] args) {
        TomcatServerConfigService c = null;
        String appName = null;
        String domainName = null;
        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("--instance")) {
                j++;
                if (c == null) {
                    c = loadOrCreateTomcatConfig(args[j]);
                } else {
                    throw new IllegalArgumentException("Instance name already defined");
                }
            } else if (args[j].equals("--catalinaVersion")) {
                j++;
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setCatalinaVersion(args[j]);
            } else if (args[j].equals("--catalinaBase")) {
                j++;
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setCatalinaBase(args[j]);
            } else if (args[j].equals("--shutdownWaitTime")) {
                j++;
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setShutdownWaitTime(Integer.parseInt(args[j]));
            } else if (args[j].equals("--app")) {
                j++;
                appName = args[j];
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getAppOrCreate(appName);
            } else if (args[j].equals("--domain")) {
                j++;
                domainName = args[j];
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getDomainOrCreate(domainName);
            } else if (args[j].equals("--domain.log")) {
                j++;
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getDomainOrCreate(domainName).getConfig().setLogFile(args[j]);

            } else if (args[j].equals("--app.source")) {
                j++;
                String value = args[j];
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                TomcatServerAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                if (tomcatAppConfig == null) {
                    throw new IllegalArgumentException("Missing --app.name");
                }
                tomcatAppConfig.getConfig().setSourceFilePath(value);
            } else if (args[j].equals("--app.deploy")) {
                j++;
                String value = args[j];
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                TomcatServerAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setDeployName(value);
            } else if (args[j].equals("--app.domain")) {
                j++;
                String value = args[j];
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                TomcatServerAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setDomain(value);
            } else if (args[j].equals("--archiveFolder")) {
                j++;
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setArchiveFolder(args[j]);
            } else if (args[j].equals("--runningFolder")) {
                j++;
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRunningFolder(args[j]);
            } else {
                throw new IllegalArgumentException("Unsupported " + args[j]);
            }
        }
        if (c != null) {
            c.saveConfig();
        }
        return 0;
    }

    public int remove(String[] args) {
        String conf = null;
        String appName = null;
        String domName = null;
        for (int j = 0; j < args.length; j++) {
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
                throw new IllegalArgumentException("Unsupported " + args[j]);
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
        return 0;
    }

    public int stop(String[] args) {
        String instance = null;
        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("--instance")) {
                j++;
                instance = args[j];
            }
        }
        TomcatServerConfigService c = loadTomcatConfig(instance);
        return c.stop() ? 0 : 1;
    }

    public int status(String[] args) {
        String instance = null;
        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("--instance")) {
                j++;
                instance = args[j];
            }
        }
        TomcatServerConfigService c = loadTomcatConfig(instance);
        c.printStatus();
        return 0;
    }

    public int install(String[] args) {
        String instance = null;
        String app = null;
        String version = null;
        String file = null;
        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("--version")) {
                j++;
                version = args[j];
            } else if (args[j].equals("--file")) {
                j++;
                file = args[j];
            } else if (args[j].equals("--instance")) {
                j++;
                instance = args[j];
            } else if (args[j].equals("--app")) {
                j++;
                app = args[j];
            } else {
                throw new IllegalArgumentException("Unsupported " + args[j]);
            }
        }
        TomcatServerConfigService c = loadTomcatConfig(instance);
        c.getApp(app).install(version, file, true);
        return 0;
    }

    public int deleteLog(String[] args) {
        String instance = null;
        String domain = null;
        boolean all = false;
        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("--all")) {
                all = true;
            } else if (args[j].equals("--instance")) {
                j++;
                instance = args[j];
            } else if (args[j].equals("--domain")) {
                j++;
                domain = args[j];
            } else {
                throw new IllegalArgumentException("Unsupported " + args[j]);
            }
        }
        TomcatServerConfigService c = loadTomcatConfig(instance);
        if (all) {
            c.deleteAllLog();
        } else {
            c.deleteOutLog();
        }
        return 0;
    }

    public int showLog(String[] args) {
        String instance = null;
        String domain = null;
        boolean path = false;
        boolean all = false;
        int count = -1;
        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("--instance")) {
                j++;
                instance = args[j];
            } else if (args[j].equals("--domain")) {
                j++;
                domain = args[j];
            } else if (args[j].equals("--path")) {
                path = true;
            } else if (args[j].startsWith("-") && TomcatUtils.isPositiveInt(args[j].substring(1))) {
                count = Integer.parseInt(args[j].substring(1));
            } else {
                throw new IllegalArgumentException("Unsupported " + args[j]);
            }
        }
        TomcatServerConfigService c = loadTomcatConfig(instance);
        if (path) {
            context.out.printf("%s\n", c.getOutLogFile().getPath());
        } else {
            c.showOutLog(count);
        }
        return 0;
    }

    public int deploy(String[] args) {
        String instance = null;
        String version = null;
        String file = null;
        String app = null;
        String domain = null;
        String contextName = null;
        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("--version")) {
                j++;
                version = args[j];
            } else if (args[j].equals("--file")) {
                j++;
                file = args[j];
            } else if (args[j].equals("--instance")) {
                j++;
                instance = args[j];
            } else if (args[j].equals("--app")) {
                j++;
                app = args[j];
            } else if (args[j].equals("--context")) {
                j++;
                contextName = args[j];
            } else if (args[j].equals("--domain")) {
                j++;
                domain = args[j];
            } else {
                throw new IllegalArgumentException("Unsupported " + args[j]);
            }
        }
        TomcatServerConfigService c = loadTomcatConfig(instance);
        if (file != null) {
            c.deployFile(new File(file), contextName, domain);
        } else {
            c.getAppOrError(app).deploy(version);
        }
        return 0;
    }

    public int restart(String[] args, boolean shutdown) {
        boolean deleteLog = false;
        String instance = null;
        List<String> apps = new ArrayList<>();
        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("--instance")) {
                j++;
                instance = args[j];
            } else if (args[j].equals("--deleteOutLog")) {
                deleteLog = true;
            } else if (args[j].equals("--deploy")) {
                j++;
                apps.add(args[j]);
            }
        }
        TomcatServerConfigService c = loadTomcatConfig(instance);
        if (shutdown) {
            c.restart(apps.toArray(new String[apps.size()]), deleteLog);
        } else {
            c.start(apps.toArray(new String[apps.size()]), deleteLog);
        }
        return 0;
    }

    public int reset() {
        for (TomcatServerConfigService tomcatConfig : listConfig()) {
            tomcatConfig.removeConfig();
        }
        return 0;
    }


    public TomcatServerConfigService[] listConfig() {
        List<TomcatServerConfigService> all = new ArrayList<>();
        File[] configFiles = new File(context.configFolder).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(TomcatServerConfigService.SERVER_CONFIG_EXT);
            }
        });
        if (configFiles != null) {
            for (File file1 : configFiles) {
                try {
                    TomcatServerConfigService c = loadTomcatConfig(file1);
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

    public TomcatServerConfigService loadTomcatConfig(File file) {
        TomcatServerConfigService t = new TomcatServerConfigService(file, this);
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
