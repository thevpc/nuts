package net.vpc.toolbox.tomcat.client;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.DefaultNonOption;
import net.vpc.toolbox.tomcat.client.config.TomcatClientConfig;
import net.vpc.toolbox.tomcat.util.NutsContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;
import net.vpc.toolbox.tomcat.util.UserCancelException;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TomcatClient {
    public NutsContext context;

    public TomcatClient(NutsWorkspace ws) {
        this(new NutsContext(ws));
    }

    public TomcatClient(NutsContext context) {
        this.context = context;
    }

    public int runArgs(String[] args) {
        CommandLine cmd = new CommandLine(args);
        Argument a;
        while (cmd.hasNext()) {
            if ((a = cmd.readNonOption("list")) != null) {
                return list(cmd);
            } else if ((a = cmd.readNonOption("add", "set")) != null) {
                return list(cmd);
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
            } else if ((a = cmd.readNonOption("reset")) != null) {
                return reset(cmd);
            } else {
                cmd.unexpectedArgument();
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
            } else if ((a = args.readBooleanOption("--instance")) != null) {
                instance = a.getStringValue();
            } else if ((a = args.readBooleanOption("--app")) != null) {
                app = a.getStringValue();
            } else if ((a = args.readBooleanOption("--property")) != null) {
                property = a.getStringValue();
            } else {
                args.unexpectedArgument();
            }
        }
        if (property == null) {
            if (app != null) {
                TomcatClientConfigService c = loadOrCreateTomcatConfig(instance);
                TomcatClientAppConfigService aa = c.getApp(app);
                if (json) {
                    context.out.printf("[[%s]] :\n", aa.getName());
                    aa.write(context.out);
                    context.out.println();
                } else {
                    context.out.println(aa.getName());
                }
            } else {
                for (TomcatClientConfigService tomcatConfig : listConfig()) {
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
            TomcatClientConfigService c = loadOrCreateTomcatConfig(instance);
            if (app != null) {
                context.out.printf("%s\n", TomcatUtils.getPropertyValue(c.getApp(app).getConfig(), property));
            } else {
                for (TomcatClientAppConfigService aa : c.getApps()) {
                    context.out.printf("[%s] %s\n", TomcatUtils.getPropertyValue(aa.getConfig(), property));
                }
            }
        }
        return 0;
    }

    private int add(CommandLine args) {
        TomcatClientConfigService c = null;
        String appName = null;
        String instanceName = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--instance")) != null) {
                if (c == null) {
                    instanceName = a.getStringValue();
                    c = loadOrCreateTomcatConfig(instanceName);
                } else {
                    throw new IllegalArgumentException("instance already defined");
                }
            } else if ((a = args.readStringOption("--server")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setServer(a.getStringValue());
            } else if ((a = args.readStringOption("--remote-instance")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRemoteInstance(a.getStringValue());
            } else if ((a = args.readStringOption("--remote-temp-path")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setRemoteTempPath(a.getStringValue());
            } else if ((a = args.readStringOption("--key-file")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setServerCertificateFile(a.getStringValue());
            } else if ((a = args.readStringOption("--password")) != null) {
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                c.getConfig().setServerPassword(a.getStringValue());
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
                TomcatClientAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setPath(value);
            } else if ((a = args.readStringOption("--app.version")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateTomcatConfig(null);
                }
                TomcatClientAppConfigService tomcatAppConfig = c.getAppOrError(appName);
                tomcatAppConfig.getConfig().setVersionCommand(value);
            } else {
                args.unexpectedArgument();
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
                    c.getConfig().setServer(context.readOrCancel("[instance=[[%s]]] Would you enter ==%s== value?", "ssh://login@myserver", c.getName(), "--server"));
                }
                if (TomcatUtils.isEmpty(c.getConfig().getRemoteInstance())) {
                    ok = false;
                    c.getConfig().setRemoteInstance(context.readOrCancel("[instance=[[%s]]] Would you enter ==%s== value?", "default", c.getName(), "--remote-instance"));
                }
                if (TomcatUtils.isEmpty(c.getConfig().getRemoteTempPath())) {
                    ok = false;
                    c.getConfig().setRemoteTempPath(context.readOrCancel("[instance=[[%s]]] Would you enter ==%s== value?", "/tmp", c.getName(), "--remote-temp-path"));
                }
                for (TomcatClientAppConfigService aa : c.getApps()) {
                    if (TomcatUtils.isEmpty(aa.getConfig().getPath())) {
                        ok = false;
                        aa.getConfig().setPath(context.readOrCancel("[instance=[[%s]]] [app=[[%s]]] Would you enter ==%s== value?", null, c.getName(), aa.getName(), "-app.path"));
                    }
                    if (TomcatUtils.isEmpty(aa.getConfig().getVersionCommand())) {
                        ok = false;
                        aa.getConfig().setVersionCommand(context.readOrCancel("[instance=[[%s]]] [app=[[%s]]] Would you enter ==%s== value?", "nsh file-version %file", c.getName(), aa.getName(), "-app.version"));
                    }

                }
            } catch (UserCancelException ex) {
                return 1;
            }
        }
        c.saveConfig();
        return 0;
    }


    private int remove(CommandLine args) {
        String instance = null;
        String appName = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--app")) != null) {
                appName = a.getStringValue();
            } else if ((a = args.readStringOption("--instance")) != null) {
                instance = a.getStringValue();
            } else {
                args.unexpectedArgument();
            }
        }
        if (appName == null) {
            loadTomcatConfig(instance).removeConfig();
        } else {
            TomcatClientConfigService c = loadTomcatConfig(instance);
            try {
                c.getAppOrError(appName).remove();
                c.saveConfig();
            } catch (Exception ex) {
                //
            }
        }
        return 0;
    }

    private int install(CommandLine args) {
        String conf = null;
        String app = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--instance")) != null) {
                conf = a.getStringValue();
            } else if ((a = args.readStringOption("--app")) != null) {
                app = a.getStringValue();
            } else {
                args.unexpectedArgument();
            }
        }
        TomcatClientConfigService c = loadTomcatConfig(conf);
        c.getApp(app).install();
        return 0;
    }

    private int stop(CommandLine args) {
        String name = null;
        Argument a;
        while (args.hasNext()) {
            name = args.readRequiredNonOption().getExpression();
        }
        TomcatClientConfigService c = loadTomcatConfig(name);
        c.shutdown();
        return 0;
    }

    public int restart(CommandLine args, boolean shutdown) {
        String name = null;
        boolean deleteLog = false;
        List<String> apps = new ArrayList<>();
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readBooleanOption("--deleteLog")) != null) {
                deleteLog = a.getBooleanValue();
            } else if ((a = args.readStringOption("--deploy")) != null) {
                apps.add(a.getStringValue());
            } else if ((a = args.readNonOption(DefaultNonOption.NAME)) != null) {
                name = a.getString();
            } else {
                args.unexpectedArgument();
            }
        }
        TomcatClientConfigService c = loadTomcatConfig(name);
        if (shutdown) {
            return c.restart(apps.toArray(new String[0]), deleteLog);
        } else {
            return c.start(apps.toArray(new String[0]), deleteLog);
        }
    }

    public int reset(CommandLine cmd) {
        for (TomcatClientConfigService tomcatConfig : listConfig()) {
            tomcatConfig.removeConfig();
        }
        return 0;
    }


    public TomcatClientConfigService[] listConfig() {
        List<TomcatClientConfigService> all = new ArrayList<>();
        File[] configFiles = new File(context.configFolder).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".config");
            }
        });
        if (configFiles != null) {
            for (File file1 : configFiles) {
                try {
                    TomcatClientConfigService c = loadTomcatConfig(file1.getName().substring(0, file1.getName().length() - ".config".length()));
                    all.add(c);
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        return all.toArray(new TomcatClientConfigService[0]);
    }


    public TomcatClientConfigService loadTomcatConfig(String name) {
        TomcatClientConfigService t = new TomcatClientConfigService(name, this);
        t.loadConfig();
        return t;
    }

    public TomcatClientConfigService createTomcatConfig(String name) {
        TomcatClientConfigService t = new TomcatClientConfigService(name, this);
        t.setConfig(new TomcatClientConfig());
        return t;
    }

    public TomcatClientConfigService loadOrCreateTomcatConfig(String name) {
        TomcatClientConfigService t = new TomcatClientConfigService(name, this);
        if (t.existsConfig()) {
            t.loadConfig();
        } else {
            t.setConfig(new TomcatClientConfig());
        }
        return t;
    }
}
