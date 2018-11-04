package net.vpc.toolbox.tomcat;

import net.vpc.app.nuts.*;
import net.vpc.common.io.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class TomcatMain {
    private NutsWorkspace ws;
    NutsSession session;
    String storeRoot;
    NutsFormattedPrintStream out;

    public static void main(String[] args) throws IOException, InterruptedException {
        NutsWorkspace ws = Nuts.openWorkspace(args);
        args = Nuts.skipNutsArgs(args);
        TomcatMain m = new TomcatMain(ws);
        m.runArgs(args);
    }

    public TomcatMain(NutsWorkspace ws) {
        this.ws = ws;
        storeRoot = ws.getStoreRoot(ws.parseNutsId("net.vpc.app.nuts.toolbox:tomcat#LATEST"));
        session = ws.createSession();
        out = session.getTerminal().getFormattedOut();
    }


    public void runArgs(String[] args) throws IOException, InterruptedException {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-l") || args[i].equals("--list")) {
                List<String> names = new ArrayList<>();
                for (int j = i + 1; j < args.length; j++) {
                    if (args[j].startsWith("-")) {
                        throw new IllegalArgumentException("Unsupported " + args[i]);
                    } else {
                        names.add(args[j]);
                    }
                }
                PrintStream out = session.getTerminal().getFormattedOut();
                PrintStream err = session.getTerminal().getFormattedErr();
                if (names.isEmpty()) {
                    for (TomcatConfig tomcatConfig : listConfig()) {
                        out.println(tomcatConfig.getName());
                    }
                } else {
                    for (String n : names) {
                        String confName = null;
                        String propName1 = null;
                        String propName2 = null;
                        if (n.contains(".")) {
                            confName = n;
                            propName1 = null;
                            propName2 = null;
                        } else {
                            String[] split = n.split("\\.");
                            confName = split[0];
                            propName1 = split[1];
                            if (split.length > 2) {
                                propName2 = split[2];
                            }
                        }
                        try {
                            TomcatConfig c = loadConfig(confName);
                            if (propName1 == null) {
                                JsonSerializer jsonSerializer = ws.getExtensionManager().createJsonSerializer();
                                PrintWriter w = new PrintWriter(out);
                                jsonSerializer.write(c, new PrintWriter(out), true);
                                w.flush();
                            } else {
                                try {
                                    Object o1 = getPropertyValue(c, propName1);
                                    if (propName2 != null) {
                                        o1 = getPropertyValue(o1, propName2);
                                    }
                                    out.println(o1);
                                } catch (Exception ex) {
                                    err.println("Property Not Found " + n);
                                }
                            }
                        } catch (Exception ex) {
                            err.println(n + "   :  Not found");
                        }
                    }
                }
                return;
            } else if (args[i].equals("-a") || args[i].equals("--add") || args[i].equals("--set")) {
                TomcatConfig c;
                i++;
                String n = args[i];
                try {
                    c = loadConfig(n);
                } catch (Exception ex) {
                    c = new TomcatConfig();
                }
                c.setName(n);
                String appName = null;
                String domainName = null;
                for (int j = i + 1; j < args.length; j++) {
                    if (args[j].equals("-v") || args[j].equals("--catalinaVersion")) {
                        j++;
                        c.setCatalinaVersion(args[j]);
                    } else if (args[j].equals("-b") || args[j].equals("--catalinaBase")) {
                        j++;
                        c.setCatalinaBase(args[j]);
                    } else if (args[j].equals("-w") || args[j].equals("--shutdownWaitTime")) {
                        j++;
                        c.setShutdownWaitTime(Integer.parseInt(args[j]));
                    } else if (args[j].equals("--app.name")) {
                        j++;
                        appName = args[j];
                        getTomcatApp(c, appName);
                    } else if (args[j].equals("--domain")) {
                        j++;
                        domainName = args[j];
                        getTomcatDomain(c, domainName);
                    } else if (args[j].equals("--domain.log")) {
                        j++;
                        getTomcatDomain(c, domainName).setLogFile(args[j]);

                    } else if (args[j].equals("--app.source")) {
                        j++;
                        String value = args[j];
                        TomcatAppConfig tomcatAppConfig = getTomcatAppOrError(c, appName);
                        if (tomcatAppConfig == null) {
                            throw new IllegalArgumentException("Missing --app.name");
                        }
                        tomcatAppConfig.setSourceName(value);
                    } else if (args[j].equals("--app.deploy")) {
                        j++;
                        String value = args[j];
                        TomcatAppConfig tomcatAppConfig = getTomcatAppOrError(c, appName);
                        tomcatAppConfig.setDeployName(value);
                    } else if (args[j].equals("--app.domain")) {
                        j++;
                        String value = args[j];
                        TomcatAppConfig tomcatAppConfig = getTomcatAppOrError(c, appName);
                        tomcatAppConfig.setDomain(value);
                    } else if (args[j].equals("--archiveFolder")) {
                        j++;
                        c.setArchiveFolder(args[j]);
                    } else if (args[j].equals("--runningFolder")) {
                        j++;
                        c.setRunningFolder(args[j]);
                    } else {
                        throw new IllegalArgumentException("Unsupported " + args[i]);
                    }
                }
                saveConfig(c);
                return;
            } else if (args[i].equals("-r") || args[i].equals("--remove")) {
                String name = null;
                String appName = null;
                for (int j = i + 1; j < args.length; j++) {
                    if (args[j].startsWith("-")) {
                        if (args[j].equals("--app.name")) {
                            j++;
                            appName = args[j];
                        }
                    } else {
                        name = args[j];
                    }
                }
                if (appName == null) {
                    removeConfig(name);
                } else {
                    TomcatConfig c;
                    try {
                        c = loadConfig(name);
                        c.getApps().remove(appName);
                        saveConfig(c);
                    } catch (Exception ex) {
                        //
                    }
                }
                return;
            } else if (args[i].equals("-s") || args[i].equals("--start")) {
                i++;
                start(args[i]);
                return;
            } else if (args[i].equals("-x") || args[i].equals("--stop") || args[i].equals("--shutdown")) {
                String name = null;
                int wait = 0;
                for (int j = i + 1; j < args.length; j++) {
                    if (args[j].equals("-w") || args[j].equals("--wait")) {
                        j++;
                        wait = Integer.parseInt(args[j]);
                    } else {
                        name = args[j];
                    }
                }
                shutdown(name);
                return;
            } else if (args[i].equals("-S") || args[i].equals("--restart")) {
                String name = null;
                int wait = 0;
                for (int j = i + 1; j < args.length; j++) {
                    if (args[j].equals("-w") || args[j].equals("--wait")) {
                        j++;
                        wait = Integer.parseInt(args[j]);
                    } else {
                        name = args[j];
                    }
                }
                restart(name);
                return;
            }
        }
    }

    public void removeAllConfigs() {
        for (TomcatConfig tomcatConfig : listConfig()) {
            removeConfig(tomcatConfig.getName());
        }
    }

    public void removeConfig(String name) {
        File f = new File(storeRoot, name + ".config");
        f.delete();
    }

    public void saveConfig(TomcatConfig config) {
        if (config.getName() == null || config.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid name");
        }
        config.setName(config.getName().trim());
        JsonSerializer jsonSerializer = ws.getExtensionManager().createJsonSerializer();
        File f = new File(storeRoot, config.getName() + ".config");
        f.getParentFile().mkdirs();
        try (FileWriter r = new FileWriter(f)) {
            jsonSerializer.write(config, r, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existsConfig(String name) {
        File f = new File(storeRoot, name + ".config");
        return (f.exists());
    }

    public TomcatConfig[] listConfig() {
        List<TomcatConfig> all = new ArrayList<>();
        File[] configFiles = new File(storeRoot).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".config");
            }
        });
        if (configFiles != null) {
            for (File file1 : configFiles) {
                try {
                    TomcatConfig c = loadConfig(file1.getName().substring(0, file1.getName().length() - ".config".length()));
                    all.add(c);
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        return all.toArray(new TomcatConfig[all.size()]);
    }

    public TomcatConfig loadConfig(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Missing config name");
        }
        File f = new File(storeRoot, name + ".config");
        if (f.exists()) {
            JsonSerializer jsonSerializer = ws.getExtensionManager().createJsonSerializer();
            try (FileReader r = new FileReader(f)) {
                TomcatConfig i = jsonSerializer.read(r, TomcatConfig.class);
                if (!name.equals(i.getName())) {
                    i.setName(name);
                }
                if (i.getCatalinaVersion() == null) {
                    i.setCatalinaVersion("8.5");
                }
                return i;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new NoSuchElementException("Config not found : " + name);
    }

    public boolean start(String name) throws IOException, InterruptedException {
        TomcatConfig c = loadConfig(name);
        JpsResult jpsResult = getJpsResult(name);
        if (jpsResult != null) {
            out.printf("[[ [Start Tomcat] ]] Already running ...\n");
            return false;
        }
        NutsFile f = ws.install("org.apache.catalina:tomcat#" + c.getCatalinaVersion() + "*", false, session);
        String catalinaHome = f.getInstallFolder();
        String catalinaBase = getCatalinaBase(name);
        if (catalinaBase != null) {
            new File(catalinaBase).mkdirs();
        }
        ProcessBuilder2 b = new ProcessBuilder2();
        String ext = ws.getPlatformOs().startsWith("windows") ? "bat" : "sh";
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
                    Files.copy(conf.toPath(), new File(catalinaBase, "conf/" + conf.getName()).toPath());
                }
            }
        }
        b.setOut(session.getTerminal().getOut());
        b.setErr(session.getTerminal().getErr());
        out.printf("[[ [Start Tomcat] ]] %s\n",b.getCommand());
        b.waitFor();
        waitForRunningStatus(name, null, null, c.getStartupWaitTime());
        return true;
    }

    private void checkExec(String pathname) {
        File file = new File(FileUtils.getNativePath(pathname));
        if (!file.canExecute()) {
            if (!file.setExecutable(true)) {
                out.println(file.canExecute());
            }
        }
    }

    public boolean restart(String name) throws IOException, InterruptedException {
        if (!shutdown(name)) {
            throw new IllegalArgumentException("Server " + name + " is running. It cannot be stopped!");
        }
        start(name);
        return true;
    }

    public String getCatalinaHome(String name) {
        TomcatConfig c = loadConfig(name);
        NutsFile f = ws.install("org.apache.catalina:tomcat#" + c.getCatalinaVersion() + "*", false, session);
        return f.getInstallFolder();
    }

    public boolean shutdown(String name) throws IOException, InterruptedException {
        if(getJpsResult(name)==null){
            out.printf("[[ [Stop Tomcat] ]] Already stopped");
        }
        TomcatConfig c = loadConfig(name);
        NutsFile f = ws.install("org.apache.catalina:tomcat#" + c.getCatalinaVersion() + "*", false, session);
        String catalinaHome = f.getInstallFolder();
        String catalinaBase = getCatalinaBase(name);
        ProcessBuilder2 b=new ProcessBuilder2();
        String javaHome = c.getJavaHome();
        if (javaHome == null) {
            javaHome = System.getProperty("java.home");
        }
        b.setEnv("JAVA_HOME", javaHome);
        b.setEnv("JRE_HOME", javaHome);
        String ext = ws.getPlatformOs().startsWith("windows") ? "bat" : "sh";
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
        out.printf("[[ [Stop Tomcat] ]] %s\n",b.getCommand());
        b.waitFor();
        waitForStoppedStatus(name,c.getShutdownWaitTime(),c.isKill());
        return getJpsResult(name)!=null;
    }

    public AppStatus waitForRunningStatus(String name, String domain, String app, int timeout) throws IOException, InterruptedException {

        AppStatus y = getStatus(name, domain, app);
        if (y == AppStatus.RUNNING) {
            out.printf("==Tomcat started==\n");
            return y;
        }
        if (timeout <= 0) {
            JpsResult ps = getJpsResult(name);
            if (ps != null) {
                out.printf("==Tomcat started==\n");
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
            y = getStatus(name, domain, app);
            if (y == AppStatus.RUNNING) {
                out.printf("==Tomcat started==\n");
                return y;
            }
        }
        if (y == AppStatus.OUT_OF_MEMORY) {
            out.printf("==Tomcat out of memory==\n");
            return y;
        }
        throw new IllegalArgumentException("Unable to start tomcat");
    }

    public void waitForStoppedStatus(String name, int timeout, boolean kill) throws IOException, InterruptedException {

        JpsResult ps = getJpsResult(name);
        if (ps == null) {
            out.printf("==Tomcat stopped==\n");
            return;
        }
        for (int i = 0; i < timeout; i++) {
            out.println("Waiting for process to die");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
            ps = getJpsResult(name);
            if (ps == null) {
                out.printf("==Tomcat stopped==\n");
                return;
            }
        }
        if (kill) {
            ps = getJpsResult(name);
            if (ps != null) {
                ProcessBuilder2 b = new ProcessBuilder2().addCommand("kill", "-9", ps.getPid());
                out.printf("[[ [Stop Tomcat] ]] Force kill : %s\n",b.getCommand());
                b.waitFor();
            }
        }
        ps = getJpsResult(name);
        if (ps != null) {
            throw new IllegalArgumentException("Unable to stop tomcat");
        }
        out.printf("==Tomcat stopped==\n");
    }

    public JpsResult getJpsResult(String name) throws IOException, InterruptedException {
        TomcatConfig c = loadConfig(name);
        String catalinaBase = getCatalinaBase(name);
        JpsResult[] ps = ProcessUtils.findJavaProcessList(null, true, true,
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

    public AppStatus getStatus(String name, String domain, String app) {
        try {
            TomcatConfig c = loadConfig(name);
            String catalinaBase = getCatalinaBase(name);
            JpsResult[] ps = ProcessUtils.findJavaProcessList(null, true, true,
                    (p) -> {
                        return p.getClassName().equals("org.apache.catalina.startup.Bootstrap")
                                && (catalinaBase == null ||
                                p.getArgsLine().contains("-Dcatalina.base=" + catalinaBase)
                        )
                                ;
                    }
            );
            if (ps.length > 0) {
                JpsResult p = ps[0];
                String startupMessage = null;
                String shutdownMessage = null;
                if (app != null) {
                    TomcatAppConfig a = getTomcatApp(c, app);
                    domain = a.getDomain();
                    startupMessage = a.getStartupMessage();
                    shutdownMessage = a.getShutdownMessage();
                }
                TomcatDomainConfig tomcatDomain = getTomcatDomain(c, domain);
                if (startupMessage == null || startupMessage.trim().isEmpty()) {
                    startupMessage = tomcatDomain.getStartupMessage();
                }
                if (shutdownMessage == null || shutdownMessage.trim().isEmpty()) {
                    shutdownMessage = tomcatDomain.getShutdownMessage();
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
                String logFile = tomcatDomain.getLogFile();
                if (logFile == null || logFile.isEmpty()) {
                    logFile = c.getLogFile();
                }
                if (logFile == null || logFile.isEmpty()) {
                    logFile = "logs/catalina.out";
                }

                File log = FileUtils.createFileByCwd(logFile, new File(catalinaBase));
                LineSource lineSource = TextFiles.create(log.getPath());
                TomcatLogLineVisitor visitor = new TomcatLogLineVisitor(startupMessage, shutdownMessage);
                TextFiles.visit(lineSource, visitor);

                if (visitor.outOfMemoryError) {
                    return AppStatus.OUT_OF_MEMORY;
                } else if (visitor.started == null) {
                    return AppStatus.NOT_RUNNING;
                } else if (visitor.started) {
                    return AppStatus.RUNNING;
                } else {
                    return AppStatus.NOT_RUNNING;
                }
            }
            return AppStatus.NOT_RUNNING;
        } catch (InterruptedException e) {
            throw new IllegalArgumentException(e);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String getCatalinaBase(String name) {
        TomcatConfig c = loadConfig(name);
        String catalinaBase = c.getCatalinaBase();
        if(catalinaBase == null){
            catalinaBase=c.getName();
        }
        return catalinaBase == null ? null : FileUtils.createFileByCwd(catalinaBase, new File(getCatalinaHome(name), "catalina-base")).getPath();
    }

    public boolean isAlive(String server, int port) {
        if (server == null) {
            server = "localhost";
        }
        if (port < 0) {
            port = 80;
        }
        try {
            URL u = new URL("http://" + server + ":" + port);
            try {
                URLConnection c = u.openConnection();
                c.setConnectTimeout(2000);
                InputStream is = c.getInputStream();
                is.close();
                return true;
            } catch (FileNotFoundException ex) {
                return true;
            } catch (ConnectException ex) {
                return false;
            } catch (Exception ex) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Object getPropertyValue(Object obj, String propName) {
        propName = Character.toUpperCase(propName.charAt(0)) + propName.substring(1);
        Method m = null;
        try {
            m = obj.getClass().getDeclaredMethod("get" + propName);
            return m.invoke(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private TomcatAppConfig getTomcatAppOrError(TomcatConfig c, String appName) {
        TomcatAppConfig a = c.getApps().get(appName);
        if (a == null) {
            throw new IllegalArgumentException("AppName not found :" + appName);
        }
        return a;
    }

    private TomcatAppConfig getTomcatApp(TomcatConfig c, String appName) {
        TomcatAppConfig a = c.getApps().get(appName);
        if (a == null) {
            a = new TomcatAppConfig();
            a.setSourceName(appName);
            c.getApps().put(appName, a);
        }
        return a;
    }

    private TomcatDomainConfig getTomcatDomain(TomcatConfig c, String domainName) {
        if (domainName == null) {
            domainName = "default";
        }
        TomcatDomainConfig a = c.getDomains().get(domainName);
        if (a == null) {
            a = new TomcatDomainConfig();
            a.setDomain(domainName);
            c.getDomains().put(domainName, a);
        }
        return a;
    }

}
