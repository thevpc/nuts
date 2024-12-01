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
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.cmdline.NCmdLine;
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
    private static final NVersion version = NVersion.of("0.8.5").get();
    private static final NId id = NId.of(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_API_ARTIFACT_ID, version).get();

    /**
     * private constructor
     */
    private Nuts() {
    }

    /**
     * current nuts version. This is no more loaded from pom file. It is faster
     * and safer to get it as a code constant!
     *
     * @return current nuts version
     */
    public static NVersion getVersion() {
        return version;
    }

    public static NId getApiId() {
        return id;
    }

    /**
     * open a workspace using "nuts.boot.args" and "nut.args" system properties.
     * "nuts.boot.args" is to be passed by nuts parent process. "nuts.args" is
     * an optional property that can be 'exec' method. This method is to be
     * called by child processes of nuts in order to inherit workspace
     * configuration.
     *
     * @param overriddenNutsArgs nuts arguments to override inherited arguments
     * @param appArgs application arguments
     * @return NutsSession instance
     */
    public static NWorkspace openInheritedWorkspace(String[] overriddenNutsArgs, String... appArgs) throws NBootUnsatisfiedRequirementsException {
        return openInheritedWorkspace(null, overriddenNutsArgs, appArgs);
    }

    /**
     * open a workspace using "nuts.boot.args" and "nut.args" system properties.
     * "nuts.boot.args" is to be passed by nuts parent process. "nuts.args" is
     * an optional property that can be 'exec' method. This method is to be
     * called by child processes of nuts in order to inherit workspace
     * configuration.
     *
     * @param term boot terminal or null for defaults
     * @param overriddenNutsArgs nuts arguments to override inherited arguments
     * @param appArgs arguments
     * @return NutsSession instance
     */
    public static NWorkspace openInheritedWorkspace(NWorkspaceTerminalOptions term, String[] overriddenNutsArgs, String... appArgs) throws NBootUnsatisfiedRequirementsException {
        Instant startTime = Instant.now();
        List<String> nutsArgs = new ArrayList<>();
        nutsArgs.addAll(NCmdLine.parseDefault(NStringUtils.trim(System.getProperty("nuts.boot.args"))).get().toStringList());
        nutsArgs.addAll(NCmdLine.parseDefault(NStringUtils.trim(System.getProperty("nuts.args"))).get().toStringList());
        if (overriddenNutsArgs != null) {
            nutsArgs.addAll(Arrays.asList(overriddenNutsArgs));
        }
        if (appArgs != null) {
            nutsArgs.addAll(Arrays.asList(appArgs));
        }
        NBootArguments options = new NBootArguments();
        options.setArgs(nutsArgs.toArray(new String[0]));
        options.setInherited(true);
        options.setStartTime(startTime);
        if (term != null) {
            options.setIn(term.getIn());
            options.setOut(term.getOut());
            options.setErr(term.getErr());
        }
        return (NWorkspace) new NBootWorkspace(options).openWorkspace();
    }

    /**
     * open a workspace. Nuts Boot arguments are passed in <code>args</code>
     *
     * @param args nuts boot arguments
     * @return new NutsSession instance
     */
    public static NWorkspace openWorkspace(String... args) throws NBootUnsatisfiedRequirementsException {
        Instant startTime = Instant.now();
        NBootArguments options = new NBootArguments();
        options.setArgs(args);
        options.setStartTime(startTime);
        return (NWorkspace) new NBootWorkspace(options).openWorkspace();
    }

    /**
     * open a workspace. Nuts Boot arguments are passed in <code>args</code>
     *
     * @param term boot terminal or null for null
     * @param args nuts boot arguments
     * @return new NutsSession instance
     */
    public static NWorkspace openWorkspace(NWorkspaceTerminalOptions term, String... args) throws NBootUnsatisfiedRequirementsException {
        Instant startTime = Instant.now();
        NBootArguments options = new NBootArguments();
        options.setArgs(args);
        options.setStartTime(startTime);
        if (term != null) {
            options.setIn(term.getIn());
            options.setOut(term.getOut());
            options.setErr(term.getErr());
        }
        return (NWorkspace) new NBootWorkspace(options).openWorkspace();
    }

    /**
     * open default workspace (no boot options)
     *
     * @return new NutsSession instance
     */
    public static NWorkspace openWorkspace() {
        return openWorkspace((NWorkspaceOptions) null);
    }

    /**
     * open a workspace using the given options
     *
     * @param options boot options
     * @return new NutsSession instance
     */
    public static NWorkspace openWorkspace(NWorkspaceOptions options) {
        return (NWorkspace) new NBootWorkspace(options==null?null:options.toBootOptionsInfo()).openWorkspace();
    }

    /**
     * <strong>open</strong> then <strong>run</strong> Nuts application with the
     * provided arguments. This Main will <strong>NOT</strong> call
     * {@link System#exit(int)}. Note that if --help or --version are detected
     * in the command line arguments the workspace will not be opened and a null
     * session is returned after displaying help/version information on the
     * standard
     *
     * @param term boot terminal or null for defaults
     * @param args boot arguments
     * @return workspace
     */
    public static NWorkspace runWorkspace(NWorkspaceTerminalOptions term, String... args) throws NExecutionException {
        Instant startTime = Instant.now();
        NBootArguments options = new NBootArguments();
        options.setArgs(args);
        options.setStartTime(startTime);
        if (term != null) {
            options.setIn(term.getIn());
            options.setOut(term.getOut());
            options.setErr(term.getErr());
        }
        return (NWorkspace) new NBootWorkspace(options).runWorkspace();
    }

    /**
     * <strong>open</strong> then <strong>run</strong> Nuts application with the
     * provided arguments. This Main will <strong>NOT</strong> call
     * {@link System#exit(int)}. Not that if --help or --version are detected in
     * the command line arguments the workspace will not be opened and a null
     * session is returned after displaying help/version information on the
     * standard
     *
     * @param args boot arguments
     * @return session
     */
    public static NWorkspace runWorkspace(String... args) throws NExecutionException {
        Instant startTime = Instant.now();
        NBootArguments options = new NBootArguments();
        options.setArgs(args);
        options.setStartTime(startTime);
        return (NWorkspace) new NBootWorkspace(options).runWorkspace();
    }
}
