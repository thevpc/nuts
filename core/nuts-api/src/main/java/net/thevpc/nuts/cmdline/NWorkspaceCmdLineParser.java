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
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.log.NLogUtils;
import net.thevpc.nuts.reserved.NApiUtilsRPI;
import net.thevpc.nuts.reserved.NReservedUtils;
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
                        a = cmdLine.nextEntry().get();
                        if (active && options != null) {
                            String file = a.getStringValue().orElse("");
                            options.setWorkspace(file);
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
                            options.setCredentials(v.toCharArray());
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "-V":
                    case "--boot-version":
                    case "--boot-api-version": {
                        a = cmdLine.nextEntry().get();
                        if (active && options != null) {
                            String v = a.getStringValue().get();
                            options.setApiVersion(NVersion.of(v));
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--boot-runtime": {
                        a = cmdLine.nextEntry().get();
                        if (active && options != null) {
                            String br = a.getStringValue().orElse("");
                            if (br.indexOf('#') >= 0) {
                                //this is a full id
                                options.setRuntimeId(NId.get(br).get());
                            } else {
                                options.setRuntimeId(NId.getRuntime(br).orNull());
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
                            options.setJavaCommand(v);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--java-home":
                    case "--boot-java-home": {
                        a = cmdLine.nextEntry().get();
                        if (active && options != null) {
                            String v = a.getStringValue().get();
                            options.setJavaCommand(NReservedUtils.resolveJavaCommand(v));
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--java-options":
                    case "--boot-java-options":
                    case "-J": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().orElse("");
                        if (active && options != null) {
                            options.setJavaOptions(v);
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
                            options.setName(v);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--archetype":
                    case "-A": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().get();
                        if (active && options != null) {
                            options.setArchetype(v);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--store-strategy": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().orElse("");
                        if (active && options != null) {
                            options.setStoreStrategy(parseNutsStoreStrategy(v));
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "-S":
                    case "--standalone":
                    case "--standalone-workspace": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.setStoreStrategy(NStoreStrategy.STANDALONE);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "-E":
                    case "--exploded":
                    case "--exploded-workspace": {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null && a.getBooleanValue().get()) {
                            options.setStoreStrategy(NStoreStrategy.EXPLODED);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }

                    case "--repo-store-strategy": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().get();
                        if (active && options != null) {
                            options.setRepositoryStoreStrategy(parseNutsStoreStrategy(v));
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--exploded-repositories": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.setRepositoryStoreStrategy(NStoreStrategy.EXPLODED);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--standalone-repositories": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.setRepositoryStoreStrategy(NStoreStrategy.STANDALONE);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--store-layout": {
                        a = cmdLine.nextEntry().get();
                        String v = a.getStringValue().get();
                        if (active && options != null) {
                            options.setStoreLayout(parseNutsOsFamily(v));
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--system-layout": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.setStoreLayout(null);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--windows-layout": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.setStoreLayout(NOsFamily.WINDOWS);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--macos-layout": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.setStoreLayout(NOsFamily.MACOS);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--linux-layout": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.setStoreLayout(NOsFamily.LINUX);
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--unix-layout": {
                        a = cmdLine.nextFlag().get();
                        if (active && a.getBooleanValue().get() && options != null) {
                            options.setStoreLayout(NOsFamily.UNIX);
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
                            options.setInstallCompanions(a.getBooleanValue().get());
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--skip-welcome":
                    case "-K": {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null) {
                            options.setSkipWelcome(a.getBooleanValue().get());
                        }
                        return NOptional.of(Collections.singletonList(a));

                    }
                    case "--skip-boot":
                    case "-Q": {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null) {
                            options.setSkipBoot(a.getBooleanValue().get());
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--switch": {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null) {
                            options.setSwitchWorkspace(a.getBooleanValue().orElse(true));
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
                            options.setSystem(a.getBooleanValue().get());
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }
                    case "--shared-instance": {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null) {
                            options.setSharedInstance(a.getBooleanValue().get());
                            return NOptional.of(Collections.singletonList(a));
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }

                    case "--gui": {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null) {
                            options.setGui(a.getBooleanValue().get());
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
                                        options.setTerminalMode(NTerminalMode.INHERITED);
                                    } else {
                                        options.setTerminalMode(NTerminalMode.FORMATTED);
                                    }
                                } else {
                                    NTerminalMode v = a.getStringValue().flatMap(NTerminalMode::parse)
                                            .ifEmpty(NTerminalMode.FORMATTED).get();
                                    if (v == NTerminalMode.DEFAULT) {
                                        v = NTerminalMode.INHERITED;
                                    }
                                    options.setTerminalMode(v);
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
                                options.setBot(a.getBooleanValue().get());
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
                                options.setPreviewRepo(a.getBooleanValue().get());
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
                                options.setReadOnly(a.getBooleanValue().get());
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
                                options.setTrace(a.getBooleanValue().get());
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
                                    options.setProgressOptions(s);
                                } else {
                                    options.setProgressOptions(s);
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
                                options.setDependencySolver(s);
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
                            options.setDry(a.getBooleanValue().get());
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }

                    case "--stacktrace": {
                        a = cmdLine.nextFlag().get();
                        if (active && options != null) {
                            options.setShowStacktrace(a.getBooleanValue().get());
                        }
                        return NOptional.of(Collections.singletonList(a));
                    }

                    case "--debug": {
                        a = cmdLine.next().get();
                        if (active) {
                            if (options != null) {
                                if (NBlankable.isBlank(a.getStringValue())) {
                                    options.setDebug(String.valueOf(a.isEnabled()));
                                } else {
                                    if (a.isNegated()) {
                                        options.setDebug(
                                                String.valueOf(!NLiteral.of(a.getStringValue().get()).asBoolean()
                                                        .ifEmpty(true).ifError(false).get()));
                                    } else {
                                        options.setDebug(a.getStringValue().get());
                                    }
                                }
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            return NOptional.of(Collections.singletonList(a));
                        }
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
                            NLogConfig logConfig = options.getLogConfig().orNull();
                            if (logConfig == null) {
                                logConfig = new NLogConfig();
                            }
                            NOptional<NArg> r = parseLogLevel(logConfig, cmdLine, active);
                            options.setLogConfig(logConfig);
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
                            List<String> old = options.getExcludedExtensions().orNull();
                            if (old == null) {
                                old = new ArrayList<>();
                            }
                            old.add(v);
                            options.setExcludedExtensions(old);
                            options.setExcludedExtensions(new ArrayList<>(
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
                                List<String> old = options.getRepositories().orNull();
                                if (old == null) {
                                    old = new ArrayList<>();
                                }
                                old.add(v);
                                options.setRepositories(old);
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
                                    options.setOutputFormat(NContentType.valueOf(t.substring(0, i).toUpperCase()));
                                    options.addOutputFormatOptions(t.substring(i + 1).toUpperCase());
                                } else {
                                    options.setOutputFormat(NContentType.valueOf(t.toUpperCase()));
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
                                options.setOutputFormat(NContentType.TSON);
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
                                options.setOutputFormat(NContentType.YAML);
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
                                options.setOutputFormat(NContentType.JSON);
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
                                options.setOutputFormat(NContentType.PLAIN);
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
                                options.setOutputFormat(NContentType.XML);
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
                                options.setOutputFormat(NContentType.TABLE);
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
                                options.setOutputFormat(NContentType.TREE);
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
                                options.setOutputFormat(NContentType.PROPS);
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
                                options.setConfirm(NConfirmationMode.YES);
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
                                options.setConfirm(NConfirmationMode.NO);
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
                                options.setConfirm(NConfirmationMode.ERROR);
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
                                options.setConfirm(NConfirmationMode.ASK);
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
                                options.setCached(a.getBooleanValue().get());
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
                                options.setIndexed(a.getBooleanValue().get());
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
                                options.setTransitive(a.getBooleanValue().get());
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
                                options.setFetchStrategy(a.getStringValue()
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
                                options.setFetchStrategy(NFetchStrategy.ANYWHERE);
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
                                options.setFetchStrategy(NFetchStrategy.OFFLINE);
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
                                options.setFetchStrategy(NFetchStrategy.ONLINE);
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
                                options.setFetchStrategy(NFetchStrategy.REMOTE);
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
                                options.setExecutionType(NExecutionType.EMBEDDED);
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
                                options.setExecutionType(NExecutionType.OPEN);
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
                                options.setExecutionType(NExecutionType.SPAWN);
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
                                options.setExecutionType(NExecutionType.SYSTEM);
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
                                options.setRunAs(NRunAs.root());
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
                                options.setRunAs(NRunAs.currentUser());
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
                                options.setRunAs(NRunAs.user(a.getStringValue().get()));
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
                                options.setRunAs(NRunAs.sudo());
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
                                options.setOpenMode(parseNutsOpenMode(v));
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
                                options.setOpenMode(NOpenMode.OPEN_OR_ERROR);
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
                                options.setOpenMode(NOpenMode.CREATE_OR_ERROR);
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
                                options.setOpenMode(NOpenMode.OPEN_OR_CREATE);
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
                                options.setOpenMode(NOpenMode.OPEN_OR_NULL);
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
                                if (!a.getValue().isNull()) {
                                    List<NMsg> showError = options.getErrors().orNull();
                                    if (showError == null) {
                                        showError = new ArrayList<>();
                                    }
                                    showError.add(NMsg.ofC("invalid argument for workspace: %s", a.asString()));
                                    options.setErrors(showError);
                                }
                                List<String> applicationArguments = options.getApplicationArguments().orNull();
                                if (applicationArguments == null) {
                                    applicationArguments = new ArrayList<>();
                                }

                                applicationArguments.addAll(newArgs);
                                options.setApplicationArguments(applicationArguments);
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
                                options.setCommandVersion(a.isActive());
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
                                    options.setReset(true);
                                    options.setRecover(false);
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
                                    options.setResetHard(true);
                                    options.setRecover(false);
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
                                    options.setReset(false);
                                    options.setRecover(true);
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
                                    options.setExpireTime(a.getValue().asInstant().ifEmpty(null).get());
                                } else {
                                    options.setExpireTime(Instant.now());
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
                                options.setOutLinePrefix(a.getStringValue().get());
                            }
                        }
                    }
                    case "--err-line-prefix": {
                        a = cmdLine.nextEntry().get();
                        if (active) {
                            if (options != null) {
                                options.setErrLinePrefix(a.getStringValue().get());
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
                                options.setOutLinePrefix(a.getStringValue().get());
                                options.setErrLinePrefix(a.getStringValue().get());
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
                                            List<String> executorOptions = options.getExecutorOptions().orNull();
                                            if (executorOptions == null) {
                                                executorOptions = new ArrayList<>();
                                            }
                                            executorOptions.add(a.asString().orElse(""));
                                            newArgs.add(a.asString().orElse(""));
                                            options.setExecutorOptions(executorOptions);
                                        } else {
                                            newArgs.add(a.asString().orElse(""));
                                        }
                                    } else {
                                        if (options != null) {
                                            List<String> applicationArguments = options.getApplicationArguments().orNull();
                                            if (applicationArguments == null) {
                                                applicationArguments = new ArrayList<>();
                                            }
                                            applicationArguments.add(a.asString().orElse(""));
                                            List<String> list = Arrays.asList(cmdLine.toStringArray());
                                            applicationArguments.addAll(list);
                                            newArgs.addAll(list);
                                            cmdLine.skipAll();
                                            options.setApplicationArguments(applicationArguments);
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
                                options.setCommandHelp(a.getBooleanValue().get());
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
                                options.setSkipErrors(a.getBooleanValue().get());
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
                                options.setLocale(a.getStringValue().get());
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
                                options.setTheme(a.getStringValue().get());
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
                                options.setIsolationLevel(a.getBooleanValue().get() ? NIsolationLevel.SANDBOX : null);
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
                                options.setIsolationLevel(a.getBooleanValue().get() ? NIsolationLevel.MEMORY : null);
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
                                options.setIsolationLevel(a.getBooleanValue().get() ? NIsolationLevel.CONFINED : null);
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
                                options.setIsolationLevel(a.getStringValue().flatMap(NIsolationLevel::parse).get());
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
                                options.setInitLaunchers(a.getBooleanValue().get());
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
                                options.setInitJava(a.getBooleanValue().get());
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
                                options.setInitPlatforms(a.getBooleanValue().get());
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
                                options.setInitScripts(a.getBooleanValue().get());
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
                                options.setDesktopLauncher(a.getStringValue().flatMap(NSupportMode::parse).get());
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
                                options.setMenuLauncher(a.getStringValue().flatMap(NSupportMode::parse).get());
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
                                options.setUserLauncher(a.getStringValue().flatMap(NSupportMode::parse).get());
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
                    case "-d":
                    case "-l":
                    case "-m":
                    default: {
                        if (k.startsWith("---") && k.length() > 3 && k.charAt(3) != '-') {
                            a = cmdLine.next().get();
                            if (options != null) {
                                List<String> customOptions = options.getCustomOptions().orNull();
                                if (customOptions == null) {
                                    customOptions = new ArrayList<>();
                                }
                                customOptions.add(a.toString());
                                options.setCustomOptions(customOptions);
                            }
                            return NOptional.of(Collections.singletonList(a));
                        } else {
                            if (options != null) {
                                List<NMsg> showError = options.getErrors().orNull();
                                if (showError == null) {
                                    showError = new ArrayList<>();
                                    options.setErrors(showError);
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
                    List<String> applicationArguments = options.getApplicationArguments().orNull();
                    if (applicationArguments == null) {
                        applicationArguments = new ArrayList<>();
                    }
                    applicationArguments.addAll(newArgs);
                    options.setApplicationArguments(applicationArguments);
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
                .setCommandName("nuts")
                .setExpandSimpleOptions(true)
                .registerSpecialSimpleOption("-version");
        while (cmdLine.hasNext()) {
            if (nextNutsArgument(cmdLine,  options).isNotPresent()) {
                //some error occurred!
                cmdLine.skip();
            }
        }
        if (options.getErrors().isNotPresent()) {
            options.setErrors(new ArrayList<>());
        }
        if (options.getApplicationArguments().isNotPresent()) {
            options.setApplicationArguments(new ArrayList<>());
        }
        if (options.getExcludedExtensions().isNotPresent()) {
            options.setExcludedExtensions(new ArrayList<>());
        }
        if (options.getRepositories().isNotPresent()) {
            options.setRepositories(new ArrayList<>());
        }
        if (options.getExecutorOptions().isNotPresent()) {
            options.setExecutorOptions(new ArrayList<>());
        }
        if (options.getCustomOptions().isNotPresent()) {
            options.setCustomOptions(new ArrayList<>());
        }
        //error only if not asking for help
        if (!(options.getApplicationArguments().get().size() > 0
                && (options.getApplicationArguments().get().get(0).equals("help")
                || options.getCommandHelp().orElse(false)
                || options.getApplicationArguments().get().get(0).equals("version")
                || options.getCommandVersion().orElse(false)))) {
            if (!options.getErrors().get().isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();
                for (NMsg s : options.getErrors().get()) {
                    errorMessage.append(s).append("\n");
                }
                errorMessage.append("Try 'nuts --help' for more information.");
                if (!options.getSkipErrors().orElse(false)) {
                    throw new NIllegalArgumentException(NMsg.ofPlain(errorMessage.toString()));
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
                            throw new NIllegalArgumentException(NMsg.ofC("invalid file size : %s", v));
                        }
                    } else {
                        //always in mega
                        fileSize = fileSize / (1024 * 1024);
                        if (fileSize <= 0) {
                            throw new NIllegalArgumentException(NMsg.ofC("invalid file size : %s < 1Mb", v));
                        }
                    }
                    if (fileSize != null) {
                        logConfig.setLogFileSize(fileSize);
                    }
                    return NOptional.of(a);
                } else {
                    return NOptional.of(a);
                }
            }

            case "--log-file-count": {
                a = cmdLine.nextEntry().get();
                if (enabled) {
                    logConfig.setLogFileCount(a.getValue().asInt().get());
                    return NOptional.of(a);
                } else {
                    return NOptional.of(a);
                }
            }

            case "--log-file-name": {
                a = cmdLine.nextEntry().get();
                String v = a.getStringValue().get();
                if (enabled) {
                    logConfig.setLogFileName(v);
                    return NOptional.of(a);
                } else {
                    return NOptional.of(a);
                }
            }

            case "--log-file-base": {
                a = cmdLine.nextEntry().get();
                String v = a.getStringValue().get();
                if (enabled) {
                    logConfig.setLogFileBase(v);
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
                    logConfig.setLogFileLevel(
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
                    logConfig.setLogTermLevel(
                            NLogUtils.parseLogLevel(id.substring("--log-term-".length())).orNull()
                    );
                }
                return NOptional.of(a);
            }

            case "--verbose": {
                cmdLine.skip();
                if (enabled && a.getBooleanValue().orElse(true)) {
                    logConfig.setLogTermLevel(Level.FINEST);
                    logConfig.setLogFileLevel(Level.FINEST);
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
                    logConfig.setLogTermLevel(lvl);
                    logConfig.setLogFileLevel(lvl);
                }
                return NOptional.of(a);
            }
        }
        return NOptional.ofNamedEmpty("log option");
    }

    private static NStoreStrategy parseNutsStoreStrategy(String s) {
        NStoreStrategy m = NStoreStrategy.parse(s).orNull();
        if (m == null && !NBlankable.isBlank(s)) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to parse value for NutsStoreStrategy : %s", s));
        }
        return m;
    }

    private static NOsFamily parseNutsOsFamily(String s) {
        NOsFamily m = NOsFamily.parse(s).orNull();
        if (m == null && !NBlankable.isBlank(s)) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to parse value for NutsOsFamily : %s", s));
        }
        return m;
    }

    private static NOpenMode parseNutsOpenMode(String s) {
        NOpenMode m = NOpenMode.parse(s).orNull();
        if (m == null && !NBlankable.isBlank(s)) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to parse value for NutsOpenMode : %s", s));
        }
        return m;
    }
}
