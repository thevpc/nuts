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

import java.util.Map;

/**
 * Nuts Top Class. Nuts is a Package manager for Java Applications and this class is
 * it's main class for creating and opening nuts workspaces.
 *
 * @since 0.1.0
 */
public final class Nuts {

    /**
     * current Nuts version
     */
    public static String version;

    /**
     * private constructor
     */
    private Nuts() {
    }

    /**
     * current nuts version
     *
     * @return current nuts version
     */
    public static String getVersion() {
        if (version == null) {
            synchronized (Nuts.class) {
                if (version == null) {
                    try {
                        version = PrivateNutsUtils.loadURLProperties(
                                Nuts.class.getResource("/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties"),
                                null, false).getProperty("project.version", "0.0.0");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        version = "0.0.0";
                    }
                    if (version == null || version.trim().isEmpty() || version.equals("0.0.0")) {
                        throw new NutsException(null, "Unable to detect nuts version. Most likely you are missing valid compilation of nuts. nuts.properties could not be resolved and hence, we are unable to resolve nuts version.");
                    }
                }
            }
        }
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
        String nutsWorkspaceOptions = PrivateNutsUtils.trim(
                PrivateNutsUtils.trim(System.getProperty("nuts.boot.args"))
                        + " " + PrivateNutsUtils.trim(System.getProperty("nuts.args"))
        );
        NutsDefaultWorkspaceOptions options;
        if (!PrivateNutsUtils.isBlank(nutsWorkspaceOptions)) {
            options = (NutsDefaultWorkspaceOptions) PrivateNutsArgumentsParser.parseNutsArguments(PrivateNutsCommandLine.parseCommandLineArray(nutsWorkspaceOptions));
        } else {
            options = new NutsDefaultWorkspaceOptions();
        }
        options.setApplicationArguments(args);
        options.setInherited(true);
        options.setCreationTime(startTime);
        boot = new NutsBootWorkspace(options);
        return boot.openWorkspace();// openWorkspace(boot.getOptions());
    }

    /**
     * open a workspace. Nuts Boot arguments are passed in <code>args</code>
     *
     * @param args nuts boot arguments
     * @return new NutsWorkspace instance
     */
    public static NutsWorkspace openWorkspace(String... args) throws NutsUnsatisfiedRequirementsException {
        return new NutsBootWorkspace(args).openWorkspace();
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
        new NutsBootWorkspace(args).runWorkspace();
    }

    /**
     * Create a {@link NutsWorkspaceOptions} instance from string array of valid
     * nuts options
     *
     * @param bootArguments input arguments to parse
     * @return newly created and filled options instance
     */
    public static NutsWorkspaceOptions parseNutsArguments(String[] bootArguments) {
        return PrivateNutsArgumentsParser.parseNutsArguments(bootArguments);
    }

    /**
     * resolves nuts home folder.Home folder is the root for nuts folders.It
     * depends on folder type and store layout. For instance log folder depends
     * on on the underlying operating system (linux,windows,...).
     * Specifications: XDG Base Directory Specification
     * (https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html)
     *
     * @param folderType          folder type to resolve home for
     * @param storeLocationLayout location layout to resolve home for
     * @param homeLocations       workspace home locations
     * @param global              global workspace
     * @param workspaceName       workspace name or id (discriminator)
     * @return home folder path
     */
    public static String getPlatformHomeFolder(
            NutsOsFamily storeLocationLayout,
            NutsStoreLocation folderType,
            Map<String, String> homeLocations,
            boolean global,
            String workspaceName) {
        return PrivateNutsPlatformUtils.getPlatformHomeFolder(storeLocationLayout, folderType, homeLocations, global, workspaceName);
    }

    /**
     * default OS family, resolvable before booting nuts workspace
     * @return default OS family, resolvable before booting nuts workspace
     */
    public static NutsOsFamily getPlatformOsFamily() {
        return PrivateNutsPlatformUtils.getPlatformOsFamily();
    }
}
