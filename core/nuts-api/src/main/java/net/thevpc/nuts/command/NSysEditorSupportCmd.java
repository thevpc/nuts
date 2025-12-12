package net.thevpc.nuts.command;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;

import java.util.Set;

/**
 * Command interface for installing syntax highlighting support across various text editors.
 * <p>
 * This component enables automatic installation of language syntax definitions to supported
 * text editors, making it easier for users to work with custom file formats. It supports
 * major editor families including VSCode, IntelliJ IDEA, Vim, Kate, and others.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Install syntax for all supported editors
 * NSysEditorSupportCmd.of()
 *     .setLanguageId("tson")
 *     .setLanguageName("TSON")
 *     .setFileExtension(".tson")
 *     .setSource(NPath.of("classpath:/integration/tson"))
 *     .addEditorFamilies(NSysEditorFamily.values())
 *     .run();
 *
 * // Install for specific editors only
 * NSysEditorSupportCmd.of()
 *     .setLanguageId("ntexup")
 *     .setLanguageName("NTexUp")
 *     .setFileExtension(".ntx")
 *     .setSource(NPath.of("/path/to/integration"))
 *     .addEditorFamily(NSysEditorFamily.VSCODE)
 *     .addEditorFamily(NSysEditorFamily.VIM)
 *     .run();
 * }</pre>
 *
 * <h2>Source Directory Structure</h2>
 * <p>
 * The source directory should contain subdirectories for each editor family, plus an
 * optional configuration file that defines language metadata:
 * </p>
 * <pre>
 * integration/
 * ├── sys-editor-support.tson  # Optional: language metadata
 * ├── gedit/
 * │   └── language.lang
 * ├── jedit/
 * │   └── language.xml
 * ├── vim/
 * │   └── language.syntax.vim
 * │   └── language.ftdetect.vim
 * ├── vscode/
 * │   ├── language-configuration.json
 * │   └── language.tmLanguage.json
 * ├── intellij/
 * │   └── language.xml
 * ├── notepad-plus-plus/
 * │   └── language.xml
 * └── kate/
 *     └── language.xml
 * </pre>
 *
 * <h2>Configuration Priority</h2>
 * <p>
 * Language properties are resolved in the following order (highest to lowest priority):
 * </p>
 * <ol>
 *   <li><strong>Explicit setters</strong> ({@code setLanguageId()}, {@code setFileName()}, etc.) - Always override</li>
 *   <li><strong>sys-editor-support.tson</strong> - Values from configuration file</li>
 *   <li><strong>Default setters</strong> ({@code setDefaultLanguageId()}, {@code setDefaultFileName()}, etc.) - Only used if not already set</li>
 * </ol>
 *
 * <p>Example sys-editor-support.tson:</p>
 * <pre>{@code
 * {
 *     languageName: "NTexUp",
 *     languageId: "ntexup",
 *     languageVersion: "1.0.0",
 *     languageGroupId: "net.thevpc",
 *     fileName: "*.ntx",
 *     fileExtension: ".ntx"
 * }
 * }</pre>
 *
 * @since 0.8.4
 * @see NSysEditorFamily
 */
public interface NSysEditorSupportCmd extends NCmdLineConfigurable, NComponent {

    /**
     * Creates a new instance of the editor support command.
     *
     * @return a new NSysEditorSupportCmd instance
     */
    static NSysEditorSupportCmd of() {
        return NExtensions.of(NSysEditorSupportCmd.class);
    }

    /**
     * Gets the set of editor families currently configured for installation.
     *
     * @return an immutable set of editor families, never null
     */
    Set<NSysEditorFamily> getEditorFamilies();

    /**
     * Adds a single editor family to the installation target list.
     * <p>
     * If the editor family is already present, this operation has no effect.
     * </p>
     *
     * @param family the editor family to add, must not be null
     * @return this instance for method chaining
     * @throws NullPointerException if family is null
     */
    NSysEditorSupportCmd addEditorFamily(NSysEditorFamily family);

