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
 * Nuts Arguments parser.
 * Creates a {@link NutsWorkspaceOptions} instance from string array of valid nuts options
 * @author vpc
 * @since 0.5.4
 */
public final class NutsArgumentsParser {

    private NutsArgumentsParser() {
    }

    /**
     * Creates a {@link NutsWorkspaceOptions} instance from string array of valid nuts options
     * @param bootArguments input arguments to parse
     * @return newly created and filled options instance
     */
    public static NutsWorkspaceOptions parseNutsArguments(String[] bootArguments) {
        NutsWorkspaceOptions o = new NutsWorkspaceOptions();
        parseNutsArguments(bootArguments, o);
        return o;
    }

    /**
     * Fills a {@link NutsWorkspaceOptions} instance from string array of valid nuts options
     * @param bootArguments input arguments to parse
     * @param options options instance to fill
     */
    public static void parseNutsArguments(String[] bootArguments, NutsWorkspaceOptions options) {
        List<String> showError = new ArrayList<>();
        HashSet<String> excludedExtensions = new HashSet<>();
        HashSet<String> excludedRepositories = new HashSet<>();
        HashSet<String> tempRepositories = new HashSet<>();
        List<String> executorOptions = new ArrayList<>();
        NutsLogConfig logConfig = null;
        List<String> applicationArguments = new ArrayList<>();
        NutsCommand cmdLine = new NutsCommandLimited(bootArguments)
                .setCommandName("nuts")
                .addSpecialSimpleOption("version")
                .expandSimpleOptions();
        while (cmdLine.hasNext()) {
            NutsArgument a=cmdLine.peek();

            if (a.isOption()) {
                boolean enabled = a.isEnabled();
                String k = a.getKey().getString();
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
                        a=cmdLine.nextString();
                        String file = a.getValue().getString();
                        if (enabled) {
                            options.setWorkspace(file);
                        }
                        break;
                    }
                    case "--login":
                    case "-o": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setLogin(v);
                        }
                        break;
                    }
                    case "--password":
                    case "-p": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setPassword(v.toCharArray());
                        }
                        break;
                    }
                    case "-V":
                    case "--boot-version":
                    case "--boot-api-version": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setRequiredBootVersion(v);
                        }
                        break;
                    }
                    case "--boot-runtime": {
                        a=cmdLine.nextString();
                        String br = a.getValue().getString();
                        if (enabled) {
                            if (br.indexOf("#") > 0) {
                                //this is a full id
                            } else {
                                br = NutsConstants.Ids.NUTS_RUNTIME + "#" + br;
                            }
                            options.setBootRuntime(br);
                        }
                        break;
                    }
                    case "--runtime-source-url": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setBootRuntimeSourceURL(v);
                        }
                        break;
                    }
                    case "--java":
                    case "--boot-java":
                    case "-j": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setBootJavaCommand(v);
                        }
                        break;
                    }
                    case "--java-home":
                    case "--boot-java-home":
                    case "--J": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setBootJavaCommand(NutsUtilsLimited.resolveJavaCommand(v));
                        }
                        break;
                    }
                    case "--java-options":
                    case "--boot-java-options":
                    case "-O": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setBootJavaOptions(v);
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
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setArchetype(v);
                        }
                        break;
                    }
                    case "--store-strategy": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setStoreLocationStrategy(v.isEmpty() ? null : NutsStoreLocationStrategy.valueOf(v.toUpperCase()));
                        }
                        break;
                    }
                    case "--standalone": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                            options.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                        }
                        break;

                    }
                    case "--standalone-workspace": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                        }
                        break;
                    }
                    case "--exploded": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                            options.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                        }
                        break;
                    }
                    case "--exploded-workspace": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                        }
                        break;
                    }

                    case "--repo-store-strategy": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setRepositoryStoreLocationStrategy(v.isEmpty() ? null : NutsStoreLocationStrategy.valueOf(v.toUpperCase()));
                        }
                        break;
                    }
                    case "--exploded-repositories": {
                        if (enabled) {
                            options.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                        }
                        break;
                    }
                    case "--standalone-repositories": {
                        if (enabled) {
                            options.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                        }
                        break;
                    }
                    case "--store-layout": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setStoreLocationLayout(v.isEmpty() ? null : NutsStoreLocationLayout.valueOf(v.toUpperCase()));
                        }
                        break;
                    }
                    case "--system-layout": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setStoreLocationLayout(NutsStoreLocationLayout.SYSTEM);
                        }
                        break;
                    }
                    case "--windows-layout": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setStoreLocationLayout(NutsStoreLocationLayout.WINDOWS);
                        }
                        break;
                    }
                    case "--macos-layout": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setStoreLocationLayout(NutsStoreLocationLayout.MACOS);
                        }
                        break;
                    }
                    case "--linux-layout": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setStoreLocationLayout(NutsStoreLocationLayout.LINUX);
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
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            NutsStoreLocation m = NutsStoreLocation.valueOf(k.substring(2, k.indexOf('-')).toUpperCase());
                            options.setStoreLocation(m, v);
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
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        NutsStoreLocationLayout layout = NutsStoreLocationLayout.valueOf(k.substring(2, k.indexOf('-', 2)).toUpperCase());
                        NutsStoreLocation folder = NutsStoreLocation.valueOf(k.substring(3 + layout.toString().length(), k.indexOf('-', 3 + layout.toString().length())).toUpperCase());
                        if (enabled) {
                            options.setHomeLocation(layout, folder, v);
                        }
                        break;
                    }
                    case "--skip-install-companions":
                    case "-k": {
                        a=cmdLine.nextBoolean();
                        if (enabled) {
                            options.setSkipInstallCompanions(a.getValue().getBoolean());
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
                        a=cmdLine.nextBoolean();
                        if (enabled) {
                            options.setGlobal(a.getValue().getBoolean());
                        }
                        break;
                    }

                    case "--gui": {
                        if (enabled) {
                            options.setGui(a.getValue().getBoolean());
                        }
                        break;
                    }

                    case "--color":
                    case "-C": {
                        a=cmdLine.nextString();
                        if (enabled) {
                            String v = a.getValue().getString();
                            switch (NutsUtilsLimited.trim(v).toLowerCase()) {
                                case "":
                                case "formatted": {
                                    options.setTerminalMode(NutsTerminalMode.FORMATTED);
                                    break;
                                }
                                case "filtered": {
                                    options.setTerminalMode(NutsTerminalMode.FILTERED);
                                    break;
                                }
                                case "inherited": {
                                    options.setTerminalMode(NutsTerminalMode.INHERITED);
                                    break;
                                }
                                default: {
                                    boolean b = NutsUtilsLimited.parseBoolean(v, false);
                                    if (b) {
                                        options.setTerminalMode(NutsTerminalMode.FORMATTED);
                                    } else {
                                        options.setTerminalMode(NutsTerminalMode.FILTERED);
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case "--no-color": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setTerminalMode(NutsTerminalMode.FILTERED);
                        }
                        break;
                    }
                    case "--term-system":
                    case "-S": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setTerminalMode(null);
                        }
                        break;
                    }
                    case "--term-filtered":
                    case "-L": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setTerminalMode(NutsTerminalMode.FILTERED);
                        }
                        break;
                    }
                    case "--term-formatted":
                    case "-F": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setTerminalMode(NutsTerminalMode.FORMATTED);
                        }
                        break;
                    }
                    case "--term-inherited":
                    case "-H": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setTerminalMode(NutsTerminalMode.INHERITED);
                        }
                        break;
                    }
                    case "--term":
                    case "-t": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            if (v.isEmpty()) {
                                options.setTerminalMode(null);
                            } else {
                                options.setTerminalMode(NutsTerminalMode.valueOf(v.trim().toUpperCase()));
                            }
                        }
                        break;
                    }
                    case "-R":
                    case "--read-only": {
                        a=cmdLine.nextBoolean();
                        if (enabled) {
                            options.setReadOnly(a.getValue().getBoolean());
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
                        parseLogLevel(logConfig, cmdLine, enabled);
                        break;
                    }
                    case "--exclude-extension": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            excludedExtensions.add(v);
                        }
                        break;
                    }

                    case "--exclude-repository": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            excludedRepositories.add(v);
                        }
                        break;
                    }
                    case "--repository":
                    case "-P": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            tempRepositories.add(v);
                        }
                        break;
                    }

                    case "--yes":
                    case "-y": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setDefaultResponse(Boolean.TRUE);
                        }
                        break;
                    }
                    case "--no":
                    case "-N": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setDefaultResponse(Boolean.FALSE);
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
                        cmdLine.skip();
                        if (enabled) {
                            options.setInitMode(NutsBootInitMode.RECOVER);
                        }
                        break;
                    }
                    case "--init":
                    case "-I": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString("");
                        if (enabled) {
                            options.setInitMode(v.isEmpty()?NutsBootInitMode.RECOVER:NutsBootInitMode.valueOf(v.toUpperCase()));
                        }
                        break;
                    }
                    case "--embedded":
                    case "-b": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setExecutionType(NutsExecutionType.EMBEDDED);
                        }
                        //ignore
                        break;
                    }
                    case "--external":
                    case "--spawn":
                    case "-x": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setExecutionType(NutsExecutionType.SPAWN);
                        }
                        break;
                    }
                    case "--native":
                    case "--syscall":
                    case "-n": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setExecutionType(NutsExecutionType.SYSCALL);
                        }
                        break;
                    }
                    case "--open-mode": {
                        a=cmdLine.nextString();
                        String v = a.getValue().getString();
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
                            options.setOpenMode(v.isEmpty() ? null : NutsWorkspaceOpenMode.valueOf(v));
                        }
                        break;
                    }
                    case "--open": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setOpenMode(NutsWorkspaceOpenMode.OPEN_EXISTING);
                        }
                        break;
                    }
                    case "--create": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setOpenMode(NutsWorkspaceOpenMode.CREATE_NEW);
                        }
                        break;
                    }

                    //**********************************
                    //*
                    //* Commands
                    //*
                    //**********************************
                    case "-": {
                        cmdLine.skip();
                        if (enabled) {
                            if (!a.getValue().isNull()) {
                                throw new NutsIllegalArgumentException(null, "Invalid argument for workspace : " + a.getString());
                            }
                            applicationArguments.add(NutsConstants.Ids.NUTS_SHELL);
                            applicationArguments.add("-c");
                            applicationArguments.addAll(Arrays.asList(cmdLine.toArray()));
                            cmdLine.skipAll();
                        } else {
                            applicationArguments.addAll(Arrays.asList(cmdLine.toArray()));
                            cmdLine.skipAll();
                        }
                        break;
                    }

                    case "-version":
                    case "-v":
                    case "--version": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setBootCommand(NutsBootCommand.VERSION);
                            applicationArguments.addAll(Arrays.asList(cmdLine.toArray()));
                            cmdLine.skipAll();
                        } else {
                            cmdLine.toArray();
                            cmdLine.skipAll();
                        }
                        break;
                    }
                    case "--reset":
                    case "-r": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setBootCommand(NutsBootCommand.RESET);
                        } else {
                            cmdLine.skipAll();
                        }
                        break;
                    }
                    case "--e":
                    case "--exec": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setBootCommand(NutsBootCommand.EXEC);
                            while ((a = cmdLine.next()) != null) {
                                if (a.isOption()) {
                                    executorOptions.add(a.getString());
                                } else {
                                    applicationArguments.add(a.getString());
                                    applicationArguments.addAll(Arrays.asList(cmdLine.toArray()));
                                    cmdLine.skipAll();
                                }
                            }
                        } else {
                            cmdLine.skipAll();
                        }
                        break;
                    }
                    case "-?":
                    case "--help":
                    case "-h": {
                        cmdLine.skip();
                        if (enabled) {
                            options.setBootCommand(NutsBootCommand.HELP);
                            applicationArguments.addAll(Arrays.asList(cmdLine.toArray()));
                            cmdLine.skipAll();
                        } else {
                            cmdLine.skipAll();
                        }
                        break;
                    }

                    //ERRORS
                    default: {
                        cmdLine.skip();
                        showError.add("nuts: invalid option [[" + a.getString() + "]]");
                    }
                }
            } else {
                cmdLine.skip();
                applicationArguments.add(a.getString());
                applicationArguments.addAll(Arrays.asList(cmdLine.toArray()));
                cmdLine.skipAll();
            }
        }

        options.setLogConfig(logConfig);
        //NutsUtilsLimited.split(bootArguments[i], " ,;")
        options.setExcludedExtensions(excludedExtensions.toArray(new String[0]));
        options.setExcludedRepositories(excludedRepositories.toArray(new String[0]));
        options.setTransientRepositories(tempRepositories.toArray(new String[0]));
        if (options.getBootCommand() != NutsBootCommand.HELP) {
            if (!showError.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();
                for (String s : showError) {
                    errorMessage.append(s).append("\n");
                }
                errorMessage.append("Try 'nuts --help' for more information.\n");
                throw new NutsIllegalArgumentException(null, errorMessage.toString());
            }
        }
        options.setApplicationArguments(applicationArguments.toArray(new String[0]));
        options.setExecutorOptions(executorOptions.toArray(new String[0]));
    }

    private static void parseLogLevel(NutsLogConfig logConfig, NutsCommand cmdLine, boolean enabled) {
        NutsArgument a=cmdLine.peek();
        switch (a.getKey().getString()) {
            case "--log-size": {
                a=cmdLine.nextString();
                String v = a.getValue().getString();
                if (enabled) {
                    logConfig.setLogSize(Integer.parseInt(v));
                }
                break;
            }

            case "--log-count": {
                a=cmdLine.nextString();
                if (enabled) {
                    logConfig.setLogCount(a.getValue().getInt());
                }
                break;
            }

            case "--log-name": {
                a=cmdLine.nextString();
                String v = a.getValue().getString();
                if (enabled) {
                    logConfig.setLogName(v);
                }
                break;
            }

            case "--log-folder": {
                a=cmdLine.nextString();
                String v = a.getValue().getString();
                if (enabled) {
                    logConfig.setLogFolder(v);
                }
                break;
            }

            case "--log-inherited": {
                cmdLine.skip();
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
                cmdLine.skip();
                if (enabled) {
                    String id = a.getKey().getString();
                    if (a.getKey().getString().startsWith("--log-")) {
                        id = id.substring("--log-".length());
                    } else if (a.getKey().getString().equals("--log")) {
                        id = a.getValue().getString();
                        if (id == null) {
                            id = "";
                        }
                    } else if (id.startsWith("--")) {
                        id = a.getKey().getString().substring(2);
                    } else {
                        id = a.getKey().getString();
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
