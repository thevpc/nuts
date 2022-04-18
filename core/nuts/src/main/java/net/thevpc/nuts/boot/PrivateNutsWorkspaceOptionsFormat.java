/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

import java.util.*;

/**
 * @author thevpc
 * @app.category Internal
 */
final class PrivateNutsWorkspaceOptionsFormat {

    private static final long serialVersionUID = 1;
    private final NutsBootOptions options;
    private boolean exportedOptions;
    private boolean runtimeOptions;
    private boolean createOptions;
    private boolean shortOptions;
    private boolean singleArgOptions;
    private boolean omitDefaults;
    private String apiVersion;
    private NutsVersion apiVersionObj;

    public PrivateNutsWorkspaceOptionsFormat(NutsBootOptions options) {
        this.options = options;
    }

    
    public boolean isInit() {
        return createOptions;
    }

    
    public PrivateNutsWorkspaceOptionsFormat setInit(boolean e) {
        this.createOptions = true;
        return this;
    }

    
    public boolean isRuntime() {
        return runtimeOptions;
    }

    
    public PrivateNutsWorkspaceOptionsFormat setRuntime(boolean e) {
        this.runtimeOptions = true;
        return this;
    }

    
    public boolean isExported() {
        return exportedOptions;
    }

    
    public PrivateNutsWorkspaceOptionsFormat setExported(boolean e) {
        this.exportedOptions = true;
        return this;
    }

    
    public String getApiVersion() {
        return apiVersion;
    }

    
    public PrivateNutsWorkspaceOptionsFormat setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        this.apiVersionObj = null;
        return this;
    }

    
    public PrivateNutsCommandLine getBootCommandLine() {
        NutsVersion apiVersionObj = getApiVersionObj();
        List<String> arguments = new ArrayList<>();
        if (exportedOptions || isImplicitAll()) {
            fillOption("--boot-runtime", null, options.getRuntimeId(), arguments, false);
            fillOption("--java", "-j", options.getJavaCommand(), arguments, false);
            fillOption("--java-options", "-O", options.getJavaOptions(), arguments, false);
            String wsString = options.getWorkspace();
            if (NutsBlankable.isBlank(wsString)) {
                //default workspace name
                wsString = "";
            } else if (wsString.contains("/") || wsString.contains("\\")) {
                //workspace path
                wsString = PrivateNutsUtilIO.getAbsolutePath(wsString);
            } else {
                //workspace name
            }
            fillOption("--workspace", "-w", wsString, arguments, false);
            fillOption("--user", "-u", options.getUserName(), arguments, false);
            fillOption("--password", "-p", options.getCredentials(), arguments, false);
            fillOption("--boot-version", "-V", options.getApiVersion(), arguments, false);
            if (!(omitDefaults && options.getTerminalMode() == null)) {
                fillOption("--color", "-c", options.getTerminalMode(), NutsTerminalMode.class, arguments, true);
            }
            NutsLogConfig logConfig = options.getLogConfig();
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

            if (apiVersionObj == null || apiVersionObj.compareTo("0.8.1") >= 0) {
                fillOption("--repositories", "-r", options.getRepositories(), ";", arguments, false);
            } else {
                fillOption("--repository", "-r", options.getRepositories(), ";", arguments, false);
            }

            fillOption("--global", "-g", options.isGlobal(), false, arguments, false);
            fillOption("--gui", null, options.isGui(), false, arguments, false);
            fillOption("--read-only", "-R", options.isReadOnly(), false, arguments, false);
            fillOption("--trace", "-t", options.isTrace(), true, arguments, false);
            fillOption("--progress", "-P", options.getProgressOptions(), arguments, true);
            fillOption("--solver", null, options.getDependencySolver(), arguments, false);
            if (apiVersionObj == null || apiVersionObj.compareTo("0.8.3") >= 0) {
                fillOption("--debug", null, options.getDebug(), arguments, true);
            }else{
                fillOption("--debug", null, options.getDebug()!=null, true, arguments, true);
            }
            fillOption("--skip-companions", "-k", options.isSkipCompanions(), false, arguments, false);
            fillOption("--skip-welcome", "-K", options.isSkipWelcome(), false, arguments, false);
            fillOption("--out-line-prefix", null, options.getOutLinePrefix(), arguments, false);
            fillOption("--skip-boot", "-Q", options.isSkipBoot(), false, arguments, false);
            fillOption("--cached", null, options.isCached(), true, arguments, false);
            fillOption("--indexed", null, options.isIndexed(), true, arguments, false);
            fillOption("--transitive", null, options.isTransitive(), true, arguments, false);
            if (apiVersionObj == null || apiVersionObj.compareTo("0.8.1") >= 0) {
                fillOption("--bot", "-B", options.isBot(), false, arguments, false);
            }
            if (options.getFetchStrategy() != null && options.getFetchStrategy() != NutsFetchStrategy.ONLINE) {
                fillOption("--fetch", "-f", options.getFetchStrategy(), NutsFetchStrategy.class, arguments, false);
            }
            fillOption(options.getConfirm(), arguments, false);
            fillOption(options.getOutputFormat(), arguments, false);
            for (String outputFormatOption : options.getOutputFormatOptions()) {
                fillOption("--output-format-option", "-T", outputFormatOption, arguments, false);
            }
            if (apiVersionObj == null || apiVersionObj.compareTo("0.8.0") >= 0) {
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
            if (apiVersionObj == null || apiVersionObj.compareTo("0.8.1") >= 0) {
                fillOption("--theme", null, options.getTheme(), arguments, false);
            }
        }

        if (createOptions || isImplicitAll()) {
            fillOption("--name", null, NutsUtilStrings.trim(options.getName()), arguments, false);
            fillOption("--archetype", "-A", options.getArchetype(), arguments, false);
            fillOption("--store-layout", null, options.getStoreLocationLayout(), NutsOsFamily.class, arguments, false);
            fillOption("--store-strategy", null, options.getStoreLocationStrategy(), NutsStoreLocationStrategy.class, arguments, false);
            fillOption("--repo-store-strategy", null, options.getRepositoryStoreLocationStrategy(), NutsStoreLocationStrategy.class, arguments, false);
            Map<NutsStoreLocation, String> storeLocations = options.getStoreLocations();
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                String s = storeLocations.get(location);
                if (!NutsBlankable.isBlank(s)) {
                    fillOption("--" + location.id() + "-location", null, s, arguments, false);
                }
            }

            Map<NutsHomeLocation, String> homeLocations = options.getHomeLocations();
            if (homeLocations != null) {
                for (NutsStoreLocation location : NutsStoreLocation.values()) {
                    String s = homeLocations.get(NutsHomeLocation.of(null, location));
                    if (!NutsBlankable.isBlank(s)) {
                        fillOption("--system-" + location.id() + "-home", null, s, arguments, false);
                    }
                }
                for (NutsOsFamily osFamily : NutsOsFamily.values()) {
                    for (NutsStoreLocation location : NutsStoreLocation.values()) {
                        String s = homeLocations.get(NutsHomeLocation.of(osFamily, location));
                        if (!NutsBlankable.isBlank(s)) {
                            fillOption("--" + osFamily.id() + "-" + location.id() + "-home", null, s, arguments, false);
                        }
                    }
                }
            }
            if (apiVersionObj == null || apiVersionObj.compareTo("0.8.0") >= 0) {
                if (options.getSwitchWorkspace() != null) {
                    fillOption("--switch", null, options.getSwitchWorkspace(), false, arguments, false);
                }
            }
        }

        if (runtimeOptions || isImplicitAll()) {
            fillOption("--help", "-h", options.isCommandHelp(), false, arguments, false);
            fillOption("--version", "-v", options.isCommandVersion(), false, arguments, false);

            if (!(omitDefaults && (options.getOpenMode() == null || options.getOpenMode() == NutsOpenMode.OPEN_OR_CREATE))) {
                fillOption(options.getOpenMode(), arguments, false);
            }
            fillOption(options.getExecutionType(), arguments, false);
            fillOption(options.getRunAs(), arguments);
            fillOption("--reset", "-Z", options.isReset(), false, arguments, false);
            fillOption("--recover", "-z", options.isRecover(), false, arguments, false);
            fillOption("--dry", "-D", options.isDry(), false, arguments, false);
            if (apiVersionObj == null || apiVersionObj.compareTo("0.8.1") >= 0) {
                fillOption("--locale", "-L", options.getLocale(), arguments, false);
            }
            if (!omitDefaults || options.getExecutorOptions().size() > 0) {
                arguments.add(selectOptionName("--exec", "-e"));
            }
            arguments.addAll(options.getExecutorOptions());
            arguments.addAll(options.getApplicationArguments());
        }
        if (true || isImplicitAll()) {
            if (apiVersionObj == null || apiVersionObj.compareTo("0.8.1") >= 0) {
                if (options.getCustomOptions() != null) {
                    arguments.addAll(options.getCustomOptions());
                }
            }
        }
        return new PrivateNutsCommandLine(arguments.toArray(new String[0]));
    }

    
    public PrivateNutsWorkspaceOptionsFormat setCompact(boolean compact) {
        if (compact) {
            shortOptions = true;
            singleArgOptions = true;
            omitDefaults = true;
        } else {
            shortOptions = false;
            singleArgOptions = false;
            omitDefaults = false;
        }
        return this;
    }

    private boolean isImplicitAll() {
        return !exportedOptions && !runtimeOptions && !createOptions;
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

    private void fillOption(String longName, String shortName, boolean value, boolean defaultValue, List<String> arguments, boolean forceSingle) {
        if (defaultValue) {
            if (!value) {
                if (shortOptions && shortName != null) {
                    arguments.add("-!" + shortName.substring(1));
                } else {
                    arguments.add("--!" + longName.substring(2));
                }
            }
        } else {
            if (value) {
                arguments.add(selectOptionName(longName, shortName));
            }
        }
    }

    private void fillOption(String longName, String shortName, char[] value, List<String> arguments, boolean forceSingle) {
        if (value != null && new String(value).isEmpty()) {
            fillOption0(selectOptionName(longName, shortName), new String(value), arguments, forceSingle);
        }
    }

    private void fillOption(String longName, String shortName, String value, List<String> arguments, boolean forceSingle) {
        if (!NutsBlankable.isBlank(value)) {
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
            if (shortOptions) {
                if (value instanceof NutsOsFamily) {
                    switch ((NutsOsFamily) value) {
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
                } else if (value instanceof NutsStoreLocationStrategy) {
                    switch ((NutsStoreLocationStrategy) value) {
                        case EXPLODED: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("exploded", "e"), arguments, forceSingle);
                            return;
                        }
                        case STANDALONE: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("standalone", "s"), arguments, forceSingle);
                            return;
                        }
                    }
                } else if (value instanceof NutsTerminalMode) {
                    switch ((NutsTerminalMode) value) {
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
                    }
                }
            }
            fillOption0(selectOptionName(longName, shortName), value.toString().toLowerCase(), arguments, forceSingle);
        } else if (enumType.equals(NutsTerminalMode.class)) {
            fillOption0(selectOptionName(longName, shortName), shortOptions ? "s" : "system", arguments, forceSingle);
        }
    }

    private boolean fillOption(NutsRunAs value, List<String> arguments) {
        switch (value.getMode()) {
            case CURRENT_USER: {
                if (!NutsBlankable.isBlank(apiVersion) &&
                        NutsVersion.of(apiVersion).get().compareTo(NutsVersion.of("0.8.1").get())
                                < 0) {
                    arguments.add("--user-cmd");
                } else {
                    if (!omitDefaults) {
                        arguments.add("--current-user");
                    }
                }
                return true;
            }
            case ROOT: {
                if (!NutsBlankable.isBlank(apiVersion) &&
                        NutsVersion.of(apiVersion).get().compareTo(NutsVersion.of("0.8.1").get())
                                < 0) {
                    arguments.add("--root-cmd");
                } else {
                    arguments.add("--as-root");
                }
                return true;
            }
            case SUDO: {
                if (!NutsBlankable.isBlank(apiVersion) &&
                        NutsVersion.of(apiVersion).get().compareTo(NutsVersion.of("0.8.1").get())
                                < 0) {
                    //ignore
                } else {
                    arguments.add("--sudo");
                }
                return true;
            }
            case USER: {
                if (!NutsBlankable.isBlank(apiVersion) &&
                        NutsVersion.of(apiVersion).get().compareTo(NutsVersion.of("0.8.1").get())
                                < 0) {
                    //ignore
                } else {
                    arguments.add("--run-as=" + value.getUser());
                }
                return true;
            }
            default: {
                throw new NutsBootException(NutsMessage.cstyle("unsupported enum %s", value.getMode()));
            }
        }
    }

    private boolean tryFillOptionShort(Enum value, List<String> arguments, boolean forceSingle) {
        if (value != null) {
            if (shortOptions) {
                if (value instanceof NutsOpenMode) {
                    switch ((NutsOpenMode) value) {
                        case OPEN_OR_ERROR: {
                            fillOption0(selectOptionName("--open-mode", "-o"), selectOptionVal("open-or-error", "r"), arguments, forceSingle);
                            return true;
                        }
                        case CREATE_OR_ERROR: {
                            fillOption0(selectOptionName("--open-mode", "-o"), selectOptionVal("create-or-error", "w"), arguments, forceSingle);
                            return true;
                        }
                        case OPEN_OR_CREATE: {
                            if (!omitDefaults) {
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
                if (value instanceof NutsExecutionType) {
                    switch ((NutsExecutionType) value) {
                        case SYSTEM: {
                            if (!NutsBlankable.isBlank(apiVersion) &&
                                    NutsVersion.of(apiVersion).get().compareTo(NutsVersion.of("0.8.1").get())
                                            < 0) {
                                arguments.add("--user-cmd");
                            } else {
                                arguments.add("--system");
                            }
                            return true;
                        }
                        case EMBEDDED: {
                            arguments.add(selectOptionName("--embedded", "-b"));
                            return true;
                        }
                        case SPAWN: {
                            if (!omitDefaults) {
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
                if (value instanceof NutsConfirmationMode) {
                    switch ((NutsConfirmationMode) value) {
                        case YES: {
                            arguments.add(selectOptionName("--yes", "-y"));
                            return true;
                        }
                        case NO: {
                            arguments.add(selectOptionName("-no", "-n"));
                            return true;
                        }
                        case ASK: {
                            if (!omitDefaults) {
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
                if (value instanceof NutsTerminalMode) {
                    switch ((NutsTerminalMode) value) {
                        case FILTERED: {
                            arguments.add(selectOptionName("--!color", "-!c"));
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
            if (value instanceof NutsEnum) {
                arguments.add("--" + ((NutsEnum) value).id());
            } else {
                arguments.add("--" + value.toString().toLowerCase().replace('_', '-'));
            }
        }
    }

    private String selectOptionVal(String longName, String shortName) {
        if (shortOptions) {
            return shortName;
        }
        return longName;
    }

    private String selectOptionName(String longName, String shortName) {
        if (shortOptions && shortName != null) {
            return shortName;
        }
        return longName;
    }

    private void fillOption0(String name, String value, List<String> arguments, boolean forceSingle) {
        if (singleArgOptions || forceSingle) {
            arguments.add(name + "=" + value);
        } else {
            arguments.add(name);
            arguments.add(value);
        }
    }

    
    public int hashCode() {
        return Objects.hash(exportedOptions, runtimeOptions, createOptions, shortOptions, singleArgOptions, omitDefaults, options);
    }

    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PrivateNutsWorkspaceOptionsFormat that = (PrivateNutsWorkspaceOptionsFormat) o;
        return exportedOptions == that.exportedOptions
                && runtimeOptions == that.runtimeOptions
                && createOptions == that.createOptions
                && shortOptions == that.shortOptions
                && singleArgOptions == that.singleArgOptions
                && omitDefaults == that.omitDefaults
                && Objects.equals(apiVersion, that.apiVersion)
                && Objects.equals(options, that.options)
                ;
    }

    
    public String toString() {
        return getBootCommandLine().toString();
    }

    public NutsVersion getApiVersionObj() {
        if (apiVersionObj == null) {
            if (apiVersion != null) {
                apiVersionObj = NutsVersion.of(apiVersion).get();
            }
        }
        return apiVersionObj;
    }
}
