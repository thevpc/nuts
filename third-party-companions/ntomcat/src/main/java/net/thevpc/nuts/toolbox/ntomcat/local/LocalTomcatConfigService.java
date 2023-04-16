package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NObjectFormat;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.ntomcat.NTomcatConfigVersions;
import net.thevpc.nuts.toolbox.ntomcat.local.config.LocalTomcatAppConfig;
import net.thevpc.nuts.toolbox.ntomcat.local.config.LocalTomcatConfig;
import net.thevpc.nuts.toolbox.ntomcat.local.config.LocalTomcatDomainConfig;
import net.thevpc.nuts.toolbox.ntomcat.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.util.*;

public class LocalTomcatConfigService extends LocalTomcatServiceBase {

    public static final String LOCAL_CONFIG_EXT = ".local-config";
    private final LocalTomcat app;
    private final NSession session;
    private final NPath sharedConfigFolder;
    private String name;
    private LocalTomcatConfig config;
    private NDefinition catalinaNDefinition;
    private String catalinaVersion;

    public LocalTomcatConfigService(NPath file, LocalTomcat app) {
        this(
                file.getName().substring(0, file.getName().length() - LocalTomcatConfigService.LOCAL_CONFIG_EXT.length()),
                app
        );
        loadConfig();
    }

    public LocalTomcatConfigService(String name, LocalTomcat app) {
        this.app = app;
        setName(name);
        this.session = app.getSession();
        sharedConfigFolder = app.getSession().getAppVersionFolder(NStoreType.CONF, NTomcatConfigVersions.CURRENT);
    }

