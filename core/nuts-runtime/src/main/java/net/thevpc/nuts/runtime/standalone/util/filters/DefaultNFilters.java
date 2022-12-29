package net.thevpc.nuts.runtime.standalone.util.filters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class DefaultNFilters implements NFilters {
    public DefaultNFilterModel model;
    public NSession session;
    public DefaultNFilters(NSession session) {
        this.model = NWorkspaceExt.of(session.getWorkspace()).getModel().filtersModel;
        this.session=session;
    }

    public DefaultNFilterModel getModel() {
        return model;
    }

    @Override
    public <T extends NFilter> T nonnull(Class<T> type, NFilter filter) {
        checkSession();
        return model.nonnull(type, filter,session);
    }

    private void checkSession() {
        NSessionUtils.checkSession(model.getWorkspace(),session);
    }

    @Override
    public <T extends NFilter> T always(Class<T> type) {
        checkSession();
        return model.always(type,session);
    }

    @Override
    public <T extends NFilter> T never(Class<T> type) {
        checkSession();
        return model.never(type,session);
    }

    @Override
    public <T extends NFilter> T all(Class<T> type, NFilter... others) {
        checkSession();
        return model.all(type,others,session);
    }

    @Override
    public <T extends NFilter> T all(NFilter... others) {
        checkSession();
        return model.all(others,session);
    }

    @Override
    public <T extends NFilter> T any(Class<T> type, NFilter... others) {
        checkSession();
        return model.any(type,others,session);
    }

    @Override
    public <T extends NFilter> T not(NFilter other) {
        checkSession();
        return model.not(other,session);
    }

    @Override
    public <T extends NFilter> T not(Class<T> type, NFilter other) {
        checkSession();
        return model.not(type,other,session);
    }

    @Override
    public <T extends NFilter> T any(NFilter... others) {
        checkSession();
        return model.any(others,session);
    }

    @Override
    public <T extends NFilter> T none(Class<T> type, NFilter... others) {
        checkSession();
        return model.none(type,others,session);
    }

    @Override
    public <T extends NFilter> T none(NFilter... others) {
        checkSession();
        return model.none(others,session);
    }

    @Override
    public <T extends NFilter> T to(Class<T> toFilterInterface, NFilter filter) {
        checkSession();
        return model.to(toFilterInterface,filter,session);
    }

    public <T extends NFilter> T as(Class<T> toFilterInterface, NFilter filter) {
        checkSession();
        return model.as(toFilterInterface,filter,session);
    }

    @Override
    public Class<? extends NFilter> detectType(NFilter nFilter) {
        checkSession();
        return model.detectType(nFilter,session);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
