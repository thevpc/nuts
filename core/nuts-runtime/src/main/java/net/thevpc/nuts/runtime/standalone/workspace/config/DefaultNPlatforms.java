package net.thevpc.nuts.runtime.standalone.workspace.config;

import java.util.function.Predicate;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStream;

public class DefaultNPlatforms implements NPlatforms {

    private DefaultNPlatformModel model;
    private NSession session;

    public DefaultNPlatforms(NSession session) {
        this.session = session;
        NWorkspace w = this.session.getWorkspace();
        NWorkspaceExt e = (NWorkspaceExt) w;
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
    public NSession getSession() {
        return session;
    }

    @Override
    public NPlatforms setSession(NSession session) {
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
    public NOptional<NPlatformLocation> findPlatformByName(NPlatformFamily platformType, String locationName) {
        checkSession();
        return model.findPlatformByName(platformType, locationName, session);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatformByPath(NPlatformFamily platformType, String path) {
        checkSession();
        return model.findPlatformByPath(platformType, path, session);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatformByVersion(NPlatformFamily platformType, String version) {
        checkSession();
        return model.findPlatformByVersion(platformType, version, session);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatform(NPlatformLocation location) {
        checkSession();
        return model.findPlatform(location, session);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatformByVersion(NPlatformFamily platformType, NVersionFilter requestedVersion) {
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
    public NOptional<NPlatformLocation> resolvePlatform(NPlatformFamily platformType, String path, String preferredName) {
        checkSession();
        return model.resolvePlatform(platformType, path, preferredName, session);
    }

    @Override
    public NOptional<NPlatformLocation> findPlatform(NPlatformFamily type, Predicate<NPlatformLocation> filter) {
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
