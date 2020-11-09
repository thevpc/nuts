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
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

/**
 * Nuts Arguments parser. Creates a {@link NutsWorkspaceOptions} instance from
 * string array of valid nuts options
 *
 * @author vpc
 * @category Internal
 * @since 0.5.4
 */
final class PrivateNutsArgumentsParser {

    /**
     * private constructor
     */
    private PrivateNutsArgumentsParser() {
    }


    /**
     * Fill a {@link NutsWorkspaceOptions} instance from string array of valid
     * nuts options
     *
     * @param bootArguments input arguments to parse
     * @param options       options instance to fill
     */
    public static void parseNutsArguments(String[] bootArguments, NutsWorkspaceOptionsBuilder options) {
        List<String> showError = new ArrayList<>();
        HashSet<String> excludedExtensions = new HashSet<>();
        HashSet<String> excludedRepositories = new HashSet<>();
        HashSet<String> tempRepositories = new HashSet<>();
        List<String> executorOptions = new ArrayList<>();
        NutsLogConfig logConfig = null;
        List<String> applicationArguments = new ArrayList<>();
        NutsCommandLine cmdLine = new PrivateNutsCommandLine(bootArguments)
                .setCommandName("nuts")
                .setExpandSimpleOptions(true)
                .registerSpecialSimpleOption("-version");
        while (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.peek();

            if (a.isOption()) {
                boolean enabled = a.isEnabled();
                String k = a.getStringKey();
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

                    case "--boot-repos": {
                        a = cmdLine.nextString();
                        String bootRepos = a.getStringValue();
                        if (enabled) {
                            options.setBootRepositories(bootRepos);
                        }
                        break;
                    }
                    case "-w":
                    case "--workspace": {
                        a = cmdLine.nextString();
                        String file = a.getStringValue();
                        if (enabled) {
                            options.setWorkspace(file);
                        }
                        break;
                    }
                    case "--user":
                    case "-u": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            options.setUsername(v);
                        }
                        break;
                    }
                    case "--password":
                    case "-p": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            options.setCredentials(v.toCharArray());
                        }
                        break;
                    }
                    case "-V":
                    case "--boot-version":
                    case "--boot-api-version": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            options.setApiVersion(v);
                        }
                        break;
                    }
                    case "--boot-runtime": {
                        a = cmdLine.nextString();
                        String br = a.getStringValue();
                        if (enabled) {
                            if (br.indexOf("#") > 0) {
                                //this is a full id
                            } else {
                                br = NutsConstants.Ids.NUTS_RUNTIME + "#" + br;
                            }
                            options.setRuntimeId(br);
                        }
                        break;
                    }
                    case "--java":
                    case "--boot-java":
                    case "-j": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            options.setJavaCommand(v);
                        }
                        break;
                    }
                    case "--java-home":
                    case "--boot-java-home":
                    case "--J": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            options.setJavaCommand(PrivateNutsUtils.resolveJavaCommand(v));
                        }
                        break;
                    }
                    case "--java-options":
                    case "--boot-java-options":
                    case "-O": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            options.setJavaOptions(v);
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
                    case "--name": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            options.setName(v);
                        }
                        break;
                    }
                    case "--archetype":
                    case "-A": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            options.setArchetype(v);
                        }
                        break;
                    }
                    case "--store-strategy": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            options.setStoreLocationStrategy(parseNutsStoreLocationStrategy(v));
                        }
                        break;
                    }
                    case "--standalone": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
//                            options.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                        }
                        break;

                    }
                    case "--standalone-workspace": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                        }
                        break;
                    }
                    case "-E":
                    case "--exploded": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
