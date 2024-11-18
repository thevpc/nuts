package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NObjectFormat;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.text.NString;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.ntomcat.NTomcatConfigVersions;
import net.thevpc.nuts.toolbox.ntomcat.util.NamedItemNotFoundException;
import net.thevpc.nuts.toolbox.ntomcat.util.RunningTomcat;
import net.thevpc.nuts.toolbox.ntomcat.util.TomcatUtils;
import net.thevpc.nuts.util.*;

import java.util.ArrayList;
import java.util.List;

public class LocalTomcat {

    private NSession session;
    private NCmdLine cmdLine;
    private NPath sharedConfigFolder;

    public LocalTomcat(NSession sessino, NCmdLine cmdLine) {
        this.setSession(sessino);
        this.cmdLine = cmdLine;
        sharedConfigFolder = sessino.getAppVersionFolder(NStoreType.CONF, NTomcatConfigVersions.CURRENT);
    }

    public void runArgs() {
        NArg a;
        cmdLine.setCommandName("tomcat --local");
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                session.configureLast(cmdLine);
            } else {
                a = cmdLine.nextNonOption().get();
                switch (a.asString().get()) {
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
                        throw new NExecutionException(NMsg.ofC("unsupported action %s", a.asString()), NExecutionException.ERROR_1);
                }
            }
        }
        throw new NExecutionException(NMsg.ofPlain("missing tomcat action. Type: nuts tomcat --help"), NExecutionException.ERROR_1);
    }

    public void list(NCmdLine args) {
        NArg a;
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
            if ((a = args.nextFlag("-a", "--apps").orNull()) != null) {
                x.apps = a.getBooleanValue().get();
            } else if ((a = args.nextFlag("-d", "--domains").orNull()) != null) {
                x.domains = a.getBooleanValue().get();
            } else if ((a = args.nextFlag("-i", "--instances").orNull()) != null) {
                x.instances = a.getBooleanValue().get();
            } else {
                session.configureLast(cmdLine);
            }
        }
        if (x.instances) {
            for (LocalTomcatConfigService tomcatConfig : listConfig()) {
                getSession().out().println(tomcatConfig.getName());
            }
        } else {
            LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NOpenMode.OPEN_OR_ERROR);
            x.print(s);
        }
    }

    public void ps(NCmdLine args) {
        NRef<String> format = NRef.of("default");
        args.setCommandName("tomcat --local show");
        while (args.hasNext()) {
            if (args.withNextTrueFlag((b, a) -> format.set("long"), "-l", "--long")) {
            } else {
                session.configureLast(cmdLine);
            }
        }
        if (args.isExecMode()) {
            NTexts factory = NTexts.of();
            if (session.isPlainOut()) {
                NPrintStream out = session.out();
                for (RunningTomcat jpsResult : TomcatUtils.getRunningInstances(session)) {
                    switch (format.get()) {
                        case "long": {
                            NCmdLine jcmd = NCmdLine.parse(jpsResult.getArgsLine()).orNull();
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
                                    jcmd == null ? jpsResult.getArgsLine() : jcmd
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
                NObjectFormat.of()
                        .setValue(TomcatUtils.getRunningInstances(session))
                        .println();
            }
        }
    }

    public void describe(NCmdLine args) {
        NArg a;
        LocalTomcatServiceBase s;
        List<LocalTomcatServiceBase> toShow = new ArrayList<>();
        args.setCommandName("tomcat --local show");
        while (args.hasNext()) {
            if ((s = readBaseServiceArg(args, NOpenMode.OPEN_OR_ERROR)) != null) {
                toShow.add(s);
            } else {
                session.configureLast(cmdLine);
            }
        }
        if (args.isExecMode()) {
            if (toShow.isEmpty()) {
                toShow.add(loadServiceBase("", NOpenMode.OPEN_OR_ERROR));
            }
            for (LocalTomcatServiceBase s2 : toShow) {
                s2.println(getSession().out());
            }
        }
    }

    public void add(NCmdLine args, NOpenMode autoCreate) {
        args.setCommandName("tomcat --local add");
        NSession session = getSession();
        NArg a = args.nextNonOption().get();
        if (a != null) {
            switch (a.asString().get()) {
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

    public void addInstance(LocalTomcatConfigService c, NCmdLine args, NOpenMode autoCreate) {
        NArg a;
        NSession session = getSession();
        args.setCommandName("tomcat --local add");
        while (args.hasNext()) {
            if ((a = args.nextEntry("--catalina-version", "--tomcat-version", "--version").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", autoCreate);
                }
                c.getConfig().setCatalinaVersion(a.getStringValue().get());
                c.getConfig().setCatalinaHome(null);
                c.getConfig().setCatalinaBase(null);
            } else if ((a = args.nextEntry("--catalina-base").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", autoCreate);
                }
                c.getConfig().setCatalinaBase(a.getStringValue().get());
            } else if ((a = args.nextEntry("--catalina-home").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", autoCreate);
                }
                c.getConfig().setCatalinaHome(a.getStringValue().get());
                c.getConfig().setCatalinaBase(null);
                c.getConfig().setCatalinaVersion(null);
            } else if ((a = args.nextEntry("--shutdown-wait-time").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", autoCreate);
                }
                c.getConfig().setShutdownWaitTime(a.getValue().asInt().get());
            } else if ((a = args.nextEntry("--archive-folder").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NOpenMode.OPEN_OR_ERROR);
                }
                c.getConfig().setArchiveFolder(a.getStringValue().get());
            } else if ((a = args.nextEntry("--running-folder").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NOpenMode.OPEN_OR_ERROR);
                }
                c.getConfig().setRunningFolder(a.getStringValue().get());
            } else if ((a = args.nextEntry("--http-port").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NOpenMode.OPEN_OR_ERROR);
                }
                c.setHttpConnectorPort(false, a.getValue().asInt().get());
            } else if ((a = args.nextEntry("--port").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NOpenMode.OPEN_OR_ERROR);
                }
                c.setHttpConnectorPort(false, a.getValue().asInt().get());
            } else if ((a = args.nextFlag("-d", "--dev").orNull()) != null) {
                if (c == null) {
                    c = openTomcatConfig("", NOpenMode.OPEN_OR_ERROR);
                }
                c.getConfig().setDev(a.getBooleanValue().get());
            } else {
                session.configureLast(args);
            }
        }
        c.save();
        c.buildCatalinaBase();
    }

    public void addDomain(LocalTomcatDomainConfigService c, NCmdLine args, NOpenMode autoCreate) {
        NArg a;
        NSession session = getSession();
//        c = nextLocalTomcatConfigService(args, NutsOpenMode.OPEN_EXISTING);
        boolean changed = false;
        args.setCommandName("tomcat --local add");
        while (args.hasNext()) {
            if ((a = args.nextEntry("--log").orNull()) != null) {
                c.getConfig().setLogFile(a.getStringValue().get());
                changed = true;
            } else {
                session.configureLast(args);
            }
        }
        if (changed) {
            c.getTomcat().save();
        }
    }

    public void addApp(LocalTomcatAppConfigService c, NCmdLine args, NOpenMode autoCreate) {
        NArg a;
        NSession session = getSession();
        boolean changed = false;
        args.setCommandName("tomcat --local add");
        while (args.hasNext()) {
            if ((a = args.nextEntry("--source").orNull()) != null) {
                String value = a.getStringValue().get();
                c.getConfig().setSourceFilePath(value);
                changed = true;
            } else if ((a = args.nextEntry("--deploy").orNull()) != null) {
                String value = a.getStringValue().get();
                c.getConfig().setDeployName(value);
                changed = true;
            } else if ((a = args.nextEntry("--domain").orNull()) != null) {
                String value = a.getStringValue().get();
                //check that domain exists!!
                c.getTomcat().getDomain(value, NOpenMode.OPEN_OR_ERROR);
                c.getConfig().setDomain(value);
                changed = true;
            } else {
                session.configureLast(args);
            }
        }
        if (changed) {
            c.getTomcat().save();
        }
    }

    public void remove(NCmdLine args) {
        NSession session = getSession();
        NArg a = args.nextNonOption().get();
        if (a != null) {
            switch (a.asString().get()) {
                case "instance": {
                    LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NOpenMode.OPEN_OR_ERROR);
                    if (NAsk.of()
                            .forBoolean(NMsg.ofC("Confirm Deleting %s?", s.getName())).setDefaultValue(true).getBooleanValue()) {
                        s.remove();
                    }
                    return;
                }
                case "domain": {
                    LocalTomcatDomainConfigService s = nextLocalTomcatDomainConfigService(args, NOpenMode.OPEN_OR_ERROR);
                    if (NAsk.of()
                            .forBoolean(NMsg.ofC("Confirm Deleting %s?", s.getName())).setDefaultValue(true).getBooleanValue()) {
                        s.remove();
                        s.getTomcat().save();
                    }
                    return;
                }
                case "app": {
                    LocalTomcatAppConfigService s = nextLocalTomcatAppConfigService(args, NOpenMode.OPEN_OR_ERROR);
                    if (NAsk.of()
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

    public void stop(NCmdLine args) {
        NSession session = getSession();
        NArg a;
        LocalTomcatConfigService c = nextLocalTomcatConfigService(args, NOpenMode.OPEN_OR_ERROR);
        args.setCommandName("tomcat --local stop");
        while (args.hasNext()) {
            session.configureLast(args);
        }
        if (!c.stop()) {
            throw new NExecutionException(NMsg.ofPlain("unable to stop"), NExecutionException.ERROR_1);
        }
    }

    public NString getBracketsPrefix(String str) {
        return NTexts.of().ofBuilder()
                .append("[")
                .append(str, NTextStyle.primary5())
                .append("]");
    }


    public void status(NCmdLine args) {
        NSession session = getSession();
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
                        NTexts.of().ofStyled("not found", NTextStyle.error())
                ));
            } else {
                session.eout().add(
                        NElements.of().ofObject()
                                .set("config-name", name)
                                .set("status", "not-found")
                                .build()
                );
            }
        }
    }

    public void installApp(NCmdLine args) {
        NSession session = getSession();
        LocalTomcatAppConfigService app = null;
        String version = null;
        String file = null;
        LocalTomcatConfigService s = null;
        NArg a;
        args.setCommandName("tomcat --local install");
        while (args.hasNext()) {
            if ((a = args.nextEntry("--name").orNull()) != null) {
                s = openTomcatConfig(a.getStringValue().get(), NOpenMode.OPEN_OR_ERROR);
            } else if ((a = args.nextEntry("--app").orNull()) != null) {
                app = loadApp(a.getStringValue().get(), NOpenMode.OPEN_OR_ERROR);
            } else if ((a = args.nextEntry("--version").orNull()) != null) {
                version = a.getStringValue().get();
            } else if ((a = args.nextEntry("--file").orNull()) != null) {
                file = a.getStringValue().get();
            } else if ((a = args.nextNonOption().get()) != null) {
                if (file == null) {
                    file = a.asString().get();
                } else {
                    args.setCommandName("tomcat --local install").throwUnexpectedArgument();
                }
            } else {
                session.configureLast(args);
            }
        }
        if (app == null) {
            throw new NExecutionException(NMsg.ofPlain("tomcat install: Missing Application"), NExecutionException.ERROR_2);
        }
        if (file == null) {
            throw new NExecutionException(NMsg.ofPlain("tomcat install: Missing File"), NExecutionException.ERROR_2);
        }
        app.install(version, file, true);
    }

    public void delete(NCmdLine args) {
        NSession session = getSession();
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

    private void deleteLog(NCmdLine args) {
        NSession session = getSession();
        LocalTomcatServiceBase s = null;
        boolean all = false;
        NArg a;
        boolean processed = false;
        args.setCommandName("tomcat --local delete-log");
        while (args.hasNext()) {
            if ((a = args.nextFlag("-a", "--all").orNull()) != null) {
                all = a.getBooleanValue().get();
            } else if ((s = readBaseServiceArg(args, NOpenMode.OPEN_OR_ERROR)) != null) {
                LocalTomcatConfigService c = toLocalTomcatConfigService(s);
                if (all) {
                    c.deleteAllLog();
                } else {
                    c.deleteOutLog();
                }
                processed = true;
            } else {
                session.configureLast(args);
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

    private void deleteTemp(NCmdLine args) {
        NSession session = getSession();
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
                session.configureLast(args);
            }
        }
        if (!processed) {
            LocalTomcatConfigService c = openTomcatConfig("", NOpenMode.OPEN_OR_ERROR);
            c.deleteTemp();
        }
    }

    private void deleteWork(NCmdLine args) {
        NSession session = getSession();
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
                session.configureLast(args);
            }
        }
        if (!processed) {
            LocalTomcatConfigService c = openTomcatConfig("", NOpenMode.OPEN_OR_ERROR);
            c.deleteWork();
        }
    }

    public void showCatalinaBase(NCmdLine args) {
        NSession session = getSession();
        args.setCommandName("tomcat --local show-catalina-base");
        LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NOpenMode.OPEN_OR_ERROR);
        NArg a;
        while (args.hasNext()) {
            session.configureLast(args);
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        NObjectFormat.of()
                .setValue(c.getCatalinaBase())
                .println();
    }

    public void showCatalinaVersion(NCmdLine args) {
        NSession session = getSession();
        args.setCommandName("tomcat --local show-catalina-version");
        LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NOpenMode.OPEN_OR_ERROR);
        NArg a;
        while (args.hasNext()) {
            session.configureLast(args);
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        NObjectFormat.of()
                .setValue(c.getValidCatalinaVersion())
                .println();
    }

    public void showCatalinaHome(NCmdLine args) {
        NSession session = getSession();
        args.setCommandName("tomcat --local show-catalina-home");
        LocalTomcatConfigService s = nextLocalTomcatConfigService(args, NOpenMode.OPEN_OR_ERROR);
        NArg a;
        while (args.hasNext()) {
            session.configureLast(args);
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        NObjectFormat.of()
                .setValue(c.getCatalinaHome())
                .println();
    }

    public void showPort(NCmdLine args) {
        NSession session = getSession();
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
            if ((a = args.nextFlag("--redirect").orNull()) != null) {
                redirect = a.getBooleanValue().get();
            } else if ((a = args.nextFlag("--shutdown").orNull()) != null) {
                type = "shutdown";
            } else if ((a = args.nextFlag("--ajp").orNull()) != null) {
                type = "ajp";
            } else if ((a = args.nextEntry("--set").orNull()) != null) {
                newValue = a.getValue().asInt().get();
                setValue = true;
            } else if ((a = args.nextEntry("--set-port").orNull()) != null) {
                int port = a.getValue().asInt().get();
                runnables.add(() -> c.setHttpConnectorPort(false, port));
            } else if ((a = args.nextEntry("--set-redirect-port").orNull()) != null) {
                int port = a.getValue().asInt().get();
                runnables.add(() -> c.setHttpConnectorPort(true, port));
            } else if ((a = args.nextEntry("--set-shutdown-port").orNull()) != null) {
                int port = a.getValue().asInt().get();
                runnables.add(() -> c.setShutdownPort(port));
            } else if ((a = args.nextEntry("--set-ajp-port").orNull()) != null) {
                int port = a.getValue().asInt().get();
                runnables.add(() -> c.setAjpConnectorPort(false, port));
            } else if ((a = args.nextEntry("--set-redirect-ajp-port").orNull()) != null) {
                int port = a.getValue().asInt().get();
                runnables.add(() -> c.setAjpConnectorPort(true, port));
            } else {
                session.configureLast(args);
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
            NObjectFormat.of()
                    .setValue(port)
                    .println();
        }
    }

    public void showLog(NCmdLine cmdLine) {
        NSession session = getSession();
        LocalTomcatServiceBase s = nextLocalTomcatServiceBase(cmdLine, NOpenMode.OPEN_OR_ERROR);
        boolean path = false;
        int count = -1;
        NArg a;
        cmdLine.setCommandName("tomcat --local log");
        while (cmdLine.hasNext()) {
            if ((a = cmdLine.nextEntry("--path").orNull()) != null) {
                path = true;
            } else if (cmdLine.isNextOption() && TomcatUtils.isPositiveInt(cmdLine.peek()
                    .get()
                    .asString().get().substring(1))) {
                count = Integer.parseInt(cmdLine.next().flatMap(NLiteral::asString).get().substring(1));
            } else {
                session.configureLast(cmdLine);
            }
        }
        LocalTomcatConfigService c = toLocalTomcatConfigService(s);
        if (path) {
            getSession().out().println(c.getOutLogFile());
        } else {
            c.showOutLog(count);
        }
    }

    public void deployFile(NCmdLine args) {
        NSession session = getSession();
        String instance = null;
        String version = null;
        String file = null;
        String app = null;
        String domain = null;
        String contextName = null;
        NArg a;
        args.setCommandName("tomcat --local deploy-file");
        while (args.hasNext()) {
            if ((a = args.nextEntry("--file").orNull()) != null) {
                file = a.getStringValue().get();
            } else if ((a = args.nextEntry("--name").orNull()) != null) {
                instance = a.getStringValue().get();
            } else if ((a = args.nextEntry("--context").orNull()) != null) {
                contextName = a.getStringValue().get();
            } else if ((a = args.nextEntry("--domain").orNull()) != null) {
                domain = a.getStringValue().get();
            } else if ((a = args.nextNonOption().orNull()) != null) {
                if (file == null) {
                    file = a.asString().get();
                } else {
                    args.setCommandName("tomcat --local deploy-file").throwUnexpectedArgument();
                }
            } else {
                session.configureLast(args);
            }
        }
        if (file == null) {
            throw new NExecutionException(NMsg.ofPlain("tomcat deploy: Missing File"), NExecutionException.ERROR_2);
        }
        LocalTomcatConfigService c = openTomcatConfig(instance, NOpenMode.OPEN_OR_ERROR);
        c.deployFile(NPath.of(file), contextName, domain);
    }

    public void deployApp(NCmdLine args) {
        NSession session = getSession();
        String version = null;
        String app = null;
        NArg a;
        args.setCommandName("tomcat --local deploy");
        while (args.hasNext()) {
            if ((a = args.nextEntry("--version").orNull()) != null) {
                version = a.getStringValue().get();
            } else if ((a = args.nextEntry("--app").orNull()) != null) {
                app = a.getStringValue().get();
            } else if ((a = args.nextNonOption().orNull()) != null) {
                if (app == null) {
                    app = a.asString().get();
                } else {
                    args.setCommandName("tomcat --local deploy").throwUnexpectedArgument();
                }
            } else {
                session.configureLast(args);
            }
        }
        loadApp(app, NOpenMode.OPEN_OR_ERROR).deploy(version);
    }

    public void restart(NCmdLine args, boolean shutdown) {
        NSession session = getSession();
        boolean deleteLog = false;
        String instance = null;
        LocalTomcatConfigService[] srvRef = new LocalTomcatConfigService[1];

        List<String> apps = new ArrayList<>();
        List<Runnable> runnables = new ArrayList<>();
        args.setCommandName("tomcat restart");
        while (args.hasNext()) {
            NArg a = null;
            if ((a = args.nextFlag("--delete-out-log").orNull()) != null) {
                deleteLog = a.getBooleanValue().get();
            } else if ((a = args.nextEntry("--deploy").orNull()) != null) {
                apps.add(a.getStringValue().get());
            } else if ((a = args.nextEntry("--port").orNull()) != null) {
                int port = a.getValue().asInt().get();
                runnables.add(() -> srvRef[0].setHttpConnectorPort(false, port));
            } else if ((a = args.nextEntry("--http-port").orNull()) != null) {
                int port = a.getValue().asInt().get();
                runnables.add(() -> srvRef[0].setHttpConnectorPort(false, port));
            } else if ((a = args.nextEntry("--redirect-port").orNull()) != null) {
                int port = a.getValue().asInt().get();
                runnables.add(() -> srvRef[0].setHttpConnectorPort(true, port));
            } else if ((a = args.nextEntry("--shutdown-port").orNull()) != null) {
                int port = a.getValue().asInt().get();
                runnables.add(() -> srvRef[0].setShutdownPort(port));
            } else if ((a = args.nextEntry("--ajp-port").orNull()) != null) {
                int port = a.getValue().asInt().get();
                runnables.add(() -> srvRef[0].setAjpConnectorPort(false, port));
            } else if ((a = args.nextEntry("--redirect-ajp-port").orNull()) != null) {
                int port = a.getValue().asInt().get();
                runnables.add(() -> srvRef[0].setAjpConnectorPort(true, port));
            } else if ((a = args.nextNonOption().orNull()) != null) {
                if (instance == null) {
                    instance = a.asString().get();
                } else {
                    args.setCommandName("tomcat --local restart").throwUnexpectedArgument();
                }
            } else {
                session.configureLast(args);
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
                                NPredicate.of((NPath pathname) -> pathname.isRegularFile() && pathname.getName().toString().endsWith(LocalTomcatConfigService.LOCAL_CONFIG_EXT))
                                        .withDesc(NEDesc.of("isRegularFile() && matches(*" + LocalTomcatConfigService.LOCAL_CONFIG_EXT + ")"))
                        )
                        .mapUnsafe(
                                NUnsafeFunction.of((NPath x) -> openTomcatConfig(x, NOpenMode.OPEN_OR_ERROR)).withDesc(NEDesc.of("openTomcatConfig"))
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
                throw new NExecutionException(NMsg.ofC("ambiguous name %s. Could be either domain or app", name), NExecutionException.ERROR_3);
            }
            if (d == null && a == null) {
                throw new NExecutionException(NMsg.ofC("unknown name %s. it is no domain nor app", name), NExecutionException.ERROR_3);
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

    public NSession getSession() {
        return session;
    }

    public void setSession(NSession session) {
        this.session = session;
    }

    public LocalTomcatConfigService toLocalTomcatConfigService(LocalTomcatServiceBase s) {
        if (s instanceof LocalTomcatAppConfigService) {
            s = ((LocalTomcatAppConfigService) s).getTomcat();
        } else if (s instanceof LocalTomcatDomainConfigService) {
            s = ((LocalTomcatDomainConfigService) s).getTomcat();
        }
        return ((LocalTomcatConfigService) s);
    }

    public LocalTomcatServiceBase nextLocalTomcatServiceBase(NCmdLine args, NOpenMode autoCreate) {
        if (args.hasNext()) {
            NArg o = args.nextNonOption().orNull();
            if (o != null) {
                return loadServiceBase(o.toString(), autoCreate);
            }
        }
        return loadServiceBase("", autoCreate);
    }

    public LocalTomcatConfigService nextLocalTomcatConfigService(NCmdLine args, NOpenMode autoCreate) {
        NSession session = getSession();
        if (args.hasNext()) {
            NArg o = args.nextNonOption().orNull();
            if (o != null) {
                return openTomcatConfig(o.toString(), autoCreate);
            }
        }
        return openTomcatConfig("", autoCreate);
    }

    public LocalTomcatDomainConfigService nextLocalTomcatDomainConfigService(NCmdLine args, NOpenMode autoCreate) {
        NSession session = getSession();
        if (args.hasNext()) {
            NArg o = args.nextNonOption().orNull();
            if (o != null) {
                String[] p = TomcatUtils.splitInstanceAppPreferApp(o.toString());
                return openTomcatConfig(p[0], NOpenMode.OPEN_OR_ERROR).getDomain(p[1], autoCreate);
            }
        }
        return openTomcatConfig("", NOpenMode.OPEN_OR_ERROR).getDomain("", autoCreate);
    }

    public LocalTomcatAppConfigService nextLocalTomcatAppConfigService(NCmdLine args, NOpenMode autoCreate) {
        NSession session = getSession();
        if (args.hasNext()) {
            NArg o = args.nextNonOption().orNull();
            if (o != null) {
                String[] p = TomcatUtils.splitInstanceAppPreferApp(o.toString());
                return openTomcatConfig(p[0], NOpenMode.OPEN_OR_ERROR).getApp(p[1], autoCreate);
            }
        }
        return openTomcatConfig("", NOpenMode.OPEN_OR_ERROR).getApp("", autoCreate);
    }

    public LocalTomcatConfigService readTomcatServiceArg(NCmdLine args, NOpenMode autoCreate) {
        LocalTomcatServiceBase s = readBaseServiceArg(args, autoCreate);
        return toLocalTomcatConfigService(s);
    }

    public LocalTomcatServiceBase readBaseServiceArg(NCmdLine args, NOpenMode autoCreate) {
        NSession session = getSession();
        NArg a;
        if ((a = args.nextEntry("--name").orNull()) != null) {
            return (loadServiceBase(a.getStringValue().get(), autoCreate));
        } else if ((a = args.nextEntry("--app").orNull()) != null) {
            return (loadApp(a.getStringValue().get(), autoCreate));
        } else if ((a = args.nextEntry("--domain").orNull()) != null) {
            return (loadDomain(a.getStringValue().get(), autoCreate));
            //TODO: should remove this line?
        } else if (args.hasNext() && args.isNextOption() && args.peek().get().isDouble()) {
            return null;
        } else if (args.hasNext() && args.isNextOption()) {
            return null;
        } else if (args.hasNext()) {
            return (loadServiceBase(args.next().flatMap(NLiteral::asString).get(), autoCreate));
        } else {
            return null;
        }
    }
}
