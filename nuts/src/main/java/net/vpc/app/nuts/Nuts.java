/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/5/17.
 */
public class Nuts {

    private static final Logger log = Logger.getLogger(Nuts.class.getName());

    public static void main(String[] args) {
        try {
            uncheckedMain(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static NutsBootWorkspace openBootWorkspace() {
        return openBootWorkspace(null);
    }

    public static NutsBootWorkspace openBootWorkspace(NutsBootOptions bootOptions) {
        return new DefaultNutsBootWorkspace(bootOptions);
    }

    public static NutsWorkspace openWorkspace(String[] args) {
        String workspace = null;
        NutsBootOptions nutsBootOptions = new NutsBootOptions();
        NutsWorkspaceCreateOptions workspaceCreateOptions = new NutsWorkspaceCreateOptions();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--nuts-no-more-args")) {
                break;
            } else if (args[i].startsWith("--nuts-")) {
                if (args[i].startsWith("--nuts-workspace=")) {
                    workspace = args[i].substring("--nuts-workspace=".length());
                } else if (args[i].startsWith("--nuts-runtimeId=")) {
                    nutsBootOptions.setRuntimeId(args[i].substring("--nuts-runtimeId=".length()));
                } else if (args[i].startsWith("--nuts-home=")) {
                    nutsBootOptions.setHome(args[i].substring("--nuts-home=".length()));
                }
                //ok
            } else {
                break;
            }
        }
        return openWorkspace(workspace, workspaceCreateOptions, nutsBootOptions);
    }

    public static NutsWorkspace openWorkspace() {
        return openWorkspace(null, null, null);
    }

    public static NutsWorkspace openWorkspace(String workspace) {
        return openWorkspace(workspace, null, null);
    }

    public static NutsWorkspace openWorkspace(String workspace, NutsWorkspaceCreateOptions options) {
        return openWorkspace(workspace, options, null);
    }

    public static NutsWorkspace openWorkspace(String workspace, NutsWorkspaceCreateOptions options, NutsBootOptions bootOptions) {
        return openBootWorkspace(bootOptions).openWorkspace(workspace, options);
    }

    public static void uncheckedMain(String[] args) {
        long startTime = System.currentTimeMillis();
        args = checkDistinctBootVersionRequest(args);
        if (args == null) {
            return;
        }
        int startAppArgs = 0;
        String nutsHome = null;
        String workspace = null;
        String archetype = null;
        String login = null;
        String password = null;
        String logFolder = null;
        String runtimeId = null;
        String runtimeSourceURL = null;
        Level logLevel = null;
        int logSize = 0;
        int logCount = 0;
        boolean save = true;
        boolean version = false;
        boolean doupdate = false;
        boolean checkupdates = false;
        String applyUpdatesFile = null;
        boolean perf = false;
        boolean showHelp = false;
        boolean showLicense = false;
        boolean nocolors = false;
        List<String> showError = new ArrayList<>();
        Set<String> excludedExtensions = new HashSet<>();
        Set<String> excludedRepositories = new HashSet<>();
        List<String> extraEnv = new ArrayList<>();
        boolean runShell = false;
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("-")) {
                switch (a) {
                    //dash (startAppArgs) should be the very last argument
                    case "-": {
                        runShell = true;
                        startAppArgs = i + 1;
                        //force exit loop
                        i = args.length;
                        continue;
                    }
                    case "--home":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for workspace");
                        }
                        nutsHome = args[i];
                        break;
                    case "--workspace":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for workspace");
                        }
                        workspace = args[i];
                        break;
                    case "--archetype":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for archetype");
                        }
                        archetype = args[i];
                        break;
                    case "--login":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for login ");
                        }
                        login = args[i];
                        break;
                    case "--password":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for password");
                        }
                        password = args[i];
                        break;
                    case "--apply-updates":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for apply-updates");
                        }
                        applyUpdatesFile = args[i];
                        break;
                    case "--runtime-id":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for runtime-id");
                        }
                        runtimeId = args[i];
                        break;
                    case "--runtime-source-url":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for boot-url");
                        }
                        runtimeSourceURL = args[i];
                        break;
                    case "--save":
                        save = true;
                        break;
                    case "--no-colors":
                        nocolors = true;
                        break;
                    case "--nosave":
                        save = false;
                        break;
                    case "-version":
                    case "--version":
                        version = true;
                        break;
                    case "--update":
                        doupdate = true;
                        break;
                    case "--check-updates":
                        checkupdates = true;
                        break;
                    case "--verbose":
                    case "--log-finest":
                        logLevel = Level.FINEST;
                        break;
                    case "--info":
                    case "--log-info":
                        logLevel = Level.INFO;
                        break;
                    case "--log-fine":
                        logLevel = Level.FINE;
                        break;
                    case "--log-finer":
                        logLevel = Level.FINER;
                        break;
                    case "--log-all":
                        logLevel = Level.ALL;
                        break;
                    case "--log-off":
                        logLevel = Level.OFF;
                        break;
                    case "--log-severe":
                        logLevel = Level.SEVERE;
                        break;
                    case "--log-size":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for log-size");
                        }
                        logSize = Integer.parseInt(args[i]);
                        break;
                    case "--log-count":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for log-count");
                        }
                        logCount = Integer.parseInt(args[i]);
                        break;
                    case "--exclude-extensions":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for exclude-extensions");
                        }
                        excludedExtensions.addAll(NutsStringUtils.split(args[i], " ,;"));
                        break;
                    case "--exclude-repositories":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for exclude-repositories");
                        }
                        excludedRepositories.addAll(NutsStringUtils.split(args[i], " ,;"));
                        break;
                    case "--help": {
                        showHelp = true;
                        break;
                    }
                    case "--license": {
                        showLicense = true;
                        break;
                    }
                    case "--perf": {
                        perf = true;
                        break;
                    }
                    default: {
                        if (a.startsWith("-J")) {
                            extraEnv.add(a);
                        } else if (a.startsWith("--nuts")) {
                            extraEnv.add(a);
                        } else {
                            showError.add("nuts: invalid option [[" + a + "]]");
                        }
                        break;
                    }
                }
                startAppArgs = i + 1;
            } else {
                break;
            }
        }

        LogUtils.prepare(logLevel, logFolder, logSize, logCount);
        NutsBootWorkspace bws = openBootWorkspace(
                new NutsBootOptions()
                        .setHome(nutsHome)
                        .setRuntimeId(runtimeId)
                        .setRuntimeSourceURL(runtimeSourceURL)
        );
        if (!showError.isEmpty()) {
            if (showHelp) {
                //dont show any error, just show help as requested!
                showStaticHelp();
                return;
            }
            for (String s : showError) {
                System.err.printf("%s·\n", s);
            }
            System.err.printf("Try 'nuts --help' for more information.\n");
            throw new NutsIllegalArgumentException("Try 'nuts --help' for more information.");
        }
        boolean someProcessing = false;

        List<String> argsList = new ArrayList<>(extraEnv);
        argsList.addAll(Arrays.asList(args).subList(startAppArgs, args.length));
        String[] commandArguments = argsList.toArray(new String[argsList.size()]);

        NutsWorkspace ws;
        long startWSTime = System.currentTimeMillis();
        try {
            ws = bws.openWorkspace(workspace, new NutsWorkspaceCreateOptions()
                    .setArchetype(archetype)
                    .setCreateIfNotFound(true)
                    .setSaveIfCreated(save)
                    .setExcludedRepositories(excludedRepositories)
                    .setExcludedExtensions(excludedExtensions)
            );
        } catch (Exception ex) {
            if (version) {
                System.err.printf("workspace-location   : %s\n", (workspace == null ? "" : workspace));
                System.err.printf("nuts-boot            : %s\n", bws.getBootId());
                System.err.printf("nuts-runtime         : %s\n", bws.getRuntimeId());
                System.err.printf("nuts-home            : %s\n", bws.getNutsHomeLocation());
                System.err.printf("java-version         : %s\n", System.getProperty("java.version"));
                System.err.printf("java-executable      : %s\n", System.getProperty("java.home") + "/bin/java");
                System.err.printf("java-class-path      : %s\n", System.getProperty("java.class.path"));
                System.err.printf("java-library-path    : %s\n", System.getProperty("java.library.path"));
            }
            if (showHelp) {
                showStaticHelp();
            }
            System.err.printf("Unable to locate nuts-core components.\n");
            System.err.printf("You need internet connexion to initialize nuts configuration. Once components are downloaded, you may work offline...\n");
            System.err.printf("Exiting nuts, Bye!\n");
            throw new NutsIllegalArgumentException("Unable to locate nuts-core components", ex);
        }
        NutsSession session = ws.createSession();
        if (nocolors) {
            session.getTerminal().getFormattedOut().print("`disable-formats`");
            session.getTerminal().getFormattedErr().print("`disable-formats`");
        }
        if (showHelp) {
            perf = showPerf(System.currentTimeMillis() - startTime, perf, session);
            ws.exec(new String[]{NutsConstants.NUTS_SHELL, "help"}, null, null,session);
            someProcessing = true;
        }
        if (showLicense) {
            perf = showPerf(System.currentTimeMillis() - startTime, perf, session);
            ws.exec(new String[]{NutsConstants.NUTS_SHELL, "help", "--license"}, null, null,session);
            someProcessing = true;
        }
        if (login != null && login.trim().length() > 0) {
            if (NutsStringUtils.isEmpty(password)) {
                password = session.getTerminal().readPassword("Password : ");
            }
            ws.getSecurityManager().login(login, password);
        }

        if (applyUpdatesFile != null) {
            ws.exec(applyUpdatesFile, args, false, false, session);
            return;
        }

        if (checkupdates || doupdate) {
            if (ws.checkWorkspaceUpdates(doupdate, args, session).length > 0) {
                return;
            }
            someProcessing = true;
        }

        if (version) {
            NutsPrintStream out = session.getTerminal().getFormattedOut();
            out.printf("workspace-location   : [[%s]]\n", ws.getConfigManager().getWorkspaceLocation());
            out.printf("nuts-boot            : [[%s]]\n", ws.getConfigManager().getWorkspaceBootId());
            out.printf("nuts-runtime         : [[%s]]\n", ws.getConfigManager().getWorkspaceRuntimeId());
            out.printf("nuts-home            : [[%s]]\n", bws.getNutsHomeLocation());
            out.printf("java-version         : [[%s]]\n", System.getProperty("java.version"));
            out.printf("java-executable      : [[%s]]\n", System.getProperty("java.home") + "/bin/java");
            out.printf("java-class-path      : [[%s]]\n", System.getProperty("java.class.path"));
            out.printf("java-library-path    : [[%s]]\n", System.getProperty("java.library.path"));
            URL[] cl = ws.getConfigManager().getBootClassWorldURLs();
            StringBuilder runtimeClasPath = new StringBuilder("?");
            if (cl != null) {
                runtimeClasPath = new StringBuilder();
                for (URL url : cl) {
                    if (url != null) {
                        if (runtimeClasPath.length() > 0) {
                            runtimeClasPath.append(":");
                        }
                        runtimeClasPath.append(url);
                    }
                }
            }
            out.printf("runtime-class-path   : [[%s]]\n", runtimeClasPath.toString());

            perf = showPerf(System.currentTimeMillis() - startTime, perf, session);
            someProcessing = true;
        }

        if (someProcessing && commandArguments.length == 0) {
            return;
        }
        if (commandArguments.length == 0 && !showHelp) {
            /*perf = */
            showPerf(System.currentTimeMillis() - startTime, perf, session);
            ws.exec(new String[]{NutsConstants.NUTS_SHELL, "help"}, null, null,session);
            return;
        }
        /*perf = */
        showPerf(System.currentTimeMillis() - startTime, perf, session);
        List<String> consoleArguments = new ArrayList<>();
        if (runShell) {
            consoleArguments.add(NutsConstants.NUTS_SHELL);
        }
        consoleArguments.addAll(Arrays.asList(commandArguments));
        ws.exec(consoleArguments.toArray(new String[consoleArguments.size()]), null, null, session);
    }

    private static boolean showPerf(long overallTimeMillis, boolean perf, NutsSession session) {
        if (perf) {
            session.getTerminal().getFormattedOut().printf("**Nuts** loaded in [[%s]]ms\n",
                    overallTimeMillis
            );
        }
        return false;
    }

    static String[] checkDistinctBootVersionRequest(String[] args) {
        List<String> goodArgs = new ArrayList<>();
        String requiredBootVersion = null;
        boolean configureLog = false;
        Level logLevel = null;
        int logSize = 0;
        int logCount = 0;
        String logFolder = null;
        String nutsHome = null;
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.equals("--run-version")) {
                i++;
                if (i >= args.length) {
                    throw new NutsIllegalArgumentException("Missing argument for archetype");
                }
                requiredBootVersion = args[i];
            } else if (a.equals("--home")) {
                i++;
                if (i >= args.length) {
                    throw new NutsIllegalArgumentException("Missing argument for workspace");
                }
                nutsHome = args[i];
            } else if (a.equals("--verbose") || a.equals("--log-finest")) {
                configureLog = true;
                logLevel = Level.FINEST;
                goodArgs.add(a);
            } else if (a.equals("--info") || a.equals("--log-info")) {
                configureLog = true;
                logLevel = Level.INFO;
                goodArgs.add(a);
            } else if (a.equals("--log-fine")) {
                configureLog = true;
                logLevel = Level.FINE;
                goodArgs.add(a);
            } else if (a.equals("--log-finer")) {
                configureLog = true;
                logLevel = Level.FINER;
                goodArgs.add(a);
            } else if (a.equals("--log-all")) {
                configureLog = true;
                logLevel = Level.ALL;
                goodArgs.add(a);
            } else if (a.equals("--log-off")) {
                configureLog = true;
                logLevel = Level.OFF;
                goodArgs.add(a);
            } else if (a.equals("--log-severe")) {
                configureLog = true;
                logLevel = Level.SEVERE;
                goodArgs.add(a);
            } else if (a.equals("--log-size")) {
                configureLog = true;
                goodArgs.add(a);
                i++;
                if (i >= args.length) {
                    throw new NutsIllegalArgumentException("Missing argument for log-size");
                }
                logSize = Integer.parseInt(args[i]);
                a = args[i];
                goodArgs.add(a);
            } else if (a.equals("--log-count")) {
                goodArgs.add(a);
                i++;
                if (i >= args.length) {
                    throw new NutsIllegalArgumentException("Missing argument for log-count");
                }
                logCount = Integer.parseInt(args[i]);
                a = args[i];
                goodArgs.add(a);
            } else {
                goodArgs.add(a);
            }
        }
        if (configureLog) {
            LogUtils.prepare(logLevel, logFolder, logSize, logCount);
        }
        args = goodArgs.toArray(new String[goodArgs.size()]);

        String actualVersion = getActualVersion();
        if (nutsHome == null) {
            nutsHome = NutsConstants.DEFAULT_NUTS_HOME;
        }
        if (requiredBootVersion != null && !requiredBootVersion.equals(actualVersion)) {
            log.fine("Running version " + actualVersion + ". Requested version " + requiredBootVersion);
            if ("CURRENT".equalsIgnoreCase(requiredBootVersion)) {
                String versionUrl = NutsConstants.NUTS_ID_BOOT_PATH + "/CURRENT/nuts.version";
                File versionFile = new File(nutsHome, NutsConstants.BOOTSTRAP_REPOSITORY_NAME + versionUrl);
                boolean loaded = false;
                try {
                    if (versionFile.isFile()) {
                        String str = NutsIOUtils.readStringFromFile(versionFile);
                        if (str != null) {
                            str = str.trim();
                            if (str.length() > 0) {
                                requiredBootVersion = str;
                            }
                            loaded = true;
                        }
                    }
                } catch (Exception ex) {
                    System.err.printf("Unable to load nuts version from " + versionUrl + ".\n");
                }
                if (loaded) {
                    log.fine("Detected version " + requiredBootVersion);
                } else {
                    requiredBootVersion = "LATEST";
                }

            }
            if ("LATEST".equalsIgnoreCase(requiredBootVersion)) {
                String mvnUrl = ("https://github.com/thevpc/vpc-public-maven/raw/master" + NutsConstants.NUTS_ID_BOOT_PATH + "/maven-metadata.xml");
                boolean loaded = false;
                try {
                    String str = NutsIOUtils.readStringFromURL(new URL(mvnUrl));
                    if (str != null) {
                        for (String line : str.split("\n")) {
                            line = line.trim();
                            if (line.startsWith("<release>")) {
                                requiredBootVersion = line.substring("<release>".length(), line.length() - "</release>".length()).trim();
                                loaded = true;
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.err.printf("Unable to load nuts version from " + mvnUrl + ".\n");
                    ex.printStackTrace();
                    throw new NutsIllegalArgumentException("Unable to load nuts version from " + mvnUrl);
                }
                if (loaded) {
                    System.out.println("detected version " + requiredBootVersion);
                } else {
                    throw new NutsIllegalArgumentException("Unable to load nuts version from " + mvnUrl);
                }
            }
            String jarUrl = NutsConstants.NUTS_ID_BOOT_PATH + "/" + requiredBootVersion + "/nuts-" + requiredBootVersion + ".jar";
            File bootFile0 = new File(nutsHome, NutsConstants.BOOTSTRAP_REPOSITORY_NAME + jarUrl);
            log.fine("Checking boot jar from " + nutsHome + "/" + NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
            File bootFile = bootFile0;
            if (!bootFile.isFile()) {
                bootFile = new File(System.getProperty("user.home"), "/.m2/repository" + jarUrl);
                log.fine("Checking boot jar from ~/.m2 (local maven)");
                if (!bootFile.isFile()) {
                    log.fine("Checking boot jar from remote vpc-public-maven repository");
                    String mvnUrl = "https://github.com/thevpc/vpc-public-maven/raw/master" + jarUrl;
                    try {
                        if (bootFile0.getParentFile() != null) {
                            bootFile0.getParentFile().mkdirs();
                        }
                        ReadableByteChannel rbc = Channels.newChannel(new URL(mvnUrl).openStream());
                        FileOutputStream fos = new FileOutputStream(bootFile0);
                        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        bootFile = bootFile0;
                    } catch (Exception ex) {
                        bootFile = null;
                        System.err.printf("Unable to load nuts from " + mvnUrl + ".\n");
                        ex.printStackTrace();
                        throw new NutsIllegalArgumentException("Unable to load nuts from " + mvnUrl);
                    }
                }
            }
            List<String> cmd = new ArrayList<>();
            cmd.add(System.getProperty("java.home") + "/bin/java");
            cmd.add("-jar");
            cmd.add(bootFile.getPath());
            cmd.addAll(goodArgs);
            try {
                new ProcessBuilder(cmd).inheritIO().start();
            } catch (IOException ex) {
                throw new IllegalArgumentException("Unable to start nuts", ex);
            }
            return null;
        } else {
            String v = getConfigCurrentVersion(nutsHome);
            if (v == null) {
                setConfigCurrentVersion(actualVersion, nutsHome);
            }
        }
        return args;
    }

    public static String getConfigCurrentVersion(String nutsHome) {
        if (nutsHome == null) {
            nutsHome = System.getProperty("user.home") + File.separator + ".nuts";
        }
        String versionUrl = NutsConstants.NUTS_ID_BOOT_PATH + "/CURRENT/nuts.version";
        File versionFile = new File(nutsHome, NutsConstants.BOOTSTRAP_REPOSITORY_NAME + versionUrl);
        try {
            if (versionFile.isFile()) {
                String str = NutsIOUtils.readStringFromFile(versionFile);
                if (str != null) {
                    str = str.trim();
                    if (str.length() > 0) {
                        return str;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.printf("Unable to load nuts version from " + versionUrl + ".\n");
        }
        return null;
    }

    public static boolean setConfigCurrentVersion(String version, String nutsHome) {
        if (nutsHome == null) {
            //System.getProperty("user.home") + File.separator + ".nuts"
            nutsHome = NutsConstants.DEFAULT_NUTS_HOME;
        }
        nutsHome = NutsIOUtils.expandPath(nutsHome);

        String versionUrl = NutsConstants.NUTS_ID_BOOT_PATH + "/CURRENT/nuts.version";
        File versionFile = new File(nutsHome, NutsConstants.BOOTSTRAP_REPOSITORY_NAME + versionUrl);
        if (version != null) {
            version = version.trim();
            if (version.isEmpty()) {
                version = null;
            }
        }
        if (version == null) {
            return versionFile.delete();
        } else {
            if (versionFile.getParentFile() != null) {
                versionFile.getParentFile().mkdirs();
            }
            PrintStream ps = null;
            try {
                try {
                    ps = new PrintStream(versionFile);
                    ps.println(version);
                    ps.close();
                } finally {
                    if (ps != null) {
                        ps.close();
                    }
                }
                return true;
            } catch (FileNotFoundException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public static String getActualVersion() {
        return NutsIOUtils.loadURLProperties(Nuts.class.getResource("/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties")).getProperty("project.version", "0.0.0");
    }

    private static void showStaticHelp() {
        String actualVersion = getActualVersion();
        String str = null;
        try {
            str = NutsIOUtils.readStringFromURL(Nuts.class.getResource("/net/vpc/app/nuts/NutsStaticHelp.txt"));
            System.out.println("Nuts " + actualVersion);
            System.out.println(str);
        } catch (IOException e) {
            System.err.println("Unable to load Help");
            e.printStackTrace();
        }
    }

    public static String[] skipNutsArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--nuts-no-more-args")) {
                if (i + 1 >= args.length) {
                    break;
                }
                return Arrays.copyOfRange(args, i + 1, args.length);
            } else if (args[i].startsWith("--nuts-")) {
                //ok
            } else {
                return Arrays.copyOfRange(args, i, args.length);
            }
        }
        return new String[0];
    }

}
