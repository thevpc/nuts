package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NObjectFormat;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.ntomcat.NTomcatConfigVersions;
import net.thevpc.nuts.toolbox.ntomcat.util.NamedItemNotFoundException;
import net.thevpc.nuts.toolbox.ntomcat.util.RunningTomcat;
import net.thevpc.nuts.toolbox.ntomcat.util.TomcatUtils;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NUnsafeFunction;

import java.util.ArrayList;
import java.util.List;

public class LocalTomcat {

    private NApplicationContext context;
    private NCommandLine cmdLine;
    private NPath sharedConfigFolder;

    public LocalTomcat(NApplicationContext applicationContext, NCommandLine cmdLine) {
        this.setContext(applicationContext);
        this.cmdLine = cmdLine;
        sharedConfigFolder = applicationContext.getVersionFolder(NStoreLocation.CONFIG, NTomcatConfigVersions.CURRENT);
    }

    public void runArgs() {
        NArg a;
        cmdLine.setCommandName("tomcat --local");
        NSession session = context.getSession();
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
                        add(cmdLine, NOpenMode.CREATE_OR_ERROR);
                        return;
                    case "set":
                        add(cmdLine, NOpenMode.OPEN_OR_ERROR);
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
                        return;
                    case "ps":
                        ps(cmdLine);
                        return;
                    default:
                        throw new NExecutionException(session, NMsg.ofC("unsupported action %s", a.asString()), 1);
                }
            }
        }
        throw new NExecutionException(session, NMsg.ofPlain("missing tomcat action. Type: nuts tomcat --help"), 1);
    }

    public void list(NCommandLine args) {
        NArg a;
        NSession session = context.getSession();
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
                            session.out().println(NMsg.ofC("[%s]:", "Apps"));
                        }
                        for (LocalTomcatAppConfigService app : apps) {
                            session.out().print(NMsg.ofPlain(app.getName()));
                        }
                    }
                }
                if (isDomains()) {
                    List<LocalTomcatDomainConfigService> domains = c.getDomains();
                    if (!domains.isEmpty()) {
                        if (isHeader()) {
                            session.out().println(NMsg.ofC("[%s]:", "Domains"));
                        }
                        for (LocalTomcatDomainConfigService app : domains) {
                            session.out().println(NMsg.ofPlain(app.getName()));
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
            LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NOpenMode.OPEN_OR_ERROR);
            x.print(s);
        }
    }

    public void ps(NCommandLine args) {
        NSession session = context.getSession();
        NRef<String> format = NRef.of("default");
        args.setCommandName("tomcat --local show");
        while (args.hasNext()) {
            if (args.withNextTrue((b, a, ss) -> format.set("long"), "-l", "--long")) {
            } else {
                context.configureLast(cmdLine);
            }
        }
        if (args.isExecMode()) {
            NTexts factory = NTexts.of(session);
            if (session.isPlainOut()) {
                NOutputStream out = session.out();
                for (RunningTomcat jpsResult : TomcatUtils.getRunningInstances(context)) {
                    switch (format.get()) {
                        case "long": {
                            NCommandLine nutsArguments = NCommandLine.parseSystem(jpsResult.getArgsLine(), session).orNull();
                            out.println(NMsg.ofC("%s: %s %s: v%s %s: %s %s: %s %s: %s",
                                    NMsg.ofStyled("pid", NTextStyle.comments()),
                                    factory.ofStyled(jpsResult.getPid(), NTextStyle.primary1()),
                                    NMsg.ofStyled("version", NTextStyle.comments()),
                                    jpsResult.getVersion(),
                                    NMsg.ofStyled("home", NTextStyle.comments()),
                                    jpsResult.getHome(),
                                    NMsg.ofStyled("base", NTextStyle.comments()),
                                    NMsg.ofStyled(jpsResult.getBase() == null ? "?" : jpsResult.getBase(), NTextStyle.path()),
                                    NMsg.ofStyled("cmd", NTextStyle.comments()),
                                    nutsArguments == null ? jpsResult.getArgsLine() : nutsArguments
                            ));
                            break;
                        }
                        default: {
                            out.println(factory.ofStyled(jpsResult.getPid(), NTextStyle.primary1()));
                            break;
                        }
                    }
                }
            } else {
                NObjectFormat.of(session)
                        .setValue(TomcatUtils.getRunningInstances(context))
                        .println();
            }
        }
    }

    public void describe(NCommandLine args) {
        NArg a;
        LocalTomcatServiceBase s;
        List<LocalTomcatServiceBase> toShow = new ArrayList<>();
        args.setCommandName("tomcat --local show");
        while (args.hasNext()) {
            if ((s = readBaseServiceArg(args, NOpenMode.OPEN_OR_ERROR)) != null) {
                toShow.add(s);
            } else {
                context.configureLast(cmdLine);
            }
        }
        if (args.isExecMode()) {
            if (toShow.isEmpty()) {
                toShow.add(loadServiceBase("", NOpenMode.OPEN_OR_ERROR));
            }
            for (LocalTomcatServiceBase s2 : toShow) {
                s2.println(getContext().getSession().out());
            }
        }
    }

    public void add(NCommandLine args, NOpenMode autoCreate) {
        args.setCommandName("tomcat --local add");
        NSession session = getContext().getSession();
        NArg a = args.nextNonOption().get(session);
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
                    args.pushBack(a);
                    args.setCommandName("tomcat --local add").throwUnexpectedArgument(NMsg.ofPlain("expected instance|domain|app"));
                    return;
                }
            }
        }
        args.setCommandName("tomcat --local add")
                .throwMissingArgument(NMsg.ofPlain("expected instance|domain|app"));
    }

    public void addInstance(LocalTomcatConfigService c, NCommandLine args, NOpenMode autoCreate) {
        NArg a;
        NSession session = getContext().getSession();
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
                    c = openTomcatConfig("", NOpenMode.OPEN_OR_ERROR);
                }
                c.getConfig().setArchiveFolder(a.getStringValue().get(session));
            } else if ((a = args.nextString("--running-folder").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NOpenMode.OPEN_OR_ERROR);
                }
                c.getConfig().setRunningFolder(a.getStringValue().get(session));
            } else if ((a = args.nextString("--http-port").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NOpenMode.OPEN_OR_ERROR);
                }
                c.setHttpConnectorPort(false, a.getValue().asInt().get(session));
            } else if ((a = args.nextString("--port").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NOpenMode.OPEN_OR_ERROR);
                }
                c.setHttpConnectorPort(false, a.getValue().asInt().get(session));
            } else if ((a = args.nextBoolean("-d", "--dev").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NOpenMode.OPEN_OR_ERROR);
                }
                c.getConfig().setDev(a.getBooleanValue().get(session));
            } else {
                context.configureLast(args);
            }
        }
        c.save();
        c.buildCatalinaBase();
    }

    public void addDomain(LocalTomcatDomainConfigService c, NCommandLine args, NOpenMode autoCreate) {
        NArg a;
        NSession session = getContext().getSession();
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

    public void addApp(LocalTomcatAppConfigService c, NCommandLine args, NOpenMode autoCreate) {
        NArg a;
        NSession session = getContext().getSession();
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
                c.getTomcat().getDomain(value, NOpenMode.OPEN_OR_ERROR);
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

    public void remove(NCommandLine args) {
        NSession session = getContext().getSession();
        NArg a = args.nextNonOption().get(session);
        if (a != null) {
            switch (a.asString().get(session)) {
                case "instance": {
                    LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NOpenMode.OPEN_OR_ERROR);
                    if (session.getTerminal().ask()
                            .resetLine()
                            .forBoolean(NMsg.ofC("Confirm Deleting %s?", s.getName())).setDefaultValue(true).getBooleanValue()) {
                        s.remove();
                    }
                    return;
                }
                case "domain": {
                    LocalTomcatDomainConfigService s = nextLocalTomcatDomainConfigService(args, NOpenMode.OPEN_OR_ERROR);
                    if (session.getTerminal().ask()
                            .resetLine()
                            .forBoolean(NMsg.ofC("Confirm Deleting %s?", s.getName())).setDefaultValue(true).getBooleanValue()) {
                        s.remove();
                        s.getTomcat().save();
                    }
                    return;
                }
                case "app": {
                    LocalTomcatAppConfigService s = nextLocalTomcatAppConfigService(args, NOpenMode.OPEN_OR_ERROR);
                    if (session.getTerminal().ask()
                            .resetLine()
                            .forBoolean(NMsg.ofC("Confirm Deleting %s?", s.getName())).setDefaultValue(true).getBooleanValue()) {
                        s.remove();
                        s.getTomcat().save();
                    }
                    return;
                }
            }
        }
        args.throwMissingArgument(NMsg.ofPlain("expected instance|domain|app"));
    }

    public void stop(NCommandLine args) {
        NSession session = getContext().getSession();
        NArg a;
        LocalTomcatConfigService c = nextLocalTomcatConfigService(args, NOpenMode.OPEN_OR_ERROR);
        args.setCommandName("tomcat --local stop");
        while (args.hasNext()) {
            context.configureLast(args);
        }
        if (!c.stop()) {
            throw new NExecutionException(context.getSession(), NMsg.ofPlain("unable to stop"), 1);
        }
    }

    public NString getBracketsPrefix(String str) {
        return NTexts.of(context.getSession()).ofBuilder()
                .append("[")
                .append(str, NTextStyle.primary5())
                .append("]");
    }


    public void status(NCommandLine args) {
        NSession session = getContext().getSession();
        LocalTomcatConfigService c = null;
        String name = null;
        NArg a;
        try {
            c = nextLocalTomcatConfigService(args, NOpenMode.OPEN_OR_ERROR);
            name = c.getName();
        } catch (NamedItemNotFoundException ex) {
            name = ex.getName();
        }
        if (c != null) {
            c.printStatus();
        } else {
            if (session.isPlainOut()) {
                session.out().println(NMsg.ofC("%s Tomcat %s.", getBracketsPrefix(name),
                        NTexts.of(session).ofStyled("not found", NTextStyle.error())
                ));
            } else {
                session.eout().add(
                        NElements.of(session).ofObject()
                                .set("config-name", name)
                                .set("status", "not-found")
                                .build()
                );
            }
        }
    }

    public void installApp(NCommandLine args) {
        NSession session = getContext().getSession();
        LocalTomcatAppConfigService app = null;
        String version = null;
        String file = null;
        LocalTomcatConfigService s = null;
        NArg a;
        args.setCommandName("tomcat --local install");
        while (args.hasNext()) {
            if ((a = args.nextString("--name").orNull()) != null) {
                s = openTomcatConfig(a.getStringValue().get(session), NOpenMode.OPEN_OR_ERROR);
            } else if ((a = args.nextString("--app").orNull()) != null) {
                app = loadApp(a.getStringValue().get(session), NOpenMode.OPEN_OR_ERROR);
            } else if ((a = args.nextString("--version").orNull()) != null) {
                version = a.getStringValue().get(session);
            } else if ((a = args.nextString("--file").orNull()) != null) {
                file = a.getStringValue().get(session);
            } else if ((a = args.nextNonOption().get(session)) != null) {
                if (file == null) {
                    file = a.asString().get(session);
                } else {
                    args.setCommandName("tomcat --local install").throwUnexpectedArgument();
                }
            } else {
                context.configureLast(args);
            }
        }
        if (app == null) {
            throw new NExecutionException(context.getSession(), NMsg.ofPlain("tomcat install: Missing Application"), 2);
        }
        if (file == null) {
            throw new NExecutionException(context.getSession(), NMsg.ofPlain("tomcat install: Missing File"), 2);
        }
        app.install(version, file, true);
    }

    public void delete(NCommandLine args) {
        NSession session = getContext().getSession();
        NArg a;
        if (args.hasNext()) {
            if ((a = (args.next("log")).orNull()) != null) {
                deleteLog(args);
            } else if ((a = (args.next("temp")).orNull()) != null) {
                deleteTemp(args);
            } else if ((a = (args.next("work")).orNull()) != null) {
                deleteWork(args);
            } else {
                args.setCommandName("tomcat --local delete").throwUnexpectedArgument();
            }
        } else {
            args.setCommandName("tomcat --local delete").throwUnexpectedArgument(NMsg.ofPlain("missing log|temp|work"));
        }
    }

    private void deleteLog(NCommandLine args) {
        NSession session = getContext().getSession();
        LocalTomcatServiceBase s = null;
        boolean all = false;
        NArg a;
        boolean processed = false;
        args.setCommandName("tomcat --local delete-log");
        while (args.hasNext()) {
            if ((a = args.nextBoolean("-a", "--all").orNull()) != null) {
                all = a.getBooleanValue().get(session);
            } else if ((s = readBaseServiceArg(args, NOpenMode.OPEN_OR_ERROR)) != null) {
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
            LocalTomcatConfigService c = openTomcatConfig("", NOpenMode.OPEN_OR_ERROR);
            if (all) {
                c.deleteAllLog();
            } else {
                c.deleteOutLog();
            }
        }
    }

    private void deleteTemp(NCommandLine args) {
        NSession session = getContext().getSession();
        LocalTomcatServiceBase s = null;
        NArg a;
        boolean processed = false;
        args.setCommandName("tomcat --local delete-temp");
        while (args.hasNext()) {
            if ((s = readBaseServiceArg(args, NOpenMode.OPEN_OR_ERROR)) != null) {
                LocalTomcatConfigService c = toLocalTomcatConfigService(s);
                c.deleteTemp();
                processed = true;
            } else {
                context.configureLast(args);
            }
        }
        if (!processed) {
            LocalTomcatConfigService c = openTomcatConfig("", NOpenMode.OPEN_OR_ERROR);
            c.deleteTemp();
        }
    }

    private void deleteWork(NCommandLine args) {
        NSession session = getContext().getSession();
        LocalTomcatServiceBase s = null;
        NArg a;
        boolean processed = false;
        args.setCommandName("tomcat --local delete-work");
        while (args.hasNext()) {
            if ((s = readBaseServiceArg(args, NOpenMode.OPEN_OR_ERROR)) != null) {
                LocalTomcatConfigService c = toLocalTomcatConfigService(s);
                c.deleteWork();
                processed = true;
            } else {
                context.configureLast(args);
            }
        }
        if (!processed) {
            LocalTomcatConfigService c = openTomcatConfig("", NOpenMode.OPEN_OR_ERROR);
            c.deleteWork();
        }
    }

    public void showCatalinaBase(NCommandLine args) {
        NSession session = getContext().getSession();
        args.setCommandName("tomcat --local show-catalina-base");
        LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NOpenMode.OPEN_OR_ERROR);
        NArg a;
        while (args.hasNext()) {
            context.configureLast(args);
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        NObjectFormat.of(context.getSession())
                .setValue(c.getCatalinaBase())
                .println();
    }

    public void showCatalinaVersion(NCommandLine args) {
        NSession session = getContext().getSession();
        args.setCommandName("tomcat --local show-catalina-version");
        LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NOpenMode.OPEN_OR_ERROR);
        NArg a;
        while (args.hasNext()) {
            context.configureLast(args);
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        NObjectFormat.of(context.getSession())
                .setValue(c.getValidCatalinaVersion())
                .println();
    }

    public void showCatalinaHome(NCommandLine args) {
        NSession session = getContext().getSession();
        args.setCommandName("tomcat --local show-catalina-home");
        LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NOpenMode.OPEN_OR_ERROR);
        NArg a;
        while (args.hasNext()) {
            context.configureLast(args);
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        NObjectFormat.of(context.getSession())
                .setValue(c.getCatalinaHome())
                .println();
    }

    public void showPort(NCommandLine args) {
        NSession session = getContext().getSession();
        args.setCommandName("tomcat --local port");
        LocalTomcatConfigService c = nextLocalTomcatConfigService(args, NOpenMode.OPEN_OR_ERROR);
        NArg a;
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
            NObjectFormat.of(context.getSession())
                    .setValue(port)
                    .println();
        }
    }

    public void showLog(NCommandLine commandLine) {
        NSession session = getContext().getSession();
        LocalTomcatServiceBase s = nextLocalTomcatServiceBase(commandLine, NOpenMode.OPEN_OR_ERROR);
        boolean path = false;
        int count = -1;
        NArg a;
        commandLine.setCommandName("tomcat --local log");
        while (commandLine.hasNext()) {
            if ((a = commandLine.nextString("--path").orNull()) != null) {
                path = true;
            } else if (commandLine.isNextOption() && TomcatUtils.isPositiveInt(commandLine.peek()
                    .get(session)
                    .asString().get(session).substring(1))) {
                count = Integer.parseInt(commandLine.next().flatMap(NLiteral::asString).get(session).substring(1));
            } else {
                context.configureLast(commandLine);
            }
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        if (path) {
            getContext().getSession().out().println(c.getOutLogFile());
        } else {
            c.showOutLog(count);
        }
    }

    public void deployFile(NCommandLine args) {
        NSession session = getContext().getSession();
        String instance = null;
        String version = null;
        String file = null;
        String app = null;
        String domain = null;
        String contextName = null;
        NArg a;
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
                    args.setCommandName("tomcat --local deploy-file").throwUnexpectedArgument();
                }
            } else {
                context.configureLast(args);
            }
        }
        if (file == null) {
            throw new NExecutionException(context.getSession(), NMsg.ofPlain("tomcat deploy: Missing File"), 2);
        }
        LocalTomcatConfigService c = openTomcatConfig(instance, NOpenMode.OPEN_OR_ERROR);
        c.deployFile(NPath.of(file, getContext().getSession()), contextName, domain);
    }

    public void deployApp(NCommandLine args) {
        NSession session = getContext().getSession();
        String version = null;
        String app = null;
        NArg a;
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
                    args.setCommandName("tomcat --local deploy").throwUnexpectedArgument();
                }
            } else {
                context.configureLast(args);
            }
        }
        loadApp(app, NOpenMode.OPEN_OR_ERROR).deploy(version);
    }

    public void restart(NCommandLine args, boolean shutdown) {
        NSession session = getContext().getSession();
        boolean deleteLog = false;
        String instance = null;
        LocalTomcatConfigService[] srvRef = new LocalTomcatConfigService[1];

        List<String> apps = new ArrayList<>();
        List<Runnable> runnables = new ArrayList<>();
        args.setCommandName("tomcat restart");
        while (args.hasNext()) {
            NArg a = null;
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
                    args.setCommandName("tomcat --local restart").throwUnexpectedArgument();
                }
            } else {
                context.configureLast(args);
            }
        }
        if (instance == null) {
            instance = "";
        }
        LocalTomcatConfigService c = openTomcatConfig(instance, NOpenMode.OPEN_OR_CREATE);
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
                sharedConfigFolder.stream().filter(
                                pathname -> pathname.isRegularFile() && pathname.getName().toString().endsWith(LocalTomcatConfigService.LOCAL_CONFIG_EXT),
                                "isRegularFile() && matches(*" + LocalTomcatConfigService.LOCAL_CONFIG_EXT + ")"
                        )
                        .mapUnsafe(
                                NUnsafeFunction.of(x -> openTomcatConfig(x, NOpenMode.OPEN_OR_ERROR), "openTomcatConfig")
                                , null)
                        .filterNonNull()
                        .toArray(LocalTomcatConfigService[]::new);
    }

    public LocalTomcatConfigService openTomcatConfig(String name, NOpenMode autoCreate) {
        LocalTomcatConfigService t = new LocalTomcatConfigService(name, this);
        if (autoCreate == null) {
            if (!t.existsConfig()) {
                return null;
            } else {
                autoCreate = NOpenMode.OPEN_OR_ERROR;
            }
        }
        t.open(autoCreate);
        return t;
    }

    public LocalTomcatConfigService openTomcatConfig(NPath file, NOpenMode autoCreate) {
        LocalTomcatConfigService t = new LocalTomcatConfigService(file, this);
        if (autoCreate == null) {
            if (!t.existsConfig()) {
                return null;
            } else {
                autoCreate = NOpenMode.OPEN_OR_ERROR;
            }
        }
        t.open(autoCreate);
        return t;
    }

    public LocalTomcatServiceBase loadServiceBase(String name, NOpenMode autoCreate) {
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
                throw new NExecutionException(context.getSession(), NMsg.ofC("ambiguous name %s. Could be either domain or app", name), 3);
            }
            if (d == null && a == null) {
                throw new NExecutionException(context.getSession(), NMsg.ofC("unknown name %s. it is no domain nor app", name), 3);
            }
            if (d != null) {
                return d;
            }
            return a;
        }
    }

    public LocalTomcatAppConfigService loadApp(String name, NOpenMode autoCreate) {
        String[] strings = TomcatUtils.splitInstanceAppPreferApp(name);
        return openTomcatConfig(strings[0], autoCreate).getApp(strings[1], NOpenMode.OPEN_OR_ERROR);
    }

    public LocalTomcatDomainConfigService loadDomain(String name, NOpenMode autoCreate) {
        String[] strings = TomcatUtils.splitInstanceAppPreferApp(name);
        return openTomcatConfig(strings[0], autoCreate).getDomain(strings[1], NOpenMode.OPEN_OR_ERROR);
    }

    public NApplicationContext getContext() {
        return context;
    }

    public void setContext(NApplicationContext context) {
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

    public LocalTomcatServiceBase nextLocalTomcatServiceBase(NCommandLine args, NOpenMode autoCreate) {
        NSession session = getContext().getSession();
        if (args.hasNext()) {
            NArg o = args.nextNonOption().orNull();
            if (o != null) {
                return loadServiceBase(o.toString(), autoCreate);
            }
        }
        return loadServiceBase("", autoCreate);
    }

    public LocalTomcatConfigService nextLocalTomcatConfigService(NCommandLine args, NOpenMode autoCreate) {
        NSession session = getContext().getSession();
        if (args.hasNext()) {
            NArg o = args.nextNonOption().orNull();
            if (o != null) {
                return openTomcatConfig(o.toString(), autoCreate);
            }
        }
        return openTomcatConfig("", autoCreate);
    }

    public LocalTomcatDomainConfigService nextLocalTomcatDomainConfigService(NCommandLine args, NOpenMode autoCreate) {
        NSession session = getContext().getSession();
        if (args.hasNext()) {
            NArg o = args.nextNonOption().orNull();
            if (o != null) {
                String[] p = TomcatUtils.splitInstanceAppPreferApp(o.toString());
                return openTomcatConfig(p[0], NOpenMode.OPEN_OR_ERROR).getDomain(p[1], autoCreate);
            }
        }
        return openTomcatConfig("", NOpenMode.OPEN_OR_ERROR).getDomain("", autoCreate);
    }

    public LocalTomcatAppConfigService nextLocalTomcatAppConfigService(NCommandLine args, NOpenMode autoCreate) {
        NSession session = getContext().getSession();
        if (args.hasNext()) {
            NArg o = args.nextNonOption().orNull();
            if (o != null) {
                String[] p = TomcatUtils.splitInstanceAppPreferApp(o.toString());
                return openTomcatConfig(p[0], NOpenMode.OPEN_OR_ERROR).getApp(p[1], autoCreate);
            }
        }
        return openTomcatConfig("", NOpenMode.OPEN_OR_ERROR).getApp("", autoCreate);
    }

    public LocalTomcatConfigService readTomcatServiceArg(NCommandLine args, NOpenMode autoCreate) {
        LocalTomcatServiceBase s = readBaseServiceArg(args, autoCreate);
        return toLocalTomcatConfigService(s);
    }

    public LocalTomcatServiceBase readBaseServiceArg(NCommandLine args, NOpenMode autoCreate) {
        NSession session = getContext().getSession();
        NArg a;
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
            return (loadServiceBase(args.next().flatMap(NLiteral::asString).get(session), autoCreate));
        } else {
            return null;
        }
    }
}
