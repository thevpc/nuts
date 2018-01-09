/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import net.vpc.app.nuts.boot.BootNutsWorkspace;
import net.vpc.app.nuts.util.IOUtils;
import net.vpc.app.nuts.util.LogUtils;
import net.vpc.app.nuts.util.MapStringMapper;
import net.vpc.app.nuts.util.StringUtils;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/5/17.
 */
public class Main {

    public static void main(String[] args) {
        try {
            uncheckedMain(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void uncheckedMain(String[] args) throws IOException, LoginException, InterruptedException {
//        System.out.println(">>"+getBootVersion()+" :: "+Arrays.asList(args));
        long startTime = System.currentTimeMillis();
        int startAppArgs = 0;
        String workspaceRoot = null;
        String workspace = null;
        String archetype = null;
        String login = null;
        String password = null;
        String logFolder = null;
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
        List<String> showError = new ArrayList<>();
        Set<String> excludedExtensions = new HashSet<>();
        Set<String> excludedRepositories = new HashSet<>();
        NutsSession session = new NutsSession();
        List<String> extraEnv = new ArrayList<>();


        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("-")) {
                switch (a) {
                    case "--workspace-root":
                        i++;
                        if (i >= args.length) {
                            throw new IllegalArgumentException("Missing argument for workspace");
                        }
                        workspaceRoot = args[i];
                        break;
                    case "--workspace":
                        i++;
                        if (i >= args.length) {
                            throw new IllegalArgumentException("Missing argument for workspace");
                        }
                        workspace = args[i];
                        break;
                    case "--archetype":
                        i++;
                        if (i >= args.length) {
                            throw new IllegalArgumentException("Missing argument for archetype");
                        }
                        archetype = args[i];
                        break;
                    case "--login":
                        i++;
                        if (i >= args.length) {
                            throw new IllegalArgumentException("Missing argument for login ");
                        }
                        login = args[i];
                        break;
                    case "--password":
                        i++;
                        if (i >= args.length) {
                            throw new IllegalArgumentException("Missing argument for password");
                        }
                        password = args[i];
                        break;
                    case "--apply-updates":
                        i++;
                        if (i >= args.length) {
                            throw new IllegalArgumentException("Missing argument for apply-updates");
                        }
                        applyUpdatesFile = args[i];
                        break;
                    case "--save":
                        save = true;
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
                            throw new IllegalArgumentException("Missing argument for log-size");
                        }
                        logSize = Integer.parseInt(args[i]);
                        break;
                    case "--log-count":
                        i++;
                        if (i >= args.length) {
                            throw new IllegalArgumentException("Missing argument for log-count");
                        }
                        logCount = Integer.parseInt(args[i]);
                        break;
                    case "--exclude-extensions":
                        i++;
                        if (i >= args.length) {
                            throw new IllegalArgumentException("Missing argument for exclude-extensions");
                        }
                        excludedExtensions.addAll(StringUtils.split(args[i], " ,;"));
                        break;
                    case "--exclude-repositories":
                        i++;
                        if (i >= args.length) {
                            throw new IllegalArgumentException("Missing argument for exclude-repositories");
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
                        if (a.startsWith("-J") || a.startsWith("--nuts")) {
                            extraEnv.add(a);
                        }
                        showError.add("nuts: invalid option [[" + a + "]]");
                        break;
                    }
                }
                startAppArgs = i + 1;
            } else {
                break;
            }
        }
        NutsWorkspace bws = openBootstrapWorkspace(workspaceRoot);
        NutsTerminal bootTerminal = null;
        if (!showError.isEmpty()) {
            for (String err : showError) {
                if (bootTerminal == null) {
                    bootTerminal = bws.createTerminal();
                }
                bootTerminal.getErr().drawln(err);
            }
            bootTerminal.getErr().drawln("Try 'nuts --help' for more information.");
            throw new IllegalArgumentException("Try 'nuts --help' for more information.");
        }
        boolean someProcessing = false;

        if (showHelp) {
            if (bootTerminal == null) {
                bootTerminal = bws.createTerminal();
            }
            perf = showPerf(startTime, perf, session);
            help(bootTerminal.getOut());
            someProcessing = true;
        }

        List<String> argsList=new ArrayList<>(extraEnv);
        argsList.addAll(Arrays.asList(args).subList(startAppArgs, args.length - startAppArgs+1));
        String[] args2=argsList.toArray(new String[argsList.size()]);

        LogUtils.prepare(logLevel, logFolder, logSize, logCount);
        NutsWorkspace ws;
        try {
            ws = bws.openWorkspace(workspace, new NutsWorkspaceCreateOptions()
                            .setArchetype(archetype)
                            .setCreateIfNotFound(true)
                            .setSaveIfCreated(save)
                            .setExcludedRepositories(excludedRepositories)
                            .setExcludedExtensions(excludedExtensions),
                    session
            );
        } catch (net.vpc.app.nuts.NutsNotFoundException ex) {

            if (bootTerminal == null) {
                bootTerminal = bws.createTerminal();
            }
            NutsPrintStream err = bootTerminal.getErr();
            err.drawln("Unable to locate nuts-core components.");
            err.drawln("You need internet connexion to initialize nuts configuration. Once components are downloaded, you may work offline...");
            err.drawln("Exiting nuts, Bye!");
            throw new IllegalArgumentException("Unable to locate nuts-core components", ex);
        }

        if (login != null && login.trim().length() > 0) {
            if (StringUtils.isEmpty(password)) {
                password = session.getTerminal().readPassword("Password : ");
            }
            ws.login(login, password);
        }

        if (applyUpdatesFile != null) {
            ws.execExternalNuts(session, new File(applyUpdatesFile), args, false, false);
            return;
        }

        if (checkupdates || doupdate) {
            if (ws.checkWorkspaceUpdates(session, doupdate, args).length > 0) {
                return;
            }
            someProcessing = true;
        }

        if (version) {
            NutsPrintStream out = session.getTerminal().getOut();

            Map<String, String> runtimeProperties = ws.getRuntimeProperties(session);
            out.drawln("boot-version         : [[" + runtimeProperties.get("nuts.boot.version") + "]]");
            out.drawln("workspace-version    : [[" + runtimeProperties.get("nuts.workspace.version") + "]]");
            out.drawln("boot-version         : [[" + runtimeProperties.get("nuts.boot.version") + "]]");
            out.drawln("boot-location        : [[" + runtimeProperties.get("nuts.boot.workspace") + "]]");
            out.drawln("boot-api             : [[" + runtimeProperties.get("nuts.boot.api-component") + "]]");
            out.drawln("boot-core            : [[" + runtimeProperties.get("nuts.boot.core-component") + "]]");
            out.drawln("target-workspace     : [[" + runtimeProperties.get("nuts.boot.target-workspace") + "]]");
            out.drawln("boot-java-version    : [[" + System.getProperty("java.version") + "]]");
            out.drawln("boot-java-executable : [[" + System.getProperty("java.home") + "/bin/java" + "]]");

            perf = showPerf(startTime, perf, session);
            someProcessing = true;
        }

        if (someProcessing && args2.length == 0) {
            return;
        }
        if (args2.length == 0) {
            perf = showPerf(startTime, perf, session);
            help(session.getTerminal().getOut());
            return;
        }
        NutsCommandLineConsoleComponent commandLine = null;
        try {
            commandLine = ws.createCommandLineConsole(session);
        } catch (NutsExtensionMissingException ex) {
            perf = showPerf(startTime, perf, session);
            session.getTerminal().getErr().println("Unable to create Console. Make sure nuts-core is installed properly.");
            return;
        }
        perf = showPerf(startTime, perf, session);
        commandLine.run(args2);


    }

