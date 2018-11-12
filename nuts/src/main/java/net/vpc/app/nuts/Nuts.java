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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/5/17.
 */
public class Nuts {

    private static final Logger log = Logger.getLogger(Nuts.class.getName());

    public static void main(String[] args) {
        try {
            System.exit(uncheckedMain(args));
        } catch (Exception ex) {
            int errorCode=204;
            //inherit error code from exception
            if(ex instanceof NutsExecutionException){
                errorCode = ((NutsExecutionException) ex).getErrorCode();
            }
            String m = ex.getMessage();
            if (m == null || m.isEmpty()) {
                m = ex.toString();
            }
            if (m == null || m.isEmpty()) {
                m = ex.getClass().getName();
            }
            System.err.println(m);
            System.exit(errorCode);
        }
    }

    private static NutsBootWorkspace openBootWorkspace() {
        return openBootWorkspace(null);
    }

    private static NutsBootWorkspace openBootWorkspace(NutsBootOptions bootOptions) {
        return new DefaultNutsBootWorkspace(bootOptions);
    }


    public static NutsWorkspace openInheritedWorkspace(String[] args) {
        long startTime = System.currentTimeMillis();
        NutsArguments nutsArguments = NutsArgumentsParser.parseNutsArguments(args, true);
        if (nutsArguments instanceof NewInstanceNutsArguments) {
            NewInstanceNutsArguments i = (NewInstanceNutsArguments) nutsArguments;
            throw new IllegalArgumentException("Unable to open a distinct version " + i.getBootFile() + "<>" + i.getRequiredVersion());
        }
        ConfigNutsArguments a = (ConfigNutsArguments) nutsArguments;
        if (a.getWorkspaceCreateOptions().getCreationTime() == 0) {
            a.getWorkspaceCreateOptions().setCreationTime(startTime);
        }
        return openWorkspace(a.getWorkspaceCreateOptions().setCreateIfNotFound(true), a.getBootOptions());
    }

    public static NutsWorkspace openWorkspace(String[] args) {
        long startTime = System.currentTimeMillis();
        NutsArguments nutsArguments = NutsArgumentsParser.parseNutsArguments(args, false);
        if (nutsArguments instanceof NewInstanceNutsArguments) {
            NewInstanceNutsArguments i = (NewInstanceNutsArguments) nutsArguments;
            throw new IllegalArgumentException("Unable to open a distinct version " + i.getBootFile() + "<>" + i.getRequiredVersion());
        }
        ConfigNutsArguments a = (ConfigNutsArguments) nutsArguments;
        if (a.getWorkspaceCreateOptions().getCreationTime() == 0) {
            a.getWorkspaceCreateOptions().setCreationTime(startTime);
        }
        return openWorkspace(a.getWorkspaceCreateOptions().setCreateIfNotFound(true), a.getBootOptions());
    }

    public static NutsWorkspace openWorkspace() {
        return openWorkspace(null, null);
    }

    public static NutsWorkspace openWorkspace(String workspace) {
        return openWorkspace(new NutsWorkspaceCreateOptions().setWorkspace(workspace), null);
    }

    public static NutsWorkspace openWorkspace(NutsWorkspaceCreateOptions options) {
        return openWorkspace(options, null);
    }

    public static NutsWorkspace openWorkspace(NutsWorkspaceCreateOptions options, NutsBootOptions bootOptions) {
        if (options == null) {
            options = new NutsWorkspaceCreateOptions();
        }
        if (options.getCreationTime() == 0) {
            options.setCreationTime(System.currentTimeMillis());
        }
        return openBootWorkspace(bootOptions).openWorkspace(options);
    }


    public static void startNewProcess(NewInstanceNutsArguments n) {
        List<String> cmd = new ArrayList<>();
        cmd.add(n.getJavaCommand());
        cmd.add("-jar");
        cmd.add(n.getBootFile().getPath());
        cmd.addAll(Arrays.asList(n.getArgs()));
        try {
            new ProcessBuilder(cmd).inheritIO().start();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to start nuts", ex);
        }
    }

