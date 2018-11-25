package net.vpc.toolbox.tomcat.server;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.toolbox.tomcat.server.config.TomcatServerConfig;
import net.vpc.toolbox.tomcat.util.NutsContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class TomcatServer {
    private NutsContext context;


    public TomcatServer(NutsWorkspace ws) {
        this(new NutsContext(ws));
    }

    public TomcatServer(NutsContext ws) {
        this.setContext(ws);
    }


    public int runArgs(String[] args) {
        CommandLine cmd = new CommandLine(args);
        while (cmd.hasNext()) {
            Argument val = cmd.readNonOption();
            switch (val.getExpression()) {
                case "list":
                    return list(cmd);
                case "add":
                case "set":
                    return add(cmd);
                case "show":
                case "describe":
                    return show(cmd);
                case "remove":
                    return remove(cmd);
                case "start":
                    return restart(cmd, false);
                case "stop":
                    return stop(cmd);
                case "status":
                    return status(cmd);
                case "restart":
                    return restart(cmd, true);
                case "install":
                    return install(cmd);
                case "reset":
                    return reset();
                case "deploy":
                    return deploy(cmd);
                case "delete-log":
                    return deleteLog(cmd);
                case "delete-temp":
                    return deleteTemp(cmd);
                case "delete-work":
                    return deleteWork(cmd);
                case "show-log":
                    return showLog(cmd);
                default:
                    throw new RuntimeException("Unsupported action " + val.getExpression());
            }
        }
        return 0;
    }

    public int list(CommandLine args) {
        boolean json = false;
        String instance = null;
        String app = null;
        String property = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readBooleanOption("--json")) != null) {
                json = a.getBooleanValue();
            } else if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getStringValue();
            } else if ((a = args.readStringOption("--app")) != null) {
                app = a.getStringValue();
            } else if ((a = args.readStringOption("--property")) != null) {
                property = a.getStringValue();
            } else {
                args.unexpectedArgument();
            }
        }
        if (property == null) {
            if (app != null) {
                TomcatServerConfigService c = loadOrCreateTomcatConfig(instance);
                TomcatServerAppConfigService aa = c.getApp(app);
                if (json) {
                    getContext().out.printf("[[%s]] :\n", aa.getName());
                    aa.write(getContext().out);
                    getContext().out.println();
                } else {
                    getContext().out.println(aa.getName());
                }
            } else {
                for (TomcatServerConfigService tomcatConfig : listConfig()) {
                    if (json) {
                        getContext().out.printf("[[%s]] :\n", tomcatConfig.getName());
                        tomcatConfig.write(getContext().out);
                        getContext().out.println();
                    } else {
                        getContext().out.println(tomcatConfig.getName());
                    }
                }
            }
        } else {
            TomcatServerConfigService c = loadOrCreateTomcatConfig(instance);
            if (app != null) {
                getContext().out.printf("%s\n", TomcatUtils.getPropertyValue(c.getApp(app).getConfig(), property));
            } else {
                for (TomcatServerAppConfigService aa : c.getApps()) {
                    getContext().out.printf("[%s] %s\n", TomcatUtils.getPropertyValue(aa.getConfig(), property));
                }
            }
        }
        return 0;
    }

    public void show(TomcatServerAppConfigService aa, boolean json) {
        if (json) {
            getContext().out.printf("[[%s]] :\n", aa.getName());
            aa.write(getContext().out);
            getContext().out.println();
        } else {
            getContext().out.printf("[[%s]] :\n", aa.getName());
            aa.write(getContext().out);
            getContext().out.println();
        }
    }

    public void show(TomcatServerConfigService tomcatConfig, boolean json) {
        if (json) {
            getContext().out.printf("[[%s]] :\n", tomcatConfig.getName());
            tomcatConfig.write(getContext().out);
            getContext().out.println();
        } else {
            getContext().out.printf("[[%s]] :\n", tomcatConfig.getName());
            tomcatConfig.write(getContext().out);
            getContext().out.println();
        }
    }

    public int show(CommandLine args) {
        boolean json = false;
        String instance = null;
        String app = null;
        Argument a;

        while (args.hasNext()) {
            if ((a = args.readBooleanOption("--json")) != null) {
                json = a.getBooleanValue();
            } else if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getStringValue();
            } else if ((a = args.readStringOption("--app")) != null) {
                app = a.getStringValue();
            } else {
                args.unexpectedArgument();
            }
        }

        if (app != null) {
            TomcatServerConfigService c = loadOrCreateTomcatConfig(instance);
            TomcatServerAppConfigService aa = c.getApp(app);
            show(aa,json);
        } else {
            show(loadOrCreateTomcatConfig(instance),json);
        }

        return 0;
    }

    public int add(CommandLine args) {
        TomcatServerConfigService c = null;
        String appName = null;
        String domainName = null;
        String instance = null;

        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(instance);
                } else {
                    throw new IllegalArgumentException("Instance name already defined");
                }
            } else if ((a = args.readStringOption("--catalinaVersion")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setCatalinaVersion(a.getStringValue());
            } else if ((a = args.readStringOption("--catalinaBase")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setCatalinaBase(a.getStringValue());
            } else if ((a = args.readStringOption("--catalinaHome")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setCatalinaHome(a.getStringValue());
            } else if ((a = args.readStringOption("--shutdownWaitTime")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setShutdownWaitTime(a.getIntValue());
            } else if ((a = args.readStringOption("--app")) != null) {
                appName = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getAppOrCreate(appName);
            } else if ((a = args.readStringOption("--domain")) != null) {
                domainName = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getDomainOrCreate(domainName);
            } else if ((a = args.readStringOption("--domain.log")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getDomainOrCreate(domainName).getConfig().setLogFile(a.getStringValue());

            } else if ((a = args.readStringOption("--app.source")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                TomcatServerAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                if (tomcatAppConfig == null) {
                    throw new IllegalArgumentException("Missing --app.name");
                }
                tomcatAppConfig.getConfig().setSourceFilePath(value);
            } else if ((a = args.readStringOption("--app.deploy")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                TomcatServerAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setDeployName(value);
            } else if ((a = args.readStringOption("--app.domain")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                TomcatServerAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setDomain(value);
            } else if ((a = args.readStringOption("--archiveFolder")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setArchiveFolder(a.getStringValue());
            } else if ((a = args.readStringOption("--runningFolder")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRunningFolder(a.getStringValue());
            } else {
                args.unexpectedArgument();
            }
        }
        if (c != null) {
            c.saveConfig();
        }
        return 0;
    }

    public int remove(CommandLine args) {
        String conf = null;
        String appName = null;
        String domName = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--app")) != null) {
                appName = a.getStringValue();
            } else if ((a = args.readStringOption("--instance")) != null) {
                conf = a.getStringValue();
            } else if ((a = args.readStringOption("--domain")) != null) {
                domName = a.getStringValue();
            } else {
                args.unexpectedArgument();
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
                for (TomcatServerAppConfigService aa : c.getApps()) {
                    if (domName.equals(aa.getConfig().getDomain())) {
                        aa.remove();
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

    public int stop(CommandLine args) {
        String instance = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getStringValue();
            } else {
                args.unexpectedArgument();
            }
        }
        TomcatServerConfigService c = loadTomcatConfig(instance);
        return c.stop() ? 0 : 1;
    }

    public int status(CommandLine args) {
        String instance = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getStringValue();
            } else {
                args.unexpectedArgument();
            }
        }
        TomcatServerConfigService c = loadTomcatConfig(instance);
        c.printStatus();
        return 0;
    }

    public int install(CommandLine args) {
        String instance = null;
        String app = null;
        String version = null;
        String file = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--version")) != null) {
                version = a.getStringValue();
            } else if ((a = args.readStringOption("--file")) != null) {
                file = a.getStringValue();
            } else if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getStringValue();
            } else if ((a = args.readStringOption("--app")) != null) {
                app = a.getStringValue();
            } else {
                args.unexpectedArgument();
            }
        }
        TomcatServerConfigService c = loadTomcatConfig(instance);
        c.getApp(app).install(version, file, true);
        return 0;
    }

    public int deleteLog(CommandLine args) {
        String instance = null;
        String domain = null;
        boolean all = false;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readBooleanOption("--all")) != null) {
                all = a.getBooleanValue();
            } else if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getStringValue();
            } else if ((a = args.readStringOption("--domain")) != null) {
                domain = a.getStringValue();
            } else {
                args.unexpectedArgument();
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

    public int deleteTemp(CommandLine args) {
        String instance = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getStringValue();
            } else {
                args.unexpectedArgument();
            }
        }
        TomcatServerConfigService c = loadTomcatConfig(instance);
        c.deleteTemp();
        return 0;
    }

    public int deleteWork(CommandLine args) {
        String instance = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getStringValue();
            } else {
                args.unexpectedArgument();
            }
        }
        TomcatServerConfigService c = loadTomcatConfig(instance);
        c.deleteWork();
        return 0;
    }

    public int showLog(CommandLine args) {
        String instance = null;
        String domain = null;
        boolean path = false;
        boolean all = false;
        int count = -1;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getStringValue();
            } else if ((a = args.readStringOption("--domain")) != null) {
                domain = a.getStringValue();
            } else if ((a = args.readStringOption("--path")) != null) {
                path = true;
            } else if (args.isOption() && TomcatUtils.isPositiveInt(args.get().getExpression().substring(1))) {
                count = Integer.parseInt(args.read().getExpression().substring(1));
            } else {
                args.unexpectedArgument();
            }
        }
        TomcatServerConfigService c = loadTomcatConfig(instance);
        if (path) {
            getContext().out.printf("%s\n", c.getOutLogFile().getPath());
        } else {
            c.showOutLog(count);
        }
        return 0;
    }

    public int deploy(CommandLine args) {
        String instance = null;
        String version = null;
        String file = null;
        String app = null;
        String domain = null;
        String contextName = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--version")) != null) {
                version = a.getStringValue();
            } else if ((a = args.readStringOption("--file")) != null) {
                file = a.getValue();
            } else if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getStringValue();
            } else if ((a = args.readStringOption("--app")) != null) {
                app = a.getStringValue();
            } else if ((a = args.readStringOption("--context")) != null) {
                contextName = a.getStringValue();
            } else if ((a = args.readStringOption("--domain")) != null) {
                domain = a.getStringValue();
            } else {
                args.unexpectedArgument();
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

    public int restart(CommandLine args, boolean shutdown) {
        boolean deleteLog = false;
        String instance = null;
        List<String> apps = new ArrayList<>();
        while (args.hasNext()) {
            Argument a = null;
            if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getValue();
            } else if ((a = args.readBooleanOption("--deleteOutLog")) != null) {
                deleteLog = a.getBooleanValue();
            } else if ((a = args.readStringOption("--deploy")) != null) {
                apps.add(a.getValue());
            } else {
                args.unexpectedArgument();
            }
        }
        TomcatServerConfigService c = loadTomcatConfig(instance);
        if (shutdown) {
            c.restart(apps.toArray(new String[0]), deleteLog);
        } else {
            c.start(apps.toArray(new String[0]), deleteLog);
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
        File[] configFiles = new File(getContext().configFolder).listFiles(new FileFilter() {
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
        return all.toArray(new TomcatServerConfigService[0]);
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

    public NutsContext getContext() {
        return context;
    }

    public void setContext(NutsContext context) {
        this.context = context;
    }
}
