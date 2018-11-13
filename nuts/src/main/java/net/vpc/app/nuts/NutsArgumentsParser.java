package net.vpc.app.nuts;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
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

    public static String[] uncompressBootArguments(String compressedArgsString) {
        if(compressedArgsString==null){
            return new String[0];
        }
        List<String> args = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        final int START = 0;
        final int IN_WORD = 1;
        final int IN_QUOTED_WORD = 2;
        int status = START;
        char[] charArray = compressedArgsString.toCharArray();
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
        return args.toArray(new String[args.size()]);
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
                    default:{
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
        }

        NewInstanceNutsArguments a = parseNewInstanceNutsArguments(nargs);
        if (a != null) {
            return a;
        }
        ConfigNutsArguments z = parseCurrentInstanceNutsArguments(nargs, aargs);
        return z;
    }

    private static ConfigNutsArguments parseCurrentInstanceNutsArguments(String[] bootArgs, String[] appArgs) {
        List<String> showError = new ArrayList<>();
        ConfigNutsArguments o = new ConfigNutsArguments();
        int startAppArgs = 0;
        HashSet<String> excludedExtensions = new HashSet<>();
        HashSet<String> excludedRepositories = new HashSet<>();
        o.getWorkspaceCreateOptions().setSaveIfCreated(true);
        for (int i = 0; i < bootArgs.length; i++) {
            String a = bootArgs[i];
            if (a.startsWith("-")) {
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
                    case "--runtime-id":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for runtime-id");
                        }
                        o.getBootOptions().setRuntimeId(bootArgs[i]);
                        break;
                    case "--runtime-source-url":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for boot-url");
                        }
                        o.getBootOptions().setRuntimeSourceURL(bootArgs[i]);
                        break;
                    case "--save":
                        o.getWorkspaceCreateOptions().setSaveIfCreated(true);
                        break;
                    case "--no-colors":
                        o.getWorkspaceCreateOptions().setNoColors(true);
                        break;
                    case "--nosave":
                        o.getWorkspaceCreateOptions().setSaveIfCreated(false);
                        break;
                    case "-version":
                    case "--version":
                        o.setVersion(true);
                        break;
                    case "--update":
                        o.setDoupdate(true);
                        break;
                    case "--check-updates":
                        o.setCheckupdates(true);
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
                        o.getBootOptions().setLogSize(Integer.parseInt(bootArgs[i]));
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
                        excludedExtensions.addAll(NutsStringUtils.split(bootArgs[i], " ,;"));
                        break;
                    case "--exclude-repositories":
                        i++;
                        if (i >= bootArgs.length) {
                            throw new NutsIllegalArgumentException("Missing argument for exclude-repositories");
                        }
                        excludedRepositories.addAll(NutsStringUtils.split(bootArgs[i], " ,;"));
                        break;
                    case "--help": {
                        o.setShowHelp(true);
                        break;
                    }
                    case "--license": {
                        o.setShowLicense(true);
                        break;
                    }
                    case "--perf": {
                        o.getBootOptions().setPerf(true);
                        break;
                    }
                    default: {
                        if (a.startsWith("-J")) {
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
            } else {
                break;
            }
        }
        o.getWorkspaceCreateOptions().setExcludedExtensions(excludedExtensions.toArray(new String[excludedExtensions.size()]));
        o.getWorkspaceCreateOptions().setExcludedRepositories(excludedRepositories.toArray(new String[excludedRepositories.size()]));
        o.getArgs().addAll(Arrays.asList(Arrays.copyOfRange(bootArgs, startAppArgs, bootArgs.length)));
        if (!o.isShowHelp()) {
            if (!showError.isEmpty()) {
                for (String s : showError) {
                    System.err.printf("%s·\n", s);
                }
                System.err.printf("Try 'nuts --help' for more information.\n");
                throw new NutsIllegalArgumentException("Try 'nuts --help' for more information.");
            }
        }
        o.getBootOptions().setBootArguments(Arrays.copyOfRange(bootArgs, 0,startAppArgs));
        o.getBootOptions().setApplicationArguments(appArgs == null ? new String[0] : appArgs);
        return o;
    }

    private static NewInstanceNutsArguments parseNewInstanceNutsArguments(String[] args) {
        String requiredBootVersion = null;
        boolean configureLog = false;
        Level logLevel = null;
        int logSize = 0;
        int logCount = 0;
        String logFolder = null;
        String nutsHome = null;
        String workspace = null;
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "--run-version":
                    i++;
                    if (i >= args.length) {
                        throw new NutsIllegalArgumentException("Missing argument for archetype");
                    }
                    requiredBootVersion = args[i];
                    break;
                case "--workspace-version":
                    requiredBootVersion = "@workspace-version";
                    break;
                case "--boot-version":
                    requiredBootVersion = null;
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
                case "--workspace":
                    i++;
                    if (i >= args.length) {
                        throw new NutsIllegalArgumentException("Missing argument for workspace");
                    }
                    workspace=args[i];
                    break;
                default:
                    break;
            }
        }

        String actualVersion = Nuts.getActualVersion();
        if (nutsHome == null) {
            nutsHome = NutsConstants.DEFAULT_NUTS_HOME;
        }
        if (requiredBootVersion != null){
            switch (requiredBootVersion){
                case "@workspace-version":{
                    requiredBootVersion=Nuts.getConfigCurrentVersion(nutsHome,workspace);
                    break;
                }
            }
        }
        if (requiredBootVersion != null && !requiredBootVersion.equals(actualVersion)) {
            if (configureLog) {
                NutsLogUtils.prepare(logLevel, logFolder, logSize, logCount,nutsHome,workspace);
            }
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
                        System.err.printf("Unable to load nuts from " + mvnUrl + ".\n");
                        ex.printStackTrace();
                        throw new NutsIllegalArgumentException("Unable to load nuts from " + mvnUrl);
                    }
                }
            }
            NewInstanceNutsArguments a = new NewInstanceNutsArguments();
            a.setBootFile(bootFile);
            //should read from params
            a.setJavaCommand(System.getProperty("java.home") + "/bin/java");
            a.setArgs(args);
            a.setBootVersion(actualVersion);
            a.setRequiredVersion(requiredBootVersion);
            return a;
        } else {
            return null;
//            String v = getConfigCurrentVersion(nutsHome);
//            if (v == null) {
//                setConfigCurrentVersion(actualVersion, nutsHome);
//            }
        }
//        return args;
    }

    private static String[][] resolveBootAndAppArgs(String[] args) {
        if (args.length > 0 && args[0].startsWith("--nuts-boot-args=")) {
            return new String[][]{
                    uncompressBootArguments(args[0].substring("--nuts-boot-args=".length())),
                    Arrays.copyOfRange(args,1,args.length)
            };
        }else{
            String s = System.getProperty("nuts.boot.args");
            if(s!=null){
                return new String[][]{
                        uncompressBootArguments(s),
                        args
                };
            }
            s = System.getenv("nuts_boot_args");
            if(s!=null){
                return new String[][]{
                        uncompressBootArguments(s),
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
