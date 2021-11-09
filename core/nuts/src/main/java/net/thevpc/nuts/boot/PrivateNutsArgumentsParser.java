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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

/**
 * Nuts Arguments parser. Creates a {@link NutsWorkspaceOptions} instance from
 * string array of valid nuts options
 *
 * @author thevpc
 * @app.category Internal
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
    public static void parseNutsArguments(String[] bootArguments, NutsWorkspaceOptionsBuilder options, PrivateNutsLog log) {
        List<NutsMessage> showError = new ArrayList<>();
        HashSet<String> excludedExtensions = new HashSet<>();
        HashSet<String> repositories = new HashSet<>();
        Set<String> customOptions = new LinkedHashSet<>();
        List<String> executorOptions = new ArrayList<>();
        NutsLogConfig logConfig = null;
        List<String> applicationArguments = new ArrayList<>();
        NutsCommandLine cmdLine = new PrivateNutsCommandLine(bootArguments)
                .setCommandName("nuts")
                .setExpandSimpleOptions(true)
                .registerSpecialSimpleOption("-version");
        boolean explicitConfirm = false;
        while (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.peek();

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
                        a = cmdLine.nextString();
                        String file = a.getValue().getString("");
                        if (enabled) {
                            options.setWorkspace(file);
                        }
                        break;
                    }
                    case "--user":
                    case "-u": {
                        a = cmdLine.nextString();
                        String v = a.getValue().getString("");
                        if (enabled) {
                            options.setUsername(v);
                        }
                        break;
                    }
                    case "--password":
                    case "-p": {
                        a = cmdLine.nextString();
                        String v = a.getValue().getString("");
                        if (enabled) {
                            options.setCredentials(v.toCharArray());
                        }
                        break;
                    }
                    case "-V":
                    case "--boot-version":
                    case "--boot-api-version": {
                        a = cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setApiVersion(v);
                        }
                        break;
                    }
                    case "--boot-runtime": {
                        a = cmdLine.nextString();
                        String br = a.getValue().getString("");
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
                        String v = a.getValue().getString("");
                        if (enabled) {
                            options.setJavaCommand(v);
                        }
                        break;
                    }
                    case "--java-home":
                    case "--boot-java-home": {
                        a = cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setJavaCommand(PrivateNutsUtils.resolveJavaCommand(v));
                        }
                        break;
                    }
                    case "--java-options":
                    case "--boot-java-options":
                    case "-J": {
                        a = cmdLine.nextString();
                        String v = a.getValue().getString("");
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
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setName(v);
                        }
                        break;
                    }
                    case "--archetype":
                    case "-A": {
                        a = cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setArchetype(v);
                        }
                        break;
                    }
                    case "--store-strategy": {
                        a = cmdLine.nextString();
                        String v = a.getValue().getString("");
                        if (enabled) {
                            options.setStoreLocationStrategy(parseNutsStoreLocationStrategy(v));
                        }
                        break;
                    }
                    case "-S":
                    case "--standalone":
                    case "--standalone-workspace": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
//                            options.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                        }
                        break;

                    }
                    case "-E":
                    case "--exploded":
                    case "--exploded-workspace": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
//                            options.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                        }
                        break;
                    }

                    case "--repo-store-strategy": {
                        a = cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setRepositoryStoreLocationStrategy(parseNutsStoreLocationStrategy(v));
                        }
                        break;
                    }
                    case "--exploded-repositories": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                        }
                        break;
                    }
                    case "--standalone-repositories": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE);
                        }
                        break;
                    }
                    case "--store-layout": {
                        a = cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setStoreLocationLayout(parseNutsOsFamily(v));
                        }
                        break;
                    }
                    case "--system-layout": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setStoreLocationLayout(null);
                        }
                        break;
                    }
                    case "--windows-layout": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setStoreLocationLayout(NutsOsFamily.WINDOWS);
                        }
                        break;
                    }
                    case "--macos-layout": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setStoreLocationLayout(NutsOsFamily.MACOS);
                        }
                        break;
                    }
                    case "--linux-layout": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setStoreLocationLayout(NutsOsFamily.LINUX);
                        }
                        break;
                    }
                    case "--unix-layout": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
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
                        String v = a.getValue().getString();
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
                        String v = a.getValue().getString();
                        NutsStoreLocation folder = NutsStoreLocation.valueOf(
                                k.substring(3 + "system".length(), k.indexOf('-', 3 + "system".length())).toUpperCase());
                        if (enabled) {
                            options.setHomeLocation(NutsHomeLocation.of(null, folder), v);
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
                        String v = a.getValue().getString();
                        NutsOsFamily layout = NutsOsFamily.valueOf(k.substring(2, k.indexOf('-', 2)).toUpperCase());
                        NutsStoreLocation folder = NutsStoreLocation.valueOf(k.substring(3 + layout.toString().length(), k.indexOf('-', 3 + layout.toString().length())).toUpperCase());
                        if (enabled) {
                            options.setHomeLocation(NutsHomeLocation.of(layout, folder), v);
                        }
                        break;
                    }
                    case "--skip-companions":
                    case "-k": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setSkipCompanions(a.getValue().getBoolean());
                        }
                        break;
                    }
                    case "--skip-welcome":
                    case "-K": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setSkipWelcome(a.getValue().getBoolean());
                        }
                        break;
                    }
                    case "--skip-boot":
                    case "-Q": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setSkipBoot(a.getValue().getBoolean());
                        }
                        break;
                    }
                    case "--switch": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setSwitchWorkspace(a.getValue().getBoolean(true));
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
                            options.setGlobal(a.getValue().getBoolean());
                        }
                        break;
                    }

                    case "--gui": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setGui(a.getValue().getBoolean());
                        }
                        break;
                    }

                    case "--color":
                    case "-c": {
                        //if the value is not immediately attached with '=' don't consider
                        a = cmdLine.next();
                        if (enabled) {
                            String v = a.getValue().getString("");
                            if (v.isEmpty()) {
                                options.setTerminalMode(a.isNegated() ? NutsTerminalMode.FILTERED : NutsTerminalMode.FORMATTED);
                            } else {
                                Boolean b = NutsUtilStrings.parseBoolean(v, null, null);
                                if (b != null) {
                                    if (b) {
                                        options.setTerminalMode(a.isNegated() ? NutsTerminalMode.FILTERED : NutsTerminalMode.FORMATTED);
                                    } else {
                                        options.setTerminalMode(a.isNegated() ? NutsTerminalMode.FORMATTED : NutsTerminalMode.FILTERED);
                                    }
                                } else {
                                    switch (v.toLowerCase()) {
                                        case "formatted": {
                                            options.setTerminalMode(a.isNegated() ? NutsTerminalMode.FILTERED : NutsTerminalMode.FORMATTED);
                                            break;
                                        }
                                        case "filtered": {
                                            options.setTerminalMode(a.isNegated() ? NutsTerminalMode.FORMATTED : NutsTerminalMode.FILTERED);
                                            break;
                                        }
                                        case "h":
                                        case "inherited": {
                                            options.setTerminalMode(a.isNegated() ? NutsTerminalMode.FORMATTED : NutsTerminalMode.INHERITED);
                                            break;
                                        }
                                        case "a":
                                        case "ansi": {
                                            options.setTerminalMode(a.isNegated() ? NutsTerminalMode.FORMATTED : NutsTerminalMode.ANSI);
                                            break;
                                        }
                                        case "s":
                                        case "auto":
                                        case "system": {
                                            options.setTerminalMode(a.isNegated() ? NutsTerminalMode.FORMATTED : null);
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
                    case "-B":
                    case "--bot": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setBot(a.getValue().getBoolean());
                        }
                        break;
                    }
                    case "-R":
                    case "--read-only": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setReadOnly(a.getValue().getBoolean());
                        }
                        break;
                    }
                    case "-t":
                    case "--trace": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setTrace(a.getValue().getBoolean());
                        }
                        break;
                    }
                    case "-P":
                    case "--progress": {
                        a = cmdLine.nextString();
                        if (enabled) {
                            String s = a.getValue().getString();
                            if (a.isNegated()) {
                                Boolean q = NutsUtilStrings.parseBoolean(s, true, null);
                                if (q == null) {
                                    if (NutsBlankable.isBlank(s)) {
                                        s = "false";
                                    } else {
                                        s = "false," + s;
                                    }
                                }
                                options.setProgressOptions(s);
                            } else {
                                options.setProgressOptions(s);
                            }
                        }
                        break;
                    }
                    case "--solver": {
                        a = cmdLine.nextString();
                        if (enabled) {
                            String s = a.getValue().getString();
                            options.setDependencySolver(s);
                        }
                        break;
                    }
                    case "--dry":
                    case "-D": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setDry(a.getValue().getBoolean());
                        }
                        break;
                    }

                    case "--debug": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setDebug(a.getValue().getBoolean());
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
                    case "--log-file-count": {
                        if (enabled) {
                            if (logConfig == null) {
                                logConfig = new NutsLogConfig();
                            }
                        }
                        parseLogLevel(logConfig, cmdLine, enabled);
                        break;
                    }
                    case "-X":
                    case "--exclude-extension": {
                        a = cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            excludedExtensions.add(v);
                        }
                        break;
                    }

                    case "--repository":
                    case "--repositories":
                    case "--repo":
                    case "--repos":
                    case "-r": {
                        a = cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            repositories.add(v);
                        }
                        break;
                    }

                    case "--output-format-option":
                    case "-T":
                        if (enabled) {
                            options.addOutputFormatOptions(cmdLine.nextString().getValue().getString());
                        } else {
                            cmdLine.skip();
                        }
                        break;
                    case "-O":
                    case "--output-format":
                        a = cmdLine.nextString();
                        if (enabled) {
                            String t = a.getValue().getString("");
                            int i = PrivateNutsUtils.firstIndexOf(t, new char[]{' ', ';', ':', '='});
                            if (i > 0) {
                                options.setOutputFormat(NutsContentType.valueOf(t.substring(0, i).toUpperCase()));
                                options.addOutputFormatOptions(t.substring(i + 1).toUpperCase());
                            } else {
                                options.setOutputFormat(NutsContentType.valueOf(t.toUpperCase()));
                                options.addOutputFormatOptions("");
                            }
                        }
                        break;
                    case "--tson":
                        a = cmdLine.next();
                        if (enabled) {
                            options.setOutputFormat(NutsContentType.TSON);
                            options.addOutputFormatOptions(a.getValue().getString(""));
                        }
                        break;
                    case "--yaml":
                        a = cmdLine.next();
                        if (enabled) {
                            options.setOutputFormat(NutsContentType.YAML);
                            options.addOutputFormatOptions(a.getValue().getString(""));
                        }
                        break;
                    case "--json":
                        a = cmdLine.next();
                        if (enabled) {
                            options.setOutputFormat(NutsContentType.JSON);
                            options.addOutputFormatOptions(a.getValue().getString(""));
                        }
                        break;
                    case "--plain":
                        a = cmdLine.next();
                        if (enabled) {
                            options.setOutputFormat(NutsContentType.PLAIN);
                            options.addOutputFormatOptions(a.getValue().getString(""));
                        }
                        break;
                    case "--xml":
                        a = cmdLine.next();
                        if (enabled) {
                            options.setOutputFormat(NutsContentType.XML);
                            options.addOutputFormatOptions(a.getValue().getString(""));
                        }
                        break;
                    case "--table":
                        a = cmdLine.next();
                        if (enabled) {
                            options.setOutputFormat(NutsContentType.TABLE);
                            options.addOutputFormatOptions(a.getValue().getString(""));
                        }
                        break;
                    case "--tree":
                        a = cmdLine.next();
                        if (enabled) {
                            options.setOutputFormat(NutsContentType.TREE);
                            options.addOutputFormatOptions(a.getValue().getString(""));
                        }
                        break;
                    case "--props":
                        a = cmdLine.next();
                        if (enabled) {
                            options.setOutputFormat(NutsContentType.PROPS);
                            options.addOutputFormatOptions(a.getValue().getString(""));
                        }
                        break;
                    case "--yes":
                    case "-y": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            explicitConfirm = true;
                            options.setConfirm(NutsConfirmationMode.YES);
                        }
                        break;
                    }
                    case "--no":
                    case "-n": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            explicitConfirm = true;
                            options.setConfirm(NutsConfirmationMode.NO);
                        }
                        break;
                    }
                    case "--error": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            explicitConfirm = true;
                            options.setConfirm(NutsConfirmationMode.ERROR);
                        }
                        break;
                    }
                    case "--ask": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            explicitConfirm = true;
                            options.setConfirm(NutsConfirmationMode.ASK);
                        }
                        break;
                    }
                    case "--cached": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setCached(a.getValue().getBoolean());
                        }
                        break;
                    }
                    case "--indexed": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setIndexed(a.getValue().getBoolean());
                        }
                        break;
                    }
                    case "--transitive": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setTransitive(a.getValue().getBoolean());
                        }
                        break;
                    }
                    case "-f":
                    case "--fetch": {
                        a = cmdLine.nextString();
                        if (enabled) {
                            options.setFetchStrategy(NutsFetchStrategy.valueOf(a.getValue().getString().toUpperCase().replace("-", "_")));
                        }
                        break;
                    }
                    case "-a":
                    case "--anywhere": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setFetchStrategy(NutsFetchStrategy.ANYWHERE);
                        }
                        break;
                    }
//                    case "-i":
//                    case "--installed":
//                    {
//                        a = cmdLine.nextBoolean();
//                        if (enabled && a.getValue().getBoolean()) {
//                            options.setFetchStrategy(NutsFetchStrategy.INSTALLED);
//                        }
//                        break;
//                    }
                    case "-F":
                    case "--offline": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setFetchStrategy(NutsFetchStrategy.OFFLINE);
                        }
                        break;
                    }
                    case "--online": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setFetchStrategy(NutsFetchStrategy.ONLINE);
                        }
                        break;
                    }
                    case "--remote": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
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
                    // Such options will be considered in creating workspaces
                    // as well but still they are not persistent.
                    case "--embedded":
                    case "-b": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setExecutionType(NutsExecutionType.EMBEDDED);
                        }
                        //ignore
                        break;
                    }
                    case "--open-file": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setExecutionType(NutsExecutionType.OPEN);
                        }
                        //ignore
                        break;
                    }
                    case "--external":
                    case "--spawn":
                    case "-x": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setExecutionType(NutsExecutionType.SPAWN);
                        }
                        break;
                    }
                    case "--user-cmd"://deprecated since 0.8.1
                    case "--system": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setExecutionType(NutsExecutionType.SYSTEM);
                        }
                        break;
                    }
                    case "--root-cmd": //deprecated since 0.8.1
                    case "--as-root": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setRunAs(NutsRunAs.root());
                        }
                        break;
                    }
                    case "--current-user": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setRunAs(NutsRunAs.currentUser());
                        }
                        break;
                    }
                    case "--run-as": {
                        a = cmdLine.nextString();
                        if (enabled) {
                            if (NutsBlankable.isBlank(a.getValue().getString())) {
                                throw new NutsBootException(NutsMessage.cstyle("missing user name"));
                            }
                            options.setRunAs(NutsRunAs.user(a.getValue().getString()));
                        }
                        break;
                    }
                    case "-o":
                    case "--open-mode": {
                        a = cmdLine.nextString();
                        String v = a.getValue().getString();
                        if (enabled) {
                            options.setOpenMode(parseNutsOpenMode(v));
                        }
                        break;
                    }
                    case "--open-or-error":
                    case "--open": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setOpenMode(NutsOpenMode.OPEN_OR_ERROR);
                        }
                        break;
                    }
                    case "--create-or-error":
                    case "--create": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setOpenMode(NutsOpenMode.CREATE_OR_ERROR);
                        }
                        break;
                    }
                    case "--open-or-create": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setOpenMode(NutsOpenMode.OPEN_OR_CREATE);
                        }
                        break;
                    }
                    case "--open-or-null": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
                            options.setOpenMode(NutsOpenMode.OPEN_OR_NULL);
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
                                throw new NutsBootException(NutsMessage.cstyle("invalid argument for workspace: %s", a.getString()));
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
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setCommandVersion(a.isEnabled());
                        }
                        break;
                    }
                    case "-Z":
                    case "--reset": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            if (a.getValue().getBoolean()) {
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
                            if (a.getValue().getBoolean()) {
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
                            if (!NutsBlankable.isBlank(a.getValue().getString())) {
                                options.setExpireTime(Instant.parse(a.getValue().getString()));
                            } else {
                                options.setExpireTime(Instant.now());
                            }
                        }
                        break;
                    }
                    case "--out-line-prefix": {
                        a = cmdLine.nextString();
                        if (enabled) {
                            options.setOutLinePrefix(a.getValue().getString());
                        }
                        break;
                    }
                    case "--err-line-prefix": {
                        a = cmdLine.nextString();
                        if (enabled) {
                            options.setErrLinePrefix(a.getValue().getString());
                        }
                        break;
                    }
                    case "--line-prefix": {
                        a = cmdLine.nextString();
                        if (enabled) {
                            options.setOutLinePrefix(a.getValue().getString());
                            options.setErrLinePrefix(a.getValue().getString());
                        }
                        break;
                    }
                    case "-e":
                    case "--exec": {
                        a = cmdLine.nextBoolean();
                        if (enabled && a.getValue().getBoolean()) {
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
                        if (enabled) {
                            options.setCommandHelp(a.getValue().getBoolean());
                        }
                        break;
                    }
                    case "--skip-errors": {
                        a = cmdLine.nextBoolean();
                        if (enabled) {
                            options.setSkipErrors(a.getValue().getBoolean());
                        }
                        break;
                    }
                    case "-L":
                    case "--locale": {
                        a = cmdLine.nextString();
                        if (enabled) {
                            options.setLocale(a.getValue().getString());
                        }
                        break;
                    }
                    case "--theme": {
                        a = cmdLine.nextString();
                        if (enabled) {
                            options.setTheme(a.getValue().getString());
                        }
                        break;
                    }

                    //ERRORS
                    case "-C":
                    case "-I":
                    case "-U":
                    case "-G":
                    case "-H":
                    case "-M":
                    case "-W":
                    case "-i":
                    case "-q":
                    case "-s":
                    case "-d":
                    case "-l":
                    case "-m":
                    default: {
                        if (k.startsWith("---") && k.length() > 3 && k.charAt(3) != '-') {
                            a = cmdLine.next();
                            customOptions.add(a.toString().substring(3));
                        } else {
                            cmdLine.skip();
                            showError.add(NutsMessage.cstyle("nuts: invalid option %s", a.getString()));
                        }
                    }
                }
            } else {
                applicationArguments.addAll(Arrays.asList(cmdLine.toStringArray()));
                cmdLine.skipAll();
            }
        }

        options.setCustomOptions(customOptions.toArray(new String[0]));
        options.setLogConfig(logConfig);
        options.setExcludedExtensions(excludedExtensions.toArray(new String[0]));
        options.setRepositories(repositories.toArray(new String[0]));
        options.setApplicationArguments(applicationArguments.toArray(new String[0]));
        options.setExecutorOptions(executorOptions.toArray(new String[0]));
        options.setErrors(showError.toArray(new NutsMessage[0]));
        //error only if not asking for help
        if (!(applicationArguments.size() > 0
                && (
                applicationArguments.get(0).equals("help")
                        || options.isCommandHelp()
                        || applicationArguments.get(0).equals("version")
                        || options.isCommandVersion()
        )
        )) {
            if (!showError.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();
                for (NutsMessage s : showError) {
                    errorMessage.append(s).append("\n");
                }
                errorMessage.append("Try 'nuts --help' for more information.");
                if (!options.isSkipErrors()) {
                    throw new NutsBootException(NutsMessage.plain(errorMessage.toString()));
                } else {
                    log.log(Level.WARNING, NutsLogVerb.WARNING, NutsMessage.jstyle("{0}", errorMessage.toString()));
                }
            }
        }
    }

    private static void parseLogLevel(NutsLogConfig logConfig, NutsCommandLine cmdLine, boolean enabled) {
        NutsArgument a = cmdLine.peek();
        switch (a.getKey().getString()) {
            case "--log-file-size": {
                a = cmdLine.nextString();
                String v = a.getValue().getString();
                if (enabled) {
                    Integer fileSize = NutsApiUtils.parseFileSizeInBytes(v, 1024 * 1024, null, null);
                    if (fileSize == null) {
                        if (NutsBlankable.isBlank(v)) {
                            throw new NutsBootException(NutsMessage.cstyle("invalid file size : %s", v));
                        }
                    } else {
                        //always in mega
                        fileSize = fileSize / (1024 * 1024);
                        if (fileSize <= 0) {
                            throw new NutsBootException(NutsMessage.cstyle("invalid file size : %s < 1Mb", v));
                        }
                    }
                    if (fileSize != null) {
                        logConfig.setLogFileSize(fileSize);
                    }
                }
                break;
            }

            case "--log-file-count": {
                a = cmdLine.nextString();
                if (enabled) {
                    logConfig.setLogFileCount(a.getValue().getInt());
                }
                break;
            }

            case "--log-file-name": {
                a = cmdLine.nextString();
                String v = a.getValue().getString();
                if (enabled) {
                    logConfig.setLogFileName(v);
                }
                break;
            }

            case "--log-file-base": {
                a = cmdLine.nextString();
                String v = a.getValue().getString();
                if (enabled) {
                    logConfig.setLogFileBase(v);
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
                    String id = a.getKey().getString();
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
                    String id = a.getKey().getString();
                    logConfig.setLogTermLevel(parseLevel(id.substring("--log-term-".length())));
                }
                break;
            }

            case "--verbose": {
                cmdLine.skip();
                if (enabled && a.getValue().getBoolean(true)) {
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
                    String id = a.getKey().getString();
                    Level lvl = parseLevel(id.substring("--log-".length()));
                    logConfig.setLogTermLevel(lvl);
                    logConfig.setLogFileLevel(lvl);
                }
                break;
            }
        }
    }

    private static NutsStoreLocationStrategy parseNutsStoreLocationStrategy(String s) {
        NutsStoreLocationStrategy m = NutsStoreLocationStrategy.parseLenient(s, null, null);
        if (m == null && !NutsBlankable.isBlank(s)) {
            throw new NutsBootException(NutsMessage.cstyle("unable to parse value for NutsStoreLocationStrategy : %s", s));
        }
        return m;
    }

    private static NutsOsFamily parseNutsOsFamily(String s) {
        NutsOsFamily m = NutsOsFamily.parseLenient(s, null, null);
        if (m == null && !NutsBlankable.isBlank(s)) {
            throw new NutsBootException(NutsMessage.cstyle("unable to parse value for NutsOsFamily : %s", s));
        }
        return m;
    }

    private static NutsOpenMode parseNutsOpenMode(String s) {
        NutsOpenMode m = NutsOpenMode.parseLenient(s, null, null);
        if (m == null && !NutsBlankable.isBlank(s)) {
            throw new NutsBootException(NutsMessage.cstyle("unable to parse value for NutsOpenMode : %s", s));
        }
        return m;
    }

    private static Level parseLevel(String s) {
        Level m = NutsApiUtils.parseLenientLogLevel(s, null, null);
        if (m == null && !NutsBlankable.isBlank(s)) {
            throw new NutsBootException(NutsMessage.cstyle("unable to parse value for Level : %s", s));
        }
        return m;
    }
}
