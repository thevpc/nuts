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
package net.thevpc.nuts.boot.internal.cmdline;

import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.boot.internal.util.NBootMsg;
import net.thevpc.nuts.boot.internal.util.NBootUtils;

import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


public final class NBootWorkspaceCmdLineParser {
    private static String[] SUPPORTED_OPTIONS = NBootOptionRegistry.allNames().toArray(new String[0]);
    private static final List<String> NEGATION_MARKERS = Arrays.asList("!", "~");

    /**
     * private constructor
     */
    private NBootWorkspaceCmdLineParser() {
    }

    public static NBootCompleteRequestOrResult complete(NBootCompleteCmdlineRequest request) {
        int argIndex = request.request().argIndex();
        int argOffset = request.request().argOffset();

        if (argIndex < 0 || argIndex > request.args().size()) {
            return new NBootCompleteResult(Collections.emptyList(),
                    Collections.singletonList(NBootCompleteResult.Flag.ERROR));
        }

        // Walk the already-typed words to find out whether argIndex is a fresh
        // key/flag slot, or the dedicated value slot of a preceding VALUE option.
        // Uses the exact same arity rules the real parser uses (nextEntry vs nextFlag vs next),
        // so this can never silently diverge from actual parsing behavior.
        NBootOptionSpec pendingValueSpec = null;
        int i = 0;
        while (i < argIndex) {
            String w = request.args().get(i);
            if(!w.startsWith("-")){
                return new NBootCompleteCmdlineRequest(
                        new NBootCompleteRequest(request.request().argIndex()-i,request.request().argOffset()),new ArrayList<>(request.args().subList(i,request.args().size()))
                );
            }
            NBootOptionSpec spec = specOf(w);
            if (spec != null && spec.arity() == NBootOptionSpec.Arity.TERMINAL) {
                // Everything from here belongs to the delegated app — out of scope for nuts' own completion.
                return new NBootCompleteResult(Collections.emptyList(), Collections.emptyList());
            }
            if (spec != null && spec.arity() == NBootOptionSpec.Arity.VALUE && !hasInlineValue(w)) {
                if (i + 1 == argIndex) {
                    pendingValueSpec = spec;
                }
                i += 2; // the flag token + its separate value token
            } else {
                i += 1;
            }
        }

        String word = argIndex < request.args().size() ? request.args().get(argIndex) : "";
        int safeOffset = Math.max(0, Math.min(argOffset, word.length()));
        String cur = word.substring(0, safeOffset);

        if (pendingValueSpec != null) {
            return valueCompletion(pendingValueSpec, cur);
        }

        int eq = cur.indexOf('=');
        if (eq >= 0) {
            String key = cur.substring(0, eq);
            String valuePrefix = cur.substring(eq + 1);
            NBootOptionSpec spec = NBootOptionRegistry.byName(key);
            if (spec != null && (spec.arity() == NBootOptionSpec.Arity.VALUE
                    || spec.arity() == NBootOptionSpec.Arity.OPTIONAL_VALUE)) {
                NBootCompleteResult inner = valueCompletion(spec, valuePrefix);
                List<NBootCompleteResult.Candidate> composed = inner.candidates().stream()
                        .map(c -> new NBootCompleteResult.Candidate(key + "=" + c.value(), c.description()))
                        .collect(Collectors.toList());
                return new NBootCompleteResult(composed, inner.flags());
            }
            return new NBootCompleteResult(Collections.emptyList(), Collections.emptyList());
        }

// fresh key/flag position — also handle "--!key" / "--~key" negation prefixes.
// Negation only ever applies to FLAG-arity options, so it's detected and
// stripped here before matching, then reapplied to the matched names.
        String marker = null;
        String matchAgainst = cur;
        for (String m : NEGATION_MARKERS) {
            if (cur.startsWith("--" + m)) {
                marker = m;
                matchAgainst = "--" + cur.substring(2 + m.length());
                break;
            }
        }
        final String finalMarker = marker;
        String finalMatchAgainst = matchAgainst;
        List<NBootCompleteResult.Candidate> candidates = NBootOptionRegistry.allNames().stream()
                .filter(n -> {
                    NBootOptionSpec spec = NBootOptionRegistry.byName(n);
                    return finalMarker == null || (spec != null && spec.arity() == NBootOptionSpec.Arity.FLAG);
                })
                .filter(n -> n.startsWith(finalMatchAgainst))
                .sorted()
                .map(n -> finalMarker == null ? n : ("--" + finalMarker + n.substring(2)))
                .map(NBootCompleteResult.Candidate::new)
                .collect(Collectors.toList());
        return new NBootCompleteResult(candidates, Collections.emptyList());
    }

    private static NBootOptionSpec specOf(String word) {
        int eq = word.indexOf('=');
        return NBootOptionRegistry.byName(eq >= 0 ? word.substring(0, eq) : word);
    }