    /**
     * Adds multiple editor families to the installation target list.
     * <p>
     * This is a convenience method equivalent to calling {@link #addEditorFamily(NSysEditorFamily)}
     * for each family. Duplicate families are ignored.
     * </p>
     *
     * @param families the editor families to add, must not be null
     * @return this instance for method chaining
     * @throws NullPointerException if families is null or contains null elements
     */
    NSysEditorSupportCmd addEditorFamilies(NSysEditorFamily... families);

    /**
     * Removes a single editor family from the installation target list.
     * <p>
     * If the editor family is not present, this operation has no effect.
     * </p>
     *
     * @param family the editor family to remove, must not be null
     * @return this instance for method chaining
     * @throws NullPointerException if family is null
     */
    NSysEditorSupportCmd removeEditorFamily(NSysEditorFamily family);

    /**
     * Removes multiple editor families from the installation target list.
     * <p>
     * This is a convenience method equivalent to calling {@link #removeEditorFamily(NSysEditorFamily)}
     * for each family. Non-present families are ignored.
     * </p>
     *
     * @param families the editor families to remove, must not be null
     * @return this instance for method chaining
     * @throws NullPointerException if families is null or contains null elements
     */
    NSysEditorSupportCmd removeEditorFamilies(NSysEditorFamily... families);

    /**
     * Gets the source path containing editor-specific syntax definition files.
     *
     * @return the source path, or null if not set
     */
    NPath getSource();

    /**
     * Sets the source path containing editor-specific syntax definition files.
     * <p>
     * The source should be a directory containing subdirectories for each supported
     * editor family (e.g., "vim/", "vscode/", "intellij/"). Each subdirectory should
     * contain the syntax files in the format expected by that editor.
     * </p>
     *
     * @param source the source path, must not be null
     * @return this instance for method chaining
     * @throws NullPointerException if source is null
     */
    NSysEditorSupportCmd setSource(NPath source);

    /**
     * Sets the unique identifier for the language.
     * <p>
     * This is typically a lowercase, hyphenated identifier (e.g., "tson", "ntexup").
     * This value takes precedence over any languageId defined in sys-editor-support.tson
     * or set via {@link #setDefaultLanguageId(String)}.
     * </p>
     *
     * @param value the language identifier, must not be null or empty
     * @return this instance for method chaining
     */
    NSysEditorSupportCmd setLanguageId(String value);

    /**
     * Sets the default language identifier to use if not defined in sys-editor-support.tson
     * and not explicitly set via {@link #setLanguageId(String)}.
     * <p>
     * This provides a fallback value and will not override values from the configuration file.
     * </p>
     *
     * @param value the default language identifier
     * @return this instance for method chaining
     */
    NSysEditorSupportCmd setDefaultLanguageId(String value);

    /**
     * Sets the human-readable display name for the language.
     * <p>
     * This is the name shown in editor UI elements (e.g., "TSON", "NTexUp").
     * This value takes precedence over any languageName defined in sys-editor-support.tson
     * or set via {@link #setDefaultLanguageName(String)}.
     * </p>
     *
     * @param value the language display name, must not be null or empty
     * @return this instance for method chaining
     */
    NSysEditorSupportCmd setLanguageName(String value);

    /**
     * Sets the default language display name to use if not defined in sys-editor-support.tson
     * and not explicitly set via {@link #setLanguageName(String)}.
     * <p>
     * This provides a fallback value and will not override values from the configuration file.
     * </p>
     *
     * @param value the default language display name
     * @return this instance for method chaining
     */
    NSysEditorSupportCmd setDefaultLanguageName(String value);

    /**
     * Sets the group identifier for the language, typically used for packaging.
     * <p>
     * This follows Maven-style group ID conventions (e.g., "net.thevpc.nuts").
     * This value takes precedence over any languageGroupId defined in sys-editor-support.tson
     * or set via {@link #setDefaultLanguageGroupId(String)}.
     * </p>
     *
     * @param value the language group identifier
     * @return this instance for method chaining
     */
    NSysEditorSupportCmd setLanguageGroupId(String value);

