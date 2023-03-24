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
package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.util.NApiUtils;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.util.NLogConfig;
import net.thevpc.nuts.util.NLogUtils;

import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

/**
 * Nuts Arguments parser. Creates a {@link NWorkspaceOptions} instance from
 * string array of valid nuts options
 *
 * @author thevpc
 * @app.category Internal
 * @since 0.5.4
 */
public final class NReservedWorkspaceCmdLineParser {

    /**
     * private constructor
     */
    private NReservedWorkspaceCmdLineParser() {
    }


    /**
     * Fill a {@link NWorkspaceOptions} instance from string array of valid
     * nuts options
     *
     * @param bootArguments input arguments to parse
     * @param options       options instance to fill
     * @param session       session, can be null
     */
    public static void parseNutsArguments(String[] bootArguments, NWorkspaceOptionsBuilder options, NSession session) {
        List<NMsg> showError = new ArrayList<>();
        HashSet<String> excludedExtensions = new HashSet<>();
        HashSet<String> repositories = new HashSet<>();
        Set<String> customOptions = new LinkedHashSet<>();
        List<String> executorOptions = new ArrayList<>();
        NLogConfig logConfig = null;
        List<String> applicationArguments = new ArrayList<>();
        NCmdLine cmdLine = NCmdLine.of(bootArguments)
                .setCommandName("nuts")
                .setExpandSimpleOptions(true)
                .registerSpecialSimpleOption("-version");
        boolean explicitConfirm = false;
        while (cmdLine.hasNext()) {
            NArg a = cmdLine.peek().get(session);

            if (a.isOption()) {
                boolean active = a.isActive();
                String k = a.key();
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
                        a = cmdLine.nextEntry().get(session);
                        String file = a.getStringValue().orElse("");
                        if (active) {
                            options.setWorkspace(file);
                        }
                        break;
                    }
                    case "--user":
                    case "-u": {
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().orElse("");
                        if (active) {
                            options.setUserName(v);
                        }
                        break;
                    }
                    case "--password":
                    case "-p": {
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().orElse("");
                        if (active) {
                            options.setCredentials(v.toCharArray());
                        }
                        break;
                    }
                    case "-V":
                    case "--boot-version":
                    case "--boot-api-version": {
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().get(session);
                        if (active) {
                            options.setApiVersion(NVersion.of(v).get(session));
                        }
                        break;
                    }
                    case "--boot-runtime": {
                        a = cmdLine.nextEntry().get(session);
                        String br = a.getStringValue().orElse("");
                        if (active) {
                            if (br.indexOf("#") > 0) {
                                //this is a full id
                                options.setRuntimeId(NId.of(br).get(session));
                            } else {
                                options.setRuntimeId(NId.ofRuntime(br).orNull());
                            }
                        }
                        break;
                    }
                    case "--java":
                    case "--boot-java":
                    case "-j": {
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().orElse("");
                        if (active) {
                            options.setJavaCommand(v);
                        }
                        break;
                    }
                    case "--java-home":
                    case "--boot-java-home": {
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().get(session);
                        if (active) {
                            options.setJavaCommand(NReservedUtils.resolveJavaCommand(v));
                        }
                        break;
                    }
                    case "--java-options":
                    case "--boot-java-options":
                    case "-J": {
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().orElse("");
                        if (active) {
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
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().get(session);
                        if (active) {
                            options.setName(v);
                        }
                        break;
                    }
                    case "--archetype":
                    case "-A": {
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().get(session);
                        if (active) {
                            options.setArchetype(v);
                        }
                        break;
                    }
                    case "--store-strategy": {
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().orElse("");
                        if (active) {
                            options.setStoreLocationStrategy(parseNutsStoreLocationStrategy(v));
                        }
                        break;
                    }
                    case "-S":
                    case "--standalone":
                    case "--standalone-workspace": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setStoreLocationStrategy(NStoreLocationStrategy.STANDALONE);
                        }
                        break;

                    }
                    case "-E":
                    case "--exploded":
                    case "--exploded-workspace": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setStoreLocationStrategy(NStoreLocationStrategy.EXPLODED);
                        }
                        break;
                    }

                    case "--repo-store-strategy": {
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().get(session);
                        if (active) {
                            options.setRepositoryStoreLocationStrategy(parseNutsStoreLocationStrategy(v));
                        }
                        break;
                    }
                    case "--exploded-repositories": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setRepositoryStoreLocationStrategy(NStoreLocationStrategy.EXPLODED);
                        }
                        break;
                    }
                    case "--standalone-repositories": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setRepositoryStoreLocationStrategy(NStoreLocationStrategy.STANDALONE);
                        }
                        break;
                    }
                    case "--store-layout": {
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().get(session);
                        if (active) {
                            options.setStoreLocationLayout(parseNutsOsFamily(v));
                        }
                        break;
                    }
                    case "--system-layout": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setStoreLocationLayout(null);
                        }
                        break;
                    }
                    case "--windows-layout": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setStoreLocationLayout(NOsFamily.WINDOWS);
                        }
                        break;
                    }
                    case "--macos-layout": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setStoreLocationLayout(NOsFamily.MACOS);
                        }
                        break;
                    }
                    case "--linux-layout": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setStoreLocationLayout(NOsFamily.LINUX);
                        }
                        break;
                    }
                    case "--unix-layout": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setStoreLocationLayout(NOsFamily.UNIX);
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
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().get(session);
                        if (active) {
                            NStoreLocation m = NStoreLocation.valueOf(k.substring(2, k.indexOf('-', 2)).toUpperCase());
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
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().get(session);
                        NStoreLocation folder = NStoreLocation.valueOf(
                                k.substring(3 + "system".length(), k.indexOf('-', 3 + "system".length())).toUpperCase());
                        if (active) {
                            options.setHomeLocation(NHomeLocation.of(null, folder), v);
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
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().get(session);
                        NOsFamily layout = NOsFamily.valueOf(k.substring(2, k.indexOf('-', 2)).toUpperCase());
                        NStoreLocation folder = NStoreLocation.valueOf(k.substring(3 + layout.toString().length(), k.indexOf('-', 3 + layout.toString().length())).toUpperCase());
                        if (active) {
                            options.setHomeLocation(NHomeLocation.of(layout, folder), v);
                        }
                        break;
                    }
                    case "--install-companions":
                    case "-k": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setInstallCompanions(a.getBooleanValue().get());
                        }
                        break;
                    }
                    case "--skip-welcome":
                    case "-K": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setSkipWelcome(a.getBooleanValue().get(session));
                        }
                        break;
                    }
                    case "--skip-boot":
                    case "-Q": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setSkipBoot(a.getBooleanValue().get(session));
                        }
                        break;
                    }
                    case "--switch": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setSwitchWorkspace(a.getBooleanValue().orElse(true));
                        }
                        break;
                    }

                    //**********************************
                    //*
                    //* Open Exported Options
                    //*
                    //**********************************
                    //
                    //  [[open exported options]] are open (so transient, non-
                    // persistent) options that will override any configured 
                    // value (if any) having the ability to be exported 
                    // to any java child process (as system property -D...) 
                    case "-g":
                    case "--global": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setGlobal(a.getBooleanValue().get(session));
                        }
                        break;
                    }

                    case "--gui": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setGui(a.getBooleanValue().get(session));
                        }
                        break;
                    }

                    case "--color":
                    case "-c": {
                        //if the value is not immediately attached with '=' don't consider
                        a = cmdLine.next().get(session);
                        if (active) {
                            options.setTerminalMode(a.getStringValue().flatMap(NTerminalMode::parse)
                                    .ifEmpty(NTerminalMode.FORMATTED).get(session));
                        }
                        break;
                    }
                    case "-B":
                    case "--bot": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setBot(a.getBooleanValue().get(session));
                        }
                        break;
                    }
                    case "-R":
                    case "--read-only": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setReadOnly(a.getBooleanValue().get(session));
                        }
                        break;
                    }
                    case "-t":
                    case "--trace": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setTrace(a.getBooleanValue().get(session));
                        }
                        break;
                    }
                    case "-P":
                    case "--progress": {
                        a = cmdLine.next().get(session);
                        if (active) {
                            String s = a.getStringValue().orNull();
                            if (a.isNegated()) {
                                if (NBlankable.isBlank(s)) {
                                    s = "false";
                                } else {
                                    s = "false," + s;
                                }
                                options.setProgressOptions(s);
                            } else {
                                if (NBlankable.isBlank(s)) {
                                    s = "true";
                                } else {
                                    s = "true," + s;
                                }
                                options.setProgressOptions(s);
                            }
                        }
                        break;
                    }
                    case "--solver": {
                        a = cmdLine.nextEntry().get(session);
                        if (active) {
                            String s = a.getStringValue().get(session);
                            options.setDependencySolver(s);
                        }
                        break;
                    }
                    case "--dry":
                    case "-D": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setDry(a.getBooleanValue().get(session));
                        }
                        break;
                    }

                    case "--debug": {
                        a = cmdLine.next().get(session);
                        if (active) {
                            if (NBlankable.isBlank(a.getStringValue())) {
                                options.setDebug(String.valueOf(a.isEnabled()));
                            } else {
                                if (a.isNegated()) {
                                    options.setDebug(
                                            String.valueOf(!
                                                    NLiteral.of(a.getStringValue().get(session)).asBoolean()
                                                            .ifEmpty(true).ifError(false).get()));
                                } else {
                                    options.setDebug(a.getStringValue().get(session));
                                }
                            }
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
                        if (active) {
                            if (logConfig == null) {
                                logConfig = new NLogConfig();
                            }
                        }
                        parseLogLevel(logConfig, cmdLine, active, session);
                        break;
                    }
                    case "-X":
                    case "--exclude-extension": {
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().get(session);
                        if (active) {
                            excludedExtensions.add(v);
                        }
                        break;
                    }

                    case "--repository":
                    case "--repositories":
                    case "--repo":
                    case "--repos":
                    case "-r": {
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().get(session);
                        if (active) {
                            repositories.add(v);
                        }
                        break;
                    }

                    case "--output-format-option":
                    case "-T":
                        if (active) {
                            options.addOutputFormatOptions(cmdLine.nextEntry().get(session).getStringValue().get(session));
                        } else {
                            cmdLine.skip();
                        }
                        break;
                    case "-O":
                    case "--output-format":
                        a = cmdLine.nextEntry().get(session);
                        if (active) {
                            String t = a.getStringValue().orElse("");
                            int i = NReservedStringUtils.firstIndexOf(t, new char[]{' ', ';', ':', '='});
                            if (i > 0) {
                                options.setOutputFormat(NContentType.valueOf(t.substring(0, i).toUpperCase()));
                                options.addOutputFormatOptions(t.substring(i + 1).toUpperCase());
                            } else {
                                options.setOutputFormat(NContentType.valueOf(t.toUpperCase()));
                                options.addOutputFormatOptions("");
                            }
                        }
                        break;
                    case "--tson":
                        a = cmdLine.next().get(session);
                        if (active) {
                            options.setOutputFormat(NContentType.TSON);
                            options.addOutputFormatOptions(a.getStringValue().orElse(""));
                        }
                        break;
                    case "--yaml":
                        a = cmdLine.next().get(session);
                        if (active) {
                            options.setOutputFormat(NContentType.YAML);
                            options.addOutputFormatOptions(a.getStringValue().orElse(""));
                        }
                        break;
                    case "--json":
                        a = cmdLine.next().get(session);
                        if (active) {
                            options.setOutputFormat(NContentType.JSON);
                            options.addOutputFormatOptions(a.getStringValue().orElse(""));
                        }
                        break;
                    case "--plain":
                        a = cmdLine.next().get(session);
                        if (active) {
                            options.setOutputFormat(NContentType.PLAIN);
                            options.addOutputFormatOptions(a.getStringValue().orElse(""));
                        }
                        break;
                    case "--xml":
                        a = cmdLine.next().get(session);
                        if (active) {
                            options.setOutputFormat(NContentType.XML);
                            options.addOutputFormatOptions(a.getStringValue().orElse(""));
                        }
                        break;
                    case "--table":
                        a = cmdLine.next().get(session);
                        if (active) {
                            options.setOutputFormat(NContentType.TABLE);
                            options.addOutputFormatOptions(a.getStringValue().orElse(""));
                        }
                        break;
                    case "--tree":
                        a = cmdLine.next().get(session);
                        if (active) {
                            options.setOutputFormat(NContentType.TREE);
                            options.addOutputFormatOptions(a.getStringValue().orElse(""));
                        }
                        break;
                    case "--props":
                        a = cmdLine.next().get(session);
                        if (active) {
                            options.setOutputFormat(NContentType.PROPS);
                            options.addOutputFormatOptions(a.getStringValue().orElse(""));
                        }
                        break;
                    case "--yes":
                    case "-y": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            explicitConfirm = true;
                            options.setConfirm(NConfirmationMode.YES);
                        }
                        break;
                    }
                    case "--no":
                    case "-n": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            explicitConfirm = true;
                            options.setConfirm(NConfirmationMode.NO);
                        }
                        break;
                    }
                    case "--error": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            explicitConfirm = true;
                            options.setConfirm(NConfirmationMode.ERROR);
                        }
                        break;
                    }
                    case "--ask": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            explicitConfirm = true;
                            options.setConfirm(NConfirmationMode.ASK);
                        }
                        break;
                    }
                    case "--cached": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setCached(a.getBooleanValue().get(session));
                        }
                        break;
                    }
                    case "--indexed": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setIndexed(a.getBooleanValue().get(session));
                        }
                        break;
                    }
                    case "--transitive": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setTransitive(a.getBooleanValue().get(session));
                        }
                        break;
                    }
                    case "-f":
                    case "--fetch": {
                        a = cmdLine.nextEntry().get(session);
                        if (active) {
                            options.setFetchStrategy(a.getStringValue()
                                    .flatMap(NFetchStrategy::parse).get(session));
                        }
                        break;
                    }
                    case "-a":
                    case "--anywhere": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setFetchStrategy(NFetchStrategy.ANYWHERE);
                        }
                        break;
                    }
                    case "-F":
                    case "--offline": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setFetchStrategy(NFetchStrategy.OFFLINE);
                        }
                        break;
                    }
                    case "--online": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setFetchStrategy(NFetchStrategy.ONLINE);
                        }
                        break;
                    }
                    case "--remote": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setFetchStrategy(NFetchStrategy.REMOTE);
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
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setExecutionType(NExecutionType.EMBEDDED);
                        }
                        //ignore
                        break;
                    }
                    case "--open-file": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setExecutionType(NExecutionType.OPEN);
                        }
                        //ignore
                        break;
                    }
                    case "--external":
                    case "--spawn":
                    case "-x": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setExecutionType(NExecutionType.SPAWN);
                        }
                        break;
                    }
                    case "--user-cmd"://deprecated since 0.8.1
                    case "--system": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setExecutionType(NExecutionType.SYSTEM);
                        }
                        break;
                    }
                    case "--root-cmd": //deprecated since 0.8.1
                    case "--as-root": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setRunAs(NRunAs.root());
                        }
                        break;
                    }
                    case "--current-user": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setRunAs(NRunAs.currentUser());
                        }
                        break;
                    }
                    case "--run-as": {
                        a = cmdLine.nextEntry().get(session);
                        if (active) {
                            options.setRunAs(NRunAs.user(a.getStringValue().get(session)));
                        }
                        break;
                    }
                    case "-o":
                    case "--open-mode": {
                        a = cmdLine.nextEntry().get(session);
                        String v = a.getStringValue().get(session);
                        if (active) {
                            options.setOpenMode(parseNutsOpenMode(v));
                        }
                        break;
                    }
                    case "--open-or-error":
                    case "--open": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setOpenMode(NOpenMode.OPEN_OR_ERROR);
                        }
                        break;
                    }
                    case "--create-or-error":
                    case "--create": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setOpenMode(NOpenMode.CREATE_OR_ERROR);
                        }
                        break;
                    }
                    case "--open-or-create": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setOpenMode(NOpenMode.OPEN_OR_CREATE);
                        }
                        break;
                    }
                    case "--open-or-null": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            options.setOpenMode(NOpenMode.OPEN_OR_NULL);
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
                        if (active) {
                            if (!a.getValue().isNull()) {
                                throw new NBootException(NMsg.ofC("invalid argument for workspace: %s", a.asString()));
                            }
                            applicationArguments.add(NConstants.Ids.NUTS_SHELL);
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
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setCommandVersion(a.isActive());
                        }
                        break;
                    }
                    case "-Z":
                    case "--reset": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            if (a.getBooleanValue().get(session)) {
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
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            if (a.getBooleanValue().get(session)) {
                                options.setReset(false);
                                options.setRecover(true);
                            }
                        }
                        break;
                    }
                    case "-N":
                    case "--expire": {
                        a = cmdLine.next().get(session);
                        if (active) {
                            if (!NBlankable.isBlank(a.getStringValue())) {
                                options.setExpireTime(a.getValue().asInstant().ifEmpty(null).get(session));
                            } else {
                                options.setExpireTime(Instant.now());
                            }
                        }
                        break;
                    }
                    case "--out-line-prefix": {
                        a = cmdLine.nextEntry().get(session);
                        if (active) {
                            options.setOutLinePrefix(a.getStringValue().get(session));
                        }
                        break;
                    }
                    case "--err-line-prefix": {
                        a = cmdLine.nextEntry().get(session);
                        if (active) {
                            options.setErrLinePrefix(a.getStringValue().get(session));
                        }
                        break;
                    }
                    case "--line-prefix": {
                        a = cmdLine.nextEntry().get(session);
                        if (active) {
                            options.setOutLinePrefix(a.getStringValue().get(session));
                            options.setErrLinePrefix(a.getStringValue().get(session));
                        }
                        break;
                    }
                    case "-e":
                    case "--exec": {
                        a = cmdLine.nextFlag().get(session);
                        if (active && a.getBooleanValue().get(session)) {
                            while ((a = cmdLine.next().orNull()) != null) {
                                if (a.isOption()) {
                                    executorOptions.add(a.asString().orElse(""));
                                } else {
                                    applicationArguments.add(a.asString().orElse(""));
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
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setCommandHelp(a.getBooleanValue().get(session));
                        }
                        break;
                    }
                    case "--skip-errors": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setSkipErrors(a.getBooleanValue().get(session));
                        }
                        break;
                    }
                    case "-L":
                    case "--locale": {
                        a = cmdLine.nextEntry().get(session);
                        if (active) {
                            options.setLocale(a.getStringValue().get(session));
                        }
                        break;
                    }
                    case "--theme": {
                        a = cmdLine.nextEntry().get(session);
                        if (active) {
                            options.setTheme(a.getStringValue().get(session));
                        }
                        break;
                    }
                    case "--sandbox": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setIsolationLevel(a.getBooleanValue().get(session) ? NIsolationLevel.SANDBOX : null);
                        }
                        break;
                    }
                    case "--confined": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setIsolationLevel(a.getBooleanValue().get(session) ? NIsolationLevel.CONFINED : null);
                        }
                        break;
                    }
                    case "--isolation-level": {
                        a = cmdLine.nextEntry().get(session);
                        if (active) {
                            options.setIsolationLevel(a.getStringValue().flatMap(NIsolationLevel::parse).get(session));
                        }
                        break;
                    }
                    case "--init-launchers": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setInitLaunchers(a.getBooleanValue().get(session));
                        }
                        break;
                    }
                    case "--init-java": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setInitJava(a.getBooleanValue().get(session));
                        }
                        break;
                    }
                    case "--init-platforms": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setInitPlatforms(a.getBooleanValue().get(session));
                        }
                        break;
                    }
                    case "--init-scripts": {
                        a = cmdLine.nextFlag().get(session);
                        if (active) {
                            options.setInitScripts(a.getBooleanValue().get(session));
                        }
                        break;
                    }
                    case "--desktop-launcher": {
                        a = cmdLine.nextEntry().get(session);
                        if (active) {
                            options.setDesktopLauncher(a.getStringValue().flatMap(NSupportMode::parse).get(session));
                        }
                        break;
                    }
                    case "--menu-launcher": {
                        a = cmdLine.nextEntry().get(session);
                        if (active) {
                            options.setMenuLauncher(a.getStringValue().flatMap(NSupportMode::parse).get(session));
                        }
                        break;
                    }
                    case "--user-launcher": {
                        a = cmdLine.nextEntry().get(session);
                        if (active) {
                            options.setUserLauncher(a.getStringValue().flatMap(NSupportMode::parse).get(session));
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
                            a = cmdLine.next().get(session);
                            customOptions.add(a.toString());
                        } else {
                            cmdLine.skip();
                            if (a.isActive()) {
                                showError.add(NMsg.ofC("nuts: invalid option %s", a.asString().orNull()));
                            }
                        }
                    }
                }
            } else {
                applicationArguments.addAll(Arrays.asList(cmdLine.toStringArray()));
                cmdLine.skipAll();
            }
        }

        options.setCustomOptions(new ArrayList<>(customOptions));
        options.setLogConfig(logConfig);
        options.setExcludedExtensions(new ArrayList<>(excludedExtensions));
        options.setRepositories(new ArrayList<>(repositories));
        options.setApplicationArguments(applicationArguments);
        options.setExecutorOptions(new ArrayList<>(executorOptions));
        options.setErrors(new ArrayList<>(showError));
        //error only if not asking for help
        if (!(applicationArguments.size() > 0
                && (
                applicationArguments.get(0).equals("help")
                        || options.getCommandHelp().orElse(false)
                        || applicationArguments.get(0).equals("version")
                        || options.getCommandVersion().orElse(false)
        )
        )) {
            if (!showError.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();
                for (NMsg s : showError) {
                    errorMessage.append(s).append("\n");
                }
                errorMessage.append("Try 'nuts --help' for more information.");
                if (!options.getSkipErrors().orElse(false)) {
                    throw new NBootException(NMsg.ofPlain(errorMessage.toString()));
                }
            }
        }
    }

    private static void parseLogLevel(NLogConfig logConfig, NCmdLine cmdLine, boolean enabled, NSession session) {
        NArg a = cmdLine.peek().get(session);
        switch (a.key()) {
            case "--log-file-size": {
                a = cmdLine.nextEntry().get(session);
                String v = a.getStringValue().get(session);
                if (enabled) {
                    Integer fileSize = NApiUtils.parseFileSizeInBytes(v, 1024 * 1024).orNull();
                    if (fileSize == null) {
                        if (NBlankable.isBlank(v)) {
                            throw new NBootException(NMsg.ofC("invalid file size : %s", v));
                        }
                    } else {
                        //always in mega
                        fileSize = fileSize / (1024 * 1024);
                        if (fileSize <= 0) {
                            throw new NBootException(NMsg.ofC("invalid file size : %s < 1Mb", v));
                        }
                    }
                    if (fileSize != null) {
                        logConfig.setLogFileSize(fileSize);
                    }
                }
                break;
            }

            case "--log-file-count": {
                a = cmdLine.nextEntry().get(session);
                if (enabled) {
                    logConfig.setLogFileCount(a.getValue().asInt().get(session));
                }
                break;
            }

            case "--log-file-name": {
                a = cmdLine.nextEntry().get(session);
                String v = a.getStringValue().get(session);
                if (enabled) {
                    logConfig.setLogFileName(v);
                }
                break;
            }

            case "--log-file-base": {
                a = cmdLine.nextEntry().get(session);
                String v = a.getStringValue().get(session);
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
                    String id = a.key();
                    logConfig.setLogFileLevel(
                            NLogUtils.parseLogLevel(id.substring("--log-file-".length())).orNull()
                    );
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
                    String id = a.key();
                    logConfig.setLogTermLevel(
                            NLogUtils.parseLogLevel(id.substring("--log-term-".length())).orNull()
                    );
                }
                break;
            }

            case "--verbose": {
                cmdLine.skip();
                if (enabled && a.getBooleanValue().orElse(true)) {
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
                    String id = a.key();
                    Level lvl = NLogUtils.parseLogLevel(id.substring("--log-".length())).orNull();
                    logConfig.setLogTermLevel(lvl);
                    logConfig.setLogFileLevel(lvl);
                }
                break;
            }
        }
    }

    private static NStoreLocationStrategy parseNutsStoreLocationStrategy(String s) {
        NStoreLocationStrategy m = NStoreLocationStrategy.parse(s).orNull();
        if (m == null && !NBlankable.isBlank(s)) {
            throw new NBootException(NMsg.ofC("unable to parse value for NutsStoreLocationStrategy : %s", s));
        }
        return m;
    }

    private static NOsFamily parseNutsOsFamily(String s) {
        NOsFamily m = NOsFamily.parse(s).orNull();
        if (m == null && !NBlankable.isBlank(s)) {
            throw new NBootException(NMsg.ofC("unable to parse value for NutsOsFamily : %s", s));
        }
        return m;
    }

    private static NOpenMode parseNutsOpenMode(String s) {
        NOpenMode m = NOpenMode.parse(s).orNull();
        if (m == null && !NBlankable.isBlank(s)) {
            throw new NBootException(NMsg.ofC("unable to parse value for NutsOpenMode : %s", s));
        }
        return m;
    }
}
