package net.thevpc.nuts.runtime.standalone;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.DefaultNutsIncrementalOutputFormat;
import net.thevpc.nuts.runtime.core.format.DefaultNutsObjectFormat;
//import net.thevpc.nuts.runtime.core.format.DefaultNutsStringFormat;
import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsElementFormat;
import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsElementFormatHelper;
import net.thevpc.nuts.runtime.core.format.tree.DefaultTreeFormat;
import net.thevpc.nuts.runtime.core.format.props.DefaultPropertiesFormat;
import net.thevpc.nuts.runtime.core.format.table.DefaultTableFormat;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManager;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManagerShared;
import net.thevpc.nuts.runtime.core.format.text.bloc.HadraBlocTextFormatter;
import net.thevpc.nuts.runtime.core.format.text.bloc.JavaBlocTextFormatter;
import net.thevpc.nuts.runtime.core.format.text.bloc.JsonCodeFormatter;
import net.thevpc.nuts.runtime.core.format.text.bloc.PlainBlocTextFormatter;
import net.thevpc.nuts.runtime.core.format.text.bloc.ShellBlocTextFormatter;
import net.thevpc.nuts.runtime.core.format.text.bloc.XmlCodeFormatter;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.DefaultNutsTextFormatTheme;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.NutsTextFormatPropertiesTheme;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.NutsTextFormatThemeWrapper;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.spi.NutsComponent;

public class DefaultNutsFormatManager implements NutsFormatManager {

    private NutsWorkspace ws;
    private DefaultNutsElementFormatHelper defaultNutsElementFormatHelper;
    private DefaultNutsTextManagerShared defaultNutsTextManagerShared;

    public DefaultNutsFormatManager(NutsWorkspace ws, NutsWorkspaceInitInformation info) {
        this.ws = ws;
        defaultNutsTextManagerShared=new DefaultNutsTextManagerShared(ws, info);
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
        if (defaultNutsElementFormatHelper == null) {
            defaultNutsElementFormatHelper = new DefaultNutsElementFormatHelper(ws);
        }
        return new DefaultNutsElementFormat(defaultNutsElementFormatHelper);
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
    public NutsIterableOutput iter() {
        return new DefaultNutsIncrementalOutputFormat(ws);
    }


    @Override
    public NutsTextManager text() {
        return new DefaultNutsTextManager(ws, defaultNutsTextManagerShared);
    }

    
}
