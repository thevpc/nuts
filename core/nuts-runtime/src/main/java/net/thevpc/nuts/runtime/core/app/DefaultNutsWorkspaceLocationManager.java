package net.thevpc.nuts.runtime.core.app;

import java.util.Map;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsWorkspaceLocationManager implements NutsWorkspaceLocationManager {

    private NutsSession session;
    private DefaultNutsWorkspaceLocationModel model;

    public DefaultNutsWorkspaceLocationManager(DefaultNutsWorkspaceLocationModel model) {
        this.model = model;
    }

    @Override
    public NutsPath getHomeLocation(NutsStoreLocation folderType) {
        checkSession();
        return model.getHomeLocation(folderType,session);
    }

    public DefaultNutsWorkspaceLocationModel getModel() {
        return model;
    }
    

    @Override
    public NutsPath getStoreLocation(NutsStoreLocation folderType) {
        checkSession();
        return model.getStoreLocation(folderType,session);
    }

    @Override
    public NutsPath getStoreLocation(NutsId id, NutsStoreLocation folderType) {
        checkSession();
        return model.getStoreLocation(id,folderType,session);
    }

    @Override
    public NutsPath getStoreLocation(NutsStoreLocation folderType, String repositoryIdOrName) {
        checkSession();
        return model.getStoreLocation(folderType, repositoryIdOrName, session);
    }

    @Override
    public NutsPath getStoreLocation(NutsId id, NutsStoreLocation folderType, String repositoryIdOrName) {
        checkSession();
        return model.getStoreLocation(id, folderType, repositoryIdOrName, session);
    }

    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        checkSession();
        return model.getStoreLocationStrategy(session);
    }

    @Override
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        checkSession();
        return model.getRepositoryStoreLocationStrategy(session);
    }

    @Override
    public NutsOsFamily getStoreLocationLayout() {
        checkSession();
        return model.getStoreLocationLayout(session);
    }

    @Override
    public Map<NutsStoreLocation, String> getStoreLocations() {
        checkSession();
        return model.getStoreLocations(session);
    }

    @Override
    public String getDefaultIdFilename(NutsId id) {
        checkSession();
        return model.getDefaultIdFilename(id,session);
    }

    @Override
    public NutsPath getDefaultIdBasedir(NutsId id) {
        checkSession();
        return model.getDefaultIdBasedir(id,session);
    }

    @Override
    public String getDefaultIdContentExtension(String packaging) {
        checkSession();
        return model.getDefaultIdContentExtension(packaging,session);
    }

    @Override
    public String getDefaultIdExtension(NutsId id) {
        checkSession();
        return model.getDefaultIdExtension(id,session);
    }

    @Override
    public Map<NutsHomeLocation, String> getHomeLocations() {
        checkSession();
        return model.getHomeLocations(session);
    }

    @Override
    public NutsPath getHomeLocation(NutsHomeLocation location) {
        checkSession();
        return model.getHomeLocation(location, session);
    }

    @Override
    public NutsPath getWorkspaceLocation() {
        return model.getWorkspaceLocation();
    }

    @Override
    public NutsWorkspaceLocationManager setStoreLocation(NutsStoreLocation folderType,String location) {
        checkSession();
        model.setStoreLocation(folderType, location, session);
        return this;
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public NutsWorkspaceLocationManager setStoreLocationStrategy(NutsStoreLocationStrategy strategy) {
        checkSession();
        model.setStoreLocationStrategy(strategy, session);
        return this;
    }

    @Override
    public NutsWorkspaceLocationManager setStoreLocationLayout(NutsOsFamily layout) {
        checkSession();
        model.setStoreLocationLayout(layout, session);
        return this;
    }

    @Override
    public NutsWorkspaceLocationManager setHomeLocation(NutsHomeLocation homeType, String location) {
        checkSession();
        model.setHomeLocation(homeType, location, session);
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsWorkspaceLocationManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

}
