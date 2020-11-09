/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts;

import java.util.Map;

/**
 * Nuts Top Class. Nuts is a Package manager for Java Applications and this class is
 * it's main class for creating and opening nuts workspaces.
 *
 * @since 0.1.0
 * @category Base
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
                                Nuts.class.getResource("/META-INF/nuts/net.thevpc.nuts/nuts/nuts.properties"),
                                null, false,new PrivateNutsLog()).getProperty("project.version", "0.0.0");
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
        NutsWorkspaceOptionsBuilder options= createOptions();
        if (!PrivateNutsUtils.isBlank(nutsWorkspaceOptions)) {
            options.parseCommandLine(nutsWorkspaceOptions);
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

    public static NutsWorkspaceOptionsBuilder createOptions(){
        return new PrivateBootWorkspaceOptions();
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
