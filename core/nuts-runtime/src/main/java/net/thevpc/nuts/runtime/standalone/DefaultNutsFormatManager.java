package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.DefaultNutsObjectFormat;
import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsElementFormat;
import net.thevpc.nuts.runtime.core.format.tree.DefaultTreeFormat;
import net.thevpc.nuts.runtime.core.format.props.DefaultPropertiesFormat;
import net.thevpc.nuts.runtime.core.format.table.DefaultTableFormat;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManager;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManagerModel;

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
        this.session = session;
        return this;
    }

//    @Override
//    public NutsJsonFormat json() {
//        return new DefaultNutsJsonFormat(ws);
//    }
//
//    @Override
//    public NutsXmlFormat xml() {
//        return new DefaultNutsXmlFormat(ws);
//    }
    @Override
    public NutsElementFormat element() {
        return new DefaultNutsElementFormat(model).setSession(getSession());
    }
//
//    @Override
//    public NutsStringFormat str() {
//        return new DefaultNutsStringFormat(ws);
//    }

    @Override
    public NutsTreeFormat tree() {
        return new DefaultTreeFormat(ws).setSession(getSession());
    }

    @Override
    public NutsTableFormat table() {
        return new DefaultTableFormat(ws).setSession(getSession());
    }

    @Override
    public NutsElementFormat element(Object value) {
        return element().setValue(value);
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
        return new DefaultPropertiesFormat(ws).setSession(getSession());
    }

    @Override
    public NutsObjectFormat object(Object value) {
        return object().setValue(value);
    }

    @Override
    public NutsObjectFormat object() {
        return new DefaultNutsObjectFormat(ws).setSession(getSession());
    }

}
