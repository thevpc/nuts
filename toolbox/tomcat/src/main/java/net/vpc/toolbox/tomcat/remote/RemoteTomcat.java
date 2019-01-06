package net.vpc.toolbox.tomcat.remote;

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsQuestion;
import net.vpc.app.nuts.app.NutsAppUtils;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.format.PropertiesFormatter;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.tomcat.remote.config.RemoteTomcatConfig;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;
import net.vpc.app.nuts.NutsUserCancelException;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class RemoteTomcat {
    public NutsApplicationContext context;

    public RemoteTomcat(NutsApplicationContext ws) {
        this.setContext(ws);
    }

    public int runArgs() {
        CommandLine cmd = new CommandLine(context.getArgs());
        Argument a;
        while (cmd.hasNext()) {
            if (cmd.isOption()) {
                if (context.configure(cmd)) {
                    //
                } else {
                    cmd.unexpectedArgument("tomcat --remote");
                }
            } else {
                if ((a = cmd.readNonOption("list")) != null) {
                    return list(cmd);
                } else if ((a = cmd.readNonOption("show")) != null) {
                    return show(cmd);
                } else if ((a = cmd.readNonOption("show-property")) != null) {
                    return showProperty(cmd);
                } else if ((a = cmd.readNonOption("add", "set")) != null) {
                    return add(cmd);
                } else if ((a = cmd.readNonOption("remove")) != null) {
                    return remove(cmd);
                } else if ((a = cmd.readNonOption("start")) != null) {
                    return restart(cmd, false);
                } else if ((a = cmd.readNonOption("restart")) != null) {
                    return restart(cmd, true);
                } else if ((a = cmd.readNonOption("stop")) != null) {
                    return stop(cmd);
                } else if ((a = cmd.readNonOption("install")) != null) {
                    return install(cmd);
                } else if ((a = cmd.readNonOption("deploy")) != null) {
                    return deploy(cmd);
                } else if ((a = cmd.readNonOption("reset")) != null) {
                    return reset(cmd);
                } else {
                    cmd.unexpectedArgument("tomcat --remote");
                }
            }
        }
        return 0;
    }

    public int list(CommandLine args) {
        Argument a;
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
            if (context.configure(args)) {
                //
            } else if ((a = args.readStringOption("--instance")) != null) {
                x.print(loadOrCreateTomcatConfig(a.getStringValue()));
            } else {
                x.print(loadOrCreateTomcatConfig(args.readRequiredNonOption().getStringValue()));
            }
        }
        if (!x.processed) {
            for (RemoteTomcatConfigService tomcatConfig : listConfig()) {
                getContext().out().println(tomcatConfig.getName());
            }
        }
        return 0;
    }

    private int add(CommandLine args) {
        RemoteTomcatConfigService c = null;
        String appName = null;
        String instanceName = null;
        Argument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else if ((a = args.readStringOption("--instance")) != null) {
                if (c == null) {
                    instanceName = a.getStringValue();
                    c = loadOrCreateTomcatConfig(instanceName);
                } else {
                    throw new NutsExecutionException("instance already defined",2);
                }
            } else if ((a = args.readStringOption("--server")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setServer(a.getStringValue());
            } else if ((a = args.readStringOption("--remote-temp-path")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRemoteTempPath(a.getStringValue());
            } else if ((a = args.readStringOption("--remote-instance")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRemoteName(value);
            } else if ((a = args.readStringOption("--app")) != null) {
                appName = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getAppOrCreate(appName);
            } else if ((a = args.readStringOption("--app.path")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                RemoteTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setPath(value);
            } else if ((a = args.readStringOption("--app.version")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                RemoteTomcatAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setVersionCommand(value);
            } else {
                args.unexpectedArgument("tomcat --remote add");
            }
        }
        if (c == null) {
            c = loadOrCreateTomcatConfig(null);
        }
        boolean ok = false;
        while (!ok) {
            try {
                ok = true;
                if (TomcatUtils.isEmpty(c.getConfig().getServer())) {
                    ok = false;
                    c.getConfig().setServer(context.terminal().ask(NutsQuestion.forString("[instance=[[%s]]] Would you enter ==%s== value?", c.getName(), "--server").setDefautValue("ssh://login@myserver/instanceName")));
                }
                if (TomcatUtils.isEmpty(c.getConfig().getRemoteTempPath())) {
                    ok = false;
                    c.getConfig().setRemoteTempPath(context.terminal().ask(NutsQuestion.forString("[instance=[[%s]]] Would you enter ==%s== value?",  c.getName(), "--remote-temp-path").setDefautValue("/tmp")));
                }
                for (RemoteTomcatAppConfigService aa : c.getApps()) {
                    if (TomcatUtils.isEmpty(aa.getConfig().getPath())) {
                        ok = false;
                        aa.getConfig().setPath(context.terminal().ask(NutsQuestion.forString("[instance=[[%s]]] [app=[[%s]]] Would you enter ==%s== value?", c.getName(), aa.getName(), "-app.path")));
                    }
                }
            } catch (NutsUserCancelException ex) {
                return 1;
            }
        }
        c.save();
        return 0;
    }

    public int remove(CommandLine args) {
        RemoteTomcatServiceBase s = null;
        Argument a;
        boolean processed = false;
        int lastExitCode = 0;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else if ((s = readBaseServiceArg(args)) != null) {
                s.remove();
                if (!(s instanceof RemoteTomcatConfigService)) {
                    toRemoteTomcatConfigService(s).save();
                }
                processed = true;
                lastExitCode = 0;
            } else {
                args.unexpectedArgument("tomcat --remote remove");
            }
        }
        if (!processed) {
            throw new NutsExecutionException("Invalid parameters",2);
        }
        return lastExitCode;
    }

    private int install(CommandLine args) {
        String conf = null;
        String app = null;
        Argument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else if ((a = args.readStringOption("--app")) != null) {
                loadApp(a.getStringValue()).install();
            } else {
                args.unexpectedArgument("tomcat --remote install");
            }
        }
        return 0;
    }

    private int deploy(CommandLine args) {
        String app = null;
        String version = null;
        Argument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else if ((a = args.readStringOption("--app")) != null) {
                app = a.getStringValue();
            } else if ((a = args.readStringOption("--version")) != null) {
                version = a.getStringValue();
            } else {
                args.unexpectedArgument("tomcat --remote deploy");
            }
        }
        loadApp(app).deploy(version);
        return 0;
    }

    private int stop(CommandLine args) {
        String name = null;
        Argument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else {
                name = args.readRequiredNonOption().getExpression();
                RemoteTomcatConfigService c = loadTomcatConfig(name);
                c.shutdown();
            }
        }
        return 0;
    }

    public int restart(CommandLine args, boolean shutdown) {
        String instance = null;
        boolean deleteLog = false;
        boolean install = false;
        List<String> apps = new ArrayList<>();
        Argument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else if ((a = args.readBooleanOption("--deleteLog")) != null) {
                deleteLog = a.getBooleanValue();
            } else if ((a = args.readBooleanOption("--install")) != null) {
                install = a.getBooleanValue();
            } else if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getStringValue();
            } else if ((a = args.readStringOption("--deploy")) != null) {
                for (String s : a.getStringValue().split(",")) {
                    s = s.trim();
                    if (!StringUtils.isEmpty(s)) {
                        apps.add(s);
                    }
                }
//            } else if ((a = args.readNonOption(DefaultNonOption.NAME)) != null) {
//                instance = a.getString();
            } else {
                instance = args.readRequiredNonOption().getStringValue();
            }
        }
        if (install) {
            for (String app : apps) {
                install(new CommandLine(
                        new String[]{
                                "--instance",
                                instance,
                                "--app",
                                app
                        }
                ));
            }
        }
        RemoteTomcatConfigService c = loadTomcatConfig(instance);
        if (shutdown) {
            return c.restart(apps.toArray(new String[0]), deleteLog);
        } else {
            return c.start(apps.toArray(new String[0]), deleteLog);
        }
    }

    public int reset(CommandLine args) {
        Argument a;
        while (args.hasNext()) {
            if (context.configure(args)) {
                //
            } else {
                args.unexpectedArgument("tomcat --remote reset");
            }
        }
        for (RemoteTomcatConfigService tomcatConfig : listConfig()) {
            tomcatConfig.remove();
        }
        return 0;
    }


    public RemoteTomcatConfigService[] listConfig() {
        List<RemoteTomcatConfigService> all = new ArrayList<>();
        File[] configFiles = new File(context.getConfigFolder()).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(RemoteTomcatConfigService.REMOTE_CONFIG_EXT);
            }
        });
        if (configFiles != null) {
            for (File file1 : configFiles) {
                try {
                    RemoteTomcatConfigService c = loadTomcatConfig(file1.getName().substring(0, file1.getName().length() - RemoteTomcatConfigService.REMOTE_CONFIG_EXT.length()));
                    all.add(c);
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        return all.toArray(new RemoteTomcatConfigService[0]);
    }

    public int show(CommandLine args) {
        Argument a;
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
            if (context.configure(args)) {
                //
            } else if ((a = args.readBooleanOption("--json")) != null) {
                h.json = a.getBooleanValue();
            } else if ((s = readBaseServiceArg(args)) != null) {
                h.show(s);
            } else {
                args.unexpectedArgument("tomcat --remote show");
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
                //
            } else if (x.s == null && (x.s = readBaseServiceArg(args)) != null) {
                //ok
            } else if ((a = args.readStringOption("--property")) != null) {
                x.addProp(a.getStringValue());
            } else if (args.isOption()) {
                args.unexpectedArgument("tomcat --remote show-property");
            } else {
                x.addProp(args.read().getExpression());
            }
        }
        x.build();
        if (x.m.isEmpty()) {
            throw new NutsExecutionException("No properties to show", 2);
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


    public RemoteTomcatConfigService loadTomcatConfig(String name) {
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
                throw new NutsExecutionException("Unknown name " + name + ". it is no domain or app", 3);
            }
            return a;
        }
    }


    public RemoteTomcatServiceBase readBaseServiceArg(CommandLine args) {
        Argument a;
        if ((a = args.readStringOption("--instance")) != null) {
            return (loadOrCreateTomcatConfig(a.getStringValue()));
        } else if ((a = args.readStringOption("--app")) != null) {
            return (loadApp(a.getStringValue()));
        } else if (args.isOption()) {
            return null;
        } else {
            return (loadServiceBase(args.read().getExpression()));
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
