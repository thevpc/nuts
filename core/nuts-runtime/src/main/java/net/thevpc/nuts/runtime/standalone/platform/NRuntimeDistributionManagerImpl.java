package net.thevpc.nuts.runtime.standalone.platform;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.artifact.NVersionFilter;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.pipeline.NStream;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNPlatformModel;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.function.Predicate;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class NRuntimeDistributionManagerImpl implements NRuntimeDistributionManager {
    private NConnectionString connectionString;


    public DefaultNPlatformModel getSdkModel() {
        return NWorkspaceExt.of().getModel().sdkModel;
    }

    public NConnectionString connectionString() {
        return connectionString;
    }

    public NRuntimeDistributionManager connectionString(String connectionString) {
        this.connectionString = NBlankable.isBlank(connectionString) ? null : NConnectionString.of(connectionString);
        return this;
    }

    @Override
    public NRuntimeDistributionManager at(String connectionString) {
        return this.connectionString(connectionString);
    }

    @Override
    public NRuntimeDistributionManager at(NConnectionString connectionString) {
        return connectionString(connectionString);
    }

    @Override
    public NOptional<NRuntimeDistribution> downloadRemoteRuntimeDistribution(NRuntimeDistributionFamily platformFamily, String product, String vendor, String version) {
        if (!NBlankable.isBlank(platformFamily) ) {
            switch (platformFamily) {
                case JAVA: {
                    NRuntimeDistribution[] e = NJavaSdkUtils.of().searchRemoteLocationsAndInstall(product, NVersion.of(version), vendor);
                    if (e.length > 0) {
                        return NOptional.of(e[0]);
                    }
                }
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("%s with version %s", platformFamily == null ? "sdk" : platformFamily.id(), version));
    }

    @Override
    public NRuntimeDistributionManager connectionString(NConnectionString connectionString) {
        if (!NBlankable.isBlank(connectionString)) {
            this.connectionString = connectionString;
        } else {
            this.connectionString = null;
        }
        return this;
    }


    @Override
    public NStream<NRuntimeDistribution> findRuntimeDistributions(NRuntimeDistributionFamily distributionFamily) {
        return getSdkModel().findPlatforms(distributionFamily, null);
    }

    @Override
    public boolean addRuntimeDistribution(NRuntimeDistribution location) {
        return getSdkModel().addPlatform(location);
    }

    @Override
    public boolean updateRuntimeDistribution(NRuntimeDistribution oldLocation, NRuntimeDistribution newLocation) {
        return getSdkModel().updatePlatform(oldLocation, newLocation);
    }

    @Override
    public boolean removeRuntimeDistribution(NRuntimeDistribution location) {
        return getSdkModel().removePlatform(location);
    }

    @Override
    public NOptional<NRuntimeDistribution> findRuntimeDistributionByName(NRuntimeDistributionFamily distributionFamily, String locationName) {
        return getSdkModel().findPlatformByName(distributionFamily, locationName);
    }

    @Override
    public NOptional<NRuntimeDistribution> findRuntimeDistributionByPath(NRuntimeDistributionFamily distributionFamily, NPath path) {
        return getSdkModel().findPlatformByPath(distributionFamily, path);
    }

    @Override
    public NOptional<NRuntimeDistribution> findRuntimeDistributionByVersion(NRuntimeDistributionFamily distributionFamily, String version) {
        return getSdkModel().findPlatformByVersion(distributionFamily, version);
    }

    @Override
    public NOptional<NRuntimeDistribution> findRuntimeDistribution(NRuntimeDistribution location) {
        return getSdkModel().findPlatform(location);
    }

    @Override
    public NOptional<NRuntimeDistribution> findRuntimeDistributionByVersion(NRuntimeDistributionFamily distributionFamily, NVersionFilter requestedVersion) {
        return getSdkModel().findPlatformByVersion(distributionFamily, requestedVersion);
    }

    @Override
    public NStream<NRuntimeDistribution> searchHostRuntimeDistributions(NRuntimeDistributionFamily platformFamily) {
        return getSdkModel().searchSystemRuntimeDistributions(platformFamily);
    }

    @Override
    public NStream<NRuntimeDistribution> searchHostRuntimeDistributions(NRuntimeDistributionFamily platformFamily, NPath path) {
        return getSdkModel().searchSystemRuntimeDistributions(platformFamily, path);
    }

    @Override
    public NOptional<NRuntimeDistribution> resolveRuntimeDistribution(NRuntimeDistributionFamily distributionFamily, NPath path, String preferredName) {
        return getSdkModel().resolveRuntimeDistribution(distributionFamily, path, preferredName);
    }

    @Override
    public NOptional<NRuntimeDistribution> findRuntimeDistribution(NRuntimeDistributionFamily distributionFamily, Predicate<NRuntimeDistribution> filter) {
        return getSdkModel().findOneRuntimeDistribution(distributionFamily, filter);
    }

    @Override
    public NStream<NRuntimeDistribution> findRuntimeDistributions(NRuntimeDistributionFamily distributionFamily, Predicate<NRuntimeDistribution> filter) {
        return getSdkModel().findPlatforms(distributionFamily, filter);
    }

    @Override
    public NStream<NRuntimeDistribution> findRuntimeDistributions() {
        return findRuntimeDistributions(null, null);
    }

    @Override
    public NRuntimeDistributionManager addDefaultRuntimeDistributions(NRuntimeDistributionFamily distributionFamily) {
        if (distributionFamily == NRuntimeDistributionFamily.JAVA) {
            NWorkspaceUtils.of().installAllJVM();
        }
        return this;
    }

    @Override
    public NRuntimeDistributionManager addDefaultRuntimeDistribution(NRuntimeDistributionFamily distributionFamily) {
        if (distributionFamily == NRuntimeDistributionFamily.JAVA) {
            //at least add current vm
            NWorkspaceUtils.of().installCurrentJVM();
        }
        return this;
    }

}
