package net.thevpc.nuts.runtime.standalone.util.filters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NFilter;

public class DefaultNFilters implements NFilters {
    public DefaultNFilterModel model;
    public DefaultNFilters() {
        this.model = NWorkspaceExt.of().getModel().filtersModel;
    }

    public DefaultNFilterModel getModel() {
        return model;
    }

    @Override
    public <T extends NFilter> T nonnull(Class<T> type, NFilter filter) {
        return model.nonnull(type, filter);
    }


    @Override
    public <T extends NFilter> T always(Class<T> type) {
        return model.always(type);
    }

    @Override
    public <T extends NFilter> T never(Class<T> type) {
        return model.never(type);
    }

    @Override
    public <T extends NFilter> T all(Class<T> type, NFilter... others) {
        return model.all(type,others);
    }

    @Override
    public <T extends NFilter> T all(NFilter... others) {
        return model.all(others);
    }

    @Override
    public <T extends NFilter> T any(Class<T> type, NFilter... others) {
        return model.any(type,others);
    }

    @Override
    public <T extends NFilter> T not(NFilter other) {
        return model.not(other);
    }

    @Override
    public <T extends NFilter> T not(Class<T> type, NFilter other) {
        return model.not(type,other);
    }

    @Override
    public <T extends NFilter> T any(NFilter... others) {
        return model.any(others);
    }

    @Override
    public <T extends NFilter> T none(Class<T> type, NFilter... others) {
        return model.none(type,others);
    }

    @Override
    public <T extends NFilter> T none(NFilter... others) {
        return model.none(others);
    }

    @Override
    public <T extends NFilter> T to(Class<T> toFilterInterface, NFilter filter) {
        return model.to(toFilterInterface,filter);
    }

    public <T extends NFilter> T as(Class<T> toFilterInterface, NFilter filter) {
        return model.as(toFilterInterface,filter);
    }

    @Override
    public Class<? extends NFilter> detectType(NFilter nFilter) {
        return model.detectType(nFilter);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
