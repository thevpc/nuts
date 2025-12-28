package net.thevpc.nuts.platform;

import net.thevpc.nuts.artifact.NVersionFilter;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStream;

import java.util.function.Predicate;

public interface NExecutionEngines extends NComponent, NConnectionStringAware {
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
    NExecutionEngines setConnectionString(String connectionString);

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
    NExecutionEngines setConnectionString(NConnectionString connectionString);


    boolean addExecutionEngine(NExecutionEngineLocation location);

    boolean updateExecutionEngine(NExecutionEngineLocation oldLocation, NExecutionEngineLocation newLocation);

    boolean removeExecutionEngine(NExecutionEngineLocation location);

    NOptional<NExecutionEngineLocation> findExecutionEngineByName(NExecutionEngineFamily executionEngineFamily, String locationName);

    NOptional<NExecutionEngineLocation> findExecutionEngineByPath(NExecutionEngineFamily executionEngineFamily, NPath path);

    NOptional<NExecutionEngineLocation> findExecutionEngineByVersion(NExecutionEngineFamily executionEngineFamily, String version);

    NOptional<NExecutionEngineLocation> findExecutionEngine(NExecutionEngineLocation location);

    NOptional<NExecutionEngineLocation> findExecutionEngineByVersion(NExecutionEngineFamily executionEngineFamily, NVersionFilter requestedVersion);

    NStream<NExecutionEngineLocation> searchHostExecutionEngines(NExecutionEngineFamily platformFamily);

    NStream<NExecutionEngineLocation> searchHostExecutionEngines(NExecutionEngineFamily platformFamily, NPath path);

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

    NOptional<NExecutionEngineLocation> findExecutionEngine(NExecutionEngineFamily executionEngineFamily, Predicate<NExecutionEngineLocation> filter);

    NStream<NExecutionEngineLocation> findExecutionEngines(NExecutionEngineFamily executionEngineFamily, Predicate<NExecutionEngineLocation> filter);

    NStream<NExecutionEngineLocation> findExecutionEngines();

    NStream<NExecutionEngineLocation> findExecutionEngines(NExecutionEngineFamily executionEngineFamily);

    NExecutionEngines addDefaultExecutionEngines(NExecutionEngineFamily executionEngineFamily);

    NExecutionEngines addDefaultExecutionEngine(NExecutionEngineFamily executionEngineFamily);

}
