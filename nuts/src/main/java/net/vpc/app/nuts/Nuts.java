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
     * opens a workspace using "--nuts-boot-args" configuration argument.
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
        return openWorkspace(boot.getOptions());
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
        return openWorkspace(boot.getOptions());
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
     * opens a workspace using the given options
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
     * resolves nuts home folder. Home folder is the root for nuts folders. It depends on folder type and store layout.
     * For instance log folder type is stored for windows and linux in distinct folders.
     * @param folderType folder type to resolve home for
     * @param storeLocationLayout location layout to resolve home for
     * @return home folder path
     */
    public static String resolveHomeFolder(NutsStoreFolder folderType, NutsStoreLocationLayout storeLocationLayout) {
        if (folderType == null) {
            folderType = NutsStoreFolder.CONFIG;
        }
        boolean wasSystem=false;
        if (storeLocationLayout == null || storeLocationLayout == NutsStoreLocationLayout.SYSTEM) {
            wasSystem=true;
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
                switch (storeLocationLayout) {
                    case WINDOWS:
                        return System.getProperty("user.home") + NutsUtils.syspath("/AppData/Roaming/nuts");
                    case LINUX:
                        return System.getProperty("user.home") + NutsUtils.syspath("/.nuts");
                }
                break;
            }
            case CACHE: {
                switch (storeLocationLayout) {
                    case WINDOWS:
                        return System.getProperty("user.home") + NutsUtils.syspath("/AppData/Local/nuts");
                    case LINUX:
                        return System.getProperty("user.home") + NutsUtils.syspath("/.cache/nuts");
                }
                break;
            }
            case TEMP: {
                switch (storeLocationLayout) {
                    case WINDOWS:
                        if(NutsUtils.getPlatformOsFamily().equals("windows")) {
                            //on windows temp folder is user defined
                            return System.getProperty("java.io.tmpdir") + NutsUtils.syspath("/nuts");
                        }else{
                            return System.getProperty("user.home") + NutsUtils.syspath("/AppData/Local/nuts");
                        }
                    case LINUX:
                        if(!NutsUtils.getPlatformOsFamily().equals("windows")) {
                            //on linux temp folder is shared. will add user folder as discriminator
                            return System.getProperty("java.io.tmpdir") + NutsUtils.syspath(("/" + System.getProperty("user.name") + "/nuts"));
                        }else{
                            return System.getProperty("user.home") + NutsUtils.syspath("/tmp/nuts");
                        }
                }
            }
        }
        throw new NutsIllegalArgumentException("Unsupported " + storeLocationLayout);
    }



}
