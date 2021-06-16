package net.thevpc.nuts.toolbox.tomcat.remote;

import net.thevpc.common.strings.StringUtils;
import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.tomcat.NTomcatConfigVersions;
import net.thevpc.nuts.toolbox.tomcat.remote.config.RemoteTomcatConfig;
import net.thevpc.nuts.toolbox.tomcat.util.TomcatUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RemoteTomcat {

    public NutsApplicationContext context;
    public NutsCommandLine cmdLine;
    public Path sharedConfigFolder;

    public RemoteTomcat(NutsApplicationContext applicationContext, NutsCommandLine cmdLine) {
        this.setContext(applicationContext);
        this.cmdLine = cmdLine;
        sharedConfigFolder = Paths.get(applicationContext.getVersionFolderFolder(NutsStoreLocation.CONFIG, NTomcatConfigVersions.CURRENT));
    }

    public void runArgs() {
        NutsArgument a;
        cmdLine.setCommandName("tomcat --remote");
        while (cmdLine.hasNext()) {
            if (cmdLine.peek().isOption()) {
                context.configureLast(cmdLine);
            } else {
                if ((a = cmdLine.next("list")) != null) {
                    list(cmdLine);
                    return;
                } else if ((a = cmdLine.next("show")) != null) {
                    show(cmdLine);
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
        throw new NutsExecutionException(context.getSession(), "missing tomcat action. Type: nuts tomcat --help", 1);
    }

    public void list(NutsCommandLine args) {
        NutsArgument a;
        class Helper {

            boolean processed = false;

            void print(RemoteTomcatConfigService c) {
                processed = true;
                List<RemoteTomcatAppConfigService> apps = c.getApps();
                for (RemoteTomcatAppConfigService app : apps) {
                    context.getSession().out().printf("%s\n", app.getName());
                }
            }
        }
        Helper x = new Helper();
        while (args.hasNext()) {
            if ((a = args.nextString("--name")) != null) {
                x.print(loadOrCreateTomcatConfig(a.getStringValue()));
            } else if (args.peek().isNonOption()) {
                x.print(loadOrCreateTomcatConfig(args.requireNonOption().next().getStringValue()));
            } else {
                context.configureLast(args);
            }
        }
        if (!x.processed) {
            for (RemoteTomcatConfigService tomcatConfig : listConfig()) {
                getContext().getSession().out().println(tomcatConfig.getName());
            }
        }
    }

    private void add(NutsCommandLine args) {
        RemoteTomcatConfigService c = null;
        String appName = null;
        String instanceName = null;
        NutsArgument a;
        args.setCommandName("tomcat --remote add");
        NutsSession session = context.getSession();
        while (args.hasNext()) {
            if ((a = args.nextString("--name")) != null) {
                if (c == null) {
                    instanceName = a.getStringValue();
                    c = loadOrCreateTomcatConfig(instanceName);
                } else {
                    throw new NutsExecutionException(session, "instance already defined", 2);
                }
            } else if ((a = args.nextString("--server")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setServer(a.getStringValue());
            } else if ((a = args.nextString("--remote-temp-path")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRemoteTempPath(a.getStringValue());
            } else if ((a = args.nextString("--remote-instance")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRemoteName(value);
            } else if ((a = args.nextString("--app")) != null) {
                appName = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getAppOrCreate(appName);
            } else if ((a = args.nextString("--app.path")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                RemoteTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setPath(value);
            } else if ((a = args.nextString("--app.version")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                RemoteTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setVersionCommand(value);
            } else {
                context.configureLast(args);
            }
        }
        if (c == null) {
            c = loadOrCreateTomcatConfig(null);
        }
        boolean ok = false;
        NutsTextManager text = getContext().getWorkspace().text();
        while (!ok) {
            try {
                ok = true;
                if (TomcatUtils.isBlank(c.getConfig().getServer())) {
                    ok = false;
                    c.getConfig().setServer(session.getTerminal()
                                    .ask()
                                    .setSession(session)
                                    .forString("[instance=%s] would you enter %s value ?"
                                    , text.forStyled(c.getName(), NutsTextStyle.primary(1))
                                    , text.forStyled("--server", NutsTextStyle.option())
                            )
                                    .setDefaultValue("ssh://login@myserver/instanceName").setSession(session)
                                    .getValue()
                    );
                }
                if (TomcatUtils.isBlank(c.getConfig().getRemoteTempPath())) {
                    ok = false;
                    c.getConfig()
                            .setRemoteTempPath(session.getTerminal().ask()
                                    .resetLine()
                                    .setSession(session)
                                    .forString("[instance=%s] would you enter %s value ?"
                                            , text.forStyled(c.getName(), NutsTextStyle.primary(1))
                                            , text.forStyled("--remote-temp-path", NutsTextStyle.option())
                                    ).setDefaultValue("/tmp")
                                    .setSession(session)
                                    .getValue()
                            );
                }
                for (RemoteTomcatAppConfigService aa : c.getApps()) {
                    if (TomcatUtils.isBlank(aa.getConfig().getPath())) {
                        ok = false;
                        aa.getConfig().setPath(session.getTerminal().ask()
                                .resetLine()
                                    .setSession(session)
                                .forString("[instance=%s] [app=%s] would you enter %s value ?"
                                        , text.forStyled(c.getName(), NutsTextStyle.primary(1))
                                        , text.forStyled(aa.getName(), NutsTextStyle.option())
                                        , text.forStyled("--app.path", NutsTextStyle.option())
                                )
                                .setSession(session)
                                .getValue());
                    }
                }
            } catch (NutsUserCancelException ex) {
                throw new NutsExecutionException(session, "Cancelled", 1);
            }
        }
        c.save();
    }

    public void remove(NutsCommandLine args) {
        RemoteTomcatServiceBase s = null;
        NutsArgument a;
        boolean processed = false;
        int lastExitCode = 0;
        args.setCommandName("tomcat --remote remove");
        while (args.hasNext()) {
            if ((s = readBaseServiceArg(args)) != null) {
                s.remove();
                if (!(s instanceof RemoteTomcatConfigService)) {
                    toRemoteTomcatConfigService(s).save();
                }
                processed = true;
                lastExitCode = 0;
            } else {
                context.configureLast(args);
            }
        }
        if (!processed) {
            throw new NutsExecutionException(context.getSession(), "Invalid parameters", 2);
        }
        if (lastExitCode != 0) {
            throw new NutsExecutionException(context.getSession(), lastExitCode);
        }
    }

    private void install(NutsCommandLine args) {
        String conf = null;
        String app = null;
        NutsArgument a;
        args.setCommandName("tomcat --remote install");
        while (args.hasNext()) {
            if ((a = args.nextString("--app")) != null) {
                loadApp(a.getStringValue()).install();
            } else {
                context.configureLast(args);
            }
        }
    }

    private void deploy(NutsCommandLine args) {
        String app = null;
        String version = null;
        NutsArgument a;
        args.setCommandName("tomcat --remote deploy");
        while (args.hasNext()) {
            if ((a = args.nextString("--app")) != null) {
                app = a.getStringValue();
            } else if ((a = args.nextString("--version")) != null) {
                version = a.getStringValue();
            } else {
                context.configureLast(args);
            }
        }
        loadApp(app).deploy(version);
    }

    private void stop(NutsCommandLine args) {
        String name = null;
        NutsArgument a;
        while (args.hasNext()) {
            if (args.peek().isNonOption()) {
                name = args.requireNonOption().next().getString();
                RemoteTomcatConfigService c = loadTomcatConfig(name);
                c.shutdown();
            } else {
                context.configureLast(args);
            }
        }
    }

    public void restart(NutsCommandLine args, boolean shutdown) {
        String instance = null;
        boolean deleteLog = false;
        boolean install = false;
        List<String> apps = new ArrayList<>();
        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.nextBoolean("--deleteLog")) != null) {
                deleteLog = a.getBooleanValue();
            } else if ((a = args.nextBoolean("--install")) != null) {
                install = a.getBooleanValue();
            } else if ((a = args.nextString("--name")) != null) {
                instance = a.getStringValue();
            } else if ((a = args.nextString("--deploy")) != null) {
                for (String s : a.getStringValue().split(",")) {
                    s = s.trim();
                    if (!StringUtils.isBlank(s)) {
                        apps.add(s);
                    }
                }
//            } else if ((a = args.nextNonOption(DefaultNonOption.NAME)) != null) {
//                instance = a.getString();
            } else if (args.peek().isNonOption()) {
                instance = args.requireNonOption().next().getStringValue();
            } else {
                context.configureLast(args);
            }
        }
        if (install) {
            for (String app : apps) {
                install(getContext().getWorkspace().commandLine().create(
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

    public void reset(NutsCommandLine args) {
        NutsArgument a;
        args.setCommandName("tomcat --remote reset");
        while (args.hasNext()) {
            context.configureLast(args);
        }
        for (RemoteTomcatConfigService tomcatConfig : listConfig()) {
            tomcatConfig.remove();
        }
    }

    public RemoteTomcatConfigService[] listConfig() {
        List<RemoteTomcatConfigService> all = new ArrayList<>();
        if (Files.isDirectory(sharedConfigFolder)) {
            try (DirectoryStream<Path> pp = Files.newDirectoryStream(sharedConfigFolder,
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
        }
        return all.toArray(new RemoteTomcatConfigService[0]);
    }

    public void show(NutsCommandLine args) {
        NutsArgument a;
        RemoteTomcatServiceBase s;
        class Helper {

            boolean json = false;

            public void show(RemoteTomcatServiceBase aa) {
                if (json) {
                    getContext().getSession().out().printf("%s :\n", getContext().getWorkspace().text().forStyled(aa.getName(), NutsTextStyle.primary(4)));
                    aa.println(getContext().getSession().out());
                } else {
                    getContext().getSession().out().printf("%s :\n", getContext().getWorkspace().text().forStyled(aa.getName(), NutsTextStyle.primary(4)));
                    aa.println(getContext().getSession().out());
                }
            }
        }
        Helper h = new Helper();
        args.setCommandName("tomcat --remote show");
        while (args.hasNext()) {
            if ((a = args.nextBoolean("--json")) != null) {
                h.json = a.getBooleanValue();
            } else if ((s = readBaseServiceArg(args)) != null) {
                h.show(s);
            } else {
                context.configureLast(args);
            }
        }
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
                throw new NutsExecutionException(context.getSession(), "Unknown name " + name + ". it is no domain or app", 3);
            }
            return a;
        }
    }

    public RemoteTomcatServiceBase readBaseServiceArg(NutsCommandLine args) {
        NutsArgument a;
        if ((a = args.nextString("--name")) != null) {
            return (loadOrCreateTomcatConfig(a.getStringValue()));
        } else if ((a = args.nextString("--app")) != null) {
            return (loadApp(a.getStringValue()));
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

    public NutsApplicationContext getContext() {
        return context;
    }

    public void setContext(NutsApplicationContext context) {
        this.context = context;
    }

    public RemoteTomcatConfigService toRemoteTomcatConfigService(RemoteTomcatServiceBase s) {
        if (s instanceof RemoteTomcatAppConfigService) {
            s = ((RemoteTomcatAppConfigService) s).getTomcat();
        }
        return ((RemoteTomcatConfigService) s);
    }

}
