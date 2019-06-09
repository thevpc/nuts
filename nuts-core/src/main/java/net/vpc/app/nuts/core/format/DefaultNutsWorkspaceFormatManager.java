package net.vpc.app.nuts.core.format;

import net.vpc.app.nuts.core.format.elem.DefaultNutsElementFormat;
import net.vpc.app.nuts.core.format.props.DefaultPropertiesFormat;
import net.vpc.app.nuts.core.format.table.DefaultTableFormat;
import net.vpc.app.nuts.core.format.tree.DefaultTreeFormat;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsDescriptorFormat;
import net.vpc.app.nuts.core.format.json.DefaultNutsJsonFormat;
import net.vpc.app.nuts.core.format.xml.DefaultNutsXmlFormat;

public class DefaultNutsWorkspaceFormatManager implements NutsFormatManager {

    private final NutsWorkspace ws;

    public DefaultNutsWorkspaceFormatManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsJsonFormat json() {
        return new DefaultNutsJsonFormat(ws);
    }

    @Override
    public NutsElementFormat element() {
        return new DefaultNutsElementFormat(ws);
    }
    
    @Override
    public NutsXmlFormat xml() {
        return new DefaultNutsXmlFormat(ws);
    }

    @Override
    public NutsIdFormat id() {
        return new DefaultNutsIdFormat(ws);
    }

    @Override
    public NutsWorkspaceVersionFormat version() {
        return new DefaultVersionFormat(ws);
    }

    @Override
    public NutsWorkspaceInfoFormat info() {
        return new DefaultInfoFormat(ws);
    }

    @Override
    public NutsDescriptorFormat descriptor() {
        return new DefaultNutsDescriptorFormat(ws);
    }

    @Override
    public NutsIncrementalFormat iter() {
        return new DefaultNutsIncrementalOutputFormat(ws);
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
    public NutsTreeFormat tree() {
        return new DefaultTreeFormat(ws);
    }

    @Override
    public NutsObjectFormat object() {
        return new DefaultNutsObjectFormat(ws);
    }

}
