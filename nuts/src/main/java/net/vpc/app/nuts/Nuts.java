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
 * Nuts Boot Class Created by vpc on 1/5/17.
 */
public class Nuts {

    public static String version;

    static {
        try {
            version = NutsUtils.loadURLProperties(Nuts.class.getResource("/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties"), null, false).getProperty("project.version", "0.0.0");
        } catch (Exception ex) {
            version = "0.0.0";
            //
        }
    }

    public static String getVersion() {
        return version;
    }

    /**
     * main method. 
     * This Main will call System.exit() at completion
     *
     * @param args main arguments
     */
    public static void main(String[] args) {
        try {
            runWorkspace(args);
            System.exit(0);
        } catch (Exception ex) {
            int errorCode = NutsExecutionException.DEFAULT_ERROR_EXIT_CODE;
            boolean showTrace = false;
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-")) {
                    if (args[i].equals("--verbose") || args[i].equals("--debug")) {
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
     * opens a workspace using "--nuts-boot-args" configuration argument. This
     * method is to be called by child processes of nuts to inherit workspace
     * configuration.
     *
     * @param args arguments
     * @return NutsWorkspace instance
     */
    public static NutsWorkspace openInheritedWorkspace(String... args) throws NutsUnsatisfiedRequirementsExeption {
        long startTime = System.currentTimeMillis();
//        System.out.println("OPEN INHERITED : "+NutsMinimalCommandLine.escapeArguments(args));
        NutsBootWorkspace boot = null;
        if (args.length > 0 && args[0].startsWith("--nuts-boot-args=")) {
//            System.out.println("OPEN INHERITED : GOT FROM ARGS : "+args[0]);
            boot = new NutsBootWorkspace(NutsMinimalCommandLine.parseCommandLine(args[0].substring("--nuts-boot-args=".length())));
            boot.getOptions().setApplicationArguments(Arrays.copyOfRange(args, 1, args.length));
        } else {
            String d = System.getProperty("nuts.export.boot.args");
            if (d != null) {
//                System.out.println("OPEN INHERITED : GOT FROM PROPS : -Dnuts.export.boot.args="+d);
                boot = new NutsBootWorkspace(NutsMinimalCommandLine.parseCommandLine(d));
                boot.getOptions().setApplicationArguments(args);
            } else {
//                System.out.println("OPEN INHERITED : NO PARAMS");
                NutsWorkspaceOptions t = new NutsWorkspaceOptions();
                t.setApplicationArguments(args);
                boot = new NutsBootWorkspace(t);
            }
        }
        if (boot.hasUnsatisfiedRequirements()) {
            throw new NutsUnsatisfiedRequirementsExeption("Unable to open a distinct version " + boot.getOptions().getRequiredBootVersion());
        }
        boot.getOptions().setCreationTime(startTime);
        return boot.openWorkspace();// openWorkspace(boot.getOptions());
    }

    /**
     * creates a workspace. Nuts Boot arguments are passed in <code>args</code>
     *
     * @param args nuts boot arguments
     * @return new NutsWorkspace instance
     */
    public static NutsWorkspace openWorkspace(String... args) throws NutsUnsatisfiedRequirementsExeption {
        long startTime = System.currentTimeMillis();
        NutsBootWorkspace boot = new NutsBootWorkspace(args);
        if (boot.hasUnsatisfiedRequirements()) {
            throw new NutsUnsatisfiedRequirementsExeption("Unable to open a distinct version : " + boot.getRequirementsHelpString(true));
        }
        if (boot.getOptions().getCreationTime() == 0) {
            boot.getOptions().setCreationTime(startTime);
        }
        return boot.openWorkspace();
    }

    /**
     * creates a default workspace (no boot options)
     *
     * @return new NutsWorkspace instance
     */
    public static NutsWorkspace openWorkspace() {
        return openWorkspace((NutsWorkspaceOptions) null);
    }

    /**
     * opens a workspace using the given options
     *
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
     * runs Nuts application with the provided arguments This Main will never
     * call System.exit()
     *
     * @param args boot arguments
     */
    public static void runWorkspace(String... args) throws NutsExecutionException {
        //long startTime = System.currentTimeMillis();
        NutsBootWorkspace boot = new NutsBootWorkspace(args);
        boot.run();
    }

}
