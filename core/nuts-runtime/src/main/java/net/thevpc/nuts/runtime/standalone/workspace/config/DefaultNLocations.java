package net.thevpc.nuts.runtime.standalone.workspace.config;

import java.util.Map;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.env.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class DefaultNLocations implements NLocations {

    private DefaultNWorkspaceLocationModel model;

    public DefaultNLocations(NWorkspace workspace) {
        this.model = NWorkspaceExt.of().getModel().locationsModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NPath getHomeLocation(NStoreType folderType) {
        return model.getHomeLocation(folderType);
    }

    public DefaultNWorkspaceLocationModel getModel() {
        return model;
    }


    @Override
    public NPath getStoreLocation(NStoreType folderType) {
        return model.getStoreLocation(folderType);
    }

    @Override
    public NPath getStoreLocation(NId id, NStoreType folderType) {
        return model.getStoreLocation(id, folderType);
    }

    @Override
    public NPath getStoreLocation(NStoreType folderType, String repositoryIdOrName) {
        return model.getStoreLocation(folderType, repositoryIdOrName);
    }

    @Override
    public NPath getStoreLocation(NId id, NStoreType folderType, String repositoryIdOrName) {
        return model.getStoreLocation(id, folderType, repositoryIdOrName);
    }

    @Override
    public NStoreStrategy getStoreStrategy() {
        return model.getStoreStrategy();
    }

    @Override
    public NStoreStrategy getRepositoryStoreStrategy() {
        return model.getRepositoryStoreStrategy();
    }

    @Override
    public NOsFamily getStoreLayout() {
        return model.getStoreLayout();
    }

    @Override
    public Map<NStoreType, String> getStoreLocations() {
        return model.getStoreLocations();
    }

    @Override
    public String getDefaultIdFilename(NId id) {
        return model.getDefaultIdFilename(id);
    }

    @Override
    public NPath getDefaultIdBasedir(NId id) {
        return model.getDefaultIdBasedir(id);
    }

    @Override
    public String getDefaultIdContentExtension(String packaging) {
        return model.getDefaultIdContentExtension(packaging);
    }

    @Override
    public String getDefaultIdExtension(NId id) {
        return model.getDefaultIdExtension(id);
    }

    @Override
    public Map<NHomeLocation, String> getHomeLocations() {
        return model.getHomeLocations();
    }

    @Override
    public NPath getHomeLocation(NHomeLocation location) {
        return model.getHomeLocation(location);
    }

    @Override
    public NPath getWorkspaceLocation() {
        return model.getWorkspaceLocation();
    }

    @Override
    public NLocations setStoreLocation(NStoreType folderType, String location) {
        model.setStoreLocation(folderType, location);
        return this;
    }


    @Override
    public NLocations setStoreStrategy(NStoreStrategy strategy) {
        model.setStoreStrategy(strategy);
        return this;
    }

    @Override
    public NLocations setStoreLayout(NOsFamily storeLayout) {
        model.setStoreLayout(storeLayout);
        return this;
    }

    @Override
    public NLocations setHomeLocation(NHomeLocation homeType, String location) {
        model.setHomeLocation(homeType, location);
        return this;
    }

}
