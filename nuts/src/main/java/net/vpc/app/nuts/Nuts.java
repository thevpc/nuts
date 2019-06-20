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

/**
 * Nuts Top Class. Nuts is a Package manager for Java Applications and Nuts
 * class is it's main class for creating and opening nuts workspaces. Created by
 * vpc on 1/5/17.
 *
 * @since 0.1.0
 */
public class Nuts {

    /**
     * Running Nuts version
     */
    public static String version;

    static {
        try {
            version = NutsUtilsLimited.loadURLProperties(
                    Nuts.class.getResource("/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties"),
                    null, false).getProperty("project.version", "0.0.0");
        } catch (Exception ex) {
            version = "0.0.0";
        }
    }

    /**
     * current nuts version
     *
     * @return current nuts version
     */
    public static String getVersion() {
        return version;
    }

    /**
     * main method. This Main will call
     * {@link Nuts#runWorkspace(java.lang.String...)} then
     * {@link System#exit(int)} at completion
     *
     * @param args main arguments
     */
    @SuppressWarnings("UseSpecificCatch")
    public static void main(String[] args) {
        try {
            runWorkspace(args);
            System.exit(0);
        } catch (Exception ex) {
            System.exit(NutsApplications.processThrowable(ex, args, null));
        }
    }

    /**
     * opens a workspace using "nuts.boot.args" and "nut.args" system
     * properties. "nuts.boot.args" is to be passed by nuts parent process.
     * "nuts.args" is an optional property that can be 'exec' method. This
     * method is to be called by child processes of nuts in order to inherit
     * workspace configuration.
     *
     * @param args arguments
     * @return NutsWorkspace instance
     */
    public static NutsWorkspace openInheritedWorkspace(String... args) throws NutsUnsatisfiedRequirementsException {
        long startTime = System.currentTimeMillis();
        NutsBootWorkspace boot;
        String d = NutsUtilsLimited.trim(
                NutsUtilsLimited.trim(System.getProperty("nuts.boot.args"))
                + " " + NutsUtilsLimited.trim(System.getProperty("nuts.args"))
        );
        if (!NutsUtilsLimited.isBlank(d)) {
            boot = new NutsBootWorkspace(NutsUtilsLimited.parseCommandLine(d));
            boot.getOptions().setApplicationArguments(args);
        } else {
            NutsWorkspaceOptions t = new NutsWorkspaceOptions();
            t.setApplicationArguments(args);
            boot = new NutsBootWorkspace(t);
        }
        if (boot.hasUnsatisfiedRequirements()) {
            throw new NutsUnsatisfiedRequirementsException(null, "Unable to open a distinct version " + boot.getOptions().getRequiredBootVersion());
        }
        boot.getOptions().setCreationTime(startTime);
        boot.getOptions().setInherited(true);
        return boot.openWorkspace();// openWorkspace(boot.getOptions());
    }

    /**
     * open a workspace. Nuts Boot arguments are passed in <code>args</code>
     *
     * @param args nuts boot arguments
     * @return new NutsWorkspace instance
     */
    public static NutsWorkspace openWorkspace(String... args) throws NutsUnsatisfiedRequirementsException {
        long startTime = System.currentTimeMillis();
        NutsBootWorkspace boot = new NutsBootWorkspace(args);
        if (boot.hasUnsatisfiedRequirements()) {
            throw new NutsUnsatisfiedRequirementsException(null, "Unable to open a distinct version : " + boot.getRequirementsHelpString(true));
        }
        if (boot.getOptions().getCreationTime() == 0) {
            boot.getOptions().setCreationTime(startTime);
        }
        return boot.openWorkspace();
    }

    /**
     * open default workspace (no boot options)
     *
     * @return new NutsWorkspace instance
     */
    public static NutsWorkspace openWorkspace() {
        return openWorkspace((NutsWorkspaceOptions) null);
    }

    /**
     * open a workspace using the given options
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
     * open then run Nuts application with the provided arguments. This Main
     * will
     * <strong>NEVER</strong>
     * call {@link System#exit(int)}.
     *
     * @param args boot arguments
     */
    public static void runWorkspace(String... args) throws NutsExecutionException {
        //long startTime = System.currentTimeMillis();
        NutsBootWorkspace boot = new NutsBootWorkspace(args);
        boot.run();
    }

}
