package net.vpc.toolbox.tomcat.server;

import net.vpc.app.nuts.JsonSerializer;
import net.vpc.app.nuts.NutsFile;
import net.vpc.common.io.*;
import net.vpc.common.io.osapi.JpsResult;
import net.vpc.common.io.osapi.PosApis;
import net.vpc.toolbox.tomcat.util.NutsContext;
import net.vpc.toolbox.tomcat.server.config.TomcatServerAppConfig;
import net.vpc.toolbox.tomcat.server.config.TomcatServerConfig;
import net.vpc.toolbox.tomcat.server.config.TomcatServerDomainConfig;
import net.vpc.toolbox.tomcat.util.AppStatus;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class TomcatServerConfigService {
    public static final String SERVER_CONFIG_EXT = ".server-config";
    private String name;
    TomcatServer app;
    TomcatServerConfig config;
    NutsContext context;
    NutsFile catalinaNutsFile;
    String catalinaVersion;

    public TomcatServerConfigService(String name, TomcatServer app) {
        this.app = app;
        setName(name);
        this.context = app.context;
    }

    public TomcatServerConfigService setName(String name) {
        this.name = name;
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid name");
        }
        this.name = name.trim();
        return this;
    }

    public String getName() {
        return name;
    }

    public TomcatServerConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }


    public TomcatServerConfigService saveConfig() {
        JsonSerializer jsonSerializer = context.ws.getExtensionManager().createJsonSerializer();
        File f = new File(context.configFolder, name + SERVER_CONFIG_EXT);
        f.getParentFile().mkdirs();
        try (FileWriter r = new FileWriter(f)) {
            jsonSerializer.write(config, r, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public boolean existsConfig() {
        File f = new File(context.configFolder, name + SERVER_CONFIG_EXT);
        return (f.exists());
    }

    public String getCatalinaBase() {
        TomcatServerConfig c = getConfig();
        String catalinaBase = c.getCatalinaBase();
        if (catalinaBase == null) {
            catalinaBase = name;
        }
        NutsFile nf = getCatalinaNutsFile();
        String v = nf.getId().getVersion().toString();
        int x1 = v.indexOf('.');
        int x2 = x1 < 0 ? -1 : v.indexOf('.', x1 + 1);
        if (x2 > 0) {
            v = v.substring(0, x2);
        }
        return catalinaBase == null ? null : FileUtils.getAbsoluteFile(new File(context.varFolder, "catalina-base-" + v), catalinaBase).getPath();
    }

    public String getCatalinaVersion() {
        TomcatServerConfig c = getConfig();
        String v = c.getCatalinaVersion();
        if (v == null) {
            v = "";
        }
        return v;
    }

    public String getCatalinaHome() {
        NutsFile f = getCatalinaNutsFile();
        return f.getInstallFolder();
    }


    public void printStatus() {
        switch (getStatus()) {
            case RUNNING: {
                context.out.printf("==Running==\n");
                break;
            }
            case STOPPED: {
                context.out.printf("<<<Stopped>>>\n");
                break;
            }
            case OUT_OF_MEMORY: {
                context.out.printf("[[OutOfMemory]]\n");
                break;
            }
        }
    }

    public String[] parseApps(String[] args) throws RuntimeIOException {
        List<String> apps = new ArrayList<>();
        if (args != null) {
            for (String arg : args) {
                if (!TomcatUtils.isEmpty(arg)) {
                    for (String s : arg.split(",| ")) {
                        if (!s.isEmpty()) {
                            apps.add(s);
                        }
                    }
                }
            }
        }
        return apps.toArray(new String[apps.size()]);
    }

    public boolean start() throws RuntimeIOException {
        return start(null, false);
    }

    public boolean start(String[] deployApps, boolean deleteLogs) throws RuntimeIOException {
        TomcatServerConfig c = getConfig();
        JpsResult jpsResult = getJpsResult();
        if (jpsResult != null) {
            context.out.printf("[[%s]] Already running ...\n", "[Start Tomcat]");
            return false;
        }
        for (String app : new HashSet<String>(Arrays.asList(parseApps(deployApps)))) {
            getApp(app).deploy();
        }
        if (deleteLogs) {
            deleteOutLog();
        }
        NutsFile f = getCatalinaNutsFile();
        String catalinaHome = f.getInstallFolder();
        String catalinaBase = getCatalinaBase();
        if (catalinaBase != null) {
            new File(catalinaBase).mkdirs();
        }
        ProcessBuilder2 b = new ProcessBuilder2();
        String ext = context.ws.getPlatformOs().startsWith("windows") ? "bat" : "sh";
        checkExec(catalinaHome + "/bin/startup." + ext);
        checkExec(catalinaHome + "/bin/catalina." + ext);
        b.addCommand(catalinaHome + "/bin/startup." + ext);
        if (catalinaHome != null) {
            b.addCommand("-Dcatalina.home=" + catalinaHome);
        }
        if (catalinaBase != null) {
            b.addCommand("-Dcatalina.base=" + catalinaBase);
        }
        b.setDirectory(new File(catalinaBase));
        String javaHome = c.getJavaHome();
        if (javaHome == null) {
            javaHome = System.getProperty("java.home");
        }
        b.setEnv("JAVA_HOME", javaHome);
        b.setEnv("JRE_HOME", javaHome);
        if (catalinaBase != null) {
            b.setEnv("CATALINA_BASE", catalinaBase);
            b.setEnv("CATALINA_OUT", FileUtils.getNativePath(catalinaBase + "/logs/catalina.out"));
            b.setEnv("CATALINA_TMPDIR", FileUtils.getNativePath(catalinaBase + "/temp"));
            new File(catalinaBase, "logs").mkdirs();
            new File(catalinaBase, "temp").mkdirs();
            new File(catalinaBase, "conf").mkdirs();
            for (File conf : new File(catalinaHome, "conf").listFiles()) {
                if (conf.isFile() && !new File(catalinaBase, "conf/" + conf.getName()).exists()) {
                    try {
                        Files.copy(conf.toPath(), new File(catalinaBase, "conf/" + conf.getName()).toPath());
                    } catch (IOException e) {
                        throw new RuntimeIOException(e);
                    }
                }
            }
        }
        b.setOut(context.session.getTerminal().getOut());
        b.setErr(context.session.getTerminal().getErr());
        context.out.printf("[[ [Start Tomcat] ]] %s\n", b.getCommand());
        b.waitFor();
        waitForRunningStatus(null, null, c.getStartupWaitTime());
        return true;
    }

    private NutsFile getCatalinaNutsFile() {
        String catalinaVersion = getCatalinaVersion();
        if(catalinaNutsFile==null || !Objects.equals(catalinaVersion,this.catalinaVersion)) {
            this.catalinaVersion=catalinaVersion;
            catalinaNutsFile = context.ws.install("org.apache.catalina:tomcat#" + catalinaVersion + "*", false, context.session);
        }
        return catalinaNutsFile;
    }


    public boolean shutdown() {
        if (getJpsResult() == null) {
            context.out.printf("[[ [Stop Tomcat] ]] Already stopped");
        }
        TomcatServerConfig c = getConfig();
        NutsFile f = getCatalinaNutsFile();
        String catalinaHome = f.getInstallFolder();
        String catalinaBase = getCatalinaBase();
        ProcessBuilder2 b = new ProcessBuilder2();
        String javaHome = c.getJavaHome();
        if (javaHome == null) {
            javaHome = System.getProperty("java.home");
        }
        b.setEnv("JAVA_HOME", javaHome);
        b.setEnv("JRE_HOME", javaHome);
        String ext = context.ws.getPlatformOs().startsWith("windows") ? "bat" : "sh";
        checkExec(catalinaHome + "/bin/shutdown." + ext);
        checkExec(catalinaHome + "/bin/catalina." + ext);
        b.addCommand(catalinaHome + "/bin/shutdown." + ext);
        if (catalinaHome != null) {
            b.addCommand("-Dcatalina.home=" + catalinaHome);
        }
        if (catalinaBase != null) {
            b.addCommand("-Dcatalina.base=" + catalinaBase);
            b.setEnv("CATALINA_BASE", catalinaBase);
            b.setEnv("CATALINA_OUT", FileUtils.getNativePath(catalinaBase + "/logs/catalina.out"));
            b.setEnv("CATALINA_TMPDIR", FileUtils.getNativePath(catalinaBase + "/temp"));
        }
        context.out.printf("[[ [Stop Tomcat] ]] %s\n", b.getCommand());
        b.waitFor();
        waitForStoppedStatus(c.getShutdownWaitTime(), c.isKill());
        return getJpsResult() != null;
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


    private TomcatServerConfigService checkExec(String pathname) {
        File file = new File(FileUtils.getNativePath(pathname));
        if (!file.canExecute()) {
            if (!file.setExecutable(true)) {
                context.out.println(file.canExecute());
            }
        }
        return this;
    }

    public boolean restart() {
        return restart(null,false);
    }

    public boolean restart(String[] deployApps, boolean deleteLogs) {
        if (!shutdown()) {
            throw new IllegalArgumentException("Server " + name + " is running. It cannot be stopped!");
        }
        start(deployApps, deleteLogs);
        return true;
    }


    public AppStatus waitForRunningStatus(String domain, String app, int timeout) {

        AppStatus y = getStatus(domain, app);
        if (y == AppStatus.RUNNING) {
            this.context.out.printf("==Tomcat started==\n");
            return y;
        }
        if (timeout <= 0) {
            JpsResult ps = getJpsResult();
            if (ps != null) {
                this.context.out.printf("==Tomcat started==\n");
                return AppStatus.RUNNING;
            }
            throw new IllegalArgumentException("Unable to start tomcat");
        }
        for (int i = 0; i < timeout; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
            y = getStatus(domain, app);
            if (y == AppStatus.RUNNING) {
                this.context.out.printf("==Tomcat started==\n");
                return y;
            }
        }
        if (y == AppStatus.OUT_OF_MEMORY) {
            this.context.out.printf("==Tomcat out of memory==\n");
            return y;
        }
        throw new IllegalArgumentException("Unable to start tomcat");
    }

    public TomcatServerConfigService waitForStoppedStatus(int timeout, boolean kill) {

        JpsResult ps = getJpsResult();
        if (ps == null) {
            context.out.printf("==Tomcat stopped==\n");
            return this;
        }
        for (int i = 0; i < timeout; i++) {
            context.out.println("Waiting for process to die");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
            ps = getJpsResult();
            if (ps == null) {
                context.out.printf("==Tomcat stopped==\n");
                return this;
            }
        }
        if (kill && PosApis.get().isSupportedKillProcess()) {
            ps = getJpsResult();
            if (ps != null) {
                if (PosApis.get().killProcess(ps.getPid())) {
                    context.out.printf("[[ [Stop Tomcat] ]] Force kill : %s\n", ps.getPid());
                } else {
                    context.out.printf("[[ [Stop Tomcat] ]] unable to kill : %s\n", ps.getPid());
                }
            }
        }
        ps = getJpsResult();
        if (ps != null) {
            throw new IllegalArgumentException("Unable to stop tomcat");
        }
        context.out.printf("==Tomcat stopped==\n");
        return this;
    }


    public AppStatus getStatus() {
        return getStatus(null, null);
    }

    public AppStatus getStatus(String domain, String app) {
        TomcatServerConfig c = getConfig();
        String catalinaBase = getCatalinaBase();
        JpsResult ps = getJpsResult();
        if (ps != null) {
            String startupMessage = null;
            String shutdownMessage = null;
            if (app != null) {
                TomcatServerAppConfigService a = getAppOrCreate(app);
                domain = a.getConfig().getDomain();
                startupMessage = a.getConfig().getStartupMessage();
                shutdownMessage = a.getConfig().getShutdownMessage();
            }
            TomcatServerDomainConfigService tomcatDomain = getDomainOrCreate(domain);
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
            LineSource lineSource = TextFiles.create(log.getPath());
            TomcatServerLogLineVisitor visitor = new TomcatServerLogLineVisitor(startupMessage, shutdownMessage);
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

    public TomcatServerConfigService loadConfig() {
        if (name == null) {
            throw new IllegalArgumentException("Missing config name");
        }
        File f = new File(context.configFolder, name + SERVER_CONFIG_EXT);
        if (f.exists()) {
            JsonSerializer jsonSerializer = context.ws.getExtensionManager().createJsonSerializer();
            try (FileReader r = new FileReader(f)) {
                TomcatServerConfig i = jsonSerializer.read(r, TomcatServerConfig.class);
                if (i.getCatalinaVersion() == null) {
                    i.setCatalinaVersion("8.5");
                }
                config = i;
                return this;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new NoSuchElementException("Config not found : " + name);
    }

    public TomcatServerConfigService removeConfig() {
        File f = new File(context.configFolder, name + SERVER_CONFIG_EXT);
        f.delete();
        return this;
    }

    public TomcatServerConfigService write(PrintStream out) {
        JsonSerializer jsonSerializer = context.ws.getExtensionManager().createJsonSerializer();
        PrintWriter w = new PrintWriter(out);
        jsonSerializer.write(getConfig(), new PrintWriter(out), true);
        w.flush();
        return this;
    }

    public TomcatServerConfigService setConfig(TomcatServerConfig config) {
        this.config = config;
        return this;
    }


    public TomcatServerAppConfigService getApp(String appName) {
        return getAppOrError(appName);
    }

    public TomcatServerAppConfigService getAppOrNull(String appName) {
        TomcatServerAppConfig a = getConfig().getApps().get(appName);
        if (a == null) {
            return null;
        }
        return new TomcatServerAppConfigService(appName, a, this);
    }

    public TomcatServerAppConfigService getAppOrError(String appName) {
        TomcatServerAppConfig a = getConfig().getApps().get(appName);
        if (a == null) {
            throw new IllegalArgumentException("App not found :" + appName);
        }
        return new TomcatServerAppConfigService(appName, a, this);
    }

    public TomcatServerAppConfigService getAppOrCreate(String appName) {
        TomcatServerAppConfig a = getConfig().getApps().get(appName);
        if (a == null) {
            a = new TomcatServerAppConfig();
            getConfig().getApps().put(appName, a);
        }
        return new TomcatServerAppConfigService(appName, a, this);
    }

    public TomcatServerDomainConfigService getDomain(String domainName) {
        return getDomainOrError(domainName);
    }

    public TomcatServerDomainConfigService getDomainOrError(String domainName) {
        TomcatServerDomainConfigService d = getDomainOrNull(domainName);
        if (d == null) {
            throw new IllegalArgumentException("Domain not found :" + domainName);
        }
        return d;
    }

    public TomcatServerDomainConfigService getDomainOrNull(String domainName) {
        if (domainName == null) {
            domainName = "";
        }
        TomcatServerDomainConfig a = getConfig().getDomains().get(domainName);
        if (a == null) {
            return null;
        }
        return new TomcatServerDomainConfigService(domainName, a, this);
    }

    public TomcatServerDomainConfigService getDomainOrCreate(String domainName) {
        if (domainName == null) {
            domainName = "";
        }
        TomcatServerDomainConfig a = getConfig().getDomains().get(domainName);
        if (a == null) {
            a = new TomcatServerDomainConfig();
            getConfig().getDomains().put(domainName, a);
        }
        return new TomcatServerDomainConfigService(domainName, a, this);
    }

    public File getLogsFolder() {
        return new File(getCatalinaBase(), "logs");
    }

    public void deleteOutLog() {
        //get it from config?
        IOUtils.delete(new File(getLogsFolder(), "catalina.out"));
    }

    public List<TomcatServerAppConfigService> getApps() {
        List<TomcatServerAppConfigService> a = new ArrayList<>();
        for (String s : getConfig().getApps().keySet()) {
            a.add(new TomcatServerAppConfigService(s, getConfig().getApps().get(s), this));
        }
        return a;
    }
}
