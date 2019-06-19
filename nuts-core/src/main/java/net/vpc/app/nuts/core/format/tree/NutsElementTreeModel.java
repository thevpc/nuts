package net.vpc.app.nuts.core.format.tree;

import net.vpc.app.nuts.NutsTreeModel;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

import java.util.*;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsElement;
import net.vpc.app.nuts.NutsElementType;
import net.vpc.app.nuts.NutsNamedElement;

class NutsElementTreeModel implements NutsTreeModel {

    private final XNode root;
    private final NutsWorkspace ws;
    private final NutsSession session;

    public NutsElementTreeModel(NutsWorkspace ws, String rootName, NutsElement data, NutsSession session) {
        this.ws = ws;
        this.session = session;
        this.root = new XNode(null, data,data.type().isPrimitive()?null: rootName);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public List getChildren(Object o) {
        return ((XNode) o).getChildren();
    }

    protected String[] getMultilineArray(String key, NutsElement value) {
        return null;
    }

    class XNode {

        String key;
        NutsElement value;
        String title;

        public XNode(String key, NutsElement value, String title) {
            this.key = key;
            this.value = value;
            this.title = title;
        }

        @Override
        public String toString() {
            String[] p = getMultilineArray(stringValue(key), value);
            if (p != null) {
                return stringValue(key);
            }
            String _title = resolveTitle();
            if (key == null) {
                return stringValue(_title != null ? _title : value);
            } else {
                if (value.type() == NutsElementType.ARRAY || value.type() == NutsElementType.OBJECT) {
                    return "==" + stringValue(key) + "==";
                } else {
                    return "==" + stringValue(key) + "=="
                            + "\\=" + stringValue(_title != null ? _title : value);
                }
            }
        }

        private String resolveTitle() {
            if (title != null) {
                return title;
            }
            switch (this.value.type()) {
                case ARRAY: {
                    return "";
                }
                case OBJECT: {
                    NutsElement bestElement = null;
                    int bestKeyOrder = -1;
                    for (NutsNamedElement me : this.value.object().children()) {
                        int keyOrder = -1;
                        switch (me.getName()) {
                            case "id": {
                                keyOrder = 1;
                                break;
                            }
                            case "name": {
                                keyOrder = 10;
                                break;
                            }
                            case "title": {
                                keyOrder = 2;
                                break;
                            }
                            case "label": {
                                keyOrder = 3;
                                break;
                            }
                        }
                        if (keyOrder > bestKeyOrder) {
                            bestKeyOrder = keyOrder;
                            bestElement = me.getValue();
                        }
                    }
                    if (bestKeyOrder >= 0) {
                        return stringValue(bestElement);
                    }
                    break;
                }
            }
            return null;
        }

        public List getChildren() {
            switch (this.value.type()) {
                case ARRAY: {
                    List<XNode> all = new ArrayList<>();
                    for (NutsElement me : this.value.array().children()) {
                        all.add(new XNode(null, me, null));
                    }
                    return all;
                }
                case OBJECT: {
                    List<XNode> all = new ArrayList<>();
                    for (NutsNamedElement me : this.value.object().children()) {
                        String[] map = getMultilineArray(stringValue(me.getName()), me.getValue());
                        if (map == null) {
                            all.add(new XNode(stringValue(me.getName()), me.getValue(), null));
                        } else {
                            all.add(new XNode(stringValue(me.getName()),
                                    ws.format().element().toElement(Arrays.asList(map)),
                                    null));
                        }
                    }
                    return all;
                }
                default: {
                    return null;
                }
            }
        }
    }

    public String stringValue(Object o) {
        String a = CoreCommonUtils.stringValueFormatted(o, false, session);
        return a;
    }
}
