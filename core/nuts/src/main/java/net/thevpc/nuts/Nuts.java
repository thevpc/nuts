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
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.boot.*;

import java.util.Map;
import java.util.logging.Level;

/**
 * Nuts Top Class. Nuts is a Package manager for Java Applications and this
 * class is it's main class for creating and opening nuts workspaces.
 *
 * @app.category Base
 * @since 0.1.0
 */
public final class Nuts {

    /**
     * current Nuts version
     */
    public static String version;
    public static String buildNumber;

    /**
     * private constructor
     */
    private Nuts() {
    }

    public static String getBuildNumber() {
        if (buildNumber == null) {
            synchronized (Nuts.class) {
                if (buildNumber == null) {
                    String v = NutsApiUtils.resolveNutsBuildNumber();
                    if (v == null) {
                        throw new NutsBootException(
                                NutsMessage.plain(
                                        "unable to detect nuts build number. Most likely you are missing valid compilation of nuts. pom.properties could not be resolved and hence, we are unable to resolve nuts version."
                                )
                        );
                    }
                    buildNumber = v;
                }
            }
        }
        return buildNumber;
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
                    String v = NutsApiUtils.resolveNutsVersionFromClassPath();
                    if (v == null) {
                        throw new NutsBootException(
                                NutsMessage.plain(
                                        "unable to detect nuts version. Most likely you are missing valid compilation of nuts. pom.properties could not be resolved and hence, we are unable to resolve nuts version."
                                )
                        );
                    }
                    version = v;
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
            NutsSession session = NutsExceptionBase.detectSession(ex);
            NutsWorkspaceOptionsBuilder bo = null;
            if (session != null) {
                bo = session.getWorkspace().env().getBootOptions().builder();
                if (!session.getWorkspace().env().isGraphicalDesktopEnvironment()) {
                    bo.setGui(false);
                }
            } else {
                bo = Nuts.createOptionsBuilder().parseArguments(args);
                try {
                    if (NutsApiUtils.isGraphicalDesktopEnvironment()) {
                        bo.setGui(false);
                    }
                } catch (Exception e) {
                    //exception may occur if the sdk is build without awt package for instance!
                    bo.setGui(false);
                }
            }

            boolean bot = bo.isBot();
            boolean gui = !bot && bo.isGui();
            boolean showTrace = bo.isDebug();
            showTrace |= (bo.getLogConfig() != null
                    && bo.getLogConfig().getLogTermLevel() != null
                    && bo.getLogConfig().getLogTermLevel().intValue() < Level.INFO.intValue());
            if (!showTrace) {
                showTrace = NutsApiUtils.getSysBoolNutsProperty("debug", false);
            }
            if (bot) {
                showTrace = false;
                gui = false;
            }
            System.exit(NutsApiUtils.processThrowable(ex, null, true, showTrace, gui));
        }
    }

    public static void run(NutsSession session, String[] args) {
        NutsWorkspace workspace = session.getWorkspace();
        NutsWorkspaceOptionsBuilder o = createOptionsBuilder().parseArguments(args);
        String[] appArgs;
        if (o.getApplicationArguments().length == 0) {
            if (o.isSkipWelcome()) {
                return;
            }
            appArgs = new String[]{"welcome"};
        } else {
            appArgs = o.getApplicationArguments();
        }
        session.configure(o.build());
        workspace.exec()
                .setSession(session)
                .addCommand(appArgs)
                .addExecutorOptions(o.getExecutorOptions())
                .setExecutionType(o.getExecutionType())
                .setFailFast(true)
                .setDry(session.isDry())
                .run();
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
    public static NutsSession openInheritedWorkspace(String... args) throws NutsUnsatisfiedRequirementsException {
        long startTime = System.currentTimeMillis();
        NutsBootWorkspace boot;
        String nutsWorkspaceOptions = NutsUtilStrings.trim(
                NutsUtilStrings.trim(System.getProperty("nuts.boot.args"))
                        + " " + NutsUtilStrings.trim(System.getProperty("nuts.args"))
        );
        NutsWorkspaceOptionsBuilder options = createOptionsBuilder();
        if (!NutsUtilStrings.isBlank(nutsWorkspaceOptions)) {
            options.parseCommandLine(nutsWorkspaceOptions);
        }
        options.setApplicationArguments(args);
        options.setInherited(true);
        options.setCreationTime(startTime);
        boot = new NutsBootWorkspace(options.build());
        return boot.openWorkspace();// openWorkspace(boot.getOptions());
    }

    /**
     * open a workspace. Nuts Boot arguments are passed in <code>args</code>
     *
     * @param args nuts boot arguments
     * @return new NutsWorkspace instance
     */
    public static NutsSession openWorkspace(String... args) throws NutsUnsatisfiedRequirementsException {
        return new NutsBootWorkspace(args).openWorkspace();
    }

    /**
     * open default workspace (no boot options)
     *
     * @return new NutsWorkspace instance
     */
    public static NutsSession openWorkspace() {
        return openWorkspace((NutsWorkspaceOptions) null);
    }

    /**
     * open a workspace using the given options
     *
     * @param options boot options
     * @return new NutsWorkspace instance
     */
    public static NutsSession openWorkspace(NutsWorkspaceOptions options) {
        return new NutsBootWorkspace(options).openWorkspace();
    }

    public static NutsWorkspaceOptionsBuilder createOptionsBuilder() {
        return NutsApiUtils.createOptionsBuilder();
    }

    public static NutsWorkspaceOptions createOptions() {
        return NutsApiUtils.createOptions();
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
     * on the underlying operating system (linux,windows,...).
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
        return NutsApiUtils.getPlatformHomeFolder(storeLocationLayout, folderType, homeLocations, global, workspaceName);
    }

    /**
     * default OS family, resolvable before booting nuts workspace
     *
     * @return default OS family, resolvable before booting nuts workspace
     */
    public static NutsOsFamily getPlatformOsFamily() {
        return NutsApiUtils.getPlatformOsFamily();
    }
}
