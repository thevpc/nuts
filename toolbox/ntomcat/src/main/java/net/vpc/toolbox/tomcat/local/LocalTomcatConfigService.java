package net.vpc.toolbox.tomcat.local;

import net.vpc.app.nuts.*;
import net.vpc.common.io.*;
import net.vpc.common.io.PosApis;
import net.vpc.toolbox.tomcat.local.config.LocalTomcatAppConfig;
import net.vpc.toolbox.tomcat.local.config.LocalTomcatConfig;
import net.vpc.toolbox.tomcat.local.config.LocalTomcatDomainConfig;
import net.vpc.toolbox.tomcat.util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class LocalTomcatConfigService extends LocalTomcatServiceBase {

    public static final String LOCAL_CONFIG_EXT = ".local-config";
    private String name;
    private LocalTomcat app;
    private LocalTomcatConfig config;
    private NutsApplicationContext context;
    private NutsDefinition catalinaNutsDefinition;
    private String catalinaVersion;

    public LocalTomcatConfigService(Path file, LocalTomcat app) {
        this(
                file.getFileName().toString().substring(0, file.getFileName().toString().length() - LocalTomcatConfigService.LOCAL_CONFIG_EXT.length()),
                app
        );
        loadConfig();
    }

    public LocalTomcatConfigService(String name, LocalTomcat app) {
        this.app = app;
        setName(name);
        this.context = app.getContext();
    }

    public LocalTomcatConfigService setName(String name) {
        if (".".equals(name)) {
            name = "default";
        }
        this.name = TomcatUtils.toValidFileName(name, "default");
        return this;
    }

    public void open(NutsWorkspaceOpenMode autoCreate) {
        switch (autoCreate) {
            case OPEN_OR_CREATE: {
                if (this.existsConfig()) {
                    this.loadConfig();
                } else {
                    this.setConfig(new LocalTomcatConfig());
                    this.save();
                }
                break;
            }
            case OPEN_EXISTING: {
                if (!this.existsConfig()) {
                    throw new NamedItemNotFoundException("Instance not found : " + this.getName(), this.getName());
                }
                this.loadConfig();
                break;
            }
            case CREATE_NEW: {
                if (this.existsConfig()) {
                    throw new NamedItemNotFoundException("Instance already exists : " + this.getName(), this.getName());
                }
                this.setConfig(new LocalTomcatConfig());
                this.save();
                break;
            }
        }
    }

    public String getName() {
        return name;
    }

    public LocalTomcatConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    public LocalTomcatConfigService save() {
        String v = getConfig().getCatalinaVersion();
        if (v == null) {
            v = getValidCatalinaVersion();
            if (v != null) {
                getConfig().setCatalinaVersion(v);
            }
        }
        Path f = getConfigPath();
        context.getWorkspace().formats().json().value(config).print(f);
        return this;
    }

    public Path getConfigPath() {
        return context.getSharedConfigFolder().resolve(getName() + LOCAL_CONFIG_EXT);
    }

    public boolean existsConfig() {
        return (Files.exists(getConfigPath()));
    }

    public String getRequestedCatalinaVersion() {
        LocalTomcatConfig c = getConfig();
        return c.getCatalinaVersion();
    }

    public String getValidCatalinaVersion() {
        String v = getConfig().getCatalinaVersion();
        if (!TomcatUtils.isBlank(v)) {
            return v;
        }
        v = TomcatUtils.getFolderCatalinaHomeVersion(getCatalinaHome());
        if (v != null) {
            return v;
        }
        //last case
        NutsDefinition nf = getCatalinaNutsDefinition();
        return nf.getId().getVersion().toString();
    }

    public Path getCatalinaBase() {
        LocalTomcatConfig c = getConfig();
        Path catalinaBase = c.getCatalinaBase() == null ? null : Paths.get(c.getCatalinaBase());
        Path catalinaHome = getCatalinaHome();
        if (TomcatUtils.isBlank(getConfig().getCatalinaHome())
                && catalinaBase == null) {
            catalinaBase = Paths.get(getName());
        }
        if (catalinaBase == null) {
            String v = getValidCatalinaVersion();
            int x1 = v.indexOf('.');
            int x2 = x1 < 0 ? -1 : v.indexOf('.', x1 + 1);
            if (x2 > 0) {
                v = v.substring(0, x2);
            }
            catalinaBase = context.getVarFolder().resolve("catalina-base-" + v).resolve("default");
        } else {
            if (!catalinaBase.isAbsolute()) {
                String v = getValidCatalinaVersion();
                int x1 = v.indexOf('.');
                int x2 = x1 < 0 ? -1 : v.indexOf('.', x1 + 1);
                if (x2 > 0) {
                    v = v.substring(0, x2);
                }
                catalinaBase = context.getVarFolder().resolve("catalina-base-" + v).resolve(catalinaBase);
            }
        }
        return catalinaBase;
    }

    public Path resolveCatalinaHome() {
        NutsDefinition f = getCatalinaNutsDefinition();
        Path u = f.getInstallInformation().getInstallFolder();
        Path[] paths;
        try {
            paths = Files.list(u).filter(x -> Files.isDirectory(x)).toArray(Path[]::new);
            if (paths.length == 1 && paths[0].getFileName().toString().toLowerCase().startsWith("apache-tomcat")) {
                return paths[0];
            }
        } catch (Exception e) {
            //
        }
        return u;
    }

    public Path getCatalinaHome() {
        String h = getConfig().getCatalinaHome();
        if (TomcatUtils.isBlank(h)) {
            Path h2 = resolveCatalinaHome();
            getConfig().setCatalinaHome(h2.toString());
            save();
            return h2;
        }
        return Paths.get(h);
    }

    public void printStatus() {
        if (context.getSession().isPlainOut()) {
            switch (getStatus()) {
                case RUNNING: {
                    context.getSession().out().printf("==[%s]== Tomcat {{Running}}.\n", getName());
                    break;
                }
                case STOPPED: {
                    context.getSession().out().printf("==[%s]== Tomcat @@Stopped@@.\n", getName());
                    break;
                }
                case OUT_OF_MEMORY: {
                    context.getSession().out().printf("==[%s]== Tomcat [[OutOfMemory]].\n", getName());
                    break;
                }
            }
        } else {
            HashMap<String, String> r = new HashMap<>();
            r.put("name", getName());
            switch (getStatus()) {
                case RUNNING: {
                    r.put("status", "running");
                    break;
                }
                case STOPPED: {
                    r.put("status", "stopped");
                    break;
                }
                case OUT_OF_MEMORY: {
                    r.put("status", "out-of-memory");
                    break;
                }
            }
            object().value(r).println();
        }
    }

    public NutsObjectFormat object() {
        return context.getWorkspace().formats().object().setSession(context.getSession());
    }

    public String[] parseApps(String[] args) {
        List<String> apps = new ArrayList<>();
        if (args != null) {
            for (String arg : args) {
                if (!TomcatUtils.isBlank(arg)) {
                    for (String s : arg.split("[, ]")) {
                        if (!s.isEmpty()) {
                            apps.add(s);
                        }
                    }
                }
            }
        }
        return apps.toArray(new String[0]);
    }

    public boolean start() {
        return start(null, false);
    }

    public boolean buildCatalinaBase() {
        Path catalinaHome = getCatalinaHome();
        Path catalinaBase = getCatalinaBase();
        boolean catalinaBaseUpdated = false;
        catalinaBaseUpdated |= mkdirs(catalinaBase);
        String ext = context.getWorkspace().env().getOsFamily() == NutsOsFamily.WINDOWS ? "bat" : "sh";
        catalinaBaseUpdated |= checkExec(catalinaHome.resolve("bin").resolve("catalina." + ext));
        LocalTomcatConfig c = getConfig();
        catalinaBaseUpdated |= mkdirs(catalinaBase.resolve("logs"));
        catalinaBaseUpdated |= mkdirs(catalinaBase.resolve("logs"));
        catalinaBaseUpdated |= mkdirs(catalinaBase.resolve("temp"));
        catalinaBaseUpdated |= mkdirs(catalinaBase.resolve("conf"));
        if (Files.isDirectory(catalinaHome.resolve("conf"))) {
            try (DirectoryStream<Path> ss = Files.newDirectoryStream(catalinaHome.resolve("conf"))) {
                for (Path conf : ss) {
                    Path confFile = catalinaBase.resolve("conf/" + conf.getFileName());
                    if (!Files.exists(confFile)) {
                        catalinaBaseUpdated = true;
                        try {
                            Files.copy(conf, confFile);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        if (c.isDev()) {
            if (Files.isDirectory(catalinaHome.resolve("webapps").resolve("host-manager"))) {
                catalinaBaseUpdated |= TomcatUtils.copyDir(
                        catalinaHome.resolve("webapps").resolve("host-manager"),
                        catalinaBase.resolve("webapps").resolve("host-manager"),
                        false
                );
            }
            if (Files.isDirectory(catalinaHome.resolve("webapps").resolve("manager"))) {
                catalinaBaseUpdated |= TomcatUtils.copyDir(
                        catalinaHome.resolve("webapps").resolve("manager"),
                        catalinaBase.resolve("webapps").resolve("manager"),
                        false
                );
            }
        } else {
            catalinaBaseUpdated |= TomcatUtils.deleteDir(catalinaBase.resolve("webapps").resolve("host-manager"));
            catalinaBaseUpdated |= TomcatUtils.deleteDir(catalinaBase.resolve("webapps").resolve("manager"));
        }
        if (catalinaBaseUpdated) {
            if (context.getSession().isPlainOut()) {
                context.getSession().out().printf("==[%s]== updated catalina base ==%s==\n", getName(), catalinaBase);
            }
            return true;
        }
        return false;
    }

    public NutsExecCommand invokeCatalina(String catalinaCommand) {
        buildCatalinaBase();
        Path catalinaHome = getCatalinaHome();
        Path catalinaBase = getCatalinaBase();
        String ext = context.getWorkspace().env().getOsFamily() == NutsOsFamily.WINDOWS ? "bat" : "sh";

        //b.
//        b.setOutput(context.getSession().out());
//        b.setErr(context.getSession().err());
        NutsExecCommand b = context.getWorkspace().exec()
                .setExecutionType(NutsExecutionType.USER_CMD).setSession(context.getSession());
        b.addCommand(catalinaHome + "/bin/catalina." + ext);
        b.addCommand(catalinaCommand);
//        if (catalinaHome != null) {
//            b.addCommand("-Dcatalina.home=" + catalinaHome);
//        }
//        b.addCommand("-Dcatalina.base=" + catalinaBase);
        b.setDirectory(catalinaBase.toString());
        LocalTomcatConfig c = getConfig();
        String javaHome = c.getJavaHome();
        if (javaHome == null) {
            javaHome = System.getProperty("java.home");
        }
        b.setEnv("JAVA_HOME", javaHome);
        b.setEnv("JRE_HOME", javaHome);
        StringBuilder javaOptions = new StringBuilder();
        javaOptions.append("-Dnuts-config-name=").append((getName() == null ? "" : getName()));
        if (getConfig().getJavaOptions() != null) {
            javaOptions.append(" ").append(getConfig().getJavaOptions());
        }
        b.setEnv("JAVA_OPTS", javaOptions.toString());

        b.setEnv("CATALINA_HOME", catalinaHome.toString());
        b.setEnv("CATALINA_BASE", catalinaBase.toString());
        b.setEnv("CATALINA_OUT", catalinaBase.resolve("logs").resolve("catalina.out").toString());
        b.setEnv("CATALINA_TMPDIR", catalinaBase.resolve("temp").toString());

        return b;
    }

    private boolean mkdirs(Path catalinaBase) {
        if (!Files.isDirectory(catalinaBase.resolve("logs"))) {
            try {
                Files.createDirectories(catalinaBase.resolve("logs"));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return true;
        }
        return false;
    }

    public boolean start(String[] deployApps, boolean deleteLog) {
        LocalTomcatConfig c = getConfig();
        RunningTomcat jpsResult = getRunningTomcat();
        if (jpsResult != null) {
            if (context.getSession().isPlainOut()) {
                context.getSession().out().printf("==[%s]== Tomcat already started on port " + getHttpConnectorPort() + ".\n", getName());
            }
            return false;
        }
        for (String app : new HashSet<String>(Arrays.asList(parseApps(deployApps)))) {
            getApp(app, NutsWorkspaceOpenMode.OPEN_EXISTING).deploy(null);
        }
        if (deleteLog) {
            deleteOutLog();
        }

        NutsExecCommand b = invokeCatalina("start");
        if (context.getSession().isPlainOut()) {
            context.getSession().out().printf("==[%s]== starting Tomcat on port " + getHttpConnectorPort() + ". CMD=%s.\n", getName(), b.toString());
        }
        b.getResult();
//        try {
//            b.waitFor();
//        } catch (IOException ex) {
//            throw new UncheckedIOException(ex);
//        }
        waitForRunningStatus(null, null, c.getStartupWaitTime());
        return true;
    }

    private NutsDefinition getCatalinaNutsDefinition() {
        String catalinaVersion = getRequestedCatalinaVersion();
        if (catalinaVersion == null) {
            catalinaVersion = "";
        }
        catalinaVersion = catalinaVersion.trim();
        if (catalinaVersion.isEmpty()) {
            NutsVersion javaVersion = context.getWorkspace().env().getPlatform().getVersion();
            //  http://tomcat.apache.org/whichversion.html
            if (javaVersion.compareTo("1.8") >= 0) {
                catalinaVersion = "[9,[";
            } else if (javaVersion.compareTo("1.7") >= 0) {
                catalinaVersion = "[8.5,9[";
            } else if (javaVersion.compareTo("1.6") >= 0) {
                catalinaVersion = "[7,8[";
            } else if (javaVersion.compareTo("1.5") < 0) {
                catalinaVersion = "[6,7[";
            } else if (javaVersion.compareTo("1.4") < 0) {
                catalinaVersion = "[5.5,6[";
            } else if (javaVersion.compareTo("1.3") < 0) {
                catalinaVersion = "[4.1,5[";
            } else {
                catalinaVersion = "[3.3,4[";
            }
        }
        if (catalinaNutsDefinition == null || !Objects.equals(catalinaVersion, this.catalinaVersion)) {
            this.catalinaVersion = catalinaVersion;
            String cv = catalinaVersion;
            if (!cv.startsWith("[") && !cv.startsWith("]")) {
                cv = "[" + catalinaVersion + "," + catalinaVersion + ".99999]";
            }
            NutsSearchCommand searchLatestCommand = context.getWorkspace().search().addId("org.apache.catalina:apache-tomcat#" + cv)
                    .setSession(context.getSession().copy().setSilent()).setLatest(true);
            NutsDefinition r = searchLatestCommand
                    .addInstallStatus(NutsInstallStatus.INSTALLED)
                    .addInstallStatus(NutsInstallStatus.REQUIRED)
                    .getResultDefinitions().first();
            if (r == null) {
                r = searchLatestCommand.addInstallStatus(NutsInstallStatus.NOT_INSTALLED).setOffline().getResultDefinitions().first();
            }
            if (r == null) {
                r = searchLatestCommand.addInstallStatus(NutsInstallStatus.NOT_INSTALLED).setOnline().getResultDefinitions().required();
            }
            if (r.getInstallInformation().isInstalledOrRequired()) {
                return r;
            } else {
                catalinaNutsDefinition = context.getWorkspace()
                        .install()
                        .id(r.getId())
                        .setSession(context.getSession().copy().setTrace(true).addListener(new NutsInstallListener() {
                            @Override
                            public void onInstall(NutsInstallEvent event) {
                                if (context.getSession().isPlainOut()) {
                                    context.getSession().out().printf("==[%s]== Tomcat installed to catalina home ==%s==\n", getName(), event.getDefinition().getInstallInformation().getInstallFolder());
                                }
                            }
                        })).getResult().required();
            }
        }
        return catalinaNutsDefinition;
    }

    public void deployFile(Path file, String contextName, String domain) {
        String fileName = file.getFileName().toString();
        if (fileName.endsWith(".war")) {
            if (TomcatUtils.isBlank(contextName)) {
                contextName = fileName.substring(0, fileName.length() - ".war".length());
            }
            Path c = getDefaulDeployFolder(domain).resolve(contextName + ".war");
            if (context.getSession().isPlainOut()) {
                context.getSession().out().printf("==[%s]== deploy file file [[%s]] to [[%s]].\n", getName(), file, c);
            }
            try {
                Files.copy(file, c);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } else {
            throw new RuntimeException("Expected war file");
        }
    }

    public boolean stop() {
        if (getRunningTomcat() == null) {
            if (context.getSession().isPlainOut()) {
                context.getSession().out().printf("==[%s]== Tomcat already stopped.\n", getName());
            }
            return false;
        }
        LocalTomcatConfig c = getConfig();
        NutsExecCommand b = invokeCatalina("stop");
        if (context.getSession().isPlainOut()) {
            context.getSession().out().printf("==[%s]== stopping Tomcat. CMD=%s.\n", getName(), b.toString());
        }
        b.getResult();
        return waitForStoppedStatus(c.getShutdownWaitTime(), c.isKill());
    }

    public RunningTomcat getRunningTomcat() {
        Path catalinaBase = getCatalinaBase();
        return Arrays.stream(TomcatUtils.getRunningInstances(context))
                .filter(p -> (catalinaBase == null
                || catalinaBase.toString().equals(p.getBase())))
                .findFirst().orElse(null);
    }

    private boolean checkExec(Path pathname) {
        File file = pathname.toFile();
        if (!file.canExecute()) {
            if (!file.setExecutable(true)) {
                //context.out.println(file.canExecute());
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean restart() {
        return restart(null, false);
    }

    public boolean restart(String[] deployApps, boolean deleteLog) {
        stop();
        if (getRunningTomcat() != null) {
            throw new NutsExecutionException(context.getWorkspace(), "server " + getName() + " is running. It cannot be stopped!", 2);
        }
        start(deployApps, deleteLog);
        return true;
    }

    public AppStatus waitForRunningStatus(String domain, String app, int timeout) {

        AppStatus y = getStatus(domain, app);
        if (y == AppStatus.RUNNING) {
            context.getSession().out().printf("==[%s]== Tomcat started on port " + getHttpConnectorPort() + ".\n", getName());
            return y;
        }
        if (timeout <= 0) {
            RunningTomcat ps = getRunningTomcat();
            if (ps != null) {
                context.getSession().out().printf("==[%s]== Tomcat started on port" + getHttpConnectorPort() + " .\n", getName());
                return AppStatus.RUNNING;
            }
            throw new NutsExecutionException(context.getWorkspace(), "Unable to start tomcat", 2);
        }
        for (int i = 0; i < timeout; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
            y = getStatus(domain, app);
            if (y == AppStatus.RUNNING) {
                context.getSession().out().printf("==[%s]== Tomcat started on port " + getHttpConnectorPort() + ".\n", getName());
                return y;
            }
        }
        if (y == AppStatus.OUT_OF_MEMORY) {
            context.getSession().out().printf("==[%s]== Tomcat out of memory.\n", getName());
            return y;
        }
        throw new NutsExecutionException(context.getWorkspace(), "Unable to start tomcat", 2);
    }

    public boolean waitForStoppedStatus(int timeout, boolean kill) {

        RunningTomcat ps = getRunningTomcat();
        if (ps == null) {
            context.getSession().out().printf("==[%s]== Tomcat stopped.\n", getName());
            return true;
        }
        for (int i = 0; i < timeout; i++) {
            context.getSession().out().printf("==[%s]== waiting Tomcat process to die.\n", getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
            ps = getRunningTomcat();
            if (ps == null) {
                context.getSession().out().printf("==[%s]== Tomcat stopped.\n", getName());
                return true;
            }
        }
        if (kill && PosApis.get().isSupportedKillProcess()) {
            ps = getRunningTomcat();
            if (ps != null) {
                try {
                    if (PosApis.get().killProcess(ps.getPid())) {
                        context.getSession().out().printf("==[%s]== Tomcat process killed (%s).\n", getName(), ps.getPid());
                        return true;
                    } else {
                        context.getSession().out().printf("==[%s]== Tomcat process could not be killed ( %s).\n", getName(), ps.getPid());
                        return false;
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        }
        ps = getRunningTomcat();
        if (ps != null) {
            context.getSession().out().printf("==[%s]== Tomcat process could not be terminated (%s).\n", getName(), ps.getPid());
            return true;
        }
        context.getSession().out().print("==Tomcat stopped==\n");
        return true;
    }

    public AppStatus getStatus() {
        return getStatus(null, null);
    }

    public AppStatus getStatus(String domain, String app) {
        LocalTomcatConfig c = getConfig();
        Path catalinaBase = getCatalinaBase();
        RunningTomcat ps = getRunningTomcat();
        if (ps != null) {
            String startupMessage = null;
            String shutdownMessage = null;
            String logFile = null;
            if (app != null) {
                LocalTomcatAppConfigService a = getApp(app, NutsWorkspaceOpenMode.OPEN_EXISTING);
                domain = a.getConfig().getDomain();
                startupMessage = a.getConfig().getStartupMessage();
                shutdownMessage = a.getConfig().getShutdownMessage();
            }
            if (domain != null) {
                LocalTomcatDomainConfigService tomcatDomain = getDomain(domain, NutsWorkspaceOpenMode.OPEN_EXISTING);
                startupMessage = tomcatDomain.getConfig().getStartupMessage();
                shutdownMessage = tomcatDomain.getConfig().getShutdownMessage();
                logFile = tomcatDomain.getConfig().getLogFile();
            }
            if (startupMessage == null || startupMessage.trim().isEmpty()) {
                startupMessage = c.getStartupMessage();
            }
            if (shutdownMessage == null || shutdownMessage.trim().isEmpty()) {
                shutdownMessage = c.getShutdownMessage();
            }
            if (startupMessage == null || startupMessage.trim().isEmpty()) {
                startupMessage = c.getStartupMessage();
            }
            if (startupMessage == null || startupMessage.trim().isEmpty()) {
                startupMessage = "org.apache.catalina.startup.Catalina.start Server startup";
            }
            if (shutdownMessage == null || shutdownMessage.trim().isEmpty()) {
                shutdownMessage = c.getShutdownMessage();
            }
            if (logFile == null || logFile.isEmpty()) {
                logFile = c.getLogFile();
            }
            if (logFile == null || logFile.isEmpty()) {
                logFile = "logs/catalina.out";
            }

            Path log = catalinaBase.resolve(logFile);
            if (!Files.exists(log)) {
                return AppStatus.STOPPED;
            }
            LineSource lineSource = TextFiles.create(log);
            LocalTomcatLogLineVisitor visitor = new LocalTomcatLogLineVisitor(startupMessage, shutdownMessage);
            try {
                TextFiles.visit(lineSource, visitor);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            if (visitor.outOfMemoryError) {
                return AppStatus.OUT_OF_MEMORY;
            } else if (visitor.started != null && visitor.started) {
                return AppStatus.RUNNING;
            } else {
                return AppStatus.STOPPED;
            }
        }
        return AppStatus.STOPPED;

    }

    public void checkExists() {
        if (!existsConfig()) {
            throw new NamedItemNotFoundException("Instance not found : " + getName(), getName());
        }
    }

    public LocalTomcatConfigService loadConfig() {
        String name = getName();
        Path f = context.getSharedConfigFolder().resolve(name + LOCAL_CONFIG_EXT);
        if (Files.exists(f)) {
            config = context.getWorkspace().formats().json().parse(f, LocalTomcatConfig.class);
            return this;
//        } else if ("default".equals(name)) {
//            //auto create default config
//            config = new LocalTomcatConfig();
//            save();
//            return this;
        }
        throw new NamedItemNotFoundException("Instance not found : " + getName(), getName());
    }

    @Override
    public LocalTomcatConfigService remove() {
        try {
            Files.delete(context.getSharedConfigFolder().resolve(getName() + LOCAL_CONFIG_EXT));
            return this;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public LocalTomcatConfigService print(PrintStream out) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", getName());
        result.put("version", getValidCatalinaVersion());
        result.put("status", getStatus());
        result.put("home", getCatalinaHome());
        result.put("base", getCatalinaBase());
        result.put("out", getOutLogFile());
        result.put("http-port", getHttpConnectorPort());
        result.put("http-redirect-port", getHttpConnectorRedirectPort());
        result.put("ajp-port", getAjpConnectorPort());
        result.put("ajp-redirect-port", getAjpConnectorRedirectPort());
        result.put("shutdown-port", getShutdownPort());
        result.put("config", getConfig());
        context.getWorkspace().formats().object().setSession(context.getSession()).setValue(result).print(out);
        return this;
    }

    public LocalTomcatConfigService setConfig(LocalTomcatConfig config) {
        this.config = config;
        return this;
    }

    public LocalTomcatAppConfigService getApp(String appName, NutsWorkspaceOpenMode mode) {
        appName = TomcatUtils.toValidFileName(appName, "default");
        LocalTomcatAppConfig a = getConfig().getApps().get(appName);
        if (mode == null) {
            if (a == null) {
                return null;
            }
        } else {
            switch (mode) {
                case OPEN_EXISTING: {
                    if (a == null) {
                        throw new NutsExecutionException(context.getWorkspace(), "App not found :" + appName, 2);
                    }
                    break;
                }
                case CREATE_NEW: {
                    if (a == null) {
                        a = new LocalTomcatAppConfig();
                        getConfig().getApps().put(appName, a);
                    } else {
                        throw new NutsExecutionException(context.getWorkspace(), "App already found :" + appName, 2);
                    }
                    break;
                }
                case OPEN_OR_CREATE: {
                    if (a == null) {
                        a = new LocalTomcatAppConfig();
                        getConfig().getApps().put(appName, a);
                    }
                    break;
                }
            }
        }
        return new LocalTomcatAppConfigService(appName, a, this);
    }

    public LocalTomcatDomainConfigService getDomain(String domainName, NutsWorkspaceOpenMode mode) {
        domainName = TomcatUtils.toValidFileName(domainName, "");
        LocalTomcatDomainConfig a = getConfig().getDomains().get(domainName);
        if (mode == null) {
            if (a == null) {
                return null;
            }
        } else {
            switch (mode) {
                case OPEN_EXISTING: {
                    if (a == null) {
                        throw new NutsExecutionException(context.getWorkspace(), "Domain not found :" + domainName, 2);
                    }
                    break;
                }
                case CREATE_NEW: {
                    if (a == null) {
                        a = new LocalTomcatDomainConfig();
                        getConfig().getDomains().put(domainName, a);
                    } else {
                        throw new NutsExecutionException(context.getWorkspace(), "Domain already found :" + domainName, 2);
                    }
                    break;
                }
                case OPEN_OR_CREATE: {
                    if (a == null) {
                        a = new LocalTomcatDomainConfig();
                        getConfig().getDomains().put(domainName, a);
                    }
                    break;
                }
            }
        }
        return new LocalTomcatDomainConfigService(domainName, a, this);
    }

    public Path getLogFolder() {
        return getCatalinaBase().resolve("logs");
    }

    public Path getTempFolder() {
        return getCatalinaBase().resolve("temp");
    }

    public Path getWorkFolder() {
        return getCatalinaBase().resolve("work");
    }

    public void deleteOutLog() {
        //get it from config?
        Path file = getLogFolder().resolve("catalina.out");
        if (Files.isRegularFile(file)) {
            if (context.getSession().isPlainOut()) {
                context.getSession().out().printf("==[%s]== Delete log file %s.\n", getName(), file);
            }
            try {
                Files.delete(file);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    public void deleteTemp() {
        Path tempFolder = getTempFolder();
        if (Files.isDirectory(tempFolder)) {
            try (DirectoryStream<Path> files = Files.newDirectoryStream(tempFolder)) {
                for (Path file : files) {
                    if (context.getSession().isPlainOut()) {
                        context.getSession().out().printf("==[%s]== delete temp file %s.\n", getName(), file);
                    }
                    Files.delete(file);
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    public void deleteWork() {
        Path workFolder = getWorkFolder();
        if (Files.isDirectory(workFolder)) {
            try (DirectoryStream<Path> files = Files.newDirectoryStream(workFolder)) {
                for (Path file : files) {
                    if (context.getSession().isPlainOut()) {
                        context.getSession().out().printf("==[%s]== delete work file %s.\n", getName(), file);
                    }
                    Files.delete(file);

                }

            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    public Path getOutLogFile() {
        return getLogFolder().resolve("catalina.out");
    }

    public void showOutLog(int tail) {
        //get it from config?
        try {
            Path file = getOutLogFile();
            if (tail <= 0) {
                Files.copy(file, context.getSession().out());
                return;
            }
            if (Files.isRegularFile(file)) {
                TextFiles.tail(file.toString(), tail, context.getSession().out());
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void deleteAllLog() {
        Path logFolder = getLogFolder();
        if (Files.isDirectory(logFolder)) {
            try (DirectoryStream<Path> files = Files.newDirectoryStream(logFolder)) {
                for (Path file : files) {
                    if (Files.isRegularFile(file)) {
                        String n = file.getFileName().toString();
                        if (n.endsWith(".out")
                                || n.endsWith(".txt")
                                || n.endsWith(".log")) {
                            //this is a log file, will delete it
                            if (context.getSession().isPlainOut()) {
                                context.getSession().out().printf("==[%s]== delete log file %s.\n", getName(), file);
                            }
                            Files.delete(file);
                        }
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

    }

    public List<LocalTomcatAppConfigService> getApps() {
        List<LocalTomcatAppConfigService> a = new ArrayList<>();
        for (String s : getConfig().getApps().keySet()) {
            a.add(new LocalTomcatAppConfigService(s, getConfig().getApps().get(s), this));
        }
        return a;
    }

    public List<LocalTomcatDomainConfigService> getDomains() {
        List<LocalTomcatDomainConfigService> a = new ArrayList<>();
        for (String s : getConfig().getDomains().keySet()) {
            a.add(new LocalTomcatDomainConfigService(s, getConfig().getDomains().get(s), this));
        }
        return a;
    }

    public Path getDefaulDeployFolder(String domainName) {
        String p = "webapps";
        if (domainName == null) {
            domainName = "";
        }
        if (!domainName.equals("")) {
            p += ("/" + domainName);
        }
        return getCatalinaBase().resolve(p);
    }

    public LocalTomcat getTomcatServer() {
        return app;
    }

    public NutsApplicationContext getContext() {
        return context;
    }

    public Integer getShutdownPort() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Path serverXml = getCatalinaBase().resolve("conf").resolve("server.xml");
            if (Files.exists(serverXml)) {
                Document doc = docBuilder.parse(serverXml.toFile());
                Element root = doc.getDocumentElement();
                String port = root.getAttribute("port");
                return port == null ? null : Integer.parseInt(port);
            }
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            if (context.getSession().isPlainOut()) {
                context.getSession().err().println("@@ERROR:@@ : " + ex);
            }
        }
        //
        return null;
    }

    public Integer getHttpConnectorPort(boolean redirect) {
        return getConnectorPort("HTTP/1.1", redirect);
    }

    public Integer getAjpConnectorPort(boolean redirect) {
        return getConnectorPort("AJP/1.3", redirect);
    }

    public Integer getHttpConnectorPort() {
        return getHttpConnectorPort(false);
    }

    public Integer getHttpConnectorRedirectPort() {
        return getHttpConnectorPort(true);
    }

    public Integer getAjpConnectorPort() {
        return getAjpConnectorPort(false);
    }

    public Integer getAjpConnectorRedirectPort() {
        return getAjpConnectorPort(true);
    }

    public Integer getConnectorPort(String protocol, final boolean redirect) {
        if (protocol == null) {
            protocol = "HTTP/1.1";
        }
        final String _protocol = protocol;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Path serverXml = getCatalinaBase().resolve("conf").resolve("server.xml");
            if (Files.exists(serverXml)) {
                Document doc = docBuilder.parse(serverXml.toFile());
                Element root = doc.getDocumentElement();
                String port = XmlUtils.streamElements(root.getChildNodes())
                        .filter(x -> "Service".equalsIgnoreCase(x.getTagName()))
                        .flatMap(x -> XmlUtils.streamElements(x.getChildNodes()))
                        .filter(x -> "Connector".equalsIgnoreCase(x.getTagName()) && _protocol.equals(x.getAttribute("protocol")))
                        .map(x -> x.getAttribute(redirect ? "redirectPort" : "port"))
                        .distinct().findAny().get();
                return port == null ? null : Integer.parseInt(port);
            }
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            if (context.getSession().isPlainOut()) {
                context.getSession().err().println("@@ERROR:@@ : " + ex);
            }
        }
        //
        return null;
    }

    public void setAjpConnectorPort(final boolean redirect, int port) {
        setConnectorPort("AJP/1.3", redirect, port);
    }

    public void setHttpConnectorPort(final boolean redirect, int port) {
        setConnectorPort("HTTP/1.1", redirect, port);
    }

    public void setConnectorPort(String protocol, final boolean redirect, int port) {
        if (protocol == null) {
            protocol = "HTTP/1.1";
        }
        if (port <= 0) {
            if ("AJP/1.3".equals(protocol)) {
                port = redirect ? 8443 : 8009;
            } else if ("HTTP/1.1".equals(protocol)) {
                port = redirect ? 8080 : 8443;
            }
        }
        final String _protocol = protocol;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Path serverXml = getCatalinaBase().resolve("conf").resolve("server.xml");
            if (Files.exists(serverXml)) {
                Document doc = docBuilder.parse(serverXml.toFile());
                Element root = doc.getDocumentElement();
                Element elem = XmlUtils.streamElements(root.getChildNodes())
                        .filter(x -> "Service".equalsIgnoreCase(x.getTagName()))
                        .flatMap(x -> XmlUtils.streamElements(x.getChildNodes()))
                        .filter(x -> "Connector".equalsIgnoreCase(x.getTagName()) && _protocol.equals(x.getAttribute("protocol")))
                        .findAny().orElse(null);
                if (elem != null) {
                    String p = elem.getAttribute(redirect ? "redirectPort" : "port");
                    if (String.valueOf(port).equals(p)) {
                        return;
                    }
                    elem.setAttribute(redirect ? "redirectPort" : "port", String.valueOf(port));
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource domSource = new DOMSource(doc);
                    StreamResult streamResult = new StreamResult(serverXml.toFile());
                    transformer.transform(domSource, streamResult);
                    return;
                }
                throw new IllegalArgumentException("Not Found Connector");
            }
        } catch (SAXException | IOException | ParserConfigurationException | TransformerException ex) {
            if (context.getSession().isPlainOut()) {
                context.getSession().err().println("@@ERROR:@@ : " + ex);
            }
            //
        }
    }

    public void setShutdownPort(int port) {
        try {
            if (port <= 0) {
                port = 8005;
            }
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Path serverXml = getCatalinaBase().resolve("conf").resolve("server.xml");
            if (Files.exists(serverXml)) {
                Document doc = docBuilder.parse(serverXml.toFile());
                Element root = doc.getDocumentElement();
                String p = root.getAttribute("port");
                if (String.valueOf(port).equals(p)) {
                    return;
                }
                root.setAttribute("port", String.valueOf(port));
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource domSource = new DOMSource(doc);
                StreamResult streamResult = new StreamResult(serverXml.toFile());
                transformer.transform(domSource, streamResult);
            }
        } catch (SAXException | IOException | ParserConfigurationException | TransformerException ex) {
            if (context.getSession().isPlainOut()) {
                context.getSession().err().println("@@ERROR:@@ : " + ex);
            }
            //
        }
    }

}
