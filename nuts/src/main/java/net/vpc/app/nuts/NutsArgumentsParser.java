package net.vpc.app.nuts;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NutsArgumentsParser {
    private static final Logger log = Logger.getLogger(NutsArgumentsParser.class.getName());

    private NutsArgumentsParser() {
    }

    public static String[] parseCommandLine(String commandLineString) {
        if (commandLineString == null) {
            return new String[0];
        }
        List<String> args = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        final int START = 0;
        final int IN_WORD = 1;
        final int IN_QUOTED_WORD = 2;
        int status = START;
        char[] charArray = commandLineString.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            switch (status) {
                case START: {
                    switch (c) {
                        case ' ': {
                            //ignore
                            break;
                        }
                        case '\'': {
                            status = IN_QUOTED_WORD;
                            //ignore
                            break;
                        }
                        case '\\': {
                            throw new IllegalArgumentException("Illegal char " + c);
                        }
                        default: {
                            sb.append(c);
                            status = IN_WORD;
                            break;
                        }
                    }
                    break;
                }
                case IN_WORD: {
                    switch (c) {
                        case ' ': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            break;
                        }
                        case '\'': {
                            throw new IllegalArgumentException("Illegal char " + c);
                        }
                        case '\\': {
                            throw new IllegalArgumentException("Illegal char " + c);
                        }
                        default: {
                            sb.append(c);
                            break;
                        }
                    }
                    break;
                }
                case IN_QUOTED_WORD: {
                    switch (c) {
                        case '\'': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            //ignore
                            break;
                        }
                        case '\\': {
                            i++;
                            sb.append(charArray[i]);
                            //ignore
                            break;
                        }
                        default: {
                            sb.append(c);
                            //ignore
                            break;
                        }
                    }
                }
            }
        }
        switch (status) {
            case START: {
                break;
            }
            case IN_WORD: {
                args.add(sb.toString());
                sb.delete(0, sb.length());
                break;
            }
            case IN_QUOTED_WORD: {
                throw new IllegalArgumentException("Expected '");
            }
        }
        return args.toArray(new String[0]);
    }

    private static String compressBootArgument(String arg) {
        StringBuilder sb = new StringBuilder();
        boolean s = false;
        if (arg != null) {
            for (char c : arg.toCharArray()) {
                switch (c) {
                    case '\'':
                    case '\\': {
                        sb.append('\\');
                        sb.append(c);
                        s = true;
                        break;
                    }
                    case ' ': {
                        s = true;
                        sb.append(c);
                        break;
                    }
                    default: {
                        sb.append(c);
                    }
                }
            }
        }
        if (s || sb.length() == 0) {
            sb.insert(0, '\'');
            sb.append('\'');
        }
        return sb.toString();
    }

    public static String compressBootArguments(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(compressBootArgument(arg));
        }
        return sb.toString();
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
            NewInstanceNutsArguments a = parseNewInstanceNutsArguments(nargs);
            if (a != null) {
                return a;
            }
        }
        ConfigNutsArguments z = parseCurrentInstanceNutsArguments(nargs, aargs);
        return z;
    }

    private static ConfigNutsArguments parseCurrentInstanceNutsArguments(String[] bootArgs, String[] appArgs) {
        List<String> showError = new ArrayList<>();
        ConfigNutsArguments o = new ConfigNutsArguments();
        int startAppArgs = 0;
        boolean expectArgs = false;
        HashSet<String> excludedExtensions = new HashSet<>();
        HashSet<String> excludedRepositories = new HashSet<>();
        o.getWorkspaceCreateOptions().setSaveIfCreated(true);
        for (int i = 0; i < bootArgs.length; i++) {
            String a = bootArgs[i];
            if (!expectArgs && a.startsWith("-")) {
                switch (a) {
                    //dash (startAppArgs) should be the very last argument
                    case "-": {
                        o.getArgs().add(0, NutsConstants.NUTS_SHELL);
                        startAppArgs = i + 1;
                        //force exit loop
                        i = bootArgs.length;
                        continue;
                    }
                    case "--home":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for workspace");
                        }
                        o.getBootOptions().setHome(bootArgs[i]);
                        break;
                    case "--workspace":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for workspace");
                        }
                        o.getWorkspaceCreateOptions().setWorkspace(bootArgs[i]);
                        break;
                    case "--archetype":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for archetype");
                        }
                        o.getWorkspaceCreateOptions().setArchetype(bootArgs[i]);
                        break;
                    case "--login":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for login ");
                        }
                        o.getWorkspaceCreateOptions().setLogin(bootArgs[i]);
                        break;
                    case "--password":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for password");
                        }
                        o.getWorkspaceCreateOptions().setPassword(bootArgs[i]);
                        break;
                    case "--apply-updates":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for apply-updates");
                        }
                        o.setApplyUpdatesFile(bootArgs[i]);
                        break;
                    case "--boot-runtime":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for runtime-id");
                        }
                        String br = bootArgs[i];
                        if (br.indexOf("#") > 0) {
                            //this is a full id
                        } else {
                            br = NutsConstants.NUTS_ID_BOOT_RUNTIME + "#" + br;
                        }
                        o.getBootOptions().setBootRuntime(br);
                        break;
                    case "--runtime-source-url":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for boot-url");
                        }
                        o.getBootOptions().setBootRuntimeSourceURL(bootArgs[i]);
                        break;
                    case "--java":
                    case "--boot-java":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for java");
                        }
                        o.getBootOptions().setBootJavaCommand(bootArgs[i]);
                        break;
                    case "--java-home":
                    case "--boot-java-home":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for java-home");
                        }
                        o.getBootOptions().setBootJavaCommand(NutsUtils.resolveJavaCommand(bootArgs[i]));
                        break;
                    case "--java-options":
                    case "--boot-java-options":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for java-options");
                        }
                        o.getBootOptions().setBootJavaOptions(bootArgs[i]);
                        break;
                    case "--save":
                        o.getWorkspaceCreateOptions().setSaveIfCreated(true);
                        break;
                    case "--no-save":
                    case "--!save":
                        o.getWorkspaceCreateOptions().setSaveIfCreated(false);
                        break;
                    case "--no-colors":
                    case "--!colors":
                        o.getWorkspaceCreateOptions().setNoColors(true);
                        break;
                    case "-version":
                    case "--version":
                        o.setBootCommand(NutsBootCommand.VERSION);
                        expectArgs=true;
                        break;
                    case "--update":
                        o.setBootCommand(NutsBootCommand.UPDATE);
                        expectArgs=true;
                        break;
                    case "--clean":
                        o.setBootCommand(NutsBootCommand.CLEAN);
                        expectArgs=true;
                        break;
                    case "--reset":
                        o.setBootCommand(NutsBootCommand.RESET);
                        expectArgs=true;
                        break;
                    case "--check-updates":
                        o.setBootCommand(NutsBootCommand.CHECK_UPDATES);
                        expectArgs=true;
                        break;
                    case "--verbose":
                    case "--log-finest":
                        o.getBootOptions().setLogLevel(Level.FINEST);
                        break;
                    case "--info":
                    case "--log-info":
                        o.getBootOptions().setLogLevel(Level.INFO);
                        break;
                    case "--log-fine":
                        o.getBootOptions().setLogLevel(Level.FINE);
                        break;
                    case "--log-finer":
                        o.getBootOptions().setLogLevel(Level.FINER);
                        break;
                    case "--log-all":
                        o.getBootOptions().setLogLevel(Level.ALL);
                        break;
                    case "--log-off":
                        o.getBootOptions().setLogLevel(Level.OFF);
                        break;
                    case "--log-severe":
                        o.getBootOptions().setLogLevel(Level.SEVERE);
                        break;
                    case "--log-size":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for log-size");
                        }
                        o.getBootOptions().setLogSize(NutsUtils.parseFileSize(bootArgs[i]));
                        break;
                    case "--log-folder":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for log-size");
                        }
                        o.getBootOptions().setLogFolder(bootArgs[i]);
                        break;
                    case "--log-name":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for log-name");
                        }
                        o.getBootOptions().setLogName(bootArgs[i]);
                        break;
                    case "--log-count":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for log-count");
                        }
                        o.getBootOptions().setLogCount(Integer.parseInt(bootArgs[i]));
                        break;
                    case "--exclude-extensions":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for exclude-extensions");
                        }
                        excludedExtensions.addAll(NutsUtils.split(bootArgs[i], " ,;"));
                        break;
                    case "--exclude-repositories":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for exclude-repositories");
                        }
                        excludedRepositories.addAll(NutsUtils.split(bootArgs[i], " ,;"));
                        break;
                    case "--help": {
                        o.setBootCommand(NutsBootCommand.HELP);
                        expectArgs=true;
                        expectArgs = true;
                        break;
                    }
                    case "--license": {
                        o.setBootCommand(NutsBootCommand.LICENSE);
                        expectArgs=true;
                        expectArgs = true;
                        break;
                    }
                    case "--perf": {
                        o.getBootOptions().setPerf(true);
                        break;
                    }
                    default: {
                        if (a.startsWith("--version=")) {
                            o.setBootCommand(NutsBootCommand.VERSION);
                            o.setVersionOptions(a.substring("--version=".length()));
                            expectArgs = true;
                        } else if (a.startsWith("-J")) {
                            o.getArgs().add(a);
                        } else if (a.startsWith("--nuts")) {
                            o.getArgs().add(a);
                        } else {
                            showError.add("nuts: invalid option [[" + a + "]]");
                        }
                        break;
                    }
                }
                startAppArgs = i + 1;
                if (expectArgs) {
                    break;
                }
            } else {
                break;
            }
        }
        o.getWorkspaceCreateOptions().setExcludedExtensions(excludedExtensions.toArray(new String[0]));
        o.getWorkspaceCreateOptions().setExcludedRepositories(excludedRepositories.toArray(new String[0]));
        o.getArgs().addAll(Arrays.asList(Arrays.copyOfRange(bootArgs, startAppArgs, bootArgs.length)));
        if (o.getBootCommand()!=NutsBootCommand.HELP) {
            if (!showError.isEmpty()) {
                for (String s : showError) {
                    System.err.printf("%s·\n", s);
                }
                System.err.printf("Try 'nuts --help' for more information.\n");
                throw new NutsIllegalArgumentException("Try 'nuts --help' for more information.");
            }
        }
        o.getBootOptions().setBootArguments(Arrays.copyOfRange(bootArgs, 0, startAppArgs));
        o.getBootOptions().setApplicationArguments(appArgs == null ? new String[0] : appArgs);
        return o;
    }

    private static NewInstanceNutsArguments parseNewInstanceNutsArguments(String[] args) {
        String requiredBootVersion = null;
        String requiredJavaCommand = null;
        String requiredJavaOptions = null;
        boolean configureLog = false;
        Level logLevel = null;
        int logSize = 0;
        int logCount = 0;
        String logName = null;
        String logFolder = null;
        String nutsHome = null;
        String workspace = null;
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "--boot-version":
                case "--boot-api-version":
                    i++;
                    if (i >= args.length) {
                        throw new NutsIllegalArgumentException("Missing argument for run-version");
                    }
                    requiredBootVersion = args[i];
                    break;
                case "--java":
                case "--boot-java":
                    i++;
                    if (i >= args.length) {
                        throw new NutsIllegalArgumentException("Missing argument for java");
                    }
                    requiredJavaCommand = args[i];
                    break;
                case "--java-home":
                case "--boot-java-home":
                    i++;
                    if (i >= args.length) {
                        throw new NutsIllegalArgumentException("Missing argument for java-home");
                    }
                    requiredJavaCommand = NutsUtils.resolveJavaCommand(args[i]);
                    break;
                case "--java-options":
                case "--boot-java-options":
                    i++;
                    if (i >= args.length) {
                        throw new NutsIllegalArgumentException("Missing argument for java-options");
                    }
                    requiredJavaOptions = args[i];
                    break;
                case "--home":
                    i++;
                    if (i >= args.length) {
                        throw new NutsIllegalArgumentException("Missing argument for workspace");
                    }
                    nutsHome = args[i];
                    break;
                case "--verbose":
                case "--log-finest":
                    configureLog = true;
                    logLevel = Level.FINEST;
                    break;
                case "--info":
                case "--log-info":
                    configureLog = true;
                    logLevel = Level.INFO;
                    break;
                case "--log-fine":
                    configureLog = true;
                    logLevel = Level.FINE;
                    break;
                case "--log-finer":
                    configureLog = true;
                    logLevel = Level.FINER;
                    break;
                case "--log-all":
                    configureLog = true;
                    logLevel = Level.ALL;
                    break;
                case "--log-off":
                    configureLog = true;
                    logLevel = Level.OFF;
                    break;
                case "--log-severe":
                    configureLog = true;
                    logLevel = Level.SEVERE;
                    break;
                case "--log-size":
                    configureLog = true;
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
                case "--log-folder":
                    i++;
                    if (i >= args.length) {
                        throw new NutsIllegalArgumentException("Missing argument for log-folder");
                    }
                    logFolder = args[i];
                    break;
                case "--log-name":
                    i++;
                    if (i >= args.length) {
                        throw new NutsIllegalArgumentException("Missing argument for log-name");
                    }
                    logName = args[i];
                    break;
                case "--workspace":
                    i++;
                    if (i >= args.length) {
                        throw new NutsIllegalArgumentException("Missing argument for workspace");
                    }
                    workspace = args[i];
                    break;
                default:
                    break;
            }
        }
        if (nutsHome == null) {
            nutsHome = NutsConstants.DEFAULT_NUTS_HOME;
        }
        NutsBootConfig defaultBootConfig = NutsUtils.loadNutsBootConfig(nutsHome, workspace);
        String actualVersion = Nuts.getActualVersion();

        if (requiredBootVersion == null) {
            requiredBootVersion = defaultBootConfig.getBootAPIVersion();
        }
        if (requiredJavaCommand == null) {
            requiredJavaCommand = defaultBootConfig.getBootJavaCommand();
        }
        if (requiredJavaOptions == null) {
            requiredJavaOptions = defaultBootConfig.getBootJavaOptions();
        }
        if (
                (requiredBootVersion == null || requiredBootVersion.trim().isEmpty() || requiredBootVersion.equals(actualVersion))
                        &&
                        isActualJavaCommand(requiredJavaCommand)
        ) {
            return null;
        }
        if (configureLog) {
            NutsLogUtils.prepare(logLevel, logFolder, logName, logSize, logCount, nutsHome, workspace);
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
        File file = NutsUtils.resolveOrDownloadJar(NutsConstants.NUTS_ID_BOOT_API + "#" + requiredBootVersion,
                new String[]{
                        nutsHome + File.separator + NutsConstants.BOOTSTRAP_REPOSITORY_NAME,
                        System.getProperty("user.home") + "/.m2/repository",
                        NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT,
                        NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_GIT,
                        NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_CENTRAL
                },
                nutsHome + "/" + NutsConstants.BOOTSTRAP_REPOSITORY_NAME
        );
        if (file == null) {
            errors.append("Unable to load " + NutsConstants.NUTS_ID_BOOT_API + "#" + requiredBootVersion + "\n");
            NutsBootConfig actualBootConfig = new NutsBootConfig()
                    .setBootAPIVersion(NutsConstants.NUTS_ID_BOOT_API + "#" + Nuts.getActualVersion())
                    .setBootRuntime(null);

            NutsUtils.showError(
                    actualBootConfig,
                    new NutsBootConfig()
                            .setBootAPIVersion(requiredBootVersion)
                            .setBootRuntime(null)
                            .setBootJavaCommand(requiredJavaCommand)
                            .setBootJavaOptions(requiredJavaOptions)
                    , nutsHome
                    , workspace,
                    errors.toString()
            );

            throw new NutsIllegalArgumentException("Unable to load " + NutsConstants.NUTS_ID_BOOT_API + "#" + requiredBootVersion);
        }
        NewInstanceNutsArguments a = new NewInstanceNutsArguments();
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
                    parseCommandLine(args[0].substring("--nuts-boot-args=".length())),
                    Arrays.copyOfRange(args, 1, args.length)
            };
        } else {
            String s = System.getProperty("nuts.boot.args");
            if (s != null) {
                return new String[][]{
                        parseCommandLine(s),
                        args
                };
            }
            s = System.getenv("nuts_boot_args");
            if (s != null) {
                return new String[][]{
                        parseCommandLine(s),
                        args
                };
            }
            return new String[][]{
                    new String[0],
                    args
            };
        }
    }
}
