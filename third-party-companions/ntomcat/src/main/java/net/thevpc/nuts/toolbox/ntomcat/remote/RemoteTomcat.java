package net.thevpc.nuts.toolbox.ntomcat.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.ntomcat.NTomcatConfigVersions;
import net.thevpc.nuts.toolbox.ntomcat.remote.config.RemoteTomcatConfig;
import net.thevpc.nuts.toolbox.ntomcat.util.TomcatUtils;
import net.thevpc.nuts.util.*;

import java.util.ArrayList;
import java.util.List;

public class RemoteTomcat {

    public NSession session;
    public NCmdLine cmdLine;
    public NPath sharedConfigFolder;

    public RemoteTomcat(NSession session, NCmdLine cmdLine) {
        this.setSession(session);
        this.cmdLine = cmdLine;
        sharedConfigFolder = NApp.of().getVersionFolder(NStoreType.CONF, NTomcatConfigVersions.CURRENT);
    }

    public void runArgs() {
        NSession session = getSession();
        NArg a;
        cmdLine.setCommandName("tomcat --remote");
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                session.configureLast(cmdLine);
            } else {
                if ((a = cmdLine.next("list").orNull()) != null) {
                    list(cmdLine);
                    return;
                } else if ((a = cmdLine.next("show").orNull()) != null) {
                    show(cmdLine);
                    return;
                } else if ((a = cmdLine.next("add", "set").orNull()) != null) {
                    add(cmdLine);
                    return;
                } else if ((a = cmdLine.next("remove").orNull()) != null) {
                    remove(cmdLine);
                    return;
                } else if ((a = cmdLine.next("start").orNull()) != null) {
                    restart(cmdLine, false);
                    return;
                } else if ((a = cmdLine.next("restart").orNull()) != null) {
                    restart(cmdLine, true);
                    return;
                } else if ((a = cmdLine.next("stop").orNull()) != null) {
                    stop(cmdLine);
                    return;
                } else if ((a = cmdLine.next("install").orNull()) != null) {
                    install(cmdLine);
                    return;
                } else if ((a = cmdLine.next("deploy").orNull()) != null) {
                    deploy(cmdLine);
                    return;
                } else if ((a = cmdLine.next("reset").orNull()) != null) {
                    reset(cmdLine);
                    return;
                } else {
                    cmdLine.setCommandName("tomcat --remote").throwUnexpectedArgument();
                }
            }
        }
        throw new NExecutionException(NMsg.ofPlain("missing tomcat action. Type: nuts tomcat --help"), NExecutionException.ERROR_1);
    }

    public void list(NCmdLine args) {
        NSession session = getSession();
        NArg a;
        class Helper {

            boolean processed = false;

            void print(RemoteTomcatConfigService c) {
                processed = true;
                List<RemoteTomcatAppConfigService> apps = c.getApps();
                for (RemoteTomcatAppConfigService app : apps) {
                    session.out().println(NMsg.ofPlain(app.getName()));
                }
            }
        }
        Helper x = new Helper();
        while (args.hasNext()) {
            if ((a = args.nextEntry("--name").orNull()) != null) {
                x.print(loadOrCreateTomcatConfig(a.getStringValue().get()));
            } else if (args.peek().get().isNonOption()) {
                x.print(loadOrCreateTomcatConfig(args.nextNonOption().get().getStringValue().get()));
            } else {
                session.configureLast(args);
            }
        }
        if (!x.processed) {
            for (RemoteTomcatConfigService tomcatConfig : listConfig()) {
                getSession().out().println(tomcatConfig.getName());
            }
        }
    }

    private void add(NCmdLine args) {
        RemoteTomcatConfigService c = null;
        String appName = null;
        String instanceName = null;
        NArg a;
        args.setCommandName("tomcat --remote add");
        while (args.hasNext()) {
            if ((a = args.nextEntry("--name").orNull()) != null) {
                if (c == null) {
                    instanceName = a.getStringValue().get();
                    c = loadOrCreateTomcatConfig(instanceName);
                } else {
                    throw new NExecutionException(NMsg.ofPlain("instance already defined"), NExecutionException.ERROR_2);
                }
            } else if ((a = args.nextEntry("--server").orNull()) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setServer(a.getStringValue().get());
            } else if ((a = args.nextEntry("--remote-temp-path").orNull()) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRemoteTempPath(a.getStringValue().get());
            } else if ((a = args.nextEntry("--remote-instance").orNull()) != null) {
                String value = a.getStringValue().get();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRemoteName(value);
            } else if ((a = args.nextEntry("--app").orNull()) != null) {
                appName = a.getStringValue().get();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getAppOrCreate(appName);
            } else if ((a = args.nextEntry("--app.path").orNull()) != null) {
                String value = a.getStringValue().get();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                RemoteTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setPath(value);
            } else if ((a = args.nextEntry("--app.version").orNull()) != null) {
                String value = a.getStringValue().get();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                RemoteTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setVersionCommand(value);
            } else {
                session.configureLast(args);
            }
        }
        if (c == null) {
            c = loadOrCreateTomcatConfig(null);
        }
        boolean ok = false;
        NTexts text = NTexts.of();
        while (!ok) {
            try {
                ok = true;
                if (NBlankable.isBlank(c.getConfig().getServer())) {
                    ok = false;
                    c.getConfig().setServer(NAsk.of()
                            .forString(
                                    NMsg.ofC("[instance=%s] would you enter %s value ?"
                                            , text.ofStyled(c.getName(), NTextStyle.primary1())
                                            , text.ofStyled("--server", NTextStyle.option())
                                    )
                            )
                            .setDefaultValue("ssh://login@myserver/instanceName")
                            .getValue()
                    );
                }
                if (NBlankable.isBlank(c.getConfig().getRemoteTempPath())) {
                    ok = false;
                    c.getConfig()
                            .setRemoteTempPath(NAsk.of()
                                    .forString(NMsg.ofC("[instance=%s] would you enter %s value ?"
                                            , text.ofStyled(c.getName(), NTextStyle.primary1())
                                            , text.ofStyled("--remote-temp-path", NTextStyle.option())
                                    )).setDefaultValue("/tmp")
                                    .getValue()
                            );
                }
                for (RemoteTomcatAppConfigService aa : c.getApps()) {
                    if (NBlankable.isBlank(aa.getConfig().getPath())) {
                        ok = false;
                        aa.getConfig().setPath(NAsk.of()
                                .forString(NMsg.ofC("[instance=%s] [app=%s] would you enter %s value ?"
                                        , text.ofStyled(c.getName(), NTextStyle.primary1())
                                        , text.ofStyled(aa.getName(), NTextStyle.option())
                                        , text.ofStyled("--app.path", NTextStyle.option())
                                ))
                                .getValue());
                    }
                }
            } catch (NCancelException ex) {
                throw new NExecutionException(NMsg.ofPlain("cancelled"), NExecutionException.ERROR_1);
            }
        }
        c.save();
    }

    public void remove(NCmdLine args) {
        RemoteTomcatServiceBase s = null;
        NArg a;
        boolean processed = false;
        int lastExitCode = NExecutionException.SUCCESS;
        args.setCommandName("tomcat --remote remove");
        while (args.hasNext()) {
            if ((s = readBaseServiceArg(args)) != null) {
                s.remove();
                if (!(s instanceof RemoteTomcatConfigService)) {
                    toRemoteTomcatConfigService(s).save();
                }
                processed = true;
                lastExitCode = NExecutionException.SUCCESS;
            } else {
                session.configureLast(args);
            }
        }
        if (!processed) {
            throw new NExecutionException(NMsg.ofPlain("invalid parameters"), NExecutionException.ERROR_2);
        }
        if (lastExitCode != NExecutionException.SUCCESS) {
            throw new NExecutionException(NMsg.ofPlain("tomcat remove failed"), lastExitCode);
        }
    }

    private void install(NCmdLine args) {
        NSession session = getSession();
        String conf = null;
        String app = null;
        NArg a;
        args.setCommandName("tomcat --remote install");
        while (args.hasNext()) {
            if ((a = args.nextEntry("--app").orNull()) != null) {
                loadApp(a.getStringValue().get()).install();
            } else {
                session.configureLast(args);
            }
        }
    }

    private void deploy(NCmdLine args) {
        NSession session = getSession();
        String app = null;
        String version = null;
        NArg a;
        args.setCommandName("tomcat --remote deploy");
        while (args.hasNext()) {
            if ((a = args.nextEntry("--app").orNull()) != null) {
                app = a.getStringValue().get();
            } else if ((a = args.nextEntry("--version").orNull()) != null) {
                version = a.getStringValue().get();
            } else {
                session.configureLast(args);
            }
        }
        loadApp(app).deploy(version);
    }

    private void stop(NCmdLine args) {
        NSession session = getSession();
        String name = null;
        NArg a;
        while (args.hasNext()) {
            if (args.peek().get().isNonOption()) {
                name = args.nextNonOption().flatMap(NLiteral::asString).get();
                RemoteTomcatConfigService c = loadTomcatConfig(name);
                c.shutdown();
            } else {
                session.configureLast(args);
            }
        }
    }

    public void restart(NCmdLine args, boolean shutdown) {
        String instance = null;
        boolean deleteLog = false;
        boolean install = false;
        List<String> apps = new ArrayList<>();
        NArg a;
        while (args.hasNext()) {
            if ((a = args.nextFlag("--deleteLog").orNull()) != null) {
                deleteLog = a.getBooleanValue().get();
            } else if ((a = args.nextFlag("--install").orNull()) != null) {
                install = a.getBooleanValue().get();
            } else if ((a = args.nextEntry("--name").orNull()) != null) {
                instance = a.getStringValue().get();
            } else if ((a = args.nextEntry("--deploy").orNull()) != null) {
                for (String s : a.getStringValue().get().split(",")) {
                    s = s.trim();
                    if (!NBlankable.isBlank(s)) {
                        apps.add(s);
                    }
                }
//            } else if ((a = args.nextNonOption(DefaultNonOption.NAME)) != null) {
//                instance = a.getString();
            } else if (args.peek().get().isNonOption()) {
                instance = args.nextNonOption().get().getStringValue().get();
            } else {
                session.configureLast(args);
            }
        }
        if (install) {
            for (String app : apps) {
                install(NCmdLine.of(
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

    public void reset(NCmdLine args) {
        NArg a;
        args.setCommandName("tomcat --remote reset");
        while (args.hasNext()) {
            session.configureLast(args);
        }
        for (RemoteTomcatConfigService tomcatConfig : listConfig()) {
            tomcatConfig.remove();
        }
    }

    public RemoteTomcatConfigService[] listConfig() {
        return
                sharedConfigFolder.stream().filter(
                                NPredicate.of((NPath pathname) -> pathname.isRegularFile() && pathname.getName().endsWith(RemoteTomcatConfigService.REMOTE_CONFIG_EXT))
                                        .withDesc(
                                                NEDesc.of("isRegularFile() && matches(*" + RemoteTomcatConfigService.REMOTE_CONFIG_EXT + ")")
                                        )

                        )
                        .mapUnsafe(
                                NUnsafeFunction.of((NPath x) -> loadTomcatConfig(x)).withDesc(NEDesc.of("loadTomcatConfig"))
                        )
                        .filterNonNull()
                        .toArray(RemoteTomcatConfigService[]::new);
    }

    public void show(NCmdLine args) {
        NArg a;
        RemoteTomcatServiceBase s;
        class Helper {

            boolean json = false;
            boolean yaml = false;

            public void show(RemoteTomcatServiceBase aa) {
                NSession session = getSession();
                if (json) {
                    session.out().println(NMsg.ofC("%s :", NText.ofStyledPrimary4(aa.getName())));
                    aa.println(session.out());
                } else if (yaml) {
                    //TODO FIX ME, what to do in Yaml?
                    session.out().println(NMsg.ofC("%s :", NText.ofStyledPrimary4(aa.getName())));
                    aa.println(session.out());
                } else {
                    session.out().println(NMsg.ofC("%s :", NText.ofStyledPrimary4(aa.getName())));
                    aa.println(session.out());
                }
            }
        }
        Helper h = new Helper();
        args.setCommandName("tomcat --remote show");
        while (args.hasNext()) {
            if ((a = args.nextFlag("--json").orNull()) != null) {
                h.json = a.getBooleanValue().get();
            } else if ((a = args.nextFlag("--yaml").orNull()) != null) {
                h.yaml = a.getBooleanValue().get();
            } else if ((s = readBaseServiceArg(args)) != null) {
                h.show(s);
            } else {
                session.configureLast(args);
            }
        }
    }

    public RemoteTomcatConfigService loadTomcatConfig(String name) {
        RemoteTomcatConfigService t = new RemoteTomcatConfigService(name, this);
        t.loadConfig();
        return t;
    }

    public RemoteTomcatConfigService loadTomcatConfig(NPath name) {
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
                throw new NExecutionException(NMsg.ofC("unknown name %s. it is no domain or app", name), NExecutionException.ERROR_3);
            }
            return a;
        }
    }

    public RemoteTomcatServiceBase readBaseServiceArg(NCmdLine args) {
        NArg a;
        if ((a = args.nextEntry("--name").orNull()) != null) {
            return (loadOrCreateTomcatConfig(a.getStringValue().get()));
        } else if ((a = args.nextEntry("--app").orNull()) != null) {
            return (loadApp(a.getStringValue().get()));
        } else if (args.hasNext() && args.isNextOption()) {
            return null;
        } else {
            return (loadServiceBase(args.next().flatMap(NLiteral::asString).get()));
        }
    }

    public RemoteTomcatAppConfigService loadApp(String name) {
        String[] strings = TomcatUtils.splitInstanceAppPreferApp(name);
        return loadOrCreateTomcatConfig(strings[0]).getApp(strings[1]);
    }

    public NSession getSession() {
        return session;
    }

    public void setSession(NSession session) {
        this.session = session;
    }

    public RemoteTomcatConfigService toRemoteTomcatConfigService(RemoteTomcatServiceBase s) {
        if (s instanceof RemoteTomcatAppConfigService) {
            s = ((RemoteTomcatAppConfigService) s).getTomcat();
        }
        return ((RemoteTomcatConfigService) s);
    }

}
