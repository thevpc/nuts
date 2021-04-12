package net.thevpc.nuts.runtime.standalone.config;

import java.util.function.Predicate;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsSdkManager implements NutsSdkManager {

    private DefaultNutsSdkModel model;
    private NutsSession session;

    public DefaultNutsSdkManager(DefaultNutsSdkModel model) {
        this.model = model;
    }

    public DefaultNutsSdkModel getModel() {
        return model;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsSdkManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public String[] findSdkTypes() {
        return model.findSdkTypes();
    }

    @Override
    public boolean add(NutsSdkLocation location) {
        checkSession();
        return model.add(location, session);
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public boolean update(NutsSdkLocation oldLocation, NutsSdkLocation newLocation) {
        checkSession();
        return model.update(oldLocation, newLocation, session);
    }

    @Override
    public boolean remove(NutsSdkLocation location) {
        checkSession();
        return model.remove(location, session);
    }

    @Override
    public NutsSdkLocation findByName(String sdkType, String locationName) {
        checkSession();
        return model.findByName(sdkType, locationName, session);
    }

    @Override
    public NutsSdkLocation findByPath(String sdkType, String path) {
        checkSession();
        return model.findByPath(sdkType, path, session);
    }

    @Override
    public NutsSdkLocation findByVersion(String sdkType, String version) {
        checkSession();
        return model.findByVersion(sdkType, version, session);
    }

    @Override
    public NutsSdkLocation find(NutsSdkLocation location) {
        checkSession();
        return model.find(location, session);
    }

    @Override
    public NutsSdkLocation findByVersion(String sdkType, NutsVersionFilter requestedVersion) {
        checkSession();
        return model.findByVersion(sdkType, requestedVersion, session);
    }

    @Override
    public NutsSdkLocation[] searchSystem(String sdkType) {
        checkSession();
        return model.searchSystem(sdkType, session);
    }

    @Override
    public NutsSdkLocation[] searchSystem(String sdkType, String path) {
        checkSession();
        return model.searchSystem(sdkType, path, session);
    }

    @Override
    public NutsSdkLocation resolve(String sdkType, String path, String preferredName) {
        checkSession();
        return model.resolve(sdkType, path, preferredName, session);
    }

    @Override
    public NutsSdkLocation findOne(String type, Predicate<NutsSdkLocation> filter) {
        checkSession();
        return model.findOne(type, filter, session);
    }

    @Override
    public NutsSdkLocation[] find(String type, Predicate<NutsSdkLocation> filter) {
        checkSession();
        return model.find(type, filter, session);
    }

}
