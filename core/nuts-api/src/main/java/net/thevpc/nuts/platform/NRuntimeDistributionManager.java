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
 * NRuntimeDistributionManager interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NRuntimeDistributionManager extends NComponent, NConnectionStringAware {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NRuntimeDistributionManager of() {
        return NExtensions.of(NRuntimeDistributionManager.class);
    }

    /**
     * Updates the target host connection string.
     * When non-blank, the connection string will be used to connect to a remote host.
     *
     * @param connectionString target host connection string
     * @return this instance for fluent API usage
     */
    @Override
    NRuntimeDistributionManager connectionString(String connectionString);

    /**
     * Shortcut to set the connection string for execution.
     *
     * @param connectionString target host connection string
     * @return this instance for fluent API usage
     */
    @Override
    NRuntimeDistributionManager at(String connectionString);

    /**
     * Shortcut to set the connection string for execution using a typed object.
     *
     * @param connectionString target host connection object
     * @return this instance for fluent API usage
     */
    @Override
    NRuntimeDistributionManager at(NConnectionString connectionString);


    /**
     * Sets the connection string for execution using a typed object.
     *
     * @param connectionString target host connection object
     * @return this instance for fluent API usage
     */
    @Override
    NRuntimeDistributionManager connectionString(NConnectionString connectionString);


    /**
     * Adds the specified execution engine.
     *
     * @param location location
     * @return add execution engine result
     */
    boolean addRuntimeDistribution(NRuntimeDistribution location);

    /**
     * Update execution engine.
     *
     * @param oldLocation old location
     * @param newLocation new location
     * @return update execution engine result
     */
    boolean updateRuntimeDistribution(NRuntimeDistribution oldLocation, NRuntimeDistribution newLocation);

    /**
     * Removes the specified execution engine.
     *
     * @param location location
     * @return remove execution engine result
     */
    boolean removeRuntimeDistribution(NRuntimeDistribution location);

    /**
     * Finds the find execution engine by name.
     *
     * @param distributionFamily execution engine family
     * @param locationName location name
     * @return find execution engine by name result
     */
    NOptional<NRuntimeDistribution> findRuntimeDistributionByName(NRuntimeDistributionFamily distributionFamily, String locationName);

    /**
     * Finds the find execution engine by path.
     *
     * @param distributionFamily execution engine family
     * @param path path
     * @return find execution engine by path result
     */
    NOptional<NRuntimeDistribution> findRuntimeDistributionByPath(NRuntimeDistributionFamily distributionFamily, NPath path);

    /**
     * Finds the find execution engine by version.
     *
     * @param distributionFamily execution engine family
     * @param version version
     * @return find execution engine by version result
     */
    NOptional<NRuntimeDistribution> findRuntimeDistributionByVersion(NRuntimeDistributionFamily distributionFamily, String version);

    /**
     * Finds the find execution engine.
     *
     * @param location location
     * @return find execution engine result
     */
    NOptional<NRuntimeDistribution> findRuntimeDistribution(NRuntimeDistribution location);

    /**
     * Finds the find execution engine by version.
     *
     * @param distributionFamily execution engine family
     * @param requestedVersion requested version
     * @return find execution engine by version result
     */
    NOptional<NRuntimeDistribution> findRuntimeDistributionByVersion(NRuntimeDistributionFamily distributionFamily, NVersionFilter requestedVersion);

    /**
     * Finds the search host execution engines.
     *
     * @param platformFamily platform family
     * @return search host execution engines result
     */
    NStream<NRuntimeDistribution> searchHostRuntimeDistributions(NRuntimeDistributionFamily platformFamily);

    /**
     * Finds the search host execution engines.
     *
     * @param platformFamily platform family
     * @param path path
     * @return search host execution engines result
     */
    NStream<NRuntimeDistribution> searchHostRuntimeDistributions(NRuntimeDistributionFamily platformFamily, NPath path);

    /**
     * Download remote execution engine.
     *
     * @param platformFamily platform family
     * @param product product, engine product. for java this could be jdk or jre
     * @param vendor for java this might include (temurin,corretto,graalvm,oracle,zulu)
     * @param version version
     * @return download remote execution engine result
     */
    NOptional<NRuntimeDistribution> downloadRemoteRuntimeDistribution(NRuntimeDistributionFamily platformFamily, String product, String vendor, String version);

    /**
     * verify if the path is a valid platform path and return null if not
     *
     * @param distributionFamily platform type
     * @param path                  platform path
     * @param preferredName         preferredName
     * @return null if not a valid jdk path
     */
    NOptional<NRuntimeDistribution> resolveRuntimeDistribution(NRuntimeDistributionFamily distributionFamily, NPath path, String preferredName);

    /**
     * Finds the find execution engine.
     *
     * @param distributionFamily execution engine family
     * @param filter filter
     * @return find execution engine result
     */
    NOptional<NRuntimeDistribution> findRuntimeDistribution(NRuntimeDistributionFamily distributionFamily, Predicate<NRuntimeDistribution> filter);

    /**
     * Finds the find execution engines.
     *
     * @param distributionFamily execution engine family
     * @param filter filter
     * @return find execution engines result
     */
    NStream<NRuntimeDistribution> findRuntimeDistributions(NRuntimeDistributionFamily distributionFamily, Predicate<NRuntimeDistribution> filter);

    /**
     * Finds the find execution engines.
     *
     * @return find execution engines result
     */
    NStream<NRuntimeDistribution> findRuntimeDistributions();

    /**
     * Finds the find execution engines.
     *
     * @param distributionFamily execution engine family
     * @return find execution engines result
     */
    NStream<NRuntimeDistribution> findRuntimeDistributions(NRuntimeDistributionFamily distributionFamily);

    /**
     * Adds the specified default execution engines.
     *
     * @param distributionFamily execution engine family
     * @return add default execution engines result
     */
    NRuntimeDistributionManager addDefaultRuntimeDistributions(NRuntimeDistributionFamily distributionFamily);

    /**
     * Adds the specified default execution engine.
     *
     * @param distributionFamily execution engine family
     * @return add default execution engine result
     */
    NRuntimeDistributionManager addDefaultRuntimeDistribution(NRuntimeDistributionFamily distributionFamily);

}
