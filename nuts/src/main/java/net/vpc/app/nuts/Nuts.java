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
//        DiagSignalHandler.install("INT");
//        System.out.println(">>"+getBootVersion()+" :: "+Arrays.asList(args));
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
                            throw new NutsIllegalArgumentsException("Missing argument for workspace");
                        }
                        root = args[i];
                        break;
                    case "--workspace":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentsException("Missing argument for workspace");
                        }
                        workspace = args[i];
                        break;
                    case "--archetype":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentsException("Missing argument for archetype");
                        }
                        archetype = args[i];
                        break;
                    case "--login":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentsException("Missing argument for login ");
                        }
                        login = args[i];
                        break;
                    case "--password":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentsException("Missing argument for password");
                        }
                        password = args[i];
                        break;
                    case "--apply-updates":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentsException("Missing argument for apply-updates");
                        }
                        applyUpdatesFile = args[i];
                        break;
                    case "--runtime-id":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentsException("Missing argument for runtime-id");
                        }
                        runtimeId = args[i];
                        break;
                    case "--runtime-source-url":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentsException("Missing argument for boot-url");
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
                        logLevel = Level.FINEST;
                        break;
                    case "--info":
                        logLevel = Level.INFO;
                        break;
                    case "--log-finest":
                        logLevel = Level.FINEST;
                        break;
                    case "--log-fine":
                        logLevel = Level.FINE;
                        break;
                    case "--log-info":
                        logLevel = Level.INFO;
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
                    case "--log-finer":
                        logLevel = Level.FINER;
                        break;
                    case "--log-size":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentsException("Missing argument for log-size");
                        }
                        logSize = Integer.parseInt(args[i]);
                        break;
                    case "--log-count":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentsException("Missing argument for log-count");
                        }
                        logCount = Integer.parseInt(args[i]);
                        break;
                    case "--exclude-extensions":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentsException("Missing argument for exclude-extensions");
                        }
                        excludedExtensions.addAll(StringUtils.split(args[i], " ,;"));
                        break;
                    case "--exclude-repositories":
                        i++;
                        if (i >= args.length) {
                            throw new NutsIllegalArgumentsException("Missing argument for exclude-repositories");
                        }
                        excludedRepositories.addAll(StringUtils.split(args[i], " ,;"));
                        break;
                    case "--help": {
                        showHelp = true;
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
                System.err.println(s);
            }
            System.err.println("Try 'nuts --help' for more information.");
            throw new NutsIllegalArgumentsException("Try 'nuts --help' for more information.");
        }
        boolean someProcessing = false;

        List<String> argsList = new ArrayList<>(extraEnv);
        argsList.addAll(Arrays.asList(args).subList(startAppArgs, args.length));
        String[] args2 = argsList.toArray(new String[argsList.size()]);

        NutsWorkspace ws;
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
                System.err.println("workspace-boot       : " + bws.getBootId() + "" + "");
                System.err.println("workspace-runtime    : " + bws.getRuntimeId() + "");
                System.err.println("workspace-root       : " + bws.getRoot() + "" + "");
                System.err.println("workspace-location   : " + (workspace == null ? "" : workspace) + "");
                System.err.println("boot-java-version    : " + System.getProperty("java.version"));
                System.err.println("boot-java-executable : " + System.getProperty("java.home") + "/bin/java" + "");
                System.err.println("boot-java-classpath  : " + System.getProperty("java.class.path") + "");
            }
            if (showHelp) {
                System.err.println("Unable to local help. No valid workspace was resolved");
            }
            System.err.println("Unable to local nuts-core components.");
            System.err.println("You need internet connexion to initialize nuts configuration. Once components are downloaded, you may work offline...");
            System.err.println("Exiting nuts, Bye!");
            throw new NutsIllegalArgumentsException("Unable to locate nuts-core components", ex);
        }
        NutsSession session = ws.createSession();
        if (nocolors) {
            session.getTerminal().getOut().print("`disable-formats`");
            session.getTerminal().getErr().print("`disable-formats`");
        }
        if (showHelp) {
            NutsPrintStream out = session.getTerminal().getOut();
            perf = showPerf(startTime, perf, session);
            help(ws, out);
            someProcessing = true;
        }

        if (login != null && login.trim().length() > 0) {
            if (StringUtils.isEmpty(password)) {
                password = session.getTerminal().readPassword("Password : ");
            }
            ws.login(login, password);
        }

        if (applyUpdatesFile != null) {
            ws.execExternalNuts(new File(applyUpdatesFile), args, false, false, session);
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

            out.println("workspace-boot       : [[" + ws.getWorkspaceBootId() + "]]");
            out.println("workspace-runtime    : [[" + ws.getWorkspaceRuntimeId() + "]]");
            out.println("workspace-root       : [[" + bws.getRoot() + "]]");
            out.println("workspace-location   : [[" + ws.getWorkspaceLocation() + "]]");
            out.println("boot-java-version    : [[" + System.getProperty("java.version") + "]]");
            out.println("boot-java-executable : [[" + System.getProperty("java.home") + "/bin/java" + "]]");
            out.println("boot-java-classpath  : [[" + System.getProperty("java.class.path") + "]]");

            perf = showPerf(startTime, perf, session);
            someProcessing = true;
        }

        if (someProcessing && args2.length == 0) {
            return;
        }
        if (args2.length == 0) {
            /*perf = */
            showPerf(startTime, perf, session);
            help(ws, session.getTerminal().getOut());
            return;
        }
        NutsConsole commandLine = null;
        try {
            commandLine = ws.createConsole(session);
        } catch (NutsExtensionMissingException ex) {
            /*perf = */
            showPerf(startTime, perf, session);
            session.getTerminal().getErr().println("Unable to create Console. Make sure nuts-core is installed properly.");
            return;
        }
        /*perf = */
        showPerf(startTime, perf, session);
        commandLine.run(args2);

    }

    private static boolean showPerf(long startTime, boolean perf, NutsSession session) {
        if (perf) {
            session.getTerminal().getOut().println("Nuts loaded in [[" + (System.currentTimeMillis() - startTime) + "]] ms");
        }
        return false;
    }

    public static void help(NutsWorkspace bws, NutsPrintStream term) {
        String help = bws.getHelpString();
        term.println(help);
    }
}
