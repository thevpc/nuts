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
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
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
            boolean showErrorClass = false;
            int errorCode = 204;
            //inherit error code from exception
            if (ex instanceof NutsExecutionException) {
                errorCode = ((NutsExecutionException) ex).getExitCode();
            }
            boolean showTrace = false;
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-")) {
                    if (args[i].equals("--verbose")) {
                        showTrace = true;
                        showErrorClass = true;
                    }
                } else {
                    break;
                }
            }
            if (ex.getClass().getName().startsWith("java.lang.")) {
                //this is a common error
                showTrace = true;
                showErrorClass = true;
            }
            String m = ex.getMessage();
            if (m == null || m.isEmpty()) {
                m = ex.toString();
            }
            if (m == null || m.isEmpty()) {
                m = ex.getClass().getName();
            }
            if (showErrorClass) {
                m = ex.toString();
            }
            System.err.println(m);
            if (showTrace) {
                ex.printStackTrace(System.err);
            }
            System.exit(errorCode);
        }
    }

    public static NutsWorkspace openInheritedWorkspace(String[] args) {
        return openWorkspace(args, true);
    }

    public static NutsWorkspace openWorkspace(String[] args) {
        return openWorkspace(args, false);
    }

    private static NutsWorkspace openWorkspace(String[] args, boolean expectedNutsArgs) {
        long startTime = System.currentTimeMillis();
        NutsArguments nutsArguments = NutsArgumentsParser.parseNutsArguments(args, expectedNutsArgs);
        if (nutsArguments instanceof NutsNewInstanceNutsArguments) {
            NutsNewInstanceNutsArguments i = (NutsNewInstanceNutsArguments) nutsArguments;
            throw new IllegalArgumentException("Unable to open a distinct version " + i.getBootFile() + "<>" + i.getRequiredVersion());
        }
        NutsWorkspaceOptions a = (NutsWorkspaceOptions) nutsArguments;
        if (a.getCreationTime() == 0) {
            a.setCreationTime(startTime);
        }
        return openWorkspace(a.setCreateIfNotFound(true));
    }

    public static NutsWorkspace openWorkspace() {
        return openWorkspace((NutsWorkspaceOptions) null);
    }

    public static NutsWorkspace openWorkspace(String workspace) {
        return openWorkspace(new NutsWorkspaceOptions().setWorkspace(workspace));
    }

    public static NutsWorkspace openWorkspace(NutsWorkspaceOptions options) {
        if (options == null) {
            options = new NutsWorkspaceOptions();
        }
        if (options.getCreationTime() == 0) {
            options.setCreationTime(System.currentTimeMillis());
        }
        NutsLogUtils.prepare(options.getLogLevel(), options.getLogFolder(), options.getLogName(),
                options.getLogSize(), options.getLogCount(), options.isLogInherited(), options.getHome(), options.getWorkspace());
        if(log.isLoggable(Level.CONFIG)) {
            log.log(Level.CONFIG, "Open Workspace : {0} @ {1}", new Object[]{
                    NutsUtils.isEmpty(options.getWorkspace()) ? NutsConstants.DEFAULT_WORKSPACE_NAME : options.getWorkspace(),
                    NutsUtils.isEmpty(options.getHome()) ? getDefaultNutsHome() : options.getHome()
            });
        }
        return new NutsBootWorkspace(options).openWorkspace();
    }


    public static void startNewProcess(NutsNewInstanceNutsArguments n) {
        List<String> cmd = new ArrayList<>();
        String jc = n.getJavaCommand();
        if (jc == null || jc.trim().isEmpty()) {
            jc = NutsUtils.resolveJavaCommand(null);
        }
        cmd.add(jc);
        boolean showCommand = false;
        for (String c : NutsMinimalCommandLine.parseCommandLine(n.getJavaOptions())) {
            if (!c.isEmpty()) {
                if (c.equals("--show-command")) {
                    showCommand = true;
                } else {
                    cmd.add(c);
                }
            }
        }
        Collections.addAll(cmd, NutsMinimalCommandLine.parseCommandLine(n.getJavaOptions()));
//        cmd.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005");
        cmd.add("-jar");
        cmd.add(n.getBootFile().getPath());
        //cmd.add("--verbose");
        cmd.addAll(Arrays.asList(n.getArgs()));
        if (showCommand) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cmd.size(); i++) {
                String s = cmd.get(i);
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(s);
            }
            System.out.println("[EXEC] " + sb);
        }
        try {
            new ProcessBuilder(cmd).inheritIO().start().waitFor();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to start nuts", ex);
        }
    }

    public static int uncheckedMain(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        NutsArguments a = NutsArgumentsParser.parseNutsArguments(args, false);
        if (a instanceof NutsNewInstanceNutsArguments) {
            startNewProcess((NutsNewInstanceNutsArguments) a);
            return 0;
        }
        NutsWorkspaceOptions o = (NutsWorkspaceOptions) a;
        o.setCreationTime(startTime);
        NutsWorkspace workspace = null;
        try {
            workspace = openWorkspace(o);
        } catch (NutsException ex) {
            switch (o.getBootCommand()){
                case VERSION:
                case INFO:
                case HELP:
                case LICENSE:
                    {
                    try {
                        runWorkspaceCommand(null, o,"Cannot start workspace to run command "+o.getBootCommand()+". "+ex.getMessage());
                        return 0;
                    } catch (NutsUserCancelException e) {
                        System.err.println(e.getMessage());
                    } catch (Exception e) {
                        System.err.println(e.toString());
                    }
                }
            }
            throw ex;
        } catch (Throwable ex) {
            int x = 204;
            try {
                x = runWorkspaceCommand(null, o,"Cannot start workspace to run command "+o.getBootCommand()+". Try --clean or --reset to help recovering :"+ex.toString());
            } catch (Exception e) {
                System.err.println(e.toString());
            }
            if (ex instanceof RuntimeException) {
                throw ex;
            }
            if (ex instanceof Error) {
                throw ex;
            }
            throw new NutsExecutionException(ex.getMessage(), ex, x);
        }
        return runWorkspaceCommand(workspace, o,"Workspace started successfully");
    }

    private static int runWorkspaceCommand(NutsWorkspace workspace, NutsWorkspaceOptions o,String message) throws IOException {
        if(log.isLoggable(Level.CONFIG)) {
            log.log(Level.CONFIG, "Running workspace command : {0}", o.getBootCommand());
        }
        NutsWorkspaceConfigManager conf = null;
        if (workspace != null) {
            conf = workspace.getConfigManager();
        }
        switch (o.getBootCommand()) {
            case VERSION: {
                if (workspace == null) {
                    System.out.println("nuts-api :" + getActualVersion());
                    System.out.println(message);
                    if(log.isLoggable(Level.SEVERE)) {
                        log.log(Level.SEVERE, message);
                    }
                    return 1;
                }
                PrintStream out = workspace.getTerminal().getFormattedOut();
                out.println(
                        workspace.getFormatManager().createWorkspaceVersionFormat()
                                .addOptions(o.getApplicationArguments())
                                .format()
                );
                return 0;
            }
            case INFO: {
                if (workspace == null) {
                    System.out.println("nuts-boot-api          :" + getActualVersion());
                    System.out.println("nuts-home              :" + (NutsUtils.isEmpty(o.getHome()) ? "<EMPTY>" : o.getHome()));
                    System.out.println("nuts-workspace         :" + (NutsUtils.isEmpty(o.getWorkspace()) ? "<EMPTY>" : o.getWorkspace()));
                    System.out.println("nuts-default-workspace :" + Nuts.getDefaultWorkspaceFolder(o.getWorkspace(), o.getHome(), null));
                    System.out.println("nuts-default-programs  :" + Nuts.getDefaultWorkspaceFolder(o.getWorkspace(), o.getHome(), RootFolderType.PROGRAMS));
                    System.out.println("nuts-default-config    :" + Nuts.getDefaultWorkspaceFolder(o.getWorkspace(), o.getHome(), RootFolderType.CONFIG));
                    System.out.println("nuts-default-var       :" + Nuts.getDefaultWorkspaceFolder(o.getWorkspace(), o.getHome(), RootFolderType.VAR));
                    System.out.println("nuts-default-logs      :" + Nuts.getDefaultWorkspaceFolder(o.getWorkspace(), o.getHome(), RootFolderType.LOGS));
                    System.out.println("nuts-default-temp      :" + Nuts.getDefaultWorkspaceFolder(o.getWorkspace(), o.getHome(), RootFolderType.TEMP));
                    System.out.println("nuts-default-cache     :" + Nuts.getDefaultWorkspaceFolder(o.getWorkspace(), o.getHome(), RootFolderType.CACHE));
                    System.out.println("java-home              :" + System.getProperty("java.home"));
                    System.out.println("java-classpath         :" + System.getProperty("java.class.path"));
                    System.out.println("java-library-path      :" + System.getProperty("java.library.path"));
                    System.out.println("os-name                :" + System.getProperty("os.name"));
                    System.out.println("os-arch                :" + System.getProperty("os.arch"));
                    System.out.println("os-version             :" + System.getProperty("os.version"));
                    System.out.println("user-dir               :" + System.getProperty("user.dir"));
                    System.out.println("user-home              :" + System.getProperty("user.home"));
                    System.out.println(message);
                    if(log.isLoggable(Level.SEVERE)) {
                        log.log(Level.SEVERE, message);
                    }
                    return 1;
                }
                PrintStream out = workspace.getTerminal().getFormattedOut();
                out.println(
                        workspace.getFormatManager().createWorkspaceInfoFormat()
                                .addOptions(o.getApplicationArguments())
                                .format()
                );
                return 0;
            }
            case HELP: {
                if (workspace == null) {
                    System.out.println(message);
                    if(log.isLoggable(Level.SEVERE)) {
                        log.log(Level.SEVERE, message);
                    }
                    return 1;
                }
                workspace.getTerminal().getFormattedOut().println(workspace.getHelpText());
                return 0;
            }
            case LICENSE: {
                if (workspace == null) {
                    System.out.println(message);
                    if(log.isLoggable(Level.SEVERE)) {
                        log.log(Level.SEVERE, message);
                    }
                    return 1;
                }
                workspace.getTerminal().getFormattedOut().println(workspace.getLicenseText());
                return 0;
            }
            case INSTALL: {
                if (workspace == null) {
                    System.out.println(message);
                    if(log.isLoggable(Level.SEVERE)) {
                        log.log(Level.SEVERE, message);
                    }
                    return 1;
                }
                List<String> ids = new ArrayList<>();
                NutsConfirmAction confirm = NutsConfirmAction.ERROR;
                for (String c : o.getApplicationArguments()) {
                    switch (c) {
                        case "-f":
                        case "--force":
                            confirm = NutsConfirmAction.FORCE;
                            break;
                        case "-i":
                        case "--ignore":
                            confirm = NutsConfirmAction.IGNORE;
                            break;
                        case "-e":
                        case "--error":
                            confirm = NutsConfirmAction.ERROR;
                            break;
                        default:
                            ids.add(c);
                            break;
                    }
                }
                if (ids.isEmpty()) {
                    throw new NutsExecutionException("Missing nuts to install", 1);
                }
                for (String id : ids) {
                    workspace.install(id, o.getApplicationArguments(), confirm, null);
                }
                return 0;
            }
            case UNINSTALL: {
                if (workspace == null) {
                    System.out.println(message);
                    if(log.isLoggable(Level.SEVERE)) {
                        log.log(Level.SEVERE, message);
                    }
                    return 1;
                }
                List<String> ids = new ArrayList<>();
                NutsConfirmAction confirm = NutsConfirmAction.ERROR;
                boolean deleteData = false;
                for (String c : o.getApplicationArguments()) {
                    if (c.equals("-f") || c.equals("--force")) {
                        confirm = NutsConfirmAction.FORCE;
                    } else if (c.equals("-i") || c.equals("--ignore")) {
                        confirm = NutsConfirmAction.IGNORE;
                    } else if (c.equals("-e") || c.equals("--error")) {
                        confirm = NutsConfirmAction.ERROR;
                    } else if (c.equals("-r") || c.equals("--erase")) {
                        deleteData = true;
                    } else {
                        ids.add(c);
                    }
                }
                if (ids.isEmpty()) {
                    throw new NutsExecutionException("Missing nuts to uninstall", 1);
                }
                for (String id : ids) {
                    workspace.uninstall(id, o.getApplicationArguments(), confirm, deleteData, null);
                }
                return 0;
            }
            case INSTALL_COMPANIONS: {
                if (workspace == null) {
                    System.out.println(message);
                    if(log.isLoggable(Level.SEVERE)) {
                        log.log(Level.SEVERE, message);
                    }
                    return 1;
                }
                boolean force = false;
                boolean silent = false;
                for (String argument : o.getApplicationArguments()) {
                    if ("-f".equals(argument) || "--force".equals(argument)) {
                        force = true;
                    }else if ("-s".equals(argument) || "--silent".equals(argument)) {
                        silent = true;
                    }
                }
                workspace.installCompanionTools(force, silent, null);
                return 0;
            }
            case UPDATE: {
                if (workspace == null) {
                    System.out.println(message);
                    if(log.isLoggable(Level.SEVERE)) {
                        log.log(Level.SEVERE, message);
                    }
                    return 1;
                }
                if (workspace.checkWorkspaceUpdates(new NutsWorkspaceUpdateOptions()
                                .setApplyUpdates(true)
                                .setEnableMajorUpdates(true)
                                .setEnableMajorUpdates(true)
                        , null).length > 0) {
                    return 0;
                }
                return 1;
            }
            case CHECK_UPDATES: {
                if (workspace == null) {
                    System.out.println(message);
                    if(log.isLoggable(Level.SEVERE)) {
                        log.log(Level.SEVERE, message);
                    }
                    return 1;
                }
                if (workspace.checkWorkspaceUpdates(new NutsWorkspaceUpdateOptions()
                                .setApplyUpdates(false)
                                .setEnableMajorUpdates(true)
                                .setEnableMajorUpdates(true)
                        , null).length > 0) {
                    return 0;
                }
                return 1;
            }
            case CLEAN: {
                if(log.isLoggable(Level.SEVERE)) {
                    log.log(Level.SEVERE, message);
                }
                boolean force = false;
                for (String argument : o.getApplicationArguments()) {
                    if ("-f".equals(argument) || "--force".equals(argument)) {
                        force = true;
                    }
                }
                List<File> folders = new ArrayList<>();
                String workspaceLocation = null;
                if (workspace != null) {
                    folders.add(new File(conf.getStoreRoot(RootFolderType.CACHE)));
                    folders.add(new File(conf.getStoreRoot(RootFolderType.LOGS)));
                    workspaceLocation = conf.getWorkspaceLocation();
                } else {
                    folders.add(new File(Nuts.getDefaultWorkspaceFolder(o.getWorkspace(), o.getHome(), RootFolderType.CACHE)));
                    folders.add(new File(Nuts.getDefaultWorkspaceFolder(o.getWorkspace(), o.getHome(), RootFolderType.LOGS)));
                    workspaceLocation = Nuts.getDefaultWorkspaceFolder(o.getWorkspace(), o.getHome(), null);
                }
                File[] children = new File(workspaceLocation, NutsConstants.FOLDER_NAME_REPOSITORIES).listFiles();
                if (children != null) {
                    for (File child : children) {
                        folders.add(new File(child, NutsConstants.FOLDER_NAME_COMPONENTS));
                    }
                }
                NutsUtils.deleteAndConfirmAll(folders.toArray(new File[0]), force);
                return 0;
            }
            case RESET: {
                if(log.isLoggable(Level.SEVERE)) {
                    log.log(Level.SEVERE, message);
                }
                boolean force = false;
                for (String argument : o.getApplicationArguments()) {
                    if ("-f".equals(argument) || "--force".equals(argument)) {
                        force = true;
                    }
                }
                System.out.println("**************");
                System.out.println("** ATTENTION *");
                System.out.println("**************");
                System.out.println("You are about to delete all workspace configuration files.");
                System.out.println("Are you sure this is what you want ??");
                List<File> folders = new ArrayList<>();
                if (workspace != null) {
                    folders.add(new File(conf.getWorkspaceLocation()));
                    for (RootFolderType value : RootFolderType.values()) {
                        folders.add(new File(conf.getStoreRoot(value)));
                    }
                } else {
                    folders.add(new File(Nuts.getDefaultWorkspaceFolder(o.getWorkspace(), o.getHome(), null)));
                    for (RootFolderType value : RootFolderType.values()) {
                        folders.add(new File(Nuts.getDefaultWorkspaceFolder(o.getWorkspace(), o.getHome(), value)));
                    }
                }
                NutsUtils.deleteAndConfirmAll(folders.toArray(new File[0]), force);
                return 0;
            }
        }
        if (workspace != null) {
            if (o.getApplicationArguments().length == 0) {
                workspace.getTerminal().getFormattedOut().println(workspace.getWelcomeText());
                return 0;
            }
            return workspace.createExecBuilder()
                    .setCommand(o.getApplicationArguments())
                    .setExecutorOptions(o.getExecutorOptions())
                    .exec()
                    .getResult();
        }
        System.out.println(message);
        if(log.isLoggable(Level.SEVERE)) {
            log.log(Level.SEVERE, message);
        }
        return 1;
    }

    public static String getActualVersion() {
        return NutsUtils.loadURLProperties(Nuts.class.getResource("/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties"), null).getProperty("project.version", "0.0.0");
    }

    public static String getDefaultNutsHome() {
        if (Boolean.getBoolean("nuts.debug.emulate-windows")) {
            return System.getProperty("user.home") + "\\AppData\\Roaming\\nuts".replace("\\", File.separator);
        }
        switch (NutsUtils.getPlatformOsFamily()) {
            case "windows":
                return System.getProperty("user.home") + "\\AppData\\Roaming\\nuts";
            default:
                return System.getProperty("user.home") + "/.nuts";
        }
    }

    public static String getDefaultWorkspaceFolder(String workspace, String home, RootFolderType folderType) {
        String defaultNutsHome = getDefaultNutsHome();
        if (home == null || home.trim().isEmpty()) {
            home = defaultNutsHome;
        }
        if (workspace == null) {
            workspace = NutsConstants.DEFAULT_WORKSPACE_NAME;
        }
        if (workspace.startsWith(home)) {
            String w = workspace.substring(home.length());
            while (w.startsWith("/") || w.startsWith("\\")) {
                w = w.substring(1);
            }
            workspace = w;
        }
        if (NutsUtils.isAbsolutePath(workspace)) {
            if (folderType == null) {
                //root
                return workspace;
            }
            switch (folderType) {
                case LOGS: {
                    return workspace + File.separator + "logs";
                }
                case VAR: {
                    return workspace + File.separator + "var";
                }
                case CONFIG: {
                    return workspace + File.separator + "config";
                }
                case TEMP: {
                    return workspace + File.separator + "temp";
                }
                case CACHE: {
                    return workspace + File.separator + "cache";
                }
                case PROGRAMS: {
                    return workspace + File.separator + "programs";
                }
                default: {
                    return workspace + File.separator + folderType.name().toLowerCase();
                }
            }
        } else {
            if (defaultNutsHome.equals(home)) {
                if (Boolean.getBoolean("nuts.debug.emulate-windows")) {
                    if (folderType == null) {
                        //root
                        return System.getProperty("user.home") + ("\\AppData\\Local\\nuts\\" + workspace).replace("\\", File.separator);
                    }
                    switch (folderType) {
                        case LOGS: {
                            return System.getProperty("user.home") + ("\\AppData\\Local\\nuts\\" + workspace + "\\logs").replace("\\", File.separator);
                        }
                        case VAR: {
                            return System.getProperty("user.home") + ("\\AppData\\Local\\nuts\\" + workspace + "\\var").replace("\\", File.separator);
                        }
                        case CONFIG: {
                            return System.getProperty("user.home") + ("\\AppData\\Local\\nuts\\" + workspace + "\\config").replace("\\", File.separator);
                        }
                        case TEMP: {
                            return System.getProperty("user.home") + ("\\AppData\\Roaming\\nuts\\" + workspace + "\\temp").replace("\\", File.separator);
                        }
                        case CACHE: {
                            return System.getProperty("user.home") + ("\\AppData\\Roaming\\nuts\\" + workspace + "\\cache").replace("\\", File.separator);
                        }
                        case PROGRAMS: {
                            return System.getProperty("user.home") + ("\\AppData\\Local\\nuts\\" + workspace + "\\programs").replace("\\", File.separator);
                        }
                    }
                    return System.getProperty("user.home") + ("\\AppData\\nuts\\Local\\" + workspace + "\\" + folderType.toString().toLowerCase()).replace("\\", File.separator);
                }
                switch (NutsUtils.getPlatformOsFamily()) {
                    case "windows": {
                        if (folderType == null) {
                            //root
                            return System.getProperty("user.home") + ("\\AppData\\nuts\\Local\\" + workspace);
                        }
                        switch (folderType) {
                            case LOGS: {
                                return System.getProperty("user.home") + ("\\AppData\\nuts\\Roaming\\" + workspace + "\\logs");
                            }
                            case VAR: {
                                return System.getProperty("user.home") + ("\\AppData\\nuts\\Roaming\\" + workspace + "\\var");
                            }
                            case CONFIG: {
                                return System.getProperty("user.home") + ("\\AppData\\nuts\\Roaming\\" + workspace + "\\config");
                            }
                            case TEMP: {
                                return System.getProperty("java.io.tmpdir") + "\\nuts\\" + workspace + "\\temp";
                                //return System.getProperty("user.home") + ("\\AppData\\nuts\\Local\\" + workspace + "\\temp");
                            }
                            case CACHE: {
                                return System.getProperty("user.home") + ("\\AppData\\nuts\\Local\\" + workspace + "\\cache");
                            }
                            case PROGRAMS: {
                                return System.getProperty("user.home") + ("\\AppData\\nuts\\Roaming\\" + workspace + "\\programs");
                            }
                        }
                        return System.getProperty("user.home") + "\\AppData\\nuts\\Local\\" + workspace + "\\" + folderType.name().toLowerCase();
                    }
                    default: {
                        if (folderType == null) {
                            //root
                            return System.getProperty("user.home") + "/.nuts/" + workspace;
                        }
                        switch (folderType) {
                            case LOGS: {
                                return System.getProperty("user.home") + "/.nuts/" + workspace + "/logs";
                            }
                            case VAR: {
                                return System.getProperty("user.home") + "/.nuts/" + workspace + "/var";
                            }
                            case CONFIG: {
                                return System.getProperty("user.home") + "/.nuts/" + workspace + "/config";
                            }
                            case TEMP: {
                                return System.getProperty("java.io.tmpdir") + "/nuts/" + workspace + "/temp";
                            }
                            case CACHE: {
                                return System.getProperty("user.home") + "/.cache/nuts/" + workspace + "/cache";
                            }
                            case PROGRAMS: {
                                return System.getProperty("user.home") + "/.nuts/" + workspace + "/programs";
                            }
                            default: {
                                return System.getProperty("user.home") + "/.nuts/" + workspace + "/" + folderType.name().toLowerCase();
                            }
                        }
                    }
                }
            } else {
                if (folderType == null) {
                    //root
                    return home + File.separator + workspace + File.separator;
                }
                switch (folderType) {
                    case LOGS: {
                        return home + File.separator + workspace + File.separator + "logs";
                    }
                    case VAR: {
                        return home + File.separator + workspace + File.separator + "var";
                    }
                    case CONFIG: {
                        return home + File.separator + workspace + File.separator + "config";
                    }
                    case TEMP: {
                        return home + File.separator + workspace + File.separator + "temp";
                    }
                    case CACHE: {
                        return home + File.separator + workspace + File.separator + "cache";
                    }
                    case PROGRAMS: {
                        return home + File.separator + workspace + File.separator + "programs";
                    }
                    default: {
                        return home + File.separator + workspace + File.separator + folderType.name().toLowerCase();
                    }
                }
            }
        }
    }
}
