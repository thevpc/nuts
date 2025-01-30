package net.thevpc.nuts.boot.reserved.cmdline;

import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootHomeLocation;
import net.thevpc.nuts.boot.NBootLogConfig;
import net.thevpc.nuts.boot.NBootVersion;
import net.thevpc.nuts.boot.reserved.util.NBootPlatformHome;
import net.thevpc.nuts.boot.reserved.util.NBootUtils;

import java.io.File;
import java.util.*;

public class NBootWorkspaceCmdLineFormatter {
    private static final String V080 = "0.8.0";
    private static final String V081 = "0.8.1";
    private static final String V083 = "0.8.3";
    private static final String V084 = "0.8.4";
    private static final String V085 = "0.8.5";
    private NBootWorkspaceOptionsConfig config;
    private NBootOptionsInfo options;

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
            fillOption0(selectOptionName(longName, shortName), value.toString().toLowerCase(), arguments, forceSingle);
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

        fillOption("--java", "-j", options.getJavaCommand(), arguments, false);
        fillOption("--java-options", "-O", options.getJavaOptions(), arguments, false);
        String wsString = options.getWorkspace();
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
        fillOption("--user", "-u", options.getUserName(), arguments, false);
        fillOption("--password", "-p", options.getCredentials(), arguments, false);
        fillOption("--boot-version", "-V", options.getApiVersion(), arguments, false);
        fillOption("--boot-runtime", null, options.getRuntimeId(), arguments, false);

        {
            String nTerminalMode = options.getTerminalMode();
            if (!isApiVersionOrAfter(V084)) {
                if (NBootUtils.firstNonNull(options.getBot(), false)) {
                    //force filtered for older nuts
                    nTerminalMode = "FILTERED";
                }
            }
            if (!(config.isOmitDefaults() && NBootUtils.sameEnum(nTerminalMode, "FORMATTED"))) {
                fillOptionEnum("--color", "-c", nTerminalMode, "NTerminalMode", arguments, true);
            }
        }
        NBootLogConfig logConfig = options.getLogConfig();
        if (logConfig != null) {
            if (logConfig.getLogTermLevel() != null && logConfig.getLogTermLevel() == logConfig.getLogFileLevel()) {
                fillOption("--log-" + logConfig.getLogFileLevel().toString().toLowerCase(), null, true, false, arguments, false);
            } else {
                if (logConfig.getLogTermLevel() != null) {
                    fillOption("--log-term-" + logConfig.getLogTermLevel().toString().toLowerCase(), null, true, false, arguments, false);
                }
                if (logConfig.getLogFileLevel() != null) {
                    fillOption("--log-file-" + logConfig.getLogFileLevel().toString().toLowerCase(), null, true, false, arguments, false);
                }
            }
            if (logConfig.getLogFileCount() > 0) {
                fillOption("--log-file-count", null, String.valueOf(logConfig.getLogFileCount()), arguments, false);
            }
            fillOption("--log-file-size", null, logConfig.getLogFileSize(), arguments, false);
            fillOption("--log-file-base", null, logConfig.getLogFileBase(), arguments, false);
            fillOption("--log-file-name", null, logConfig.getLogFileName(), arguments, false);
        }
        fillOption("--exclude-extension", "-X", options.getExcludedExtensions(), ";", arguments, false);

        if (isApiVersionOrAfter(V081)) {
            fillOption("--repositories", "-r", options.getRepositories(), ";", arguments, false);
        } else {
            fillOption("--repository", "-r", options.getRepositories(), ";", arguments, false);
        }

