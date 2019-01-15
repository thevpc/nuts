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
import java.util.Arrays;

/**
 * Nuts Boot Class
 * Created by vpc on 1/5/17.
 */
public class Nuts {

    /**
     * main method
     * This Main will call System.exit() at completion
     * @param args main arguments
     */
    public static void main(String[] args) {
        try {
            System.exit(uncheckedMain(args));
        } catch (Exception ex) {
            int errorCode = 204;
            boolean showTrace = false;
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-")) {
                    if (args[i].equals("--verbose")) {
                        showTrace = true;
                    }
                } else {
                    break;
                }
            }
            if (ex instanceof NutsExecutionException) {
                NutsExecutionException ex2 = (NutsExecutionException) ex;
                if (ex2.getExitCode() == 0) {
                    System.exit(0);
                    return;
                } else {
                    errorCode = ex2.getExitCode();
                }
            }
            String m = ex.getMessage();
            if (m == null || m.length() < 5) {
                m = ex.toString();
            }
            System.err.println(m);
            if (showTrace) {
                ex.printStackTrace(System.err);
            }
            System.exit(errorCode);
        }
    }

    /**
     * Creates a workspace using "--nuts-boot-args" configuration argument.
     * This method is to be called by child processes of nuts to inherit workspace configuration.
     * @param args arguments
     * @return NutsWorkspace instance
     */
    public static NutsWorkspace openInheritedWorkspace(String[] args) {
        long startTime = System.currentTimeMillis();
        NutsBootWorkspace boot = null;
        if (args.length > 0 && args[0].startsWith("--nuts-boot-args=")) {
            boot = new NutsBootWorkspace(NutsMinimalCommandLine.parseCommandLine(args[0].substring("--nuts-boot-args=".length())));
            boot.getOptions().setApplicationArguments(Arrays.copyOfRange(args, 1, args.length));
        } else {
            String d = System.getProperty("nuts-boot-args");
            if (d != null) {
                boot = new NutsBootWorkspace(NutsMinimalCommandLine.parseCommandLine(d));
                boot.getOptions().setApplicationArguments(args);
            } else {
                NutsWorkspaceOptions t = new NutsWorkspaceOptions();
                t.setApplicationArguments(args);
                boot = new NutsBootWorkspace(t);
            }
        }
        if (boot.isRequiredNewProcess()) {
            throw new IllegalArgumentException("Unable to open a distinct version " + boot.getOptions().getRequiredBootVersion());
        }
        boot.getOptions().setCreationTime(startTime);
        return openWorkspace(boot.getOptions().setCreateIfNotFound(true));
    }

    /**
     * creates a workspace. Nuts Boot arguments are passed in <code>args</code>
     * @param args nuts boot arguments
     * @return new NutsWorkspace instance
     */
    public static NutsWorkspace openWorkspace(String[] args) {
        long startTime = System.currentTimeMillis();
        NutsBootWorkspace boot = new NutsBootWorkspace(args);
        if (boot.isRequiredNewProcess()) {
            throw new IllegalArgumentException("Unable to open a distinct version " + boot.getOptions().getRequiredBootVersion());
        }
        if (boot.getOptions().getCreationTime() == 0) {
            boot.getOptions().setCreationTime(startTime);
        }
        return openWorkspace(boot.getOptions().setCreateIfNotFound(true));
    }

    /**
     * creates a default workspace (no boot options)
     * @return new NutsWorkspace instance
     */
    public static NutsWorkspace openWorkspace() {
        return openWorkspace((NutsWorkspaceOptions) null);
    }

    /**
     * creates a workspace at location <code>workspace</code>
     * @param workspace workspace location
     * @return new NutsWorkspace instance
     */
    public static NutsWorkspace openWorkspace(String workspace) {
        return openWorkspace(new NutsWorkspaceOptions().setWorkspace(workspace));
    }

    /**
     * creates a workspace using the given options
     * @param options boot options
     * @return new NutsWorkspace instance
     */
    public static NutsWorkspace openWorkspace(NutsWorkspaceOptions options) {
        if (options == null) {
            options = new NutsWorkspaceOptions();
        }
        if (options.getCreationTime() == 0) {
            options.setCreationTime(System.currentTimeMillis());
        }
        return new NutsBootWorkspace(options).openWorkspace();
    }

    /**
     * unchecked (may throw exception) main of Nuts application.
     * This Main will never call System.exit()
     * @param args boot arguments
     * @return return code
     * @throws Exception error exception
     */
    public static int uncheckedMain(String[] args) throws Exception {
        //long startTime = System.currentTimeMillis();
        NutsBootWorkspace boot = new NutsBootWorkspace(args);
        return boot.run();
    }

    /**
     * resolves nuts home folder
     * @param folderType
     * @param home
     * @param storeLocationLayout
     * @return
     */
    public static String resolveHomeFolder(NutsStoreFolder folderType, String home, NutsStoreLocationLayout storeLocationLayout) {
        if (folderType == null) {
            folderType = NutsStoreFolder.CONFIG;
        }
        boolean absolute = false;
        if (home == null || home.trim().isEmpty()) {
            home = "";
        } else {
            if (new File(home).isAbsolute()) {
                absolute = true;
            }
        }

        if (storeLocationLayout == null || storeLocationLayout == NutsStoreLocationLayout.SYSTEM) {
            if ("windows".equals(NutsUtils.getPlatformOsFamily())) {
                storeLocationLayout = NutsStoreLocationLayout.WINDOWS;
            } else {
                storeLocationLayout = NutsStoreLocationLayout.LINUX;
            }
        }
        switch (folderType) {
            case LOGS:
            case VAR:
            case CONFIG:
            case PROGRAMS:
            case LIB: {
                if (absolute) {
                    return NutsUtils.syspath(home);
                } else if (home.isEmpty()) {
                    switch (storeLocationLayout) {
                        case WINDOWS:
                            return System.getProperty("user.home") + NutsUtils.syspath("/AppData/Roaming/nuts");
                        case LINUX:
                            return System.getProperty("user.home") + NutsUtils.syspath("/.nuts");
                    }
                } else {
                    switch (storeLocationLayout) {
                        case WINDOWS:
                            return System.getProperty("user.home") + NutsUtils.syspath("/AppData/Roaming/nuts/boot/" + home);
                        case LINUX:
                            return System.getProperty("user.home") + NutsUtils.syspath("/.nuts/boot/" + home);
                    }
                }
                break;
            }
            case CACHE: {
                if (absolute) {
                    return NutsUtils.syspath(home);
                } else if (home.isEmpty()) {
                    switch (storeLocationLayout) {
                        case WINDOWS:
                            return System.getProperty("user.home") + NutsUtils.syspath("/AppData/Local/nuts");
                        case LINUX:
                            return System.getProperty("user.home") + NutsUtils.syspath("/.cache/nuts");
                    }
                } else {
                    switch (storeLocationLayout) {
                        case WINDOWS:
                            return System.getProperty("user.home") + NutsUtils.syspath("/AppData/Local/nuts/boot/" + home);
                        case LINUX:
                            return System.getProperty("user.home") + NutsUtils.syspath("/.cache/nuts/boot/" + home);
                    }
                }
                break;
            }
            case TEMP: {
                switch (storeLocationLayout) {
                    case WINDOWS:
                        //on windows temp folder is user defined
                        return System.getProperty("java.io.tmpdir") + NutsUtils.syspath("/nuts");
                    case LINUX:
                        //on linux temp folder is shared. will add user folder as descriminator
                        return System.getProperty("java.io.tmpdir") + NutsUtils.syspath(("/" + System.getProperty("user.name") + "/nuts"));
                }
            }
        }
        throw new NutsIllegalArgumentException("Unsupported " + storeLocationLayout);
    }

    /**
     * resolves and expands path (folderType) for a given workspace
     * @param folderType store type
     * @param config boot config. Should contain home,workspace, and all StoreLocation information
     * @return resolved folder location
     */
    public static String resolveWorkspaceFolder(NutsStoreFolder folderType, NutsBootConfig config) {
        String workspace=config.getWorkspace();
        String home=Nuts.resolveHomeFolder(NutsStoreFolder.CONFIG, config.getHome(), config.getStoreLocationLayout());
        String path=null;
        switch (folderType){
            case PROGRAMS:{
                path=config.getProgramsStoreLocation();
                break;
            }
            case CACHE:{
                path=config.getCacheStoreLocation();
                break;
            }
            case LOGS:{
                path=config.getLogsStoreLocation();
                break;
            }
            case TEMP:{
                path=config.getTempStoreLocation();
                break;
            }
            case CONFIG:{
                path=config.getConfigStoreLocation();
                break;
            }
            case VAR:{
                path=config.getVarStoreLocation();
                break;
            }
            case LIB:{
                path=config.getLibStoreLocation();
                break;
            }
            default:{
                throw new NutsIllegalArgumentException("Unexpected "+folderType);
            }
        }
        if (path != null && !path.trim().isEmpty() && NutsUtils.isAbsolutePath(path)) {
            return path;
        }
        NutsStoreLocationStrategy storeLocationStrategy=config.getStoreLocationStrategy();
        if (storeLocationStrategy == null) {
            storeLocationStrategy = NutsStoreLocationStrategy.SYSTEM;
        }
        if (home == null || home.trim().isEmpty()) {
            throw new NutsIllegalArgumentException("Missing Home");
        }
        if (workspace == null || workspace.isEmpty()) {
            workspace = NutsConstants.DEFAULT_WORKSPACE_NAME;
        }
        if (workspace.startsWith(home)) {
            String w = workspace.substring(home.length());
            while (w.startsWith("/") || w.startsWith("\\")) {
                w = w.substring(1);
            }
            workspace = w;
        }
        String workspaceName = new File(workspace).getName();
        if (NutsUtils.isAbsolutePath(workspace)) {
            String name = folderType.name().toLowerCase();
            switch (folderType) {
                case LOGS:
                case VAR:
                case CONFIG:
                case PROGRAMS:
                case LIB: {
                    return workspace + File.separator + name;
                }
                case TEMP:
                case CACHE: {
                    if (storeLocationStrategy == NutsStoreLocationStrategy.STANDALONE) {
                        return workspace + File.separator + name;
                    }
                    return home + NutsUtils.syspath("/" + workspaceName + "/" + name);
                }
            }
        } else {
            String name = folderType.name().toLowerCase();
            if (storeLocationStrategy == NutsStoreLocationStrategy.STANDALONE) {
                return home + NutsUtils.syspath("/" + workspaceName + "/" + name);
            }
            return home + NutsUtils.syspath("/" + workspaceName + "/" + name);
        }
        throw new NutsIllegalArgumentException("Unsupported");
    }

}
