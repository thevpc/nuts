//package net.thevpc.nuts.runtime.standalone;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.runtime.core.format.DefaultNutsObjectFormat;
//import net.thevpc.nuts.runtime.core.format.props.DefaultNutsPropertiesFormat;
//import net.thevpc.nuts.runtime.core.format.tree.DefaultNutsTreeFormat;
//import net.thevpc.nuts.runtime.core.format.table.DefaultTableFormat;
//import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManagerModel;
//import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
//
//public class DefaultNutsFormatManager implements NutsFormatManager {
//
//    private NutsWorkspace ws;
//    private NutsSession session;
//    private DefaultNutsTextManagerModel model;
//
//    public DefaultNutsFormatManager(NutsWorkspace ws, DefaultNutsTextManagerModel model) {
//        this.ws = ws;
//        this.model = model;
//    }
//
//    public NutsSession getSession() {
//        return session;
//    }
//
//    public NutsFormatManager setSession(NutsSession session) {
//        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
//        return this;
//    }
//
//}
