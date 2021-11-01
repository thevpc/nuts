package net.thevpc.nuts.runtime.core.filters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class DefaultNutsFilters implements NutsFilters {
    public DefaultNutsFilterModel model;
    public NutsSession session;
    public DefaultNutsFilters(NutsSession session) {
        this.model = NutsWorkspaceExt.of(session.getWorkspace()).getModel().filtersModel;
        this.session=session;
    }

    public DefaultNutsFilterModel getModel() {
        return model;
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
    public int getSupportLevel(NutsSupportLevelContext<Object> context) {
        return DEFAULT_SUPPORT;
    }
}
