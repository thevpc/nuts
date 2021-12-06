package net.thevpc.nuts.runtime.standalone.workspace.config;

import java.util.function.Predicate;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;

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
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
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
    public NutsPlatformLocation findPlatformByName(NutsPlatformFamily platformType, String locationName) {
        checkSession();
        return model.findPlatformByName(platformType, locationName, session);
    }

    @Override
    public NutsPlatformLocation findPlatformByPath(NutsPlatformFamily platformType, String path) {
        checkSession();
        return model.findPlatformByPath(platformType, path, session);
    }

    @Override
    public NutsPlatformLocation findPlatformByVersion(NutsPlatformFamily platformType, String version) {
        checkSession();
        return model.findPlatformByVersion(platformType, version, session);
    }

    @Override
    public NutsPlatformLocation findPlatform(NutsPlatformLocation location) {
        checkSession();
        return model.findPlatform(location, session);
    }

    @Override
    public NutsPlatformLocation findPlatformByVersion(NutsPlatformFamily platformType, NutsVersionFilter requestedVersion) {
        checkSession();
        return model.findPlatformByVersion(platformType, requestedVersion, session);
    }

    @Override
    public NutsPlatformLocation[] searchSystemPlatforms(NutsPlatformFamily platformType) {
        checkSession();
        return model.searchSystemPlatforms(platformType, session);
    }

    @Override
    public NutsPlatformLocation[] searchSystemPlatforms(NutsPlatformFamily platformType, String path) {
        checkSession();
        return model.searchSystemPlatforms(platformType, path, session);
    }

    @Override
    public NutsPlatformLocation resolvePlatform(NutsPlatformFamily platformType, String path, String preferredName) {
        checkSession();
        return model.resolvePlatform(platformType, path, preferredName, session);
    }

    @Override
    public NutsPlatformLocation findPlatform(NutsPlatformFamily type, Predicate<NutsPlatformLocation> filter) {
        checkSession();
        return model.findOnePlatform(type, filter, session);
    }

    @Override
    public NutsPlatformLocation[] findPlatforms(NutsPlatformFamily type, Predicate<NutsPlatformLocation> filter) {
        checkSession();
        return model.findPlatforms(type, filter, session);
    }

    @Override
    public NutsPlatformLocation[] findPlatforms() {
        return findPlatforms(null,null);
    }

    @Override
    public NutsPlatformLocation[] findPlatforms(NutsPlatformFamily type) {
        checkSession();
        return model.findPlatforms(type, null, session);
    }
}
