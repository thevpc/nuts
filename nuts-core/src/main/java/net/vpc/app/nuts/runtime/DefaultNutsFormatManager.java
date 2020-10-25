package net.vpc.app.nuts.runtime;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.format.DefaultNutsIncrementalOutputFormat;
import net.vpc.app.nuts.runtime.format.DefaultNutsObjectFormat;
import net.vpc.app.nuts.runtime.format.DefaultNutsStringFormat;
import net.vpc.app.nuts.runtime.format.elem.DefaultNutsElementFormat;
import net.vpc.app.nuts.runtime.format.json.DefaultNutsJsonFormat;
import net.vpc.app.nuts.runtime.format.props.DefaultPropertiesFormat;
import net.vpc.app.nuts.runtime.format.table.DefaultTableFormat;
import net.vpc.app.nuts.runtime.format.tree.DefaultTreeFormat;
import net.vpc.app.nuts.runtime.format.xml.DefaultNutsXmlFormat;

public class DefaultNutsFormatManager implements NutsFormatManager {
    private NutsWorkspace ws;

    public DefaultNutsFormatManager(NutsWorkspace ws) {
        this.ws = ws;
    }


    @Override
    public NutsJsonFormat json() {
        return new DefaultNutsJsonFormat(ws);
    }

    @Override
    public NutsXmlFormat xml() {
        return new DefaultNutsXmlFormat(ws);
    }

    @Override
    public NutsElementFormat element() {
        return new DefaultNutsElementFormat(ws);
    }
    @Override
    public NutsStringFormat str() {
        return new DefaultNutsStringFormat(ws);
    }


    @Override
    public NutsTreeFormat tree() {
        return new DefaultTreeFormat(ws);
    }

    @Override
    public NutsTableFormat table() {
        return new DefaultTableFormat(ws);
    }

    @Override
    public NutsPropertiesFormat props() {
        return new DefaultPropertiesFormat(ws);
    }

    @Override
    public NutsObjectFormat object() {
        return new DefaultNutsObjectFormat(ws);
    }

    @Override
    public NutsIterableOutput iter() {
        return new DefaultNutsIncrementalOutputFormat(ws);
    }

}
