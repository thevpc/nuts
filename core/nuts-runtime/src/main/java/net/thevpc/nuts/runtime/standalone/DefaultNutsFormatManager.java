package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.DefaultNutsIncrementalOutputFormat;
import net.thevpc.nuts.runtime.core.format.DefaultNutsObjectFormat;
//import net.thevpc.nuts.runtime.core.format.DefaultNutsStringFormat;
import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsElementFormat;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextFormatManager;
import net.thevpc.nuts.runtime.core.format.tree.DefaultTreeFormat;
import net.thevpc.nuts.runtime.core.format.props.DefaultPropertiesFormat;
import net.thevpc.nuts.runtime.core.format.table.DefaultTableFormat;

public class DefaultNutsFormatManager implements NutsFormatManager {
    private NutsWorkspace ws;
    private DefaultNutsTextFormatManager txt;

    public DefaultNutsFormatManager(NutsWorkspace ws) {
        this.ws = ws;
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
        return new DefaultNutsElementFormat(ws);
    }
//
//    @Override
//    public NutsStringFormat str() {
//        return new DefaultNutsStringFormat(ws);
//    }

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
    public NutsTextFormatManager text() {
        if(txt==null){
            txt=new DefaultNutsTextFormatManager(ws);
        }
        return txt;
    }

    @Override
    public NutsIterableOutput iter() {
        return new DefaultNutsIncrementalOutputFormat(ws);
    }
}