        fillOption("--global", "-g", options.getSystem(), false, arguments, false);
        fillOption("--gui", null, options.getGui(), false, arguments, false);
        fillOption("--read-only", "-R", options.getReadOnly(), false, arguments, false);
        fillOption("--trace", "-t", options.getTrace(), true, arguments, false);
        fillOption("--progress", "-P", options.getProgressOptions(), arguments, true);
        fillOption("--solver", null, options.getDependencySolver(), arguments, false);
        if (isApiVersionOrAfter(V083)) {
            fillOption("--debug", null, options.getDebug(), arguments, true);
        } else {
            fillOption("--debug", null, options.getDebug() != null, false, arguments, true);
        }
        fillOption("--install-companions", "-k", options.getInstallCompanions(), false, arguments, false);
        fillOption("--skip-welcome", "-K", NBootUtils.firstNonNull(options.getSkipWelcome(), false), false, arguments, false);
        fillOption("--out-line-prefix", null, options.getOutLinePrefix(), arguments, false);
        fillOption("--skip-boot", "-Q", options.getSkipBoot(), false, arguments, false);
        fillOption("--cached", null, options.getCached(), true, arguments, false);
        fillOption("--indexed", null, options.getIndexed(), true, arguments, false);
        fillOption("--transitive", null, options.getTransitive(), true, arguments, false);
        if (isApiVersionOrAfter(V081)) {
            fillOption("--bot", "-B", options.getBot(), false, arguments, false);
        }
        if (isApiVersionOrAfter(V085)) {
            fillOption("--preview-repo", "-U", options.getPreviewRepo(), false, arguments, false);
            fillOption("--shared-instance", null, options.getSharedInstance(), false, arguments, false);
        }
        if (options.getFetchStrategy() != null && NBootUtils.sameEnum(options.getFetchStrategy(), "ONLINE")) {
            fillOptionEnum("--fetch", "-f", options.getFetchStrategy(), "NFetchStrategy", arguments, false);
        }
        fillOptionEnum(options.getConfirm(), "NConfirmationMode", arguments, false);
        fillOptionEnum(options.getOutputFormat(), "NContentType", arguments, false);
        if (options.getOutputFormatOptions() != null) {
            for (String outputFormatOption : options.getOutputFormatOptions()) {
                fillOption("--output-format-option", "-T", outputFormatOption, arguments, false);
            }
        }
        if (isApiVersionOrAfter(V080)) {
            fillOption("--expire", "-N",
                    options.getExpireTime() == null ? null : options.getExpireTime().toString(),
                    arguments, false);
            if (options.getOutLinePrefix() != null
                    && Objects.equals(options.getOutLinePrefix(), options.getErrLinePrefix())
                    && options.getOutLinePrefix().length() > 0) {
                fillOption("--line-prefix", null, options.getOutLinePrefix(), arguments, false);
            } else {
                if (options.getOutLinePrefix() != null && options.getOutLinePrefix().length() > 0) {
                    fillOption("--out-line-prefix", null, options.getOutLinePrefix(), arguments, false);
                }
                if (options.getErrLinePrefix() != null && options.getErrLinePrefix().length() > 0) {
                    fillOption("--err-line-prefix", null, options.getErrLinePrefix(), arguments, false);
                }
            }
        }
        if (isApiVersionOrAfter(V081)) {
            fillOption("--theme", null, options.getTheme(), arguments, false);
        }
        if (isApiVersionOrAfter(V081)) {
            fillOption("--locale", "-L", options.getLocale(), arguments, false);
        }
        if (isApiVersionOrAfter(V084)) {
            fillOption("--init-launchers", null, options.getInitLaunchers(), true, arguments, false);
            fillOption("--init-platforms", null, options.getInitLaunchers(), true, arguments, false);
            fillOption("--init-java", null, options.getInitLaunchers(), true, arguments, false);
            fillOption("--init-scripts", null, options.getInitLaunchers(), true, arguments, false);
            fillOptionEnum("--desktop-launcher", null, options.getDesktopLauncher(), "NSupportMode", arguments, false);
            fillOptionEnum("--menu-launcher", null, options.getDesktopLauncher(), "NSupportMode", arguments, false);
            fillOptionEnum("--user-launcher", null, options.getDesktopLauncher(), "NSupportMode", arguments, false);
            fillOptionEnum("--isolation-level", null, options.getIsolationLevel(), "NIsolationLevel", arguments, false);
        } else if (isApiVersionOrAfter(V081)) {
            fillOption("---init-launchers", null, options.getInitLaunchers(), true, arguments, false);
            fillOption("---init-platforms", null, options.getInitLaunchers(), true, arguments, false);
            fillOption("---init-java", null, options.getInitLaunchers(), true, arguments, false);
            fillOption("---init-scripts", null, options.getInitLaunchers(), true, arguments, false);
            fillOptionEnum("---system-desktop-launcher", null, options.getDesktopLauncher(), "NSupportMode", arguments, false);
            fillOptionEnum("---system-menu-launcher", null, options.getDesktopLauncher(), "NSupportMode", arguments, false);
            fillOptionEnum("---system-custom-launcher", null, options.getDesktopLauncher(), "NSupportMode", arguments, false);
        }

