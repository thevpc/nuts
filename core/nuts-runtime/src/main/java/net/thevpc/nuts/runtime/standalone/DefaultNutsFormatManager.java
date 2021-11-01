package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.DefaultNutsObjectFormat;
import net.thevpc.nuts.runtime.core.format.props.DefaultNutsPropertiesFormat;
import net.thevpc.nuts.runtime.core.format.tree.DefaultNutsTreeFormat;
import net.thevpc.nuts.runtime.core.format.table.DefaultTableFormat;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManagerModel;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsFormatManager implements NutsFormatManager {

    private NutsWorkspace ws;
    private NutsSession session;
    private DefaultNutsTextManagerModel model;

    public DefaultNutsFormatManager(NutsWorkspace ws, DefaultNutsTextManagerModel model) {
        this.ws = ws;
        this.model = model;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsFormatManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    @Override
    public NutsTreeFormat tree() {
        return new DefaultNutsTreeFormat(getSession());
    }

    @Override
    public NutsTableFormat table() {
        return new DefaultTableFormat(getSession());
    }

    @Override
    public NutsTableFormat table(Object value) {
        return table().setValue(value);
    }

    @Override
    public NutsTreeFormat tree(Object value) {
        return tree().setValue(value);
    }

    @Override
    public NutsPropertiesFormat props(Object value) {
        return props().setValue(value);
    }

    @Override
    public NutsPropertiesFormat props() {
        return new DefaultNutsPropertiesFormat(getSession());
    }

    @Override
    public NutsObjectFormat object(Object value) {
        return object().setValue(value);
    }

    @Override
    public NutsObjectFormat object() {
        return new DefaultNutsObjectFormat(getSession());
    }

}
