package net.thevpc.nuts.runtime.standalone.config;

import java.util.function.Predicate;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsPlatformManager implements NutsPlatformManager {

    private DefaultNutsPlatformModel model;
    private NutsSession session;

    public DefaultNutsPlatformManager(DefaultNutsPlatformModel model) {
        this.model = model;
    }

    public DefaultNutsPlatformModel getModel() {
        return model;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsPlatformManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public boolean addPlatform(NutsPlatformLocation location) {
        checkSession();
        return model.addPlatform(location, session);
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public boolean updatePlatform(NutsPlatformLocation oldLocation, NutsPlatformLocation newLocation) {
        checkSession();
        return model.updatePlatform(oldLocation, newLocation, session);
    }

    @Override
    public boolean removePlatform(NutsPlatformLocation location) {
        checkSession();
        return model.removePlatform(location, session);
    }

    @Override
    public NutsPlatformLocation findPlatformByName(NutsPlatformType platformType, String locationName) {
        checkSession();
        return model.findPlatformByName(platformType, locationName, session);
    }

    @Override
    public NutsPlatformLocation findPlatformByPath(NutsPlatformType platformType, String path) {
        checkSession();
        return model.findPlatformByPath(platformType, path, session);
    }

    @Override
    public NutsPlatformLocation findPlatformByVersion(NutsPlatformType platformType, String version) {
        checkSession();
        return model.findPlatformByVersion(platformType, version, session);
    }

    @Override
    public NutsPlatformLocation findPlatform(NutsPlatformLocation location) {
        checkSession();
        return model.findPlatform(location, session);
    }

    @Override
    public NutsPlatformLocation findPlatformByVersion(NutsPlatformType platformType, NutsVersionFilter requestedVersion) {
        checkSession();
        return model.findPlatformByVersion(platformType, requestedVersion, session);
    }

    @Override
    public NutsPlatformLocation[] searchSystemPlatforms(NutsPlatformType platformType) {
        checkSession();
        return model.searchSystemPlatforms(platformType, session);
    }

    @Override
    public NutsPlatformLocation[] searchSystemPlatforms(NutsPlatformType platformType, String path) {
        checkSession();
        return model.searchSystemPlatforms(platformType, path, session);
    }

    @Override
    public NutsPlatformLocation resolvePlatform(NutsPlatformType platformType, String path, String preferredName) {
        checkSession();
        return model.resolvePlatform(platformType, path, preferredName, session);
    }

    @Override
    public NutsPlatformLocation findPlatform(NutsPlatformType type, Predicate<NutsPlatformLocation> filter) {
        checkSession();
        return model.findOnePlatform(type, filter, session);
    }

    @Override
    public NutsPlatformLocation[] findPlatforms(NutsPlatformType type, Predicate<NutsPlatformLocation> filter) {
        checkSession();
        return model.findPlatforms(type, filter, session);
    }

    @Override
    public NutsPlatformLocation[] findPlatforms(NutsPlatformType type) {
        checkSession();
        return model.findPlatforms(type, null, session);
    }
}
