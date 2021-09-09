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
    public String[] findPlatformTypes() {
        return model.findPlatformTypes();
    }

    @Override
    public boolean add(NutsPlatformLocation location) {
        checkSession();
        return model.add(location, session);
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public boolean update(NutsPlatformLocation oldLocation, NutsPlatformLocation newLocation) {
        checkSession();
        return model.update(oldLocation, newLocation, session);
    }

    @Override
    public boolean remove(NutsPlatformLocation location) {
        checkSession();
        return model.remove(location, session);
    }

    @Override
    public NutsPlatformLocation findByName(String platformType, String locationName) {
        checkSession();
        return model.findByName(platformType, locationName, session);
    }

    @Override
    public NutsPlatformLocation findByPath(String platformType, String path) {
        checkSession();
        return model.findByPath(platformType, path, session);
    }

    @Override
    public NutsPlatformLocation findByVersion(String platformType, String version) {
        checkSession();
        return model.findByVersion(platformType, version, session);
    }

    @Override
    public NutsPlatformLocation find(NutsPlatformLocation location) {
        checkSession();
        return model.find(location, session);
    }

    @Override
    public NutsPlatformLocation findByVersion(String platformType, NutsVersionFilter requestedVersion) {
        checkSession();
        return model.findByVersion(platformType, requestedVersion, session);
    }

    @Override
    public NutsPlatformLocation[] searchSystem(String platformType) {
        checkSession();
        return model.searchSystem(platformType, session);
    }

    @Override
    public NutsPlatformLocation[] searchSystem(String platformType, String path) {
        checkSession();
        return model.searchSystem(platformType, path, session);
    }

    @Override
    public NutsPlatformLocation resolve(String platformType, String path, String preferredName) {
        checkSession();
        return model.resolve(platformType, path, preferredName, session);
    }

    @Override
    public NutsPlatformLocation findOne(String type, Predicate<NutsPlatformLocation> filter) {
        checkSession();
        return model.findOne(type, filter, session);
    }

    @Override
    public NutsPlatformLocation[] find(String type, Predicate<NutsPlatformLocation> filter) {
        checkSession();
        return model.find(type, filter, session);
    }

    @Override
    public NutsPlatformLocation[] findAll(String type) {
        checkSession();
        return model.find(type, null, session);
    }
}
