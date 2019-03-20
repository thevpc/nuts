/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.*;
import java.util.logging.Level;

public final class NutsArgumentsParser {

    private NutsArgumentsParser() {
    }

    public static NutsWorkspaceOptions parseNutsArguments(String[] bootArguments) {
        List<String> showError = new ArrayList<>();
        NutsWorkspaceOptions o = new NutsWorkspaceOptions();
        HashSet<String> excludedExtensions = new HashSet<>();
        HashSet<String> excludedRepositories = new HashSet<>();
        HashSet<String> tempRepositories = new HashSet<>();
        List<String> executorOptions = new ArrayList<>();
        NutsLogConfig logConfig = null;
        NutsMinimalCommandLine.Arg cmdArg;
        List<String> applicationArguments = new ArrayList<>();
        NutsMinimalCommandLine cmdArgList = new NutsMinimalCommandLine(bootArguments);
        while ((cmdArg = cmdArgList.next()) != null) {
            if (cmdArg.isOption()) {
                switch (cmdArg.getKey()) {
                    //dash  should be the very last argument
                    case "-V":
                    case "--boot-version":
                    case "--boot-api-version": {
                        o.setRequiredBootVersion(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "-!V":
                    case "--!boot-version":
                    case "--!boot-api-version": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--embedded":
                    case "-b": {

                        o.setExecutionType(NutsExecutionType.EMBEDDED);
                        //ignore
                        break;
                    }
                    case "--!embedded":
                    case "-!b": {

                        //ignore
                        break;
                    }
                    case "--external":
                    case "-x": {

                        o.setExecutionType(NutsExecutionType.EXTERNAL);
                        //ignore
                        break;
                    }
                    case "--!external":
                    case "-!x": {

                        //ignore
                        break;
                    }
                    case "--native":
                    case "-n": {

                        o.setExecutionType(NutsExecutionType.NATIVE);
                        //ignore
                        break;
                    }
                    case "--!native":
                    case "-!n": {

                        //ignore
                        break;
                    }
                    case "-": {
                        if (cmdArg.getValue() != null) {
                            throw new NutsIllegalArgumentException("Invalid argument for workspace : " + cmdArg.getArg());
                        }
                        applicationArguments.add(NutsConstants.NUTS_SHELL);
                        applicationArguments.addAll(cmdArgList.removeAll());
                        break;
                    }
                    case "-w":
                    case "--workspace": {
                        String file = cmdArgList.getValueFor(cmdArg);
                        o.setWorkspace(file);
                        break;
                    }
                    case "-!w":
                    case "--!workspace": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--archetype":
                    case "-p": {
                        o.setArchetype(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--!archetype":
                    case "-!p": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--login":
                    case "-U": {
                        o.setLogin(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--!login":
                    case "-!U": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
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
                    case "--!boot-runtime": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--runtime-source-url": {
                        o.setBootRuntimeSourceURL(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--!runtime-source-url": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--java":
                    case "--boot-java":
                    case "-j": {
                        o.setBootJavaCommand(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--!java":
                    case "--!boot-java":
                    case "-!j": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--java-home":
                    case "--boot-java-home":
                    case "--J": {
                        o.setBootJavaCommand(NutsUtils.resolveJavaCommand(cmdArgList.getValueFor(cmdArg)));
                        break;
                    }
                    case "--!java-home":
                    case "--!boot-java-home":
                    case "--!J": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--java-options":
                    case "--boot-java-options":
                    case "-O": {
                        o.setBootJavaOptions(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--!java-options":
                    case "--!boot-java-options":
                    case "-!O": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--color":
                    case "-C": {

                        o.setTerminalMode(NutsTerminalMode.FORMATTED);
                        break;
                    }
                    case "--!color":
                    case "--no-color":
                    case "-!C": {

                        o.setTerminalMode(NutsTerminalMode.FILTERED);
                        break;
                    }
                    case "--term-system":
                    case "-S": {

                        o.setTerminalMode(null);
                        break;
                    }
                    case "--!term-system":
                    case "-!S": {

                        //ignore
                        break;
                    }
                    case "--term-filtered":
                    case "-L": {

                        o.setTerminalMode(NutsTerminalMode.FILTERED);
                        break;
                    }
                    case "--!term-filtered":
                    case "-!L": {

                        //ignore
                        break;
                    }
                    case "--term-formatted":
                    case "-F": {

                        o.setTerminalMode(NutsTerminalMode.FORMATTED);
                        break;
                    }
                    case "--!term-formatted":
                    case "-!F": {

                        //ignore
                        break;
                    }
                    case "--term-inherited":
                    case "-H": {

                        o.setTerminalMode(NutsTerminalMode.INHERITED);
                        break;
                    }
                    case "--!term-inherited":
                    case "-!H": {

                        //ignore
                        break;
                    }
                    case "--term":
                    case "-t": {
                        String v = cmdArgList.getValueFor(cmdArg);
                        if (v.isEmpty()) {
                            o.setTerminalMode(null);
                        } else {
                            o.setTerminalMode(NutsTerminalMode.valueOf(v.trim().toUpperCase()));
                        }
                        break;
                    }
                    case "--!term":
                    case "-!t": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "-R":
                    case "--read-only": {

                        o.setReadOnly(true);
                        break;
                    }
                    case "-!R":
                    case "--!read-only": {

                        o.setReadOnly(false);
                        break;
                    }
                    case "-0":
                    case "--recover": {

                        o.setRecover(true);
                        break;
                    }
                    case "-!0":
                    case "--!recover": {

                        o.setRecover(false);
                        break;
                    }
                    case "-g":
                    case "--global": {

                        o.setGlobal(true);
                        break;
                    }
                    case "-!g":
                    case "--!global": {

                        o.setGlobal(false);
                        break;
                    }
                    case "--skip-install-companions":
                    case "-k": {

                        o.setSkipPostCreateInstallCompanionTools(true);
                        break;
                    }
                    case "--!skip-install-companions":
                    case "-!k": {

                        o.setRecover(false);
                        break;
                    }
                    case "-version":
                    case "-v":
                    case "--version": {
                        o.setBootCommand(NutsBootCommand.VERSION);
                        applicationArguments.addAll(cmdArgList.removeAll());
                        break;
                    }
                    case "-!version":
                    case "--!version":
                    case "-!v": {

                        //ignore
                        break;
                    }
                    case "--info":
                    case "-f": {
                        o.setBootCommand(NutsBootCommand.INFO);
                        applicationArguments.addAll(cmdArgList.removeAll());
                        break;
                    }
                    case "--!info":
                    case "-!f": {

                        //ignore
                        break;
                    }
                    case "--update":
                    case "-d": {
                        o.setBootCommand(NutsBootCommand.UPDATE);
                        applicationArguments.addAll(cmdArgList.removeAll());
                        break;
                    }
                    case "--!update":
                    case "-!d": {

                        //ignore
                        break;
                    }
                    case "--clean":
                    case "-c": {
                        o.setBootCommand(NutsBootCommand.CLEAN);
                        applicationArguments.addAll(cmdArgList.removeAll());
                        break;
                    }
                    case "--!clean":
                    case "-!c": {

                        //ignore
                        break;
                    }
                    case "--reset":
                    case "-r": {
                        o.setBootCommand(NutsBootCommand.RESET);
                        applicationArguments.addAll(cmdArgList.removeAll());
                        break;
                    }
                    case "--!reset":
                    case "-!r": {

                        //ignore
                        break;
                    }
                    case "--install-companions":
                    case "-X": {
                        o.setBootCommand(NutsBootCommand.INSTALL_COMPANION_TOOLS);
                        applicationArguments.addAll(cmdArgList.removeAll());
                        break;
                    }
                    case "--!install-companions":
                    case "-!X": {

                        //ignore
                        break;
                    }
                    case "--check-updates":
                    case "-D": {
                        o.setBootCommand(NutsBootCommand.CHECK_UPDATES);
                        applicationArguments.addAll(cmdArgList.removeAll());
                        break;
                    }
                    case "--!check-updates":
                    case "-!D": {

                        //ignore
                        break;
                    }
                    case "--install":
                    case "-i": {
                        o.setBootCommand(NutsBootCommand.INSTALL);
                        applicationArguments.addAll(cmdArgList.removeAll());
                        break;
                    }
                    case "--!install":
                    case "-!i": {

                        //ignore
                        break;
                    }
                    case "--uninstall":
                    case "-u": {
                        o.setBootCommand(NutsBootCommand.UNINSTALL);
                        applicationArguments.addAll(cmdArgList.removeAll());
                        break;
                    }
                    case "--!uninstall":
                    case "-!u": {

                        //ignore
                        break;
                    }
                    case "--e":
                    case "--exec": {
                        o.setBootCommand(NutsBootCommand.EXEC);
                        while ((cmdArg = cmdArgList.next()) != null) {
                            if (cmdArg.isOption()) {
                                executorOptions.add(cmdArg.getArg());
                            } else {
                                applicationArguments.add(cmdArg.getArg());
                                applicationArguments.addAll(cmdArgList.removeAll());
                            }
                        }
                        break;
                    }
                    case "--!exec":
                    case "--!e": {

                        //ignore
                        break;
                    }
                    case "-?":
                    case "--help":
                    case "-h": {
                        o.setBootCommand(NutsBootCommand.HELP);
                        applicationArguments.addAll(cmdArgList.removeAll());
                        break;
                    }
                    case "-!?":
                    case "--!help": {

                        //ignore
                        break;
                    }
                    case "--license": {
                        o.setBootCommand(NutsBootCommand.LICENSE);
                        applicationArguments.addAll(cmdArgList.removeAll());
                        break;
                    }
                    case "--!license": {

                        //ignore
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
                        logConfig = new NutsLogConfig();
                        parseLogLevel(logConfig, cmdArg, cmdArgList);
                        break;
                    }
                    case "--!verbose":
                    case "--!log-finest":
                    case "--!log-finer":
                    case "--!log-fine":
                    case "--!log-info":
                    case "--!log-warning":
                    case "--!log-severe":
                    case "--!log-all":
                    case "--!log-off":
                    case "--!log-size":
                    case "--!log-name":
                    case "--!log-folder":
                    case "--!log-count":
                    case "--!log-inherited": {

                        //ignore
                    }
                    case "--exclude-extension": {
                        excludedExtensions.add(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--!exclude-extension": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--exclude-repository": {
                        excludedRepositories.add(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--!exclude-repository": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--repository": {
                        tempRepositories.add(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--!repository": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--perf": {

                        o.setPerf(true);
                        break;
                    }
                    case "--!perf": {

                        //ignore
                        break;
                    }
                    case "--auto-config": {

                        o.setAutoConfig(cmdArg.getKey() == null ? "" : cmdArg.getKey());
                        break;
                    }
                    case "--!auto-config": {

                        //ignore
                        break;
                    }
                    case "--open-mode": {
                        String v = cmdArgList.getValueFor(cmdArg);
                        o.setOpenMode(v.isEmpty() ? null : NutsWorkspaceOpenMode.valueOf(v.toUpperCase()));
                        break;
                    }
                    case "--!open-mode": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--open": {
                        o.setOpenMode(NutsWorkspaceOpenMode.OPEN_EXISTING);
                        break;
                    }
                    case "--!open": {
                        //ignore
                        break;
                    }
                    case "--create": {
                        o.setOpenMode(NutsWorkspaceOpenMode.CREATE_NEW);
                        break;
                    }
                    case "--!create": {
                        //ignore
                        break;
                    }
                    case "--store-layout": {
                        String v = cmdArgList.getValueFor(cmdArg);
                        o.setStoreLocationLayout(v.isEmpty() ? null : NutsStoreLocationLayout.valueOf(v.toUpperCase()));
                        break;
                    }
                    case "--!store-layout": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--store-strategy": {
                        String v = cmdArgList.getValueFor(cmdArg);
                        o.setStoreLocationStrategy(v.isEmpty() ? null : NutsStoreLocationStrategy.valueOf(v.toUpperCase()));
                        break;
                    }
                    case "--!store-strategy": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--repo-store-strategy": {
                        String v = cmdArgList.getValueFor(cmdArg);
                        o.setRepositoryStoreLocationStrategy(v.isEmpty() ? null : NutsStoreLocationStrategy.valueOf(v.toUpperCase()));
                        break;
                    }
                    case "--!repo-store-strategy": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--config-location": {
                        o.setConfigStoreLocation(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--!config-location": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--programs-location": {
                        o.setProgramsStoreLocation(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--!programs-location": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--cache-location": {
                        o.setCacheStoreLocation(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--!cache-location": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--temp-location": {
                        o.setTempStoreLocation(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--!temp-location": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--var-location": {
                        o.setVarStoreLocation(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--!var-location": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--logs-location": {
                        o.setLogsStoreLocation(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--!logs-location": {
                        cmdArgList.getValueFor(cmdArg);
                        //ignore
                        break;
                    }
                    case "--system-layout": {

                        o.setStoreLocationLayout(NutsStoreLocationLayout.SYSTEM);
                        break;
                    }
                    case "--!system-layout": {

                        //ignore
                        break;
                    }
                    case "--windows-layout": {

                        o.setStoreLocationLayout(NutsStoreLocationLayout.WINDOWS);
                        break;
                    }
                    case "--!windows-layout": {

                        //ignore
                        break;
                    }
                    case "--linux-layout": {

                        o.setStoreLocationLayout(NutsStoreLocationLayout.LINUX);
                        break;
                    }
                    case "--!linux-layout": {

                        break;
                    }
                    case "--standalone":
                    case "--standalone-workspace": {

                        o.setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                        break;
                    }
                    case "--!standalone":
                    case "--!standalone-workspace": {

                        break;
                    }
                    case "--exploded":
                    case "--exploded-workspace": {

                        o.setStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                        break;
                    }
                    case "--!exploded":
                    case "--!exploded-workspace": {

                        break;
                    }
                    case "--exploded-repositories": {

                        o.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                        break;
                    }
                    case "--!exploded-repositories": {

                        break;
                    }
                    case "--standalone-repositories": {

                        o.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                        break;
                    }
                    case "--!standalone-repositories": {
                        break;
                    }
                    case "--yes": {
                        o.setDefaultResponse(Boolean.TRUE);
                        break;
                    }
                    case "--!yes": {
                        break;
                    }
                    case "--no": {
                        o.setDefaultResponse(Boolean.FALSE);
                        break;
                    }
                    case "--!no": {
                        break;
                    }
                    default: {

                        showError.add("nuts: invalid option [[" + cmdArg.getArg() + "]]");
                    }
                }
            } else {
                applicationArguments.add(cmdArg.getArg());
                applicationArguments.addAll(cmdArgList.removeAll());
            }
        }

        o.setLogConfig(logConfig);
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
        o.setApplicationArguments(applicationArguments.toArray(new String[0]));
        o.setExecutorOptions(executorOptions.toArray(new String[0]));
        return o;
    }

    private static void parseLogLevel(NutsLogConfig logConfig, NutsMinimalCommandLine.Arg cmdArg, NutsMinimalCommandLine cmdArgList) {
        switch (cmdArg.getKey()) {
            case "--log-size": {
                logConfig.setLogSize(Integer.parseInt(cmdArgList.getValueFor(cmdArg)));
                break;
            }
            case "--log-count": {
                logConfig.setLogCount(Integer.parseInt(cmdArgList.getValueFor(cmdArg)));
                break;
            }
            case "--log-name": {
                logConfig.setLogName(cmdArgList.getValueFor(cmdArg));
                break;
            }
            case "--log-folder": {
                logConfig.setLogFolder(cmdArgList.getValueFor(cmdArg));
                break;
            }
            case "--log-inherited": {
                logConfig.setLogInherited(true);
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
                        logConfig.setLogLevel(Level.FINEST);
                        break;
                    }
                    case "finest": {
                        logConfig.setLogLevel(Level.FINEST);
                        break;
                    }
                    case "finer": {
                        logConfig.setLogLevel(Level.FINER);
                        break;
                    }
                    case "fine": {
                        logConfig.setLogLevel(Level.FINE);
                        break;
                    }
                    case "info": {
                        logConfig.setLogLevel(Level.INFO);
                        break;
                    }
                    case "warning": {
                        logConfig.setLogLevel(Level.WARNING);
                        break;
                    }
                    case "config": {
                        logConfig.setLogLevel(Level.CONFIG);
                        break;
                    }
                    case "all": {
                        logConfig.setLogLevel(Level.ALL);
                        break;
                    }
                    case "off": {
                        logConfig.setLogLevel(Level.OFF);
                        break;
                    }
                    default: {
                        logConfig.setLogLevel(Level.INFO);
                        break;
                    }
                }
                break;
            }
        }
    }

}
