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

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.NWorkspaceOptions;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for accessing and managing Nuts workspaces.
 * <p>
 * The {@code Nuts} class provides static methods to open, run, or require a {@link NWorkspace}.
 * A workspace defines the execution context for all Nuts operations —
 * including configuration, repositories, installed packages, and runtime environment.
 * </p>
 *
 * <p>
 * There are three primary ways to obtain a workspace, depending on the context:
 * </p>
 *
 * <ul>
 *   <li>
 *     <b>{@link #openWorkspace(NWorkspaceOptions)}</b><br>
 *     Opens or creates a new workspace explicitly using the given options.
 *     This is the standard way for applications to initialize and control their
 *     own Nuts context and lifecycle.
 *     <pre>{@code
 * NWorkspace ws = Nuts.openWorkspace("--workspace=/path/to/ws");
 * }</pre>
 *   </li>
 *
 *   <li>
 *     <b>{@link #require()}</b><br>
 *     Ensures that a workspace exists and makes it the shared global workspace.
 *     If no workspace is currently available, an in-memory one will be created
 *     and shared automatically.
 *     <p>
 *     This method is particularly useful for libraries or scripts that depend on Nuts
 *     (e.g., to parse TSON via NAF) but cannot assume a workspace has been explicitly opened yet.
 *     </p>
 *     <pre>{@code
 * NWorkspace ws = Nuts.require();
 * NElements.of().parse(Tson.parse("...")); // uses shared workspace
 * }</pre>
 *   </li>
 *
 *   <li>
 *     <b>{@code defaultWorkspace()}</b> (planned)<br>
 *     Returns a guaranteed, non-shared workspace — suitable for temporary or
 *     isolated operations that need access to Nuts APIs but should not alter
 *     the global context.
 *     <p>
 *     It can also be used to run code within a controlled workspace scope:
 *     </p>
 *     <pre>{@code
 * Nuts.defaultWorkspace().callWith(() -> {
 *     NElement e = NElements.of().parse(Tson.parse("123"));
 * });
 * }</pre>
 *   </li>
 * </ul>
 *
 * <p>In summary:</p>
 * <pre>
 * openWorkspace()   → explicit, user-managed workspace
 * require()         → global, shared workspace
 * defaultWorkspace()→ transient, isolated workspace (non-shared)
 * </pre>
 *
 * @since 0.8.0
 */
public final class Nuts {

    /**
     * current Nuts version
     */
    private static final NVersion version = NVersion.of("0.8.9");
    private static final NVersion bootVersion = NVersion.of(NBootWorkspace.NUTS_BOOT_VERSION);
    private static final NId id = NId.of(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_API_ARTIFACT_ID, version);
    private static volatile NWorkspace defaultInMemoryWorkspace;

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
     * Ensures that a shared workspace is available.
     * <p>
     * If a workspace is already bound to the current thread, it will be returned.
     * Otherwise, an in-memory workspace will be opened and automatically shared
     * so that subsequent calls to {@link NWorkspace#get()} will return it.
     * </p>
     *
     * <p>
     * This method is intended for scripts and libraries that require access
     * to Nuts APIs (such as NAF or TSON parsing) before any workspace has been
     * explicitly opened.
     * </p>
     *
     * @return the existing or newly created shared workspace
     */
    public static NWorkspace require() throws NBootUnsatisfiedRequirementsException {
        NOptional<NWorkspace> w = NWorkspace.get();
        if (w.isPresent()) {
            return w.get();
        }
        return defaultWorkspace().share();
    }

    /**
     * Returns a guaranteed workspace instance for temporary or isolated use.
     * <p>
     * This method ensures that a workspace exists so that Nuts APIs (like NAF or TSON parsing)
     * can be safely used, even if no workspace has been explicitly opened yet.
     * </p>
     *
     * <p>
     * The workspace returned by this method is <strong>not shared</strong> with the
     * global context. Subsequent calls to {@link NWorkspace#get()} will <em>not</em>
     * return this workspace unless {@link #require()} is called explicitly.
     * </p>
     *
     * <p>
     * This method can be used to temporarily execute code within a controlled workspace scope:
     * </p>
     *
     * <pre>{@code
     * Nuts.defaultWorkspace().callWith(() -> {
     *     NElement e = NElements.of().parse(Tson.parse("123"));
     *     // do something with the temporary workspace
     * });
     * }</pre>
     *
     * <p>
     * After the call completes, the workspace context falls back to its previous state.
     * Internally, the workspace is cached to avoid unnecessary recreation.
     * </p>
     *
     * @return a non-shared, cached workspace instance
     * @throws NBootUnsatisfiedRequirementsException if workspace creation fails
     */
    public static NWorkspace defaultWorkspace() throws NBootUnsatisfiedRequirementsException {
        NOptional<NWorkspace> w = NWorkspace.get();
        if (w.isPresent()) {
            return w.get();
        }
        if(defaultInMemoryWorkspace==null){
            synchronized (Nuts.class) {
                if(defaultInMemoryWorkspace==null) {
                    defaultInMemoryWorkspace = openWorkspace("--reset-options", "--in-memory");
                }
            }
        }
        return defaultInMemoryWorkspace;
    }

    /**
     * Ensures that a shared workspace is available, using the provided boot options if a workspace
     * must be created.
     * <p>
     * If a workspace is already bound to the current thread, it will be returned as-is.
     * Otherwise, a new in-memory workspace will be opened with the specified {@code options} and
     * shared globally, so that subsequent calls to {@link NWorkspace#get()} return it.
     * </p>
     *
     * <p>
     * This method is useful for libraries, scripts, or utilities that require access
     * to Nuts APIs (such as NAF or TSON parsing) and want to influence the workspace
     * creation through boot arguments.
     * </p>
     *
     * <pre>{@code
     * // Example: library code needing a workspace with custom options
     * NWorkspace ws = Nuts.require("--workspace=/tmp/nuts", "--some-flag");
     * NElements.of().parse(Tson.parse("..."));
     * }</pre>
     *
     * <p>
     * Internally, this method always adds "--reset-options" and "--in-memory" to ensure
     * a safe, minimal workspace if none exists.
     * </p>
     *
     * @param options optional boot arguments to configure the workspace
     * @return the existing or newly created shared workspace
     * @throws NBootUnsatisfiedRequirementsException if workspace creation fails
     */
    public static NWorkspace require(String... options) throws NBootUnsatisfiedRequirementsException {
        NOptional<NWorkspace> w = NWorkspace.get();
        if (w.isPresent()) {
            return w.get();
        }
        List<String> newOptions = new ArrayList<>();
        newOptions.add("--reset-options");
        if (options != null && options.length > 0) {
            for (String o : options) {
                if (!NBlankable.isBlank(o)) {
                    newOptions.add(o);
                }
            }
        }
        newOptions.add("--in-memory");
        return openWorkspace(newOptions.toArray(new String[0])).share();
    }

    /**
     * Opens a new workspace using the provided Nuts boot arguments.
     * <p>
     * This method is intended for applications that need to explicitly create
     * and control their workspace lifecycle. The workspace may be persistent
     * or in-memory depending on the arguments supplied.
     * </p>
     *
     * <p>
     * Unlike {@link #require()}, this method does <strong>not</strong> automatically
     * share the workspace globally. Each call returns a new workspace instance
     * unless the boot arguments point to an existing workspace location.
     * </p>
     *
     * <pre>{@code
     * // Example: open a workspace with custom boot arguments
     * NWorkspace ws = Nuts.openWorkspace("--workspace=/path/to/ws", "--some-flag");
     * }</pre>
     *
     * @param args the Nuts boot arguments to configure the workspace
     * @return a new workspace instance corresponding to the provided arguments
     * @throws NBootUnsatisfiedRequirementsException if workspace creation fails
     */
    public static NWorkspace openWorkspace(String... args) throws NBootUnsatisfiedRequirementsException {
        return openWorkspace(NBootArguments.of(args));
    }


    /**
     * Opens a new workspace using the given {@link NBootArguments}.
     * <p>
     * This method is intended for applications that need explicit control over
     * workspace creation and lifecycle. The returned workspace may be persistent
     * or in-memory depending on the boot arguments.
     * </p>
     *
     * <p>
     * Unlike {@link #require()}, this workspace is not automatically shared globally.
     * Each call returns a new workspace instance unless the boot arguments
     * point to an existing workspace location.
     * </p>
     *
     * <pre>{@code
     * NBootArguments args = NBootArguments.of("--workspace=/tmp/nuts", "--some-flag");
     * NWorkspace ws = Nuts.openWorkspace(args);
     * }</pre>
     *
     * @param args the boot arguments to configure the workspace; may be {@code null}
     * @return a new workspace instance corresponding to the provided arguments
     * @throws NBootUnsatisfiedRequirementsException if workspace creation fails
     */
    public static NWorkspace openWorkspace(NBootArguments args) throws NBootUnsatisfiedRequirementsException {
        return (NWorkspace) NBootWorkspace.of(args).getWorkspace();
    }

    /**
     * Opens a new workspace using default settings, with no boot arguments.
     * <p>
     * This method provides a straightforward way for applications to obtain a workspace
     * without specifying any custom configuration. The workspace may be persistent or
     * in-memory depending on the default Nuts behavior.
     * </p>
     *
     * <p>
     * Unlike {@link #require()}, this workspace is not automatically shared globally.
     * Each call returns a new workspace instance.
     * </p>
     *
     * <pre>{@code
     * NWorkspace ws = Nuts.openWorkspace();
     * }</pre>
     *
     * @return a new workspace instance using default options
     */
    public static NWorkspace openWorkspace() {
        return openWorkspace((NBootArguments) null);
    }

    /**
     * Opens a workspace using the given {@link NWorkspaceOptions}.
     * <p>
     * {@link NWorkspaceOptions} provides a high-level, structured way to configure
     * workspace properties such as repository locations, output formatting, isolation level,
     * launcher settings, and other runtime behaviors.
     * </p>
     *
     * <p>
     * This differs from {@link #openWorkspace(NBootArguments)} which is a lower-level API
     * where you pass raw boot arguments (string arrays) directly. Use {@link NBootArguments}
     * when you already have command-line-like arguments, and {@link NWorkspaceOptions}
     * when you want structured programmatic configuration.
     * </p>
     *
     * <p>
     * The returned workspace is a new instance. Unlike {@link #require()},
     * it is not automatically shared globally.
     * </p>
     *
     * <pre>{@code
     * NWorkspaceOptions options = MyWorkspaceOptions.builder()
     *                                                .setName("myWorkspace")
     *                                                .setHomeLocation("/tmp/nuts")
     *                                                .build();
     * NWorkspace ws = Nuts.openWorkspace(options);
     * }</pre>
     *
     * @param options structured workspace options, may be null for defaults
     * @return a new workspace instance configured using the given options
     */
    public static NWorkspace openWorkspace(NWorkspaceOptions options) {
        return (NWorkspace) NBootWorkspace.of(options == null ? null : options.toBootOptionsInfo()).getWorkspace();
    }


    /**
     * <strong>Opens</strong> and <strong>runs</strong> a Nuts workspace with the
     * provided boot arguments.
     * <p>
     * This method will initialize and execute the workspace based on the given arguments.
     * It <strong>does not</strong> call {@link System#exit(int)}.
     * If <code>--help</code> or <code>--version</code> are detected in the arguments,
     * the workspace will not be opened and a <code>null</code> session is returned
     * after displaying the corresponding help or version information on standard output.
     * </p>
     *
     * @param args the boot arguments for the workspace
     * @return the workspace session after execution, or <code>null</code> if help/version was displayed
     * @throws NExecutionException if execution fails
     */
    public static NWorkspace runWorkspace(String... args) throws NExecutionException {
        return runWorkspace(NBootArguments.of(args));
    }

    /**
     * <strong>Opens</strong> and <strong>runs</strong> a Nuts workspace using the
     * provided {@link NWorkspaceOptions}.
     * <p>
     * This method will initialize and execute the workspace based on the given options.
     * It <strong>does not</strong> call {@link System#exit(int)}.
     * </p>
     *
     * @param options the workspace options to configure and run the workspace;
     *                if <code>null</code>, default boot options are used
     * @return the workspace session after execution
     */
    public static NWorkspace runWorkspace(NWorkspaceOptions options) {
        return (NWorkspace) NBootWorkspace.of(options == null ? null : options.toBootOptionsInfo()).runWorkspace().getWorkspace();
    }


    /**
     * <strong>Opens</strong> and <strong>runs</strong> a Nuts workspace/application
     * with the provided {@link NBootArguments}.
     * <p>
     * This method <strong>does not</strong> call {@link System#exit(int)}.
     * If {@code --help} or {@code --version} are detected in the arguments,
     * the workspace will <strong>not</strong> be opened. In such cases, the method
     * will display help or version information on the standard output and
     * return <code>null</code>.
     * </p>
     *
     * @param args the boot arguments to configure and run the workspace
     * @return the workspace session if executed normally, or <code>null</code> if
     *         help/version output was triggered
     * @throws NExecutionException if an error occurs while running the workspace
     */
    public static NWorkspace runWorkspace(NBootArguments args) throws NExecutionException {
        return (NWorkspace) NBootWorkspace.of(args).runWorkspace().getWorkspace();
    }
}
