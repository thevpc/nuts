package net.thevpc.nuts.runtime.core.filters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsFilterManager implements NutsFilterManager {
    public DefaultNutsFilterModel model;
    public NutsSession session;
    public DefaultNutsFilterManager(DefaultNutsFilterModel model) {
        this.model = model;
    }

    public DefaultNutsFilterModel getModel() {
        return model;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsFilterManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }
    

    @Override
    public <T extends NutsFilter> T nonnull(Class<T> type, NutsFilter filter) {
        checkSession();
        return model.nonnull(type, filter,session);
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(),session);
    }

    @Override
    public <T extends NutsFilter> T always(Class<T> type) {
        checkSession();
        return model.always(type,session);
    }

    @Override
    public <T extends NutsFilter> T never(Class<T> type) {
        checkSession();
        return model.never(type,session);
    }

    @Override
    public <T extends NutsFilter> T all(Class<T> type, NutsFilter... others) {
        checkSession();
        return model.all(type,others,session);
    }

    @Override
    public <T extends NutsFilter> T all(NutsFilter... others) {
        checkSession();
        return model.all(others,session);
    }

    @Override
    public <T extends NutsFilter> T any(Class<T> type, NutsFilter... others) {
        checkSession();
        return model.any(type,others,session);
    }

    @Override
    public <T extends NutsFilter> T not(NutsFilter other) {
        checkSession();
        return model.not(other,session);
    }

    @Override
    public <T extends NutsFilter> T not(Class<T> type, NutsFilter other) {
        checkSession();
        return model.not(type,other,session);
    }

    @Override
    public <T extends NutsFilter> T any(NutsFilter... others) {
        checkSession();
        return model.any(others,session);
    }

    @Override
    public <T extends NutsFilter> T none(Class<T> type, NutsFilter... others) {
        checkSession();
        return model.none(type,others,session);
    }

    @Override
    public <T extends NutsFilter> T none(NutsFilter... others) {
        checkSession();
        return model.none(others,session);
    }

    @Override
    public <T extends NutsFilter> T to(Class<T> toFilterInterface, NutsFilter filter) {
        checkSession();
        return model.to(toFilterInterface,filter,session);
    }

    public <T extends NutsFilter> T as(Class<T> toFilterInterface, NutsFilter filter) {
        checkSession();
        return model.as(toFilterInterface,filter,session);
    }

    @Override
    public Class<? extends NutsFilter> detectType(NutsFilter nutsFilter) {
        checkSession();
        return model.detectType(nutsFilter,session);
    }

    @Override
    public NutsIdFilterManager id() {
//        checkSession();
        return model.id().setSession(session);
    }

    @Override
    public NutsDependencyFilterManager dependency() {
//        checkSession();
        return model.dependency().setSession(session);
    }

    @Override
    public NutsRepositoryFilterManager repository() {
//        checkSession();
        return model.repository().setSession(session);
    }

    @Override
    public NutsVersionFilterManager version() {
//        checkSession();
        return model.version().setSession(session);
    }

    @Override
    public NutsDescriptorFilterManager descriptor() {
//        checkSession();
        return model.descriptor().setSession(session);
    }

    @Override
    public NutsInstallStatusFilterManager installStatus() {
//        checkSession();
        return model.installStatus().setSession(session);
    }

}
