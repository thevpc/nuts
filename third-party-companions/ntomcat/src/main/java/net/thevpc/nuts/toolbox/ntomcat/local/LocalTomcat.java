package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.ntomcat.NTomcatConfigVersions;
import net.thevpc.nuts.toolbox.ntomcat.util.NamedItemNotFoundException;
import net.thevpc.nuts.toolbox.ntomcat.util.RunningTomcat;
import net.thevpc.nuts.toolbox.ntomcat.util.TomcatUtils;

import java.util.ArrayList;
import java.util.List;

public class LocalTomcat {

    private NutsApplicationContext context;
    private NutsCommandLine cmdLine;
    private NutsPath sharedConfigFolder;

    public LocalTomcat(NutsApplicationContext applicationContext, NutsCommandLine cmdLine) {
        this.setContext(applicationContext);
        this.cmdLine = cmdLine;
        sharedConfigFolder = applicationContext.getVersionFolder(NutsStoreLocation.CONFIG, NTomcatConfigVersions.CURRENT);
    }

    public void runArgs() {
        NutsArgument a;
        cmdLine.setCommandName("tomcat --local");
        NutsSession session = context.getSession();
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                context.configureLast(cmdLine);
            } else {
                a = cmdLine.nextNonOption().get(session);
                switch (a.asString().get(session)) {
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
                        throw new NutsExecutionException(session, NutsMessage.cstyle("unsupported action %s", a.asString()), 1);
                }
            }
        }
        throw new NutsExecutionException(session, NutsMessage.cstyle("missing tomcat action. Type: nuts tomcat --help"), 1);
    }

    public void list(NutsCommandLine args) {
        NutsArgument a;
        NutsSession session = context.getSession();
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
                            session.out().printf("[[\\[%s\\]]]:\n", "Apps");
                        }
                        for (LocalTomcatAppConfigService app : apps) {
                            session.out().printf("%s\n", app.getName());
                        }
                    }
                }
                if (isDomains()) {
                    List<LocalTomcatDomainConfigService> domains = c.getDomains();
                    if (!domains.isEmpty()) {
                        if (isHeader()) {
                            session.out().printf("[[\\[%s\\]]]:\n", "Domains");
                        }
                        for (LocalTomcatDomainConfigService app : domains) {
                            session.out().printf("%s\n", app.getName());
                        }
                    }
                }
            }
        }
        Helper x = new Helper();
        while (args.hasNext()) {
            if ((a = args.nextBoolean("-a", "--apps").orNull()) != null) {
                x.apps = a.getBooleanValue().get(session);
            } else if ((a = args.nextBoolean("-d", "--domains").orNull()) != null) {
                x.domains = a.getBooleanValue().get(session);
            } else if ((a = args.nextBoolean("-i", "--instances").orNull()) != null) {
                x.instances = a.getBooleanValue().get(session);
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
            if ((a = args.nextBoolean("-l", "--long").orNull()) != null) {
                format = "long";
            } else if ((a = args.nextBoolean("-s", "--short").orNull()) != null) {
                format = "short";
            } else {
                context.configureLast(cmdLine);
            }
        }
        if (args.isExecMode()) {
            NutsSession session = context.getSession();
            NutsTexts factory = NutsTexts.of(session);
            if (session.isPlainOut()) {
                NutsPrintStream out = session.out();
                for (RunningTomcat jpsResult : TomcatUtils.getRunningInstances(context)) {
                    switch (format) {
                        case "short": {
                            out.printf("%s\n",
                                    factory.ofStyled(jpsResult.getPid(), NutsTextStyle.primary1())
                            );
                            break;
                        }
                        case "long": {
                            out.printf("%s v%s HOME: %s BASE: %s ==CMD:== %s\n",
                                    factory.ofStyled(jpsResult.getPid(), NutsTextStyle.primary1()),
                                    jpsResult.getHome() == null ? null : TomcatUtils.getFolderCatalinaHomeVersion(jpsResult.getHome()),
                                    jpsResult.getHome(),
                                    jpsResult.getBase(),
                                    NutsCommandLine.parseSystem(jpsResult.getArgsLine(),session)
                            );
                            break;
                        }
                        default: {
                            out.printf("%s ==v==%s ==BASE:== %s\n",
                                    factory.ofStyled(jpsResult.getPid(), NutsTextStyle.primary1()),
                                    jpsResult.getHome() == null ? null : TomcatUtils.getFolderCatalinaHomeVersion(jpsResult.getHome()),
                                    jpsResult.getBase()
                            );
                            break;
                        }
                    }
                }
            } else {
                NutsObjectFormat.of(context.getSession())
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
        args.setCommandName("tomcat --local add");
        NutsSession session = getContext().getSession();
        NutsArgument a = args.nextNonOption().get(session);
        if (a != null) {
            switch (a.asString().get(session)) {
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
                    args.pushBack(a, session);
                    args.setCommandName("tomcat --local add").throwUnexpectedArgument(NutsMessage.cstyle("expected instance|domain|app"), session);
                    return;
                }
            }
        }
        args.setCommandName("tomcat --local add")
                .throwMissingArgument(NutsMessage.cstyle("expected instance|domain|app"),session);
    }

    public void addInstance(LocalTomcatConfigService c, NutsCommandLine args, NutsOpenMode autoCreate) {
        NutsArgument a;
        NutsSession session = getContext().getSession();
        args.setCommandName("tomcat --local add");
        while (args.hasNext()) {
            if ((a = args.nextString("--catalina-version", "--tomcat-version", "--version").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", autoCreate);
                }
                c.getConfig().setCatalinaVersion(a.getStringValue().get(session));
                c.getConfig().setCatalinaHome(null);
                c.getConfig().setCatalinaBase(null);
            } else if ((a = args.nextString("--catalina-base").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", autoCreate);
                }
                c.getConfig().setCatalinaBase(a.getStringValue().get(session));
            } else if ((a = args.nextString("--catalina-home").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", autoCreate);
                }
                c.getConfig().setCatalinaHome(a.getStringValue().get(session));
                c.getConfig().setCatalinaBase(null);
                c.getConfig().setCatalinaVersion(null);
            } else if ((a = args.nextString("--shutdown-wait-time").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", autoCreate);
                }
                c.getConfig().setShutdownWaitTime(a.getValue().asInt().get(session));
            } else if ((a = args.nextString("--archive-folder").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR);
                }
                c.getConfig().setArchiveFolder(a.getStringValue().get(session));
            } else if ((a = args.nextString("--running-folder").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR);
                }
                c.getConfig().setRunningFolder(a.getStringValue().get(session));
            } else if ((a = args.nextString("--http-port").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR);
                }
                c.setHttpConnectorPort(false, a.getValue().asInt().get(session));
            } else if ((a = args.nextString("--port").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR);
                }
                c.setHttpConnectorPort(false, a.getValue().asInt().get(session));
            } else if ((a = args.nextBoolean("-d", "--dev").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR);
                }
                c.getConfig().setDev(a.getBooleanValue().get(session));
            } else {
                context.configureLast(args);
            }
        }
        c.save();
        c.buildCatalinaBase();
    }

    public void addDomain(LocalTomcatDomainConfigService c, NutsCommandLine args, NutsOpenMode autoCreate) {
        NutsArgument a;
        NutsSession session = getContext().getSession();
//        c = nextLocalTomcatConfigService(args, NutsOpenMode.OPEN_EXISTING);
        boolean changed = false;
        args.setCommandName("tomcat --local add");
        while (args.hasNext()) {
            if ((a = args.nextString("--log").orNull()) != null) {
                c.getConfig().setLogFile(a.getStringValue().get(session));
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
        NutsSession session = getContext().getSession();
        boolean changed = false;
        args.setCommandName("tomcat --local add");
        while (args.hasNext()) {
            if ((a = args.nextString("--source").orNull()) != null) {
                String value = a.getStringValue().get(session);
                c.getConfig().setSourceFilePath(value);
                changed = true;
            } else if ((a = args.nextString("--deploy").orNull()) != null) {
                String value = a.getStringValue().get(session);
                c.getConfig().setDeployName(value);
                changed = true;
            } else if ((a = args.nextString("--domain").orNull()) != null) {
                String value = a.getStringValue().get(session);
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
        NutsSession session = getContext().getSession();
        NutsArgument a = args.nextNonOption().get(session);
        if (a != null) {
            switch (a.asString().get(session)) {
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
        args.throwMissingArgument(NutsMessage.cstyle("expected instance|domain|app"),session);
    }

    public void stop(NutsCommandLine args) {
        NutsSession session = getContext().getSession();
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
        return NutsTexts.of(context.getSession()).builder()
                .append("[")
                .append(str, NutsTextStyle.primary5())
                .append("]");
    }


    public void status(NutsCommandLine args) {
        NutsSession session = getContext().getSession();
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
            if (session.isPlainOut()) {
                session.out().printf("%s Tomcat %s.\n", getBracketsPrefix(name),
                        NutsTexts.of(session).ofStyled("not found", NutsTextStyle.error())
                );
            } else {
                session.eout().add(
                        NutsElements.of(session).ofObject()
                                .set("config-name", name)
                                .set("status", "not-found")
                                .build()
                );
            }
        }
    }

    public void installApp(NutsCommandLine args) {
        NutsSession session = getContext().getSession();
        LocalTomcatAppConfigService app = null;
        String version = null;
        String file = null;
        LocalTomcatConfigService s = null;
        NutsArgument a;
        args.setCommandName("tomcat --local install");
        while (args.hasNext()) {
            if ((a = args.nextString("--name").orNull()) != null) {
                s = openTomcatConfig(a.getStringValue().get(session), NutsOpenMode.OPEN_OR_ERROR);
            } else if ((a = args.nextString("--app").orNull()) != null) {
                app = loadApp(a.getStringValue().get(session), NutsOpenMode.OPEN_OR_ERROR);
            } else if ((a = args.nextString("--version").orNull()) != null) {
                version = a.getStringValue().get(session);
            } else if ((a = args.nextString("--file").orNull()) != null) {
                file = a.getStringValue().get(session);
            } else if ((a = args.nextNonOption().get(session)) != null) {
                if (file == null) {
                    file = a.asString().get(session);
                } else {
                    args.setCommandName("tomcat --local install").throwUnexpectedArgument(session);
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
        NutsSession session = getContext().getSession();
        NutsArgument a;
        if (args.hasNext()) {
            if ((a = (args.next("log")).orNull()) != null) {
                deleteLog(args);
            } else if ((a = (args.next("temp")).orNull()) != null) {
                deleteTemp(args);
            } else if ((a = (args.next("work")).orNull()) != null) {
                deleteWork(args);
            } else {
                args.setCommandName("tomcat --local delete").throwUnexpectedArgument(session);
            }
        } else {
            args.setCommandName("tomcat --local delete").throwUnexpectedArgument(NutsMessage.cstyle("missing log|temp|work"),session);
        }
    }

    private void deleteLog(NutsCommandLine args) {
        NutsSession session = getContext().getSession();
        LocalTomcatServiceBase s = null;
        boolean all = false;
        NutsArgument a;
        boolean processed = false;
        args.setCommandName("tomcat --local delete-log");
        while (args.hasNext()) {
            if ((a = args.nextBoolean("-a", "--all").orNull()) != null) {
                all = a.getBooleanValue().get(session);
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
        NutsSession session = getContext().getSession();
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
        NutsSession session = getContext().getSession();
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
        NutsSession session = getContext().getSession();
        args.setCommandName("tomcat --local show-catalina-base");
        LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NutsOpenMode.OPEN_OR_ERROR);
        NutsArgument a;
        while (args.hasNext()) {
            context.configureLast(args);
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        NutsObjectFormat.of(context.getSession())
                .setValue(c.getCatalinaBase())
                .println();
    }

    public void showCatalinaVersion(NutsCommandLine args) {
        NutsSession session = getContext().getSession();
        args.setCommandName("tomcat --local show-catalina-version");
        LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NutsOpenMode.OPEN_OR_ERROR);
        NutsArgument a;
        while (args.hasNext()) {
            context.configureLast(args);
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        NutsObjectFormat.of(context.getSession())
                .setValue(c.getValidCatalinaVersion())
                .println();
    }

    public void showCatalinaHome(NutsCommandLine args) {
        NutsSession session = getContext().getSession();
        args.setCommandName("tomcat --local show-catalina-home");
        LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NutsOpenMode.OPEN_OR_ERROR);
        NutsArgument a;
        while (args.hasNext()) {
            context.configureLast(args);
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        NutsObjectFormat.of(context.getSession())
                .setValue(c.getCatalinaHome())
                .println();
    }

    public void showPort(NutsCommandLine args) {
        NutsSession session = getContext().getSession();
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
            if ((a = args.nextBoolean("--redirect").orNull()) != null) {
                redirect = a.getBooleanValue().get(session);
            } else if ((a = args.nextBoolean("--shutdown").orNull()) != null) {
                type = "shutdown";
            } else if ((a = args.nextBoolean("--ajp").orNull()) != null) {
                type = "ajp";
            } else if ((a = args.nextString("--set").orNull()) != null) {
                newValue = a.getValue().asInt().get(session);
                setValue = true;
            } else if ((a = args.nextString("--set-port").orNull()) != null) {
                int port = a.getValue().asInt().get(session);
                runnables.add(() -> c.setHttpConnectorPort(false, port));
            } else if ((a = args.nextString("--set-redirect-port").orNull()) != null) {
                int port = a.getValue().asInt().get(session);
                runnables.add(() -> c.setHttpConnectorPort(true, port));
            } else if ((a = args.nextString("--set-shutdown-port").orNull()) != null) {
                int port = a.getValue().asInt().get(session);
                runnables.add(() -> c.setShutdownPort(port));
            } else if ((a = args.nextString("--set-ajp-port").orNull()) != null) {
                int port = a.getValue().asInt().get(session);
                runnables.add(() -> c.setAjpConnectorPort(false, port));
            } else if ((a = args.nextString("--set-redirect-ajp-port").orNull()) != null) {
                int port = a.getValue().asInt().get(session);
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
            NutsObjectFormat.of(context.getSession())
                    .setValue(port)
                    .println();
        }
    }

    public void showLog(NutsCommandLine commandLine) {
        NutsSession session = getContext().getSession();
        LocalTomcatServiceBase s = nextLocalTomcatServiceBase(commandLine, NutsOpenMode.OPEN_OR_ERROR);
        boolean path = false;
        int count = -1;
        NutsArgument a;
        commandLine.setCommandName("tomcat --local log");
        while (commandLine.hasNext()) {
            if ((a = commandLine.nextString("--path").orNull()) != null) {
                path = true;
            } else if (commandLine.isNextOption() && TomcatUtils.isPositiveInt(commandLine.peek()
                    .get(session)
                    .asString().get(session).substring(1))) {
                count = Integer.parseInt(commandLine.next().flatMap(NutsValue::asString).get(session).substring(1));
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
        NutsSession session = getContext().getSession();
        String instance = null;
        String version = null;
        String file = null;
        String app = null;
        String domain = null;
        String contextName = null;
        NutsArgument a;
        args.setCommandName("tomcat --local deploy-file");
        while (args.hasNext()) {
            if ((a = args.nextString("--file").orNull()) != null) {
                file = a.getStringValue().get(session);
            } else if ((a = args.nextString("--name").orNull()) != null) {
                instance = a.getStringValue().get(session);
            } else if ((a = args.nextString("--context").orNull()) != null) {
                contextName = a.getStringValue().get(session);
            } else if ((a = args.nextString("--domain").orNull()) != null) {
                domain = a.getStringValue().get(session);
            } else if ((a = args.nextNonOption().orNull()) != null) {
                if (file == null) {
                    file = a.asString().get(session);
                } else {
                    args.setCommandName("tomcat --local deploy-file").throwUnexpectedArgument(session);
                }
            } else {
                context.configureLast(args);
            }
        }
        if (file == null) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("tomcat deploy: Missing File"), 2);
        }
        LocalTomcatConfigService c = openTomcatConfig(instance, NutsOpenMode.OPEN_OR_ERROR);
        c.deployFile(NutsPath.of(file,getContext().getSession()), contextName, domain);
    }

    public void deployApp(NutsCommandLine args) {
        NutsSession session = getContext().getSession();
        String version = null;
        String app = null;
        NutsArgument a;
        args.setCommandName("tomcat --local deploy");
        while (args.hasNext()) {
            if ((a = args.nextString("--version").orNull()) != null) {
                version = a.getStringValue().get(session);
            } else if ((a = args.nextString("--app").orNull()) != null) {
                app = a.getStringValue().get(session);
            } else if ((a = args.nextNonOption().orNull()) != null) {
                if (app == null) {
                    app = a.asString().get(session);
                } else {
                    args.setCommandName("tomcat --local deploy").throwUnexpectedArgument(session);
                }
            } else {
                context.configureLast(args);
            }
        }
        loadApp(app, NutsOpenMode.OPEN_OR_ERROR).deploy(version);
    }

    public void restart(NutsCommandLine args, boolean shutdown) {
        NutsSession session = getContext().getSession();
        boolean deleteLog = false;
        String instance = null;
        LocalTomcatConfigService[] srvRef = new LocalTomcatConfigService[1];

        List<String> apps = new ArrayList<>();
        List<Runnable> runnables = new ArrayList<>();
        args.setCommandName("tomcat restart");
        while (args.hasNext()) {
            NutsArgument a = null;
            if ((a = args.nextBoolean("--delete-out-log").orNull()) != null) {
                deleteLog = a.getBooleanValue().get(session);
            } else if ((a = args.nextString("--deploy").orNull()) != null) {
                apps.add(a.getStringValue().get(session));
            } else if ((a = args.nextString("--port").orNull()) != null) {
                int port = a.getValue().asInt().get(session);
                runnables.add(() -> srvRef[0].setHttpConnectorPort(false, port));
            } else if ((a = args.nextString("--http-port").orNull()) != null) {
                int port = a.getValue().asInt().get(session);
                runnables.add(() -> srvRef[0].setHttpConnectorPort(false, port));
            } else if ((a = args.nextString("--redirect-port").orNull()) != null) {
                int port = a.getValue().asInt().get(session);
                runnables.add(() -> srvRef[0].setHttpConnectorPort(true, port));
            } else if ((a = args.nextString("--shutdown-port").orNull()) != null) {
                int port = a.getValue().asInt().get(session);
                runnables.add(() -> srvRef[0].setShutdownPort(port));
            } else if ((a = args.nextString("--ajp-port").orNull()) != null) {
                int port = a.getValue().asInt().get(session);
                runnables.add(() -> srvRef[0].setAjpConnectorPort(false, port));
            } else if ((a = args.nextString("--redirect-ajp-port").orNull()) != null) {
                int port = a.getValue().asInt().get(session);
                runnables.add(() -> srvRef[0].setAjpConnectorPort(true, port));
            } else if ((a = args.nextNonOption().orNull()) != null) {
                if (instance == null) {
                    instance = a.asString().get(session);
                } else {
                    args.setCommandName("tomcat --local restart").throwUnexpectedArgument(session);
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
        return
                sharedConfigFolder.list().filter(
                                pathname -> pathname.isRegularFile() &&  pathname.getName().toString().endsWith(LocalTomcatConfigService.LOCAL_CONFIG_EXT),
                                "isRegularFile() && matches(*"+ LocalTomcatConfigService.LOCAL_CONFIG_EXT+")"
                        )
                        .mapUnsafe(
                                NutsUnsafeFunction.of(x->openTomcatConfig(x, NutsOpenMode.OPEN_OR_ERROR),"openTomcatConfig")
                                ,null)
                        .filterNonNull()
                        .toArray(LocalTomcatConfigService[]::new);
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

    public LocalTomcatConfigService openTomcatConfig(NutsPath file, NutsOpenMode autoCreate) {
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
        NutsSession session = getContext().getSession();
        if (args.hasNext()) {
            NutsArgument o = args.nextNonOption().orNull();
            if (o != null) {
                return loadServiceBase(o.toString(), autoCreate);
            }
        }
        return loadServiceBase("", autoCreate);
    }

    public LocalTomcatConfigService nextLocalTomcatConfigService(NutsCommandLine args, NutsOpenMode autoCreate) {
        NutsSession session = getContext().getSession();
        if (args.hasNext()) {
            NutsArgument o = args.nextNonOption().orNull();
            if (o != null) {
                return openTomcatConfig(o.toString(), autoCreate);
            }
        }
        return openTomcatConfig("", autoCreate);
    }

    public LocalTomcatDomainConfigService nextLocalTomcatDomainConfigService(NutsCommandLine args, NutsOpenMode autoCreate) {
        NutsSession session = getContext().getSession();
        if (args.hasNext()) {
            NutsArgument o = args.nextNonOption().orNull();
            if (o != null) {
                String[] p = TomcatUtils.splitInstanceAppPreferApp(o.toString());
                return openTomcatConfig(p[0], NutsOpenMode.OPEN_OR_ERROR).getDomain(p[1], autoCreate);
            }
        }
        return openTomcatConfig("", NutsOpenMode.OPEN_OR_ERROR).getDomain("", autoCreate);
    }

    public LocalTomcatAppConfigService nextLocalTomcatAppConfigService(NutsCommandLine args, NutsOpenMode autoCreate) {
        NutsSession session = getContext().getSession();
        if (args.hasNext()) {
            NutsArgument o = args.nextNonOption().orNull();
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
        NutsSession session = getContext().getSession();
        NutsArgument a;
        if ((a = args.nextString("--name").orNull()) != null) {
            return (loadServiceBase(a.getStringValue().get(session), autoCreate));
        } else if ((a = args.nextString("--app").orNull()) != null) {
            return (loadApp(a.getStringValue().get(session), autoCreate));
        } else if ((a = args.nextString("--domain").orNull()) != null) {
            return (loadDomain(a.getStringValue().get(session), autoCreate));
            //TODO: should remove this line?
        } else if (args.hasNext() && args.isNextOption() && args.peek().get(session).isDouble()) {
            return null;
        } else if (args.hasNext() && args.isNextOption()) {
            return null;
        } else if (args.hasNext()) {
            return (loadServiceBase(args.next().flatMap(NutsValue::asString).get(session), autoCreate));
        } else {
            return null;
        }
    }
}
