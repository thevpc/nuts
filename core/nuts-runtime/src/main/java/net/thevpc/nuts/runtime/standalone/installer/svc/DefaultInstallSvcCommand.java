package net.thevpc.nuts.runtime.standalone.installer.svc;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NExecutionException;
import net.thevpc.nuts.NInstallSvcCmd;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.env.NOsServiceType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathPermission;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultInstallSvcCommand implements NInstallSvcCmd {
    private NOsServiceType systemServiceType;
    private NOsServiceType serviceType;
    private String serviceName;
    private NPath root;
    private NPath workingDirectory;
    private NSession session;
    private boolean verbose;
    private String[] startCommand;
    private String[] stopCommand;
    private String[] statusCommand;
    private DefaultMapper vars = new DefaultMapper(this);
    private Map<String, String> env;
    private String nutsApiVersion = "0.8.4";
    private String serviceDescription = "System service";

    public DefaultInstallSvcCommand(NSession session) {
        this.session = session;
    }

    public DefaultInstallSvcCommand setServiceType(NOsServiceType serviceType) {
        this.serviceType = serviceType;
        return this;
    }


    public NOsServiceType getServiceType() {
        return serviceType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public NInstallSvcCmd setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public NPath getRoot() {
        return root;
    }

    public NInstallSvcCmd setRootDirectory(NPath root) {
        this.root = root;
        return this;
    }

    @Override
    public NPath getWorkingDirectory() {
        return workingDirectory;
    }

    public NInstallSvcCmd setWorkingDirectory(NPath workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public NInstallSvcCmd setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public NInstallSvcCmd setControlCommand(String[] startCommand) {
        List<String> base0 = new ArrayList<>(Arrays.asList(startCommand));

        List<String> base = new ArrayList<>(base0);
        base.add("start");
        setStartCommand(base.toArray(new String[0]));

        base = new ArrayList<>(base0);
        base.add("stop");
        setStopCommand(base.toArray(new String[0]));

        base = new ArrayList<>(base0);
        base.add("status");
        setStatusCommand(base.toArray(new String[0]));

        return this;
    }

    @Override
    public Map<String, String> getEnv() {
        return env;
    }

    public NInstallSvcCmd setEnv(Map<String, String> env) {
        this.env = env;
        return this;
    }

    @Override
    public String[] getStartCommand() {
        return startCommand;
    }

    @Override
    public DefaultInstallSvcCommand setStartCommand(String[] startCommand) {
        this.startCommand = startCommand;
        return this;
    }

    @Override
    public String[] getStopCommand() {
        return stopCommand;
    }

    @Override
    public DefaultInstallSvcCommand setStopCommand(String[] stopCommand) {
        this.stopCommand = stopCommand;
        return this;
    }

    @Override
    public String[] getStatusCommand() {
        return statusCommand;
    }

    @Override
    public DefaultInstallSvcCommand setStatusCommand(String[] statusCommand) {
        this.statusCommand = statusCommand;
        return this;
    }

    public boolean uninstall() {
        switch (getActualServiceType()) {
            case SYSTEMD: {
                String serviceFilePath = "/etc/systemd/system/" + serviceName + ".service";
                String dir = getCurrentWorkingDir().toString();
                String javaHome = System.getProperty("java.home");
                logInfo("");
                logInfo("== UNINSTALLING SERVICE SCRIPT ==");
                logInfo("install service    : " + serviceName);
                logInfo("working-dir        : " + dir);
                logInfo("java-home          : " + javaHome);
                logInfo("service-file       : " + serviceFilePath);
                logInfo("trying to remove service file: " + serviceFilePath);
                logInfo("ATTENTION: please ensure that the service is not running (sudo systemctl status " + serviceName + ")");
                logInfo("We need root privileges to run de-installation script. Please enter your root password.");
                runAsRoot(
                        new ScriptBuilder("uninstall-" + serviceName, "uninstall-" + serviceName)
                                .printlnEcho("systemctl stop " + serviceName)
                                .printlnEcho("rm -Rf " + serviceFilePath)
                );
                logInfoSuccess(serviceName + " uninstalled successfully.");
                return true;
            }
            case INITD: {
                String serviceFilePath = "/etc/init.d/" + serviceName;
                String dir = getCurrentWorkingDir().toString();
                String javaHome = System.getProperty("java.home");
                logInfo("");
                logInfo("== UNINSTALLING SERVICE SCRIPT ==");
                logInfo("install service    : " + serviceName);
                logInfo("working-dir        : " + dir);
                logInfo("java-home          : " + javaHome);
                logInfo("service-file       : " + serviceFilePath);
                logInfo("trying to remove service file: " + serviceFilePath);
                logInfo("ATTENTION: please ensure that the service is not running (" + "sudo " + serviceFilePath + " stop " + ")");
                logInfo("We need root privileges to run de-installation script. Please enter your root password.");
                runAsRoot(
                        new ScriptBuilder("uninstall-" + serviceName, "uninstall-" + serviceName)
                                .printlnEcho("sudo " + serviceFilePath + " stop")
                                .printlnEcho("rm -Rf " + serviceFilePath)
                );
                logInfoSuccess(serviceName + " uninstalled successfully.");
                return true;
            }
            default: {
                logError("Install service " + serviceName + " Failed");
                logError("Services are not supported on this platform. systemctl command is not available. Ignoring service installation");
                return false;
            }
        }
    }

    public boolean install() {
        logInfoStart("Installing System Service...");
//        if (!isValidInstallDir(null)) {
//            logError("Invalid installation at " + getCurrentWorkingDir());
//            System.exit(1);
//            return false;
//        }
//        checkValidInstallDir(null);
        switch (getActualServiceType()) {
            case SYSTEMD: {
                return installServiceSystemD();
            }
            case INITD: {
                return installServiceInitd();
            }
            default: {
                logError("Services are not supported on this platform. systemctl command is not available. Ignoring service installation");
                logInfo("use:");
                logInfo(formatCommand(startCommand));
                logInfo("      to start the service");
                logInfo(formatCommand(stopCommand));
                logInfo("      to stop the service");
                logInfo(formatCommand(statusCommand));
                logInfo("      to check the service status");
                return false;
            }
        }
    }

    private NPath getCurrentWorkingDir() {
        if (workingDirectory == null) {
            return NPath.ofUserDirectory(session);
        }
        return workingDirectory;
    }

    private boolean installServiceSystemD() {
        String serviceFilePath = rootFile("/etc/systemd/system/" + serviceName + ".service").toString();
        String dir = getCurrentWorkingDir().toString();
        String javaHome = System.getProperty("java.home");
        logInfo("");
        logInfo("== INSTALLING SERVICE SCRIPT ==");
        logInfo("service type       : " + NOsServiceType.SYSTEMD.name().toLowerCase());
        logInfo("install service    : " + serviceName);
        logInfo("working-dir        : " + dir);
        logInfo("java-home          : " + javaHome);
        logInfo("service-file       : " + serviceFilePath);

        File tempFile = null;
        try {
            logInfoStart("Creating systemd service file : " + serviceFilePath);
            tempFile = File.createTempFile("srv-" + serviceName, ".service");
            createFileFromTemplate("service-systemd", tempFile.toString());
            ScriptBuilder script = new ScriptBuilder(serviceName, "systemctl enable/start " + serviceName + " script")
                    .printlnEcho("cp " + tempFile.getPath() + " " + serviceFilePath);
            if (!isRootOverridden()) {
                script.printlnEcho("systemctl daemon-reload")
                        .printlnEcho("systemctl enable " + serviceName)
                        .printlnEcho("systemctl stop " + serviceName)
                        .printlnEcho("systemctl start " + serviceName)
                        .printlnEcho("systemctl status " + serviceName);
            }
            if (session.isDry()) {
                new File(serviceFilePath).getParentFile().mkdirs();
                Files.copy(tempFile.toPath(), new File(serviceFilePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
                logInfo("[DRY] run script: ");
                logInfo(script.toString());
            } else {
                logInfo("We need root privileges to run installation script. Please enter your root password.");
                runAsRoot(script);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
        logInfo("system controller service installed successfully.");
        logInfo("use:");
        logInfo("sudo systemctl start " + serviceName);
        logInfo("      to start the service");
        logInfo("sudo systemctl stop " + serviceName);
        logInfo("      to stop the service");
        logInfo("sudo systemctl status " + serviceName);
        logInfo("      to check the service status");
        logInfoSuccess("Service installed");
        return true;
    }

    private boolean installServiceInitd() {
        NPath serviceFilePath = rootFile("/etc/init.d/" + serviceName);
        String dir = getCurrentWorkingDir().toString();
        String javaHome = System.getProperty("java.home");
        logInfo("");
        logInfo("== INSTALLING SERVICE SCRIPT ==");
        logInfo("service type       : " + NOsServiceType.INITD.name().toLowerCase());
        logInfo("install service    : " + serviceName);
        logInfo("working-dir        : " + dir);
        logInfo("java-home          : " + javaHome);
        logInfo("service-file       : " + serviceFilePath);

        File tempFile = null;
        try {
            logInfoStart("Creating initd service file : " + serviceFilePath);
            tempFile = File.createTempFile("srv-" + serviceName, ".service");
            createFileFromTemplate("service-initd", tempFile.toString());
            NPath rcFile = rootFile("/etc/rc.d/S99z_" + serviceName);
            ScriptBuilder script = new ScriptBuilder(serviceName, "initd service enable/start " + serviceName + " script")
                    .printlnEcho("mkdir -p " + rcFile.getParent())
                    .printlnEcho("mkdir -p " + serviceFilePath.getParent())
                    .printlnEcho("cp " + tempFile.getPath() + " " + serviceFilePath)
                    .printlnEcho("rm -Rf " + rcFile)
                    .printlnEcho("ln -s " + serviceFilePath + " " + rcFile);
//                    .printlnEcho("update-rc.d "+serviceName+" defaults" + serviceFilePath)
//                    .printlnEcho("systemctl daemon-reload")
            if (!isRootOverridden()) {
                script.printlnEcho(serviceFilePath + " stop ")
//                        .printlnEcho(serviceFilePath + " start ")
//                        .printlnEcho(serviceFilePath + " status ")
                ;
            }
            // added to always return 0 code
            script.printlnEcho("echo 'end of script'");
            if (session.isDry()) {
                serviceFilePath.getParent().mkdirs();
                Files.copy(tempFile.toPath(), serviceFilePath.toPath().get(), StandardCopyOption.REPLACE_EXISTING);
                logInfo("[DRY] run script: ");
                logInfo(script.toString());
            } else {
                logInfo("We need root privileges to run installation script. Please enter your root password.");
                runAsRoot(
                        script
                );
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
        logInfo("system controller service installed successfully.");
        logInfo("use:");
        logInfo("sudo " + serviceFilePath + " start ");
        logInfo("      to start the service");
        logInfo("sudo " + serviceFilePath + " stop ");
        logInfo("      to stop the service");
        logInfo("sudo " + serviceFilePath + " status ");
        logInfo("      to check the service status");
        logInfoSuccess("Service installed");
        return true;
    }

    private void logVerbose(String msg) {
        if (verbose) {
            if (session.isTrace()) {
                session.out().println(NMsg.ofC("[DEBUG] %s", msg));
            }
        }
    }

    private void logInfoStart(String msg) {
        for (String line : SvcHelper.splitLines(msg)) {
            logInfo("[START  ] " + line);
        }
    }

    private void logInfoSuccess(String msg) {
        for (String line : SvcHelper.splitLines(msg)) {
            logInfo("[SUCCESS] " + line);
        }
    }

    private void logInfo(String msg) {
        for (String line : SvcHelper.splitLines(msg)) {
            if (session.isTrace()) {
                session.out().println(NMsg.ofC("[INFO ] %s", line));
            }
        }
    }

    private void logWarn(String msg) {
        for (String line : SvcHelper.splitLines(msg)) {
            if (session.isTrace()) {
                session.out().println(NMsg.ofC("[WARN ] %s", line));
            }
        }
    }

    private void logError(String msg) {
        for (String line : SvcHelper.splitLines(msg)) {
            if (session.isTrace()) {
                session.out().println(NMsg.ofC("[ERROR ] %s", line));
            }
        }
    }


    public NOsServiceType getActualServiceType() {
        if (serviceType != null) {
            return serviceType;
        }
        return getSystemServiceType();
    }

    @Override
    public NOsServiceType getSystemServiceType() {
        if (systemServiceType == null) {
            logVerbose("Checking if systemctl is available...");
            try {
                runSystemCommand("systemctl", "--version");
                logVerbose("[SUCCESS] found valid systemctl...");
                systemServiceType = NOsServiceType.SYSTEMD;
            } catch (Exception e) {
                //
            }
            logVerbose("[FAIL   ] systemctl not found...");
            if (systemServiceType == null) {
                logVerbose("Checking if initd is available...");
                try {
                    if (new File("/etc/init.d/").isDirectory()) {
                        logVerbose("[SUCCESS] found valid initd...");
                        systemServiceType = NOsServiceType.INITD;
                    }
                } catch (Exception e) {
                    //
                }
                logVerbose("[FAIL   ] initd not found...");
            }
            if (systemServiceType == null) {
                systemServiceType = NOsServiceType.UNSUPPORTED;
            }
        }
        return systemServiceType;
    }

    private void runSystemCommand(String... cmd) {
        logVerbose("[RUNNING COMMAND] " + formatCommand(cmd));
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.inheritIO();
        Process p = null;
        int ret = -1;
        try {
            p = processBuilder.start();
            if ((ret = p.waitFor()) == 0) {
                logVerbose("[RUNNING COMMAND] COMMAND SUCCEEDED : code " + ret);
                return;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new UncheckedIOException(new IOException(e));
        }
        logVerbose("[RUNNING COMMAND] COMMAND FAILED : code " + ret);
        throw new NExecutionException(session, NMsg.ofC("run command returned %s", ret), ret);
    }

    private String formatCommand(String[] cmd) {
        return String.join(" ", cmd);
    }

    private void createFileFromTemplate(String resource0, String file) {
        String resource = "/net/thevpc/nuts/runtime/svc/" + resource0;
        logVerbose("[FILE] CREATE FILE " + resource);
        String lineSeparator = System.getProperty("line.separator");
        if (getClass().getResource(resource) == null) {
            throw new RuntimeException("resource not found " + resource);
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(resource)))) {
            String line = null;
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                while ((line = br.readLine()) != null) {
                    for (String line2 : SvcHelper.splitLines(vars.replaceVars(line))) {
                        bw.write(line2);
                        bw.write(lineSeparator);
                        logVerbose("[FILE] " + line2);
                    }
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        file(file).setPermissions(NPathPermission.CAN_EXECUTE);
    }

    private String getInstallDir() {
        return getCurrentWorkingDir().toString();
    }

    private static class DefaultMapper implements Function<String, String> {
        private Pattern PATTERN = Pattern.compile("[$][$](?<name>([^$]+))[$][$]");
        private DefaultInstallSvcCommand base;

        public DefaultMapper(DefaultInstallSvcCommand base) {
            this.base = base;
        }

        @Override
        public String apply(String s) {
            switch (s) {
                case "APP_INSTALL_DIR":
                    return base.getInstallDir();
                case "JAVA":
                    return System.getProperty("java.home") + "/bin/java";
                case "NUTS_JAR":
                    return base.getLibDir() + "/net/thevpc/nuts/nuts/" + base.nutsApiVersion + "/nuts-" + base.nutsApiVersion + ".jar";
                case "USER":
                    return System.getProperty("user.name");
                case "START_COMMANDLINE": {
                    return base.formatCommand(base.startCommand);
                }
                case "STOP_COMMANDLINE": {
                    return base.formatCommand(base.stopCommand);
                }
                case "VAR_RUN": {
                    return base.rootFile("/var/run").toString();
                }
                case "VAR_LOG": {
                    return base.rootFile("/var/log").toString();
                }
                case "SERVICE_NAME": {
                    return base.serviceName;
                }
                case "SERVICE_DESCRIPTION": {
                    return base.serviceDescription;
                }
                case "PID_FILE": {
                    return base.getCurrentWorkingDir() + "/" + base.serviceName + ".pid";
                }
            }
            return "$$" + s + "$$";
        }

        public String replaceVars(String line) {
            Matcher matcher = PATTERN.matcher(line);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String name = matcher.group("name");
                String x = this.apply(name);
                if (x == null) {
                    x = "$$" + name + "$$";
                }
                matcher.appendReplacement(sb, Matcher.quoteReplacement(x));
            }
            matcher.appendTail(sb);
            return sb.toString();
        }
    }


    private NPath file(String parent, String child) {
        return file(parent).resolve(child);
    }

    private boolean isRootOverridden() {
        if (NBlankable.isBlank(root)) {
            return false;
        }
        if (root.toString().equals("/")) {
            return false;
        }
        return true;
    }

    private NPath rootFile(String path) {
        NPath rootFolder = isRootOverridden() ? this.root : NPath.of("/", session);
        rootFolder = rootFolder.toAbsolute().normalize();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return rootFolder.resolve(path);
    }

    private NPath file(String path) {

        NPath file = NPath.of(path, session);
        if (!file.isAbsolute()) {
            file = getCurrentWorkingDir().resolve(path);
        }
        return file.normalize();
    }

    private String getLibDir() {
        String t = env.get("target");
        if (t == null) {
            t = "lib";
        }
        t = replaceDollarString(t, env);
        return file(t).toString();
    }

    private static String replaceDollarString(String text, Map<String, String> m) {
        return replaceDollarString(text, m, true, 1000);
    }

    private static String replaceDollarString(String text, Map<String, String> m, boolean err, int max) {
        return NStringUtils.replaceDollarPlaceHolder(text, new Function<String, String>() {
            @Override
            public String apply(String s) {
                return getProp(s, "${"+s+"}", m, err, max - 1);
            }
        });
    }


    private boolean isRoot() {
        return "root".equals(System.getProperty("user.name"));
    }

    private void runAsRoot(ScriptBuilder script) {
        logVerbose("[ROOT-SCRIPT] " + script.name + " (" + script.description + ")");
        File tempFile = null;
        try {
            tempFile = File.createTempFile("script-", ".root");
            try (PrintStream out = new PrintStream(tempFile)) {
                for (String s : script.lines()) {
                    out.println(s);
                    logVerbose("[ROOT-SCRIPT] " + s);
                }
            }
            tempFile.setExecutable(true);
            logVerbose("[ROOT-SCRIPT] start ");
            runSystemCommandAsRoot(tempFile.getPath());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    private void runSystemCommandAsRoot(String cmd) {
        boolean sudo = false;
        List<String> suCommand =
                sudo ? Arrays.asList("sudo", "-S", cmd)
                        : Arrays.asList("su", "-s", "/bin/sh", "root", "-c", cmd);
        runSystemCommand(suCommand.toArray(new String[0]));
    }

    private static String getProp(String n, String image, Map<String, String> m, boolean err, int max) {
        String x = m.get(n);
        if (x == null) {
            try {
                x = System.getProperty(n);
            } catch (Exception e) {
                //
            }
        }
        if (x == null) {
            try {
                x = System.getenv(n);
            } catch (Exception e) {
                //
            }
        }
        if (x == null) {
            if (err) {
                throw new IllegalArgumentException("var not found " + n);
            } else {
                x = image;
            }
        } else {
            if (x.indexOf('$') >= 0) {
                x = replaceDollarString(x, m, false, max);
            }
        }
        return x;
    }


    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        return false;
    }

}
