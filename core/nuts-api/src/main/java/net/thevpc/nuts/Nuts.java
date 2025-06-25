/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
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
import net.thevpc.nuts.util.NOptional;

import java.time.Instant;

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
    private static final NVersion version = NVersion.of("0.8.6");
    private static final NVersion bootVersion = NVersion.of(NBootWorkspace.NUTS_BOOT_VERSION);
    private static final NId id = NId.of(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_API_ARTIFACT_ID, version);

    /**
     * private constructor
     */
    private Nuts() {
    }

    /**
     * current nuts version. This is no more loaded from pom file. It's faster
     * and safer to get it as a code constant!
     *
     * @return current nuts version
     */
    public static NVersion getVersion() {
        return version;
    }

    public static NVersion getBootVersion() {
        return bootVersion;
    }

    public static NId getApiId() {
        return id;
    }

    /**
     * return current context workspace, if none create one
     *
     * @return current context workspace, if none create one and share it
     * @throws NBootUnsatisfiedRequirementsException
     */
    public static NWorkspace require() throws NBootUnsatisfiedRequirementsException {
        NOptional<NWorkspace> w = NWorkspace.get();
        if (w.isPresent()) {
            return w.get();
        }
        return openWorkspace("--reset-options", "--in-memory").share();
    }

    /**
     * open a workspace. Nuts Boot arguments are passed in <code>args</code>
     *
     * @param args nuts boot arguments
     * @return new NSession instance
     */
    public static NWorkspace openWorkspace(String... args) throws NBootUnsatisfiedRequirementsException {
        return openWorkspace(NBootArguments.of(args));
    }


    /**
     * open a workspace. Nuts Boot arguments are passed in <code>args</code>
     *
     * @param args boot args
     * @return new NSession instance
     */
    public static NWorkspace openWorkspace(NBootArguments args) throws NBootUnsatisfiedRequirementsException {
        return (NWorkspace) NBootWorkspace.of(args).getWorkspace();
    }

    /**
     * open default workspace (no boot options)
     *
     * @return new NSession instance
     */
    public static NWorkspace openWorkspace() {
        return openWorkspace((NBootArguments) null);
    }

    /**
     * open a workspace using the given options
     *
     * @param options boot options
     * @return new NSession instance
     */
    public static NWorkspace openWorkspace(NWorkspaceOptions options) {
        return (NWorkspace) NBootWorkspace.of(options == null ? null : options.toBootOptionsInfo()).getWorkspace();
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
        return runWorkspace(NBootArguments.of(args));
    }

    /**
     * open a workspace using the given options
     *
     * @param options boot options
     * @return new NSession instance
     */
    public static NWorkspace runWorkspace(NWorkspaceOptions options) {
        return (NWorkspace) NBootWorkspace.of(options == null ? null : options.toBootOptionsInfo()).runWorkspace().getWorkspace();
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
    public static NWorkspace runWorkspace(NBootArguments args) throws NExecutionException {
        return (NWorkspace) NBootWorkspace.of(args).runWorkspace().getWorkspace();
    }
}
