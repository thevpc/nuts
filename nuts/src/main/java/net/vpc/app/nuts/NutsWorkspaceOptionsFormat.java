/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.*;

/**
 *
 * @author vpc
 */
public class NutsWorkspaceOptionsFormat implements Serializable {
    private static final long serialVersionUID = 1;

    private boolean exportedOptions;
    private boolean runtimeOptions;
    private boolean createOptions;
    private boolean shortOptions;
    private boolean singleArgOptions;
    private boolean omitDefaults;
    private final NutsWorkspaceOptions options;

    public NutsWorkspaceOptionsFormat(NutsWorkspaceOptions options) {
        this.options = options;
    }

    public boolean isInit() {
        return createOptions;
    }

    public boolean isRuntime() {
        return runtimeOptions;
    }

    public boolean isExported() {
        return exportedOptions;
    }

    public NutsWorkspaceOptionsFormat exported() {
        return exported(true);
    }

    public NutsWorkspaceOptionsFormat exported(boolean e) {
        return setExported(e);
    }

    public NutsWorkspaceOptionsFormat setExported(boolean e) {
        this.exportedOptions = true;
        return this;
    }

    public NutsWorkspaceOptionsFormat runtime() {
        return runtime(true);
    }

    public NutsWorkspaceOptionsFormat runtime(boolean e) {
        return setRuntime(e);
    }

    public NutsWorkspaceOptionsFormat setRuntime(boolean e) {
        this.runtimeOptions = true;
        return this;
    }

    public NutsWorkspaceOptionsFormat init() {
        return init(true);
    }

    public NutsWorkspaceOptionsFormat init(boolean e) {
        return setInit(e);
    }

    public NutsWorkspaceOptionsFormat setInit(boolean e) {
        this.createOptions = true;
        return this;
    }

    private boolean isImplicitAll() {
        return !exportedOptions && !runtimeOptions && !createOptions;
    }

    public String getBootCommandLine() {
        return PrivateNutsCommandLine.escapeArguments(getBootCommand());
    }