    private static boolean showPerf(long startTime, boolean perf, NutsSession session) {
        if (perf) {
            session.getTerminal().getOut().drawln("Nuts loaded in [[" + (System.currentTimeMillis() - startTime) + "]] ms");
        }
        return false;
    }

    public static String getBootVersion() {
        return
                IOUtils.loadProperties(Main.class.getResource("/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties"))
                        .getProperty("project.version", "0.0.0");
    }

    public static NutsWorkspace openBootstrapWorkspace(String workspaceRoot) throws IOException {
        NutsWorkspace w = new BootNutsWorkspace();
        w.initializeWorkspace(workspaceRoot, null, null, null, null, new NutsSession());
        w.save();
        return w;
    }

    public static void help(NutsPrintStream term) {
        String help = getHelpString();
        term.drawln(help);
    }


    public static String getHelpString() {
        String help = null;
        try {
            InputStream s = null;
            try {
                s = Main.class.getResourceAsStream("/net/vpc/app/nuts/help.help");
                if (s != null) {
                    help = IOUtils.readStreamAsString(s, true);
                }
            } finally {
                if (s != null) {
                    s.close();
                }
            }
        } catch (IOException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Unable to load main help", e);
        }
        if (help == null) {
            help = "no help found";
        }
        HashMap<String, String> props = new HashMap<>((Map) System.getProperties());
        props.put("nuts.boot-version", getBootVersion());
        help = StringUtils.replaceVars(help, new MapStringMapper(props));
        return help;
    }

}