    public void open(NOpenMode autoCreate) {
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
            case OPEN_OR_ERROR: {
                if (!this.existsConfig()) {
                    throw new NamedItemNotFoundException("Instance not found : " + this.getName(), this.getName());
                }
                this.loadConfig();
                break;
            }
            case CREATE_OR_ERROR: {
                if (this.existsConfig()) {
                    throw new NamedItemNotFoundException("Instance already exists : " + this.getName(), this.getName());
                }
                this.setConfig(new LocalTomcatConfig());
                this.save();
                break;
            }
        }
    }

    public LocalTomcatConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    public String getName() {
        return name;
    }

    public LocalTomcatConfigService setName(String name) {
        if (".".equals(name)) {
            name = "default";
        }
        this.name = TomcatUtils.toValidFileName(name, "default");
        return this;
    }

    @Override
    public LocalTomcatConfigService print(NPrintStream out) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("config-name", getName());
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
        NObjectFormat.of(getSession()).setValue(result).print(out);
        return this;
    }

    @Override
    public LocalTomcatConfigService remove() {
        sharedConfigFolder.resolve(getName() + LOCAL_CONFIG_EXT).delete();
        return this;
    }

    public LocalTomcatConfigService setConfig(LocalTomcatConfig config) {
        this.config = config;
        return this;
    }

    public LocalTomcatConfigService save() {
        String v = getConfig().getCatalinaVersion();
        if (v == null) {
            v = getValidCatalinaVersion();
            if (v != null) {
                getConfig().setCatalinaVersion(v);
            }
        }
        NPath f = getConfigPath();
        NSession session = getSession();
        NElements.of(session).json().setValue(config).print(f);
        return this;
    }

    public NPath getConfigPath() {
        return sharedConfigFolder.resolve(getName() + LOCAL_CONFIG_EXT);
    }

    public boolean existsConfig() {
        return getConfigPath().exists();
    }

    public String getRequestedCatalinaVersion() {
        LocalTomcatConfig c = getConfig();
        return c.getCatalinaVersion();
    }

    public String getValidCatalinaVersion() {
        String v = getConfig().getCatalinaVersion();
        if (!NBlankable.isBlank(v)) {
            return v;
        }
        v = TomcatUtils.getFolderCatalinaHomeVersion(getCatalinaHome());
        if (v != null) {
            return v;
        }
        //last case
        NDefinition nf = getCatalinaNutsDefinition();
        return nf.getId().getVersion().toString();
    }

    public NPath getCatalinaBase() {
        LocalTomcatConfig c = getConfig();
        NPath catalinaBase = c.getCatalinaBase() == null ? null : NPath.of(c.getCatalinaBase(), getSession());
        NPath catalinaHome = getCatalinaHome();
        if (NBlankable.isBlank(getConfig().getCatalinaHome())
                && catalinaBase == null) {
            catalinaBase = NPath.of(getName(), getSession());
        }
        if (catalinaBase == null) {
            String v = getValidCatalinaVersion();
            int x1 = v.indexOf('.');
            int x2 = x1 < 0 ? -1 : v.indexOf('.', x1 + 1);
            if (x2 > 0) {
                v = v.substring(0, x2);
            }
            catalinaBase = session.getAppSharedConfFolder().resolve("catalina-base-" + v).resolve("default");
        } else {
            if (!catalinaBase.isAbsolute()) {
                String v = getValidCatalinaVersion();
                int x1 = v.indexOf('.');
                int x2 = x1 < 0 ? -1 : v.indexOf('.', x1 + 1);
                if (x2 > 0) {
                    v = v.substring(0, x2);
                }
                catalinaBase = session.getAppSharedConfFolder().resolve("catalina-base-" + v).resolve(catalinaBase);
            }
        }
        return catalinaBase;
    }

    public NPath resolveCatalinaHome() {
        NDefinition f = getCatalinaNutsDefinition();
        NPath u = f.getInstallInformation().get(session).getInstallFolder();
        NPath[] paths;
        try {
            paths = u.stream().filter(NPath::isDirectory, "isDirectory").toArray(NPath[]::new);
            if (paths.length == 1 && paths[0].getName().toLowerCase().startsWith("apache-tomcat")) {
                return paths[0];
            }
        } catch (Exception e) {
            //
        }
        return u;
    }

    public NPath getCatalinaHome() {
        String h = getConfig().getCatalinaHome();
        if (NBlankable.isBlank(h)) {
            NPath h2 = resolveCatalinaHome();
            getConfig().setCatalinaHome(h2.toString());
            save();
            return h2;
        }
        return NPath.of(h, getSession());
    }

    public NString getFormattedError(String str) {
        return NTexts.of(getSession()).ofStyled(str, NTextStyle.error());
    }

    public NString getFormattedSuccess(String str) {
        return NTexts.of(getSession()).ofStyled(str, NTextStyle.success());
    }

    public NString getFormattedPrefix(String str) {
        return NTexts.of(getSession()).ofBuilder()
                .append("[")
                .append(str, NTextStyle.primary5())
                .append("]");
    }

    public void printStatus() {
        if (getSession().isPlainOut()) {
            switch (getStatus()) {
                case RUNNING: {
                    getSession().out().print(NMsg.ofC("%s Tomcat %s.\n", getFormattedPrefix(getName()), getFormattedSuccess("running")));
                    break;
                }
                case STOPPED: {
                    getSession().out().print(NMsg.ofC("%s Tomcat %s.\n", getFormattedPrefix(getName()), getFormattedError("stopped")));
                    break;
                }
                case OUT_OF_MEMORY: {
                    getSession().out().print(NMsg.ofC("%s Tomcat %s.\n", getFormattedPrefix(getName()), getFormattedError("out-of-memory")));
                    break;
                }
            }
        } else {
            HashMap<String, String> r = new HashMap<>();
            r.put("config-name", getName());
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
            object().setValue(r).println();
        }
    }

    public NObjectFormat object() {
        return NObjectFormat.of(getSession());
    }

    public String[] parseApps(String[] args) {
        List<String> apps = new ArrayList<>();
        if (args != null) {
            for (String arg : args) {
                if (!NBlankable.isBlank(arg)) {
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
        NPath catalinaHome = getCatalinaHome();
        NPath catalinaBase = getCatalinaBase();
        boolean catalinaBaseUpdated = false;
        catalinaBaseUpdated |= mkdirs(catalinaBase);
        String ext = NEnvs.of(getSession()).getOsFamily() == NOsFamily.WINDOWS ? "bat" : "sh";
        catalinaBaseUpdated |= checkExec(catalinaHome.resolve("bin").resolve("catalina." + ext));
        LocalTomcatConfig c = getConfig();
        catalinaBaseUpdated |= mkdirs(catalinaBase.resolve("logs"));
        catalinaBaseUpdated |= mkdirs(catalinaBase.resolve("temp"));
        catalinaBaseUpdated |= mkdirs(catalinaBase.resolve("conf"));
        if (catalinaHome.resolve("conf").isDirectory()) {
            for (NPath conf : catalinaHome.resolve("conf").stream()) {
                NPath confFile = catalinaBase.resolve("conf/" + conf.getName());
                if (!confFile.exists()) {
                    catalinaBaseUpdated = true;
                    NCp.of(getSession())
                            .from(conf).to(confFile)
                            .run();
                }
            }
        }
        if (c.isDev()) {
            if (catalinaHome.resolve("webapps").resolve("host-manager").isDirectory()) {
                NCp.of(getSession())
                        .from(catalinaHome.resolve("webapps").resolve("host-manager"))
                        .to(catalinaBase.resolve("webapps").resolve("host-manager"))
                        .removeOptions(NPathOption.REPLACE_EXISTING)
                        .run();
                catalinaBaseUpdated = true;
            }
            if (catalinaHome.resolve("webapps").resolve("manager").isDirectory()) {
                NCp.of(getSession())
                        .from(catalinaHome.resolve("webapps").resolve("manager"))
                        .to(catalinaBase.resolve("webapps").resolve("manager"))
                        .removeOptions(NPathOption.REPLACE_EXISTING)
                        .run();
                catalinaBaseUpdated = true;
            }
        } else {
            if (catalinaBase.resolve("webapps").resolve("host-manager").isDirectory()) {
                catalinaBase.resolve("webapps").resolve("host-manager").deleteTree();
            }
            if (catalinaBase.resolve("webapps").resolve("manager").isDirectory()) {
                catalinaBase.resolve("webapps").resolve("manager").deleteTree();
            }
            catalinaBaseUpdated = true;
        }
        if (catalinaBaseUpdated) {
            if (getSession().isPlainOut()) {
                getSession().out().print(NMsg.ofC("%s updated catalina base %s\n", getFormattedPrefix(getName()), catalinaBase));
            }
            return true;
        }
        return false;
    }

    public NExecCommand invokeCatalina(String catalinaCommand) {
        buildCatalinaBase();
        NPath catalinaHome = getCatalinaHome();
        NPath catalinaBase = getCatalinaBase();
        NSession session = getSession();
        String ext = NEnvs.of(session).getOsFamily() == NOsFamily.WINDOWS ? "bat" : "sh";

        //b.
//        b.setOutput(context.getSession().out());
//        b.setErr(context.getSession().err());
        NExecCommand b = NExecCommand.of(session)
                .setExecutionType(NExecutionType.SYSTEM).setSession(session);
        b.addCommand(catalinaHome + "/bin/catalina." + ext);
        b.addCommand(catalinaCommand);
//        if (catalinaHome != null) {
//            b.addCommand("-Dcatalina.home=" + catalinaHome);
//        }
//        b.addCommand("-Dcatalina.base=" + catalinaBase);
        b.setDirectory(catalinaBase);
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

        NElements elem = NElements.of(session);
        if ("start".equals(catalinaCommand)) {
            if (session.isPlainOut()) {
                session.out().print(NMsg.ofC("%s starting Tomcat on port " + getHttpConnectorPort() + ". CMD=%s.\n", getFormattedPrefix(getName()), b.toString()));
                b.getResult();
            } else {
                b.grabOutputString();
                int x = b.getResult();
                String txt = b.getOutputString();
                session.eout().add(
                        elem.ofObject()
                                .set("command", "catalina-start")
                                .set("result-code", x)
                                .set("catalina-out", txt)
                                .build()
                );
            }
        } else if ("stop".equals(catalinaCommand)) {
            if (session.isPlainOut()) {
                session.out().print(NMsg.ofC("%s stopping Tomcat. CMD=%s.\n", getFormattedPrefix(getName()), b.toString()));
                b.getResult();
            } else {
                b.grabOutputString();
                int x = b.getResult();
                String txt = b.getOutputString();
                session.eout().add(
                        elem.ofObject()
                                .set("command", "catalina-stop")
                                .set("result-code", x)
                                .set("catalina-out", txt)
                                .build()
                );
            }
        }

        return b;
    }

    private boolean mkdirs(NPath catalinaBase) {
        if (!catalinaBase.isDirectory()) {
            catalinaBase.mkdirs();
            return true;
        }
        return false;
    }

    public boolean start(String[] deployApps, boolean deleteLog) {
        LocalTomcatConfig c = getConfig();
        RunningTomcat jpsResult = getRunningTomcat();
        if (jpsResult != null) {
            NSession session = getSession();
            if (session.isPlainOut()) {
                session.out().print(NMsg.ofC("%s Tomcat already started on port " + getHttpConnectorPort() + ".\n", getFormattedPrefix(getName())));
            } else {
                session.eout().add(
                        NElements.of(session).ofObject()
                                .set("config-name", getName())
                                .set("command", "start")
                                .set("result", "already-started")
                                .build()
                );
            }
            return false;
        }
        for (String app : new HashSet<String>(Arrays.asList(parseApps(deployApps)))) {
            getApp(app, NOpenMode.OPEN_OR_ERROR).deploy(null);
        }
        if (deleteLog) {
            deleteOutLog();
        }

        NExecCommand b = invokeCatalina("start");
//        try {
//            b.waitFor();
//        } catch (IOException ex) {
//            throw new UncheckedIOException(ex);
//        }
        waitForRunningStatus(null, null, c.getStartupWaitTime());
        return true;
    }

    private NDefinition getCatalinaNutsDefinition() {
        String catalinaVersion = getRequestedCatalinaVersion();
        if (catalinaVersion == null) {
            catalinaVersion = "";
        }
        catalinaVersion = catalinaVersion.trim();
        NSession session = getSession();
        if (catalinaVersion.isEmpty()) {
            NVersion javaVersion = NEnvs.of(session).getPlatform().getVersion();
            //  http://tomcat.apache.org/whichversion.html
            if (javaVersion.compareTo("1.8") >= 0) {
                catalinaVersion = "[9,10.1[";
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
        if (catalinaNDefinition == null || !Objects.equals(catalinaVersion, this.catalinaVersion)
                || !catalinaNDefinition.getInstallInformation().get(session).getInstallStatus().isInstalled()) {
            this.catalinaVersion = catalinaVersion;
            String cv = catalinaVersion;
            if (!cv.startsWith("[") && !cv.startsWith("]")) {
                cv = "[" + catalinaVersion + "," + catalinaVersion + ".99999]";
            }
            String cid="org.apache.catalina:apache-tomcat";//+"#"+cv;
            cid= NIdBuilder.of().setAll(NId.of(cid).get()).setCondition(
                    new DefaultNEnvConditionBuilder()
                            .addPlatform(NEnvs.of(session).getPlatform().toString())
            ).toString();

            NSearchCommand searchLatestCommand = NSearchCommand.of(session).addId(cid)
                    .setLatest(true);
            NDefinition r = searchLatestCommand
                    .setInstallStatus(NInstallStatusFilters.of(session).byDeployed(true))
                    .getResultDefinitions().first();
            if (r == null) {
                r = searchLatestCommand.setInstallStatus(NInstallStatusFilters.of(session).byInstalled(false))
                        .setSession(searchLatestCommand.getSession().copy().setFetchStrategy(NFetchStrategy.OFFLINE))
                        .getResultDefinitions().first();
            }
            if (r == null) {
                r = searchLatestCommand
                        .setSession(searchLatestCommand.getSession().copy().setFetchStrategy(NFetchStrategy.ONLINE))
                        .setInstallStatus(NInstallStatusFilters.of(session).byInstalled(false)).getResultDefinitions().required();
            }
            if (r.getInstallInformation().get(session).isInstalledOrRequired()) {
                return r;
            } else {
                //TODO: FIX install return
                catalinaNDefinition = NInstallCommand.of(session)
                        .addId(r.getId())
                        .setSession(getSession().copy().addListener(new NInstallListener() {
                            @Override
                            public void onInstall(NInstallEvent event) {
                                if (getSession().isPlainOut()) {
                                    getSession().out().print(NMsg.ofC("%s Tomcat installed to catalina home %s\n", getFormattedPrefix(getName()),
                                            event.getDefinition().getInstallInformation().get(session).getInstallFolder()
                                    ));
                                }
                            }
                        })).getResult().required();
                //this is a workaround. Def returned by install does not include all information!
                catalinaNDefinition = searchLatestCommand.setInstallStatus(NInstallStatusFilters.of(session).byInstalled(true))
                        .getResultDefinitions().first();
            }
        }
        return catalinaNDefinition;
    }

    public void deployFile(NPath file, String contextName, String domain) {
        String fileName = file.getName().toString();
        if (fileName.endsWith(".war")) {
            if (NBlankable.isBlank(contextName)) {
                contextName = fileName.substring(0, fileName.length() - ".war".length());
            }
            NPath c = getDefaulDeployFolder(domain).resolve(contextName + ".war");
            if (getSession().isPlainOut()) {
                getSession().out().print(NMsg.ofC("%s deploy file file %s to %s.\n", getFormattedPrefix(getName()), file, c));
            }
            NCp.of(getSession()).from(file).to(c).run();
        } else {
            throw new RuntimeException("expected war file");
        }
    }

    public boolean stop() {
        if (getRunningTomcat() == null) {
            if (getSession().isPlainOut()) {
                getSession().out().print(NMsg.ofC("%s Tomcat already stopped.\n", getFormattedPrefix(getName())));
            }
            return false;
        }
        LocalTomcatConfig c = getConfig();
        NExecCommand b = invokeCatalina("stop");
        return waitForStoppedStatus(c.getShutdownWaitTime(), c.isKill());
    }

    public RunningTomcat getRunningTomcat() {
        NPath catalinaBase = getCatalinaBase();
        return Arrays.stream(TomcatUtils.getRunningInstances(session))
                .filter(p -> (catalinaBase == null
                        || catalinaBase.toString().equals(p.getBase())))
                .findFirst().orElse(null);
    }

    private boolean checkExec(NPath pathname) {
        if (!pathname.getPermissions().contains(NPathPermission.CAN_EXECUTE)) {
            pathname.addPermissions(NPathPermission.CAN_EXECUTE);
            return true;
        }
        return false;
    }

    public boolean restart() {
        return restart(null, false);
    }

    public boolean restart(String[] deployApps, boolean deleteLog) {
        stop();
        if (getRunningTomcat() != null) {
            throw new NExecutionException(getSession(), NMsg.ofC("server %s is running. it cannot be stopped!", getName()), 2);
        }
        start(deployApps, deleteLog);
        return true;
    }

    public AppStatus waitForRunningStatus(String domain, String app, int timeout) {
        long startTime = System.currentTimeMillis();
        AppStatus y = getStatus(domain, app);
        NSession session = getSession();
        NElements elem = NElements.of(session);
        if (y == AppStatus.RUNNING) {
            if (session.isPlainOut()) {
                session.out().print(NMsg.ofC("%s Tomcat started on port " + getHttpConnectorPort() + ".\n", getFormattedPrefix(getName())));
            } else {
                session.eout().add(elem
                        .ofObject()
                        .set("command", "wait-for-running")
                        .set("time", 0)
                        .set("result", "success")
                        .build()
                );
            }
            return y;
        }
        if (timeout <= 0) {
            RunningTomcat ps = getRunningTomcat();
            if (ps != null) {
                if (session.isPlainOut()) {
                    session.out().print(NMsg.ofC("%s Tomcat started on port" + getHttpConnectorPort() + " .\n", getFormattedPrefix(getName())));
                    return AppStatus.RUNNING;
                } else {
                    session.eout().add(elem
                            .ofObject()
                            .set("command", "wait-for-running")
                            .set("time", 0)
                            .set("result", "success")
                            .build()
                    );
                }
            } else {
                if (!session.isPlainOut()) {
                    session.eout().add(elem
                            .ofObject()
                            .set("command", "wait-for-running")
                            .set("time", 0)
                            .set("result", "fail")
                            .build()
                    );
                }
            }
            throw new NExecutionException(session, NMsg.ofPlain("unable to start tomcat"), 2);
        }
        for (int i = 0; i < timeout; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
            y = getStatus(domain, app);
            if (y == AppStatus.RUNNING) {
                if (session.isPlainOut()) {
                    session.out().print(NMsg.ofC("%s Tomcat started on port " + getHttpConnectorPort() + ".\n", getFormattedPrefix(getName())));
                } else {
                    session.eout().add(elem
                            .ofObject()
                            .set("command", "wait-for-running")
                            .set("config-name", getName())
                            .set("http-connector-port", getHttpConnectorPort())
                            .set("time", System.currentTimeMillis() - startTime)
                            .set("result", "success")
                            .build()
                    );
                }
                return y;
            }
        }
        if (y == AppStatus.OUT_OF_MEMORY) {
            if (session.isPlainOut()) {
                session.out().print(NMsg.ofC("%s Tomcat out of memory.\n", getFormattedPrefix(getName())));
            } else {
                session.eout().add(elem
                        .ofObject()
                        .set("command", "wait-for-running")
                        .set("config-name", getName())
                        .set("http-connector-port", getHttpConnectorPort())
                        .set("time", System.currentTimeMillis() - startTime)
                        .set("result", "out-of-memory")
                        .build()
                );
            }
            return y;
        }
        throw new NExecutionException(session, NMsg.ofPlain("unable to start tomcat"), 2);
    }

    public boolean waitForStoppedStatus(int timeout, boolean kill) {
        long startTime = System.currentTimeMillis();
        RunningTomcat ps = getRunningTomcat();
        NSession session = getSession();
        if (ps == null) {
            if (session.isPlainOut()) {
                session.out().print(NMsg.ofC("%s Tomcat stopped.\n", getFormattedPrefix(getName())));
            } else {
                session.eout().add(NElements.of(session)
                        .ofObject()
                        .set("command", "wait-for-stopped")
                        .set("config-name", getName())
                        .set("http-connector-port", getHttpConnectorPort())
                        .set("time", 0)
                        .set("result", "stopped")
                        .build()
                );
            }
            return true;
        }
        for (int i = 0; i < timeout; i++) {
            session.out().print(NMsg.ofC("%s waiting Tomcat process to die.\n", getFormattedPrefix(getName())));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
            ps = getRunningTomcat();
            if (ps == null) {
                if (session.isPlainOut()) {
                    session.out().print(NMsg.ofC("%s Tomcat stopped.\n", getFormattedPrefix(getName())));
                } else {
                    session.eout().add(NElements.of(session)
                            .ofObject()
                            .set("command", "wait-for-stopped")
                            .set("config-name", getName())
                            .set("http-connector-port", getHttpConnectorPort())
                            .set("time", System.currentTimeMillis() - startTime)
                            .set("result", "stopped")
                            .build()
                    );
                }
                return true;
            }
        }
        if (kill && NPs.of(session).isSupportedKillProcess()) {

            ps = getRunningTomcat();
            if (ps != null) {

                if (NPs.of(session).killProcess(ps.getPid())) {
                    if (session.isPlainOut()) {
                        session.out().print(NMsg.ofC("%s Tomcat process killed (%s).\n", getFormattedPrefix(getName()), ps.getPid()));
                    } else {
                        session.eout().add(NElements.of(session)
                                .ofObject()
                                .set("command", "wait-for-stopped")
                                .set("config-name", getName())
                                .set("http-connector-port", getHttpConnectorPort())
                                .set("time", System.currentTimeMillis() - startTime)
                                .set("pid", ps.getPid())
                                .set("result", "killed")
                                .build()
                        );
                    }
                    return true;
                } else {
                    if (session.isPlainOut()) {
                        session.out().print(NMsg.ofC("%s Tomcat process could not be killed ( %s).\n", getFormattedPrefix(getName()), ps.getPid()));
                    } else {
                        session.eout().add(NElements.of(session)
                                .ofObject()
                                .set("command", "wait-for-stopped")
                                .set("config-name", getName())
                                .set("http-connector-port", getHttpConnectorPort())
                                .set("time", System.currentTimeMillis() - startTime)
                                .set("pid", ps.getPid())
                                .set("result", "unable-to-kill")
                                .build()
                        );
                    }
                    return false;
                }

            }
        }
        ps = getRunningTomcat();
        if (ps != null) {
            if (session.isPlainOut()) {
                session.out().print(NMsg.ofC("%s Tomcat process could not be terminated (%s).\n", getFormattedPrefix(getName()), ps.getPid()));
            } else {
                session.eout().add(NElements.of(session)
                        .ofObject()
                        .set("command", "wait-for-stopped")
                        .set("config-name", getName())
                        .set("http-connector-port", getHttpConnectorPort())
                        .set("time", System.currentTimeMillis() - startTime)
                        .set("pid", ps.getPid())
                        .set("result", "unable-to-stop")
                        .build()
                );
            }
            return true;
        }
        if (session.isPlainOut()) {
            session.out().print(NMsg.ofC("%s\n", getFormattedError("Tomcat stopped")));
        } else {
            session.eout().add(NElements.of(session)
                    .ofObject()
                    .set("command", "wait-for-stopped")
                    .set("config-name", getName())
                    .set("http-connector-port", getHttpConnectorPort())
                    .set("time", System.currentTimeMillis() - startTime)
                    .set("result", "stopped")
                    .build()
            );
        }
        return true;
    }

    public AppStatus getStatus() {
        return getStatus(null, null);
    }

    public AppStatus getStatus(String domain, String app) {
        LocalTomcatConfig c = getConfig();
        NPath catalinaBase = getCatalinaBase();
        RunningTomcat ps = getRunningTomcat();
        if (ps != null) {
            String startupMessage = null;
            String shutdownMessage = null;
            String logFile = null;
            if (app != null) {
                LocalTomcatAppConfigService a = getApp(app, NOpenMode.OPEN_OR_ERROR);
                domain = a.getConfig().getDomain();
                startupMessage = a.getConfig().getStartupMessage();
                shutdownMessage = a.getConfig().getShutdownMessage();
            }
            if (domain != null) {
                LocalTomcatDomainConfigService tomcatDomain = getDomain(domain, NOpenMode.OPEN_OR_ERROR);
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

            NPath log = catalinaBase.resolve(logFile);
            if (!log.exists()) {
                return AppStatus.STOPPED;
            }
            LocalTomcatLogLineVisitor visitor = new LocalTomcatLogLineVisitor(log.toString(), startupMessage, shutdownMessage, getSession());
            visitor.visit();
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
        NPath f = sharedConfigFolder.resolve(name + LOCAL_CONFIG_EXT);
        if (f.exists()) {
            NSession session = getSession();
            config = NElements.of(session).json().parse(f, LocalTomcatConfig.class);
            return this;
//        } else if ("default".equals(name)) {
//            //auto create default config
//            config = new LocalTomcatConfig();
//            save();
//            return this;
        }
        throw new NamedItemNotFoundException("Instance not found : " + getName(), getName());
    }

    public LocalTomcatAppConfigService getApp(String appName, NOpenMode mode) {
        appName = TomcatUtils.toValidFileName(appName, "default");
        LocalTomcatAppConfig a = getConfig().getApps().get(appName);
        if (mode == null) {
            if (a == null) {
                return null;
            }
        } else {
            switch (mode) {
                case OPEN_OR_ERROR: {
                    if (a == null) {
                        throw new NExecutionException(getSession(), NMsg.ofC("app not found : %s", appName), 2);
                    }
                    break;
                }
                case CREATE_OR_ERROR: {
                    if (a == null) {
                        a = new LocalTomcatAppConfig();
                        getConfig().getApps().put(appName, a);
                    } else {
                        throw new NExecutionException(getSession(), NMsg.ofC("app already found : %s", appName), 2);
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

    public LocalTomcatDomainConfigService getDomain(String domainName, NOpenMode mode) {
        domainName = TomcatUtils.toValidFileName(domainName, "");
        LocalTomcatDomainConfig a = getConfig().getDomains().get(domainName);
        if (mode == null) {
            if (a == null) {
                return null;
            }
        } else {
            switch (mode) {
                case OPEN_OR_ERROR: {
                    if (a == null) {
                        throw new NExecutionException(getSession(), NMsg.ofC("domain not found : %s", domainName), 2);
                    }
                    break;
                }
                case CREATE_OR_ERROR: {
                    if (a == null) {
                        a = new LocalTomcatDomainConfig();
                        getConfig().getDomains().put(domainName, a);
                    } else {
                        throw new NExecutionException(getSession(), NMsg.ofC("domain already found : %s", domainName), 2);
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

    public NPath getLogFolder() {
        return getCatalinaBase().resolve("logs");
    }

    public NPath getTempFolder() {
        return getCatalinaBase().resolve("temp");
    }

    public NPath getWorkFolder() {
        return getCatalinaBase().resolve("work");
    }

    public void deleteOutLog() {
        //get it from config?
        NPath file = getLogFolder().resolve("catalina.out");
        if (file.isRegularFile()) {
            if (getSession().isPlainOut()) {
                getSession().out().print(NMsg.ofC("%s delete log file %s.\n", getFormattedPrefix(getName()), file));
            }
            file.delete();
        }
    }

    public void deleteTemp() {
        NPath tempFolder = getTempFolder();
        if (tempFolder.isDirectory()) {
            tempFolder.stream()
                    .forEach(file -> {
                        if (getSession().isPlainOut()) {
                            getSession().out().print(NMsg.ofC("%s delete temp file %s.\n", getFormattedPrefix(getName()), file));
                        }
                        file.delete();
                    });
        }
    }

    public void deleteWork() {
        NPath workFolder = getWorkFolder();
        if (workFolder.isDirectory()) {
            workFolder.stream()
                    .forEach(file -> {
                        if (getSession().isPlainOut()) {
                            getSession().out().print(NMsg.ofC("%s delete work file %s.\n", getFormattedPrefix(getName()), file));
                        }
                        file.delete();
                    });
        }
    }

    public NPath getOutLogFile() {
        return getLogFolder().resolve("catalina.out");
    }

    public void showOutLog(int tail) {
        //get it from config?
        NPath file = getOutLogFile();
        NSession session = getSession();
        if (tail <= 0) {
            NCp.of(session).from(file).to(session.out()).run();
            return;
        }
        if (file.isRegularFile()) {
            for (String line : file.tail(tail)) {
                session.out().println(line);
            }
        }
    }

    public void deleteAllLog() {
        NPath logFolder = getLogFolder();
        if (logFolder.isDirectory()) {
            logFolder.stream()
                    .forEach(file -> {
                        if (file.isRegularFile()) {
                            String n = file.getName();
                            if (n.endsWith(".out")
                                    || n.endsWith(".txt")
                                    || n.endsWith(".log")) {
                                //this is a log file, will delete it
                                if (getSession().isPlainOut()) {
                                    getSession().out().print(NMsg.ofC("%s delete log file %s.\n", getFormattedPrefix(getName()), file));
                                }
                                file.delete();
                            }
                        }
                    });
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

    public NPath getDefaulDeployFolder(String domainName) {
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

    public NSession getSession() {
        return session;
    }

    public Integer getShutdownPort() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            NPath serverXml = getCatalinaBase().resolve("conf").resolve("server.xml");
            if (serverXml.exists()) {
                Document doc = docBuilder.parse(serverXml.toFile().toFile());
                Element root = doc.getDocumentElement();
                String port = root.getAttribute("port");
                return port == null ? null : Integer.parseInt(port);
            }
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            if (getSession().isPlainOut()) {
                getSession().err().println("```error ERROR:``` : " + ex);
            }
        }
        //
        return null;
    }

    public void setShutdownPort(int port) {
        try {
            if (port <= 0) {
                port = 8005;
            }
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            NPath serverXml = getCatalinaBase().resolve("conf").resolve("server.xml");
            if (serverXml.exists()) {
                Document doc = docBuilder.parse(serverXml.toFile().toFile());
                Element root = doc.getDocumentElement();
                String p = root.getAttribute("port");
                if (String.valueOf(port).equals(p)) {
                    return;
                }
                root.setAttribute("port", String.valueOf(port));
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource domSource = new DOMSource(doc);
                StreamResult streamResult = new StreamResult(serverXml.toFile().toFile());
                transformer.transform(domSource, streamResult);
            }
        } catch (SAXException | IOException | ParserConfigurationException | TransformerException ex) {
            if (getSession().isPlainOut()) {
                getSession().err().println("```error ERROR:``` : " + ex);
            }
            //
        }
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
            NPath serverXml = getCatalinaBase().resolve("conf").resolve("server.xml");
            if (serverXml.exists()) {
                Document doc = docBuilder.parse(serverXml.toFile().toFile());
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
            if (getSession().isPlainOut()) {
                getSession().err().println("```error ERROR:``` : " + ex);
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
            NPath serverXml = getCatalinaBase().resolve("conf").resolve("server.xml");
            if (serverXml.exists()) {
                Document doc = docBuilder.parse(serverXml.toFile().toFile());
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
                    StreamResult streamResult = new StreamResult(serverXml.toFile().toFile());
                    transformer.transform(domSource, streamResult);
                    return;
                }
                throw new NIllegalArgumentException(getSession(), NMsg.ofPlain("not found connector"));
            }
        } catch (SAXException | IOException | ParserConfigurationException | TransformerException ex) {
            if (getSession().isPlainOut()) {
                getSession().err().println((NMsg.ofC("```error ERROR:``` : %s", ex)));
            }
            //
        }
    }

}
