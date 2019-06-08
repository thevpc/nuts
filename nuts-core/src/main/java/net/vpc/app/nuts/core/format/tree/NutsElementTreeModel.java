package net.vpc.app.nuts.core.format.tree;

import net.vpc.app.nuts.NutsTreeModel;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

import java.util.*;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsElement;
import net.vpc.app.nuts.NutsNamedElement;

class NutsElementTreeModel implements NutsTreeModel {

    private final XNode root;
    private final NutsWorkspace ws;
    private final NutsSession session;

    public NutsElementTreeModel(NutsWorkspace ws, String rootName, NutsElement data, NutsSession session) {
        this.ws = ws;
        this.session = session;
        this.root = new XNode(rootName, data);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public List getChildren(Object o) {
        XNode t = (XNode) o;
        switch (t.value.type()) {
            case ARRAY: {
                List<XNode> all = new ArrayList<>();
                for (NutsElement me : t.value.array().children()) {
                    all.add(new XNode(me));
                }
                return all;
            }
            case OBJECT: {
                List<XNode> all = new ArrayList<>();
                for (NutsNamedElement me : t.value.object().children()) {
                    String[] map = getMultilineArray(stringValue(me.getName()), me.getValue());
                    if (map == null) {
                        all.add(new XNode(stringValue(me.getName()), me.getValue()));
                    } else {
                        all.add(new XNode(stringValue(me.getName()),
                                ws.format().element().toElement(Arrays.asList(map))
                        ));
                    }
                }
                return all;
            }
            default: {
                return null;
            }
        }
    }

    protected String[] getMultilineArray(String key, NutsElement value) {
        return null;
    }

    class XNode {

        String name;
        NutsElement value;
        boolean noName = false;

        public XNode(NutsElement value) {
            this.value = value;
            noName = true;
        }

        public XNode(String name, NutsElement value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            if (value instanceof Map) {
                return ws.io().getTerminalFormat().escapeText(stringValue(name));
            } else if (value instanceof Collection) {
                return ws.io().getTerminalFormat().escapeText(stringValue(name));
            } else {
                String[] p = getMultilineArray(stringValue(name), value);
                if (p != null) {
                    return ws.io().getTerminalFormat().escapeText(stringValue(name));
                }
                if (noName) {
                    return ws.io().getTerminalFormat().escapeText(stringValue(value));
                } else {
                    return "==" + ws.io().getTerminalFormat().escapeText(stringValue(name)) + "==" + "\\=" + ws.io().getTerminalFormat().escapeText(stringValue(value));
                }
            }
        }
    }

    public String stringValue(Object o) {
        return CoreCommonUtils.stringValueFormatted(o, ws, session);
    }
}