//                            options.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                        }
                        break;
                    }
                    case "--exploded-workspace": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                        }
                        break;
                    }

                    case "--repo-store-strategy": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            options.setRepositoryStoreLocationStrategy(parseNutsStoreLocationStrategy(v));
                        }
                        break;
                    }
                    case "--exploded-repositories": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                        }
                        break;
                    }
                    case "--standalone-repositories": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                        }
                        break;
                    }
                    case "--store-layout": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            options.setStoreLocationLayout(parseNutsStoreLocationLayout(v));
                        }
                        break;
                    }
                    case "--system-layout": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setStoreLocationLayout(null);
                        }
                        break;
                    }
                    case "--windows-layout": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setStoreLocationLayout(NutsOsFamily.WINDOWS);
                        }
                        break;
                    }
                    case "--macos-layout": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setStoreLocationLayout(NutsOsFamily.MACOS);
                        }
                        break;
                    }
                    case "--linux-layout": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setStoreLocationLayout(NutsOsFamily.LINUX);
                        }
                        break;
                    }
                    case "--unix-layout": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setStoreLocationLayout(NutsOsFamily.UNIX);
                        }
                        break;
                    }
                    case "--apps-location":
                    case "--config-location":
                    case "--var-location":
                    case "--log-location":
                    case "--temp-location":
                    case "--cache-location":
                    case "--lib-location": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            NutsStoreLocation m = NutsStoreLocation.valueOf(k.substring(2, k.indexOf('-')).toUpperCase());
                            options.setStoreLocation(m, v);
                        }
                        break;
                    }
                    case "--system-apps-home":
                    case "--system-config-home":
                    case "--system-var-home":
                    case "--system-log-home":
                    case "--system-temp-home":
                    case "--system-cache-home":
                    case "--system-lib-home":
                    case "--system-run-home": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        NutsStoreLocation folder = NutsStoreLocation.valueOf(
                                k.substring(3 + "system".length(), k.indexOf('-', 3 + "system".length())).toUpperCase());
                        if (enabled) {
                            options.setHomeLocation(null, folder, v);
                        }
                        break;
                    }
                    case "--windows-apps-home":
                    case "--windows-config-home":
                    case "--windows-var-home":
                    case "--windows-log-home":
                    case "--windows-temp-home":
                    case "--windows-cache-home":
                    case "--windows-lib-home":
                    case "--windows-run-home":
                    case "--macos-apps-home":
                    case "--macos-config-home":
                    case "--macos-var-home":
                    case "--macos-log-home":
                    case "--macos-temp-home":
                    case "--macos-cache-home":
                    case "--macos-lib-home":
                    case "--macos-run-home":
                    case "--linux-apps-home":
                    case "--linux-config-home":
                    case "--linux-var-home":
                    case "--linux-log-home":
                    case "--linux-temp-home":
                    case "--linux-cache-home":
                    case "--linux-lib-home":
                    case "--linux-run-home":
                    case "--unix-apps-home":
                    case "--unix-config-home":
                    case "--unix-var-home":
                    case "--unix-log-home":
                    case "--unix-temp-home":
                    case "--unix-cache-home":
                    case "--unix-lib-home":
                    case "--unix-run-home": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        NutsOsFamily layout = NutsOsFamily.valueOf(k.substring(2, k.indexOf('-', 2)).toUpperCase());
                        NutsStoreLocation folder = NutsStoreLocation.valueOf(k.substring(3 + layout.toString().length(), k.indexOf('-', 3 + layout.toString().length())).toUpperCase());
                        if (enabled) {
                            options.setHomeLocation(layout, folder, v);
                        }
                        break;
                    }
                    case "--skip-companions":
                    case "-k": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setSkipCompanions(a.getBooleanValue());
                        }
                        break;
                    }
                    case "--skip-welcome":
                    case "-K": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setSkipWelcome(a.getBooleanValue());
                        }
                        break;
                    }
                    case "--skip-boot":
                    case "-Q": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setSkipBoot(a.getBooleanValue());
                        }
                        break;
                    }
                    case "--switch": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setSwitchWorkspace(a.getBooleanValue(true));
                        }
                        break;
                    }
                    case "--no-switch": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setSwitchWorkspace(!a.getBooleanValue(true));
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
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setGlobal(a.getBooleanValue());
                        }
                        break;
                    }

                    case "--gui": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setGui(a.getBooleanValue());
                        }
                        break;
                    }

                    case "--color":
                    case "-c": {
                        //if the value is not imediately attatched with '=' don't consider
                        a = cmdLine.next();
                        if (enabled) {
                            String v = a.getStringValue("");
                            if (v.isEmpty()) {
                                options.setTerminalMode(NutsTerminalMode.FORMATTED);
                            } else {
                                Boolean b = PrivateNutsUtils.parseBoolean(v, null);
                                if (b != null) {
                                    if (b) {
                                        options.setTerminalMode(NutsTerminalMode.FORMATTED);

                                    } else {
                                        options.setTerminalMode(NutsTerminalMode.FILTERED);
                                    }
                                } else {
                                    switch (v.toLowerCase()) {
                                        case "formatted": {
                                            options.setTerminalMode(NutsTerminalMode.FORMATTED);
                                            break;
                                        }
                                        case "filtered": {
                                            options.setTerminalMode(NutsTerminalMode.FILTERED);
                                            break;
                                        }
                                        case "h":
                                        case "inherited": {
                                            options.setTerminalMode(NutsTerminalMode.INHERITED);
                                            break;
                                        }
                                        case "s":
                                        case "auto":
                                        case "system": {
                                            options.setTerminalMode(null);
                                            break;
                                        }
                                        default: {
                                            cmdLine.pushBack(a);
                                            cmdLine.unexpectedArgument();
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case "-C":
                    case "--no-color": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setTerminalMode(NutsTerminalMode.FILTERED);
                        }
                        break;
                    }
                    case "--bot": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setTerminalMode(NutsTerminalMode.FILTERED);
                            options.setProgressOptions("none");
                            options.setConfirm(NutsConfirmationMode.ERROR);
                            options.setTrace(false);
                            options.setDebug(false);
                            options.setGui(false);
                        }
                        break;
                    }
                    case "-R":
                    case "--read-only": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setReadOnly(a.getBooleanValue());
                        }
                        break;
                    }
                    case "-t":
                    case "--trace": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setTrace(a.getBooleanValue());
                        }
                        break;
                    }
                    case "-P":
                    case "--progress": {
                        a = cmdLine.nextString();
                        if (enabled) {
                            options.setProgressOptions(a.getStringValue());
                        }
                        break;
                    }
                    case "--no-progress": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setProgressOptions("none");
                        }
                        break;
                    }

                    case "--dry":
                    case "-D": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setDry(a.getBooleanValue());
                        }
                        break;
                    }

                    case "--debug": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setDebug(a.getBooleanValue());
                        }
                        break;
                    }

                    case "--verbose":

                    case "--log-verbose":
                    case "--log-finest":
                    case "--log-finer":
                    case "--log-fine":
                    case "--log-info":
                    case "--log-warning":
                    case "--log-severe":
                    case "--log-config":
                    case "--log-all":
                    case "--log-off":

                    case "--log-term-verbose":
                    case "--log-term-finest":
                    case "--log-term-finer":
                    case "--log-term-fine":
                    case "--log-term-info":
                    case "--log-term-warning":
                    case "--log-term-severe":
                    case "--log-term-config":
                    case "--log-term-all":
                    case "--log-term-off":

                    case "--log-file-verbose":
                    case "--log-file-finest":
                    case "--log-file-finer":
                    case "--log-file-fine":
                    case "--log-file-info":
                    case "--log-file-warning":
                    case "--log-file-severe":
                    case "--log-file-config":
                    case "--log-file-all":
                    case "--log-file-off":

                    case "--log-file-size":
                    case "--log-file-name":
                    case "--log-file-base":
                    case "--log-file-count":
                    case "--log-inherited": {
                        if (enabled) {
                            if (logConfig == null) {
                                logConfig = new NutsLogConfig();
                            }
                        }
                        parseLogLevel(logConfig, cmdLine, enabled);
                        break;
                    }
                    case "--exclude-extension": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            excludedExtensions.add(v);
                        }
                        break;
                    }

                    case "--exclude-repository": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            excludedRepositories.add(v);
                        }
                        break;
                    }
                    case "--repository":
                    case "-r": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            tempRepositories.add(v);
                        }
                        break;
                    }

                    case "--output-format-option":
                    case "-T":
                        if (enabled) {
                            options.addOutputFormatOptions(cmdLine.nextString().getStringValue());
                        } else {
                            cmdLine.skip();
                        }
                        break;
                    case "--json":
                        a = cmdLine.next();
                        if (enabled) {
                            options.setOutputFormat(NutsOutputFormat.JSON);
                            options.addOutputFormatOptions(a.getStringValue(""));
                        }
                        break;
                    case "--plain":
                        a = cmdLine.next();
                        if (enabled) {
                            options.setOutputFormat(NutsOutputFormat.PLAIN);
                            options.addOutputFormatOptions(a.getStringValue(""));
                        }
                        break;
                    case "--xml":
                        a = cmdLine.next();
                        if (enabled) {
                            options.setOutputFormat(NutsOutputFormat.XML);
                            options.addOutputFormatOptions(a.getStringValue(""));
                        }
                        break;
                    case "--table":
                        a = cmdLine.next();
                        if (enabled) {
                            options.setOutputFormat(NutsOutputFormat.TABLE);
                            options.addOutputFormatOptions(a.getStringValue(""));
                        }
                        break;
                    case "--tree":
                        a = cmdLine.next();
                        if (enabled) {
                            options.setOutputFormat(NutsOutputFormat.TREE);
                            options.addOutputFormatOptions(a.getStringValue(""));
                        }
                        break;
                    case "--props":
                        a = cmdLine.next();
                        if (enabled) {
                            options.setOutputFormat(NutsOutputFormat.PROPS);
                            options.addOutputFormatOptions(a.getStringValue(""));
                        }
                        break;
                    case "--yes":
                    case "-y": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setConfirm(NutsConfirmationMode.YES);
                        }
                        break;
                    }
                    case "--no":
                    case "-n": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setConfirm(NutsConfirmationMode.NO);
                        }
                        break;
                    }
                    case "--error": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setConfirm(NutsConfirmationMode.ERROR);
                        }
                        break;
                    }
                    case "--ask": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setConfirm(NutsConfirmationMode.ASK);
                        }
                        break;
                    }
                    case "--cached": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setCached(a.getBooleanValue());
                        }
                        break;
                    }
                    case "--indexed": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setIndexed(a.getBooleanValue());
                        }
                        break;
                    }
                    case "--transitive": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setTransitive(a.getBooleanValue());
                        }
                        break;
                    }
                    case "-f":
                    case "--fetch": {
                        a = cmdLine.nextString();
                        if (enabled) {
                            options.setFetchStrategy(NutsFetchStrategy.valueOf(a.getStringValue().toUpperCase().replace("-", "_")));
                        }
                        break;
                    }
                    case "-a":
                    case "--anywhere": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setFetchStrategy(NutsFetchStrategy.ANYWHERE);
                        }
                        break;
                    }
