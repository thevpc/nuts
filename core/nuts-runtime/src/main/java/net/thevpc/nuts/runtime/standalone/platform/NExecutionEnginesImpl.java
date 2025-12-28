package net.thevpc.nuts.runtime.standalone.platform;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.artifact.NVersionFilter;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNPlatformModel;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.function.Predicate;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class NExecutionEnginesImpl implements NExecutionEngines {
    private NConnectionString connectionString;


    public DefaultNPlatformModel getSdkModel() {
        return NWorkspaceExt.of().getModel().sdkModel;
    }

    public NConnectionString getConnectionString() {
        return connectionString;
    }

    public NExecutionEngines setConnectionString(String connectionString) {
        this.connectionString = NBlankable.isBlank(connectionString) ? null : NConnectionString.of(connectionString);
        return this;
    }

    @Override
    public NExecutionEngines at(String connectionString) {
        return setConnectionString(connectionString);
    }

    @Override
    public NExecutionEngines at(NConnectionString connectionString) {
        return setConnectionString(connectionString);
    }

    @Override
    public NOptional<NExecutionEngineLocation> downloadRemoteExecutionEngine(NExecutionEngineFamily platformFamily, String product, String packaging, String version) {
        if (!NBlankable.isBlank(platformFamily) ) {
            switch (platformFamily) {
                case JAVA: {
                    NExecutionEngineLocation[] e = NJavaSdkUtils.of().searchRemoteLocationsAndInstall(product, NVersion.of(version));
                    if (e.length > 0) {
                        return NOptional.of(e[0]);
                    }
                }
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("%s with version %s", platformFamily == null ? "sdk" : platformFamily.id(), version));
    }

    @Override
    public NExecutionEngines setConnectionString(NConnectionString connectionString) {
        if (!NBlankable.isBlank(connectionString)) {
            this.connectionString = connectionString;
        } else {
            this.connectionString = null;
        }
        return this;
    }


    @Override
    public NStream<NExecutionEngineLocation> findExecutionEngines(NExecutionEngineFamily executionEngineFamily) {
        return getSdkModel().findPlatforms(executionEngineFamily, null);
    }

    @Override
    public boolean addExecutionEngine(NExecutionEngineLocation location) {
        return getSdkModel().addPlatform(location);
    }

    @Override
    public boolean updateExecutionEngine(NExecutionEngineLocation oldLocation, NExecutionEngineLocation newLocation) {
        return getSdkModel().updatePlatform(oldLocation, newLocation);
    }

    @Override
    public boolean removeExecutionEngine(NExecutionEngineLocation location) {
        return getSdkModel().removePlatform(location);
    }

    @Override
    public NOptional<NExecutionEngineLocation> findExecutionEngineByName(NExecutionEngineFamily executionEngineFamily, String locationName) {
        return getSdkModel().findPlatformByName(executionEngineFamily, locationName);
    }

    @Override
    public NOptional<NExecutionEngineLocation> findExecutionEngineByPath(NExecutionEngineFamily executionEngineFamily, NPath path) {
        return getSdkModel().findPlatformByPath(executionEngineFamily, path);
    }

    @Override
    public NOptional<NExecutionEngineLocation> findExecutionEngineByVersion(NExecutionEngineFamily executionEngineFamily, String version) {
        return getSdkModel().findPlatformByVersion(executionEngineFamily, version);
    }

    @Override
    public NOptional<NExecutionEngineLocation> findExecutionEngine(NExecutionEngineLocation location) {
        return getSdkModel().findPlatform(location);
    }

    @Override
    public NOptional<NExecutionEngineLocation> findExecutionEngineByVersion(NExecutionEngineFamily executionEngineFamily, NVersionFilter requestedVersion) {
        return getSdkModel().findPlatformByVersion(executionEngineFamily, requestedVersion);
    }

    @Override
    public NStream<NExecutionEngineLocation> searchHostExecutionEngines(NExecutionEngineFamily platformFamily) {
        return getSdkModel().searchSystemExecutionEngines(platformFamily);
    }

    @Override
    public NStream<NExecutionEngineLocation> searchHostExecutionEngines(NExecutionEngineFamily platformFamily, NPath path) {
        return getSdkModel().searchSystemExecutionEngines(platformFamily, path);
    }

    @Override
    public NOptional<NExecutionEngineLocation> resolveExecutionEngine(NExecutionEngineFamily executionEngineFamily, NPath path, String preferredName) {
        return getSdkModel().resolveExecutionEngine(executionEngineFamily, path, preferredName);
    }

    @Override
    public NOptional<NExecutionEngineLocation> findExecutionEngine(NExecutionEngineFamily executionEngineFamily, Predicate<NExecutionEngineLocation> filter) {
        return getSdkModel().findOneExecutionEngine(executionEngineFamily, filter);
    }

    @Override
    public NStream<NExecutionEngineLocation> findExecutionEngines(NExecutionEngineFamily executionEngineFamily, Predicate<NExecutionEngineLocation> filter) {
        return getSdkModel().findPlatforms(executionEngineFamily, filter);
    }

    @Override
    public NStream<NExecutionEngineLocation> findExecutionEngines() {
        return findExecutionEngines(null, null);
    }

    @Override
    public NExecutionEngines addDefaultExecutionEngines(NExecutionEngineFamily executionEngineFamily) {
        if (executionEngineFamily == NExecutionEngineFamily.JAVA) {
            NWorkspaceUtils.of().installAllJVM();
        }
        return this;
    }

    @Override
    public NExecutionEngines addDefaultExecutionEngine(NExecutionEngineFamily executionEngineFamily) {
        if (executionEngineFamily == NExecutionEngineFamily.JAVA) {
            //at least add current vm
            NWorkspaceUtils.of().installCurrentJVM();
        }
        return this;
    }

}