        fillOption("--name", null, NBootUtils.trim(options.getName()), arguments, false);
        fillOption("--archetype", "-A", options.getArchetype(), arguments, false);
        fillOptionEnum("--store-layout", null, options.getStoreLayout(), "NOsFamily", arguments, false);
        fillOptionEnum("--store-strategy", null, options.getStoreStrategy(), "NStoreStrategy", arguments, false);
        fillOptionEnum("--repo-store-strategy", null, options.getRepositoryStoreStrategy(), "NStoreStrategy", arguments, false);
        Map<String, String> storeLocations = options.getStoreLocations();
        if (storeLocations == null) {
            storeLocations = new HashMap<>();
        }
        for (String location : NBootPlatformHome.storeTypes()) {
            String s = storeLocations.get(location);
            if (!NBootUtils.isBlank(s)) {
                fillOption("--" + NBootUtils.enumId(location) + "-location", null, s, arguments, false);
            }
        }

        Map<NBootHomeLocation, String> homeLocations = options.getHomeLocations();
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
            if (options.getSwitchWorkspace() != null) {
                fillOption("--switch", null, options.getSwitchWorkspace(), false, arguments, false);
            }
        }

        fillOption("--help", "-h", NBootUtils.firstNonNull(options.getCommandHelp(), false), false, arguments, false);
        fillOption("--version", "-v", NBootUtils.firstNonNull(options.getCommandVersion(), false), false, arguments, false);

        if (!(config.isOmitDefaults() && (options.getOpenMode() == null) || NBootUtils.sameEnum(options.getOpenMode(), "OPEN_OR_CREATE"))) {
            fillOptionEnum(options.getOpenMode(), "NOpenMode", arguments, false);
        }
        fillOptionEnum(options.getExecutionType(), "NExecutionType", arguments, false);
        fillOptionEnumRunAs(options.getRunAs(), arguments);
        fillOption("--reset", "-Z", options.getReset(), false, arguments, false);
        fillOption("--recover", "-z", options.getRecover(), false, arguments, false);
        fillOption("--dry", "-D", options.getDry(), false, arguments, false);
        if (isApiVersionOrAfter(V085)) {
            fillOption("--reset-hard", null, options.getResetHard(), false, arguments, false);
        }
        if (isApiVersionOrAfter(V084)) {
            fillOption("--stacktrace", null, options.getShowStacktrace(), false, arguments, false);
        }
        if (isApiVersionOrAfter(V081)) {
            if (options.getCustomOptions() != null) {
                arguments.addAll(NBootUtils.nonNullStrList(options.getCustomOptions()));
            }
        }
        //final options for execution
        if ((!config.isOmitDefaults() &&
                !NBootUtils.isEmptyList(options.getApplicationArguments())
                || !NBootUtils.nonNullStrList(options.getExecutorOptions()).isEmpty())) {
            arguments.add(selectOptionName("--exec", "-e"));
        }
        arguments.addAll(NBootUtils.nonNullStrList(options.getExecutorOptions()));
        arguments.addAll(NBootUtils.nonNullStrList(options.getApplicationArguments()));
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
