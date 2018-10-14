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
 * Copyright (C) 2016-2017 Taha BEN SALAH
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

import java.io.File;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by vpc on 1/5/17.
 */
public class Nuts {

    public static NutsBootWorkspace openBootWorkspace() {
        return new DefaultNutsBootWorkspace();
    }

    public static NutsBootWorkspace openBootWorkspace(NutsBootOptions bootOptions) {
        return new DefaultNutsBootWorkspace(bootOptions);
    }

    public static NutsWorkspace openWorkspace() {
        return openWorkspace(null, null, null);
    }

    public static NutsWorkspace openWorkspace(String workspace) {
        return openWorkspace(workspace, null, null);
    }

    public static NutsWorkspace openWorkspace(String workspace, NutsWorkspaceCreateOptions options) {
        return openWorkspace(workspace, options, null);
    }

    public static NutsWorkspace openWorkspace(String workspace, NutsWorkspaceCreateOptions options, NutsBootOptions bootOptions) {
        return openBootWorkspace(bootOptions).openWorkspace(workspace, options);
    }

    public static void main(String[] args) {
        try {
            uncheckedMain(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void uncheckedMain(String[] args) {
        long startTime = System.currentTimeMillis();
        int startAppArgs = 0;
        String root = null;
        String workspace = null;
        String archetype = null;
        String login = null;
        String password = null;
        String logFolder = null;
        String runtimeId = null;
        String runtimeSourceURL = null;
        Level logLevel = null;
        int logSize = 0;
        int logCount = 0;
        boolean save = true;
        boolean version = false;
        boolean doupdate = false;
        boolean checkupdates = false;
        String applyUpdatesFile = null;
        boolean perf = false;
        boolean showHelp = false;
        boolean showLicense = false;
        boolean nocolors = false;
        List<String> showError = new ArrayList<>();
        Set<String> excludedExtensions = new HashSet<>();
        Set<String> excludedRepositories = new HashSet<>();
        List<String> extraEnv = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("-")) {
                switch (a) {
                    case "--workspace-root":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for workspace");
                        }
                        root = args[i];
                        break;
                    case "--workspace":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for workspace");
                        }
                        workspace = args[i];
                        break;
                    case "--archetype":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for archetype");
                        }
                        archetype = args[i];
                        break;
                    case "--login":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for login ");
                        }
                        login = args[i];
                        break;
                    case "--password":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for password");
                        }
                        password = args[i];
                        break;
                    case "--apply-updates":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for apply-updates");
                        }
                        applyUpdatesFile = args[i];
                        break;
                    case "--runtime-id":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for runtime-id");
                        }
                        runtimeId = args[i];
                        break;
                    case "--runtime-source-url":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for boot-url");
                        }
                        runtimeSourceURL = args[i];
                        break;
                    case "--save":
                        save = true;
                        break;
                    case "--no-colors":
                        nocolors = true;
                        break;
                    case "--nosave":
                        save = false;
                        break;
                    case "--version":
                        version = true;
                        break;
                    case "--update":
                        doupdate = true;
                        break;
                    case "--check-updates":
                        checkupdates = true;
                        break;
                    case "--verbose":
                    case "--log-finest":
                        logLevel = Level.FINEST;
                        break;
                    case "--info":
                    case "--log-info":
                        logLevel = Level.INFO;
                        break;
                    case "--log-fine":
                        logLevel = Level.FINE;
                        break;
                    case "--log-finer":
                        logLevel = Level.FINER;
                        break;
                    case "--log-all":
                        logLevel = Level.ALL;
                        break;
                    case "--log-off":
                        logLevel = Level.OFF;
                        break;
                    case "--log-severe":
                        logLevel = Level.SEVERE;
                        break;
                    case "--log-size":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for log-size");
                        }
                        logSize = Integer.parseInt(args[i]);
                        break;
                    case "--log-count":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for log-count");
                        }
                        logCount = Integer.parseInt(args[i]);
                        break;
                    case "--exclude-extensions":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for exclude-extensions");
                        }
                        excludedExtensions.addAll(StringUtils.split(args[i], " ,;"));
                        break;
                    case "--exclude-repositories":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentException("Missing argument for exclude-repositories");
                        }
                        excludedRepositories.addAll(StringUtils.split(args[i], " ,;"));
                        break;
                    case "--help": {
                        showHelp = true;
                        break;
                    }
                    case "--license": {
                        showLicense = true;
                        break;
                    }
                    case "--perf": {
                        perf = true;
                        break;
                    }
                    default: {
                        if (a.startsWith("-J")) {
                            extraEnv.add(a);
                        } else if (a.startsWith("--nuts")) {
                            extraEnv.add(a);
                        } else {
                            showError.add("nuts: invalid option [[" + a + "]]");
                        }
                        break;
                    }
                }
                startAppArgs = i + 1;
            } else {
                break;
            }
        }

        LogUtils.prepare(logLevel, logFolder, logSize, logCount);
        NutsBootWorkspace bws = openBootWorkspace(
                new NutsBootOptions()
                        .setRoot(root)
                        .setRuntimeId(runtimeId)
                        .setRuntimeSourceURL(runtimeSourceURL)
        );
        if (!showError.isEmpty()) {
            for (String s : showError) {
                System.err.printf("%s·\n", s);
            }
            System.err.printf("Try 'nuts --help' for more information.\n");
            throw new NutsIllegalArgumentException("Try 'nuts --help' for more information.");
        }
        boolean someProcessing = false;

        List<String> argsList = new ArrayList<>(extraEnv);
        argsList.addAll(Arrays.asList(args).subList(startAppArgs, args.length));
        String[] commandArguments = argsList.toArray(new String[argsList.size()]);

        NutsWorkspace ws;
        long startWSTime = System.currentTimeMillis();
        try {
            ws = bws.openWorkspace(workspace, new NutsWorkspaceCreateOptions()
                    .setArchetype(archetype)
                    .setCreateIfNotFound(true)
                    .setSaveIfCreated(save)
                    .setExcludedRepositories(excludedRepositories)
                    .setExcludedExtensions(excludedExtensions)
            );
        } catch (Exception ex) {
            if (version) {
                System.err.printf("workspace-boot       : %s\n", bws.getBootId());
                System.err.printf("workspace-runtime    : %s\n", bws.getRuntimeId());
                System.err.printf("workspace-root       : %s\n", bws.getRootLocation());
                System.err.printf("workspace-location   : %s\n", (workspace == null ? "" : workspace));
                System.err.printf("boot-java-version    : %s\n", System.getProperty("java.version"));
                System.err.printf("boot-java-executable : %s\n", System.getProperty("java.home") + "/bin/java");
                System.err.printf("boot-java-classpath  : %s\n", System.getProperty("java.class.path"));
            }
            if (showHelp) {
                System.err.printf("Unable to locate help. No valid workspace was resolved\n");
            }
            System.err.printf("Unable to locate nuts-core components.\n");
            System.err.printf("You need internet connexion to initialize nuts configuration. Once components are downloaded, you may work offline...\n");
            System.err.printf("Exiting nuts, Bye!\n");
            throw new NutsIllegalArgumentException("Unable to locate nuts-core components", ex);
        }
        NutsSession session = ws.createSession();
        if (nocolors) {
            session.getTerminal().getOut().print("`disable-formats`");
            session.getTerminal().getErr().print("`disable-formats`");
        }
        if (showHelp) {
            perf = showPerf(startTime, perf, session);
            ws.exec(new String[]{"console", "help"}, null, session);
            someProcessing = true;
        }
        if (showLicense) {
            perf = showPerf(startTime, perf, session);
            ws.exec(new String[]{"console", "help", "--license"}, null, session);
            someProcessing = true;
        }
        if (login != null && login.trim().length() > 0) {
            if (StringUtils.isEmpty(password)) {
                password = session.getTerminal().readPassword("Password : ");
            }
            ws.getSecurityManager().login(login, password);
        }

        if (applyUpdatesFile != null) {
            ws.exec(new File(applyUpdatesFile), args, false, false, session);
            return;
        }

        if (checkupdates || doupdate) {
            if (ws.checkWorkspaceUpdates(doupdate, args, session).length > 0) {
                return;
            }
            someProcessing = true;
        }

        if (version) {
            NutsPrintStream out = session.getTerminal().getOut();

            out.printf("workspace-boot       : [[%s]]\n", ws.getConfigManager().getWorkspaceBootId());
            out.printf("workspace-runtime    : [[%s]]\n", ws.getConfigManager().getWorkspaceRuntimeId());
            out.printf("workspace-root       : [[%s]]\n", bws.getRootLocation());
            out.printf("workspace-location   : [[%s]]\n", ws.getConfigManager().getWorkspaceLocation());
            out.printf("boot-java-version    : [[%s]]\n", System.getProperty("java.version"));
            out.printf("boot-java-executable : [[%s]]\n", System.getProperty("java.home") + "/bin/java");
            out.printf("boot-java-classpath  : [[%s]]\n", System.getProperty("java.class.path"));

            perf = showPerf(System.currentTimeMillis() - startTime, perf, session);
            someProcessing = true;
        }

        if (someProcessing && commandArguments.length == 0) {
            return;
        }
        if (commandArguments.length == 0 && !showHelp) {
            /*perf = */
            showPerf(startTime, perf, session);
            ws.exec(new String[]{"console", "help"}, null, session);
            return;
        }
        /*perf = */
        showPerf(System.currentTimeMillis() - startTime, perf, session);
        List<String> consoleArguments = new ArrayList<>();
        consoleArguments.add("console");
        consoleArguments.addAll(Arrays.asList(commandArguments));
        ws.exec(consoleArguments.toArray(new String[consoleArguments.size()]), null, session);
    }

    private static boolean showPerf(long overallTime, boolean perf, NutsSession session) {
        if (perf) {
            session.getTerminal().getOut().printf("**Nuts** loaded in [[%s]]ms\n",
                    overallTime
            );
        }
        return false;
    }

}
