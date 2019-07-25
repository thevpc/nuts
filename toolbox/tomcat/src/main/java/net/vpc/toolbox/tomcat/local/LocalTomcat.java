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
import java.util.List;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;

public class LocalTomcat {

    private NutsApplicationContext context;
    private NutsCommandLine cmdLine;

    public LocalTomcat(NutsApplicationContext ws, NutsCommandLine cmdLine) {
        this.setContext(ws);
        this.cmdLine = cmdLine;
    }

    public void runArgs() {
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if (cmdLine.peek().isOption()) {
                if (context.configureFirst(cmdLine)) {
                    //
                } else {
                    cmdLine.setCommandName("tomcat --local").unexpectedArgument();
                }
            } else {
                a = cmdLine.requireNonOption().next();
                switch (a.getString()) {
                    case "list":
                        list(cmdLine);
                        return;
                    case "show":
                    case "describe":
                        show(cmdLine);
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
                    case "get-port":
                    case "show-port":
                        showPort(cmdLine);
                        return;
                    case "set-port":
                        setPort(cmdLine);
                        return;
                    default:
                        throw new NutsExecutionException(context.getWorkspace(), "Unsupported action " + a.getString(), 1);
                }
            }
        }
        throw new NutsExecutionException(context.getWorkspace(), "Missing tomcat action. Type: nuts tomcat --help", 1);
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
                            context.session().out().printf("[[\\[%s\\]]]:\n", "Apps");
                        }
                        for (LocalTomcatAppConfigService app : apps) {
                            context.session().out().printf("%s\n", app.getName());
                        }
                    }
                }
                if (isDomains()) {
                    List<LocalTomcatDomainConfigService> domains = c.getDomains();
                    if (!domains.isEmpty()) {
                        if (isHeader()) {
                            context.session().out().printf("[[\\[%s\\]]]:\n", "Domains");
                        }
                        for (LocalTomcatDomainConfigService app : domains) {
                            context.session().out().printf("%s\n", app.getName());
                        }
                    }
                }
            }
        }
        Helper x = new Helper();
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else if ((a = args.nextBoolean("--apps")) != null) {
                x.apps = a.getBooleanValue();
            } else if ((a = args.nextBoolean("--domains")) != null) {
                x.domains = a.getBooleanValue();
            } else if ((a = args.nextString("--name")) != null) {
                x.print(loadOrCreateTomcatConfig(a.getStringValue()));
            } else {
                x.print(loadOrCreateTomcatConfig(args.requireNonOption().next().getStringValue()));
            }
        }
        if (!x.processed) {
            for (LocalTomcatConfigService tomcatConfig : listConfig()) {
                getContext().session().out().println(tomcatConfig.getName());
            }
        }
    }

    public void show(NutsCommandLine args) {
        NutsArgument a;
        LocalTomcatServiceBase s;
        List<LocalTomcatServiceBase> toShow = new ArrayList<>();
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else if ((s = readBaseServiceArg(args)) != null) {
                toShow.add(s);
            } else {
                args.setCommandName("tomcat --local show").unexpectedArgument();
            }
        }
        if (args.isExecMode()) {
            if (toShow.isEmpty()) {
                toShow.add(loadServiceBase(""));
            }
            for (LocalTomcatServiceBase s2 : toShow) {
                getContext().session().out().printf("[[%s]] :\n", s2.getName());
                s2.write(getContext().session().out());
                getContext().session().out().println();
            }
        }
    }

    public void add(NutsCommandLine args) {
        LocalTomcatConfigService c = null;
        String appName = null;
        String domainName = null;
        String instance = null;

        NutsArgument a;
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else if ((a = args.nextString("--name")) != null) {
                instance = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(instance);
                } else {
                    throw new NutsExecutionException(context.getWorkspace(), "Instance name already defined", 1);
                }
            } else if ((a = args.nextString("--catalina-version")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setCatalinaVersion(a.getStringValue());
            } else if ((a = args.nextString("--catalina-base")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setCatalinaBase(a.getStringValue());
            } else if ((a = args.nextString("--catalina-home")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setCatalinaHome(a.getStringValue());
            } else if ((a = args.nextString("--shutdown-wait-time")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setShutdownWaitTime(a.getArgumentValue().getInt());
            } else if ((a = args.nextString("--app")) != null) {
                appName = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getAppOrCreate(appName);
            } else if ((a = args.nextString("--domain")) != null) {
                domainName = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getDomainOrCreate(domainName);
            } else if ((a = args.nextString("--domain.log")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getDomainOrCreate(domainName).getConfig().setLogFile(a.getStringValue());

            } else if ((a = args.nextString("--app.source")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                LocalTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                if (tomcatAppConfig == null) {
                    throw new NutsExecutionException(context.getWorkspace(), "Missing --app.source", 2);
                }
                tomcatAppConfig.getConfig().setSourceFilePath(value);
            } else if ((a = args.nextString("--app.deploy")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                LocalTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setDeployName(value);
            } else if ((a = args.nextString("--app.domain")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                LocalTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setDomain(value);
            } else if ((a = args.nextString("--archive-folder")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setArchiveFolder(a.getStringValue());
            } else if ((a = args.nextString("--running-folder")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRunningFolder(a.getStringValue());
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
            if (context.configureFirst(args)) {
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
            throw new NutsExecutionException(context.getWorkspace(), "tomcat --local remove: Invalid parameters", 2);
        }
        if (lastExitCode != 0) {
            throw new NutsExecutionException(context.getWorkspace(), lastExitCode);
        }
    }

    public void stop(NutsCommandLine args) {
        LocalTomcatConfigService s = null;
        NutsArgument a;
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else if ((s = readTomcatServiceArg(args)) != null) {
                if (!s.stop()) {
                    throw new NutsExecutionException(context.getWorkspace(), "Unable to stop", 1);
                }
                return;
            } else {
                args.setCommandName("tomcat --local stop").unexpectedArgument();
            }
        }
        LocalTomcatConfigService c = loadTomcatConfig("");
        if (!c.stop()) {
            throw new NutsExecutionException(context.getWorkspace(), "Unable to stop", 1);
        }
    }

    public void status(NutsCommandLine args) {
        LocalTomcatConfigService s = null;
        NutsArgument a;
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
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
            if (context.configureFirst(args)) {
                //
            } else if ((a = args.nextString("--app")) != null) {
                app = loadApp(a.getStringValue());
            } else if ((a = args.nextString("--version")) != null) {
                version = a.getStringValue();
            } else if ((a = args.nextString("--file")) != null) {
                file = a.getStringValue();
            } else {
                if (file == null) {
                    file = args.requireNonOption().next().getString();
                } else {
                    args.setCommandName("tomcat --local install").unexpectedArgument();
                }
            }
        }
        if (app == null) {
            throw new NutsExecutionException(context.getWorkspace(), "tomcat install: Missing Application", 2);
        }
        if (file == null) {
            throw new NutsExecutionException(context.getWorkspace(), "tomcat install: Missing File", 2);
        }
        app.install(version, file, true);
    }

    public void deleteLog(NutsCommandLine args) {
        LocalTomcatServiceBase s = null;
        boolean all = false;
        NutsArgument a;
        boolean processed = false;
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else if ((a = args.nextBoolean("-a", "--all")) != null) {
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
            if (context.configureFirst(args)) {
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
            if (context.configureFirst(args)) {
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

    public void showPort(NutsCommandLine args) {
        args.setCommandName("tomcat --local show-port");
        LocalTomcatServiceBase s = null;
        NutsArgument a;
        boolean redirect = false;
        boolean shutdown = false;
        boolean ajp = false;
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else if (s == null && (s = readBaseServiceArg(args)) != null) {
                //good
            } else if ((a = args.nextBoolean("--redirect")) != null) {
                redirect = a.getBooleanValue();
            } else if ((a = args.nextBoolean("--shutdown")) != null) {
                shutdown = a.getBooleanValue();
            } else if ((a = args.nextBoolean("--ajp")) != null) {
                ajp = a.getBooleanValue();
            } else {
                args.unexpectedArgument();
            }
        }
        if (s == null) {
            s = loadServiceBase("");
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        Integer port = null;
        if (shutdown) {
            port = c.getShutdownPort();
        } else if (ajp) {
            port = c.getConnectorPort("AJP/1.3", redirect);
        } else {
            port = c.getConnectorPort("HTTP/1.1", redirect);
        }
        context.workspace().object().session(context.session())
                .value(port)
                .println();
    }

    public void setPort(NutsCommandLine args) {
        args.setCommandName("tomcat --local set-port");
        LocalTomcatServiceBase s = null;
        NutsArgument a;
        String type="http";
        boolean redirect = false;
//        boolean shutdown = false;
//        boolean ajp = false;
        Integer port = null;
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else if (s == null && (s = readBaseServiceArg(args)) != null) {
                //good
            } else if ((a = args.nextBoolean("--redirect")) != null) {
                redirect = a.getBooleanValue();
            } else if ((a = args.nextBoolean("--shutdown")) != null) {
                type = a.getBooleanValue()?"shutdown":"http";
            } else if ((a = args.nextBoolean("--ajp")) != null) {
                type = a.getBooleanValue()?"ajp":"http";
            } else if ((a = args.nextBoolean("--http")) != null) {
                type = a.getBooleanValue()?"http":"http";
            } else if (port == null && args.peek().isInt()) {
                port = args.next().getInt();
            } else {
                args.unexpectedArgument();
            }
        }
        if (s == null) {
            s = loadServiceBase("");
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        switch (type){
            case "shutdown":{
                if (port == null) {
                    port = 8005;
                }
                c.setShutdownPort(port);
                break;
            }
            case "ajp":{
                if (port == null) {
                    port = redirect ? 8443 : 8009;
                }
                c.setConnectorPort("AJP/1.3", redirect, port);
                break;
            }
            case "http":{
                if (port == null) {
                    port = redirect ? 8080 : 8443;
                }
                c.setConnectorPort("HTTP/1.1", redirect, port);
                break;
            }
        }
    }

    public void showLog(NutsCommandLine args) {
        LocalTomcatServiceBase s = null;
        boolean path = false;
        int count = -1;
        NutsArgument a;
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else if (s == null && (s = readBaseServiceArg(args)) != null) {
                //good
            } else if ((a = args.nextString("--path")) != null) {
                path = true;
            } else if (args.peek().isOption() && TomcatUtils.isPositiveInt(args.peek().getString().substring(1))) {
                count = Integer.parseInt(args.next().getString().substring(1));
            } else {
                args.setCommandName("tomcat --local show-log").unexpectedArgument();
            }
        }
        if (s == null) {
            args.required();
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        if (path) {
            getContext().session().out().printf("%s\n", c.getOutLogFile());
        } else {
            c.showOutLog(count);
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
            if ((a = args.nextString("--file")) != null) {
                file = a.getStringValue();
            } else if ((a = args.nextString("--name")) != null) {
                instance = a.getStringValue();
            } else if ((a = args.nextString("--context")) != null) {
                contextName = a.getStringValue();
            } else if ((a = args.nextString("--domain")) != null) {
                domain = a.getStringValue();
            } else {
                if (file == null) {
                    file = args.requireNonOption().next().getString();
                } else {
                    args.setCommandName("tomcat --local deploy-file").unexpectedArgument();
                }
            }
        }
        if (file == null) {
            throw new NutsExecutionException(context.getWorkspace(), "tomcat deploy: Missing File", 2);
        }
        LocalTomcatConfigService c = loadTomcatConfig(instance);
        c.deployFile(getContext().getWorkspace().io().path(file), contextName, domain);
    }

    public void deployApp(NutsCommandLine args) {
        String version = null;
        String app = null;
        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.nextString("--version")) != null) {
                version = a.getStringValue();
            } else if ((a = args.nextString("--app")) != null) {
                app = a.getStringValue();
            } else {
                if (app == null) {
                    app = args.requireNonOption().next().getString();
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
            if ((a = args.nextString("--name")) != null) {
                instance = a.getStringValue();
            } else if ((a = args.nextBoolean("--delete-out-log")) != null) {
                deleteLog = a.getBooleanValue();
            } else if ((a = args.nextString("--deploy")) != null) {
                apps.add(a.getStringValue());
            } else {
                if (instance == null) {
                    instance = args.requireNonOption().next().getString();
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
        try (DirectoryStream<Path> pp = Files.newDirectoryStream(getContext().getSharedConfigFolder(),
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
                throw new NutsExecutionException(context.getWorkspace(), "Ambiguous name " + name + ". Could be either domain or app", 3);
            }
            if (d == null && a == null) {
                throw new NutsExecutionException(context.getWorkspace(), "Unknown name " + name + ". it is no domain or app", 3);
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
        if ((a = args.nextString("--name")) != null) {
            return (loadServiceBase(a.getStringValue()));
        } else if ((a = args.nextString("--app")) != null) {
            return (loadApp(a.getStringValue()));
        } else if ((a = args.nextString("--domain")) != null) {
            return (loadDomain(a.getStringValue()));
        } else if (args.hasNext() && args.peek().isOption() && args.peek().isDouble()) {
            return null;
        } else {
            return (loadServiceBase(args.next().getString()));
        }
    }
}