//                    case "-i":
//                    case "--installed":
//                    {
//                        a = cmdLine.nextBoolean();
//                        if (enabled && a.getBooleanValue()) {
//                            options.setFetchStrategy(NutsFetchStrategy.INSTALLED);
//                        }
//                        break;
//                    }
                    case "-F":
                    case "--offline": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setFetchStrategy(NutsFetchStrategy.OFFLINE);
                        }
                        break;
                    }
                    case "--online": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setFetchStrategy(NutsFetchStrategy.ONLINE);
                        }
                        break;
                    }
                    case "--remote": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setFetchStrategy(NutsFetchStrategy.REMOTE);
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
                    case "--embedded":
                    case "-b": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setExecutionType(NutsExecutionType.EMBEDDED);
                        }
                        //ignore
                        break;
                    }
                    case "--external":
                    case "--spawn":
                    case "-x": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setExecutionType(NutsExecutionType.SPAWN);
                        }
                        break;
                    }
                    case "--user-cmd": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setExecutionType(NutsExecutionType.USER_CMD);
                        }
                        break;
                    }
                    case "--root-cmd": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setExecutionType(NutsExecutionType.ROOT_CMD);
                        }
                        break;
                    }
                    case "-o":
                    case "--open-mode": {
                        a = cmdLine.nextString();
                        String v = a.getStringValue();
                        if (enabled) {
                            options.setOpenMode(parseNutsWorkspaceOpenMode(v));
                        }
                        break;
                    }
                    case "--open": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            options.setOpenMode(NutsWorkspaceOpenMode.OPEN_EXISTING);
                        }
                        break;
                    }
                    case "--create": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
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
                            if (!a.getArgumentValue().isNull()) {
                                throw new NutsIllegalArgumentException(null, "Invalid argument for workspace : " + a.getString());
                            }
                            applicationArguments.add(NutsConstants.Ids.NUTS_SHELL);
                            if (!cmdLine.isEmpty()) {
                                applicationArguments.add("-c");
                                applicationArguments.addAll(Arrays.asList(cmdLine.toStringArray()));
                            }
                            cmdLine.skipAll();
                        } else {
                            applicationArguments.addAll(Arrays.asList(cmdLine.toStringArray()));
                            cmdLine.skipAll();
                        }
                        break;
                    }

                    case "-version":
                    case "-v":
                    case "--version": {
                        cmdLine.skip();
                        if (enabled) {
                            applicationArguments.add("version");
                            applicationArguments.addAll(Arrays.asList(cmdLine.toStringArray()));
                            cmdLine.skipAll();
                        } else {
                            cmdLine.toStringArray();
                            cmdLine.skipAll();
                        }
                        break;
                    }
                    case "-Z":
                    case "--reset": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            if (a.getBooleanValue()) {
                                options.setReset(true);
                                options.setRecover(false);
                            }
                        } else {
                            cmdLine.skipAll();
                        }
                        break;
                    }
                    case "-z":
                    case "--recover": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            if (a.getBooleanValue()) {
                                options.setReset(false);
                                options.setRecover(true);
                            }
                        }
                        break;
                    }
                    case "-N":
                    case "--expire": {
                        a = cmdLine.next();
                        if (enabled) {
                            if (a.getStringValue() != null) {
                                options.setExpireTime(Instant.parse(a.getStringValue()));
                            } else {
                                options.setExpireTime(Instant.now());
                            }
                        }
                        break;
                    }
                    case "--out-line-prefix": {
                        a = cmdLine.nextString();
                        if (enabled) {
                            options.setOutLinePrefix(a.getStringValue());
                        }
                        break;
                    }
                    case "--err-line-prefix": {
                        a = cmdLine.nextString();
                        if (enabled) {
                            options.setErrLinePrefix(a.getStringValue());
                        }
                        break;
                    }
                    case "--line-prefix": {
                        a = cmdLine.nextString();
                        if (enabled) {
                            options.setOutLinePrefix(a.getStringValue());
                            options.setErrLinePrefix(a.getStringValue());
                        }
                        break;
                    }
                    case "-e":
                    case "--exec": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            while ((a = cmdLine.next()) != null) {
                                if (a.isOption()) {
                                    executorOptions.add(a.getString());
                                } else {
                                    applicationArguments.add(a.getString());
                                    applicationArguments.addAll(Arrays.asList(cmdLine.toStringArray()));
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
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getBooleanValue()) {
                            applicationArguments.add("help");
                            applicationArguments.addAll(Arrays.asList(cmdLine.toStringArray()));
                            cmdLine.skipAll();
                        } else {
                            cmdLine.skipAll();
                        }
                        break;
                    }
                    case "--skip-errors": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setSkipErrors(a.getBooleanValue());
                        }
                        break;
                    }

                    //ERRORS
                    case "-I":
                    case "-U":
                    case "-S":
                    case "-G":
                    case "-H":
                    case "-J":
                    case "-L":
                    case "-M":
                    case "-W":
                    case "-X":
                    case "-B":
                    case "-i":
                    case "-q":
                    case "-s":
                    case "-d":
                    case "-l":
                    case "-m":
                    default: {
                        cmdLine.skip();
                        showError.add("nuts: invalid option " + a.getString());
                    }
                }
            } else {
                applicationArguments.addAll(Arrays.asList(cmdLine.toStringArray()));
                cmdLine.skipAll();
            }
        }

        options.setLogConfig(logConfig);
        options.setExcludedExtensions(excludedExtensions.toArray(new String[0]));
        options.setExcludedRepositories(excludedRepositories.toArray(new String[0]));
        options.setTransientRepositories(tempRepositories.toArray(new String[0]));
        options.setApplicationArguments(applicationArguments.toArray(new String[0]));
        options.setExecutorOptions(executorOptions.toArray(new String[0]));
        options.setErrors(showError.toArray(new String[0]));
        //error only if not asking for help
        if (!(applicationArguments.size() > 0
                && (applicationArguments.get(0).equals("help")
                || applicationArguments.get(0).equals("--help")
        )
        )) {
            if (!showError.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();
                for (String s : showError) {
                    errorMessage.append(s).append("\n");
                }
                errorMessage.append("Try 'nuts --help' for more information.");
                if (!options.isSkipErrors()) {
                    throw new NutsIllegalArgumentException(null, errorMessage.toString());
                } else {
                    System.err.println(errorMessage.toString());
                }
            }
        }
    }

    private static void parseLogLevel(NutsLogConfig logConfig, NutsCommandLine cmdLine, boolean enabled) {
        NutsArgument a = cmdLine.peek();
        switch (a.getStringKey()) {
            case "--log-file-size": {
                a = cmdLine.nextString();
                String v = a.getStringValue();
                if (enabled) {
                    logConfig.setLogFileSize(Integer.parseInt(v));
                }
                break;
            }

            case "--log-file-count": {
                a = cmdLine.nextString();
                if (enabled) {
                    logConfig.setLogFileCount(a.getArgumentValue().getInt());
                }
                break;
            }

            case "--log-file-name": {
                a = cmdLine.nextString();
                String v = a.getStringValue();
                if (enabled) {
                    logConfig.setLogFileName(v);
                }
                break;
            }

            case "--log-file-base": {
                a = cmdLine.nextString();
                String v = a.getStringValue();
                if (enabled) {
                    logConfig.setLogFileBase(v);
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
            case "--log-file-verbose":
            case "--log-file-finest":
            case "--log-file-finer":
            case "--log-file-fine":
            case "--log-file-info":
            case "--log-file-warning":
            case "--log-file-config":
            case "--log-file-severe":
            case "--log-file-all":
            case "--log-file-off": {
                cmdLine.skip();
                if (enabled) {
                    String id = a.getStringKey();
                    logConfig.setLogFileLevel(parseLevel(id.substring("--log-file-".length())));
                }
                break;
            }

            case "--log-term-verbose":
            case "--log-term-finest":
            case "--log-term-finer":
            case "--log-term-fine":
            case "--log-term-info":
            case "--log-term-warning":
            case "--log-term-config":
            case "--log-term-severe":
            case "--log-term-all":
            case "--log-term-off": {
                cmdLine.skip();
                if (enabled) {
                    String id = a.getStringKey();
                    logConfig.setLogTermLevel(parseLevel(id.substring("--log-term-".length())));
                }
                break;
            }

            case "--verbose": {
                cmdLine.skip();
                if (enabled && a.getArgumentValue().getBoolean(true)) {
                    logConfig.setLogTermLevel(Level.FINEST);
                    logConfig.setLogFileLevel(Level.FINEST);
                }
                break;
            }
            case "--log-verbose":
            case "--log-finest":
            case "--log-finer":
            case "--log-fine":
            case "--log-info":
            case "--log-warning":
            case "--log-config":
            case "--log-severe":
            case "--log-all":
            case "--log-off": {
                cmdLine.skip();
                if (enabled) {
                    String id = a.getStringKey();
                    Level lvl = parseLevel(id.substring("--log-".length()));
                    logConfig.setLogTermLevel(lvl);
                    logConfig.setLogFileLevel(lvl);
                }
                break;
            }
        }
    }

    private static NutsStoreLocationStrategy parseNutsStoreLocationStrategy(String s) {
        String s0 = s;
        if (s == null || s.isEmpty()) {
            return null;
        }
        s = s.toUpperCase().replace('-', '_');
        switch (s) {
            case "S":
            case "STANDALONE":
                return NutsStoreLocationStrategy.STANDALONE;
            case "E":
            case "EXPLODED":
                return NutsStoreLocationStrategy.EXPLODED;
        }
        throw new IllegalArgumentException("Unable to parse value for NutsStoreLocationStrategy : " + s0);
    }

    private static NutsOsFamily parseNutsStoreLocationLayout(String s) {
        String s0 = s;
        if (s == null || s.isEmpty()) {
            return null;
        }
        s = s.toUpperCase().replace('-', '_');
        switch (s) {
            case "L":
            case "LINUX":
                return NutsOsFamily.LINUX;
            case "U":
            case "UNIX":
                return NutsOsFamily.UNIX;
            case "X":
            case "UNKNOWN":
                return NutsOsFamily.UNKNOWN;
            case "W":
            case "WINDOWS":
                return NutsOsFamily.WINDOWS;
            case "M":
            case "MACOS":
                return NutsOsFamily.MACOS;
            case "S":
            case "SYSTEM":
                return null;
        }
        throw new IllegalArgumentException("Unable to parse value for NutsStoreLocationLayout : " + s0);
    }

    private static NutsTerminalMode parseNutsTerminalMode(String s) {
        String s0 = s;
        if (s == null || s.isEmpty()) {
            return null;
        }
        s = s.toUpperCase().replace('-', '_');
        switch (s) {
            case "L":
            case "FILTERED":
                return NutsTerminalMode.FILTERED;
            case "F":
            case "FORMATTED":
                return NutsTerminalMode.FORMATTED;
            case "H":
            case "INHERITED":
                return NutsTerminalMode.INHERITED;
        }
        throw new IllegalArgumentException("Unable to parse value for NutsTerminalMode : " + s0);
    }

    private static NutsWorkspaceOpenMode parseNutsWorkspaceOpenMode(String s) {
        String s0 = s;
        if (s == null || s.isEmpty()) {
            return null;
        }
        s = s.toUpperCase().replace('-', '_').replace('/', '_');
        switch (s) {
            case "R":
            case "READ":
            case "O":
            case "OPEN": {
                return NutsWorkspaceOpenMode.OPEN_EXISTING;
            }
            case "W":
            case "WRITE":
            case "N":
            case "NEW":
            case "C":
            case "CREATE": {
                return NutsWorkspaceOpenMode.CREATE_NEW;
            }
            case "RW":
            case "R_W":
            case "READ_WRITE":
            case "ON":
            case "O_N":
            case "OPEN_NEW":
            case "OC":
            case "O_C":
            case "OPEN_CREATE": {
                return NutsWorkspaceOpenMode.OPEN_OR_CREATE;
            }
        }
        throw new IllegalArgumentException("Unable to parse value for NutsWorkspaceOpenMode : " + s0);
    }

    private static Level parseLevel(String s) {
        switch (s.trim().toLowerCase()) {
            case "off": {
                return Level.OFF;
            }
            case "verbose":
            case "finest": {
                return Level.FINEST;
            }
            case "finer": {
                return Level.FINER;
            }
            case "fine": {
                return Level.FINE;
            }
            case "info": {
                return Level.INFO;
            }
            case "all": {
                return Level.ALL;
            }
            case "warning": {
                return Level.WARNING;
            }
            case "severe": {
                return Level.SEVERE;
            }
            case "config": {
                return Level.CONFIG;
            }
        }
        return Level.INFO;
    }
}
