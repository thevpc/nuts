package net.vpc.toolbox.tomcat.local;

import net.vpc.app.nuts.*;
import net.vpc.common.io.*;
import net.vpc.common.io.JpsResult;
import net.vpc.common.io.PosApis;
import net.vpc.toolbox.tomcat.local.config.LocalTomcatAppConfig;
import net.vpc.toolbox.tomcat.local.config.LocalTomcatConfig;
import net.vpc.toolbox.tomcat.local.config.LocalTomcatDomainConfig;
import net.vpc.toolbox.tomcat.util.AppStatus;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
        this.name = TomcatUtils.toValidFileName(name, "default");
        return this;
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
        String h = getConfig().getCatalinaHome();
        if (!TomcatUtils.isBlank(h)) {
            if (TomcatUtils.isBlank(v)) {
                File file = new File(h, "RELEASE-NOTES");
                if (file.exists()) {
                    try (BufferedReader r = new BufferedReader(new FileReader(file))) {
                        String line = null;
                        while ((line = r.readLine()) != null) {
                            line = line.trim();
                            if (line.startsWith("Apache Tomcat Version")) {
                                v = line.substring("Apache Tomcat Version".length()).trim();
                                if (!TomcatUtils.isBlank(v)) {
                                    getConfig().setCatalinaVersion(v);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        //
                    }
                }
            }
        }
        NutsIOManager io = context.getWorkspace().io();
        Path f = getConfigPath();
        io.json().write(config, f);
        return this;
    }

    public Path getConfigPath() {
        return context.getConfigFolder().resolve(getName() + LOCAL_CONFIG_EXT);
    }

    public boolean existsConfig() {
        return (Files.exists(getConfigPath()));
    }

    public String getEffectiveCatalinaVersion() {
        String h = getConfig().getCatalinaHome();
        if (TomcatUtils.isBlank(h)) {
            NutsDefinition nf = getCatalinaNutsDefinition();
            return nf.getId().getVersion().toString();
        }
        return h.trim();
    }

    public Path getCatalinaBase() {
        LocalTomcatConfig c = getConfig();
        Path catalinaBase = getContext().getWorkspace().io().path(c.getCatalinaBase());
        Path catalinaHome = getCatalinaHome();
        if (TomcatUtils.isBlank(getConfig().getCatalinaHome())
                && catalinaBase == null) {
            catalinaBase = getContext().getWorkspace().io().path(getName());
        }
        if (catalinaBase == null) {
            catalinaBase = catalinaHome;
        } else {
            if (!catalinaBase.isAbsolute()) {
                String v = getEffectiveCatalinaVersion();
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

    public String getRequestedCatalinaVersion() {
        LocalTomcatConfig c = getConfig();
        return c.getCatalinaVersion();
    }

    public Path getCatalinaHome() {
        String h = getConfig().getCatalinaHome();
        if (TomcatUtils.isBlank(h)) {
            NutsDefinition f = getCatalinaNutsDefinition();
            return f.getInstallation().getInstallFolder();
        } else {
            return getContext().getWorkspace().io().path(h);
        }
    }

    public void printStatus() {
        switch (getStatus()) {
            case RUNNING: {
                context.session().out().printf("==[%s]== Tomcat {{Running}}.\n", getName());
                break;
            }
            case STOPPED: {
                context.session().out().printf("==[%s]== Tomcat @@Stopped@@.\n", getName());
                break;
            }
            case OUT_OF_MEMORY: {
                context.session().out().printf("==[%s]== Tomcat [[OutOfMemory]].\n", getName());
                break;
            }
        }
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

    public ProcessBuilder2 invokeCatalina(String catalinaCommand) {
        Path catalinaHome = getCatalinaHome();
        Path catalinaBase = getCatalinaBase();
        boolean catalinaBaseUpdated = false;
        catalinaBaseUpdated |= mkdirs(catalinaBase);
        ProcessBuilder2 b = new ProcessBuilder2();
        String ext = context.getWorkspace().config().getPlatformOsFamily() == NutsOsFamily.WINDOWS ? "bat" : "sh";
        catalinaBaseUpdated |= checkExec(catalinaHome + "/bin/catalina." + ext);
        b.addCommand(catalinaHome + "/bin/catalina." + ext);
        b.addCommand(catalinaCommand);
//        if (catalinaHome != null) {
//            b.addCommand("-Dcatalina.home=" + catalinaHome);
//        }
//        b.addCommand("-Dcatalina.base=" + catalinaBase);
        b.setDirectory(catalinaBase.toFile());
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
        b.setEnv("CATALINA_OUT", FileUtils.getNativePath(catalinaBase + "/logs/catalina.out"));
        b.setEnv("CATALINA_TMPDIR", FileUtils.getNativePath(catalinaBase + "/temp"));
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
        if (catalinaBaseUpdated) {
            context.session().out().printf("==[%s]== Updated catalina base ==%s==\n", getName(), catalinaBase);
        }
        b.setOutput(context.getSession().getTerminal().out());
        b.setErr(context.getSession().getTerminal().err());
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
        JpsResult jpsResult = getJpsResult();
        if (jpsResult != null) {
            context.session().out().printf("==[%s]== Tomcat Already started.\n", getName());
            return false;
        }
        for (String app : new HashSet<String>(Arrays.asList(parseApps(deployApps)))) {
            getApp(app).deploy(null);
        }
        if (deleteLog) {
            deleteOutLog();
        }
        ProcessBuilder2 b = invokeCatalina("start");
        context.session().out().printf("==[%s]== Starting Tomcat. CMD=%s.\n", getName(), b.getCommand());
        try {
            b.waitFor();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
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
            catalinaVersion = "8.5";
        }
        if (catalinaNutsDefinition == null || !Objects.equals(catalinaVersion, this.catalinaVersion)) {
            this.catalinaVersion = catalinaVersion;
            NutsDefinition r = context.getWorkspace().search().id("org.apache.catalina:tomcat#" + catalinaVersion + "*")
                    .installInformation().session(context.getSession())
                    .getResultDefinitions().first();
            if(r!=null && r.getInstallation().isInstalled()){
             return r;
            }else{
            catalinaNutsDefinition = context.getWorkspace()
                    .install()
                    .id("org.apache.catalina:tomcat#" + catalinaVersion + "*")
                    .setSession(context.getSession().copy().trace().addListeners(new NutsInstallListener() {
                @Override
                public void onInstall(NutsDefinition nutsDefinition, boolean update, NutsSession session) {
                    context.session().out().printf("==[%s]== Tomcat Installed to catalina home ==%s==\n", getName(), nutsDefinition.getInstallation().getInstallFolder());
                }
            })).run().getResult()[0];
            }
        }
        return catalinaNutsDefinition;
    }

    public void deployFile(Path file, String contextName, String domain) {
        if (file.getFileName().toString().endsWith(".war")) {
            if (TomcatUtils.isBlank(contextName)) {
                contextName = file.getFileName().toString().substring(0, file.getFileName().toString().length() - ".war".length());
            }
            Path c = getDefaulDeployFolder(domain).resolve(contextName + ".war");
            context.session().out().printf("==[%s]== Deploy file file [[%s]] to [[%s]].\n", getName(), file, c);
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
        if (getJpsResult() == null) {
            context.session().out().printf("==[%s]== Tomcat already stopped.\n", getName());
            return false;
        }
        LocalTomcatConfig c = getConfig();
        ProcessBuilder2 b = invokeCatalina("stop");
        context.session().out().printf("==[%s]== Stopping Tomcat. CMD=%s.\n", getName(), b.getCommand());
        try {
            b.waitFor();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return waitForStoppedStatus(c.getShutdownWaitTime(), c.isKill());
    }

    public JpsResult getJpsResult() {
        Path catalinaBase = getCatalinaBase();
        JpsResult[] ps;
        try {
            ps = PosApis.get().findJavaProcessList(null, true, true,
                    (p) -> {
                        return p.getClassName().equals("org.apache.catalina.startup.Bootstrap")
                        && (catalinaBase == null
                        || p.getArgsLine().contains("-Dcatalina.base=" + catalinaBase));
                    }
            );
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        if (ps.length > 0) {
            return ps[0];
        }
        return null;
    }

    private boolean checkExec(String pathname) {
        File file = new File(FileUtils.getNativePath(pathname));
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
        if (getJpsResult() != null) {
            throw new NutsExecutionException(context.getWorkspace(),"Server " + getName() + " is running. It cannot be stopped!", 2);
        }
        start(deployApps, deleteLog);
        return true;
    }

    public AppStatus waitForRunningStatus(String domain, String app, int timeout) {

        AppStatus y = getStatus(domain, app);
        if (y == AppStatus.RUNNING) {
            context.session().out().printf("==[%s]== Tomcat started.\n", getName());
            return y;
        }
        if (timeout <= 0) {
            JpsResult ps = getJpsResult();
            if (ps != null) {
                context.session().out().printf("==[%s]== Tomcat started.\n", getName());
                return AppStatus.RUNNING;
            }
            throw new NutsExecutionException(context.getWorkspace(),"Unable to start tomcat", 2);
        }
        for (int i = 0; i < timeout; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
            y = getStatus(domain, app);
            if (y == AppStatus.RUNNING) {
                context.session().out().printf("==[%s]== Tomcat started.\n", getName());
                return y;
            }
        }
        if (y == AppStatus.OUT_OF_MEMORY) {
            context.session().out().printf("==[%s]== Tomcat out of memory.\n", getName());
            return y;
        }
        throw new NutsExecutionException(context.getWorkspace(),"Unable to start tomcat", 2);
    }

    public boolean waitForStoppedStatus(int timeout, boolean kill) {

        JpsResult ps = getJpsResult();
        if (ps == null) {
            context.session().out().printf("==[%s]== Tomcat stopped.\n", getName());
            return true;
        }
        for (int i = 0; i < timeout; i++) {
            context.session().out().printf("==[%s]== Waiting Tomcat process to die.\n", getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
            ps = getJpsResult();
            if (ps == null) {
                context.session().out().printf("==[%s]== Tomcat stopped.\n", getName());
                return true;
            }
        }
        if (kill && PosApis.get().isSupportedKillProcess()) {
            ps = getJpsResult();
            if (ps != null) {
                try {
                    if (PosApis.get().killProcess(ps.getPid())) {
                        context.session().out().printf("==[%s]== Tomcat process killed (%s).\n", getName(), ps.getPid());
                        return true;
                    } else {
                        context.session().out().printf("==[%s]== Tomcat process could not be killed ( %s).\n", getName(), ps.getPid());
                        return false;
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        }
        ps = getJpsResult();
        if (ps != null) {
            context.session().out().printf("==[%s]== Tomcat process could not be terminated (%s).\n", getName(), ps.getPid());
            return true;
        }
        context.session().out().printf("==Tomcat stopped==\n");
        return true;
    }

    public AppStatus getStatus() {
        return getStatus(null, null);
    }

    public AppStatus getStatus(String domain, String app) {
        LocalTomcatConfig c = getConfig();
        Path catalinaBase = getCatalinaBase();
        JpsResult ps = getJpsResult();
        if (ps != null) {
            String startupMessage = null;
            String shutdownMessage = null;
            if (app != null) {
                LocalTomcatAppConfigService a = getAppOrCreate(app);
                domain = a.getConfig().getDomain();
                startupMessage = a.getConfig().getStartupMessage();
                shutdownMessage = a.getConfig().getShutdownMessage();
            }
            LocalTomcatDomainConfigService tomcatDomain = getDomainOrCreate(domain);
            if (startupMessage == null || startupMessage.trim().isEmpty()) {
                startupMessage = tomcatDomain.getConfig().getStartupMessage();
            }
            if (shutdownMessage == null || shutdownMessage.trim().isEmpty()) {
                shutdownMessage = tomcatDomain.getConfig().getShutdownMessage();
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
            String logFile = tomcatDomain.getConfig().getLogFile();
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
            } else if (visitor.started == null) {
                return AppStatus.STOPPED;
            } else if (visitor.started) {
                return AppStatus.RUNNING;
            } else {
                return AppStatus.STOPPED;
            }
        }
        return AppStatus.STOPPED;

    }

    public LocalTomcatConfigService loadConfig() {
        String name = getName();
        Path f = context.getConfigFolder().resolve(name + LOCAL_CONFIG_EXT);
        if (Files.exists(f)) {
            NutsIOManager jsonSerializer = context.getWorkspace().io();
            config = jsonSerializer.json().read(f, LocalTomcatConfig.class);
            return this;
        } else if ("default".equals(name)) {
            //auto create default config
            config = new LocalTomcatConfig();
            save();
            return this;
        }
        throw new NoSuchElementException("Config not found : " + name);
    }

    public LocalTomcatConfigService remove() {
        try {
            Files.delete(context.getConfigFolder().resolve(getName() + LOCAL_CONFIG_EXT));
            return this;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public LocalTomcatConfigService write(PrintStream out) {
        TomcatUtils.writeJson(out, getConfig(), context.getWorkspace());
        return this;
    }

    public LocalTomcatConfigService setConfig(LocalTomcatConfig config) {
        this.config = config;
        return this;
    }

    public LocalTomcatAppConfigService getApp(String appName) {
        return getAppOrError(appName);
    }

    public LocalTomcatAppConfigService getAppOrNull(String appName) {
        appName = TomcatUtils.toValidFileName(appName, "default");
        LocalTomcatAppConfig a = getConfig().getApps().get(appName);
        if (a == null) {
            return null;
        }
        return new LocalTomcatAppConfigService(appName, a, this);
    }

    public LocalTomcatAppConfigService getAppOrError(String appName) {
        LocalTomcatAppConfigService a = getAppOrNull(appName);
        if (a == null) {
            throw new NutsExecutionException(context.getWorkspace(),"App not found :" + appName, 2);
        }
        return a;
    }

    public LocalTomcatAppConfigService getAppOrCreate(String appName) {
        appName = TomcatUtils.toValidFileName(appName, "default");
        LocalTomcatAppConfigService a = getAppOrNull(appName);
        if (a == null) {
            LocalTomcatAppConfig c = new LocalTomcatAppConfig();
            getConfig().getApps().put(appName, c);
            a = getAppOrNull(appName);
        }
        return a;
    }

    public LocalTomcatDomainConfigService getDomain(String domainName) {
        return getDomainOrError(domainName);
    }

    public LocalTomcatDomainConfigService getDomainOrError(String domainName) {
        domainName = TomcatUtils.toValidFileName(domainName, "default");
        LocalTomcatDomainConfigService d = getDomainOrNull(domainName);
        if (d == null) {
            throw new NutsExecutionException(context.getWorkspace(),"Domain not found :" + domainName, 2);
        }
        return d;
    }

    public LocalTomcatDomainConfigService getDomainOrNull(String domainName) {
        domainName = TomcatUtils.toValidFileName(domainName, "");
        LocalTomcatDomainConfig a = getConfig().getDomains().get(domainName);
        if (a == null) {
            return null;
        }
        return new LocalTomcatDomainConfigService(domainName, a, this);
    }

    public LocalTomcatDomainConfigService getDomainOrCreate(String domainName) {
        domainName = TomcatUtils.toValidFileName(domainName, "");
        LocalTomcatDomainConfig a = getConfig().getDomains().get(domainName);
        if (a == null) {
            a = new LocalTomcatDomainConfig();
            getConfig().getDomains().put(domainName, a);
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
            context.session().out().printf("==[%s]== Delete log file %s.\n", getName(), file);
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
                    context.session().out().printf("==[%s]== Delete temp file %s.\n", getName(), file);
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
                    context.session().out().printf("==[%s]== Delete work file %s.\n", getName(), file);
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
                Files.copy(file, context.session().out());
                return;
            }
            if (Files.isRegularFile(file)) {
                TextFiles.tail(file.toString(), tail, context.session().out());
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
                        }
                        {
                            context.session().out().printf("==[%s]== Delete log file %s.\n", getName(), file);
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

}
