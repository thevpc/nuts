package net.vpc.toolbox.tomcat.local;

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.toolbox.tomcat.local.config.LocalTomcatConfig;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsArgument;

public class LocalTomcat {

    private NutsApplicationContext context;
    private NutsCommandLine cmdLine;

    public LocalTomcat(NutsApplicationContext ws,NutsCommandLine cmdLine) {
        this.setContext(ws);
        this.cmdLine=cmdLine;
    }

    public void runArgs() {
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if (cmdLine.get().isOption()) {
                if (context.configure(cmdLine)) {
                    //
                } else {
                    cmdLine.setCommandName("tomcat --local").unexpectedArgument();
                }
            } else {
                a = cmdLine.readNonOption();
                switch (a.getString()) {
                    case "list":
                        list(cmdLine);
                        return;
                    case "show":
                    case "describe":
                        show(cmdLine);
                        return;
                    case "show-property":
                        showProperty(cmdLine);
                        return;
                    case "add":
                    case "set":
                        add(cmdLine);
                        return;
                    case "remove":
                        remove(cmdLine);
                        return;
                    case "start":
                        restart(cmdLine, false);
                        return;
                    case "stop":
                        stop(cmdLine);
                        return;
                    case "status":
                        status(cmdLine);
                        return;
                    case "restart":
                        restart(cmdLine, true);
                        return;
                    case "install":
                        install(cmdLine);
                        return;
                    case "reset":
                        reset();
                        return;
                    case "deploy":
                        deployApp(cmdLine);
                        return;
                    case "deploy-file":
                        deployFile(cmdLine);
                        return;
                    case "delete-log":
                        deleteLog(cmdLine);
                        return;
                    case "delete-temp":
                        deleteTemp(cmdLine);
                        return;
                    case "delete-work":
                        deleteWork(cmdLine);
                        return;
                    case "show-log":
                        showLog(cmdLine);
                        return;
                    default:
                        throw new NutsExecutionException("Unsupported action " + a.getString(), 1);
                }
            }
        }
                        throw new NutsExecutionException("Missing tomcat command. Type help", 1);
    }

    public void list(NutsCommandLine args) {
        NutsArgument a;
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
                //
            } else if ((a = args.readBooleanOption("--apps")) != null) {
                x.apps = a.getBooleanValue();
            } else if ((a = args.readBooleanOption("--domains")) != null) {
                x.domains = a.getBooleanValue();
            } else if ((a = args.readStringOption("--name")) != null) {
                x.print(loadOrCreateTomcatConfig(a.getValue().getString()));
            } else {
                x.print(loadOrCreateTomcatConfig(args.readRequiredNonOption().getValue().getString()));
            }
        }
        if (!x.processed) {
            for (LocalTomcatConfigService tomcatConfig : listConfig()) {
                getContext().out().println(tomcatConfig.getName());
            }
        }
    }

    public void show(NutsCommandLine args) {
        NutsArgument a;
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
                //
            } else if ((a = args.readBooleanOption("--json")) != null) {
                h.json = a.getBooleanValue();
            } else if ((s = readBaseServiceArg(args)) != null) {
                h.show(s);
            } else {
                args.setCommandName("tomcat --local show").unexpectedArgument();
            }
        }
    }

    public void showProperty(NutsCommandLine args) {
        NutsArgument a;
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
                        Object o = context.getWorkspace().parser().parseExpression(s.getConfig(), prop);
                        m.put(new Item(prop), o);
                    }
                    props.clear();
                }
            }
        }
        PropsHelper x = new PropsHelper();
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else if (x.s == null && (x.s = readBaseServiceArg(args)) != null) {
                //ok
            } else if ((a = args.readStringOption("--property")) != null) {
                x.addProp(a.getValue().getString());
            } else if (args.get().isOption()) {
                args.setCommandName("tomcat --local show-property").unexpectedArgument();
            } else {
                x.addProp(args.read().getString());
            }
        }
        x.build();
        if (x.m.isEmpty()) {
            throw new NutsExecutionException("No properties to show", 2);
        }
        if (x.m.size() == 1) {
            for (Object value : x.m.values()) {
                context.out().printf("%s\n", String.valueOf(value));
                break;
            }
        } else {
            getContext().getWorkspace().formatter().createPropertiesFormat().format(x.m, context.out());
        }
    }

    public void add(NutsCommandLine args) {
        LocalTomcatConfigService c = null;
        String appName = null;
        String domainName = null;
        String instance = null;

        NutsArgument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else if ((a = args.readStringOption("--name")) != null) {
                instance = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(instance);
                } else {
                    throw new NutsExecutionException("Instance name already defined", 1);
                }
            } else if ((a = args.readStringOption("--catalina-version")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setCatalinaVersion(a.getValue().getString());
            } else if ((a = args.readStringOption("--catalina-base")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setCatalinaBase(a.getValue().getString());
            } else if ((a = args.readStringOption("--catalina-home")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setCatalinaHome(a.getValue().getString());
            } else if ((a = args.readStringOption("--shutdown-wait-time")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setShutdownWaitTime(a.getValue().getInt());
            } else if ((a = args.readStringOption("--app")) != null) {
                appName = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getAppOrCreate(appName);
            } else if ((a = args.readStringOption("--domain")) != null) {
                domainName = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getDomainOrCreate(domainName);
            } else if ((a = args.readStringOption("--domain.log")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getDomainOrCreate(domainName).getConfig().setLogFile(a.getValue().getString());

            } else if ((a = args.readStringOption("--app.source")) != null) {
                String value = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                LocalTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                if (tomcatAppConfig == null) {
                    throw new NutsExecutionException("Missing --app.source", 2);
                }
                tomcatAppConfig.getConfig().setSourceFilePath(value);
            } else if ((a = args.readStringOption("--app.deploy")) != null) {
                String value = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                LocalTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setDeployName(value);
            } else if ((a = args.readStringOption("--app.domain")) != null) {
                String value = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                LocalTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setDomain(value);
            } else if ((a = args.readStringOption("--archive-folder")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setArchiveFolder(a.getValue().getString());
            } else if ((a = args.readStringOption("--running-folder")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRunningFolder(a.getValue().getString());
            } else {
                args.setCommandName("tomcat --local add").unexpectedArgument();
            }
        }
        if (c == null) {
            c = loadOrCreateTomcatConfig(null);
        }
        if (c != null) {
            c.save();
            //just check it is installed!!
            c.getCatalinaBase();
        }
    }

    public void remove(NutsCommandLine args) {
        LocalTomcatServiceBase s = null;
        NutsArgument a;
        boolean processed = false;
        int lastExitCode = 0;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else if ((s = readBaseServiceArg(args)) != null) {
                s.remove();
                if (!(s instanceof LocalTomcatConfigService)) {
                    toLocalTomcatConfigService(s).save();
                }
                processed = true;
                lastExitCode = 0;
            } else {
                args.setCommandName("tomcat --local remove").unexpectedArgument();
            }
        }
        if (!processed) {
            throw new NutsExecutionException("tomcat --local remove: Invalid parameters", 2);
        }
        if (lastExitCode != 0) {
            throw new NutsExecutionException(lastExitCode);
        }
    }

    public void stop(NutsCommandLine args) {
        LocalTomcatConfigService s = null;
        NutsArgument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else if ((s = readTomcatServiceArg(args)) != null) {
                if (!s.stop()) {
                    throw new NutsExecutionException("Unable to stop", 1);
                }
                return;
            } else {
                args.setCommandName("tomcat --local stop").unexpectedArgument();
            }
        }
        LocalTomcatConfigService c = loadTomcatConfig("");
        if (!c.stop()) {
            throw new NutsExecutionException("Unable to stop", 1);
        }
    }

    public void status(NutsCommandLine args) {
        LocalTomcatConfigService s = null;
        NutsArgument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else if ((s = readTomcatServiceArg(args)) != null) {
                s.printStatus();
                return;
            } else {
                args.setCommandName("tomcat --local status").unexpectedArgument();
            }
        }
        LocalTomcatConfigService c = loadTomcatConfig("");
        c.printStatus();
    }

    public void install(NutsCommandLine args) {
        LocalTomcatAppConfigService app = null;
        String version = null;
        String file = null;
        NutsArgument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else if ((a = args.readStringOption("--app")) != null) {
                app = loadApp(a.getValue().getString());
            } else if ((a = args.readStringOption("--version")) != null) {
                version = a.getValue().getString();
            } else if ((a = args.readStringOption("--file")) != null) {
                file = a.getValue().getString();
            } else {
                if (file == null) {
                    file = args.readNonOption().getString();
                } else {
                    args.setCommandName("tomcat --local install").unexpectedArgument();
                }
            }
        }
        if (app == null) {
            throw new NutsExecutionException("tomcat install: Missing Application", 2);
        }
        if (file == null) {
            throw new NutsExecutionException("tomcat install: Missing File", 2);
        }
        app.install(version, file, true);
    }

    public void deleteLog(NutsCommandLine args) {
        LocalTomcatServiceBase s = null;
        boolean all = false;
        NutsArgument a;
        boolean processed = false;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
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
                args.setCommandName("tomcat --local delete-log").unexpectedArgument();
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
    }

    public void deleteTemp(NutsCommandLine args) {
        LocalTomcatServiceBase s = null;
        NutsArgument a;
        boolean processed = false;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else if ((s = readBaseServiceArg(args)) != null) {
                LocalTomcatConfigService c = toLocalTomcatConfigService(s);
                c.deleteTemp();
                processed = true;
            } else {
                args.setCommandName("tomcat --local delete-temp").unexpectedArgument();
            }
        }
        if (!processed) {
            LocalTomcatConfigService c = loadTomcatConfig("");
            c.deleteTemp();
        }
    }

    public void deleteWork(NutsCommandLine args) {
        LocalTomcatServiceBase s = null;
        NutsArgument a;
        boolean processed = false;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else if ((s = readBaseServiceArg(args)) != null) {
                LocalTomcatConfigService c = toLocalTomcatConfigService(s);
                c.deleteWork();
                processed = true;
            } else {
                args.setCommandName("tomcat --local delete-work").unexpectedArgument();
            }
        }
        if (!processed) {
            LocalTomcatConfigService c = loadTomcatConfig("");
            c.deleteWork();
        }
    }

    public void showLog(NutsCommandLine args) {
        LocalTomcatServiceBase s = null;
        boolean processed = false;
        String instance = null;
        String domain = null;
        boolean path = false;
        boolean all = false;
        int count = -1;
        NutsArgument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else if ((s = readBaseServiceArg(args)) != null) {
                LocalTomcatConfigService c = toLocalTomcatConfigService(s);
                if (path) {
                    getContext().out().printf("%s\n", c.getOutLogFile());
                } else {
                    c.showOutLog(count);
                }
                processed = true;
            } else if ((a = args.readStringOption("--path")) != null) {
                path = true;
            } else if (args.get().isOption() && TomcatUtils.isPositiveInt(args.get().getString().substring(1))) {
                count = Integer.parseInt(args.read().getString().substring(1));
            } else {
                args.setCommandName("tomcat --local show-log").unexpectedArgument();
            }
        }
        if (!processed) {
            LocalTomcatConfigService c = loadTomcatConfig("");
            if (path) {
                getContext().out().printf("%s\n", c.getOutLogFile());
            } else {
                c.showOutLog(count);
            }
        }
    }

    public void deployFile(NutsCommandLine args) {
        String instance = null;
        String version = null;
        String file = null;
        String app = null;
        String domain = null;
        String contextName = null;
        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--file")) != null) {
                file = a.getValue().getString();
            } else if ((a = args.readStringOption("--name")) != null) {
                instance = a.getValue().getString();
            } else if ((a = args.readStringOption("--context")) != null) {
                contextName = a.getValue().getString();
            } else if ((a = args.readStringOption("--domain")) != null) {
                domain = a.getValue().getString();
            } else {
                if (file == null) {
                    file = args.readNonOption().getString();
                } else {
                    args.setCommandName("tomcat --local deploy-file").unexpectedArgument();
                }
            }
        }
        if (file == null) {
            throw new NutsExecutionException("tomcat deploy: Missing File", 2);
        }
        LocalTomcatConfigService c = loadTomcatConfig(instance);
        c.deployFile(getContext().getWorkspace().io().path(file), contextName, domain);
    }

    public void deployApp(NutsCommandLine args) {
        String version = null;
        String app = null;
        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--version")) != null) {
                version = a.getValue().getString();
            } else if ((a = args.readStringOption("--app")) != null) {
                app = a.getValue().getString();
            } else {
                if (app == null) {
                    app = args.readNonOption().getString();
                } else {
                    args.setCommandName("tomcat --local deploy").unexpectedArgument();
                }
            }
        }
        loadApp(app).deploy(version);
    }

    public void restart(NutsCommandLine args, boolean shutdown) {
        boolean deleteLog = false;
        String instance = null;
        List<String> apps = new ArrayList<>();
        while (args.hasNext()) {
            NutsArgument a = null;
            if ((a = args.readStringOption("--name")) != null) {
                instance = a.getValue().getString();
            } else if ((a = args.readBooleanOption("--delete-out-log")) != null) {
                deleteLog = a.getBooleanValue();
            } else if ((a = args.readStringOption("--deploy")) != null) {
                apps.add(a.getValue().getString());
            } else {
                if (instance == null) {
                    instance = args.readNonOption().getString();
                } else {
                    args.setCommandName("tomcat --local restart").unexpectedArgument();
                }
            }
        }
        LocalTomcatConfigService c = loadTomcatConfig(instance);
        if (shutdown) {
            c.restart(apps.toArray(new String[0]), deleteLog);
        } else {
            c.start(apps.toArray(new String[0]), deleteLog);
        }
    }

    public void reset() {
        for (LocalTomcatConfigService tomcatConfig : listConfig()) {
            tomcatConfig.remove();
        }
    }

    public LocalTomcatConfigService[] listConfig() {
        List<LocalTomcatConfigService> all = new ArrayList<>();
        try (DirectoryStream<Path> pp = Files.newDirectoryStream(getContext().getConfigFolder(),
                (Path entry) -> entry.getFileName().toString().endsWith(LocalTomcatConfigService.LOCAL_CONFIG_EXT))) {
            for (Path entry : pp) {
                try {
                    LocalTomcatConfigService c = loadTomcatConfig(entry);
                    all.add(c);
                } catch (Exception ex) {
                    //ignore
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return all.toArray(new LocalTomcatConfigService[0]);
    }

    public LocalTomcatConfigService loadTomcatConfig(String name) {
        LocalTomcatConfigService t = new LocalTomcatConfigService(name, this);
        t.loadConfig();
        return t;
    }

    public LocalTomcatConfigService loadTomcatConfig(Path file) {
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

    public LocalTomcatConfigService readTomcatServiceArg(NutsCommandLine args) {
        LocalTomcatServiceBase s = readBaseServiceArg(args);
        return toLocalTomcatConfigService(s);
    }

    public LocalTomcatServiceBase readBaseServiceArg(NutsCommandLine args) {
        NutsArgument a;
        if ((a = args.readStringOption("--name")) != null) {
            return (loadOrCreateTomcatConfig(a.getValue().getString()));
        } else if ((a = args.readStringOption("--app")) != null) {
            return (loadApp(a.getValue().getString()));
        } else if ((a = args.readStringOption("--domain")) != null) {
            return (loadDomain(a.getValue().getString()));
        } else if (args.hasNext() && args.get().isOption()) {
            return null;
        } else {
            return (loadServiceBase(args.read().getString()));
        }
    }
}
