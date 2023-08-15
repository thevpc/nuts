package net.thevpc.nuts.runtime.standalone.workspace.config;

import java.util.Map;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.env.NOsFamily;

public class DefaultNLocations implements NLocations {

    private NSession session;
    private DefaultNWorkspaceLocationModel model;

    public DefaultNLocations(NSession session) {
        this.session = session;
        NWorkspace w = this.session.getWorkspace();
        NWorkspaceExt e = (NWorkspaceExt) w;
        this.model = e.getModel().locationsModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NPath getHomeLocation(NStoreType folderType) {
        checkSession();
        return model.getHomeLocation(folderType, session);
    }

    public DefaultNWorkspaceLocationModel getModel() {
        return model;
    }


    @Override
    public NPath getStoreLocation(NStoreType folderType) {
        checkSession();
        return model.getStoreLocation(folderType, session);
    }

    @Override
    public NPath getStoreLocation(NId id, NStoreType folderType) {
        checkSession();
        return model.getStoreLocation(id, folderType, session);
    }

    @Override
    public NPath getStoreLocation(NStoreType folderType, String repositoryIdOrName) {
        checkSession();
        return model.getStoreLocation(folderType, repositoryIdOrName, session);
    }

    @Override
    public NPath getStoreLocation(NId id, NStoreType folderType, String repositoryIdOrName) {
        checkSession();
        return model.getStoreLocation(id, folderType, repositoryIdOrName, session);
    }

    @Override
    public NStoreStrategy getStoreStrategy() {
        checkSession();
        return model.getStoreStrategy(session);
    }

    @Override
    public NStoreStrategy getRepositoryStoreStrategy() {
        checkSession();
        return model.getRepositoryStoreStrategy(session);
    }

    @Override
    public NOsFamily getStoreLayout() {
        checkSession();
        return model.getStoreLayout(session);
    }

    @Override
    public Map<NStoreType, String> getStoreLocations() {
        checkSession();
        return model.getStoreLocations(session);
    }

    @Override
    public String getDefaultIdFilename(NId id) {
        checkSession();
        return model.getDefaultIdFilename(id, session);
    }

    @Override
    public NPath getDefaultIdBasedir(NId id) {
        checkSession();
        return model.getDefaultIdBasedir(id, session);
    }

    @Override
    public String getDefaultIdContentExtension(String packaging) {
        checkSession();
        return model.getDefaultIdContentExtension(packaging, session);
    }

    @Override
    public String getDefaultIdExtension(NId id) {
        checkSession();
        return model.getDefaultIdExtension(id, session);
    }

    @Override
    public Map<NHomeLocation, String> getHomeLocations() {
        checkSession();
        return model.getHomeLocations(session);
    }

    @Override
    public NPath getHomeLocation(NHomeLocation location) {
        checkSession();
        return model.getHomeLocation(location, session);
    }

    @Override
    public NPath getWorkspaceLocation() {
        return model.getWorkspaceLocation();
    }

    @Override
    public NLocations setStoreLocation(NStoreType folderType, String location) {
        checkSession();
        model.setStoreLocation(folderType, location, session);
        return this;
    }

    protected void checkSession() {
        NSessionUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public NLocations setStoreStrategy(NStoreStrategy strategy) {
        checkSession();
        model.setStoreStrategy(strategy, session);
        return this;
    }

    @Override
    public NLocations setStoreLayout(NOsFamily storeLayout) {
        checkSession();
        model.setStoreLayout(storeLayout, session);
        return this;
    }

    @Override
    public NLocations setHomeLocation(NHomeLocation homeType, String location) {
        checkSession();
        model.setHomeLocation(homeType, location, session);
        return this;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NLocations setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

}