    public static int uncheckedMain(String[] args) {
        long startTime = System.currentTimeMillis();
        NutsArguments a = NutsArgumentsParser.parseNutsArguments(args, false);
        if (a instanceof NewInstanceNutsArguments) {
            startNewProcess((NewInstanceNutsArguments) a);
            return 0;
        }
        ConfigNutsArguments o = (ConfigNutsArguments) a;
        o.getWorkspaceCreateOptions().setCreationTime(startTime);
        NutsWorkspace ws = openWorkspace(o.getWorkspaceCreateOptions(), o.getBootOptions());
        boolean someProcessing = false;

        String[] commandArguments = o.getArgs().toArray(new String[o.getArgs().size()]);
        NutsSession session = ws.createSession();
        int errorCode=0;
        if (o.isShowHelp()) {
            errorCode=ws.createExecBuilder()
                    .setSession(session)
                    .setCommand(NutsConstants.NUTS_SHELL, "help")
                    .exec().getResult();
            someProcessing = true;
        }
        if (o.isShowLicense()) {
            errorCode=ws.createExecBuilder()
                    .setSession(session)
                    .setCommand(NutsConstants.NUTS_SHELL, "help", "--license")
                    .exec().getResult()
            ;
            someProcessing = true;
        }

        if (o.getApplyUpdatesFile() != null) {
            errorCode=ws.exec(o.getApplyUpdatesFile(), args, false, false, session);
            return errorCode;
        }

        if (o.isCheckupdates() || o.isDoupdate()) {
            if (ws.checkWorkspaceUpdates(o.isDoupdate(), args, session).length > 0) {
                return 0;
            }
            someProcessing = true;
        }

        if (o.isVersion()) {
            NutsPrintStream out = session.getTerminal().getFormattedOut();
            out.printf("workspace-location   : [[%s]]\n", ws.getConfigManager().getWorkspaceLocation());
            out.printf("nuts-boot            : [[%s]]\n", ws.getConfigManager().getWorkspaceBootId());
            out.printf("nuts-runtime         : [[%s]]\n", ws.getConfigManager().getWorkspaceRuntimeId());
            out.printf("nuts-home            : [[%s]]\n", ws.getConfigManager().getNutsHomeLocation());
            out.printf("java-version         : [[%s]]\n", System.getProperty("java.version"));
            out.printf("java-executable      : [[%s]]\n", System.getProperty("java.home") + "/bin/java");
            out.printf("java-class-path      : [[%s]]\n", System.getProperty("java.class.path"));
            out.printf("java-library-path    : [[%s]]\n", System.getProperty("java.library.path"));
            URL[] cl = ws.getConfigManager().getBootClassWorldURLs();
            StringBuilder runtimeClassPath = new StringBuilder("?");
            if (cl != null) {
                runtimeClassPath = new StringBuilder();
                for (URL url : cl) {
                    if (url != null) {
                        if (runtimeClassPath.length() > 0) {
                            runtimeClassPath.append(":");
                        }
                        runtimeClassPath.append(url);
                    }
                }
            }
            out.printf("runtime-class-path   : [[%s]]\n", runtimeClassPath.toString());
            someProcessing = true;
        }

        if (someProcessing && commandArguments.length == 0) {
            return 0;
        }
        if (commandArguments.length == 0 && !o.isShowHelp()) {
            return ws.createExecBuilder()
                    .setSession(session)
                    .setCommand(NutsConstants.NUTS_SHELL, "help")
                    .exec().getResult();
        }
        List<String> consoleArguments = new ArrayList<>();
        consoleArguments.addAll(Arrays.asList(commandArguments));
        return ws.createExecBuilder()
                .setSession(session)
                .setCommand(consoleArguments)
                .exec().getResult();
    }

    private static boolean showPerf(long overallTimeMillis, boolean perf, NutsSession session) {
        if (perf) {
            session.getTerminal().getFormattedOut().printf("**Nuts** loaded in [[%s]]ms\n",
                    overallTimeMillis
            );
        }
        return false;
    }


    public static String getConfigCurrentVersion(String nutsHome) {
        if (nutsHome == null) {
            nutsHome = System.getProperty("user.home") + File.separator + ".nuts";
        }
        String versionUrl = NutsConstants.NUTS_ID_BOOT_PATH + "/CURRENT/nuts.version";
        File versionFile = new File(nutsHome, NutsConstants.BOOTSTRAP_REPOSITORY_NAME + versionUrl);
        try {
            if (versionFile.isFile()) {
                String str = NutsIOUtils.readStringFromFile(versionFile);
                if (str != null) {
                    str = str.trim();
                    if (str.length() > 0) {
                        return str;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.printf("Unable to load nuts version from " + versionUrl + ".\n");
        }
        return null;
    }

    public static boolean setConfigCurrentVersion(String version, String nutsHome) {
        if (nutsHome == null) {
            //System.getProperty("user.home") + File.separator + ".nuts"
            nutsHome = NutsConstants.DEFAULT_NUTS_HOME;
        }
        nutsHome = NutsIOUtils.expandPath(nutsHome);

        String versionUrl = NutsConstants.NUTS_ID_BOOT_PATH + "/CURRENT/nuts.version";
        File versionFile = new File(nutsHome, NutsConstants.BOOTSTRAP_REPOSITORY_NAME + versionUrl);
        if (version != null) {
            version = version.trim();
            if (version.isEmpty()) {
                version = null;
            }
        }
        if (version == null) {
            return versionFile.delete();
        } else {
            if (versionFile.getParentFile() != null) {
                versionFile.getParentFile().mkdirs();
            }
            PrintStream ps = null;
            try {
                try {
                    ps = new PrintStream(versionFile);
                    ps.println(version);
                    ps.close();
                } finally {
                    if (ps != null) {
                        ps.close();
                    }
                }
                return true;
            } catch (FileNotFoundException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public static String getActualVersion() {
        return NutsIOUtils.loadURLProperties(Nuts.class.getResource("/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties")).getProperty("project.version", "0.0.0");
    }

    private static void showStaticHelp() {
        String actualVersion = getActualVersion();
        String str = null;
        try {
            str = NutsIOUtils.readStringFromURL(Nuts.class.getResource("/net/vpc/app/nuts/NutsStaticHelp.txt"));
            System.out.println("Nuts " + actualVersion);
            System.out.println(str);
        } catch (IOException e) {
            System.err.println("Unable to load Help");
            e.printStackTrace();
        }
    }


}
