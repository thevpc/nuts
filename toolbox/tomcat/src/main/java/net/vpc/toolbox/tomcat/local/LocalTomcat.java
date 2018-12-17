package net.vpc.toolbox.tomcat.local;

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.app.NutsAppUtils;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.format.PropertiesFormatter;
import net.vpc.toolbox.tomcat.local.config.LocalTomcatConfig;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class LocalTomcat {
    private NutsApplicationContext context;


    public LocalTomcat(NutsApplicationContext ws) {
        this.setContext(ws);
    }

    public int runArgs() {
        CommandLine cmd = new CommandLine(context.getArgs());
        Argument a;
        while (cmd.hasNext()) {
            if (cmd.isOption()) {
                if (context.configure(cmd)) {
                    if (context.isRequiredExit()) {
                        return context.getExitCode();
                    }
                } else {
                    cmd.unexpectedArgument("tomcat --local");
                }
            } else {
                a = cmd.readNonOption();
                switch (a.getExpression()) {
                    case "list":
                        return list(cmd);
                    case "show":
                    case "describe":
                        return show(cmd);
                    case "show-property":
                        return showProperty(cmd);
                    case "add":
                    case "set":
                        return add(cmd);
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
                        return deployApp(cmd);
                    case "deploy-file":
                        return deployFile(cmd);
                    case "delete-log":
                        return deleteLog(cmd);
                    case "delete-temp":
                        return deleteTemp(cmd);
                    case "delete-work":
                        return deleteWork(cmd);
                    case "show-log":
                        return showLog(cmd);
                    default:
                        throw new RuntimeException("Unsupported action " + a.getExpression());
                }
            }
        }
        return 0;
    }

    public int list(CommandLine args) {
        Argument a;
        class Helper {
            boolean apps = false;
            boolean domains = false;
            boolean processed = false;

            boolean isApps() {
                return apps || (!apps && !domains);
            }

            boolean isDomains() {
                return domains || (!apps && !domains);
            }

            boolean isHeader() {
                return isApps() && isDomains();
            }

            void print(LocalTomcatConfigService c) {
                processed = true;
                if (isApps()) {
                    List<LocalTomcatAppConfigService> apps = c.getApps();
                    if (!apps.isEmpty()) {
                        if (isHeader()) {
                            context.out().printf("[[\\[%s\\]]]:\n", "Apps");
                        }
                        for (LocalTomcatAppConfigService app : apps) {
                            context.out().printf("%s\n", app.getName());
                        }
                    }
                }
                if (isDomains()) {
                    List<LocalTomcatDomainConfigService> domains = c.getDomains();
                    if (!domains.isEmpty()) {
                        if (isHeader()) {
                            context.out().printf("[[\\[%s\\]]]:\n", "Domains");
                        }
                        for (LocalTomcatDomainConfigService app : domains) {
                            context.out().printf("%s\n", app.getName());
                        }
                    }
                }
            }
        }
        Helper x = new Helper();
        while (args.hasNext()) {
            if (context.configure(args)) {
                if (context.isRequiredExit()) {
                    return context.getExitCode();
                }
            } else if ((a = args.readBooleanOption("--apps")) != null) {
                x.apps = a.getBooleanValue();
            } else if ((a = args.readBooleanOption("--domains")) != null) {
                x.domains = a.getBooleanValue();
            } else if ((a = args.readStringOption("--instance")) != null) {
                x.print(loadOrCreateTomcatConfig(a.getStringValue()));
            } else {
                x.print(loadOrCreateTomcatConfig(args.readRequiredNonOption().getStringValue()));
            }
        }
        if (!x.processed) {
            for (LocalTomcatConfigService tomcatConfig : listConfig()) {
                getContext().out().println(tomcatConfig.getName());
            }
        }
        return 0;
    }


    public int show(CommandLine args) {
        Argument a;
        LocalTomcatServiceBase s;
        class Helper {
            boolean json = false;

            public void show(LocalTomcatServiceBase aa) {
                if (json) {
                    getContext().out().printf("[[%s]] :\n", aa.getName());
                    aa.write(getContext().out());
                    getContext().out().println();
                } else {
                    getContext().out().printf("[[%s]] :\n", aa.getName());
                    aa.write(getContext().out());
                    getContext().out().println();
                }
            }
        }
        Helper h = new Helper();
        while (args.hasNext()) {
            if (context.configure(args)) {
                if (context.isRequiredExit()) {
                    return context.getExitCode();
                }
            } else if ((a = args.readBooleanOption("--json")) != null) {
                h.json = a.getBooleanValue();
            } else if ((s = readBaseServiceArg(args)) != null) {
                h.show(s);
            } else {
                args.unexpectedArgument("tomcat --local show");
            }
        }
        return 0;
    }

    public int showProperty(CommandLine args) {
        Argument a;
        class Item {
            String name;

            public Item(String name) {
                this.name = name;
            }
        }
        class PropsHelper {
            LocalTomcatServiceBase s = null;
            List<String> props = new ArrayList<>();
            LinkedHashMap<Item, Object> m = new LinkedHashMap<>();

            void addProp(String p) {
                props.add(p);
                build();
            }

            void build() {
                if (s != null) {
                    for (String prop : props) {
                        Object o = NutsAppUtils.getPropertyValue(s.getConfig(), prop);
                        m.put(new Item(prop), o);
                    }
                    props.clear();
                }
            }
        }
        PropsHelper x = new PropsHelper();
        while (args.hasNext()) {
            if (context.configure(args)) {
                if (context.isRequiredExit()) {
                    return context.getExitCode();
                }
            } else if (x.s == null && (x.s = readBaseServiceArg(args)) != null) {
                //ok
            } else if ((a = args.readStringOption("--property")) != null) {
                x.addProp(a.getStringValue());
            } else if (args.isOption()) {
                args.unexpectedArgument("tomcat --local show-property");
            } else {
                x.addProp(args.read().getExpression());
            }
        }
        x.build();
        if (x.m.isEmpty()) {
            throw new NutsExecutionException("No properties to show",2);
        }
        if (x.m.size() == 1) {
            for (Object value : x.m.values()) {
                context.out().printf("%s\n",String.valueOf(value));
                break;
            }
        } else {
            new PropertiesFormatter().format(x.m, context.out());
        }
        return 0;
    }

    public int add(CommandLine args) {
        LocalTomcatConfigService c = null;
        String appName = null;
        String domainName = null;
        String instance = null;

        Argument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                if (context.isRequiredExit()) {
                    return context.getExitCode();
                }
            } else if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(instance);
                } else {
                    throw new IllegalArgumentException("Instance name already defined");
                }
            } else if ((a = args.readStringOption("--catalina-version")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setCatalinaVersion(a.getStringValue());
            } else if ((a = args.readStringOption("--catalina-base")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setCatalinaBase(a.getStringValue());
            } else if ((a = args.readStringOption("--catalina-home")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setCatalinaHome(a.getStringValue());
            } else if ((a = args.readStringOption("--shutdown-wait-time")) != null) {
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
                LocalTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                if (tomcatAppConfig == null) {
                    throw new IllegalArgumentException("Missing --app.source");
                }
                tomcatAppConfig.getConfig().setSourceFilePath(value);
            } else if ((a = args.readStringOption("--app.deploy")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                LocalTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setDeployName(value);
            } else if ((a = args.readStringOption("--app.domain")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                LocalTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setDomain(value);
            } else if ((a = args.readStringOption("--archive-folder")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setArchiveFolder(a.getStringValue());
            } else if ((a = args.readStringOption("--running-folder")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRunningFolder(a.getStringValue());
            } else {
                args.unexpectedArgument("tomcat --local add");
            }
        }
        if (c != null) {
            c.save();
            //just check it is installed!!
            c.getCatalinaBase();
        }
        return 0;
    }

    public int remove(CommandLine args) {
        LocalTomcatServiceBase s = null;
        Argument a;
        boolean processed = false;
        int lastExitCode = 0;
        while (args.hasNext()) {
            if (context.configure(args)) {
                if (context.isRequiredExit()) {
                    return context.getExitCode();
                }
            } else if ((s = readBaseServiceArg(args)) != null) {
                s.remove();
                if (!(s instanceof LocalTomcatConfigService)) {
                    toLocalTomcatConfigService(s).save();
                }
                processed = true;
                lastExitCode = 0;
            } else {
                args.unexpectedArgument("tomcat --local remove");
            }
        }
        if (!processed) {
            throw new IllegalArgumentException("tomcat --local remove: Invalid parameters");
        }
        return lastExitCode;
    }

    public int stop(CommandLine args) {
        LocalTomcatConfigService s = null;
        Argument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                if (context.isRequiredExit()) {
                    return context.getExitCode();
                }
            } else if ((s = readTomcatServiceArg(args)) != null) {
                return s.stop() ? 0 : 1;
            } else {
                args.unexpectedArgument("tomcat --local stop");
            }
        }
        LocalTomcatConfigService c = loadTomcatConfig("");
        return c.stop() ? 0 : 1;
    }

    public int status(CommandLine args) {
        LocalTomcatConfigService s = null;
        Argument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                if (context.isRequiredExit()) {
                    return context.getExitCode();
                }
            } else if ((s = readTomcatServiceArg(args)) != null) {
                s.printStatus();
                return 0;
            } else {
                args.unexpectedArgument("tomcat --local status");
            }
        }
        LocalTomcatConfigService c = loadTomcatConfig("");
        c.printStatus();
        return 0;
    }

    public int install(CommandLine args) {
        LocalTomcatAppConfigService app = null;
        String version = null;
        String file = null;
        Argument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                if (context.isRequiredExit()) {
                    return context.getExitCode();
                }
            } else if ((a = args.readStringOption("--app")) != null) {
                app = loadApp(a.getStringValue());
            } else if ((a = args.readStringOption("--version")) != null) {
                version = a.getStringValue();
            } else if ((a = args.readStringOption("--file")) != null) {
                file = a.getStringValue();
            } else {
                if (file == null) {
                    file = args.readNonOption().getString();
                } else {
                    args.unexpectedArgument("tomcat --local install");
                }
            }
        }
        if (app == null) {
            throw new NutsExecutionException("Missing Application", 2);
        }
        if (file == null) {
            throw new NutsExecutionException("Missing File", 2);
        }
        app.install(version, file, true);
        return 0;
    }

    public int deleteLog(CommandLine args) {
        LocalTomcatServiceBase s = null;
        boolean all = false;
        Argument a;
        boolean processed = false;
        while (args.hasNext()) {
            if (context.configure(args)) {
                if (context.isRequiredExit()) {
                    return context.getExitCode();
                }
            } else if ((a = args.readBooleanOption("-a", "--all")) != null) {
                all = a.getBooleanValue();
            } else if ((s = readBaseServiceArg(args)) != null) {
                LocalTomcatConfigService c = toLocalTomcatConfigService(s);
                if (all) {
                    c.deleteAllLog();
                } else {
                    c.deleteOutLog();
                }
                processed = true;
            } else {
                args.unexpectedArgument("tomcat --local delete-log");
            }
        }
        if (!processed) {
            LocalTomcatConfigService c = loadTomcatConfig("");
            if (all) {
                c.deleteAllLog();
            } else {
                c.deleteOutLog();
            }
        }
        return 0;
    }

    public int deleteTemp(CommandLine args) {
        LocalTomcatServiceBase s = null;
        Argument a;
        boolean processed = false;
        while (args.hasNext()) {
            if (context.configure(args)) {
                if (context.isRequiredExit()) {
                    return context.getExitCode();
                }
            } else if ((s = readBaseServiceArg(args)) != null) {
                LocalTomcatConfigService c = toLocalTomcatConfigService(s);
                c.deleteTemp();
                processed = true;
            } else {
                args.unexpectedArgument("tomcat --local delete-temp");
            }
        }
        if (!processed) {
            LocalTomcatConfigService c = loadTomcatConfig("");
            c.deleteTemp();
        }
        return 0;
    }

    public int deleteWork(CommandLine args) {
        LocalTomcatServiceBase s = null;
        Argument a;
        boolean processed = false;
        while (args.hasNext()) {
            if (context.configure(args)) {
                if (context.isRequiredExit()) {
                    return context.getExitCode();
                }
            } else if ((s = readBaseServiceArg(args)) != null) {
                LocalTomcatConfigService c = toLocalTomcatConfigService(s);
                c.deleteWork();
                processed = true;
            } else {
                args.unexpectedArgument("tomcat --local delete-work");
            }
        }
        if (!processed) {
            LocalTomcatConfigService c = loadTomcatConfig("");
            c.deleteWork();
        }
        return 0;
    }

    public int showLog(CommandLine args) {
        LocalTomcatServiceBase s = null;
        boolean processed = false;
        String instance = null;
        String domain = null;
        boolean path = false;
        boolean all = false;
        int count = -1;
        Argument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                if (context.isRequiredExit()) {
                    return context.getExitCode();
                }
            } else if ((s = readBaseServiceArg(args)) != null) {
                LocalTomcatConfigService c = toLocalTomcatConfigService(s);
                if (path) {
                    getContext().out().printf("%s\n", c.getOutLogFile().getPath());
                } else {
                    c.showOutLog(count);
                }
                processed = true;
            } else if ((a = args.readStringOption("--path")) != null) {
                path = true;
            } else if (args.isOption() && TomcatUtils.isPositiveInt(args.get().getExpression().substring(1))) {
                count = Integer.parseInt(args.read().getExpression().substring(1));
            } else {
                args.unexpectedArgument("tomcat --local show-log");
            }
        }
        if (!processed) {
            LocalTomcatConfigService c = loadTomcatConfig("");
            if (path) {
                getContext().out().printf("%s\n", c.getOutLogFile().getPath());
            } else {
                c.showOutLog(count);
            }
        }
        return 0;
    }

    public int deployFile(CommandLine args) {
        String instance = null;
        String version = null;
        String file = null;
        String app = null;
        String domain = null;
        String contextName = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--file")) != null) {
                file = a.getValue();
            } else if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getStringValue();
            } else if ((a = args.readStringOption("--context")) != null) {
                contextName = a.getStringValue();
            } else if ((a = args.readStringOption("--domain")) != null) {
                domain = a.getStringValue();
            } else {
                if (file == null) {
                    file = args.readNonOption().getExpression();
                } else {
                    args.unexpectedArgument("tomcat --local deploy-file");
                }
            }
        }
        if (file == null) {
            throw new NutsExecutionException("Missing File", 2);
        }
        LocalTomcatConfigService c = loadTomcatConfig(instance);
        c.deployFile(new File(file), contextName, domain);
        return 0;
    }

    public int deployApp(CommandLine args) {
        String version = null;
        String app = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--version")) != null) {
                version = a.getStringValue();
            } else if ((a = args.readStringOption("--app")) != null) {
                app = a.getStringValue();
            } else {
                if (app == null) {
                    app = args.readNonOption().getExpression();
                } else {
                    args.unexpectedArgument("tomcat --local deploy");
                }
            }
        }
        loadApp(app).deploy(version);
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
            } else if ((a = args.readBooleanOption("--delete-out-log")) != null) {
                deleteLog = a.getBooleanValue();
            } else if ((a = args.readStringOption("--deploy")) != null) {
                apps.add(a.getValue());
            } else {
                if (instance == null) {
                    instance = args.readNonOption().getExpression();
                } else {
                    args.unexpectedArgument("tomcat --local restart");
                }
            }
        }
        LocalTomcatConfigService c = loadTomcatConfig(instance);
        if (shutdown) {
            c.restart(apps.toArray(new String[0]), deleteLog);
        } else {
            c.start(apps.toArray(new String[0]), deleteLog);
        }
        return 0;
    }

    public int reset() {
        for (LocalTomcatConfigService tomcatConfig : listConfig()) {
            tomcatConfig.remove();
        }
        return 0;
    }


    public LocalTomcatConfigService[] listConfig() {
        List<LocalTomcatConfigService> all = new ArrayList<>();
        File[] configFiles = new File(getContext().getConfigFolder()).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(LocalTomcatConfigService.LOCAL_CONFIG_EXT);
            }
        });
        if (configFiles != null) {
            for (File file1 : configFiles) {
                try {
                    LocalTomcatConfigService c = loadTomcatConfig(file1);
                    all.add(c);
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        return all.toArray(new LocalTomcatConfigService[0]);
    }


    public LocalTomcatConfigService loadTomcatConfig(String name) {
        LocalTomcatConfigService t = new LocalTomcatConfigService(name, this);
        t.loadConfig();
        return t;
    }

    public LocalTomcatConfigService loadTomcatConfig(File file) {
        LocalTomcatConfigService t = new LocalTomcatConfigService(file, this);
        t.loadConfig();
        return t;
    }

    public LocalTomcatConfigService createTomcatConfig(String name) {
        LocalTomcatConfigService t = new LocalTomcatConfigService(name, this);
        t.setConfig(new LocalTomcatConfig());
        return t;
    }

    public LocalTomcatServiceBase loadServiceBase(String name) {
        String[] strings = TomcatUtils.splitInstanceAppPreferInstance(name);
        if (strings[1].isEmpty()) {
            return loadOrCreateTomcatConfig(strings[0]);
        } else {
            LocalTomcatConfigService u = loadOrCreateTomcatConfig(strings[0]);
            LocalTomcatDomainConfigService d = u.getDomainOrNull(strings[1]);
            LocalTomcatAppConfigService a = u.getAppOrNull(strings[1]);
            if (d != null && a != null) {
                throw new NutsExecutionException("Ambiguous name " + name + ". Could be either domain or app", 3);
            }
            if (d == null && a == null) {
                throw new NutsExecutionException("Unknown name " + name + ". it is no domain or app", 3);
            }
            if (d != null) {
                return d;
            }
            return a;
        }
    }

    public LocalTomcatAppConfigService loadApp(String name) {
        String[] strings = TomcatUtils.splitInstanceAppPreferApp(name);
        return loadOrCreateTomcatConfig(strings[0]).getApp(strings[1]);
    }

    public LocalTomcatDomainConfigService loadDomain(String name) {
        String[] strings = TomcatUtils.splitInstanceAppPreferApp(name);
        return loadOrCreateTomcatConfig(strings[0]).getDomain(strings[1]);
    }

    public LocalTomcatConfigService loadOrCreateTomcatConfig(String name) {
        LocalTomcatConfigService t = new LocalTomcatConfigService(name, this);
        if (t.existsConfig()) {
            t.loadConfig();
        } else {
            t.setConfig(new LocalTomcatConfig());
        }
        return t;
    }

    public NutsApplicationContext getContext() {
        return context;
    }

    public void setContext(NutsApplicationContext context) {
        this.context = context;
    }

    public LocalTomcatConfigService toLocalTomcatConfigService(LocalTomcatServiceBase s) {
        if (s instanceof LocalTomcatAppConfigService) {
            s = ((LocalTomcatAppConfigService) s).getTomcat();
        } else if (s instanceof LocalTomcatDomainConfigService) {
            s = ((LocalTomcatDomainConfigService) s).getTomcat();
        }
        return ((LocalTomcatConfigService) s);
    }

    public LocalTomcatConfigService readTomcatServiceArg(CommandLine args) {
        LocalTomcatServiceBase s = readBaseServiceArg(args);
        return toLocalTomcatConfigService(s);
    }

    public LocalTomcatServiceBase readBaseServiceArg(CommandLine args) {
        Argument a;
        if ((a = args.readStringOption("--instance")) != null) {
            return (loadOrCreateTomcatConfig(a.getStringValue()));
        } else if ((a = args.readStringOption("--app")) != null) {
            return (loadApp(a.getStringValue()));
        } else if ((a = args.readStringOption("--domain")) != null) {
            return (loadDomain(a.getStringValue()));
        } else if (args.isOption()) {
            return null;
        } else {
            return (loadServiceBase(args.read().getExpression()));
        }
    }
}
