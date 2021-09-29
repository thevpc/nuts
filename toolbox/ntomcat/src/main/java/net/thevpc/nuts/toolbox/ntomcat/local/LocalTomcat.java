package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.ntomcat.NTomcatConfigVersions;
import net.thevpc.nuts.toolbox.ntomcat.util.NamedItemNotFoundException;
import net.thevpc.nuts.toolbox.ntomcat.util.RunningTomcat;
import net.thevpc.nuts.toolbox.ntomcat.util.TomcatUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LocalTomcat {

    private NutsApplicationContext context;
    private NutsCommandLine cmdLine;
    private Path sharedConfigFolder;

    public LocalTomcat(NutsApplicationContext applicationContext, NutsCommandLine cmdLine) {
        this.setContext(applicationContext);
        this.cmdLine = cmdLine;
        sharedConfigFolder = Paths.get(applicationContext.getVersionFolderFolder(NutsStoreLocation.CONFIG, NTomcatConfigVersions.CURRENT));
    }

    public void runArgs() {
        NutsArgument a;
        cmdLine.setCommandName("tomcat --local");
        while (cmdLine.hasNext()) {
            if (cmdLine.peek().isOption()) {
                context.configureLast(cmdLine);
            } else {
                a = cmdLine.requireNonOption().next();
                switch (a.getString()) {
                    case "list":
                        list(cmdLine);
                        return;
                    case "show":
                    case "describe":
                        describe(cmdLine);
                        return;
                    case "add":
                        add(cmdLine, NutsOpenMode.CREATE_OR_ERROR);
                        return;
                    case "set":
                        add(cmdLine, NutsOpenMode.OPEN_OR_ERROR);
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
                        installApp(cmdLine);
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
                    case "delete":
                        delete(cmdLine);
                        return;
                    case "log":
                        showLog(cmdLine);
                        return;
                    case "port":
                        showPort(cmdLine);
                        return;
                    case "base":
                    case "catalina-base":
                        showCatalinaBase(cmdLine);
                        return;
                    case "home":
                    case "catalina-home":
                        showCatalinaHome(cmdLine);
                        return;
                    case "version":
                    case "catalina-version":
                        showCatalinaVersion(cmdLine);
                    case "ps":
                        ps(cmdLine);
                        return;
                    default:
                        throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unsupported action %s", a.getString()), 1);
                }
            }
        }
        throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("missing tomcat action. Type: nuts tomcat --help"), 1);
    }

    public void list(NutsCommandLine args) {
        NutsArgument a;
        class Helper {

            boolean apps = false;
            boolean domains = false;
            boolean processed = false;
            boolean instances = false;

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
                            context.getSession().out().printf("[[\\[%s\\]]]:\n", "Apps");
                        }
                        for (LocalTomcatAppConfigService app : apps) {
                            context.getSession().out().printf("%s\n", app.getName());
                        }
                    }
                }
                if (isDomains()) {
                    List<LocalTomcatDomainConfigService> domains = c.getDomains();
                    if (!domains.isEmpty()) {
                        if (isHeader()) {
                            context.getSession().out().printf("[[\\[%s\\]]]:\n", "Domains");
                        }
                        for (LocalTomcatDomainConfigService app : domains) {
                            context.getSession().out().printf("%s\n", app.getName());
                        }
                    }
                }
            }
        }
        Helper x = new Helper();
        while (args.hasNext()) {
            if ((a = args.nextBoolean("-a", "--apps")) != null) {
                x.apps = a.getValue().getBoolean();
            } else if ((a = args.nextBoolean("-d", "--domains")) != null) {
                x.domains = a.getValue().getBoolean();
            } else if ((a = args.nextBoolean("-i", "--instances")) != null) {
                x.instances = a.getValue().getBoolean();
            } else {
                context.configureLast(cmdLine);
            }
        }
        if (x.instances) {
            for (LocalTomcatConfigService tomcatConfig : listConfig()) {
                getContext().getSession().out().println(tomcatConfig.getName());
            }
        } else {
            LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NutsOpenMode.OPEN_OR_ERROR);
            x.print(s);
        }
    }

    public void ps(NutsCommandLine args) {
        NutsArgument a;
        String format = "default";
        args.setCommandName("tomcat --local show");
        while (args.hasNext()) {
            if ((a = args.nextBoolean("-l", "--long")) != null) {
                format = "long";
            } else if ((a = args.nextBoolean("-s", "--short")) != null) {
                format = "short";
            } else {
                context.configureLast(cmdLine);
            }
        }
        if (args.isExecMode()) {
            NutsSession session = context.getSession();
            NutsTextManager factory = session.text();
            if (session.isPlainOut()) {
                NutsPrintStream out = session.out();
                for (RunningTomcat jpsResult : TomcatUtils.getRunningInstances(context)) {
                    switch (format) {
                        case "short": {
                            out.printf("%s\n",
                                    factory.forStyled(jpsResult.getPid(), NutsTextStyle.primary1())
                            );
                            break;
                        }
                        case "long": {
                            out.printf("%s v%s HOME: %s BASE: %s ==CMD:== %s\n",
                                    factory.forStyled(jpsResult.getPid(), NutsTextStyle.primary1()),
                                    jpsResult.getHome() == null ? null : TomcatUtils.getFolderCatalinaHomeVersion(Paths.get(jpsResult.getHome())),
                                    jpsResult.getHome(),
                                    jpsResult.getBase(),
                                    context.getCommandLine().parseLine(jpsResult.getArgsLine())
                            );
                            break;
                        }
                        default: {
                            out.printf("%s ==v==%s ==BASE:== %s\n",
                                    factory.forStyled(jpsResult.getPid(), NutsTextStyle.primary1()),
                                    jpsResult.getHome() == null ? null : TomcatUtils.getFolderCatalinaHomeVersion(Paths.get(jpsResult.getHome())),
                                    jpsResult.getBase()
                            );
                            break;
                        }
                    }
                }
            } else {
                context.getSession().formats().object()
                        .setValue(TomcatUtils.getRunningInstances(context))
                        .println();
            }
        }
    }

    public void describe(NutsCommandLine args) {
        NutsArgument a;
        LocalTomcatServiceBase s;
        List<LocalTomcatServiceBase> toShow = new ArrayList<>();
        args.setCommandName("tomcat --local show");
        while (args.hasNext()) {
            if ((s = readBaseServiceArg(args, NutsOpenMode.OPEN_OR_ERROR)) != null) {
                toShow.add(s);
            } else {
                context.configureLast(cmdLine);
            }
        }
        if (args.isExecMode()) {
            if (toShow.isEmpty()) {
                toShow.add(loadServiceBase("", NutsOpenMode.OPEN_OR_ERROR));
            }
            for (LocalTomcatServiceBase s2 : toShow) {
                s2.println(getContext().getSession().out());
            }
        }
    }

    public void add(NutsCommandLine args, NutsOpenMode autoCreate) {
        NutsArgument a = args.nextNonOption();
        if (a != null) {
            switch (a.getString()) {
                case "instance": {
                    LocalTomcatConfigService s = nextLocalTomcatConfigService(args, autoCreate);
                    addInstance(s, args, autoCreate);
                    return;
                }
                case "domain": {
                    LocalTomcatDomainConfigService s = nextLocalTomcatDomainConfigService(args, autoCreate);
                    addDomain(s, args, autoCreate);
                    return;
                }
                case "app": {
                    LocalTomcatAppConfigService s = nextLocalTomcatAppConfigService(args, autoCreate);
                    addApp(s, args, autoCreate);
                    return;
                }
                default: {
                    args.pushBack(a);
                    args.setCommandName("tomcat --local add").unexpectedArgument(NutsMessage.cstyle("expected instance|domain|app"));
                    return;
                }
            }
        }
        args.setCommandName("tomcat --local add").required(NutsMessage.cstyle("expected instance|domain|app"));
    }

    public void addInstance(LocalTomcatConfigService c, NutsCommandLine args, NutsOpenMode autoCreate) {
        NutsArgument a;
        args.setCommandName("tomcat --local add");
        while (args.hasNext()) {
            if ((a = args.nextString("--catalina-version", "--tomcat-version", "--version")) != null) {
                if (c == null) {
                    c = openTomcatConfig("", autoCreate);
                }
                c.getConfig().setCatalinaVersion(a.getValue().getString());
                c.getConfig().setCatalinaHome(null);
                c.getConfig().setCatalinaBase(null);
            } else if ((a = args.nextString("--catalina-base")) != null) {
                if (c == null) {
                    c = openTomcatConfig("", autoCreate);
                }
                c.getConfig().setCatalinaBase(a.getValue().getString());
            } else if ((a = args.nextString("--catalina-home")) != null) {
                if (c == null) {
                    c = openTomcatConfig("", autoCreate);
                }
                c.getConfig().setCatalinaHome(a.getValue().getString());
                c.getConfig().setCatalinaBase(null);
                c.getConfig().setCatalinaVersion(null);
            } else if ((a = args.nextString("--shutdown-wait-time")) != null) {
                if (c == null) {
                    c = openTomcatConfig("", autoCreate);
                }
                c.getConfig().setShutdownWaitTime(a.getValue().getInt());
            } else if ((a = args.nextString("--archive-folder")) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR);
                }
                c.getConfig().setArchiveFolder(a.getValue().getString());
            } else if ((a = args.nextString("--running-folder")) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR);
                }
                c.getConfig().setRunningFolder(a.getValue().getString());
            } else if ((a = args.nextString("--http-port")) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR);
                }
                c.setHttpConnectorPort(false, a.getValue().getInt());
            } else if ((a = args.nextString("--port")) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR);
                }
                c.setHttpConnectorPort(false, a.getValue().getInt());
            } else if ((a = args.nextBoolean("-d", "--dev")) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR);
                }
                c.getConfig().setDev(a.getValue().getBoolean());
            } else {
                context.configureLast(args);
            }
        }
        c.save();
        c.buildCatalinaBase();
    }

    public void addDomain(LocalTomcatDomainConfigService c, NutsCommandLine args, NutsOpenMode autoCreate) {
        NutsArgument a;
//        c = nextLocalTomcatConfigService(args, NutsOpenMode.OPEN_EXISTING);
        boolean changed = false;
        args.setCommandName("tomcat --local add");
        while (args.hasNext()) {
            if ((a = args.nextString("--log")) != null) {
                c.getConfig().setLogFile(a.getValue().getString());
                changed = true;
            } else {
                context.configureLast(args);
            }
        }
        if (changed) {
            c.getTomcat().save();
        }
    }

    public void addApp(LocalTomcatAppConfigService c, NutsCommandLine args, NutsOpenMode autoCreate) {
        NutsArgument a;
        boolean changed = false;
        args.setCommandName("tomcat --local add");
        while (args.hasNext()) {
            if ((a = args.nextString("--source")) != null) {
                String value = a.getValue().getString();
                c.getConfig().setSourceFilePath(value);
                changed = true;
            } else if ((a = args.nextString("--deploy")) != null) {
                String value = a.getValue().getString();
                c.getConfig().setDeployName(value);
                changed = true;
            } else if ((a = args.nextString("--domain")) != null) {
                String value = a.getValue().getString();
                //check that domain exists!!
                c.getTomcat().getDomain(value, NutsOpenMode.OPEN_OR_ERROR);
                c.getConfig().setDomain(value);
                changed = true;
            } else {
                context.configureLast(args);
            }
        }
        if (changed) {
            c.getTomcat().save();
        }
    }

    public void remove(NutsCommandLine args) {
        NutsArgument a = args.nextNonOption();
        if (a != null) {
            NutsSession session = context.getSession();
            switch (a.getString()) {
                case "instance": {
                    LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NutsOpenMode.OPEN_OR_ERROR);
                    if (session.getTerminal().ask()
                            .resetLine()
                            .forBoolean("Confirm Deleting %s?", s.getName()).setDefaultValue(true).getBooleanValue()) {
                        s.remove();
                    }
                    return;
                }
                case "domain": {
                    LocalTomcatDomainConfigService s = nextLocalTomcatDomainConfigService(args, NutsOpenMode.OPEN_OR_ERROR);
                    if (session.getTerminal().ask()
                            .resetLine()
                            .forBoolean("Confirm Deleting %s?", s.getName()).setDefaultValue(true).getBooleanValue()) {
                        s.remove();
                        s.getTomcat().save();
                    }
                    return;
                }
                case "app": {
                    LocalTomcatAppConfigService s = nextLocalTomcatAppConfigService(args, NutsOpenMode.OPEN_OR_ERROR);
                    if (session.getTerminal().ask()
                            .resetLine()
                            .forBoolean("Confirm Deleting %s?", s.getName()).setDefaultValue(true).getBooleanValue()) {
                        s.remove();
                        s.getTomcat().save();
                    }
                    return;
                }
            }
        }
        args.required(NutsMessage.cstyle("expected instance|domain|app"));
    }

    public void stop(NutsCommandLine args) {
        NutsArgument a;
        LocalTomcatConfigService c = nextLocalTomcatConfigService(args, NutsOpenMode.OPEN_OR_ERROR);
        args.setCommandName("tomcat --local stop");
        while (args.hasNext()) {
            context.configureLast(args);
        }
        if (!c.stop()) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unable to stop"), 1);
        }
    }

    public NutsString getBracketsPrefix(String str) {
        return context.getWorkspace().text().builder()
                .append("[")
                .append(str, NutsTextStyle.primary5())
                .append("]");
    }


    public void status(NutsCommandLine args) {
        LocalTomcatConfigService c = null;
        String name = null;
        NutsArgument a;
        try {
            c = nextLocalTomcatConfigService(args, NutsOpenMode.OPEN_OR_ERROR);
            name = c.getName();
        } catch (NamedItemNotFoundException ex) {
            name = ex.getName();
        }
        if (c != null) {
            c.printStatus();
        } else {
            if (context.getSession().isPlainOut()) {
                context.getSession().out().printf("%s Tomcat %s.\n", getBracketsPrefix(name),
                        context.getWorkspace().text().forStyled("not found", NutsTextStyle.error())
                );
            } else {
                context.getSession().eout().add(
                        context.getSession().elem().forObject()
                                .set("config-name", name)
                                .set("status", "not-found")
                                .build()
                );
            }
        }
    }

    public void installApp(NutsCommandLine args) {
        LocalTomcatAppConfigService app = null;
        String version = null;
        String file = null;
        LocalTomcatConfigService s = null;
        NutsArgument a;
        args.setCommandName("tomcat --local install");
        while (args.hasNext()) {
            if ((a = args.nextString("--name")) != null) {
                s = openTomcatConfig(a.getValue().getString(), NutsOpenMode.OPEN_OR_ERROR);
            } else if ((a = args.nextString("--app")) != null) {
                app = loadApp(a.getValue().getString(), NutsOpenMode.OPEN_OR_ERROR);
            } else if ((a = args.nextString("--version")) != null) {
                version = a.getValue().getString();
            } else if ((a = args.nextString("--file")) != null) {
                file = a.getValue().getString();
            } else if ((a = args.nextNonOption()) != null) {
                if (file == null) {
                    file = a.getString();
                } else {
                    args.setCommandName("tomcat --local install").unexpectedArgument();
                }
            } else {
                context.configureLast(args);
            }
        }
        if (app == null) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("tomcat install: Missing Application"), 2);
        }
        if (file == null) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("tomcat install: Missing File"), 2);
        }
        app.install(version, file, true);
    }

    public void delete(NutsCommandLine args) {
        NutsArgument a;
        if (args.hasNext()) {
            if ((a = (args.next("log"))) != null) {
                deleteLog(args);
            } else if ((a = (args.next("temp"))) != null) {
                deleteTemp(args);
            } else if ((a = (args.next("work"))) != null) {
                deleteWork(args);
            } else {
                args.setCommandName("tomcat --local delete").unexpectedArgument();
            }
        } else {
            args.setCommandName("tomcat --local delete").required(NutsMessage.cstyle("missing log|temp|work"));
        }
    }

    private void deleteLog(NutsCommandLine args) {
        LocalTomcatServiceBase s = null;
        boolean all = false;
        NutsArgument a;
        boolean processed = false;
        args.setCommandName("tomcat --local delete-log");
        while (args.hasNext()) {
            if ((a = args.nextBoolean("-a", "--all")) != null) {
                all = a.getValue().getBoolean();
            } else if ((s = readBaseServiceArg(args, NutsOpenMode.OPEN_OR_ERROR)) != null) {
                LocalTomcatConfigService c = toLocalTomcatConfigService(s);
                if (all) {
                    c.deleteAllLog();
                } else {
                    c.deleteOutLog();
                }
                processed = true;
            } else {
                context.configureLast(args);
            }
        }
        if (!processed) {
            LocalTomcatConfigService c = openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR);
            if (all) {
                c.deleteAllLog();
            } else {
                c.deleteOutLog();
            }
        }
    }

    private void deleteTemp(NutsCommandLine args) {
        LocalTomcatServiceBase s = null;
        NutsArgument a;
        boolean processed = false;
        args.setCommandName("tomcat --local delete-temp");
        while (args.hasNext()) {
            if ((s = readBaseServiceArg(args, NutsOpenMode.OPEN_OR_ERROR)) != null) {
                LocalTomcatConfigService c = toLocalTomcatConfigService(s);
                c.deleteTemp();
                processed = true;
            } else {
                context.configureLast(args);
            }
        }
        if (!processed) {
            LocalTomcatConfigService c = openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR);
            c.deleteTemp();
        }
    }

    private void deleteWork(NutsCommandLine args) {
        LocalTomcatServiceBase s = null;
        NutsArgument a;
        boolean processed = false;
        args.setCommandName("tomcat --local delete-work");
        while (args.hasNext()) {
            if ((s = readBaseServiceArg(args, NutsOpenMode.OPEN_OR_ERROR)) != null) {
                LocalTomcatConfigService c = toLocalTomcatConfigService(s);
                c.deleteWork();
                processed = true;
            } else {
                context.configureLast(args);
            }
        }
        if (!processed) {
            LocalTomcatConfigService c = openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR);
            c.deleteWork();
        }
    }

    public void showCatalinaBase(NutsCommandLine args) {
        args.setCommandName("tomcat --local show-catalina-base");
        LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NutsOpenMode.OPEN_OR_ERROR);
        NutsArgument a;
        while (args.hasNext()) {
            context.configureLast(args);
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        context.getWorkspace().formats().object().setSession(context.getSession())
                .setValue(c.getCatalinaBase())
                .println();
    }

    public void showCatalinaVersion(NutsCommandLine args) {
        args.setCommandName("tomcat --local show-catalina-version");
        LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NutsOpenMode.OPEN_OR_ERROR);
        NutsArgument a;
        while (args.hasNext()) {
            context.configureLast(args);
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        context.getWorkspace().formats().object().setSession(context.getSession())
                .setValue(c.getValidCatalinaVersion())
                .println();
    }

    public void showCatalinaHome(NutsCommandLine args) {
        args.setCommandName("tomcat --local show-catalina-home");
        LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NutsOpenMode.OPEN_OR_ERROR);
        NutsArgument a;
        while (args.hasNext()) {
            context.configureLast(args);
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        context.getWorkspace().formats().object().setSession(context.getSession())
                .setValue(c.getCatalinaHome())
                .println();
    }

    public void showPort(NutsCommandLine args) {
        args.setCommandName("tomcat --local port");
        LocalTomcatConfigService c = nextLocalTomcatConfigService(args, NutsOpenMode.OPEN_OR_ERROR);
        NutsArgument a;
        boolean redirect = false;
//        boolean shutdown = false;
//        boolean ajp = false;
        boolean setValue = false;
        String type = "http";
        int newValue = -1;
        List<Runnable> runnables = new ArrayList<>();
        while (args.hasNext()) {
            if ((a = args.nextBoolean("--redirect")) != null) {
                redirect = a.getValue().getBoolean();
            } else if ((a = args.nextBoolean("--shutdown")) != null) {
                type = "shutdown";
            } else if ((a = args.nextBoolean("--ajp")) != null) {
                type = "ajp";
            } else if ((a = args.nextString("--set")) != null) {
                newValue = a.getValue().getInt();
                setValue = true;
            } else if ((a = args.nextString("--set-port")) != null) {
                int port = a.getValue().getInt();
                runnables.add(() -> c.setHttpConnectorPort(false, port));
            } else if ((a = args.nextString("--set-redirect-port")) != null) {
                int port = a.getValue().getInt();
                runnables.add(() -> c.setHttpConnectorPort(true, port));
            } else if ((a = args.nextString("--set-shutdown-port")) != null) {
                int port = a.getValue().getInt();
                runnables.add(() -> c.setShutdownPort(port));
            } else if ((a = args.nextString("--set-ajp-port")) != null) {
                int port = a.getValue().getInt();
                runnables.add(() -> c.setAjpConnectorPort(false, port));
            } else if ((a = args.nextString("--set-redirect-ajp-port")) != null) {
                int port = a.getValue().getInt();
                runnables.add(() -> c.setAjpConnectorPort(true, port));
            } else {
                context.configureLast(args);
            }
        }
        if (setValue) {
            runnables.forEach(Runnable::run);
            int port = newValue;
            switch (type) {
                case "shutdown": {
                    c.setShutdownPort(port);
                    break;
                }
                case "ajp": {
                    c.setAjpConnectorPort(redirect, port);
                    break;
                }
                case "http": {
                    c.setHttpConnectorPort(redirect, port);
                    break;
                }
            }
        } else if (runnables.size() > 0) {
            runnables.forEach(Runnable::run);
        } else {
            int port = 8080;
            switch (type) {
                case "shutdown": {
                    port = c.getShutdownPort();
                    break;
                }
                case "ajp": {
                    port = c.getAjpConnectorPort(redirect);
                    break;
                }
                case "http": {
                    port = c.getHttpConnectorPort(redirect);
                    break;
                }
            }
            context.getWorkspace().formats().object().setSession(context.getSession())
                    .setValue(port)
                    .println();
        }
    }

    public void showLog(NutsCommandLine commandLine) {
        LocalTomcatServiceBase s = nextLocalTomcatServiceBase(commandLine, NutsOpenMode.OPEN_OR_ERROR);
        boolean path = false;
        int count = -1;
        NutsArgument a;
        commandLine.setCommandName("tomcat --local log");
        while (commandLine.hasNext()) {
            if ((a = commandLine.nextString("--path")) != null) {
                path = true;
            } else if (commandLine.peek().isOption() && TomcatUtils.isPositiveInt(commandLine.peek().getString().substring(1))) {
                count = Integer.parseInt(commandLine.next().getString().substring(1));
            } else {
                context.configureLast(commandLine);
            }
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        if (path) {
            getContext().getSession().out().printf("%s\n", c.getOutLogFile());
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
        args.setCommandName("tomcat --local deploy-file");
        while (args.hasNext()) {
            if ((a = args.nextString("--file")) != null) {
                file = a.getValue().getString();
            } else if ((a = args.nextString("--name")) != null) {
                instance = a.getValue().getString();
            } else if ((a = args.nextString("--context")) != null) {
                contextName = a.getValue().getString();
            } else if ((a = args.nextString("--domain")) != null) {
                domain = a.getValue().getString();
            } else if ((a = args.nextNonOption()) != null) {
                if (file == null) {
                    file = a.getString();
                } else {
                    args.setCommandName("tomcat --local deploy-file").unexpectedArgument();
                }
            } else {
                context.configureLast(args);
            }
        }
        if (file == null) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("tomcat deploy: Missing File"), 2);
        }
        LocalTomcatConfigService c = openTomcatConfig(instance, NutsOpenMode.OPEN_OR_ERROR);
        c.deployFile(Paths.get(file), contextName, domain);
    }

    public void deployApp(NutsCommandLine args) {
        String version = null;
        String app = null;
        NutsArgument a;
        args.setCommandName("tomcat --local deploy");
        while (args.hasNext()) {
            if ((a = args.nextString("--version")) != null) {
                version = a.getValue().getString();
            } else if ((a = args.nextString("--app")) != null) {
                app = a.getValue().getString();
            } else if ((a = args.nextNonOption()) != null) {
                if (app == null) {
                    app = a.getString();
                } else {
                    args.setCommandName("tomcat --local deploy").unexpectedArgument();
                }
            } else {
                context.configureLast(args);
            }
        }
        loadApp(app, NutsOpenMode.OPEN_OR_ERROR).deploy(version);
    }

    public void restart(NutsCommandLine args, boolean shutdown) {
        boolean deleteLog = false;
        String instance = null;
        LocalTomcatConfigService[] srvRef = new LocalTomcatConfigService[1];

        List<String> apps = new ArrayList<>();
        List<Runnable> runnables = new ArrayList<>();
        args.setCommandName("tomcat restart");
        while (args.hasNext()) {
            NutsArgument a = null;
            if ((a = args.nextBoolean("--delete-out-log")) != null) {
                deleteLog = a.getValue().getBoolean();
            } else if ((a = args.nextString("--deploy")) != null) {
                apps.add(a.getValue().getString());
            } else if ((a = args.nextString("--port")) != null) {
                int port = a.getValue().getInt();
                runnables.add(() -> srvRef[0].setHttpConnectorPort(false, port));
            } else if ((a = args.nextString("--http-port")) != null) {
                int port = a.getValue().getInt();
                runnables.add(() -> srvRef[0].setHttpConnectorPort(false, port));
            } else if ((a = args.nextString("--redirect-port")) != null) {
                int port = a.getValue().getInt();
                runnables.add(() -> srvRef[0].setHttpConnectorPort(true, port));
            } else if ((a = args.nextString("--shutdown-port")) != null) {
                int port = a.getValue().getInt();
                runnables.add(() -> srvRef[0].setShutdownPort(port));
            } else if ((a = args.nextString("--ajp-port")) != null) {
                int port = a.getValue().getInt();
                runnables.add(() -> srvRef[0].setAjpConnectorPort(false, port));
            } else if ((a = args.nextString("--redirect-ajp-port")) != null) {
                int port = a.getValue().getInt();
                runnables.add(() -> srvRef[0].setAjpConnectorPort(true, port));
            } else if ((a = args.nextNonOption()) != null) {
                if (instance == null) {
                    instance = a.getString();
                } else {
                    args.setCommandName("tomcat --local restart").unexpectedArgument();
                }
            } else {
                context.configureLast(args);
            }
        }
        if (instance == null) {
            instance = "";
        }
        LocalTomcatConfigService c = openTomcatConfig(instance, NutsOpenMode.OPEN_OR_CREATE);
        srvRef[0] = c;
        c.buildCatalinaBase();//need build catalina base befor setting ports...
        runnables.forEach(Runnable::run);
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
        if (Files.isDirectory(sharedConfigFolder)) {
            try (DirectoryStream<Path> pp = Files.newDirectoryStream(sharedConfigFolder,
                    (Path entry) -> entry.getFileName().toString().endsWith(LocalTomcatConfigService.LOCAL_CONFIG_EXT))) {
                for (Path entry : pp) {
                    try {
                        LocalTomcatConfigService c = openTomcatConfig(entry, NutsOpenMode.OPEN_OR_ERROR);
                        all.add(c);
                    } catch (Exception ex) {
                        //ignore
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return all.toArray(new LocalTomcatConfigService[0]);
    }

    public LocalTomcatConfigService openTomcatConfig(String name, NutsOpenMode autoCreate) {
        LocalTomcatConfigService t = new LocalTomcatConfigService(name, this);
        if (autoCreate == null) {
            if (!t.existsConfig()) {
                return null;
            } else {
                autoCreate = NutsOpenMode.OPEN_OR_ERROR;
            }
        }
        t.open(autoCreate);
        return t;
    }

    public LocalTomcatConfigService openTomcatConfig(Path file, NutsOpenMode autoCreate) {
        LocalTomcatConfigService t = new LocalTomcatConfigService(file, this);
        if (autoCreate == null) {
            if (!t.existsConfig()) {
                return null;
            } else {
                autoCreate = NutsOpenMode.OPEN_OR_ERROR;
            }
        }
        t.open(autoCreate);
        return t;
    }

    public LocalTomcatServiceBase loadServiceBase(String name, NutsOpenMode autoCreate) {
        if (".".equals(name)) {
            name = "";
        }
        String[] strings = TomcatUtils.splitInstanceAppPreferInstance(name);
        if (strings[1].isEmpty()) {
            return openTomcatConfig(strings[0], autoCreate);
        } else {
            LocalTomcatConfigService u = openTomcatConfig(strings[0], autoCreate);
            LocalTomcatDomainConfigService d = u.getDomain(strings[1], null);
            LocalTomcatAppConfigService a = u.getApp(strings[1], null);
            if (d != null && a != null) {
                throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("ambiguous name %s. Could be either domain or app", name), 3);
            }
            if (d == null && a == null) {
                throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unknown name %s. it is no domain nor app", name), 3);
            }
            if (d != null) {
                return d;
            }
            return a;
        }
    }

    public LocalTomcatAppConfigService loadApp(String name, NutsOpenMode autoCreate) {
        String[] strings = TomcatUtils.splitInstanceAppPreferApp(name);
        return openTomcatConfig(strings[0], autoCreate).getApp(strings[1], NutsOpenMode.OPEN_OR_ERROR);
    }

    public LocalTomcatDomainConfigService loadDomain(String name, NutsOpenMode autoCreate) {
        String[] strings = TomcatUtils.splitInstanceAppPreferApp(name);
        return openTomcatConfig(strings[0], autoCreate).getDomain(strings[1], NutsOpenMode.OPEN_OR_ERROR);
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

    public LocalTomcatServiceBase nextLocalTomcatServiceBase(NutsCommandLine args, NutsOpenMode autoCreate) {
        if (args.hasNext()) {
            NutsArgument o = args.nextNonOption();
            if (o != null) {
                return loadServiceBase(o.toString(), autoCreate);
            }
        }
        return loadServiceBase("", autoCreate);
    }

    public LocalTomcatConfigService nextLocalTomcatConfigService(NutsCommandLine args, NutsOpenMode autoCreate) {
        if (args.hasNext()) {
            NutsArgument o = args.nextNonOption();
            if (o != null) {
                return openTomcatConfig(o.toString(), autoCreate);
            }
        }
        return openTomcatConfig("", autoCreate);
    }

    public LocalTomcatDomainConfigService nextLocalTomcatDomainConfigService(NutsCommandLine args, NutsOpenMode autoCreate) {
        if (args.hasNext()) {
            NutsArgument o = args.nextNonOption();
            if (o != null) {
                String[] p = TomcatUtils.splitInstanceAppPreferApp(o.toString());
                return openTomcatConfig(p[0], NutsOpenMode.OPEN_OR_ERROR).getDomain(p[1], autoCreate);
            }
        }
        return openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR).getDomain("", autoCreate);
    }

    public LocalTomcatAppConfigService nextLocalTomcatAppConfigService(NutsCommandLine args, NutsOpenMode autoCreate) {
        if (args.hasNext()) {
            NutsArgument o = args.nextNonOption();
            if (o != null) {
                String[] p = TomcatUtils.splitInstanceAppPreferApp(o.toString());
                return openTomcatConfig(p[0], NutsOpenMode.OPEN_OR_ERROR).getApp(p[1], autoCreate);
            }
        }
        return openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR).getApp("", autoCreate);
    }

    public LocalTomcatConfigService readTomcatServiceArg(NutsCommandLine args, NutsOpenMode autoCreate) {
        LocalTomcatServiceBase s = readBaseServiceArg(args, autoCreate);
        return toLocalTomcatConfigService(s);
    }

    public LocalTomcatServiceBase readBaseServiceArg(NutsCommandLine args, NutsOpenMode autoCreate) {
        NutsArgument a;
        if ((a = args.nextString("--name")) != null) {
            return (loadServiceBase(a.getValue().getString(), autoCreate));
        } else if ((a = args.nextString("--app")) != null) {
            return (loadApp(a.getValue().getString(), autoCreate));
        } else if ((a = args.nextString("--domain")) != null) {
            return (loadDomain(a.getValue().getString(), autoCreate));
            //TODO: should remove this line?
        } else if (args.hasNext() && args.peek().isOption() && args.peek().getAll().isDouble()) {
            return null;
        } else if (args.hasNext() && args.peek().isOption()) {
            return null;
        } else if (args.hasNext()) {
            return (loadServiceBase(args.next().getString(), autoCreate));
        } else {
            return null;
        }
    }
}
