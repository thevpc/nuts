package net.vpc.toolbox.tomcat.remote;

import net.vpc.app.nuts.*;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.tomcat.remote.config.RemoteTomcatConfig;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.vpc.app.nuts.NutsCommand;

public class RemoteTomcat {

    public NutsApplicationContext context;
    public NutsCommand cmdLine;

    public RemoteTomcat(NutsApplicationContext ws, NutsCommand cmdLine) {
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
                    cmdLine.setCommandName("tomcat --remote").unexpectedArgument();
                }
            } else {
                if ((a = cmdLine.next("list")) != null) {
                    list(cmdLine);
                    return;
                } else if ((a = cmdLine.next("show")) != null) {
                    show(cmdLine);
                    return;
                } else if ((a = cmdLine.next("show-property")) != null) {
                    showProperty(cmdLine);
                    return;
                } else if ((a = cmdLine.next("add", "set")) != null) {
                    add(cmdLine);
                    return;
                } else if ((a = cmdLine.next("remove")) != null) {
                    remove(cmdLine);
                    return;
                } else if ((a = cmdLine.next("start")) != null) {
                    restart(cmdLine, false);
                    return;
                } else if ((a = cmdLine.next("restart")) != null) {
                    restart(cmdLine, true);
                    return;
                } else if ((a = cmdLine.next("stop")) != null) {
                    stop(cmdLine);
                    return;
                } else if ((a = cmdLine.next("install")) != null) {
                    install(cmdLine);
                    return;
                } else if ((a = cmdLine.next("deploy")) != null) {
                    deploy(cmdLine);
                    return;
                } else if ((a = cmdLine.next("reset")) != null) {
                    reset(cmdLine);
                    return;
                } else {
                    cmdLine.setCommandName("tomcat --remote").unexpectedArgument();
                }
            }
        }
    }

    public void list(NutsCommand args) {
        NutsArgument a;
        class Helper {

            boolean processed = false;

            void print(RemoteTomcatConfigService c) {
                processed = true;
                List<RemoteTomcatAppConfigService> apps = c.getApps();
                for (RemoteTomcatAppConfigService app : apps) {
                    context.out().printf("%s\n", app.getName());
                }
            }
        }
        Helper x = new Helper();
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else if ((a = args.nextString("--name")) != null) {
                x.print(loadOrCreateTomcatConfig(a.getValue().getString()));
            } else {
                x.print(loadOrCreateTomcatConfig(args.requireNonOption().next().getValue().getString()));
            }
        }
        if (!x.processed) {
            for (RemoteTomcatConfigService tomcatConfig : listConfig()) {
                getContext().out().println(tomcatConfig.getName());
            }
        }
    }

    private void add(NutsCommand args) {
        RemoteTomcatConfigService c = null;
        String appName = null;
        String instanceName = null;
        NutsArgument a;
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else if ((a = args.nextString("--name")) != null) {
                if (c == null) {
                    instanceName = a.getValue().getString();
                    c = loadOrCreateTomcatConfig(instanceName);
                } else {
                    throw new NutsExecutionException(context.getWorkspace(), "instance already defined", 2);
                }
            } else if ((a = args.nextString("--server")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setServer(a.getValue().getString());
            } else if ((a = args.nextString("--remote-temp-path")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRemoteTempPath(a.getValue().getString());
            } else if ((a = args.nextString("--remote-instance")) != null) {
                String value = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRemoteName(value);
            } else if ((a = args.nextString("--app")) != null) {
                appName = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getAppOrCreate(appName);
            } else if ((a = args.nextString("--app.path")) != null) {
                String value = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                RemoteTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setPath(value);
            } else if ((a = args.nextString("--app.version")) != null) {
                String value = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                RemoteTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setVersionCommand(value);
            } else {
                args.setCommandName("tomcat --remote add").unexpectedArgument();
            }
        }
        if (c == null) {
            c = loadOrCreateTomcatConfig(null);
        }
        boolean ok = false;
        while (!ok) {
            try {
                ok = true;
                if (TomcatUtils.isBlank(c.getConfig().getServer())) {
                    ok = false;
                    c.getConfig().setServer(
                            context.terminal()
                                    .ask().forString("[instance=[[%s]]] Would you enter ==%s== value?", c.getName(), "--server")
                                    .defaultValue("ssh://login@myserver/instanceName").session(context.getSession())
                                    .getResult()
                    );
                }
                if (TomcatUtils.isBlank(c.getConfig().getRemoteTempPath())) {
                    ok = false;
                    c.getConfig()
                            .setRemoteTempPath(context.terminal().ask()
                                    .forString("[instance=[[%s]]] Would you enter ==%s== value?", c.getName(), "--remote-temp-path").setDefaultValue("/tmp")
                                    .session(context.getSession())
                                    .getResult()
                            );
                }
                for (RemoteTomcatAppConfigService aa : c.getApps()) {
                    if (TomcatUtils.isBlank(aa.getConfig().getPath())) {
                        ok = false;
                        aa.getConfig().setPath(context.terminal().ask()
                                .forString("[instance=[[%s]]] [app=[[%s]]] Would you enter ==%s== value?", c.getName(), aa.getName(), "-app.path")
                                .session(context.getSession())
                                .getResult());
                    }
                }
            } catch (NutsUserCancelException ex) {
                throw new NutsExecutionException(context.getWorkspace(), "Cancelled", 1);
            }
        }
        c.save();
    }

    public void remove(NutsCommand args) {
        RemoteTomcatServiceBase s = null;
        NutsArgument a;
        boolean processed = false;
        int lastExitCode = 0;
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else if ((s = readBaseServiceArg(args)) != null) {
                s.remove();
                if (!(s instanceof RemoteTomcatConfigService)) {
                    toRemoteTomcatConfigService(s).save();
                }
                processed = true;
                lastExitCode = 0;
            } else {
                args.setCommandName("tomcat --remote remove").unexpectedArgument();
            }
        }
        if (!processed) {
            throw new NutsExecutionException(context.getWorkspace(), "Invalid parameters", 2);
        }
        if (lastExitCode != 0) {
            throw new NutsExecutionException(context.getWorkspace(), lastExitCode);
        }
    }

    private void install(NutsCommand args) {
        String conf = null;
        String app = null;
        NutsArgument a;
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else if ((a = args.nextString("--app")) != null) {
                loadApp(a.getValue().getString()).install();
            } else {
                args.setCommandName("tomcat --remote install").unexpectedArgument();
            }
        }
    }

    private void deploy(NutsCommand args) {
        String app = null;
        String version = null;
        NutsArgument a;
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else if ((a = args.nextString("--app")) != null) {
                app = a.getValue().getString();
            } else if ((a = args.nextString("--version")) != null) {
                version = a.getValue().getString();
            } else {
                args.setCommandName("tomcat --remote deploy").unexpectedArgument();
            }
        }
        loadApp(app).deploy(version);
    }

    private void stop(NutsCommand args) {
        String name = null;
        NutsArgument a;
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else {
                name = args.requireNonOption().next().getString();
                RemoteTomcatConfigService c = loadTomcatConfig(name);
                c.shutdown();
            }
        }
    }

    public void restart(NutsCommand args, boolean shutdown) {
        String instance = null;
        boolean deleteLog = false;
        boolean install = false;
        List<String> apps = new ArrayList<>();
        NutsArgument a;
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else if ((a = args.nextBoolean("--deleteLog")) != null) {
                deleteLog = a.getValue().getBoolean();
            } else if ((a = args.nextBoolean("--install")) != null) {
                install = a.getValue().getBoolean();
            } else if ((a = args.nextString("--name")) != null) {
                instance = a.getValue().getString();
            } else if ((a = args.nextString("--deploy")) != null) {
                for (String s : a.getValue().getString().split(",")) {
                    s = s.trim();
                    if (!StringUtils.isBlank(s)) {
                        apps.add(s);
                    }
                }
//            } else if ((a = args.nextNonOption(DefaultNonOption.NAME)) != null) {
//                instance = a.getString();
            } else {
                instance = args.requireNonOption().next().getValue().getString();
            }
        }
        if (install) {
            for (String app : apps) {
                install(getContext().getWorkspace().parser().parseCommand(
                        new String[]{
                            "--name",
                            instance,
                            "--app",
                            app
                        }
                ));
            }
        }
        RemoteTomcatConfigService c = loadTomcatConfig(instance);
        if (shutdown) {
            c.restart(apps.toArray(new String[0]), deleteLog);
        } else {
            c.start(apps.toArray(new String[0]), deleteLog);
        }
    }

    public void reset(NutsCommand args) {
        NutsArgument a;
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else {
                args.setCommandName("tomcat --remote reset").unexpectedArgument();
            }
        }
        for (RemoteTomcatConfigService tomcatConfig : listConfig()) {
            tomcatConfig.remove();
        }
    }

    public RemoteTomcatConfigService[] listConfig() {
        List<RemoteTomcatConfigService> all = new ArrayList<>();
        try (DirectoryStream<Path> pp = Files.newDirectoryStream(getContext().getConfigFolder(),
                (Path entry) -> entry.getFileName().toString().endsWith(RemoteTomcatConfigService.REMOTE_CONFIG_EXT))) {
            for (Path entry : pp) {
                try {
                    RemoteTomcatConfigService c = loadTomcatConfig(entry);
                    all.add(c);
                } catch (Exception ex) {
                    //ignore
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return all.toArray(new RemoteTomcatConfigService[0]);
    }

    public void show(NutsCommand args) {
        NutsArgument a;
        RemoteTomcatServiceBase s;
        class Helper {

            boolean json = false;

            public void show(RemoteTomcatServiceBase aa) {
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
            if (context.configureFirst(args)) {
                //
            } else if ((a = args.nextBoolean("--json")) != null) {
                h.json = a.getValue().getBoolean();
            } else if ((s = readBaseServiceArg(args)) != null) {
                h.show(s);
            } else {
                args.setCommandName("tomcat --remote show").unexpectedArgument();
            }
        }
    }

    public void showProperty(NutsCommand args) {
        NutsArgument a;
        class Item {

            String name;

            public Item(String name) {
                this.name = name;
            }
        }
        class PropsHelper {

            RemoteTomcatServiceBase s = null;
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
            if (context.configureFirst(args)) {
                //
            } else if (x.s == null && (x.s = readBaseServiceArg(args)) != null) {
                //ok
            } else if ((a = args.nextString("--property")) != null) {
                x.addProp(a.getValue().getString());
            } else if (args.peek().isOption()) {
                args.setCommandName("tomcat --remote show-property").unexpectedArgument();
            } else {
                x.addProp(args.next().getString());
            }
        }
        x.build();
        if (x.m.isEmpty()) {
            throw new NutsExecutionException(context.getWorkspace(), "No properties to show", 2);
        }
        getContext().printOutObject(x.m);
    }

    public RemoteTomcatConfigService loadTomcatConfig(String name) {
        RemoteTomcatConfigService t = new RemoteTomcatConfigService(name, this);
        t.loadConfig();
        return t;
    }

    public RemoteTomcatConfigService loadTomcatConfig(Path name) {
        RemoteTomcatConfigService t = new RemoteTomcatConfigService(name, this);
        t.loadConfig();
        return t;
    }

    public RemoteTomcatConfigService createTomcatConfig(String name) {
        RemoteTomcatConfigService t = new RemoteTomcatConfigService(name, this);
        t.setConfig(new RemoteTomcatConfig());
        return t;
    }

    public RemoteTomcatConfigService loadOrCreateTomcatConfig(String name) {
        RemoteTomcatConfigService t = new RemoteTomcatConfigService(name, this);
        if (t.existsConfig()) {
            t.loadConfig();
        } else {
            t.setConfig(new RemoteTomcatConfig());
        }
        return t;
    }

    public RemoteTomcatServiceBase loadServiceBase(String name) {
        String[] strings = TomcatUtils.splitInstanceAppPreferInstance(name);
        if (strings[1].isEmpty()) {
            return loadOrCreateTomcatConfig(strings[0]);
        } else {
            RemoteTomcatConfigService u = loadOrCreateTomcatConfig(strings[0]);
            RemoteTomcatAppConfigService a = u.getAppOrNull(strings[1]);
            if (a == null) {
                throw new NutsExecutionException(context.getWorkspace(), "Unknown name " + name + ". it is no domain or app", 3);
            }
            return a;
        }
    }

    public RemoteTomcatServiceBase readBaseServiceArg(NutsCommand args) {
        NutsArgument a;
        if ((a = args.nextString("--name")) != null) {
            return (loadOrCreateTomcatConfig(a.getValue().getString()));
        } else if ((a = args.nextString("--app")) != null) {
            return (loadApp(a.getValue().getString()));
        } else if (args.hasNext() && args.peek().isOption()) {
            return null;
        } else {
            return (loadServiceBase(args.next().getString()));
        }
    }

    public RemoteTomcatAppConfigService loadApp(String name) {
        String[] strings = TomcatUtils.splitInstanceAppPreferApp(name);
        return loadOrCreateTomcatConfig(strings[0]).getApp(strings[1]);
    }

    public void setContext(NutsApplicationContext context) {
        this.context = context;
    }

    public NutsApplicationContext getContext() {
        return context;
    }

    public RemoteTomcatConfigService toRemoteTomcatConfigService(RemoteTomcatServiceBase s) {
        if (s instanceof RemoteTomcatAppConfigService) {
            s = ((RemoteTomcatAppConfigService) s).getTomcat();
        }
        return ((RemoteTomcatConfigService) s);
    }

}
