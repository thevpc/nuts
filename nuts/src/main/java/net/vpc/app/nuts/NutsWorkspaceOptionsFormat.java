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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author vpc
 */
public class NutsWorkspaceOptionsFormat {

    private boolean exportedOptions;
    private boolean runtimeOptions;
    private boolean createOptions;
    private boolean shortOptions;
    private boolean singleArgOptions;
    private boolean omitDefaults;
    private NutsWorkspaceOptions options;

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
        return NutsUtilsLimited.escapeArguments(getBootCommand());
    }

    public String[] getBootCommand() {
        List<String> arguments = new ArrayList<>();
        if (exportedOptions || isImplicitAll()) {
            String[] homeLocations = options.getHomeLocations();
            for (int i = 0; i < NutsStoreLocationLayout.values().length; i++) {
                for (int j = 0; j < NutsStoreLocation.values().length; j++) {
                    String s = homeLocations[i * NutsStoreLocation.values().length + j];
                    if (!NutsUtilsLimited.isBlank(s)) {
                        NutsStoreLocationLayout layout = NutsStoreLocationLayout.values()[i];
                        NutsStoreLocation folder = NutsStoreLocation.values()[j];
                        //config is exported!
                        if ((folder == NutsStoreLocation.CONFIG)) {
                            fillOption("--" + layout.name().toLowerCase() + "-" + folder.name().toLowerCase() + "-home", null, s, arguments);
                        }
                    }
                }
            }
            fillOption("--boot-runtime", null, options.getBootRuntime(), arguments);
            fillOption("--runtime-source-url", null, options.getBootRuntimeSourceURL(), arguments);
            fillOption("--java", "-j", options.getBootJavaCommand(), arguments);
            fillOption("--java-options", "-O", options.getBootJavaOptions(), arguments);
            fillOption("--workspace", "-w", NutsUtilsLimited.isBlank(options.getWorkspace()) ? "" : NutsUtilsLimited.getAbsolutePath(options.getWorkspace()), arguments);
            fillOption("--user", "-u", options.getUserName(), arguments);
            fillOption("--password", "-p", options.getPassword(), arguments);
            fillOption("--boot-version", "-V", options.getRequiredBootVersion(), arguments);
            fillOption("--term", "-t", options.getTerminalMode(), arguments);
            if (options.getLogConfig() != null) {
                if (options.getLogConfig().getLogLevel() != null) {
                    if (options.getLogConfig().getLogLevel() == Level.FINEST) {
                        if (options.getLogConfig().isDebug()) {
                            fillOption("--debug", null, true, arguments);
                        } else {
                            fillOption("--verbose", null, true, arguments);
                        }
                    } else {
                        fillOption("--log-" + options.getLogConfig().getLogLevel().toString().toLowerCase(), null, true, arguments);
                    }
                }
                if (options.getLogConfig().getLogCount() > 0) {
                    fillOption("--log-count", null, String.valueOf(options.getLogConfig().getLogCount()), arguments);
                }
                fillOption("--log-size", null, options.getLogConfig().getLogSize(), arguments);
                fillOption("--log-folder", null, options.getLogConfig().getLogFolder(), arguments);
                fillOption("--log-name", null, options.getLogConfig().getLogName(), arguments);
                fillOption("--log-inherited", null, options.getLogConfig().isLogInherited(), arguments);
            }
            fillOption("--exclude-extension", null, options.getExcludedExtensions(), ";", arguments);
            fillOption("--exclude-repository", null, options.getExcludedRepositories(), ";", arguments);
            fillOption("--repository", "-r", options.getTransientRepositories(), ";", arguments);
            fillOption("--global", "-g", options.isGlobal(), arguments);
            fillOption("--gui", null, options.isGui(), arguments);
            fillOption("--read-only", "-R", options.isReadOnly(), arguments);
            fillOption("--skip-install-companions", "-k", options.isSkipInstallCompanions(), arguments);
            fillOption(options.getConfirm(), arguments);
            fillOption(options.getOutputFormat(), arguments);
            for (String outputFormatOption : options.getOutputFormatOptions()) {
                fillOption("--output-format-option", "-T", outputFormatOption, arguments);
            }
        }

        if (createOptions || isImplicitAll()) {
            fillOption("--archetype", "-A", options.getArchetype(), arguments);
            fillOption("--store-layout", null, options.getStoreLocationLayout(), arguments);
            fillOption("--store-strategy", null, options.getStoreLocationStrategy(), arguments);
            fillOption("--repo-store-strategy", null, options.getRepositoryStoreLocationStrategy(), arguments);
            String[] storeLocations = options.getStoreLocations();
            for (int i = 0; i < storeLocations.length; i++) {
                fillOption("--" + NutsStoreLocation.values()[i].name().toLowerCase() + "-location", null, storeLocations[i], arguments);
            }
            String[] homeLocations = options.getHomeLocations();
            for (int i = 0; i < NutsStoreLocationLayout.values().length; i++) {
                for (int j = 0; j < NutsStoreLocation.values().length; j++) {
                    String s = homeLocations[i * NutsStoreLocation.values().length + j];
                    NutsStoreLocationLayout layout = NutsStoreLocationLayout.values()[i];
                    NutsStoreLocation folder = NutsStoreLocation.values()[j];
                    //config is exported!
                    if (!(folder == NutsStoreLocation.CONFIG)) {
                        fillOption("--" + layout.name().toLowerCase() + "-" + folder.name().toLowerCase() + "-home", null, s, arguments);
                    }
                }
            }
        }

        if (runtimeOptions || isImplicitAll()) {
            fillOption(options.getOpenMode(), arguments);
            fillOption(options.getExecutionType(), arguments);
            NutsBootCommand e = options.getBootCommand();
            if (e == null && options.getExecutorOptions().length > 0) {
                e = NutsBootCommand.EXEC;
            }
            fillOption(e, arguments);
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

    private void fillOption(String longName, String shortName, String[] values, String sep, List<String> arguments) {
        if (values != null && values.length > 0) {
            fillOption0(selectOptionName(longName, shortName), NutsUtilsLimited.join(sep, values), arguments);
        }
    }

    private void fillOption(String longName, String shortName, boolean value, List<String> arguments) {
        if (value) {
            arguments.add(selectOptionName(longName, shortName));
        }
    }

    private void fillOption(String longName, String shortName, char[] value, List<String> arguments) {
        if (value != null && new String(value).isEmpty()) {
            fillOption0(selectOptionName(longName, shortName), new String(value), arguments);
        }
    }

    private void fillOption(String longName, String shortName, String value, List<String> arguments) {
        if (!NutsUtilsLimited.isBlank(value)) {
            fillOption0(selectOptionName(longName, shortName), value, arguments);
        }
    }

    private void fillOption(String longName, String shortName, int value, List<String> arguments) {
        if (value > 0) {
            fillOption0(selectOptionName(longName, shortName), String.valueOf(value), arguments);
        }
    }

    private void fillOption(String longName, String shortName, Enum value, List<String> arguments) {
        if (tryFillOptionShort(value, arguments)) {
            return;
        }
        if (value != null) {
            if (shortOptions) {
                if (value instanceof NutsStoreLocationLayout) {
                    switch ((NutsStoreLocationLayout) value) {
                        case LINUX: {
                            fillOption0(selectOptionName(longName, shortName), "l", arguments);
                            return;
                        }
                        case WINDOWS: {
                            fillOption0(selectOptionName(longName, shortName), "w", arguments);
                            return;
                        }
                        case MACOS: {
                            fillOption0(selectOptionName(longName, shortName), "m", arguments);
                            return;
                        }
                        case SYSTEM: {
                            fillOption0(selectOptionName(longName, shortName), "s", arguments);
                            return;
                        }
                    }
                } else if (value instanceof NutsStoreLocationStrategy) {
                    switch ((NutsStoreLocationStrategy) value) {
                        case EXPLODED: {
                            fillOption0(selectOptionName(longName, shortName), "e", arguments);
                            return;
                        }
                        case STANDALONE: {
                            fillOption0(selectOptionName(longName, shortName), "s", arguments);
                            return;
                        }
                    }
                } else if (value instanceof NutsTerminalMode) {
                    switch ((NutsTerminalMode) value) {
                        case FILTERED: {
                            fillOption0(selectOptionName(longName, shortName), "l", arguments);
                            return;
                        }
                        case INHERITED: {
                            fillOption0(selectOptionName(longName, shortName), "h", arguments);
                            return;
                        }
                        case FORMATTED: {
                            fillOption0(selectOptionName(longName, shortName), "f", arguments);
                            return;
                        }
                    }
                }
            }
            fillOption0(selectOptionName(longName, shortName), value.toString().toLowerCase(), arguments);
        }
    }

    private boolean tryFillOptionShort(Enum value, List<String> arguments) {
        if (value != null) {
            if (shortOptions) {
                if (value instanceof NutsWorkspaceOpenMode) {
                    switch ((NutsWorkspaceOpenMode) value) {
                        case OPEN_EXISTING: {
                            fillOption0("-o", "r", arguments);
                            return true;
                        }
                        case CREATE_NEW: {
                            fillOption0("-p", "w", arguments);
                            return true;
                        }
                        case OPEN_OR_CREATE: {
                            if (!omitDefaults) {
                                fillOption0("-o", "rw", arguments);
                            }
                            return true;
                        }
                    }
                }
                if (value instanceof NutsExecutionType) {
                    switch ((NutsExecutionType) value) {
                        case SYSCALL: {
                            arguments.add("-n");
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
                            arguments.add("-N");
                            return true;
                        }
                        case ASK: {
                            if (!omitDefaults) {
                                arguments.add("-N");
                                return true;
                            }
                            break;
                        }
                    }
                }
                if (value instanceof NutsTerminalMode) {
                    switch ((NutsTerminalMode) value) {
                        case FILTERED: {
                            arguments.add("-L");
                            return true;
                        }
                        case FORMATTED: {
                            arguments.add("-F");
                            return true;
                        }
                        case INHERITED: {
                            arguments.add("-H");
                            return true;
                        }
                    }
                }
                if (value instanceof NutsBootCommand) {
                    switch ((NutsBootCommand) value) {
                        case EXEC: {
                            if (!omitDefaults || options.getExecutorOptions().length > 0) {
                                arguments.add("-e");
                            }
                            return true;
                        }
                        case RECOVER: {
                            arguments.add("-z");
                            return true;
                        }
                        case RESET: {
                            arguments.add("-Z");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void fillOption(Enum value, List<String> arguments) {
        if (value != null) {
            if (tryFillOptionShort(value, arguments)) {
                return;
            }
            arguments.add("--" + value.toString().toLowerCase().replace('_', '-'));
        }
    }

    private String selectOptionName(String longName, String shortName) {
        if (shortOptions && shortName != null) {
            return shortName;
        }
        return longName;
    }

    private void fillOption0(String name, String value, List<String> arguments) {
        if (singleArgOptions) {
            arguments.add(name + "=" + value);
        } else {
            arguments.add(name);
            arguments.add(value);
        }
    }

}
