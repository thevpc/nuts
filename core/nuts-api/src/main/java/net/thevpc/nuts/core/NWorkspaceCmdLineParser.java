/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.core;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExecutionType;
import net.thevpc.nuts.command.NFetchStrategy;
import net.thevpc.nuts.platform.NHomeLocation;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.log.NLogUtils;
import net.thevpc.nuts.internal.NApiUtilsRPI;
import net.thevpc.nuts.internal.NReservedUtils;
import net.thevpc.nuts.util.*;

import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Nuts Arguments parser. Creates a {@link NWorkspaceOptions} instance from
 * string array of valid nuts options
 *
 * @author thevpc
 * @app.category Internal
 * @since 0.5.4
 */
public final class NWorkspaceCmdLineParser {

    /**
     * private constructor
     */
    private NWorkspaceCmdLineParser() {
    }

    public static NOptional<List<NArg>> nextNutsArgument(NCmdLine cmdLine, NWorkspaceOptionsBuilder options) {
        while (cmdLine.hasNext()) {
            NArg a = cmdLine.peek().get();
            if (a.isOption()) {
                boolean active = a.isUncommented();
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
                        a = cmdLine.nextEntry().get();
                        if (active && options != null) {
                            String file = a.getStringValue().orElse("");
                            options.workspace(file);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--user":
                    case "-u": {
                        a = cmdLine.nextEntry().get();
                        if (active && options != null) {
                            String v = a.getStringValue().orElse("");
                            options.setUserName(v);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--password":
                    case "-p": {
                        a = cmdLine.nextEntry().get();
                        if (active && options != null) {
                            String v = a.getStringValue().orElse("");
                            options.credential(v.toCharArray());
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "-V":
                    case "--boot-version":
                    case "--boot-api-version": {
                        a = cmdLine.nextEntry().get();
                        if (active && options != null) {
                            String v = a.getStringValue().get();
                            options.apiVersion(NVersion.of(v));
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--boot-runtime": {
                        a = cmdLine.nextEntry().get();
                        if (active && options != null) {
                            String br = a.getStringValue().orElse("");
                            if (br.indexOf('#') >= 0) {
                                //this is a full id
                                options.runtimeId(NId.get(br).get());
                            } else {
                                options.runtimeId(NId.getRuntime(br).orNull());
                            }
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--java":
                    case "--boot-java":
                    case "-j": {
                        a = cmdLine.nextEntry().get();
                        if (active && options != null) {
                            String v = a.getStringValue().orElse("");
                            options.javaCommand(v);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--java-home":
                    case "--boot-java-home": {
                        a = cmdLine.nextEntry().get();
                        if (active && options != null) {
                            String v = a.getStringValue().get();
                            options.javaCommand(NReservedUtils.resolveJavaCommand(v));
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--java-options":
                    case "--boot-java-options":
                    case "-J": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().orElse("");
                        if (active && options != null) {
                            options.javaOptions(v);
                        }
                        return NOptional.of(Collections.singletonList(a));
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
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().get();
                        if (active && options != null) {
                            options.name(v);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--archetype":
                    case "-A": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().get();
                        if (active && options != null) {
                            options.archetype(v);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--store-strategy": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().orElse("");
                        if (active && options != null) {
                            options.storeStrategy(parseNutsStoreStrategy(v));
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "-S":
                    case "--standalone":
                    case "--standalone-workspace": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.storeStrategy(NStoreStrategy.STANDALONE);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "-E":
                    case "--exploded":
                    case "--exploded-workspace": {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null && a.getBooleanValue().get()) {
                            options.storeStrategy(NStoreStrategy.EXPLODED);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }

                    case "--repo-store-strategy": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().get();
                        if (active && options != null) {
                            options.repositoryStoreStrategy(parseNutsStoreStrategy(v));
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--exploded-repositories": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.repositoryStoreStrategy(NStoreStrategy.EXPLODED);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--standalone-repositories": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.repositoryStoreStrategy(NStoreStrategy.STANDALONE);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--store-layout": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().get();
                        if (active && options != null) {
                            options.storeLayout(parseNutsOsFamily(v));
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--system-layout": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.storeLayout(null);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--windows-layout": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.storeLayout(NOsFamily.WINDOWS);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--macos-layout": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.storeLayout(NOsFamily.MACOS);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--linux-layout": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.storeLayout(NOsFamily.LINUX);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--unix-layout": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.storeLayout(NOsFamily.UNIX);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--bin-location":
                    case "--config-location":
                    case "--var-location":
                    case "--log-location":
                    case "--temp-location":
                    case "--cache-location":
                    case "--lib-location": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().get();
                        if (active && options != null) {
                            NStoreType m = NStoreType.valueOf(k.substring(2, k.indexOf('-', 2)).toUpperCase());
                            options.setStoreLocation(m, v);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--system-bin-home":
                    case "--system-conf-home":
                    case "--system-var-home":
                    case "--system-log-home":
                    case "--system-temp-home":
                    case "--system-cache-home":
                    case "--system-lib-home":
                    case "--system-run-home": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().get();
                        NStoreType folder = NStoreType.valueOf(
                                k.substring(3 + "system".length(), k.indexOf('-', 3 + "system".length())).toUpperCase());
                        if (active && options != null) {
                            options.setHomeLocation(NHomeLocation.of(null, folder), v);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--windows-bin-home":
                    case "--windows-conf-home":
                    case "--windows-var-home":
                    case "--windows-log-home":
                    case "--windows-temp-home":
                    case "--windows-cache-home":
                    case "--windows-lib-home":
                    case "--windows-run-home":
                    case "--macos-bin-home":
                    case "--macos-conf-home":
                    case "--macos-var-home":
                    case "--macos-log-home":
                    case "--macos-temp-home":
                    case "--macos-cache-home":
                    case "--macos-lib-home":
                    case "--macos-run-home":
                    case "--linux-bin-home":
                    case "--linux-conf-home":
                    case "--linux-var-home":
                    case "--linux-log-home":
                    case "--linux-temp-home":
                    case "--linux-cache-home":
                    case "--linux-lib-home":
                    case "--linux-run-home":
                    case "--unix-bin-home":
                    case "--unix-conf-home":
                    case "--unix-var-home":
                    case "--unix-log-home":
                    case "--unix-temp-home":
                    case "--unix-cache-home":
                    case "--unix-lib-home":
                    case "--unix-run-home": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().get();
                        NOsFamily layout = NOsFamily.valueOf(k.substring(2, k.indexOf('-', 2)).toUpperCase());
                        NStoreType folder = NStoreType.valueOf(k.substring(3 + layout.toString().length(), k.indexOf('-', 3 + layout.toString().length())).toUpperCase());
                        if (active && options != null) {
                            options.setHomeLocation(NHomeLocation.of(layout, folder), v);
                        }
                        return NOptional.of(Collections.singletonList(a));

                    }
                    case "--install-companions":
                    case "-k": {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null) {
                            options.installCompanions(a.getBooleanValue().get());
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--skip-welcome":
                    case "-K": {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null) {
                            options.skipWelcome(a.getBooleanValue().get());
                        }
                        return NOptional.of(Collections.singletonList(a));

                    }
                    case "--skip-boot":
                    case "-Q": {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null) {
                            options.skipBoot(a.getBooleanValue().get());
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--switch": {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null) {
                            options.switchWorkspace(a.getBooleanValue().orElse(true));
                        }
                        return NOptional.of(Collections.singletonList(a));
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
                        a = cmdLine.nextFlag().get();
                        if (active && options != null) {
                            options.system(a.getBooleanValue().get());
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--shared-instance": {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null) {
                            options.sharedInstance(a.getBooleanValue().get());
                            return NOptional.of(Collections.singletonList(a));
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }

                    case "--gui": {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null) {
                            options.gui(a.getBooleanValue().get());
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }

                    case "--color":
                    case "-c": {
                        //if the value is not immediately attached with '=' don't consider
                        a = cmdLine.next().get();
                        if (active) {
                            if (options != null) {
                                if (a.isFlagOption()) {
                                    if (a.isNegated()) {
                                        options.terminalMode(NTerminalMode.INHERITED);
                                    } else {
                                        options.terminalMode(NTerminalMode.FORMATTED);
                                    }
                                } else {
                                    NTerminalMode v = a.getStringValue().flatMap(NTerminalMode::parse)
                                            .onEmpty(NTerminalMode.FORMATTED).get();
                                    if (v == NTerminalMode.DEFAULT) {
                                        v = NTerminalMode.INHERITED;
                                    }
                                    options.terminalMode(v);
                                }
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "-B":
                    case "--bot": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.bot(a.getBooleanValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "-U":
                    case "--preview-repo": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.previewRepo(a.getBooleanValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "-R":
                    case "--read-only": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.readOnly(a.getBooleanValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "-t":
                    case "--trace": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.trace(a.getBooleanValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "-P":
                    case "--progress": {
                        a = cmdLine.next().get();
                        if (active) {
                            if (options != null) {
                                String s = a.getStringValue().orNull();
                                if (a.isNegated()) {
                                    if (NBlankable.isBlank(s)) {
                                        s = "false";
                                    } else {
                                        s = "false," + s;
                                    }
                                    options.progressOptions(s);
                                } else {
                                    options.progressOptions(s);
                                }
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--solver": {
                        a = cmdLine.nextEntry().get();
                        if (active) {
                            if (options != null) {
                                String s = a.getStringValue().get();
                                options.dependencySolver(s);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--dry":
                    case "-D": {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null) {
                            options.dry(a.getBooleanValue().get());
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }

                    case "--stacktrace":
                    case "-d":
                    {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null) {
                            options.showStacktrace(a.getBooleanValue().get());
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }

                    case "--debug": {
                        a = cmdLine.next().get();
                        if (active) {
                            if (options != null) {
                                if (NBlankable.isBlank(a.getStringValue())) {
                                    options.debug(String.valueOf(a.isEnabled()));
                                } else {
                                    if (a.isNegated()) {
                                        options.debug(
                                                String.valueOf(!NLiteral.of(a.getStringValue().get()).asBoolean()
                                                        .onEmpty(true).onError(false).get()));
                                    } else {
                                        options.debug(a.getStringValue().get());
                                    }
                                }
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }

                    case "-l":
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
                            NLogConfig logConfig = options.logConfig().orNull();
                            if (logConfig == null) {
                                logConfig = new NLogConfig();
                            }
                            NOptional<NArg> r = parseLogLevel(logConfig, cmdLine, active);
                            options.logConfig(logConfig);
                            NArg finalA = a;
                            return r.isEmpty()
                                    ? NOptional.ofEmpty(() -> NMsg.ofC("unsupported option %s", finalA))
                                    : NOptional.of(Collections.singletonList(r.get()));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "-X":
                    case "--exclude-extension": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().get();
                        if (active && options != null) {
                            List<String> old = options.excludedExtensions().orNull();
                            if (old == null) {
                                old = new ArrayList<>();
                            }
                            old.add(v);
                            options.excludedExtensions(old);
                            options.excludedExtensions(new ArrayList<>(
                                    new LinkedHashSet<>(old)
                            ));
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }

                    case "--repository":
                    case "--repositories":
                    case "--repo":
                    case "--repos":
                    case "-r": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().get();
                        if (active) {
                            if (options != null) {
                                List<String> old = options.repositories().orNull();
                                if (old == null) {
                                    old = new ArrayList<>();
                                }
                                old.add(v);
                                options.repositories(old);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }

                    case "--boot-repository":
                    case "--boot-repositories":
                    case "--boot-repo":
                    case "--boot-repos":
                    {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().get();
                        if (active) {
                            if (options != null) {
                                List<String> old = options.bootRepositories().orNull();
                                if (old == null) {
                                    old = new ArrayList<>();
                                }
                                old.add(v);
                                options.bootRepositories(old);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }

                    case "--output-format-option":
                    case "-T": {
                        if (active) {
                            if (options != null) {
                                options.addOutputFormatOptions(cmdLine.nextEntry().get().getStringValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            cmdLine.skip();
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "-O":
                    case "--output-format":
                        a = cmdLine.nextEntry().get();
                        if (active) {
                            if (options != null) {
                                String t = a.getStringValue().orElse("");
                                int i = NStringUtils.indexOf(t, new char[]{' ', ';', ':', '='});
                                if (i > 0) {
                                    options.outputFormat(NContentType.valueOf(t.substring(0, i).toUpperCase()));
                                    options.addOutputFormatOptions(t.substring(i + 1).toUpperCase());
                                } else {
                                    options.outputFormat(NContentType.valueOf(t.toUpperCase()));
                                    options.addOutputFormatOptions("");
                                }
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    case "--tson":
                        a = cmdLine.next().get();
                        if (active) {
                            if (options != null) {
                                options.outputFormat(NContentType.TSON);
                                options.addOutputFormatOptions(a.getStringValue().orElse(""));
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    case "--yaml":
                        a = cmdLine.next().get();
                        if (active) {
                            if (options != null) {
                                options.outputFormat(NContentType.YAML);
                                options.addOutputFormatOptions(a.getStringValue().orElse(""));
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    case "--json":
                        a = cmdLine.next().get();
                        if (active) {
                            if (options != null) {
                                options.outputFormat(NContentType.JSON);
                                options.addOutputFormatOptions(a.getStringValue().orElse(""));
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    case "--plain":
                        a = cmdLine.next().get();
                        if (active) {
                            if (options != null) {
                                options.outputFormat(NContentType.PLAIN);
                                options.addOutputFormatOptions(a.getStringValue().orElse(""));
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    case "--xml":
                        a = cmdLine.next().get();
                        if (active) {
                            if (options != null) {
                                options.outputFormat(NContentType.XML);
                                options.addOutputFormatOptions(a.getStringValue().orElse(""));
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    case "--table":
                        a = cmdLine.next().get();
                        if (active) {
                            if (options != null) {
                                options.outputFormat(NContentType.TABLE);
                                options.addOutputFormatOptions(a.getStringValue().orElse(""));
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    case "--tree":
                        a = cmdLine.next().get();
                        if (active) {
                            if (options != null) {
                                options.outputFormat(NContentType.TREE);
                                options.addOutputFormatOptions(a.getStringValue().orElse(""));
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    case "--props":
                        a = cmdLine.next().get();
                        if (active) {
                            if (options != null) {
                                options.outputFormat(NContentType.PROPS);
                                options.addOutputFormatOptions(a.getStringValue().orElse(""));
                            }
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    case "--yes":
                    case "-y": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.confirm(NConfirmationMode.YES);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--no":
                    case "-n": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.confirm(NConfirmationMode.NO);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--error": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.confirm(NConfirmationMode.ERROR);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--ask": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.confirm(NConfirmationMode.ASK);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--cached": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.cached(a.getBooleanValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--indexed": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.indexed(a.getBooleanValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--transitive": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.transitive(a.getBooleanValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "-f":
                    case "--fetch": {
                        a = cmdLine.nextEntry().get();
                        if (active) {
                            if (options != null) {
                                options.fetchStrategy(a.getStringValue()
                                        .flatMap(NFetchStrategy::parse).get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "-a":
                    case "--anywhere": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.fetchStrategy(NFetchStrategy.ANYWHERE);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "-F":
                    case "--offline": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.fetchStrategy(NFetchStrategy.OFFLINE);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--online": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.fetchStrategy(NFetchStrategy.ONLINE);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--remote": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.fetchStrategy(NFetchStrategy.REMOTE);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
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
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.executionType(NExecutionType.EMBEDDED);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--open-file": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.executionType(NExecutionType.OPEN);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--external":
                    case "--spawn":
                    case "-x": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.executionType(NExecutionType.SPAWN);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--user-cmd"://deprecated since 0.8.1
                    case "--system": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.executionType(NExecutionType.SYSTEM);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--root-cmd": //deprecated since 0.8.1
                    case "--as-root": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.runAs(NRunAs.root());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--current-user": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.runAs(NRunAs.currentUser());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--run-as": {
                        a = cmdLine.nextEntry().get();
                        if (active) {
                            if (options != null) {
                                options.runAs(NRunAs.user(a.getStringValue().get()));
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--sudo": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.runAs(NRunAs.sudo());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "-o":
                    case "--open-mode": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().get();
                        if (active) {
                            if (options != null) {
                                options.openMode(parseNutsOpenMode(v));
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--open-or-error":
                    case "--open": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.openMode(NOpenMode.OPEN_OR_ERROR);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--create-or-error":
                    case "--create": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.openMode(NOpenMode.CREATE_OR_ERROR);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--open-or-create": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.openMode(NOpenMode.OPEN_OR_CREATE);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--open-or-null": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get()) {
                            if (options != null) {
                                options.openMode(NOpenMode.OPEN_OR_NULL);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }

                    //**********************************
                    //*
                    //* Commands
                    //*
                    //**********************************
                    case "-": {
                        if (active) {
                            List<String> newArgs = new ArrayList<>();
                            newArgs.addAll(Arrays.asList(cmdLine.toStringArray()));
                            cmdLine.skipAll();
                            if (options != null) {
                                if (!a.toLiteral().isNull()) {
                                    List<NMsg> showError = options.errors().orNull();
                                    if (showError == null) {
                                        showError = new ArrayList<>();
                                    }
                                    showError.add(NMsg.ofC("invalid argument for workspace: %s", a.image()));
                                    options.errors(showError);
                                }
                                List<String> applicationArguments = options.applicationArguments().orNull();
                                if (applicationArguments == null) {
                                    applicationArguments = new ArrayList<>();
                                }

                                applicationArguments.addAll(newArgs);
                                options.applicationArguments(applicationArguments);
                            }
                            return NOptional.of(newArgs.stream().map(NArg::of).collect(Collectors.toList()));
                        } else {
                            List<String> newArgs = new ArrayList<>(Arrays.asList(cmdLine.toStringArray()));
                            cmdLine.skipAll();
                            return NOptional.of(newArgs.stream().map(NArg::of).collect(Collectors.toList()));
                        }
                    }

                    case "-version":
                    case "-v":
                    case "--version": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.commandVersion(a.isUncommented());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "-Z":
                    case "--reset": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                if (a.getBooleanValue().get()) {
                                    options.reset(true);
                                    options.recover(false);
                                }
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            List<String> newArgs = new ArrayList<>();
                            newArgs.addAll(Arrays.asList(cmdLine.toStringArray()));
                            cmdLine.skipAll();
                            return NOptional.of(newArgs.stream().map(NArg::of).collect(Collectors.toList()));
                        }
                    }
                    case "--reset-hard": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                if (a.getBooleanValue().get()) {
                                    options.resetHard(true);
                                    options.recover(false);
                                }
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            List<String> newArgs = new ArrayList<>();
                            newArgs.addAll(Arrays.asList(cmdLine.toStringArray()));
                            cmdLine.skipAll();
                            return NOptional.of(newArgs.stream().map(NArg::of).collect(Collectors.toList()));
                        }
                    }
                    case "-z":
                    case "--recover": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                if (a.getBooleanValue().get()) {
                                    options.reset(false);
                                    options.recover(true);
                                }
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "-N":
                    case "--expire": {
                        a = cmdLine.next().get();
                        if (active) {
                            if (options != null) {
                                if (!NBlankable.isBlank(a.getStringValue())) {
                                    options.expireTime(a.toLiteral().asInstant().onEmpty(null).get());
                                } else {
                                    options.expireTime(Instant.now());
                                }
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--out-line-prefix": {
                        a = cmdLine.nextEntry().get();
                        if (active) {
                            if (options != null) {
                                options.outLinePrefix(a.getStringValue().get());
                            }
                        }
                    }
                    case "--err-line-prefix": {
                        a = cmdLine.nextEntry().get();
                        if (active) {
                            if (options != null) {
                                options.errLinePrefix(a.getStringValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--line-prefix": {
                        a = cmdLine.nextEntry().get();
                        if (active) {
                            if (options != null) {
                                options.outLinePrefix(a.getStringValue().get());
                                options.errLinePrefix(a.getStringValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "-e":
                    case "--exec": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            List<String> newArgs = new ArrayList<>();
                            newArgs.add(a.toString());
                            if (a.getBooleanValue().get()) {
                                while ((a = cmdLine.next().orNull()) != null) {
                                    if (a.isOption()) {
                                        if (options != null) {
                                            List<String> executorOptions = options.executorOptions().orNull();
                                            if (executorOptions == null) {
                                                executorOptions = new ArrayList<>();
                                            }
                                            executorOptions.add(a.asString().orElse(""));
                                            newArgs.add(a.asString().orElse(""));
                                            options.executorOptions(executorOptions);
                                        } else {
                                            newArgs.add(a.asString().orElse(""));
                                        }
                                    } else {
                                        if (options != null) {
                                            List<String> applicationArguments = options.applicationArguments().orNull();
                                            if (applicationArguments == null) {
                                                applicationArguments = new ArrayList<>();
                                            }
                                            applicationArguments.add(a.asString().orElse(""));
                                            List<String> list = Arrays.asList(cmdLine.toStringArray());
                                            applicationArguments.addAll(list);
                                            newArgs.addAll(list);
                                            cmdLine.skipAll();
                                            options.applicationArguments(applicationArguments);
                                        } else {
                                            newArgs.addAll(Arrays.asList(cmdLine.toStringArray()));
                                            cmdLine.skipAll();
                                        }
                                    }

                                }
                            }
                            return NOptional.of(newArgs.stream().map(NArg::of).collect(Collectors.toList()));
                        } else {
                            List<String> newArgs = new ArrayList<>();
                            newArgs.addAll(Arrays.asList(cmdLine.toStringArray()));
                            cmdLine.skipAll();
                            return NOptional.of(newArgs.stream().map(NArg::of).collect(Collectors.toList()));
                        }
                    }
                    case "-?":
                    case "--help":
                    case "-h": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.commandHelp(a.getBooleanValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--skip-errors": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.skipErrors(a.getBooleanValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "-L":
                    case "--locale": {
                        a = cmdLine.nextEntry().get();
                        if (active) {
                            if (options != null) {
                                options.locale(a.getStringValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--theme": {
                        a = cmdLine.nextEntry().get();
                        if (active) {
                            if (options != null) {
                                options.theme(a.getStringValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--sandbox": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.isolationLevel(a.getBooleanValue().get() ? NIsolationLevel.SANDBOX : null);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                        // singe 0.8.6
                    case "--reset-options": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.resetOptions();
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    //@since 0.8.5
                    case "--in-memory": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.isolationLevel(a.getBooleanValue().get() ? NIsolationLevel.MEMORY : null);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--confined": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.isolationLevel(a.getBooleanValue().get() ? NIsolationLevel.CONFINED : null);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--isolation-level": {
                        a = cmdLine.nextEntry().get();
                        if (active) {
                            if (options != null) {
                                options.isolationLevel(a.getStringValue().flatMap(NIsolationLevel::parse).get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--init-launchers": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.initLaunchers(a.getBooleanValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--init-java": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.initJava(a.getBooleanValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--init-platforms": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.initPlatforms(a.getBooleanValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--init-scripts": {
                        a = cmdLine.nextFlag().get();
                        if (active) {
                            if (options != null) {
                                options.initScripts(a.getBooleanValue().get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--desktop-launcher": {
                        a = cmdLine.nextEntry().get();
                        if (active) {
                            if (options != null) {
                                options.desktopLauncher(a.getStringValue().flatMap(NSupportMode::parse).get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--menu-launcher": {
                        a = cmdLine.nextEntry().get();
                        if (active) {
                            if (options != null) {
                                options.menuLauncher(a.getStringValue().flatMap(NSupportMode::parse).get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    case "--user-launcher": {
                        a = cmdLine.nextEntry().get();
                        if (active) {
                            if (options != null) {
                                options.userLauncher(a.getStringValue().flatMap(NSupportMode::parse).get());
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
                    }
                    //ERRORS
                    case "-C":
                    case "-I":
                    case "-G":
                    case "-H":
                    case "-M":
                    case "-W":
                    case "-i":
                    case "-q":
                    case "-s":
                    case "-m":
                    default: {
                        if (k.startsWith("---") && k.length() > 3 && k.charAt(3) != '-') {
                            a = cmdLine.next().get();
                            if (options != null) {
                                List<String> customOptions = options.customOptions().orNull();
                                if (customOptions == null) {
                                    customOptions = new ArrayList<>();
                                }
                                customOptions.add(a.toString());
                                options.customOptions(customOptions);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            if (options != null) {
                                List<NMsg> showError = options.errors().orNull();
                                if (showError == null) {
                                    showError = new ArrayList<>();
                                    options.errors(showError);
                                }
                                showError.add(NMsg.ofC("nuts: invalid option %s", a.asString().orNull()));
                            }
                            NArg finalA1 = a;
                            return NOptional.ofEmpty(() -> NMsg.ofC("unsupported option %s", finalA1));
                        }
                    }
                }
            } else {
                List<String> newArgs = new ArrayList<>();
                newArgs.addAll(Arrays.asList(cmdLine.toStringArray()));
                cmdLine.skipAll();
                if (options != null) {
                    List<String> applicationArguments = options.applicationArguments().orNull();
                    if (applicationArguments == null) {
                        applicationArguments = new ArrayList<>();
                    }
                    applicationArguments.addAll(newArgs);
                    options.applicationArguments(applicationArguments);
                }
                return NOptional.of(newArgs.stream().map(NArg::of).collect(Collectors.toList()));
            }
        }
        if (cmdLine.isEmpty()) {
            return NOptional.ofNamedEmpty("option");
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("unsupported %s", cmdLine.peek().get()));
    }

    /**
     * Fill a {@link NWorkspaceOptions} instance from string array of valid nuts
     * options
     *
     * @param bootArguments input arguments to parse
     * @param options       options instance to fill
     */
    public static void parseNutsArguments(String[] bootArguments, NWorkspaceOptionsBuilder options) {
        NCmdLine cmdLine = NCmdLine.of(bootArguments)
                .commandName("nuts")
                .expandSimpleOptions(true)
                .registerSpecialSimpleOption("-version");
        while (cmdLine.hasNext()) {
            if (nextNutsArgument(cmdLine, options).isNotPresent()) {
                //some error occurred!
                cmdLine.skip();
            }
        }
        if (options.errors().isNotPresent()) {
            options.errors(new ArrayList<>());
        }
        if (options.applicationArguments().isNotPresent()) {
            options.applicationArguments(new ArrayList<>());
        }
        if (options.excludedExtensions().isNotPresent()) {
            options.excludedExtensions(new ArrayList<>());
        }
        if (options.repositories().isNotPresent()) {
            options.repositories(new ArrayList<>());
        }
        if (options.executorOptions().isNotPresent()) {
            options.executorOptions(new ArrayList<>());
        }
        if (options.customOptions().isNotPresent()) {
            options.customOptions(new ArrayList<>());
        }
        //error only if not asking for help
        if (!(options.applicationArguments().get().size() > 0
                && (options.applicationArguments().get().get(0).equals("help")
                || options.commandHelp().orElse(false)
                || options.applicationArguments().get().get(0).equals("version")
                || options.commandVersion().orElse(false)))) {
            if (!options.errors().get().isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();
                for (NMsg s : options.errors().get()) {
                    errorMessage.append(s).append("\n");
                }
                errorMessage.append("Try 'nuts --help' for more information.");
                if (!options.skipErrors().orElse(false)) {
                    throw NExceptions.ofSafeCmdLineException(NMsg.ofPlain(errorMessage.toString()));
                }
            }
        }
    }

    private static NOptional<NArg> parseLogLevel(NLogConfig logConfig, NCmdLine cmdLine, boolean enabled) {
        NArg a = cmdLine.peek().get();
        switch (a.key()) {
            case "--log-file-size": {
                a = cmdLine.nextEntry().get();
                String v = a.getStringValue().get();
                if (enabled) {
                    Integer fileSize = NApiUtilsRPI.parseFileSizeInBytes(v, 1024 * 1024).orNull();
                    if (fileSize == null) {
                        if (NBlankable.isBlank(v)) {
                            throw NExceptions.ofSafeCmdLineException(NMsg.ofC("invalid file size : %s", v));
                        }
                    } else {
                        //always in mega
                        fileSize = fileSize / (1024 * 1024);
                        if (fileSize <= 0) {
                            throw NExceptions.ofSafeCmdLineException(NMsg.ofC("invalid file size : %s < 1Mb", v));
                        }
                    }
                    if (fileSize != null) {
                        logConfig.logFileSize(fileSize);
                    }
                    return NOptional.of(a);
                } else {
                    return NOptional.of(a);
                }
            }

            case "--log-file-count": {
                a = cmdLine.nextEntry().get();
                if (enabled) {
                    logConfig.logFileCount(a.toLiteral().asInt().get());
                    return NOptional.of(a);
                } else {
                    return NOptional.of(a);
                }
            }

            case "--log-file-name": {
                a = cmdLine.nextEntry().get();
                String v = a.getStringValue().get();
                if (enabled) {
                    logConfig.logFileName(v);
                    return NOptional.of(a);
                } else {
                    return NOptional.of(a);
                }
            }

            case "--log-file-base": {
                a = cmdLine.nextEntry().get();
                String v = a.getStringValue().get();
                if (enabled) {
                    logConfig.logFileBase(v);
                    return NOptional.of(a);
                } else {
                    return NOptional.of(a);
                }
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
                    logConfig.logFileLevel(
                            NLogUtils.parseLogLevel(id.substring("--log-file-".length())).orNull()
                    );
                }
                return NOptional.of(a);
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
                    logConfig.logTermLevel(
                            NLogUtils.parseLogLevel(id.substring("--log-term-".length())).orNull()
                    );
                }
                return NOptional.of(a);
            }

            case "-l":
            case "--verbose":
            {
                cmdLine.skip();
                if (enabled && a.getBooleanValue().orElse(true)) {
                    logConfig.logTermLevel(Level.FINEST);
                    logConfig.logFileLevel(Level.FINEST);
                }
                return NOptional.of(a);
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
                    logConfig.logTermLevel(lvl);
                    logConfig.logFileLevel(lvl);
                }
                return NOptional.of(a);
            }
        }
        return NOptional.ofNamedEmpty("log option");
    }

    private static NStoreStrategy parseNutsStoreStrategy(String s) {
        NStoreStrategy m = NStoreStrategy.parse(s).orNull();
        if (m == null && !NBlankable.isBlank(s)) {
            throw NExceptions.ofSafeCmdLineException(NMsg.ofC("unable to parse value for NutsStoreStrategy : %s,", s));
        }
        return m;
    }

    private static NOsFamily parseNutsOsFamily(String s) {
        NOsFamily m = NOsFamily.parse(s).orNull();
        if (m == null && !NBlankable.isBlank(s)) {
            throw NExceptions.ofSafeCmdLineException(NMsg.ofC("unable to parse value for NutsOsFamily : %s", s));
        }
        return m;
    }

    private static NOpenMode parseNutsOpenMode(String s) {
        NOpenMode m = NOpenMode.parse(s).orNull();
        if (m == null && !NBlankable.isBlank(s)) {
            throw NExceptions.ofSafeCmdLineException(NMsg.ofC("unable to parse value for NutsOpenMode : %s", s));
        }
        return m;
    }
}
