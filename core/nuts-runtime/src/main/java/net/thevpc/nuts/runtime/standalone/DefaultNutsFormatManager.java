package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.format.DefaultNutsIncrementalOutputFormat;
import net.thevpc.nuts.runtime.standalone.format.DefaultNutsObjectFormat;
import net.thevpc.nuts.runtime.standalone.format.DefaultNutsStringFormat;
import net.thevpc.nuts.runtime.standalone.format.elem.DefaultNutsElementFormat;
import net.thevpc.nuts.runtime.standalone.format.text.DefaultNutsTextFormatManager;
import net.thevpc.nuts.runtime.standalone.format.tree.DefaultTreeFormat;
import net.thevpc.nuts.runtime.standalone.format.props.DefaultPropertiesFormat;
import net.thevpc.nuts.runtime.standalone.format.table.DefaultTableFormat;

public class DefaultNutsFormatManager implements NutsFormatManager {
    private NutsWorkspace ws;

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
    public NutsTextFormatManager text() {
        return new DefaultNutsTextFormatManager(ws);
    }

    @Override
    public NutsIterableOutput iter() {
        return new DefaultNutsIncrementalOutputFormat(ws);
    }

    @Override
    public NutsFormat of(NutsFormattable any) {
        if(any instanceof NutsId){
            return ws.id().formatter((NutsId) any);
        }
        if(any instanceof NutsDependency){
            return ws.dependency().formatter((NutsDependency) any);
        }
        if(any instanceof NutsVersion){
            return ws.version().formatter((NutsVersion) any);
        }
        if(any instanceof NutsDescriptor){
            return ws.descriptor().formatter((NutsDescriptor) any);
        }
        if(any instanceof NutsCommandLine){
            return ws.commandLine().formatter((NutsCommandLine) any);
        }
        throw new NutsIllegalArgumentException(ws,"Unsupported formattable "+((any==null)?"null":any.getClass().getName()));
    }


}
