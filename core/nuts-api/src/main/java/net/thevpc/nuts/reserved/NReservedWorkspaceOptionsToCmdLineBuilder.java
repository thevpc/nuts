package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;
import net.thevpc.nuts.env.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.log.NLogConfig;

import java.io.File;
import java.util.*;

public class NReservedWorkspaceOptionsToCmdLineBuilder {
    private static final String V080 = "0.8.0";
    private static final String V081 = "0.8.1";
    private static final String V083 = "0.8.3";
    private static final String V084 = "0.8.4";
    private static final String V085 = "0.8.5";
    private NWorkspaceOptionsConfig config;
    private NWorkspaceOptions options;

    public NReservedWorkspaceOptionsToCmdLineBuilder(NWorkspaceOptionsConfig config, NWorkspaceOptions options) {
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
        if (!NBlankable.isBlank(value)) {
            fillOption0(selectOptionName(longName, shortName), value, arguments, forceSingle);
        }
    }

    private void fillOption(String longName, String shortName, int value, List<String> arguments, boolean forceSingle) {
        if (value > 0) {
            fillOption0(selectOptionName(longName, shortName), String.valueOf(value), arguments, forceSingle);
        }
    }

    private void fillOption(String longName, String shortName, Enum value, Class enumType, List<String> arguments, boolean forceSingle) {
        if (tryFillOptionShort(value, arguments, forceSingle)) {
            return;
        }
        if (value != null) {
            if (config.isShortOptions()) {
                if (value instanceof NOsFamily) {
                    switch ((NOsFamily) value) {
                        case LINUX: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("linux", "l"), arguments, forceSingle);
                            return;
                        }
                        case WINDOWS: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("windows", "w"), arguments, forceSingle);
                            return;
                        }
                        case MACOS: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("macos", "m"), arguments, forceSingle);
                            return;
                        }
                        case UNIX: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("unix", "u"), arguments, forceSingle);
                            return;
                        }
                        case UNKNOWN: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("unknown", "x"), arguments, forceSingle);
                            return;
                        }
                    }
                } else if (value instanceof NStoreStrategy) {
                    switch ((NStoreStrategy) value) {
                        case EXPLODED: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("exploded", "e"), arguments, forceSingle);
                            return;
                        }
                        case STANDALONE: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("standalone", "s"), arguments, forceSingle);
                            return;
                        }
                    }
                } else if (value instanceof NTerminalMode) {
                    switch ((NTerminalMode) value) {
                        case FILTERED: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("no", "n"), arguments, forceSingle);
                            return;
                        }
                        case INHERITED: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("inherited", "h"), arguments, forceSingle);
                            return;
                        }
                        case FORMATTED: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("yes", "y"), arguments, forceSingle);
                            return;
                        }
                        case DEFAULT: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("default", null), arguments, forceSingle);
                            return;
                        }
                    }
                }
            }
            NVersion apiVersionObj = config.getApiVersion();
            if (value instanceof NSupportMode) {
                if (!isApiVersionOrAfter(V084)) {
                    switch ((NSupportMode) value) {
                        case ALWAYS: {
                            fillOption0(selectOptionName(longName, shortName), "preferred", arguments, forceSingle);
                            return;
                        }
                        case NEVER: {
                            fillOption0(selectOptionName(longName, shortName), "unsupported", arguments, forceSingle);
                            return;
                        }
                    }
                }
            }
            if (value instanceof NEnum) {
                fillOption0(selectOptionName(longName, shortName), ((NEnum) value).id(), arguments, forceSingle);
            } else {
                fillOption0(selectOptionName(longName, shortName), value.toString().toLowerCase(), arguments, forceSingle);
            }
        }
    }

    private boolean fillOption(NRunAs value, List<String> arguments) {
        if (value == null) {
            return false;
        }
        NVersion apiVersion = options.getApiVersion().orNull();
        switch (value.getMode()) {
            case CURRENT_USER: {
                if (isApiVersionOrAfter(V081)) {
                    if (!config.isOmitDefaults()) {
                        arguments.add("--current-user");
                    }
                } else {
                    arguments.add("--user-cmd");
                }
                return true;
            }
            case ROOT: {
                if (isApiVersionOrAfter(V081)) {
                    arguments.add("--as-root");
                } else {
                    arguments.add("--root-cmd");
                }
                return true;
            }
            case SUDO: {
                if (isApiVersionOrAfter(V081)) {
                    arguments.add("--sudo");
                }
                return true;
            }
            case USER: {
                if (isApiVersionOrAfter(V081)) {
                    arguments.add("--run-as=" + value.getUser());
                }
                return true;
            }
            default: {
                throw new UnsupportedOperationException("unsupported " + value.getMode());
            }
        }
    }

    private boolean tryFillOptionShort(Enum value, List<String> arguments, boolean forceSingle) {
        NVersion apiVersion = options.getApiVersion().orNull();
        if (value != null) {
            if (config.isShortOptions()) {
                if (value instanceof NOpenMode) {
                    switch ((NOpenMode) value) {
                        case OPEN_OR_ERROR: {
                            fillOption0(selectOptionName("--open-mode", "-o"), selectOptionVal("open-or-error", "r"), arguments, forceSingle);
                            return true;
                        }
                        case CREATE_OR_ERROR: {
                            fillOption0(selectOptionName("--open-mode", "-o"), selectOptionVal("create-or-error", "w"), arguments, forceSingle);
                            return true;
                        }
                        case OPEN_OR_CREATE: {
                            if (!config.isOmitDefaults()) {
                                fillOption0(selectOptionName("--open-mode", "-o"), selectOptionVal("open-or-create", "rw"), arguments, forceSingle);
                            }
                            return true;
                        }
                        case OPEN_OR_NULL: {
                            fillOption0(selectOptionName("--open-mode", "-o"), selectOptionVal("open-or-null", "on"), arguments, forceSingle);
                            return true;
                        }
                    }
                }
                if (value instanceof NExecutionType) {
                    switch ((NExecutionType) value) {
                        case SYSTEM: {
                            if (isApiVersionOrAfter(V081)) {
                                arguments.add("--system");
                            } else {
                                arguments.add("--user-cmd");
                            }
                            return true;
                        }
                        case EMBEDDED: {
                            arguments.add(selectOptionName("--embedded", "-b"));
                            return true;
                        }
                        case SPAWN: {
                            if (!config.isOmitDefaults()) {
                                arguments.add(selectOptionName("--spawn", "-x"));
                            }
                            return true;
                        }
                        case OPEN: {
                            arguments.add(selectOptionName("--open-file", "--open-file"));
                            return true;
                        }
                    }
                }
                if (value instanceof NConfirmationMode) {
                    switch ((NConfirmationMode) value) {
                        case YES: {
                            arguments.add(selectOptionName("--yes", "-y"));
                            return true;
                        }
                        case NO: {
                            arguments.add(selectOptionName("-no", "-n"));
                            return true;
                        }
                        case ASK: {
                            if (!config.isOmitDefaults()) {
                                arguments.add("--ask");
                                return true;
                            }
                            break;
                        }
                        case ERROR: {
                            arguments.add("--error");
                            return true;
                        }
                    }
                }
                if (value instanceof NTerminalMode) {
                    NVersion apiVersionObj = config.getApiVersion();
                    switch ((NTerminalMode) value) {
                        case FILTERED: {
                            if (isApiVersionOrAfter(V084)) {
                                arguments.add("--color=filtered");
                            } else {
                                arguments.add(selectOptionName("--!color", "-!c"));
                            }
                            return true;
                        }
                        case FORMATTED: {
                            arguments.add(selectOptionName("--color", "-c"));
                            return true;
                        }
                        case INHERITED: {
                            arguments.add(selectOptionName("--color=inherited", "-c=h"));
                            return true;
                        }
                        case ANSI: {
                            arguments.add(selectOptionName("--color=ansi", "-c=a"));
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void fillOption(Enum value, List<String> arguments, boolean forceSingle) {
        if (value != null) {
            if (tryFillOptionShort(value, arguments, forceSingle)) {
                return;
            }
            if (value instanceof NEnum) {
                arguments.add("--" + ((NEnum) value).id());
            } else {
                arguments.add("--" +
                        NNameFormat.CONST_NAME.format(value.name())
                );
            }
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


    public NCmdLine toCmdLine() {
        NVersion apiVersionObj = config.getApiVersion();
        List<String> arguments = new ArrayList<>();

        fillOption("--java", "-j", options.getJavaCommand().orNull(), arguments, false);
        fillOption("--java-options", "-O", options.getJavaOptions().orNull(), arguments, false);
        String wsString = options.getWorkspace().orNull();
        if (NBlankable.isBlank(wsString)) {
            //default workspace name
            wsString = "";
        } else if (wsString.contains("/") || wsString.contains("\\")) {
            //workspace path
            wsString = new File(wsString).toPath().toAbsolutePath().normalize().toString();
        } else {
            //workspace name
        }
        fillOption("--workspace", "-w", wsString, arguments, false);
        fillOption("--user", "-u", options.getUserName().orNull(), arguments, false);
        fillOption("--password", "-p", options.getCredentials().orNull(), arguments, false);
        fillOption("--boot-version", "-V", options.getApiVersion().map(Object::toString).orNull(), arguments, false);
        fillOption("--boot-runtime", null, options.getRuntimeId().map(Object::toString).orNull(), arguments, false);

        {
            NTerminalMode nTerminalMode = options.getTerminalMode().orNull();
            if (!isApiVersionOrAfter(V084)) {
                if (options.getBot().orElse(false)) {
                    //force filtered for older nuts
                    nTerminalMode = NTerminalMode.FILTERED;
                }
            }
            if (!(config.isOmitDefaults() && nTerminalMode == NTerminalMode.FORMATTED)) {
                fillOption("--color", "-c", nTerminalMode, NTerminalMode.class, arguments, true);
            }
        }
        NLogConfig logConfig = options.getLogConfig().orNull();
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
        fillOption("--exclude-extension", "-X", options.getExcludedExtensions().orElseGet(Collections::emptyList), ";", arguments, false);

        if (isApiVersionOrAfter(V081)) {
            fillOption("--repositories", "-r", options.getRepositories().orElseGet(Collections::emptyList), ";", arguments, false);
        } else {
            fillOption("--repository", "-r", options.getRepositories().orElseGet(Collections::emptyList), ";", arguments, false);
        }

        fillOption("--global", "-g", options.getSystem().orNull(), false, arguments, false);
        fillOption("--gui", null, options.getGui().orNull(), false, arguments, false);
        fillOption("--read-only", "-R", options.getReadOnly().orNull(), false, arguments, false);
        fillOption("--trace", "-t", options.getTrace().orNull(), true, arguments, false);
        fillOption("--progress", "-P", options.getProgressOptions().orNull(), arguments, true);
        fillOption("--solver", null, options.getDependencySolver().orNull(), arguments, false);
        if (isApiVersionOrAfter(V083)) {
            fillOption("--debug", null, options.getDebug().orNull(), arguments, true);
        } else {
            fillOption("--debug", null, options.getDebug().isPresent(), false, arguments, true);
        }
        fillOption("--install-companions", "-k", options.getInstallCompanions().orNull(), false, arguments, false);
        fillOption("--skip-welcome", "-K", options.getSkipWelcome().orElse(false), false, arguments, false);
        fillOption("--out-line-prefix", null, options.getOutLinePrefix().orNull(), arguments, false);
        fillOption("--skip-boot", "-Q", options.getSkipBoot().orNull(), false, arguments, false);
        fillOption("--cached", null, options.getCached().orNull(), true, arguments, false);
        fillOption("--indexed", null, options.getIndexed().orNull(), true, arguments, false);
        fillOption("--transitive", null, options.getTransitive().orNull(), true, arguments, false);
        if (isApiVersionOrAfter(V081)) {
            fillOption("--bot", "-B", options.getBot().orNull(), false, arguments, false);
        }
        if (isApiVersionOrAfter(V085)) {
            fillOption("--preview-repo", "-U", options.getPreviewRepo().orNull(), false, arguments, false);
            fillOption("--shared-instance", null, options.getSharedInstance().orNull(), false, arguments, false);
        }
        if (options.getFetchStrategy().isPresent() && options.getFetchStrategy().orNull() != NFetchStrategy.ONLINE) {
            fillOption("--fetch", "-f", options.getFetchStrategy().orNull(), NFetchStrategy.class, arguments, false);
        }
        fillOption(options.getConfirm().orNull(), arguments, false);
        fillOption(options.getOutputFormat().orNull(), arguments, false);
        for (String outputFormatOption : options.getOutputFormatOptions().orElseGet(Collections::emptyList)) {
            fillOption("--output-format-option", "-T", outputFormatOption, arguments, false);
        }
        if (isApiVersionOrAfter(V080)) {
            fillOption("--expire", "-N",
                    options.getExpireTime().map(Object::toString).orNull(),
                    arguments, false);
            if (options.getOutLinePrefix().isPresent()
                    && Objects.equals(options.getOutLinePrefix(), options.getErrLinePrefix())
                    && options.getOutLinePrefix().get().length() > 0) {
                fillOption("--line-prefix", null, options.getOutLinePrefix().orNull(), arguments, false);
            } else {
                if (options.getOutLinePrefix().isPresent() && options.getOutLinePrefix().get().length() > 0) {
                    fillOption("--out-line-prefix", null, options.getOutLinePrefix().orNull(), arguments, false);
                }
                if (options.getErrLinePrefix().isPresent() && options.getErrLinePrefix().get().length() > 0) {
                    fillOption("--err-line-prefix", null, options.getErrLinePrefix().orNull(), arguments, false);
                }
            }
        }
        if (isApiVersionOrAfter(V081)) {
            fillOption("--theme", null, options.getTheme().orNull(), arguments, false);
        }
        if (isApiVersionOrAfter(V081)) {
            fillOption("--locale", "-L", options.getLocale().orNull(), arguments, false);
        }
        if (isApiVersionOrAfter(V084)) {
            fillOption("--init-launchers", null, options.getInitLaunchers().orNull(), true, arguments, false);
            fillOption("--init-platforms", null, options.getInitLaunchers().orNull(), true, arguments, false);
            fillOption("--init-java", null, options.getInitLaunchers().orNull(), true, arguments, false);
            fillOption("--init-scripts", null, options.getInitLaunchers().orNull(), true, arguments, false);
            fillOption("--desktop-launcher", null, options.getDesktopLauncher().orNull(), NSupportMode.class, arguments, false);
            fillOption("--menu-launcher", null, options.getDesktopLauncher().orNull(), NSupportMode.class, arguments, false);
            fillOption("--user-launcher", null, options.getDesktopLauncher().orNull(), NSupportMode.class, arguments, false);
            fillOption("--isolation-level", null, options.getIsolationLevel().orNull(), NIsolationLevel.class, arguments, false);
        } else if (isApiVersionOrAfter(V081)) {
            fillOption("---init-launchers", null, options.getInitLaunchers().orNull(), true, arguments, false);
            fillOption("---init-platforms", null, options.getInitLaunchers().orNull(), true, arguments, false);
            fillOption("---init-java", null, options.getInitLaunchers().orNull(), true, arguments, false);
            fillOption("---init-scripts", null, options.getInitLaunchers().orNull(), true, arguments, false);
            fillOption("---system-desktop-launcher", null, options.getDesktopLauncher().orNull(), NSupportMode.class, arguments, false);
            fillOption("---system-menu-launcher", null, options.getDesktopLauncher().orNull(), NSupportMode.class, arguments, false);
            fillOption("---system-custom-launcher", null, options.getDesktopLauncher().orNull(), NSupportMode.class, arguments, false);
        }

        fillOption("--name", null, NStringUtils.trim(options.getName().orNull()), arguments, false);
        fillOption("--archetype", "-A", options.getArchetype().orNull(), arguments, false);
        fillOption("--store-layout", null, options.getStoreLayout().orNull(), NOsFamily.class, arguments, false);
        fillOption("--store-strategy", null, options.getStoreStrategy().orNull(), NStoreStrategy.class, arguments, false);
        fillOption("--repo-store-strategy", null, options.getRepositoryStoreStrategy().orNull(), NStoreStrategy.class, arguments, false);
        Map<NStoreType, String> storeLocations = options.getStoreLocations().orElseGet(Collections::emptyMap);
        for (NStoreType location : NStoreType.values()) {
            String s = storeLocations.get(location);
            if (!NBlankable.isBlank(s)) {
                fillOption("--" + location.id() + "-location", null, s, arguments, false);
            }
        }

        Map<NHomeLocation, String> homeLocations = options.getHomeLocations().orElseGet(Collections::emptyMap);
        if (homeLocations != null) {
            for (NStoreType location : NStoreType.values()) {
                String s = homeLocations.get(NHomeLocation.of(null, location));
                if (!NBlankable.isBlank(s)) {
                    fillOption("--system-" + location.id() + "-home", null, s, arguments, false);
                }
            }
            for (NOsFamily osFamily : NOsFamily.values()) {
                for (NStoreType location : NStoreType.values()) {
                    String s = homeLocations.get(NHomeLocation.of(osFamily, location));
                    if (!NBlankable.isBlank(s)) {
                        fillOption("--" + osFamily.id() + "-" + location.id() + "-home", null, s, arguments, false);
                    }
                }
            }
        }
        if (isApiVersionOrAfter(V080)) {
            if (options.getSwitchWorkspace().isPresent()) {
                fillOption("--switch", null, options.getSwitchWorkspace().orNull(), false, arguments, false);
            }
        }

        fillOption("--help", "-h", options.getCommandHelp().orElse(false), false, arguments, false);
        fillOption("--version", "-v", options.getCommandVersion().orElse(false), false, arguments, false);

        if (!(config.isOmitDefaults() && (options.getOpenMode().isNotPresent() || options.getOpenMode().orNull() == NOpenMode.OPEN_OR_CREATE))) {
            fillOption(options.getOpenMode().orNull(), arguments, false);
        }
        fillOption(options.getExecutionType().orNull(), arguments, false);
        fillOption(options.getRunAs().orNull(), arguments);
        fillOption("--reset", "-Z", options.getReset().orNull(), false, arguments, false);
        fillOption("--recover", "-z", options.getRecover().orNull(), false, arguments, false);
        fillOption("--dry", "-D", options.getDry().orNull(), false, arguments, false);
        if (isApiVersionOrAfter(V084)) {
            fillOption("--stacktrace", null, options.getShowStacktrace().orNull(), false, arguments, false);
        }
        if (isApiVersionOrAfter(V081)) {
            if (options.getCustomOptions() != null) {
                arguments.addAll(options.getCustomOptions().orElseGet(Collections::emptyList));
            }
        }
        //final options for execution
        if ((!config.isOmitDefaults() && options.getApplicationArguments().isPresent() && !options.getApplicationArguments().get().isEmpty())
                || !options.getExecutorOptions().orElseGet(Collections::emptyList).isEmpty()) {
            arguments.add(selectOptionName("--exec", "-e"));
        }
        arguments.addAll(options.getExecutorOptions().orElseGet(Collections::emptyList));
        arguments.addAll(options.getApplicationArguments().orElseGet(Collections::emptyList));
        return NCmdLine.of(arguments);
    }

    private boolean isApiVersionOrAfter(String version) {
        NVersion apiVersionObj = config.getApiVersion();
        return apiVersionObj == null || apiVersionObj.compareTo(version) >= 0;
    }

}
