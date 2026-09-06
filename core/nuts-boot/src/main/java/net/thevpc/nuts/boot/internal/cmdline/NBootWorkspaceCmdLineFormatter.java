package net.thevpc.nuts.boot.internal.cmdline;

import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootHomeLocation;
import net.thevpc.nuts.boot.NBootLogConfig;
import net.thevpc.nuts.boot.internal.NBootVersion;
import net.thevpc.nuts.boot.internal.util.NBootPlatformHome;
import net.thevpc.nuts.boot.internal.util.NBootUtils;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class NBootWorkspaceCmdLineFormatter {
    private static final String V080 = "0.8.0";
    private static final String V081 = "0.8.1";
    private static final String V083 = "0.8.3";
    private static final String V084 = "0.8.4";
    private static final String V085 = "0.8.5";
    private static final String V086 = "0.8.6";
    private static final String V087 = "0.8.7";
    private static final String V089 = "0.8.9";
    private static final String V100 = "1.0.0";
    private final NBootWorkspaceOptionsConfig config;
    private final NBootOptionsInfo options;

    public NBootWorkspaceCmdLineFormatter(NBootWorkspaceOptionsConfig config, NBootOptionsInfo options) {
        this.config = config;
        this.options = options;
    }


    private void fillOption(String longName, String shortName, List<String> values, String sep, List<String> arguments, boolean forceSingle) {
        if (values != null && values.size() > 0) {
            fillOption0(selectOptionName(longName, shortName), String.join(sep, values), arguments, forceSingle);
        }
    }

    private void fillOption(String longName, String shortName, String[] values, String sep, List<String> arguments, boolean forceSingle) {
        if (values != null && values.length > 0) {
            fillOption0(selectOptionName(longName, shortName), String.join(sep, values), arguments, forceSingle);
        }
    }

    private void fillOption(String longName, String shortName, Boolean value, boolean defaultValue, List<String> arguments, boolean forceSingle) {
        if (value != null) {
            if (defaultValue) {
                if (!value) {
                    if (config.isShortOptions() && shortName != null) {
                        arguments.add("-!" + shortName.substring(1));
                    } else {
                        if (longName.startsWith("---")) {
                            arguments.add("---!" + longName.substring(3));
                        } else {
                            arguments.add("--!" + longName.substring(2));
                        }
                    }
                }
            } else {
                if (value) {
                    arguments.add(selectOptionName(longName, shortName));
                }
            }
        }
    }

    private void fillOption(String longName, String shortName, char[] value, List<String> arguments, boolean forceSingle) {
        if (value != null && new String(value).isEmpty()) {
            fillOption0(selectOptionName(longName, shortName), new String(value), arguments, forceSingle);
        }
    }

    private void fillOption(String longName, String shortName, String value, List<String> arguments, boolean forceSingle) {
        if (!NBootUtils.isBlank(value)) {
            fillOption0(selectOptionName(longName, shortName), value, arguments, forceSingle);
        }
    }

    private void fillOption(String longName, String shortName, int value, List<String> arguments, boolean forceSingle) {
        if (value > 0) {
            fillOption0(selectOptionName(longName, shortName), String.valueOf(value), arguments, forceSingle);
        }
    }

    private void fillOptionEnum(String longName, String shortName, String value, String enumType, List<String> arguments, boolean forceSingle) {
        if (tryFillOptionShortEnum(value, enumType, arguments, forceSingle)) {
            return;
        }
        if (value != null) {
            if (config.isShortOptions()) {
                if ("NOsFamily".equals(enumType)) {
                    switch (NBootUtils.enumName(value)) {
                        case "LINUX": {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("linux", "l"), arguments, forceSingle);
                            return;
                        }
                        case "WINDOWS": {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("windows", "w"), arguments, forceSingle);
                            return;
                        }
                        case "MACOS": {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("macos", "m"), arguments, forceSingle);
                            return;
                        }
                        case "UNIX": {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("unix", "u"), arguments, forceSingle);
                            return;
                        }
                        case "UNKNOWN": {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("unknown", "x"), arguments, forceSingle);
                            return;
                        }
                    }
                } else if ("NStoreStrategy".equals(enumType)) {
                    switch (NBootUtils.enumName(value)) {
                        case "EXPLODED": {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("exploded", "e"), arguments, forceSingle);
                            return;
                        }
                        case "STANDALONE": {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("standalone", "s"), arguments, forceSingle);
                            return;
                        }
                    }
                } else if ("NTerminalMode".equals(enumType)) {
                    switch (NBootUtils.enumName(value)) {
                        case "FILTERED": {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("no", "n"), arguments, forceSingle);
                            return;
                        }
                        case "INHERITED": {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("inherited", "h"), arguments, forceSingle);
                            return;
                        }
                        case "FORMATTED": {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("yes", "y"), arguments, forceSingle);
                            return;
                        }
                        case "DEFAULT": {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("default", null), arguments, forceSingle);
                            return;
                        }
                    }
                }
            }
            if ("NSupportMode".equals(enumType)) {
                if (!isApiVersionOrAfter(V084)) {
                    switch (NBootUtils.enumName(value)) {
                        case "ALWAYS": {
                            fillOption0(selectOptionName(longName, shortName), "preferred", arguments, forceSingle);
                            return;
                        }
                        case "NEVER": {
                            fillOption0(selectOptionName(longName, shortName), "unsupported", arguments, forceSingle);
                            return;
                        }
                    }
                }
            }
            fillOption0(selectOptionName(longName, shortName), value.toLowerCase(), arguments, forceSingle);
        }
    }

    private boolean fillOptionEnumRunAs(String value, List<String> arguments) {
        if (value == null) {
            return false;
        }
        switch (NBootUtils.enumName(value)) {
            case "CURRENT_USER": {
                if (isApiVersionOrAfter(V081)) {
                    if (!config.isOmitDefaults()) {
                        arguments.add("--current-user");
                    }
                } else {
                    arguments.add("--user-cmd");
                }
                return true;
            }
            case "ROOT": {
                if (isApiVersionOrAfter(V081)) {
                    arguments.add("--as-root");
                } else {
                    arguments.add("--root-cmd");
                }
                return true;
            }
            case "SUDO": {
                if (isApiVersionOrAfter(V081)) {
                    arguments.add("--sudo");
                }
                return true;
            }
            default: {
                if (value.toUpperCase().startsWith("USER:")) {
                    String user = value.substring("USER:".length());
                    if (isApiVersionOrAfter(V081)) {
                        arguments.add("--run-as=" + user);
                    }
                    return true;
                } else {
                    throw new UnsupportedOperationException("unsupported RunAs : " + value);
                }
            }
        }
    }

    private boolean tryFillOptionShortEnum(String value, String enumType, List<String> arguments, boolean forceSingle) {
        if (value != null) {
            if (config.isShortOptions()) {
                if ("NOpenMode".equals(enumType)) {
                    switch (NBootUtils.enumName(value)) {
                        case "OPEN_OR_ERROR": {
                            fillOption0(selectOptionName("--open-mode", "-o"), selectOptionVal("open-or-error", "r"), arguments, forceSingle);
                            return true;
                        }
                        case "CREATE_OR_ERROR": {
                            fillOption0(selectOptionName("--open-mode", "-o"), selectOptionVal("create-or-error", "w"), arguments, forceSingle);
                            return true;
                        }
                        case "OPEN_OR_CREATE": {
                            if (!config.isOmitDefaults()) {
                                fillOption0(selectOptionName("--open-mode", "-o"), selectOptionVal("open-or-create", "rw"), arguments, forceSingle);
                            }
                            return true;
                        }
                        case "OPEN_OR_NULL": {
                            fillOption0(selectOptionName("--open-mode", "-o"), selectOptionVal("open-or-null", "on"), arguments, forceSingle);
                            return true;
                        }
                    }
                }
                if ("NExecutionType".equals(enumType)) {
                    switch (NBootUtils.enumName(value)) {
                        case "SYSTEM": {
                            if (isApiVersionOrAfter(V081)) {
                                arguments.add("--system");
                            } else {
                                arguments.add("--user-cmd");
                            }
                            return true;
                        }
                        case "EMBEDDED": {
                            arguments.add(selectOptionName("--embedded", "-b"));
                            return true;
                        }
                        case "SPAWN": {
                            if (!config.isOmitDefaults()) {
                                arguments.add(selectOptionName("--spawn", "-x"));
                            }
                            return true;
                        }
                        case "OPEN": {
                            arguments.add(selectOptionName("--open-file", "--open-file"));
                            return true;
                        }
                    }
                }
                if ("NConfirmationMode".equals(enumType)) {
                    switch (NBootUtils.enumName(value)) {
                        case "YES": {
                            arguments.add(selectOptionName("--yes", "-y"));
                            return true;
                        }
                        case "NO": {
                            arguments.add(selectOptionName("-no", "-n"));
                            return true;
                        }
                        case "ASK": {
                            if (!config.isOmitDefaults()) {
                                arguments.add("--ask");
                                return true;
                            }
                            break;
                        }
                        case "ERROR": {
                            arguments.add("--error");
                            return true;
                        }
                    }
                }
                if ("NTerminalMode".equals(enumType)) {
                    switch (NBootUtils.enumName(value)) {
                        case "FILTERED": {
                            if (isApiVersionOrAfter(V084)) {
                                arguments.add("--color=filtered");
                            } else {
                                arguments.add(selectOptionName("--!color", "-!c"));
                            }
                            return true;
                        }
                        case "FORMATTED": {
                            arguments.add(selectOptionName("--color", "-c"));
                            return true;
                        }
                        case "INHERITED": {
                            arguments.add(selectOptionName("--color=inherited", "-c=h"));
                            return true;
                        }
                        case "ANSI": {
                            arguments.add(selectOptionName("--color=ansi", "-c=a"));
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void fillOptionEnum(String value, String enumType, List<String> arguments, boolean forceSingle) {
        if (value != null) {
            if (tryFillOptionShortEnum(value, enumType, arguments, forceSingle)) {
                return;
            }
            arguments.add("--" + NBootUtils.enumId(value));
        }
    }

    private String selectOptionVal(String longName, String shortName) {
        if (config.isShortOptions()) {
            return shortName;
        }
        return longName;
    }

    private String selectOptionName(String longName, String shortName) {
        if (config.isShortOptions() && shortName != null) {
            return shortName;
        }
        return longName;
    }

    private void fillOption0(String name, String value, List<String> arguments, boolean forceSingle) {
        if (config.isSingleArgOptions() || forceSingle) {
            arguments.add(name + "=" + value);
        } else {
            arguments.add(name);
            arguments.add(value);
        }
    }


    public NBootCmdLine toCmdLine() {
//        NVersionBoot apiVersionObj = config.getApiVersion();
        List<String> arguments = new ArrayList<>();

        fillOption("--java", "-j", options.javaCommand(), arguments, false);
        fillOption("--java-options", "-O", options.javaOptions(), arguments, false);
        String wsString = options.workspace();
        if (NBootUtils.isBlank(wsString)) {
            //default workspace name
            wsString = "";
        } else if (wsString.contains("/") || wsString.contains("\\")) {
            //workspace path
            wsString = new File(wsString).toPath().toAbsolutePath().normalize().toString();
        } else {
            //workspace name
        }
        fillOption("--workspace", "-w", wsString, arguments, false);
        fillOption("--user", "-u", options.userName(), arguments, false);
        fillOption("--password", "-p", options.credential(), arguments, false);
        fillOption("--boot-version", "-V", options.apiVersion(), arguments, false);
        fillOption("--boot-runtime", null, options.runtimeId(), arguments, false);

        {
            String nTerminalMode = options.terminalMode();
            if (!isApiVersionOrAfter(V084)) {
                if (NBootUtils.firstNonNull(options.bot(), false)) {
                    //force filtered for older nuts
                    nTerminalMode = "FILTERED";
                }
            }
            if (!(config.isOmitDefaults() && NBootUtils.sameEnum(nTerminalMode, "FORMATTED"))) {
                fillOptionEnum("--color", "-c", nTerminalMode, "NTerminalMode", arguments, true);
            }
        }
        NBootLogConfig logConfig = options.logConfig();
        if (logConfig != null) {
            if (logConfig.logTermLevel() != null && logConfig.logTermLevel() == logConfig.logFileLevel()) {
                if (logConfig.logTermLevel() == Level.FINEST) {
                    if (isApiVersionOrAfter(V089)) {
                        fillOption("--verbose", "-l", true, true, arguments, false);
                    }else{
                        fillOption("--verbose", null, true, true, arguments, false);
                    }
                } else {
                    fillOption("--log-" + logConfig.logFileLevel().toString().toLowerCase(), null, true, false, arguments, false);
                }
            } else {
                if (logConfig.logTermLevel() != null) {
                    fillOption("--log-term-" + logConfig.logTermLevel().toString().toLowerCase(), null, true, false, arguments, false);
                }
                if (logConfig.logFileLevel() != null) {
                    fillOption("--log-file-" + logConfig.logFileLevel().toString().toLowerCase(), null, true, false, arguments, false);
                }
            }
            if (logConfig.logFileCount() > 0) {
                fillOption("--log-file-count", null, String.valueOf(logConfig.logFileCount()), arguments, false);
            }
            fillOption("--log-file-size", null, logConfig.logFileSize(), arguments, false);
            fillOption("--log-file-base", null, logConfig.logFileBase(), arguments, false);
            fillOption("--log-file-name", null, logConfig.logFileName(), arguments, false);
        }
        fillOption("--exclude-extension", "-X", options.excludedExtensions(), ";", arguments, false);
        if (isApiVersionOrAfter(V081)) {
            fillOption("--repositories", "-r", options.repositories(), ";", arguments, false);
        } else {
            fillOption("--repository", "-r", options.repositories(), ";", arguments, false);
        }
        if (isApiVersionOrAfter(V085)) {
            fillOption("--boot-repositories", null, options.bootRepositories(), ";", arguments, false);
        }

        fillOption("--global", "-g", options.system(), false, arguments, false);
        fillOption("--gui", null, options.gui(), false, arguments, false);
        fillOption("--read-only", "-R", options.readOnly(), false, arguments, false);
        fillOption("--trace", "-t", options.trace(), true, arguments, false);
        fillOption("--progress", "-P", options.progressOptions(), arguments, true);
        fillOption("--solver", null, options.dependencySolver(), arguments, false);
        if (isApiVersionOrAfter(V083)) {
            fillOption("--debug", null, options.debug(), arguments, true);
        } else {
            fillOption("--debug", null, options.debug() != null, false, arguments, true);
        }
        fillOption("--install-companions", "-k", options.installCompanions(), false, arguments, false);
        fillOption("--skip-welcome", "-K", NBootUtils.firstNonNull(options.skipWelcome(), false), false, arguments, false);
        fillOption("--out-line-prefix", null, options.outLinePrefix(), arguments, false);
        fillOption("--skip-boot", "-Q", options.skipBoot(), false, arguments, false);
        fillOption("--cached", null, options.cached(), true, arguments, false);
        fillOption("--indexed", null, options.indexed(), true, arguments, false);
        fillOption("--transitive", null, options.transitive(), true, arguments, false);
        if (isApiVersionOrAfter(V081)) {
            fillOption("--bot", "-B", options.bot(), false, arguments, false);
        }
        if (isApiVersionOrAfter(V085)) {
            fillOption("--preview-repo", "-U", options.previewRepo(), false, arguments, false);
            fillOption("--shared-instance", null, options.sharedInstance(), false, arguments, false);
        }
        if (options.fetchStrategy() != null && NBootUtils.sameEnum(options.fetchStrategy(), "ONLINE")) {
            fillOptionEnum("--fetch", "-f", options.fetchStrategy(), "NFetchStrategy", arguments, false);
        }
        fillOptionEnum(options.confirm(), "NConfirmationMode", arguments, false);
        fillOptionEnum(options.outputFormat(), "NContentType", arguments, false);
        if (options.outputFormatOptions() != null) {
            for (String outputFormatOption : options.outputFormatOptions()) {
                fillOption("--output-format-option", "-T", outputFormatOption, arguments, false);
            }
        }
        if (isApiVersionOrAfter(V080)) {
            fillOption("--expire", "-N",
                    options.expireTime() == null ? null : options.expireTime().toString(),
                    arguments, false);
            if (options.outLinePrefix() != null
                    && Objects.equals(options.outLinePrefix(), options.errLinePrefix())
                    && options.outLinePrefix().length() > 0) {
                fillOption("--line-prefix", null, options.outLinePrefix(), arguments, false);
            } else {
                if (options.outLinePrefix() != null && options.outLinePrefix().length() > 0) {
                    fillOption("--out-line-prefix", null, options.outLinePrefix(), arguments, false);
                }
                if (options.errLinePrefix() != null && options.errLinePrefix().length() > 0) {
                    fillOption("--err-line-prefix", null, options.errLinePrefix(), arguments, false);
                }
            }
        }
        if (isApiVersionOrAfter(V081)) {
            fillOption("--theme", null, options.theme(), arguments, false);
        }
        if (isApiVersionOrAfter(V081)) {
            fillOption("--locale", "-L", options.locale(), arguments, false);
        }
        if (isApiVersionOrAfter(V084)) {
            fillOption("--init-launchers", null, options.initLaunchers(), true, arguments, false);
            fillOption("--init-platforms", null, options.initLaunchers(), true, arguments, false);
            fillOption("--init-java", null, options.initLaunchers(), true, arguments, false);
            fillOption("--init-scripts", null, options.initLaunchers(), true, arguments, false);
            fillOptionEnum("--desktop-launcher", null, options.desktopLauncher(), "NSupportMode", arguments, false);
            fillOptionEnum("--menu-launcher", null, options.desktopLauncher(), "NSupportMode", arguments, false);
            fillOptionEnum("--user-launcher", null, options.desktopLauncher(), "NSupportMode", arguments, false);
            fillOptionEnum("--isolation-level", null, options.isolationLevel(), "NIsolationLevel", arguments, false);
        } else if (isApiVersionOrAfter(V081)) {
            fillOption("---init-launchers", null, options.initLaunchers(), true, arguments, false);
            fillOption("---init-platforms", null, options.initLaunchers(), true, arguments, false);
            fillOption("---init-java", null, options.initLaunchers(), true, arguments, false);
            fillOption("---init-scripts", null, options.initLaunchers(), true, arguments, false);
            fillOptionEnum("---system-desktop-launcher", null, options.desktopLauncher(), "NSupportMode", arguments, false);
            fillOptionEnum("---system-menu-launcher", null, options.desktopLauncher(), "NSupportMode", arguments, false);
            fillOptionEnum("---system-custom-launcher", null, options.desktopLauncher(), "NSupportMode", arguments, false);
        }

        fillOption("--name", null, NBootUtils.trim(options.name()), arguments, false);
        fillOption("--archetype", "-A", options.archetype(), arguments, false);
        fillOptionEnum("--store-layout", null, options.storeLayout(), "NOsFamily", arguments, false);
        fillOptionEnum("--store-strategy", null, options.storeStrategy(), "NStoreStrategy", arguments, false);
        fillOptionEnum("--repo-store-strategy", null, options.repositoryStoreStrategy(), "NStoreStrategy", arguments, false);
        Map<String, String> storeLocations = options.storeLocations();
        if (storeLocations == null) {
            storeLocations = new HashMap<>();
        }
        for (String location : NBootPlatformHome.storeTypes()) {
            String s = storeLocations.get(location);
            if (!NBootUtils.isBlank(s)) {
                fillOption("--" + NBootUtils.enumId(location) + "-location", null, s, arguments, false);
            }
        }

        Map<NBootHomeLocation, String> homeLocations = options.homeLocations();
        if (homeLocations != null) {
            for (String location : NBootPlatformHome.storeTypes()) {
                String s = homeLocations.get(NBootHomeLocation.of(null, location));
                if (!NBootUtils.isBlank(s)) {
                    fillOption("--system-" + NBootUtils.enumId(location) + "-home", null, s, arguments, false);
                }
            }
            for (String osFamily : NBootPlatformHome.osFamilies()) {
                for (String location : NBootPlatformHome.storeTypes()) {
                    String s = homeLocations.get(NBootHomeLocation.of(osFamily, location));
                    if (!NBootUtils.isBlank(s)) {
                        fillOption("--" + NBootUtils.enumId(osFamily) + "-" + NBootUtils.enumId(location) + "-home", null, s, arguments, false);
                    }
                }
            }
        }
        if (isApiVersionOrAfter(V080)) {
            if (options.switchWorkspace() != null) {
                fillOption("--switch", null, options.switchWorkspace(), false, arguments, false);
            }
        }

        fillOption("--help", "-h", NBootUtils.firstNonNull(options.commandHelp(), false), false, arguments, false);
        fillOption("--version", "-v", NBootUtils.firstNonNull(options.commandVersion(), false), false, arguments, false);

        if (!(config.isOmitDefaults() && (options.openMode() == null) || NBootUtils.sameEnum(options.openMode(), "OPEN_OR_CREATE"))) {
            fillOptionEnum(options.openMode(), "NOpenMode", arguments, false);
        }
        fillOptionEnum(options.executionType(), "NExecutionType", arguments, false);
        fillOptionEnumRunAs(options.runAs(), arguments);
        fillOption("--reset", "-Z", options.reset(), false, arguments, false);
        fillOption("--recover", "-z", options.recover(), false, arguments, false);
        fillOption("--dry", "-D", options.dry(), false, arguments, false);
        if (isApiVersionOrAfter(V085)) {
            fillOption("--reset-hard", null, options.resetHard(), false, arguments, false);
        }
        if (isApiVersionOrAfter(V084)) {
            fillOption("--stacktrace", "-d", options.showStacktrace(), false, arguments, false);
        }
        if (isApiVersionOrAfter(V081)) {
            if (options.customOptions() != null) {
                arguments.addAll(NBootUtils.nonNullStrList(options.customOptions()));
            }
        }
        //final options for execution
        if ((!config.isOmitDefaults() &&
                !NBootUtils.isEmptyList(options.applicationArguments())
                || !NBootUtils.nonNullStrList(options.executorOptions()).isEmpty())) {
            arguments.add(selectOptionName("--exec", "-e"));
        }
        arguments.addAll(NBootUtils.nonNullStrList(options.executorOptions()));
        arguments.addAll(NBootUtils.nonNullStrList(options.applicationArguments()));
        return new NBootCmdLine(arguments);
    }

    private boolean isApiVersionOrAfter(String version) {
        String apiVersionStr = config.getApiVersion();
        if (apiVersionStr == null) {
            return true;
        }
        NBootVersion apiVersionObj = NBootVersion.of(apiVersionStr);
        return apiVersionObj.compareTo(version) >= 0;
    }

}
