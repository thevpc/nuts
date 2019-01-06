package net.vpc.toolbox.tomcat.local;

import net.vpc.app.nuts.*;
import net.vpc.common.io.*;
import net.vpc.common.io.JpsResult;
import net.vpc.common.io.PosApis;
import net.vpc.toolbox.tomcat.local.config.LocalTomcatAppConfig;
import net.vpc.toolbox.tomcat.local.config.LocalTomcatConfig;
import net.vpc.toolbox.tomcat.local.config.LocalTomcatDomainConfig;
import net.vpc.toolbox.tomcat.util.AppStatus;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class LocalTomcatConfigService extends LocalTomcatServiceBase {
    public static final String LOCAL_CONFIG_EXT = ".local-config";
    private String name;
    private LocalTomcat app;
    private LocalTomcatConfig config;
    private NutsApplicationContext context;
    private NutsDefinition catalinaNutsDefinition;
    private String catalinaVersion;

    public LocalTomcatConfigService(File file, LocalTomcat app) {
        this(
                file.getName().substring(0, file.getName().length() - LocalTomcatConfigService.LOCAL_CONFIG_EXT.length()),
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
        if (!TomcatUtils.isEmpty(h)) {
            if (TomcatUtils.isEmpty(v)) {
                File file = new File(h, "RELEASE-NOTES");
                if (file.exists()) {
                    try (BufferedReader r = new BufferedReader(new FileReader(file))) {
                        String line = null;
                        while ((line = r.readLine()) != null) {
                            line = line.trim();
                            if (line.startsWith("Apache Tomcat Version")) {
                                v = line.substring("Apache Tomcat Version".length()).trim();
                                if (!TomcatUtils.isEmpty(v)) {
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
        NutsIOManager jsonSerializer = context.getWorkspace().getIOManager();
        File f = new File(context.getConfigFolder(), getName() + LOCAL_CONFIG_EXT);
        f.getParentFile().mkdirs();
        try (FileWriter r = new FileWriter(f)) {
            jsonSerializer.writeJson(config, r, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public boolean existsConfig() {
        File f = new File(context.getConfigFolder(), getName() + LOCAL_CONFIG_EXT);
        return (f.exists());
    }

    public String getEffectiveCatalinaVersion() {
        String h = getConfig().getCatalinaHome();
        if (TomcatUtils.isEmpty(h)) {
            NutsDefinition nf = getCatalinaNutsDefinition();
            return nf.getId().getVersion().toString();
        }
        return h.trim();
    }

    public String getCatalinaBase() {
        LocalTomcatConfig c = getConfig();
        String catalinaBase = c.getCatalinaBase();
        String catalinaHome = getCatalinaHome();
        if (TomcatUtils.isEmpty(getConfig().getCatalinaHome())
            && TomcatUtils.isEmpty(catalinaBase)
        ) {
            catalinaBase = getName();
        }
        if (TomcatUtils.isEmpty(catalinaBase)) {
            catalinaBase = catalinaHome;
        } else {
            if (!new File(catalinaBase).isAbsolute()) {
                String v = getEffectiveCatalinaVersion();
                int x1 = v.indexOf('.');
                int x2 = x1 < 0 ? -1 : v.indexOf('.', x1 + 1);
                if (x2 > 0) {
                    v = v.substring(0, x2);
                }
                catalinaBase = FileUtils.getAbsoluteFile(new File(context.getVarFolder(), "catalina-base-" + v), catalinaBase).getPath();
            }
        }
        return catalinaBase;
    }

    public String getRequestedCatalinaVersion() {
        LocalTomcatConfig c = getConfig();
        return c.getCatalinaVersion();
    }

    public String getCatalinaHome() {
        String h = getConfig().getCatalinaHome();
        if (TomcatUtils.isEmpty(h)) {
            NutsDefinition f = getCatalinaNutsDefinition();
            return f.getInstallFolder();
        } else {
            return h;
        }
    }


    public void printStatus() {
        switch (getStatus()) {
            case RUNNING: {
                context.out().printf("==[%s]== Tomcat {{Running}}.\n", getName());
                break;
            }
            case STOPPED: {
                context.out().printf("==[%s]== Tomcat @@Stopped@@.\n", getName());
                break;
            }
            case OUT_OF_MEMORY: {
                context.out().printf("==[%s]== Tomcat [[OutOfMemory]].\n", getName());
                break;
            }
        }
    }

    public String[] parseApps(String[] args) throws RuntimeIOException {
        List<String> apps = new ArrayList<>();
        if (args != null) {
            for (String arg : args) {
                if (!TomcatUtils.isEmpty(arg)) {
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

    public boolean start() throws RuntimeIOException {
        return start(null, false);
    }

    public ProcessBuilder2 invokeCatalina(String catalinaCommand) throws RuntimeIOException {
        String catalinaHome = getCatalinaHome();
        String catalinaBase = getCatalinaBase();
        boolean catalinaBaseUpdated = false;
        catalinaBaseUpdated |= new File(catalinaBase).mkdirs();
        ProcessBuilder2 b = new ProcessBuilder2();
        String ext = context.getWorkspace().getConfigManager().getPlatformOs().getName().equals("windows") ? "bat" : "sh";
        catalinaBaseUpdated |= checkExec(catalinaHome + "/bin/catalina." + ext);
        b.addCommand(catalinaHome + "/bin/catalina." + ext);
        b.addCommand(catalinaCommand);
//        if (catalinaHome != null) {
//            b.addCommand("-Dcatalina.home=" + catalinaHome);
//        }
//        b.addCommand("-Dcatalina.base=" + catalinaBase);
        b.setDirectory(new File(catalinaBase));
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

        b.setEnv("CATALINA_HOME", catalinaHome);
        b.setEnv("CATALINA_BASE", catalinaBase);
        b.setEnv("CATALINA_OUT", FileUtils.getNativePath(catalinaBase + "/logs/catalina.out"));
        b.setEnv("CATALINA_TMPDIR", FileUtils.getNativePath(catalinaBase + "/temp"));
        catalinaBaseUpdated |= new File(catalinaBase, "logs").mkdirs();
        catalinaBaseUpdated |= new File(catalinaBase, "temp").mkdirs();
        catalinaBaseUpdated |= new File(catalinaBase, "conf").mkdirs();
        for (File conf : new File(catalinaHome, "conf").listFiles()) {
            File confFile = new File(catalinaBase, "conf/" + conf.getName());
            if (conf.isFile() && !confFile.exists()) {
                catalinaBaseUpdated = true;
                try {
                    Files.copy(conf.toPath(), confFile.toPath());
                } catch (IOException e) {
                    throw new RuntimeIOException(e);
                }
            }
        }
        if (catalinaBaseUpdated) {
            context.out().printf("==[%s]== Updated catalina base ==%s==\n", getName(), catalinaBase);
        }
        b.setOutput(context.getSession().getTerminal().getOut());
        b.setErr(context.getSession().getTerminal().getErr());
        return b;
    }

    public boolean start(String[] deployApps, boolean deleteLogs) throws RuntimeIOException {
        LocalTomcatConfig c = getConfig();
        JpsResult jpsResult = getJpsResult();
        if (jpsResult != null) {
            context.out().printf("==[%s]== Tomcat Already started.\n", getName());
            return false;
        }
        for (String app : new HashSet<String>(Arrays.asList(parseApps(deployApps)))) {
            getApp(app).deploy(null);
        }
        if (deleteLogs) {
            deleteOutLog();
        }
        ProcessBuilder2 b = invokeCatalina("start");
        context.out().printf("==[%s]== Starting Tomcat. CMD=%s.\n", getName(), b.getCommand());
        b.waitFor();
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
            catalinaNutsDefinition = context.getWorkspace().install("org.apache.catalina:tomcat#" + catalinaVersion + "*", new String[0], NutsConfirmAction.IGNORE, context.getSession().copy().addListeners(new NutsInstallListener() {
                @Override
                public void onInstall(NutsDefinition nutsDefinition, boolean update, NutsSession session) {
                    context.out().printf("==[%s]== Tomcat Installed to catalina home ==%s==\n", getName(), nutsDefinition.getInstallFolder());
                }
            }));
        }
        return catalinaNutsDefinition;
    }


    public void deployFile(File file, String contextName, String domain) {
        if (file.getName().endsWith(".war")) {
            if (TomcatUtils.isEmpty(contextName)) {
                contextName = file.getName().substring(0, file.getName().length() - ".war".length());
            }
            File c = new File(getDefaulDeployFolder(domain), contextName + ".war");
            context.out().printf("==[%s]== Deploy file file [[%s]] to [[%s]].\n", getName(), file.getPath(), c);
            IOUtils.copy(file, c);
        } else {
            throw new RuntimeException("Expected war file");
        }
    }

    public boolean stop() {
        if (getJpsResult() == null) {
            context.out().printf("==[%s]== Tomcat already stopped.\n", getName());
            return false;
        }
        LocalTomcatConfig c = getConfig();
        ProcessBuilder2 b = invokeCatalina("stop");
        context.out().printf("==[%s]== Stopping Tomcat. CMD=%s.\n", getName(), b.getCommand());
        b.waitFor();
        return waitForStoppedStatus(c.getShutdownWaitTime(), c.isKill());
    }

    public JpsResult getJpsResult() throws RuntimeIOException {
        String catalinaBase = getCatalinaBase();
        JpsResult[] ps = PosApis.get().findJavaProcessList(null, true, true,
                (p) -> {
                    return p.getClassName().equals("org.apache.catalina.startup.Bootstrap")
                            && (catalinaBase == null ||
                            p.getArgsLine().contains("-Dcatalina.base=" + catalinaBase)
                    )
                            ;
                }
        );
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

    public boolean restart(String[] deployApps, boolean deleteLogs) {
        stop();
        if (getJpsResult() != null) {
            throw new NutsExecutionException("Server " + getName() + " is running. It cannot be stopped!",2);
        }
        start(deployApps, deleteLogs);
        return true;
    }


    public AppStatus waitForRunningStatus(String domain, String app, int timeout) {

        AppStatus y = getStatus(domain, app);
        if (y == AppStatus.RUNNING) {
            context.out().printf("==[%s]== Tomcat started.\n", getName());
            return y;
        }
        if (timeout <= 0) {
            JpsResult ps = getJpsResult();
            if (ps != null) {
                context.out().printf("==[%s]== Tomcat started.\n", getName());
                return AppStatus.RUNNING;
            }
            throw new NutsExecutionException("Unable to start tomcat",2);
        }
        for (int i = 0; i < timeout; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
            y = getStatus(domain, app);
            if (y == AppStatus.RUNNING) {
                context.out().printf("==[%s]== Tomcat started.\n", getName());
                return y;
            }
        }
        if (y == AppStatus.OUT_OF_MEMORY) {
            context.out().printf("==[%s]== Tomcat out of memory.\n", getName());
            return y;
        }
        throw new NutsExecutionException("Unable to start tomcat",2);
    }

    public boolean waitForStoppedStatus(int timeout, boolean kill) {

        JpsResult ps = getJpsResult();
        if (ps == null) {
            context.out().printf("==[%s]== Tomcat stopped.\n", getName());
            return true;
        }
        for (int i = 0; i < timeout; i++) {
            context.out().printf("==[%s]== Waiting Tomcat process to die.\n", getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
            ps = getJpsResult();
            if (ps == null) {
                context.out().printf("==[%s]== Tomcat stopped.\n", getName());
                return true;
            }
        }
        if (kill && PosApis.get().isSupportedKillProcess()) {
            ps = getJpsResult();
            if (ps != null) {
                if (PosApis.get().killProcess(ps.getPid())) {
                    context.out().printf("==[%s]== Tomcat process killed (%s).\n", getName(), ps.getPid());
                    return true;
                } else {
                    context.out().printf("==[%s]== Tomcat process could not be killed ( %s).\n", getName(), ps.getPid());
                    return false;
                }
            }
        }
        ps = getJpsResult();
        if (ps != null) {
            context.out().printf("==[%s]== Tomcat process could not be terminated (%s).\n", getName(), ps.getPid());
            return true;
        }
        context.out().printf("==Tomcat stopped==\n");
        return true;
    }


    public AppStatus getStatus() {
        return getStatus(null, null);
    }

    public AppStatus getStatus(String domain, String app) {
        LocalTomcatConfig c = getConfig();
        String catalinaBase = getCatalinaBase();
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

            File log = FileUtils.getAbsoluteFile(new File(catalinaBase), logFile);
            if (!log.exists()) {
                return AppStatus.STOPPED;
            }
            LineSource lineSource = TextFiles.create(log.getPath());
            LocalTomcatLogLineVisitor visitor = new LocalTomcatLogLineVisitor(startupMessage, shutdownMessage);
            TextFiles.visit(lineSource, visitor);
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
        File f = new File(context.getConfigFolder(), name + LOCAL_CONFIG_EXT);
        if (f.exists()) {
            NutsIOManager jsonSerializer = context.getWorkspace().getIOManager();
            try (FileReader r = new FileReader(f)) {
                config = jsonSerializer.readJson(r, LocalTomcatConfig.class);
                return this;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if ("default".equals(name)) {
            //auto create default config
            config = new LocalTomcatConfig();
            save();
            return this;
        }
        throw new NoSuchElementException("Config not found : " + name);
    }

    public LocalTomcatConfigService remove() {
        File f = new File(context.getConfigFolder(), getName() + LOCAL_CONFIG_EXT);
        f.delete();
        return this;
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
            throw new NutsExecutionException("App not found :" + appName,2);
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
            throw new NutsExecutionException("Domain not found :" + domainName,2);
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

    public File getLogsFolder() {
        return new File(getCatalinaBase(), "logs");
    }

    public File getTempFolder() {
        return new File(getCatalinaBase(), "temp");
    }

    public File getWorkFolder() {
        return new File(getCatalinaBase(), "work");
    }

    public void deleteOutLog() {
        //get it from config?
        File file = new File(getLogsFolder(), "catalina.out");
        if (file.isFile()) {
            context.out().printf("==[%s]== Delete log file %s.\n", getName(), file.getPath());
            IOUtils.delete(file);
        }
    }

    public void deleteTemp() {
        File[] files = getTempFolder().listFiles();
        if (files != null) {
            for (File file : files) {
                context.out().printf("==[%s]== Delete temp file %s.\n", getName(), file.getPath());
                IOUtils.delete(file);
            }
        }
    }

    public void deleteWork() {
        File[] files = getWorkFolder().listFiles();
        if (files != null) {
            for (File file : files) {
                context.out().printf("==[%s]== Delete work file %s.\n", getName(), file.getPath());
                IOUtils.delete(file);
            }
        }
    }

    public File getOutLogFile() {
        return new File(getLogsFolder(), "catalina.out");
    }

    public void showOutLog(int tail) {
        //get it from config?
        File file = getOutLogFile();
        if (tail <= 0) {
            IOUtils.copy(file, context.out(), false);
            return;
        }
        if (file.isFile()) {
            TextFiles.tail(file.getPath(), tail, context.out());
        }
    }

    public void deleteAllLog() {
        File[] files = getLogsFolder().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String n = file.getName();
                    if (
                            n.endsWith(".out")
                                    || n.endsWith(".txt")
                                    || n.endsWith(".log")
                    ) {
                        //this is a log file, will delete it
                    }
                    {
                        context.out().printf("==[%s]== Delete log file %s.\n", getName(), file.getPath());
                        IOUtils.delete(file);
                    }
                }
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

    public String getDefaulDeployFolder(String domainName) {
        String p = "webapps";
        if (domainName == null) {
            domainName = "";
        }
        if (!domainName.equals("")) {
            p += ("/" + domainName);
        }
        return getCatalinaBase() + "/" + p;
    }

    public LocalTomcat getTomcatServer() {
        return app;
    }

    public NutsApplicationContext getContext() {
        return context;
    }

}