    public String[] getBootCommand() {
        List<String> arguments = new ArrayList<>();
        if (exportedOptions || isImplicitAll()) {
            fillOption("--boot-runtime", null, options.getRuntimeId(), arguments, false);
            fillOption("--java", "-j", options.getJavaCommand(), arguments, false);
            fillOption("--java-options", "-O", options.getJavaOptions(), arguments, false);
            String wsString = options.getWorkspace();
            if(PrivateNutsUtils.isBlank(wsString)){
                //default workspace name
                wsString="";
            }else if(wsString.contains("/") || wsString.contains("\\")){
                //workspace path
                wsString=PrivateNutsUtils.getAbsolutePath(wsString);
            }else{
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
                if (logConfig.getLogTermLevel() != null && logConfig.getLogTermLevel()== logConfig.getLogFileLevel()) {
                    fillOption("--log-" + logConfig.getLogFileLevel().toString().toLowerCase(), null, true, false, arguments, false);
                }else {
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
                fillOption("--log-inherited", null, logConfig.isLogInherited(), false, arguments, false);
            }
            fillOption("--exclude-extension", null, options.getExcludedExtensions(), ";", arguments, false);
            fillOption("--exclude-repository", null, options.getExcludedRepositories(), ";", arguments, false);
            fillOption("--repository", "-r", options.getTransientRepositories(), ";", arguments, false);
            fillOption("--global", "-g", options.isGlobal(), false, arguments, false);
            fillOption("--gui", null, options.isGui(), false, arguments, false);
            fillOption("--read-only", "-R", options.isReadOnly(), false, arguments, false);
            fillOption("--trace", "-t", options.isTrace(), true, arguments, false);
            fillOption("--skip-companions", "-k", options.isSkipCompanions(), false, arguments, false);
            fillOption("--skip-welcome", "-K", options.isSkipWelcome(), false, arguments, false);
            fillOption(options.getConfirm(), arguments, false);
            fillOption(options.getOutputFormat(), arguments, false);
            for (String outputFormatOption : options.getOutputFormatOptions()) {
                fillOption("--output-format-option", "-T", outputFormatOption, arguments, false);
            }
        }

        if (createOptions || isImplicitAll()) {
            fillOption("--name", null, PrivateNutsUtils.trim(options.getName()), arguments, false);
            fillOption("--archetype", "-A", options.getArchetype(), arguments, false);
            fillOption("--store-layout", null, options.getStoreLocationLayout(), NutsOsFamily.class, arguments, false);
            fillOption("--store-strategy", null, options.getStoreLocationStrategy(), NutsStoreLocationStrategy.class, arguments, false);
            fillOption("--repo-store-strategy", null, options.getRepositoryStoreLocationStrategy(), NutsStoreLocationStrategy.class, arguments, false);
            Map<String, String> storeLocations = options.getStoreLocations();
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                String s = storeLocations.get(location.id());
                if (!PrivateNutsUtils.isBlank(s)) {
                    fillOption("--" + location.id() + "-location", null, s, arguments, false);
                }
            }

            Map<String, String> homeLocations = options.getHomeLocations();
            if (homeLocations != null) {
                for (NutsStoreLocation location : NutsStoreLocation.values()) {
                    String s = homeLocations.get(NutsDefaultWorkspaceOptions.createHomeLocationKey(null, location));
                    if (!PrivateNutsUtils.isBlank(s)) {
                        fillOption("--system-" + location.id() + "-home", null, s, arguments, false);
                    }
                }
                for (NutsOsFamily osFamily : NutsOsFamily.values()) {
                    for (NutsStoreLocation location : NutsStoreLocation.values()) {
                        String s = homeLocations.get(NutsDefaultWorkspaceOptions.createHomeLocationKey(osFamily, location));
                        if (!PrivateNutsUtils.isBlank(s)) {
                            fillOption("--" + osFamily.id() + "-" + location.id() + "-home", null, s, arguments, false);
                        }
                    }
                }
            }
        }

        if (runtimeOptions || isImplicitAll()) {
            if (!(omitDefaults && (options.getOpenMode() == null || options.getOpenMode() == NutsWorkspaceOpenMode.OPEN_OR_CREATE))) {
                fillOption(options.getOpenMode(), arguments, false);
            }
            fillOption(options.getExecutionType(), arguments, false);
            fillOption("--reset", "-Z", options.isReset(), false, arguments, false);
            fillOption("--debug", "-z", options.isRecover(), false, arguments, false);
            fillOption("--dry", "-D", options.isDry(), false, arguments, false);
            if (!omitDefaults || options.getExecutorOptions().length > 0) {
                arguments.add(selectOptionName("--exec", "-e"));
            }
            arguments.addAll(Arrays.asList(options.getExecutorOptions()));
            arguments.addAll(Arrays.asList(options.getApplicationArguments()));
        }
        return arguments.toArray(new String[0]);
    }

    public NutsWorkspaceOptionsFormat compact() {
        return compact(true);
    }

    public NutsWorkspaceOptionsFormat compact(boolean compact) {
        return setCompact(compact);
    }

