package net.thevpc.nuts.runtime.standalone.workspace.config;

import java.util.function.Predicate;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.util.NStream;

public class DefaultNPlatformManager implements NPlatformManager {

    private DefaultNPlatformModel model;
    private NSession session;

    public DefaultNPlatformManager(DefaultNPlatformModel model) {
        this.model = model;
    }

    public DefaultNPlatformModel getModel() {
        return model;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NPlatformManager setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    @Override
    public boolean addPlatform(NPlatformLocation location) {
        checkSession();
        return model.addPlatform(location, session);
    }

    private void checkSession() {
        NSessionUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public boolean updatePlatform(NPlatformLocation oldLocation, NPlatformLocation newLocation) {
        checkSession();
        return model.updatePlatform(oldLocation, newLocation, session);
    }

    @Override
    public boolean removePlatform(NPlatformLocation location) {
        checkSession();
        return model.removePlatform(location, session);
    }

    @Override
    public NPlatformLocation findPlatformByName(NPlatformFamily platformType, String locationName) {
        checkSession();
        return model.findPlatformByName(platformType, locationName, session);
    }

    @Override
    public NPlatformLocation findPlatformByPath(NPlatformFamily platformType, String path) {
        checkSession();
        return model.findPlatformByPath(platformType, path, session);
    }

    @Override
    public NPlatformLocation findPlatformByVersion(NPlatformFamily platformType, String version) {
        checkSession();
        return model.findPlatformByVersion(platformType, version, session);
    }

    @Override
    public NPlatformLocation findPlatform(NPlatformLocation location) {
        checkSession();
        return model.findPlatform(location, session);
    }

    @Override
    public NPlatformLocation findPlatformByVersion(NPlatformFamily platformType, NVersionFilter requestedVersion) {
        checkSession();
        return model.findPlatformByVersion(platformType, requestedVersion, session);
    }

    @Override
    public NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformType) {
        checkSession();
        return model.searchSystemPlatforms(platformType, session);
    }

    @Override
    public NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformType, String path) {
        checkSession();
        return model.searchSystemPlatforms(platformType, path, session);
    }

    @Override
    public NPlatformLocation resolvePlatform(NPlatformFamily platformType, String path, String preferredName) {
        checkSession();
        return model.resolvePlatform(platformType, path, preferredName, session);
    }

    @Override
    public NPlatformLocation findPlatform(NPlatformFamily type, Predicate<NPlatformLocation> filter) {
        checkSession();
        return model.findOnePlatform(type, filter, session);
    }

    @Override
    public NStream<NPlatformLocation> findPlatforms(NPlatformFamily type, Predicate<NPlatformLocation> filter) {
        checkSession();
        return model.findPlatforms(type, filter, session);
    }

    @Override
    public NStream<NPlatformLocation> findPlatforms() {
        return findPlatforms(null,null);
    }

    @Override
    public NStream<NPlatformLocation> findPlatforms(NPlatformFamily type) {
        checkSession();
        return model.findPlatforms(type, null, session);
    }
}
