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

import net.thevpc.nuts.boot.DefaultNWorkspaceOptionsBuilder;
import net.thevpc.nuts.boot.NBootWorkspace;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.reserved.NReservedBootLog;
import net.thevpc.nuts.util.NApiUtils;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Nuts Top Class. Nuts is a Package manager for Java Applications and this
 * class is its main class for creating and opening nuts workspaces.
 *
 * @author thevpc
 * @app.category Base
 * @since 0.1.0
 */
public final class Nuts {

    /**
     * current Nuts version
     */
    public static NVersion version;

    /**
     * private constructor
     */
    private Nuts() {
    }

    /**
     * current nuts version, loaded from pom file
     *
     * @return current nuts version
     */
    public static NVersion getVersion() {
        if (version == null) {
            synchronized (Nuts.class) {
                if (version == null) {
                    String v = NApiUtils.resolveNutsVersionFromClassPath(new NReservedBootLog(null));
                    if (v == null) {
                        throw new NBootException(
                                NMsg.ofPlain(
                                        "unable to detect nuts version. Most likely you are missing valid compilation of nuts. pom.properties could not be resolved and hence, we are unable to resolve nuts version."
                                )
                        );
                    }
                    version = NVersion.of(v).get();
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
    public static void main(String[] args)  throws Throwable{
        try {
            runWorkspace(args);
            System.exit(0);
        } catch (Exception ex) {
            NSession session = NSessionAwareExceptionBase.resolveSession(ex).orNull();
            if (session != null) {
                System.exit(NApplicationExceptionHandler.of(session)
                        .processThrowable(args, ex));
            } else {
                System.exit(NApiUtils.processThrowable(ex, args));
            }
        }
    }

    /**
     * open a workspace using "nuts.boot.args" and "nut.args" system
     * properties. "nuts.boot.args" is to be passed by nuts parent process.
     * "nuts.args" is an optional property that can be 'exec' method. This
     * method is to be called by child processes of nuts in order to inherit
     * workspace configuration.
     *
     * @param overriddenNutsArgs nuts arguments to override inherited arguments
     * @param appArgs            application arguments
     * @return NutsSession instance
     */
    public static NSession openInheritedWorkspace(String[] overriddenNutsArgs, String... appArgs) throws NUnsatisfiedRequirementsException {
        return openInheritedWorkspace(null, overriddenNutsArgs, appArgs);
    }

    /**
     * open a workspace using "nuts.boot.args" and "nut.args" system
     * properties. "nuts.boot.args" is to be passed by nuts parent process.
     * "nuts.args" is an optional property that can be 'exec' method. This
     * method is to be called by child processes of nuts in order to inherit
     * workspace configuration.
     *
     * @param term               boot terminal or null for defaults
     * @param overriddenNutsArgs nuts arguments to override inherited arguments
     * @param appArgs            arguments
     * @return NutsSession instance
     */
    public static NSession openInheritedWorkspace(NWorkspaceTerminalOptions term, String[] overriddenNutsArgs, String... appArgs) throws NUnsatisfiedRequirementsException {
        Instant startTime = Instant.now();
        List<String> nutsArgs = new ArrayList<>();
        nutsArgs.addAll(NCmdLine.parseDefault(NStringUtils.trim(System.getProperty("nuts.boot.args"))).get().toStringList());
        nutsArgs.addAll(NCmdLine.parseDefault(NStringUtils.trim(System.getProperty("nuts.args"))).get().toStringList());
        if (overriddenNutsArgs != null) {
            nutsArgs.addAll(Arrays.asList(overriddenNutsArgs));
        }
        NWorkspaceOptionsBuilder options = new DefaultNWorkspaceOptionsBuilder();
        options.setCmdLine(nutsArgs.toArray(new String[0]), null);
        if (options.getApplicationArguments().isNotPresent()) {
            options.setApplicationArguments(new ArrayList<>());
        }
        options.getApplicationArguments().get().addAll(Arrays.asList(appArgs));
        options.setApplicationArguments(Arrays.asList(appArgs));
        options.setInherited(true);
        options.setCreationTime(startTime);
        if (term != null) {
            options.setStdin(term.getIn());
            options.setStdout(term.getOut());
            options.setStderr(term.getErr());
        }
        return new NBootWorkspace(options).openWorkspace();
    }

    /**
     * open a workspace. Nuts Boot arguments are passed in <code>args</code>
     *
     * @param args nuts boot arguments
     * @return new NutsSession instance
     */
    public static NSession openWorkspace(String... args) throws NUnsatisfiedRequirementsException {
        return new NBootWorkspace(null, args).openWorkspace();
    }

    /**
     * open a workspace. Nuts Boot arguments are passed in <code>args</code>
     *
     * @param term boot terminal or null for null
     * @param args nuts boot arguments
     * @return new NutsSession instance
     */
    public static NSession openWorkspace(NWorkspaceTerminalOptions term, String... args) throws NUnsatisfiedRequirementsException {
        return new NBootWorkspace(term, args).openWorkspace();
    }

    /**
     * open default workspace (no boot options)
     *
     * @return new NutsSession instance
     */
    public static NSession openWorkspace() {
        return openWorkspace((NWorkspaceOptions) null);
    }

    /**
     * open a workspace using the given options
     *
     * @param options boot options
     * @return new NutsSession instance
     */
    public static NSession openWorkspace(NWorkspaceOptions options) {
        return new NBootWorkspace(options).openWorkspace();
    }

    /**
     * open then run Nuts application with the provided arguments. This Main
     * will <strong>NEVER</strong> call {@link System#exit(int)}.
     * Not that if --help or --version are detected in the command line arguments
     * the workspace will not be opened and a null session is returned after displaying
     * help/version information on the standard
     *
     * @param term boot terminal or null for defaults
     * @param args boot arguments
     * @return session
     */
    public static NSession runWorkspace(NWorkspaceTerminalOptions term, String... args) throws NExecutionException {
        return new NBootWorkspace(term, args).runWorkspace();
    }

    /**
     * open then run Nuts application with the provided arguments. This Main
     * will <strong>NEVER</strong> call {@link System#exit(int)}.
     * Not that if --help or --version are detected in the command line arguments
     * the workspace will not be opened and a null session is returned after displaying
     * help/version information on the standard
     *
     * @param args boot arguments
     * @return session
     */
    public static NSession runWorkspace(String... args) throws NExecutionException {
        return new NBootWorkspace(null, args).runWorkspace();
    }
}