    private static boolean hasInlineValue(String word) {
        return word.indexOf('=') >= 0;
    }

    private static NBootCompleteResult valueCompletion(NBootOptionSpec spec, String prefix) {
        if (!spec.hasValueCompletion()) {
            return new NBootCompleteResult(Collections.emptyList(), spec.resultFlags()); // e.g. FILENAMES fallback
        }
        return new NBootCompleteResult(spec.completeValue(prefix), spec.resultFlags());
    }

    public static List<NBootArg> nextNutsArgument(NBootCmdLine cmdLine, NBootOptionsInfo options) {
        while (cmdLine.hasNext()) {
            NBootArg a = cmdLine.peek();
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
                        a = cmdLine.nextEntry();
                        if (active && options != null) {
                            String file = NBootUtils.firstNonNull(a.stringValue(), "");
                            options.workspace(file);
                            if ("MEMORY".equalsIgnoreCase(options.isolationLevel())
                                    || "SANDBOX".equalsIgnoreCase(options.isolationLevel())
                            ) {
                                options.isolationLevel(null);
                            }
                        }
                        return Collections.singletonList(a);
                    }
                    case "--user":
                    case "-u": {
                        a = cmdLine.nextEntry();
                        if (active && options != null) {
                            String v = NBootUtils.firstNonNull(a.stringValue(), "");
                            options.userName(v);
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--password":
                    case "-p": {
                        a = cmdLine.nextEntry();
                        if (active && options != null) {
                            String v = NBootUtils.firstNonNull(a.stringValue(), "");
                            options.credential(v.toCharArray());
                        }
                        return (Collections.singletonList(a));
                    }
                    case "-V":
                    case "--boot-version":
                    case "--boot-api-version": {
                        a = cmdLine.nextEntry();
                        if (active && options != null) {
                            String v = a.stringValue();
                            options.apiVersion(v);
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--boot-runtime": {
                        a = cmdLine.nextEntry();
                        if (active && options != null) {
                            String br = NBootUtils.firstNonNull(a.stringValue(), "");
                            if (br.indexOf('#') >= 0) {
                                //this is a full id
                                options.runtimeId(br);
                            } else {
                                NBootDependency r = NBootDependency.ofRuntime(br);
                                options.runtimeId(r == null ? null : r.toString());
                            }
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--java":
                    case "--boot-java":
                    case "-j": {
                        a = cmdLine.nextEntry();
                        if (active && options != null) {
                            String v = NBootUtils.firstNonNull(a.stringValue(), "");
                            options.javaCommand(v);
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--java-home":
                    case "--boot-java-home": {
                        a = cmdLine.nextEntry();
                        if (active && options != null) {
                            String v = a.stringValue();
                            options.javaCommand(NBootUtils.resolveJavaCommand(v));
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--java-options":
                    case "--boot-java-options":
                    case "-J": {
                        a = cmdLine.nextEntry();
                        String v = NBootUtils.firstNonNull(a.stringValue(), "");
                        if (active && options != null) {
                            options.javaOptions(v);
                        }
                        return (Collections.singletonList(a));
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
                        a = cmdLine.nextEntry();
                        String v = a.stringValue();
                        if (active && options != null) {
                            options.name(v);
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--archetype":
                    case "-A": {
                        a = cmdLine.nextEntry();
                        String v = a.stringValue();
                        if (active && options != null) {
                            options.archetype(v);
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--store-strategy": {
                        a = cmdLine.nextEntry();
                        String v = NBootUtils.firstNonNull(a.stringValue(), "");
                        if (active && options != null) {
                            options.storeStrategy(parseStoreStrategy(v));
                        }
                        return (Collections.singletonList(a));
                    }
                    case "-S":
                    case "--standalone":
                    case "--standalone-workspace": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue() && options != null) {
                            options.storeStrategy("STANDALONE");
                        }
                        return (Collections.singletonList(a));
                    }
                    case "-E":
                    case "--exploded":
                    case "--exploded-workspace": {
                        a = cmdLine.nextFlag();
                        if (active && options != null && a.booleanValue()) {
                            options.storeStrategy("EXPLODED");
                        }
                        return (Collections.singletonList(a));
                    }

                    case "--repo-store-strategy": {
                        a = cmdLine.nextEntry();
                        String v = a.stringValue();
                        if (active && options != null) {
                            options.repositoryStoreStrategy(parseStoreStrategy(v));
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--exploded-repositories": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue() && options != null) {
                            options.repositoryStoreStrategy("EXPLODED");
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--standalone-repositories": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue() && options != null) {
                            options.repositoryStoreStrategy("STANDALONE");
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--store-layout": {
                        a = cmdLine.nextEntry();
                        String v = a.stringValue();
                        if (active && options != null) {
                            options.storeLayout(parseOsFamily(v));
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--system-layout": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue() && options != null) {
                            options.storeLayout(null);
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--windows-layout": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue() && options != null) {
                            options.storeLayout("WINDOWS");
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--macos-layout": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue() && options != null) {
                            options.storeLayout("MACOS");
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--linux-layout": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue() && options != null) {
                            options.storeLayout("LINUX");
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--unix-layout": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue() && options != null) {
                            options.storeLayout("UNIX");
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--bin-location":
                    case "--config-location":
                    case "--var-location":
                    case "--log-location":
                    case "--temp-location":
                    case "--cache-location":
                    case "--lib-location": {
                        a = cmdLine.nextEntry();
                        String v = a.stringValue();
                        if (active && options != null) {
                            String m = parseStoreType(k.substring(2, k.indexOf('-', 2)).toUpperCase());
                            options.setStoreLocation(m, v);
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--system-bin-home":
                    case "--system-conf-home":
                    case "--system-var-home":
                    case "--system-log-home":
                    case "--system-temp-home":
                    case "--system-cache-home":
                    case "--system-lib-home":
                    case "--system-run-home": {
                        a = cmdLine.nextEntry();
                        String v = a.stringValue();
                        String folder = parseStoreType(
                                k.substring(3 + "system".length(), k.indexOf('-', 3 + "system".length())).toUpperCase());
                        if (active && options != null) {
                            options.setHomeLocation(NBootHomeLocation.of(null, folder), v);
                        }
                        return (Collections.singletonList(a));
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
                        a = cmdLine.nextEntry();
                        String v = a.stringValue();
                        String layout = parseOsFamily(k.substring(2, k.indexOf('-', 2)).toUpperCase());
                        String folder = parseStoreType(k.substring(3 + layout.length(), k.indexOf('-', 3 + layout.length())).toUpperCase());
                        if (active && options != null) {
                            options.setHomeLocation(NBootHomeLocation.of(layout, folder), v);
                        }
                        return (Collections.singletonList(a));

                    }
                    case "--install-companions":
                    case "-k": {
                        a = cmdLine.nextFlag();
                        if (active && options != null) {
                            options.installCompanions(a.booleanValue());
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--skip-welcome":
                    case "-K": {
                        a = cmdLine.nextFlag();
                        if (active && options != null) {
                            options.skipWelcome(a.booleanValue());
                        }
                        return (Collections.singletonList(a));

                    }
                    case "--skip-boot":
                    case "-Q": {
                        a = cmdLine.nextFlag();
                        if (active && options != null) {
                            options.skipBoot(a.booleanValue());
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--switch": {
                        a = cmdLine.nextFlag();
                        if (active && options != null) {
                            options.switchWorkspace(NBootUtils.firstNonNull(a.booleanValue(), true));
                        }
                        return (Collections.singletonList(a));
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
                        a = cmdLine.nextFlag();
                        if (active && options != null) {
                            options.system(a.booleanValue());
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--shared-instance": {
                        a = cmdLine.nextFlag();
                        if (active && options != null) {
                            options.sharedInstance(a.booleanValue());
                            return (Collections.singletonList(a));
                        }
                        return (Collections.singletonList(a));
                    }

                    case "--gui": {
                        a = cmdLine.nextFlag();
                        if (active && options != null) {
                            options.gui(a.booleanValue());
                        }
                        return (Collections.singletonList(a));
                    }

                    case "--color":
                    case "-c": {
                        //if the value is not immediately attached with '=' don't consider
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                if (a.isFlagOption()) {
                                    if (a.isNegated()) {
                                        options.terminalMode("INHERITED");
                                    } else {
                                        options.terminalMode("FORMATTED");
                                    }
                                } else {
                                    String v = NBootUtils.firstNonNull(parseTerminalMode(a.stringValue()), "FORMATTED");
                                    if (NBootUtils.sameEnum(v, "DEFAULT")) {
                                        v = "INHERITED";
                                    }
                                    options.terminalMode(v);
                                }
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-B":
                    case "--bot": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.bot(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-U":
                    case "--preview-repo": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.previewRepo(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-R":
                    case "--read-only": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.readOnly(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-t":
                    case "--trace": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.trace(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-P":
                    case "--progress": {
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                String s = a.stringValue();
                                if (a.isNegated()) {
                                    if (NBootUtils.isBlank(s)) {
                                        s = "false";
                                    } else {
                                        s = "false," + s;
                                    }
                                    options.progressOptions(s);
                                } else {
                                    options.progressOptions(s);
                                }
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--solver": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                String s = a.stringValue();
                                options.dependencySolver(s);
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--dry":
                    case "-D": {
                        a = cmdLine.nextFlag();
                        if (active && options != null) {
                            options.dry(a.booleanValue());
                        }
                        return (Collections.singletonList(a));
                    }

                    case "-d":
                    case "--stacktrace":
                    {
                        a = cmdLine.nextFlag();
                        if (active && options != null) {
                            options.showStacktrace(a.booleanValue());
                        }
                        return (Collections.singletonList(a));
                    }

                    case "--debug": {
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                if (NBootUtils.isBlank(a.stringValue())) {
                                    options.debug(String.valueOf(a.isEnabled()));
                                } else {
                                    if (a.isNegated()) {
                                        options.debug(String.valueOf(!NBootUtils.parseBoolean(a.stringValue(), true, false)));
                                    } else {
                                        options.debug(a.stringValue());
                                    }
                                }
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
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
                            NBootLogConfig logConfig = options.logConfig();
                            if (logConfig == null) {
                                logConfig = new NBootLogConfig();
                            }
                            NBootArg r = parseLogLevel(logConfig, cmdLine, active);
                            options.logConfig(logConfig);
                            return r == null
                                    ? null
                                    : Collections.singletonList(r);
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-X":
                    case "--exclude-extension": {
                        a = cmdLine.nextEntry();
                        String v = a.stringValue();
                        if (active && options != null) {
                            List<String> old = options.excludedExtensions();
                            if (old == null) {
                                old = new ArrayList<>();
                            }
                            old.add(v);
                            options.excludedExtensions(old);
                            options.excludedExtensions(new ArrayList<>(
                                    new LinkedHashSet<>(old)
                            ));
                        }
                        return (Collections.singletonList(a));
                    }

                    case "--repository":
                    case "--repositories":
                    case "--repo":
                    case "--repos":
                    case "-r": {
                        a = cmdLine.nextEntry();
                        String v = a.stringValue();
                        if (active) {
                            if (options != null) {
                                List<String> old = options.repositories();
                                if (old == null) {
                                    old = new ArrayList<>();
                                }
                                old.add(v);
                                options.repositories(old);
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }

                    case "--boot-repository":
                    case "--boot-repositories":
                    case "--boot-repo":
                    case "--boot-repos": {
                        a = cmdLine.nextEntry();
                        String v = a.stringValue();
                        if (active) {
                            if (options != null) {
                                List<String> old = options.bootRepositories();
                                if (old == null) {
                                    old = new ArrayList<>();
                                }
                                old.add(v);
                                options.bootRepositories(old);
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }

                    case "--output-format-option":
                    case "-T": {
                        if (active) {
                            if (options != null) {
                                options.addOutputFormatOptions(cmdLine.nextEntry().stringValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            cmdLine.skip();
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-O":
                    case "--output-format":
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                String t = NBootUtils.firstNonNull(a.stringValue(), "");
                                int i = NBootUtils.firstIndexOf(t, new char[]{' ', ';', ':', '='});
                                if (i > 0) {
                                    options.outputFormat((t.substring(0, i).toUpperCase()));
                                    options.addOutputFormatOptions(t.substring(i + 1).toUpperCase());
                                } else {
                                    options.outputFormat((t.toUpperCase()));
                                    options.addOutputFormatOptions("");
                                }
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--tson":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("TSON");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--yaml":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("YAML");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--json":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("JSON");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--plain":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("PLAIN");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--xml":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("XML");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--table":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("TABLE");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--tree":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("TREE");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--props":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("PROPS");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--yes":
                    case "-y": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.confirm("YES");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--no":
                    case "-n": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.confirm("NO");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--error": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.confirm("ERROR");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--ask": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.confirm("ASK");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--cached": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.cached(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--indexed": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.indexed(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--transitive": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.transitive(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-f":
                    case "--fetch": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.setchStrategy(parseFetchStrategy(a.stringValue()));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-a":
                    case "--anywhere": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.setchStrategy("ANYWHERE");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-F":
                    case "--offline": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.setchStrategy("OFFLINE");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--online": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.setchStrategy("ONLINE");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--remote": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.setchStrategy("REMOTE");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
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
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.executionType("EMBEDDED");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--open-file": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.executionType("OPEN");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--external":
                    case "--spawn":
                    case "-x": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.executionType("SPAWN");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--user-cmd"://deprecated since 0.8.1
                    case "--system": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.executionType("SYSTEM");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--root-cmd": //deprecated since 0.8.1
                    case "--as-root": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.runAs("ROOT");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--current-user": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.runAs("CURRENT_USER");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--run-as": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.runAs("USER:" + a.stringValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--sudo": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.runAs("SUDO");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-o":
                    case "--open-mode": {
                        a = cmdLine.nextEntry();
                        String v = a.stringValue();
                        if (active) {
                            if (options != null) {
                                options.openMode(parseNutsOpenMode(v));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--open-or-error":
                    case "--open": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.openMode("OPEN_OR_ERROR");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--create-or-error":
                    case "--create": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.openMode("CREATE_OR_ERROR");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--open-or-create": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.openMode("OPEN_OR_CREATE");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--open-or-null": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.openMode("OPEN_OR_NULL");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
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
                                if (a.value() != null) {
                                    addError(NBootMsg.ofC("invalid argument for workspace: %s", a.image()), options);
                                }
                                List<String> applicationArguments = NBootUtils.nonNullStrList(options.applicationArguments());
                                applicationArguments.addAll(newArgs);
                                options.applicationArguments(applicationArguments);
                            }
                            return (newArgs.stream().map(NBootArg::of).collect(Collectors.toList()));
                        } else {
                            List<String> newArgs = new ArrayList<>(Arrays.asList(cmdLine.toStringArray()));
                            cmdLine.skipAll();
                            return (newArgs.stream().map(NBootArg::of).collect(Collectors.toList()));
                        }
                    }

                    case "-version":
                    case "-v":
                    case "--version": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.commandVersion(a.isActive());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-Z":
                    case "--reset": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                if (a.booleanValue()) {
                                    options.reset(true);
                                    options.recover(false);
                                }
                            }
                            return (Collections.singletonList(a));
                        } else {
                            List<String> newArgs = new ArrayList<>();
                            newArgs.addAll(Arrays.asList(cmdLine.toStringArray()));
                            cmdLine.skipAll();
                            return (newArgs.stream().map(NBootArg::of).collect(Collectors.toList()));
                        }
                    }
                    case "--reset-hard": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                if (a.booleanValue()) {
                                    options.resetHard(true);
                                    options.recover(false);
                                }
                            }
                            return (Collections.singletonList(a));
                        } else {
                            //TODO : why consume all the rest??
                            List<String> newArgs = new ArrayList<>();
                            newArgs.addAll(Arrays.asList(cmdLine.toStringArray()));
                            cmdLine.skipAll();
                            return (newArgs.stream().map(NBootArg::of).collect(Collectors.toList()));
                        }
                    }
                    case "-z":
                    case "--recover": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                if (a.booleanValue()) {
                                    options.reset(false);
                                    options.recover(true);
                                }
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-N":
                    case "--expire": {
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                if (!NBootUtils.isBlank(a.stringValue())) {
                                    options.expireTime(NBootUtils.parseInstant(a.value()));
                                } else {
                                    options.expireTime(Instant.now());
                                }
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--out-line-prefix": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.outLinePrefix(a.stringValue());
                            }
                        }
                    }
                    case "--err-line-prefix": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.errLinePrefix(a.stringValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--line-prefix": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.outLinePrefix(a.stringValue());
                                options.errLinePrefix(a.stringValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-e":
                    case "--exec": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            List<String> newArgs = new ArrayList<>();
                            newArgs.add(a.toString());
                            if (a.booleanValue()) {
                                while ((a = cmdLine.next()) != null) {
                                    if (a.isOption()) {
                                        if (options != null) {
                                            List<String> executorOptions = options.executorOptions();
                                            if (executorOptions == null) {
                                                executorOptions = new ArrayList<>();
                                            }
                                            executorOptions.add(NBootUtils.firstNonNull(a.image(), ""));
                                            newArgs.add(NBootUtils.firstNonNull(a.image(), ""));
                                            options.executorOptions(executorOptions);
                                        } else {
                                            newArgs.add(NBootUtils.firstNonNull(a.image(), ""));
                                        }
                                    } else {
                                        if (options != null) {
                                            List<String> applicationArguments = NBootUtils.nonNullStrList(options.applicationArguments());
                                            applicationArguments.add(NBootUtils.firstNonNull(a.toString(), ""));
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
                            return (newArgs.stream().map(NBootArg::of).collect(Collectors.toList()));
                        } else {
                            List<String> newArgs = new ArrayList<>();
                            newArgs.addAll(Arrays.asList(cmdLine.toStringArray()));
                            cmdLine.skipAll();
                            return (newArgs.stream().map(NBootArg::of).collect(Collectors.toList()));
                        }
                    }
                    case "-?":
                    case "--help":
                    case "-h": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.commandHelp(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--skip-errors": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.skipErrors(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-L":
                    case "--locale": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.locale(a.stringValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--theme": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.theme(a.stringValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--sandbox": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.isolationLevel(a.booleanValue() ? "SANDBOX" : null);
                                if (a.booleanValue()) {
                                    options.workspace(null);
                                }
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--reset-options": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.resetOptions();
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    //@since 0.8.5
                    case "--in-memory": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.isolationLevel(a.booleanValue() ? "MEMORY" : null);
                                if (a.booleanValue()) {
                                    options.workspace(null);
                                }
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--confined": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.isolationLevel(a.booleanValue() ? "CONFINED" : null);
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--isolation-level": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.isolationLevel(parseIsolationLevel(a.stringValue()));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--init-launchers": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.initLaunchers(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--init-java": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.initJava(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--init-platforms": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.initPlatforms(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--init-scripts": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.initScripts(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--desktop-launcher": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.desktopLauncher(parseSupportMode(a.stringValue()));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--menu-launcher": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.menuLauncher(parseSupportMode(a.stringValue()));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--user-launcher": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.userLauncher(parseSupportMode(a.stringValue()));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
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
                            a = cmdLine.next();
                            if (options != null) {
                                List<String> customOptions = options.customOptions();
                                if (customOptions == null) {
                                    customOptions = new ArrayList<>();
                                }
                                customOptions.add(a.toString());
                                options.customOptions(customOptions);
                            }
                            return (Collections.singletonList(a));
                        } else {
                            a = cmdLine.next();
                            String r = NBootUtils.damerauLevenshteinClosest(0.5, a.image(), SUPPORTED_OPTIONS);
                            NBootMsg errorMsg = null;
                            if (r != null) {
                                errorMsg = (NBootMsg.ofC("nuts: invalid option %s. do you mean %s?", a.image(), r));
                            } else {
                                errorMsg = (NBootMsg.ofC("nuts: invalid option %s", a.image()));
                            }
                            if (options != null) {
                                addError(errorMsg, options);
                            }
                            if (r != null) {
                                throw new NBootException(errorMsg);
                            }
                        }
                    }
                }
            } else {
                List<String> newArgs = new ArrayList<>();
                newArgs.addAll(Arrays.asList(cmdLine.toStringArray()));
                cmdLine.skipAll();
                if (options != null) {
                    List<String> applicationArguments = NBootUtils.nonNullStrList(options.applicationArguments());
                    applicationArguments.addAll(newArgs);
                    options.applicationArguments(applicationArguments);
                }
                return (newArgs.stream().map(NBootArg::of).collect(Collectors.toList()));
            }
        }
        if (cmdLine.isEmpty()) {
            return null;
        }
        throw new NBootException(NBootMsg.ofC("unsupported %s", cmdLine.peek()));
    }


    public static void denullProperties(NBootOptionsInfo options) {
        if (options.errors() == null) {
            options.errors(new ArrayList<>());
        }
        if (options.applicationArguments() == null) {
            options.applicationArguments(new ArrayList<>());
        }
        if (options.excludedExtensions() == null) {
            options.excludedExtensions(new ArrayList<>());
        }
        if (options.repositories() == null) {
            options.repositories(new ArrayList<>());
        }
        if (options.executorOptions() == null) {
            options.executorOptions(new ArrayList<>());
        }
        if (options.customOptions() == null) {
            options.customOptions(new ArrayList<>());
        }
    }
    public static void parseNutsArguments(String[] bootArguments, NBootOptionsInfo options) {
        NBootCmdLine cmdLine = new NBootCmdLine(bootArguments)
                .setCommandName("nuts")
                .setExpandSimpleOptions(true)
                .registerSpecialSimpleOption("-version");
        while (cmdLine.hasNext()) {
            if (nextNutsArgument(cmdLine, options) == null) {
                //some error occurred!
                cmdLine.skip();
            }
        }
        denullProperties(options);
        //error only if not asking for help
        if (!(!options.applicationArguments().isEmpty()
                && (options.applicationArguments().get(0).equals("help")
                || NBootUtils.firstNonNull(options.commandHelp(), false)
                || options.applicationArguments().get(0).equals("version")
                || NBootUtils.firstNonNull(options.commandVersion(), false)))) {
            if (!options.errors().isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();
                for (String s : options.errors()) {
                    errorMessage.append(s).append("\n");
                }
                errorMessage.append("Try 'nuts --help' for more information.");
                if (!NBootUtils.firstNonNull(options.skipErrors(), false)) {
                    throw new NBootException(NBootMsg.ofPlain(errorMessage.toString()));
                }
            }
        }
    }

    public static NBootArg parseLogLevel(NBootLogConfig logConfig, NBootCmdLine cmdLine, boolean enabled) {
        NBootArg a = cmdLine.peek();
        switch (a.key()) {
            case "--log-file-size": {
                a = cmdLine.nextEntry();
                String v = a.stringValue();
                if (enabled) {
                    Integer fileSize = NBootUtils.parseFileSizeInBytes(v, 1024 * 1024);
                    if (fileSize == null) {
                        if (NBootUtils.isBlank(v)) {
                            throw new NBootException(NBootMsg.ofC("invalid file size : %s", v));
                        }
                    } else {
                        //always in mega
                        fileSize = fileSize / (1024 * 1024);
                        if (fileSize <= 0) {
                            throw new NBootException(NBootMsg.ofC("invalid file size : %s < 1Mb", v));
                        }
                    }
                    if (fileSize != null) {
                        logConfig.logFileSize(fileSize);
                    }
                    return (a);
                } else {
                    return (a);
                }
            }

            case "--log-file-count": {
                a = cmdLine.nextEntry();
                if (enabled) {
                    logConfig.logFileCount(NBootUtils.firstNonNull(NBootUtils.parseInt(a.value()), 0));
                    return (a);
                } else {
                    return (a);
                }
            }

            case "--log-file-name": {
                a = cmdLine.nextEntry();
                String v = a.stringValue();
                if (enabled) {
                    logConfig.logFileName(v);
                    return (a);
                } else {
                    return (a);
                }
            }

            case "--log-file-base": {
                a = cmdLine.nextEntry();
                String v = a.stringValue();
                if (enabled) {
                    logConfig.logFileBase(v);
                    return (a);
                } else {
                    return (a);
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
                            parseLogLevel(id.substring("--log-file-".length()))
                    );
                }
                return (a);
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
                            parseLogLevel(id.substring("--log-term-".length()))
                    );
                }
                return (a);
            }

            case "-l":
            case "--verbose":
            {
                cmdLine.skip();
                if (enabled && NBootUtils.firstNonNull(a.booleanValue(), true)) {
                    logConfig.logTermLevel(Level.FINEST);
                    logConfig.logFileLevel(Level.FINEST);
                }
                return (a);
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
                    Level lvl = parseLogLevel(id.substring("--log-".length()));
                    logConfig.logTermLevel(lvl);
                    logConfig.logFileLevel(lvl);
                }
                return (a);
            }
        }
        return null;
    }

    private static String parseStoreStrategy(String s) {
        switch (NBootUtils.enumName(NBootUtils.firstNonNull(NBootUtils.trim(s), ""))) {
            case "EXPLODED":
            case "E":
                return "EXPLODED";
            case "STANDALONE":
            case "S":
                return "STANDALONE";
            case "":
                return null;
        }
        throw new NBootException(NBootMsg.ofC("unable to parse value for NStoreStrategy : %s, possible values include : exploded, standalone", s));
    }

    private static String parseIsolationLevel(String s) {
        switch (NBootUtils.enumName(NBootUtils.firstNonNull(NBootUtils.trim(s), ""))) {
            case "SYSTEM":
                return "SYSTEM";
            case "USER":
                return "USER";
            case "CONFINED":
                return "CONFINED";
            case "SANDBOX":
                return "SANDBOX";
            case "":
                return null;
        }
        throw new NBootException(NBootMsg.ofC("unable to parse value for NStoreStrategy : %s, possible values include : system, user, confined, sandbox", s));
    }

    private static String parseFetchStrategy(String s) {
        switch (NBootUtils.enumName(NBootUtils.firstNonNull(NBootUtils.trim(s), ""))) {
            case "OFFLINE":
                return "OFFLINE";
            case "ONLINE":
                return "ONLINE";
            case "ANYWHERE":
                return "ANYWHERE";
            case "REMOTE":
                return "REMOTE";
            case "":
                return null;
        }
        throw new NBootException(NBootMsg.ofC("unable to parse value for NFetchStrategy : %s, possible values include : offline, online, anywhere, remote", s));
    }

    private static String parseSupportMode(String s) {
        switch (NBootUtils.enumName(NBootUtils.firstNonNull(NBootUtils.trim(s), ""))) {
            case "SUPPORTED":
                return "SUPPORTED";
            case "PREFERRED":
                return "PREFERRED";
            case "ALWAYS":
            case "YES":
            case "TRUE":
                return "ALWAYS";
            case "NEVER":
            case "UNSUPPORTED":
            case "NO":
            case "FALSE":
                return "NEVER";
            case "":
                return null;
        }
        throw new NBootException(NBootMsg.ofC("unable to parse value for NSupportMode : %s, possible values include : supported, preferred, always, never", s));
    }

    private static String parseStoreType(String s) {
        switch (NBootUtils.enumName(NBootUtils.firstNonNull(NBootUtils.trim(s), ""))) {
            case "CACHE":
                return "CACHE";
            case "BIN":
                return "BIN";
            case "CONF":
                return "CONF";
            case "VAR":
                return "VAR";
            case "LOG":
                return "LOG";
            case "TEMP":
            case "TMP":
                return "TEMP";
            case "LIB":
                return "LIB";
            case "RUN":
                return "RUN";
            case "":
                return null;
        }
        throw new NBootException(NBootMsg.ofC("unable to parse value for NStoreType : %s, possible values include : cache, bin, conf, vqr, log, temp, lib, run", s));
    }

    private static String parseTerminalMode(String s) {
        switch (NBootUtils.enumName(NBootUtils.firstNonNull(NBootUtils.trim(s), ""))) {
            case "DEFAULT":
            case "SYSTEM":
            case "S":
            case "AUTO":
            case "D":
                return "DEFAULT";
            case "INHERITED":
            case "H":
                return "INHERITED";
            case "ANSI":
            case "A":
                return "ANSI";
            case "TRUE":
            case "YES":
            case "Y":
            case "SUPPORTED":
            case "ALWAYS":
            case "FORMATTED":
                return "FORMATTED";
            case "NO":
            case "FALSE":
            case "NEVER":
            case "UNSUPPORTED":
            case "FILTERED":
                return "FILTERED";
            case "":
                return null;
        }
        throw new NBootException(NBootMsg.ofC("unable to parse value for NTerminalMode : %s, possible values include : default, system, inherited, ansi, formatted, filtered", s));
    }


    private static String parseOsFamily(String s) {
        String e = NBootUtils.enumName(NBootUtils.firstNonNull(NBootUtils.trim(s), ""));
        switch (e) {
            case "WINDOWS":
            case "W":
                return "WINDOWS";
            case "LINUX":
            case "L":
                return "LINUX";
            case "MACOS":
            case "MAC_OS":
            case "MAC":
            case "M":
                return "MACOS";
            case "UNIX":
            case "U":
                return "UNIX";
            case "UNKNOWN":
                return "UNKNOWN";
            case "":
                return null;
        }
        if (e.startsWith("LINUX")) {
            return "LINUX";
        }
        if (e.startsWith("WIN")) {
            return "WINDOWS";
        }
        if (e.startsWith("MAC")) {
            return "MACOS";
        }
        if (e.startsWith("SUNOS") || e.startsWith("SUN_OS")) {
            return "UNIX";
        }
        if (e.startsWith("FREEBSD") || e.startsWith("FREE_BSD")) {
            return "UNIX";
        }
        //process plexus os families
        switch (e) {
            case "DOS":
            case "MSDOS":
            case "MS_DOS":
                return "WINDOWS";
            case "NETWARE":
            case "NET_WARE":
                return "UNKNOWN";
            case "OS2":
            case "OS_2":
                return "UNKNOWN";
            case "TANDEM":
                return "UNKNOWN";
            case "Z_OS":
            case "ZOS":
                return "UNKNOWN";
            case "OS400":
            case "OS_400":
                return "UNIX";
            case "OPENVMS":
            case "OPEN_VMS":
                return "UNKNOWN";
        }
        return "UNKNOWN";
    }

    private static String parseNutsOpenMode(String s) {
        String e = NBootUtils.enumName(NBootUtils.firstNonNull(NBootUtils.trim(s), ""));
        switch (e) {
            case "OPEN_OR_CREATE":
            case "RW":
            case "R_W":
            case "READ_WRITE":
            case "OC":
            case "O_C":
            case "OPEN_CREATE":
            case "AUTO":
            case "AUTO_CREATE":
                return "OPEN_OR_CREATE";

            case "CREATE_OR_ERROR":
            case "W":
            case "WRITE":
            case "C":
            case "CE":
            case "C_E":
            case "CREATE":
            case "CREATE_ERROR":
            case "NEW":
                return "CREATE_OR_ERROR";

            case "OPEN_OR_ERROR":
            case "R":
            case "READ":
            case "O":
            case "OE":
            case "O_E":
            case "OPEN":
            case "OPEN_ERROR":
            case "EXISTING":
                return "OPEN_OR_ERROR";

            case "OPEN_OR_NULL":
            case "ON":
            case "O_N":
            case "OPEN_NULL":
            case "TRY_OPEN":
                return "OPEN_OR_NULL";
            case "":
                return null;
        }
        throw new NBootException(NBootMsg.ofC("unable to parse value for NutsOpenMode : %s, possible values include : open-or-create, create, open, try-open", s));
    }

    public static Level parseLogLevel(String value) {
        value = value == null ? "" : value.trim();
        if (value.isEmpty()) {
            return null;
        }
        switch (value.trim().toLowerCase()) {
            case "off": {
                return (Level.OFF);
            }
            case "verbose":
            case "finest": {
                return (Level.FINEST);
            }
            case "finer": {
                return (Level.FINER);
            }
            case "fine": {
                return (Level.FINE);
            }
            case "info": {
                return (Level.INFO);
            }
            case "all": {
                return (Level.ALL);
            }
            case "warning": {
                return (Level.WARNING);
            }
            case "severe": {
                return (Level.SEVERE);
            }
            case "config": {
                return (Level.CONFIG);
            }
        }
        Integer i = NBootUtils.parseInt(value);
        if (i != null) {
            switch (i) {
                case Integer.MAX_VALUE:
                    return (Level.OFF);
                case 1000:
                    return (Level.SEVERE);
                case 900:
                    return (Level.WARNING);
                case 800:
                    return (Level.INFO);
                case 700:
                    return (Level.CONFIG);
                case 500:
                    return (Level.FINE);
                case 400:
                    return (Level.FINER);
                case 300:
                    return (Level.FINEST);
                case Integer.MIN_VALUE:
                    return (Level.ALL);
            }
            return (new CustomLogLevel("LEVEL" + i, i));
        }
        return null;
    }

    private static void addError(NBootMsg errorMessage, NBootOptionsInfo options) {
        if (errorMessage != null && options != null) {
            List<String> showError = options.errors();
            if (showError == null) {
                showError = new ArrayList<>();
            }
            showError.add(errorMessage.toString());
            options.errors(showError);
        }
    }

    private static class CustomLogLevel extends Level {
        public CustomLogLevel(String name, int value) {
            super(name, value);
        }
    }

}
