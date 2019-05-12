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

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public final class NutsArgumentsParser {

    private NutsArgumentsParser() {
    }

    public static NutsWorkspaceOptions parseNutsArguments(String[] bootArguments) {
        NutsWorkspaceOptions o = new NutsWorkspaceOptions();
        parseNutsArguments(bootArguments, o);
        return o;
    }

    public static void parseNutsArguments(String[] bootArguments, NutsWorkspaceOptions o) {
        List<String> showError = new ArrayList<>();
        HashSet<String> excludedExtensions = new HashSet<>();
        HashSet<String> excludedRepositories = new HashSet<>();
        HashSet<String> tempRepositories = new HashSet<>();
        List<String> executorOptions = new ArrayList<>();
        NutsLogConfig logConfig = null;
        NutsArgument a;
        List<String> applicationArguments = new ArrayList<>();
        NutsCommandLine cmdArgList = new NutsDefaultCommandLine(bootArguments)
                .addSpecialSimpleOption("version")
                .expandSimpleOptions();
        
        while ((a = cmdArgList.next()) != null) {
            if (a.isOption()) {
                boolean enabled = !a.isComment();
                String k = a.strKey();
                switch (k) {
                    //**********************************
                    //*
                    //* Create Exported Options
                    //*
                    //**********************************
                    //
                    // [[create exported options]] are considered both when creating a new workspace 
                    // and when running it. If they are specified in creation 
                    // they will be persisted. If they are specified later they 
                    // will override persisted values without persisting the changes

                    case "-w":
                    case "--workspace": {
                        String file = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            o.setWorkspace(file);
                        }
                        break;
                    }
                    case "--login":
                    case "-o": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            o.setLogin(v);
                        }
                        break;
                    }
                    case "--password":
                    case "-p": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            o.setPassword(v);
                        }
                        break;
                    }
                    case "-V":
                    case "--boot-version":
                    case "--boot-api-version": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            o.setRequiredBootVersion(v);
                        }
                        break;
                    }
                    case "--boot-runtime": {
                        String br = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            if (br.indexOf("#") > 0) {
                                //this is a full id
                            } else {
                                br = NutsConstants.Ids.NUTS_RUNTIME + "#" + br;
                            }
                            o.setBootRuntime(br);
                        }
                        break;
                    }
                    case "--runtime-source-url": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            o.setBootRuntimeSourceURL(v);
                        }
                        break;
                    }
                    case "--java":
                    case "--boot-java":
                    case "-j": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            o.setBootJavaCommand(v);
                        }
                        break;
                    }
                    case "--java-home":
                    case "--boot-java-home":
                    case "--J": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            o.setBootJavaCommand(NutsUtils.resolveJavaCommand(v));
                        }
                        break;
                    }
                    case "--java-options":
                    case "--boot-java-options":
                    case "-O": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            o.setBootJavaOptions(v);
                        }
                        break;
                    }

                    //**********************************
                    //*
                    //* Create Options
                    //*
                    //**********************************
                    // [[create options]] are considered solely when creating a new workspace. 
                    // They will be persisted then (to the configuration file)
                    // but They will be ignored elsewhere if the workspace already 
                    // exists : configured parameters will be in use.
                    case "--archetype":
                    case "-A": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            o.setArchetype(v);
                        }
                        break;
                    }
                    case "--store-strategy": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            o.setStoreLocationStrategy(v.isEmpty() ? null : NutsStoreLocationStrategy.valueOf(v.toUpperCase()));
                        }
                        break;
                    }
                    case "--standalone": {
                        if (enabled) {
                            o.setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                            o.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                        }
                        break;

                    }
                    case "--standalone-workspace": {
                        if (enabled) {
                            o.setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                        }
                        break;
                    }
                    case "--exploded": {
                        if (enabled) {
                            o.setStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                            o.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                        }
                        break;
                    }
                    case "--exploded-workspace": {
                        if (enabled) {
                            o.setStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                        }
                        break;
                    }

                    case "--repo-store-strategy": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            o.setRepositoryStoreLocationStrategy(v.isEmpty() ? null : NutsStoreLocationStrategy.valueOf(v.toUpperCase()));
                        }
                        break;
                    }
                    case "--exploded-repositories": {
                        if (enabled) {
                            o.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                        }
                        break;
                    }
                    case "--standalone-repositories": {
                        if (enabled) {
                            o.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                        }
                        break;
                    }
                    case "--store-layout": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            o.setStoreLocationLayout(v.isEmpty() ? null : NutsStoreLocationLayout.valueOf(v.toUpperCase()));
                        }
                        break;
                    }
                    case "--system-layout": {
                        if (enabled) {
                            o.setStoreLocationLayout(NutsStoreLocationLayout.SYSTEM);
                        }
                        break;
                    }
                    case "--windows-layout": {
                        if (enabled) {
                            o.setStoreLocationLayout(NutsStoreLocationLayout.WINDOWS);
                        }
                        break;
                    }
                    case "--macos-layout": {
                        if (enabled) {
                            o.setStoreLocationLayout(NutsStoreLocationLayout.MACOS);
                        }
                        break;
                    }
                    case "--linux-layout": {
                        if (enabled) {
                            o.setStoreLocationLayout(NutsStoreLocationLayout.LINUX);
                        }
                        break;
                    }
                    case "--programs-location":
                    case "--config-location":
                    case "--var-location":
                    case "--logs-location":
                    case "--temp-location":
                    case "--cache-location":
                    case "--lib-location": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            NutsStoreLocation m = NutsStoreLocation.valueOf(k.substring(2, k.indexOf('-')).toUpperCase());
                            o.setStoreLocation(m, v);
                        }
                        break;
                    }
                    case "--system-programs-home":
                    case "--system-config-home":
                    case "--system-var-home":
                    case "--system-logs-home":
                    case "--system-temp-home":
                    case "--system-cache-home":
                    case "--system-lib-home":
                    case "--windows-programs-home":
                    case "--windows-config-home":
                    case "--windows-var-home":
                    case "--windows-logs-home":
                    case "--windows-temp-home":
                    case "--windows-cache-home":
                    case "--windows-lib-home":
                    case "--macos-programs-home":
                    case "--macos-config-home":
                    case "--macos-var-home":
                    case "--macos-logs-home":
                    case "--macos-temp-home":
                    case "--macos-cache-home":
                    case "--macos-lib-home":
                    case "--linux-programs-home":
                    case "--linux-config-home":
                    case "--linux-var-home":
                    case "--linux-logs-home":
                    case "--linux-temp-home":
                    case "--linux-cache-home":
                    case "--linux-lib-home": {
                        String v = cmdArgList.getValueFor(a).getString();
                        NutsStoreLocationLayout layout = NutsStoreLocationLayout.valueOf(k.substring(2, k.indexOf('-', 2)).toUpperCase());
                        NutsStoreLocation folder = NutsStoreLocation.valueOf(k.substring(3 + layout.toString().length(), k.indexOf('-', 3 + layout.toString().length())).toUpperCase());
                        if (enabled) {
                            o.setHomeLocation(layout, folder, v);
                        }
                        break;
                    }
                    case "--skip-install-companions":
                    case "-k": {
                        if (enabled) {
                            o.setSkipInstallCompanions(true);
                        }
                        break;
                    }

                    //**********************************
                    //*
                    //* Open Exported Options
                    //*
                    //**********************************
                    //
                    //  [[open exported options]] are open (so transient, non 
                    // persistent) options that will override any configured 
                    // value (if any) having the ability to be exported 
                    // to any java child process (as system property -D...) 
                    case "-g":
                    case "--global": {
                        if (enabled) {
                            o.setGlobal(a.getBooleanValue());
                        }
                        break;
                    }

                    case "--gui": {
                        if (enabled) {
                            o.setGui(a.getBooleanValue());
                        }
                        break;
                    }

                    case "--color":
                    case "-C": {
                        if (enabled) {
                            String v = a.getValue().getString();
                            switch (NutsUtils.trim(v).toLowerCase()) {
                                case "":
                                case "always":
                                case "yes":
                                case "enable":
                                case "y":
                                case "true": {
                                    o.setTerminalMode(NutsTerminalMode.FORMATTED);
                                    break;
                                }
                                case "never":
                                case "no":
                                case "none":
                                case "false":
                                case "filtered":
                                case "disable":
                                case "n": {
                                    o.setTerminalMode(NutsTerminalMode.FILTERED);
                                    break;
                                }
                                case "inherited": {
                                    o.setTerminalMode(NutsTerminalMode.INHERITED);
                                    break;
                                }
                            }
                        }
                        break;
                    }
                    case "--no-color": {
                        if (enabled) {
                            o.setTerminalMode(NutsTerminalMode.FILTERED);
                        }
                        break;
                    }
                    case "--term-system":
                    case "-S": {
                        if (enabled) {
                            o.setTerminalMode(null);
                        }
                        break;
                    }
                    case "--term-filtered":
                    case "-L": {
                        if (enabled) {
                            o.setTerminalMode(NutsTerminalMode.FILTERED);
                        }
                        break;
                    }
                    case "--term-formatted":
                    case "-F": {
                        if (enabled) {
                            o.setTerminalMode(NutsTerminalMode.FORMATTED);
                        }
                        break;
                    }
                    case "--term-inherited":
                    case "-H": {
                        if (enabled) {
                            o.setTerminalMode(NutsTerminalMode.INHERITED);
                        }
                        break;
                    }
                    case "--term":
                    case "-t": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            if (v.isEmpty()) {
                                o.setTerminalMode(null);
                            } else {
                                o.setTerminalMode(NutsTerminalMode.valueOf(v.trim().toUpperCase()));
                            }
                        }
                        break;
                    }
                    case "-R":
                    case "--read-only": {
                        if (enabled) {
                            o.setReadOnly(a.getBooleanValue());
                        }
                        break;
                    }

                    case "--verbose":
                    case "--debug":
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
                        if (enabled) {
                            logConfig = new NutsLogConfig();
                        }
                        parseLogLevel(logConfig, a, cmdArgList, enabled);
                        break;
                    }
                    case "--exclude-extension": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            excludedExtensions.add(v);
                        }
                        break;
                    }

                    case "--exclude-repository": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            excludedRepositories.add(v);
                        }
                        break;
                    }
                    case "--repository":
                    case "-P": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            tempRepositories.add(v);
                        }
                        break;
                    }

                    case "--yes":
                    case "-y": {
                        if (enabled) {
                            o.setDefaultResponse(Boolean.TRUE);
                        }
                        break;
                    }
                    case "--no":
                    case "-N": {
                        if (enabled) {
                            o.setDefaultResponse(Boolean.FALSE);
                        }
                        break;
                    }

                    //**********************************
                    //*
                    //* open Options
                    //*
                    //**********************************
                    //
                    // [[open options]] are transient (non persistent) options that will 
                    // override any configured value (if any) and will be 
                    // in use in the current process (and ignored elsewhere). 
                    // Such options will be considered in creating worspaces 
                    // as well but still they are not persistent.
                    case "--recover":
                    case "-0": {
                        if (enabled) {
                            o.setInitMode(NutsBootInitMode.RECOVER);
                        }
                        break;
                    }
                    case "--init":
                    case "-I": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            o.setInitMode(NutsBootInitMode.valueOf(v.toUpperCase()));
                        }
                        break;
                    }
                    case "--perf": {
                        if (enabled) {
                            o.setPerf(a.getBooleanValue());
                        }
                        break;
                    }
                    case "--embedded":
                    case "-b": {
                        if (enabled) {
                            o.setExecutionType(NutsExecutionType.EMBEDDED);
                        }
                        //ignore
                        break;
                    }
                    case "--external":
                    case "--spawn":
                    case "-x": {
                        if (enabled) {
                            o.setExecutionType(NutsExecutionType.SPAWN);
                        }
                        break;
                    }
                    case "--native":
                    case "--syscall":
                    case "-n": {
                        if (enabled) {
                            o.setExecutionType(NutsExecutionType.SYSCALL);
                        }
                        break;
                    }
                    case "--open-mode": {
                        String v = cmdArgList.getValueFor(a).getString();
                        if (enabled) {
                            v = v.toUpperCase().replace('-', '_').replace('/', '_');
                            switch (v) {
                                case "R":
                                case "READ":
                                case "O":
                                case "OPEN": {
                                    v = NutsWorkspaceOpenMode.OPEN_EXISTING.name();
                                    break;
                                }
                                case "W":
                                case "WRITE":
                                case "N":
                                case "NEW":
                                case "C":
                                case "CREATE": {
                                    v = NutsWorkspaceOpenMode.CREATE_NEW.name();
                                    break;
                                }
                                case "RW":
                                case "READ_WRITE":
                                case "ON":
                                case "OPEN_NEW":
                                case "OC":
                                case "OPEN_CREATE": {
                                    v = NutsWorkspaceOpenMode.OPEN_OR_CREATE.name();
                                    break;
                                }
                            }
                            o.setOpenMode(v.isEmpty() ? null : NutsWorkspaceOpenMode.valueOf(v));
                        }
                        break;
                    }
                    case "--open": {
                        if (enabled) {
                            o.setOpenMode(NutsWorkspaceOpenMode.OPEN_EXISTING);
                        }
                        break;
                    }
                    case "--create": {
                        if (enabled) {
                            o.setOpenMode(NutsWorkspaceOpenMode.CREATE_NEW);
                        }
                        break;
                    }

                    //**********************************
                    //*
                    //* Commands
                    //*
                    //**********************************
                    case "-": {
                        if (enabled) {
                            if (a.getValue() != null) {
                                throw new NutsIllegalArgumentException("Invalid argument for workspace : " + a.getString());
                            }
                            applicationArguments.add(NutsConstants.Ids.NUTS_SHELL);
                            applicationArguments.add("-c");
                            applicationArguments.addAll(cmdArgList.removeAll());
                        } else {
                            applicationArguments.addAll(cmdArgList.removeAll());
                        }
                        break;
                    }

                    case "-version":
                    case "-v":
                    case "--version": {
                        if (enabled) {
                            o.setBootCommand(NutsBootCommand.VERSION);
                            applicationArguments.addAll(cmdArgList.removeAll());
                        } else {
                            cmdArgList.removeAll();
                        }
                        break;
                    }
                    case "--reset":
                    case "-r": {
                        if (enabled) {
                            o.setBootCommand(NutsBootCommand.RESET);
                        } else {
                            cmdArgList.removeAll();
                        }
                        break;
                    }
                    case "--e":
                    case "--exec": {
                        if (enabled) {
                            o.setBootCommand(NutsBootCommand.EXEC);
                            while ((a = cmdArgList.next()) != null) {
                                if (a.isOption()) {
                                    executorOptions.add(a.getString());
                                } else {
                                    applicationArguments.add(a.getString());
                                    applicationArguments.addAll(cmdArgList.removeAll());
                                }
                            }
                        } else {
                            cmdArgList.removeAll();
                        }
                        break;
                    }
                    case "-?":
                    case "--help":
                    case "-h": {
                        if (enabled) {
                            o.setBootCommand(NutsBootCommand.HELP);
                            applicationArguments.addAll(cmdArgList.removeAll());
                        } else {
                            cmdArgList.removeAll();
                        }
                        break;
                    }

                    //ERRORS
                    default: {

                        showError.add("nuts: invalid option [[" + a.getString() + "]]");
                    }
                }
            } else {
                applicationArguments.add(a.getString());
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
    }

    private static void parseLogLevel(NutsLogConfig logConfig, NutsArgument arg, NutsCommandLine cmdArgList, boolean enabled) {
        switch (arg.strKey()) {
            case "--log-size": {
                String v = cmdArgList.getValueFor(arg).getString();
                if (enabled) {
                    logConfig.setLogSize(Integer.parseInt(v));
                }
                break;
            }

            case "--log-count": {
                String v = cmdArgList.getValueFor(arg).getString();
                if (enabled) {
                    logConfig.setLogCount(Integer.parseInt(v));
                }
                break;
            }

            case "--log-name": {
                String v = cmdArgList.getValueFor(arg).getString();
                if (enabled) {
                    logConfig.setLogName(v);
                }
                break;
            }

            case "--log-folder": {
                String v = cmdArgList.getValueFor(arg).getString();
                if (enabled) {
                    logConfig.setLogFolder(v);
                }
                break;
            }

            case "--log-inherited": {
                if (enabled) {
                    logConfig.setLogInherited(true);
                }
                break;
            }
            case "--verbose":
            case "--debug":
            case "--log-finest":
            case "--log-finer":
            case "--log-fine":
            case "--log-info":
            case "--log-warning":
            case "--log-severe":
            case "--log-all":
            case "--log-off": {
                if (enabled) {
                    String id = arg.strKey();
                    if (arg.strKey().startsWith("--log-")) {
                        id = id.substring("--log-".length());
                    } else if (arg.strKey().equals("--log")) {
                        id = arg.getValue().getString();
                        if (id == null) {
                            id = "";
                        }
                    } else if (id.startsWith("--")) {
                        id = arg.strKey().substring(2);
                    } else {
                        id = arg.strKey();
                    }
                    switch (id.toLowerCase()) {
                        case "verbose": {
                            logConfig.setLogLevel(Level.FINEST);
                            break;
                        }
                        case "debug": {
                            logConfig.setLogLevel(Level.FINEST);
                            logConfig.setDebug(true);
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
                }
                break;
            }
        }
    }

}
