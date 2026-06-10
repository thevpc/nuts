package net.thevpc.nuts.ext;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NScored;

import java.util.List;

/**
 * Catalog of Nuts extensions. Provides lookup capabilities to find extension implementations
 * based on a type and a pattern.
 *
 * <p>This component is typically obtained from the Nuts workspace.
 *
 * <p><b>Example usage:</b>
 * <pre>{@code
 * NExtensionCatalog catalog = ...;
 * // Find all compression extensions that handle "tar.gz"
 * List<NScored<NId>> compressors = catalog.findExtensions("net.thevpc.spi.compression", "targz");
 * // Find all path extensions that implement "ssh"
 * List<NScored<NId>> pathExt = catalog.findExtensions("net.thevpc.spi.path", "ssh");
 * }</pre>
 *
 * @see NComponent
 * @see NScored
 */
public interface NExtensionCatalog extends NComponent {
    /**
     * Finds extensions whose type matches the given pattern and returns them sorted
     * with their associated scores.
     *
     * <p>The interpretation of the {@code pattern} depends on the implementation.
     * Common patterns include:
     * <ul>
     *   <li>Exact match (e.g., {@code "ssh"})</li>
     *   <li>Glob (e.g., {@code "tar.*"})</li>
     *   <li>Regular expression (if the implementation supports it)</li>
     * </ul>
     *
     * @param type    the extension type (e.g., {@code "net.thevpc.spi.compression"},
     *                {@code "net.thevpc.spi.path"}, or any logical category)
     * @param pattern the search pattern; may be {@code null} or empty to return all extensions of the given type
     * @return a list of scored extension identifiers, ordered by descending score
     *         (higher score indicates better match or higher priority);
     *         never {@code null}
     */
    List<NScored<NId>> findExtensions(String type, String pattern);
}
