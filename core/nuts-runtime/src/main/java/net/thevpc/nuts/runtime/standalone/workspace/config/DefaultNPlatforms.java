package net.thevpc.nuts.runtime.standalone.workspace.config;

import java.util.function.Predicate;
import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.env.NPlatformFamily;
import net.thevpc.nuts.util.NStream;

public class DefaultNPlatforms implements NPlatforms {

    private DefaultNPlatformModel model;
    private NWorkspace workspace;

    public DefaultNPlatforms(NWorkspace workspace) {
        this.workspace = workspace;
        NWorkspaceExt e = (NWorkspaceExt) workspace;
        this.model = e.getModel().sdkModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    public DefaultNPlatformModel getModel() {
        return model;
    }


    @Override
    public boolean addPlatform(NPlatformLocation location) {
        return model.addPlatform(location);
    }

    @Override
    public boolean updatePlatform(NPlatformLocation oldLocation, NPlatformLocation newLocation) {
        return model.updatePlatform(oldLocation, newLocation);
    }

    @Override
    public boolean removePlatform(NPlatformLocation location) {
        return model.removePlatform(location);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatformByName(NPlatformFamily platformType, String locationName) {
        return model.findPlatformByName(platformType, locationName);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatformByPath(NPlatformFamily platformType, NPath path) {
        return model.findPlatformByPath(platformType, path);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatformByVersion(NPlatformFamily platformType, String version) {
        return model.findPlatformByVersion(platformType, version);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatform(NPlatformLocation location) {
        return model.findPlatform(location);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatformByVersion(NPlatformFamily platformType, NVersionFilter requestedVersion) {
        return model.findPlatformByVersion(platformType, requestedVersion);
    }

    @Override
    public NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformFamily) {
        return model.searchSystemPlatforms(platformFamily);
    }

    @Override
    public NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformFamily, NPath path) {
        return model.searchSystemPlatforms(platformFamily, path);
    }

    @Override
    public NOptional<NPlatformLocation> resolvePlatform(NPlatformFamily platformFamily, NPath path, String preferredName) {
        return model.resolvePlatform(platformFamily, path, preferredName);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatform(NPlatformFamily platformFamily, Predicate<NPlatformLocation> filter) {
        return model.findOnePlatform(platformFamily, filter);
    }

    @Override
    public NStream<NPlatformLocation> findPlatforms(NPlatformFamily platformFamily, Predicate<NPlatformLocation> filter) {
        return model.findPlatforms(platformFamily, filter);
    }

    @Override
    public NStream<NPlatformLocation> findPlatforms() {
        return findPlatforms(null,null);
    }

    @Override
    public void addDefaultPlatforms(NPlatformFamily type) {
        if(type==NPlatformFamily.JAVA) {
            NWorkspaceUtils.of(workspace).installAllJVM();
        }
    }

    @Override
    public void addDefaultPlatform(NPlatformFamily type) {
        if(type==NPlatformFamily.JAVA) {
            //at least add current vm
            NWorkspaceUtils.of(workspace).installCurrentJVM();
        }
    }

    @Override
    public NStream<NPlatformLocation> findPlatforms(NPlatformFamily type) {
        return model.findPlatforms(type, null);
    }
}
