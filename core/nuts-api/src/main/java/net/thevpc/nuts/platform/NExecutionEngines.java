package net.thevpc.nuts.platform;

import net.thevpc.nuts.artifact.NVersionFilter;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.pipeline.NStream;

import java.util.function.Predicate;

/**
 * NExecutionEngines interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExecutionEngines extends NComponent, NConnectionStringAware {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NExecutionEngines of() {
        return NExtensions.of(NExecutionEngines.class);
    }

    /**
     * Updates the target host connection string.
     * When non-blank, the connection string will be used to connect to a remote host.
     *
     * @param connectionString target host connection string
     * @return this instance for fluent API usage
     */
    @Override
    NExecutionEngines connectionString(String connectionString);

    /**
     * Shortcut to set the connection string for execution.
     *
     * @param connectionString target host connection string
     * @return this instance for fluent API usage
     */
    @Override
    NExecutionEngines at(String connectionString);

    /**
     * Shortcut to set the connection string for execution using a typed object.
     *
     * @param connectionString target host connection object
     * @return this instance for fluent API usage
     */
    @Override
    NExecutionEngines at(NConnectionString connectionString);


    /**
     * Sets the connection string for execution using a typed object.
     *
     * @param connectionString target host connection object
     * @return this instance for fluent API usage
     */
    @Override
    NExecutionEngines connectionString(NConnectionString connectionString);


    /**
     * Adds the specified execution engine.
     *
     * @param location location
     * @return add execution engine result
     */
    boolean addExecutionEngine(NExecutionEngineLocation location);

    /**
     * Update execution engine.
     *
     * @param oldLocation old location
     * @param newLocation new location
     * @return update execution engine result
     */
    boolean updateExecutionEngine(NExecutionEngineLocation oldLocation, NExecutionEngineLocation newLocation);

    /**
     * Removes the specified execution engine.
     *
     * @param location location
     * @return remove execution engine result
     */
    boolean removeExecutionEngine(NExecutionEngineLocation location);

    /**
     * Finds the find execution engine by name.
     *
     * @param executionEngineFamily execution engine family
     * @param locationName location name
     * @return find execution engine by name result
     */
    NOptional<NExecutionEngineLocation> findExecutionEngineByName(NExecutionEngineFamily executionEngineFamily, String locationName);

    /**
     * Finds the find execution engine by path.
     *
     * @param executionEngineFamily execution engine family
     * @param path path
     * @return find execution engine by path result
     */
    NOptional<NExecutionEngineLocation> findExecutionEngineByPath(NExecutionEngineFamily executionEngineFamily, NPath path);

    /**
     * Finds the find execution engine by version.
     *
     * @param executionEngineFamily execution engine family
     * @param version version
     * @return find execution engine by version result
     */
    NOptional<NExecutionEngineLocation> findExecutionEngineByVersion(NExecutionEngineFamily executionEngineFamily, String version);

    /**
     * Finds the find execution engine.
     *
     * @param location location
     * @return find execution engine result
     */
    NOptional<NExecutionEngineLocation> findExecutionEngine(NExecutionEngineLocation location);

    /**
     * Finds the find execution engine by version.
     *
     * @param executionEngineFamily execution engine family
     * @param requestedVersion requested version
     * @return find execution engine by version result
     */
    NOptional<NExecutionEngineLocation> findExecutionEngineByVersion(NExecutionEngineFamily executionEngineFamily, NVersionFilter requestedVersion);

    /**
     * Finds the search host execution engines.
     *
     * @param platformFamily platform family
     * @return search host execution engines result
     */
    NStream<NExecutionEngineLocation> searchHostExecutionEngines(NExecutionEngineFamily platformFamily);

    /**
     * Finds the search host execution engines.
     *
     * @param platformFamily platform family
     * @param path path
     * @return search host execution engines result
     */
    NStream<NExecutionEngineLocation> searchHostExecutionEngines(NExecutionEngineFamily platformFamily, NPath path);

    /**
     * Download remote execution engine.
     *
     * @param platformFamily platform family
     * @param product product
     * @param packaging packaging
     * @param version version
     * @return download remote execution engine result
     */
    NOptional<NExecutionEngineLocation> downloadRemoteExecutionEngine(NExecutionEngineFamily platformFamily, String product, String packaging, String version);

    /**
     * verify if the path is a valid platform path and return null if not
     *
     * @param executionEngineFamily platform type
     * @param path                  platform path
     * @param preferredName         preferredName
     * @return null if not a valid jdk path
     */
    NOptional<NExecutionEngineLocation> resolveExecutionEngine(NExecutionEngineFamily executionEngineFamily, NPath path, String preferredName);

    /**
     * Finds the find execution engine.
     *
     * @param executionEngineFamily execution engine family
     * @param filter filter
     * @return find execution engine result
     */
    NOptional<NExecutionEngineLocation> findExecutionEngine(NExecutionEngineFamily executionEngineFamily, Predicate<NExecutionEngineLocation> filter);

    /**
     * Finds the find execution engines.
     *
     * @param executionEngineFamily execution engine family
     * @param filter filter
     * @return find execution engines result
     */
    NStream<NExecutionEngineLocation> findExecutionEngines(NExecutionEngineFamily executionEngineFamily, Predicate<NExecutionEngineLocation> filter);

    /**
     * Finds the find execution engines.
     *
     * @return find execution engines result
     */
    NStream<NExecutionEngineLocation> findExecutionEngines();

    /**
     * Finds the find execution engines.
     *
     * @param executionEngineFamily execution engine family
     * @return find execution engines result
     */
    NStream<NExecutionEngineLocation> findExecutionEngines(NExecutionEngineFamily executionEngineFamily);

    /**
     * Adds the specified default execution engines.
     *
     * @param executionEngineFamily execution engine family
     * @return add default execution engines result
     */
    NExecutionEngines addDefaultExecutionEngines(NExecutionEngineFamily executionEngineFamily);

    /**
     * Adds the specified default execution engine.
     *
     * @param executionEngineFamily execution engine family
     * @return add default execution engine result
     */
    NExecutionEngines addDefaultExecutionEngine(NExecutionEngineFamily executionEngineFamily);

}
