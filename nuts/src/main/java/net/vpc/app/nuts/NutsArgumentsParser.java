package net.vpc.app.nuts;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NutsArgumentsParser {
    private static final Logger log = Logger.getLogger(NutsArgumentsParser.class.getName());

    private NutsArgumentsParser() {
    }

    public static NutsArguments parseNutsArguments(String[] args, boolean expectedNutsArgs) {
        String[] nargs = null;
        String[] aargs = null;
        if (expectedNutsArgs) {
            String[][] s = resolveBootAndAppArgs(args);
            nargs = s[0];
            aargs = s[1];
        } else {
            nargs = args;
            aargs = null;
            //never create new instance if expected nuts args(inherited workspace)
            NutsNewInstanceNutsArguments a = parseNewInstanceNutsArguments(nargs);
            if (a != null) {
                return a;
            }
        }
        return parseCurrentInstanceNutsArguments(nargs, aargs);
    }


    private static NutsWorkspaceOptions parseCurrentInstanceNutsArguments(String[] bootArguments, String[] initialApplicationArguments) {
        List<String> showError = new ArrayList<>();
        NutsWorkspaceOptions o = new NutsWorkspaceOptions().setCreateIfNotFound(true);
        HashSet<String> excludedExtensions = new HashSet<>();
        HashSet<String> excludedRepositories = new HashSet<>();
        HashSet<String> tempRepositories = new HashSet<>();
        List<String> executorOptions = new ArrayList<>();
        LogConfig logConfig = new LogConfig();
        o.setSaveIfCreated(true);
        NutsMinimalCommandLine.Arg cmdArg;
        CmdArgList2 cmdArgList = new CmdArgList2(bootArguments);
        while ((cmdArg = cmdArgList.next()) != null) {
            if (cmdArg.isOption()) {
                switch (cmdArg.getKey()) {
                    //dash  should be the very last argument
                    case "-": {
                        if (cmdArg.getValue() != null) {
                            throw new NutsIllegalArgumentException("Invalid argument for workspace : " + cmdArg.getArg());
                        }
                        cmdArgList.applicationArguments.add(NutsConstants.NUTS_SHELL);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--home": {
                        o.setHome(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--workspace": {
                        o.setWorkspace(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--archetype": {
                        o.setArchetype(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--login": {
                        o.setLogin(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--boot-runtime": {
                        String br = cmdArgList.getValueFor(cmdArg);
                        if (br.indexOf("#") > 0) {
                            //this is a full id
                        } else {
                            br = NutsConstants.NUTS_ID_BOOT_RUNTIME + "#" + br;
                        }
                        o.setBootRuntime(br);
                        break;
                    }
                    case "--runtime-source-url": {
                        o.setBootRuntimeSourceURL(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--java":
                    case "--boot-java": {
                        o.setBootJavaCommand(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--java-home":
                    case "--boot-java-home": {
                        o.setBootJavaCommand(NutsUtils.resolveJavaCommand(cmdArgList.getValueFor(cmdArg)));
                        break;
                    }
                    case "--java-options":
                    case "--boot-java-options": {
                        o.setBootJavaOptions(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--save": {
                        cmdArgList.bootOnlyArgsList.add(cmdArg.getArg());
                        o.setSaveIfCreated(true);
                        break;
                    }
                    case "--no-save":
                    case "--!save": {
                        cmdArgList.bootOnlyArgsList.add(cmdArg.getArg());
                        o.setSaveIfCreated(false);
                        break;
                    }
                    case "--!colors":
                    case "--no-colors": {
                        cmdArgList.bootOnlyArgsList.add(cmdArg.getArg());
                        o.setNoColors(true);
                        break;
                    }
                    case "--read-only": {
                        cmdArgList.bootOnlyArgsList.add(cmdArg.getArg());
                        o.setReadOnly(true);
                        break;
                    }
                    case "-version":
                    case "--version": {
                        o.setBootCommand(NutsBootCommand.VERSION);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--info": {
                        o.setBootCommand(NutsBootCommand.INFO);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--update": {
                        o.setBootCommand(NutsBootCommand.UPDATE);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--clean": {
                        o.setBootCommand(NutsBootCommand.CLEAN);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--reset": {
                        o.setBootCommand(NutsBootCommand.RESET);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--install-companions": {
                        o.setBootCommand(NutsBootCommand.INSTALL_COMPANIONS);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--check-updates": {
                        o.setBootCommand(NutsBootCommand.CHECK_UPDATES);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--install": {
                        o.setBootCommand(NutsBootCommand.INSTALL);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--uninstall": {
                        o.setBootCommand(NutsBootCommand.UNINSTALL);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--exec": {
                        o.setBootCommand(NutsBootCommand.EXEC);
                        while ((cmdArg = cmdArgList.next()) != null) {
                            if (cmdArg.isOption()) {
                                executorOptions.add(cmdArg.getArg());
                            } else {
                                cmdArgList.applicationArguments.add(cmdArg.getArg());
                                cmdArgList.consumeApplicationArguments();
                            }
                        }
                        break;
                    }
                    case "--help": {
                        o.setBootCommand(NutsBootCommand.HELP);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--license": {
                        o.setBootCommand(NutsBootCommand.LICENSE);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--verbose":
                    case "--log-finest":
                    case "--log-finer":
                    case "--log-fine":
                    case "--log-info":
                    case "--log-warning":
                    case "--log-severe":
                    case "--log-all":
                    case "--log-off":
                    case "--log-size":
                    case "--log-name":
                    case "--log-folder":
                    case "--log-count":
                    case "--log-inherited": {
                        parseLogLevel(logConfig, cmdArg, cmdArgList);
                        break;
                    }
                    case "--exclude-extension": {
                        excludedExtensions.add(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--exclude-repository": {
                        excludedRepositories.add(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--repository": {
                        tempRepositories.add(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--perf": {
                        cmdArgList.bootOnlyArgsList.add(cmdArg.getArg());
                        o.setPerf(true);
                        break;
                    }
                    case "--auto-config": {
                        o.setAutoConfig(cmdArg.getKey() == null ? "" : cmdArg.getKey());
                        break;
                    }
                    default: {
                        cmdArgList.bootOnlyArgsList.add(cmdArg.getArg());
                        showError.add("nuts: invalid option [[" + cmdArg.getArg() + "]]");
                    }
                }
            } else {
                cmdArgList.applicationArguments.add(cmdArg.getArg());
                cmdArgList.consumeApplicationArguments();
            }
        }
        if (logConfig.logConfigured) {
            o.setLogLevel(logConfig.logLevel);
            o.setLogCount(logConfig.logCount);
            o.setLogSize(logConfig.logSize);
            o.setLogFolder(logConfig.logFolder);
            o.setLogName(logConfig.logName);
            o.setLogInherited(logConfig.logInherited);
        }
        //NutsUtils.split(bootArguments[i], " ,;")
        o.setExcludedExtensions(excludedExtensions.toArray(new String[0]));
        o.setExcludedRepositories(excludedRepositories.toArray(new String[0]));
        o.setTransientRepositories(tempRepositories.toArray(new String[0]));
        if (o.getBootCommand() != NutsBootCommand.HELP) {
            if (!showError.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();
                for (String s : showError) {
                    errorMessage.append(s).append("\n");
                }
                errorMessage.append("Try 'nuts --help' for more information.\n");
                throw new NutsIllegalArgumentException(errorMessage.toString());
            }
        }
        if (initialApplicationArguments != null) {
            cmdArgList.applicationArguments.addAll(Arrays.asList(initialApplicationArguments));
        }
        o.setBootArguments(cmdArgList.bootOnlyArgsList.toArray(new String[0]));
        o.setApplicationArguments(cmdArgList.applicationArguments.toArray(new String[0]));
        o.setExecutorOptions(executorOptions.toArray(new String[0]));
        return o;
    }

    private static class LogConfig {
        boolean logConfigured = false;
        Level logLevel = null;
        int logSize = 0;
        int logCount = 0;
        String logName = null;
        String logFolder = null;
        boolean logInherited = false;
    }

    private static NutsNewInstanceNutsArguments parseNewInstanceNutsArguments(String[] args) {
        String requiredBootVersion = null;
        String requiredJavaCommand = null;
        String requiredJavaOptions = null;
        String nutsHome = null;
        String workspace = null;
        LogConfig logConfig = new LogConfig();
        NutsMinimalCommandLine.Arg cmdArg;
        NutsMinimalCommandLine cmdArgList = new NutsMinimalCommandLine(args);
        while ((cmdArg = cmdArgList.next()) != null) {
            switch (cmdArg.getKey()) {
                //these commands should be executed in the very same process!!
                case "--check-updates":
                case "--clean":
                case "--update":
                case "--reset": {
                    return null;
                }
                case "--boot-version":
                case "--boot-api-version": {
                    requiredBootVersion = cmdArgList.getValueFor(cmdArg);
                    break;
                }
                case "--java":
                case "--boot-java": {
                    requiredJavaCommand = cmdArgList.getValueFor(cmdArg);
                    break;
                }
                case "--java-home":
                case "--boot-java-home": {
                    requiredJavaCommand = NutsUtils.resolveJavaCommand(cmdArgList.getValueFor(cmdArg));
                    break;
                }
                case "--java-options":
                case "--boot-java-options": {
                    requiredJavaOptions = cmdArgList.getValueFor(cmdArg);
                    break;
                }
                case "--home": {
                    nutsHome = cmdArgList.getValueFor(cmdArg);
                    break;
                }
                case "--workspace": {
                    workspace = cmdArgList.getValueFor(cmdArg);
                    break;
                }
                case "--verbose":
                case "--log-finest":
                case "--log-finer":
                case "--log-fine":
                case "--log-info":
                case "--log-warning":
                case "--log-severe":
                case "--log-all":
                case "--log-off":
                case "--log-size":
                case "--log-name":
                case "--log-folder":
                case "--log-count":
                case "--log-inherited": {
                    parseLogLevel(logConfig, cmdArg, cmdArgList);
                    break;
                }
            }
        }
        if (nutsHome == null) {
            nutsHome = Nuts.getDefaultNutsHome();
        }
        NutsBootConfig defaultBootConfig = NutsUtils.loadNutsBootConfig(nutsHome, workspace);
        String actualVersion = Nuts.getActualVersion();

        if (requiredBootVersion == null) {
            requiredBootVersion = defaultBootConfig.getApiVersion();
        }
        if (requiredJavaCommand == null) {
            requiredJavaCommand = defaultBootConfig.getJavaCommand();
        }
        if (requiredJavaOptions == null) {
            requiredJavaOptions = defaultBootConfig.getJavaOptions();
        }
        if (
                (requiredBootVersion == null || requiredBootVersion.trim().isEmpty() || requiredBootVersion.equals(actualVersion))
                        &&
                        isActualJavaCommand(requiredJavaCommand)
        ) {
            return null;
        }
        if (logConfig.logConfigured) {
            NutsLogUtils.prepare(logConfig.logLevel, logConfig.logFolder, logConfig.logName, logConfig.logSize, logConfig.logCount, logConfig.logInherited, nutsHome, workspace);
        }
        log.fine("Running version " + actualVersion + ". Requested version " + requiredBootVersion);
        StringBuilder errors = new StringBuilder();
        if ("LATEST".equalsIgnoreCase(requiredBootVersion) || "RELEASE".equalsIgnoreCase(requiredBootVersion)) {
            String releaseVersion = null;
            try {
                releaseVersion = NutsUtils.resolveMavenReleaseVersion(NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT, NutsConstants.NUTS_ID_BOOT_API_PATH);
                requiredBootVersion = releaseVersion;
            } catch (Exception ex) {
                errors.append("Unable to load nuts version from " + NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT + ".\n");
                throw new NutsIllegalArgumentException("Unable to load nuts version from " + NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT);
            }
            System.out.println("detected version " + requiredBootVersion);
        }
        String defaultWorkspaceCacheFolder = Nuts.getDefaultWorkspaceFolder(NutsConstants.DEFAULT_WORKSPACE_NAME, nutsHome, RootFolderType.CACHE);
        File file = NutsUtils.resolveOrDownloadJar(NutsConstants.NUTS_ID_BOOT_API + "#" + requiredBootVersion,
                new String[]{
                        defaultWorkspaceCacheFolder,
                        System.getProperty("user.home") + "/.m2/repository",
                        NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT,
                        NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_GIT,
                        NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_CENTRAL
                },
                defaultWorkspaceCacheFolder
        );
        if (file == null) {
            errors.append("Unable to load " + NutsConstants.NUTS_ID_BOOT_API + "#").append(requiredBootVersion).append("\n");
            NutsBootConfig actualBootConfig = new NutsBootConfig()
                    .setApiVersion(NutsConstants.NUTS_ID_BOOT_API + "#" + Nuts.getActualVersion())
                    .setRuntimeId(null);

            NutsUtils.showError(
                    actualBootConfig,
                    new NutsBootConfig()
                            .setApiVersion(requiredBootVersion)
                            .setRuntimeId(null)
                            .setJavaCommand(requiredJavaCommand)
                            .setJavaOptions(requiredJavaOptions)
                    , nutsHome
                    , workspace, null,
                    errors.toString()
            );

            throw new NutsIllegalArgumentException("Unable to load " + NutsConstants.NUTS_ID_BOOT_API + "#" + requiredBootVersion);
        }
        NutsNewInstanceNutsArguments a = new NutsNewInstanceNutsArguments();
        a.setBootFile(file);
        //should read from params
        a.setBootVersion(requiredBootVersion);
        a.setJavaCommand(requiredJavaCommand);
        a.setJavaOptions(requiredJavaOptions);
        a.setArgs(args);
//            a.setRequiredVersion(requiredBootVersion);
        return a;
    }

    private static boolean isActualJavaCommand(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) {
            return true;
        }
        String jh = System.getProperty("java.home").replace("\\", "/");
        cmd = cmd.replace("\\", "/");
        if (cmd.equals(jh + "/bin/java")) {
            return true;
        }
        if (cmd.equals(jh + "/bin/java.exe")) {
            return true;
        }
        if (cmd.equals(jh + "/jre/bin/java")) {
            return true;
        }
        if (cmd.equals(jh + "/jre/bin/java.exe")) {
            return true;
        }
        return false;
    }

    private static String[][] resolveBootAndAppArgs(String[] args) {
        if (args.length > 0 && args[0].startsWith("--nuts-boot-args=")) {
            return new String[][]{
                    NutsMinimalCommandLine.parseCommandLine(args[0].substring("--nuts-boot-args=".length())),
                    Arrays.copyOfRange(args, 1, args.length)
            };
        } else {
            String s = System.getProperty("nuts.boot.args");
            if (s != null) {
                return new String[][]{
                        NutsMinimalCommandLine.parseCommandLine(s),
                        args
                };
            }
            s = System.getenv("nuts_boot_args");
            if (s != null) {
                return new String[][]{
                        NutsMinimalCommandLine.parseCommandLine(s),
                        args
                };
            }
            return new String[][]{
                    new String[0],
                    args
            };
        }
    }

    private static void parseLogLevel(LogConfig logConfig, NutsMinimalCommandLine.Arg cmdArg, NutsMinimalCommandLine cmdArgList) {
        switch (cmdArg.getKey()) {
            case "--log-size": {
                logConfig.logConfigured = true;
                logConfig.logSize = Integer.parseInt(cmdArgList.getValueFor(cmdArg));
                break;
            }
            case "--log-count": {
                logConfig.logConfigured = true;
                logConfig.logCount = Integer.parseInt(cmdArgList.getValueFor(cmdArg));
                break;
            }
            case "--log-name": {
                logConfig.logConfigured = true;
                logConfig.logName = cmdArgList.getValueFor(cmdArg);
                break;
            }
            case "--log-folder": {
                logConfig.logConfigured = true;
                logConfig.logFolder = cmdArgList.getValueFor(cmdArg);
                break;
            }
            case "--log-inherited": {
                logConfig.logConfigured = true;
                logConfig.logInherited = true;
                break;
            }
            case "--verbose":
            case "--log-finest":
            case "--log-finer":
            case "--log-fine":
            case "--log-info":
            case "--log-warning":
            case "--log-severe":
            case "--log-all":
            case "--log-off": {
                if (cmdArgList instanceof CmdArgList2) {
                    ((CmdArgList2) cmdArgList).bootOnlyArgsList.add(cmdArg.getArg());
                }
                logConfig.logConfigured = true;
                String id = cmdArg.getKey();
                if (cmdArg.getKey().startsWith("--log-")) {
                    id = id.substring("--log-".length());
                } else if (cmdArg.getKey().equals("--log")) {
                    id = cmdArg.getValue();
                    if (id == null) {
                        id = "";
                    }
                } else if (id.startsWith("--")) {
                    id = cmdArg.getKey().substring(2);
                } else {
                    id = cmdArg.getKey();
                }
                switch (id.toLowerCase()) {
                    case "verbose": {
                        logConfig.logLevel = Level.FINEST;
                        break;
                    }
                    case "finest": {
                        logConfig.logLevel = Level.FINEST;
                        break;
                    }
                    case "finer": {
                        logConfig.logLevel = Level.FINER;
                        break;
                    }
                    case "fine": {
                        logConfig.logLevel = Level.FINE;
                        break;
                    }
                    case "info": {
                        logConfig.logLevel = Level.INFO;
                        break;
                    }
                    case "warning": {
                        logConfig.logLevel = Level.WARNING;
                        break;
                    }
                    case "config": {
                        logConfig.logLevel = Level.CONFIG;
                        break;
                    }
                    case "all": {
                        logConfig.logLevel = Level.ALL;
                        break;
                    }
                    case "off": {
                        logConfig.logLevel = Level.OFF;
                        break;
                    }
                    default: {
                        logConfig.logLevel = Level.INFO;
                        break;
                    }
                }
                break;
            }
        }
    }

    private static class CmdArgList2 extends NutsMinimalCommandLine {
        List<String> bootOnlyArgsList = new ArrayList<>();
        List<String> applicationArguments = new ArrayList<>();

        public CmdArgList2(String[] args) {
            super(args);
        }

        public String getValueFor(Arg cmdArg) {
            String v = super.getValueFor(cmdArg);
            bootOnlyArgsList.add(cmdArg.getKey() + "=" + v);
            return v;
        }

        public void consumeApplicationArguments() {
            applicationArguments.addAll(removeAll());
        }
    }

}
