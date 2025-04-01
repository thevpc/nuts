package net.thevpc.nuts.runtime.standalone.format.tree;

import net.thevpc.nuts.format.NTreeModel;
import net.thevpc.nuts.text.NText;

import java.util.List;
import java.util.Map;

class NElementTreeModel implements NTreeModel {

    private final XNode root;

    public NElementTreeModel(NText rootName, Object destructredObject, XNodeFormatter format) {
        this.root = new XNode(null, destructredObject, 
                ((destructredObject instanceof List) || (destructredObject instanceof Map))?rootName:null, format);
    }

    public NElementTreeModel(XNode node) {
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