    public NutsWorkspaceOptionsFormat setCompact(boolean compact) {
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

    private void fillOption(String longName, String shortName, String[] values, String sep, List<String> arguments, boolean forceSingle) {
        if (values != null && values.length > 0) {
            fillOption0(selectOptionName(longName, shortName), String.join(sep, values), arguments, forceSingle);
        }
    }

    private void fillOption(String longName, String shortName, boolean value, boolean defaultValue, List<String> arguments, boolean forceSingle) {
        if(defaultValue){
            if (!value) {
                if (shortOptions && shortName != null) {
                    arguments.add("-!"+shortName.substring(1));
                }else {
                    arguments.add("--!"+longName.substring(2));
                }
            }
        }else{
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
        if (!PrivateNutsUtils.isBlank(value)) {
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
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("l", "linux"), arguments, forceSingle);
                            return;
                        }
                        case WINDOWS: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("w", "windows"), arguments, forceSingle);
                            return;
                        }
                        case MACOS: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("m", "macos"), arguments, forceSingle);
                            return;
                        }
                        case UNIX: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("u", "unix"), arguments, forceSingle);
                            return;
                        }
                        case UNKNOWN: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("x", "unknown"), arguments, forceSingle);
                            return;
                        }
                    }
                } else if (value instanceof NutsStoreLocationStrategy) {
                    switch ((NutsStoreLocationStrategy) value) {
                        case EXPLODED: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("e", "exploded"), arguments, forceSingle);
                            return;
                        }
                        case STANDALONE: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("s", "standalone"), arguments, forceSingle);
                            return;
                        }
                    }
                } else if (value instanceof NutsTerminalMode) {
                    switch ((NutsTerminalMode) value) {
                        case FILTERED: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("n", "no"), arguments, forceSingle);
                            return;
                        }
                        case INHERITED: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("h", "inherited"), arguments, forceSingle);
                            return;
                        }
                        case FORMATTED: {
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("y", "yes"), arguments, forceSingle);
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

    private boolean tryFillOptionShort(Enum value, List<String> arguments, boolean forceSingle) {
        if (value != null) {
            if (shortOptions) {
                if (value instanceof NutsWorkspaceOpenMode) {
                    switch ((NutsWorkspaceOpenMode) value) {
                        case OPEN_EXISTING: {
                            fillOption0("-o", "r", arguments, forceSingle);
                            return true;
                        }
                        case CREATE_NEW: {
                            fillOption0("-p", "w", arguments, forceSingle);
                            return true;
                        }
                        case OPEN_OR_CREATE: {
                            if (!omitDefaults) {
                                fillOption0("-o", "rw", arguments, forceSingle);
                            }
                            return true;
                        }
                    }
                }
                if (value instanceof NutsExecutionType) {
                    switch ((NutsExecutionType) value) {
                        case SYSCALL: {
                            arguments.add("-s");
                            return true;
                        }
                        case EMBEDDED: {
                            arguments.add("-b");
                            return true;
                        }
                        case SPAWN: {
                            if (!omitDefaults) {
                                arguments.add("-x");
                            }
                            return true;
                        }
                    }
                }
                if (value instanceof NutsConfirmationMode) {
                    switch ((NutsConfirmationMode) value) {
                        case YES: {
                            arguments.add("-y");
                            return true;
                        }
                        case NO: {
                            arguments.add("-n");
                            return true;
                        }
                        case ASK: {
                            if (!omitDefaults) {
                                arguments.add("--ask");
                                return true;
                            }
                            break;
                        }
                    }
                }
                if (value instanceof NutsTerminalMode) {
                    switch ((NutsTerminalMode) value) {
                        case FILTERED: {
                            arguments.add("-C");
                            return true;
                        }
                        case FORMATTED: {
                            arguments.add("-c");
                            return true;
                        }
                        case INHERITED: {
                            arguments.add("-c=h");
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
            arguments.add("--" + value.toString().toLowerCase().replace('_', '-'));
        }
    }

    private String selectOptionVal(String shortName, String longName) {
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

    @Override
    public String toString() {
        return getBootCommandLine();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsWorkspaceOptionsFormat that = (NutsWorkspaceOptionsFormat) o;
        return exportedOptions == that.exportedOptions &&
                runtimeOptions == that.runtimeOptions &&
                createOptions == that.createOptions &&
                shortOptions == that.shortOptions &&
                singleArgOptions == that.singleArgOptions &&
                omitDefaults == that.omitDefaults &&
                Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exportedOptions, runtimeOptions, createOptions, shortOptions, singleArgOptions, omitDefaults, options);
    }


}