    /**
     * Sets the default group identifier to use if not defined in sys-editor-support.tson
     * and not explicitly set via {@link #setLanguageGroupId(String)}.
     * <p>
     * This provides a fallback value and will not override values from the configuration file.
     * </p>
     *
     * @param value the default language group identifier
     * @return this instance for method chaining
     */
    NSysEditorSupportCmd setDefaultLanguageGroupId(String value);

    /**
     * Sets the version string for the language syntax definition.
     * <p>
     * This is used in some editor configurations (e.g., VSCode extensions).
     * This value takes precedence over any languageVersion defined in sys-editor-support.tson
     * or set via {@link #setDefaultLanguageVersion(String)}.
     * </p>
     *
     * @param value the language version (e.g., "1.0.0")
     * @return this instance for method chaining
     */
    NSysEditorSupportCmd setLanguageVersion(String value);

    /**
     * Sets the default version string to use if not defined in sys-editor-support.tson
     * and not explicitly set via {@link #setLanguageVersion(String)}.
     * <p>
     * This provides a fallback value and will not override values from the configuration file.
     * </p>
     *
     * @param value the default language version
     * @return this instance for method chaining
     */
    NSysEditorSupportCmd setDefaultLanguageVersion(String value);

    /**
     * Sets the primary file extension for files in this language.
     * <p>
     * Should include the leading dot (e.g., ".tson", ".ntx"). This can be used alongside
     * {@link #setFileName(String)} as both patterns can be active simultaneously.
     * This value takes precedence over any fileExtension defined in sys-editor-support.tson
     * or set via {@link #setDefaultFileExtension(String)}.
     * </p>
     *
     * @param value the file extension including the dot, must not be null or empty
     * @return this instance for method chaining
     */
    NSysEditorSupportCmd setFileExtension(String value);

    /**
     * Sets the default file extension to use if not defined in sys-editor-support.tson
     * and not explicitly set via {@link #setFileExtension(String)}.
     * <p>
     * This provides a fallback value and will not override values from the configuration file.
     * </p>
     *
     * @param value the default file extension including the dot
     * @return this instance for method chaining
     */
    NSysEditorSupportCmd setDefaultFileExtension(String value);

    /**
     * Sets a specific file name pattern for this language (e.g., "Makefile", "*.ntx", ".bashrc").
     * <p>
     * This is used when the language applies to specific file name patterns. Can be used
     * alongside {@link #setFileExtension(String)} as both patterns can be active simultaneously.
     * This value takes precedence over any fileName defined in sys-editor-support.tson
     * or set via {@link #setDefaultFileName(String)}.
     * </p>
     *
     * @param value the file name pattern
     * @return this instance for method chaining
     */
    NSysEditorSupportCmd setFileName(String value);

    /**
     * Sets the default file name pattern to use if not defined in sys-editor-support.tson
     * and not explicitly set via {@link #setFileName(String)}.
     * <p>
     * This provides a fallback value and will not override values from the configuration file.
     * </p>
     *
     * @param value the default file name pattern
     * @return this instance for method chaining
     */
    NSysEditorSupportCmd setDefaultFileName(String value);

    /**
     * Executes the syntax installation command.
     * <p>
     * This method reads the syntax definition files from the configured source path
     * and installs them to the appropriate configuration directories for each selected
     * editor family. The installation process:
     * </p>
     * <ul>
     *   <li>Detects which editors are installed on the system</li>
     *   <li>Creates necessary configuration directories if they don't exist</li>
     *   <li>Copies syntax definition files to the correct locations</li>
     *   <li>Updates editor-specific configuration files (e.g., jEdit catalog, VSCode package.json)</li>
     *   <li>Provides feedback on successful installations and any skipped editors</li>
     * </ul>
     *
     * @return this instance for potential further configuration
     * @throws net.thevpc.nuts.util.NIllegalArgumentException if required configuration is missing
     */
    NSysEditorSupportCmd run();
}