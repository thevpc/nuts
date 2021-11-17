package net.thevpc.nuts.runtime.standalone.format.tree;

import net.thevpc.nuts.*;

import java.util.List;
import java.util.Map;

class NutsElementTreeModel implements NutsTreeModel {

    private final XNode root;

    public NutsElementTreeModel(NutsWorkspace ws, NutsString rootName, Object destructredObject, NutsSession session, XNodeFormatter format) {
        this.root = new XNode(null, destructredObject, 
                ((destructredObject instanceof List) || (destructredObject instanceof Map))?rootName:null, session, format);
    }

    public NutsElementTreeModel(XNode node) {
        this.root = node;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public List getChildren(Object o) {
        return ((XNode) o).getChildren();
    }

}
